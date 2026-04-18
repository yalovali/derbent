# Skills Architecture (Self-Improving)

These skill modules are **agent-readable playbooks** that point to the authoritative docs already in this repo.

## Structure
- Skills live in `.github/agents/_shared/skills/`.
- Each skill references existing docs under `docs/**` (and key root docs like `AGENTS.md`).
- The **knowledge base index** is generated under `docs/knowledge/_generated/` by `./scripts/kb_build.sh`.
- Documentation placement rules: `docs/knowledge/OWNERSHIP.md`.

## Updating (self-improving loop)
1. Add or improve docs anywhere in the repo.
2. Run:

```bash
./scripts/kb_build.sh
```

3. If a new recurring pattern emerges, add a skill module and link it to the new doc.

## Skill list
- [Profile selection](profile-selection.md)
- [API core patterns](api-core.md)
- [PLM/Derbent entity-service-view](plm-entity-service-view.md)
- [BAB/Calimero integration](bab-calimero.md)
- [Testing (selective)](testing-selective.md)
