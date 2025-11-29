# Coding Standards and Best Practices

## Overview

This document defines the coding standards, naming conventions, and best practices for the Derbent project. Following these standards ensures consistency, maintainability, and optimal performance.

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
    
    // 1. Constants
    public static final String DEFAULT_COLOR = "#DC143C";
    private static final Logger LOGGER = LoggerFactory.getLogger(CEntity.class);
    
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
Check.notNull(entity, "Entity cannot be null");
Check.notBlank(name, "Name cannot be blank");
Check.notEmpty(list, "List cannot be empty");
Check.isTrue(value > 0, "Value must be positive");

// ❌ INCORRECT - Manual checks
if (entity == null) {
    throw new IllegalArgumentException("Entity cannot be null");
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
    Check.notNull(activity, "Activity cannot be null");
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
    Check.notNull(activity, "Activity cannot be null");
    
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

## Code Formatting

### Import Organization (Mandatory)

**ALWAYS use import statements instead of full class names:**

#### ✅ Correct
```java
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.projects.domain.CProject;

public class CActivityService {
    public CActivity createActivity(String name, CProject project) {
        CActivity activity = new CActivity(name, project);
        return save(activity);
    }
}
```

#### ❌ Incorrect
```java
public class CActivityService {
    public tech.derbent.app.activities.domain.CActivity createActivity(
            String name, tech.derbent.app.projects.domain.CProject project) {
        tech.derbent.app.activities.domain.CActivity activity = 
            new tech.derbent.app.activities.domain.CActivity(name, project);
        return save(activity);
    }
}
```

**Benefits**:
- Improved code readability
- Easier code maintenance
- Better IDE support and refactoring
- Reduced line length
- Consistent with Java best practices

**Rule**: Full package paths should only appear in import statements at the top of the file.

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
    Check.notNull(project, "Project cannot be null");
    Check.notNull(project.getId(), "Project must be persisted");
    
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

### Lazy Loading

```java
// Use LAZY for optional relationships
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "parent_id")
private CActivity parent;

// Use EAGER for required, frequently accessed relationships
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "project_id", nullable = false)
private CProject project;
```

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
        Check.notNull(master, "Master cannot be null");
        if (master.getId() == null) return List.of();
        return getTypedRepository().findByMaster(master);
    }
    
    /** Find by master ID. */
    public List<CSprintItem> findByMasterId(Long masterId) {
        Check.notNull(masterId, "Master ID cannot be null");
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

- [Entity Inheritance Patterns](entity-inheritance-patterns.md)
- [Service Layer Patterns](service-layer-patterns.md)
- [View Layer Patterns](view-layer-patterns.md)
- [Multi-User Singleton Advisory](multi-user-singleton-advisory.md) - **CRITICAL for service development**
- [Multi-User Development Checklist](../development/multi-user-development-checklist.md)
- [GitHub Copilot Guidelines](../development/copilot-guidelines.md)

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
        Check.notNull(sessionService, "Session service required");
        CCompany company = sessionService.getCurrentCompany();
        Check.notNull(company, "No active company");
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

