# Kanban Post-it Event Flow

This board now forwards both **selection** and **drag/drop** notifications using the same pattern already used by sprint/backlog grids.

## Components in the Chain

1. `CComponentKanbanPostit` (card widget)
2. `CComponentKanbanColumn` (column container and drop target)
3. `CComponentKanbanBoard` (board aggregator, registered as `kanbanBoard`)
4. `CPageServiceKanbanLine` (`on_kanbanBoard_selected/dragStart/dragEnd/drop` handlers)

## Selection Propagation

1. Post-it click creates a `CSelectEvent` and calls `select_notifyEvents`.
2. `CComponentKanbanColumn` receives the event through `setupSelectionNotification` and forwards it.
3. `CComponentKanbanBoard` marks the clicked post-it as selected and passes the event to its listeners.
4. `CPageServiceKanbanLine.on_kanbanBoard_selected(...)` is invoked via the `on_{component}_{action}` binding.

## Drag/Drop Propagation

1. Post-it drag start creates `CDragStartEvent` with the sprint item and calls `notifyEvents`.
2. Column drop targets emit `CDragDropEvent` with the target column (or sprint item) and propagate it.
3. Board forwards all drag events and page service binding delivers them to:
   - `on_kanbanBoard_dragStart(...)` → caches the active drag start event.
   - `on_kanbanBoard_drop(...)` → resolves the target column and updates the sprint item.
   - `on_kanbanBoard_dragEnd(...)` → clears cached drag state.

## Notes

- All handlers use `Check` to fail fast and `LOGGER` entries with `[KanbanDrag]`/`[KanbanSelect]` tags for traceability.
- Column drop targets run with `DropEffect.MOVE` and set `GridDropLocation` to `EMPTY` when dropped onto the column background, keeping the drag payload consistent with grid-based components.
