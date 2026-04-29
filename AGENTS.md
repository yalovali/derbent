# AGENTS.md (Root) - Derbent AI Agent Entry Point

This file is the mandatory entry point for any AI agent or automation running in this repository.

## Specialized Entry Points
- **[CLAUDE.md](CLAUDE.md)** - Specific entry point for Claude Code (CLI + IDE extensions).
- **[CODEX.md](CODEX.md)** - Specific entry point for Codex CLI agent.
- **[GEMINI.md](GEMINI.md)** - Specific entry point for Gemini CLI agent.

## Claude Code Integration
Claude Code must use this root file first, then load **[CLAUDE.md](CLAUDE.md)**. Claude Code starts every Derbent task as the Orchestrator Agent and reuses the shared `.github/agents/` definitions, configs, and `scripts/agents.sh` task artifacts. Project-level settings live in `.claude/settings.json`. This is an additive integration only; Copilot, Gemini, Codex, Cursor, Cline, and AI Digest configs remain independent clients of the same `.github/agents/` system.

## Codex CLI Integration
Codex CLI must use this root file first, then load **[CODEX.md](CODEX.md)**. Codex must start every Derbent task through the existing `.github/agents/orchestrator/` role and reuse the shared `.github/agents/` definitions, configs, and `scripts/agents.sh` task artifacts. This is an additive integration only; Copilot, Gemini, Cursor, Cline, and AI Digest configs remain independent clients of the same `.github/agents/` system.

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
- **Changed-code comment rule:** every task must leave meaningful explanatory comments in changed code where behavior or intent is not obvious; do not ship uncommented tricky logic.
- **Java warnings rule:** always run a Java compile with warnings visible for code tasks and treat warnings introduced in touched files as must-fix before completion.
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
