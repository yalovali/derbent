# Copilot Agent Guideline for Vaadin Java Projects (MVC Design)

## Project Overview

This is a comprehensive **project management and collaboration platform** built with **Java 17, Spring Boot 3.5, and Vaadin Flow 24.8**. The application provides workflows, task management, resource planning, and team collaboration features inspired by Atlassian Jira and ProjeQtOr.

### Key Implemented Features
- **Enhanced Activity Management**: 25+ field activity tracking with status workflows, priorities, time/cost tracking
- **User & Company Management**: Multi-tenant user system with role-based access control
- **Project Management**: Project lifecycle management with resource allocation
- **Meeting Management**: Meeting scheduling, participants, and types management
- **Risk Management**: Project risk tracking and mitigation
- **Dashboard & Reports**: KPI tracking and visual analytics

### Core Domain Model Examples
```java
// Actual domain classes from the project:
CUser extends CEntityNamed        // User management with company associations
CProject extends CEntityNamed     // Project lifecycle management
CActivity extends CEntityOfProject // 25+ field activity tracking system
CActivityStatus                   // Workflow management (TODO, IN_PROGRESS, REVIEW, etc.)
CActivityPriority                 // Priority system (CRITICAL, HIGH, MEDIUM, LOW, LOWEST)
CMeeting extends CEntityOfProject // Meeting management system
CRisk extends CEntityOfProject    // Risk management system
```

Check [project_design_description.md](./project_design_description.md) file for complete project scope and architectural details.

## Table of Contents
- [1. MVC Architecture Principles](#1-mvc-architecture-principles)
- [2. Commenting Standards](#2-commenting-standards)
- [3. Java Checks Before Submission](#3-java-checks-before-submission)
- [4. Pull Request Checklist](#4-pull-request-checklist)
- [5. Copilot Review Protocol](#5-copilot-review-protocol)
- [6. Coding Styles & Best Practices](#6-coding-styles--best-practices)
- [7. CSS Guidelines](#7-css-guidelines)
- [8. Database Rules](#8-database-rules)
- [9. Documentation & Modularity](#9-documentation--modularity)
- [10. Strict Prohibitions](#10-strict-prohibitions)
- [11. Additional Best Practices](#11-additional-best-practices)
- [Related Documentation](#related-documentation)

---

## 1. MVC Architecture Principles

- **Domain:**  
  Represents business logic and data. Use POJOs, Entities, and Service classes.
- **View:**  
  UI components using Vaadin's framework. Avoid embedding business logic in views.
- **Controller/Service:**  
  Handles user input, updates models, and refreshes views. Typically, Vaadin views act as controllers.

**Example directory structure:**
```
src/main/java/tech/derbent/
├── abstracts/           # Base classes and annotations
│   ├── annotations/     # MetaData and other annotations
│   ├── domains/         # Base domain entities (CEntityNamed, CEntityOfProject)
│   ├── services/        # Base service classes
│   └── views/           # Base UI components (CButton, CDialog, CGrid)
├── activities/          # Activity management module
│   ├── domain/         # CActivity, CActivityStatus, CActivityPriority
│   ├── service/        # Activity business logic
│   └── view/           # Activity UI components
├── users/              # User management module
│   ├── domain/         # CUser, CUserRole, CUserType
│   ├── service/        # User business logic
│   └── view/           # User UI components
├── projects/           # Project management module
├── meetings/           # Meeting management module
├── companies/          # Company management module
└── base/               # Core infrastructure
    └── ui/dialogs/     # Standard dialogs (CWarningDialog, CInformationDialog)
```

## 2. Development Environment Setup

**Current Tech Stack:**
- **Java 17+** with Spring Boot 3.5.0
- **Vaadin Flow 24.8.3** for server-side UI rendering
- **PostgreSQL** as the primary database (avoid H2 in production)
- **Maven** build system with provided pom.xml configuration
- **Spring Boot DevTools** for hot reloading during development

**Key Dependencies:**
- Spring Data JPA for database operations
- Spring Security for authentication/authorization
- Vaadin Charts for data visualization
- H2 for testing only (PostgreSQL for production)
- JUnit 5 and TestContainers for testing

**Code Quality Tools:**
- Follow the `eclipse-formatter.xml` code formatting rules
- Use static analysis tools (CheckStyle, PMD, SonarLint)
- Maintain test coverage above 80% for critical business logic

**Database Configuration:**
- Always ensure **PostgreSQL-only** configuration for production
- Use `spring.jpa.defer-datasource-initialization=true`
- Update `data.sql` with correct sample and initial database values after any database change
- Password is always **test123** for all users with hash: `$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu`

---

## 3. Commenting Standards

- **Every class:** Document its role in MVC.
- **Every method:** Describe parameters, return value, and side effects.
- **Complex logic:** Inline comments explaining tricky parts.
- **Copilot suggestions:** Review and expand comments for clarity.

---


## 4. Security and Validation Standards

- Always validate all user inputs in controllers and services
- Use Spring Security for authentication and authorization
- Implement proper RBAC (Role-Based Access Control)
- Never store sensitive data in plain text
- Use parameterized queries to prevent SQL injection
- Implement CSRF protection for all forms


## 5. Testing Requirements

- Write unit tests for all business logic and service methods
- Use TestContainers for integration testing with PostgreSQL
- Maintain test coverage above 80% for critical business logic
- Test all validation scenarios and edge cases
- Mock external dependencies appropriately

---

## 6. Java Build and Quality Checks

- Compile using `mvn clean install` or your build tool.
- Run static analysis: CheckStyle, PMD, or SonarLint.
- Run unit and integration tests. Ensure coverage for all controller logic.
- Confirm no business logic is present in View classes.
- Check for unused imports and variables.
- Validate all forms of input in controllers.

---


## 7. Pull Request Checklist


- Code follows MVC separation principles
- Methods and classes are properly commented with JavaDoc
- All Copilot-generated code has been reviewed and tested
- All Java quality checks passed: compilation, static analysis, testing
- No hardcoded values in controllers or views
- UI logic (Vaadin) does not leak into model/service layers
- Database changes include proper migrations and sample data

---


## 8. Copilot Review Protocol

After accepting Copilot suggestions, manually review for:
- Correct MVC placement and architecture compliance
- Security considerations (input validation, access control)
- Performance and scalability implications
- Proper exception handling and error messages
- Code quality and maintainability standards

---

## 9. Coding Styles & Best Practices

**General Principles:**
- Use `final` keyword wherever possible (variables, parameters, methods, classes)
- Favor abstraction: if two or more features are similar, create an abstract base class with abstract fields and methods
- Always start class names with a capital "C" (e.g., `CUser`, `CSettings`). Do not use standard Java class naming for domain classes
- Check for lazy loading issues using best practices (e.g., `@Transactional`, `fetch = FetchType.LAZY`). Add comments explaining lazy loading risks or solutions
- Always check for `null` values and possible `NullPointerException` in every function. If necessary, also check for empty strings
- Always prefer using base classes to avoid code duplication
- Every important function must start with a logger statement detailing function name and parameter values (do not log at function end)
- Always catch exceptions in Vaadin UI and handle them gracefully
- Use Vaadin UI components to display exceptions to users where possible
- Always use `MetaData.java` annotation system to bind and generate forms for entities when possible
- Extend base classes or add new ones to increase modularity and maintainability
- Always use appropriate icons for views and UI components
- For delete operations, always require explicit user approval (e.g., confirmation dialog)
- All selective ComboBoxes must be **selection only** (user must not be able to type arbitrary text)
- Keep project documentation updated in the `docs` folder; create separate documents for each design concept. If implementing complex solutions (e.g., Spring technologies), create a step-by-step solution document

**Concrete Implementation Examples:**

**Domain Class Pattern:**
```java
@Entity
@Table(name = "cactivity")
@AttributeOverride(name = "id", column = @Column(name = "activity_id"))
public class CActivity extends CEntityOfProject {
    
    @MetaData(
        displayName = "Activity Type", required = false, readOnly = false,
        description = "Type category of the activity", hidden = false, order = 2,
        dataProviderBean = "CActivityTypeService"
    )
    private CActivityType activityType;
    // ... more fields with MetaData annotations
}
```

**Service Class Pattern:**
```java
public class CActivityService extends CEntityOfProjectService<CActivity> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CActivityService.class);
    
    public CActivity createActivity(final CActivity activity) {
        LOGGER.info("createActivity called with activity: {}", activity);
        // Always log method entry with parameters
        // ... implementation
    }
}
```

**UI Component Pattern:**
```java
// Always use base classes from abstracts/views package
public class CActivityView extends CProjectAwareMDPage<CActivity> {
    
    @Override
    protected void setupButtons() {
        // Never use standard Vaadin Button - always use CButton
        final CButton saveButton = CButton.createPrimary("Save", this::saveActivity);
        final CButton cancelButton = CButton.createSecondary("Cancel", this::cancel);
    }
}
```

- Use the `abstracts/views` package for project-wide UI components (e.g., `CButton`, `CGrid`, `CDialog`). Always use these superclasses and extend them as needed; add new UI classes here
- Whenever a button action is not applicable (e.g., user not selected but project add requested), do not silently return. Show a user warning explaining the situation. Use new dialog classes:
  - `CWarningDialog` (located at `src/main/java/tech/derbent/base/ui/dialogs/CWarningDialog.java`)
  - `CInformationDialog` (located at `src/main/java/tech/derbent/base/ui/dialogs/CInformationDialog.java`)
  - `CExceptionDialog` (located at `src/main/java/tech/derbent/base/ui/dialogs/CExceptionDialog.java`)
  - These dialogs must be simple, visually appealing, and inherit from the common `CDialog` superclass
- Never add loggers at the end of functions. Always log at the start with full detail about the function and parameters
- All code must follow Java naming conventions for variables and methods, except for class names which must start with "C"

## 9.1. MetaData Annotation System

This project uses a sophisticated **@MetaData** annotation system for automatic form generation and field configuration. This is a key architectural pattern that must be followed:

**MetaData Annotation Pattern:**
```java
@MetaData(
    displayName = "User Name",           // UI label
    required = true,                     // Validation requirement
    readOnly = false,                    // Field editability
    defaultValue = "",                   // Default field value
    description = "User's first name",   // Tooltip/help text
    hidden = false,                      // Visibility control
    order = 1,                          // Display order in forms
    maxLength = MAX_LENGTH_NAME,        // Field length constraints
    dataProviderBean = "CUserService"   // For ComboBox data source
)
private String name;
```

**Real Examples from the Codebase:**
```java
// From CActivity.java - Complex relationship with data provider
@MetaData(
    displayName = "Activity Type", required = false, readOnly = false,
    description = "Type category of the activity", hidden = false, order = 2,
    dataProviderBean = "CActivityTypeService"
)
private CActivityType activityType;

// From CUser.java - Simple string field with validation
@MetaData(
    displayName = "User Name", required = true, readOnly = false,
    defaultValue = "", description = "User's first name", hidden = false,
    order = 1, maxLength = MAX_LENGTH_NAME
)
private String name;
```

**Benefits of MetaData System:**
- Automatic form generation using `CEntityFormBuilder`
- Consistent validation across the application
- Centralized field configuration
- Automatic ComboBox data provider binding
- Consistent UI labeling and ordering

**Usage Guidelines:**
- Always use MetaData for all entity fields that appear in UI forms
- Specify appropriate `dataProviderBean` for relationship fields
- Use meaningful `displayName` values for user-friendly labels
- Set proper `order` values to control form field sequence
- Include helpful `description` text for complex fields

## 9.2. CSS Guidelines
- Update CSS names from class name, also update CSS file accordingly
- Always use very simple CSS. Don't use JavaScript in Java or CSS
- You can use CSS built-in simple functions
- To style a layout use syntax like:
  ```css
  #vaadinLoginOverlayWrapper::part(overlay) {
      background-image: url('./images/background1.png');
  }
  ```
- Check the pattern of CPanelActivityDescription and its super classes. Use this pattern to group each entity's fields according to related business topic. Don't leave any field out. Create necessary classes, base classes with same naming convention for every entity and group fields in the view. Only open the first one
- Always check reference projects and do this task for a better structure, view and requirements of successful resource management, task tracking, project management, budget planning and better UI experience
- Always if necessary create new classes to support the new requirements with always less code and nice super classes for reusability
- Prepare a detailed requirements document that includes this task description, main features, user roles, and any specific technologies or integrations that should be considered

## 9.3. Database Rules and Sample Data
- Password is always test123 for all users with hash code '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu'
- Every entity should have an example in data.sql for per project, per company per user per activity etc...
- Always ensure **PostgreSQL-only** configuration. Update `data.sql` with correct sample and initial database values after any database change
- Keep spring.jpa.defer-datasource-initialization=true
- Don't use memory database H2 in production
- Always delete tables at top of data.sql before you insert values into it. Check them before they exist
- Delete constraints etc. if there is change in the DB structure
- Always reset sequences in the format and escape characters below with a conditional to check if they exist first:
  ```sql
  DO '
  BEGIN
      IF EXISTS (
          SELECT 1
          FROM pg_class c
          JOIN pg_namespace n ON n.oid = c.relnamespace
          WHERE c.relkind = ''S'' AND c.relname = ''ctask_task_id_seq''
      ) THEN
          EXECUTE ''SELECT setval(''''ctask_task_id_seq'''', 1, false)'';
      END IF;
  END;
  ';
  ```

## 9.4. Documentation & Modularity
- Update the `docs` folder for every significant change:
  - Add new documentation for each project design concept (one file per concept)
  - For complex or Spring-based solutions, create a step-by-step solution file
- All new UI components, dialogs, and base classes must be documented
- Maintain consistency with existing documentation patterns

### Available Documentation:
- [Project Design Description](./project_design_description.md) - Overall project scope and requirements
- [Enhanced Activity Management](./enhanced-activity-management-implementation.md) - Activity system implementation
- [CSS Guidelines](./CSS_GUIDELINES.md) - Styling standards and best practices
- [CPanelActivityDescription Implementation](./CPANEL_ACTIVITY_DESCRIPTION_IMPLEMENTATION.md) - Entity panel patterns
- [User Profile Dialog Implementation](./user-profile-dialog-implementation.md) - Dialog implementation patterns
- [Annotation-Based ComboBox Data Providers](./ANNOTATION_BASED_COMBOBOX_DATA_PROVIDERS.md) - Form building patterns
- [Hierarchical Side Menu Implementation](./HIERARCHICAL_SIDE_MENU_IMPLEMENTATION.md) - Navigation patterns


## 10. Strict Prohibitions

- **Never use standard Vaadin `Button` directly!**  
  Immediately refactor to use `CButton` (or its improved/extended version).  
  If `CButton` can be improved, extend it so all buttons share consistent behavior and appearance
- Never bypass base classes for UI components. Always use or extend base classes from `abstracts/view`
- Never silently fail on user actions. Always notify the user with an appropriate dialog
- Never allow editable text in selection-only ComboBoxes
- Never use H2 database in production environments
- Never log sensitive information (passwords, tokens, personal data)
- Never commit hardcoded configuration values or secrets
## 11. Additional Best Practices
- Use dependency injection wherever possible
- All domain classes should be immutable where practical
- Always write unit tests for new features or bug fixes
- Always use proper JavaDoc for public classes and methods
- Ensure all database changes are backward compatible and thoroughly tested
- Use enums for all constant lists and types
- Ensure all UI components are accessible (a11y) and responsive
- Follow SOLID principles in class design
- Implement proper logging with appropriate log levels
- Use transactions appropriately for data consistency

---

## Related Documentation

For comprehensive development guidance, refer to these additional documents:

### Core Architecture & Design
- [Project Design Description](./project_design_description.md) - Complete project requirements and architecture
- [Enhanced Activity Management Requirements](./enhanced-activity-management-requirements.md) - Activity system specifications

### UI/UX Implementation Guides  
- [CSS Guidelines](./CSS_GUIDELINES.md) - Complete styling standards and Vaadin theming
- [CPanelActivityDescription Implementation](./CPANEL_ACTIVITY_DESCRIPTION_IMPLEMENTATION.md) - Entity panel patterns
- [User Profile Dialog Implementation](./user-profile-dialog-implementation.md) - Dialog patterns
- [Hierarchical Side Menu Implementation](./HIERARCHICAL_SIDE_MENU_IMPLEMENTATION.md) - Navigation implementation

### Technical Implementation Patterns
- [Annotation-Based ComboBox Data Providers](./ANNOTATION_BASED_COMBOBOX_DATA_PROVIDERS.md) - Form building with annotations
- [Comprehensive Lazy Loading Fix](./COMPREHENSIVE_LAZY_LOADING_FIX.md) - JPA performance optimization
- [User Company Association Requirements](./user-company-association-requirements.md) - Multi-tenant patterns

### Development Best Practices
- [CActivity Enhancement Summary](./cactivity-enhancement-summary.md) - Activity domain implementation patterns

---

> **How to use with GitHub Copilot and PRs:**  
> - This file exists in your `docs` folder and should also be copied to project root.  
> - Link to this file in your README and in your PR/issue templates.
> - Remind contributors (and Copilot) to always follow these rules in every code review and commit.
> - Reference the related documentation above for specific implementation patterns.

