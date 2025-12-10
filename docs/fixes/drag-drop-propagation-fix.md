# Drag-Drop Event Propagation Fix

## Issue Description

When dragging sprint items from `CComponentWidgetSprint` (nested sprint items grid inside a widget) to the backlog grid, the dragged item was not being added back to backlog and not removed from the sprint item list.

### Observed Behavior (Before Fix)

The logs showed:
```
08:52:26.0 DEBUG [DragDebug] CComponentListEntityBase<CSprintItem>: dragStart - source=grid, items=1
08:52:26.0 DEBUG [DragDebug] CComponentListSprintItems: dragStart - source=grid, items=1, firstItemId=14
08:52:26.0 DEBUG [DragDebug] CComponentWidgetSprint: dragStart propagated from componentSprintItems to external listener for sprint 5, items=1
08:52:26.0 DEBUG [DragDebug] Widget CComponentWidgetSprint fired drag start, notifying CComponentGridEntity listeners
08:52:28.7 DEBUG [DragDebug] CComponentListEntityBase<CSprintItem>: dragEnd - source=grid
08:52:28.7 DEBUG [DragDebug] CComponentListSprintItems: dragEnd - source=grid
08:52:28.7 DEBUG [DragDebug] CComponentWidgetSprint: dragEnd propagated from componentSprintItems to external listener for sprint 5
08:52:28.7 DEBUG [DragDebug] Widget CComponentWidgetSprint fired drag end, notifying CComponentGridEntity listeners
```

**Missing**: No drop event logs from `CComponentGridEntity` to page service!

## Root Cause

In `CComponentGridEntity.registerWidgetComponentWithPageService()` (lines 920-943):
- Only `IHasDragStart` and `IHasDragEnd` interfaces were being checked and registered
- `IHasDrop` interface was **NOT** being checked or registered
- This broke the event propagation chain for drop events

### Event Propagation Chain

The complete drag-drop event flow should be:

```
1. User drags item from sprint widget
   CGrid (sprint items) → CComponentListEntityBase → CComponentListSprintItems → 
   CComponentWidgetSprint → CComponentGridEntity → CPageService → on_masterGrid_dragStart()

2. User drops on backlog grid
   CGrid (backlog) → CComponentBacklog → CPageService → on_backlogItems_drop()
```

However, the drop event propagation was missing the widget → grid entity connection.

## Solution

### Changes Made

1. **Added dropListeners list** (line 88):
   ```java
   private final List<ComponentEventListener<GridDropEvent<CEntityDB<?>>>> dropListeners = new ArrayList<>();
   ```

2. **Implemented notifyDropListeners method** (lines 791-808):
   ```java
   private void notifyDropListeners(final GridDropEvent event) {
       if (!dropListeners.isEmpty()) {
           LOGGER.debug("[DragDebug] Notifying {} drop listeners", dropListeners.size());
           for (final ComponentEventListener listener : dropListeners) {
               try {
                   listener.onComponentEvent(event);
               } catch (final Exception e) {
                   LOGGER.error("[DragDebug] Error notifying drop listener: {}", e.getMessage());
               }
           }
       }
   }
   ```

3. **Updated addDropListener** (lines 162-185):
   - Now stores listeners in `dropListeners` list for widget event propagation
   - Also adds to underlying grid for direct drops
   - Returns combined registration that removes from both

4. **Added IHasDrop registration** (lines 943-951):
   ```java
   if (component instanceof IHasDrop<?>) {
       final IHasDrop widgetWithDrop = (IHasDrop) component;
       widgetWithDrop.addDropListener(event -> {
           LOGGER.debug("[DragDebug] Widget {} fired drop, notifying CComponentGridEntity listeners",
                   component.getClass().getSimpleName());
           notifyDropListeners((GridDropEvent) event);
       });
   }
   ```

5. **Updated interface check** (line 920):
   ```java
   if (!(component instanceof IHasDragStart<?>) && !(component instanceof IHasDragEnd<?>) && !(component instanceof IHasDrop<?>)) {
   ```

## Expected Behavior (After Fix)

When dragging from sprint widget to backlog, the logs should now show:

```
[DragDebug] CComponentListEntityBase<CSprintItem>: dragStart - source=grid, items=1
[DragDebug] CComponentListSprintItems: dragStart - source=grid, items=1, firstItemId=14
[DragDebug] CComponentWidgetSprint: dragStart propagated from componentSprintItems
[DragDebug] Widget CComponentWidgetSprint fired drag start, notifying CComponentGridEntity listeners
[DragDebug] CPageServiceSprint.on_masterGrid_dragStart: Sprint item drag started

... (drag continues) ...

[DragDebug] CComponentBacklog received drop event
[DragDebug] Widget CComponentWidgetSprint fired drop, notifying CComponentGridEntity listeners    ← NEW!
[DragDebug] Notifying X drop listeners                                                              ← NEW!
[DragDebug] CPageServiceSprint.on_backlogItems_drop: Sprint item X dropped on backlog             ← NEW!
[DragDebug] Sprint item deleted, item moved to backlog                                             ← NEW!

[DragDebug] CComponentListEntityBase<CSprintItem>: dragEnd - source=grid
[DragDebug] CComponentListSprintItems: dragEnd - source=grid
[DragDebug] CComponentWidgetSprint: dragEnd propagated from componentSprintItems
[DragDebug] Widget CComponentWidgetSprint fired drag end, notifying CComponentGridEntity listeners
[DragDebug] CPageServiceSprint.on_masterGrid_dragEnd: Clearing dragged sprint item tracker
```

## Testing

### Unit Tests

Added comprehensive tests in `CComponentWidgetSprintDragDropTest`:

1. `testImplementsIHasDragStart()` - Verifies IHasDragStart interface
2. `testImplementsIHasDragEnd()` - Verifies IHasDragEnd interface
3. `testImplementsIHasDrop()` - **NEW** - Verifies IHasDrop interface
4. `testAllDragDropInterfacesImplemented()` - **NEW** - Verifies all three interfaces work together

All 5 tests passing successfully.

### Manual Testing Steps

To verify the fix works correctly:

1. Start the application with H2 profile:
   ```bash
   mvn spring-boot:run -Dspring.profiles.active=h2
   ```

2. Navigate to Sprint management view

3. Create a sprint with items

4. Drag an item from the sprint widget to the backlog grid

5. Verify:
   - Item is removed from sprint widget
   - Item appears in backlog grid
   - Success notification shown: "Item removed from sprint"
   - Application logs show complete event propagation chain

## Impact

### Fixed Components

- `CComponentGridEntity` - Now propagates drop events from widgets
- All widget components implementing `IHasDrop` inside grids

### Pattern for Future Development

**CRITICAL**: When implementing hierarchical drag-drop components where events need to propagate:

1. **ALL THREE interfaces must be checked and registered**:
   - `IHasDragStart`
   - `IHasDragEnd`
   - `IHasDrop`

2. **Follow the notification pattern**:
   - Store listeners in a list
   - Implement `notifyXxxListeners()` method
   - Call from widget event handlers

3. **Example template**:
   ```java
   // Check all three interfaces
   if (!(component instanceof IHasDragStart<?>) && 
       !(component instanceof IHasDragEnd<?>) && 
       !(component instanceof IHasDrop<?>)) {
       return; // Skip registration
   }
   
   // Register all three if implemented
   if (component instanceof IHasDragStart<?>) {
       ((IHasDragStart) component).addDragStartListener(event -> 
           notifyDragStartListeners(event));
   }
   if (component instanceof IHasDragEnd<?>) {
       ((IHasDragEnd) component).addDragEndListener(event -> 
           notifyDragEndListeners(event));
   }
   if (component instanceof IHasDrop<?>) {
       ((IHasDrop) component).addDropListener(event -> 
           notifyDropListeners(event));
   }
   ```

## References

- `CComponentGridEntity.java` - Main fix implementation
- `CComponentWidgetSprint.java` - Widget implementing all three interfaces
- `CPageServiceSprint.java` - Page service with drop handlers
- `CComponentWidgetSprintDragDropTest.java` - Comprehensive tests
