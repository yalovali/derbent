# Service Layer Patterns

## Overview

The Service Layer in Derbent implements the business logic tier of the MVC architecture. Services provide a consistent API for data access, business rules, and transaction management. This document describes the service inheritance hierarchy, patterns, and best practices.

## Service Layer Architecture

```
IAbstractRepository<T>
    ↓
CAbstractService<T>
    ↓
CEntityNamedService<T>
    ↓
CEntityOfProjectService<T>
    ↓
[Domain Services: CActivityService, CUserService, etc.]
```

## Base Interface: IAbstractRepository\<T>

**Location**: `src/main/java/tech/derbent/api/services/IAbstractRepository.java`

**Purpose**: Repository interface extending Spring Data JPA repositories.

**Key Features**:
- Extends `JpaRepository<EntityClass, Long>`
- Extends `JpaSpecificationExecutor<EntityClass>`
- Provides standard CRUD operations
- Supports dynamic queries via Specifications

**Usage Pattern**:
```java
public interface IAbstractRepository<EntityClass extends CEntityDB<EntityClass>>
    extends JpaRepository<EntityClass, Long>,
            JpaSpecificationExecutor<EntityClass> {
    // Additional custom query methods
}
```

## Base Service: CAbstractService\<T>

**Location**: `src/main/java/tech/derbent/api/services/CAbstractService.java`

**Purpose**: Abstract base service providing common CRUD operations for all entity types.

**Key Features**:
- Repository injection and management
- Clock injection for time-based operations
- Session service integration (optional)
- Transaction management
- Logging infrastructure
- Dependency checking interface
- Pagination support
- Search functionality

**Core Fields**:
```java
public abstract class CAbstractService<EntityClass extends CEntityDB<EntityClass>> {
    protected final Clock clock;
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    protected final IAbstractRepository<EntityClass> repository;
    protected @Nullable ISessionService sessionService;
}
```

**Core Constructor Pattern**:
```java
// Without session service (for global entities)
public CAbstractService(
    final IAbstractRepository<EntityClass> repository,
    final Clock clock) {
    this.clock = clock;
    this.repository = repository;
    this.sessionService = null;
    Check.notNull(repository, "repository cannot be null");
}

// With session service (for project-aware entities)
public CAbstractService(
    final IAbstractRepository<EntityClass> repository,
    final Clock clock,
    final ISessionService sessionService) {
    this.clock = clock;
    this.repository = repository;
    this.sessionService = sessionService;
    Check.notNull(repository, "repository cannot be null");
}
```

### Core CRUD Operations

#### Create
```java
@Transactional
public EntityClass createEntity() {
    try {
        final EntityClass entity = newEntity();
        repository.saveAndFlush(entity);
        return entity;
    } catch (final Exception e) {
        throw new RuntimeException("Failed to create instance", e);
    }
}

@Transactional
public EntityClass save(final EntityClass entity) {
    Check.notNull(entity, "Entity cannot be null");
    LOGGER.debug("Saving entity: {}", entity.getClass().getSimpleName());
    return repository.save(entity);
}
```

#### Read
```java
@Transactional(readOnly = true)
public Optional<EntityClass> findById(final Long id) {
    Check.notNull(id, "ID cannot be null");
    return repository.findById(id);
}

@Transactional(readOnly = true)
public List<EntityClass> findAll() {
    return repository.findAll();
}

@Transactional(readOnly = true)
public Page<EntityClass> findAll(final Pageable pageable) {
    return repository.findAll(pageable);
}
```

#### Update
```java
@Transactional
public EntityClass update(final EntityClass entity) {
    Check.notNull(entity, "Entity cannot be null");
    Check.notNull(entity.getId(), "Entity ID cannot be null");
    return repository.save(entity);
}
```

#### Delete
```java
@Transactional
public void delete(final EntityClass entity) {
    Check.notNull(entity, "Entity cannot be null");
    Check.notNull(entity.getId(), "Entity ID cannot be null");
    LOGGER.debug("Deleting entity: {}", entity.getClass().getSimpleName());
    repository.deleteById(entity.getId());
}

@Transactional
public void delete(final Long id) {
    Check.notNull(id, "Entity ID cannot be null");
    LOGGER.debug("Deleting entity with ID: {}", id);
    repository.deleteById(id);
}
```

### Dependency Checking Pattern

Services implement dependency checking to prevent deletion of entities in use:

```java
public String checkDeleteAllowed(final EntityClass entity) {
    Check.notNull(entity, "Entity cannot be null");
    Check.notNull(entity.getId(), "Entity ID cannot be null");
    return null; // Return null if delete is allowed, error message otherwise
}
```

**Implementation Example**:
```java
@Override
public String checkDeleteAllowed(final CActivityType type) {
    final String superCheck = super.checkDeleteAllowed(type);
    if (superCheck != null) {
        return superCheck;
    }
    
    // Check if type is marked as non-deletable
    if (type.getIsDeletable() != null && !type.getIsDeletable()) {
        return "This activity type is marked as non-deletable";
    }
    
    // Check if type is in use
    final long count = activityRepository.countByType(type);
    if (count > 0) {
        return String.format("Cannot delete activity type. " +
                           "It is being used by %d activities.", count);
    }
    
    return null; // Delete is allowed
}
```

### Search and Filtering

Services support search functionality through the `ISearchable` interface:

```java
@Transactional(readOnly = true)
public Page<EntityClass> search(
    final String searchTerm,
    final Pageable pageable) {
    
    if (searchTerm == null || searchTerm.isBlank()) {
        return findAll(pageable);
    }
    
    return repository.findAll(
        createSearchSpecification(searchTerm),
        pageable
    );
}
```

## Level 1: CEntityNamedService\<T>

**Location**: `src/main/java/tech/derbent/api/services/CEntityNamedService.java`

**Purpose**: Service for named entities with audit trail support.

**Additional Features**:
- Name-based queries
- Timestamp management (created/modified dates)
- Enhanced initialization logic

**Key Methods**:
```java
@Transactional(readOnly = true)
public Optional<EntityClass> findByName(final String name) {
    Check.notBlank(name, "Entity name cannot be null or empty");
    return ((IEntityNamedRepository<EntityClass>) repository)
        .findByName(name);
}

@Override
public void initializeNewEntity(final EntityClass entity) {
    super.initializeNewEntity(entity);
    final LocalDateTime now = LocalDateTime.now(clock);
    entity.setCreatedDate(now);
    entity.setLastModifiedDate(now);
}

@Override
public EntityClass save(final EntityClass entity) {
    entity.setLastModifiedDate(LocalDateTime.now(clock));
    return super.save(entity);
}
```

## Level 2: CEntityOfProjectService\<T>

**Location**: `src/main/java/tech/derbent/api/services/CEntityOfProjectService.java`

**Purpose**: Service for project-scoped entities with multi-tenant support.

**Additional Features**:
- Automatic project filtering
- Project-aware CRUD operations
- User assignment support
- Session integration

**Key Methods**:
```java
@Transactional(readOnly = true)
public List<EntityClass> listByProject(final CProject project) {
    Check.notNull(project, "Project cannot be null");
    return ((IEntityOfProjectRepository<EntityClass>) repository)
        .listByProject(project);
}

@Transactional(readOnly = true)
public long countByProject(final CProject project) {
    Check.notNull(project, "Project cannot be null");
    return ((IEntityOfProjectRepository<EntityClass>) repository)
        .countByProject(project);
}

@Transactional(readOnly = true)
public Optional<EntityClass> findByNameAndProject(
    final String name,
    final CProject project) {
    
    Check.notNull(project, "Project cannot be null");
    Check.notBlank(name, "Entity name cannot be null or empty");
    return ((IEntityOfProjectRepository<EntityClass>) repository)
        .findByNameAndProject(name, project);
}

@Override
public void initializeNewEntity(final EntityClass entity) {
    super.initializeNewEntity(entity);
    final CProject activeProject = sessionService.getActiveProject()
        .orElseThrow(() -> new IllegalStateException(
            "No active project in session"));
    final CUser currentUser = sessionService.getActiveUser()
        .orElseThrow(() -> new IllegalStateException(
            "No active user in session"));
    
    entity.setProject(activeProject);
    entity.setCreatedBy(currentUser);
}

@Override
public List<EntityClass> findAll() {
    final CProject project = sessionService.getActiveProject()
        .orElseThrow(() -> new IllegalStateException(
            "No active project selected"));
    return listByProject(project);
}
```

## Domain Service Implementation

Domain services extend the appropriate base service and add business logic.

### Example: CActivityService

```java
@Service
@PreAuthorize("isAuthenticated()")
public class CActivityService extends CEntityOfProjectService<CActivity> 
    implements IKanbanService<CActivity, CActivityStatus> {
    
    private final CActivityPriorityService activityPriorityService;
    private final CActivityStatusService activityStatusService;
    private final CActivityTypeService activityTypeService;
    
    public CActivityService(
        final IActivityRepository repository,
        final Clock clock,
        final ISessionService sessionService,
        final CActivityTypeService activityTypeService,
        final CActivityStatusService activityStatusService,
        final CActivityPriorityService activityPriorityService) {
        
        super(repository, clock, sessionService);
        this.activityTypeService = activityTypeService;
        this.activityStatusService = activityStatusService;
        this.activityPriorityService = activityPriorityService;
    }
    
    @Override
    protected Class<CActivity> getEntityClass() {
        return CActivity.class;
    }
    
    @Override
    public String checkDeleteAllowed(final CActivity activity) {
        final String superCheck = super.checkDeleteAllowed(activity);
        if (superCheck != null) {
            return superCheck;
        }
        
        // Add activity-specific deletion checks
        // Check for child activities, time logs, etc.
        
        return null;
    }
    
    // Business logic methods
    @Transactional(readOnly = true)
    public Map<CActivityStatus, List<CActivity>> 
        getActivitiesGroupedByStatus(final CProject project) {
        
        final List<CActivity> activities = 
            ((IEntityOfProjectRepository<CActivity>) repository)
                .listByProject(project);
        
        return activities.stream()
            .collect(Collectors.groupingBy(
                activity -> activity.getStatus() != null 
                    ? activity.getStatus() 
                    : createNoStatusInstance(project),
                Collectors.toList()));
    }
    
    @Transactional
    public void moveToStatus(
        final CActivity activity,
        final CActivityStatus newStatus) {
        
        Check.notNull(activity, "Activity cannot be null");
        Check.notNull(newStatus, "Status cannot be null");
        
        activity.setStatus(newStatus);
        activity.updateLastModified();
        save(activity);
        
        LOGGER.info("Moved activity {} to status {}",
                   activity.getName(), newStatus.getName());
    }
}
```

## Repository Patterns

### Base Repository Interface

```java
public interface IAbstractRepository<EntityClass extends CEntityDB<EntityClass>>
    extends JpaRepository<EntityClass, Long>,
            JpaSpecificationExecutor<EntityClass> {
}
```

### Named Entity Repository

```java
public interface IEntityNamedRepository<EntityClass extends CEntityNamed<EntityClass>>
    extends IAbstractRepository<EntityClass> {
    
    Optional<EntityClass> findByName(String name);
    
    List<EntityClass> findByNameContainingIgnoreCase(String name);
}
```

### Project Entity Repository

```java
public interface IEntityOfProjectRepository<EntityClass extends CEntityOfProject<EntityClass>>
    extends IEntityNamedRepository<EntityClass> {
    
    List<EntityClass> listByProject(CProject project);
    
    long countByProject(CProject project);
    
    Optional<EntityClass> findByNameAndProject(String name, CProject project);
    
    @Query("SELECT e FROM #{#entityName} e WHERE e.project = :project " +
           "AND e.active = true ORDER BY e.name")
    List<EntityClass> findActiveByProject(@Param("project") CProject project);
}
```

### Domain Repository Example

```java
public interface IActivityRepository 
    extends IEntityOfProjectRepository<CActivity> {
    
    // Custom query methods
    List<CActivity> findByStatus(CActivityStatus status);
    
    List<CActivity> findByAssignedTo(CUser user);
    
    @Query("SELECT a FROM CActivity a WHERE a.project = :project " +
           "AND a.status = :status ORDER BY a.priority DESC, a.name")
    List<CActivity> findByProjectAndStatus(
        @Param("project") CProject project,
        @Param("status") CActivityStatus status);
    
    @Query("SELECT COUNT(a) FROM CActivity a WHERE a.type = :type")
    long countByType(@Param("type") CActivityType type);
    
    @Query("SELECT a FROM CActivity a WHERE a.plannedEndDate < :date " +
           "AND a.status.isClosedStatus = false")
    List<CActivity> findOverdueActivities(@Param("date") LocalDate date);
}
```

## Best Practices

### 1. Use Appropriate Base Service

| Entity Type | Base Service | Use Case |
|-------------|-------------|----------|
| Simple entities with ID | `CAbstractService<T>` | System config |
| Named entities | `CEntityNamedService<T>` | Companies, global types |
| Project-scoped | `CEntityOfProjectService<T>` | Activities, risks |

### 2. Constructor Dependency Injection

Always use constructor injection, never field injection:

```java
// ✅ CORRECT
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

// ❌ INCORRECT - Field injection
@Service
public class CActivityService extends CEntityOfProjectService<CActivity> {
    @Autowired
    private CActivityTypeService typeService;
}
```

### 3. Security Annotations

Apply security at class level:

```java
@Service
@PreAuthorize("isAuthenticated()")
public class CActivityService extends CEntityOfProjectService<CActivity> {
    // All methods require authentication
}

// For more granular control
@Service
public class CUserService extends CEntityNamedService<CUser> {
    
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(CUser user) {
        // Only admins can delete users
    }
}
```

### 4. Transaction Management

Use `@Transactional` appropriately:

```java
// Read operations
@Transactional(readOnly = true)
public List<CActivity> findOverdue() {
    return repository.findOverdueActivities(LocalDate.now());
}

// Write operations
@Transactional
public CActivity createActivity(String name) {
    final CActivity activity = new CActivity(name, getCurrentProject());
    initializeNewEntity(activity);
    return save(activity);
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

### 5. Implement getEntityClass()

Always implement this method for proper instantiation:

```java
@Override
protected Class<CActivity> getEntityClass() {
    return CActivity.class;
}
```

### 6. Add Business Logic Methods

Services should contain business logic, not just CRUD:

```java
@Service
public class CActivityService extends CEntityOfProjectService<CActivity> {
    
    // ✅ GOOD - Business logic
    @Transactional
    public void completeActivity(CActivity activity) {
        Check.notNull(activity, "Activity cannot be null");
        
        CActivityStatus completedStatus = statusService
            .findCompletedStatus(activity.getProject());
        
        activity.setStatus(completedStatus);
        activity.setActualEndDate(LocalDate.now(clock));
        activity.updateLastModified();
        
        save(activity);
        
        LOGGER.info("Activity {} marked as completed", activity.getName());
    }
    
    // ✅ GOOD - Complex query
    @Transactional(readOnly = true)
    public ActivityStatistics getStatistics(CProject project) {
        List<CActivity> activities = listByProject(project);
        return ActivityStatistics.builder()
            .total(activities.size())
            .completed(countCompleted(activities))
            .overdue(countOverdue(activities))
            .build();
    }
}
```

### 7. Validation in Services

Validate inputs using Check utility:

```java
@Transactional
public CActivity createActivity(String name, CProject project) {
    Check.notBlank(name, "Activity name cannot be blank");
    Check.notNull(project, "Project cannot be null");
    Check.notNull(project.getId(), "Project must be persisted");
    
    // Check for duplicates
    Optional<CActivity> existing = findByNameAndProject(name, project);
    if (existing.isPresent()) {
        throw new IllegalArgumentException(
            "Activity with name '" + name + "' already exists");
    }
    
    CActivity activity = new CActivity(name, project);
    initializeNewEntity(activity);
    return save(activity);
}
```

### 8. Logging Best Practices

```java
// Use appropriate log levels
LOGGER.debug("Finding activity by ID: {}", id);  // Detailed info
LOGGER.info("Activity {} created successfully", name);  // Important events
LOGGER.warn("Activity {} is overdue by {} days", name, days);  // Warnings
LOGGER.error("Failed to save activity: {}", e.getMessage());  // Errors

// Include context in error logs
try {
    save(activity);
} catch (Exception e) {
    LOGGER.error("Error saving activity '{}' in project '{}': {}",
                activity.getName(), 
                activity.getProject().getName(),
                e.getMessage(), e);
    throw new RuntimeException("Failed to save activity", e);
}
```

## Testing Services

### Unit Test Example

```java
@ExtendWith(MockitoExtension.class)
class CActivityServiceTest {
    
    @Mock
    private IActivityRepository repository;
    
    @Mock
    private Clock clock;
    
    @Mock
    private ISessionService sessionService;
    
    @InjectMocks
    private CActivityService service;
    
    @Test
    void testCreateActivity() {
        // Given
        String name = "Test Activity";
        CProject project = new CProject("Test Project");
        project.setId(1L);
        
        when(sessionService.getActiveProject())
            .thenReturn(Optional.of(project));
        when(clock.instant())
            .thenReturn(Instant.now());
        when(repository.save(any(CActivity.class)))
            .thenAnswer(i -> i.getArgument(0));
        
        // When
        CActivity result = service.createActivity(name);
        
        // Then
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(project, result.getProject());
        verify(repository).save(any(CActivity.class));
    }
}
```

### Integration Test Example

```java
@SpringBootTest
@Transactional
class CActivityServiceIntegrationTest {
    
    @Autowired
    private CActivityService activityService;
    
    @Autowired
    private CProjectService projectService;
    
    @Test
    void testCompleteActivityWorkflow() {
        // Given
        CProject project = projectService.createProject("Test Project");
        CActivity activity = activityService.createActivity(
            "Test Activity", project);
        
        // When
        activityService.completeActivity(activity);
        
        // Then
        CActivity saved = activityService.findById(activity.getId())
            .orElseThrow();
        assertTrue(saved.isCompleted());
        assertNotNull(saved.getActualEndDate());
    }
}
```

## Related Documentation

- [Entity Inheritance Patterns](entity-inheritance-patterns.md) - Entity design patterns
- [View Layer Patterns](view-layer-patterns.md) - UI component patterns
- [Coding Standards](coding-standards.md) - Naming conventions and best practices
- [Dependency Checking System](../implementation/DEPENDENCY_CHECKING_SYSTEM.md) - Deletion protection patterns
