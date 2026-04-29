# Agent Memory System

Use file-based, task-scoped memory so agents can resume work without reloading large docs.

## Workspace
```text
tasks/agents/<task-id>/
├── TASK.md
├── meta.json
├── memory/
│   ├── orchestrator.md
│   ├── analyzer.md
│   ├── pattern-designer.md
│   ├── coder.md
│   ├── verifier.md
│   ├── tester.md
│   ├── documenter.md
│   ├── todo-fix.md
│   └── cleanup.md
├── outputs/
│   ├── 10-analysis.md
│   ├── 20-design.md
│   ├── 30-implementation.md
│   ├── 40-verification.md
│   ├── 50-tests.md
│   ├── 60-documentation.md
│   ├── 70-todo.md
│   └── 80-cleanup.md
└── logs/
```

## Rules
- Treat memory as append-only for the duration of a task.
- Record decisions once, then reference the task artifact instead of restating them in later prompts.
- Capture rationale, alternatives, impact, and evidence.
- Record unresolved work in `outputs/70-todo.md`.
- If a finding changes architecture or profile guidance, update the canonical doc and note that update in memory.

## What belongs where
- `TASK.md`: problem statement, acceptance criteria, explicit user constraints
- `memory/<agent>.md`: agent-local findings and decisions
- `outputs/10-analysis.md`: scope, profile, risks, candidate files
- `outputs/20-design.md`: pattern choice and implementation plan
- `outputs/30-implementation.md`: what changed
- `outputs/40-verification.md`: compile/rule check results
- `outputs/50-tests.md`: selective test results
- `outputs/60-documentation.md`: doc changes and redirects
- `outputs/70-todo.md`: remaining work
- `outputs/80-cleanup.md`: archive or redirect recommendations

## Minimal memory template
```markdown
# <Agent> Memory

## Context
- Task:
- Profile:
- Scope:

## Decisions
- D1:

## Evidence
- file:line -> observation

## Next actions
- A1:
```
