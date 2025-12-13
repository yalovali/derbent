# Drag-Drop Event Flow Pattern

## Overview

This document describes the complete drag-drop event flow from CGrid through intermediate components to CPageService handler methods. The pattern ensures proper event propagation without loops and uses a unified event hierarchy.

## Event Hierarchy

```
CEvent (extends ComponentEvent<Component>)
├── CDragStartEvent<T> - Fired when drag starts, contains dragged items
├── CDragEndEvent - Fired when drag ends (no items, track from dragStart if needed)
└── CDropEvent<T> - Fired when items dropped, contains dragged items, source, target, location
```

**Note:** `CDragDropEvent` was removed as it duplicated the functionality of the above three event types.

## Complete Event Flow

### 1. Drag Start Flow

```
User drags item in CGrid
    ↓
Vaadin GridDragStartEvent<EntityClass>
    ↓
CGrid.on_grid_dragStart() listener
    ├── Extracts: List<EntityClass> draggedItems = event.getDraggedItems()
    ├── Tracks: activeDraggedItems = draggedItems (for later use in drop)
    ├── Creates: new CDragStartEvent<>(this, draggedItems, true)
    └── Calls: notifyDragStartListeners(dragStartEvent)
        ↓
Parent component (e.g., CComponentGridEntity) listeners
    ├── Receives: CDragStartEvent<?>
    └── Calls: notifyDragStartListeners(event) [forwards up]
        ↓
CPageService.bindDragStart() listener
    ├── Receives: CDragStartEvent<?>
    ├── Logs: Component name and item count
    └── Calls: method.invoke(this, component, event)
        ↓
Handler: on_{componentName}_dragStart(Component component, Object value)
    ├── Casts: CDragStartEvent<?> event = (CDragStartEvent<?>) value
    ├── Extracts: event.getDraggedItems()
    └── Business logic (e.g., track dragged item for cross-component drops)
```

### 2. Drag End Flow

```
User releases drag (drop or cancel)
    ↓
Vaadin GridDragEndEvent<EntityClass>
    ↓
CGrid.on_grid_dragEnd() listener
    ├── Creates: new CDragEndEvent(this, true)
    ├── Calls: notifyDragEndListeners(dragEndEvent)
    └── Clears: activeDraggedItems = null (cleanup)
        ↓
Parent component listeners
    ├── Receives: CDragEndEvent
    └── Calls: notifyDragEndListeners(event) [forwards up]
        ↓
CPageService.bindDragEnd() listener
    ├── Receives: CDragEndEvent
    └── Calls: method.invoke(this, component, event)
        ↓
Handler: on_{componentName}_dragEnd(Component component, Object value)
    ├── Casts: CDragEndEvent event = (CDragEndEvent) value
    └── Business logic (e.g., clear tracked drag state)
```

### 3. Drop Flow

```
User drops items on CGrid
    ↓
Vaadin GridDropEvent<EntityClass>
    ↓
CGrid.on_grid_dragDrop() listener
    ├── Retrieves: activeDraggedItems (tracked from dragStart)
    ├── Extracts: EntityClass targetItem = event.getDropTargetItem().orElse(null)
    ├── Extracts: GridDropLocation dropLocation = event.getDropLocation()
    ├── Creates: new CDropEvent<>(this, draggedItems, this, targetItem, dropLocation, true)
    └── Calls: notifyDropListeners(dropEvent)
        ↓
Parent component listeners
    ├── Receives: CDropEvent<?>
    └── Calls: notifyDropListeners(event) [forwards up]
        ↓
CPageService.bindIHasDropEvent() listener
    ├── Receives: CDropEvent<?>
    └── Calls: method.invoke(this, component, event)
        ↓
Handler: on_{componentName}_drop(Component component, Object value)
    ├── Casts: CDropEvent<?> event = (CDropEvent<?>) value
    ├── Extracts: event.getDraggedItems()
    ├── Extracts: event.getDragSource() (to check where drag came from)
    ├── Extracts: event.getTargetItem(), event.getDropLocation()
    └── Business logic (e.g., move/reorder items)
```

## No Notification Loops in Grid

The pattern **prevents notification loops** through these mechanisms:

### 1. One-Way Event Flow
- Vaadin Grid events → CGrid methods (one-way conversion)
- CGrid never adds listeners to itself
- Events flow upward only (Grid → Parent → PageService)

### 2. Listener Registration Pattern
```java
// In CGrid constructor:
addDragStartListener(on_grid_dragStart());  // Vaadin API
addDragEndListener(on_grid_dragEnd());      // Vaadin API  
addDropListener(on_grid_dragDrop());        // Vaadin API

// These register with Vaadin Grid's internal event system
// They do NOT register with IHasDragControl listeners
```

### 3. Notification Methods
```java
// CGrid converts Vaadin events and notifies IHasDragControl listeners:
private ComponentEventListener<GridDragStartEvent<EntityClass>> on_grid_dragStart() {
    return event -> {
        // Convert Vaadin event
        final CDragStartEvent<EntityClass> dragStartEvent = new CDragStartEvent<>(...);
        // Notify external listeners (not self)
        notifyDragStartListeners(dragStartEvent);
    };
}
```

### 4. Tracking State
- **CGrid.activeDraggedItems**: Tracks items during drag for use in drop (GridDropEvent doesn't provide them)
- **Cleared in dragEnd**: Prevents stale data
- **Not used for loops**: Only for data passing between events

## Example: Sprint Management Drag-Drop

### Scenario: Drag activity from backlog to sprint

```java
// 1. User drags activity from backlog grid
CGrid<CActivity> fires GridDragStartEvent
    ↓
CGrid.on_grid_dragStart()
    → Creates CDragStartEvent<CActivity>
    → notifyDragStartListeners()
        ↓
CComponentBacklog receives event
    → notifyDragStartListeners() [forwards]
        ↓
CPageServiceSprint.on_backlogItems_dragStart()
    → Tracks: draggedFromBacklog = event.getDraggedItem()

// 2. User drops activity on sprint widget grid
CGrid<CSprintItem> in widget fires GridDropEvent
    ↓
CGrid.on_grid_dragDrop()
    → Creates CDropEvent<CSprintItem>
    → notifyDropListeners()
        ↓
CComponentWidgetSprint receives event
    → notifyDropListeners() [forwards]
        ↓
CPageServiceSprint.on_masterGrid_drop()
    → Checks: event.getDragSource() instanceof CComponentBacklog
    → Creates: new CSprintItem(sprint, draggedFromBacklog)
    → Saves and refreshes
```

## Key Benefits

1. **Type Safety**: Each event type carries appropriate data
2. **No Loops**: One-way flow prevents circular notifications
3. **Traceable**: Clear path from user action to handler
4. **Extensible**: Easy to add new event types
5. **Clean Separation**: Grid handles Vaadin API, custom events for business logic

## Handler Method Signature Pattern

All page service drag-drop handlers use this signature for generalization:

```java
public void on_{componentName}_{eventType}(final Component component, final Object value) {
    // Cast to specific event type
    if (value instanceof CDragStartEvent) {
        final CDragStartEvent<?> event = (CDragStartEvent<?>) value;
        // ... handle drag start
    }
}
```

This allows:
- **Flexibility**: Handlers can accept any event type
- **Runtime dispatch**: Java's reflection invokes the right handler
- **Type safety**: Handlers validate and cast to expected type
- **Fail-fast**: Use Check.instanceOf() for validation

## Troubleshooting

### Issue: Drop event has empty dragged items
**Cause**: GridDropEvent doesn't provide dragged items
**Solution**: CGrid tracks items in `activeDraggedItems` from dragStart, uses them in drop

### Issue: Events not reaching page service
**Cause**: Intermediate component not forwarding events
**Solution**: Ensure component calls `notifyDragStartListeners(event)` in its listener

### Issue: Multiple handlers executing
**Cause**: Event propagation through component hierarchy (intentional)
**Solution**: Use `event.getDragSource()` to filter in handlers
