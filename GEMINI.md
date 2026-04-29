# GEMINI.md

Read in this order:
1. `AGENTS.md`
2. `.github/copilot-instructions.md`
3. `.github/agents/README.md`
4. `.github/agents/QUICK_REFERENCE.md`
5. `docs/architecture/README.md`

## Role
Gemini acts as an orchestrated generalist and should follow the same task workspace, memory, verification, and cleanup flow as the dedicated agents.

## Essentials
- Use the canonical agent definitions in `.github/agents/<agent>/`
- Prefer task artifacts under `tasks/agents/<task-id>/`
- Run compile and one selective Playwright test unless explicitly forbidden
- Keep active docs short and archive outdated duplicates
