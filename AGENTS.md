# AGENTS.md

Mandatory entry point for AI agents working in `/home/yasin/git/derbent`.

## Required read order
1. `.github/copilot-instructions.md`
2. `.github/agents/README.md`
3. `.github/agents/QUICK_REFERENCE.md`
4. Relevant agent definition: `.github/agents/<agent>/<agent>.agent.md`
5. Relevant agent config: `.github/agents/<agent>/config/settings.md`

## Canonical sources
- Repository-wide rules: `.github/copilot-instructions.md`
- Agent operating model: `.github/agents/README.md`
- Task memory, skills, and artifacts: `.github/agents/_shared/`
- Architecture and coding rules: `docs/architecture/README.md`
- BAB-specific rules: `docs/bab/CODING_RULES.md`

## Per-tool entry points
- Claude Code: `CLAUDE.md`
- Codex CLI: `CODEX.md`
- Gemini CLI: `GEMINI.md`

These are thin pointers. The shared `.github/agents/` system is the SSOT — every tool reuses the same orchestrator, specialists, configs, and `scripts/agents.sh` task helpers.

## Non-negotiable enforcement
- Read `.github/` before code changes or test runs.
- Treat `.github/copilot-instructions.md` as the single source of truth for active agent rules.
- Use the orchestrated agent workflow for every task.
- For code tasks, run compile with warnings visible and fix warnings introduced in touched files.
- For completed tasks, run at least one selective Playwright test unless the user explicitly forbids tests.
- Add explanatory comments only when changed logic is not obvious.
- Finalize completed tasks with a git commit and push unless the user explicitly says not to or push is unavailable.

## Critical implementation guards
- Entity fields used by screens/forms must be valid JavaBean properties. Missing getters or writable setters can break Vaadin `Binder.bind("fieldName")`.
- Gnnt `setItems(...)` refreshes must preserve user state by stable `CGnntItem.getEntityKey()` values. For `TreeGrid`, preserve expansion state too.
- Prefer `CQuickAccessPanel` via `CAbstractGnntGridBase#setQuickAccessPanel(...)` or `setLeftHeaderComponent(...)` instead of ad hoc grid sidebars.
- Use constructor injection only. Never use field injection or `@Autowired` on fields.
- When using `CComboBoxOption` icons/colors, update the prefix component when the value changes so the selected icon is visible in the collapsed field.
- Remove unused imports, fields, and methods in touched files.
- Prefer concrete bean lookup over unchecked casts.

## Agent facilities
- Use task-scoped artifacts under `tasks/agents/<task-id>/`.
- Use file-based memory in `tasks/agents/<task-id>/memory/`.
- Record phased outputs in `tasks/agents/<task-id>/outputs/`.
- Record follow-up work in `tasks/agents/<task-id>/outputs/70-todo.md`.
- Prefer shared skills and scripts from `.github/agents/_shared/` and `.github/agents/verifier/scripts/` over re-describing procedures inline.

## Documentation policy
- Active agent-facing docs live in:
  - `.github/copilot-instructions.md`
  - `.github/agents/**`
  - `docs/architecture/**`
  - `docs/bab/CODING_RULES.md`
  - `docs/implementation/PLAYWRIGHT_TEST_GUIDE.md`
- Historical material belongs under `docs/archive/**` or `.github/agents/archive/**`.
- Archive instead of deleting when retiring outdated guidance still referenced by code.

## If instructions conflict
Stop and ask for clarification before proceeding.
