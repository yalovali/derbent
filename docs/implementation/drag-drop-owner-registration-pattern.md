# Drag-Drop Owner Registration Pattern

## Overview

The drag-drop owner registration pattern provides a standardized mechanism for components to register with their owner (typically a parent component or page service) for automatic drag-drop event propagation. This pattern ensures clean ownership hierarchies and prevents third-party classes from directly registering to component fields.

## Architecture

### Component Hierarchy

```
CPageService (Owner/Root)
    ↓ owns and binds events from
CComponentGridEntity (Component)
    ↓ owns and binds events from
CGrid<T> (Grid Component)
```

### Event Flow

```
User Action (Drag/Drop on Grid)
    ↓
CGrid fires GridDragStartEvent/GridDragEndEvent/GridDropEvent
    ↓
CComponentGridEntity receives event via setupGridDragDropListeners()
    ↓
CComponentGridEntity propagates to registered listeners
    ↓
CPageService receives event via bound handler method (on_gridName_dragStart)
```

## Implementation Pattern

### Step 1: Component Implements IHasDragControl

All drag-drop enabled components must implement `IHasDragControl` interface with owner registration support:

```java
public class CComponentGridEntity extends CDiv 
        implements IHasDragStart<CEntityDB<?>>, IHasDragEnd<CEntityDB<?>>, 
                   IHasDrop<CEntityDB<?>>, IHasDragControl {
    
    // Owner registration fields
    private Object dragDropOwner = null;
    
    @Override
    public void setDragDropOwner(final Object owner) {
        this.dragDropOwner = owner;
        LOGGER.debug("[DragDebug] Owner set to {}", 
            owner != null ? owner.getClass().getSimpleName() : "null");
    }
    
    @Override
    public Object getDragDropOwner() {
        return dragDropOwner;
    }
    
    @Override
    public void registerWithOwner() {
        Check.notNull(dragDropOwner, "Owner must be set before registration");
        LOGGER.debug("[DragDebug] Registering with owner {}", 
            dragDropOwner.getClass().getSimpleName());
        // Registration logic here
    }
}
```

### Step 2: Owner Sets Ownership

The owner component or page service sets itself as the owner:

```java
// In CPageService or parent component
public void initializeGridComponent(CComponentGridEntity gridComponent) {
    // Set ownership
    gridComponent.setDragDropOwner(this);
    
    // Register component with owner
    gridComponent.registerWithOwner();
}
```

### Step 3: Automatic Event Binding

When `registerWithOwner()` is called, the component automatically binds its events to the owner:

```java
@Override
public void registerWithOwner() {
    Check.notNull(dragDropOwner, "Owner must be set before registration");
    
    // The owner's existing event binding mechanism (e.g., CPageService.bindMethods())
    // will automatically bind this component's events to handler methods like:
    // - on_gridName_dragStart(Component, CDragDropEvent)
    // - on_gridName_dragEnd(Component, CDragDropEvent)
    // - on_gridName_drop(Component, CDragDropEvent)
}
```

### Step 4: Grid-Level Registration

Components with internal grids should also register the grid:

```java
private void createContent() {
    grid = new CGrid(entityClass);
    
    // Set up ownership chain
    grid.setDragDropOwner(this);
    
    // Add listeners to propagate events
    setupGridDragDropListeners();
}

private void setupGridDragDropListeners() {
    grid.addDragStartListener(event -> {
        LOGGER.debug("[DragDebug] Grid drag start, notifying listeners");
        notifyDragStartListeners(event);
    });
    
    grid.addDragEndListener(event -> {
        LOGGER.debug("[DragDebug] Grid drag end, notifying listeners");
        notifyDragEndListeners(event);
    });
    
    grid.addDropListener(event -> {
        LOGGER.debug("[DragDebug] Grid drop, notifying listeners");
        notifyDropListeners(event);
    });
}
```

## Complete Usage Example

### 1. Page Service Implementation

```java
public class CPageServiceActivity extends CPageService<CActivity> {
    
    private CComponentGridEntity gridActivities;
    
    public void initialize() {
        // Create grid component
        gridActivities = new CComponentGridEntity(gridEntity, sessionService);
        
        // Set ownership and register
        gridActivities.setDragDropOwner(this);
        gridActivities.registerWithOwner();
        
        // Enable drag-drop
        gridActivities.setDragEnabled(true);
        gridActivities.setDropEnabled(true);
    }
    
    // Handler methods are automatically bound via CPageService.bindMethods()
    public void on_gridActivities_dragStart(Component component, Object value) {
        if (value instanceof CDragDropEvent) {
            final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
            LOGGER.debug("Drag started with {} items", event.getDraggedItems().size());
        }
    }
    
    public void on_gridActivities_drop(Component component, Object value) {
        if (value instanceof CDragDropEvent) {
            final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
            LOGGER.debug("Drop received from {}", event.getDragSource());
        }
    }
}
```

### 2. Component Implementation

```java
public class CComponentGridEntity extends CDiv 
        implements IHasDragStart<CEntityDB<?>>, IHasDragEnd<CEntityDB<?>>, 
                   IHasDrop<CEntityDB<?>>, IHasDragControl {
    
    private Object dragDropOwner = null;
    private CGrid<?> grid;
    
    @Override
    public void setDragDropOwner(final Object owner) {
        this.dragDropOwner = owner;
        if (grid != null) {
            grid.setDragDropOwner(owner);
        }
    }
    
    @Override
    public void registerWithOwner() {
        Check.notNull(dragDropOwner, "Owner must be set before registration");
        
        // Component's events are already available via IHasDragStart/IHasDragEnd/IHasDrop
        // The owner's binding mechanism (e.g., CPageService.bindMethods()) will
        // automatically find and bind to these listeners
        
        // Also register the grid
        if (grid != null) {
            grid.registerWithOwner();
        }
    }
}
```

### 3. Grid Implementation

```java
public class CGrid<EntityClass> extends Grid<EntityClass> 
        implements IHasDragStart<EntityClass>, IHasDragEnd<EntityClass>, IHasDragControl {
    
    private Object dragDropOwner = null;
    private boolean dragEnabled = false;
    private boolean dropEnabled = false;
    
    @Override
    public void setDragEnabled(final boolean enabled) {
        dragEnabled = enabled;
        setRowsDraggable(enabled);
    }
    
    @Override
    public void setDropEnabled(final boolean enabled) {
        dropEnabled = enabled;
        if (enabled) {
            setDropMode(GridDropMode.BETWEEN);
        } else {
            setDropMode(null);
        }
    }
    
    @Override
    public void setDragDropOwner(final Object owner) {
        this.dragDropOwner = owner;
    }
    
    @Override
    public void registerWithOwner() {
        Check.notNull(dragDropOwner, "Owner must be set before registration");
        // Grid's built-in addDragStartListener/addDragEndListener/addDropListener
        // are already available for the owner to bind to
    }
}
```

## Benefits

### 1. Clean Ownership Hierarchy
- Components register with their owners, creating a clear ownership chain
- Events flow upward through the hierarchy automatically

### 2. Prevents Third-Party Registration
- Owner registration pattern prevents external classes from directly accessing component fields
- Only the designated owner can receive events from the component

### 3. Automatic Event Binding
- CPageService's `bindMethods()` automatically discovers and binds handler methods
- No manual listener registration required in page services

### 4. Consistent Pattern
- All drag-drop enabled components follow the same registration pattern
- Easy to understand and maintain

### 5. Debugging Support
- Comprehensive logging at each level of the hierarchy
- Easy to trace event flow from grid → component → owner

## Best Practices

### 1. Always Set Owner Before Registration
```java
// ✅ CORRECT
component.setDragDropOwner(this);
component.registerWithOwner();

// ❌ WRONG - Will throw IllegalStateException
component.registerWithOwner(); // No owner set!
```

### 2. Register Child Components
```java
@Override
public void registerWithOwner() {
    Check.notNull(dragDropOwner, "Owner must be set before registration");
    
    // Register this component's events
    // ...
    
    // ✅ Also register child components
    if (grid != null) {
        grid.registerWithOwner();
    }
}
```

### 3. Use Component Name for Handler Methods
```java
// Component declares its name
@Override
public String getComponentName() {
    return "gridActivities";
}

// Page service handler follows naming convention
public void on_gridActivities_dragStart(Component component, Object value) {
    // Handle event
}
```

### 4. Enable Drag-Drop After Registration
```java
// ✅ CORRECT order
gridComponent.setDragDropOwner(this);
gridComponent.registerWithOwner();
gridComponent.setDragEnabled(true);  // Enable after registration

// ❌ WRONG order - Enable before registration may not work correctly
gridComponent.setDragEnabled(true);
gridComponent.setDragDropOwner(this);
gridComponent.registerWithOwner();
```

## Troubleshooting

### Events Not Received by Owner

**Problem**: Owner's handler methods are not being called.

**Solution**:
1. Verify owner is set: `component.getDragDropOwner()` should not be null
2. Verify registration was called: `component.registerWithOwner()`
3. Check handler method naming: Must follow `on_{componentName}_{action}` pattern
4. Verify component is registered with page service: Check `CPageService.bindMethods()` logs
5. Enable debug logging: Look for `[DragDebug]` messages in logs

### IllegalStateException on registerWithOwner()

**Problem**: `registerWithOwner()` throws IllegalStateException.

**Solution**: Call `setDragDropOwner()` before `registerWithOwner()`:
```java
component.setDragDropOwner(this);  // Must be called first
component.registerWithOwner();     // Then call this
```

### Grid Implements IHasDrop Compilation Error

**Problem**: Adding IHasDrop to CGrid causes ambiguous method reference errors.

**Solution**: CGrid should NOT implement IHasDrop because Grid already has `addDropListener()`. Use Grid's built-in drop API:
```java
// ✅ CORRECT
public class CGrid<T> extends Grid<T> 
        implements IHasDragStart<T>, IHasDragEnd<T>, IHasDragControl {
    // No IHasDrop
}

// ❌ WRONG - Causes compilation errors
public class CGrid<T> extends Grid<T> 
        implements IHasDragStart<T>, IHasDragEnd<T>, IHasDrop<T> {
    // ERROR: Ambiguous addDropListener method
}
```

## Related Documentation

- [IHasDragControl Interface](../api/interfaces/IHasDragControl.java) - Interface definition
- [CPageService Event Binding](../services/pageservice/CPageService.java) - Automatic event binding mechanism
- [CDragDropEvent](../services/pageservice/CDragDropEvent.java) - Event data wrapper
- [CGrid Drag-Drop](../grid/domain/CGrid.java) - Grid implementation
- [CComponentGridEntity](../screens/view/CComponentGridEntity.java) - Grid component wrapper
