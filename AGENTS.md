# AGENTS Master Playbook

**Version**: 2.1  
**Date**: 2026-01-23  
**Status**: MANDATORY - All AI agents and developers MUST follow these rules  
**Self-Improving**: This document should be updated as new patterns emerge

---

## Table of Contents

1. [Orientation & Architecture](#1-orientation--architecture)
2. [Core Commands](#2-core-commands)
3. [Coding Standards (MANDATORY)](#3-coding-standards-mandatory)
4. [Entity Management Patterns](#4-entity-management-patterns)
5. [Service Layer Patterns](#5-service-layer-patterns)
6. [View & UI Patterns](#6-view--ui-patterns)
7. [Testing Standards](#7-testing-standards)
8. [Security & Multi-Tenant](#8-security--multi-tenant)
9. [Workflow & CI/CD](#9-workflow--cicd)
10. [Agent Execution Guidelines](#10-agent-execution-guidelines)
11. [Pattern Enforcement Rules](#11-pattern-enforcement-rules)
12. [Self-Improvement Process](#12-self-improvement-process)

---

## 1. Orientation & Architecture

### 1.1 Technology Stack

**Platform**: Spring Boot 3.x + Vaadin 24.x + Maven  
**Database**: PostgreSQL (production), H2 (testing)  
**Framework Code**: `src/main/java/tech/derbent/api/`  
**Feature Modules**: `src/main/java/tech/derbent/*` (domain → service → view layering)

### 1.2 Project Structure

```
src/main/java/tech/derbent/
├── api/                          # Shared framework (base classes)
│   ├── domains/                  # Base entity classes
│   ├── services/                 # Base service classes
│   ├── views/                    # Base view classes
│   └── utils/                    # Utility classes
├── {feature}/                    # Feature modules
│   ├── domain/                   # Entity classes
│   ├── service/                  # Service layer
│   └── view/                     # UI layer
src/main/frontend/                # Vaadin frontend assets
src/main/resources/               # Configuration, seeds
src/test/java/                    # Unit & integration tests
docs/                             # Project documentation
├── architecture/                 # Design patterns & standards
├── development/                  # Development guides
├── implementation/               # Implementation details
└── testing/                      # Testing guidelines
```

### 1.3 Core Architecture Principles

**DO**:
- ✅ Follow existing Derbent package structure
- ✅ Use existing base classes (CEntityDB, CAbstractService, CAbstractPage)
- ✅ Reuse components (CGrid, CPageService, CEntityFormBuilder)
- ✅ Keep UI logic in Vaadin components
- ✅ Keep business logic in services

**DO NOT**:
- ❌ Introduce new frameworks or libraries
- ❌ Change public APIs unless explicitly requested
- ❌ Modify unrelated files
- ❌ Remove existing functionality
- ❌ Bypass existing abstractions
- ❌ Create initializers/views for relation classes (e.g., CWorkflowStatusRelation)

### 1.4 Documentation Hierarchy (AI Agent Rule)

When generating code, consult documentation in this order:

1. **This file (AGENTS.md)** - Master playbook
2. **Specific pattern documents** - For targeted guidance
3. **Code examples** - Existing implementations
4. **Ask for clarification** - If uncertain

---

## 2. Core Commands

### 2.1 Development Commands

```bash
# Start application (H2 profile)
./mvnw spring-boot:run -Dspring.profiles.active=h2

# Start application (PostgreSQL profile)  
./mvnw spring-boot:run -Dspring.profiles.active=postgres

# Build + test + format
./mvnw clean verify

# Format code only
./mvnw spotless:apply

# Reset sample data
mvn spring-boot:run \
  -Dspring-boot.run.main-class=tech.derbent.api.dbResetApplication \
  -Dspring-boot.run.profiles=reset-db
```

### 2.2 Testing Commands

```bash
# Run Playwright tests (visible browser)
./run-playwright-tests.sh [menu|login|comprehensive|all]

# Run with specific profile
PLAYWRIGHT_HEADLESS=false ./run-playwright-tests.sh menu

# Run unit tests
./mvnw test -Dspring.profiles.active=test

# Run specific test
./mvnw test -Dtest=CActivityServiceTest
```

---

## 3. Coding Standards (MANDATORY)

### 3.1 C-Prefix Convention (Non-Negotiable)

**RULE**: All custom classes MUST start with "C"

#### ✅ CORRECT
```java
public class CActivity extends CProjectItem<CActivity> { }
public class CActivityService extends CEntityOfProjectService<CActivity> { }
public class CActivityView extends CAbstractPage { }
public class CButton extends Button { }
```

#### ❌ INCORRECT
```java
public class Activity { }              // Missing C-prefix
public class ActivityService { }       // Missing C-prefix
```

**Exceptions**:
- Interfaces: `I*` (e.g., `IActivityRepository`, `ISessionService`)
- Tests: `C*Test` (e.g., `CActivityTest`, `CActivityServiceTest`)
- Packages: lowercase without prefix

**Benefits**:
- Instant recognition of custom vs. framework classes
- Enhanced IDE navigation
- AI-assisted development optimization
- Prevents naming conflicts

### 3.2 Naming Conventions

#### Classes

| Type | Pattern | Example |
|------|---------|---------|
| Entity | `C{Entity}` | `CActivity`, `CUser`, `CProject` |
| Service | `C{Entity}Service` | `CActivityService`, `CUserService` |
| Repository | `I{Entity}Repository` | `IActivityRepository`, `IUserRepository` |
| View/Page | `C{Entity}View` or `C{Entity}Page` | `CActivityView`, `CUserPage` |
| Component | `C{Component}` | `CButton`, `CGrid`, `CDialog` |
| Utility | `C{Purpose}` | `CAuxillaries`, `CPageableUtils` |
| Interface | `I{Name}` | `ISearchable`, `IKanbanEntity` |
| Test | `C{Class}Test` | `CActivityTest`, `CActivityServiceTest` |

**Validation module**: Use "Validation" for business entities, reserve "Test" for automated tests

#### Fields & Variables

```java
// Private fields - camelCase
private String activityName;
private LocalDate plannedStartDate;
private CUser assignedTo;

// Constants - UPPER_SNAKE_CASE
public static final String DEFAULT_COLOR = "#DC143C";
public static final int MAX_LENGTH_NAME = 255;
private static final Logger LOGGER = LoggerFactory.getLogger(CActivity.class);

// Boolean fields - "is" prefix
private Boolean isActive;
private Boolean isDeletable;
private Boolean isCompleted;

// UI Component fields - typeName convention
private CButton buttonAdd;           // {type}{Name}
private CButton buttonDelete;
private CDialog dialogConfirmation;
private CVerticalLayout layoutMain;
private CGrid<CEntity> gridItems;
private ComboBox<String> comboBoxStatus;
```

#### Methods

```java
// Getters/Setters - standard Java bean
public String getName() { return name; }
public void setName(String name) { this.name = name; }

// Boolean getters
public Boolean getIsActive() { return isActive; }
public boolean isActive() { return isActive != null && isActive; }

// Event handlers - on_{component}_{event}
protected void on_buttonAdd_clicked() { }
protected void on_buttonDelete_clicked() { }
protected void on_comboBoxStatus_selected(String status) { }

// Factory methods - create_{component}
protected CButton create_buttonAdd() { }
protected CDialog create_dialogConfirmation() { }

// Business logic - descriptive verbs
public void completeActivity() { }
public boolean canDelete() { }
public void assignToUser(CUser user) { }
```

### 3.3 Type Safety (MANDATORY)

**RULE**: Always use generic type parameters

#### ✅ CORRECT
```java
public class CActivity extends CProjectItem<CActivity> {
    // Type-safe
}

List<CActivity> activities = service.findAll();
```

#### ❌ INCORRECT
```java
public class CActivity extends CProjectItem {  // Raw type!
    // Loses type safety
}

List activities = service.findAll();  // Raw type!
```

### 3.4 Metadata-Driven Development

**RULE**: Use `@AMetaData` for automatic UI generation

```java
@Column(nullable = false, length = 255)
@Size(max = 255)
@NotBlank(message = "Name is required")
@AMetaData(
    displayName = "Activity Name",    // UI label
    required = true,                   // Required indicator
    readOnly = false,                  // Editable
    description = "Activity name",     // Tooltip
    hidden = false,                    // Visible
    order = 10,                        // Display order
    maxLength = 255,                   // Max input length
    dataProviderBean = "CUserService"  // ComboBox data source
)
private String name;
```

**CRITICAL**: Field names must be exact - UI metadata helpers are reflection-based

### 3.5 Code Formatting (MANDATORY)

#### Import Organization
**RULE**: ALWAYS use import statements, NEVER fully-qualified names

#### ✅ CORRECT
```java
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.api.projects.domain.CProject;
import java.util.List;
import java.time.LocalDate;

public class CActivityService {
    public CActivity createActivity(String name, CProject project) {
        CActivity activity = new CActivity(name, project);
        return save(activity);
    }
}
```

#### ❌ INCORRECT
```java
public class CActivityService {
    public tech.derbent.plm.activities.domain.CActivity createActivity(
            String name, tech.derbent.api.projects.domain.CProject project) {
        // WRONG: Fully-qualified names clutter code
    }
}
```

#### Spotless Configuration
```bash
# Apply formatting (MANDATORY before commit)
mvn spotless:apply

# Check formatting
mvn spotless:check
```

**Key Rules**:
- Indentation: 4 spaces (no tabs)
- Line length: 140 characters (soft limit)
- Braces: Always use, even for single-line blocks
- Final keyword: Use for method parameters and local variables

### 3.6 Entity Constants (MANDATORY)

**RULE**: Every entity class MUST define these constants:

| Constant | Purpose | Example |
|----------|---------|---------|
| `DEFAULT_COLOR` | UI display color | `"#DC143C"` |
| `DEFAULT_ICON` | Vaadin icon ID | `"vaadin:tasks"` |
| `ENTITY_TITLE_SINGULAR` | Human-readable singular | `"Activity"` |
| `ENTITY_TITLE_PLURAL` | Human-readable plural | `"Activities"` |
| `VIEW_NAME` | View/page title | `"Activities View"` |

#### ✅ CORRECT
```java
public class CActivity extends CProjectItem<CActivity> {
    public static final String DEFAULT_COLOR = "#DC143C";
    public static final String DEFAULT_ICON = "vaadin:tasks";
    public static final String ENTITY_TITLE_PLURAL = "Activities";
    public static final String ENTITY_TITLE_SINGULAR = "Activity";
    public static final String VIEW_NAME = "Activities View";
    // ...
}
```

#### ❌ INCORRECT
```java
public class CActivity extends CProjectItem<CActivity> {
    public static final String DEFAULT_COLOR = "#DC143C";
    // Missing: DEFAULT_ICON, ENTITY_TITLE_SINGULAR, ENTITY_TITLE_PLURAL, VIEW_NAME
}
```

### 3.7 Name Field Validation Pattern (MANDATORY)

**RULE**: Base class `CEntityNamed` allows null/empty names for flexibility (e.g., type entities, intermediate classes). Concrete business entities (CActivity, CIssue, CMeeting, etc.) MUST enforce non-empty name validation in their service's `validateEntity()` method.

**Base Class (CEntityNamed)**: Allows null/empty names
```java
@Column (nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
@Size (max = CEntityConstants.MAX_LENGTH_NAME)
@AMetaData (displayName = "Name", required = false, ...)
private String name;
```

**Concrete Entity Service**: Enforces non-empty name
```java
@Override
protected void validateEntity(final CActivity entity) {
    super.validateEntity(entity);
    
    // Name validation - MANDATORY for business entities
    Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
    
    if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
        throw new IllegalArgumentException(
            ValidationMessages.formatMaxLength(
                ValidationMessages.NAME_MAX_LENGTH, 
                CEntityConstants.MAX_LENGTH_NAME));
    }
    
    // Other validations...
}
```

**Entities that MUST validate name**:
- ✅ Business entities: CActivity, CIssue, CMeeting, CTask, CRisk, etc.
- ✅ Project items: CProjectItem and all subclasses
- ✅ Company entities: CProvider, CCustomer, CProduct, etc.
- ❌ Type entities: Can have empty names (optional)
- ❌ Intermediate/abstract classes: Validation in concrete classes only

### 3.8 Validation Pattern (MANDATORY)

**Service validation** in `validateEntity()`:
```java
@Override
protected void validateEntity(final CActivity entity) throws CValidationException {
    super.validateEntity(entity);
    
    // 1. Required Fields (including name for business entities)
    Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
    
    if (entity.getStatus() == null) {
        throw new CValidationException(ValidationMessages.VALUE_REQUIRED);
    }
    
    // 2. Length Checks
    if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
        throw new CValidationException(ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
    }
    
    // 3. Unique Checks (Mirror DB Constraints)
    final Optional<CActivity> existing = repository.findByNameAndProject(entity.getName(), entity.getProject());
    if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
        throw new CValidationException(String.format(ValidationMessages.DUPLICATE_NAME, entity.getName()));
    }
}
```

**Key Validation Rules**:
1. **Always override** `validateEntity(T entity)` in your service.
2. **Always call** `super.validateEntity(entity)` first.
3. **Validate name** for business entities using `Check.notBlank()`.
4. **Use `CValidationException`** for ALL validation errors (avoid IllegalArgumentException).
5. **Use `ValidationMessages`** constants for consistent error messages.
6. **Mirror DB constraints**: If DB has a unique constraint, you MUST check it in `validateEntity` before saving.
7. **Unified handling**: Catch `CValidationException` in UI and show via `CNotificationService.showValidationException(e)`.

### 3.14 Fail-Fast Pattern (MANDATORY)

**RULE**: All database constraints (Unique, Not Null, Length, Foreign Key) MUST be mirrored in `validateEntity()` to catch errors before the database does.

**Why?** To provide user-friendly, specific error messages instead of generic "Database Error" or cryptic SQL exceptions.

**Checklist for `validateEntity()`:**
1.  **Not Null Checks**: Explicitly check required fields.
    *   Use `Check.notNull(value, ValidationMessages.FIELD_REQUIRED)` or similar.
2.  **String Length Checks**: Check max length for strings.
    *   `if (str.length() > MAX) throw ...` using `ValidationMessages.FIELD_MAX_LENGTH`.
3.  **Unique Constraint Checks**: Query repository to check for duplicates.
    *   *Must* handle update scenario (exclude current entity ID from check).
    *   Use `ValidationMessages.DUPLICATE_*`.
4.  **Business Logic**: Any other domain-specific rules.

**Example (CUser):**
```java
@Override
protected void validateEntity(final CUser entity) {
    super.validateEntity(entity);

    // 1. Required Fields
    Check.notBlank(entity.getLogin(), ValidationMessages.FIELD_REQUIRED);
    
    // 2. Length Checks
    if (entity.getLogin().length() > CEntityConstants.MAX_LENGTH_NAME) {
        throw new CValidationException(ValidationMessages.formatMaxLength(ValidationMessages.FIELD_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
    }

    // 3. Unique Checks (Database Mirror)
    // Check Login Unique in Company
    Optional<CUser> existingLogin = repository.findByLoginAndCompany(entity.getLogin(), entity.getCompany());
    if (existingLogin.isPresent() && !existingLogin.get().getId().equals(entity.getId())) {
        throw new CValidationException(ValidationMessages.DUPLICATE_USERNAME);
    }
    
    // Check Email Unique (if set)
    if (entity.getEmail() != null && !entity.getEmail().isBlank()) {
         Optional<CUser> existingEmail = repository.findByEmailAndCompany(entity.getEmail(), entity.getCompany());
         if (existingEmail.isPresent() && !existingEmail.get().getId().equals(entity.getId())) {
             throw new CValidationException(ValidationMessages.DUPLICATE_EMAIL);
         }
    }
}
```

### 3.14 Fail-Fast Pattern (MANDATORY)

**RULE**: Avoid silent guards; use explicit validation

#### ✅ CORRECT
```java
public void processActivity(CActivity activity) {
    Objects.requireNonNull(activity, "Activity cannot be null");
    Check.notBlank(activity.getName(), "Activity name required");
    Check.instanceOf(activity, CProjectItem.class, "Must be project item");
    
    // Process...
}
```

#### ❌ INCORRECT
```java
public void processActivity(CActivity activity) {
    if (activity == null) return;  // Silent failure!
    if (activity.getName() == null) return;  // Silent failure!
    
    // Process...
}
```

### 3.9 Exception Handling (MANDATORY)

**RULE**: Let exceptions bubble up; only UI layer shows to user

#### ✅ CORRECT - Service Layer
```java
@Transactional
public void completeActivity(CActivity activity) {
    Objects.requireNonNull(activity, "Activity cannot be null");
    
    // Let exceptions propagate
    activity.setStatus(getCompletedStatus());
    repository.save(activity);
}
```

#### ✅ CORRECT - UI Layer
```java
private void on_complete_clicked() {
    try {
        service.completeActivity(selectedActivity);
        CNotificationService.showSuccess("Activity completed");
    } catch (Exception e) {
        LOGGER.error("Error completing activity: {}", e.getMessage(), e);
        CNotificationService.showException("Failed to complete activity", e);
    }
}
```

#### ❌ INCORRECT - Service Layer Shows UI
```java
@Transactional
public void completeActivity(CActivity activity) {
    try {
        repository.save(activity);
    } catch (Exception e) {
        CNotificationService.showError("Error");  // WRONG LAYER!
    }
}
```

### 3.14 Logging Standards

**Console output format** (enforced in `application*.properties`):
```properties
spring.output.ansi.enabled=ALWAYS
logging.pattern.console=%clr(%d{${LOG_DATEFORMAT_PATTERN:HH:mm:ss.S}}){magenta} %clr(${LOG_LEVEL_PATTERN:%-5.5p}) \(%clr(%file:%line){cyan}\) %clr(%msg){red} %clr(%-40.40logger{39}){cyan}%n
```

**Log levels**:
```java
LOGGER.trace("Entering method: {}", params);     // Detailed debug
LOGGER.debug("Processing entity: {}", id);       // Debug info
LOGGER.info("Activity {} created by {}", name, user);  // Important events
LOGGER.warn("Activity {} is overdue", id);       // Warnings
LOGGER.error("Failed to save: {}", e.getMessage(), e);  // Errors (with stack trace)
```

**RULE**: Use parameterized logging (not concatenation)

#### ✅ CORRECT
```java
LOGGER.info("User {} created activity {}", userId, activityId);
```

#### ❌ INCORRECT
```java
LOGGER.info("User " + userId + " created activity " + activityId);
```

### 3.14 Initializer + Sample Wiring (MANDATORY)

- When introducing a new entity initializer service, wire it into `CDataInitializer` in the same change.
- Call `initialize(...)` inside the project loop so grids/pages are created, and call the matching `initializeSample(...)` in the company/sample sections (and sample-project type block if applicable).
- Do not leave initializers or sample creators unreachable; every new entity must be reachable from data bootstrap paths.

### 3.14 Menu Titles, Orders, and Icons (MANDATORY)

- Use the `Menu_Order_*` constants (e.g., `Menu_Order_PRODUCTS + ".40"`) and corresponding `MenuTitle_*` prefixes to keep navigation ordering consistent; avoid raw strings when a constant exists.
- Ensure every entity defines `DEFAULT_ICON` and `DEFAULT_COLOR` and the initializer `menuTitle`/`pageTitle` clearly matches the entity titles (plural for menus, descriptive for pages).
- Place related entities near each other by order (types before entities before transactions; e.g., storage types `.30`, storages `.40`, items `.50`, transactions `.60`) and keep `showInQuickToolbar` explicit.

### 3.14 PageView Fetch Completeness (MANDATORY)

- `listBy*ForPageView`/`findById` queries must eagerly fetch all UI-critical relationships: project/company, status/workflow/type, parent references, responsible/assigned users, and standard compositions (attachments/comments/links) for the entity and its immediate container (e.g., storage item → storage → type).
- Use `LEFT JOIN FETCH` with `DISTINCT` where collections are fetched to avoid duplicates; include responsible collections (e.g., service department responsibleUsers) to prevent lazy-load errors in grids/forms.
- Apply the same pattern to company-scoped entities (service departments) and transaction views that depend on nested entities.

---

## 4. Entity Management Patterns

### 4.1 Entity Class Structure (MANDATORY)

```java
@Entity
@Table(name = "table_name")
@AttributeOverride(name = "id", column = @Column(name = "entity_id"))
public class CEntity extends CProjectItem<CEntity> {
    
    // 1. Constants (MANDATORY - alphabetically ordered)
    public static final String DEFAULT_COLOR = "#DC143C";
    public static final String DEFAULT_ICON = "vaadin:tasks";
    public static final String ENTITY_TITLE_PLURAL = "Entities";
    public static final String ENTITY_TITLE_SINGULAR = "Entity";
    private static final Logger LOGGER = LoggerFactory.getLogger(CEntity.class);
    public static final String VIEW_NAME = "Entities View";
    
    // 2. Fields - grouped by type
    // Basic fields
    @Column(nullable = false)
    @AMetaData(displayName = "Name", required = true)
    private String name;
    
    // Relationships
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id")
    private CStatus status;
    
    // Collections
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<CChild> children = new ArrayList<>();
    
    // 3. Constructors
    /** Default constructor for JPA. */
    protected CEntity() {
        super();
    }
    
    public CEntity(String name, CProject project) {
        super(CEntity.class, name, project);
    }
    
    // 4. Business logic methods
    public boolean isOverdue() {
        // Implementation
    }
    
    // 5. Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    // 6. copyEntityTo (MANDATORY - see section 4.3)
    @Override
    protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
        super.copyEntityTo(target, options);
        // Copy entity-specific fields
    }
    
    // 7. equals, hashCode, toString (if overriding)
}
```

### 4.2 Entity Hierarchy

```
CEntityDB<T>                      # Base: ID, active, audit
    ↓
CEntityNamed<T>                   # Adds: name, description, dates
    ↓
CEntityOfProject<T>               # Adds: project, createdBy
    ↓
CProjectItem<T>                   # Adds: status, workflow, parent
    ↓
[Domain Entities]                 # CActivity, CMeeting, etc.
```

### 4.3 CopyTo Pattern (MANDATORY)

**RULE**: ALL entities MUST implement `copyEntityTo()`

#### Template
```java
@Override
protected void copyEntityTo(final CEntityDB<?> target, @SuppressWarnings("rawtypes") final CAbstractService serviceTarget, final CCloneOptions options) {
    // STEP 1: ALWAYS call parent first
    super.copyEntityTo(target, serviceTarget, options);
    
    // STEP 2: Type-check target
    if (target instanceof YourEntity) {
        final YourEntity targetEntity = (YourEntity) target;
        
        // STEP 3: Copy basic fields (always)
        copyField(this::getYourField1, targetEntity::setYourField1);
        copyField(this::getYourField2, targetEntity::setYourField2);
        
        // STEP 4: Handle unique fields (make unique!)
        if (this.getEmail() != null) {
            targetEntity.setEmail(this.getEmail().replace("@", "+copy@"));
        }
        
        // STEP 5: Handle dates (conditional)
        if (!options.isResetDates()) {
            copyField(this::getDueDate, targetEntity::setDueDate);
        }
        
        // STEP 6: Handle relations (conditional)
        if (options.includesRelations()) {
            copyField(this::getRelatedEntity, targetEntity::setRelatedEntity);
        }
        
        // STEP 7: Handle collections (conditional)
        if (options.includesRelations()) {
            copyCollection(this::getChildren, targetEntity::setChildren, true);
        }
        
        // STEP 8: DON'T copy sensitive/auto-generated fields
        // Password, tokens, IDs, audit fields handled by base
        
        // STEP 9: Log for debugging
        LOGGER.debug("Copied {} with options: {}", getName(), options);
    }
}
```

#### Field Copy Rules

**✅ ALWAYS Copy**:
- Basic data fields (name, description, notes)
- Numeric fields (amounts, quantities)
- Boolean flags (except security/state)
- Enum values (type, category, priority)

**⚠️ CONDITIONAL Copy** (check options):
- Dates: Only if `!options.isResetDates()`
- Relations: Only if `options.includesRelations()`
- Status: Only if `options.isCloneStatus()`
- Workflow: Only if `options.isCloneWorkflow()`

**❌ NEVER Copy**:
- ID fields (auto-generated)
- Passwords/tokens (security)
- Session data (temporary)
- Audit fields (createdBy, lastModifiedBy)
- Unique constraints (must make unique)

#### Handling Unique Fields
```java
// Make unique to avoid constraint violations
if (this.getEmail() != null) {
    targetEntity.setEmail(this.getEmail().replace("@", "+copy@"));
}
if (this.getLogin() != null) {
    targetEntity.setLogin(this.getLogin() + "_copy");
}
```

### 4.4 Entity Initialization (MANDATORY)

**RULE 1**: ALL entities MUST implement `initializeDefaults()` (overriding `CEntityDB.initializeDefaults()`) and call it in their default constructor.
**RULE 2**: `initializeDefaults()` must initialize all intrinsic fields (Lists, Sets, Boolean=false, numeric=0, default Enums, composition objects).
**RULE 3**: `service.initializeNewEntity()` should ONLY handle context-dependent initialization (Project, User, Workflow, Priority lookup) that requires service injection.

```java
// ✅ CORRECT - Entity Internal Initialization
@Entity
public class CActivity extends CProjectItem<CActivity> {
    
    public CActivity() {
        super();
        initializeDefaults(); // ← MANDATORY call in constructor
    }
    
    public CActivity(String name, CProject project) {
        super(CActivity.class, name, project);
        initializeDefaults(); // ← MANDATORY call in constructor
    }

    @Override
    protected void initializeDefaults() {
        super.initializeDefaults();
        
        // Initialize intrinsic defaults (Direct assignment preferred in constructor)
        estimatedCost = BigDecimal.ZERO;
        attachments = new HashSet<>();
        
        // Initialize composition (OneToOne)
        if (sprintItem == null) {
            sprintItem = new CSprintItem(); // Creates default sprint item
            sprintItem.setParentItem(this);
        }
    }
}

// ✅ CORRECT - Service Contextual Initialization
@Override
public void initializeNewEntity(final CActivity entity) {
    super.initializeNewEntity(entity);
    
    // Initialize context-dependent fields (Project, Status, Priority)
    CProject currentProject = sessionService.getActiveProject().orElseThrow();
    
    entity.initializeDefaults_IHasStatusAndWorkflow(
        currentProject, entityTypeService, projectItemStatusService);
        
    // Lookup default priority from DB (Context-aware)
    List<CPriority> priorities = priorityService.listByCompany(currentProject.getCompany());
    entity.setPriority(priorities.get(0));
}
```

**What `initializeDefaults()` does (Entity-side)**:
- Sets empty collections (Sets, Lists)
- Sets default values (0, false, NONE) - **Direct assignment preferred** (no null checks needed in constructor)
- Creates composition objects (CSprintItem, CAgileParentRelation)
- **NO service calls allowed here**

**What `initializeNewEntity()` does (Service-side)**:
- Sets project/company from session
- Sets createdBy user
- Initializes Workflow/Status (using `initializeDefaults_IHasStatusAndWorkflow`)
- Looks up default entities (Type, Priority, Category) from DB
- **NO intrinsic default setting (redundant)**

**Initialization order**:
1. Create/copy entity
2. Call `initializeNewEntity()`
3. Set custom fields (if needed)
4. Save entity
5. Navigate to entity

### 4.5 Abstract Entity & Service Patterns (CRITICAL)

**RULE**: Abstract entities use `@MappedSuperclass` and have corresponding abstract services. This pattern is essential for type hierarchies and Hibernate compatibility.

#### 4.5.1 Abstract Entity Pattern

**When to use**: When you have multiple concrete entities sharing common fields and behavior.

#### ✅ CORRECT - Abstract Entity
```java
/** 
 * Abstract base class for communication nodes.
 * Following Derbent pattern: @MappedSuperclass for inheritance.
 */
@MappedSuperclass  // ✅ NOT @Entity - abstract entities are @MappedSuperclass
public abstract class CBabNode<EntityClass> extends CEntityOfCompany<EntityClass> {
    
    // Common fields for all node types
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;
    
    @Column(name = "node_type", length = 50)
    private String nodeType;
    
    /** Default constructor for JPA. */
    protected CBabNode() {
        super();
        // Abstract classes do NOT call initializeDefaults() in constructors
    }
    
    protected CBabNode(Class<EntityClass> clazz, String name, String nodeType) {
        super(clazz, name);
        this.nodeType = nodeType;
        // Abstract classes do NOT call initializeDefaults() in constructors
    }
    
    @Override
    protected void initializeDefaults() {
        super.initializeDefaults();
        enabled = true;  // Common defaults for all nodes
    }
    
    // Common getters/setters
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
```

#### ❌ INCORRECT - Abstract Entity
```java
@Entity  // ❌ WRONG - Abstract entities are NOT @Entity
@Table(name = "node")  // ❌ WRONG - No table for abstract
public abstract class CBabNode extends CEntityOfCompany<CBabNode> {  // ❌ WRONG - Raw type
    
    protected CBabNode() {
        super();
        initializeDefaults();  // ❌ WRONG - Abstract constructors don't call this
    }
}
```

#### 4.5.2 Concrete Entity Pattern

#### ✅ CORRECT - Concrete Entity
```java
/** Concrete CAN communication node entity. */
@Entity  // ✅ Concrete entities are @Entity
@Table(name = "cbab_node_can")  // ✅ Concrete entities have @Table
public class CBabNodeCAN extends CBabNode<CBabNodeCAN> {  // ✅ Proper generics
    
    // Entity constants (MANDATORY)
    public static final String DEFAULT_COLOR = "#FF5722";
    public static final String DEFAULT_ICON = "vaadin:car";
    public static final String ENTITY_TITLE_PLURAL = "CAN Nodes";
    public static final String ENTITY_TITLE_SINGULAR = "CAN Node";
    public static final String VIEW_NAME = "CAN Node Configuration";
    
    // CAN-specific fields
    @Column(name = "bitrate")
    private Integer bitrate;
    
    /** Default constructor for JPA. */
    public CBabNodeCAN() {
        super();
        initializeDefaults();  // ✅ MANDATORY - Concrete constructors call this
    }
    
    public CBabNodeCAN(String name, String nodeType) {
        super(CBabNodeCAN.class, name, nodeType);
        initializeDefaults();  // ✅ MANDATORY - All constructors call this
    }
    
    @Override
    protected void initializeDefaults() {
        super.initializeDefaults();
        bitrate = 500000;  // CAN-specific defaults
    }
    
    // MANDATORY - copyEntityTo implementation
    @Override
    protected void copyEntityTo(final CEntityDB<?> target, @SuppressWarnings("rawtypes") final CAbstractService serviceTarget, final CCloneOptions options) {
        super.copyEntityTo(target, serviceTarget, options);
        
        if (target instanceof CBabNodeCAN) {
            final CBabNodeCAN targetNode = (CBabNodeCAN) target;
            copyField(this::getBitrate, targetNode::setBitrate);
        }
    }
}
```

#### 4.5.3 Abstract Service Pattern  

#### ✅ CORRECT - Abstract Service
```java
/** 
 * Abstract service for CBabNode hierarchy.
 * Following Derbent pattern: Abstract service with NO @Service annotation.
 */
@Profile("bab")
@PreAuthorize("isAuthenticated()")
// ✅ NO @Service - Abstract services are NOT Spring beans
// ✅ NO IEntityRegistrable, IEntityWithView - Only concrete services implement these
public abstract class CBabNodeService<NodeType extends CBabNode<NodeType>> extends CEntityOfCompanyService<NodeType> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CBabNodeService.class);
    
    protected CBabNodeService(final IBabNodeRepository<NodeType> repository, final Clock clock, final ISessionService sessionService) {
        super(repository, clock, sessionService);
    }
    
    @Override
    protected void validateEntity(final NodeType entity) {
        super.validateEntity(entity);
        // Common validation for all node types
        Check.notBlank(entity.getNodeType(), "Node Type is required");
    }
    
    // Abstract methods implemented by concrete services
    public abstract List<NodeType> findByDevice(CBabDevice device);
}
```

#### ✅ CORRECT - Concrete Service
```java
/** Concrete service for CBabNodeCAN entities. */
@Service  // ✅ Concrete services are @Service
@Profile("bab")
@PreAuthorize("isAuthenticated()")
public class CBabNodeCANService extends CBabNodeService<CBabNodeCAN> 
        implements IEntityRegistrable, IEntityWithView {  // ✅ Concrete services implement interfaces
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CBabNodeCANService.class);
    
    public CBabNodeCANService(final IBabNodeCANRepository repository, final Clock clock, final ISessionService sessionService) {
        super(repository, clock, sessionService);
    }
    
    @Override
    public Class<CBabNodeCAN> getEntityClass() {  // ✅ PUBLIC - implements interface
        return CBabNodeCAN.class;
    }
    
    @Override
    protected void validateEntity(final CBabNodeCAN entity) {
        super.validateEntity(entity);
        // CAN-specific validation
        if (entity.getBitrate() != null && entity.getBitrate() <= 0) {
            throw new IllegalArgumentException("CAN bitrate must be positive");
        }
    }
    
    @Override
    public List<CBabNodeCAN> findByDevice(CBabDevice device) {
        return ((IBabNodeCANRepository) repository).findByDevice(device);
    }
    
    // Implement interface methods
    @Override public Class<?> getInitializerServiceClass() { return CBabNodeInitializerService.class; }
    @Override public Class<?> getPageServiceClass() { return CPageServiceBabNode.class; }
    @Override public Class<?> getServiceClass() { return this.getClass(); }
}
```

#### 4.5.4 Abstract Repository Pattern

#### ✅ CORRECT - Abstract Repository
```java
/** Abstract repository with @NoRepositoryBean - no HQL queries. */
@Profile("bab")
@NoRepositoryBean  // ✅ MANDATORY - Abstract repositories are not beans
public interface IBabNodeRepository<NodeType extends CBabNode<NodeType>> extends IEntityOfCompanyRepository<NodeType> {
    
    // Abstract method signatures - implemented by concrete repositories
    List<NodeType> findByDevice(CBabDevice device);
    List<NodeType> findEnabledByDevice(CBabDevice device);
    Long countByDevice(CBabDevice device);
}
```

#### ✅ CORRECT - Concrete Repository
```java
/** Concrete repository with HQL queries for CBabNodeCAN. */
@Profile("bab")
public interface IBabNodeCANRepository extends IBabNodeRepository<CBabNodeCAN> {
    
    @Override
    @Query("SELECT e FROM CBabNodeCAN e WHERE e.device = :device ORDER BY e.name ASC")  // ✅ Concrete entity name
    List<CBabNodeCAN> findByDevice(@Param("device") CBabDevice device);
    
    @Override
    @Query("SELECT e FROM CBabNodeCAN e WHERE e.device = :device AND e.enabled = true ORDER BY e.name ASC")
    List<CBabNodeCAN> findEnabledByDevice(@Param("device") CBabDevice device);
    
    @Override
    @Query("SELECT COUNT(e) FROM CBabNodeCAN e WHERE e.device = :device")
    Long countByDevice(@Param("device") CBabDevice device);
}
```

#### ❌ CRITICAL ERROR - Hibernate Issue
```java
@Query("SELECT e FROM CBabNode e WHERE ...")  // ❌ FAILS - @MappedSuperclass entities are NOT queryable!
```

**Why this fails**: Hibernate cannot query `@MappedSuperclass` entities directly. Only concrete `@Entity` classes are queryable.

#### 4.5.5 Abstract Entity Architecture Summary

| Component Type | Abstract (Base) | Concrete (Implementation) |
|---------------|-----------------|---------------------------|
| **Entity** | `@MappedSuperclass` + `protected` constructors | `@Entity` + `@Table` + `public` constructors |
| **Service** | NO `@Service` + NO interfaces | `@Service` + `IEntityRegistrable` + `IEntityWithView` |
| **Repository** | `@NoRepositoryBean` + abstract methods | Concrete HQL queries + `@Override` methods |
| **Constructor** | NO `initializeDefaults()` call | MANDATORY `initializeDefaults()` call |
| **Validation** | Common validation logic | Type-specific validation logic |

#### 4.5.6 When to Use Abstract Entities

**✅ Use Abstract Entities When**:
- Multiple concrete entities share 80%+ common fields
- Common business logic across entity types
- Type hierarchy represents real-world relationships
- Need polymorphic queries via services (not HQL)

**❌ Don't Use Abstract Entities When**:
- Only 1-2 concrete implementations
- Entities are fundamentally different
- No shared business logic
- Simple code duplication is preferable

### 4.6 Lazy Loading Best Practices

**RULE**: Repository queries MUST eagerly fetch lazy collections for UI

```java
// ✅ CORRECT - Eager fetch for UI
@Query("""
    SELECT a FROM CActivity a
    LEFT JOIN FETCH a.status
    LEFT JOIN FETCH a.type
    LEFT JOIN FETCH a.attachments
    LEFT JOIN FETCH a.comments
    LEFT JOIN FETCH a.links
    WHERE a.project = :project
    """)
List<CActivity> listByProjectForPageView(@Param("project") CProject project);
```

**Pattern for entities** with `IHasAttachments`, `IHasComments`, `IHasLinks`:
- Use `LAZY` in entity definition
- Use `LEFT JOIN FETCH` in queries
- Avoid on-demand `Hibernate.initialize()`

### 4.7 Delete via Relations

**RULE**: When child has `orphanRemoval = true`, delete via parent

```java
// ✅ CORRECT
parent.getChildren().remove(child);
parentService.save(parent);

// ❌ INCORRECT
childRepository.delete(child);  // May violate FK constraints
```

### 4.8 Composition Pattern for Child Entities (MANDATORY)

**RULE**: Child entities with @OneToMany or @OneToOne composition MUST follow this pattern

#### Pattern Components

1. **Entity Class**: CComment, CAgileParentRelation, CAttachment, CLink, etc.
2. **Interface**: IHasComments, IHasAgileParentRelation, IHasAttachments, IHasLinks
3. **Initializer Service**: CCommentInitializerService, CAgileParentRelationInitializerService, etc.
4. **Component**: CComponentListComments, CComponentAgileParentSelector, etc.

#### Field Definition Pattern

**For @OneToMany collections (CComment, CAttachment, CLink):**

```java
@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
@JoinColumn(name = "activity_id")  // Parent FK in child table
@AMetaData(
    displayName = "Comments",
    required = false,
    readOnly = false,
    description = "Comments for this activity",
    hidden = false,
    dataProviderBean = "CCommentService",
    createComponentMethod = "createComponent"
)
private Set<CComment> comments = new HashSet<>();
```

**For @OneToOne compositions (CAgileParentRelation):**

```java
@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
@JoinColumn(name = "agile_parent_relation_id", nullable = false)
@NotNull(message = "Agile parent relation is required for agile hierarchy")
@AMetaData(
    displayName = "Agile Parent Relation",
    required = true,
    readOnly = true,
    description = "Agile hierarchy tracking for this activity",
    hidden = true  // Hidden because accessed via interface methods
    // Note: No dataProviderBean/createComponentMethod needed
    // UI for parent selection handled via interface methods and CComponentAgileParentSelector
)
private CAgileParentRelation agileParentRelation;
```

**Key Difference**: OneToOne compositions like CAgileParentRelation:
- Are marked `hidden=true` (not directly displayed)
- Provide interface methods for accessing nested properties (e.g., `getParentActivity()`)
- UI components bind to interface methods, not the composition entity itself
- Future enhancement: Virtual field binding for interface-based properties

#### Interface Pattern

```java
public interface IHasComments {
    Set<CComment> getComments();
    void setComments(Set<CComment> comments);
    
    // Optional: Helper method for copying
    static boolean copyCommentsTo(CEntityDB<?> source, CEntityDB<?> target, CCloneOptions options) {
        // Implementation
    }
}
```

#### Initializer Service Pattern

```java
public final class CCommentInitializerService extends CInitializerServiceBase {
    
    public static final String FIELD_NAME_COMMENTS = "comments";
    public static final String SECTION_NAME_COMMENTS = "Comments";
    
    /**
     * Add standard Comments section to any entity detail view.
     * ALL entity initializers MUST call this method.
     */
    public static void addCommentsSection(final CDetailSection detailSection, 
            final Class<?> entityClass) throws Exception {
        Check.notNull(detailSection, "detailSection cannot be null");
        Check.notNull(entityClass, "entityClass cannot be null");
        
        if (isBabProfile()) {
            return;  // Skip for BAB profile
        }
        
        // Section header - IDENTICAL for all entities
        detailSection.addScreenLine(CDetailLinesService.createSection(SECTION_NAME_COMMENTS));
        
        // Field - renders via component factory
        detailSection.addScreenLine(
            CDetailLinesService.createLineFromDefaults(entityClass, FIELD_NAME_COMMENTS));
    }
    
    private CCommentInitializerService() {
        // Utility class - no instantiation
    }
}
```

#### Service createComponent Method

```java
@Service
public class CCommentService extends CEntityOfCompanyService<CComment> {
    
    /**
     * Create component for managing comments.
     * Called by component factory via @AMetaData createComponentMethod.
     */
    public Component createComponent() {
        try {
            final CComponentListComments component = 
                new CComponentListComments(this, sessionService);
            LOGGER.debug("Created comment component");
            return component;
        } catch (final Exception e) {
            LOGGER.error("Failed to create comment component.", e);
            final Div errorDiv = new Div();
            errorDiv.setText("Error loading comment component: " + e.getMessage());
            errorDiv.addClassName("error-message");
            return errorDiv;
        }
    }
}
```

#### Usage in Entity Initializers

```java
public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
    final CDetailSection scr = createBaseScreenEntity(project, clazz);
    
    // ... other fields ...
    
    // Standard composition sections
    CAttachmentInitializerService.addAttachmentsSection(scr, clazz);
    CLinkInitializerService.addLinksSection(scr, clazz);
    CCommentInitializerService.addCommentsSection(scr, clazz);
    CAgileParentRelationInitializerService.addAgileParentSection(scr, clazz, project);
    
    return scr;
}
```

#### Composition Pattern Summary

| Pattern Element | @OneToMany (Collection) | @OneToOne (Composition) |
|----------------|-------------------------|-------------------------|
| **Entity** | CComment, CAttachment | CAgileParentRelation |
| **Interface** | IHasComments, IHasAttachments | IHasAgileParentRelation |
| **Field Type** | `Set<CComment>` | `CAgileParentRelation` |
| **Cascade** | `ALL, orphanRemoval=true` | `ALL, orphanRemoval=true` |
| **Fetch** | `LAZY` | `EAGER` |
| **Hidden** | `false` | `true` (accessed via interface) |
| **Component** | CComponentListComments | CComponentAgileParentSelector |
| **Initializer** | CCommentInitializerService | CAgileParentRelationInitializerService |

#### Key Rules

1. ✅ **DO** create initializer service with `addXxxSection()` method
2. ✅ **DO** add `createComponent()` to service class
3. ✅ **DO** call initializer in ALL entity initializers that use the feature
4. ✅ **DO** use interface for type-safe access
5. ❌ **DON'T** create standalone views/pages for composition entities
6. ❌ **DON'T** skip initializer - ALL entities must be consistent

---

## 5. Service Layer Patterns

### 5.1 Service Hierarchy

```
IAbstractRepository<T>
    ↓
CAbstractService<T>               # Base CRUD
    ↓
CEntityNamedService<T>            # Name queries, timestamps
    ↓
CEntityOfProjectService<T>        # Project-scoped
    ↓
[Domain Services]                 # CActivityService, etc.
```

### 5.2 Service Class Structure (MANDATORY)

```java
@Service
@PreAuthorize("isAuthenticated()")
public class CEntityService extends CEntityOfProjectService<CEntity> {
    
    // 1. Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(CEntityService.class);
    
    // 2. Dependencies (final, injected via constructor)
    private final CStatusService statusService;
    private final CTypeService typeService;
    
    // 3. Constructor (dependency injection)
    public CEntityService(
        final IEntityRepository repository,
        final Clock clock,
        final ISessionService sessionService,
        final CStatusService statusService,
        final CTypeService typeService) {
        
        super(repository, clock, sessionService);
        this.statusService = statusService;
        this.typeService = typeService;
    }
    
    // 4. Override getEntityClass() (MANDATORY)
    @Override
    protected Class<CEntity> getEntityClass() {
        return CEntity.class;
    }
    
    // 5. Override checkDeleteAllowed()
    @Override
    public String checkDeleteAllowed(final CEntity entity) {
        final String superCheck = super.checkDeleteAllowed(entity);
        if (superCheck != null) return superCheck;
        
        // Add entity-specific deletion checks
        // Return null if delete allowed, error message otherwise
        return null;
    }
    
    // 6. Business logic methods
    @Transactional
    public void completeEntity(CEntity entity) {
        // Implementation
    }
    
    // 7. Query methods
    @Transactional(readOnly = true)
    public List<CEntity> findOverdue() {
        // Implementation
    }
}
```

### 5.3 Multi-User Safety (CRITICAL)

**RULE**: Services are SINGLETON - ONE instance for ALL users

#### ❌ WRONG - Storing User State
```java
@Service
public class CBadService {
    // ❌ WRONG: All users will overwrite this!
    private CUser currentUser;
    private CProject currentProject;
    private Map<Long, List<CActivity>> userCache;  // Shared by all users!
}
```

#### ✅ CORRECT - Stateless Service
```java
@Service
public class CGoodService {
    // ✅ GOOD: Only dependencies (thread-safe)
    private final IRepository repository;
    private final Clock clock;
    private final ISessionService sessionService;
    
    @Transactional(readOnly = true)
    public List<CActivity> getUserActivities() {
        // ✅ GOOD: Get user from session each time
        CUser currentUser = sessionService.getActiveUser()
            .orElseThrow(() -> new IllegalStateException("No active user"));
        
        return repository.findByUserId(currentUser.getId());
    }
}
```

**Golden Rules**:
1. Services are singletons (ONE instance for ALL users)
2. NEVER store user-specific data in service instance fields
3. ALWAYS retrieve user context from `sessionService` per-request
4. Use `VaadinSession` for user-specific temporary state
5. Use database for persistent user data
6. Test with concurrent users

### 5.4 Transaction Management

```java
// Read operations
@Transactional(readOnly = true)
public List<CActivity> findAll() {
    return repository.findAll();
}

// Write operations
@Transactional
public CActivity save(CActivity activity) {
    return repository.save(activity);
}

// Complex operations
@Transactional
public void assignActivities(CUser user, List<CActivity> activities) {
    for (CActivity activity : activities) {
        activity.setAssignedTo(user);
        save(activity);
    }
}
```

### 5.5 Dependency Injection (MANDATORY)

**RULE**: Always use constructor injection, never field injection

#### ✅ CORRECT
```java
@Service
public class CActivityService extends CEntityOfProjectService<CActivity> {
    private final CActivityTypeService typeService;
    
    public CActivityService(
        final IActivityRepository repository,
        final Clock clock,
        final ISessionService sessionService,
        final CActivityTypeService typeService) {
        
        super(repository, clock, sessionService);
        this.typeService = typeService;
    }
}
```

#### ❌ INCORRECT
```java
@Service
public class CActivityService extends CEntityOfProjectService<CActivity> {
    @Autowired
    private CActivityTypeService typeService;  // Field injection!
}
```

---

## 6. View & UI Patterns

### 6.1 View Class Structure

```java
@Route(value = "entities", layout = MainLayout.class)
@PageTitle("Entities")
@RolesAllowed("USER")
public class CEntityView extends CAbstractPage {
    
    // 1. Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(CEntityView.class);
    
    // 2. Services
    private final CEntityService service;
    
    // 3. Components (typeName convention)
    private CGrid<CEntity> grid;
    private CButton buttonAdd;
    private CButton buttonEdit;
    private CDialog dialogEdit;
    
    // 4. Constructor
    public CEntityView(CEntityService service) {
        this.service = service;
        
        initializeComponents();
        configureBindings();
        configureLayout();
        loadData();
    }
    
    // 5. Initialization
    private void initializeComponents() {
        buttonAdd = create_buttonAdd();
        buttonEdit = create_buttonEdit();
        grid = createGrid();
    }
    
    // 6. Factory methods
    private CButton create_buttonAdd() {
        CButton button = new CButton("Add");
        button.setId("custom-add-button");
        button.addClickListener(e -> on_buttonAdd_clicked());
        return button;
    }
    
    // 7. Event handlers
    private void on_buttonAdd_clicked() {
        // Handle click
    }
    
    // 8. Data methods
    private void loadData() {
        grid.setItems(service.findAll());
    }
}
```

### 6.2 Dialog UI Design Rules (MANDATORY)

#### Width and Spacing
```java
// ✅ CORRECT
mainLayout.setMaxWidth("600px");  // Max constraint
mainLayout.setWidthFull();        // Responsive
mainLayout.setSpacing(false);
mainLayout.getStyle().set("gap", "12px");  // Custom gap

// ❌ WRONG
mainLayout.setWidth("600px");     // Fixed, no flexibility
mainLayout.setSpacing(true);      // Default spacing too generous
```

**Rationale**: 600px optimal, max-width prevents overflow, custom gaps for compact look

#### Multi-Column Layouts
**RULE**: Use 2-column grid for 6+ checkboxes

```java
// ✅ CORRECT - 8 checkboxes in 2 columns
final HorizontalLayout grid = new HorizontalLayout();
final VerticalLayout leftColumn = new VerticalLayout();   // 4 items
final VerticalLayout rightColumn = new VerticalLayout();  // 4 items
grid.add(leftColumn, rightColumn);
```

#### Select All/Deselect All
**RULE**: Toggle must affect ALL checkboxes equally

```java
// ✅ CORRECT
private void toggleSelectAll() {
    allSelected = !allSelected;
    buttonSelectAll.setText(allSelected ? "Deselect All" : "Select All");
    
    // All checkboxes get same value
    checkboxes.forEach(cb -> cb.setValue(allSelected));
}

// ❌ WRONG - Inverse logic
if (allSelected) {
    checkbox1.setValue(true);
    checkbox2.setValue(false);  // ❌ Confusing!
}
```

### 6.3 Entity Type Selection Rules

**RULE**: Use `CComboBox` with `CEntityRegistry`

```java
// ✅ CORRECT
final CComboBox<String> comboBox = new CComboBox<>("Select Entity Type");
final List<String> entityKeys = CEntityRegistry.getAllRegisteredEntityKeys();
comboBox.setItems(entityKeys);

comboBox.setItemLabelGenerator(key -> {
    final Class<?> clazz = CEntityRegistry.getEntityClass(key);
    return CEntityRegistry.getEntityTitleSingular(clazz);
});

// ❌ WRONG
final ComboBox<Class<?>> comboBox = new ComboBox<>();
comboBox.setItems(CActivity.class, CMeeting.class);  // Hardcoded!
comboBox.setItemLabelGenerator(Class::getSimpleName); // Technical!
```

**Special first item** for copy/move dialogs:
```java
private static final String SAME_AS_SOURCE_KEY = "__SAME_AS_SOURCE__";

comboBox.setItemLabelGenerator(key -> {
    if (SAME_AS_SOURCE_KEY.equals(key)) {
        return "⭐ Same as Source (" + sourceEntityTitle + ")";
    }
    // ... other items
});
```

### 6.4 Unique Name Generation

**RULE**: Use `service.newEntity()` for auto-generated names

```java
// ✅ CORRECT - Let service generate
final CAbstractService service = getServiceForEntity(targetClass);
final CEntityDB tempEntity = service.newEntity();
if (tempEntity instanceof CEntityNamed) {
    final String uniqueName = ((CEntityNamed<?>) tempEntity).getName();
    // Use uniqueName (format: EntityName##)
}

// ❌ WRONG - Manual generation
final String name = entityName + " (Copy)";  // Not unique!
final String name = entityName + System.currentTimeMillis();  // Ugly!
```

**Update name on type change**:
```java
comboBoxTargetType.addValueChangeListener(event -> {
    if (event.getValue() != null) {
        updateGeneratedName(event.getValue());
    }
});
```

### 6.5 Navigation Rules

**RULE**: Use `CDynamicPageRouter.navigateToEntity()`

```java
// ✅ CORRECT
final CEntityDB saved = service.save(entity);
CDynamicPageRouter.navigateToEntity(saved);

// ❌ WRONG
UI.getCurrent().navigate("/activities/" + entity.getId());  // Hardcoded!
```

**What it does**:
1. Gets entity's `VIEW_NAME` field
2. Looks up `CPageEntity` by view name and project
3. Constructs route: `cdynamicpagerouter/page:{pageId}&item:{entityId}`
4. Navigates and auto-selects entity in grid

### 6.6 Grid Component Standards (MANDATORY)

**RULE**: Use entity column helpers, not manual rendering

#### ❌ WRONG - Manual Rendering
```java
grid.addColumn(activity -> {
    CStatus status = activity.getStatus();
    return status != null ? status.getName() : "";
}).setHeader("Status");

grid.addColumn(activity -> {
    List<CUser> users = activity.getParticipants();
    if (users == null || users.isEmpty()) return "None";
    return users.stream().map(CUser::getName).collect(Collectors.joining(", "));
}).setHeader("Participants");
```

#### ✅ CORRECT - Entity Column Helpers
```java
// Single entity reference
grid.addColumnEntityNamed(CActivity::getStatus, "Status");
grid.addColumnEntityNamed(CActivity::getAssignedTo, "Assigned To");

// Collection of entities
grid.addColumnEntityCollection(CActivity::getParticipants, "Participants");
grid.addColumnEntityCollection(CActivity::getTags, "Tags");

// Custom rendering with CLabelEntity
grid.addEntityColumn(CActivity::getStatus, "Status", "status", CStatus.class);
```

**Benefits**:
- Consistent rendering across application
- Null safety built-in
- Lazy loading handling automatic
- Name extraction automatic
- Less code (1 line vs 5-10 lines)

### 6.7 Component ID Standards (MANDATORY)

**RULE**: All interactive components must have stable IDs for Playwright

```java
// ✅ CORRECT
button.setId("custom-save-button");
textField.setId("custom-username-input");
grid.setId("custom-activities-grid");

// ❌ WRONG
button.setId("btn" + System.currentTimeMillis());  // Dynamic!
// Or no ID at all
```

**Format**: `custom-{entity}-{component}-{action}`
- `custom-activity-save-button`
- `custom-user-name-input`
- `custom-meeting-grid`

### 6.8 Two-View Pattern (Critical for Complex Entities)

Some entities need TWO views:
1. **Standard View** (Grid + Detail) - CRUD operations
2. **Single-Page View** (Full-screen Component) - Specialized workflow

**When to use**:
- Entity has BOTH management needs AND complex interactive workflow
- Examples: Kanban (board management + sprint board), Validation (session management + execution)

**Implementation**:
```java
public static void initialize(...) {
    // View 1: Standard CRUD (always create first)
    CDetailSection detailSection = createBasicView(project);
    CGridEntity grid = createGridEntity(project);
    initBase(clazz, project, ..., menuTitle, pageTitle, description, toolbar, menuOrder);
    
    // View 2: Single-page specialized workflow (optional)
    CDetailSection specialSection = createSpecializedView(project);
    CGridEntity specialGrid = createGridEntity(project);
    specialGrid.setAttributeNone(true);  // ← CRITICAL: Hide grid
    specialSection.setName(pageTitle + " Specialized Section");
    specialGrid.setName(pageTitle + " Specialized Grid");
    initBase(clazz, project, ..., 
        menuTitle + ".Execute",          // Submenu
        pageTitle + " Execution",
        "Specialized description",
        true,
        menuOrder + ".1");               // Submenu order
}
```

---

## 7. Testing Standards

### 7.1 Core Testing Principles (MANDATORY)

#### Browser Visibility
```bash
# ✅ CORRECT - Browser ALWAYS visible by default
PLAYWRIGHT_HEADLESS=false mvn test -Dtest=TestClass

# ❌ WRONG - Don't run headless during development
PLAYWRIGHT_HEADLESS=true mvn test
```

**Rule**: Browser must be VISIBLE during test development and debugging

#### Playwright Logging
```bash
# ✅ CORRECT - Always log to /tmp/playwright.log
mvn test -Dtest=CAdaptivePageTest 2>&1 | tee /tmp/playwright.log

# Monitor in another terminal
tail -f /tmp/playwright.log
```

### 7.2 Navigation Pattern (MANDATORY)

**RULE**: Use CPageTestAuxillary buttons, NOT side menu

```java
// ❌ WRONG - Don't navigate via side menu
navigateToViewByText("Activities");
clickMenuItem("Activities");

// ✅ CORRECT - Use CPageTestAuxillary buttons
@Test
void testSpecificView() {
    loginToApplication();
    
    // Navigate to test auxiliary page
    page.navigate("http://localhost:" + port + "/cpagetestauxillary");
    wait_500();
    
    // Click button by stable ID
    page.locator("#test-aux-btn-activities-0").click();
    wait_1000();
    
    // Continue test...
}
```

**Button ID Pattern**: `test-aux-btn-{sanitized-title}-{index}`

**Filtering**:
```bash
# Test only specific routes
mvn test -Dtest=CAdaptivePageTest -Dtest.routeKeyword=activity

# Test specific button
mvn test -Dtest=CAdaptivePageTest -Dtest.targetButtonId=test-aux-btn-activities-0
```

### 7.3 Intelligent Adaptive Testing Pattern (MANDATORY)

**RULE**: Use `CAdaptivePageTest` for ALL page testing

```bash
# ✅ CORRECT - Use adaptive test
mvn test -Dtest=CAdaptivePageTest 2>&1 | tee /tmp/playwright.log

# ✅ CORRECT - Test specific page
mvn test -Dtest=CAdaptivePageTest -Dtest.targetButtonId=test-aux-btn-activities-0

# ❌ WRONG - Don't create page-specific tests
@Test void testActivitiesPage() { ... }  // DON'T DO THIS
```

**Architecture**:
```
CAdaptivePageTest (Main Test Class)
├── IControlSignature → Component detection
├── IComponentTester → Component testing
├── CBaseComponentTester → Common utilities
└── Component Implementations
    ├── CCrudToolbarTester
    ├── CGridComponentTester
    ├── CAttachmentComponentTester
    ├── CCommentComponentTester
    └── CLinkComponentTester
```

**Creating new component tester**:
```java
public class CTagComponentTester extends CBaseComponentTester {
    
    private static final String TAG_SELECTOR = "#custom-tag-component, [id*='tag']";
    
    @Override
    public boolean canTest(final Page page) {
        return elementExists(page, TAG_SELECTOR);
    }
    
    @Override
    public String getComponentName() {
        return "Tag Component";
    }
    
    @Override
    public void test(final Page page) {
        LOGGER.info("      🏷️ Testing Tag Component...");
        // Test tag-specific functionality
        LOGGER.info("      ✅ Tag component test complete");
    }
}
```

### 7.4 Screenshot Policy (MANDATORY)

**RULE**: Only take screenshots on errors, NOT on success

```java
// ❌ WRONG - Taking screenshot after every operation
loginToApplication();
takeScreenshot("after-login", false);  // ← Remove this
clickNewButton();
takeScreenshot("clicked-new", false);  // ← Remove this

// ✅ CORRECT - Only on errors
try {
    loginToApplication();
    clickNewButton();
    fillForm();
    clickSave();
    // No screenshots - test passed
} catch (Exception e) {
    takeScreenshot("test-failure", true);  // ← Only on error
    throw e;
}
```

**Rationale**: Reduces test runtime, disk usage, focuses on failures

### 7.5 Test Execution Strategy

```bash
# Run all pages
mvn test -Dtest=CAdaptivePageTest 2>&1 | tee /tmp/playwright.log

# Run keyword-filtered pages
mvn test -Dtest=CAdaptivePageTest -Dtest.routeKeyword=activity 2>&1 | tee /tmp/playwright.log

# Run specific button
mvn test -Dtest=CAdaptivePageTest -Dtest.targetButtonId=test-aux-btn-activities-0 2>&1 | tee /tmp/playwright.log
```

### 7.6 Testing Rules Summary

1. ✅ Browser visible by default
2. ✅ Log to `/tmp/playwright.log`
3. ✅ Use `CAdaptivePageTest` (no page-specific tests)
4. ✅ Create component testers, not test scripts
5. ✅ Navigate via CPageTestAuxillary buttons
6. ✅ Throw exceptions, never ignore errors
7. ✅ Fail-fast on errors
8. ✅ Generic component tests (work across all entities)
9. ✅ Screenshots only on errors
10. ✅ Stable component IDs

---

## 8. Security & Multi-Tenant

### 8.1 Login Pattern

**Pattern**: `username@company_id`

```java
// CCustomLoginView constructs identifier
String loginIdentifier = username + "@" + companyId;

// CUserService splits it
CUser user = userService.findByUsername(companyId, username);
```

**Services must**:
- Fail fast when company context missing
- Filter all queries by company/project
- Verify ownership before operations

### 8.2 Method-Level Security

```java
@Service
@PreAuthorize("isAuthenticated()")
public class CActivityService extends CEntityOfProjectService<CActivity> {
    
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteActivity(CActivity activity) {
        // Only admins can delete
    }
}
```

### 8.3 View-Level Security

```java
@Route("admin/settings")
@PageTitle("Admin Settings")
@RolesAllowed("ADMIN")
public class CAdminSettingsView extends CAbstractPage {
    // Admin-only view
}
```

### 8.4 Input Validation

```java
@Transactional
public CActivity createActivity(String name, CProject project) {
    // Validate inputs
    Check.notBlank(name, "Name cannot be blank");
    Objects.requireNonNull(project, "Project cannot be null");
    Objects.requireNonNull(project.getId(), "Project must be persisted");
    
    // Validate length
    if (name.length() > CEntityConstants.MAX_LENGTH_NAME) {
        throw new IllegalArgumentException("Name too long");
    }
    
    // Check duplicates
    Optional<CActivity> existing = findByNameAndProject(name, project);
    if (existing.isPresent()) {
        throw new IllegalArgumentException("Activity already exists");
    }
    
    // Create
    CActivity activity = new CActivity(name, project);
    initializeNewEntity(activity);
    return save(activity);
}
```

### 8.5 Tenant Context

- Always read company/project from session service
- Never trust caller-provided IDs without verification
- Cascading deletes respect tenant cleanup (`@OnDelete(CASCADE)`)

---

## 9. Workflow & CI/CD

### 9.1 Commit Standards

**Format**: Short, present-tense, imperative

**Examples**:
- ✅ "Add Playwright login regression"
- ✅ "Fix null pointer in activity service"
- ✅ "Update copy pattern documentation"
- ❌ "Fixed a bug" (too vague)
- ❌ "Adding new feature" (wrong tense)

**Best practices**:
- Group related changes
- Describe cross-module impacts in body
- Link Jira/GitHub issues

### 9.2 Pull Request Checklist

- [ ] Code formatted (`mvn spotless:apply`)
- [ ] Build passes (`mvn clean verify`)
- [ ] Tests pass (backend + Playwright)
- [ ] Documentation updated (if patterns changed)
- [ ] Screenshots included (if UI changes)
- [ ] Commit messages descriptive

### 9.3 Before Review

```bash
# Format code
mvn spotless:apply

# Full build + test
mvn clean verify

# Playwright tests (if UI changes)
./run-playwright-tests.sh menu

# Check screenshots
ls -lh target/screenshots/
```

---

## 10. Agent Execution Guidelines

### 10.1 Validation Rules

**User says "do not run tests"**:
- ✅ Make code changes only
- ✅ Static inspection
- ❌ Do NOT run `mvn test` or `verify`
- ❌ Do NOT run Playwright tests

**User says "test it"**:
- ✅ Run appropriate tests
- ✅ Report results
- ✅ Fix failures

### 10.2 Output Requirements

- Always start CLI responses with "SSC WAS HERE!!"
- Only change files strictly required
- Explain changes file by file
- If unclear, make reasonable assumption and document it
- Do not leave TODOs
- Choose conservative option if unsure

### 10.3 Before Coding

1. Read existing implementations of similar features
2. Identify patterns already used in codebase
3. Follow closest existing example
4. Check coding rules in this file

---

## 11. Pattern Enforcement Rules

### 11.1 Entity Checklist

When creating/modifying entity:

- [ ] Extends appropriate base class
- [ ] Has all mandatory constants (DEFAULT_COLOR, ENTITY_TITLE_*, VIEW_NAME)
- [ ] Fields have `@AMetaData` annotations
- [ ] Implements `copyEntityTo()` method with 3-parameter signature
- [ ] Calls `super.copyEntityTo()` first
- [ ] Handles unique fields (makes them unique)
- [ ] No sensitive fields copied
- [ ] Proper lazy loading (`LAZY` + `LEFT JOIN FETCH`)

### 11.1.1 Abstract Entity Checklist (CRITICAL)

When creating/modifying abstract entity:

- [ ] Uses `@MappedSuperclass` (NOT `@Entity`)
- [ ] No `@Table` annotation
- [ ] Generic type parameter `<EntityClass>`
- [ ] Protected constructors (not public)
- [ ] Does NOT call `initializeDefaults()` in constructors
- [ ] Implements `initializeDefaults()` but only called by concrete classes
- [ ] Corresponding abstract service with NO `@Service` annotation

### 11.1.2 Concrete Entity Checklist (CRITICAL)

When creating/modifying concrete entity:

- [ ] Uses `@Entity` and `@Table` annotations
- [ ] Public constructors
- [ ] MANDATORY call to `initializeDefaults()` in ALL constructors
- [ ] All entity constants defined (DEFAULT_COLOR, ENTITY_TITLE_*, VIEW_NAME)
- [ ] Implements `copyEntityTo()` with proper type checking
- [ ] Corresponding concrete service with `@Service` and interfaces

### 11.2 Service Checklist

When creating/modifying service:

- [ ] Extends appropriate base service
- [ ] Constructor dependency injection (no field injection)
- [ ] Implements `getEntityClass()`
- [ ] Overrides `checkDeleteAllowed()` if needed
- [ ] No mutable instance fields (stateless!)
- [ ] Uses `sessionService` for user context
- [ ] `@Transactional` annotations correct
- [ ] Security annotations present

### 11.2.1 Abstract Service Checklist (CRITICAL)

When creating/modifying abstract service:

- [ ] NO `@Service` annotation
- [ ] NO `IEntityRegistrable, IEntityWithView` interfaces
- [ ] Generic type parameter matching entity
- [ ] Common validation and business logic
- [ ] Protected constructor for dependency injection

### 11.2.2 Concrete Service Checklist (CRITICAL)

When creating/modifying concrete service:

- [ ] `@Service` annotation present
- [ ] Implements `IEntityRegistrable, IEntityWithView` interfaces
- [ ] Public `getEntityClass()` method
- [ ] Type-specific validation logic
- [ ] Implements all interface methods

### 11.3 Repository Checklist

When creating/modifying repository:

- [ ] Follows inheritance pattern correctly
- [ ] Query methods return correct types
- [ ] Proper `@Query` annotations

### 11.3.1 Abstract Repository Checklist (CRITICAL)

When creating/modifying abstract repository:

- [ ] `@NoRepositoryBean` annotation present
- [ ] NO HQL queries (abstract method signatures only)
- [ ] Generic type parameters correct
- [ ] Extends appropriate base repository

### 11.3.2 Concrete Repository Checklist (CRITICAL)

When creating/modifying concrete repository:

- [ ] HQL queries reference concrete entity names (NOT abstract)
- [ ] `@Override` all abstract methods
- [ ] Proper `@Query` and `@Param` annotations
- [ ] Returns concrete entity types

### 11.4 View Checklist

When creating/modifying view:

- [ ] Extends appropriate base view
- [ ] `@Route`, `@PageTitle`, `@RolesAllowed` annotations
- [ ] Component fields use typeName convention
- [ ] Event handlers use `on_{component}_{event}` pattern
- [ ] Factory methods use `create_{component}` pattern
- [ ] Stable component IDs for Playwright
- [ ] Uses entity column helpers for grids
- [ ] Uses `CDynamicPageRouter` for navigation

### 11.5 Testing Checklist

When creating/modifying tests:

- [ ] Uses `CAdaptivePageTest` (not page-specific tests)
- [ ] Creates component testers (not inline test logic)
- [ ] Uses CPageTestAuxillary for navigation
- [ ] Browser visible during development
- [ ] Logs to `/tmp/playwright.log`
- [ ] Screenshots only on errors
- [ ] Stable selectors (component IDs)

---

## 12. Self-Improvement Process

### 12.1 When to Update This Document

**Trigger events**:
- New pattern discovered and validated
- Existing pattern improved
- Common mistake identified
- Testing approach enhanced
- Security issue addressed
- Performance optimization found

### 12.2 Update Procedure

1. **Identify pattern**: Recognize recurring solution
2. **Validate**: Ensure it solves problem correctly
3. **Document**: Add to appropriate section
4. **Examples**: Provide ✅ CORRECT and ❌ WRONG examples
5. **Cross-reference**: Link related sections
6. **Version**: Update version number and date
7. **Review**: Team review before merging

### 12.3 Pattern Validation Criteria

**A pattern is valid if**:
- ✅ Solves real problem
- ✅ Works across multiple cases
- ✅ Maintains consistency
- ✅ Doesn't break existing code
- ✅ Improves maintainability
- ✅ Testable
- ✅ Documented with examples

### 12.4 Documentation Evolution

**Version history**:
- v1.0 (2026-01-15): Initial playbook
- v2.0 (2026-01-19): Consolidated all patterns and rules
- v2.1 (2026-01-23): Added Abstract Entity & Service Patterns (Section 4.5)

**Continuous improvement**:
- Monthly review of patterns
- Quarterly major updates
- Immediate updates for critical patterns
- Community feedback integration

### 12.5 AI Agent Self-Learning

**How agents should use this document**:
1. Read before any task
2. Reference during implementation
3. Validate against patterns
4. Report inconsistencies
5. Suggest improvements

**Learning loop**:
```
Task → Check AGENTS.md → Implement → Validate → Update AGENTS.md (if new pattern)
```

---

## 13. Quick Reference

### 13.1 Core Rules (Never Break)

1. **C-prefix**: All custom classes start with C
2. **Type safety**: Always use generics
3. **Stateless services**: No user state in service fields
4. **CopyTo pattern**: All entities implement it
5. **Entity constants**: All entities have them
6. **Fail fast**: No silent failures
7. **Session context**: Always from `sessionService`
8. **Navigation**: Use `CDynamicPageRouter`
9. **Grid columns**: Use entity helpers
10. **Testing**: Use `CAdaptivePageTest`

### 13.2 Common Mistakes

| Mistake | Correct Pattern |
|---------|----------------|
| Missing C-prefix | Add C to all custom classes |
| Raw types | Use generics: `<CActivity>` |
| User state in service | Use `sessionService.getActiveUser()` |
| No `copyEntityTo()` | Implement mandatory method with 3 parameters |
| Wrong copyEntityTo signature | Use `(CEntityDB<?>, CAbstractService, CCloneOptions)` |
| Manual grid rendering | Use `addColumnEntityNamed()` |
| Direct navigation | Use `CDynamicPageRouter` |
| Page-specific tests | Use `CAdaptivePageTest` |
| Silent failures | Throw exceptions |
| Field injection | Constructor injection |
| Hardcoded entity types | Use `CEntityRegistry` |
| **Abstract entity as @Entity** | **Use @MappedSuperclass for abstract entities** |
| **Abstract service with @Service** | **NO @Service annotation for abstract services** |
| **HQL queries on @MappedSuperclass** | **Query concrete @Entity classes only** |
| **Abstract constructors call initializeDefaults()** | **Only concrete constructors call initializeDefaults()** |

### 13.2.1 Critical Abstract Entity Mistakes (Hibernate Issues)

| ❌ **WRONG** | ✅ **CORRECT** | **Why?** |
|-------------|---------------|----------|
| `@Entity abstract class CBabNode` | `@MappedSuperclass abstract class CBabNode` | Abstract entities aren't queryable |
| `@Query("FROM CBabNode")` | `@Query("FROM CBabNodeCAN")` | Hibernate can't query @MappedSuperclass |
| `@Service abstract class CBabNodeService` | NO `@Service` for abstract services | Spring can't instantiate abstract classes |
| Abstract constructor calls `initializeDefaults()` | Only concrete constructors call it | Abstract entities don't initialize fully |

### 13.3 Where to Find Answers

| Question | Look Here |
|----------|-----------|
| How to structure entity? | Section 4.1 |
| How to structure abstract entities? | Section 4.5 |
| How to write service? | Section 5.2 |
| How to create view? | Section 6.1 |
| How to test? | Section 7 |
| Multi-user safety? | Section 5.3 |
| UI design rules? | Section 6.2 |
| Copy pattern? | Section 4.3 |
| Navigation? | Section 6.5 |
| Security? | Section 8 |
| Abstract entity patterns? | Section 4.5 |
| Repository patterns? | Section 4.5.4 |

---

## 14. Contact & Support

**Questions or Issues?**
- Review this document first
- Check code examples in codebase
- Consult team lead if still unclear

**Contributing to This Document**:
- Follow update procedure (Section 12.2)
- Include examples and rationale
- Test patterns before documenting
- Update version number

---

**END OF AGENTS MASTER PLAYBOOK**

**Remember**: This document is MANDATORY. Following these patterns ensures:
- ✅ Consistency across codebase
- ✅ Multi-user safety
- ✅ Hibernate/JPA compatibility
- ✅ Testability
- ✅ Maintainability
- ✅ AI-assisted development effectiveness

**Version**: 2.1  
**Last Updated**: 2026-01-23  
**Next Review**: 2026-02-23

---

## ⚠️ CRITICAL UPDATE - Abstract Entity Patterns

**Version 2.1 adds MANDATORY patterns for abstract entities discovered through BAB node implementation:**

1. **Abstract entities MUST use `@MappedSuperclass`** (not `@Entity`)
2. **Abstract services MUST NOT have `@Service`** annotation
3. **Repository queries MUST reference concrete entities** (not abstract)
4. **Only concrete constructors call `initializeDefaults()`**

**This prevents Hibernate `UnknownEntityException` and ensures proper Spring bean management.**

See **Section 4.5** for complete implementation patterns and examples.
