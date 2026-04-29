# Skills And Tools

Use shared facilities before adding new prose.

## Principles
- Prefer existing scripts, checklists, and task artifacts over re-explaining workflows.
- Open the smallest useful doc set.
- Keep one source of truth per workflow.

## Agent responsibilities
- `orchestrator`: create task workspace, phase order, and handoff points
- `analyzer`: profile selection, impact map, risks
- `pattern-designer`: locate existing patterns and relevant docs
- `coder`: implement the smallest correct change
- `verifier`: compile, rule checks, warning review
- `tester`: selective Playwright execution and log summary
- `documenter`: update canonical docs and add redirects/archive moves
- `todo-fix`: convert gaps into actionable follow-ups
- `cleanup`: identify stale or duplicated guidance

## Shared assets
- Profile rules: `.github/agents/_shared/PROFILE_AWARENESS.md`
- Memory rules: `.github/agents/_shared/MEMORY_SYSTEM.md`
- Skill modules: `.github/agents/_shared/skills/`
- Verification scripts: `.github/agents/verifier/scripts/`
- Architecture index: `docs/architecture/README.md`

## Preferred behavior
- Store intermediate reasoning in task artifacts.
- Reuse prior task outputs when handing off between agents.
- Replace duplicated docs with a canonical doc plus a redirect or archive move.
