# Service Class Patterns

This document outlines the service layer architecture and patterns used in the Derbent project. Understanding these patterns is essential for creating consistent, maintainable business logic and proper GitHub Copilot usage.

## 🏗️ Service Inheritance Hierarchy

### Core Service Hierarchy
```
CEntityService<T>
    ↓
CProjectItemService<T>
    ↓
[Your Service Classes]
```

### Repository Hierarchy
```
CEntityRepository<T>
    ↓
CEntityOfProjectRepository<T>
    ↓
[Your Repository Interfaces]
```

## 📋 Base Service Classes

### CEntityService<T>
**Purpose**: Base service for all entity operations
**Location**: `tech.derbent.api.services.CEntityService`

```java
@Service
@PreAuthorize("isAuthenticated()")
public abstract class CEntityService<T extends CEntityDB<T>> {
    
    protected final CEntityRepository<T> repository;
    protected final Clock clock;
    
    protected CEntityService(CEntityRepository<T> repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }
    
    // Basic CRUD operations
    public T save(T entity) { /* Implementation */ }
    public Optional<T> findById(Long id) { /* Implementation */ }
    public List<T> findAll() { /* Implementation */ }
    public void delete(T entity) { /* Implementation */ }
}
```

**Key Features**:
- Generic type safety for entity operations
- Clock injection for audit trail
- Security annotations for authentication
- Standard CRUD operation patterns

### CProjectItemService<T>
**Purpose**: Service for project-scoped entities
**Location**: `tech.derbent.api.domains.CProjectItemService`

```java
@Service
@PreAuthorize("isAuthenticated()")
public abstract class CProjectItemService<T extends CProjectItem<T>> extends CEntityService<T> {
    
    protected final CSessionService sessionService;
    
    protected CProjectItemService(CEntityOfProjectRepository<T> repository, 
                                Clock clock, CSessionService sessionService) {
        super(repository, clock);
        this.sessionService = sessionService;
    }
    
    // Project-aware operations
    public List<T> listByProject(CProject project) { /* Implementation */ }
    public List<T> listByCurrentProject() { /* Implementation */ }
    public T createForProject(String name, CProject project) { /* Implementation */ }
}
```

**Key Features**:
- Project context awareness
- Session service integration
- Multi-tenant operation support
- Project-scoped queries

## 🛠️ Service Implementation Patterns

### Standard Business Service
```java
@Service
@PreAuthorize("isAuthenticated()")
public class CActivityService extends CProjectItemService<CActivity> 
    implements CKanbanService<CActivity, CActivityStatus> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CActivityService.class);
    
    // Constructor injection - required pattern
    public CActivityService(CActivityRepository repository, Clock clock, CSessionService sessionService) {
        super(repository, clock, sessionService);
    }
    
    // Business logic methods
    @Transactional(readOnly = true)
    public List<CActivity> findActivitiesByStatus(CActivityStatus status) {
        Check.notNull(status, "Status cannot be null");
        
        LOGGER.debug("Finding activities by status: {}", status.getName());
        
        final CProject currentProject = sessionService.getCurrentProject();
        return ((CActivityRepository) repository).findByProjectAndStatus(currentProject, status);
    }
    
    @Transactional
    public CActivity createActivity(String name, String description, CActivityType type) {
        Check.notBlank(name, "Activity name cannot be blank");
        
        LOGGER.debug("Creating activity: name={}, type={}", name, type != null ? type.getName() : "null");
        
        final CProject currentProject = sessionService.getCurrentProject();
        final CActivity activity = new CActivity(name, currentProject);
        activity.setDescription(description);
        activity.setActivityType(type);
        
        final CActivity saved = save(activity);
        
        LOGGER.info("Activity created successfully: id={}, name={}", saved.getId(), saved.getName());
        
        return saved;
    }
    
    // Kanban interface implementation
    @Override
    @Transactional(readOnly = true)
    public Map<CActivityStatus, List<CActivity>> getItemsGroupedByStatus(CProject project) {
        Check.notNull(project, "Project cannot be null");
        
        final List<CActivity> activities = listByProject(project);
        return activities.stream()
            .collect(Collectors.groupingBy(
                activity -> activity.getStatus() != null ? activity.getStatus() : createNoStatusInstance(project),
                Collectors.toList()
            ));
    }
    
    // Helper methods
    private CActivityStatus createNoStatusInstance(CProject project) {
        final CActivityStatus noStatus = new CActivityStatus("No Status", project);
        noStatus.setDescription("Activities without an assigned status");
        noStatus.setColor("#6c757d"); // Gray color
        return noStatus;
    }
}
```

### Type Entity Service
```java
@Service
@PreAuthorize("isAuthenticated()")
public class CActivityTypeService extends CProjectItemService<CActivityType> 
    implements DataProvider<CActivityType> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CActivityTypeService.class);
    
    public CActivityTypeService(CActivityTypeRepository repository, Clock clock, CSessionService sessionService) {
        super(repository, clock, sessionService);
    }
    
    // DataProvider implementation for ComboBox
    @Override
    @Transactional(readOnly = true)
    public List<CActivityType> getItems() {
        final CProject currentProject = sessionService.getCurrentProject();
        if (currentProject == null) {
            LOGGER.warn("No current project found for activity type lookup");
            return Collections.emptyList();
        }
        
        return listByProject(currentProject);
    }
    
    @Override
    public String getDisplayName(CActivityType item) {
        return item != null ? item.getName() : "";
    }
    
    // Type-specific business methods
    @Transactional
    public CActivityType createDefaultType(String name, String description, String color) {
        Check.notBlank(name, "Type name cannot be blank");
        
        final CProject currentProject = sessionService.getCurrentProject();
        final CActivityType type = new CActivityType(name, currentProject);
        type.setDescription(description);
        
        return save(type);
    }
    
    @Transactional(readOnly = true)
    public boolean isTypeNameExists(String name) {
        Check.notBlank(name, "Type name cannot be blank");
        
        final CProject currentProject = sessionService.getCurrentProject();
        return ((CActivityTypeRepository) repository).existsByNameAndProject(name, currentProject);
    }
}
```

### Initializer Service Pattern
```java
@Service
@Order(100) // Execution order for initialization
public class CActivityInitializerService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CActivityInitializerService.class);
    
    private final CActivityService activityService;
    private final CActivityTypeService activityTypeService;
    private final CActivityStatusService activityStatusService;
    private final CProjectService projectService;
    
    public CActivityInitializerService(CActivityService activityService,
                                     CActivityTypeService activityTypeService,
                                     CActivityStatusService activityStatusService,
                                     CProjectService projectService) {
        this.activityService = activityService;
        this.activityTypeService = activityTypeService;
        this.activityStatusService = activityStatusService;
        this.projectService = projectService;
    }
    
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeDefaultData() {
        LOGGER.info("Initializing default activity data");
        
        final List<CProject> projects = projectService.findAll();
        for (CProject project : projects) {
            initializeProjectData(project);
        }
        
        LOGGER.info("Default activity data initialization completed");
    }
    
    private void initializeProjectData(CProject project) {
        // Initialize types if none exist
        if (activityTypeService.listByProject(project).isEmpty()) {
            createDefaultTypes(project);
        }
        
        // Initialize statuses if none exist
        if (activityStatusService.listByProject(project).isEmpty()) {
            createDefaultStatuses(project);
        }
        
        // Create sample activities if none exist
        if (activityService.listByProject(project).isEmpty()) {
            createSampleActivities(project);
        }
    }
    
    private void createDefaultTypes(CProject project) {
        final List<String> defaultTypes = List.of(
            "Development", "Testing", "Documentation", "Meeting", "Research"
        );
        
        for (String typeName : defaultTypes) {
            if (!activityTypeService.isTypeNameExists(typeName)) {
                activityTypeService.createDefaultType(typeName, "Default " + typeName + " type", null);
            }
        }
    }
    
    private void createDefaultStatuses(CProject project) {
        final Map<String, String> defaultStatuses = Map.of(
            "Not Started", "#6c757d",
            "In Progress", "#007bff", 
            "Review", "#ffc107",
            "Complete", "#28a745",
            "Blocked", "#dc3545"
        );
        
        for (Map.Entry<String, String> entry : defaultStatuses.entrySet()) {
            if (!activityStatusService.isStatusNameExists(entry.getKey())) {
                activityStatusService.createDefaultStatus(entry.getKey(), 
                    "Default " + entry.getKey() + " status", entry.getValue());
            }
        }
    }
}
```

## 📊 Repository Patterns

### Standard Repository Interface
```java
@Repository
public interface CActivityRepository extends CEntityOfProjectRepository<CActivity> {
    
    // Project-scoped queries
    @Query("SELECT a FROM CActivity a JOIN FETCH a.activityType WHERE a.project = :project")
    List<CActivity> findByProjectWithType(@Param("project") CProject project);
    
    @Query("SELECT a FROM CActivity a WHERE a.project = :project AND a.status = :status")
    List<CActivity> findByProjectAndStatus(@Param("project") CProject project, 
                                          @Param("status") CActivityStatus status);
    
    @Query("SELECT a FROM CActivity a WHERE a.project = :project AND a.dueDate <= :date")
    List<CActivity> findOverdueActivities(@Param("project") CProject project, 
                                         @Param("date") LocalDate date);
    
    // Aggregate queries
    @Query("SELECT COUNT(a) FROM CActivity a WHERE a.project = :project AND a.status = :status")
    long countByProjectAndStatus(@Param("project") CProject project, 
                                @Param("status") CActivityStatus status);
    
    @Query("SELECT a.status, COUNT(a) FROM CActivity a WHERE a.project = :project GROUP BY a.status")
    List<Object[]> getStatusCounts(@Param("project") CProject project);
    
    // Existence checks
    boolean existsByNameAndProject(String name, CProject project);
    
    // Complex queries with joins
    @Query("""
        SELECT a FROM CActivity a 
        JOIN FETCH a.activityType 
        JOIN FETCH a.status 
        LEFT JOIN FETCH a.assignedUser 
        WHERE a.project = :project 
        ORDER BY a.priority DESC, a.dueDate ASC
    """)
    List<CActivity> findByProjectWithAllRelations(@Param("project") CProject project);
}
```

### Repository Method Naming Conventions
```java
// Find methods
findByProject(CProject project)
findByProjectAndStatus(CProject project, CActivityStatus status)
findByProjectAndAssignedUser(CProject project, CUser user)

// Count methods  
countByProject(CProject project)
countByProjectAndStatus(CProject project, CActivityStatus status)

// Existence checks
existsByNameAndProject(String name, CProject project)
existsByProjectAndStatus(CProject project, CActivityStatus status)

// Complex queries
findOverdueByProject(CProject project)
findCompletedInDateRange(CProject project, LocalDate start, LocalDate end)
```

## 🔐 Security Patterns

### Method-Level Security
```java
@Service
@PreAuthorize("isAuthenticated()")
public class CActivityService extends CProjectItemService<CActivity> {
    
    // Admin-only operations
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteAllActivities(CProject project) {
        // Implementation
    }
    
    // Project member access
    @PreAuthorize("@securityService.hasProjectAccess(#project)")
    public List<CActivity> getProjectActivities(CProject project) {
        // Implementation
    }
    
    // Owner or admin access
    @PreAuthorize("@securityService.isOwnerOrAdmin(#activity)")
    public CActivity updateActivity(CActivity activity) {
        // Implementation
    }
}
```

### Session-Based Security
```java
@Transactional(readOnly = true)
public List<CActivity> getCurrentUserActivities() {
    final CUser currentUser = sessionService.getCurrentUser();
    final CProject currentProject = sessionService.getCurrentProject();
    
    if (currentUser == null || currentProject == null) {
        return Collections.emptyList();
    }
    
    return ((CActivityRepository) repository).findByProjectAndAssignedUser(currentProject, currentUser);
}
```

## 🎯 Interface Implementation Patterns

### DataProvider Interface
```java
public class CActivityTypeService extends CProjectItemService<CActivityType> 
    implements DataProvider<CActivityType> {
    
    @Override
    @Transactional(readOnly = true)
    public List<CActivityType> getItems() {
        return listByCurrentProject();
    }
    
    @Override
    public String getDisplayName(CActivityType item) {
        return item != null ? item.getName() : "";
    }
    
    @Override
    public String getDisplayValue(CActivityType item) {
        return getDisplayName(item);
    }
}
```

### Kanban Service Interface  
```java
public class CActivityService extends CProjectItemService<CActivity> 
    implements CKanbanService<CActivity, CActivityStatus> {
    
    @Override
    @Transactional(readOnly = true)
    public Map<CActivityStatus, List<CActivity>> getItemsGroupedByStatus(CProject project) {
        // Implementation
    }
    
    @Override
    @Transactional
    public void moveItem(CActivity item, CActivityStatus newStatus) {
        // Implementation with audit trail
    }
}
```

## 🚫 Service Anti-Patterns

### Prohibited Practices
```java
// ❌ Don't use auxiliary setter methods
public void setAuxiliaryField(String value) { 
    // This pattern is forbidden
}

// ❌ Don't access session directly in services
HttpSession session = request.getSession(); // Wrong

// ❌ Don't use static service references
private static CActivityService activityService; // Wrong

// ❌ Don't create entities without proper validation
public CActivity createActivity(String name) {
    return new CActivity(name, null); // Missing project and validation
}
```

### Required Practices
```java
// ✅ Always use dependency injection
private final CActivityRepository repository;
private final CSessionService sessionService;

// ✅ Always validate parameters
Check.notNull(entity, "Entity cannot be null");
Check.notBlank(name, "Name cannot be blank");

// ✅ Use transactional annotations appropriately
@Transactional(readOnly = true) // For read operations
@Transactional              // For write operations

// ✅ Use project context
final CProject currentProject = sessionService.getCurrentProject();
```

## 📝 Service Documentation Standards

### Class-Level Documentation
```java
/**
 * Service for managing CActivity entities within project context.
 * Provides CRUD operations, kanban board support, and activity lifecycle management.
 * 
 * <p>This service handles:</p>
 * <ul>
 *   <li>Activity creation and modification</li>
 *   <li>Status transitions and workflow</li>
 *   <li>Project-scoped activity queries</li>
 *   <li>Kanban board organization</li>
 * </ul>
 * 
 * @author Derbent Framework
 * @since 1.0
 */
@Service
@PreAuthorize("isAuthenticated()")
public class CActivityService extends CProjectItemService<CActivity> {
```

### Method-Level Documentation
```java
/**
 * Creates a new activity with the specified parameters.
 * 
 * @param name the activity name (required, max 100 characters)
 * @param description optional activity description (max 500 characters)
 * @param type the activity type (optional)
 * @return the created and persisted activity
 * @throws IllegalArgumentException if name is blank
 * @throws SecurityException if user lacks project access
 */
@Transactional
public CActivity createActivity(String name, String description, CActivityType type) {
```

## 🔧 CAuxillaries Integration

### Service Class Registration
```java
// In CAuxillaries.java
public static Class<?> getServiceClass(String simpleName) {
    switch (simpleName) {
        case "CActivity":
            return CActivityService.class;
        case "CActivityType":
            return CActivityTypeService.class;
        // Add your services here
        default:
            throw new IllegalArgumentException("Unknown service for entity: " + simpleName);
    }
}
```

### Dynamic Service Resolution
```java
// Get service class dynamically
Class<?> serviceClass = CAuxillaries.getServiceClass("CActivity");

// Get initializer service  
Class<?> initializerClass = CAuxillaries.getInitializerService("CActivity");
```

## ✅ Service Checklist

When creating a new service, ensure:

### Required Elements
- [ ] Extends appropriate base class (CProjectItemService, CEntityService)
- [ ] Uses @Service and @PreAuthorize annotations
- [ ] Constructor injection for all dependencies
- [ ] Proper transaction annotations
- [ ] Parameter validation with Check utilities
- [ ] Logging with SLF4J
- [ ] Error handling and meaningful exceptions

### Optional Elements
- [ ] Implements relevant interfaces (DataProvider, CKanbanService)
- [ ] Business-specific query methods
- [ ] Batch operation methods
- [ ] Audit trail support
- [ ] Cache configuration
- [ ] Event publishing

### Integration Requirements
- [ ] Added to CAuxillaries service resolution
- [ ] Corresponding repository interface created
- [ ] Initializer service if default data needed
- [ ] Unit tests for business logic
- [ ] Integration tests for complex queries

This service architecture provides a robust foundation for business logic while maintaining consistency, security, and project-aware operations across the entire application.

## 🔧 Profile-Based Configuration and Testing

### Service Profile Configuration
Services in Derbent use Spring profiles to provide different implementations for different environments. This pattern is essential for database reset functionality and testing.

#### Profile-Based Session Service
```java
// Reset-db environment (non-web)
@Profile("reset-db")
@Service
public class CSessionService implements ISessionService {
    // Simple implementation without Vaadin dependencies
}

// Web environments (default, test, production)
@Service("CSessionService")
@ConditionalOnWebApplication
@Profile("!reset-db")
public class CWebSessionService implements ISessionService {
    // Full web implementation with Vaadin session management
}
```

#### Configuration Bridge Pattern
When services depend on concrete types but multiple profile-based implementations exist:

```java
@Configuration
public class CSessionServiceConfig {
    
    /**
     * Provides CSessionService bean for web environments by delegating to CWebSessionService.
     * This ensures type compatibility while maintaining profile separation.
     */
    @Bean
    @Primary
    @ConditionalOnWebApplication
    @Profile("!reset-db")
    public CSessionService sessionServiceDelegate(final CWebSessionService webSessionService,
                                                   final CUserRepository userRepository,
                                                   final CProjectRepository projectRepository) {
        return new CSessionService(userRepository, projectRepository) {
            // Delegate all operations to webSessionService
            @Override
            public Optional<CUser> getActiveUser() {
                return webSessionService.getActiveUser();
            }
            // ... other delegated methods
        };
    }
}
```

### Testing Profile Configuration

#### Test Profile Requirements
```java
// ✅ Correct test configuration
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class CBaseUITest {
    // Test will use CWebSessionService via delegation
}

// ✅ Infrastructure test (uses default profile)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = Application.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver"
})
public class PlaywrightInfrastructureTest {
    // Test will use CWebSessionService via delegation
}
```

#### Reset-DB Profile Testing
```java
// For testing database reset functionality
@SpringBootTest
@ActiveProfiles("reset-db")
@TestPropertySource(properties = {
    "spring.main.web-application-type=none",
    "spring.datasource.url=jdbc:h2:mem:resetdb"
})
public class DatabaseResetTest {
    // Test will use original CSessionService
}
```

### Profile Validation Checklist

When adding profile-based services, ensure:

#### Bean Availability
- [ ] **Default profile**: Web services available
- [ ] **Test profile**: Web services available  
- [ ] **Reset-db profile**: Non-web services available
- [ ] **Production profile**: Web services available

#### Type Compatibility
- [ ] Services expecting concrete types can inject appropriate beans
- [ ] Configuration bridges provided where needed
- [ ] Primary bean annotations used to resolve conflicts
- [ ] Profile conditions don't create bean gaps

#### Testing Requirements
- [ ] Unit tests pass with test profile
- [ ] Integration tests pass with default profile
- [ ] Infrastructure tests don't fail on missing beans
- [ ] Reset-db profile tests use correct implementation

### Common Profile Configuration Issues

#### ❌ Problematic Patterns
```java
// Creates bean gap - no service available for some profiles
@Profile("reset-db")
@Service
public class OnlyResetService implements IService { }

// No corresponding service for other profiles!
```

#### ✅ Correct Patterns  
```java
// Covers reset-db profile
@Profile("reset-db") 
@Service
public class CSessionService implements ISessionService { }

// Covers all other profiles
@Profile("!reset-db")
@ConditionalOnWebApplication
@Service("CSessionService")
public class CWebSessionService implements ISessionService { }

// Bridge for type compatibility if needed
@Configuration
public class ServiceConfig {
    @Bean
    @Primary
    @Profile("!reset-db")
    public CSessionService bridge(CWebSessionService web) {
        return new CSessionService(...) { /* delegate */ };
    }
}
```

### Profile Testing Strategy

#### Development Guidelines
1. **Always test both profiles** when modifying profile-based services
2. **Run infrastructure tests** to catch bean dependency issues early  
3. **Verify type compatibility** when services expect concrete classes
4. **Document profile requirements** in service comments

#### Continuous Integration  
```bash
# Test default profile
mvn test -Dtest=PlaywrightInfrastructureTest

# Test explicit test profile  
mvn test -Dtest=PlaywrightSimpleTest

# Test reset-db profile
mvn clean compile -Preset-db
```

This profile configuration pattern ensures that:
- ✅ Tests pass in all environments
- ✅ Database reset functionality works
- ✅ Web application runs properly
- ✅ No bean dependency injection failures occur
- ✅ Type compatibility is maintained across profiles