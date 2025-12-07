# Agile Sprint Planning Drag-and-Drop Pattern

## Overview

This document describes the generalized drag-and-drop pattern used in Derbent for agile sprint planning. The pattern separates internal reordering from cross-component dragging using standard interfaces.

## Agile Sprint Planning Workflow

### Components

1. **Backlog (CComponentBacklog)**
   - Contains all project items NOT in the current sprint
   - Items ordered by `sprintOrder` field (priority/sequence)
   - Users can reorder items to adjust priority
   - Items can be dragged into sprint

2. **Sprint Items (CComponentListSprintItems)**
   - Contains items selected for the current sprint
   - Items ordered by `itemOrder` field (execution sequence)
   - Users can reorder items within sprint
   - Accepts items dropped from backlog

### User Actions

| Action | Component | Field Updated | Behavior |
|--------|-----------|---------------|----------|
| Reorder within backlog | Backlog | `sprintOrder` | Internal drag-drop, priority changes |
| Drag backlog → sprint | Both | `itemOrder` | Cross-grid drag-drop, adds to sprint |
| Reorder within sprint | Sprint Items | `itemOrder` | Internal drag-drop, execution order changes |
| Drag sprint → backlog | Both | None | Removes from sprint |

## Architecture Pattern

### Interfaces

The pattern uses two standard interfaces for drag-and-drop:

```java
// For components that support dragging FROM them
public interface IGridDragDropSupport<T> {
    void setDragEnabled(boolean enabled);
    boolean isDragEnabled();
    void setDropHandler(Consumer<T> handler);
    Consumer<T> getDropHandler();
}

// For components that can RECEIVE drops
public interface IDropTarget<T> {
    void setDropHandler(Consumer<T> handler);
    Consumer<T> getDropHandler();
    boolean isDropEnabled();
}
```

### Implementation Pattern

#### 1. Drag Source Component (Backlog)

```java
public class CComponentBacklog 
    extends CComponentEntitySelection<CProjectItem<?>> 
    implements IGridDragDropSupport<CProjectItem<?>> {
    
    private boolean dragEnabled = false;
    private Consumer<CProjectItem<?>> externalDropHandler = null;
    private CProjectItem<?> draggedItem = null;
    
    // Internal reordering configuration
    private void configureInternalDragAndDrop() {
        grid.setRowsDraggable(true);
        grid.setDropMode(GridDropMode.BETWEEN);
        
        // Track dragged item
        grid.addDragStartListener(event -> {
            draggedItem = event.getDraggedItems().get(0);
        });
        
        // Handle internal drops (reordering)
        grid.addDropListener(event -> {
            if (draggedItem != null && targetItem != null) {
                handleInternalReordering(draggedItem, targetItem);
            }
        });
    }
    
    // IGridDragDropSupport implementation
    @Override
    public void setDragEnabled(boolean enabled) {
        this.dragEnabled = enabled;
        grid.setRowsDraggable(enabled);
    }
    
    @Override
    public void setDropHandler(Consumer<CProjectItem<?>> handler) {
        this.externalDropHandler = handler;
    }
}
```

#### 2. Drop Target Component (Sprint Items)

```java
public class CComponentListSprintItems 
    extends CComponentListEntityBase<CSprint, CSprintItem>
    implements IDropTarget<CProjectItem<?>> {
    
    private Consumer<CProjectItem<?>> dropHandler = null;
    
    // IDropTarget implementation
    @Override
    public void setDropHandler(Consumer<CProjectItem<?>> handler) {
        dropHandler = handler;
        if (grid != null) {
            grid.setDropMode(GridDropMode.BETWEEN);
            // Drop listener configured by coordinator
        }
    }
    
    // Method to add dropped items
    public void addDroppedItem(CProjectItem<?> item) {
        // Create sprint item
        CSprintItem sprintItem = new CSprintItem();
        sprintItem.setItem(item);
        sprintItem.setItemOrder(getNextOrder());
        service.save(sprintItem);
        refreshGrid();
    }
}
```

#### 3. Coordinator (Page Service)

```java
public class CPageServiceSprint {
    private CComponentBacklog backlog;
    private CComponentListSprintItems sprintItems;
    
    private void setupDragAndDrop() {
        // Enable dragging from backlog
        backlog.setDragEnabled(true);
        
        // Track items dragged from backlog
        CProjectItem<?>[] draggedItem = new CProjectItem<?>[1];
        
        backlog.getGrid().addDragStartListener(event -> {
            draggedItem[0] = event.getDraggedItems().get(0);
        });
        
        // Configure sprint items to accept drops
        sprintItems.getGridItems().setDropMode(GridDropMode.BETWEEN);
        sprintItems.getGridItems().addDropListener(event -> {
            if (draggedItem[0] != null) {
                sprintItems.addDroppedItem(draggedItem[0]);
                backlog.refresh(); // Hide item from backlog
                draggedItem[0] = null;
            }
        });
    }
}
```

## Key Design Principles

### 1. Separation of Concerns

- **Internal Reordering**: Handled within each component
- **Cross-Component Drag**: Coordinated by page service
- **Visual Feedback**: Component responsibility
- **Data Persistence**: Service layer responsibility

### 2. Clear Responsibility Boundaries

| Component | Responsibility |
|-----------|----------------|
| Backlog | Internal reordering, sprintOrder updates, drag source |
| Sprint Items | Internal reordering, itemOrder updates, drop target |
| Page Service | Cross-component drag coordination |
| Services | Data persistence, validation |

### 3. Generalization

The pattern can be applied to any master-detail relationship:

```java
// Generic drag source
public class CComponentSource<T> implements IGridDragDropSupport<T> {
    // Internal reordering
    // External drag support
}

// Generic drop target  
public class CComponentTarget<T> implements IDropTarget<T> {
    // Drop handling
    // Item addition logic
}

// Coordinator
public class PageService {
    // Wire source → target
}
```

## Implementation Checklist

When implementing drag-and-drop for a new scenario:

### For Drag Source Component

- [ ] Implement `IGridDragDropSupport<T>` interface
- [ ] Configure internal drag-and-drop for reordering
- [ ] Add drag start/end listeners
- [ ] Handle internal drop events (reordering)
- [ ] Update ordering field (e.g., sprintOrder, displayOrder)
- [ ] Save changes to database
- [ ] Provide appropriate user feedback

### For Drop Target Component

- [ ] Implement `IDropTarget<T>` interface  
- [ ] Provide method to add dropped items
- [ ] Set grid drop mode (BETWEEN, ON_TOP, etc.)
- [ ] Calculate next order value
- [ ] Create appropriate entity relationships
- [ ] Save changes to database
- [ ] Refresh grid display

### For Coordinator (Page Service)

- [ ] Enable drag on source: `source.setDragEnabled(true)`
- [ ] Track dragged items from source
- [ ] Configure target drop mode
- [ ] Add drop listener to target grid
- [ ] Call target's add method on drop
- [ ] Refresh source to reflect changes
- [ ] Handle edge cases (validation, errors)

## Order Field Management

### Sprint Order (Backlog Priority)

```java
// Field in entity
@Column(name = "sprint_order", nullable = true)
private Integer sprintOrder;

// Query ordering
ORDER BY sprintOrder ASC NULLS LAST, id DESC

// Usage: Backlog displays items by priority
// Items with sprintOrder appear first, then items without (ordered by ID)
```

### Item Order (Sprint Execution Sequence)

```java
// Field in sprint item
@Column(name = "item_order", nullable = false)
private Integer itemOrder;

// Query ordering
ORDER BY itemOrder ASC

// Usage: Sprint items display in execution order
// Sequential numbering: 1, 2, 3, ...
```

## Best Practices

### 1. Consistent Notifications

```java
// Internal reordering
CNotificationService.showSuccess("Backlog priority updated");

// Cross-component add
CNotificationService.showSuccess("Item added to sprint");

// Remove from target
CNotificationService.showSuccess("Item removed from sprint");
```

### 2. Proper State Management

```java
// Clear dragged item on drag end
grid.addDragEndListener(event -> {
    draggedItem = null;
});

// Refresh both grids after cross-component operation
sprintItems.addDroppedItem(item);
backlog.refresh(); // Hide item from backlog
```

### 3. Validation

```java
// Check if item is already in target
if (isAlreadyInSprint(item)) {
    CNotificationService.showWarning("Item already in sprint");
    return;
}

// Check if item type is supported
if (!(item instanceof ISprintableItem)) {
    CNotificationService.showError("Item type not supported");
    return;
}
```

### 4. Error Handling

```java
try {
    handleReordering(draggedItem, targetItem, dropLocation);
} catch (Exception e) {
    LOGGER.error("Error reordering items", e);
    CNotificationService.showException("Error reordering items", e);
    // Rollback or refresh to restore correct state
    refresh();
}
```

## Future Extensions

### Adding New Entity Types

To add a new entity type to the backlog (e.g., CRisk, CIssue):

1. Implement `ISprintableItem` interface
2. Add `sprintOrder` field to entity
3. Create repository query with sprint order
4. Update `CComponentBacklog.createEntityTypes()`:

```java
private static List<EntityTypeConfig<?>> createEntityTypes() {
    List<EntityTypeConfig<?>> entityTypes = new ArrayList<>();
    // Existing types
    entityTypes.add(new EntityTypeConfig<>("CActivity", CActivity.class, activityService));
    entityTypes.add(new EntityTypeConfig<>("CMeeting", CMeeting.class, meetingService));
    // New type
    entityTypes.add(new EntityTypeConfig<>("CRisk", CRisk.class, riskService));
    return entityTypes;
}
```

5. Update `createItemsProvider()` to load new entity type

### Reverse Drag (Sprint → Backlog)

To enable dragging items back to backlog:

1. Implement `IGridDragDropSupport` in sprint items component
2. Make backlog implement `IDropTarget` as well
3. Add remove-from-sprint logic in coordinator
4. Clear `itemOrder`, keep `sprintOrder`

## Summary

This pattern provides:
- ✅ Clean separation between internal and cross-component drag-drop
- ✅ Reusable interfaces for any master-detail scenario
- ✅ Clear responsibility boundaries
- ✅ Consistent user experience
- ✅ Easy extensibility for new entity types
- ✅ Proper state management and error handling
