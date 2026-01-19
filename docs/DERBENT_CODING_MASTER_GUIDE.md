# Derbent Project - Master Coding Guide

**Version**: 3.0 (2026-01-17)  
**Status**: Authoritative Reference - Single Source of Truth  
**Purpose**: Complete coding guidelines for Derbent project development

---

## Document Structure

1. [Quick Start](#quick-start)
2. [Entity Implementation Guide](#entity-implementation-guide)
3. [Core Coding Standards](#core-coding-standards)
4. [Architecture Patterns](#architecture-patterns)
5. [Repository & Query Patterns](#repository--query-patterns)
6. [Service Layer Patterns](#service-layer-patterns)
7. [UI Component Standards](#ui-component-standards)
8. [Data Initialization](#data-initialization)
9. [Testing Guidelines](#testing-guidelines)
10. [Common Pitfalls](#common-pitfalls)
11. [Lessons Learned](#lessons-learned)

---

## Quick Start

### For New Entity Implementation

```bash
# STEP 1: Determine entity type
# Use decision tree in Section 2.1

# STEP 2: Create required files
# Domain: C{Entity}.java
# Repository: I{Entity}Repository.java
# Service: C{Entity}Service.java
# Initializer: C{Entity}InitializerService.java
# PageService: CPageService{Entity}.java (if needed)

# STEP 3: Register in CDataInitializer
# Add imports + initialize() + initializeSample() calls

# STEP 4: Verify
mvn clean compile
mvn spring-boot:run -Dspring.profiles.active=h2
./run-playwright-tests.sh menu
```

### For Code Reviews

**MUST verify:**
- [ ] Follows C-prefix naming convention
- [ ] Repository has JOIN FETCH for attachments/comments
- [ ] Service implements getEntityClass()
- [ ] Initializer has all 4 methods
- [ ] CDataInitializer registration complete
- [ ] All constants defined (5 mandatory)

---

## Entity Implementation Guide

### 2.1 Entity Type Decision Tree

**CRITICAL: Use this tree BEFORE creating any entity**

```
Is entity stored in database?
├─ NO → POJO or Component (no persistence)
└─ YES → Extend CEntityDB<T>
    │
    ├─ Has human-readable name?
    │  └─ YES → Extend CEntityNamed<T>
    │      │
    │      ├─ Company-scoped? (workflows, roles, types)
    │      │  └─ YES → Extend CEntityOfCompany<T>
    │      │      └─ Examples: CRole, CWorkflowBase, CValidationCaseType
    │      │
    │      └─ Project-scoped?
    │          └─ YES → Extend CEntityOfProject<T>
    │              │
    │              ├─ Work item with status workflow?
    │              │  └─ YES → Extend CProjectItem<T>
    │              │      └─ Examples: CActivity, CValidationCase, CMeeting
    │              │
    │              └─ NO → Stay at CEntityOfProject<T>
    │                  └─ Examples: CProject, CValidationSuite
    │
    └─ NO → Stay at CEntityDB<T>
        └─ Examples: CValidationStep (child), CValidationCaseResult
```

### 2.2 Entity Structure Template

**File**: `src/main/java/tech/derbent/app/{module}/{entity}/domain/C{Entity}.java`

```java
@Entity
@Table(name = "c{entity}")  // lowercase, no underscores
@AttributeOverride(name = "id", column = @Column(name = "{entity}_id"))
public class C{Entity} extends [BaseClass]<C{Entity}>
        implements [Interfaces] {

    // ============ MANDATORY CONSTANTS ============
    public static final String DEFAULT_COLOR = "#4169E1";
    public static final String DEFAULT_ICON = "vaadin:clipboard-check";
    public static final String ENTITY_TITLE_PLURAL = "Validation Cases";
    public static final String ENTITY_TITLE_SINGULAR = "Validation Case";
    public static final String VIEW_NAME = "Validation Cases View";

    // ============ FIELDS ============
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entitytype_id", nullable = true)
    @AMetaData(
        displayName = "Type",
        required = false,
        description = "Entity type",
        dataProviderBean = "C{Entity}TypeService",
        setBackgroundFromColor = true,
        useIcon = true
    )
    private C{Entity}Type entityType;

    // Attachments (if IHasAttachments)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "{entity}_id")
    @AMetaData(
        displayName = "Attachments",
        dataProviderBean = "CAttachmentService",
        createComponentMethod = "createComponent"
    )
    private Set<CAttachment> attachments = new HashSet<>();

    // Comments (if IHasComments)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "{entity}_id")
    @AMetaData(
        displayName = "Comments",
        dataProviderBean = "CCommentService",
        createComponentMethod = "createComponent"
    )
    private Set<CComment> comments = new HashSet<>();

    // ============ CONSTRUCTORS ============
    public C{Entity}() {
        super();
        initializeDefaults();
    }

    public C{Entity}(final String name, final CProject project) {
        super(C{Entity}.class, name, project);
        initializeDefaults();
    }

    // ============ INITIALIZATION ============
    @Override
    protected void initializeDefaults() {
        super.initializeDefaults();
        if (priority == null) priority = EPriority.MEDIUM;
        if (severity == null) severity = ESeverity.NORMAL;
    }

    // ============ INTERFACE IMPLEMENTATIONS ============
    @Override
    public Set<CAttachment> getAttachments() {
        if (attachments == null) attachments = new HashSet<>();
        return attachments;
    }

    @Override
    public void setAttachments(final Set<CAttachment> attachments) {
        this.attachments = attachments;
    }

    @Override
    public Set<CComment> getComments() {
        if (comments == null) comments = new HashSet<>();
        return comments;
    }

    @Override
    public void setComments(final Set<CComment> comments) {
        this.comments = comments;
    }

    @Override
    public CTypeEntity<?> getEntityType() {
        return entityType;
    }

    @Override
    public void setEntityType(CTypeEntity<?> typeEntity) {
        Check.notNull(typeEntity, "Type entity must not be null");
        Check.instanceOf(typeEntity, C{Entity}Type.class, 
            "Type entity must be instance of C{Entity}Type");
        entityType = (C{Entity}Type) typeEntity;
        updateLastModified();
    }

    @Override
    public CWorkflowEntity getWorkflow() {
        Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
        return entityType.getWorkflow();
    }

    // ============ GETTERS AND SETTERS ============
    public String getName() { return name; }
    
    public void setName(final String name) {
        this.name = name;
        updateLastModified();  // MANDATORY in all setters
    }
}
```

### 2.3 Required Interfaces by Entity Type

| Entity Type | Required Interfaces |
|-------------|---------------------|
| **Work Item** (CProjectItem) | `IHasStatusAndWorkflow<T>`, `IHasAttachments`, `IHasComments` |
| **Project Entity** | `IHasAttachments`, `IHasComments` (optional) |
| **Type Entity** (CTypeEntity) | None (base class handles) |
| **Child Entity** | None typically |
| **Sprintable Item** | `ISprintableItem` (optional) |
| **Gantt Display** | `IGanttDisplayable` (optional) |

### 2.4 Entity Constants (MANDATORY)

**ALL entities MUST define these 5 constants:**

```java
public static final String DEFAULT_COLOR = "#RRGGBB";       // Hex color for UI
public static final String DEFAULT_ICON = "vaadin:icon";    // Vaadin icon name
public static final String ENTITY_TITLE_PLURAL = "Items";   // Used by CEntityRegistry
public static final String ENTITY_TITLE_SINGULAR = "Item";  // Used by CEntityRegistry
public static final String VIEW_NAME = "Items View";        // View title
```

**Why Mandatory:**
- CEntityRegistry uses ENTITY_TITLE_* for type lookups
- UI components use DEFAULT_COLOR and DEFAULT_ICON
- Dynamic routing uses VIEW_NAME

### 2.5 File Organization by Entity Type

#### CProjectItem (Work Item with Type)

```
app/{module}/
├── {entity}/
│   ├── domain/
│   │   ├── C{Entity}.java              ← Main entity
│   │   ├── E{Entity}Priority.java      ← Enum (optional)
│   │   └── E{Entity}Severity.java      ← Enum (optional)
│   ├── service/
│   │   ├── I{Entity}Repository.java    ← Repository
│   │   ├── C{Entity}Service.java       ← Service
│   │   ├── C{Entity}InitializerService.java
│   │   └── CPageService{Entity}.java   ← Page service
│   └── view/
│       └── C{Entity}View.java          ← Custom view (if needed)
└── {entity}type/
    ├── domain/
    │   └── C{Entity}Type.java          ← Type entity
    └── service/
        ├── I{Entity}TypeRepository.java
        ├── C{Entity}TypeService.java
        ├── C{Entity}TypeInitializerService.java
        └── CPageService{Entity}Type.java

Total: 13 files (11 if no custom view)
```

#### Child Entity

```
app/{module}/{child}/
├── domain/
│   └── C{Child}.java                   ← Child entity
└── service/
    ├── I{Child}Repository.java         ← Extends IChildEntityRepository
    └── C{Child}Service.java            ← Service

Total: 3 files (no initializer - created via master)
```

---

## Core Coding Standards

### 3.1 C-Prefix Naming Convention (MANDATORY)

**Rule**: ALL custom classes MUST be prefixed with "C"

```java
// ✅ CORRECT
public class CActivity extends CProjectItem<CActivity> { }
public class CActivityService extends CEntityOfProjectService<CActivity> { }
public class CButton extends Button { }
public class CGrid<T> extends Grid<T> { }

// ❌ INCORRECT
public class Activity { }           // Missing C-prefix
public class ActivityService { }     // Missing C-prefix
```

**Exceptions**:
- Interfaces: Start with "I" (e.g., `IActivityRepository`)
- Enums: Start with "E" (e.g., `EActivityPriority`)
- Test classes: `C{Entity}Test`, `C{Entity}ServiceTest`
- Package names: lowercase without prefix

**Benefits**:
- Instant recognition of custom vs framework classes
- Enhanced IDE navigation and autocomplete
- AI-assisted development optimization

### 3.2 Import Organization (MANDATORY)

**Rule**: ALWAYS use import statements, NEVER full class names in code

```java
// ✅ CORRECT
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.projects.domain.CProject;

public class CActivityService {
    public CActivity createActivity(String name, CProject project) {
        CActivity activity = new CActivity(name, project);
        return save(activity);
    }
}

// ❌ INCORRECT - Full package paths in code
public class CActivityService {
    public tech.derbent.plm.activities.domain.CActivity createActivity(
            String name, tech.derbent.plm.projects.domain.CProject project) {
        // ...
    }
}
```

**Cast Expressions (CRITICAL)**:

```java
// ✅ CORRECT - Short name with import
import tech.derbent.plm.meetings.domain.CMeeting;

meetingService.save((CMeeting) item);

// ❌ INCORRECT - Full path in cast
meetingService.save((tech.derbent.plm.meetings.domain.CMeeting) item);
```

### 3.3 UI Component Naming (MANDATORY)

**Pattern**: `{type}{Name}` format

```java
// ✅ CORRECT - typeName format
private CButton buttonAdd;
private CButton buttonDelete;
private CButton buttonSave;
private CDialog dialogConfirmation;
private CVerticalLayout layoutMain;
private CHorizontalLayout layoutToolbar;
private CGrid<CEntity> gridItems;
private ComboBox<String> comboBoxStatus;
private TextField textFieldName;

// ❌ WRONG - Do NOT use
private CButton addBtn;              // Wrong format
private CButton btn_delete;          // Wrong format
private Button addButton;            // Missing C-prefix
```

**Event Handler Naming**: `on_{componentName}_{eventType}`

```java
// ✅ CORRECT
protected void on_buttonAdd_clicked() { }
protected void on_buttonDelete_clicked() { }
protected void on_gridItems_selected(CEntity item) { }
protected void on_comboBoxStatus_changed(String status) { }

// ❌ WRONG
private void handleAdd() { }
private void onDeleteClick() { }
```

**Factory Method Naming**: `create_{componentName}`

```java
// ✅ CORRECT
protected CButton create_buttonAdd() {
    final CButton button = new CButton(VaadinIcon.PLUS.create());
    button.addClickListener(e -> on_buttonAdd_clicked());
    return button;
}

// ❌ WRONG
protected CButton createAddButton() { }
protected CButton getDeleteButton() { }
```

### 3.4 C-Prefixed Components (MANDATORY)

**Rule**: NEVER use raw Vaadin components, ALWAYS use C-prefixed wrappers

| ❌ Vaadin Component | ✅ Use Instead |
|---------------------|----------------|
| `Button` | `CButton` |
| `Dialog` | `CDialog` |
| `VerticalLayout` | `CVerticalLayout` |
| `HorizontalLayout` | `CHorizontalLayout` |
| `Grid<T>` | `CGrid<T>` |
| `TextField` | `CTextField` |
| `FormLayout` | `CFormLayout` |

### 3.5 Notification Standards (CRITICAL)

**Rule**: NEVER use direct Vaadin Notification.show(), ALWAYS use unified system

**With Dependency Injection**:
```java
@Autowired
private CNotificationService notificationService;

// Toast notifications
notificationService.showSuccess("Data saved successfully");
notificationService.showError("Save failed");
notificationService.showWarning("Check your input");
notificationService.showInfo("Process completed");

// Modal dialogs
notificationService.showErrorDialog(exception);
notificationService.showWarningDialog("Important warning");
notificationService.showConfirmationDialog("Delete?", () -> deleteItem());

// Convenience methods
notificationService.showSaveSuccess();
notificationService.showDeleteSuccess();
notificationService.showOptimisticLockingError();
```

**Static Context (Utility Classes)**:
```java
import tech.derbent.api.ui.notifications.CNotifications;

CNotifications.showSuccess("Operation completed");
CNotifications.showError("Something went wrong");
CNotifications.showSaveSuccess();
```

**FORBIDDEN**:
```java
// ❌ NEVER use these
Notification.show("message");
new CWarningDialog("message").open();
```

### 3.6 Lambda vs Named Methods

**Rule**: Avoid complex lambda expressions, use named methods

```java
// ❌ WRONG - Complex lambda
buttonAdd.addClickListener(e -> {
    try {
        final CEntity newEntity = createNewEntity();
        service.save(newEntity);
        refreshGrid();
        CNotificationService.showSaveSuccess();
    } catch (final Exception ex) {
        LOGGER.error("Error", ex);
    }
});

// ✅ CORRECT - Delegate to named method
buttonAdd.addClickListener(e -> on_buttonAdd_clicked());

protected void on_buttonAdd_clicked() {
    try {
        final CEntity newEntity = createNewEntity();
        service.save(newEntity);
        refreshGrid();
        CNotificationService.showSaveSuccess();
    } catch (final Exception ex) {
        LOGGER.error("Error adding entity", ex);
        notificationService.showException("Error adding entity", ex);
    }
}
```

**Why**:
- Named methods can be overridden in subclasses
- Easier to test and debug
- Better stack traces
- More readable code

### 3.7 Validation and Error Handling

**Rule**: Use Check utility for fail-fast validation

```java
// ✅ CORRECT - Check utility
Objects.requireNonNull(entity, "Entity cannot be null");
Check.notBlank(name, "Name cannot be blank");
Check.notEmpty(list, "List cannot be empty");
Check.isTrue(value > 0, "Value must be positive");
Check.instanceOf(obj, CEntityNamed.class, "Must be CEntityNamed");

// ❌ INCORRECT - Manual checks
if (entity == null) {
    throw new IllegalArgumentException("Entity cannot be null");
}
```

**Developer Errors vs Runtime Errors**:

```java
// Developer Error - MUST throw exception
private String getEntityName(EntityClass item) {
    Objects.requireNonNull(item, "Item cannot be null");
    Check.instanceOf(item, CEntityNamed.class, "Item must be CEntityNamed");
    return ((CEntityNamed<?>) item).getName();
}

// Runtime Error - CAN handle gracefully
protected List<CStatus> statusProvider(T entity) {
    try {
        return entity.getValidNextStatuses();
    } catch (LazyInitializationException e) {
        LOGGER.debug("Lazy loading failed, falling back to all statuses");
        return statusService.findAll();
    }
}
```

### 3.8 Code Formatting

- **Indentation**: 4 spaces (no tabs)
- **Line length**: 140 characters (soft limit)
- **Braces**: Always use, even for single-line blocks
- **Final keyword**: Use for method parameters and local variables
- **Formatter**: Use `eclipse-formatter.xml` in IDE

```java
// ✅ CORRECT
public void processActivity(final CActivity activity) {
    final String name = activity.getName();
    if (name != null) {
        doSomething(name);
    }
}

// ❌ INCORRECT
public void processActivity(CActivity activity) {
    String name = activity.getName();
    if (name != null)
        doSomething(name);  // Missing braces
}
```

---

## Architecture Patterns

### 4.1 Multi-User & Concurrency (CRITICAL)

**Rule**: Services MUST be stateless (shared across ALL users)

```java
// ✅ CORRECT: Stateless Service
@Service
public class CActivityService extends CEntityOfProjectService<CActivity> {
    // ✅ GOOD: Only dependencies, no user state
    private final IActivityRepository repository;
    private final ISessionService sessionService;
    
    @Transactional(readOnly = true)
    public List<CActivity> getUserActivities() {
        // ✅ GOOD: Get user from session each time
        CUser currentUser = sessionService.getActiveUser()
            .orElseThrow(() -> new IllegalStateException("No active user"));
        return repository.findByUserId(currentUser.getId());
    }
}

// ❌ WRONG: Service with User State
@Service
public class CBadActivityService {
    // ❌ WRONG: User state stored in service (shared across users!)
    private CUser currentUser;
    private List<CActivity> cachedActivities;
}
```

**Service Field Rules**:

| Field Type | Allowed? | Example |
|------------|----------|---------|
| Repository dependency | ✅ Yes | `private final IUserRepository repository` |
| Clock dependency | ✅ Yes | `private final Clock clock` |
| Session service | ✅ Yes | `private final ISessionService sessionService` |
| Logger | ✅ Yes | `private static final Logger LOGGER` |
| Constants | ✅ Yes | `private static final String MENU_TITLE` |
| User context | ❌ No | `private CUser currentUser` **WRONG!** |
| User data cache | ❌ No | `private List<CEntity> userCache` **WRONG!** |
| Mutable static collections | ❌ No | `private static Map<Long, CUser>` **WRONG!** |

**Session State Management**:

```java
// ✅ DO: Use VaadinSession for user-specific state
@Service
public class CPreferenceService {
    public void savePreference(String preference) {
        VaadinSession.getCurrent().setAttribute("pref", preference);
    }
    
    public String getPreference() {
        return (String) VaadinSession.getCurrent().getAttribute("pref");
    }
}
```

### 4.2 Interface vs Inheritance

**When to use Inheritance**:
- Shared **identity** (id, name, etc.)
- Core **persistence behavior**
- **IS-A relationship** that cannot change

**When to use Interfaces**:
- Optional **capabilities** (can be added/removed)
- **Behavior contracts**
- Features for **multiple unrelated hierarchies**

**Example: ISprintableItem (Optional Capability)**:

```java
// ✅ GOOD: Interface for optional capability
public interface ISprintableItem {
    CSprintItem getSprintItem();
    Long getStoryPoint();
    void moveSprintItemToSprint(CSprint targetSprint);
}

// Entities opt-in
public class CActivity extends CProjectItem<CActivity> 
        implements ISprintableItem {
    @OneToOne(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    private CSprintItem sprintItem;
}

// NOT all project items need sprints
public class CRisk extends CProjectItem<CRisk> {
    // Does NOT implement ISprintableItem
}
```

### 4.3 Composition Over Inheritance

**Rule**: Use composition for mutable relationships

```java
// ✅ EXCELLENT: Sprint data is OWNED BY Activity (composition)
public class CSprintItem extends CEntityDB<CSprintItem> {
    @ManyToOne
    @JoinColumn(name = "sprint_id")
    private CSprint sprint;
    
    @OneToOne
    @JoinColumn(name = "activity_id")
    private CActivity activity;
}

public class CActivity extends CProjectItem<CActivity> 
        implements ISprintableItem {
    // GOOD: Activity OWNS sprint item
    @OneToOne(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    private CSprintItem sprintItem;
}
```

**Why Composition**:
- Sprint assignment is **mutable** (changes)
- Activity might have **no sprint item**
- Sprint item is **join table** with ordering
- Keeps Activity focused on **core responsibility**

### 4.4 Lazy Loading Patterns (CRITICAL)

**Problem**: Detached entities cause LazyInitializationException

**Pattern 1: Refresh Entity Before Access**:

```java
protected void on_grid_selectionChanged(SelectionEvent<CGrid<T>, T> event) {
    T selectedEntity = event.getFirstSelectedItem().orElse(null);
    
    if (selectedEntity != null && selectedEntity.getId() != null) {
        // CRITICAL: Refresh from database
        selectedEntity = service.getById(selectedEntity.getId())
            .orElse(selectedEntity);
    }
    
    updateToolbar(selectedEntity);  // Now safe
}
```

**Pattern 2: Graceful Fallback**:

```java
protected List<CStatus> statusProvider(T entity) {
    try {
        return entity.getValidNextStatuses();
    } catch (LazyInitializationException e) {
        LOGGER.debug("Lazy load failed, fallback to all statuses");
        return statusService.findAll();
    }
}
```

**Pattern 3: Eager Loading in Service** (See Repository section)

### 4.5 Child Entity Patterns

**Master-Detail Relationship**:

```java
// Child entity
public class CValidationStep extends CEntityDB<CValidationStep> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validationcase_id", nullable = false)
    private CValidationCase testCase;  // Master reference
    
    @Column(name = "step_order", nullable = false)
    private Integer stepOrder = 1;  // Ordering
}

// Master entity
public class CValidationCase extends CProjectItem<CValidationCase> {
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, 
               fetch = FetchType.LAZY, mappedBy = "testCase")
    private Set<CValidationStep> testSteps = new HashSet<>();
}
```

**Repository Pattern**:

```java
public interface IValidationStepRepository 
        extends IChildEntityRepository<CValidationStep, CValidationCase> {
    
    @Query("SELECT e FROM #{#entityName} e WHERE e.testCase = :master ORDER BY e.stepOrder ASC")
    List<CValidationStep> findByMaster(@Param("master") CValidationCase master);
    
    @Query("SELECT COALESCE(MAX(e.stepOrder), 0) + 1 FROM #{#entityName} e WHERE e.testCase = :master")
    Integer getNextItemOrder(@Param("master") CValidationCase master);
}
```

---

## Repository & Query Patterns

### 5.1 Repository Query Standards (MANDATORY)

**Rule 1: Override findById() with JOIN FETCH**

```java
@Override
@Query("""
        SELECT e FROM #{#entityName} e
        LEFT JOIN FETCH e.project
        LEFT JOIN FETCH e.assignedTo
        LEFT JOIN FETCH e.createdBy
        LEFT JOIN FETCH e.entityType et
        LEFT JOIN FETCH et.workflow
        LEFT JOIN FETCH e.status
        LEFT JOIN FETCH e.attachments    // MANDATORY if IHasAttachments
        LEFT JOIN FETCH e.comments        // MANDATORY if IHasComments
        WHERE e.id = :id
        """)
Optional<C{Entity}> findById(@Param("id") Long id);
```

**Why MANDATORY**:
- Prevents LazyInitializationException on detached entities
- UI frequently uses detached entities from grid selection
- Performance: Single query instead of N+1 queries

**Rule 2: Use Triple-Quote Multiline Format**

```java
// ✅ CORRECT - Readable multiline
@Query("""
        SELECT e FROM #{#entityName} e
        WHERE e.project = :project
        ORDER BY e.name ASC
        """)

// ❌ WRONG - Hard to read single line
@Query("SELECT e FROM #{#entityName} e WHERE e.project = :project ORDER BY e.name ASC")
```

**Rule 3: Always Include ORDER BY**

```java
// Named entities
ORDER BY e.name ASC

// Regular entities  
ORDER BY e.id DESC

// Sprintable items
ORDER BY e.sprintOrder ASC NULLS LAST, e.id DESC

// Child entities
ORDER BY e.itemOrder ASC
```

**Rule 4: Use #{#entityName} Placeholder**

```java
// ✅ CORRECT - Generic
SELECT e FROM #{#entityName} e

// ❌ WRONG - Hardcoded
SELECT e FROM CActivity e
```

### 5.2 Repository by Entity Type

**CProjectItem Repository**:

```java
public interface IActivityRepository extends IProjectItemRespository<CActivity> {
    
    @Override
    @Query("""
            SELECT a FROM #{#entityName} a
            LEFT JOIN FETCH a.attachments
            LEFT JOIN FETCH a.comments
            LEFT JOIN FETCH a.project
            LEFT JOIN FETCH a.assignedTo
            LEFT JOIN FETCH a.entityType et
            LEFT JOIN FETCH et.workflow
            WHERE a.id = :id
            """)
    Optional<CActivity> findById(@Param("id") Long id);
    
    @Override
    @Query("""
            SELECT a FROM #{#entityName} a
            WHERE a.project = :project
            ORDER BY a.id DESC
            """)
    Page<CActivity> listByProject(@Param("project") CProject project, Pageable pageable);
}
```

**CEntityOfCompany Repository (Type Entity)**:

```java
public interface IActivityTypeRepository 
        extends IEntityOfCompanyRepository<CActivityType> {
    
    // Simple query - no attachments/comments for type entities
    @Query("SELECT e FROM #{#entityName} e WHERE e.name = :name AND e.company.id = :companyId")
    CActivityType findByNameAndCompanyId(@Param("name") String name, 
                                         @Param("companyId") Long companyId);
}
```

**Child Entity Repository**:

```java
public interface IValidationStepRepository 
        extends IChildEntityRepository<CValidationStep, CValidationCase> {
    
    @Override
    @Query("""
            SELECT ts FROM #{#entityName} ts
            LEFT JOIN FETCH ts.testCase
            WHERE ts.testCase = :master
            ORDER BY ts.stepOrder ASC
            """)
    List<CValidationStep> findByMaster(@Param("master") CValidationCase master);
    
    @Override
    @Query("SELECT COUNT(ts) FROM #{#entityName} ts WHERE ts.testCase = :master")
    Long countByMaster(@Param("master") CValidationCase master);
    
    @Override
    @Query("SELECT COALESCE(MAX(ts.stepOrder), 0) + 1 FROM #{#entityName} ts WHERE ts.testCase = :master")
    Integer getNextItemOrder(@Param("master") CValidationCase master);
}
```

---

## Service Layer Patterns

### 6.1 Service Structure Template

```java
@Service
@PreAuthorize("isAuthenticated()")
@PermitAll
public class C{Entity}Service extends [BaseService]<C{Entity}> {

    private static final Logger LOGGER = LoggerFactory.getLogger(C{Entity}Service.class);
    private final I{Entity}Repository repository;

    public C{Entity}Service(final I{Entity}Repository repository,
                            final Clock clock,
                            final ISessionService sessionService) {
        super(repository, clock, sessionService);
        this.repository = repository;
    }

    @Override
    public Class<C{Entity}> getEntityClass() {
        return C{Entity}.class;
    }

    @Override
    public Class<?> getInitializerService() {
        return C{Entity}InitializerService.class;
    }
    
    // Custom business logic methods here
}
```

### 6.2 Base Service Types

| Entity Type | Base Service |
|-------------|--------------|
| Project Item | `CEntityOfProjectService<T>` |
| Project Entity | `CEntityOfProjectService<T>` |
| Company Entity | `CEntityOfCompanyService<T>` |
| Simple Entity | `CAbstractService<T>` |

### 6.3 Custom Service Methods

```java
// Business logic example
@Transactional
public void startTestExecution(CValidationSession testRun, CUser executedBy) {
    Check.notNull(testRun, "Test run cannot be null");
    Check.notNull(executedBy, "Executed by user cannot be null");
    
    testRun.setExecutedBy(executedBy);
    testRun.setExecutionStart(LocalDateTime.now());
    testRun.setResult(CValidationResult.IN_PROGRESS);
    
    save(testRun);
    LOGGER.info("Started test run: {} by user: {}", testRun.getId(), executedBy.getUsername());
}

// Calculation example
@Transactional(readOnly = true)
public Double calculatePassRate(CValidationSession testRun) {
    if (testRun.getTotalTestSteps() == null || testRun.getTotalTestSteps() == 0) {
        return 0.0;
    }
    return (testRun.getPassedTestSteps().doubleValue() / 
            testRun.getTotalTestSteps().doubleValue()) * 100.0;
}
```

### 6.4 Component Creation Methods (For @AMetaData Integration)

**Rule**: Services referenced by `@AMetaData(createComponentMethod)` MUST implement the specified method

**Pattern**: When an entity field uses `@AMetaData` with `createComponentMethod`, the service MUST provide that method:

```java
// In Domain Class
@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
@AMetaData(
    displayName = "Validation Steps",
    description = "Ordered test steps",
    dataProviderBean = "CValidationStepService",
    createComponentMethod = "createComponentListTestSteps"
)
private Set<CValidationStep> testSteps = new HashSet<>();

// In Service Class
@Service
public class CValidationStepService extends CAbstractService<CValidationStep> {
    
    public Component createComponentListTestSteps() {
        try {
            // Return actual component
            return new CComponentListValidationSteps(this, sessionService);
            
            // OR: Return placeholder during development
            final Div container = new Div();
            container.add(new Span("Validation Steps - Under Development"));
            return container;
        } catch (final Exception e) {
            LOGGER.error("Failed to create test step component.", e);
            final Div errorDiv = new Div();
            errorDiv.setText("Error loading component: " + e.getMessage());
            errorDiv.addClassName("error-message");
            return errorDiv;
        }
    }
}
```

**Key Requirements**:
1. Method MUST be `public` (called via reflection by CDataProviderResolver)
2. Method MUST return `Component`
3. Method name should be descriptive: `createComponent{Description}`
4. Method is called by `CFormBuilder` during metadata-driven form generation
5. Handle exceptions gracefully and return error component

**Common Patterns**:
- `createComponentListAttachments()` - CAttachmentService
- `createComponentListComments()` - CCommentService  
- `createComponentListTestSteps()` - CValidationStepService
- `createComponentListTestCases()` - CValidationCaseService
- `createSpritBacklogComponent()` - CPageServiceSprint (in PageService, not Service)

**Error Pattern** (if method missing):
```
java.lang.IllegalArgumentException: Method createComponentListTestSteps 
not found in class CValidationStepService$$SpringCGLIB$$0
```

**Why This Pattern Exists**:
- Enables metadata-driven UI generation
- Services can create context-aware components
- Supports dependency injection in components
- Allows lazy component instantiation

---

## UI Component Standards

### 7.1 Component Creation Pattern

```java
public class CComponentExample extends CVerticalLayout {

    // ============ FIELDS (typeName format) ============
    private final CButton buttonAdd;
    private final CButton buttonDelete;
    private final CGrid<CEntity> gridItems;
    private final CDialog dialogConfirmation;
    
    // ============ CONSTRUCTOR ============
    public CComponentExample() {
        // Create components
        buttonAdd = create_buttonAdd();
        buttonDelete = create_buttonDelete();
        gridItems = create_gridItems();
        dialogConfirmation = create_dialogConfirmation();
        
        // Layout
        add(buttonAdd, buttonDelete, gridItems);
    }
    
    // ============ FACTORY METHODS (create_xxx) ============
    protected CButton create_buttonAdd() {
        final CButton button = new CButton(VaadinIcon.PLUS.create());
        button.addClickListener(e -> on_buttonAdd_clicked());
        return button;
    }
    
    // ============ EVENT HANDLERS (on_xxx_eventType) ============
    protected void on_buttonAdd_clicked() {
        try {
            final CEntity entity = createNewEntity();
            service.save(entity);
            gridItems.setItems(service.findAll());
            notificationService.showSaveSuccess();
        } catch (final Exception ex) {
            LOGGER.error("Error adding entity", ex);
            notificationService.showException("Error", ex);
        }
    }
}
```

### 7.2 Grid Configuration

```java
protected CGrid<CEntity> create_gridItems() {
    final CGrid<CEntity> grid = new CGrid<>(CEntity.class);
    
    // Columns
    grid.addColumn(CEntity::getId).setHeader("ID").setAutoWidth(true);
    grid.addColumn(CEntity::getName).setHeader("Name").setFlexGrow(1);
    grid.addColumn(CEntity::getStatus).setHeader("Status").setAutoWidth(true);
    
    // Selection
    grid.setSelectionMode(Grid.SelectionMode.SINGLE);
    grid.addSelectionListener(e -> on_gridItems_selected(
        e.getFirstSelectedItem().orElse(null)));
    
    return grid;
}
```

### 7.3 CSS Utility Classes

**Use existing utility classes**:

```java
// Layout
component.addClassName("gap-m");          // Spacing
component.addClassName("padding-l");      // Padding
component.addClassName("width-full");     // Full width

// Flex layout
layout.addClassName("flex");              // Flex container
layout.addClassName("flex-wrap");         // Wrap items
layout.addClassName("justify-between");   // Space between
layout.addClassName("align-center");      // Center align

// Colors
button.addClassName("error");             // Error color
button.addClassName("success");           // Success color
button.addClassName("warning");           // Warning color
```

---

## Data Initialization

### 8.1 Initializer Service Structure

**MANDATORY: All 4 methods must be implemented**

```java
public class C{Entity}InitializerService extends CInitializerServiceBase {

    private static final Class<?> clazz = C{Entity}.class;
    private static final Logger LOGGER = LoggerFactory.getLogger(C{Entity}InitializerService.class);
    private static final String menuOrder = Menu_Order_PROJECT + ".XX";
    private static final String menuTitle = MenuTitle_PROJECT + ".{Entity}";
    private static final String pageDescription = "{Entity} management";
    private static final String pageTitle = "{Entity} Management";
    private static final boolean showInQuickToolbar = false;

    // 1. DETAIL VIEW STRUCTURE
    public static CDetailSection createBasicView(final CProject project) throws Exception {
        final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
        CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
        
        // Entity-specific fields
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "priority"));
        
        // Attachments (if IHasAttachments)
        CAttachmentInitializerService.addAttachmentsSection(detailSection, clazz);
        
        // Comments (if IHasComments)
        CCommentInitializerService.addCommentsSection(detailSection, clazz);
        
        // Audit section
        detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
        
        return detailSection;
    }

    // 2. GRID COLUMN CONFIGURATION
    public static CGridEntity createGridEntity(final CProject project) {
        final CGridEntity grid = createBaseGridEntity(project, clazz);
        grid.setColumnFields(List.of(
            "id", "name", "description", "entityType", "status",
            "assignedTo", "project", "createdDate"
        ));
        return grid;
    }

    // 3. SYSTEM REGISTRATION
    public static void initialize(final CProject project,
                                  final CGridEntityService gridEntityService,
                                  final CDetailSectionService detailSectionService,
                                  final CPageEntityService pageEntityService) throws Exception {
        final CDetailSection detailSection = createBasicView(project);
        final CGridEntity grid = createGridEntity(project);
        initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService,
                 detailSection, grid, menuTitle, pageTitle, pageDescription,
                 showInQuickToolbar, menuOrder);
    }

    // 4. SAMPLE DATA GENERATION
    public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
        final C{Entity}Service service = (C{Entity}Service) CSpringContext.getBean(
            CEntityRegistry.getServiceClassForEntity(clazz));
        
        // Clear existing
        final List<C{Entity}> existing = service.findAll();
        if (!existing.isEmpty()) {
            LOGGER.info("Clearing {} existing entities", existing.size());
            for (final C{Entity} item : existing) {
                try {
                    service.delete(item);
                } catch (final Exception e) {
                    LOGGER.warn("Could not delete: {}", e.getMessage());
                }
            }
        }
        
        // Sample data
        final String[][] nameAndDescriptions = {
            { "Name 1", "Description 1" },
            { "Name 2", "Description 2" },
            { "Name 3", "Description 3" }
        };
        
        initializeProjectEntity(nameAndDescriptions,
            (CEntityOfProjectService<?>) service, project, minimal,
            (item, index) -> {
                final C{Entity} entity = (C{Entity}) item;
                // Customize entity
                entity.setCustomField(value);
            });
    }
}
```

### 8.2 Type Entity Initializer (Special Case)

**CRITICAL: Type entities do NOT have createdBy field**

```java
public static CDetailSection createBasicView(final CProject project) throws Exception {
    final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
    CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
    detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "company"));
    detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "workflow"));
    
    detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
    // ❌ NO createdBy for type entities
    detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
    detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
    
    return detailSection;
}

public static CGridEntity createGridEntity(final CProject project) {
    final CGridEntity grid = createBaseGridEntity(project, clazz);
    // ❌ NO createdBy in grid columns
    grid.setColumnFields(List.of("id", "name", "description", "color", 
                                  "icon", "workflow", "company", "createdDate"));
    return grid;
}

public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
    final String[][] nameAndDescriptions = {
        { "Type 1", "Description 1" },
        { "Type 2", "Description 2" }
    };
    // Use initializeCompanyEntity (NOT initializeProjectEntity)
    initializeCompanyEntity(nameAndDescriptions,
        (CEntityOfCompanyService<?>) CSpringContext.getBean(
            CEntityRegistry.getServiceClassForEntity(clazz)),
        project.getCompany(),
        minimal,
        null);
}
```

### 8.3 CDataInitializer Registration (MANDATORY)

**File**: `src/main/java/tech/derbent/api/config/CDataInitializer.java`

**Step 1: Add Imports**

```java
import tech.derbent.plm.{module}.{entity}.service.C{Entity}InitializerService;
import tech.derbent.plm.{module}.{entitytype}.service.C{Entity}TypeInitializerService;
```

**Step 2: System Initialization (around line 730-790)**

```java
// Type entity (if exists)
C{Entity}TypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);

// Main entity
C{Entity}InitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
```

**Step 3: Sample Data - Types (around line 796-818)**

```java
if (project.getId().equals(sampleProject.getId())) {
    // Types only for first project (company-scoped)
    C{Entity}TypeInitializerService.initializeSample(sampleProject, minimal);
}
```

**Step 4: Sample Data - Entities (around line 820-844)**

```java
// Entities for all projects (project-scoped)
C{Entity}InitializerService.initializeSample(project, minimal);
```

**Order Matters**:
- Types before entities
- Dependencies before dependents
- Example: Scenarios → Validation Cases → Validation Sessions

---

## Testing Guidelines

### 9.1 Manual Testing Checklist

After implementation:

- [ ] Application starts without errors
- [ ] Menu item appears in correct location
- [ ] Grid displays entities
- [ ] Create operation works
- [ ] Edit operation works
- [ ] Delete operation works (if applicable)
- [ ] Form validation works
- [ ] Workflow transitions work (if applicable)
- [ ] Attachments work (if IHasAttachments)
- [ ] Comments work (if IHasComments)
- [ ] Sample data created on initialization

### 9.2 Playwright Tests

**File**: `src/test/java/automated_tests/tech/derbent/ui/automation/C{Entity}PlaywrightTest.java`

```java
@Test
void testNavigationTo{Entity}() {
    // Navigate to entity page
    navigateToPage("/{entity}");
    
    // Verify page loaded
    assertThat(page.locator("h1")).containsText("{Entity}");
}

@Test
void testCreate{Entity}() {
    navigateToPage("/{entity}");
    
    // Click create button
    page.locator("button[icon='vaadin:plus']").click();
    
    // Fill form
    page.locator("input[name='name']").fill("Test Entity");
    page.locator("textarea[name='description']").fill("Test Description");
    
    // Save
    page.locator("button:has-text('Save')").click();
    
    // Verify success
    assertThat(page.locator(".notification")).containsText("saved successfully");
}
```

### 9.3 Running Tests

```bash
# Setup environment
source ./bin/setup-java-env.sh

# Compile
mvn clean compile -DskipTests

# Start application
mvn spring-boot:run -Dspring.profiles.active=h2

# Run Playwright tests (separate terminal)
./run-playwright-tests.sh menu

# Run comprehensive tests
./run-playwright-tests.sh comprehensive
```

---

## Common Pitfalls

### 10.1 CRITICAL Mistakes to Avoid

#### ❌ Mistake 1: Missing createdBy on Type Entities

**Problem**:
```java
// Type entity initializer
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
// ERROR: java.lang.NoSuchFieldException: createdBy
```

**Solution**: Type entities (CTypeEntity) only have `createdDate` and `lastModifiedDate`

```java
// ✅ CORRECT
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
```

#### ❌ Mistake 2: Missing Attachments/Comments JOIN FETCH

**Problem**: LazyInitializationException when accessing attachments/comments on detached entities

**Solution**: ALWAYS add JOIN FETCH in findById()

```java
@Override
@Query("""
        SELECT e FROM #{#entityName} e
        LEFT JOIN FETCH e.attachments    // MANDATORY
        LEFT JOIN FETCH e.comments        // MANDATORY
        WHERE e.id = :id
        """)
Optional<C{Entity}> findById(@Param("id") Long id);
```

#### ❌ Mistake 3: Missing Services for Child Entities

**Problem**: Child/result entities without services cause runtime errors

**Solution**: ALL entities need services, even if no initializer

```java
@Service
public class CValidationStepService extends CAbstractService<CValidationStep> {
    @Override
    public Class<CValidationStep> getEntityClass() {
        return CValidationStep.class;
    }
}
```

#### ❌ Mistake 4: Missing CDataInitializer Registration

**Problem**: Entity not visible in menu, sample data not created

**Solution**: Add ALL THREE registrations:
1. Import statement
2. initialize() call
3. initializeSample() call

#### ❌ Mistake 5: No ORDER BY in Queries

**Problem**: Unpredictable ordering, different results on each load

**Solution**: ALL list queries MUST have ORDER BY

```java
// ✅ CORRECT
@Query("SELECT e FROM #{#entityName} e WHERE e.project = :project ORDER BY e.name ASC")

// ❌ WRONG
@Query("SELECT e FROM #{#entityName} e WHERE e.project = :project")
```

#### ❌ Mistake 6: User State in Service

**Problem**: Service stores user-specific state (shared across ALL users)

**Solution**: Services MUST be stateless

```java
// ❌ WRONG
@Service
public class CBadService {
    private CUser currentUser;  // SHARED ACROSS ALL USERS!
}

// ✅ CORRECT
@Service
public class CGoodService {
    @Transactional
    public void doSomething() {
        CUser user = sessionService.getActiveUser().orElseThrow();
        // Use user locally
    }
}
```

#### ❌ Mistake 7: Missing updateLastModified()

**Problem**: Last modified timestamp not updated

**Solution**: ALL setters MUST call updateLastModified()

```java
public void setName(final String name) {
    this.name = name;
    updateLastModified();  // MANDATORY
}
```

#### ❌ Mistake 8: Missing Entity Constants

**Problem**: CEntityRegistry cannot find entity, UI components fail

**Solution**: Define ALL 5 mandatory constants

```java
public static final String DEFAULT_COLOR = "#4169E1";
public static final String DEFAULT_ICON = "vaadin:icon";
public static final String ENTITY_TITLE_PLURAL = "Items";
public static final String ENTITY_TITLE_SINGULAR = "Item";
public static final String VIEW_NAME = "Items View";
```

#### ❌ Mistake 9: Wrong Base Class

**Problem**: Using CEntityDB when CProjectItem is needed

**Solution**: Use entity type decision tree (Section 2.1)

#### ❌ Mistake 10: Unverified @AMetaData References

**Problem**: dataProviderBean references non-existent service

**Solution**: Verify every service referenced in @AMetaData exists

```java
@AMetaData(dataProviderBean = "CValidationCaseResultService")  // Service MUST exist
private Set<CValidationCaseResult> results;
```

### 10.2 Detection Checklist

**Before committing, verify:**

- [ ] ✅ All classes use C-prefix
- [ ] ✅ Imports used (no full paths in code)
- [ ] ✅ Repository has JOIN FETCH for attachments/comments
- [ ] ✅ Service implements getEntityClass()
- [ ] ✅ Initializer has all 4 methods
- [ ] ✅ CDataInitializer has 3 registrations
- [ ] ✅ All queries have ORDER BY
- [ ] ✅ Type entities don't use createdBy
- [ ] ✅ All setters call updateLastModified()
- [ ] ✅ All entity constants defined

---

## Lessons Learned

### 11.1 Validation Module Implementation Insights

**From January 2026 implementation:**

**What We Initially Missed**:

1. **CValidationCaseType.createdBy** - Type entities don't have this field
2. **CValidationCaseResultService** - Child entities still need services
3. **CValidationStepResultService** - Result entities need services
4. **IValidationStepResultRepository** - Repository was missing
5. **Repository JOIN FETCH** - Inconsistent across repositories
6. **CDataInitializer calls** - Forgot to register all initializers

**Why We Missed Them**:
- No comprehensive checklist to follow
- Assumptions without verification
- Focused on main entities, overlooked children
- No systematic audit process

**Impact**:
- Multiple fix commits needed
- Runtime errors discovered late
- Inconsistent patterns

**Solutions Applied**:
- Created comprehensive checklist (176 verification points)
- Documented 10 new mandatory rules
- Pattern summary by entity type
- Systematic verification process

### 11.2 Key Takeaways

**Rule 1: Never Skip the Checklist**
- Follow decision tree for every entity
- Work through all sections systematically
- Check off every box

**Rule 2: Verify Base Class Fields**
- Don't assume all entities have same fields
- Check base class before adding to initializer
- Type entities are different from regular entities

**Rule 3: All Entities Need Services**
- Even child entities need services
- Even result entities need services
- No exceptions

**Rule 4: JOIN FETCH is Non-Negotiable**
- Always for attachments/comments in findById()
- Prevents LazyInitializationException
- Single query instead of N+1

**Rule 5: CDataInitializer is Three-Step**
- Import statement
- initialize() call in system phase
- initializeSample() call in data phase

**Rule 6: Compile After Each Major Step**
- Don't wait until end to compile
- Catch errors early
- Fix incrementally

**Rule 7: Test as You Go**
- Don't wait for complete implementation
- Test each component as created
- Faster feedback loop

### 11.3 Evolution of Patterns

**Version 1.0 (2025)**:
- Basic entity patterns
- Scattered documentation
- No unified checklist

**Version 2.0 (2026-01)**:
- Test module implementation
- Discovered missing patterns
- Created comprehensive checklist

**Version 3.0 (2026-01-17) - Current**:
- Consolidated all guidelines into this master guide
- 176 verification checkboxes
- 10 critical mistakes documented
- Complete pattern library

---

## Appendix: Quick Reference

### Entity Type Summary

| Type | Extends | Implements | Has Type Entity | Initializer Sample Method |
|------|---------|------------|-----------------|---------------------------|
| **Type** | CTypeEntity | - | No | initializeCompanyEntity() |
| **Work Item** | CProjectItem | IHasStatusAndWorkflow, IHasAttachments, IHasComments | Yes | initializeProjectEntity() |
| **Project Entity** | CEntityOfProject | IHasAttachments (optional) | No | initializeProjectEntity() |
| **Child** | CEntityDB | - | No | No initializer |

### File Count by Entity Type

| Type | Files |
|------|-------|
| Work Item + Type | 11-13 files |
| Project Entity | 6-8 files |
| Type Entity | 5 files |
| Child Entity | 3 files |

### Required Methods by Component

| Component | Required Methods |
|-----------|------------------|
| **Entity** | Constructors, initializeDefaults(), getters, setters, interface methods |
| **Repository** | findById() override, listByProject/Company(), custom queries |
| **Service** | getEntityClass(), getInitializerService(), custom business logic |
| **Initializer** | createBasicView(), createGridEntity(), initialize(), initializeSample() |
| **PageService** | bind(), interface methods |

---

## Version History

- **v3.0** (2026-01-17): Consolidated master guide from 28 separate documents
- **v2.0** (2026-01-17): Complete entity checklist with 176 checkboxes
- **v1.0** (2026-01-15): Initial scattered documentation

---

## Related Documentation

**This is the SINGLE SOURCE OF TRUTH. Other docs provide supplementary details:**

- Validation Module Implementation: `TEST_MANAGEMENT_IMPLEMENTATION_COMPLETE.md`
- Validation Module Audit: `TEST_MODULE_AUDIT_COMPLETE.md`
- Lessons Learned: `LESSONS_LEARNED_TEST_MODULE_2026-01.md`
- Copilot Guidelines: `.copilot/instructions.md`

---

**Last Updated**: 2026-01-17  
**Maintained By**: Development Team  
**Review Frequency**: After each new pattern discovery  
**Status**: Production - Authoritative Reference
