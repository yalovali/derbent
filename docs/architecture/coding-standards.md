# Copilot Agent Guideline for Vaadin Java Projects (MVC Design)

## Project Overview

This is a comprehensive **project management and collaboration platform** built with **Java 17, Spring Boot 3.5, and Vaadin Flow 24.8**. The application provides workflows, task management, resource planning, and team collaboration features inspired by Atlassian Jira and ProjeQtOr.

### Key Implemented Features
- **Enhanced Activity Management**: 25+ field activity tracking with status workflows, priorities, time/cost tracking, parent-child relationships
- **User & Company Management**: Multi-tenant user system with role-based access control, company associations
- **Project Management**: Project lifecycle management with resource allocation and timeline tracking
- **Meeting Management**: Comprehensive meeting scheduling with participants, types, and agenda management
- **Risk Management**: Project risk identification, assessment, and mitigation tracking
- **Dashboard & Analytics**: KPI tracking, progress visualization, and performance metrics
- **Hierarchical Navigation**: Multi-level side menu system with context-aware navigation
- **Advanced UI Components**: Custom dialog system, accordion layouts, responsive grid components

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
- [Java Strict Coding Rules for Derbent Project](#java-strict-coding-rules-for-derbent-project)
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
│   └── tests/           # Tests UI, unit etc.
├── activities/          # Activity management module
│   ├── domain/         # CActivity, CActivityStatus, CActivityPriority
│   ├── service/        # Activity business logic
│   └── view/           # Activity UI components
│   └── tests/           # Tests UI, unit etc.
├── users/              # User management module
│   ├── domain/         # CUser, CUserRole, CUserType
│   ├── service/        # User business logic
│   └── tests/           # Tests UI, unit etc.
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
- Update `data.sql` with correct database clean up procedures not initial value.
- For initial values update sample CSampleDataInitializer for every entity entitytype with at least 5 examples
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

### 5.1 Test Organization Structure

Tests are organized into three main directories under `src/test/java/`:

1. **Unit Tests** (`src/test/java/unit-tests/`):
   - Business logic tests
   - Service layer tests  
   - Domain entity tests
   - Validation tests
   - Integration tests
   - Manual verification tests

2. **UI Tests** (`src/test/java/ui-tests/`):
   - Vaadin UI component tests
   - View tests without browser automation
   - Form validation UI tests
   - Component interaction tests

3. **Automated Tests** (`src/test/java/automated-tests/`):
   - Playwright browser automation tests
   - End-to-end testing scenarios
   - Full application workflow tests
   - Cross-browser compatibility tests

### 5.2 Running Tests

Use the following commands to run specific test categories:

```bash
# Run all tests
mvn test

# Run only unit tests
mvn test -Dtest="**/unit-tests/**/*Test"

# Run only UI tests  
mvn test -Dtest="**/ui-tests/**/*Test"

# Run only automated tests
mvn test -Dtest="**/automated-tests/**/*Test"

# Run Playwright tests using script
./run-playwright-tests.sh all
```

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
- **Lazy Loading Best Practices**: Use eager loading queries in repositories (e.g., `LEFT JOIN FETCH`) for commonly accessed relationships to prevent `LazyInitializationException`
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
    maxLength = CEntityConstants.MAX_LENGTH_NAME,        // Field length constraints
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
    order = 1, maxLength = CEntityConstants.MAX_LENGTH_NAME
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

**File Organization:**
- Main styles: `src/main/frontend/themes/default/styles.css`
- Login styles: `src/main/frontend/themes/default/dev-login.css`
- Background styles: `src/main/frontend/themes/default/login-background.css`

**CSS Best Practices:**
- Always use very simple CSS. Don't use JavaScript in Java or CSS
- Use CSS built-in simple functions and Vaadin Lumo design tokens
- Update CSS class names based on component class names, update CSS file accordingly
- Use Vaadin Shadow DOM parts for component styling:
  ```css
  #vaadinLoginOverlayWrapper::part(overlay) {
      background-image: url('./images/background1.png');
  }
  ```

**Real CSS Examples from the Project:**
```css
/* Dashboard view styling */
.cdashboard-view {
    width: 100%;
    padding: var(--lumo-space-m);
    background: var(--lumo-base-color);
}

/* Detailed tab layout with background */
.details-tab-layout {
    background-color: #fff7e9;
    width: 99%;
    border-radius: 12px;
    border: 1px solid var(--lumo-contrast-10pct);
    font-size: 1.6em;
    font-weight: bold;
}

/* Accordion styling */
vaadin-accordion-heading {
    font-size: var(--lumo-font-size-m);
    font-weight: 600;
    color: #5d6069;
    font-size: 1.3em;
    border-bottom: 1px solid var(--lumo-contrast-10pct);
}
```

**Entity Panel Pattern Implementation:**
- Follow the pattern of `CPanelActivityDescription` and its superclasses
- Use this pattern to group each entity's fields according to related business topics
- Don't leave any field out - create comprehensive panels for all entity aspects
- Create necessary classes and base classes with consistent naming conventions
- Only open the first panel by default (call `openPanel()` in constructor)

**Example Panel Implementation:**
```java
public class CPanelActivityDescription extends CPanelActivityBase {
    public CPanelActivityDescription(CActivity currentEntity,
        CEnhancedBinder<CActivity> binder, CActivityService service) {
        super("Basic Information", currentEntity, binder, service);
        openPanel(); // Only open this panel by default
    }
    
    @Override
    protected void updatePanelEntityFields() {
        // Group related fields logically
        setEntityFields(List.of("name", "description", "activityType", "project"));
    }
}
```

**Panel Organization Guidelines:**
- Create separate panels for different business aspects:
  - `CPanelActivityDescription` - Basic information
  - `CPanelActivityStatusPriority` - Status and priority management
  - `CPanelActivityTimeTracking` - Time and progress tracking
  - `CPanelActivityBudgetManagement` - Cost and budget information
  - `CPanelActivityResourceManagement` - User assignments and resources

## 9.5. Architecture Patterns and Best Practices

**Entity Hierarchy Pattern:**
The project uses a sophisticated inheritance hierarchy for domain entities:
```java
CEntityBase (id, createdBy, createdDate, lastModified)
├── CEntityNamed (name, description) 
│   ├── CUser, CProject, CActivityType, CActivityStatus
│   └── CCompany, CMeetingType, CUserType
└── CEntityOfProject (extends CEntityBase + project relationship)
    ├── CActivity (comprehensive activity management)
    ├── CMeeting (meeting management)
    └── CRisk (risk management)
```

**Service Layer Pattern:**
```java
// Base service with CRUD operations
public abstract class CEntityService<T extends CEntityBase> {
    protected abstract CEntityRepository<T> getRepository();
    // Common CRUD methods with logging
}

// Project-aware service for entities linked to projects
public abstract class CEntityOfProjectService<T extends CEntityOfProject> 
    extends CEntityService<T> {
    // Project-specific operations
}

// Concrete implementation
public class CActivityService extends CEntityOfProjectService<CActivity> {
    // Activity-specific business logic
}
```

**UI Component Hierarchy:**
```java
CAbstractPage (base page functionality)
├── CAbstractMDPage (metadata-driven pages)
│   ├── CProjectAwareMDPage (project context awareness)
│   │   └── CActivitiesView, CMeetingsView
│   └── CCustomizedMDPage (custom behavior)
└── CDialog (base dialog functionality)
    └── CWarningDialog, CInformationDialog, CExceptionDialog
```

**Form Building Pattern:**
- Use `@MetaData` annotations on entity fields
- Automatic form generation via reflection
- Consistent validation and data binding
- ComboBox data providers via service beans
- Panel-based organization for complex entities

**Logging Standards:**
```java
public class CActivityService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CActivityService.class);
    
    public CActivity save(CActivity activity) {
        LOGGER.info("save called with activity: {}", activity);
        // Implementation follows
    }
}
```

**Security and Validation:**
- Always validate inputs in service layer
- Use Spring Security for authentication/authorization
- Implement proper null checking in all methods
- Use `@Transactional` for data consistency
- Handle exceptions gracefully with user-friendly dialogs

**Testing Standards:**
The project includes **41 comprehensive test classes** covering:
```java
// Unit tests for domain logic
class CActivityCardTest {
    @Test
    void testActivityCardCreation() {
        final CProject project = new CProject();
        project.setName("Test Project");
        final CActivity activity = new CActivity("Test Activity", project);
        
        final CActivityCard card = new CActivityCard(activity);
        
        assertNotNull(card);
        assertEquals(activity, card.getActivity());
    }
}

// Integration tests for services
@SpringBootTest
class SessionServiceProjectChangeTest {
    // Test service layer integration
}

// Manual verification tests for complex UI components
class ManualVerificationTest {
    // UI component validation tests
}
```

## 9.6. Lazy Loading Architecture Guidelines (Current Implementation)

**Critical**: Follow the established lazy loading architecture. See `COMPREHENSIVE_LAZY_LOADING_FIX.md` for complete implementation details.

### 9.6.3. Current Lazy Loading Architecture (✅ UPDATED)
The application uses a comprehensive lazy loading solution implemented in base classes:

**Base Architecture**:
- **CAbstractService**: Provides `initializeLazyFields()` with automatic `CEntityOfProject` detection
- **CEntityOfProjectService**: Enhanced lazy field initialization for project-aware entities  
- **Repository Layer**: Uses `LEFT JOIN FETCH` queries for eager loading
- **Service Layer**: Overrides `getById()` methods for entity-specific eager loading

**Fixed User Service Method**:
```java
@Transactional (readOnly = true)
public CUser getUserWithProjects(final Long id) {
    LOGGER.debug("Getting user with projects for ID: {}", id);
    // Get user and initialize all lazy fields including project settings
    final CUser user = repository.findById(id).orElseThrow(
        () -> new EntityNotFoundException("User not found with ID: " + id));
    
    // Initialize lazy fields to prevent LazyInitializationException
    initializeLazyFields(user);
    
    return user;
}
```

This fix ensures that when the CUsersView loads user data with projects, all lazy relationships (UserType, Company, ProjectSettings) are properly initialized, preventing LazyInitializationException issues.

### 9.6.2. Implementation Pattern for New Entities
All new entities with lazy relationships must follow this established pattern:

**1. Repository Layer** - Add eager loading queries when needed:
```java
@Repository
public interface CNewEntityRepository extends CEntityOfProjectRepository<CNewEntity> {
    // Only add custom queries if base class methods are insufficient
    @Query("SELECT e FROM CNewEntity e LEFT JOIN FETCH e.entityType WHERE e.project = :project")
    List<CNewEntity> findByProject(@Param("project") CProject project);
}
```

**2. Service Layer** - Minimal overrides, leverage base class functionality:
```java
@Service
public class CNewEntityService extends CEntityOfProjectService<CNewEntity> {
    
    // Only override getById() if custom eager loading is needed
    @Override
    @Transactional(readOnly = true)
    public Optional<CNewEntity> getById(final Long id) {
        final Optional<CNewEntity> entity = repository.findById(id);
        entity.ifPresent(this::initializeLazyFields);
        return entity;
    }
    
    // Only override initializeLazyFields() if entity has specific lazy relationships
    @Override
    protected void initializeLazyFields(final CNewEntity entity) {
        super.initializeLazyFields(entity); // Handles CEntityOfProject automatically
        initializeLazyRelationship(entity.getSpecificField());
    }
    
    @Override
    protected Class<CNewEntity> getEntityClass() { return CNewEntity.class; }
}
```

**3. UI Layer** - Use service methods, avoid direct entity access:
```java
// ✅ CORRECT: Use service.getById() which handles lazy loading
public class CNewEntityView extends CProjectAwareMDPage<CNewEntity> {
    private void displayEntityDetails(final Long entityId) {
        final Optional<CNewEntity> entity = service.getById(entityId);
        entity.ifPresent(e -> {
            // All lazy fields are properly initialized
            typeField.setValue(e.getEntityType());
        });
    }
}
```

### 9.6.3. Prohibited Service Patterns
**❌ DO NOT create unnecessary auxiliary service methods**:

```java
// ❌ PROHIBITED: Unnecessary auxiliary setter methods
@Transactional
public CNewEntity setEntityType(final CNewEntity entity, final CNewEntityType type) {
    entity.setEntityType(type);
    return save(entity);
}

// ✅ CORRECT: Use entity setters directly
entity.setEntityType(type);
service.save(entity);
```

**❌ DO NOT create redundant find methods**:
```java
// ❌ PROHIBITED: Redundant find methods when base class provides equivalent
public List<CNewEntity> findEntitiesByProject(final CProject project) {
    return findEntriesByProject(project); // Base class already provides this
}
```

### 9.6.4. Migration from Auxiliary Methods
Existing auxiliary methods are being deprecated in favor of direct entity manipulation:

```java
// ❌ DEPRECATED: Auxiliary service methods (being phased out)
@Deprecated
@Transactional
public CActivity setActivityType(final CActivity activity, final CActivityType type, final String description) {
    // Implementation
}

// ✅ PREFERRED: Direct entity setters + service save
activity.setActivityType(type);
activity.setDescription(description);
activityService.save(activity);
```

## 9.7. Database Rules and Sample Data
- **Never use primitive data types in entities** (int, boolean, etc.) - use wrapper types (Integer, Boolean, etc.)
- Password is always **test123** for all users with hash code `$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu`
- Every entity should have examples in data.sql for per project, per company, per user, per activity etc.
- Always ensure **PostgreSQL-only** configuration. Update `data.sql` with correct sample and initial database values after any database change
- Keep `spring.jpa.defer-datasource-initialization=true`
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

## 9.8. Project Parameter Requirements for Project-Related Classes

### 9.8.1. Mandatory Project Context in Queries

**CRITICAL RULE**: All project-related entity classes (extending `CEntityOfProject`) must ALWAYS include project parameter in queries. Never execute queries without project context.

**Repository Layer - Project-Aware Queries:**
```java
@Repository
public interface CActivityRepository extends CEntityRepository<CActivity> {
    
    // ✅ REQUIRED: Always include project parameter
    @Query("SELECT a FROM CActivity a WHERE a.project.id = :projectId")
    List<CActivity> findByProjectId(@Param("projectId") Long projectId);
    
    // ✅ REQUIRED: Project parameter with additional filters
    @Query("SELECT a FROM CActivity a WHERE a.project.id = :projectId AND a.activityStatus.id = :statusId")
    List<CActivity> findByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("statusId") Long statusId);
    
    // ✅ REQUIRED: Project parameter with eager loading
    @Query("SELECT a FROM CActivity a LEFT JOIN FETCH a.activityType WHERE a.project.id = :projectId")
    List<CActivity> findByProjectIdWithActivityType(@Param("projectId") Long projectId);
    
    // ❌ FORBIDDEN: Queries without project parameter
    @Query("SELECT a FROM CActivity a WHERE a.activityStatus.id = :statusId")
    List<CActivity> findByStatus(@Param("statusId") Long statusId); // VIOLATION!
    
    // ❌ FORBIDDEN: findAll() usage for project entities
    List<CActivity> findAll(); // VIOLATION - returns data from all projects!
}
```

**Service Layer - Project Context Enforcement:**
```java
@Service
public class CActivityService extends CEntityOfProjectService<CActivity> {
    
    // ✅ REQUIRED: Override methods to enforce project context
    @Override
    public List<CActivity> findAll() {
        throw new UnsupportedOperationException(
            "findAll() is not allowed for project entities. Use findByProjectId(Long projectId) instead."
        );
    }
    
    // ✅ REQUIRED: Project-aware method implementations
    public List<CActivity> findByProjectId(final Long projectId) {
        LOGGER.info("findByProjectId called with projectId: {}", projectId);
        
        if (projectId == null) {
            throw new ServiceException("Project ID cannot be null for project-related queries");
        }
        
        return ((CActivityRepository) repository).findByProjectId(projectId);
    }
    
    // ✅ REQUIRED: Always validate project context in save operations
    @Override
    public CActivity save(final CActivity activity) {
        LOGGER.info("save called with activity: {}", activity);
        
        if (activity.getProject() == null || activity.getProject().getId() == null) {
            throw new ServiceException("Activity must be associated with a valid project before saving");
        }
        
        return super.save(activity);
    }
    
    // ✅ REQUIRED: Project context in search methods
    public List<CActivity> searchByNameAndProject(final String name, final Long projectId) {
        LOGGER.info("searchByNameAndProject called with name: {}, projectId: {}", name, projectId);
        
        if (projectId == null) {
            throw new ServiceException("Project ID is required for activity search");
        }
        
        return ((CActivityRepository) repository).findByNameContainingIgnoreCaseAndProjectId(name, projectId);
    }
}
```

### 9.8.2. UI Layer Project Context

**View Layer - Project-Aware Components:**
```java
public class CActivitiesView extends CProjectAwareMDPage<CActivity> {
    
    // ✅ REQUIRED: Always use current project from session
    @Override
    protected void refreshGrid() {
        final Long currentProjectId = getCurrentProjectId();
        if (currentProjectId == null) {
            showWarning("Please select a project to view activities");
            grid.setItems(Collections.emptyList());
            return;
        }
        
        final List<CActivity> activities = service.findByProjectId(currentProjectId);
        grid.setItems(activities);
    }
    
    // ✅ REQUIRED: Project validation in create operations
    @Override
    protected void createNewEntity() {
        final Long currentProjectId = getCurrentProjectId();
        if (currentProjectId == null) {
            showWarning("Please select a project before creating a new activity");
            return;
        }
        
        final CActivity newActivity = new CActivity();
        newActivity.setProject(getCurrentProject());
        openEntityDialog(newActivity);
    }
    
    // ✅ REQUIRED: Project context in filter operations
    private void applyStatusFilter(final CActivityStatus selectedStatus) {
        final Long currentProjectId = getCurrentProjectId();
        if (currentProjectId == null) {
            return;
        }
        
        if (selectedStatus == null) {
            refreshGrid(); // Shows all activities for current project
        } else {
            final List<CActivity> filteredActivities = 
                service.findByProjectIdAndStatus(currentProjectId, selectedStatus.getId());
            grid.setItems(filteredActivities);
        }
    }
}
```

### 9.8.3. Session Management for Project Context

**Project Session Service:**
```java
@Service
@Component
@Scope("session")
public class CProjectSessionService {
    
    private Long currentProjectId;
    private CProject currentProject;
    
    // ✅ REQUIRED: Always validate project context
    public Long getCurrentProjectId() {
        if (currentProjectId == null) {
            throw new SessionException("No project selected in current session");
        }
        return currentProjectId;
    }
    
    // ✅ REQUIRED: Project context change notifications
    public void setCurrentProject(final CProject project) {
        LOGGER.info("setCurrentProject called with project: {}", project);
        
        if (project == null || project.getId() == null) {
            throw new SessionException("Cannot set null project as current project");
        }
        
        this.currentProject = project;
        this.currentProjectId = project.getId();
        
        // Notify all project-aware components
        eventBus.fireEvent(new ProjectChangeEvent(project));
    }
}
```

### 9.8.4. Query Security and Validation

**Repository Security Patterns:**
```java
@Repository
public interface CRiskRepository extends CEntityRepository<CRisk> {
    
    // ✅ REQUIRED: Security through project filtering
    @Query("SELECT r FROM CRisk r WHERE r.project.id = :projectId AND r.createdBy.id = :userId")
    List<CRisk> findByProjectIdAndCreatedBy(@Param("projectId") Long projectId, @Param("userId") Long userId);
    
    // ✅ REQUIRED: Always include project in count queries
    @Query("SELECT COUNT(r) FROM CRisk r WHERE r.project.id = :projectId")
    Long countByProjectId(@Param("projectId") Long projectId);
    
    // ✅ REQUIRED: Project context in complex queries
    @Query("SELECT r FROM CRisk r WHERE r.project.id = :projectId AND r.severity = :severity AND r.status = 'OPEN'")
    List<CRisk> findOpenRisksBySeverityAndProject(@Param("projectId") Long projectId, @Param("severity") String severity);
}
```

## 9.9. Repository and Service Class Guidelines

### 9.8.1. Avoiding Lazy Loading Issues

**Repository Layer - Always Provide Eager Loading Queries:**
```java
@Repository
public interface CEntityRepository extends CEntityRepository<CEntity> {
    // ✅ REQUIRED: Eager loading query for main relationships
    @Query("SELECT e FROM CEntity e LEFT JOIN FETCH e.entityType WHERE e.id = :id")
    Optional<CEntity> findByIdWithEntityType(@Param("id") Long id);
    
    // ✅ REQUIRED: Full eager loading for complex views
    @Query("SELECT e FROM CEntity e LEFT JOIN FETCH e.entityType LEFT JOIN FETCH e.project WHERE e.id = :id")
    Optional<CEntity> findByIdWithFullData(@Param("id") Long id);
    
    // ✅ REQUIRED: Project-aware queries (see section 9.9)
    @Query("SELECT e FROM CEntity e LEFT JOIN FETCH e.entityType WHERE e.project.id = :projectId")
    List<CEntity> findByProjectIdWithEntityType(@Param("projectId") Long projectId);
}
```

**Service Layer - Mandatory Overrides and Patterns:**
```java
@Service
public class CEntityService extends CEntityOfProjectService<CEntity> {
    
    // ✅ REQUIRED: Override get() method for eager loading
    @Override
    @Transactional(readOnly = true)
    public Optional<CEntity> get(final Long id) {
        LOGGER.info("get called with id: {} (eager loading relationships)", id);
        final Optional<CEntity> entity = ((CEntityRepository) repository).findByIdWithEntityType(id);
        entity.ifPresent(this::initializeLazyFields);
        return entity;
    }
    
    // ✅ REQUIRED: Override initializeLazyFields for custom relationships
    @Override
    protected void initializeLazyFields(final CEntity entity) {
        super.initializeLazyFields(entity); // Handles CEntityOfProject relationships
        initializeLazyRelationship(entity.getEntityType(), "entityType");
        // Initialize other lazy relationships as needed
    }
}
```

### 9.8.2. Reflection Usage Guidelines

**Proper Reflection Implementation:**
```java
public class CEntityService extends CEntityOfProjectService<CEntity> {
    
    // ✅ CORRECT: Use reflection for generic field initialization
    private void initializeFieldsUsingReflection(final Object entity) {
        final Field[] fields = entity.getClass().getDeclaredFields();
        for (final Field field : fields) {
            try {
                if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)) {
                    field.setAccessible(true);
                    final Object relationshipValue = field.get(entity);
                    if (relationshipValue != null) {
                        // Force initialization to avoid lazy loading
                        Hibernate.initialize(relationshipValue);
                    }
                }
            } catch (final IllegalAccessException e) {
                LOGGER.warn("Could not access field {} for lazy initialization", field.getName(), e);
            }
        }
    }
    
    // ✅ CORRECT: Use reflection for dynamic method invocation
    public Object invokeMethodDynamically(final String methodName, final Object... args) {
        try {
            final Method method = this.getClass().getDeclaredMethod(methodName, 
                Arrays.stream(args).map(Object::getClass).toArray(Class[]::new));
            method.setAccessible(true);
            return method.invoke(this, args);
        } catch (final ReflectiveOperationException e) {
            LOGGER.error("Failed to invoke method {} dynamically", methodName, e);
            throw new ServiceException("Dynamic method invocation failed", e);
        }
    }
}
```

### 9.8.3. Avoiding Primitive Data Types

**Mandatory Wrapper Type Usage:**
```java
@Entity
public class CEntity extends CEntityOfProject {
    
    // ❌ FORBIDDEN: Primitive types
    private int count;
    private boolean active;
    private double amount;
    private long timestamp;
    
    // ✅ REQUIRED: Wrapper types only
    private Integer count;
    private Boolean active;
    private Double amount;
    private Long timestamp;
    
    @MetaData(
        displayName = "Count", required = false, readOnly = false,
        description = "Number of items", hidden = false, order = 1
    )
    @Column(name = "count_value")
    private Integer count; // Always nullable wrapper types
    
    @MetaData(
        displayName = "Active Status", required = false, readOnly = false,
        description = "Whether the entity is active", hidden = false, order = 2
    )
    @Column(name = "is_active")
    private Boolean active; // Allows null values for better data integrity
}
```

**Validation and Null Handling:**
```java
public class CEntityService extends CEntityOfProjectService<CEntity> {
    
    public CEntity updateCount(final Long entityId, final Integer newCount) {
        LOGGER.info("updateCount called with entityId: {}, newCount: {}", entityId, newCount);
        
        // ✅ REQUIRED: Always validate wrapper types for null
        if (entityId == null) {
            throw new ServiceException("Entity ID cannot be null");
        }
        
        final Optional<CEntity> entityOpt = get(entityId);
        if (entityOpt.isEmpty()) {
            throw new ServiceException("Entity not found with ID: " + entityId);
        }
        
        final CEntity entity = entityOpt.get();
        
        // ✅ REQUIRED: Handle null wrapper types appropriately
        entity.setCount(newCount != null ? newCount : 0);
        
        return save(entity);
    }
}
```

### 9.8.4. Service Layer Best Practices

**Transaction Management:**
```java
@Service
@Transactional
public class CEntityService extends CEntityOfProjectService<CEntity> {
    
    // ✅ REQUIRED: Read-only transactions for queries
    @Override
    @Transactional(readOnly = true)
    public Optional<CEntity> get(final Long id) {
        // Implementation
    }
    
    // ✅ REQUIRED: Write transactions for modifications
    @Override
    @Transactional
    public CEntity save(final CEntity entity) {
        // Implementation
    }
    
    // ✅ REQUIRED: Custom transaction boundaries for complex operations
    @Transactional(rollbackFor = {ServiceException.class, DataIntegrityViolationException.class})
    public CEntity performComplexOperation(final CEntity entity) {
        // Implementation
    }
}
```

**Error Handling and Validation:**
```java
public class CEntityService extends CEntityOfProjectService<CEntity> {
    
    // ✅ REQUIRED: Comprehensive validation
    private void validateEntity(final CEntity entity) {
        if (entity == null) {
            throw new ServiceException("Entity cannot be null");
        }
        if (entity.getProject() == null) {
            throw new ServiceException("Entity must be associated with a project");
        }
        if (entity.getName() == null || entity.getName().trim().isEmpty()) {
            throw new ServiceException("Entity name cannot be null or empty");
        }
    }
    
    // ✅ REQUIRED: Proper exception handling
    @Override
    public CEntity save(final CEntity entity) {
        LOGGER.info("save called with entity: {}", entity);
        
        try {
            validateEntity(entity);
            return super.save(entity);
        } catch (final DataIntegrityViolationException e) {
            LOGGER.error("Data integrity violation while saving entity: {}", entity, e);
            throw new ServiceException("Failed to save entity due to data integrity violation", e);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error while saving entity: {}", entity, e);
            throw new ServiceException("Failed to save entity", e);
        }
    }
}
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


## 9.12. Service Layer Best Practices - Avoiding Unnecessary Functions

### 9.12.1. Core Principle: Minimal Service Functions
Services should contain only essential business logic, not simple wrapper methods around entity setters.

**✅ REQUIRED Service Functions:**
- CRUD operations (`save`, `getById`, `findEntriesByProject`)
- Complex business logic with validation
- Multi-entity transactions  
- Security-sensitive operations
- Domain-specific queries and calculations

**❌ PROHIBITED Service Functions:**
- Simple wrapper methods around entity setters
- Methods that just set multiple fields sequentially
- Auxiliary methods that provide no business value

### 9.12.2. Current Refactoring: Deprecated Auxiliary Methods

**Services Currently Being Refactored:**
```java
// CActivityService - Following methods are deprecated:
@Deprecated setActivityType()    // Use: activity.setActivityType() + save()
@Deprecated setAssignedUsers()   // Use: activity.setAssignedTo() + save()
@Deprecated setTimeTracking()    // Use: activity.setEstimatedHours() + save()
@Deprecated setDateInfo()        // Use: activity.setStartDate() + save()
@Deprecated setStatusAndPriority() // Use: activity.setStatus() + save()

// CUserService - Following methods are deprecated:
@Deprecated setUserProfile()     // Use: user.setLastname() + save()
@Deprecated setUserRole()        // Use: user.setUserRole() + save()
@Deprecated setCompanyAssociation() // Use: user.setCompany() + save()

// CMeetingService - Following methods are deprecated:
@Deprecated setParticipants()    // Use: meeting.addParticipant() + save()
@Deprecated setAttendees()       // Use: meeting.addAttendee() + save()
@Deprecated setMeetingDetails()  // Use: meeting.setMeetingType() + save()
```

### 9.12.3. Recommended Migration Pattern

**Old Pattern (Deprecated):**
```java
// Sample data initialization - OLD WAY
activityService.setActivityType(activity, type, "description");
activityService.setAssignedUsers(activity, user1, user2);
activityService.setTimeTracking(activity, hours1, hours2, hours3);
activityService.save(activity);
```

**New Pattern (Preferred):**
```java
// Sample data initialization - NEW WAY  
activity.setActivityType(type);
activity.setDescription("description");
activity.setAssignedTo(user1);
activity.setCreatedBy(user2);
activity.setEstimatedHours(hours1);
activity.setActualHours(hours2);
activity.setRemainingHours(hours3);
activityService.save(activity); // Single save operation
```

### 9.12.4. When Service Methods Are Justified

**✅ Business Logic Examples:**
```java
// Complex validation and business rules
@Transactional
public CActivity startActivity(final CActivity activity) {
    validateActivityCanBeStarted(activity);
    activity.setStatus(findStatusByName("IN_PROGRESS"));
    activity.setStartDate(LocalDate.now());
    updateRelatedActivities(activity);
    return save(activity);
}

// Multi-entity operations
@Transactional  
public CProject completeProject(final CProject project) {
    validateAllActivitiesCompleted(project);
    updateProjectStatus(project);
    notifyStakeholders(project);
    generateCompletionReport(project);
    return save(project);
}

// Domain-specific calculations
public BigDecimal calculateProjectProgress(final CProject project) {
    final List<CActivity> activities = findEntriesByProject(project);
    return activities.stream()
        .map(CActivity::getProgressPercentage)
        .collect(averagingInt(Integer::intValue))
        .toBigDecimal();
}
```

---

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

# Java Strict Coding Rules for Derbent Project

## 1. Class Structure and Design Principles

### 1.1 Entity Classes
- All entity classes must extend appropriate base classes (CEntityDB, CEntityNamed, CEntityOfProject, CTypeEntity)
- Type entities MUST extend CTypeEntity for consistency
- Fields must be private with proper getters/setters
- Use appropriate JPA annotations (@Entity, @Table, @AttributeOverride)
- Include comprehensive JavaDoc comments

### 1.2 Field Declarations
- All fields must be private unless specifically required otherwise
- Use final for immutable fields
- Initialize collections at declaration
- Use appropriate validation annotations (@NotNull, @Size, @Column)

### 1.3 Constructor Guidelines
- Provide default constructor for JPA
- Provide parameterized constructors for required fields
- Use constructor chaining with super() calls
- Include parameter validation

## 2. Code Quality Standards

### 2.1 Logging
- Use SLF4J Logger with correct class reference
- Logger must be static final and named LOGGER
- Use appropriate log levels (DEBUG, INFO, WARN, ERROR)

### 2.2 Method Implementation
- All public methods must have JavaDoc
- Use defensive programming (null checks, validation)
- Follow single responsibility principle
- Implement proper equals(), hashCode(), and toString() methods

### 2.3 Exception Handling
- Use specific exception types
- Include meaningful error messages
- Log exceptions appropriately
- Don't catch and ignore exceptions

## 3. JPA and Database Standards

### 3.1 Entity Annotations
- Use @Entity for all entity classes
- Specify @Table name explicitly
- Use @AttributeOverride for inherited ID fields
- Apply proper fetch strategies (LAZY vs EAGER)

### 3.2 Relationship Mapping
- Use appropriate relationship annotations (@ManyToOne, @OneToMany, etc.)
- Specify join columns explicitly
- Consider cascade operations carefully
- Use fetch = FetchType.LAZY for performance

## 4. Metadata and Validation

### 4.1 MetaData Annotations
- Use @MetaData for UI generation
- Specify display names, descriptions, and order
- Set appropriate maxLength values
- Define data provider beans for relationships

### 4.2 Bean Validation
- Use @NotNull, @Size, @Pattern as appropriate
- Validate at entity level, not just UI level
- Include custom validators when needed

## 5. Testing Standards

### 5.3 Unit Tests  
- Test all public methods
- Include edge cases and error conditions
- Use meaningful test names
- Follow AAA pattern (Arrange, Act, Assert)
- Write unit tests for all business logic and service methods
- Test all views, select all new items in grids, always test CRUD functions of views in all pages
- Use TestContainers for integration testing with PostgreSQL
- Maintain test coverage above 80% for critical business logic
- Test all validation scenarios and edge cases
- Mock external dependencies appropriately
- Include manual verification tests for complex UI interactions
- Always create tests for UI functions
- Test against grid selection changes for every page every view
- Test contents of every combobox

### 5.4 Integration Tests
- Test entity persistence and retrieval
- Verify relationship mappings
- Test validation constraints
- Include performance tests for critical paths

### 5.3 Test Structure and Guidelines

**Test Organization Structure:**
Tests must be organized in module-specific directories following this structure:
```
src/test/java/tech/derbent/
├── abstracts/tests/           # Generic test superclasses and utilities
│   ├── CGenericViewTest.java  # Generic view test patterns
│   └── CTestUtils.java        # Common utility functions
├── activities/tests/          # Activity-related tests
├── users/tests/               # User-related tests  
├── meetings/tests/            # Meeting-related tests
├── projects/tests/            # Project-related tests
└── ui/automation/             # Base UI test infrastructure
```

**Test Super Classes and Inheritance:**
All UI tests must extend appropriate super classes:

```java
// Generic test superclass - provides common patterns
public abstract class CGenericViewTest<T> extends CBaseUITest {
    protected abstract Class<?> getViewClass();
    protected abstract Class<T> getEntityClass();
    
    @Test
    public void testViewNavigation() { /* Common navigation testing */ }
    @Test
    public void testNewItemCreation() { /* Common creation testing */ }
    @Test
    public void testGridInteractions() { /* Common grid testing */ }
    @Test
    public void testViewComboBoxes() { /* Common combobox testing */ }
}

// Concrete implementation - minimal code required
public class CActivitiesViewGenericTest extends CGenericViewTest<CActivity> {
    @Override
    protected Class<?> getViewClass() { return CActivitiesView.class; }
    
    @Override
    protected Class<CActivity> getEntityClass() { return CActivity.class; }
}
```

**Common Test Utility Functions:**
All tests must use these standardized utility functions:
```java
// From CTestUtils and CBaseUITest
clickCancel();        // Standard cancel button interaction
clickNew();           // Standard new/add button interaction  
clickSave();          // Standard save/create button interaction
clickGrid(int index); // Standard grid row selection
takeScreenshot(String name, boolean isFailure); // Conditional screenshots
```

### 5.4 UI Automated Tests
- Don't call applicationLogin in every @test, just use it in the setup
- Don't wait after every navigation etc, if there is a wait in the previous call it is enough
- Try to navigate between views using class annotations via `getRouteFromClass(Class<?> viewClass)`
- Always fail all tests with fail assertion
- Always generate for all views and functions a playwright tests
- Create auxiliary functions for playwright tester for simpler commands
- Always try to use selection by ID not by CSS or tag
- Try to insert ID to used components in test in java
- Keep tests in non headless chromium execution
- Test entity persistence and retrieval
- Verify relationship mappings
- Test validation constraints
- Include performance tests for critical paths
- Write separate test classes for each view to keep code easy to understand
- Write short if blocks, quick returns to increase maintenance
- Don't have repeating blocks - extract to super classes
- **Only take screenshot if there is a fail in tests** - use `takeScreenshot(name, true)` for failures only
- **Reduce the number of log messages** - use DEBUG level for routine operations
- Put all test classes in tests folder of that class group
- Create or use common functions like `clickCancel()`, `clickNew()`, `clickGrid()`
- **Generate super class tests with classname, entity class parameters** to run common test patterns

**Test Coverage Requirements:**
- Test all CRUD operations for every entity view
- Test grid selection changes for every page/view  
- Test contents of every combobox and data provider
- Test against all validation scenarios
- Include manual verification tests for complex UI interactions
- Test all navigation paths between views
- Maintain test coverage above 80% for critical business logic 

## 6. Sample Implementation Guidelines

### 6.1 Type Entity Pattern
All type entities should follow this pattern:
- Extend CTypeEntity
- Provide proper constructors
- Include complete getters/setters
- Add comprehensive JavaDoc
- Implement equals/hashCode based on business key

### 6.2 Service Layer Pattern
- Extend appropriate base service classes
- Implement proper error handling
- Use transactions appropriately
- Include logging and monitoring

## 7. Code Organization

### 7.1 Package Structure
- Follow domain-driven design principles
- Separate concerns (domain, service, controller)
- Use consistent naming conventions
- Keep related classes together

### 7.2 Import Organization
- Group imports logically
- Remove unused imports
- Use specific imports, avoid wildcards
- Follow IDE formatting rules

## 9.10. Color and Icon Usage Guidelines for UI Components

### 9.10.1. Component Color Standards

**Grid Component Colors:**
```java
public class CActivitiesGrid extends CGrid<CActivity> {
    
    // ✅ REQUIRED: Use Lumo design tokens for consistent colors
    private void setupGridStyling() {
        // Status-based row styling
        setClassNameGenerator(activity -> {
            if (activity.getActivityStatus() != null) {
                switch (activity.getActivityStatus().getName()) {
                    case "COMPLETED":
                        return "activity-completed"; // Green background
                    case "IN_PROGRESS": 
                        return "activity-in-progress"; // Blue background
                    case "BLOCKED":
                        return "activity-blocked"; // Red background
                    case "TODO":
                        return "activity-todo"; // Gray background
                    default:
                        return null;
                }
            }
            return null;
        });
    }
}

// CSS styling using Lumo design tokens
.activity-completed {
    background-color: var(--lumo-success-color-10pct);
    color: var(--lumo-success-text-color);
}

.activity-in-progress {
    background-color: var(--lumo-primary-color-10pct);
    color: var(--lumo-primary-text-color);
}

.activity-blocked {
    background-color: var(--lumo-error-color-10pct);
    color: var(--lumo-error-text-color);
}

.activity-todo {
    background-color: var(--lumo-contrast-5pct);
    color: var(--lumo-body-text-color);
}
```

**TextBox and ComboBox Colors:**
```java
public class CProjectFormPanel extends CPanel<CProject> {
    
    // ✅ REQUIRED: Validation-based coloring
    private void setupFieldStyling() {
        nameField.addValueChangeListener(event -> {
            final String value = event.getValue();
            if (value == null || value.trim().isEmpty()) {
                nameField.addThemeVariants(TextFieldVariant.LUMO_ERROR);
                nameField.setErrorMessage("Project name is required");
            } else {
                nameField.removeThemeVariants(TextFieldVariant.LUMO_ERROR);
                nameField.setErrorMessage(null);
            }
        });
        
        // ComboBox styling for required fields
        statusComboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        if (isRequired()) {
            statusComboBox.getStyle().set("--lumo-primary-color", "var(--lumo-primary-color)");
        }
    }
}
```

### 9.10.2. Icon Usage Standards

**Component Icon Guidelines:**
```java
public class CActivityButtons extends CHorizontalLayout {
    
    // ✅ REQUIRED: Consistent icon usage with actions
    private void createActionButtons() {
        // Create button with standard icon
        final CButton newButton = CButton.createPrimary("New Activity", VaadinIcon.PLUS);
        newButton.addClickListener(e -> createNewActivity());
        
        // Edit button with edit icon
        final CButton editButton = CButton.createSecondary("Edit", VaadinIcon.EDIT);
        editButton.addClickListener(e -> editSelectedActivity());
        
        // Delete button with warning coloring
        final CButton deleteButton = CButton.createTertiary("Delete", VaadinIcon.TRASH);
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> deleteSelectedActivity());
        
        // Status action buttons with specific icons
        final CButton startButton = CButton.createSuccess("Start", VaadinIcon.PLAY);
        final CButton pauseButton = CButton.createSecondary("Pause", VaadinIcon.PAUSE);
        final CButton completeButton = CButton.createPrimary("Complete", VaadinIcon.CHECK);
        
        add(newButton, editButton, deleteButton, startButton, pauseButton, completeButton);
    }
}
```

### 9.10.3. ComboBox Styling Guidelines

**Data Provider ComboBox Colors:**
```java
public class CActivityTypeComboBox extends ComboBox<CActivityType> {
    
    public CActivityTypeComboBox() {
        super("Activity Type");
        setupStyling();
        setupDataProvider();
    }
    
    // ✅ REQUIRED: Consistent ComboBox styling
    private void setupStyling() {
        addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        setItemLabelGenerator(CActivityType::getName);
        
        // Add icon to ComboBox items
        setRenderer(new ComponentRenderer<>(activityType -> {
            final CHorizontalLayout layout = new CHorizontalLayout();
            
            // Type-specific icons
            final Icon typeIcon = getActivityTypeIcon(activityType.getName());
            typeIcon.getStyle().set("color", getActivityTypeColor(activityType.getName()));
            
            final Span typeName = new Span(activityType.getName());
            typeName.getStyle().set("margin-left", "var(--lumo-space-s)");
            
            layout.add(typeIcon, typeName);
            layout.setAlignItems(Alignment.CENTER);
            return layout;
        }));
    }
    
    // ✅ REQUIRED: Type-specific icon mapping
    private Icon getActivityTypeIcon(final String typeName) {
        switch (typeName.toUpperCase()) {
            case "DEVELOPMENT": return new Icon(VaadinIcon.CODE);
            case "TESTING": return new Icon(VaadinIcon.BUG);
            case "DOCUMENTATION": return new Icon(VaadinIcon.FILE_TEXT);
            case "MEETING": return new Icon(VaadinIcon.USERS);
            case "RESEARCH": return new Icon(VaadinIcon.SEARCH);
            default: return new Icon(VaadinIcon.DOT_CIRCLE);
        }
    }
}
```

### 9.10.4. Button Color and Icon Standards

**Button Hierarchy and Colors:**
```java
public class CButtonFactory {
    
    // ✅ REQUIRED: Standardized button creation methods
    public static CButton createPrimary(final String text, final VaadinIcon icon) {
        final CButton button = new CButton(text);
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        if (icon != null) {
            button.setIcon(new Icon(icon));
        }
        return button;
    }
    
    public static CButton createSecondary(final String text, final VaadinIcon icon) {
        final CButton button = new CButton(text);
        // Default secondary styling
        if (icon != null) {
            button.setIcon(new Icon(icon));
        }
        return button;
    }
    
    public static CButton createSuccess(final String text, final VaadinIcon icon) {
        final CButton button = new CButton(text);
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        if (icon != null) {
            button.setIcon(new Icon(icon));
        }
        return button;
    }
    
    public static CButton createError(final String text, final VaadinIcon icon) {
        final CButton button = new CButton(text);
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        if (icon != null) {
            button.setIcon(new Icon(icon));
        }
        return button;
    }
}
```

### 9.10.5. CSS Color Variables

**Standard Color Palette:**
```css
/* Custom color extensions for specific use cases */
:root {
    /* Status colors */
    --status-todo-color: var(--lumo-contrast-60pct);
    --status-progress-color: var(--lumo-primary-color);
    --status-review-color: var(--lumo-warning-color);
    --status-completed-color: var(--lumo-success-color);
    --status-blocked-color: var(--lumo-error-color);
    
    /* Priority colors */
    --priority-critical-color: #ff4757;
    --priority-high-color: #ff6b35;
    --priority-medium-color: #ffa726;
    --priority-low-color: #66bb6a;
    --priority-lowest-color: var(--lumo-contrast-40pct);
    
    /* Background variations */
    --status-todo-bg: var(--lumo-contrast-5pct);
    --status-progress-bg: var(--lumo-primary-color-10pct);
    --status-review-bg: var(--lumo-warning-color-10pct);
    --status-completed-bg: var(--lumo-success-color-10pct);
    --status-blocked-bg: var(--lumo-error-color-10pct);
}

/* Component-specific color applications */
.activity-grid vaadin-grid-cell-content {
    border-left: 3px solid transparent;
}

.activity-grid .activity-critical {
    border-left-color: var(--priority-critical-color);
}

.activity-grid .activity-high {
    border-left-color: var(--priority-high-color);
}

.activity-grid .activity-medium {
    border-left-color: var(--priority-medium-color);
}
```

## 9.11. Child Class Simplicity and Super Class Usage Guidelines

### 9.11.1. Keep Child Classes Simple

**Principle**: Child classes should contain only specific business logic while delegating common functionality to super classes through polymorphism, reflection, and proxy patterns.

**Simple Child Class Pattern:**
```java
// ✅ REQUIRED: Minimal child class with specific behavior only
@Entity
@Table(name = "cactivity")
public class CActivity extends CEntityOfProject {
    
    // Only specific fields for this entity
    @MetaData(displayName = "Activity Type", order = 2, 
              dataProviderBean = "CActivityTypeService")
    private CActivityType activityType;
    
    @MetaData(displayName = "Priority", order = 3,
              dataProviderBean = "CActivityPriorityService")
    private CActivityPriority activityPriority;
    
    // Simple constructors delegating to super
    public CActivity() {
        super();
    }
    
    public CActivity(final String name, final CProject project) {
        super(name, project);
    }
    
    // Only entity-specific business logic
    public Boolean isHighPriority() {
        return activityPriority != null && 
               "HIGH".equals(activityPriority.getName());
    }
    
    // Delegate complex operations to service layer
    public Boolean canBeStarted() {
        // Delegate to service for complex business rules
        return CActivityService.getInstance().canActivityBeStarted(this);
    }
}
```

### 9.11.2. Generic Super Class Functions with Polymorphism

**Super Class with Generic Operations:**
```java
public abstract class CEntityOfProjectService<T extends CEntityOfProject> 
    extends CEntityService<T> {
    
    // ✅ REQUIRED: Generic method using reflection and polymorphism
    public List<T> findByProjectIdWithDynamicEagerLoading(final Long projectId) {
        LOGGER.info("findByProjectIdWithDynamicEagerLoading called for {} with projectId: {}", 
                   getEntityClass().getSimpleName(), projectId);
        
        // Use reflection to determine entity-specific eager loading strategy
        final String entityName = getEntityClass().getSimpleName();
        final String methodName = "findByProjectIdWith" + getEagerLoadingStrategy(entityName);
        
        try {
            // Dynamic method invocation using reflection
            final Method method = getRepository().getClass().getMethod(methodName, Long.class);
            @SuppressWarnings("unchecked")
            final List<T> result = (List<T>) method.invoke(getRepository(), projectId);
            
            // Apply lazy field initialization to all entities
            result.forEach(this::initializeLazyFields);
            
            return result;
        } catch (final ReflectiveOperationException e) {
            LOGGER.warn("Dynamic eager loading failed for {}, falling back to standard query", 
                       entityName, e);
            return findByProjectIdStandard(projectId);
        }
    }
    
    // ✅ REQUIRED: Polymorphic method overridden by child classes
    protected abstract String getEagerLoadingStrategy(String entityName);
    
    // ✅ REQUIRED: Generic validation using reflection
    protected void validateEntityUsingReflection(final T entity) {
        final Field[] fields = entity.getClass().getDeclaredFields();
        
        for (final Field field : fields) {
            if (field.isAnnotationPresent(MetaData.class)) {
                final MetaData metadata = field.getAnnotation(MetaData.class);
                if (metadata.required()) {
                    validateRequiredField(entity, field, metadata.displayName());
                }
            }
        }
    }
    
    // ✅ REQUIRED: Generic method using class proxy pattern
    public T createEntityProxy(final Class<T> entityClass) {
        return (T) Proxy.newProxyInstance(
            entityClass.getClassLoader(),
            new Class<?>[]{entityClass},
            new CEntityInvocationHandler<>(entityClass)
        );
    }
}
```

### 9.11.3. Service Layer Simplicity

**Simple Child Service Implementation:**
```java
@Service
public class CActivityService extends CEntityOfProjectService<CActivity> {
    
    // ✅ REQUIRED: Override only to provide entity-specific behavior
    @Override
    protected String getEagerLoadingStrategy(final String entityName) {
        // Specific eager loading strategy for activities
        return "ActivityTypeAndPriority";
    }
    
    // ✅ REQUIRED: Delegate complex operations to super class
    @Override
    public CActivity save(final CActivity activity) {
        LOGGER.info("save called with activity: {}", activity);
        
        // Use super class generic validation
        validateEntityUsingReflection(activity);
        
        // Entity-specific validation only
        validateActivitySpecificRules(activity);
        
        // Delegate to super class
        return super.save(activity);
    }
    
    // Only activity-specific business logic
    private void validateActivitySpecificRules(final CActivity activity) {
        if (activity.getActivityPriority() == null) {
            throw new ServiceException("Activity must have a priority assigned");
        }
    }
    
    // ✅ REQUIRED: Use super class functionality with entity-specific parameters
    public List<CActivity> findActivitiesByProject(final Long projectId) {
        // Delegate to super class generic method
        return findByProjectIdWithDynamicEagerLoading(projectId);
    }
}
```

### 9.11.4. UI Layer Child Class Simplicity

**Simple Child View Implementation:**
```java
@Route("activities")
@PageTitle("Activities")
public class CActivitiesView extends CProjectAwareMDPage<CActivity> {
    
    // ✅ REQUIRED: Minimal setup, delegate to super class
    @Override
    protected void setupView() {
        super.setupView(); // Handles common UI setup
        
        // Only activity-specific customizations
        setupActivitySpecificButtons();
        setupActivitySpecificFilters();
    }
    
    // ✅ REQUIRED: Override only to provide entity-specific behavior
    @Override
    protected CActivity createNewEntity() {
        final CActivity activity = super.createNewEntity();
        
        // Activity-specific initialization only
        activity.setActivityPriority(getDefaultPriority());
        
        return activity;
    }
    
    // ✅ REQUIRED: Use super class generic functionality
    @Override
    protected void refreshGrid() {
        // Delegate complex refresh logic to super class
        super.refreshGrid();
        
        // Apply activity-specific styling only
        applyActivityGridStyling();
    }
    
    // Minimal entity-specific logic
    private void setupActivitySpecificButtons() {
        final CButton startButton = CButton.createSuccess("Start Selected", VaadinIcon.PLAY);
        startButton.addClickListener(e -> startSelectedActivity());
        addCustomButton(startButton);
    }
    
    private void startSelectedActivity() {
        final CActivity selected = getSelectedEntity();
        if (selected != null) {
            // Delegate business logic to service
            service.startActivity(selected);
            refreshGrid(); // Use super class method
        }
    }
}
```

### 9.11.5. Reflection and Invoke Functions Best Practices

**Generic Function Invocation:**
```java
public class CGenericEntityOperations {
    
    // ✅ REQUIRED: Generic method invocation using reflection
    public static <T> Object invokeEntityMethod(final T entity, final String methodName, 
                                               final Object... parameters) {
        try {
            final Class<?>[] parameterTypes = Arrays.stream(parameters)
                .map(Object::getClass)
                .toArray(Class<?>[]::new);
            
            final Method method = entity.getClass().getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            
            return method.invoke(entity, parameters);
        } catch (final ReflectiveOperationException e) {
            LOGGER.error("Failed to invoke method {} on entity {}", methodName, 
                        entity.getClass().getSimpleName(), e);
            throw new RuntimeException("Method invocation failed", e);
        }
    }
    
    // ✅ REQUIRED: Generic field access using reflection
    public static <T> void setEntityFieldValue(final T entity, final String fieldName, 
                                             final Object value) {
        try {
            final Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(entity, value);
        } catch (final ReflectiveOperationException e) {
            LOGGER.error("Failed to set field {} on entity {}", fieldName, 
                        entity.getClass().getSimpleName(), e);
            throw new RuntimeException("Field access failed", e);
        }
    }
}
```

### 9.11.6. Class Proxy Pattern Implementation

**Entity Proxy for Lazy Loading:**
```java
public class CEntityInvocationHandler<T extends CEntityBase> implements InvocationHandler {
    
    private final T targetEntity;
    private final CEntityService<T> entityService;
    
    public CEntityInvocationHandler(final T targetEntity, final CEntityService<T> service) {
        this.targetEntity = targetEntity;
        this.entityService = service;
    }
    
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) 
            throws Throwable {
        
        // ✅ REQUIRED: Automatic lazy loading for getter methods
        if (method.getName().startsWith("get") && isLazyField(method)) {
            // Ensure entity is fully loaded before accessing lazy fields
            final T fullEntity = entityService.get(targetEntity.getId()).orElse(targetEntity);
            return method.invoke(fullEntity, args);
        }
        
        // Delegate to original entity for other methods
        return method.invoke(targetEntity, args);
    }
    
    private boolean isLazyField(final Method method) {
        try {
            final String fieldName = getFieldNameFromGetter(method.getName());
            final Field field = targetEntity.getClass().getDeclaredField(fieldName);
            
            return field.isAnnotationPresent(ManyToOne.class) && 
                   field.getAnnotation(ManyToOne.class).fetch() == FetchType.LAZY;
        } catch (final NoSuchFieldException e) {
            return false;
        }
    }
}
```

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