# AGENTS.md (Root) - Derbent AI Agent Entry Point

This file is the mandatory entry point for any AI agent or automation running in this repository.

## Specialized Entry Points
- **[GEMINI.md](GEMINI.md)** - Specific entry point for Gemini CLI agent.

## Required reading order
1. `.github/copilot-instructions.md` (master playbook; mandatory rules)
2. `.github/agents/README.md` (agent roster and workflow)
3. `.github/agents/QUICK_REFERENCE.md` (quick rules + triggers)
4. Relevant agent definition (e.g., `.github/agents/coder/coder.agent.md`)
5. Relevant agent config (e.g., `.github/agents/coder/config/settings.md`)

## Non-negotiable enforcement
- Always check `.github/` for rules before any code changes or test runs.
- Treat `.github/copilot-instructions.md` as authoritative.
- **Task finalization rule:** after each completed task, create a git commit and push it to the tracked remote unless the user explicitly says not to or push is impossible in the current environment.
- **Playwright rule:** run at least one selective Playwright test for every completed task unless the user explicitly forbids tests.
- **CFormBuilder/Binder rule (CRITICAL):** Any entity field referenced by screens/forms (e.g., `createLineFromDefaults(...)`, `@AMetaData`) MUST be a valid JavaBean property (public getter; and a public setter when the field is writable). Missing accessors can make Vaadin `Binder.bind("fieldName")` crash the page (common with `@Transient`/calculated fields).
- **Gnnt refresh state rule (CRITICAL):** any Gnnt Grid/TreeGrid refresh that calls `setItems(...)` MUST preserve user state (selection + scroll; for TreeGrid also expanded nodes) using stable `CGnntItem.getEntityKey()` keys.
- **Gnnt drag/drop safety:** allow hierarchy moves even when Gnnt filters are active, but warn that the moved item may disappear from the filtered view (filters hide parts of the tree).
- **Grid header quick-access rule:** for Gnnt/SprintPlanning grids, prefer `CQuickAccessPanel` hosted via `CAbstractGnntGridBase#setQuickAccessPanel(...)` (or `setLeftHeaderComponent(...)`) instead of ad-hoc sidebars; add controls via stable keys (`panel.addControl("my-action", ...)`) so Playwright selectors stay predictable.
- **Spring DI warning rule:** never use field injection; specifically avoid `@Autowired` on `final` fields (constructor injection only).
- **ComboBox icon display rule:** when using `CComboBoxOption` with icons/colors, update the prefix component on value change so the chosen icon is visible in the collapsed input (renderer affects overlay rows only).
- **No stale/unreachable code:** remove unused fields/methods/imports in touched files (they generate IDE warnings and hide real problems).
- **No unsafe casts when a concrete type exists:** prefer `CSpringContext.getBean(ConcreteService.class)` over registry + unchecked casts.
- If instructions conflict, ask for clarification before proceeding.

## Skills / reusable patterns (session)
- **Hierarchy children CRUD component** (KEYWORDS: `CComponentAgileChildren`, `placeHolder_createComponentParentChildren`, `CParentRelationService.setParent`, `CDialogEntitySelection`).
- **Playwright fast navigation** via Test Auxillary page + filters (KEYWORDS: `CPageComprehensiveTest`, `test.targetButtonText`, `test.routeKeyword`).

## Notes
- Vaadin documentation: prefer the MCP server `vaadin` (configured in `mcp.json` as `servers.vaadin.url=https://mcp.vaadin.com/docs`) instead of ad-hoc web browsing.
- If `.github/` is missing or unreadable, explicitly state that and continue with best effort.
