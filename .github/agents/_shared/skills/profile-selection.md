# Skill: Profile selection (bab vs derbent vs common)

## Purpose
Decide where code/docs belong and which runtime profile(s) must be validated.

## Rules
- BAB-only code/doc: `tech.derbent.bab.*` and `docs/bab/**`.
- PLM-only code/doc (Derbent profile): `tech.derbent.plm.*` and `docs/features/**`, `docs/implementation/**`.
- Shared API: `tech.derbent.api.*` and cross-cutting docs in `docs/architecture/**`, `docs/development/**`.

## References
- `.github/agents/_shared/PROFILE_AWARENESS.md`
- `AGENTS.md` (root) / `.github/copilot-instructions.md`
- `docs/bab/README.md`
- `docs/features/README.md`
