# Skill: Testing (selective-first)

## Purpose
Validate changes quickly using existing test entrypoints.

## Rules
- Prefer selective UI tests by keyword.
- Don’t introduce new test tools.

## Commands
```bash
# Fast compile gate
./mvnw clean compile -Pagents -DskipTests

# Code compliance gate
.github/agents/verifier/scripts/verify-code.sh

# Selective UI test
.github/agents/verifier/scripts/test-selective.sh activity
```

## References
- `docs/testing/TESTING_QUICK_REFERENCE.md`
- `.github/agents/verifier/scripts/test-selective.sh`
- `.github/agents/verifier/scripts/verify-code.sh`
