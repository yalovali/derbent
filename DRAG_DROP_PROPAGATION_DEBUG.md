# Drag-Drop Event Propagation - Debug Implementation

## Overview
This document explains the hierarchical drag-drop event propagation implementation with debug logging to trace events through the component chain.

## Component Hierarchy

The drag-drop events propagate through the following chain:

```
┌────────────────────────────────────┐
│ CGrid (internal to component)      │
│ - Grid drag events originate here  │
└──────────────┬─────────────────────┘
               │ GridDragStartEvent / GridDragEndEvent
               ↓
┌────────────────────────────────────┐
│ CComponentListEntityBase           │
│ - on_grid_dragStart()              │
│ - on_grid_dragEnd()                │
│ - Implements IHasDragStart/End     │
└──────────────┬─────────────────────┘
               │ Propagates via addDragStartListener()
               ↓
┌────────────────────────────────────┐
│ CComponentListSprintItems          │
│ - Overrides on_grid_dragStart()    │
│ - Overrides on_grid_dragEnd()      │
│ - Adds sprint-specific logging     │
└──────────────┬─────────────────────┘
               │ Contained in
               ↓
┌────────────────────────────────────┐
│ CComponentWidgetSprint             │
│ - addDragStartListener()           │
│ - addDragEndListener()             │
│ - Logs propagation to external     │
└──────────────┬─────────────────────┘
               │ IHasDragStart/End interface
               ↓
┌────────────────────────────────────┐
│ CPageService                       │
│ - bindDragStart()                  │
│ - bindDragEnd()                    │
│ - Invokes page service handlers    │
└──────────────┬─────────────────────┘
               │ Method invocation
               ↓
┌────────────────────────────────────┐
│ CPageServiceSprint                 │
│ - on_sprintItems_dragStart()       │
│ - on_sprintItems_dragEnd()         │
│ - Business logic handlers          │
└────────────────────────────────────┘
```

## Debug Logging Format

All debug messages use the `[DragDebug]` prefix for easy filtering:

```
[DragDebug] ComponentName: dragStart/dragEnd - source=X, items=Y, additionalInfo
```

### Example Log Output

When dragging a sprint item, you should see this sequence:

```
[DragDebug] CComponentListEntityBase<CSprintItem>: dragStart - source=grid, items=1
[DragDebug] CComponentListSprintItems: dragStart - source=grid, items=1, firstItemId=123
[DragDebug] CComponentWidgetSprint: dragStart propagated from componentSprintItems to external listener for sprint 456, items=1
[DragDebug] CPageService.bindDragStart: Invoking on_sprintItems_dragStart on component CComponentWidgetSprint, items=1
[DragDebug] CPageServiceSprint.on_sprintItems_dragStart: Sprint item drag started: 789 (itemId: 123)

... drag operation continues ...

[DragDebug] CComponentListEntityBase<CSprintItem>: dragEnd - source=grid
[DragDebug] CComponentListSprintItems: dragEnd - source=grid
[DragDebug] CComponentWidgetSprint: dragEnd propagated from componentSprintItems to external listener for sprint 456
[DragDebug] CPageService.bindDragEnd: Invoking on_sprintItems_dragEnd on component CComponentWidgetSprint
[DragDebug] CPageServiceSprint.on_sprintItems_dragEnd: Sprint items drag operation completed
```

## Implementation Details

### 1. CComponentListEntityBase
**File:** `src/main/java/tech/derbent/api/ui/component/enhanced/CComponentListEntityBase.java`

**Changes:**
- Added internal drag event listeners in `createGrid()` method
- Added `on_grid_dragStart(GridDragStartEvent<ChildEntity>)` method
- Added `on_grid_dragEnd(GridDragEndEvent<ChildEntity>)` method
- Both methods log with `[DragDebug]` prefix including entity class name and item count

**Purpose:** Base-level event capture and initial logging

### 2. CComponentListSprintItems
**File:** `src/main/java/tech/derbent/api/ui/component/enhanced/CComponentListSprintItems.java`

**Changes:**
- Override `on_grid_dragStart()` to add sprint-specific logging
- Override `on_grid_dragEnd()` to add sprint-specific logging
- Logs include first dragged item ID for better tracing
- Calls `super` methods to maintain base class logging

**Purpose:** Sprint-specific event details

### 3. CComponentWidgetSprint
**File:** `src/main/java/tech/derbent/app/sprints/view/CComponentWidgetSprint.java`

**Changes:**
- Enhanced `addDragStartListener()` to wrap listener with logging
- Enhanced `addDragEndListener()` to wrap listener with logging
- Logs when events are propagated to external listeners
- Includes sprint ID in log messages

**Purpose:** Track event propagation to external components

### 4. CPageService
**File:** `src/main/java/tech/derbent/api/services/pageservice/CPageService.java`

**Changes:**
- Added debug logging in `bindDragStart()` before method invocation
- Added debug logging in `bindDragEnd()` before method invocation
- Logs include method name, component class, and item count

**Purpose:** Track method binding and invocation

### 5. CPageServiceSprint
**File:** `src/main/java/tech/derbent/app/sprints/service/CPageServiceSprint.java`

**Changes:**
- Updated `on_backlogItems_dragStart()` with `[DragDebug]` prefix
- Updated `on_backlogItems_dragEnd()` with `[DragDebug]` prefix
- Updated `on_sprintItems_dragStart()` with `[DragDebug]` prefix
- Updated `on_sprintItems_dragEnd()` with `[DragDebug]` prefix

**Purpose:** Final handler logging at business logic level

## Testing Instructions

### Manual Testing

1. **Start the application:**
   ```bash
   cd /home/runner/work/derbent/derbent
   source ./bin/setup-java-env.sh
   mvn spring-boot:run -Dspring.profiles.active=h2
   ```

2. **Navigate to Sprint view:**
   - Login to the application
   - Open a Sprint entity
   - Expand the sprint items section

3. **Perform drag operation:**
   - Drag a sprint item within the sprint items grid
   - Observe the console/log output

4. **Expected log output:**
   ```
   [DragDebug] CComponentListEntityBase<CSprintItem>: dragStart - source=grid, items=1
   [DragDebug] CComponentListSprintItems: dragStart - source=grid, items=1, firstItemId=XXX
   [DragDebug] CComponentWidgetSprint: dragStart propagated from componentSprintItems to external listener for sprint YYY, items=1
   [DragDebug] CPageService.bindDragStart: Invoking on_sprintItems_dragStart on component CComponentWidgetSprint, items=1
   [DragDebug] CPageServiceSprint.on_sprintItems_dragStart: Sprint item drag started: ZZZ (itemId: XXX)
   
   ... on drag end ...
   
   [DragDebug] CComponentListEntityBase<CSprintItem>: dragEnd - source=grid
   [DragDebug] CComponentListSprintItems: dragEnd - source=grid
   [DragDebug] CComponentWidgetSprint: dragEnd propagated from componentSprintItems to external listener for sprint YYY
   [DragDebug] CPageService.bindDragEnd: Invoking on_sprintItems_dragEnd on component CComponentWidgetSprint
   [DragDebug] CPageServiceSprint.on_sprintItems_dragEnd: Sprint items drag operation completed
   ```

### Filtering Logs

To see only drag-drop debug messages:
```bash
# While application is running
tail -f logs/application.log | grep "\[DragDebug\]"
```

Or configure your IDE to filter console output by `[DragDebug]`.

## Verification Checklist

- [ ] Base component logs drag events from grid
- [ ] Sprint items component logs with item details
- [ ] Widget logs propagation to external listeners
- [ ] CPageService logs method invocation
- [ ] Page service handler receives and logs the event
- [ ] Complete chain is visible in logs for both dragStart and dragEnd
- [ ] No breaks in the chain (all 5+ log messages appear in sequence)

## Troubleshooting

### Missing Log Messages

If some log messages are missing:

1. **Check log level:** Ensure DEBUG level is enabled for relevant packages:
   ```properties
   logging.level.tech.derbent.api.ui.component.enhanced=DEBUG
   logging.level.tech.derbent.api.services.pageservice=DEBUG
   logging.level.tech.derbent.app.sprints=DEBUG
   ```

2. **Check component initialization:** Ensure `CComponentWidgetSprint` properly initializes `componentSprintItems` in `createSecondLine()`

3. **Check event binding:** Verify `CPageService.bindMethods()` is called and finds the components

### Broken Chain

If the chain breaks at a specific point:

1. **After CComponentListEntityBase:** Check that grid's drag listeners are properly added in `createGrid()`
2. **After CComponentListSprintItems:** Verify the component properly overrides parent methods and calls `super`
3. **After CComponentWidgetSprint:** Check that `IHasDragStart/End` interfaces are implemented correctly
4. **After CPageService:** Verify component is registered and method name follows `on_{componentName}_dragStart` pattern

## Benefits

1. **Visibility:** Complete visibility into drag-drop event flow
2. **Debugging:** Easy identification of where events are dropped or not propagated
3. **Tracing:** Consistent `[DragDebug]` prefix allows easy log filtering
4. **Documentation:** Self-documenting code with clear event chain
5. **Maintenance:** Future developers can quickly understand event propagation

## Future Enhancements

Potential improvements:
- Add drag position/coordinates to logs
- Add timing information (duration of drag operations)
- Add event ID to correlate dragStart with dragEnd
- Extend to other drag-drop components (backlog, etc.)
