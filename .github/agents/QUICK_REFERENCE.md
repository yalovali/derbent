# Agent Quick Reference

## Read order
1. `AGENTS.md`
2. `.github/copilot-instructions.md`
3. `.github/agents/README.md`
4. Relevant agent definition and config
5. `docs/architecture/README.md`

## Fast workflow
1. Pick profile: `derbent`, `bab`, or `common`
2. Create `tasks/agents/<task-id>/`
3. Write `outputs/10-analysis.md`
4. Implement the smallest compliant change
5. Run compile and selective verification
6. Run one selective Playwright keyword test unless forbidden
7. Update docs only if behavior or rules changed
8. Record follow-ups in `outputs/70-todo.md`
9. Commit and push unless explicitly skipped

## Preferred commands
```bash
./mvnw clean compile -Pagents -DskipTests
./mvnw spotless:apply
.github/agents/verifier/scripts/verify-code.sh
.github/agents/verifier/scripts/test-selective.sh <keyword>
```

## Do not do
- Do not load long archived docs for normal work.
- Do not maintain duplicate rule documents.
- Do not invent new testing entry points when the selective test path already exists.
- Do not skip task memory, outputs, or follow-up notes for multi-agent work.
