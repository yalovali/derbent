# Copilot Instructions

Mandatory playbook for all AI work in this repository.

## 1. Operating model
- Use the orchestrated workflow for every task:
  1. `orchestrator`
  2. `analyzer`
  3. `pattern-designer`
  4. `coder`
  5. `verifier`
  6. `tester`
  7. `documenter`
  8. `todo-fix`
  9. `cleanup`
- Keep diffs minimal and aligned with existing patterns.
- Prefer task-scoped artifacts over long repeated reasoning in agent prompts.
- **Play a sound at the start and end of every agent task:**
  ```bash
  paplay /usr/share/sounds/freedesktop/stereo/service-login.oga      >/dev/null 2>&1 || true   # start
  paplay /usr/share/sounds/freedesktop/stereo/alarm-clock-elapsed.oga >/dev/null 2>&1 || true  # done
  ```

## 2. Mandatory read path
1. `AGENTS.md`
2. `.github/agents/README.md`
3. `.github/agents/QUICK_REFERENCE.md`
4. Relevant agent definition and config
5. `docs/architecture/README.md`
6. Task-specific rule doc only when needed

## 3. Definition of done
- Code change implemented.
- Compile run with warnings visible for code tasks.
- Warnings introduced in touched files fixed.
- At least one selective Playwright test run unless the user explicitly forbids tests.
- Non-obvious changed logic commented.
- Docs updated only if rules, workflows, or architecture changed.
- Git commit and push completed unless the user explicitly says not to or push is unavailable.

## 4. Core repository rules
- All custom classes use the `C` prefix. Interfaces use `I`.
- No fully qualified class names in code when imports are possible.
- No field injection. Constructor injection only.
- No raw types.
- No stale or unreachable code in touched files.
- Use concrete bean lookup instead of unchecked registry casts when a concrete type exists.
- Keep profile-specific logic separated:
  - `tech.derbent.api.*` for shared framework
  - `tech.derbent.plm.*` for Derbent-only logic
  - `tech.derbent.bab.*` for BAB-only logic

## 5. Critical runtime and UI guards
- Entity fields referenced by forms or screens must be valid JavaBean properties.
- Gnnt refreshes using `setItems(...)` must preserve selection and scroll by stable entity keys. Tree grids must preserve expanded nodes too.
- Prefer `CQuickAccessPanel` for grid header quick actions.
- `CComboBoxOption` icon/color usage must update the collapsed-field prefix on value change.
- Do not create initializer/views for relation classes.

## 6. Build and test commands
```bash
./mvnw clean compile -Pagents -DskipTests
./mvnw spotless:apply
.github/agents/verifier/scripts/verify-code.sh
.github/agents/verifier/scripts/test-selective.sh <keyword>
```

## 7. Documentation policy
- Keep mandatory docs short.
- Put detailed architecture content in `docs/architecture/**`.
- Put BAB-specific rules in `docs/bab/CODING_RULES.md`.
- Treat `docs/archive/**` and `.github/agents/archive/**` as historical only.
- Replace duplicate docs with references to the canonical file.

## 8. Agent facilities
- Task workspace: `tasks/agents/<task-id>/`
- Memory: `tasks/agents/<task-id>/memory/<agent>.md`
- Phase outputs: `tasks/agents/<task-id>/outputs/*.md`
- Logs: `tasks/agents/<task-id>/logs/*`
- Shared skills and helpers:
  - `.github/agents/_shared/MEMORY_SYSTEM.md`
  - `.github/agents/_shared/SKILLS_AND_TOOLS.md`
  - `.github/agents/_shared/PROFILE_AWARENESS.md`

## 9. Task-to-doc routing
- Start with `docs/architecture/README.md`.
- Open only the smallest relevant doc:
  - new entity: `docs/architecture/NEW_ENTITY_COMPLETE_CHECKLIST.md`
  - initializer wiring: `docs/architecture/initializer-service-architecture.md`
  - service rules: `docs/architecture/service-layer-patterns.md`
  - validation: `docs/architecture/VALIDATION_CODING_RULES.md`
  - view/UI: `docs/architecture/view-layer-patterns.md`
  - grid work: `docs/architecture/cgrid-configuration-patterns.md`
  - styling/layout: `docs/architecture/ui-css-coding-standards.md`
  - BAB work: `docs/bab/CODING_RULES.md`

## 10. Documentation refactor rule
When a doc becomes duplicated, outdated, or too expensive for agents to load:
- keep one canonical version
- archive or stub the rest
- update indexes and entry points
- prefer references over repeated examples
