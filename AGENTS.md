# AGENTS.md

Mandatory entry point for AI agents working in `/home/yasin/git/derbent`.

<<<<<<< HEAD
## Required read order
1. `.github/copilot-instructions.md`
2. `.github/agents/README.md`
3. `.github/agents/QUICK_REFERENCE.md`
4. Relevant agent definition: `.github/agents/<agent>/<agent>.agent.md`
5. Relevant agent config: `.github/agents/<agent>/config/settings.md`
=======
## Specialized Entry Points
- **[CLAUDE.md](CLAUDE.md)** - Specific entry point for Claude Code (CLI + IDE extensions).
- **[CODEX.md](CODEX.md)** - Specific entry point for Codex CLI agent.
- **[GEMINI.md](GEMINI.md)** - Specific entry point for Gemini CLI agent.
>>>>>>> branch 'main' of https://github.com/yalovali/derbent

<<<<<<< HEAD
## Canonical sources
- Repository-wide rules: `.github/copilot-instructions.md`
- Agent operating model: `.github/agents/README.md`
- Task memory, skills, and artifacts: `.github/agents/_shared/`
- Architecture and coding rules: `docs/architecture/README.md`
- BAB-specific rules: `docs/bab/CODING_RULES.md`
=======
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
>>>>>>> branch 'main' of https://github.com/yalovali/derbent

## Non-negotiable enforcement
- Read `.github/` before code changes or test runs.
- Treat `.github/copilot-instructions.md` as the single source of truth for active agent rules.
- Use the orchestrated agent workflow for every task.
- For code tasks, run compile with warnings visible and fix warnings introduced in touched files.
- For completed tasks, run at least one selective Playwright test unless the user explicitly forbids tests.
- Add explanatory comments when changed logic is not obvious.
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
- Archive instead of deleting when retiring outdated guidance.

## If instructions conflict
Stop and ask for clarification before proceeding.
