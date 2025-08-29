# Coding Rules and Standards for Derbent Project

## Overview

This document defines the coding rules and standards for the Derbent project, with special emphasis on data access patterns, entity management, and performance optimization.

## Entity Layer Rules

### Entity Design Guidelines

#### 1. Fetch Strategy Rules
```java
// ✅ CORRECT: EAGER for small lookup entities
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "status_id")
private CEntityStatus status;

// ✅ CORRECT: LAZY for collections and large objects
@OneToMany(fetch = FetchType.LAZY, mappedBy = "parent")
private Set<CChildEntity> children = new HashSet<>();

// ❌ INCORRECT: EAGER for collections
@OneToMany(fetch = FetchType.EAGER, mappedBy = "parent")
private Set<CChildEntity> children; // Will cause performance issues
```

#### 2. Collection Initialization
```java
// ✅ CORRECT: Initialize collections in constructor
public CMeeting() {
    super();
    this.attendees = new HashSet<>();
    this.participants = new HashSet<>();
}

// ❌ INCORRECT: Uninitialized collections
public CMeeting() {
    super();
    // Missing collection initialization
}
```

#### 3. Null Checking in Entity Methods
```java
// ✅ CORRECT: Defensive null checking
public void addParticipant(final CUser user) {
    if (user != null) {
        participants.add(user);
    }
}

// ❌ INCORRECT: No null checking
public void addParticipant(final CUser user) {
    participants.add(user); // NPE risk
}
```

### Entity Validation Rules

#### 1. Use Jakarta Validation Annotations
```java
// ✅ CORRECT: Proper validation
@Column(name = "name", nullable = false, length = 255)
@Size(max = 255, message = "Name cannot exceed 255 characters")
@NotBlank(message = "Name is required")
private String name;

// ❌ INCORRECT: Missing validation
@Column(name = "name")
private String name;
```

#### 2. AMetaData Annotations
```java
// ✅ CORRECT: Complete metadata
@AMetaData(
    displayName = "Meeting Type", 
    required = false, 
    readOnly = false,
    description = "Type category of the meeting", 
    hidden = false, 
    order = 2,
    dataProviderBean = "CMeetingTypeService"
)
private CMeetingType meetingType;
```

## Repository Layer Rules

### Query Optimization Rules

#### 1. Always Use JOIN FETCH for Detail Queries
```java
// ✅ CORRECT: JOIN FETCH prevents N+1 queries
@Query("SELECT m FROM CMeeting m " +
       "LEFT JOIN FETCH m.meetingType " +
       "LEFT JOIN FETCH m.status " +
       "LEFT JOIN FETCH m.responsible " +
       "WHERE m.id = :id")
Optional<CMeeting> findByIdWithEagerLoading(@Param("id") Long id);

// ❌ INCORRECT: No JOIN FETCH
@Query("SELECT m FROM CMeeting m WHERE m.id = :id")
Optional<CMeeting> findById(@Param("id") Long id);
```

#### 2. Separate Queries for Different Use Cases
```java
// ✅ CORRECT: Specific method for list views
@Query("SELECT m FROM CMeeting m " +
       "LEFT JOIN FETCH m.status " +
       "LEFT JOIN FETCH m.project " +
       "WHERE m.project = :project")
List<CMeeting> findByProjectForListView(@Param("project") CProject project);

// ✅ CORRECT: Comprehensive method for detail views  
@Query("SELECT m FROM CMeeting m " +
       "LEFT JOIN FETCH m.meetingType " +
       "LEFT JOIN FETCH m.status " +
       "LEFT JOIN FETCH m.responsible " +
       "LEFT JOIN FETCH m.relatedActivity " +
       "WHERE m.id = :id")
Optional<CMeeting> findByIdWithEagerLoading(@Param("id") Long id);
```

#### 3. Use DISTINCT for Collection Fetches
```java
// ✅ CORRECT: DISTINCT prevents duplicates
@Query("SELECT DISTINCT m FROM CMeeting m " +
       "LEFT JOIN FETCH m.participants " +
       "WHERE m.project = :project")
List<CMeeting> findByProjectWithParticipants(@Param("project") CProject project);
```

### Repository Interface Rules

#### 1. Extend Appropriate Base Repository
```java
// ✅ CORRECT: Extend specific base repository for project entities
public interface CActivityRepository extends CEntityOfProjectRepository<CActivity> {
    // Activity-specific methods with optimized eager loading
}

// ✅ CORRECT: Extend named entity repository for standalone entities
public interface CUserRepository extends CAbstractNamedRepository<CUser> {
    // User-specific methods
}

// ✅ CORRECT: Extend base repository for simple entities
public interface CActivityTypeRepository extends CAbstractRepository<CActivityType> {
    // Type-specific methods
}
```

#### 2. Method Naming Conventions and Required Patterns
```java
// ✅ REQUIRED: Optimized eager loading for detail views
@Query("SELECT e FROM EntityName e " +
       "LEFT JOIN FETCH e.type " +
       "LEFT JOIN FETCH e.status " +
       "LEFT JOIN FETCH e.project " +
       "WHERE e.id = :id")
Optional<EntityName> findByIdWithEagerLoading(@Param("id") Long id);

// ✅ REQUIRED: Project-aware list queries
@Override
@Query("SELECT e FROM EntityName e " +
       "LEFT JOIN FETCH e.type " +
       "LEFT JOIN FETCH e.status " +
       "WHERE e.project = :project")
List<EntityName> findByProject(@Param("project") CProject project, Pageable pageable);

// ❌ INCORRECT: Generic names without eager loading strategy
Optional<EntityName> findWithDetails(@Param("id") Long id);
List<EntityName> findSpecial();
```

#### 3. Performance-Optimized Query Patterns
```java
// ✅ CORRECT: Separate queries for different use cases
@Query("SELECT e FROM EntityName e " +
       "LEFT JOIN FETCH e.basicField1 " +
       "LEFT JOIN FETCH e.basicField2 " +
       "WHERE e.project = :project")
List<EntityName> findByProjectForListView(@Param("project") CProject project);

@Query("SELECT e FROM EntityName e " +
       "LEFT JOIN FETCH e.basicField1 " +
       "LEFT JOIN FETCH e.basicField2 " +
       "LEFT JOIN FETCH e.complexRelationship " +
       "LEFT JOIN FETCH e.heavyRelationship " +
       "WHERE e.id = :id")
Optional<EntityName> findByIdWithEagerLoading(@Param("id") Long id);
```

## Service Layer Rules

### Service Design Rules

#### 1. Service Constructor and Security Patterns
```java
// ✅ CORRECT: Modern service pattern with security
@Service
@PreAuthorize("isAuthenticated()")
public class CActivityService extends CEntityOfProjectService<CActivity> 
        implements CKanbanService<CActivity, CActivityStatus> {
    
    public CActivityService(final CActivityRepository repository, final Clock clock, final CSessionService sessionService){
        super(repository, clock,sessionService);
    }
    
    // Service methods with proper authorization
}
```

#### 2. Optimized findById Implementation
```java
// ✅ CORRECT: Optimized findById with validation
public CActivity findById(final Long id) {
    Check.notNull(id, "Activity ID cannot be null");
    return ((CActivityRepository) repository).findByIdWithEagerLoading(id).orElse(null);
}

// ❌ INCORRECT: Using default implementation without optimization
// Don't override if no optimization is needed
```

#### 3. Specialized Service Interface Implementation
```java
// ✅ CORRECT: Implement domain-specific interfaces
public class CActivityService extends CEntityOfProjectService<CActivity>
        implements CKanbanService<CActivity, CActivityStatus> {
    
    @Override
    @Transactional(readOnly = true)
    public Map<CActivityStatus, List<CActivity>> getEntitiesGroupedByStatus(final CProject project) {
        final List<CActivity> activities = findByProject(project);
        return activities.stream().collect(Collectors.groupingBy(
            activity -> activity.getStatus() != null ? activity.getStatus() : createNoStatusInstance(project),
            Collectors.toList()));
    }
}
```

#### 2. Avoid Redundant Service Methods - Enhanced Guidelines
```java
// ❌ INCORRECT: Redundant auxiliary methods (DEPRECATED)
@Deprecated
public CActivity setActivityType(CActivity activity, CActivityType type, String description) {
    activity.setActivityType(type);
    activity.setDescription(description);
    return save(activity);
}

@Deprecated  
public CActivity setAssignedUsers(CActivity activity, CUser assignedTo, CUser createdBy) {
    activity.setAssignedTo(assignedTo);
    activity.setCreatedBy(createdBy);
    return save(activity);
}

// ✅ CORRECT: Use entity setters directly with enhanced binder
activity.setActivityType(type);
activity.setDescription(description);
activity.setAssignedTo(assignedTo);
activity.setCreatedBy(createdBy);
activityService.save(activity); // Single save operation

// ✅ CORRECT: Business logic methods that add real value
@Transactional
public CActivity startActivity(final CActivity activity) {
    validateActivityCanBeStarted(activity);
    activity.setStatus(findStatusByName("IN_PROGRESS"));
    activity.setStartDate(LocalDate.now());
    updateRelatedActivities(activity);
    return save(activity);
}
```

#### 3. Enhanced Lazy Field Initialization Best Practices
```java
// ✅ CORRECT: Comprehensive lazy initialization with automatic detection
@Override
protected void initializeLazyFields(final CActivity entity) {
    if (entity == null) {
        return;
    }
    super.initializeLazyFields(entity); // Handles CEntityOfProject automatically
    
    // Only initialize relationships not handled by JOIN FETCH queries
    // These are typically complex collections or rarely accessed fields
    initializeLazyRelationship(entity.getSubActivities());
    initializeLazyRelationship(entity.getComments());
    initializeLazyRelationship(entity.getAttachments());
}

// ❌ INCORRECT: Initializing eagerly loaded fields
@Override
protected void initializeLazyFields(final CActivity entity) {
    initializeLazyRelationship(entity.getStatus()); // Already EAGER in @ManyToOne
    initializeLazyRelationship(entity.getActivityType()); // Already loaded via JOIN FETCH
    initializeLazyRelationship(entity.getProject()); // Handled by super class
}

// ✅ CORRECT: Entity-specific validation with helper methods  
private CActivityStatus createNoStatusInstance(final CProject project) {
    final CActivityStatus noStatus = new CActivityStatus("No Status", project);
    noStatus.setDescription("Activities without an assigned status");
    return noStatus;
}
```

### Transaction Management Rules

#### 1. Use @Transactional Appropriately
```java
// ✅ CORRECT: Read-only for query methods
@Transactional(readOnly = true)
public List<CMeeting> findByProject(CProject project) {
    return repository.findByProject(project);
}

// ✅ CORRECT: Writable for update methods
@Transactional
public CMeeting save(CMeeting meeting) {
    return repository.save(meeting);
}
```

#### 2. Service Class Transaction Configuration
```java
// ✅ CORRECT: Default read-only with selective write operations
@Service
@Transactional(readOnly = true)
public class CMeetingService extends CEntityOfProjectService<CMeeting> {
    
    @Transactional // Override for write operations
    public CMeeting save(CMeeting meeting) {
        return repository.save(meeting);
    }
}
```

## Performance Rules

### Query Performance Rules

#### 1. Monitor N+1 Query Problems with Enhanced Detection
```java
// ✅ CORRECT: Log and monitor queries in development
# application-dev.properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

// ✅ CORRECT: Service-level query monitoring
@Override
public List<CActivity> findByProject(final CProject project) {
    final long startTime = System.currentTimeMillis();
    final List<CActivity> results = super.findByProject(project);
    LOGGER.debug("findByProject took {}ms, returned {} activities", 
                System.currentTimeMillis() - startTime, results.size());
    return results;
}
```

#### 2. Enhanced Pagination Patterns
```java
// ✅ CORRECT: Repository-level pagination with eager loading
@Query("SELECT e FROM CActivity e " +
       "LEFT JOIN FETCH e.activityType " +
       "LEFT JOIN FETCH e.status " +
       "WHERE e.project = :project")
Page<CActivity> findByProjectWithEagerLoading(@Param("project") CProject project, Pageable pageable);

// ✅ CORRECT: Service-level pagination with project context
@Transactional(readOnly = true)
public List<CActivity> findByProjectPaginated(final CProject project, final int page, final int size) {
    final Pageable pageable = PageableUtils.createPageable(page, size);
    return ((CActivityRepository) repository).findByProjectWithEagerLoading(project, pageable).getContent();
}
```

#### 3. Optimized Data Loading Strategies
```java
// ✅ CORRECT: Different queries for different use cases
@Query("SELECT a.id, a.name, a.status.name FROM CActivity a WHERE a.project = :project")
List<Object[]> findBasicInfoByProject(@Param("project") CProject project);

@Query("SELECT a FROM CActivity a " +
       "LEFT JOIN FETCH a.activityType " +
       "LEFT JOIN FETCH a.status " +
       "LEFT JOIN FETCH a.assignedTo " +
       "WHERE a.project = :project")  
List<CActivity> findByProjectForDetailView(@Param("project") CProject project);

// ❌ INCORRECT: Loading full entities for list views
@Query("SELECT a FROM CActivity a " +
       "LEFT JOIN FETCH a.subActivities " +
       "LEFT JOIN FETCH a.comments " +
       "WHERE a.project = :project")
List<CActivity> findByProjectForListView(@Param("project") CProject project); // Too much data
```

## Code Documentation Rules

### JavaDoc Standards

#### 1. Document All Public Methods
```java
/**
 * Find meeting by ID with optimized eager loading.
 * Uses repository method with JOIN FETCH to prevent N+1 queries.
 * @param id the meeting ID
 * @return the meeting with eagerly loaded associations, or null if not found
 */
@Override
public CMeeting findById(final Long id) {
    // Implementation
}
```

#### 2. Explain Performance Implications
```java
/**
 * Finds meetings by participant user with eager loading of associations.
 * This method uses JOIN FETCH to prevent N+1 queries when accessing
 * meeting type, status, and responsible user information.
 * @param userId the user ID
 * @return list of meetings where the user is a participant
 */
@Query("SELECT DISTINCT m FROM CMeeting m " +
       "LEFT JOIN FETCH m.meetingType " +
       "LEFT JOIN FETCH m.status " +
       "LEFT JOIN FETCH m.responsible " +
       "JOIN m.participants p WHERE p.id = :userId")
List<CMeeting> findByParticipantId(@Param("userId") Long userId);
```

### Code Comments

#### 1. Explain Complex Logic
```java
// Initialize only the lazy collections that aren't handled by eager queries
// Note: meetingType, status, responsible are now eagerly loaded via JOIN FETCH
initializeLazyRelationship(entity.getParticipants());
initializeLazyRelationship(entity.getAttendees());
```

#### 2. Mark Deprecated Code
```java
/**
 * @deprecated Use entity setters directly instead of this auxiliary method.
 * This method will be removed in favor of direct entity manipulation.
 */
@Deprecated
public CMeeting setParticipants(CMeeting meeting, Set<CUser> participants) {
    // Implementation
}
```

## Testing Rules

### Unit Test Requirements

#### 1. Test Repository Queries with Current Patterns
```java
@Test
void testFindByIdWithEagerLoading() {
    // Given - Create test entity with relationships
    final CProject project = createTestProject();
    final CActivityType activityType = createTestActivityType(project);
    final CActivity activity = createTestActivity(project, activityType);
    activityRepository.save(activity);
    
    // When - Use optimized repository method
    final Optional<CActivity> result = activityRepository.findByIdWithEagerLoading(activity.getId());
    
    // Then - Verify eager loading worked
    assertThat(result).isPresent();
    assertThat(result.get().getActivityType()).isNotNull(); // Verify eager loading
    assertThat(result.get().getProject()).isNotNull(); // Verify eager loading
    assertThat(result.get().getStatus()).isNotNull(); // Verify eager loading
}

@Test
void testFindByProjectWithPagination() {
    // Given - Create multiple test activities
    final CProject project = createTestProject();
    createMultipleTestActivities(project, 15);
    
    // When - Use paginated query
    final Pageable pageable = PageableUtils.createPageable(0, 10);
    final List<CActivity> results = activityRepository.findByProject(project, pageable);
    
    // Then - Verify pagination works
    assertThat(results).hasSize(10);
    assertThat(results.get(0).getProject()).isEqualTo(project);
}
```

#### 2. Test Service Layer Optimizations with Enhanced Patterns
```java
@Test
void testServiceUsesOptimizedRepositoryMethod() {
    // Given - Mock repository with optimized method
    final CActivity activity = createTestActivity();
    when(activityRepository.findByIdWithEagerLoading(1L))
        .thenReturn(Optional.of(activity));
    
    // When - Use service method
    final CActivity result = activityService.findById(1L);
    
    // Then - Verify optimized method was called
    assertThat(result).isEqualTo(activity);
    verify(activityRepository).findByIdWithEagerLoading(1L);
    verify(activityRepository, never()).findById(1L); // Standard method not called
}

@Test
void testKanbanServiceImplementation() {
    // Given - Create activities with different statuses
    final CProject project = createTestProject();
    final List<CActivity> activities = createActivitiesWithVariousStatuses(project);
    
    // When - Use kanban service method
    final Map<CActivityStatus, List<CActivity>> groupedActivities = 
        activityService.getEntitiesGroupedByStatus(project);
    
    // Then - Verify grouping works correctly
    assertThat(groupedActivities).isNotEmpty();
    assertThat(groupedActivities.keySet())
        .extracting(CActivityStatus::getName)
        .contains("TODO", "IN_PROGRESS", "COMPLETED");
}
```

## Error Handling Rules

### Exception Handling

#### 1. Handle Lazy Loading Exceptions
```java
try {
    initializeLazyRelationship(entity.getParticipants());
} catch (LazyInitializationException e) {
    LOGGER.warn("Could not initialize lazy relationship: {}", e.getMessage());
    // Handle gracefully or re-throw as appropriate
}
```

#### 2. Validate Input Parameters
```java
public CMeeting findById(final Long id) {
    if (id == null) {
        return null; // Or throw IllegalArgumentException
    }
    return repository.findByIdWithEagerLoading(id).orElse(null);
}
```

## Security Rules

### Data Access Security

#### 1. Use @PreAuthorize Annotations
```java
@Service
@PreAuthorize("isAuthenticated()")
public class CMeetingService extends CEntityOfProjectService<CMeeting> {
    // Service methods
}
```

#### 2. Validate Access to Related Entities
```java
@PreAuthorize("hasRole('ADMIN') or @projectService.hasAccess(#projectId, authentication)")
public List<CMeeting> findByProject(Long projectId) {
    // Implementation
}
```

## Maintenance Rules

### Code Cleanup Guidelines

#### 1. Remove Deprecated Code Regularly
- Schedule regular reviews of @Deprecated methods
- Remove deprecated code after appropriate migration period
- Update documentation when removing deprecated features

#### 2. Monitor Performance Metrics
- Regular query performance analysis
- Database query optimization reviews
- Memory usage monitoring for entity loading

#### 3. Update Documentation
- Keep lazy loading guides current
- Update examples when patterns change
- Document performance improvements and their impact

## Conclusion

Following these enhanced coding rules ensures:
- **Consistent code quality** across all project modules and components
- **Optimal performance** for data access operations with comprehensive lazy loading
- **Maintainable and scalable** codebase with proper service layer patterns
- **Clear documentation** and examples for team members and new developers
- **Robust testing infrastructure** with automated UI and integration testing
- **Modern annotation-driven** form generation with enhanced binder support
- **Efficient repository patterns** with optimized eager loading strategies
- **Comprehensive panel architecture** for complex entity management
- **Project-aware functionality** with proper session management

### Key Improvements in Current Version:
- Enhanced lazy loading with automatic `CEntityOfProject` detection
- Comprehensive panel-based UI architecture across all modules
- Advanced annotation system with string ComboBox and data provider support
- Optimized repository patterns with specialized eager loading queries
- Modern service patterns with security annotations and interface implementations
- Robust testing framework with generic test superclasses
- Performance monitoring and query optimization guidelines
- Component-specific CSS organization for better maintainability

These rules should be reviewed and updated regularly as the project evolves, new patterns emerge, and additional modules are implemented. The comprehensive nature of these guidelines ensures consistency across the expanding codebase while maintaining high performance and code quality standards.