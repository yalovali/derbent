# AGENTS Playbook

## 1. Orientation
- Platform: Spring Boot + Vaadin + Maven. Shared framework code is under `src/main/java/tech/derbent/api/`; every feature module in `tech/derbent/*` mirrors the `domain/ → service/ → view/` layering from `docs/development/project-structure.md`.
- Frontend/Vaadin assets live in `src/main/frontend`; configuration, seeds, and metadata live in `src/main/resources`; UI automation sits in `src/test/java/automated_tests/tech/derbent/ui/automation`.
- Documentation is organized by subject (`docs/architecture`, `docs/development`, `docs/implementation`, `docs/testing`). Most coding rules referenced here come from `docs/architecture/coding-standards.md`, `docs/architecture/multi-user-singleton-advisory.md`, and `docs/development/copilot-guidelines.md`.

## 2. Core Commands
- `./mvnw spring-boot:run -Dspring.profiles.active=h2` starts the local app with the in-memory H2 profile.
- `./mvnw clean verify` compiles, runs Spotless/Prettier, executes tests, and builds Vaadin assets; use it (or at least `./mvnw spotless:apply`) before review.
- `./run-playwright-tests.sh [menu|login|comprehensive|all]` runs the headless UI suites; `./run-playwright-visible-postgres.sh` replays the menu suite against a live Postgres instance with a visible browser.
- Reset sample data via `mvn spring-boot:run -Dspring-boot.run.main-class=tech.derbent.api.dbResetApplication -Dspring-boot.run.profiles=reset-db`.

## 3. Coding Standards
- **C-prefix everywhere**: all concrete classes start with `C` (e.g., `CActivity`, `CActivityService`, `CActivityView`). Interfaces use `I*`, tests use `C*Test`. This is non-negotiable and central to AI/code navigation.
- **Extend the base layers**: entities extend the closest `api/domains` base (e.g., `CProjectItem<T>`), services extend the matching `api/services` base, and views extend the Vaadin base views. Reuse metadata annotations and helper components from `tech.derbent.api`.
- **Type safety + metadata**: never use raw types; annotate entity fields with validation constraints plus `@AMetaData` (display name, order, requirements) so Vaadin builders stay deterministic.
- **IDs & constants**: keep `VIEW_NAME`, `TITLE`, and DOM IDs (`#custom-*`) consistent for Playwright selectors. Constants are `static final`, SCREAMING_SNAKE_CASE.
- **Console logging style**: keep the ANSI-colored, clickable format defined in `application*.properties` (`spring.output.ansi.enabled=ALWAYS` + the shared `logging.pattern.console` with magenta timestamps, padded level, cyan `(file:line)`, red message, cyan logger). See Coding Standards for the exact string and the optional method-name note.
- **Formatting**: four spaces in Java, shared Eclipse formatter + Prettier. Run `./mvnw spotless:apply` before pushing.
- **Sprint/backlog binding**: backlog = `sprintItem == null`. Add via `CSprintItemService.save(...)` (binds `itemId/itemType` and `item.sprintItem`), remove via `CSprintItemService.delete(...)` (clears `item.sprintItem` first). Deleting a sprintable item deletes its sprint item; deleting a sprint item must not delete the sprintable item. `CComponentBacklog` only needs a project; sprint context is optional.
- **Drag/drop + refresh**: use `IHasDragControl` forwarding and call component-level `refreshComponent()` (backlog, sprint items, sprint widget) instead of ad-hoc grid refreshes. Keep refresh code nondestructive—update existing UI elements (labels/buttons) rather than recreating them.
- **Fail fast**: avoid silent `if (x == null) return;` guards; prefer `Check.notNull`, `Check.instanceOf`, etc., so errors surface instead of being ignored.
- **Exception handling**: let exceptions bubble up; only user-triggered handlers (e.g., `on_*_clicked`, drop listeners) should convert them to UI via `CNotificationService.showException(...)`. Service/controller layers should log once (concise `LOGGER.error(e.getMessage(), e)`) and rethrow.
- **Field-name fidelity**: Screen/grid initializers must use the exact entity field names (and matching getters/setters) when calling `createLineFromDefaults`, `setColumnFields`, etc. Reflection-based metadata will throw if names drift—no aliases.

## 4. Stateless Service Pattern (Multi-User Safe)
- Services are singleton-scoped, so they **must not** hold mutable user/project/company state. Only keep injected dependencies, loggers, and constants as fields. All user/company/project context is retrieved per method call via `ISessionService`.
- No static mutable collections; if you truly need listeners/caches, store them in VaadinSession (see `docs/architecture/multi-user-singleton-advisory.md`) or persist them.
- Access control annotations (`@PreAuthorize`, `@RolesAllowed`) and transaction boundaries (`@Transactional` / `@Transactional(readOnly = true)`) are required on service entry points.
- Checklist reminders from `docs/development/multi-user-development-checklist.md`: never cache session lookups, no instance collections of user data, and write tests that mock `ISessionService` for context.

## 5. View & Component Rules
- Vaadin views keep the `@Route`, `@PageTitle`, and layout definitions in dedicated initialization methods. Each view maintains deterministic component IDs for UI automation and reuses shared components from `tech.derbent.api.ui`.
- UI classes may keep state in instance fields (per-user instances) but must clean up listeners on detach and use `UI.access()` for async updates.
- Follow the pattern docs in `docs/architecture/view-layer-patterns.md` for layout scaffolding, and consult `docs/development/copilot-guidelines.md` for entity/service/view scaffolds that Copilot can mirror.
- Inline grid renderers (e.g., component columns) must remain UI-only: no persistence or notifications inside the renderer; pass save/error callbacks to the owning component or page service and handle `CNotificationService.showException(...)` in those user-triggered handlers.

## 6. Testing Expectations
- Unit/service tests suffix with `*Test`, mock the session, and never share mutable fixtures. UI automation extends `CBaseUITest` to reuse login/navigation helpers and semantic waits (`wait_afterlogin()` etc.).
- Run backend tests with `./mvnw test -Dspring.profiles.active=test`. Playwright screenshots go to `target/screenshots/`—keep only meaningful artifacts and refresh baselines when UI changes.
- Selectors should be stable (`#custom-username-input`, `vaadin-button:has-text("Save")`) and avoid brittle XPath or `Thread.sleep`.

## 7. Security & Tenant Context
- Login pattern: `username@company_id` (see `docs/implementation/COMPANY_LOGIN_PATTERN.md`). `CCustomLoginView` constructs the identifier, `CUserService` splits it (`findByUsername(companyId, login)`), and services must fail fast when company context is missing.
- Entities rely on cascading deletes (`@OnDelete(CASCADE)`) to respect tenant cleanup; keep these annotations intact whenever you touch associations.
- Always read company/project context through the session service or request scope—never trust caller-provided IDs without verifying ownership.

## 8. Workflow & Reviews
- Commits: short, present-tense, imperative (e.g., “Add Playwright login regression”). Group related changes, describe cross-module impacts in the body, and link Jira/GitHub issues where possible.
- PRs: document commands/tests run, include screenshots for UI work, and update relevant docs under `docs/` when patterns/flows change.
- Before requesting review: Spotless/Prettier clean, `./mvnw clean verify` (or a scoped test plan if faster), and Playwright evidence when UI changes are involved.

## 9. Reference Map
- Architecture deep dives: `docs/architecture/coding-standards.md`, `service-layer-patterns.md`, `view-layer-patterns.md`, `multi-user-singleton-advisory.md`.
- Development quickstarts: `docs/development/getting-started.md`, `project-structure.md`, `copilot-guidelines.md`, `multi-user-development-checklist.md`.
- Security/authentication: `docs/implementation/COMPANY_LOGIN_PATTERN.md`, `LOGIN_AUTHENTICATION_MECHANISM.md`.
- Testing: `docs/testing/*` plus Playwright scripts (`run-playwright-tests.sh`, `run-playwright-visible-h2.sh`, etc.).
