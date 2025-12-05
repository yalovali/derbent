# Drag and Drop Pattern Guide

## Overview

The Derbent application implements a reusable drag and drop pattern for grid components using two key interfaces:

- **IGridDragDropSupport**: For components that support dragging items FROM them
- **IDropTarget**: For components that can RECEIVE dropped items

## Architecture

### IGridDragDropSupport Interface

This interface enables grid components to support dragging items to other components.

```java
public interface IGridDragDropSupport<T> {
    void setDragEnabled(boolean enabled);
    boolean isDragEnabled();
    void setDropHandler(Consumer<T> handler);
    Consumer<T> getDropHandler();
}
```

**Key Features:**
- Enable/disable row dragging dynamically
- Visual feedback during drag operations (opacity changes)
- Handler callback when drag completes
- Drag start/end event tracking

### IDropTarget Interface

This interface allows components to receive items dropped from other components.

```java
public interface IDropTarget<T> {
    void setDropHandler(Consumer<T> handler);
    Consumer<T> getDropHandler();
    boolean isDropEnabled();
}
```

**Key Features:**
- Configure drop handling
- Grid drop mode configuration (BETWEEN, ON_TOP, etc.)
- Automatic grid refresh after drop
- Drop location tracking

## Implementation Example: Sprint Backlog to Sprint Items

### Source Component (Drag Source): CComponentEntitySelection

The backlog component implements `IGridDragDropSupport<EntityClass>`:

```java
public class CComponentEntitySelection<EntityClass extends CEntityDB<?>> 
    extends Composite<CVerticalLayout> 
    implements IGridDragDropSupport<EntityClass> {
    
    private boolean dragEnabled = false;
    private Consumer<EntityClass> dropHandler = null;
    private EntityClass draggedItem = null;
    
    @Override
    public void setDragEnabled(boolean enabled) {
        dragEnabled = enabled;
        if (gridItems != null) {
            gridItems.setRowsDraggable(enabled);
            if (enabled) {
                setupDragListeners();
            }
        }
    }
    
    private void setupDragListeners() {
        // Drag start - store item and add visual feedback
        gridItems.addDragStartListener(event -> {
            draggedItem = event.getDraggedItems().get(0);
            gridItems.getStyle().set("opacity", "0.6");
        });
        
        // Drag end - clear visual feedback and notify handler
        gridItems.addDragEndListener(event -> {
            gridItems.getStyle().remove("opacity");
            if (dropHandler != null && draggedItem != null) {
                dropHandler.accept(draggedItem);
            }
            draggedItem = null;
        });
    }
}
```

### Target Component (Drop Target): CComponentListSprintItems

The sprint items component implements `IDropTarget<CProjectItem<?>>`:

```java
public class CComponentListSprintItems 
    extends CComponentListEntityBase<CSprint, CSprintItem>
    implements IDropTarget<CProjectItem<?>> {
    
    private Consumer<CProjectItem<?>> dropHandler = null;
    
    @Override
    public void setDropHandler(Consumer<CProjectItem<?>> handler) {
        dropHandler = handler;
        if (getGridItems() != null) {
            if (handler != null) {
                getGridItems().setDropMode(GridDropMode.BETWEEN);
                getGridItems().addDropListener(event -> {
                    // Drop handling logic
                    LOGGER.debug("Item dropped at location: {}", 
                        event.getDropLocation());
                });
            } else {
                getGridItems().setDropMode(null);
            }
        }
    }
    
    public void addDroppedItem(CProjectItem<?> item) {
        // Create sprint item from dropped project item
        CSprintItem sprintItem = new CSprintItem();
        sprintItem.setSprint(getMasterEntity());
        sprintItem.setItemId(item.getId());
        sprintItem.setItemType(item.getClass().getSimpleName());
        sprintItem.setItemOrder(getNextOrder());
        sprintItem.setItem(item);
        
        // Save and refresh
        childService.save(sprintItem);
        refreshGrid();
        CNotificationService.showSuccess("Item added to sprint");
    }
}
```

### Wiring Components Together

The page service connects the drag source and drop target:

```java
public class CPageServiceSprint extends CPageServiceDynamicPage<CSprint> {
    
    private CComponentEntitySelection<CProjectItem<?>> componentBacklogItems;
    private CComponentListSprintItems componentItemsSelection;
    
    private void setupDragAndDrop() {
        if (componentBacklogItems != null && componentItemsSelection != null) {
            // Enable drag from backlog
            componentBacklogItems.setDragEnabled(true);
            
            // Set drop handler on backlog to handle drag completion
            componentBacklogItems.setDropHandler(item -> {
                // Add item to sprint
                componentItemsSelection.addDroppedItem(item);
                // Refresh backlog to hide added item
                componentBacklogItems.refresh();
            });
            
            // Enable drop on sprint items
            componentItemsSelection.setDropHandler(item -> 
                componentItemsSelection.addDroppedItem(item)
            );
        }
    }
}
```

## Visual Feedback

The pattern includes built-in visual feedback:

1. **Drag Start**: Grid opacity changes to 0.6 (semi-transparent)
2. **Drag Over**: Standard browser drag-over cursor
3. **Drop Location**: Vaadin grid shows drop indicator between rows
4. **Drag End**: Opacity restored to 1.0

## Best Practices

### 1. Always Refresh After Drop

When items are dropped and added to a target, refresh the source component to reflect the change:

```java
dropHandler = item -> {
    targetComponent.addItem(item);
    sourceComponent.refresh(); // Important!
};
```

### 2. Validate Before Adding

Check if the item can be added before processing the drop:

```java
public void addDroppedItem(Item item) {
    if (isItemAlreadyInList(item)) {
        CNotificationService.showWarning("Item already in list");
        return;
    }
    // Proceed with adding item
}
```

### 3. Use Appropriate Drop Modes

Choose the right `GridDropMode` for your use case:
- `BETWEEN`: Drop between rows (for ordered lists)
- `ON_TOP`: Drop on top of existing row (for hierarchies)
- `ON_GRID`: Drop anywhere on grid (for simple addition)

### 4. Provide User Feedback

Always notify users when operations complete:

```java
CNotificationService.showSuccess("Item added successfully");
```

### 5. Handle Errors Gracefully

Wrap drop operations in try-catch blocks:

```java
try {
    addDroppedItem(item);
} catch (Exception e) {
    LOGGER.error("Error adding dropped item", e);
    CNotificationService.showException("Error adding item", e);
}
```

## Testing Drag and Drop

### Manual Testing Checklist

1. **Drag Start**: Verify opacity change when drag begins
2. **Drag Over**: Check drop indicator appears at correct position
3. **Drop**: Confirm item is added to target component
4. **Source Refresh**: Verify item is removed/hidden from source
5. **Notifications**: Check success messages appear
6. **Error Handling**: Test with invalid data

### Common Issues

**Issue**: Items not appearing in target after drop
- **Cause**: Drop handler not properly wired
- **Solution**: Verify `setDropHandler` is called with correct callback

**Issue**: Items still visible in source after drop
- **Cause**: Source component not refreshed
- **Solution**: Call `refresh()` on source after successful drop

**Issue**: Visual feedback not showing
- **Cause**: Drag listeners not set up
- **Solution**: Ensure `setDragEnabled(true)` is called before interaction

## Future Enhancements

Potential improvements to the drag and drop pattern:

1. **Undo/Redo Support**: Track drag operations for undo functionality
2. **Multi-Select Drag**: Support dragging multiple items at once
3. **Cross-Component Drag**: Drag between different types of grids
4. **Custom Visual Indicators**: More sophisticated drag feedback
5. **Drag Constraints**: Validate if drop is allowed based on business rules
6. **Performance Optimization**: Lazy loading for large datasets

## Related Classes

- `CComponentEntitySelection`: Reusable entity selection with drag support
- `CComponentListSprintItems`: Sprint items list with drop support
- `CComponentListEntityBase`: Base class for entity list components
- `CDialogEntitySelection`: Modal dialog wrapping entity selection
- `IEntitySelectionDialogSupport`: Interface for entity selection configuration

## See Also

- [Vaadin Grid Drag and Drop Documentation](https://vaadin.com/docs/latest/components/grid/drag-and-drop)
- [Coding Standards](./architecture/coding-standards.md)
- [Component Patterns](./architecture/component-patterns.md)
