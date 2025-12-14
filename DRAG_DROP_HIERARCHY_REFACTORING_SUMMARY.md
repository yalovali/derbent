# Drag-Drop Event Hierarchy Refactoring Summary

## Problem Statement
The drag-drop event system had several issues:
1. **CDragDropEvent duplicated functionality** of CDragStartEvent, CDragEndEvent, and CDropEvent
2. **Confusing class hierarchy** with repeating concepts
3. **Incomplete CGrid implementations** (methods returning null)
4. **Potential notification loops** not verified
5. **Unnecessary state tracking** in CPageService

## Solution Implemented

### 1. Unified Event Hierarchy
Simplified to three core event types, all extending CEvent:

```
CEvent (extends ComponentEvent<Component>)
├── CDragStartEvent<T> - Fired when drag starts, contains dragged items
├── CDragEndEvent - Fired when drag ends
└── CDropEvent<T> - Fired when items dropped, contains all drop context
```

**Removed:** `CDragDropEvent` (was redundant)

### 2. Complete CGrid Implementation
Fixed incomplete event handler methods in CGrid:

**Before:**
```java
private ComponentEventListener<GridDragStartEvent<EntityClass>> on_grid_dragStart() {
    
    return null;
}
```

**After:**
```java
private ComponentEventListener<GridDragStartEvent<EntityClass>> on_grid_dragStart() {
    return event -> {
        final List<EntityClass> draggedItems = new ArrayList<>(event.getDraggedItems());
        activeDraggedItems = draggedItems; // Track for drop event
        final CDragStartEvent<EntityClass> dragStartEvent = new CDragStartEvent<>(this, draggedItems, true);
        notifyDragStartListeners(dragStartEvent);
    };
}
```

### 3. Simplified CPageService
Removed unnecessary state tracking since events now carry all required information:

**Removed:**
- `private Component activeDragSource` (no longer needed)
- `private List<?> activeDraggedItems` (no longer needed)

**Simplified event binding methods:**
- Use `Check.instanceOf()` for fail-fast validation
- Removed redundant logging
- Pass events directly without wrapping

### 4. Updated CPageServiceSprint
All handler methods now use proper event types:

**Before:**
```java
public void on_backlogItems_dragStart(final Component component, final Object value) {
    if (value instanceof CDragDropEvent) {
        final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
        // ...
    }
}
```

**After:**
```java
public void on_backlogItems_dragStart(final Component component, final Object value) {
    if (value instanceof CDragStartEvent) {
        final CDragStartEvent<?> event = (CDragStartEvent<?>) value;
        // ...
    }
}
```

### 5. Enhanced CDropEvent
Added convenience method for clarity:

```java
/** Gets the component where items are being dropped (convenience method).
 * This is equivalent to getSource() but provides clearer semantics for drop operations.
 * @return the drop target component (same as getSource()) */
public Component getDropTarget() { return (Component) getSource(); }
```

### 6. Verified No Notification Loops
Confirmed the pattern prevents loops through:

1. **One-way event flow**: Vaadin events → CGrid conversion → upward propagation only
2. **CGrid never listens to itself**: Only registers with Vaadin Grid's internal event system
3. **Listener registration pattern**: CGrid methods convert and notify external listeners
4. **State tracking**: CGrid tracks dragged items internally (not for loops, but for data passing)

## Event Flow Pattern

### Complete Flow (Drag Start Example)
```
User drags in CGrid
    ↓
Vaadin GridDragStartEvent
    ↓
CGrid.on_grid_dragStart()
    ├── Tracks: activeDraggedItems = event.getDraggedItems()
    ├── Creates: new CDragStartEvent<>(this, draggedItems, true)
    └── Calls: notifyDragStartListeners(dragStartEvent)
        ↓
Parent Component (CComponentGridEntity)
    ├── Receives: CDragStartEvent<?>
    └── Calls: notifyDragStartListeners(event) [forwards up]
        ↓
CPageService.bindDragStart()
    ├── Receives: CDragStartEvent<?>
    └── Calls: method.invoke(this, component, event)
        ↓
Handler: on_{componentName}_dragStart(Component, Object)
    ├── Casts: CDragStartEvent<?> event = (CDragStartEvent<?>) value
    ├── Extracts: event.getDraggedItems()
    └── Business logic
```

Same pattern applies for `dragEnd` and `drop` events.

## Files Changed

### Deleted
- `src/main/java/tech/derbent/api/services/pageservice/CDragDropEvent.java`
- `src/test/java/tech/derbent/api/services/pageservice/CDragDropEventSourceTrackingTest.java`

### Modified
- `src/main/java/tech/derbent/api/grid/domain/CGrid.java`
  - Implemented complete drag-drop event handlers
  - Added activeDraggedItems tracking
  - Removed CDragDropEvent import
  
- `src/main/java/tech/derbent/api/services/pageservice/CPageService.java`
  - Removed unnecessary state tracking
  - Simplified event binding with fail-fast approach
  - Removed redundant logging
  
- `src/main/java/tech/derbent/app/sprints/service/CPageServiceSprint.java`
  - Updated all handler methods to use proper event types
  - Updated helper methods to accept proper event types
  - Used getDropTarget() for clarity
  - Applied fail-fast validation with Check.instanceOf()
  
- `src/main/java/tech/derbent/api/interfaces/drag/CDropEvent.java`
  - Added getDropTarget() convenience method

### Created
- `docs/implementation/drag-drop-event-flow.md`
  - Complete documentation of drag-drop event flow
  - No notification loop verification
  - Troubleshooting guide

## Benefits

1. **Cleaner hierarchy**: Three focused event types instead of four overlapping ones
2. **Type safety**: Each event type carries appropriate data
3. **No loops**: One-way flow prevents circular notifications
4. **Traceable**: Clear path from user action to handler
5. **Extensible**: Easy to add new event types
6. **Clean separation**: Grid handles Vaadin API, custom events for business logic
7. **Fail-fast**: Use Check.instanceOf() for immediate error detection
8. **Maintainable**: Less code, clearer intent

## Testing

✅ **Compilation**: All code compiles successfully
✅ **Security**: No vulnerabilities found in CodeQL scan
⏳ **Playwright Tests**: Ready to run (UI automation tests)

## Migration Guide

For any custom code using CDragDropEvent:

**Old Pattern:**
```java
public void on_component_drop(Component component, Object value) {
    if (value instanceof CDragDropEvent) {
        CDragDropEvent<?> event = (CDragDropEvent<?>) value;
        List<?> items = event.getDraggedItems();
        Object source = event.getDragSource();
        Object target = event.getDropTarget();
    }
}
```

**New Pattern:**
```java
public void on_component_drop(Component component, Object value) {
    if (value instanceof CDropEvent) {
        CDropEvent<?> event = (CDropEvent<?>) value;
        List<?> items = event.getDraggedItems();
        Component source = event.getDragSource();
        Component target = event.getDropTarget();  // or event.getSource()
    }
}
```

For drag start handlers, use `CDragStartEvent` instead of `CDragDropEvent`.
For drag end handlers, use `CDragEndEvent` instead of `CDragDropEvent`.

## References

- **Complete Flow Documentation**: `docs/implementation/drag-drop-event-flow.md`
- **IHasDragControl API**: `src/main/java/tech/derbent/api/interfaces/IHasDragControl.java`
- **CGrid Implementation**: `src/main/java/tech/derbent/api/grid/domain/CGrid.java`
- **Example Usage**: `src/main/java/tech/derbent/app/sprints/service/CPageServiceSprint.java`
