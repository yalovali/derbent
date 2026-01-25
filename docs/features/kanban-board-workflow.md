# Kanban Board Workflow and Status Management

## Overview

The kanban board system in Derbent provides a visual workflow management interface that integrates status-based workflows with drag-and-drop functionality. This document describes how the kanban board works, how statuses are mapped to columns, and the critical validation rules that ensure data integrity.

## Architecture Components

### 1. Domain Model

#### CKanbanLine
- Represents a kanban board configuration
- Contains multiple kanban columns (ordered collection)
- Belongs to a company (multi-tenant isolation)
- Used across all projects within a company

#### CKanbanColumn
- Represents a single column in a kanban board
- **includedStatuses**: List of statuses that should appear in this column
- **defaultColumn**: Boolean flag marking this as the fallback column for unmapped statuses
- **itemOrder**: Sort order for column display (left to right)
- **color**: Background color for visual differentiation

#### CSprintItem
- Links project items (Activities, Meetings, etc.) to sprints
- **kanbanColumnId**: Tracks which column displays this item
- Contains reference to the actual project item (ISprintableItem)

### 2. Status-to-Column Mapping

The kanban board uses a sophisticated mapping system to determine which column displays each project item:

```
Project Item → Status → Status-to-Column Map → Kanban Column → Display
```

#### Mapping Algorithm

1. **Build Status Map** (`prepareStatusToColumnIdMap`)
   - Creates a Map<Long, Long> where key = status ID, value = column ID
   - Iterates through all columns in the kanban line
   - For each column's includedStatuses, maps status ID → column ID
   - Special key -1L maps to the default column ID (if exists)
   - Uses `putIfAbsent()` to respect first mapping when overlap exists

2. **Assign Columns** (`assignKanbanColumns`)
   - For each sprint item:
     - Skip if kanbanColumnId already set (manual override)
     - Look up item's status ID in the status map
     - If found: assign corresponding column ID
     - If not found: assign default column ID (key -1L)
     - If no default and status not found: log warning, item not displayed

3. **Display Filtering** (`filterItems` in CComponentKanbanColumn)
   - Each column filters sprint items where `item.getKanbanColumnId() == column.getId()`
   - Only matching items are displayed as post-it cards in that column

### 3. Drag-and-Drop Workflow

When a user drags a post-it card from one column to another:

#### Step-by-Step Process

1. **Drag Start** (`CComponentKanbanPostit`)
   ```java
   dragSource.addDragStartListener(event -> {
       CDragStartEvent dragStartEvent = new CDragStartEvent(this, [sprintItem], true);
       notifyEvents(dragStartEvent);
   });
   ```
   - Post-it becomes draggable
   - Drag start event propagates up through column → board → page service
   - Page service stores active drag event for later reference

2. **Drop** (`CComponentKanbanColumn`)
   ```java
   dropTarget.addDropListener(event -> {
       CDragDropEvent dropEvent = new CDragDropEvent(id, this, null, null, true);
       notifyEvents(dropEvent);
   });
   ```
   - Drop event propagates to page service
   - Page service handles the business logic

3. **Status Resolution** (`CPageServiceKanbanLine.handleKanbanDrop`)
   ```java
   // Get statuses valid for target column
   List<CProjectItemStatus> targetStatuses = 
       statusService.resolveStatusesForColumn(targetColumn, item);
   
   // Intersects column statuses with workflow-valid transitions
   // Returns only statuses that are:
   // 1. Mapped to the target column (in includedStatuses)
   // 2. Valid transitions from current status (per workflow rules)
   ```

4. **Status Update and Save**
   - If valid statuses found: update item status to first valid status
   - If multiple valid statuses: use first one (TODO: add user selection)
   - If no valid statuses: show warning, column changes but status doesn't
   - Save updated item to database
   - Refresh kanban board to reflect changes

#### Drag-Drop Event Flow Diagram

```
CComponentKanbanPostit (drag start)
        ↓
CComponentKanbanColumn (propagate)
        ↓
CComponentKanbanBoard (propagate)
        ↓
CPageServiceKanbanLine (store active drag)
        ↓
CComponentKanbanColumn (drop)
        ↓
CComponentKanbanBoard (propagate)
        ↓
CPageServiceKanbanLine.handleKanbanDrop()
        ↓ (resolve target column)
        ↓ (resolve valid statuses)
        ↓ (update status)
        ↓ (save item)
        ↓ (refresh board)
```

## Critical Validation Rules

### Rule 1: Status Uniqueness Across Columns

**CRITICAL: Each status must be mapped to AT MOST ONE column within a kanban line.**

#### Why This Matters

If a status is mapped to multiple columns (status overlap), it creates:
1. **Display Ambiguity**: System cannot determine which column should display items with that status
2. **Drag-Drop Errors**: Incorrect column assignment during status changes
3. **Data Inconsistency**: Items could conceptually appear in multiple columns

#### Enforcement

Enforced at **two levels**:

**Level 1: Fail-Fast Validation** (`CKanbanColumnService.validateStatusUniqueness`)
- Runs during `validateEntity()` before save
- Checks if any status in the column being saved already exists in another column
- Throws `CValidationException` immediately if overlap detected
- Prevents invalid configuration from being saved to database

```java
// Example error message:
"Status overlap detected in kanban line 'Development Board': 
The following statuses are already mapped to other columns: 
'In Progress' (already in column 'Active'), 
'Testing' (already in column 'QA'). 
Each status must be mapped to exactly one column to avoid 
ambiguity in kanban board display."
```

**Level 2: Automatic Cleanup** (`CKanbanColumnService.applyStatusAndDefaultConstraints`)
- Runs after save as a safeguard
- If a column is saved with statuses, automatically removes those statuses from other columns
- Ensures database consistency even if validation is bypassed
- Logs all removals for debugging

```java
// Example log output:
[KanbanValidation] Removing 2 overlapping status(es) from column 
'Completed' (ID: 5) to maintain status uniqueness
[KanbanValidation] Enforced status uniqueness: removed 3 overlapping 
status mapping(s) from other columns in kanban line 'Sprint Board'
```

#### Debug Checking

For troubleshooting status mapping issues:

1. **Check status-to-column map building**:
   ```
   [KanbanValidation] Mapping status id 123:In Progress to column id 5 company id:1
   ```

2. **Check overlap detection**:
   ```
   [KanbanValidation] Status overlap detected: status 'Testing' (ID: 456) 
   is mapped to both column 'QA Column' and column 'Active Tasks'
   ```

3. **Check automatic cleanup**:
   ```
   [KanbanValidation] Removing 1 overlapping status(es) from column 
   'Old Column' (ID: 3) to maintain status uniqueness
   ```

### Rule 2: Single Default Column

**CRITICAL: Only one column can be marked as the default column within a kanban line.**

#### Purpose

The default column serves as a fallback for:
- Items whose status is not explicitly mapped to any column
- Items with null or unmapped statuses
- New items before status is set

#### Enforcement

When a column is saved with `defaultColumn=true`:
1. Automatically removes `defaultColumn=true` from all other columns in the line
2. Logged for debugging:
   ```
   [KanbanValidation] Removing default flag from column 'Backlog' (ID: 2) 
   because column 'Unmapped Items' (ID: 8) is now the default
   ```

## Common Patterns and Best Practices

### Pattern 1: Initial Status Mapping

When configuring a new kanban board:

1. Create columns matching your workflow stages
2. Map initial status to the first column (e.g., "To Do" → "Backlog")
3. Map intermediate statuses to middle columns (e.g., "In Progress" → "Active")
4. Map terminal statuses to final columns (e.g., "Done" → "Completed")
5. Create one default column for unmapped items (optional but recommended)

### Pattern 2: Status Transition Flow

Ensure columns respect workflow transitions:

```
Column: "Backlog"        → Statuses: ["To Do", "Planned"]
Column: "Active"         → Statuses: ["In Progress", "In Review"]
Column: "Blocked"        → Statuses: ["Blocked", "Waiting"]
Column: "Completed"      → Statuses: ["Done", "Closed"]
Column: "Unmapped" (default) → Catches all unmapped statuses
```

### Pattern 3: Multiple Statuses Per Column

A column CAN contain multiple statuses (common pattern):

```java
// Example: "Active Work" column shows both in-progress and in-review items
kanbanColumn.setIncludedStatuses(List.of(
    statusInProgress,
    statusInReview,
    statusTesting
));
```

This allows grouping related workflow stages into a single visual column.

### Pattern 4: Workflow-Aware Drag-Drop

The system automatically validates that drag-drop operations respect workflow rules:

```
Current Status: "To Do"
Drag to Column: "Completed" (contains "Done" status)

Workflow Check:
- Get valid transitions from "To Do" → ["In Progress"]
- Intersect with "Completed" column statuses → []
- Result: Drop rejected or status stays "To Do"
```

## Troubleshooting

### Issue: Item not appearing in any column

**Possible Causes:**
1. Item's status is not mapped to any column
2. No default column exists to catch unmapped statuses
3. Sprint item's kanbanColumnId is null or invalid

**Solution:**
1. Check logs for: `No kanban column found for status id X in line Y`
2. Map the status to a column or create a default column
3. Verify status exists and is active

### Issue: Item appears in wrong column after drag-drop

**Possible Causes:**
1. Multiple statuses mapped to same column
2. Workflow rules preventing status change
3. Sprint item kanbanColumnId not updated

**Solution:**
1. Check validation logs for status overlap warnings
2. Verify workflow allows transition to target column's statuses
3. Check if `resolveStatusesForColumn` returns empty list

### Issue: "Status overlap detected" error when saving column

**This is expected behavior!** The validation is working correctly.

**Solution:**
1. Remove the overlapping status from one of the columns
2. Decide which column should own each status
3. Update column's includedStatuses to remove overlap
4. Save again - should succeed

## API Reference

### Key Methods

#### CKanbanColumnService

```java
// Validates status uniqueness (fail-fast)
protected void validateEntity(CKanbanColumn entity)

// Enforces uniqueness after save (cleanup)
private void applyStatusAndDefaultConstraints(CKanbanColumn saved)

// Checks for status overlap
private void validateStatusUniqueness(CKanbanColumn entity)
```

#### CComponentKanbanBoard

```java
// Builds status → column mapping
Map<Long, Long> prepareStatusToColumnIdMap(List<CKanbanColumn> columns)

// Assigns columns to sprint items
private void assignKanbanColumns(List<CSprintItem> items, List<CKanbanColumn> columns)
```

#### CPageServiceKanbanLine

```java
// Handles drag-drop status changes
private void handleKanbanDrop(CDragDropEvent event)

// Resolves target column from drop event
private CKanbanColumn resolveTargetColumn(CDragDropEvent event)
```

### Key Domain Fields

```java
// CKanbanColumn
List<CProjectItemStatus> includedStatuses;  // Statuses displayed in this column
Boolean defaultColumn;                       // Fallback for unmapped statuses
Integer itemOrder;                           // Display order (left to right)

// CSprintItem
Long kanbanColumnId;                         // Which column displays this item

// CKanbanLine
Set<CKanbanColumn> kanbanColumns;           // All columns in this board
```

## Related Documentation

- [Workflow Status Change Pattern](../development/workflow-status-change-pattern.md)
- [Drag-Drop Component Pattern](../architecture/drag-drop-component-pattern.md)
- [Coding Standards](../architecture/coding-standards.md)

## Version History

- **v1.0** (2026-01-01): Initial documentation with status overlap validation
- **v1.0** (2026-01-01): Added drag-drop workflow documentation
- **v1.0** (2026-01-01): Added troubleshooting and best practices
