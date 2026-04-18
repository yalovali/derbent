# Orchestrator Settings

## Task folder layout

```
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
    ├── build.log
    └── tests.log
```

## Profile detection hints
- If the task mentions: Calimero, gateway, routing, interfaces, system metrics → **bab**.
- If the task mentions: Activities, Storage, Meetings, CRM, Kanban, Gantt → **derbent**.
- If changes touch only `tech.derbent.api.*` → **common** (verify both profiles).
