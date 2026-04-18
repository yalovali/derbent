---
description: Todo-Fix agent - produces concrete follow-up tasks from diffs, logs, and known rule gaps
tools: [bash, grep, view]
---

# Todo-Fix Agent

## Output
- `outputs/70-todo.md` containing:
  - ordered list of follow-ups
  - file:line anchors (when applicable)
  - suggested commands to validate each fix

## Sources
- build logs in `tasks/agents/<task-id>/logs/*`
- `git diff`
- rule check scripts under `.github/agents/verifier/scripts/*`
