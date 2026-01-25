# Kanban Backlog Column Implementation

## Overview

This document describes the implementation of `CComponentKanbanColumnBacklog`, a specialized kanban column that displays the project backlog and enables drag-drop operations between the backlog and sprint kanban columns.

## Components Created/Modified

### New Components

#### 1. CComponentKanbanColumnBacklog
**Location**: `src/main/java/tech/derbent/app/kanban/kanbanline/view/CComponentKanbanColumnBacklog.java`

A specialized kanban column that extends `CComponentKanbanColumn` to display project backlog items.

**Key Features**:
- Embeds `CComponentBacklog` for displaying items not in sprints
- Compact mode toggle for narrow/wide display (220px vs 320px)
- Drag source for backlog items (adds to sprint when dropped on kanban column)
- Drop target for sprint items (removes from sprint when dropped on backlog)
- Automatically created as first column in kanban board

**Public Methods**:
- `CComponentKanbanColumnBacklog(CProject project)` - Constructor requiring project context
- `setCompactMode(boolean compact)` - Toggle between normal and narrow display
- `isCompactMode()` - Check current mode
- `getBacklogComponent()` - Access embedded backlog component
- `refreshBacklog()` - Reload backlog items from database

### Modified Components

#### 2. CComponentKanbanBoard
**Location**: `src/main/java/tech/derbent/app/kanban/kanbanline/view/CComponentKanbanBoard.java`

**Changes**:
- Added `backlogColumn` field to track the backlog column instance
- Added `createBacklogColumn(CProject)` method to create and configure backlog column
- Modified `refreshComponent()` to create backlog column as first column when sprint is selected
- Added `getBacklogColumn()` getter for page service access
- Added `getCurrentSprint()` getter to provide sprint context for drag-drop operations

#### 3. CPageServiceKanbanLine
**Location**: `src/main/java/tech/derbent/app/kanban/kanbanline/service/CPageServiceKanbanLine.java`

**Changes**:
- Refactored `handleKanbanDrop()` to distinguish between backlog and column drops
- Added `isDropOnBacklog()` to detect drops onto backlog column
- Added `handleDropOnBacklog()` to remove sprint items from sprint
- Added `handleDragFromBacklog()` to add backlog items to sprint
- Added `showStatusSelectionDialogForBacklog()` for user status selection
- Extracted `handleDragBetweenColumns()` from original kanban drop logic
- All methods include comprehensive status resolution with workflow validation

#### 4. ISprintItemRepository & CSprintItemService
**Location**: `src/main/java/tech/derbent/app/sprints/service/`

**Changes**:
- Added `getNextItemOrder(CSprint master)` query method to repository
- Added `getNextItemOrder(CSprint master)` service method for proper item ordering

## Drag-Drop Workflows

### 1. Drag from Backlog to Kanban Column (Add to Sprint)

**Flow**:
1. User drags a backlog item (CProjectItem) onto a kanban column
2. `handleDragFromBacklog()` is called
3. Creates new `CSprintItem` linking the project item to current sprint
4. Resolves valid statuses for target column using workflow rules
5. Three cases:
   - **No valid status**: Adds to sprint, warns user, status unchanged
   - **One valid status**: Automatically applies status, adds to sprint
   - **Multiple valid statuses**: Shows selection dialog, user chooses status
6. Saves sprint item and refreshes both board and backlog

**Code**:
```java
// In CPageServiceKanbanLine.handleDragFromBacklog()
final CSprintItem newSprintItem = new CSprintItem();
newSprintItem.setSprint(currentSprint);
newSprintItem.setItemType(projectItem.getClass().getSimpleName());
newSprintItem.setItemId(projectItem.getId());
newSprintItem.setKanbanColumnId(targetColumn.getId());
newSprintItem.setItemOrder(sprintItemService.getNextItemOrder(currentSprint));
```

### 2. Drop on Backlog Column (Remove from Sprint)

**Flow**:
1. User drags a sprint item (CSprintItem) onto the backlog column
2. `handleDropOnBacklog()` is called
3. Deletes the sprint item (removes sprint membership)
4. Project item returns to backlog (no longer in any sprint)
5. Refreshes both board and backlog

**Code**:
```java
// In CPageServiceKanbanLine.handleDropOnBacklog()
sprintItemService.delete(sprintItem);
componentKanbanBoard.reloadSprintItems();
componentKanbanBoard.refreshComponent();
backlogColumn.refreshBacklog();
```

### 3. Drag Between Kanban Columns (Existing Functionality)

**Flow**: Unchanged from original implementation
1. User drags sprint item between kanban columns
2. `handleDragBetweenColumns()` is called (extracted from original logic)
3. Updates kanban column assignment
4. Resolves status based on workflow rules (same as backlog to column)

### 4. Backlog Internal Reordering

**Flow**: Handled by `CComponentBacklog` (no changes needed)
- Drag-drop within backlog grid updates `sprintOrder` field
- Used by `listForProjectBacklog()` methods for display ordering

## Status Resolution Algorithm

All drag-drop operations that change status use the same workflow-aware resolution:

```java
// 1. Get statuses mapped to target column
final List<CProjectItemStatus> targetStatuses =
    statusService.resolveStatusesForColumn(targetColumn, item);

// 2. Handle based on number of valid statuses
if (targetStatuses.isEmpty()) {
    // Save without status change + warning
} else if (targetStatuses.size() == 1) {
    // Automatically apply single status
} else {
    // Show dialog for user to select status
}
```

This ensures:
- Workflow rules are always respected
- Users can choose when multiple transitions exist
- Items can be moved even without valid transitions (with warning)

## Event Propagation Pattern

The implementation follows the existing event propagation pattern:

```
Component → drag_checkEventBeforePass() → Notify Listeners → 
→ Page Service Handler → Business Logic → 
→ drag_checkEventAfterPass() → Refresh Components
```

**Example**: Backlog column marks itself as drop target:
```java
@Override
public void drag_checkEventBeforePass(final CEvent event) {
    if (event instanceof CDragDropEvent dropEvent) {
        if (dropEvent.getTargetItem() == null) {
            dropEvent.setTargetItem(this);  // Mark as backlog drop
        }
    }
}
```

## Compact Mode Implementation

Backlog column supports two display modes via toggle button:

- **Normal Mode**: 320px width - full backlog grid display
- **Compact Mode**: 220px width - narrow display for space-constrained boards

```java
public void setCompactMode(final boolean compact) {
    this.compactMode = compact;
    if (compact) {
        setWidth("220px");
        buttonCompactMode.setIcon(VaadinIcon.EXPAND.create());
    } else {
        setWidth("320px");
        buttonCompactMode.setIcon(VaadinIcon.COMPRESS.create());
    }
}
```

## Integration Points

### Sprint Context Requirement

The backlog column requires a sprint to be selected for drag-drop to work:
- Sprint selection is handled by `CComponentKanbanBoardFilterToolbar`
- Board tracks `currentSprint` field
- Page service accesses via `componentKanbanBoard.getCurrentSprint()`

**Important**: If no sprint is selected, drag from backlog will show error:
```java
Check.notNull(currentSprint, "No sprint selected - cannot add backlog item to sprint");
```

### Refresh Mechanisms

After any drag-drop operation, multiple components refresh:

```java
// Reload sprint items from database
componentKanbanBoard.reloadSprintItems();
// Refresh kanban board UI
componentKanbanBoard.refreshComponent();
// Refresh backlog grid
if (backlogColumn != null) {
    backlogColumn.refreshBacklog();
}
```

This ensures:
- Sprint items show latest data from database
- Backlog shows items removed from/added to sprint
- No stale data is displayed

## Testing Scenarios

### Manual Testing Checklist

1. **Backlog to Column**:
   - [ ] Drag backlog item to column with one valid status → Auto-applies status
   - [ ] Drag backlog item to column with multiple statuses → Shows dialog
   - [ ] Drag backlog item to column with no valid statuses → Warns, adds anyway
   - [ ] Verify item removed from backlog after successful add

2. **Column to Backlog**:
   - [ ] Drag sprint item to backlog → Removes from sprint
   - [ ] Verify item appears in backlog after removal
   - [ ] Verify sprint item deleted from database

3. **Column to Column**:
   - [ ] Existing kanban drag-drop still works
   - [ ] Status resolution works as before

4. **Compact Mode**:
   - [ ] Toggle compact mode button
   - [ ] Width changes correctly (320px → 220px)
   - [ ] Backlog grid remains functional in both modes

5. **Error Cases**:
   - [ ] No sprint selected → Shows error when dragging from backlog
   - [ ] Invalid workflow transition → Shows warning, allows column assignment

### Automated Testing

Playwright tests should be added to:
- Navigate to kanban line view with sample data
- Select a sprint in filter toolbar
- Test drag from backlog to column
- Test drag from column to backlog
- Verify backlog column visibility and compact mode toggle

## Known Limitations

1. **Sprint Selection Required**: Backlog column only functional when sprint is selected
2. **Project Context**: Backlog column tied to project from selected sprint
3. **Database Profile**: Application requires proper H2/PostgreSQL configuration to start

## Future Enhancements

Potential improvements for future iterations:

1. **Default Sprint Selection**: Auto-select most recent sprint when opening kanban board
2. **Multi-Item Drag**: Support dragging multiple backlog items at once
3. **Backlog Filtering**: Add filters to backlog column (by type, status, assignee)
4. **Column Reordering**: Allow backlog column to be repositioned
5. **Swimlanes**: Support multiple backlog columns for different projects

## Code Style Compliance

Implementation follows Derbent coding standards:

- ✅ All custom classes prefixed with "C"
- ✅ UI components use `typeName` field naming (e.g., `buttonCompactMode`)
- ✅ Event handlers use `on_xxx_eventType` naming (e.g., `on_buttonCompactMode_clicked`)
- ✅ Factory methods use `create_xxx` naming (e.g., `create_buttonCompactMode`)
- ✅ Notifications use `CNotificationService` exclusively
- ✅ Validation uses `Check` utility for fail-fast
- ✅ Comprehensive JavaDoc documentation
- ✅ Logger statements at appropriate levels

## References

**Related Documentation**:
- `docs/architecture/coding-standards.md` - Derbent coding conventions
- `docs/implementation/drag-drop-patterns.md` - Drag-drop event system
- `docs/implementation/workflow-status-transitions.md` - Status resolution

**Related Classes**:
- `CComponentBacklog` - Embedded backlog component
- `CComponentKanbanColumn` - Parent class for regular columns
- `CComponentKanbanBoard` - Main kanban board component
- `CPageServiceSprint` - Sprint page service (backlog usage example)
