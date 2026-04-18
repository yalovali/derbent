# Agent Skills & Tooling Matrix

See also: `.github/agents/_shared/skills/README.md` (skill modules) and `docs/knowledge/README.md` (generated knowledge base index).

This is the **single source of truth** for what each agent is responsible for and which tools it should prefer.

## Agents

### Orchestrator
**Skills**: task decomposition, profile selection, phase sequencing, risk management.
**Tools**: view/grep/glob first; bash only for known-safe commands; delegates to other agents.

### Analyzer
**Skills**: requirements analysis, impact analysis, dependency mapping, profile boundary detection.
**Tools**: grep/glob/view; produces analysis artifacts into `tasks/agents/**`.

### Pattern Designer
**Skills**: discover existing patterns, propose design consistent with `AGENTS.md` + docs.
**Tools**: grep/glob/view; optionally `scripts/analyze-patterns.sh`.

### Coder
**Skills**: implement minimal, correct changes; wire initializers; enforce patterns.
**Tools**: edit/create/view/grep/bash; must keep changes surgical.

### Verifier
**Skills**: rule enforcement, compilation verification, selective tests.
**Tools**: bash/grep; uses `.github/agents/verifier/scripts/*`.

### Tester
**Skills**: select best test scope (unit vs selective UI), interpret logs, report regressions.
**Tools**: bash; focuses on existing test entrypoints only.

### Documenter
**Skills**: update or create only necessary docs; cross-reference; keep SSOT.
**Tools**: view/edit/grep.

### Todo-Fix
**Skills**: generate actionable TODO list from diffs/logs; propose smallest fixes.
**Tools**: git diff parsing + grep; outputs `TODO.md` in task folder.

### Cleanup
**Skills**: detect stale/duplicated docs, propose archive moves (never delete blindly).
**Tools**: glob/grep; produces a report and safe `git mv` suggestions.
