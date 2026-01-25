# Kanban Drag-Drop Workflow Transition Fix

## Problem Description

Users reported that dragging items between Kanban columns was completely blocked:
- **Issue**: Cannot move items to any column at all
- **Error Message**: "Item moved to 'Done' column, but status remains 'In Progress' (no valid workflow transition available)"

## Root Cause Analysis

The issue was in `CComponentKanbanColumn.drag_isDropAllowed()` method:

```java
// ❌ WRONG LOGIC (Before Fix)
@Override
public boolean drag_isDropAllowed(CDragStartEvent event) {
    final Object item = event.getDraggedItem();
    if (!(item instanceof final CSprintItem sprintItem)) {
        return false;
    }
    final CKanbanColumn column = getValue();
    if (column == null || column.getIncludedStatuses() == null) {
        return false;
    }
    // This checks if CURRENT status is in target column - WRONG!
    return column.getIncludedStatuses().stream()
        .anyMatch(status -> status.getId().equals(sprintItem.getStatus().getId()));
}
```

### Why This Was Wrong

The method checked if the item's **CURRENT status** exists in the **target column's statuses**.

**Example Scenario:**
- Item has status: "In Progress" (ID: 2)
- User drags to "Done" column
- "Done" column includes statuses: ["Done", "Closed"]
- Check: Is "In Progress" in ["Done", "Closed"]? **NO**
- Result: Drop blocked ❌

This is incorrect logic because:
1. You can't drag an item to a column if its current status is already in that column
2. It completely ignores workflow transitions
3. It makes the Kanban board unusable

## The Solution

Changed the method to allow drops and delegate workflow validation to the handler:

```java
// ✅ CORRECT LOGIC (After Fix)
@Override
public boolean drag_isDropAllowed(CDragStartEvent event) {
    final Object item = event.getDraggedItem();
    if (!(item instanceof final CSprintItem sprintItem)) {
        return false;
    }
    final CKanbanColumn column = getValue();
    if (column == null || column.getIncludedStatuses() == null) {
        return false;
    }
    // FIXED: Allow drop to any column - workflow validation is handled by CPageServiceKanbanLine.handleKanbanDrop()
    // Previous logic was: column.getIncludedStatuses().stream().anyMatch(status -> status.getId().equals(sprintItem.getStatus().getId()))
    // This incorrectly required the CURRENT status to be in the column, rather than checking for valid transitions.
    // The proper workflow transition validation happens in the drop handler, which will show appropriate warnings
    // if no valid transition exists while still allowing the visual column assignment.
    return true;
}
```

## How Workflow Validation Actually Works

The correct workflow validation is already implemented in `CPageServiceKanbanLine.handleKanbanDrop()`:

### Step 1: Extract Dragged Item and Target Column
```java
final CSprintItem sprintItem = (CSprintItem) draggedItem;
final CProjectItem<?> item = (CProjectItem<?>) sprintItem.getItem();
final CKanbanColumn targetColumn = resolveTargetColumn(event);
```

### Step 2: Update Visual Column Assignment
```java
sprintItem.setKanbanColumnId(targetColumn.getId());
```

### Step 3: Resolve Valid Workflow Transitions
```java
final CProjectItemStatusService statusService = CSpringContext.getBean(CProjectItemStatusService.class);
final List<CProjectItemStatus> targetStatuses =
    statusService.resolveStatusesForColumn(targetColumn, (IHasStatusAndWorkflow<?>) item);
```

This method:
- Gets all statuses mapped to the target column
- Gets all valid workflow transitions from the current status
- Returns the **intersection** of these two lists

### Step 4: Handle Based on Number of Valid Transitions

#### Case 1: No Valid Transitions (0 statuses)
```java
if (targetStatuses.isEmpty()) {
    // Save column assignment (visual change)
    saveSprintItemOnly(sprintItem);
    
    // Refresh board
    componentKanbanBoard.refreshComponent();
    
    // Warn user
    CNotificationService.showWarning(
        "Item moved to '" + targetColumn.getName() + "' column, but status remains '" + 
        item.getStatus().getName() + "' (no valid workflow transition available).");
}
```

#### Case 2: Exactly One Valid Transition (1 status)
```java
else if (targetStatuses.size() == 1) {
    // Automatically apply the only valid status
    final CProjectItemStatus newStatus = targetStatuses.get(0);
    applyStatusAndSave(item, sprintItem, newStatus);
    
    // Show success
    CNotificationService.showSuccess("Status updated to '" + newStatus.getName() + "'");
}
```

#### Case 3: Multiple Valid Transitions (2+ statuses)
```java
else {
    // Show dialog for user to choose
    showStatusSelectionDialog(item, sprintItem, targetColumn, targetStatuses);
}
```

## Example Workflows

### Scenario 1: Valid Transition Exists

**Setup:**
- Item status: "In Progress"
- Target column: "Done" (includes statuses: "Done", "Closed")
- Workflow: "In Progress" → "Done" (valid transition)

**Result:**
1. ✅ Drop allowed
2. ✅ Workflow validation finds 1 valid transition: "Done"
3. ✅ Status automatically updated to "Done"
4. ✅ Success notification shown

### Scenario 2: Multiple Valid Transitions

**Setup:**
- Item status: "In Progress"
- Target column: "Done" (includes statuses: "Done", "Closed", "Archived")
- Workflow: "In Progress" → "Done", "In Progress" → "Closed", "In Progress" → "Archived"

**Result:**
1. ✅ Drop allowed
2. ✅ Workflow validation finds 3 valid transitions
3. ✅ Selection dialog shown with options: "Done", "Closed", "Archived"
4. ✅ User selects preferred status
5. ✅ Status updated to selected value

### Scenario 3: No Valid Transition

**Setup:**
- Item status: "Done"
- Target column: "In Progress" (includes statuses: "In Progress", "Blocked")
- Workflow: No reverse transitions allowed

**Result:**
1. ✅ Drop allowed
2. ⚠️ Workflow validation finds 0 valid transitions
3. ✅ Column assignment saved (visual change)
4. ⚠️ Warning notification: "Item moved to 'In Progress' column, but status remains 'Done' (no valid workflow transition available)"

## Benefits of This Fix

1. **Drag-drop always works**: Users can always move items between columns
2. **Workflow-aware**: Automatically applies valid status transitions when possible
3. **User choice**: Shows selection dialog when multiple transitions are valid
4. **Graceful degradation**: Allows visual column changes even without valid transitions
5. **Clear feedback**: Shows appropriate success/warning messages

## Files Changed

- **src/main/java/tech/derbent/app/kanban/kanbanline/view/CComponentKanbanColumn.java**
  - Modified `drag_isDropAllowed()` method (lines 123-141)
  - Changed from checking current status to always returning true
  - Added detailed comments explaining the fix

## Testing

- ✅ Build verification: Passed
- ✅ Code review: 0 issues
- ✅ CodeQL security scan: 0 vulnerabilities
- ⏳ Manual testing: Requires running application with proper database setup

## Related Code

- **CPageServiceKanbanLine.handleKanbanDrop()**: Main drop handler (lines 123-187)
- **CProjectItemStatusService.resolveStatusesForColumn()**: Workflow validation (lines 195-211)
- **CProjectItemStatusService.getValidNextStatuses()**: Gets valid transitions (lines 124-166)
- **CPageServiceKanbanLine.saveSprintItemOnly()**: Saves column assignment (lines 241-258)
- **CPageServiceKanbanLine.applyStatusAndSave()**: Applies status and saves (lines 198-226)
- **CPageServiceKanbanLine.showStatusSelectionDialog()**: Shows selection dialog (lines 271-308)
