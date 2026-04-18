# Knowledge Base (API / PLM / BAB)

This folder provides an **organized, profile-aware index** over the existing Markdown documentation in this repository.

## Goals
- Keep your existing docs as-is (no risky mass moves/deletes).
- Provide a **single navigation layer** split by scope:
  - **API (common)**: `tech.derbent.api.*` patterns and shared rules
  - **PLM / Derbent profile**: `tech.derbent.plm.*`
  - **BAB profile**: `tech.derbent.bab.*` + Calimero integration
- Be **self-improving**: indexes regenerate automatically from the current file set.

## Generated indexes

## Timestamp pattern
All generated files use this fixed timestamp marker:

```xml
<current_datetime>2026-04-18T16:00:49.060Z</current_datetime>
```

The canonical indexes are generated into:

- `docs/knowledge/_generated/api-common.md`
- `docs/knowledge/_generated/plm-derbent.md`
- `docs/knowledge/_generated/bab.md`
- `docs/knowledge/_generated/misc.md`

Do not hand-edit generated files.

## Regenerate

```bash
# direct
./scripts/kb_build.sh

# via orchestrator runner
./scripts/agents.sh kb
```

## Cleanup audit (recommended)

To gradually move old root-level docs into the right `docs/**` buckets, generate an audit report:

```bash
./scripts/kb_audit.sh
```

This writes: `docs/knowledge/_generated/cleanup-suggestions.md`.

## Contributing rules (keep it clean)
See: `docs/knowledge/OWNERSHIP.md`.

- Shared rules/patterns go to `docs/architecture/**` or `docs/development/**`.
- PLM/Derbent feature docs go to `docs/features/**` and `docs/implementation/**`.
- BAB docs go to `docs/bab/**`.
- If you add a new doc anywhere, re-run `./scripts/kb_build.sh`.
