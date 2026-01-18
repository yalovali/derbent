# Coding Standards and Best Practices

## Overview

This document defines the coding standards, naming conventions, and best practices for the Derbent project. Following these standards ensures consistency, maintainability, and optimal performance.

**Target Audience**: Developers, AI agents (GitHub Copilot, Codex), code reviewers

## Meta-Guidelines: Using This Documentation

### For AI Agents (MANDATORY)

This section defines how AI agents (GitHub Copilot, Codex, etc.) MUST use and reference the project documentation.

#### Rule 1: Documentation Hierarchy (MANDATORY)

When generating code, AI agents MUST consult documentation in this order:

1. **Specific Guidelines First**: Check specialized documents for the task at hand
   - **Method placement** → [Method Placement Guidelines](method-placement-guidelines.md) ⭐ **NEW - CHECK FIRST**
   - **Component/utility reference** → [Component and Utility Reference](component-utility-reference.md) ⭐ **NEW - CHECK FIRST**
   - UI/CSS work → [UI, CSS, and Layout Coding Standards](ui-css-coding-standards.md)
   - Component development → [Component Coding Standards](../development/component-coding-standards.md)
   - Testing → [Playwright Testing Guidelines](../testing/)
   
2. **General Coding Standards**: This document (coding-standards.md)
   
3. **Pattern Documents**: For specific implementation patterns
   - [Service Layer Patterns](service-layer-patterns.md)
   - [View Layer Patterns](view-layer-patterns.md)
   - [Entity Inheritance Patterns](entity-inheritance-patterns.md)

4. **AI-Specific Guidelines**: For agent-specific best practices
   - [GitHub Copilot Guidelines](../development/copilot-guidelines.md)

#### Rule 2: Cross-Referencing Requirements (MANDATORY)

When creating or updating guidelines:

1. **Add Cross-References**: Every guideline document MUST link to related documents
2. **Use Consistent Paths**: Use relative paths from the current document
3. **Reference by Section**: Link to specific sections when possible
4. **Avoid Duplication**: Don't copy guidelines between documents—reference them instead

**Example**:
```markdown
For UI component styling patterns, see [UI, CSS, and Layout Coding Standards](ui-css-coding-standards.md#button-creation-patterns).

For entity color application, refer to [Color and Icon Standards](ui-css-coding-standards.md#color-and-icon-standards).
```

#### Rule 3: Guideline Discoverability (MANDATORY)

All guideline documents MUST include:

1. **Clear Title**: Describes document scope
2. **Target Audience**: Who should use this document
3. **Overview Section**: Brief description of content
4. **Table of Contents**: For documents > 500 lines (optional but recommended)
5. **Related Documentation**: Links to related guidelines at the end
6. **AI Agent Guidelines Section**: Specific instructions for code generation

#### Rule 4: Pattern Recognition Format (MANDATORY)

When documenting patterns for AI agents, use this format:

```markdown
### Pattern: [Pattern Name]

**When to Use**: [Describe scenario]

**✅ CORRECT**:
```java
// Example code
```

**❌ INCORRECT**:
```java
// Anti-pattern code
```

**Rule**: [Explicit rule statement]
```

#### Rule 5: Update Procedures (MANDATORY)

When updating guidelines:

1. **Version Number**: Increment version at bottom of document
2. **Last Updated Date**: Update date stamp
3. **Change Log** (for major updates): Document significant changes
4. **Cross-Check References**: Ensure all cross-references still valid
5. **AI Agent Validation**: Test with AI agents to ensure patterns work

### Documentation Structure

The Derbent project documentation is organized as follows:

```
docs/
├── architecture/          # Architecture and design patterns
│   ├── coding-standards.md              # THIS FILE - General coding standards
│   ├── method-placement-guidelines.md   # ⭐ NEW - Where methods belong
│   ├── component-utility-reference.md   # ⭐ NEW - Complete component index
│   ├── ui-css-coding-standards.md       # UI, CSS, and layout patterns
│   ├── entity-inheritance-patterns.md   # Entity class hierarchies
│   ├── service-layer-patterns.md        # Service class patterns
│   └── view-layer-patterns.md           # View/UI class patterns
├── development/          # Development workflows and guidelines
│   ├── copilot-guidelines.md            # AI agent usage guidelines
│   ├── component-coding-standards.md    # Component development
│   └── multi-user-development-checklist.md
├── implementation/       # Specific implementation patterns
├── testing/              # Testing guidelines and patterns
└── features/             # Feature-specific documentation
```

**Rule for AI Agents**: When asked about a specific topic, search for the most specific guideline document first, then fall back to general guidelines.

## ⭐ MANDATORY: Method Placement and Component Usage (NEW)

**CRITICAL RULES** - These MUST be followed by all developers and AI agents:

### Rule 1: Check Before Creating Methods

Before creating ANY new method, you MUST:

1. **Check existing code** - Search for similar functionality
2. **Consult references** - Check [Component and Utility Reference](component-utility-reference.md)
3. **Verify placement** - Use [Method Placement Guidelines](method-placement-guidelines.md) decision tree
4. **Use existing utilities** - Don't duplicate what exists

**Complete Guidelines**: See [Method Placement Guidelines](method-placement-guidelines.md)

### Rule 2: NEVER Use Raw Vaadin Components

**MANDATORY**: Always use C-prefixed components instead of raw Vaadin components.

| ❌ FORBIDDEN | ✅ USE INSTEAD |
|--------------|----------------|
| `new Span()` | `new CSpan()` |
| `new Div()` | `new CDiv()` |
| `new Button()` | `new CButton()` |
| `new VerticalLayout()` | `new CVerticalLayout()` |
| `new HorizontalLayout()` | `new CHorizontalLayout()` |

**Why**: C-prefixed components provide consistent styling, framework-specific behavior, and better maintainability.

**Complete Reference**: See [Component and Utility Reference](component-utility-reference.md)

### Rule 3: Use Existing Utility Classes

Common utilities that MUST be used instead of creating custom methods:

| Need | Use | NOT |
|------|-----|-----|
| Display entity label | `CLabelEntity.createLabel(entity)` | Custom label creation |
| Display user | `CLabelEntity.createUserLabel(user)` | Custom user display |
| Get entity color | `CColorUtils.getColorFromEntity(entity)` | Private getColor methods |
| Get contrast color | `CColorUtils.getContrastTextColor(bg)` | Private color calculation |
| Create icon | `CColorUtils.createStyledIcon(icon)` | Custom icon creation |
| Validate not null | `Check.notNull(obj, msg)` | Manual if checks |
| Validate not blank | `Check.notBlank(str, msg)` | Manual string checks |
| Show notification | `CNotificationService.show*()` | `Notification.show()` |

**Complete List**: See [Component and Utility Reference](component-utility-reference.md)

### Quick Decision Tree

```
Before creating a method, ask:
├─ Does CColorUtils have this? (colors, icons, contrast)
├─ Does CLabelEntity have this? (entity display, labels)
├─ Does Check have this? (validation)
├─ Does CNotificationService have this? (notifications)
├─ Is this component-specific? → Keep in component
└─ General utility? → Add to appropriate utility class
```

## Core Principles

### 1. **C-Prefix Convention** (Mandatory)

All custom classes MUST be prefixed with "C":

#### ✅ Correct
```java
public class CActivity extends CProjectItem<CActivity> { }
public class CActivityService extends CEntityOfProjectService<CActivity> { }
public class CActivityView extends CAbstractPage { }
public class CButton extends Button { }
```

#### ❌ Incorrect
```java
public class Activity { }  // Missing C-prefix
public class ActivityService { }  // Missing C-prefix
```

**Benefits**:
- Instant recognition of custom vs. framework classes
- Enhanced IDE navigation and auto-complete
- AI-assisted development optimization
- Prevents naming conflicts with framework classes

**Exceptions**:
- Interface names start with `I`: `IActivityRepository`, `ISessionService`
- Test classes: `CActivityTest`, `CActivityServiceTest`
- Package names: lowercase without prefix

## Profile Seed Initialization (MANDATORY)

**Rule**: Profile-specific seed data MUST live in the related `*InitializerService` classes, not inside profile data initializers.

### Pattern: Profile Seed Initialization

**When to Use**: Adding or changing seed data for a product profile (e.g., BAB).

**✅ CORRECT**:
```java
// In initializer services
public static CCompany initializeSampleBab(final boolean minimal) throws Exception { ... }

// In CBabDataInitializer
final CCompany company = CCompanyInitializerService.initializeSampleBab(minimal);
final CUserCompanyRole adminRole = CUserCompanyRoleInitializerService.initializeSampleBab(company, minimal);
```

**❌ INCORRECT**:
```java
// In CBabDataInitializer
final CCompany company = new CCompany("BAB Gateway");
entityManager.persist(company);
```

**Rule**: `CBabDataInitializer` (and other profile initializers) must only orchestrate calls to `initializeSampleBab(...)` methods and UI definition initializers. Do not create or persist profile seed data directly in the initializer.

**Related**: [Method Placement Guidelines](method-placement-guidelines.md), [Service Layer Patterns](service-layer-patterns.md)

### 2. **Type Safety** (Mandatory)

Always use generic type parameters:

#### ✅ Correct
```java
public class CActivity extends CProjectItem<CActivity> {
    // Type-safe implementation
}

List<CActivity> activities = service.findAll();
```

#### ❌ Incorrect
```java
public class CActivity extends CProjectItem {  // Raw type
    // Loses type safety
}

List activities = service.findAll();  // Raw type
```

### 3. **Metadata-Driven Development**

Use `@AMetaData` for automatic UI generation:

```java
@Column(nullable = false, length = 255)
@Size(max = 255)
@NotBlank(message = "Name is required")
@AMetaData(
    displayName = "Activity Name",    // UI label
    required = true,                   // Required field indicator
    readOnly = false,                  // Editable in forms
    description = "Activity name",     // Tooltip/help text
    hidden = false,                    // Visible in UI
                            // Field display order
    maxLength = 255,                   // Max input length
    dataProviderBean = "CUserService"  // For ComboBox data source
)
private String name;
```

- **Field names must be exact**: UI metadata helpers are reflection-based (`createLineFromDefaults`, `setColumnFields`, dynamic forms). Always reference the real entity field name (and ensure matching getters/setters exist). Do not use aliases/renames in initializers—mismatches fail at runtime with `NoSuchFieldException` and block data initialization.

### 4. **Validation Flow (Service → UI)**

- **Service validation belongs in `validateEntity(...)`**: Each service must override `validateEntity` to enforce business rules (e.g., uniqueness, required relations). The method should throw `CValidationException` when a rule is violated.
- **Scope-aware uniqueness**: Uniqueness must be scoped correctly:
  - per **company** in `CEntityOfCompanyService` subclasses,
  - per **project** in `CEntityOfProjectService` subclasses,
  - per **parent entity** (e.g., child items under a master record).
  Always differentiate new vs. existing entities by comparing IDs.
- **UI handling**: User-triggered actions (`on_*_clicked`, dialog saves, toolbar saves) must catch `CValidationException` and show a user-facing notification (use `CNotificationService.showValidationException(...)`). Do not swallow validation errors; surface them clearly to the user.

## Naming Conventions

### Classes

| Type | Pattern | Example |
|------|---------|---------|
| Domain Entity | `C{Entity}` | `CActivity`, `CUser`, `CProject` |
| Service | `C{Entity}Service` | `CActivityService`, `CUserService` |
| Repository | `I{Entity}Repository` | `IActivityRepository`, `IUserRepository` |
| View/Page | `C{Entity}View` or `C{Entity}Page` | `CActivityView`, `CUserPage` |
| Component | `C{Component}` | `CButton`, `CGrid`, `CDialog` |
| Utility | `C{Purpose}` | `CAuxillaries`, `CPageableUtils` |
| Interface | `I{Name}` | `ISearchable`, `IKanbanEntity` |
| Test | `C{Class}Test` | `CActivityTest`, `CActivityServiceTest` |

**Validation module naming**: Use **Validation** for business entities (classes, packages, tables, menus, UI labels). Reserve **Test** terminology for automated tests (unit/integration/Playwright) and standards references to avoid ambiguity.

### Fields

```java
// Private fields - camelCase
private String activityName;
private LocalDate plannedStartDate;
private CUser assignedTo;

// Constants - UPPER_SNAKE_CASE
public static final String DEFAULT_COLOR = "#DC143C";
public static final int MAX_LENGTH_NAME = 255;
private static final Logger LOGGER = LoggerFactory.getLogger(CActivity.class);

// Boolean fields - use "is" prefix
private Boolean isActive;
private Boolean isDeletable;
private Boolean isCompleted;

// UI Component fields - typeName convention
private CButton buttonAdd;           // {type}{Name} format
private CButton buttonDelete;
private CDialog dialogConfirmation;
private CVerticalLayout layoutMain;
private CGrid<CEntity> gridItems;
private ComboBox<String> comboBoxStatus;
```

### UI Component Field Naming (typeName Convention)

All UI component fields must follow the `typeName` naming convention:

| Pattern | Example | Description |
|---------|---------|-------------|
| `button{Name}` | `buttonAdd`, `buttonDelete` | For all buttons |
| `dialog{Name}` | `dialogConfirmation`, `dialogSelection` | For dialogs |
| `layout{Name}` | `layoutMain`, `layoutToolbar` | For layouts |
| `grid{Name}` | `gridItems`, `gridUsers` | For grids |
| `comboBox{Name}` | `comboBoxStatus`, `comboBoxType` | For combo boxes |
| `textField{Name}` | `textFieldName`, `textFieldSearch` | For text fields |

### Event Handler Naming Convention

All event handlers must follow the `on_{componentName}_{eventType}` pattern:

```java
// Event handlers - on_{componentName}_{eventType} format
protected void on_buttonAdd_clicked() { }
protected void on_buttonDelete_clicked() { }
protected void on_comboBoxStatus_selected(String status) { }
protected void on_gridItems_doubleClicked(CEntity item) { }
```

### Factory Method Pattern for Components

Use `create_{componentName}` pattern for component factory methods:

```java
// Factory methods - create_{componentName} format
protected CButton create_buttonAdd() { }
protected CButton create_buttonDelete() { }
protected CDialog create_dialogConfirmation() { }
```

### Methods

```java
// Getters/Setters - standard Java bean convention
public String getName() { return name; }
public void setName(String name) { this.name = name; }

// Boolean getters
public Boolean getIsActive() { return isActive; }
public boolean isActive() { return isActive != null && isActive; }

// Business logic methods - descriptive verbs
public void completeActivity() { }
public boolean canDelete() { }
public void assignToUser(CUser user) { }

// Query methods
public List<CActivity> findOverdue() { }
public Optional<CActivity> findByName(String name) { }
public long countByStatus(CProjectItemStatus status) { }
```

### Packages

```java
tech.derbent.{module}.{layer}

// Examples:
tech.derbent.activities.domain      // Entity classes
tech.derbent.activities.service     // Service layer
tech.derbent.activities.view        // View/UI layer
tech.derbent.api.domains            // Base entity classes
tech.derbent.api.services           // Base service classes
tech.derbent.api.views              // Base view classes
tech.derbent.api.utils              // Utility classes
```

## Code Structure

### Entity Class Structure

```java
@Entity
@Table(name = "table_name")
@AttributeOverride(name = "id", column = @Column(name = "entity_id"))
public class CEntity extends CProjectItem<CEntity> {
    
    // 1. Constants (MANDATORY - alphabetically ordered)
    public static final String DEFAULT_COLOR = "#DC143C";
    public static final String DEFAULT_ICON = "vaadin:tasks";
    public static final String ENTITY_TITLE_PLURAL = "Entities";    // MANDATORY
    public static final String ENTITY_TITLE_SINGULAR = "Entity";     // MANDATORY
    private static final Logger LOGGER = LoggerFactory.getLogger(CEntity.class);
    public static final String VIEW_NAME = "Entities View";
    
    // 2. Fields - grouped by type
    // Basic fields
    @Column(nullable = false)
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
    
    // 6. equals, hashCode, toString (if overriding)
}
```

### Mandatory Entity Constants (CRITICAL)

Every entity class MUST define the following constants:

| Constant | Purpose | Example |
|----------|---------|---------|
| `DEFAULT_COLOR` | UI display color | `"#DC143C"` |
| `DEFAULT_ICON` | Vaadin icon identifier | `"vaadin:tasks"` |
| `ENTITY_TITLE_SINGULAR` | Human-readable singular name | `"Activity"`, `"User"` |
| `ENTITY_TITLE_PLURAL` | Human-readable plural name | `"Activities"`, `"Users"` |
| `VIEW_NAME` | View/page title | `"Activities View"` |

#### ✅ Correct Entity Constants
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

#### ❌ Incorrect - Missing Required Constants
```java
public class CActivity extends CProjectItem<CActivity> {
    public static final String DEFAULT_COLOR = "#DC143C";
    // Missing: DEFAULT_ICON, ENTITY_TITLE_SINGULAR, ENTITY_TITLE_PLURAL, VIEW_NAME
}
```

### Using Entity Titles via CEntityRegistry

The `CEntityRegistry` class provides methods to look up entity classes by title and vice versa:

```java
// Get entity class from title
Class<?> entityClass = CEntityRegistry.getEntityClassByTitle("Activity");       // By singular
Class<?> entityClass = CEntityRegistry.getEntityClassByTitle("Activities");     // By plural
Class<?> entityClass = CEntityRegistry.getEntityClassBySingularTitle("User");   // Singular only
Class<?> entityClass = CEntityRegistry.getEntityClassByPluralTitle("Users");    // Plural only

// Get title from entity class
String singular = CEntityRegistry.getEntityTitleSingular(CActivity.class);  // "Activity"
String plural = CEntityRegistry.getEntityTitlePlural(CActivity.class);      // "Activities"
```

### Service Class Structure

```java
@Service
@PreAuthorize("isAuthenticated()")
public class CEntityService extends CEntityOfProjectService<CEntity> {
    
    // 1. Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(CEntityService.class);
    
    // 2. Dependencies
    private final CStatusService statusService;
    private final CTypeService typeService;
    
    // 3. Constructor
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
    
    // 4. Override methods
    @Override
    protected Class<CEntity> getEntityClass() {
        return CEntity.class;
    }
    
    @Override
    public String checkDeleteAllowed(final CEntity entity) {
        // Implementation
    }
    
    // 5. Business logic methods
    @Transactional
    public void completeEntity(CEntity entity) {
        // Implementation
    }
    
    // 6. Query methods
    @Transactional(readOnly = true)
    public List<CEntity> findOverdue() {
        // Implementation
    }
}
```

### View Class Structure

```java
@Route(value = "entities", layout = MainLayout.class)
@PageTitle("Entities")
@RolesAllowed("USER")
public class CEntityView extends CAbstractPage {
    
    // 1. Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(CEntityView.class);
    
    // 2. Services
    private final CEntityService service;
    
    // 3. Components
    private CGrid<CEntity> grid;
    private CEntityForm form;
    
    // 4. Constructor
    public CEntityView(CEntityService service) {
        this.service = service;
        
        initializeComponents();
        configureBindings();
        configureLayout();
        loadData();
    }
    
    // 5. Initialization methods
    private void initializeComponents() { }
    private void configureBindings() { }
    private void configureLayout() { }
    
    // 6. Data methods
    private void loadData() { }
    private void refresh() { }
    
    // 7. Event handlers
    private void onSave() { }
    private void onCancel() { }
    private void onDelete() { }
}
```

## Best Practices

### Validation

#### Use Check Utility
```java
// ✅ CORRECT - Use Check utility
Objects.requireNonNull(entity, "Entity cannot be null");
Check.notBlank(name, "Name cannot be blank");
Check.notEmpty(list, "List cannot be empty");
Check.isTrue(value > 0, "Value must be positive");
Check.instanceof(obj, CEntityNamed.class, "Object must be an instance of CEntityNamed");

// ❌ INCORRECT - Manual checks
if (entity == null) {
    throw new IllegalArgumentException("Entity cannot be null");
}
```

#### Avoid Reflection and Caching Patterns
**IMPORTANT**: Do not use reflection method caching patterns as they make code difficult to read and maintain.

```java
// ❌ INCORRECT - Reflection with method caching
private Method cachedGetNameMethod;

private void cacheMethod(Class<?> clazz) {
    try {
        cachedGetNameMethod = clazz.getMethod("getName");
    } catch (NoSuchMethodException e) {
        cachedGetNameMethod = null;
    }
}

private String getName(Object entity) {
    if (cachedGetNameMethod == null) return "";
    try {
        return (String) cachedGetNameMethod.invoke(entity);
    } catch (Exception e) {
        return "";
    }
}

// ❌ INCORRECT - Hiding developer errors with safe returns
private String getName(Object entity) {
    Objects.requireNonNull(entity, "Entity cannot be null");
    if (entity instanceof CEntityNamed) {
        return ((CEntityNamed<?>) entity).getName();
    }
    return "";  // WRONG: Silently returns empty string for wrong type
}

// ✅ CORRECT - Fail fast on developer errors using Check.instanceOf
private String getName(Object entity) {
    Objects.requireNonNull(entity, "Entity cannot be null");
    Check.instanceOf(entity, CEntityNamed.class, "Item must be of type CEntityNamed to access name");
    return ((CEntityNamed<?>) entity).getName();
}

// ✅ CORRECT - Use type-safe interfaces with Check.instanceOf
private Object getStatus(Object entity) {
    Objects.requireNonNull(entity, "Entity cannot be null");
    Check.instanceOf(entity, IHasStatusAndWorkflow.class, "Item must implement IHasStatusAndWorkflow to access status");
    return ((IHasStatusAndWorkflow) entity).getStatus();
}
```

**Critical Rule: Developer Errors vs Runtime Errors**

**Developer Errors** (MUST throw exceptions):
- Calling `getName()` on an object that doesn't have a name property
- Calling `getStatus()` on an object that doesn't implement the status interface
- Wrong type being passed to a method that expects a specific type
- **Action**: Use `Check.instanceOf()` to fail fast and catch bugs during development

**Runtime Errors** (CAN handle gracefully):
- User input validation
- Network failures
- File not found
- Database connection issues
- **Action**: Use try-catch and return sensible defaults or error messages

```java
// ❌ WRONG - Hiding developer error
private String getEntityName(EntityClass item) {
    if (item instanceof CEntityNamed) {
        return ((CEntityNamed<?>) item).getName();
    }
    return "";  // WRONG: Developer should never call this with wrong type
}

// ✅ CORRECT - Exposing developer error immediately
private String getEntityName(EntityClass item) {
    Objects.requireNonNull(item, "Item cannot be null");
    Check.instanceOf(item, CEntityNamed.class, "Item must be of type CEntityNamed");
    return ((CEntityNamed<?>) item).getName();
}

// ✅ CORRECT - Valid runtime check when iterating mixed collections
private void processItems(List<?> items) {
    for (Object item : items) {
        if (item instanceof CEntityNamed) {
            // Process named entities
            String name = ((CEntityNamed<?>) item).getName();
        } else if (item instanceof CProject) {
            // Process projects differently
        }
        // This is OK - we expect mixed types at runtime
    }
}
```

**Why fail fast on developer errors?**
- Catches bugs immediately during development
- Makes code intentions explicit
- Prevents silent failures that are hard to debug
- Forces correct type usage
- Improves code quality through compile-time thinking

**When to use Check.instanceOf**:
```java
// ALWAYS use when you expect a specific type
Check.instanceOf(entity, CEntityNamed.class, "Entity must extend CEntityNamed");
final CEntityNamed<?> named = (CEntityNamed<?>) entity;
return named.getName();

// DON'T use when legitimately handling mixed types at runtime
for (Object item : mixedCollection) {
    if (item instanceof TypeA) {
        // Handle TypeA
    } else if (item instanceof TypeB) {
        // Handle TypeB
    }
}
```


#### Bean Validation Annotations
```java
@Column(nullable = false, length = 255)
@NotBlank(message = "Name is required")
@Size(max = 255, message = "Name too long")
private String name;

@Email(message = "Invalid email format")
@Column(nullable = false)
private String email;

@Min(value = 0, message = "Must be non-negative")
@Max(value = 100, message = "Must not exceed 100")
private Integer progress;
```

### Transaction Management

```java
// Read-only transactions
@Transactional(readOnly = true)
public List<CActivity> findAll() {
    return repository.findAll();
}

// Write transactions
@Transactional
public CActivity save(CActivity activity) {
    return repository.save(activity);
}

// Complex transactions
@Transactional
public void assignActivities(CUser user, List<CActivity> activities) {
    for (CActivity activity : activities) {
        activity.setAssignedTo(user);
        save(activity);
    }
}
```

### Exception Handling

```java
// Service layer
@Transactional
public CActivity createActivity(String name) {
    try {
        Check.notBlank(name, "Name cannot be blank");
        
        CActivity activity = new CActivity(name, getCurrentProject());
        return save(activity);
        
    } catch (IllegalArgumentException e) {
        LOGGER.warn("Invalid input for activity creation: {}", e.getMessage());
        throw e;
    } catch (Exception e) {
        LOGGER.error("Error creating activity: {}", e.getMessage(), e);
        throw new RuntimeException("Failed to create activity", e);
    }
}

// View layer
private void saveActivity() {
    try {
        service.save(activity);
        Notification.show("Activity saved successfully");
        refresh();
    } catch (Exception e) {
        LOGGER.error("Error saving activity", e);
        Notification.show("Error: " + e.getMessage())
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
```

### Logging

```java
// Use appropriate log levels
LOGGER.trace("Entering method with params: {}", params);  // Detailed debug
LOGGER.debug("Processing entity: {}", entity.getId());    // Debug info
LOGGER.info("Activity {} created by {}", name, user);     // Important events
LOGGER.warn("Activity {} is overdue", activityId);        // Warnings
LOGGER.error("Failed to save activity: {}", e.getMessage(), e);  // Errors

// Use parameterized logging (not string concatenation)
// ✅ CORRECT
LOGGER.info("User {} created activity {}", userId, activityId);

// ❌ INCORRECT
LOGGER.info("User " + userId + " created activity " + activityId);
```

**Console output format (enforced)**  
Keep console logs ANSI-colored and clickable in the IDE using the shared Spring Boot pattern (mirrored across `application*.properties`). Do not downgrade to plain text or change the color order without team approval.

```
spring.output.ansi.enabled=ALWAYS
logging.pattern.console=%clr(%d{${LOG_DATEFORMAT_PATTERN:HH:mm:ss.S}}){magenta} %clr(${LOG_LEVEL_PATTERN:%-5.5p}) \(%clr(%file:%line){cyan}\) %clr(%msg){red} %clr(%-40.40logger{39}){cyan}%n
logging.exception-conversion-word=%ex{short}
```

- The magenta timestamp + padded level keep alignment; the cyan `(file:line)` makes stack frame links clickable in Eclipse; the red message + cyan logger keep hot paths readable. Keep this pattern identical in every profile unless there is a compelling reason to override it locally.
- **Method names**: Logback can add them with `%M` (e.g., place `%clr(%M){yellow}` after `%clr(%file:%line){cyan}`), but `%M` forces caller data resolution on every log line and hurts throughput. Only enable it temporarily in a dedicated debug profile when you specifically need method names.

### Dependency Injection

```java
// ✅ CORRECT - Constructor injection
@Service
public class CActivityService extends CEntityOfProjectService<CActivity> {
    private final CStatusService statusService;
    
    public CActivityService(
        IActivityRepository repository,
        Clock clock,
        ISessionService sessionService,
        CStatusService statusService) {
        
        super(repository, clock, sessionService);
        this.statusService = statusService;
    }
}

// ❌ INCORRECT - Field injection
@Service
public class CActivityService extends CEntityOfProjectService<CActivity> {
    @Autowired
    private CStatusService statusService;
}
```

### Method Design

```java
// Keep methods focused and single-purpose
// ✅ GOOD
public void completeActivity(CActivity activity) {
    Objects.requireNonNull(activity, "Activity cannot be null");
    activity.setStatus(getCompletedStatus());
    activity.setActualEndDate(LocalDate.now());
    save(activity);
}

// ❌ BAD - Does too many things
public void processActivity(CActivity activity, String action) {
    if ("complete".equals(action)) {
        // Complete logic
    } else if ("assign".equals(action)) {
        // Assignment logic
    } else if ("delete".equals(action)) {
        // Delete logic
    }
}

// Use descriptive method names
// ✅ GOOD
public List<CActivity> findOverdueActivities() { }
public boolean canDeleteActivity(CActivity activity) { }
public void assignActivityToUser(CActivity activity, CUser user) { }

// ❌ BAD - Vague names
public List<CActivity> get() { }
public boolean check(CActivity activity) { }
public void process(CActivity activity, CUser user) { }
```

- Prefer instance methods that rely on the owning class state over utility-style methods with parameters. If a method can use existing fields
  (e.g., `currentSprint`, `getValue()`, cached lists), refactor to reduce parameters and bind behavior to the instance. Avoid static-looking helpers
  inside stateful classes unless there's a clear reason.

### Comments and Documentation

```java
/**
 * Completes an activity by setting its status to completed and recording
 * the actual end date.
 *
 * @param activity the activity to complete (must not be null)
 * @throws IllegalArgumentException if activity is null or already completed
 */
@Transactional
public void completeActivity(CActivity activity) {
    Objects.requireNonNull(activity, "Activity cannot be null");
    
    if (activity.isCompleted()) {
        throw new IllegalArgumentException("Activity is already completed");
    }
    
    // Set completion status
    CProjectItemStatus completedStatus = statusService.findCompletedStatus(
        activity.getProject());
    activity.setStatus(completedStatus);
    
    // Record actual end date
    activity.setActualEndDate(LocalDate.now(clock));
    activity.updateLastModified();
    
    save(activity);
    
    LOGGER.info("Activity {} marked as completed", activity.getName());
}
```

## Grid Column Standards (MANDATORY)

### Rule: Always Use Entity-Enabled Column Methods

When adding grid columns for entity references (status, assigned user, approved by, participants, etc.), **ALWAYS** use `CGrid`'s entity column helper methods instead of manual rendering.

**❌ INCORRECT - Manual rendering:**
```java
// DON'T do this - manual name extraction
grid.addColumn(activity -> {
    CStatus status = activity.getStatus();
    return status != null ? status.getName() : "";
}).setHeader("Status");

grid.addColumn(activity -> {
    CUser user = activity.getAssignedTo();
    return user != null ? user.getFullName() : "";
}).setHeader("Assigned To");

grid.addColumn(activity -> {
    List<CUser> participants = activity.getParticipants();
    if (participants == null || participants.isEmpty()) {
        return "No participants";
    }
    return participants.stream()
        .map(CUser::getFullName)
        .collect(Collectors.joining(", "));
}).setHeader("Participants");
```

**✅ CORRECT - Use entity column helper methods:**
```java
// Single entity reference - use addColumnEntityNamed()
grid.addColumnEntityNamed(CActivity::getStatus, "Status");
grid.addColumnEntityNamed(CActivity::getAssignedTo, "Assigned To");
grid.addColumnEntityNamed(CActivity::getApprovedBy, "Approved By");
grid.addColumnEntityNamed(CActivity::getCreatedBy, "Created By");
grid.addColumnEntityNamed(CMeeting::getMeetingType, "Type");
grid.addColumnEntityNamed(CRisk::getRiskLevel, "Level");

// Collection of entities - use addColumnEntityCollection()
grid.addColumnEntityCollection(CActivity::getParticipants, "Participants");
grid.addColumnEntityCollection(CActivity::getTags, "Tags");
grid.addColumnEntityCollection(CProject::getTeamMembers, "Team");

// Custom rendering with CLabelEntity - use addEntityColumn()
grid.addEntityColumn(CActivity::getStatus, "Status", "status", CStatus.class);
```

### Available CGrid Entity Column Methods

| Method | Signature | Use For | Benefits |
|--------|-----------|---------|----------|
| `addColumnEntityNamed()` | `addColumnEntityNamed(ValueProvider<T, CEntityDB<?>>, String)` | Single entity reference | Auto name extraction, null handling, lazy loading safe |
| `addColumnEntityCollection()` | `addColumnEntityCollection(ValueProvider<T, Collection<?>>, String)` | Collection of entities | Comma-separated names, empty collection handling, lazy loading safe |
| `addEntityColumn()` | `addEntityColumn(ValueProvider<T, ?>, String, String, Class<?>)` | Custom entity rendering | Uses CLabelEntity for rich formatting, color/icon support |

### Benefits of Entity Column Methods

1. **Consistent Rendering**: All entity columns look and behave the same across the application
2. **Null Safety**: Built-in null checks prevent NullPointerException
3. **Lazy Loading Handling**: Detects uninitialized collections and displays fallback text
4. **Name Extraction**: Automatically extracts names from `CEntityNamed` entities
5. **Collection Formatting**: Comma-separated list with proper empty handling
6. **Error Resilience**: Gracefully handles errors with fallback display
7. **Less Code**: One line instead of 5-10 lines of manual rendering
8. **Maintainability**: Changes to entity rendering update all grids automatically

### When to Use Each Method

**Use `addColumnEntityNamed()` when:**
- Field type is `CEntityDB<?>` or extends it
- You need simple name display
- Entity implements `CEntityNamed` or has a name property
- Examples: status, assignedTo, approvedBy, createdBy, project, type, category

**Use `addColumnEntityCollection()` when:**
- Field type is `Collection<? extends CEntityDB<?>>`
- You want comma-separated names
- Examples: participants, tags, teamMembers, approvers, watchers

**Use `addEntityColumn()` when:**
- You need custom rendering with colors/icons
- Entity has visual attributes (color, icon)
- You want rich formatted display using `CLabelEntity`
- Examples: status with color indicator, priority with icon

### Implementation Details

**`addColumnEntityNamed()` internals:**
```java
// Automatically extracts entity name
final ValueProvider<EntityClass, String> nameProvider = entity -> {
    final CEntityDB<?> ref = valueProvider.apply(entity);
    return ref == null ? "" : entityName(ref);  // Uses CGrid.entityName()
};
```

**`addColumnEntityCollection()` internals:**
```java
// Handles null, uninitialized, and empty collections
- Checks Hibernate.isInitialized() to avoid lazy loading exceptions
- Returns "No {header}" for empty/uninitialized collections
- Extracts names from CEntityDB items
- Joins names with comma separator
- Falls back to toString() for non-entity items
```

### Validation Checklist

Before committing code with grid columns:

- [ ] ✅ All entity reference columns use `addColumnEntityNamed()`
- [ ] ✅ All entity collection columns use `addColumnEntityCollection()`
- [ ] ✅ No manual `addColumn()` calls for entities
- [ ] ✅ No manual name extraction in lambda expressions
- [ ] ✅ No manual null checks for entity references
- [ ] ✅ No manual collection iteration for name lists

### Migration Example

**Before (manual rendering):**
```java
@Override
public void configureGrid(final CGrid<CActivity> grid) {
    grid.addColumn(a -> a.getName()).setHeader("Name");
    
    // Manual entity rendering (WRONG)
    grid.addColumn(a -> {
        CStatus status = a.getStatus();
        return status != null ? status.getName() : "";
    }).setHeader("Status").setWidth("150px");
    
    // Manual collection rendering (WRONG)
    grid.addColumn(a -> {
        List<CUser> users = a.getParticipants();
        if (users == null || users.isEmpty()) return "None";
        return users.stream().map(CUser::getName).collect(Collectors.joining(", "));
    }).setHeader("Participants");
}
```

**After (entity column methods):**
```java
@Override
public void configureGrid(final CGrid<CActivity> grid) {
    grid.addShortTextColumn(CActivity::getName, "Name", "name");
    
    // Use entity column helper (CORRECT)
    grid.addColumnEntityNamed(CActivity::getStatus, "Status");
    
    // Use collection helper (CORRECT)
    grid.addColumnEntityCollection(CActivity::getParticipants, "Participants");
}
```

**Result:**
- Lines of code: 15 → 6 (60% reduction)
- Null safety: Manual → Automatic
- Lazy loading: Manual → Automatic
- Error handling: None → Built-in

### Related Documentation

- [CGrid Configuration Patterns](cgrid-configuration-patterns.md) - Complete grid configuration guide
- [View Layer Patterns](view-layer-patterns.md#grid-configuration-patterns) - Grid patterns in views
- [Component and Utility Reference](component-utility-reference.md) - All available CGrid methods

---

## Code Formatting

### Import Organization (Mandatory)

**ALWAYS use import statements instead of fully-qualified class names:**

#### ✅ Correct
```java
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.api.projects.domain.CProject;
import java.util.List;
import java.time.LocalDate;

public class CActivityService {
    public CActivity createActivity(String name, CProject project) {
        CActivity activity = new CActivity(name, project);
        return save(activity);
    }
    
    public List<CActivity> findActivitiesByDate(LocalDate date) {
        return repository.findByDate(date);
    }
}
```

#### ❌ Incorrect
```java
public class CActivityService {
    public tech.derbent.app.activities.domain.CActivity createActivity(
            String name, tech.derbent.api.projects.domain.CProject project) {
        tech.derbent.app.activities.domain.CActivity activity = 
            new CActivity(name, project);
        return save(activity);
    }
    
    // WRONG: Using fully-qualified names for java.* classes
    public java.util.List<CActivity> findActivitiesByDate(java.time.LocalDate date) {
        return repository.findByDate(date);
    }
}
```

**Benefits**:
- Improved code readability
- Easier code maintenance
- Better IDE support and refactoring
- Reduced line length
- Consistent with Java best practices
- Eliminates visual clutter from long package names

**Rule**: Full package paths (tech.derbent.*, java.*, org.*, etc.) should ONLY appear in import statements at the top of the file. Never use fully-qualified class names in method signatures, field declarations, local variables, or method bodies.

**Exceptions**:
- When there are name conflicts between classes (e.g., `java.util.Date` vs `java.sql.Date`)
- In rare cases where a class is used only once and importing would add clutter (very rare)

### Sample Data Initialization Pattern (Mandatory)

**ALWAYS use the `initializeSample()` pattern in InitializerService classes:**

#### Pattern Structure
```java
public class CEntityTypeInitializerService extends CInitializerServiceBase {
    
    private static final Class<?> clazz = CEntityType.class;
    
    // Standard initialize method for UI setup
    public static void initialize(final CProject project, 
            final CGridEntityService gridEntityService,
            final CDetailSectionService detailSectionService, 
            final CPageEntityService pageEntityService) throws Exception {
        // Setup grid and detail views...
    }
    
    // Sample data initialization method (REQUIRED)
    public static void initializeSample(final CProject project, 
            final boolean minimal) throws Exception {
        final String[][] nameAndDescriptions = {
            { "Type 1", "Description for type 1" },
            { "Type 2", "Description for type 2" },
            { "Type 3", "Description for type 3" }
        };
        initializeProjectEntity(nameAndDescriptions,
            (CEntityOfProjectService<?>) CSpringContext.getBean(
                CEntityRegistry.getServiceClassForEntity(clazz)), 
            project, minimal, null);
    }
}
```

#### Implementation Rules

1. **Naming Convention**: Method must be named `initializeSample`
2. **Parameters**: `(final CProject project, final boolean minimal)`
3. **Data Format**: Use String[][] for name and description pairs
4. **Base Method**: Call `initializeProjectEntity()` from `CInitializerServiceBase`
5. **Minimal Mode**: Respect the `minimal` parameter to create reduced datasets

#### Sample Data Customization with Consumer

For complex entities requiring additional configuration:

```java
public static void initializeSample(final CProject project, 
        final boolean minimal) throws Exception {
    final String[][] nameAndDescriptions = {
        { "Entity 1", "Description 1" },
        { "Entity 2", "Description 2" }
    };
    
    // Use consumer for custom initialization
    initializeProjectEntity(nameAndDescriptions,
        (CEntityOfProjectService<?>) CSpringContext.getBean(
            CEntityRegistry.getServiceClassForEntity(clazz)), 
        project, minimal, 
        entity -> {
            // Custom configuration
            entity.setCustomField(someValue);
            entity.setRelatedEntity(getRelatedEntity());
        });
}
```

#### Integration with CDataInitializer

Replace manual initialization calls with InitializerService calls:

```java
// ❌ OLD Pattern - Manual initialization in CDataInitializer
private void initializeSampleEntityTypes(CProject project, boolean minimal) {
    final String[][] types = { {"Type1", "Desc1"}, {"Type2", "Desc2"} };
    final CEntityTypeService service = CSpringContext.getBean(CEntityTypeService.class);
    initializeType(types, service, project, minimal);
}

// ✅ NEW Pattern - Delegate to InitializerService
CEntityTypeInitializerService.initializeSample(project, minimal);
```

### Spotless Configuration

The project uses Spotless for automatic code formatting:

```bash
# Always use the system Maven executable (do not use ./mvnw)
# Apply formatting
mvn spotless:apply

# Check formatting
mvn spotless:check
```

### Key Formatting Rules

- **Indentation**: 4 spaces (no tabs)
- **Line length**: 140 characters (soft limit)
- **Braces**: Always use braces, even for single-line blocks
- **Import organization**: Remove unused imports, organize alphabetically
- **Final keyword**: Use `final` for method parameters and local variables

```java
// ✅ CORRECT
public void processActivity(final CActivity activity) {
    final String name = activity.getName();
    if (name != null) {
        doSomething(name);
    }
}

// ❌ INCORRECT - No final, no braces
public void processActivity(CActivity activity) {
    String name = activity.getName();
    if (name != null)
        doSomething(name);
}
```

## Security Best Practices

### Method-Level Security

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

### View-Level Security

```java
@Route("admin/settings")
@PageTitle("Admin Settings")
@RolesAllowed("ADMIN")
public class CAdminSettingsView extends CAbstractPage {
    // Admin-only view
}
```

### Input Validation

Always validate user input:

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
    
    // Check for duplicates
    Optional<CActivity> existing = findByNameAndProject(name, project);
    if (existing.isPresent()) {
        throw new IllegalArgumentException(
            "Activity with name '" + name + "' already exists");
    }
    
    // Create entity
    CActivity activity = new CActivity(name, project);
    initializeNewEntity(activity);
    return save(activity);
}
```

## Testing Standards

### Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class CActivityServiceTest {
    
    @Mock
    private IActivityRepository repository;
    
    @Mock
    private Clock clock;
    
    @InjectMocks
    private CActivityService service;
    
    @Test
    void testCreateActivity_Success() {
        // Given
        String name = "Test Activity";
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        
        // When
        CActivity result = service.createActivity(name);
        
        // Then
        assertNotNull(result);
        assertEquals(name, result.getName());
        verify(repository).save(any(CActivity.class));
    }
    
    @Test
    void testCreateActivity_NullName_ThrowsException() {
        // When/Then
        assertThrows(IllegalArgumentException.class,
                   () -> service.createActivity(null));
    }
}
```

### Integration Tests

```java
@SpringBootTest
@Transactional
class CActivityServiceIntegrationTest {
    
    @Autowired
    private CActivityService service;
    
    @Autowired
    private CProjectService projectService;
    
    @Test
    void testCompleteActivityWorkflow() {
        // Given
        CProject project = projectService.createProject("Test Project");
        CActivity activity = service.createActivity("Test", project);
        
        // When
        service.completeActivity(activity);
        
        // Then
        CActivity saved = service.findById(activity.getId()).orElseThrow();
        assertTrue(saved.isCompleted());
        assertNotNull(saved.getActualEndDate());
    }
}
```

## Performance Guidelines

### Query Optimization

```java
// Use JOIN FETCH to avoid N+1 queries
@Query("SELECT a FROM CActivity a " +
       "JOIN FETCH a.status " +
       "JOIN FETCH a.type " +
       "WHERE a.project = :project")
List<CActivity> findByProjectWithRelations(@Param("project") CProject project);

// Use pagination for large datasets
@Transactional(readOnly = true)
public Page<CActivity> findAll(Pageable pageable) {
    return repository.findAll(pageable);
}
```

- **Page-view list queries**: every `IEntityOfProjectRepository` must provide `listByProjectForPageView(...)` and every
  `IEntityOfCompanyRepository` must provide `listByCompanyForPageView(...)` with entity-specific `LEFT JOIN FETCH` clauses so grids/pages load
  fully-initialized entities (all relations needed for display).

### Lazy Loading

**MANDATORY**: All repository queries MUST eagerly fetch lazy collections when entities will be used in UI.

For entities implementing `IHasAttachments`, `IHasComments`, and/or `IHasLinks`, **ALL queries MUST include**:

```java
LEFT JOIN FETCH entity.attachments  // if implements IHasAttachments
LEFT JOIN FETCH entity.comments     // if implements IHasComments
LEFT JOIN FETCH entity.links        // if implements IHasLinks
```

See [Lazy Loading Best Practices](LAZY_LOADING_BEST_PRACTICES.md) for complete details.

**Example**:
```java
@Override
@Query("""
    SELECT u FROM #{#entityName} u
    LEFT JOIN FETCH u.company
    LEFT JOIN FETCH u.attachments
    LEFT JOIN FETCH u.comments
    LEFT JOIN FETCH u.links
    WHERE u.id = :id
    """)
Optional<CUser> findById(@Param("id") Long id);
```

**Pattern for all entities**:
```java
// Use LAZY for collections in entity definition
@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
private Set<CAttachment> attachments = new HashSet<>();

@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
private Set<CComment> comments = new HashSet<>();

@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
private Set<CLink> links = new HashSet<>();

// Use LAZY for optional relationships
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "parent_id")
private CActivity parent;

// Use EAGER for required, frequently accessed relationships
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "project_id", nullable = false)
private CProject project;
```

**Important Notes**:
- ILinkable has been deprecated and merged into IHasLinks (bidirectional nature of links)
- Entities implementing IHasLinks can both HAVE links and BE linked to
- Links are stored unidirectionally via @OneToMany with @JoinColumn (no back-reference in CLink)

## Child Entity Repository Standards (Master-Detail Pattern)

### 5. **Consistent Naming for Child Entity Repositories** (Mandatory)

When creating repositories for child entities (entities that belong to a parent/master entity), use consistent method naming to ensure uniformity across the project.

#### Standard Method Names

| Method Pattern | Purpose | Example |
|----------------|---------|---------|
| `findByMaster(M master)` | Find all children by master entity | `findByMaster(CDetailSection master)` |
| `findByMasterId(Long id)` | Find all children by master ID | `findByMasterId(Long sprintId)` |
| `findActiveByMaster(M master)` | Find active children by master | `findActiveByMaster(CDetailSection master)` |
| `countByMaster(M master)` | Count children by master entity | `countByMaster(CDetailSection master)` |
| `getNextItemOrder(M master)` | Get next order number for new items | `getNextItemOrder(CDetailSection master)` |
| `findByMasterIdAndItemId(Long, Long)` | Find specific child by both IDs | `findByMasterIdAndItemId(Long sprintId, Long itemId)` |

#### ✅ Correct Repository Interface Example

```java
public interface ISprintItemRepository extends IAbstractRepository<CSprintItem> {

    /** Find all sprint items for a master (sprint), ordered by itemOrder. */
    @Query("SELECT e FROM #{#entityName} e WHERE e.sprint = :master ORDER BY e.itemOrder ASC")
    List<CSprintItem> findByMaster(@Param("master") CSprint master);

    /** Find all sprint items by master ID, ordered by itemOrder. */
    @Query("SELECT e FROM #{#entityName} e WHERE e.sprint.id = :masterId ORDER BY e.itemOrder ASC")
    List<CSprintItem> findByMasterId(@Param("masterId") Long masterId);

    /** Count children by master. */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.sprint = :master")
    Long countByMaster(@Param("master") CSprint master);

    /** Get next item order. */
    @Query("SELECT COALESCE(MAX(e.itemOrder), 0) + 1 FROM #{#entityName} e WHERE e.sprint = :master")
    Integer getNextItemOrder(@Param("master") CSprint master);
}
```

#### ❌ Incorrect - Entity-Specific Naming

```java
// DON'T use entity-specific names
List<CSprintItem> findBySprintId(Long sprintId);        // ❌ Use findByMasterId
List<CSprintItem> findBySprintIdOrderByItemOrderAsc();  // ❌ Use findByMasterId (already ordered)
Long countByScreen(CDetailSection screen);              // ❌ Use countByMaster
Integer getNextitemOrder(CDetailSection section);       // ❌ Use getNextItemOrder (camelCase!)
```

### JPQL Query Standards

#### Entity Alias Convention
Always use `e` as the entity alias in JPQL queries for consistency:

```java
// ✅ Correct - use 'e' as entity alias
@Query("SELECT e FROM #{#entityName} e WHERE e.master = :master ORDER BY e.itemOrder ASC")

// ❌ Incorrect - inconsistent aliases
@Query("SELECT a FROM #{#entityName} a WHERE a.master = :master")  // Don't use 'a'
@Query("SELECT si FROM #{#entityName} si WHERE si.sprint = :sprint")  // Don't use 'si'
```

#### Parameter Naming Convention
Use generic names like `master` instead of entity-specific names:

```java
// ✅ Correct - generic parameter name
@Query("SELECT e FROM #{#entityName} e WHERE e.detailSection = :master")
List<CDetailLines> findByMaster(@Param("master") CDetailSection master);

// ❌ Incorrect - entity-specific parameter name
@Query("SELECT e FROM #{#entityName} e WHERE e.detailSection = :detailSection")
List<CDetailLines> findByMaster(@Param("detailSection") CDetailSection master);
```

#### JOIN FETCH Syntax
Always use explicit `e.` prefix for JOINed fields:

```java
// ✅ Correct
@Query("SELECT e FROM #{#entityName} e LEFT JOIN FETCH e.detailSection WHERE e.detailSection = :master")

// ❌ Incorrect - missing entity prefix
@Query("SELECT e FROM #{#entityName} e LEFT JOIN FETCH detailSection WHERE e.detailSection = :master")
```

### Service Method Standards

Services should provide consistent methods that match repository patterns:

```java
@Service
public class CSprintItemService extends CAbstractService<CSprintItem> 
        implements IOrderedEntityService<CSprintItem> {
    
    /** Get typed repository. */
    private ISprintItemRepository getTypedRepository() {
        return (ISprintItemRepository) repository;
    }
    
    /** Find by master entity. */
    public List<CSprintItem> findByMaster(CSprint master) {
        Objects.requireNonNull(master, "Master cannot be null");
        if (master.getId() == null) return List.of();
        return getTypedRepository().findByMaster(master);
    }
    
    /** Find by master ID. */
    public List<CSprintItem> findByMasterId(Long masterId) {
        Objects.requireNonNull(masterId, "Master ID cannot be null");
        return getTypedRepository().findByMasterId(masterId);
    }
    
    /** Find by master ID with eager loading of transient data. */
    public List<CSprintItem> findByMasterIdWithItems(Long masterId) {
        List<CSprintItem> items = findByMasterId(masterId);
        loadItems(items);  // Load transient data
        return items;
    }
}
```

## Related Documentation

### Architecture and Design
- [UI, CSS, and Layout Coding Standards](ui-css-coding-standards.md) - **UI component styling and layout patterns**
- [Entity Inheritance Patterns](entity-inheritance-patterns.md)
- [Service Layer Patterns](service-layer-patterns.md)
- [View Layer Patterns](view-layer-patterns.md)
- [Multi-User Singleton Advisory](multi-user-singleton-advisory.md) - **CRITICAL for service development**

### Development Guidelines
- [Component Coding Standards](../development/component-coding-standards.md) - Component development rules
- [GitHub Copilot Guidelines](../development/copilot-guidelines.md) - AI agent usage patterns
- [Multi-User Development Checklist](../development/multi-user-development-checklist.md)

## Multi-User Web Application Patterns (CRITICAL)

### 4. **Stateless Service Pattern** (Mandatory for Multi-User Safety)

All services MUST be stateless to support multiple concurrent users.

#### ✅ Correct: Stateless Service

```java
@Service
public class CActivityService extends CEntityOfProjectService<CActivity> {
    // ✅ GOOD: Only dependencies, no user-specific state
    private final IActivityRepository repository;
    private final Clock clock;
    private final ISessionService sessionService;
    
    public CActivityService(IActivityRepository repository, Clock clock, ISessionService sessionService) {
        super(repository, clock, sessionService);
        this.repository = repository;
        this.sessionService = sessionService;
    }
    
    @Transactional(readOnly = true)
    public List<CActivity> getUserActivities() {
        // ✅ GOOD: Get user from session each time
        CUser currentUser = sessionService.getActiveUser()
            .orElseThrow(() -> new IllegalStateException("No active user"));
        return repository.findByUserId(currentUser.getId());
    }
}
```

#### ❌ Incorrect: Service with User-Specific State

```java
@Service
public class CBadActivityService {
    // ❌ WRONG: User state stored in service (shared across ALL users!)
    private CUser currentUser;
    private List<CActivity> cachedActivities;
    
    public void setCurrentUser(CUser user) {
        // ❌ WRONG: This will be overwritten by other users' requests
        this.currentUser = user;
    }
    
    public List<CActivity> getActivities() {
        // ❌ WRONG: Returns wrong data when multiple users access simultaneously
        return cachedActivities;
    }
}
```

### Service Field Rules

| Field Type | Allowed? | Example | Notes |
|------------|----------|---------|-------|
| Repository dependency | ✅ Yes | `private final IUserRepository repository` | Injected via constructor |
| Clock dependency | ✅ Yes | `private final Clock clock` | Injected via constructor |
| Session service | ✅ Yes | `private final ISessionService sessionService` | Injected via constructor |
| Logger | ✅ Yes | `private static final Logger LOGGER` | Thread-safe, immutable |
| Constants | ✅ Yes | `private static final String MENU_TITLE` | Immutable |
| User context | ❌ No | `private CUser currentUser` | **WRONG! Shared across users** |
| User data cache | ❌ No | `private List<CEntity> userCache` | **WRONG! Shared across users** |
| Mutable static collections | ❌ No | `private static Map<Long, CUser> cache` | **WRONG! Not thread-safe** |

### Session State Management Rules

#### ✅ DO: Use VaadinSession for User-Specific State

```java
@Service
public class CPreferenceService {
    private static final String PREFERENCE_KEY = "userPreference";
    
    public void savePreference(String preference) {
        // ✅ GOOD: Each user has their own VaadinSession
        VaadinSession.getCurrent().setAttribute(PREFERENCE_KEY, preference);
    }
    
    public String getPreference() {
        // ✅ GOOD: Retrieved from user's own session
        return (String) VaadinSession.getCurrent().getAttribute(PREFERENCE_KEY);
    }
}
```

#### ✅ DO: Retrieve Context from Session Per-Request

```java
@Service
public class CActivityService extends CAbstractService<CActivity> {
    
    @Transactional(readOnly = true)
    public List<CActivity> findAll() {
        // ✅ GOOD: Get company from session each time method is called
        CCompany currentCompany = getCurrentCompany();
        return repository.findByCompany_Id(currentCompany.getId());
    }
    
    private CCompany getCurrentCompany() {
        Objects.requireNonNull(sessionService, "Session service required");
        CCompany company = sessionService.getCurrentCompany();
        Objects.requireNonNull(company, "No active company");
        return company;
    }
}
```

#### ❌ DON'T: Cache User Context in Service

```java
@Service
public class CBadService {
    private CCompany cachedCompany;  // ❌ WRONG!
    
    public void initializeService() {
        // ❌ WRONG: Caching company in service instance
        this.cachedCompany = sessionService.getCurrentCompany();
    }
    
    public List<CActivity> findAll() {
        // ❌ WRONG: Will return wrong company for other users
        return repository.findByCompany_Id(cachedCompany.getId());
    }
}
```

### Quick Multi-User Safety Check

Before committing service code, verify:

1. **No instance fields storing user data** ✓
2. **No instance fields storing collections of user data** ✓
3. **No static mutable collections** ✓
4. **All user context retrieved from sessionService** ✓
5. **No caching of user-specific data in service** ✓

### For Detailed Patterns and Examples

See **[Multi-User Singleton Advisory](multi-user-singleton-advisory.md)** for:
- Complete do's and don'ts
- Code examples
- Migration guides
- Testing strategies
- Debugging tips

### Development Checklist

Use **[Multi-User Development Checklist](../development/multi-user-development-checklist.md)** when:
- Creating new services
- Modifying existing services
- Reviewing pull requests
- Testing multi-user scenarios


## Playwright Test Support Patterns

### CPageTestAuxillary Pattern

**Purpose**: Provide a centralized navigation hub for Playwright automated testing with metadata support.

**When to Use**:
- Creating pages that serve as test navigation points
- Implementing dynamic page discovery for automated tests
- Passing metadata from application to Playwright tests

**Implementation Requirements**:

#### 1. **Button ID Generation** (Mandatory)
All test navigation buttons MUST have unique, stable IDs:

```java
String buttonId = generateButtonId(title, index);
routeButton.setId(buttonId);

private String generateButtonId(String title, int buttonIndex) {
    String sanitized = title.toLowerCase()
        .replaceAll("[^a-z0-9]+", "-")
        .replaceAll("(^-|-$)", "");
    return "test-aux-btn-" + sanitized + "-" + buttonIndex;
}
```

**Format**: `test-aux-btn-{sanitized-title}-{index}`

#### 2. **Data Attributes** (Recommended)
Add metadata attributes for Playwright consumption:

```java
routeButton.getElement().setAttribute("data-route", routeEntry.route);
routeButton.getElement().setAttribute("data-title", routeEntry.title);
routeButton.getElement().setAttribute("data-button-index", String.valueOf(buttonIndex));
```

#### 3. **Metadata Div** (Recommended)
Include a hidden metadata div for test automation:

```java
CDiv metadataDiv = new CDiv();
metadataDiv.setId("test-auxillary-metadata");
metadataDiv.getStyle().set("display", "none");
metadataDiv.getElement().setAttribute("data-button-count", 
    String.valueOf(pageTestAuxillaryService.getRoutes().size()));
add(metadataDiv);
```

**Benefits**:
- Enables dynamic button discovery in tests
- Provides stable selectors for Playwright
- Allows test metadata without visible UI changes
- Supports changing button counts without test updates

#### 4. **Generic Page Testing Pattern**

**Test Structure**:
```java
@Test
void testAllPages() {
    // 1. Login
    loginToApplication();
    
    // 2. Navigate to test page
    navigateToTestAuxillaryPage();
    
    // 3. Discover buttons dynamically
    List<ButtonInfo> buttons = discoverNavigationButtons();
    
    // 4. Test each button
    for (ButtonInfo button : buttons) {
        testNavigationButton(button);
    }
}
```

**Check Functions** (Generic Validators):
```java
private boolean checkGridExists() { ... }
private boolean checkGridHasData() { ... }
private boolean checkCrudToolbarExists() { ... }
```

**Test Functions** (Conditional):
```java
if (checkGridExists()) {
    runGridTests(pageName);
}
if (checkCrudToolbarExists()) {
    runCrudToolbarTests(pageName);
}
```

**Benefits**:
- Tests ALL pages without hardcoding
- Adapts to page content automatically
- Reusable check functions
- Fast execution with reasonable timeouts
- Complete coverage guarantee

#### Example Implementation

```java
@Route("cpagetestauxillary")
public class CPageTestAuxillary extends Main {
    
    protected void prepareRoutes() {
        // Create metadata div
        CDiv metadataDiv = new CDiv();
        metadataDiv.setId("test-auxillary-metadata");
        metadataDiv.getStyle().set("display", "none");
        metadataDiv.getElement().setAttribute("data-button-count", 
            String.valueOf(routes.size()));
        add(metadataDiv);
        
        // Create buttons with IDs and metadata
        int buttonIndex = 0;
        for (var routeEntry : routes) {
            CButton button = new CButton(routeEntry.title, icon, e -> {
                getUI().ifPresent(ui -> ui.navigate(routeEntry.route));
            });
            
            // Set ID and attributes
            String buttonId = "test-aux-btn-" + 
                sanitize(routeEntry.title) + "-" + buttonIndex;
            button.setId(buttonId);
            button.getElement().setAttribute("data-route", routeEntry.route);
            button.getElement().setAttribute("data-title", routeEntry.title);
            button.getElement().setAttribute("data-button-index", 
                String.valueOf(buttonIndex));
            
            pageLinksLayout.add(button);
            buttonIndex++;
        }
    }
}
```

**Reference**: See `docs/testing/comprehensive-page-testing.md` for complete guide.

### Context-aware random selection

- Always prefer context-bound helpers like `getRandom(project)` or `getRandom(company)` instead of parameterless randomizers to avoid pulling entities from other tenants.
- Seed data and workflow/type defaults must validate tenant ownership (`Check.isSameCompany`) before binding statuses, roles, or workflows together.
- When no context is available, treat it as a bug: fail fast rather than silently choosing a global random record.
- Initializers that attach workflows or statuses must pass explicit project/company context into `getRandom(...)` and assert the workflow/status is non-null before saving the entity.

## Kanban Board Validation Rules (CRITICAL)

### Rule: Status Uniqueness Across Columns (MANDATORY)

**Each status must be mapped to AT MOST ONE column within a kanban line.**

#### Why This Matters

If a status is mapped to multiple columns (status overlap), it creates:
1. **Display Ambiguity**: System cannot determine which column should display items with that status
2. **Drag-Drop Errors**: Incorrect column assignment during status changes
3. **Data Inconsistency**: Items could conceptually appear in multiple columns

#### Implementation Requirements

**Validation Layer 1: Fail-Fast Check** (Primary Defense)
```java
// In CKanbanColumnService.validateStatusUniqueness()
// Runs during validateEntity() before save
// Throws CValidationException immediately if overlap detected
// Prevents invalid configuration from reaching database

private void validateStatusUniqueness(final CKanbanColumn entity) {
    // Check if any status in this column already exists in another column
    // Build map of statusId -> columnName for all other columns
    // If overlap found: throw CValidationException with clear error message
}
```

**Validation Layer 2: Automatic Cleanup** (Safeguard)
```java
// In CKanbanColumnService.applyStatusAndDefaultConstraints()
// Runs after save as defensive programming
// Removes overlapping statuses from other columns automatically
// Logs all removals for debugging

private void applyStatusAndDefaultConstraints(final CKanbanColumn saved) {
    // For each status in saved column:
    //   Remove that status from all other columns in the line
    //   Log removal count for debugging
}
```

#### Error Message Format

When validation fails, provide clear, actionable error:

```
Status overlap detected in kanban line 'Sprint Board': 
The following statuses are already mapped to other columns: 
'In Progress' (already in column 'Active Tasks'), 
'Testing' (already in column 'QA Column'). 
Each status must be mapped to exactly one column to avoid 
ambiguity in kanban board display.
```

#### Debug Logging Requirements

All kanban column operations must include detailed logging:

```java
// Status mapping
LOGGER.debug("[KanbanValidation] Mapping status id {}:{} to column id {}", 
    statusId, statusName, columnId);

// Overlap detection
LOGGER.warn("[KanbanValidation] Status overlap detected: status '{}' (ID: {}) " +
    "is mapped to both column '{}' and column '{}'", 
    statusName, statusId, existingColumnName, newColumnName);

// Automatic cleanup
LOGGER.info("[KanbanValidation] Enforced status uniqueness: removed {} " +
    "overlapping status mapping(s) from other columns in kanban line '{}'",
    removalCount, lineName);

// Validation success
LOGGER.debug("[KanbanValidation] Status uniqueness validated successfully " +
    "for column '{}' with {} status(es)", columnName, statusCount);
```

### Rule: Single Default Column per Kanban Line

**Only one column can be marked as defaultColumn=true within a kanban line.**

The default column serves as a fallback for items whose status is not explicitly mapped to any column.

#### Enforcement

When saving a column with `defaultColumn=true`:
1. Automatically remove `defaultColumn=true` from all other columns
2. Log the change for debugging

```java
if (isDefaultColumn && column.getDefaultColumn()) {
    LOGGER.debug("[KanbanValidation] Removing default flag from column '{}' " +
        "(ID: {}) because column '{}' (ID: {}) is now the default",
        column.getName(), column.getId(), saved.getName(), saved.getId());
    column.setDefaultColumn(false);
}
```

## Status Initialization and Management Rules (CRITICAL - MANDATORY)

### Rule: Status Must NEVER Be Null for Workflow Entities

**All entities implementing `IHasStatusAndWorkflow` MUST maintain a valid status at all times after initialization.**

This rule is enforced at multiple levels:
1. **Interface level**: `IHasStatusAndWorkflow.setStatus()` default implementation prevents null assignment
2. **Domain level**: Entity `setStatus()` overrides enforce null checks (e.g., `CProjectItem`, `CProject`)
3. **Service level**: `initializeNewEntity()` methods must assign initial status

#### Why This Matters

Setting status to null breaks workflow state management:
- Workflow transitions become undefined
- Status validation fails
- UI components crash when rendering null status
- Kanban boards cannot determine column placement

### Rule: Use IHasStatusAndWorkflowService.initializeNewEntity

**ALWAYS use `IHasStatusAndWorkflowService.initializeNewEntity()` to initialize entities with workflow and status.**

This utility method:
1. Assigns entity type from available types in company
2. Resolves workflow from entity type
3. Gets initial status from workflow status relations
4. Assigns initial status to entity

#### ✅ CORRECT - Using IHasStatusAndWorkflowService

```java
@Override
public void initializeNewEntity(final CActivity entity) {
    super.initializeNewEntity(entity);
    LOGGER.debug("Initializing new activity entity");
    
    // Get current project from session
    final CProject currentProject = sessionService.getActiveProject()
            .orElseThrow(() -> new CInitializationException("No active project"));
    
    // Initialize workflow-based status and type (CRITICAL - ALWAYS DO THIS)
    IHasStatusAndWorkflowService.initializeNewEntity(
            entity, currentProject, entityTypeService, projectItemStatusService);
    
    // Initialize activity-specific fields
    entity.setPriority(activityPriorityService.listByCompany(company).get(0));
    // ... other fields
}
```

#### ❌ INCORRECT - Manual status assignment

```java
// WRONG - Don't manually assign status
@Override
public void initializeNewEntity(final CActivity entity) {
    super.initializeNewEntity(entity);
    
    // WRONG: Manual status lookup bypasses workflow logic
    final List<CProjectItemStatus> statuses = projectItemStatusService.listByCompany(company);
    entity.setStatus(statuses.get(0));  // Wrong: Ignores workflow initial status!
}
```

### Rule: Removed Method - Do Not Use assignStatusToActivity

**The method `CProjectItemStatusService.assignStatusToActivity()` has been REMOVED as redundant.**

Use the proper initialization pattern instead:

#### ✅ CORRECT - Workflow-based initialization in data initializers

```java
// For manually created entities in data initializers
final CActivity activity = new CActivity("Activity Name", project);
activity.setEntityType(activityType);
activity.setAssignedTo(user);

// Initialize status using workflow (CORRECT PATTERN)
if (activityType != null && activityType.getWorkflow() != null) {
    final List<CProjectItemStatus> initialStatuses = 
            projectItemStatusService.getValidNextStatuses(activity);
    if (!initialStatuses.isEmpty()) {
        activity.setStatus(initialStatuses.get(0));
    }
}

activityService.save(activity);
```

#### ❌ INCORRECT - Using removed method

```java
// WRONG - This method no longer exists
projectItemStatusService.assignStatusToActivity(activity);  // COMPILATION ERROR
```

### Rule: Workflow Initial Status Marking

**Workflows MUST have at least one status marked as initial status (`CWorkflowStatusRelation.initialStatus = true`).**

The initial status is automatically assigned to new entities when they are created:
1. Service calls `IHasStatusAndWorkflowService.initializeNewEntity()`
2. Method calls `CProjectItemStatusService.getInitialStatusFromWorkflow()`
3. Returns status marked with `initialStatus = true` in workflow relations
4. Falls back to first status in workflow if no initial status marked

#### Implementation in Workflow Status Relations

```java
// When creating workflow status relations
final CWorkflowStatusRelation relation = new CWorkflowStatusRelation();
relation.setWorkflow(workflow);
relation.setFromStatus(null);  // null means "new entity"
relation.setToStatus(toDoStatus);
relation.setInitialStatus(true);  // Mark as initial status for new entities
workflowStatusRelationService.save(relation);
```

### Rule: Status Changes Must Follow Workflow Transitions

**Status can only be changed to values returned by `getValidNextStatuses()`.**

Never bypass workflow validation:

#### ✅ CORRECT - Using workflow transitions

```java
// Get valid next statuses from workflow
final List<CProjectItemStatus> validStatuses = 
        projectItemStatusService.getValidNextStatuses(entity);

// Check if target status is valid
if (validStatuses.contains(newStatus)) {
    entity.setStatus(newStatus);
    service.save(entity);
} else {
    throw new IllegalStateException(
            "Invalid status transition from " + entity.getStatus().getName() + 
            " to " + newStatus.getName());
}
```

#### ❌ INCORRECT - Bypassing workflow

```java
// WRONG - Direct status assignment without workflow validation
entity.setStatus(anyStatus);  // May violate workflow rules!
service.save(entity);
```

### Exception: CProject Status Initialization

**CProject entities require special handling because they extend `CEntityOfCompany`, not `CEntityOfProject`.**

Since `IHasStatusAndWorkflowService.initializeNewEntity()` expects a `CProject` parameter (which CProject itself cannot provide), use inline initialization:

```java
@Override
public void initializeNewEntity(final CProject entity) {
    super.initializeNewEntity(entity);
    
    final CCompany currentCompany = getCurrentCompany();
    entity.setCompany(currentCompany);
    
    // Initialize entity type
    final List<?> availableTypes = projectTypeService.listByCompany(currentCompany);
    Check.notEmpty(availableTypes, "No project types available");
    entity.setEntityType((CProjectType) availableTypes.get(0));
    
    // Initialize workflow-based status using static method
    Check.notNull(entity.getWorkflow(), "Workflow cannot be null");
    final CProjectItemStatus initialStatus = 
            IHasStatusAndWorkflowService.getInitialStatus(entity, projectItemStatusService);
    entity.setStatus(initialStatus);
}
```

### Related Documentation

- [Entity Inheritance Patterns](entity-inheritance-patterns.md) - Entity design principles
- [IHasStatusAndWorkflow Interface](../../src/main/java/tech/derbent/api/workflow/service/IHasStatusAndWorkflow.java) - Interface definition
- [IHasStatusAndWorkflowService](../../src/main/java/tech/derbent/api/workflow/service/IHasStatusAndWorkflowService.java) - Service utility methods

### Rule: Workflow-Aware Status Transitions

When items are dragged between kanban columns:

1. **Resolve valid statuses**: Intersect column's included statuses with workflow-valid transitions
2. **Single status**: Automatically apply it
3. **Multiple statuses**: Show selection dialog for user choice
4. **No valid statuses**: Show warning, update column but not status

```java
// Example: Item with status "To Do" dropped on "Completed" column
// 1. Get column statuses: ["Done", "Closed"]  
// 2. Get workflow transitions from "To Do": ["In Progress"]
// 3. Intersect: [] (empty - no valid transition)
// 4. Action: Show warning, don't change status
```

### Related Documentation

- [Kanban Board Workflow](../../features/kanban-board-workflow.md) - Complete kanban system documentation
- [Workflow Status Change Pattern](../../development/workflow-status-change-pattern.md) - Workflow validation patterns
- [Drag-Drop Component Pattern](../drag-drop-component-pattern.md) - Drag-drop implementation guide

## Master-Detail Validation Pattern (CRITICAL - MANDATORY)

### Rule: Validate Against BOTH Persisted AND In-Memory Child Collections

**When validating child entities in master-detail relationships, ALWAYS check both persisted children (from database) AND in-memory children (from parent's collection).**

This rule applies to ALL services that:
1. Manage child entities with a master-detail relationship
2. Perform uniqueness validation within the master's scope
3. Support batch creation/modification of child entities

### Why This Matters

During batch operations (especially initialization), child entities are:
1. Added to the parent's collection in memory (e.g., `master.getChildren().add(child)`)
2. Parent is saved, which cascade-saves all children
3. Validation runs per-child, but new children don't have IDs yet
4. Result: **In-memory children are invisible to validation queries**

This creates a validation gap where constraints can be violated during batch operations.

### Implementation Pattern

**WRONG - Only checks persisted entities:**
```java
private void validateUniqueness(final ChildEntity entity) {
    final List<ChildEntity> allChildren = findByMaster(entity.getMaster());
    // This only returns persisted children from database
    // Misses in-memory children being created in the same batch
}
```

**CORRECT - Checks both persisted AND in-memory entities:**
```java
private void validateUniqueness(final ChildEntity entity) {
    final MasterEntity master = entity.getMaster();
    
    // Get persisted children from database
    final List<ChildEntity> persistedChildren = findByMaster(master);
    
    // Combine with in-memory children from parent's collection
    final Set<ChildEntity> allChildren = new HashSet<>(persistedChildren);
    if (master.getChildren() != null) {
        allChildren.addAll(master.getChildren());
    }
    
    // Log validation coverage for debugging
    LOGGER.debug("[Validation] Checking {} total children ({} persisted + {} in-memory)",
        allChildren.size(), persistedChildren.size(), 
        master.getChildren() != null ? master.getChildren().size() : 0);
    
    // Perform validation against ALL children
    for (final ChildEntity existing : allChildren) {
        // Skip self - use equals() for in-memory, ID for persisted
        if (entity.equals(existing)) {
            continue;
        }
        if (entity.getId() != null && existing.getId() != null 
            && entity.getId().equals(existing.getId())) {
            continue;
        }
        
        // Check for constraint violations
        if (violatesConstraint(entity, existing)) {
            throw new CValidationException("Constraint violated");
        }
    }
}
```

### Key Implementation Details

1. **Use HashSet to combine sources** - Automatically deduplicates if an entity appears in both
2. **Check entity.equals() for in-memory** - Works even when IDs are null
3. **Check ID equality for persisted** - Standard comparison for database entities
4. **Add debug logging** - Show count of persisted vs in-memory children checked

### Common Master-Detail Patterns Requiring This Validation

| Master Entity | Child Entity | Validation Scenario |
|---------------|--------------|---------------------|
| CKanbanLine | CKanbanColumn | Status uniqueness across columns |
| CDetailSection | CDetailLines | Field name uniqueness within section |
| CSprint | CSprintItem | Item uniqueness within sprint |
| CWorkflow | CWorkflowStatusRelation | Transition uniqueness within workflow |
| Any parent | Any ordered children | Order uniqueness within parent |

### Example: CKanbanColumnService (Reference Implementation)

See `CKanbanColumnService.validateStatusUniqueness()` for a complete working example:

```java
private void validateStatusUniqueness(final CKanbanColumn entity) {
    // Skip if no data to validate
    if (entity.getIncludedStatuses() == null || entity.getIncludedStatuses().isEmpty()) {
        return;
    }
    
    final CKanbanLine line = entity.getKanbanLine();
    
    // CRITICAL: Check BOTH persisted AND in-memory columns
    final List<CKanbanColumn> persistedColumns = findByMaster(line);
    final Set<CKanbanColumn> allColumns = new HashSet<>(persistedColumns);
    
    if (line.getKanbanColumns() != null) {
        allColumns.addAll(line.getKanbanColumns());
        LOGGER.debug("[KanbanValidation] Checking {} total columns ({} persisted + {} in-memory)",
            allColumns.size(), persistedColumns.size(), line.getKanbanColumns().size());
    }
    
    // Build constraint map from ALL columns
    final Map<Long, String> statusToColumnMap = new HashMap<>();
    for (final CKanbanColumn column : allColumns) {
        if (entity.equals(column)) continue;
        if (entity.getId() != null && column.getId() != null 
            && column.getId().equals(entity.getId())) continue;
        
        // Add constraint data from this column
        // ... validation logic ...
    }
    
    // Check for violations
    // ... throw CValidationException if found ...
}
```

### Checklist for Implementing This Pattern

When creating or reviewing validation logic in a child entity service:

- [ ] Identify the master-detail relationship
- [ ] Check if validation queries use `findByMaster()` or similar
- [ ] Verify parent entity has a collection of children (e.g., `master.getChildren()`)
- [ ] Combine persisted and in-memory children using `HashSet`
- [ ] Use `entity.equals()` comparison in addition to ID comparison
- [ ] Add debug logging showing counts of persisted vs in-memory
- [ ] Test with batch initialization to verify validation catches violations
- [ ] Document the validation pattern in the service class

### Testing Requirements

All services implementing this pattern MUST be tested for:
1. **Single entity validation** - Creates one child, validates against persisted
2. **Batch creation validation** - Creates multiple children together, validates against in-memory
3. **Mixed scenario validation** - Creates children when some already exist

### Related Patterns

- **Fail-fast validation** - Throw exceptions immediately when constraints violated
- **Defensive cleanup** - Remove violations after save as safeguard
- **Debug logging** - Use consistent prefixes like `[Validation]` for troubleshooting

## Universal Filter Toolbar Pattern (Mandatory)

### Overview

The Universal Filter Toolbar Framework provides a composable, type-safe filtering system used throughout the application. This pattern MUST be used for all filtering UIs including kanban boards, grids, master-detail views, asset management, and budget filtering.

**Location**: `tech.derbent.api.ui.component.filter` package

**Documentation**: See `docs/development/universal-filter-toolbar-framework.md` for comprehensive usage guide.

### Core Principles

1. **Composition over Inheritance** - Build toolbars from reusable filter components
2. **Automatic Value Persistence** - Filter selections persist across refreshes (enabled by default)
3. **Fail-Fast Validation** - Component ID MUST be set before building toolbar
4. **Dynamic Discovery** - Entity types auto-detected from data
5. **Prefix Support** - Multiple toolbars in same view use storage ID prefixes to avoid conflicts

### Mandatory Pattern

#### ✅ CORRECT - Composable Filter Toolbar

```java
public class CMyFilterToolbar extends CUniversalFilterToolbar<MyEntity> {
    
    private final CSprintFilter sprintFilter;
    private final CEntityTypeFilter entityTypeFilter;
    private final CResponsibleUserFilter responsibleUserFilter;
    
    public CMyFilterToolbar() {
        super();
        
        // CRITICAL: Set ID BEFORE adding filters (fail-fast check)
        setId("myFilterToolbar");
        
        // Optional: Set prefix if multiple toolbars exist in same view
        // setStorageIdPrefix("left_");  
        
        // Create and add filter components
        sprintFilter = new CSprintFilter();
        entityTypeFilter = new CEntityTypeFilter();
        responsibleUserFilter = new CResponsibleUserFilter();
        
        addFilterComponent(sprintFilter);
        addFilterComponent(entityTypeFilter);
        addFilterComponent(responsibleUserFilter);
        
        // Build toolbar (automatically enables value persistence)
        build();
    }
    
    // Provide methods to update filter options dynamically
    public void setAvailableSprints(List<CSprint> sprints, CSprint defaultSprint) {
        Objects.requireNonNull(sprints, "Sprints list cannot be null");
        sprintFilter.setAvailableSprints(sprints, defaultSprint);
    }
    
    public void setAvailableItems(List<MyEntity> items) {
        Objects.requireNonNull(items, "Items list cannot be null");
        // Extract entities for type discovery
        entityTypeFilter.setAvailableEntityTypes(items);
    }
}
```

#### ❌ INCORRECT - Old Complex Pattern

```java
// DON'T create custom filter toolbars with inner classes and manual logic
public class CMyFilterToolbar extends CHorizontalLayout {
    
    // ❌ Inner class for type options
    private static class TypeOption { }
    
    // ❌ Manual FilterCriteria class
    public static class FilterCriteria {
        private String type;
        private String status;
        // Manual getters/setters...
    }
    
    // ❌ Manual ComboBox creation and wiring
    private ComboBox<TypeOption> comboType;
    private ComboBox<String> comboStatus;
    
    // ❌ 200+ lines of custom logic
}
```

### Using Filter Criteria

```java
// In parent component
filterToolbar.addFilterChangeListener(criteria -> {
    // Get filter values using filter keys
    Class<?> entityType = criteria.getValue(CEntityTypeFilter.FILTER_KEY);
    CSprint sprint = criteria.getValue(CSprintFilter.FILTER_KEY);
    ResponsibleFilterMode mode = criteria.getValue(CResponsibleUserFilter.FILTER_KEY);
    
    // Apply filters
    List<MyEntity> filtered = applyFilters(allItems, entityType, sprint, mode);
    refreshDisplay(filtered);
});
```

### Multiple Toolbars in Same View

When multiple filter toolbars exist in the same view (e.g., split screen), use storage ID prefixes:

```java
// Left toolbar
leftToolbar = new CMyFilterToolbar();
leftToolbar.setId("myFilterToolbar");
leftToolbar.setStorageIdPrefix("left_");  // Prefix to avoid ID collision
leftToolbar.build();

// Right toolbar
rightToolbar = new CMyFilterToolbar();
rightToolbar.setId("myFilterToolbar");     // Same ID is OK with different prefix
rightToolbar.setStorageIdPrefix("right_"); // Different prefix ensures unique storage IDs
rightToolbar.build();
```

### Fail-Fast Requirements

Filter toolbars MUST implement fail-fast validation:

1. **Component ID Required**: Call `setId()` BEFORE `build()` - throws `IllegalStateException` if missing
2. **Non-Null Lists**: All `setAvailable*()` methods throw `IllegalArgumentException` if null
3. **Non-Null Components**: Filter components checked for null in `addFilterComponent()`

```java
// ✅ Fail-fast examples
Objects.requireNonNull(items, "Items list cannot be null");
Objects.requireNonNull(entityClass, "Entity class cannot be null");

// Component ID validation (automatic in getStorageId())
if (componentId == null || componentId.isBlank()) {
    throw new IllegalStateException("Component ID must be set before building toolbar");
}
```

### Available Filter Components

Pre-built filter components in `tech.derbent.api.ui.component.filter`:

| Component | Filter Key | Value Type | Purpose |
|-----------|------------|------------|---------|
| `CEntityTypeFilter` | `"entityType"` | `Class<?>` | Dynamic entity type discovery (Activity, Meeting, Sprint, etc.) |
| `CSprintFilter` | `"sprint"` | `CSprint` | Sprint selection with color-aware dropdown |
| `CResponsibleUserFilter` | `"responsibleUser"` | `ResponsibleFilterMode` | Ownership filtering (All items / My items) |

### Creating Custom Filter Components

To create a custom filter component, extend `CAbstractFilterComponent<T>`:

```java
public class CStatusFilter extends CAbstractFilterComponent<String> {
    
    public static final String FILTER_KEY = "status";
    
    private final ComboBox<String> comboBox;
    
    public CStatusFilter() {
        super(FILTER_KEY);
        comboBox = new ComboBox<>("Status");
        comboBox.addValueChangeListener(event -> {
            notifyChangeListeners(event.getValue());
        });
    }
    
    @Override
    protected Component createComponent() {
        return comboBox;
    }
    
    @Override
    protected void updateComponentValue(String value) {
        comboBox.setValue(value);
    }
    
    @Override
    public void clearFilter() {
        comboBox.clear();
    }
    
    @Override
    public void enableValuePersistence(String storageId) {
        CValueStorageHelper.valuePersist_enable(
            comboBox, 
            storageId + "_" + FILTER_KEY,
            value -> value,
            value -> value
        );
    }
    
    // Custom configuration methods
    public void setAvailableStatuses(Set<String> statuses) {
        Objects.requireNonNull(statuses, "Statuses set cannot be null");
        comboBox.setItems(statuses);
    }
}
```

### Migration from Old Pattern

When migrating existing filter toolbars:

1. **Identify filter needs**: What filters does the toolbar need?
2. **Choose or create filter components**: Use existing or create custom
3. **Extend CUniversalFilterToolbar**: Replace custom inheritance
4. **Set ID in constructor**: Call `setId()` before adding filters
5. **Add filter components**: Use `addFilterComponent()`
6. **Call build()**: Replaces manual clear button and persistence setup
7. **Update parent component**: Use `FilterCriteria.getValue(FILTER_KEY)` instead of custom getters

### Checklist for Filter Toolbar Implementation

- [ ] Extends `CUniversalFilterToolbar<T>` or `CAbstractFilterToolbar<T>`
- [ ] Calls `setId("uniqueId")` in constructor BEFORE adding filters
- [ ] Adds prefix if multiple toolbars in same view: `setStorageIdPrefix("prefix_")`
- [ ] Uses composition: creates filter components and adds with `addFilterComponent()`
- [ ] Calls `build()` after adding all filters
- [ ] Provides `setAvailable*()` methods with fail-fast null checks
- [ ] Uses `Objects.requireNonNull()` for all method parameters that cannot be null
- [ ] Accesses filter values via `criteria.getValue(FilterKey.FILTER_KEY)`
- [ ] Does NOT call `valuePersist_enable()` manually (automatic in `build()`)

### Filtering Framework Usage (MANDATORY)

**Rule**: All filtering UI must use the **Universal Filter Toolbar Framework**. Ad-hoc filter rows are forbidden.

✅ Use:
- `CUniversalFilterToolbar<T>` / `CAbstractFilterToolbar<T>` for page-level filters
- `CComponentGridSearchToolbar` / `CComponentFilterToolbar` for grid search and selection filters
- `CComponentEntitySelection` (or `CDialogEntitySelection`) for entity selection dialogs

❌ Avoid:
- Custom `HorizontalLayout` + `TextField` filter rows
- Manual filter persistence without `setId()` + framework helpers
- Duplicated filter logic outside `FilterCriteria`/toolbar callbacks

**Reference**: `docs/development/universal-filter-toolbar-framework.md`

### Related Documentation

- **Comprehensive Guide**: `docs/development/universal-filter-toolbar-framework.md`
- **UI Component Standards**: `docs/architecture/ui-css-coding-standards.md`
- **Value Persistence Pattern**: `docs/architecture/value-persistence-pattern.md`

---

**Version**: 1.2  
**Last Updated**: 2026-01-12  
**Pattern Added**: Profile Seed Initialization

## Entity-Enabled Grid Columns (MANDATORY)

**Rule**: Always use entity-enabled columns (`CGridColumnEntity`) for entity references like `assignedTo`, `approvedBy`, `status`, `createdBy`, etc.

**Benefits**:
- Automatic entity name formatting
- Icon/avatar display
- Click-to-view entity details
- Consistent UI across application
- Null-safe rendering

**Example**:
```java
// ✅ CORRECT - Use entity column
grid.addColumn_entity(CActivity::getAssignedTo)
    .setHeader("Assigned To")
    .setAutoWidth(true);

// ❌ WRONG - Manual formatting
grid.addColumn(activity -> activity.getAssignedTo() != null 
    ? activity.getAssignedTo().getName() 
    : "")
    .setHeader("Assigned To");
```

## Attachment/Dialog Component Pattern (MANDATORY)

**Rule**: When creating dialogs for entity management (especially with file uploads), follow these patterns:

### 1. Use Existing Base Classes
```java
// ✅ CORRECT - Extend CDBEditDialog
public class CDialogAttachment extends CDBEditDialog<CAttachment> {
    // Implementation
}
```

### 2. Use FormBuilder for Form Fields
```java
// ✅ CORRECT - Use FormBuilder
protected void setupForm() {
    formBuilder = new CFormBuilder<>(binder, entity);
    formBuilder.buildForm(formLayout);
}

// ❌ WRONG - Manual field creation
private TextField textFieldName = new TextField("Name");
private TextArea textAreaDescription = new TextArea("Description");
```

### 3. Single Dialog for Add/Edit
```java
// ✅ CORRECT - Handle both modes in one dialog
public CDialogAttachment(CAttachment entity, CAttachmentService service) {
    if (entity.getId() == null) {
        // New mode - show upload
    } else {
        // Edit mode - existing document
    }
}

// ❌ WRONG - Separate dialogs
public class CDialogAttachmentUpload { }
public class CDialogAttachmentEdit { }
```

### 4. Selection-Aware Display Grid
```java
// ✅ CORRECT - Enable/disable based on selection
grid.addSelectionListener(event -> {
    boolean hasSelection = event.getFirstSelectedItem().isPresent();
    buttonEdit.setEnabled(hasSelection);
    buttonDelete.setEnabled(hasSelection);
});
```

### 5. Double-Click to Edit
```java
// ✅ CORRECT - Add double-click listener
grid.addItemDoubleClickListener(event -> {
    openEditDialog(event.getItem());
});
```

## CRUD Pattern Standards (MANDATORY)

All CRUD views MUST implement these features:

1. **Selection-Aware Toolbar**
   - Buttons enabled/disabled based on grid selection
   - Update immediately on selection change

2. **Double-Click Grid Editing**
   - Double-click any row opens edit dialog
   - Single-click for selection only

3. **Inline Validation**
   - Validate before save
   - Show clear error messages
   - Focus on first error field

4. **Optimistic Locking Handling**
   - Catch `OptimisticLockingFailureException`
   - Show user-friendly message
   - Offer to refresh and retry

5. **Confirmation Dialogs**
   - Confirm before delete
   - Warn about unsaved changes
   - Clear yes/no buttons

**Example Complete CRUD View**:
```java
public class CAttachmentGridView extends CComponentListEntityBase<CAttachment> {
    
    private CButton buttonAdd;
    private CButton buttonEdit;
    private CButton buttonDelete;
    private CGrid<CAttachment> grid;
    
    @Override
    protected void createUI() {
        // 1. Create toolbar with disabled buttons
        buttonAdd = create_buttonAdd();
        buttonEdit = create_buttonEdit();
        buttonEdit.setEnabled(false);  // Disabled initially
        buttonDelete = create_buttonDelete();
        buttonDelete.setEnabled(false);  // Disabled initially
        
        // 2. Create grid with double-click
        grid = new CGrid<>(CAttachment.class);
        grid.addItemDoubleClickListener(e -> on_buttonEdit_clicked());
        
        // 3. Add selection listener for toolbar
        grid.addSelectionListener(this::on_grid_selectionChanged);
    }
    
    protected void on_grid_selectionChanged(SelectionEvent<Grid<CAttachment>, CAttachment> event) {
        boolean hasSelection = event.getFirstSelectedItem().isPresent();
        buttonEdit.setEnabled(hasSelection);
        buttonDelete.setEnabled(hasSelection);
    }
    
    protected void on_buttonEdit_clicked() {
        CAttachment selected = grid.asSingleSelect().getValue();
        Objects.requireNonNull(selected, "No item selected");
        
        CDialogAttachment dialog = new CDialogAttachment(selected, service);
        dialog.addSaveListener(event -> refreshGrid());
        dialog.open();
    }
    
    protected void on_buttonDelete_clicked() {
        CAttachment selected = grid.asSingleSelect().getValue();
        Objects.requireNonNull(selected, "No item selected");
        
        // Confirmation dialog
        CNotifications.showConfirmationDialog(
            "Delete attachment '" + selected.getName() + "'?",
            () -> {
                try {
                    service.delete(selected);
                    CNotifications.showDeleteSuccess();
                    refreshGrid();
                } catch (OptimisticLockingFailureException ex) {
                    CNotifications.showOptimisticLockingError();
                    refreshGrid();
                } catch (Exception ex) {
                    LOGGER.error("Error deleting attachment", ex);
                    CNotifications.showException("Error deleting attachment", ex);
                }
            }
        );
    }
}
```

## Entity Copy Pattern (MANDATORY - Automatic Interface Copy)

**Status**: ✅ COMPLETE - Enforced in base class since 2026-01-17

**Full Documentation**: See [Entity Clone and Copy Pattern](CLONE_PATTERN.md) for complete details on both clone and copyTo patterns.

All entities MUST follow the **automatic interface-based copy pattern**. The base class `CEntityDB` automatically copies fields from common interfaces, eliminating manual interface method calls in child entities.

### Pattern Overview

**Core Principle**: Interface-specific copy logic lives in **static methods on interfaces**, and the **base class calls them automatically** during `copyEntityTo()`.

### Rule 1: Base Class Handles Interface Copying (MANDATORY)

The base `CEntityDB.copyEntityTo()` automatically copies fields for all common interfaces:

```java
// CEntityDB.java - Base class handles automatic interface copying
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    // Copy active field (always)
    copyField(this::getActive, target::setActive);
    
    // Automatically copy common interface fields if both source and target implement them
    // This reduces code duplication across all entities
    IHasComments.copyCommentsTo(this, target, options);
    IHasAttachments.copyAttachmentsTo(this, target, options);
    IHasStatusAndWorkflow.copyStatusAndWorkflowTo(this, target, options);
}
```

**Effect**: All 35+ entities inheriting from `CEntityDB` automatically get interface-based copying with ZERO manual code!

### Rule 2: Entity-Specific Override (MANDATORY)

Child entities MUST:
1. Call `super.copyEntityTo(target, options)` first (handles automatic interface copying)
2. Only copy entity-specific fields
3. **NEVER** manually call interface copy methods (handled by base class)

**✅ CORRECT - Entity-Specific Fields Only**:
```java
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);  // ← Handles ALL interface copying automatically!
    
    if (target instanceof CActivity) {
        CActivity targetActivity = (CActivity) target;
        
        // Copy ONLY Activity-specific fields
        copyField(this::getPriority, targetActivity::setPriority);
        copyField(this::getEstimatedHours, targetActivity::setEstimatedHours);
        copyField(this::getActualHours, targetActivity::setActualHours);
        
        // Note: Comments, attachments, status/workflow copied automatically by base class
    }
}
```

**❌ INCORRECT - Manual Interface Calls**:
```java
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    
    if (target instanceof CActivity) {
        CActivity targetActivity = (CActivity) target;
        
        // ❌ DON'T DO THIS - Handled by base class
        IHasComments.copyCommentsTo(this, targetActivity, options);
        IHasAttachments.copyAttachmentsTo(this, targetActivity, options);
        IHasStatusAndWorkflow.copyStatusAndWorkflowTo(this, targetActivity, options);
        
        // Entity-specific fields
        copyField(this::getPriority, targetActivity::setPriority);
    }
}
```

### Rule 3: Interface Copy Method Signature (MANDATORY)

All interface copy methods MUST follow this signature:

```java
public interface IHasComments {
    
    Set<CComment> getComments();
    void setComments(Set<CComment> comments);
    
    /** Copy comments from source to target if both implement IHasComments and options allow.
     * @param source the source entity (CEntityDB to access copyCollection)
     * @param target the target entity
     * @param options copy options controlling whether comments are included
     * @return true if comments were copied, false if skipped */
    static boolean copyCommentsTo(final CEntityDB<?> source, final CEntityDB<?> target, final CCloneOptions options) {
        // 1. Check if copying is enabled in options
        if (!options.includesComments()) {
            return false;
        }
        
        // 2. Check if both source and target implement this interface
        if (!(source instanceof IHasComments) || !(target instanceof IHasComments)) {
            return false;  // Skip silently if target doesn't support interface
        }
        
        try {
            // 3. Cast to interface types
            final IHasComments sourceWithComments = (IHasComments) source;
            final IHasComments targetWithComments = (IHasComments) target;
            
            // 4. Use source's copyCollection method to handle cloning
            source.copyCollection(
                sourceWithComments::getComments, 
                (col) -> targetWithComments.setComments((Set<CComment>) col), 
                true  // createNew = true to clone comments
            );
            return true;
        } catch (final Exception e) {
            // 5. Log and skip on error - don't fail entire copy operation
            return false;
        }
    }
}
```

**Key Points**:
- Parameter type is `CEntityDB<?>` (not `T extends CEntityDB<T>`) to allow base class calls
- Method checks both source AND target implement the interface
- Returns `false` silently if incompatible (no exception thrown)
- Uses `source.copyCollection()` to handle recursive cloning

### Rule 4: Cross-Type Copy Support (MANDATORY)

The pattern automatically supports copying between different entity types:

```java
// Copy Activity → Meeting (both implement IHasComments, IHasAttachments)
CActivity activity = new CActivity("Sprint Planning");
activity.getComments().add(new CComment("Important note"));
activity.getAttachments().add(new CAttachment("agenda.pdf"));

// Cross-type copy - comments and attachments automatically copied
CMeeting meeting = activity.copyTo(CMeeting.class, 
    new CCloneOptions.Builder()
        .includeComments(true)
        .includeAttachments(true)
        .build());

// meeting now has comments and attachments from activity!
assertEquals(1, meeting.getComments().size());
assertEquals(1, meeting.getAttachments().size());
```

**How It Works**:
1. `activity.copyTo(CMeeting.class, options)` creates new `CMeeting` instance
2. Calls `CEntityDB.copyEntityTo(meeting, options)` (base class)
3. Base class calls `IHasComments.copyCommentsTo(activity, meeting, options)`
4. Method checks: `activity instanceof IHasComments` ✅ and `meeting instanceof IHasComments` ✅
5. Comments automatically copied!
6. Same for attachments, status/workflow, etc.

### Rule 5: Adding New Interfaces (MANDATORY)

When creating a new interface that needs copy support:

**Step 1**: Add static `copy*To()` method to interface following Rule 3 signature

```java
public interface IHasLabels {
    Set<CLabel> getLabels();
    void setLabels(Set<CLabel> labels);
    
    static boolean copyLabelsTo(final CEntityDB<?> source, final CEntityDB<?> target, final CCloneOptions options) {
        if (!(source instanceof IHasLabels) || !(target instanceof IHasLabels)) {
            return false;
        }
        // ... copy logic
    }
}
```

**Step 2**: Add ONE line to `CEntityDB.copyEntityTo()`:

```java
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    copyField(this::getActive, target::setActive);
    IHasComments.copyCommentsTo(this, target, options);
    IHasAttachments.copyAttachmentsTo(this, target, options);
    IHasStatusAndWorkflow.copyStatusAndWorkflowTo(this, target, options);
    IHasLabels.copyLabelsTo(this, target, options);  // ← ONE LINE = ALL 35+ ENTITIES SUPPORTED!
}
```

**Step 3**: Done! All 35+ entities automatically support label copying with ZERO additional code!

### Rule 6: CloneOptions Integration (MANDATORY)

Always respect `CCloneOptions` flags in interface copy methods:

```java
static boolean copyAttachmentsTo(final CEntityDB<?> source, final CEntityDB<?> target, final CCloneOptions options) {
    // ALWAYS check options first
    if (!options.includesAttachments()) {
        return false;  // Skip if not requested
    }
    // ... rest of copy logic
}
```

**Available Options**:
- `options.includesComments()` - Copy comments?
- `options.includesAttachments()` - Copy attachments?
- `options.includesRelations()` - Copy relations?
- `options.isCloneStatus()` - Copy status?
- `options.isCloneWorkflow()` - Copy workflow?

### Benefits Summary

✅ **Zero Manual Calls**: Entities never call interface methods manually
✅ **35+ Entities Supported**: All automatically get interface copying
✅ **Cross-Type Copy**: Activity → Meeting works automatically
✅ **Safe Skipping**: Missing interfaces silently skipped (no errors)
✅ **One-Line Addition**: Add new interface = instant support everywhere
✅ **Backward Compatible**: Existing code still works
✅ **Performance Neutral**: Same execution path

### Documentation References

- **Implementation Details**: `AUTOMATIC_INTERFACE_COPY_COMPLETE.md`
- **Clone Pattern**: `docs/architecture/CLONE_PATTERN.md`
- **Interface Hierarchy**: `docs/architecture/interface-hierarchy-before-after.md`

### Quality Matrix Compliance

**Required Checks**:
- [ ] Entity extends `CEntityDB` or compatible base class
- [ ] Entity overrides `copyEntityTo()` if has entity-specific fields
- [ ] Override calls `super.copyEntityTo(target, options)` FIRST
- [ ] Override does NOT manually call interface copy methods
- [ ] Interface implements static `copy*To()` method with correct signature
- [ ] Interface method checks both source and target implement interface
- [ ] Interface method respects `CCloneOptions` flags
- [ ] Interface method registered in `CEntityDB.copyEntityTo()` base class

## Grid Component Standards (MANDATORY)

All display/list grids MUST:

1. **Update toolbar on selection change**
   ```java
   grid.addSelectionListener(this::on_grid_selectionChanged);
   ```

2. **Enable/disable buttons based on selection**
   ```java
   protected void on_grid_selectionChanged(SelectionEvent<?, T> event) {
       boolean hasSelection = !event.getAllSelectedItems().isEmpty();
       buttonEdit.setEnabled(hasSelection);
       buttonDelete.setEnabled(hasSelection);
       buttonMoveUp.setEnabled(hasSelection);
       buttonMoveDown.setEnabled(hasSelection);
   }
   ```

3. **Support keyboard navigation**
   - Arrow keys for navigation
   - Enter key for edit
   - Delete key for delete (with confirmation)

4. **Have configurable columns**
   ```java
   grid.getColumns().forEach(col -> col.setResizable(true));
   grid.setColumnReorderingAllowed(true);
   ```

5. **Use consistent column types**
   - Entity references: `addColumn_entity()`
   - Dates: `addColumn_date()`
   - Numbers: `addColumn_number()`
   - Status/workflow: `addColumn_status()`

---

# Testing Standards and Patterns (MANDATORY)

**Version:** 2.3  
**Date:** 2026-01-18  
**Status:** MANDATORY - All tests must follow these rules  
**Change Log:** 2026-01-18 - Add link component coverage, enforce component tester pattern, and mandate filter framework usage

## 🎯 Core Testing Principles

### 1. Browser Visibility - MANDATORY

```bash
# ✅ CORRECT - Browser ALWAYS visible by default
PLAYWRIGHT_HEADLESS=false mvn test -Dtest=TestClass

# ❌ WRONG - Don't run headless during development
PLAYWRIGHT_HEADLESS=true mvn test
```

**Rule:** Browser must be VISIBLE during test development and debugging.  
**Rationale:** Visual feedback is essential for understanding test behavior and debugging failures.  
**Default:** `CBaseUITest.java` defaults to visible browser mode

### 2. Playwright Logging - MANDATORY

```bash
# ✅ CORRECT - Always log to /tmp/playwright.log
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest 2>&1 | tee /tmp/playwright.log

# Monitor in another terminal
tail -f /tmp/playwright.log
```

**Rule:** ALL Playwright test output must be logged to `/tmp/playwright.log`.  
**Rationale:** Centralized logging location for debugging and monitoring across all test runs.  
**Implementation:** Add `2>&1 | tee /tmp/playwright.log` to all test runner commands.

### 3. Navigation Pattern - MANDATORY (Use CPageTestAuxillary Buttons)

```java
// ❌ WRONG - Don't navigate via side menu
navigateToViewByText("Activities");
clickMenuItem("Activities");

// ❌ WRONG - Don't create custom navigation test scripts  
@Test
void testJumpToSpecificView() {
    page.navigate("http://localhost:8080/cdynamicpagerouter/specific-view");
}

// ✅ CORRECT - Use CPageTestAuxillary buttons with deterministic IDs
@Test
void testSpecificView() {
    loginToApplication();
    
    // Navigate to CPageTestAuxillary page
    page.navigate("http://localhost:" + port + "/cpagetestauxillary");
    wait_500();
    
    // Click button by ID (stable, deterministic)
    page.locator("#test-aux-btn-activities-0").click();
    wait_1000();
    
    // Continue test...
}

// ✅ BETTER - Use route filtering from comprehensive test
@Test
void testFilteredViews() {
    // Set system property to filter by keyword
    System.setProperty("test.routeKeyword", "activity");
    
    // Run comprehensive test - it will only test matching routes
    testAllAuxillaryPages();
}
```

**Rule:** Always use **CPageTestAuxillary button IDs** for navigation in Playwright tests.  
**Rationale:**
- **Deterministic**: Button IDs are stable and generated consistently
- **No Side Menu Issues**: Avoids Vaadin side menu rendering/timing issues
- **Filtering Support**: Can skip already-passed tests via keyword filtering
- **Single Source**: CPageTestAuxillaryService provides all routes dynamically

**Button ID Pattern:**
- Format: `test-aux-btn-{sanitized-title}-{index}`
- Example: `#test-aux-btn-activities-0`, `#test-aux-btn-budget-types-5`
- Attributes: `data-route`, `data-title`, `data-button-index` for metadata

**Filtering Pattern:**
```bash
# Test only specific routes by keyword
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dtest.routeKeyword=activity

# Test only specific button by ID
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dtest.targetButtonId=test-aux-btn-activities-0

# Test specific route directly
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dtest.targetRoute=cdynamicpagerouter/activities
```

### 4. Exception Handling - MANDATORY

```java
// ✅ CORRECT - Always throw exceptions, never ignore
catch (Exception e) {
    LOGGER.error("Error: {}", e.getMessage(), e);  // Log with stack trace
    CNotificationService.showError("Error occurred"); // Notify user
    throw new RuntimeException("Context info", e);   // Throw, don't ignore
}

// ❌ WRONG - Never silently ignore exceptions
catch (Exception e) {
    LOGGER.warn("Error: {}", e.getMessage());  // Only warning
    // No throw - execution continues
}
```

**Rule:** All exceptions must be logged, shown to user (if UI context), and thrown.  
**Rationale:** Silent failures mask problems and make debugging impossible.

### 5. Fail-Fast on Errors - MANDATORY

```java
// ✅ CORRECT - Stop immediately on exceptions
@Test
void testEntity() {
    try {
        performOperation();
        performFailFastCheck("Operation Name");  // Checks for exception dialogs
    } catch (AssertionError e) {
        takeScreenshot("error-operation", true);
        throw e;  // Propagate immediately
    }
}
```

**Rule:** Tests must stop immediately when exceptions occur.  
**Rationale:** Continuing after errors wastes time and produces misleading results.

## 📋 Comprehensive CRUD Testing Pattern

### Entity Test Checklist (MANDATORY for all entities)

```java
@Test
@DisplayName("🧪 Test Entity CRUD Operations")
void testEntityCrud() {
    String pageName = "entity-name";
    
    // 1. NAVIGATE via CPageTestAuxillary
    page.navigate("http://localhost:" + port + "/cpagetestauxillary");
    wait_500();
    page.locator("#test-aux-btn-entity-name-0").click();
    wait_1000();
    takeScreenshot("001-entity-page-loaded");
    
    // 2. VERIFY GRID LOADS
    boolean hasGrid = checkGridExists();
    LOGGER.info("Grid present: {}", hasGrid);
    
    // 3. VERIFY CRUD TOOLBAR
    boolean hasCrud = checkCrudToolbarExists();
    LOGGER.info("CRUD toolbar present: {}", hasCrud);
    
    // 4. TEST CREATE
    if (checkCrudButtonExists(CRUD_NEW_BUTTON_ID)) {
        testCreateAndSave(pageName);
    }
    
    // 5. TEST UPDATE
    if (checkGridHasData() && checkCrudButtonExists(CRUD_SAVE_BUTTON_ID)) {
        testUpdateAndSave(pageName);
    }
    
    // 6. TEST DELETE
    if (checkGridHasData() && checkCrudButtonExists(CRUD_DELETE_BUTTON_ID)) {
        testDeleteButton(pageName);
    }
    
    // 7. TEST CUSTOM COMPONENTS
    if (hasKanbanBoard()) {
        runKanbanBoardTests(pageName);
    }
    
    takeScreenshot("999-entity-test-complete");
}
```

### Test Execution Order (MANDATORY)

1. **Login** → `loginToApplication()`
2. **Navigate to Test Auxiliary** → `page.navigate(".../cpagetestauxillary")`
3. **Click Target Button** → `page.locator("#test-aux-btn-...").click()`
4. **Analyze Page** → Check for grids, CRUD toolbars, custom components
5. **Test Grid** → If present: sort, filter, select
6. **Test CRUD** → If present: create, update, delete
7. **Test Components** → If present: kanban, attachments, comments, links
8. **Screenshot** → At each major step

### Component-Specific Testing

#### Kanban Board Testing
```java
private void runKanbanBoardTests(String pageName) {
    Locator columns = page.locator(".kanban-column");
    Locator postits = page.locator(".kanban-postit");
    
    // Test story point editing
    editKanbanStoryPoints(pageName, postits.first());
    
    // Test drag between columns
    dragKanbanPostitBetweenColumns(pageName, columns, postits.first());
    
    // Test drag to backlog
    dragKanbanPostitToBacklog(pageName, columns, postits.first());
    
    takeScreenshot("kanban-tested");
}
```

#### Attachments Section Testing
```java
private void testAttachmentsSection(String pageName) {
    clickTabOrSection("Attachments");
    
    // Test upload
    uploadFile("test-document.pdf");
    verifyAttachmentInList("test-document.pdf");
    
    // Test download
    downloadAttachment("test-document.pdf");
    
    // Test delete
    deleteAttachment("test-document.pdf");
    confirmDialog();
    
    takeScreenshot("attachments-tested");
}
```

#### Comments Section Testing
```java
private void testCommentsSection(String pageName) {
    clickTabOrSection("Comments");
    
    // Test add
    fillCommentField("Test comment " + System.currentTimeMillis());
    clickButton("Add Comment");
    verifyCommentInList("Test comment");
    
    // Test edit
    selectComment("Test comment");
    clickButton("Edit");
    fillCommentField("Updated comment");
    clickButton("Save");
    
    // Test delete
    deleteComment("Updated comment");
    
    takeScreenshot("comments-tested");
}
```

#### Links Section Testing
```java
private void testLinksSection(String pageName) {
    clickTabOrSection("Links");
    
    // Test add
    clickButton("Add Link");
    selectTargetEntityType("Activity");
    fillTargetEntityId("1");
    fillLinkType("Related");
    clickButton("Save");
    verifyLinkInList("Related");
    
    // Test edit
    selectLink("Related");
    clickButton("Edit");
    fillLinkType("Related-Updated");
    clickButton("Save");
    
    // Test delete
    deleteLink("Related-Updated");
    
    takeScreenshot("links-tested");
}
```

#### Component Tester Pattern (MANDATORY)

All component-specific validation must be encapsulated in testers that implement `IComponentTester` and extend `CBaseComponentTester`.
Orchestration classes must only declare testers and call `canTest(page)` + `test(page)`.

✅ CORRECT:
```java
private final CAttachmentComponentTester attachmentTester = new CAttachmentComponentTester();
private final CCommentComponentTester commentTester = new CCommentComponentTester();
private final CLinkComponentTester linkTester = new CLinkComponentTester();

if (attachmentTester.canTest(page)) {
    attachmentTester.test(page);
}
if (commentTester.canTest(page)) {
    commentTester.test(page);
}
if (linkTester.canTest(page)) {
    linkTester.test(page);
}
```

❌ INCORRECT:
```java
// Direct selectors in the orchestration class
if (page.locator("#custom-attachment-component").count() > 0) {
    // attachment CRUD inline here
}
```

## 🎯 Test Helper Methods (MANDATORY)

All test classes extending `CBaseUITest` must use these standardized helpers:

### Navigation Helpers
```java
// Login
protected void loginToApplication();
protected void loginToApplication(String username, String password);

// Navigate to test auxiliary page
protected void navigateToTestAuxillaryPage();

// Click button by ID (from CPageTestAuxillary)
protected void clickAuxillaryButton(String buttonId);
```

### Grid Helpers
```java
protected boolean checkGridExists();
protected boolean checkGridHasData();
protected boolean checkGridIsSortable();
protected int getGridRowCount();
protected String getFirstGridCellText();
protected void selectGridRowByCellText(String text);
protected void testGridSorting(String pageName);
protected void testGridFiltering(String pageName);
```

### CRUD Helpers
```java
protected boolean checkCrudToolbarExists();
protected boolean checkCrudButtonExists(String buttonId);
protected void testCreateAndSave(String pageName);
protected void testUpdateAndSave(String pageName);
protected void testDeleteButton(String pageName);
protected void testRefreshButton(String pageName);
```

### Field Helpers
```java
protected void fillFieldById(String fieldId, String value);
protected String readFieldValueById(String fieldId);
protected void selectFirstComboBoxOptionById(String fieldId);
protected boolean isComboBoxById(String fieldId);
protected boolean isFieldEditable(Locator field);
protected FieldValueResult populateEditableFields(String pageName);
```

### Screenshot Helpers
```java
protected void takeScreenshot(String name);
protected void takeScreenshot(String name, boolean isError);
```

**Screenshot Policy - MANDATORY:**
- ❌ **NEVER** take screenshots on successful operations
- ✅ **ONLY** take screenshots when errors/exceptions occur
- **Rationale**: Reduces test runtime, disk usage, and focuses attention on failures

```java
// ❌ WRONG - Taking screenshot after every operation
loginToApplication();
takeScreenshot("after-login", false);  // ← Remove this
clickNewButton();
takeScreenshot("clicked-new", false);  // ← Remove this

// ✅ CORRECT - Only screenshot on errors
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

### Wait Helpers
```java
protected void wait_500();
protected void wait_1000();
protected void wait_2000();
protected void wait_afterlogin();
```

## 📊 Test Reporting Format (MANDATORY)

### Log Format
```
INFO  (TestClass.java:123) testMethod:🚀 Starting comprehensive test...
INFO  (TestClass.java:125) testMethod:📝 Step 1: Logging into application...
INFO  (TestClass.java:130) testMethod:✅ Successfully logged in
INFO  (TestClass.java:135) testMethod:🧭 Step 2: Navigating to CPageTestAuxillary...
INFO  (TestClass.java:140) testMethod:🔍 Step 3: Discovering navigation buttons...
INFO  (TestClass.java:145) testMethod:🔍 Discovered 45 navigation buttons
INFO  (TestClass.java:150) testMethod:🎯 Testing button 1/45: Activities
INFO  (TestClass.java:155) testMethod:   ✓ Grid has data: true
INFO  (TestClass.java:160) testMethod:   ✓ CRUD toolbar present: true
INFO  (TestClass.java:165) testMethod:   ➕ Testing New button...
INFO  (TestClass.java:170) testMethod:      ✓ Created row (5 -> 6)
INFO  (TestClass.java:175) testMethod:   ✏️  Testing Update + Save...
INFO  (TestClass.java:180) testMethod:      ✓ Updated value
INFO  (TestClass.java:185) testMethod:   🗑️ Testing Delete button...
INFO  (TestClass.java:190) testMethod:      ✓ Deleted row (6 -> 5)
INFO  (TestClass.java:195) testMethod:✅ Completed testing button 1/45
```

### Screenshot Naming Convention
```
001-after-login.png
002-test-auxillary-page.png
003-page-activities-initial.png
004-page-activities-grid-tested.png
005-page-activities-created.png
006-page-activities-updated.png
007-page-activities-deleted.png
008-page-activities-final.png
```

**Pattern**: `{counter:03d}-{description}.png`

## 🔁 Test Execution Strategy

### Run Comprehensive Test with Filtering

```bash
# Run all pages
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest 2>&1 | tee /tmp/playwright.log

# Run only activity-related pages
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dtest.routeKeyword=activity 2>&1 | tee /tmp/playwright.log

# Run only test management pages
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dtest.routeKeyword=test 2>&1 | tee /tmp/playwright.log

# Run only financial pages
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dtest.routeKeyword=budget 2>&1 | tee /tmp/playwright.log

# Run specific single page by button ID
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dtest.targetButtonId=test-aux-btn-activities-0 2>&1 | tee /tmp/playwright.log

# Run specific route directly
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dtest.targetRoute=cdynamicpagerouter/activities 2>&1 | tee /tmp/playwright.log
```

### Repeat Until Success Pattern

```bash
#!/bin/bash
# run-tests-until-success.sh

while true; do
    echo "🚀 Starting test run..."
    mvn test -Dtest=CPageTestAuxillaryComprehensiveTest 2>&1 | tee /tmp/playwright.log
    
    if [ $? -eq 0 ]; then
        echo "✅ ALL TESTS PASSED!"
        break
    else
        echo "❌ Tests failed, analyzing logs..."
        tail -n 100 /tmp/playwright.log
        echo "Fix issues and press Enter to retry..."
        read
    fi
done
```

## 🤖 Intelligent Adaptive Testing Pattern (MANDATORY)

### Overview

The **Adaptive Testing Pattern** uses intelligent component detection to automatically test pages based on their actual UI components, eliminating hardcoded test scripts and making tests self-maintaining.

### Architecture

```
CAdaptivePageTest (Main Test Class)
├── IControlSignature (Interface)
│   ├── isDetected(Page) → boolean
│   ├── getSignatureName() → String
│   └── getTester() → IComponentTester
├── CControlSignature (Default Implementation)
├── IComponentTester (Interface)
│   ├── canTest(Page) → boolean
│   ├── getComponentName() → String
│   └── test(Page) → void
├── CBaseComponentTester (Base Class)
│   └── Common utilities (elementExists, clickButton, wait_*)
└── Component Implementations
    ├── CCrudToolbarTester
    ├── CCloneToolbarTester
    ├── CGridComponentTester
    ├── CAttachmentComponentTester
    ├── CCommentComponentTester
    ├── CStatusFieldTester
    └── CDatePickerTester
```

### Rule 1: Use CAdaptivePageTest for All Page Testing (MANDATORY)

```bash
# ✅ CORRECT - Use adaptive test for all pages
mvn test -Dtest=CAdaptivePageTest 2>&1 | tee /tmp/playwright.log

# ✅ CORRECT - Test specific page by test support button ID
mvn test -Dtest=CAdaptivePageTest -Dtest.targetButtonId=test-aux-btn-activities-0 2>&1 | tee /tmp/playwright.log

# ❌ WRONG - Don't create page-specific test classes
@Test
void testActivitiesPage() { ... }  // DON'T DO THIS

# ❌ WRONG - Don't hardcode component tests in test methods
@Test
void testWithAttachments() {
    if (page.locator("#attachment").count() > 0) {  // DON'T DO THIS
        // hardcoded attachment test
    }
}
```

**Rationale:**
- **Self-Maintaining**: New pages automatically tested without new test code
- **Component-Based**: Tests adapt to page content (grid, CRUD, attachments, comments, links, etc.)
- **No Duplication**: Single test class handles ALL pages
- **Extensible**: Add new component testers without touching test logic

### Rule 2: Create Component Testers, Not Page-Specific Tests (MANDATORY)

When you need to test a new UI component type (e.g., tags, calendar, charts):

```java
// ✅ CORRECT - Create a component tester
package automated_tests.tech.derbent.ui.automation.components;

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
        if (elementExists(page, "#cbutton-add-tag")) {
            LOGGER.info("         ✓ Add tag button found");
            // Perform actual operation test if possible
            try {
                page.locator("#cbutton-add-tag").click();
                waitMs(page, 500);
                checkForExceptions(page);
                LOGGER.info("         ✅ Add tag button works");
            } catch (Exception e) {
                LOGGER.warn("         ⚠️ Add tag failed: {}", e.getMessage());
            }
        }
        LOGGER.info("      ✅ Tag component test complete");
    }
}
```

Then register it in `CAdaptivePageTest` control signatures:

```java
private final IComponentTester tagTester = new CTagComponentTester();

private final List<IControlSignature> controlSignatures = List.of(
    CControlSignature.forSelector("Tag Signature", "#custom-tag-component, [id*='tag']", tagTester)
);
```

**Result:** Every page with tags is automatically tested—no additional test methods needed!

**Component Tester Best Practices:**
- ✅ **Validate Operations**: Don't just check existence—test button clicks work
- ✅ **Check Side Effects**: Verify grid counts change after Create/Delete
- ✅ **Open Tabs/Accordions**: If a signature is a tab/accordion, open it before CRUD checks (attachments/comments/links)
- ✅ **Handle Exceptions**: Catch and log errors gracefully
- ✅ **Generic Implementation**: Work across all pages, not page-specific
- ❌ **Never Assume**: Use try-catch for all interactions

### Rule 3: Component Testers Must Be Generic (MANDATORY)

```java
// ✅ CORRECT - Generic, works with any entity
public class CAttachmentComponentTester extends CBaseComponentTester {
    @Override
    public void test(final Page page) {
        // Generic test - works for Activities, Meetings, Issues, etc.
        if (elementExists(page, "#cbutton-add-attachment")) {
            LOGGER.info("         ✓ Add attachment button found");
        }
    }
}

// ❌ WRONG - Entity-specific logic
public class CAttachmentComponentTester extends CBaseComponentTester {
    @Override
    public void test(final Page page) {
        if (page.url().contains("activity")) {  // DON'T CHECK ENTITY TYPE
            // activity-specific test
        }
    }
}
```

**Rule:** Component testers must work generically across all pages—no entity-specific logic.

### Rule 4: Navigation via CPageTestAuxillary Buttons (MANDATORY)

```java
// ✅ CORRECT - CAdaptivePageTest uses buttons automatically
// Just run the test - it discovers all buttons itself
mvn test -Dtest=CAdaptivePageTest

// ❌ WRONG - Don't navigate via side menu
page.locator("vaadin-side-nav-item").filter(new Locator.FilterOptions().setHasText("Activities")).click();

// ❌ WRONG - Don't create custom navigation
page.navigate("http://localhost:8080/cdynamicpagerouter/activities");
```

**Rule:** Never use the side menu for Playwright navigation. The test support page is the only allowed entry point.  
**Rule:** Use `-Dtest.titleContains=...` or `-Dtest.targetButtonId=...` to filter targets when needed.  
**Rule:** To run every match for a keyword, pass `-Dtest.runAllMatches=true`.  

Example:
```bash
mvn test -Dtest=CAdaptivePageTest -Dtest.titleContains=user
mvn test -Dtest=CAdaptivePageTest -Dtest.titleContains=user -Dtest.runAllMatches=true
```

**How CAdaptivePageTest Works:**
1. Navigates to CPageTestAuxillary
2. Discovers all buttons with `[id^='test-aux-btn-']` selector
3. Selects target button (first by default or `-Dtest.targetButtonId=...`)
4. Clicks the test support button to navigate
5. Detects control signatures on the page
6. Runs component testers mapped to detected signatures

### Rule 5: Exception Detection is Automatic (MANDATORY)

Component testers inherit exception detection from `CBaseComponentTester`:

```java
// Automatically available in all component testers
protected boolean hasException(final Page page);  // Checks for exception dialogs
protected boolean elementExists(final Page page, final String selector);
protected boolean clickButton(final Page page, final String buttonId);
protected boolean fillField(final Page page, final String fieldId, final String value);
protected void wait_500(final Page page);
```

**Rule:** Use inherited utilities—don't reimplement common functionality.

### Example: Adding a New Component Tester

Scenario: You want to test calendar/schedule components.

**Step 1:** Create the tester:

```java
package automated_tests.tech.derbent.ui.automation.components;

public class CCalendarComponentTester extends CBaseComponentTester {
    
    private static final String CALENDAR_SELECTOR = "vaadin-calendar, [id*='calendar']";
    
    @Override
    public boolean canTest(final Page page) {
        return elementExists(page, CALENDAR_SELECTOR);
    }
    
    @Override
    public String getComponentName() {
        return "Calendar Component";
    }
    
    @Override
    public void test(final Page page) {
        LOGGER.info("      📅 Testing Calendar Component...");
        if (elementExists(page, "vaadin-calendar")) {
            LOGGER.info("         ✓ Calendar component detected");
            // Test calendar-specific functionality
        }
        LOGGER.info("      ✅ Calendar component test complete");
    }
}
```

**Step 2:** Register in `CAdaptivePageTest` control signatures:

```java
private final IComponentTester calendarTester = new CCalendarComponentTester();

private final List<IControlSignature> controlSignatures = List.of(
    CControlSignature.forSelector("Calendar Signature", "vaadin-calendar, [id*='calendar']", calendarTester)
);
```

**Step 3:** Done! Run test:

```bash
mvn test -Dtest=CAdaptivePageTest 2>&1 | tee /tmp/playwright.log
```

All pages with calendars are now automatically tested!

## ✅ Testing Rules Summary

1. **Browser Visible**: Default to visible mode for development
2. **Log to /tmp/playwright.log**: All Playwright output centralized
3. **Use CAdaptivePageTest**: Single test class for all pages (no page-specific tests)
4. **Create Component Testers**: Extend `CBaseComponentTester`, implement `IComponentTester`
5. **Navigate via CPageTestAuxillary**: Use test support buttons (no direct routes)
6. **Target with Button ID**: Use `-Dtest.targetButtonId` for a specific page
7. **Throw Exceptions**: Never silently ignore errors
8. **Fail-Fast**: Stop immediately on exceptions
9. **Generic Component Tests**: Work across all entities, no entity-specific logic
10. **Inherit Utilities**: Use `CBaseComponentTester` methods, don't reimplement
11. **Deterministic IDs**: All buttons/fields have stable IDs
12. **Run Until Success**: Don't stop until all tests pass

## 📁 Required Test Files

```
src/test/java/automated_tests/tech/derbent/ui/automation/
├── CBaseUITest.java                           ✅ Base test class with helpers
├── CPageTestAuxillaryComprehensiveTest.java  ✅ Main comprehensive test
├── CPageTestNewEntities.java                  ✅ Focused new entity tests
└── [Entity]CrudTest.java                      ✅ Individual entity tests

src/main/java/tech/derbent/api/views/
├── CPageTestAuxillary.java                    ✅ Navigation button page
└── CPageTestAuxillaryService.java             ✅ Route discovery service
```

## 🎯 Success Criteria

- ✅ Browser visible during all test runs
- ✅ All output logged to `/tmp/playwright.log`
- ✅ Navigation via CPageTestAuxillary buttons (not side menu)
- ✅ All exceptions logged and thrown
- ✅ Tests stop immediately on errors
- ✅ All CRUD operations tested per entity
- ✅ Custom components tested (kanban, attachments, comments, links)
- ✅ Screenshots captured at each step
- ✅ Tests filtered by keyword to skip passed pages
- ✅ Deterministic button/field IDs used throughout

---

**END OF TESTING STANDARDS**

## CSV Reporting Framework (Mandatory)

### Overview

The CSV reporting framework provides consistent, user-friendly report generation across all entity types.
All grid-based views MUST implement CSV export functionality using this framework.

### Implementation Pattern

**Step 1: Override actionReport() in Page Service**

```java
@Override
public void actionReport() throws Exception {
    LOGGER.debug("Report action triggered for CActivity");
    if (getView() instanceof CGridViewBaseDBEntity) {
        @SuppressWarnings("unchecked")
        final CGridViewBaseDBEntity<CActivity> gridView = (CGridViewBaseDBEntity<CActivity>) getView();
        gridView.generateGridReport();
    } else {
        super.actionReport();
    }
}
```

**Step 2: Grid View Implements Report Data Provider**

```java
/**
 * Returns the list of items currently displayed in the grid for CSV export.
 */
protected List<EntityClass> getGridItemsForReport() {
    // Get items from grid's data provider
    return grid.getListDataView().getItems().collect(Collectors.toList());
}

/**
 * Generates CSV report from grid data.
 */
public void generateGridReport() throws Exception {
    final List<EntityClass> items = getGridItemsForReport();
    CReportHelper.generateReport(items, entityClass);
}
```

### Field Discovery Rules

**Automatic Field Discovery:**
- All fields with `@AMetaData` annotations are discoverable
- Fields marked with `@AMetaData(hidden = true)` are excluded
- Static and transient fields are excluded
- Field display names come from `@AMetaData(displayName = "...")`

**Nested Entity Fields:**
- First-level nested fields are automatically included
- Common nested fields: name, description, color, icon
- Example: Activity.status.name, Activity.status.color

**Field Grouping:**
- Base fields grouped under "Base (EntityName)"
- Nested fields grouped under parent field name
- Example groups: "Base (Activity)", "Status", "Assigned To"

### CSV Export Standards

**File Format:**
- RFC 4180 compliant CSV format
- UTF-8 encoding with BOM for Excel compatibility
- CRLF line endings (\r\n)
- Comma delimiter (,)

**Value Formatting:**
- NULL values → empty string ""
- Entities → toString() or name if available
- Collections → semicolon-separated list "item1; item2; item3"
- Quotes escaped by doubling: " → ""
- Values with commas/quotes/newlines wrapped in quotes

**CSV Headers:**
- Format: "GroupName - FieldName" or just "FieldName"
- Example: "Base (Activity) - Name", "Status - Name"
- Derived from @AMetaData displayName

**Filename Convention:**
- Pattern: `{entityname}_{timestamp}.csv`
- Example: `activities_20240118_152030.csv`
- Timestamp format: yyyyMMdd_HHmmss
- Lowercase entity name (without C prefix)

### Dialog UX Standards

**Field Selection Dialog:**
- Max width: 800px
- Grouped checkbox layout
- "Select All" / "Deselect All" per group
- Two-column layout for groups with 6+ fields
- All fields selected by default
- Visual indicator for collection fields: "(List)"
- Minimum one field required validation

**Action Buttons:**
- Cancel (secondary) - closes dialog
- Generate CSV (primary, with download icon) - validates and exports

### Component Stack

```
User clicks Report button (CCrudToolbar)
    ↓
CPageService.actionReport()
    ↓
CGridViewBaseDBEntity.generateGridReport()
    ↓
CReportHelper.generateReport(data, entityClass)
    ↓
CReportFieldDescriptor.discoverFields(entityClass)
    ↓
CDialogReportConfiguration.open()
    ↓
User selects fields → clicks Generate
    ↓
CCSVExporter.exportToCSV(data, selectedFields, filename)
    ↓
Browser downloads CSV file
```

### Integration Checklist

For each new entity with grid view:

- [ ] Override `actionReport()` in page service
- [ ] Implement `getGridItemsForReport()` in grid view
- [ ] Ensure `@AMetaData` annotations on all exportable fields
- [ ] Set `hidden = true` for fields that should not be exported
- [ ] Provide meaningful `displayName` for all fields
- [ ] Test with empty data (shows warning)
- [ ] Test with large datasets (performance)
- [ ] Verify CSV opens correctly in Excel
- [ ] Verify special characters (quotes, commas, newlines) handled

### Best Practices

**Field Annotations:**
```java
@AMetaData(
    displayName = "Activity Name",
    order = 10,
    required = true
)
private String name;

@AMetaData(
    displayName = "Description",
    order = 20
)
private String description;

@AMetaData(
    displayName = "Internal ID",
    hidden = true  // Exclude from reports
)
private String internalCode;
```

**Error Handling:**
- Empty data → Warning notification, no dialog
- No fields → Warning notification, no dialog
- Field extraction error → Empty string, log debug
- CSV generation error → Exception dialog with details

**Performance:**
- Field discovery is cached per entity class
- Large datasets (1000+ records) handled efficiently
- Streaming CSV generation (no memory issues)

**User Experience:**
- Default: all fields selected
- Collections clearly marked with "(List)"
- Grouped fields for better organization
- Instant download after generation
- Success notification with record count

### Error Messages

**Standard Messages:**
- "No data available to export" - Empty entity list
- "No exportable fields found for this entity" - Field discovery failed
- "Please select at least one field to export" - No fields selected
- "Exporting N records to CSV" - Success message
- "Failed to generate report" - Generation error

### Testing Requirements

**Unit Tests:**
- CReportFieldDescriptor.discoverFields() for each entity
- CCSVExporter escaping (quotes, commas, newlines, nulls)
- CReportFieldDescriptor.extractValue() for nested fields

**Integration Tests:**
- Full report flow for at least one entity (Activity)
- Empty data handling
- Large dataset (100+ records)
- All field types (simple, entity, collection)
- Special characters in field values

**Manual Tests:**
- Open CSV in Excel (verify BOM works)
- Open CSV in Google Sheets
- Verify field grouping in dialog
- Verify Select All / Deselect All
- Verify minimum one field validation
