# Owner Notification Interfaces Implementation

## Overview
This document describes the implementation of owner notification interfaces that enable child components to notify their parent containers about user interactions such as selection changes, drag operations, and drop events.

## Problem Statement
Components like `CComponentBacklog` needed a standardized way to notify their parent containers about:
1. Selection changes - when users select/deselect items
2. Drag start events - when users start dragging items
3. Drop events - when users complete drop operations
4. Click events - when users click on components

## Solution

### New Interfaces Created

#### 1. ISelectionOwner<T>
**Purpose:** Notifies parent components when selection changes in a child component.

**Interface Definition:**
```java
public interface ISelectionOwner<T> {
    void onSelectionChanged(Set<T> selectedItems);
}
```

**Use Case:** A sprint planning page needs to know when items are selected in the backlog to enable/disable toolbar buttons.

#### 2. IDragOwner<T>
**Purpose:** Notifies parent components when drag operations start in a child component.

**Interface Definition:**
```java
public interface IDragOwner<T> {
    void onDragStart(Set<T> draggedItems);
}
```

**Use Case:** A sprint planning page needs to know when drag starts to highlight valid drop zones and prepare drop targets.

#### 3. IDropOwner<T>
**Purpose:** Notifies parent components when drop operations complete in a child component.

**Interface Definition:**
```java
public interface IDropOwner<T> {
    void onDropComplete(Set<T> droppedItems, Object targetComponent);
}
```

**Use Case:** A sprint planning page needs to know when items are dropped to refresh related components and update statistics.

### ClickNotifier Interface
**Status:** Already implemented by Vaadin base classes.

All C-prefixed layout components inherit `ClickNotifier` from their Vaadin base classes:
- `CDiv` → `Div` → `ClickNotifier<Div>`
- `CVerticalLayout` → `VerticalLayout` → `ClickNotifier<VerticalLayout>`
- `CHorizontalLayout` → `HorizontalLayout` → `ClickNotifier<HorizontalLayout>`
- `CFlexLayout` → `FlexLayout` → `ClickNotifier<FlexLayout>`
- `CSpan` → `Span` → `ClickNotifier<Span>`

**Usage:** Simply call `component.addClickListener(e -> handleClick())` - no implementation needed.

## Implementation Details

### Base Component Classes Updated

#### CComponentEntitySelection
Added support for all three owner interfaces:
- Owner fields: `selectionOwner`, `dragOwner`, `dropOwner`
- Setter methods: `setSelectionOwner()`, `setDragOwner()`, `setDropOwner()`
- Notification methods: `notifySelectionOwner()`, `notifyDragOwner()`, `notifyDropOwner()`
- Integration: `notifySelectionOwner()` called in `updateSelectionIndicator()`

#### CComponentListEntityBase
Added support for all three owner interfaces:
- Owner fields: `selectionOwner`, `dragOwner`, `dropOwner`
- Setter methods: `setSelectionOwner()`, `setDragOwner()`, `setDropOwner()`
- Notification methods: `notifySelectionOwner()`, `notifyDragOwner()`, `notifyDropOwner()`
- Integration: `notifySelectionOwner()` called in `on_gridItems_selected()`

#### CComponentBacklog
Integrated drag and drop notifications:
- `notifyDragOwner()` called when drag starts in `addDragStartListener()`
- `notifyDropOwner()` called after successful reordering in `handleInternalReordering()`

## Usage Pattern

### Parent Component Implementation
```java
public class CPageSprintView implements ISelectionOwner<CProjectItem<?>>, 
                                       IDragOwner<CProjectItem<?>>, 
                                       IDropOwner<CProjectItem<?>> {
    
    private CComponentBacklog backlogComponent;
    
    public CPageSprintView(final CSprint sprint) {
        // Create child component
        backlogComponent = new CComponentBacklog(sprint);
        
        // Register this page as the owner for all events
        backlogComponent.setSelectionOwner(this);
        backlogComponent.setDragOwner(this);
        backlogComponent.setDropOwner(this);
    }
    
    @Override
    public void onSelectionChanged(final Set<CProjectItem<?>> selectedItems) {
        // Handle selection change
        updateToolbarButtons(!selectedItems.isEmpty());
    }
    
    @Override
    public void onDragStart(final Set<CProjectItem<?>> draggedItems) {
        // Handle drag start
        enableDropTargets();
        highlightValidDropZones();
    }
    
    @Override
    public void onDropComplete(final Set<CProjectItem<?>> droppedItems, final Object targetComponent) {
        // Handle drop completion
        refreshRelatedComponents();
        updateStatistics();
    }
}
```

### Selective Implementation
You can implement only the interfaces you need:
```java
public class CPageActivityView implements ISelectionOwner<CProjectItem<?>> {
    
    private CComponentBacklog backlogComponent;
    
    public CPageActivityView(final CSprint sprint) {
        backlogComponent = new CComponentBacklog(sprint);
        
        // Only register for selection events (not drag/drop)
        backlogComponent.setSelectionOwner(this);
    }
    
    @Override
    public void onSelectionChanged(final Set<CProjectItem<?>> selectedItems) {
        // Handle selection change only
    }
}
```

### ClickNotifier Usage
```java
public void setupClickHandling() {
    CDiv myDiv = new CDiv();
    
    // Direct usage - no implementation needed
    myDiv.addClickListener(e -> {
        System.out.println("Div clicked!");
    });
    
    CHorizontalLayout layout = new CHorizontalLayout();
    
    // Works on any C-prefixed layout component
    layout.addClickListener(e -> {
        System.out.println("Layout clicked!");
    });
}
```

## Benefits

1. **Decoupling:** Parent and child components are loosely coupled through interfaces
2. **Flexibility:** Parent can implement only the interfaces it needs
3. **Standardization:** Consistent pattern across all components
4. **Type Safety:** Generic types ensure compile-time type checking
5. **Extensibility:** Easy to add more owner interfaces in the future

## Testing

Example tests are provided in `OwnerInterfacesUsageExample.java` demonstrating:
- How to implement owner interfaces
- How to register parent components
- How to use interfaces selectively
- ClickNotifier usage patterns

## Files Modified

### Created:
- `src/main/java/tech/derbent/api/interfaces/ISelectionOwner.java`
- `src/main/java/tech/derbent/api/interfaces/IDragOwner.java`
- `src/main/java/tech/derbent/api/interfaces/IDropOwner.java`
- `src/test/java/tech/derbent/api/interfaces/OwnerInterfacesUsageExample.java`

### Modified:
- `src/main/java/tech/derbent/api/ui/component/enhanced/CComponentEntitySelection.java`
- `src/main/java/tech/derbent/api/ui/component/enhanced/CComponentListEntityBase.java`
- `src/main/java/tech/derbent/api/ui/component/enhanced/CComponentBacklog.java`
- `src/main/java/tech/derbent/api/ui/component/basic/CDiv.java` (documentation)
- `src/main/java/tech/derbent/api/ui/component/basic/CVerticalLayout.java` (documentation)
- `src/main/java/tech/derbent/api/ui/component/basic/CHorizontalLayout.java` (documentation)
- `src/main/java/tech/derbent/api/ui/component/basic/CFlexLayout.java` (documentation)
- `src/main/java/tech/derbent/api/ui/component/basic/CSpan.java` (documentation)

## Future Enhancements

Potential future additions:
- `IFocusOwner` - for focus change notifications
- `IValueChangeOwner` - for form field value changes
- `IResizeOwner` - for component resize events
- `IVisibilityOwner` - for visibility change notifications

## Notes

1. The owner interfaces use empty method bodies by default, so implementing classes only need to override methods they care about
2. Child components should always check if owner is not null before calling notification methods
3. Notification methods are protected, allowing subclasses to customize notification behavior
4. All notifications pass Set<T> for consistency, even for single-select components
