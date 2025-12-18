# Codex Entry Point (Derbent)

This repository already contains a detailed playbook in `docs/development/AGENTS.md`. Read that first, then use this file as the quick “what matters most” index.

## Core Coding Rules (High Signal)
- **Naming is strict**: concrete classes start with `C*`, interfaces with `I*` (see `docs/architecture/coding-standards.md`).
- **Layering**: keep `domain/ → service/ → view/` separation; avoid UI logic in services and persistence logic in views (see `docs/architecture/service-layer-patterns.md`, `docs/architecture/view-layer-patterns.md`).
- **Singleton services are stateless**: never cache user/project/company state in service fields; always read context via `ISessionService` per call (see `docs/architecture/multi-user-singleton-advisory.md`).
- **Drag & drop**: use the unified `IHasDragControl` forwarding/notification pattern; refresh should be explicit and deterministic (see `docs/architecture/drag-drop-component-pattern.md` and `docs/implementation/drag-drop-unified-pattern.md`).

## Sprint/Backlog Invariants (Do Not Break)
- **Backlog definition**: backlog items are exactly the sprintable items where `sprintItem IS NULL` (FK `sprintitem_id` on the sprintable entity).
- **Add to sprint**: create a `CSprintItem` row and bind `item.sprintItem = thatSprintItem` (and set `CSprintItem.itemType/itemId`).
- **Remove from sprint**: clear `item.sprintItem` first, then delete the `CSprintItem` row (order matters to avoid FK violations).
- **Deletion semantics**:
  - Deleting a sprintable item (`CActivity`, `CMeeting`) must also delete its `CSprintItem` if present.
  - Deleting a `CSprintItem` must **not** delete the sprintable item.

## Where To Implement Sprint Rules
- **Binding/unbinding**: prefer `CSprintItemService.save(...)` and `CSprintItemService.delete(...)` (do not delete sprint items via repository directly).
- **Sprint delete**: delete sprint items through `CSprintItemService` before deleting the sprint, so items are unbound cleanly.
- **Queries**: repository “backlog” methods must include `... WHERE <entity>.sprintItem IS NULL`; “sprint members” methods should join through `<entity>.sprintItem.sprint`.

## Agent Execution Notes
- Follow user instructions about validation runs. If the user says “do not run tests”, do not run `mvn test`/`verify`; limit to code changes and static inspection unless explicitly asked.

