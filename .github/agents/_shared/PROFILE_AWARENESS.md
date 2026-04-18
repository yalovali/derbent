# Profile Awareness (bab vs derbent)

Derbent is a single codebase with **two runtime profiles**:

- **derbent**: PLM application modules under `src/main/java/tech/derbent/plm/**`
- **bab**: BAB Gateway modules under `src/main/java/tech/derbent/bab/**`
- **common API**: shared framework under `src/main/java/tech/derbent/api/**` (used by both)

## Non‑negotiable rules

1. **Do not mix profile implementations**
   - BAB-only logic must live in `tech.derbent.bab.*` and be guarded by `@Profile("bab")` where applicable.
   - PLM-only logic must live in `tech.derbent.plm.*` and be guarded by `@Profile("derbent")` where applicable.
   - Shared abstractions belong in `tech.derbent.api.*`.

2. **Profile must be explicit in every agent run**
   - If the task is ambiguous, default to **derbent** unless the request mentions BAB/Calimero/gateway/network/system-metrics.

3. **Use the correct build profile for agents/CI**
   - Compilation in constrained environments: `./mvnw clean compile -Pagents -DskipTests`

## Runtime commands

```bash
# Run app (BAB)
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=bab"

# Run app (Derbent)
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=derbent"
```

## Testing implications

- Prefer **selective** UI testing via `.github/agents/verifier/scripts/test-selective.sh <keyword>`.
- Don’t expand test scope unless the change is cross-cutting in `tech.derbent.api.*`.
