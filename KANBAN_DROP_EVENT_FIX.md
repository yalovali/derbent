# Kanban Drop Event UI Detachment Fix

## Problem Description
When dropping an entity on the backlog column of the Kanban board, the operation succeeds but throws an exception:
```
java.lang.IllegalStateException: Drop target received a drop event but not attached to an UI.
	at com.vaadin.flow.component.dnd.DropEvent.lambda$new$0(DropEvent.java:67)
```

## Root Cause Analysis

### Event Processing Timeline (BEFORE Fix)
1. User drops item on backlog column
2. Vaadin DropEvent is triggered
3. `drag_on_backlog_drop()` listener is called
4. Drop event is propagated to `CPageServiceKanbanLine.handleDropOnBacklog()`
5. Database operations complete successfully (item removed from sprint)
6. **PROBLEM**: `componentKanbanBoard.refreshComponent()` is called immediately
7. `refreshComponent()` calls `layoutColumns.removeAll()` which **detaches the backlog column from UI**
8. Control returns to Vaadin's drop event handler
9. **EXCEPTION**: Vaadin tries to access UI context of the now-detached backlog column

### Why This Happens
The issue is a timing problem with Vaadin's component lifecycle:
- Vaadin's DropEvent processing expects the drop target component to remain attached to the UI throughout the entire event handling process
- Our code was calling `refreshComponent()` which removes and recreates all columns (including the drop target)
- This detachment happened WHILE Vaadin was still finishing its drop event processing
- When Vaadin tried to complete the drop event (cleanup, notifications, etc.), the component was already detached

## Solution

### Defer UI Refresh Using UI.access()
Wrap all `refreshComponent()` and `reloadSprintItems()` calls in a deferred execution block:

```java
// BEFORE (causes exception)
componentKanbanBoard.reloadSprintItems();
componentKanbanBoard.refreshComponent();

// AFTER (fixed)
componentKanbanBoard.getUI().ifPresent(ui -> ui.access(() -> {
    componentKanbanBoard.reloadSprintItems();
    componentKanbanBoard.refreshComponent();
}));
```

### How UI.access() Solves the Problem
- `UI.access(Runnable)` defers the execution of the provided code until after the current server request completes
- This ensures Vaadin finishes processing the DropEvent BEFORE we detach/reattach components
- The refresh happens in the next UI update cycle when it's safe to modify the component tree

### Event Processing Timeline (AFTER Fix)
1. User drops item on backlog column
2. Vaadin DropEvent is triggered
3. `drag_on_backlog_drop()` listener is called
4. Drop event is propagated to `CPageServiceKanbanLine.handleDropOnBacklog()`
5. Database operations complete successfully (item removed from sprint)
6. **FIXED**: Refresh operations are scheduled via `UI.access()` but NOT executed yet
7. Control returns to Vaadin's drop event handler
8. **SUCCESS**: Vaadin completes drop event processing with component still attached
9. **DEFERRED**: UI refresh executes in next update cycle, safely updating the board

## Files Modified

### CPageServiceKanbanLine.java
All methods that refresh the Kanban board after drag-drop operations:

1. **handleDropOnBacklog()** - Lines 225-235
   - Wraps board refresh after removing item from sprint
   
2. **handleDragFromBacklog() - Empty status case** - Lines 311-318
   - Wraps board refresh when adding item to sprint without status change
   
3. **handleDragFromBacklog() - Single status case** - Lines 343-350
   - Wraps board refresh when adding item to sprint with automatic status
   
4. **handleDragBetweenColumns() - Empty status case** - Lines 504-511
   - Wraps board refresh when moving between columns without status change
   
5. **applyStatusAndSave()** - Lines 552-559 and 564-570
   - Wraps board refresh after status change (success and error cases)
   
6. **showStatusSelectionDialogForBacklog() - Success callback** - Lines 415-424
   - Wraps board refresh after user selects status for backlog item
   
7. **showStatusSelectionDialogForBacklog() - Cancel callback** - Lines 434-445
   - Wraps board refresh after user cancels status selection
   
8. **showStatusSelectionDialog() - Cancel callback** - Lines 644-649
   - Wraps board refresh after user cancels status selection for column move

## Testing Checklist

### Manual Testing Scenarios

#### 1. Drop on Backlog (Primary Issue)
- [ ] Start application and navigate to Kanban board
- [ ] Select a sprint with items in columns
- [ ] Drag an item from a column and drop it on the backlog
- [ ] **Verify**: No exception in logs
- [ ] **Verify**: Item appears in backlog
- [ ] **Verify**: Item removed from column
- [ ] **Verify**: Success notification shown

#### 2. Drag from Backlog to Column
- [ ] Drag an item from backlog to a column
- [ ] **Verify**: No exception in logs
- [ ] **Verify**: Item appears in column
- [ ] **Verify**: Item removed from backlog
- [ ] **Verify**: Success notification shown

#### 3. Drag Between Columns
- [ ] Drag an item from one column to another
- [ ] **Verify**: No exception in logs
- [ ] **Verify**: Item moves to target column
- [ ] **Verify**: Status updated if applicable
- [ ] **Verify**: Notification shown

#### 4. Status Selection Dialog
- [ ] Drag an item to a column with multiple valid statuses
- [ ] **Verify**: Dialog appears
- [ ] Select a status and confirm
- [ ] **Verify**: No exception in logs
- [ ] **Verify**: Item moves and status updates
- [ ] Repeat and cancel the dialog
- [ ] **Verify**: No exception in logs
- [ ] **Verify**: Item moves but status unchanged

### Expected Log Output (Success)
```
DEBUG on_postit_selected: Kanban board post-it selection changed to null
DEBUG displayEntityInDynamicOnepager: Locating entity in dynamic page: null
DEBUG loadSpecificPage: No page entity ID provided, clearing dynamic page router content.
DEBUG refreshBacklog: Refreshing backlog component
INFO  handleDropOnBacklog: Successfully removed sprint item 18 from sprint (status preserved)
```

**No ERROR or IllegalStateException should appear**

## Benefits of This Fix

1. **Eliminates Exception**: No more "Drop target not attached to UI" errors
2. **Preserves Functionality**: All drag-drop operations work as before
3. **Better Separation of Concerns**: Event handling and UI refresh are properly decoupled
4. **Vaadin Best Practice**: Follows recommended pattern for async UI updates
5. **Consistent Pattern**: Applied uniformly across all drag-drop handlers

## Performance Impact
- **Negligible**: UI.access() adds minimal overhead (< 1ms typically)
- **Visual**: No perceivable delay to users - refresh happens in next frame
- **Benefit**: Actually prevents potential race conditions with component lifecycle

## Related Documentation
- Vaadin Flow Documentation: [UI.access() for Thread-Safe Updates](https://vaadin.com/docs/latest/flow/advanced/server-push)
- Vaadin Component Lifecycle: [Attach and Detach Events](https://vaadin.com/docs/latest/flow/creating-components/lifecycle-callbacks)

## Future Considerations
If we encounter similar issues with other drag-drop operations:
1. Check if refreshComponent() is called during event processing
2. Wrap UI modifications in UI.access() to defer execution
3. Test both success and error paths
4. Ensure notifications are shown inside UI.access() blocks
