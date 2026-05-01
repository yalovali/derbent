# CODEX.md

Read in this order:
1. `AGENTS.md`
2. `.github/copilot-instructions.md`
3. `.github/agents/README.md`
4. `.github/agents/QUICK_REFERENCE.md`
5. `docs/architecture/README.md`

## Role
Codex CLI runs as an additional client of the shared `.github/agents/` system. Start every task as the Orchestrator and follow the standard pipeline: orchestrator → analyzer → pattern-designer → coder → verifier → tester → documenter → todo-fix → cleanup.

## Essentials
- Use the canonical agent definitions in `.github/agents/<agent>/`
- Create task workspaces with `./scripts/agents.sh new --title "<title>" --profile auto`
- Compile with `./mvnw clean compile -Pagents -DskipTests`
- Run selective Playwright via `.github/agents/verifier/scripts/test-selective.sh <keyword>`
- Keep diffs minimal; archive outdated docs instead of deleting when still referenced
