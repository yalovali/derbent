# Unified Drag-Drop Pattern

## Overview

The Derbent application uses a unified drag-drop pattern based on the `IHasDragControl` interface. This pattern eliminates the need for manual state tracking by storing all drag-related data directly in event objects.

## Core Principles

### 1. **No Manual State Tracking**

❌ **Don't do this:**
```java
// BAD: Manual tracking fields
private CProjectItem<?> draggedFromBacklog = null;
private CSprintItem draggedFromSprint = null;

public void on_backlog_dragStart(...) {
    draggedFromBacklog = item; // Manual tracking
}

public void on_sprint_drop(...) {
    if (draggedFromBacklog != null) { ... }
}
```

✅ **Do this:**
```java
// GOOD: Use event data directly
public void on_sprint_drop(Component component, Object value) {
    CDragDropEvent<?> event = (CDragDropEvent<?>) value;
    Object draggedItem = event.getDraggedItem();
    if (draggedItem instanceof CProjectItem) {
        // Handle the drop using event data
    }
}
```

### 2. **Event Data Contains Everything**

All drag-drop events carry complete information:

- **CDragStartEvent**: Contains dragged items
- **CDragDropEvent**: Contains dragged items, source, target, and source list
- **CDragEndEvent**: Signals end of drag (rarely needed)

### 3. **Source List for Internal vs External Detection**

Use `event.getSourceList()` to detect if a drag is internal or external:

```java
public void on_backlogItems_drop(Component component, Object value) {
    CDragDropEvent<?> event = (CDragDropEvent<?>) value;
    
    // Check if source list contains backlog component
    boolean isInternalDrag = event.getSourceList().stream()
        .anyMatch(source -> source instanceof CComponentBacklog);
    
    if (isInternalDrag) {
        // Internal reordering - let component handle it
        return;
    } else {
        // External drop - handle cross-component operation
        handleExternalDrop(event);
    }
}
```

## Event Classes

### CDragStartEvent&lt;T>

Fired when drag starts. Contains:
- `getDraggedItems()`: List of dragged items
- `getDraggedItem()`: First dragged item (convenience)
- `getDragSource()`: Component from which drag started
- `getSourceList()`: List of all source components in hierarchy

### CDragDropEvent&lt;T>

Fired when drop occurs. Contains:
- `getDraggedItems()`: List of dragged items
- `getDraggedItem()`: First dragged item (convenience)
- `getDragSource()`: Component from which drag started
- `getDropTarget()`: Component where drop occurred
- `getTargetItem()`: Item at drop location (may be null)
- `getDropLocation()`: Drop position (ABOVE, BELOW, ON_TOP, EMPTY)
- `getSourceList()`: List of all source components in hierarchy

### CDragEndEvent

Fired when drag ends (success or cancel). Contains:
- `getDragSource()`: Component from which drag started
- `getSourceList()`: List of all source components in hierarchy

**Note:** CDragEndEvent is rarely needed since cleanup is handled automatically through event data.

## Implementation Pattern

### Component Level

Components implementing `IHasDragControl` forward events to parents:

```java
public class CComponentBacklog extends CComponentListEntityBase<CProjectItem<?>> 
        implements IHasDragControl {
    
    @Override
    public void on_dragStart(CDragStartEvent<?> event) {
        // Forward to parent
        notifyEvents(event);
    }
    
    @Override
    public void on_dragDrop(CDragDropEvent<?> event) {
        // Handle internal logic first
        handleInternalDrop(event);
        // Then forward to parent
        notifyEvents(event);
    }
}
```

### Page Service Level

Page services handle drop events using `on_{component}_{action}` pattern:

```java
public class CPageServiceSprint extends CPageServiceDynamicPage<CSprint> {
    
    public void on_backlogItems_drop(Component component, Object value) {
        CDragDropEvent<?> event = (CDragDropEvent<?>) value;
        
        // Detect drag type using source list
        boolean isInternal = event.getSourceList().stream()
            .anyMatch(s -> s instanceof CComponentBacklog);
        
        if (isInternal) {
            return; // Let component handle it
        }
        
        // Handle external drop
        if (event.getDraggedItem() instanceof CSprintItem) {
            moveSprintItemToBacklog((CSprintItem) event.getDraggedItem(), event);
        }
    }
    
    public void on_masterGrid_drop(Component component, Object value) {
        CDragDropEvent<?> event = (CDragDropEvent<?>) value;
        Object draggedItem = event.getDraggedItem();
        
        // Route based on dragged item type
        if (draggedItem instanceof CProjectItem) {
            handleBacklogToSprintDrop(event);
        } else if (draggedItem instanceof CSprintItem) {
            handleSprintItemReorder(event);
        }
    }
}
```

## Benefits

### 1. **Simpler Code**
- No manual tracking fields
- No dragStart handlers just for tracking
- No dragEnd handlers just for cleanup

### 2. **Type-Safe**
- Event data provides type information
- Can use instanceof checks
- Clear intent in code

### 3. **Automatic Propagation**
- Events bubble up component hierarchy
- Source list tracks complete path
- Easy to detect internal vs external drag

### 4. **Maintainable**
- Less state to manage
- Fewer potential bugs
- Clearer code flow

## CGrid Internal Tracking (Framework Boundary Exception)

**Important:** The "No Manual State Tracking" rule applies to **application code** only, not framework boundary layers.

`CGrid` is the **only exception** because it bridges Vaadin Grid to our custom event system:
- **Why tracking exists**: Vaadin's `GridDropEvent` API limitation - doesn't provide dragged items
- **Where it lives**: Inside `CGrid` class only (framework boundary)
- **Scope**: Tracks items from `GridDragStartEvent` to `GridDropEvent` conversion
- **Cleanup**: Automatically cleared in `GridDragEndEvent` handler

**Key distinction:**
- ❌ **Application code** (CPageServiceSprint, components) - NEVER track drag state
- ✅ **Framework boundary** (CGrid Vaadin bridge) - Acceptable for API limitations

Application developers should never need to know about `activeDraggedItems` - it's an internal implementation detail of CGrid's Vaadin Grid integration.

## Migration from Old Pattern

If you find code with manual tracking:

1. Remove tracking fields (`draggedFromXxx`)
2. Remove dragStart handlers that only set tracking fields
3. Remove dragEnd handlers that only clear tracking fields
4. Update drop handlers to use `event.getDraggedItem()` and `event.getSourceList()`

## Example: Complete Sprint Drag-Drop

```java
// Drop backlog item onto sprint
public void on_masterGrid_drop(Component component, Object value) {
    CDragDropEvent<?> event = (CDragDropEvent<?>) value;
    
    // Get dragged item from event (no manual tracking!)
    Object draggedItem = event.getDraggedItem();
    
    if (draggedItem instanceof CProjectItem) {
        // User dragged from backlog to sprint
        CProjectItem<?> item = (CProjectItem<?>) draggedItem;
        CSprint targetSprint = getTargetSprint(event);
        addItemToSprint(item, targetSprint);
    } else if (draggedItem instanceof CSprintItem) {
        // User reordered within sprint
        reorderSprintItem((CSprintItem) draggedItem, event);
    }
}

// Drop sprint item back to backlog
public void on_backlogItems_drop(Component component, Object value) {
    CDragDropEvent<?> event = (CDragDropEvent<?>) value;
    
    // Detect internal vs external using source list
    boolean isInternal = event.getSourceList().stream()
        .anyMatch(s -> s instanceof CComponentBacklog);
    
    if (isInternal) {
        return; // Internal reorder - component handles it
    }
    
    // External drop from sprint
    if (event.getDraggedItem() instanceof CSprintItem) {
        CSprintItem sprintItem = (CSprintItem) event.getDraggedItem();
        moveSprintItemToBacklog(sprintItem, event);
    }
}
```

## Key Takeaways

1. ✅ **All drag data in events** - Never store in component fields
2. ✅ **Use source list for detection** - Internal vs external drag
3. ✅ **Type check dragged items** - Route based on type
4. ❌ **No manual tracking** - Event carries all information
5. ❌ **No dragEnd for cleanup** - Automatic through event data
6. ❌ **No dragStart for tracking** - Event already has items

This pattern makes drag-drop operations simple, type-safe, and maintainable across the entire application.
