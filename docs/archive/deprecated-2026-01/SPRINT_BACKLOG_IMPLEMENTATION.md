# Sprint Backlog Component Enhancement - Implementation Summary

## Overview

This implementation enhances the sprint management system with drag and drop functionality, enabling users to easily move items from a backlog to sprint items by dragging them between grids.

### Current binding rules (2025-12 refresh)
- Backlog = items where `sprintItem` is **NULL** on the sprintable entity (e.g., `CActivity.sprintItem`).
- Adding to a sprint must go through `CSprintItemService.save(...)`, which binds the sprintable item and populates `itemId/itemType`.
- Removing from a sprint must call `CSprintItemService.delete(...)`, which first clears `item.sprintItem` and then deletes the `CSprintItem`.
- Deleting a sprintable item (`CActivity`, `CMeeting`) clears and deletes its `CSprintItem`; deleting a `CSprintItem` does **not** delete the sprintable item.
- UI drag/drop code should refresh both backlog and sprint lists after any add/remove to reflect these invariants.
- `CComponentBacklog` now only requires a project (sprint is not needed) because backlog is defined globally by `sprintItem IS NULL`; hiding already-selected items is handled at the service layer.
- Refresh pattern: call component-level `refreshComponent()` on backlog, sprint items, and sprint widgets after drag/drop; update UI text in place (avoid tearing down/rebuilding labels or buttons).

## Changes Made

### 1. Bug Fix: CPageServiceSprint.createSpritBacklogComponent()

**Issue**: The method was returning `componentItemsSelection` instead of `componentBacklogItems`.

**Fix**:
- Corrected return statement to return the correct component
- Renamed method to `createBacklogItemsComponent()` for proper initialization
- Added proper instantiation with all required parameters

**Location**: `src/main/java/tech/derbent/app/sprints/service/CPageServiceSprint.java`

### 2. New Interfaces for Drag and Drop Pattern

#### IGridDragDropSupport Interface
**Location**: `src/main/java/tech/derbent/api/interfaces/IGridDragDropSupport.java`

Provides standardized drag support for grid components:
```java
public interface IGridDragDropSupport<T> {
    void setDragEnabled(boolean enabled);
    boolean isDragEnabled();
    void setDropHandler(Consumer<T> handler);
    Consumer<T> getDropHandler();
}
```

**Features**:
- Enable/disable row dragging
- Set handler for drag completion
- Query drag state

#### IDropTarget Interface
**Location**: `src/main/java/tech/derbent/api/interfaces/IDropTarget.java`

Provides standardized drop target support:
```java
public interface IDropTarget<T> {
    void setDropHandler(Consumer<T> handler);
    Consumer<T> getDropHandler();
    boolean isDropEnabled();
}
```

**Features**:
- Configure drop handling
- Check if drops are enabled
- Receive dropped items

### 3. Enhanced CComponentEntitySelection

**Location**: `src/main/java/tech/derbent/api/ui/component/enhanced/CComponentEntitySelection.java`

#### Implements IGridDragDropSupport<EntityClass>

**New Fields**:
```java
private boolean dragEnabled = false;
private Consumer<EntityClass> dropHandler = null;
private EntityClass draggedItem = null;
```

**New Methods**:

1. **Drag and Drop Support**:
   - `setDragEnabled(boolean)`: Enable/disable row dragging with visual feedback
   - `isDragEnabled()`: Check if drag is enabled
   - `setDropHandler(Consumer)`: Set callback for when items are dragged
   - `getDropHandler()`: Get current drop handler
   - `setupDragListeners()`: Configure drag start/end event handlers

2. **Event Handlers**:
   - `on_gridItems_dragStart(GridDragStartEvent)`: Handle drag start with visual feedback
   - `on_gridItems_dragEnd(GridDragEndEvent)`: Handle drag end and notify handler

3. **Utility Methods**:
   - `refresh()`: Reload items from provider
   - `setCurrentItem(EntityClass)`: Track current item in parent context
   - `getCurrentEntityType()`: Get selected entity type
   - `getSelectedCount()`: Count of selected items
   - `getAllItems()`: Get all loaded items
   - `getItemCount()`: Total item count

**Visual Feedback**:
- Drag start: Grid opacity set to 0.6 (semi-transparent)
- Drag end: Opacity restored to 1.0

### 4. Enhanced CComponentListSprintItems

**Location**: `src/main/java/tech/derbent/api/ui/component/enhanced/CComponentListSprintItems.java`

#### Implements IDropTarget<CProjectItem<?>>

**New Fields**:
```java
private Consumer<CProjectItem<?>> dropHandler = null;
```

**New Methods**:

1. **Drop Target Support**:
   - `setDropHandler(Consumer)`: Configure drop handling with GridDropMode.BETWEEN
   - `getDropHandler()`: Get current drop handler
   - `isDropEnabled()`: Check if drops are enabled

2. **Item Management**:
   - `addDroppedItem(CProjectItem)`: Handle dropped items
     - Creates CSprintItem from dropped CProjectItem
     - Saves to database
     - Refreshes grid
     - Shows success notification
     - Notifies change listeners

**Grid Configuration**:
- Drop mode set to `GridDropMode.BETWEEN` for ordered insertion
- Drop listener configured when handler is set

### 5. Enhanced CPageServiceSprint

**Location**: `src/main/java/tech/derbent/app/sprints/service/CPageServiceSprint.java`

#### Field Type Corrections:
```java
private CComponentEntitySelection<CProjectItem<?>> componentBacklogItems;
private CComponentListSprintItems componentItemsSelection;
```

#### New Methods:

1. **createBacklogItemsComponent()**: Factory method for backlog component
   - Creates entity type configurations for Activities and Meetings
   - Sets up items provider to load project items
   - Configures already-selected provider to hide sprint items
   - Returns fully configured CComponentEntitySelection

2. **setupDragAndDrop()**: Wires drag source and drop target
   - Enables drag on backlog component
   - Sets drop handler to:
     - Add item to sprint items
     - Refresh backlog to hide added item
   - Configures sprint items as drop target

#### Updated Methods:
- `createSpritActivitiesComponent()`: Added drop handler setup
- `createSpritBacklogComponent()`: Added drag/drop wiring call

### 6. Documentation

**Location**: `docs/implementation/drag-and-drop-pattern.md`

Comprehensive guide covering:
- Interface architecture and design
- Implementation examples with code snippets
- Step-by-step integration guide
- Visual feedback documentation
- Best practices and recommendations
- Troubleshooting common issues
- Future enhancement ideas

## Usage Workflow

### User Perspective

1. User opens sprint detail view
2. Two grids are displayed:
   - **Backlog Items**: Shows activities/meetings not in sprint
   - **Sprint Items**: Shows items currently in sprint
3. User selects entity type from backlog (Activity or Meeting)
4. Backlog displays available items (hiding already-added items)
5. User drags item from backlog
   - Grid becomes semi-transparent
6. User drops item onto sprint items grid
   - Item is added to sprint with next order number
   - Success notification appears
   - Backlog refreshes (item no longer shown)
   - Sprint items grid refreshes (item appears)

### Technical Flow

```
1. Backlog Component (CComponentEntitySelection)
   ↓ User drags item
2. Drag Start Event
   - Store dragged item
   - Apply visual feedback (opacity: 0.6)
   ↓
3. User drops on Sprint Items Grid
   ↓
4. Drag End Event
   - Remove visual feedback
   - Call dropHandler with dragged item
   ↓
5. Drop Handler in CPageServiceSprint
   - Call addDroppedItem() on sprint items component
   - Refresh backlog component
   ↓
6. Sprint Items Component (CComponentListSprintItems)
   - Create CSprintItem from dropped item
   - Save to database
   - Refresh grid
   - Show notification
```

## Key Benefits

1. **Reusable Pattern**: Interfaces can be applied to any grid component
2. **Type Safety**: Generic types ensure compile-time checks
3. **Visual Feedback**: Clear indication of drag operations
4. **Automatic Refresh**: Components stay synchronized
5. **Error Handling**: Comprehensive exception handling
6. **User Friendly**: Intuitive drag and drop interaction
7. **Maintainable**: Well-documented with clear responsibilities
8. **Extensible**: Easy to add drag/drop to other components

## Testing Checklist

- [ ] Compile project successfully
- [ ] Start application with H2 profile
- [ ] Navigate to sprint detail view
- [ ] Verify backlog component displays
- [ ] Select Activity entity type
- [ ] Verify activities appear in backlog
- [ ] Drag activity from backlog to sprint items
- [ ] Verify visual feedback during drag
- [ ] Verify item added to sprint items
- [ ] Verify item removed from backlog
- [ ] Verify success notification shown
- [ ] Repeat with Meeting entity type
- [ ] Test with no items in backlog
- [ ] Test with all items already in sprint
- [ ] Test error scenarios (DB errors, null items)

## Future Enhancements

1. **Multi-Select Drag**: Drag multiple items at once
2. **Undo Support**: Ability to undo drag operations
3. **Drag Between Sprints**: Move items between different sprints
4. **Custom Drag Images**: Show item preview during drag
5. **Drop Validation**: Business rules for allowed drops
6. **Keyboard Support**: Drag/drop via keyboard
7. **Mobile Support**: Touch-based drag and drop
8. **Performance**: Virtual scrolling for large lists

## Related Documentation

- [Drag and Drop Pattern Guide](./drag-and-drop-pattern.md)
- [Coding Standards](../architecture/coding-standards.md)
- [Component Patterns](../architecture/component-patterns.md)

## Notes

- Java 21 is required for compilation
- Vaadin 24.8.3 provides grid drag/drop APIs
- H2 profile recommended for development testing
- All code follows project naming conventions
- Comprehensive error handling throughout
- LOGGER statements for debugging and troubleshooting
