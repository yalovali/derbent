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
    order = 10,                        // Field display order
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
        return repository.findByCompanyId(currentCompany.getId());
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
        return repository.findByCompanyId(cachedCompany.getId());
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

