# Project Structure and Organization

## Overview

This document describes the complete project structure of Derbent, explaining the purpose and organization of each directory and key files.

**Recent Updates:**
- 📦 Added package-info.java documentation to all major packages
- 🤖 Added AI tool configuration files (.cursorrules, .clinerules, .aidigestconfig)
- 📚 Reorganized documentation with archive for historical docs
- 🗂️ Clean root directory with only essential files

For information on finding documentation, see the [Documentation Guide](documentation-guide.md).

## Root Directory Structure

```
derbent/
├── .github/                    # GitHub configuration
│   ├── workflows/             # CI/CD pipelines
│   └── copilot-instructions.md # GitHub Copilot configuration (468 lines)
├── .cursorrules               # Cursor IDE AI configuration (NEW)
├── .clinerules                # Cline AI Assistant configuration (NEW)
├── .aidigestconfig            # AI Digest / general AI tools config (NEW)
├── .mvn/                      # Maven wrapper files
├── docs/                      # Documentation
│   ├── architecture/          # Design patterns and architecture
│   ├── development/           # Developer guides
│   ├── implementation/        # Implementation details
│   ├── testing/               # Testing documentation
│   └── archive/               # Historical documentation (NEW)
│       ├── tasks/             # Archived task summaries
│       └── README.md          # Archive explanation
├── profile-pictures/          # User profile images
├── src/                       # Source code
│   ├── main/
│   │   └── java/tech/derbent/ # Each package now has package-info.java (NEW)
│   └── test/
├── target/                    # Build output (generated)
├── .gitignore                 # Git ignore rules
├── .prettierrc.json          # Prettier configuration
├── eclipse-formatter.xml      # Eclipse code formatter
├── LICENSE                    # MIT License
├── pom.xml                    # Maven project configuration
├── README.md                  # Project overview
└── run-playwright-tests.sh   # UI test runner
```

### Recent Organizational Improvements

**AI Tool Support:**
- Added `.cursorrules` for Cursor IDE with quick reference
- Added `.clinerules` for Cline AI Assistant with detailed rules
- Added `.aidigestconfig` for AI Digest and general AI tools
- See [AI Tools Guide](ai-tools-guide.md) for complete documentation

**Documentation Organization:**
- Historical task summaries moved to `docs/archive/tasks/`
- Development docs consolidated in `docs/development/`
- Testing docs organized in `docs/testing/`
- Root directory now contains only essential files
- See [Documentation Guide](documentation-guide.md) for finding information

**Package Documentation:**
- Added package-info.java to all major packages (11 files)
- Documents package purpose, contents, and relationships
- Improves IDE tooltips and Javadoc generation
- Enhances AI assistant context understanding

## Source Code Structure

### Main Source Directory

```
src/main/java/tech/derbent/
├── Application.java           # Main Spring Boot application
├── api/                       # Core framework and shared modules
│   ├── annotations/          # Custom annotations
│   ├── components/           # Reusable UI components
│   ├── companies/            # Company management (shared)
│   ├── domains/              # Base entity classes
│   ├── exceptions/           # Custom exceptions
│   ├── interfaces/           # Shared interfaces
│   ├── page/                 # Dynamic page system (shared)
│   ├── projects/             # Project management (shared)
│   ├── roles/                # Role management (shared)
│   ├── screens/              # Screen definitions and builders
│   ├── services/             # Base service classes
│   ├── ui/                   # UI utilities
│   ├── utils/                # Utility classes
│   └── views/                # Base view classes
├── app/                      # Derbent business modules
│   ├── activities/           # Activity management module
│   ├── comments/             # Comment management
│   ├── decisions/            # Decision management
│   ├── gannt/                # Gantt chart views
│   ├── kanban/               # Kanban board implementation
│   ├── meetings/             # Meeting management
│   ├── orders/               # Order management
│   ├── risks/                # Risk management
│   └── ui/view/              # Dashboard view (app/ui/view/CDashboardView)
├── bab/                      # BAB Gateway modules
│   └── ui/view/              # BAB dashboard and future IoT UI
├── base/                     # Infrastructure modules
│   ├── login/                # Login and authentication
│   ├── session/              # Session management
│   ├── setup/                # System setup
│   └── users/                # User management
```

## Module Structure Pattern

Each business module follows a consistent structure:

```
module-name/
├── domain/                    # Domain entities
│   ├── CMainEntity.java      # Primary entity
│   ├── CEntityStatus.java    # Status entity
│   ├── CEntityType.java      # Type entity
│   └── CEntityPriority.java  # Priority entity (if applicable)
├── service/                   # Business logic
│   ├── CMainEntityService.java
│   ├── IMainEntityRepository.java
│   ├── CEntityStatusService.java
│   └── CEntityTypeService.java
└── view/                      # UI components
    ├── CEntityCard.java       # Card component
    └── CEntityDialog.java     # Dialog component
```

### Example: Activities Module

```
activities/
├── domain/
│   ├── CActivity.java         # Main activity entity
│   ├── CProjectItemStatus.java   # Activity status
│   ├── CActivityType.java     # Activity type
│   └── CActivityPriority.java # Activity priority
├── service/
│   ├── CActivityService.java
│   ├── IActivityRepository.java
│   ├── CProjectItemStatusService.java
│   ├── IProjectItemStatusRepository.java
│   ├── CActivityTypeService.java
│   ├── IActivityTypeRepository.java
│   ├── CActivityPriorityService.java
│   └── IActivityPriorityRepository.java
└── view/
    └── CActivityCard.java
```

## API Package Structure

The `api` package contains shared infrastructure used across all modules:

### api/annotations

Custom annotations for metadata-driven development:

```
annotations/
├── AMetaData.java              # UI metadata annotation
└── CSpringAuxillaries.java     # Spring utilities
```

### api/domains

Base entity classes forming the inheritance hierarchy:

```
domains/
├── CEntity.java                # Root entity
├── CEntityDB.java              # Database entity
├── CEntityNamed.java           # Named entity
├── CEntityOfCompany.java       # Company-scoped entity
├── CEntityOfProject.java       # Project-scoped entity
├── CProjectItem.java           # Hierarchical entity
├── CTypeEntity.java            # Company-scoped type entity base
├── CEvent.java                 # Event base
└── AbstractEntity.java         # Legacy support
```

### api/services

Base service classes:

```
services/
├── CAbstractService.java            # Base service
├── CEntityNamedService.java         # Named entity service
├── CEntityOfCompanyService.java     # Company-scoped service
├── CEntityOfProjectService.java     # Project-scoped service
├── IAbstractRepository.java         # Base repository interface
├── IEntityNamedRepository.java      # Named entity repository
├── IEntityOfCompanyRepository.java  # Company-scoped repository
└── IEntityOfProjectRepository.java  # Project-scoped repository
```

### api/views

Base view components:

```
views/
├── CAbstractPage.java              # Base page
├── CAbstractEntityDBPage.java      # Entity page
├── CAbstractNamedEntityPage.java   # Named entity page
├── components/                      # Reusable components
│   ├── CButton.java
│   ├── CEntityLabel.java
│   └── CComponentFieldSelection.java
├── dialogs/                        # Dialog components
│   ├── CDialogClone.java
│   └── CDBRelationDialog.java
└── grids/                          # Grid components
    ├── CGrid.java                  # Enhanced grid
    ├── CGridViewBaseDBEntity.java
    ├── CGridViewBaseNamed.java
    └── CMasterViewSectionGrid.java
```

### api/utils

Utility classes:

```
utils/
├── CAuxillaries.java           # General utilities
├── Check.java                  # Validation utilities
├── CKanbanUtils.java           # Kanban utilities
├── CPageableUtils.java         # Pagination utilities
└── SqlDebugUtils.java          # SQL debugging
```

## Resources Structure

```
src/main/resources/
├── application.properties           # Main configuration
├── application-h2.properties        # H2 database profile
├── application-postgres.properties  # PostgreSQL profile
├── application-test.properties      # Test profile
└── META-INF/
    └── resources/                   # Static resources
        ├── images/
        ├── themes/
        └── frontend/                # Vaadin frontend resources
```

## Test Structure

```
src/test/java/
└── automated_tests/tech/derbent/
    └── ui/automation/
        ├── CBaseUITest.java         # Base test class
        ├── CMainViewsTest.java      # Main views tests
        ├── CAdminViewsTest.java     # Admin views tests
        ├── CKanbanViewsTest.java    # Kanban tests
        └── pages/                   # Page Object Model
            ├── LoginPage.java
            ├── DashboardPage.java
            └── ActivityPage.java
```

## Documentation Structure

```
docs/
├── architecture/                    # Architecture documentation
│   ├── entity-inheritance-patterns.md
│   ├── service-layer-patterns.md
│   ├── view-layer-patterns.md
│   └── coding-standards.md
├── development/                     # Developer guides
│   ├── copilot-guidelines.md
│   ├── getting-started.md
│   └── project-structure.md
├── implementation/                  # Implementation details
│   ├── COMPANY_LOGIN_PATTERN.md
│   ├── DEPENDENCY_CHECKING_SYSTEM.md
│   └── PLAYWRIGHT_TEST_GUIDE.md
└── testing/                        # Testing documentation
    ├── PLAYWRIGHT_TEST_SUMMARY.md
    └── playwright-screenshots/
```

## Key Files

### pom.xml

Maven project configuration:
- Dependencies (Spring Boot, Vaadin, PostgreSQL, H2)
- Build plugins
- Profiles (h2-local-development, postgres)
- Java version (21)

### Application.java

Main entry point:
```java
@SpringBootApplication
public class Application implements AppShellConfigurator {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### application.properties

Main configuration file:
- Server settings
- Database configuration
- Vaadin settings
- Security settings
- Logging configuration

## Package Naming Convention

All packages follow the pattern:
```
tech.derbent.{module}.{layer}
```

Where:
- `module` = Business domain (activities, users, projects, etc.)
- `layer` = Architecture layer (domain, service, view)

Examples:
- `tech.derbent.activities.domain` - Activity entities
- `tech.derbent.activities.service` - Activity business logic
- `tech.derbent.activities.view` - Activity UI components

## Class Organization Within Packages

### Domain Package

```
domain/
├── CMainEntity.java           # Primary entity
├── CEntityStatus.java         # Status management
├── CEntityType.java           # Type categorization
├── CEntityPriority.java       # Priority levels (optional)
└── package-info.java          # Package documentation
```

### Service Package

```
service/
├── CMainEntityService.java          # Main service
├── IMainEntityRepository.java       # Main repository
├── CEntityStatusService.java        # Status service
├── IEntityStatusRepository.java     # Status repository
├── CEntityTypeService.java          # Type service
├── IEntityTypeRepository.java       # Type repository
└── CEntityInitializerService.java   # Initialization (optional)
```

### View Package

```
view/
├── CEntityCard.java           # Card component
├── CEntityDialog.java         # Dialog component
├── CEntityGrid.java           # Grid component
└── CEntityForm.java           # Form component
```

## Configuration Files

### Maven Configuration

- `pom.xml` - Main Maven configuration
- `.mvn/wrapper/` - Maven wrapper files for consistent Maven version

### IDE Configuration

- `eclipse-formatter.xml` - Eclipse code formatter
- `.prettierrc.json` - Prettier configuration

### Git Configuration

- `.gitignore` - Ignored files and directories
  - `/target/` - Build output
  - `*.log` - Log files
  - `.idea/` - IntelliJ IDEA files
  - `*.iml` - IntelliJ module files

## Build Output

The `target/` directory (generated by Maven):

```
target/
├── classes/                   # Compiled Java classes
├── generated-sources/         # Generated source files
├── maven-status/             # Maven build status
├── test-classes/             # Compiled test classes
├── screenshots/              # Playwright test screenshots
└── derbent-1.0-SNAPSHOT.jar # Built application JAR
```

## Static Resources

### Profile Pictures

```
profile-pictures/
├── admin.jpg
├── user1.jpg
└── default.jpg
```

### Frontend Resources

Vaadin frontend resources are managed in:
```
src/main/resources/META-INF/resources/
```

## Module Dependencies

### Core Modules

All business modules depend on:
- `api` package (base classes, utilities)
- `session` package (session management)
- `projects` package (project context)

### Example Dependency Chain

```
CActivity (activities.domain)
    ↓ extends
CProjectItem (api.domains)
    ↓ extends
CEntityOfProject (api.domains)
    ↓ uses
CProject (projects.domain)
```

## Adding New Modules

To add a new module:

1. Create module package: `tech.derbent.newmodule`
2. Create subpackages: `domain`, `service`, `view`
3. Follow naming conventions: C-prefix for classes
4. Extend appropriate base classes
5. Add to navigation menu
6. Create tests
7. Update documentation

Example:
```
tech/derbent/newmodule/
├── domain/
│   ├── CNewEntity.java
│   ├── CNewEntityStatus.java
│   └── CNewEntityType.java
├── service/
│   ├── CNewEntityService.java
│   ├── INewEntityRepository.java
│   ├── CNewEntityStatusService.java
│   └── INewEntityStatusRepository.java
└── view/
    ├── CNewEntityCard.java
    └── CNewEntityDialog.java
```

## Best Practices

### Package Organization

1. Keep packages focused on single business domain
2. Maintain consistent structure across modules
3. Use clear, descriptive package names
4. Avoid circular dependencies

### File Naming

1. Use C-prefix for all custom classes
2. Use descriptive names that indicate purpose
3. Group related files in same package
4. Follow Java naming conventions

### Code Organization

1. Keep classes focused and cohesive
2. Place utility methods in utility classes
3. Use base classes to avoid duplication
4. Document complex structures

## Related Documentation

- [Entity Inheritance Patterns](../architecture/entity-inheritance-patterns.md)
- [Service Layer Patterns](../architecture/service-layer-patterns.md)
- [View Layer Patterns](../architecture/view-layer-patterns.md)
- [Getting Started Guide](getting-started.md)
- [Coding Standards](../architecture/coding-standards.md)
- [AI Tools Guide](ai-tools-guide.md) - AI assistant configuration
- [Documentation Guide](documentation-guide.md) - Finding and organizing documentation

## Package-Level Documentation

All major packages now include `package-info.java` files that document:
- Package purpose and scope
- Key classes and subpackages
- Relationships with other packages
- Usage examples where applicable

### Documented Packages

**Top-Level Packages:**
- `tech.derbent.api` - Core framework overview
- `tech.derbent.plm` - Business modules overview
- `tech.derbent.base` - Infrastructure overview

**Business Modules:**
- `tech.derbent.plm.activities` - Activity management
- `tech.derbent.api.projects` - Project management  
- `tech.derbent.api.companies` - Company/multi-tenancy
- `tech.derbent.base.users` - User management

**Core API:**
- `tech.derbent.api.annotations` - Custom annotations (@AMetaData)
- `tech.derbent.api.services` - Base service classes
- `tech.derbent.api.ui` - UI framework components
- `tech.derbent.api.exceptions` - Custom exceptions

These package-info.java files:
- Appear in IDE tooltips when hovering over package names
- Generate package-level documentation in Javadoc
- Provide context to AI coding assistants
- Help new developers understand package purposes

To view package documentation:
1. **In IDE**: Hover over package name or press F1/Ctrl+Q
2. **In Javadoc**: Generate with `mvn javadoc:javadoc` and open `target/site/apidocs/`
3. **In Source**: Open `package-info.java` file directly

## AI Tool Configuration

The project includes configuration files for multiple AI coding assistants:

**Configuration Files (in root directory):**
- `.github/copilot-instructions.md` - GitHub Copilot (complete 468-line guide)
- `.cursorrules` - Cursor IDE (quick reference)
- `.clinerules` - Cline AI Assistant (detailed rules)
- `.aidigestconfig` - AI Digest and general AI tools

**Key Features Documented:**
- C-prefix naming convention (MANDATORY)
- Package structure (module/layer pattern)
- Notification system (CNotificationService)
- Environment setup (source ./setup-java-env.sh)
- Build commands and timing expectations

See [AI Tools Guide](ai-tools-guide.md) for complete information on AI configuration and usage.

## Documentation Archive

Historical documentation has been moved to `docs/archive/` to keep the main documentation clean and current:

**Archive Location:** `docs/archive/tasks/`
- Task completion summaries
- Implementation summaries
- Test run reports
- Screenshot documentation
- Historical development records

**Note:** Archive docs are preserved for historical context but may contain outdated information. Always refer to current documentation in main `docs/` directories.

See [Documentation Guide](documentation-guide.md) for complete documentation organization and how to find information.
