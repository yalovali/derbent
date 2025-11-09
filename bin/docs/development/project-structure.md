# Project Structure and Organization

## Overview

This document describes the complete project structure of Derbent, explaining the purpose and organization of each directory and key files.

## Root Directory Structure

```
derbent/
├── .github/                    # GitHub configuration
│   ├── workflows/             # CI/CD pipelines
│   └── copilot-instructions.md # Copilot configuration
├── .mvn/                      # Maven wrapper files
├── docs/                      # Documentation
│   ├── architecture/          # Design patterns and architecture
│   ├── development/           # Developer guides
│   ├── implementation/        # Implementation details
│   └── testing/               # Testing documentation
├── profile-pictures/          # User profile images
├── src/                       # Source code
│   ├── main/
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

## Source Code Structure

### Main Source Directory

```
src/main/java/tech/derbent/
├── Application.java           # Main Spring Boot application
├── api/                       # Core framework and utilities
│   ├── annotations/          # Custom annotations
│   ├── components/           # Reusable UI components
│   ├── domains/              # Base entity classes
│   ├── exceptions/           # Custom exceptions
│   ├── interfaces/           # Shared interfaces
│   ├── roles/                # Role management
│   ├── services/             # Base service classes
│   ├── ui/                   # UI utilities
│   ├── utils/                # Utility classes
│   └── views/                # Base view classes
├── activities/               # Activity management module
│   ├── domain/              # CActivity, CProjectItemStatus, CActivityType
│   ├── service/             # CActivityService, repositories
│   └── view/                # Activity UI components
├── comments/                 # Comment management
├── companies/                # Company management
├── config/                   # Application configuration
├── dashboard/                # Dashboard view
├── decisions/                # Decision management
├── gannt/                    # Gantt chart views
├── kanban/                   # Kanban board implementation
├── login/                    # Login and authentication
├── meetings/                 # Meeting management
├── orders/                   # Order management
├── page/                     # Dynamic page system
├── projects/                 # Project management
├── risks/                    # Risk management
├── screens/                  # Screen management
├── session/                  # Session management
├── setup/                    # System setup
└── users/                    # User management
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
├── CEntityOfProject.java       # Project-scoped entity
├── CProjectItem.java           # Hierarchical entity
├── CTypeEntity.java            # Type entity base
├── CEvent.java                 # Event base
└── AbstractEntity.java         # Legacy support
```

### api/services

Base service classes:

```
services/
├── CAbstractService.java            # Base service
├── CEntityNamedService.java         # Named entity service
├── CEntityOfProjectService.java     # Project-scoped service
├── IAbstractRepository.java         # Base repository interface
├── IEntityNamedRepository.java      # Named entity repository
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
