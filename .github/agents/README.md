# Derbent Agents

Canonical overview of the repository’s agent system.

## Purpose
Use specialized agents to keep work small, reviewable, and token-efficient. Agents should load the minimum required context, persist task state in files, and rely on shared skills/scripts instead of repeating procedures in prompts.

## Workflow
1. `orchestrator`: create the task workspace and phase plan
2. `analyzer`: determine scope, profile, risks, and impacted files
3. `pattern-designer`: find the closest existing implementation and applicable rule docs
4. `coder`: make the smallest compliant change
5. `verifier`: run rule checks and compile gate
6. `tester`: run at least one selective Playwright test unless forbidden
7. `documenter`: update only docs that changed meaningfully
8. `todo-fix`: capture remaining follow-ups
9. `cleanup`: archive or redirect stale guidance

## Task workspace
All multi-agent work should use:

```text
tasks/agents/<task-id>/
├── TASK.md
├── meta.json
├── memory/
├── outputs/
└── logs/
```

Use this workspace to avoid re-reading long docs across agents.

## Shared facilities
- Memory rules: `.github/agents/_shared/MEMORY_SYSTEM.md`
- Skills and tool preferences: `.github/agents/_shared/SKILLS_AND_TOOLS.md`
- Profile selection: `.github/agents/_shared/PROFILE_AWARENESS.md`
- Verification helpers: `.github/agents/verifier/scripts/*`

## Active agent docs
- `orchestrator/`
- `analyzer/`
- `pattern-designer/`
- `coder/`
- `verifier/`
- `tester/`
- `documenter/`
- `todo-fix/`
- `cleanup/`

Top-level `*.agent.md` files are compatibility aliases only. The subdirectory definitions are canonical.

## Documentation loading policy
- Read mandatory entry points first.
- Open `docs/architecture/README.md` before opening any specific architecture doc.
- Open only the one or two docs needed for the current task.
- Treat `docs/archive/**` and `.github/agents/archive/**` as historical only.

## Cleanup policy
- Do not delete old guidance blindly.
- Archive obsolete docs or replace them with short redirects.
- Keep one active source of truth per rule or workflow.
