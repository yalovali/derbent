# Skill: BAB / Calimero integration

## Purpose
Build BAB profile features under `tech.derbent.bab.*` and follow Calimero + BAB UI rules.

## Key patterns
- JSON parsing must follow actual Calimero nested response patterns.
- Dashboard components use BAB placeholder pattern (`@Transient placeHolder_*` fields).
- Keep BAB services/components behind `@Profile("bab")`.

## References
- `docs/bab/README.md`
- `docs/bab/CODING_RULES.md`
- `docs/bab/CALIMERO_API_RESPONSE_PATTERNS.md`
- `docs/bab/JSON_NETWORK_SERIALIZATION_CODING_RULES.md`
- `TRANSIENT_PLACEHOLDER_COMPONENT_PATTERN.md`
