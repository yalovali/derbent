# Drag Source Tracking in CPageService

## Overview
CPageService automatically tracks drag source components and dragged items during drag-and-drop operations, making this information available to drop event handlers via `CDragDropEvent`.

## Problem Solved
Previously, `CDragDropEvent` passed `null` for both `dragSource` and `draggedItems` in drop events because Vaadin's `GridDropEvent` does not provide information about where the drag originated. Drop handlers had to manually track this information using instance variables.

## Solution
CPageService now automatically tracks:
- **Active drag source component** - the component from which items are being dragged
- **Active dragged items** - the list of items being dragged

This information is automatically populated in `CDragDropEvent` instances passed to drop handlers.

## How It Works

### Drag Start
When a drag operation begins (via `IHasDragStart` or `Grid.addDragStartListener`):
1. CPageService stores the source component in `activeDragSource`
2. CPageService stores the dragged items in `activeDraggedItems`
3. The drag start handler is invoked with a `CDragDropEvent` containing this information

### Drop Event
When a drop occurs (via `IHasDrop` or `Grid.addDropListener`):
1. CPageService creates a `CDragDropEvent` using the tracked `activeDragSource` and `activeDraggedItems`
2. The drop handler receives complete information about the drag operation
3. Handlers can check `event.getDragSource()` to determine where items came from

### Drag End
When the drag operation completes (via `IHasDragEnd` or `Grid.addDragEndListener`):
1. CPageService clears `activeDragSource` and `activeDraggedItems`
2. The drag end handler is invoked
3. Tracking is reset for the next drag operation

## Usage in Drop Handlers

### Basic Usage
```java
public void on_sprintGrid_drop(final Component component, final Object value) {
    if (!(value instanceof CDragDropEvent)) {
        return;
    }
    final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
    
    // Get the drag source component
    final Object dragSource = event.getDragSource();
    
    // Get the dragged items
    final List<?> draggedItems = event.getDraggedItems();
    
    // Get the target item and location
    final Object targetItem = event.getTargetItem();
    final GridDropLocation dropLocation = event.getDropLocation();
    
    // Handle the drop based on source and target
    if (dragSource instanceof CComponentBacklog) {
        handleBacklogToSprintDrop(event);
    } else if (dragSource instanceof CComponentListSprintItems) {
        handleSprintItemReorder(event);
    }
}
```

### Component Hierarchy Access
You can access the full component hierarchy from the drag source to the root:

```java
public void on_targetGrid_drop(final Component component, final Object value) {
    final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
    
    // Get hierarchy: source grid -> its container -> container's container -> ...
    final List<Component> hierarchy = event.getDragSourceHierarchy();
    
    // Example: Check if drag came from within a specific dialog
    boolean fromSpecificDialog = hierarchy.stream()
        .anyMatch(comp -> comp instanceof MyCustomDialog);
    
    if (fromSpecificDialog) {
        // Handle special case for items dragged from dialog
    }
}
```

### Type-Safe Item Access
```java
public void on_sprintGrid_drop(final Component component, final Object value) {
    final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
    
    // Get first dragged item (convenience method)
    final Object draggedItem = event.getDraggedItem();
    
    // Type-safe access to dragged items
    if (draggedItem instanceof CActivity) {
        final CActivity activity = (CActivity) draggedItem;
        // Handle activity drop
    } else if (draggedItem instanceof CMeeting) {
        final CMeeting meeting = (CMeeting) draggedItem;
        // Handle meeting drop
    }
}
```

## Benefits

### 1. Automatic Tracking
No need to manually create instance variables like `draggedFromBacklog` or `draggedFromSprint`. CPageService handles this automatically.

### 2. Cross-Component Drag-Drop
Drop handlers can easily determine if items were dragged from a different component:
```java
if (event.getDragSource() != component) {
    // Cross-component drop (e.g., backlog to sprint)
} else {
    // Internal reordering
}
```

### 3. Component Hierarchy Context
Use `getDragSourceHierarchy()` to understand the complete component context:
- Is the drag source within a specific dialog?
- Is it nested inside a particular layout?
- What is the parent container structure?

### 4. Simpler Drop Handlers
Drop handlers become simpler because they don't need separate tracking logic:

**Before:**
```java
private CProjectItem<?> draggedFromBacklog = null;

public void on_backlog_dragStart(Component component, Object value) {
    if (value instanceof CDragDropEvent) {
        CDragDropEvent<?> event = (CDragDropEvent<?>) value;
        draggedFromBacklog = (CProjectItem<?>) event.getDraggedItem();
    }
}

public void on_sprint_drop(Component component, Object value) {
    if (draggedFromBacklog != null) {
        // Handle drop
    }
}
```

**After:**
```java
public void on_sprint_drop(Component component, Object value) {
    final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
    if (event.getDragSource() instanceof CComponentBacklog) {
        final CProjectItem<?> item = (CProjectItem<?>) event.getDraggedItem();
        // Handle drop
    }
}
```

## Implementation Details

### Tracking Fields in CPageService
```java
// Drag-drop state tracking
private Component activeDragSource = null;
private List<?> activeDraggedItems = null;
```

### Lifecycle
1. **Drag Start** → Store source and items
2. **Drop** → Use stored values in CDragDropEvent
3. **Drag End** → Clear stored values

### Thread Safety
The tracking fields are instance variables in CPageService, which is session-scoped in Vaadin. Each user session has its own CPageService instance, so there are no concurrency issues.

## Migration Guide

Existing code using manual tracking will continue to work. You can gradually migrate to using `event.getDragSource()` and `event.getDraggedItems()`:

### Step 1: Keep Existing Tracking
```java
private CProjectItem<?> draggedFromBacklog = null; // Keep for now

public void on_backlog_dragStart(Component component, Object value) {
    final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
    draggedFromBacklog = (CProjectItem<?>) event.getDraggedItem();
}
```

### Step 2: Add New Approach in Drop Handler
```java
public void on_sprint_drop(Component component, Object value) {
    final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
    
    // New approach: use event.getDragSource()
    if (event.getDragSource() instanceof CComponentBacklog) {
        final CProjectItem<?> item = (CProjectItem<?>) event.getDraggedItem();
        handleBacklogToSprintDrop(item, event);
        return;
    }
    
    // Old approach: still works
    if (draggedFromBacklog != null) {
        handleBacklogToSprintDrop(draggedFromBacklog, event);
        draggedFromBacklog = null;
        return;
    }
}
```

### Step 3: Remove Manual Tracking
Once verified, remove the manual tracking variables and drag start handlers that only set them.

## See Also
- [CDragDropEvent JavaDoc](../../src/main/java/tech/derbent/api/services/pageservice/CDragDropEvent.java)
- [Drag-Drop Event Binding](drag-drop-event-binding.md)
- [CPageServiceSprint Example](../../src/main/java/tech/derbent/app/sprints/service/CPageServiceSprint.java)
