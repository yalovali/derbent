# Drag-Drop Notification Propagation Pattern

## Overview
This document explains the drag-drop notification propagation pattern implemented in the Derbent framework, specifically how drag-drop events from internal grids are propagated through component hierarchies to external listeners.

## Problem Statement
When components contain internal grids with drag-drop functionality, external listeners (such as page services) need to be able to register for drag-drop events on those internal grids. However, direct access to internal components violates encapsulation principles.

## Solution: Interface-Based Event Propagation

### Core Interfaces

#### IHasDragStart<T>
Components implementing this interface can expose drag start events from their internal grids:
```java
public interface IHasDragStart<T> {
    Registration addDragStartListener(ComponentEventListener<GridDragStartEvent> listener);
}
```

#### IHasDragEnd<T>
Components implementing this interface can expose drag end events from their internal grids:
```java
public interface IHasDragEnd<T> {
    Registration addDragEndListener(ComponentEventListener<GridDragEndEvent> listener);
}
```

## Implementation Pattern

### Step 1: Base Component (CComponentListSprintItems)
The base component (`CComponentListEntityBase`) already implements both interfaces and delegates to its internal grid:

```java
public abstract class CComponentListEntityBase<MasterEntity, ChildEntity>
        implements IHasDragStart<ChildEntity>, IHasDragEnd<ChildEntity> {
    
    protected CGrid<ChildEntity> grid;
    
    @Override
    public Registration addDragStartListener(
            ComponentEventListener<GridDragStartEvent<ChildEntity>> listener) {
        Check.notNull(listener, "Drag start listener cannot be null");
        Check.notNull(grid, "Grid must be initialized before adding drag start listener");
        return grid.addDragStartListener(listener);
    }
    
    @Override
    public Registration addDragEndListener(
            ComponentEventListener<GridDragEndEvent<ChildEntity>> listener) {
        Check.notNull(listener, "Drag end listener cannot be null");
        Check.notNull(grid, "Grid must be initialized before adding drag end listener");
        return grid.addDragEndListener(listener);
    }
}
```

### Step 2: Wrapper Component (CComponentWidgetSprint)
The wrapper component implements the same interfaces and delegates to its internal component:

```java
public class CComponentWidgetSprint extends CComponentWidgetEntityOfProject<CSprint> 
        implements IHasDragStart<CSprintItem>, IHasDragEnd<CSprintItem> {
    
    private CComponentListSprintItems componentSprintItems;
    
    // IHasDragStart implementation - propagate drag events from internal grid
    @Override
    public Registration addDragStartListener(
            ComponentEventListener<GridDragStartEvent<CSprintItem>> listener) {
        Check.notNull(listener, "Drag start listener cannot be null");
        if (componentSprintItems == null) {
            LOGGER.warn("componentSprintItems not initialized, cannot add drag start listener");
            return () -> {}; // Return empty registration
        }
        return componentSprintItems.addDragStartListener(listener);
    }
    
    // IHasDragEnd implementation - propagate drag events from internal grid
    @Override
    public Registration addDragEndListener(
            ComponentEventListener<GridDragEndEvent<CSprintItem>> listener) {
        Check.notNull(listener, "Drag end listener cannot be null");
        if (componentSprintItems == null) {
            LOGGER.warn("componentSprintItems not initialized, cannot add drag end listener");
            return () -> {}; // Return empty registration
        }
        return componentSprintItems.addDragEndListener(listener);
    }
}
```

### Step 3: Enable Drag-Drop on Internal Grid
The internal grid must have drag-drop enabled:

```java
private void createSprintItemsComponent() throws Exception {
    // ... component creation code ...
    
    // Enable drag-drop on the grid for external drag-drop operations
    if (componentSprintItems.getGrid() != null) {
        componentSprintItems.getGrid().setRowsDraggable(true);
        LOGGER.debug("Drag-drop enabled on sprint items grid within widget");
    }
}
```

### Step 4: Register Listeners (Page Service or External Components)
External components can now register for drag-drop events:

```java
public class CPageServiceSprint extends CPageServiceDynamicPage<CSprint> {
    
    public void setupSprintWidget(CComponentWidgetSprint widget) {
        // Register drag start listener
        widget.addDragStartListener(event -> {
            CSprintItem draggedItem = event.getDraggedItems().get(0);
            LOGGER.debug("Drag started: {}", draggedItem.getId());
            // Handle drag start
        });
        
        // Register drag end listener
        widget.addDragEndListener(event -> {
            LOGGER.debug("Drag ended");
            // Handle drag end
        });
    }
}
```

## Key Design Principles

### 1. Encapsulation
- Internal components remain private
- Only controlled interfaces are exposed
- Implementation details are hidden

### 2. Null Safety
- Null checks prevent NullPointerExceptions
- Empty registrations returned when component not initialized
- Defensive programming throughout

### 3. Type Safety
- Generic type parameters ensure type-safe event handling
- Compile-time type checking prevents errors
- Clear type signatures in all interfaces

### 4. Delegation Pattern
- Each layer delegates to the next
- No direct grid access from external code
- Clean separation of concerns

### 5. Logging
- Warnings when components not initialized
- Debug logging for event propagation
- Helpful for troubleshooting

## Integration with CPageService

The `CPageService` class supports automatic method binding for drag-drop events:

```java
// In page service constructor:
registerComponent("sprintItems", componentSprintItems.getGrid());
bindMethods(this);

// Automatic binding creates these handlers:
public void on_sprintItems_dragStart(Component component, Object value) {
    CDragDropEvent event = (CDragDropEvent) value;
    // Handle drag start
}

public void on_sprintItems_dragEnd(Component component, Object value) {
    CDragDropEvent event = (CDragDropEvent) value;
    // Handle drag end
}

public void on_sprintItems_drop(Component component, Object value) {
    CDragDropEvent event = (CDragDropEvent) value;
    // Handle drop
}
```

## Testing

Unit tests verify the pattern works correctly:

```java
@Test
void testImplementsIHasDragStart() {
    CSprint sprint = new CSprint();
    sprint.setName("Test Sprint");
    
    CComponentWidgetSprint widget = new CComponentWidgetSprint(sprint);
    
    Registration registration = widget.addDragStartListener(event -> {
        // Handler
    });
    
    assertNotNull(registration);
}
```

## Benefits

1. **Maintainability**: Clear interfaces make code easy to understand and modify
2. **Testability**: Interfaces enable easy mocking and testing
3. **Flexibility**: Easy to add new drag-drop enabled components
4. **Consistency**: Same pattern used throughout the framework
5. **Safety**: Null checks and type safety prevent runtime errors

## Usage Example

Complete example of drag-drop between two components:

```java
// Component 1: Backlog items (source)
CComponentBacklog backlog = new CComponentBacklog(sprint);
backlog.setDragEnabled(true);
backlog.addDragStartListener(event -> {
    draggedItem = event.getDraggedItems().get(0);
});

// Component 2: Sprint items (target)
CComponentWidgetSprint sprintWidget = new CComponentWidgetSprint(sprint);
sprintWidget.getGrid().setDropMode(GridDropMode.BETWEEN);
sprintWidget.getGrid().addDropListener(event -> {
    // Use draggedItem tracked from backlog
    sprintWidget.addDroppedItem(draggedItem);
});

// Cleanup on drag end
backlog.addDragEndListener(event -> {
    draggedItem = null;
});
```

## Related Files

- `src/main/java/tech/derbent/api/interfaces/IHasDragStart.java` - Drag start interface
- `src/main/java/tech/derbent/api/interfaces/IHasDragEnd.java` - Drag end interface
- `src/main/java/tech/derbent/api/ui/component/enhanced/CComponentListEntityBase.java` - Base implementation
- `src/main/java/tech/derbent/app/sprints/view/CComponentWidgetSprint.java` - Wrapper implementation
- `src/main/java/tech/derbent/app/sprints/service/CPageServiceSprint.java` - Page service integration
- `src/test/java/tech/derbent/app/sprints/view/CComponentWidgetSprintDragDropTest.java` - Unit tests
