# Skill: API (common) core patterns

## Purpose
Implement changes in `tech.derbent.api.*` without breaking either profile.

## What to check
- Component base class rules (no raw Vaadin components for custom UI).
- Validation helper rules (no manual uniqueness checks).
- Initialization rules (JPA ctor vs business ctor).

## References
- `docs/architecture/README.md`
- `docs/architecture/coding-standards.md`
- `docs/architecture/VALIDATION_CODING_RULES.md`
- `FULLY_QUALIFIED_NAMES_CODING_RULE.md`
- `SSOT_CODING_RULE.md`
