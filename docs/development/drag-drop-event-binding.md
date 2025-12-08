# Drag-Drop Event Binding in CPageService

## Overview

This document describes the drag-and-drop event binding mechanism in `CPageService` that allows grid components to automatically notify their container page services about drag and drop operations using a consistent naming pattern.

## Architecture

The binding system follows the same pattern as existing event handlers:
- Method pattern: `on_{componentName}_{action}`
- Supported actions: `dragStart`, `dragEnd`, `drop`
- Event data passed via `CDragDropEvent` wrapper class

## CDragDropEvent Class

The `CDragDropEvent<T>` class wraps drag-drop event information:

```java
public class CDragDropEvent<T> {
    // Constructors
    CDragDropEvent(List<T> draggedItems, Object dragSource)  // For drag start
    CDragDropEvent(List<T> draggedItems, Object dragSource, T targetItem, 
                   GridDropLocation dropLocation, Object dropTarget)  // For drop
    
    // Key methods
    List<T> getDraggedItems()        // All dragged items
    T getDraggedItem()               // First dragged item (convenience)
    T getTargetItem()                // Item at drop location
    GridDropLocation getDropLocation() // ABOVE, BELOW, ON_TOP
    Object getDragSource()           // Source component
    Object getDropTarget()           // Target component
    boolean isDropEvent()            // true if drop, false if drag start
    boolean isDragStartEvent()       // true if drag start, false if drop
}
```

## Usage Pattern

### Step 1: Register Component

In your page service, register the component when it's created. You have two options:

**Option A: Register wrapper component directly (recommended if it implements interfaces)**
```java
public class CPageServiceSprint extends CPageServiceDynamicPage<CSprint> {
    private CComponentListSprintItems componentItemsSelection;
    private CComponentBacklog componentBacklogItems;
    
    public CComponentListSprintItems createSpritActivitiesComponent() {
        if (componentItemsSelection == null) {
            componentItemsSelection = new CComponentListSprintItems(...);
            
            // Register the component directly (implements IHasDragStart/IHasDragEnd)
            registerComponent("sprintItems", componentItemsSelection);
            
            // Re-bind methods to include the newly registered component
            bindMethods(this);
        }
        return componentItemsSelection;
    }
    
    public CComponentBacklog createSpritBacklogComponent() {
        if (componentBacklogItems == null) {
            componentBacklogItems = new CComponentBacklog(...);
            
            // Register the component directly (implements IHasDragStart/IHasDragEnd)
            registerComponent("backlogItems", componentBacklogItems);
            
            // Re-bind methods to include the newly registered component
            bindMethods(this);
        }
        return componentBacklogItems;
    }
}
```

**Option B: Register internal grid (backward compatibility)**
```java
public class CPageServiceSprint extends CPageServiceDynamicPage<CSprint> {
    private CComponentListSprintItems componentItemsSelection;
    
    public CComponentListSprintItems createSpritActivitiesComponent() {
        if (componentItemsSelection == null) {
            componentItemsSelection = new CComponentListSprintItems(...);
            
            // Register the grid directly (when component doesn't implement interfaces)
            if (componentItemsSelection.getGrid() != null) {
                registerComponent("sprintItems", componentItemsSelection.getGrid());
                bindMethods(this);
            }
        }
        return componentItemsSelection;
    }
}
```

### Step 2: Create Handler Methods

Create handler methods following the naming pattern `on_{componentName}_{action}`:

```java
/** Handler for drag start events on sprint items grid. */
public void on_sprintItems_dragStart(final Component component, final Object value) {
    if (value instanceof CDragDropEvent) {
        final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
        final List<?> draggedItems = event.getDraggedItems();
        
        LOGGER.info("Drag started with {} items", draggedItems.size());
        
        // Your logic here:
        // - Track dragged items for cross-component drag-drop
        // - Highlight potential drop targets
        // - Update UI state
    }
}

/** Handler for drag end events on sprint items grid. */
public void on_sprintItems_dragEnd(final Component component, final Object value) {
    if (value instanceof CDragDropEvent) {
        LOGGER.info("Drag operation completed");
        
        // Your logic here:
        // - Clean up drag-related UI state
        // - Clear temporary drag tracking
    }
}

/** Handler for drop events on sprint items grid. */
public void on_sprintItems_drop(final Component component, final Object value) {
    if (value instanceof CDragDropEvent) {
        final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
        final Object targetItem = event.getTargetItem();
        final GridDropLocation dropLocation = event.getDropLocation();
        
        LOGGER.info("Drop at location: {} relative to target: {}", 
                   dropLocation, targetItem);
        
        // Your logic here:
        // - Handle item insertion at specific position
        // - Reorder items based on drop location
        // - Process cross-component drag-drop
        
        if (dropLocation == GridDropLocation.BELOW) {
            // Insert after target
        } else {
            // Insert before target (ABOVE or ON_TOP)
        }
    }
}
```

## Complete Example: Cross-Component Drag-Drop

This example shows how to implement drag-from-backlog-to-sprint functionality:

```java
public class CPageServiceSprint extends CPageServiceDynamicPage<CSprint> {
    
    // Track items being dragged across components
    private CProjectItem<?> currentlyDraggedItem = null;
    
    // === DRAG FROM BACKLOG ===
    
    public void on_backlogItems_dragStart(final Component component, final Object value) {
        if (value instanceof CDragDropEvent) {
            final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
            if (!event.getDraggedItems().isEmpty()) {
                // Track the item being dragged
                currentlyDraggedItem = (CProjectItem<?>) event.getDraggedItem();
                LOGGER.info("Started dragging item {} from backlog", 
                           currentlyDraggedItem.getId());
            }
        }
    }
    
    public void on_backlogItems_dragEnd(final Component component, final Object value) {
        // Clear tracking
        LOGGER.info("Backlog drag ended");
        currentlyDraggedItem = null;
    }
    
    // === DROP ON SPRINT ITEMS ===
    
    public void on_sprintItems_drop(final Component component, final Object value) {
        if (value instanceof CDragDropEvent && currentlyDraggedItem != null) {
            final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
            final CSprintItem targetItem = (CSprintItem) event.getTargetItem();
            final GridDropLocation dropLocation = event.getDropLocation();
            
            LOGGER.info("Dropping backlog item {} into sprint at location {}", 
                       currentlyDraggedItem.getId(), dropLocation);
            
            // Add item to sprint at specific position
            componentItemsSelection.addDroppedItem(
                currentlyDraggedItem, targetItem, dropLocation);
            
            // Clear tracking
            currentlyDraggedItem = null;
            
            // Refresh backlog to reflect the change
            componentBacklogItems.refreshGrid();
        }
    }
    
    // === DRAG FROM SPRINT ITEMS (Reverse) ===
    
    private CSprintItem currentlyDraggedSprintItem = null;
    
    public void on_sprintItems_dragStart(final Component component, final Object value) {
        if (value instanceof CDragDropEvent) {
            final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
            if (!event.getDraggedItems().isEmpty()) {
                currentlyDraggedSprintItem = (CSprintItem) event.getDraggedItem();
                LOGGER.info("Started dragging sprint item {}", 
                           currentlyDraggedSprintItem.getId());
            }
        }
    }
    
    public void on_sprintItems_dragEnd(final Component component, final Object value) {
        // Clear tracking
        currentlyDraggedSprintItem = null;
    }
    
    // === DROP ON BACKLOG (Reverse) ===
    
    public void on_backlogItems_drop(final Component component, final Object value) {
        if (value instanceof CDragDropEvent && currentlyDraggedSprintItem != null) {
            final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
            final CProjectItem<?> targetItem = (CProjectItem<?>) event.getTargetItem();
            final GridDropLocation dropLocation = event.getDropLocation();
            
            LOGGER.info("Dropping sprint item {} back into backlog at location {}", 
                       currentlyDraggedSprintItem.getId(), dropLocation);
            
            final CProjectItem<?> item = currentlyDraggedSprintItem.getItem();
            
            // Set sprint order based on drop position
            if (targetItem != null && item instanceof ISprintableItem) {
                final ISprintableItem sprintableItem = (ISprintableItem) item;
                final ISprintableItem targetSprintableItem = 
                    (ISprintableItem) targetItem;
                
                final Integer targetOrder = targetSprintableItem.getSprintOrder();
                if (targetOrder != null) {
                    if (dropLocation == GridDropLocation.BELOW) {
                        sprintableItem.setSprintOrder(targetOrder + 1);
                    } else {
                        sprintableItem.setSprintOrder(targetOrder);
                    }
                    
                    // Save the item with new order
                    saveProjectItem(item);
                    
                    // Reorder other backlog items if needed
                    reorderBacklogItemsAfterInsert(
                        sprintableItem.getSprintOrder(), item.getId());
                }
            }
            
            // Remove from sprint
            componentItemsSelection.removeSprintItem(currentlyDraggedSprintItem);
            
            // Clear tracking
            currentlyDraggedSprintItem = null;
            
            // Refresh backlog to show item in correct position
            componentBacklogItems.refreshGrid();
        }
    }
    
    // Helper methods
    
    private void saveProjectItem(CProjectItem<?> item) {
        if (item instanceof CActivity) {
            activityService.save((CActivity) item);
        } else if (item instanceof CMeeting) {
            meetingService.save((CMeeting) item);
        }
    }
}
```

## Internal Grid Reordering

For internal reordering (dragging within the same grid), the Grid's built-in drop listeners handle this automatically. The handler methods are primarily for:

1. **Cross-component drag-drop** (between different grids)
2. **Custom drag start/end logic** (UI updates, tracking, validation)
3. **Position-aware operations** (insert at specific location)

## Benefits

1. **Clean Code**: Drag-drop logic is in named methods, not nested in setup code
2. **Maintainability**: Easy to find and modify drag-drop behavior
3. **Consistency**: Same pattern as other event handlers (on_name_change, etc.)
4. **Type Safety**: CDragDropEvent provides structured access to event data
5. **Flexibility**: Can handle internal and cross-component drag-drop uniformly

## Migration from Manual Wiring

**Before (manual event wiring):**
```java
private void setupDragAndDrop() {
    grid.addDragStartListener(event -> {
        // Inline logic hard to find and maintain
        draggedItem[0] = event.getDraggedItems().get(0);
        LOGGER.debug("Started dragging...");
    });
    
    grid.addDropListener(event -> {
        // More inline logic
        if (draggedItem[0] != null) {
            // Complex drop handling
        }
    });
}
```

**After (method binding):**
```java
// Component registration
registerComponent("sprintItems", sprintItemsGrid);
bindMethods(this);

// Handler methods
public void on_sprintItems_dragStart(Component component, Object value) {
    // Clear, maintainable drag start logic
}

public void on_sprintItems_drop(Component component, Object value) {
    // Clear, maintainable drop logic
}
```

## Limitations

1. **Component Interface Support**: Components must implement `IHasDragStart<T>` and/or `IHasDragEnd<T>` interfaces, or be Grid instances directly
2. **Drag End**: GridDragEndEvent doesn't provide dragged items (track in dragStart)
3. **Manual Registration**: Components must be explicitly registered with registerComponent()

## Interface-Based Binding

Components implementing `IHasDragStart<T>` and `IHasDragEnd<T>` interfaces are automatically supported without requiring direct Grid access. This provides better encapsulation for wrapper components:

```java
// Wrapper component with internal grid
public class CComponentBacklog extends CComponentEntitySelection<CProjectItem<?>> 
        implements IHasDragStart<CProjectItem<?>>, IHasDragEnd<CProjectItem<?>> {
    
    // Internal grid is encapsulated
    private CGrid<CProjectItem<?>> grid;
    
    @Override
    public Registration addDragStartListener(
            ComponentEventListener<GridDragStartEvent<CProjectItem<?>>> listener) {
        // Delegate to internal grid
        return grid.addDragStartListener(listener);
    }
    
    @Override
    public Registration addDragEndListener(
            ComponentEventListener<GridDragEndEvent<CProjectItem<?>>> listener) {
        // Delegate to internal grid
        return grid.addDragEndListener(listener);
    }
}

// Page service can register the wrapper component directly
registerComponent("backlogItems", componentBacklogItems);  // No need to access .getGrid()
bindMethods(this);
```

The binding precedence:
1. **First**: Check if component implements `IHasDragStart` / `IHasDragEnd`
2. **Second**: Check if component is a Grid instance
3. **Fallback**: Use generic DOM event listeners

This approach allows wrapper components to hide their internal structure while still propagating events properly.

## Best Practices

1. **Always check event type**: Use `instanceof CDragDropEvent` before casting
2. **Track items across events**: Store dragged items in dragStart for use in drop
3. **Clear tracking in dragEnd**: Always reset tracked items to avoid stale state
4. **Use position information**: Leverage GridDropLocation for insertion logic
5. **Refresh affected components**: Update both source and target grids after drop
6. **Log for debugging**: Include meaningful log messages for troubleshooting

## See Also

- `CPageService.java` - Base class with binding mechanism
- `CDragDropEvent.java` - Event wrapper class
- `CPageServiceSprint.java` - Example implementation
- `CComponentListSprintItems.java` - Grid component with drag-drop support
