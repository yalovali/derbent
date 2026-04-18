# Skill: PLM / Derbent profile (entities + services + UI)

## Purpose
Build PLM features under `tech.derbent.plm.*` using Derbent patterns.

## Key patterns
- Entity constants (all 5)
- Service `validateEntity()` uses helpers
- Repository `listByProjectForPageView` / `findById` with correct JOIN FETCH
- UI 3-step form population pattern

## References
- `AGENTS.md` (root)
- `docs/architecture/NEW_ENTITY_COMPLETE_CHECKLIST.md`
- `docs/architecture/form-population-pattern.md`
- `docs/architecture/LAZY_LOADING_BEST_PRACTICES.md`
- `docs/implementation/CRUD-Operations-Guide.md`
- `docs/implementation/PageService-Pattern.md`
