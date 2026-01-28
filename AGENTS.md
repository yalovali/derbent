# AGENTS.md (Root) - Derbent AI Agent Entry Point

This file is the mandatory entry point for any AI agent or automation running in this repository.

## Required reading order
1. `.github/copilot-instructions.md` (master playbook; mandatory rules)
2. `.github/agents/README.md` (agent roster and workflow)
3. `.github/agents/QUICK_REFERENCE.md` (quick rules + triggers)
4. Relevant agent definition (e.g., `.github/agents/coder/coder.agent.md`)
5. Relevant agent config (e.g., `.github/agents/coder/config/settings.md`)

## Non-negotiable enforcement
- Always check `.github/` for rules before any code changes or test runs.
- Treat `.github/copilot-instructions.md` as authoritative.
- If instructions conflict, ask for clarification before proceeding.

## Notes
- If `.github/` is missing or unreadable, explicitly state that and continue with best effort.
