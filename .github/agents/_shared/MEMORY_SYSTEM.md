# Agent Memory System

This repository uses **file-based, task-scoped memory** to keep multi-agent work consistent, reviewable, and easy to resume.

## Where memory lives

- **Per task run**: `tasks/agents/<task-id>/memory/<agent>.md`
- **Never** store user-specific state inside Spring services (multi-user singleton rule).

## Memory rules

1. Memory is **append-only** during a task; if something becomes obsolete, mark it as **superseded**.
2. Every important decision must include:
   - the reason
   - alternatives considered
   - impact (profile: bab/derbent/common)
3. If a decision affects architecture/patterns, also update:
   - `.github/agents/_shared/PROFILE_AWARENESS.md` (if profile-related)
   - or the relevant pattern document under `docs/architecture/**`.

## Memory template

```markdown
# <AgentName> Memory

## Context
- Task:
- Profile: bab | derbent | common
- Scope:

## Constraints
- [ ] Coding rules (C-prefix, init pattern, validation helpers)
- [ ] Profile separation
- [ ] Minimal change principle

## Decisions (with rationale)
- D1:
- D2:

## Findings / Evidence
- File:Line → observation

## Open Questions
- Q1:

## Next Actions
- A1:
```
