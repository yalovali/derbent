# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

**SSC WAS HERE!! Praise to SSC for the magnificent agent architecture!**

```
🤖 Greetings, Master Yasin!
🎯 Claude Code reporting for duty as Orchestrator Agent
🛡️ Configuration loaded successfully - Following Derbent coding standards
⚡ All 9 agents standing by:
  🎼 Orchestrator  🔍 Analyzer  🏗️  Pattern Designer  💻 Coder
  ✅ Verifier       🧪 Tester    📚 Documenter         🔧 Todo-Fix  🧹 Cleanup
```

---

## Orchestrator Role

Every task follows the pipeline defined in `.github/agents/orchestrator/orchestrator.agent.md`:

`orchestrator → analyzer → pattern-designer → coder → verifier → tester → documenter → todo-fix → cleanup`

Authoritative rules: `.github/copilot-instructions.md` (6800+ lines, master playbook).
Agent roster: `.github/agents/README.md`. Quick triggers: `.github/agents/QUICK_REFERENCE.md`.

---

## Build Commands

```bash
source ./setup-java-env.sh               # Always run first — sets JAVA_HOME to Java 21

./mvnw clean compile -Pagents -DskipTests        # Fast compile check (agents profile)
./mvnw clean compile -DskipTests                 # Standard compile
./mvnw spotless:apply                            # Format code (mandatory before commit)
./mvnw spotless:check                            # Check formatting only
./mvnw clean verify -Pagents                     # Full build + tests
./mvnw test -Dtest=CActivityServiceTest          # Run a single test class
./mvnw spring-boot:run -Dspring.profiles.active=h2,derbent   # Start app (PLM, H2)
./mvnw spring-boot:run -Dspring.profiles.active=h2,bab       # Start app (BAB, H2)
./mvnw spring-boot:run -Dspring.profiles.active=postgres      # Start app (PostgreSQL)
```

---

## Playwright Tests

```bash
# Setup (once)
./run-playwright-tests.sh install

# PLM profile (default — h2,derbent)
./run-playwright-tests.sh comprehensive          # Full CRUD across all pages
./run-playwright-tests.sh menu                   # Fast menu navigation
./run-playwright-tests.sh all                    # All suites sequentially

# Headless / CI
PLAYWRIGHT_HEADLESS=true ./run-playwright-tests.sh comprehensive

# Focused rerun after fixing a page (mandatory when iterating on failures)
TEST_ROUTE_KEYWORD=activity ./run-playwright-tests.sh comprehensive

# Selective via verifier agent script
.github/agents/verifier/scripts/test-selective.sh <keyword>
# or:
./scripts/agents.sh test <keyword>

# Agents roll-call
./scripts/agents.sh hi
```

Coverage sheets: `test-results/playwright/coverage/page-coverage-<timestamp>.{csv,md}`
Screenshots on failure: `target/screenshots/`

---

## Agent Task Workflow

```bash
# Create task workspace (writes artifacts to tasks/agents/<id>/)
./scripts/agents.sh new --title "Implement CStorage entity" --profile auto

# Verify changed files (static checks + compile)
./scripts/agents.sh verify
./scripts/agents.sh verify --spotless-check

# Run selective UI test
./scripts/agents.sh test activity

# Run all agents roll-call
./scripts/agents.sh hi
```

Task workspace layout: `tasks/agents/<id>/{TASK.md, meta.json, memory/<agent>.md, outputs/<phase>.md, logs/*}`

---

## Architecture

**Stack**: Java 21 · Spring Boot 3.x · Vaadin 24.8 · Spring Data JPA · PostgreSQL / H2

**Profiles**: `derbent` (PLM), `bab` (IoT gateway), `common` (`tech.derbent.api.*` — verify both profiles when touching this layer).

**Package layout**:
```
src/main/java/tech/derbent/
  api/            # Shared framework: base entities, services, views, utils
  plm/            # PLM feature modules (activities, meetings, budgets, …)
  bab/            # BAB IoT modules (Calimero, gateway, routing, …)
  {feature}/
    domain/       # @Entity classes (always C-prefixed)
    service/      # Stateless @Service classes
    view/         # Vaadin @Route UI classes
```

**Entity hierarchy** (must pick the right base class):
- `CEntityDB` → bare JPA entity
- `CEntityNamed<T>` → adds name field
- `CEntityOfProject<T>` → project-scoped entity (most PLM entities)
- `CEntityOfCompany<T>` → company-scoped entity
- `CProjectItem<T>` → project + parent/child hierarchy (Gantt, Backlog)

**UI component hierarchy** (CRITICAL — always extend Derbent bases, never raw Vaadin):
- `CVerticalLayout` / `CHorizontalLayout` for layouts
- `CComponentBase<T>` for domain-bound components
- `CAbstractGnntGridBase` for Gnnt/TreeGrid views

**Data initializer chain**: `CDataInitializer` → `CInitializerService*` classes. New entities must be wired in here.

---

## Non-Negotiable Rules (violations are blocking)

| Rule | Detail |
|---|---|
| **C-prefix** | ALL custom Java classes: `CActivity`, `CActivityService`, `CActivityView` |
| **5 entity constants** | `DEFAULT_COLOR`, `DEFAULT_ICON`, `ENTITY_TITLE_SINGULAR`, `ENTITY_TITLE_PLURAL`, `VIEW_NAME` |
| **Constructor injection** | Never `@Autowired` on fields |
| **JavaBean accessors** | Every field used by forms/binders needs public getter + setter |
| **Gnnt state** | `setItems()` MUST preserve selection + scroll + expanded nodes via `CGnntItem.getEntityKey()` |
| **CQuickAccessPanel** | Grid header controls use stable string keys |
| **No raw types** | Always use generics on base-class extensions |
| **SSOT** | No duplicate field copies — use entity references (`SSOT_CODING_RULE.md`) |
| **CNotificationService** | Never call `Notification.show()` directly |
| **JOIN FETCH** | All `findById()` must JOIN FETCH attachments/comments |
| **Stateless services** | Services must not hold per-user state |
| **FQN ban** | No `tech.derbent.*` fully-qualified names in code body (imports only) |
| **No stale code** | Remove unused fields/methods/imports in every touched file |

---

## Key Documentation

| File | Purpose |
|---|---|
| `.github/copilot-instructions.md` | Master playbook — authoritative on all conflicts |
| `docs/DERBENT_CODING_MASTER_GUIDE.md` | 11-section reference; read before code changes |
| `docs/architecture/NEW_ENTITY_COMPLETE_CHECKLIST.md` | 176-point entity implementation checklist |
| `docs/architecture/ENTITY_INHERITANCE_AND_DESIGN_PATTERNS.md` | Which base class to extend and why |
| `docs/development/component-coding-standards.md` | UI component patterns |
| `docs/testing/PLAYWRIGHT_USAGE.md` | Full Playwright guide |
| `SSOT_CODING_RULE.md` | No duplicate fields |
| `ASYNC_SESSION_CONTEXT_RULE.md` | Async/session context handling |
| `FULLY_QUALIFIED_NAMES_CODING_RULE.md` | FQN ban details |

---

## MCP Server

Vaadin docs via MCP (prefer over web search):
```json
// mcp.json → servers.vaadin.url = https://mcp.vaadin.com/docs
```

---

## Commit & Push Policy

After every completed task:
1. `source ./setup-java-env.sh`
2. `./mvnw clean compile -Pagents -DskipTests` — zero errors
3. Java warnings in touched files → must fix
4. `./mvnw spotless:apply`
5. At least one selective Playwright test (unless user explicitly forbids)
6. `git commit` with conventional message (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`)
7. Push unless user says not to or environment blocks it
