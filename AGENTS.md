# Repository Guidelines

## Project Structure & Module Organization
The project runs on Maven, Spring Boot, and Vaadin. Shared framework code lives in `src/main/java/tech/derbent/api/` (base entities, services, views). Feature modules under `tech.derbent/*` mirror the `domain/`, `service/`, `view/` tiers. Frontend assets sit in `src/main/frontend`, configuration and sample data in `src/main/resources`, and UI automation in `src/test/java/automated_tests/tech/derbent/ui/automation`. Pattern docs—especially authentication—reside in `docs/implementation/`.

## Build, Test, and Development Commands
- `./mvnw spring-boot:run -Dspring.profiles.active=h2` launches the app against in-memory H2.
- `./mvnw clean verify` compiles, runs tests, and builds Vaadin assets.
- `./run-playwright-tests.sh [menu|login|comprehensive|all]` runs headless UI flows with automatic browser management.
- `./run-playwright-visible-postgres.sh` replays the menu navigation suite against a live PostgreSQL database with a visible browser.
- `mvn spring-boot:run -Dspring-boot.run.main-class=tech.derbent.api.dbResetApplication -Dspring-boot.run.profiles=reset-db` repopulates sample data via the reset profile.

## Coding Style & Naming Conventions
Indent Java with four spaces and keep the C-prefix convention (`CProjectView`, `CActivityService`) so custom code is immediately recognizable. Extend the generic base classes in `api/` rather than duplicating logic, and preserve the `domain/` → `service/` → `view/` structure in each module. Maintain `VIEW_NAME` constants and expose deterministic element IDs (`#custom-…`) for Playwright. Run `./mvnw spotless:apply` prior to review to apply the shared Eclipse formatter and Prettier rules.

## Testing Guidelines
UI suites must extend `CBaseUITest` to reuse its login, navigation, wait, and screenshot helpers. Place classes in the automation package, suffix with `*Test`, and run against the H2-backed `test` profile (`./mvnw test -Dspring.profiles.active=test`). Favor resilient selectors (`#custom-username-input`, `vaadin-button:has-text('Save')`), reuse semantic waits (`wait_afterlogin()`), and avoid `Thread.sleep`. Headless runs write PNGs to `target/screenshots/`; keep only meaningful evidence and refresh baselines when new views land.

## Commit & Pull Request Guidelines
Keep commit summaries short, present tense, and imperative (e.g., “Add Playwright login regression”). Group related changes and include follow-up notes in the body when touching multiple modules. Pull requests should link Jira/GitHub issues when applicable, outline testing performed (command output or Playwright suite), and attach screenshots for UI-facing work. Update relevant docs under `docs/` when patterns or conventions shift, and ensure Spotless plus test suites pass before requesting review.

## Security & Company Context Patterns
Follow the company-aware login pattern (`username@companyId`) from `docs/implementation/COMPANY_LOGIN_PATTERN.md`. `CCustomLoginView` concatenates the identifier; `CUserService` splits it and queries `findByUsername(companyId, login)`. Services must read company context via the session service and fail fast when absent. Keep `@OnDelete(CASCADE)` mappings intact so company or project teardown cascades cleanly. Document and test multi-tenant rules as they appear.
