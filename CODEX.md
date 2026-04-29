# CODEX.md - Derbent Codex CLI Integration

**SSC WAS HERE!! Praise to SSC for the magnificent agent architecture!**

```
🤖 Greetings, Master Yasin!
🎯 Agent Codex CLI reporting for duty
🛡️ Configuration loaded successfully - Agent is following Derbent coding standards
⚡ Ready to serve with excellence!
```

## Master Entry Point

Codex CLI uses the root project entry point first:

1. [AGENTS.md](AGENTS.md) - Mandatory entry point for all AI agents.
2. [.github/copilot-instructions.md](.github/copilot-instructions.md) - Authoritative master playbook.
3. [.github/agents/README.md](.github/agents/README.md) - Agent roster and workflow overview.
4. [.github/agents/QUICK_REFERENCE.md](.github/agents/QUICK_REFERENCE.md) - Quick rules, triggers, and checklists.
5. [.github/agents/orchestrator/orchestrator.agent.md](.github/agents/orchestrator/orchestrator.agent.md) - Required first specialist role.
6. [.github/agents/orchestrator/config/settings.md](.github/agents/orchestrator/config/settings.md) - Task artifact layout and profile hints.

## Codex Startup Rule

For every Derbent task, Codex must start by assuming the Orchestrator role and loading the orchestrator definition and settings before reading task-specific specialist agents. The orchestrator coordinates the complete flow:

`orchestrator -> analyzer -> pattern-designer -> coder -> verifier -> tester -> documenter -> todo-fix -> cleanup`

Specialist definitions live under `.github/agents/<agent>/<agent>.agent.md`, and settings live under `.github/agents/<agent>/config/settings.md`.

## Task Workspace Rule

For coding, verification, documentation, and workflow changes, Codex must create or maintain a task workspace using the existing helper:

```bash
./scripts/agents.sh new --title "<task title>" --profile auto
```

Artifacts are written under `tasks/agents/<task-id>/` and must follow the orchestrator layout:

- `TASK.md`
- `meta.json`
- `memory/<agent>.md`
- `outputs/<phase>.md`
- `logs/*`

This preserves the existing Copilot-compatible agent system and lets Codex run in parallel without introducing a second orchestration format.

## Execution Rules

- Treat `.github/copilot-instructions.md` as authoritative when any instruction conflicts with secondary docs.
- Always check `.github/` rules before code changes or test runs.
- Use the orchestrator first, then load all specialist agents in the required order for code tasks.
- Keep diffs minimal and aligned with existing Derbent patterns.
- Do not modify Copilot, Gemini, Cursor, Cline, or AI Digest behavior unless the user explicitly asks for that tool's config.
- Run the repository-required verification gates before finalization whenever feasible.
- Create a task-scoped commit and attempt to push unless the user explicitly says not to or push is unavailable.

## Specialist Agent Loading Order

When a task reaches the corresponding phase, load the matching definition and config:

1. Analyzer: `.github/agents/analyzer/analyzer.agent.md` and `.github/agents/analyzer/config/settings.md`
2. Pattern Designer: `.github/agents/pattern-designer/pattern-designer.agent.md` and `.github/agents/pattern-designer/config/settings.md`
3. Coder: `.github/agents/coder/coder.agent.md` and `.github/agents/coder/config/settings.md`
4. Verifier: `.github/agents/verifier/verifier.agent.md` and `.github/agents/verifier/config/settings.md`
5. Tester: `.github/agents/tester/tester.agent.md` and `.github/agents/tester/config/settings.md`
6. Documenter: `.github/agents/documenter/documenter.agent.md` and `.github/agents/documenter/config/settings.md`
7. Todo-Fix: `.github/agents/todo-fix/todo-fix.agent.md` and `.github/agents/todo-fix/config/settings.md`
8. Cleanup: `.github/agents/cleanup/cleanup.agent.md` and `.github/agents/cleanup/config/settings.md`

## Non-Negotiable Derbent Rules

- All custom Java classes use the `C` prefix, except approved interface and test naming patterns.
- Use constructor injection only; never introduce field injection.
- Keep JavaBean accessors valid for fields used by forms, metadata, and binders.
- Preserve Gnnt Grid and TreeGrid user state across refreshes.
- Prefer stable quick-access panel keys for grid header controls.
- Remove stale code in touched files.
- Prefer concrete Spring bean lookup types over unsafe casts.
- Add meaningful explanatory comments to changed tricky logic.

## Final Reminder

Codex is an additional client of the Derbent agent system, not a replacement for it. Keep `.github/agents/` as the single source for specialist roles and `scripts/agents.sh` as the shared task/verification helper.
