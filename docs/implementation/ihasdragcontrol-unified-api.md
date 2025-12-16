# IHasDragControl - Unified Drag-Drop API

## Overview

`IHasDragControl` is the **single unified interface** for all drag-and-drop operations in the Derbent application. It consolidates drag-start, drag-end, and drop event handling into one interface, eliminating the need for separate interfaces and providing a consistent API across all components.

## Key Principles

### 1. Single Interface for All Drag-Drop Operations

**DO** use `IHasDragControl` for all drag-drop functionality:
```java
public class CComponentListSprintItems extends CComponentListEntityBase 
        implements IHasDragControl {
    // All drag-drop methods in one place
}
```

**DON'T** use Vaadin's Grid drag-drop methods directly:
```java
// ❌ WRONG - Do NOT use Vaadin Grid methods directly
grid.setRowsDraggable(true);
grid.setDropMode(GridDropMode.BETWEEN);
grid.addDragStartListener(event -> { });
grid.addDropListener(event -> { });

// ✅ CORRECT - Use CGrid's IHasDragControl methods
grid.setDragEnabled(true);
grid.setDropEnabled(true);
grid.addDragStartListener(event -> { });
grid.addDropListener(event -> { });
```

### 2. Recursive Event Propagation

Events automatically bubble up through the component hierarchy:

```
CGrid (implements IHasDragControl)
    ↓ forwards internal Grid events to IHasDragControl listeners
CComponentListEntityBase (implements IHasDragControl)
    ↓ propagates events to parent via notifyEvents()
Parent Component or PageService
    ↓ handles drag-drop logic
```

**Implementation Pattern:**
```java
// CGrid automatically forwards Grid events to IHasDragControl listeners
private void setupDragDropForwarding() {
    super.addDragStartListener(event -> {
        for (ComponentEventListener listener : dragStartListeners) {
            listener.onComponentEvent(event);
        }
    });
}

// Parent components register listeners
grid.addDragStartListener(event -> {
    // Handle drag start
});
```

### 3. CGrid as the Foundation

`CGrid` is the only class that should interact with Vaadin's Grid drag-drop API directly. All other components use `CGrid` through its `IHasDragControl` interface.

**CGrid responsibilities:**
- Wraps Vaadin Grid's drag-drop functionality
- Forwards Grid events to IHasDragControl listeners
- Provides setDragEnabled/setDropEnabled for enabling/disabling drag-drop

**Other components:**
- Use CGrid's IHasDragControl methods only
- NEVER call Vaadin Grid methods directly

## IHasDragControl Interface

### Methods

```java
public interface IHasDragControl {
    // Listener registration
    Registration addDragStartListener(ComponentEventListener<GridDragStartEvent> listener);
    Registration addDragEndListener(ComponentEventListener<GridDragEndEvent> listener);
    Registration addDropListener(ComponentEventListener<GridDropEvent> listener);
    
    // Listener access (for notifyEvents)
    List<ComponentEventListener<GridDragStartEvent>> getDragStartListeners();
    List<ComponentEventListener<GridDragEndEvent>> getDragEndListeners();
    List<ComponentEventListener<GridDropEvent>> getDropListeners();
    
    // Enable/disable functionality
    void setDragEnabled(boolean enabled);
    void setDropEnabled(boolean enabled);
    boolean isDragEnabled();
    boolean isDropEnabled();
    
    // Event notification helper
    default void notifyEvents(ComponentEvent event) {
        // Automatically notifies all registered listeners
    }
}
```

### Implementation Pattern

```java
public class CComponentListEntityBase implements IHasDragControl {
    
    // State fields
    private boolean dragEnabled = false;
    private boolean dropEnabled = false;
    private final List<ComponentEventListener<GridDragStartEvent>> dragStartListeners = new ArrayList<>();
    private final List<ComponentEventListener<GridDragEndEvent>> dragEndListeners = new ArrayList<>();
    private final List<ComponentEventListener<GridDropEvent>> dropListeners = new ArrayList<>();
    
    // Listener registration
    @Override
    public Registration addDragStartListener(ComponentEventListener<GridDragStartEvent> listener) {
        dragStartListeners.add(listener);
        return () -> dragStartListeners.remove(listener);
    }
    
    // Enable/disable
    @Override
    public void setDragEnabled(boolean enabled) {
        dragEnabled = enabled;
        if (grid != null) {
            grid.setDragEnabled(enabled);  // Use CGrid's method, NOT grid.setRowsDraggable()
        }
    }
    
    // Forward events from child grid to parent
    private void setupGridListeners() {
        grid.addDragStartListener(event -> {
            // Notify parent listeners
            notifyEvents(event);
        });
    }
}
```

## Custom Event Classes

For richer event data, the application provides custom event classes that extend `ComponentEvent`:

### CDragStartEvent
```java
public class CDragStartEvent extends ComponentEvent<Component> {
    private final List<T> draggedItems;
    
    public List<T> getDraggedItems() { return draggedItems; }
    public T getDraggedItem() { return draggedItems.get(0); }
}
```

### CDragEndEvent
```java
public class CDragEndEvent extends ComponentEvent<Component> {
    // Simple event indicating drag operation ended
}
```

### CDropEvent
```java
public class CDropEvent extends ComponentEvent<Component> {
    private final List<T> draggedItems;
    private final Component dragSource;
    private final T targetItem;
    private final GridDropLocation dropLocation;
    
    public List<T> getDraggedItems() { return draggedItems; }
    public Component getDragSource() { return dragSource; }
    public Optional<T> getDropTargetItem() { return Optional.ofNullable(targetItem); }
    public GridDropLocation getDropLocation() { return dropLocation; }
}
```

**Benefits of custom events:**
- Work with any component, not just Grid
- Provide richer context (drag source component, item hierarchy)
- Enable future extensibility (add more fields as needed)
- Consistent with application's component hierarchy

## Migration Guide

### Old Pattern (❌ Deprecated)

```java
// Using Vaadin Grid methods directly
grid.setRowsDraggable(true);
grid.setDropMode(GridDropMode.BETWEEN);

grid.addDragStartListener(event -> {
    List<?> items = event.getDraggedItems();
    // Handle drag start
});

grid.addDropListener(event -> {
    Object targetItem = event.getDropTargetItem().orElse(null);
    GridDropLocation location = event.getDropLocation();
    // Handle drop
});
```

### New Pattern (✅ Correct)

```java
// Using IHasDragControl methods
grid.setDragEnabled(true);
grid.setDropEnabled(true);

grid.addDragStartListener(event -> {
    GridDragStartEvent gridEvent = (GridDragStartEvent) event;
    List<?> items = gridEvent.getDraggedItems();
    // Handle drag start
});

grid.addDropListener(event -> {
    GridDropEvent dropEvent = (GridDropEvent) event;
    Object targetItem = dropEvent.getDropTargetItem().orElse(null);
    GridDropLocation location = dropEvent.getDropLocation();
    // Handle drop
});
```

## Component Hierarchy Examples

### Example 1: Sprint Planning with Backlog

```java
// Sprint page service sets up drag-drop between backlog and sprint items
public class CPageServiceSprint extends CPageService<CSprint> {
    
    @Override
    public void initialize() {
        // Enable drag from backlog to sprint
        componentBacklog.setDragEnabled(true);
        componentSprintItems.setDropEnabled(true);
        
        // Register listeners
        componentBacklog.addDragStartListener(event -> {
            // Track what's being dragged
        });
        
        componentSprintItems.addDropListener(event -> {
            GridDropEvent dropEvent = (GridDropEvent) event;
            // Determine if drop came from backlog
            // Move items from backlog to sprint
        });
    }
}
```

### Example 2: Grid Reordering

```java
// Component that supports internal reordering
public class CComponentListEntityBase implements IHasDragControl {
    
    protected void enableReordering() {
        grid.setDragEnabled(true);
        grid.setDropEnabled(true);
        
        grid.addDropListener(event -> {
            GridDropEvent<ChildEntity> dropEvent = (GridDropEvent<ChildEntity>) event;
            ChildEntity draggedItem = getDraggedItemFromTracking();
            ChildEntity targetItem = dropEvent.getDropTargetItem().orElse(null);
            GridDropLocation location = dropEvent.getDropLocation();
            
            // Reorder items
            service.reorder(draggedItem, targetItem, location);
            refreshGrid();
        });
    }
}
```

## Testing

### Unit Testing Drag-Drop

```java
@Test
public void testDragEnabledPropagation() {
    CGrid<CActivity> grid = new CGrid<>(CActivity.class);
    CComponentListEntityBase component = new CComponentListSprintItems(...);
    
    // Enable drag on parent
    component.setDragEnabled(true);
    
    // Verify grid received the setting
    assertTrue(grid.isDragEnabled());
}

@Test
public void testEventPropagation() {
    CGrid<CActivity> grid = new CGrid<>(CActivity.class);
    AtomicBoolean listenerCalled = new AtomicBoolean(false);
    
    // Register listener on component
    grid.addDragStartListener(event -> listenerCalled.set(true));
    
    // Simulate drag start
    // ... trigger drag start event on internal Grid
    
    // Verify listener was called
    assertTrue(listenerCalled.get());
}
```

### Integration Testing with Playwright

```java
// Sprint planning drag-drop test
@Test
public void testDragFromBacklogToSprint() {
    // Navigate to sprint page
    page.navigate("/sprint/123");
    
    // Find backlog item and sprint grid
    Locator backlogItem = page.locator("#backlog-grid .grid-row").first();
    Locator sprintGrid = page.locator("#sprint-items-grid");
    
    // Perform drag-drop
    backlogItem.dragTo(sprintGrid);
    
    // Verify item moved
    assertFalse(page.locator("#backlog-grid").locator(backlogItem).isVisible());
    assertTrue(page.locator("#sprint-items-grid").locator(backlogItem).isVisible());
}
```

## Best Practices

### DO:
1. ✅ Implement IHasDragControl on components that need drag-drop
2. ✅ Use CGrid's setDragEnabled/setDropEnabled methods
3. ✅ Register listeners via IHasDragControl methods
4. ✅ Use notifyEvents() to propagate events up the hierarchy
5. ✅ Cast events to specific types when needed (GridDragStartEvent, GridDropEvent)

### DON'T:
1. ❌ Call grid.setRowsDraggable() or grid.setDropMode() directly
2. ❌ Use Vaadin Grid's addDragStartListener/addDropListener directly
3. ❌ Create separate interfaces for drag-start, drag-end, drop operations
4. ❌ Bypass IHasDragControl and implement custom drag-drop logic

## Troubleshooting

### Issue: Events not propagating to parent

**Cause:** Not calling notifyEvents() or not registering listeners properly.

**Solution:**
```java
// In child component
grid.addDragStartListener(event -> {
    notifyEvents(event);  // Propagate to parent listeners
});

// In parent component
childComponent.addDragStartListener(event -> {
    // Will receive events from child
});
```

### Issue: Cannot cast event to GridDragStartEvent

**Cause:** Event is wrapped in ComponentEvent.

**Solution:**
```java
grid.addDragStartListener(event -> {
    GridDragStartEvent gridEvent = (GridDragStartEvent) event;
    List<?> items = gridEvent.getDraggedItems();
});
```

### Issue: Drag not working on CGrid

**Cause:** Not enabling drag via setDragEnabled().

**Solution:**
```java
grid.setDragEnabled(true);  // Enable drag
grid.setDropEnabled(true);  // Enable drop
```

## Future Enhancements

### Custom Event Migration (Optional)

In the future, IHasDragControl could be updated to use custom event classes:

```java
public interface IHasDragControl {
    Registration addDragStartListener(ComponentEventListener<CDragStartEvent> listener);
    Registration addDragEndListener(ComponentEventListener<CDragEndEvent> listener);
    Registration addDropListener(ComponentEventListener<CDropEvent> listener);
}
```

This would provide:
- Richer event data
- Better abstraction from Vaadin Grid specifics
- Easier testing and mocking

## Related Documentation

- [IStateOwnerComponent Implementation](./istateownercomponent-implementation.md) - State preservation during grid refresh
- [Drag Source Tracking](../development/drag-source-tracking.md) - CPageService drag source tracking
- [Copilot Guidelines](../development/copilot-guidelines.md) - Coding standards and patterns

## Summary

`IHasDragControl` provides a unified, consistent API for all drag-drop operations in the application:
- **Single interface** for drag-start, drag-end, and drop operations
- **Recursive propagation** through component hierarchy
- **CGrid as foundation** - the only class that interacts with Vaadin Grid directly
- **Custom event classes** for richer context and future extensibility
- **No direct Vaadin Grid usage** - always use IHasDragControl methods
