# Documentation ownership (API vs PLM vs BAB)

This file defines where new documentation should live so the knowledge base stays clean.

## API / Common (shared across both profiles)
Put docs here when the rule/pattern applies to both BAB and Derbent:
- `docs/architecture/**` (patterns, rules, constraints)
- `docs/development/**` (workflows, setup, coding guides)
- `docs/standards/**` (standards and compliance)
- `docs/testing/**` (testing strategy and infra)
- `docs/components/**` (shared UI component docs)

Typical topics:
- entity/service/view base patterns
- validation helpers
- lazy loading rules
- UI form population rules
- multi-user singleton rules

## PLM / Derbent profile
Put docs here when the behavior is specific to the PLM (derbent) domain:
- `docs/features/**`
- `docs/implementation/**`

Typical topics:
- Activities/Storage/Meeting/CRM/Kanban/Gantt domain behavior
- PLM page-service wiring and grids
- PLM-only workflows

## BAB profile
Put docs here when the behavior is specific to the BAB gateway:
- `docs/bab/**`
- `bab/docs/**` (Calimero client references)

Typical topics:
- Calimero APIs, DTO mapping, JSON parsing patterns
- BAB dashboard components and placeholder pattern
- BAB HTTP client/auth patterns

## What NOT to do
- Don’t add new permanent docs at repo root (prefer `docs/**`).
- Don’t duplicate rules: link to the SSOT doc instead.
- Don’t delete docs; archive under `docs/archive/**` only.
