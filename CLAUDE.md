# CLAUDE.md

Read in this order:
1. `AGENTS.md`
2. `.github/copilot-instructions.md`
3. `.github/agents/README.md`
4. `docs/architecture/README.md`

## Essentials
- Compile with `./mvnw clean compile -Pagents -DskipTests`
- Format with `./mvnw spotless:apply`
- Use selective Playwright via `.github/agents/verifier/scripts/test-selective.sh <keyword>`
- Update docs only when rules, workflows, or architecture changed
- Avoid archived docs for normal tasks

## Task routing
- New entity: `docs/architecture/NEW_ENTITY_COMPLETE_CHECKLIST.md`
- Initializer: `docs/architecture/initializer-service-architecture.md`
- Service/validation: `docs/architecture/service-layer-patterns.md`, `docs/architecture/VALIDATION_CODING_RULES.md`
- View/UI: `docs/architecture/view-layer-patterns.md`, `docs/architecture/ui-css-coding-standards.md`
- BAB: `docs/bab/CODING_RULES.md`
