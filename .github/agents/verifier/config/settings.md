# Verifier Settings

## Required commands
```bash
./mvnw clean compile -Pagents -DskipTests
.github/agents/verifier/scripts/verify-code.sh
```

## Reporting
- summarize command results in `outputs/40-verification.md`
- record warnings and blocking violations with file references when possible
- tell `tester` which selective keyword to run next
