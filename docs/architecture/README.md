# Architecture Documentation

Canonical index for active architecture and coding-rule documents.

## How agents should use this directory
- Start here.
- Open only the one or two docs needed for the task.
- Prefer references over copying rules into new docs.
- Treat `docs/archive/**` as historical only.

## Primary documents by task

| Task | Primary doc | Open only if needed |
| --- | --- | --- |
| New entity | `NEW_ENTITY_COMPLETE_CHECKLIST.md` | `entity-inheritance-patterns.md`, `initializer-service-architecture.md` |
| Initializer wiring | `initializer-service-architecture.md` | `coding-standards.md` |
| Service logic | `service-layer-patterns.md` | `VALIDATION_CODING_RULES.md` |
| Validation | `VALIDATION_CODING_RULES.md` | `VALIDATION_PATTERN.md` |
| View/UI | `view-layer-patterns.md` | `ui-css-coding-standards.md` |
| Grid/Gnnt | `cgrid-configuration-patterns.md` | `kanban-and-refresh-patterns.md` |
| Styling/layout | `ui-css-coding-standards.md` | `component-utility-reference.md` |
| Multi-user safety | `multi-user-singleton-advisory.md` | `bean-access-patterns.md` |
| Copy behavior | `COPY_TO_PATTERN_CODING_RULE.md` | `SERVICE_BASED_COPY_PATTERN.md`, `SIMPLIFIED_COPY_PATTERN.md` |
| Profile/build rules | `SPRING_BOOT_PROFILE_CODING_RULES.md` | `../bab/CODING_RULES.md` |

## Core active docs
- `coding-standards.md`
- `entity-inheritance-patterns.md`
- `initializer-service-architecture.md`
- `service-layer-patterns.md`
- `VALIDATION_CODING_RULES.md`
- `view-layer-patterns.md`
- `ui-css-coding-standards.md`
- `cgrid-configuration-patterns.md`
- `multi-user-singleton-advisory.md`

## Secondary reference docs
Open these only when their topic is directly involved:
- `ASYNC_SESSION_CONTEXT_RULE.md`
- `bean-access-patterns.md`
- `CHILD_ENTITY_PATTERNS.md`
- `component-utility-reference.md`
- `drag-drop-component-pattern.md`
- `entity-selection-component-design.md`
- `form-population-pattern.md`
- `kanban-and-refresh-patterns.md`
- `method-placement-guidelines.md`
- `SSOT_CODING_RULE.md`
- `STATUS_INITIALIZATION_PATTERN.md`
- `TRANSIENT_PLACEHOLDER_COMPONENT_PATTERN.md`
- `value-persistence-pattern.md`

## Refactor policy
- Keep this directory as the source of truth for active architecture rules.
- If two docs say the same thing, keep one and archive or redirect the other.
- Move narrative implementation summaries, old migration notes, and task-specific writeups to `docs/archive/**`.
