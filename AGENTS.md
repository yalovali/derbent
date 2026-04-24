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
- **CFormBuilder/Binder rule (CRITICAL):** Any entity field referenced by screens/forms (e.g., `createLineFromDefaults(...)`, `@AMetaData`) MUST be a valid JavaBean property (public getter; and a public setter when the field is writable). Missing accessors can make Vaadin `Binder.bind("fieldName")` crash the page (common with `@Transient`/calculated fields).
- **Gnnt refresh state rule (CRITICAL):** any Gnnt Grid/TreeGrid refresh that calls `setItems(...)` MUST preserve user state (selection + scroll; for TreeGrid also expanded nodes) using stable `CGnntItem.getEntityKey()` keys.
- **Gnnt drag/drop safety:** refuse hierarchy moves while Gnnt filters are active; a filtered tree is not a complete snapshot of valid parent targets.
- **ComboBox icon display rule:** when using `CComboBoxOption` with icons/colors, update the prefix component on value change so the chosen icon is visible in the collapsed input (renderer affects overlay rows only).
- **No stale/unreachable code:** remove unused fields/methods/imports in touched files (they generate IDE warnings and hide real problems).
- **No unsafe casts when a concrete type exists:** prefer `CSpringContext.getBean(ConcreteService.class)` over registry + unchecked casts.
- If instructions conflict, ask for clarification before proceeding.

## Skills / reusable patterns (session)
- **Hierarchy children CRUD component** (KEYWORDS: `CComponentAgileChildren`, `placeHolder_createComponentParentChildren`, `CParentRelationService.setParent`, `CDialogEntitySelection`).
- **Playwright fast navigation** via Test Auxillary page + filters (KEYWORDS: `CPageComprehensiveTest`, `test.targetButtonText`, `test.routeKeyword`).

## Notes
- If `.github/` is missing or unreadable, explicitly state that and continue with best effort.
