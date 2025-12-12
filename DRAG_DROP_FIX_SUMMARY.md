# Drag-Drop Bug Fix Summary

## Problem Statement
When moving a backlog item within the backlog grid itself (internal reordering), the application incorrectly triggered `moveSprintItemToBacklog()`, resulting in a `NullPointerException` because `draggedFromSprint` was null.

### Error Log
```
java.lang.NullPointerException: Cannot invoke "tech.derbent.app.sprints.domain.CSprintItem.getItem()" because "sprintItem" is null
	at tech.derbent.app.sprints.service.CPageServiceSprint.moveSprintItemToBacklog(CPageServiceSprint.java:264)
	at tech.derbent.app.sprints.service.CPageServiceSprint.on_backlogItems_drop(CPageServiceSprint.java:321)
```

## Root Cause
The `on_backlogItems_drop()` handler in `CPageServiceSprint` was unconditionally calling `moveSprintItemToBacklog()` for **all** drop events on the backlog grid, without checking the drag source. This caused:

1. **Internal backlog reordering** (CComponentBacklog → CComponentBacklog) to trigger sprint-to-backlog logic
2. **NullPointerException** because `draggedFromSprint` was null for internal backlog drags

## Solution Implemented

### 1. Drag-Drop Operation Type Detection
Created `DragDropOperationType` enum to categorize all possible drag-drop scenarios:

```java
private enum DragDropOperationType {
    BACKLOG_REORDER,      // Backlog → Backlog (internal reordering)
    SPRINT_TO_BACKLOG,    // Sprint Items → Backlog (remove from sprint)
    BACKLOG_TO_SPRINT,    // Backlog → Sprint Items (add to sprint)
    SPRINT_REORDER,       // Sprint Items → Sprint Items (reorder within sprint)
    UNKNOWN               // Unsupported operations
}
```

### 2. Drag Source Analysis Method
Implemented `determineDragDropOperation()` to analyze drag source and drop target:

```java
private DragDropOperationType determineDragDropOperation(final CDragDropEvent<?> event) {
    final Object dragSource = event.getDragSource();
    final Object dropTarget = event.getDropTarget();
    
    // Scenario 1: Backlog → Backlog (internal reordering)
    if (dragSource instanceof CComponentBacklog && dropTarget instanceof CComponentBacklog) {
        return DragDropOperationType.BACKLOG_REORDER;
    }
    
    // Scenario 2: Sprint Items → Backlog (remove from sprint)
    if (dragSource instanceof CComponentListSprintItems && dropTarget instanceof CComponentBacklog) {
        return DragDropOperationType.SPRINT_TO_BACKLOG;
    }
    
    // ... other scenarios
}
```

### 3. Enhanced Logging (New Requirement)
Added comprehensive debug logging to understand drag events:

```java
LOGGER.debug("[DragSourceDebug] ========== BACKLOG DROP EVENT START ==========");
LOGGER.debug("[DragSourceDebug] Drop target component: {}", component.getClass().getSimpleName());
LOGGER.debug("[DragSourceDebug] Drag source: {}", event.getDragSource().getClass().getSimpleName());
LOGGER.debug("[DragSourceDebug] Dragged items count: {}", event.getDraggedItems().size());
LOGGER.debug("[DragSourceDebug] draggedFromBacklog field: {}", draggedFromBacklog);
LOGGER.debug("[DragSourceDebug] draggedFromSprint field: {}", draggedFromSprint);

// Log component hierarchy from drag source to root
final List<Component> hierarchy = event.getDragSourceHierarchy();
LOGGER.debug("[DragSourceDebug] Drag source hierarchy (source to root):");
for (int i = 0; i < hierarchy.size(); i++) {
    LOGGER.debug("[DragSourceDebug]   [{}] {}", i, hierarchy.get(i).getClass().getSimpleName());
}
```

### 4. Fixed Drop Handler Logic
Updated `on_backlogItems_drop()` to check operation type before executing:

```java
public void on_backlogItems_drop(final Component component, final Object value) {
    // ... logging code ...
    
    // Determine operation type based on source and destination
    final DragDropOperationType operationType = determineDragDropOperation(event);
    
    // Handle based on operation type
    switch (operationType) {
        case BACKLOG_REORDER:
            // Internal backlog reordering is handled by CComponentBacklog itself
            LOGGER.debug("Internal backlog reordering detected - letting CComponentBacklog handle it");
            return;  // EXIT EARLY - Don't call moveSprintItemToBacklog()
            
        case SPRINT_TO_BACKLOG:
            // Move sprint item back to backlog
            final CSprintItem sprintItem = draggedFromSprint;
            Check.notNull(sprintItem, "Sprint item cannot be null when dropping from sprint to backlog");
            
            try {
                moveSprintItemToBacklog(sprintItem, event);
                refreshAfterBacklogDrop();
                CNotificationService.showSuccess("Item removed from sprint");
            } catch (final Exception e) {
                LOGGER.error("Error moving item to backlog", e);
                CNotificationService.showException("Error removing item from sprint", e);
            } finally {
                draggedFromSprint = null;
            }
            break;
            
        case BACKLOG_TO_SPRINT:
        case SPRINT_REORDER:
        case UNKNOWN:
        default:
            LOGGER.warn("Unexpected drag-drop operation on backlog: {}", operationType);
            break;
    }
}
```

## Key Design Decisions

### 1. Kept Fast Error Detection (New Requirement)
Maintained the fail-fast approach with `Check.notNull()`:
- **Before fix**: Would crash on internal backlog drag with NullPointerException
- **After fix**: Early return for internal backlog drags (no crash)
- **Sprint-to-backlog**: Still validates with `Check.notNull()` to catch developer errors

### 2. Separation of Concerns
- **CComponentBacklog**: Handles internal backlog reordering (already implemented)
- **CPageServiceSprint**: Handles cross-component drags (backlog ↔ sprint)

### 3. Comprehensive Logging
All drag events now logged with `[DragSourceDebug]` prefix for easy filtering:
- Drag source and drop target component types
- Dragged items and target items
- Component hierarchy (source to root)
- Internal tracking fields (draggedFromBacklog, draggedFromSprint)
- Determined operation type

## Testing Instructions

### Test Case 1: Internal Backlog Reordering (Bug Fix)
1. Navigate to Sprints page
2. Select a sprint
3. Drag a backlog item to a different position within the backlog grid
4. **Expected**: Item reorders smoothly, no exception
5. **Check logs**: Should see "Internal backlog reordering detected" message

### Test Case 2: Sprint Item to Backlog
1. Navigate to Sprints page
2. Select a sprint that has items
3. Drag a sprint item to the backlog grid
4. **Expected**: Item removed from sprint and added to backlog
5. **Check logs**: Should see "Determined operation type: SPRINT_TO_BACKLOG"

### Test Case 3: Backlog to Sprint
1. Navigate to Sprints page
2. Select a sprint
3. Drag a backlog item to the sprint items grid
4. **Expected**: Item added to sprint
5. **Check logs**: Should see appropriate operation type

## Files Modified
- `src/main/java/tech/derbent/app/sprints/service/CPageServiceSprint.java`
  - Added `DragDropOperationType` enum
  - Added `determineDragDropOperation()` method
  - Enhanced `on_backlogItems_drop()` with comprehensive logging and source checking
  - Maintains fail-fast error detection with `Check.notNull()`

## Benefits
1. **Bug Fixed**: No more NullPointerException when reordering backlog items
2. **Better Debugging**: Comprehensive logging helps diagnose future drag-drop issues
3. **Maintainability**: Clear operation type enum makes drag-drop logic easier to understand
4. **Fast Error Detection**: Developer errors still caught immediately with Check.notNull()
5. **Separation of Concerns**: Each component handles its own internal drag-drop logic

## Related Code Patterns
- **Drag Source Tracking**: CPageService automatically populates CDragDropEvent.getDragSource()
- **Component Hierarchy**: CDragDropEvent.getDragSourceHierarchy() provides full parent chain
- **Event Propagation**: Internal drops return early to let components handle their own reordering
