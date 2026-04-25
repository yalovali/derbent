# Agile Sprint Planning (Gnnt) - Drag-and-Drop Pattern

## Overview

Derbent sprint planning is implemented as a **Gnnt-style timeline board** driven by a project-scoped view entity.

Key invariants:

- **Backlog = sprintItem is NULL** on the sprintable entity.
- Sprint membership is owned by `CSprintItem` (composition-like helper attached to the sprintable item), not by the sprint.
- Use the convenience API on the sprintable item:
  - `ISprintableItem.moveSprintItemToSprint(targetSprint)`
  - `ISprintableItem.moveSprintItemToBacklog()`
- **Leaf-only rule (critical)**: only hierarchy leaf items (entity level **-1**) can be committed into a sprint.
  - Parent/group nodes remain visible for context but are blocked from assignment (DnD + dialog).

## Main building blocks

### View entity + board

- View entity: `tech.derbent.plm.sprints.planning.domain.CSprintPlanningViewEntity`
- Board component: `tech.derbent.plm.sprints.planning.view.CComponentSprintPlanningBoard`
- Pages:
  - "Sprint Planning Views" (CRUD page)
  - "Sprint Planning Board" (dedicated board page)

Why a view entity:
- Lets us ship a new planning UI without changing the core `CSprint` entity pages.
- Allows multiple saved planning “presets” per project (Release/Hotfix/etc.).

### Board layout (performance-first)

- **Sprints (top)**: `CSprintPlanningTreeGrid` (Sprint → Items) with a timeline column.
- **Backlog (bottom)**: `CSprintPlanningBacklogTreeGrid` (hierarchical backlog) with the same timeline column.
- **Details panel**: hidden by default; shown via a **toggle button** (planning mode stays fast).

### Quick-access widget / filters

`CSprintPlanningFilterToolbar` provides:
- Text search (name + description)
- Entity type filter
- Backlog state filter: Active / Closed / All
- Sprint state filter: Active / Closed / All
- Selected sprint metrics: **item count** + **story points sum**
- "Add to sprint" button (dialog-based planning, not only drag/drop)

## Drag-and-drop architecture

### Cross-grid drag context (server-side)

We avoid brittle UI assumptions by keeping a small server-side drag context:

- `CSprintPlanningDragContext` stores the currently dragged `CGnntItem` and source-grid id.
- Both grids set `setRowsDraggable(true)` and `setDropMode(GridDropMode.ON_TOP)`.

### Drop rules

- **Backlog → Sprint**
  - Drop target must resolve to a `CSprint` (dropping on a sprint row).
  - Block if target sprint is closed (final status).
  - Block if dragged entity is not leaf (level != -1).
  - Apply: `moveSprintItemToSprint(targetSprint)`.

- **Sprint → Backlog**
  - Dropping anywhere on backlog grid removes from sprint.
  - Apply: `moveSprintItemToBacklog()`.

- **Sprint → Sprint**
  - Same handler as Backlog → Sprint; the dragged item may already be in a sprint.
  - Apply: `moveSprintItemToSprint(otherSprint)`.

### Dialog-based assignment

Planning must not depend on drag/drop (mobile, touchpads, accessibility). The board provides:

- `CDialogAddBacklogItemToSprint` (select sprint → confirm)

The dialog enforces the same rules:
- leaf-only
- cannot add to closed sprint

## Refresh pattern

After any drop/dialog assignment:

- Call `CComponentSprintPlanningBoard.refreshComponent()`.
- The board rebuilds both hierarchies and keeps selection stable via entityKey restoration.
- Details panel updates only when it is visible (toggle-driven to avoid extra binder/layout work).

