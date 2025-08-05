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

#### 2. MetaData Annotations
```java
// ✅ CORRECT: Complete metadata
@MetaData(
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
// ✅ CORRECT: Extend specific base repository
public interface CMeetingRepository extends CEntityOfProjectRepository<CMeeting> {
    // Meeting-specific methods
}

// ✅ CORRECT: Extend named entity repository
public interface CUserRepository extends CAbstractNamedRepository<CUser> {
    // User-specific methods
}
```

#### 2. Method Naming Conventions
```java
// ✅ CORRECT: Descriptive method names
Optional<CMeeting> findByIdWithEagerLoading(@Param("id") Long id);
List<CUser> findAllEnabledWithEagerLoading();
List<CMeeting> findByProjectForListView(@Param("project") CProject project);

// ❌ INCORRECT: Generic names
Optional<CMeeting> findWithDetails(@Param("id") Long id);
List<CUser> findSpecial();
```

## Service Layer Rules

### Service Design Rules

#### 1. Override findById for Optimization
```java
// ✅ CORRECT: Optimized findById
@Override
public CMeeting findById(final Long id) {
    if (id == null) {
        return null;
    }
    return ((CMeetingRepository) repository).findByIdWithEagerLoading(id).orElse(null);
}

// ❌ INCORRECT: Using default implementation
// Don't override if no optimization is needed
```

#### 2. Avoid Redundant Service Methods
```java
// ❌ INCORRECT: Redundant helper methods
@Deprecated
public CMeeting setParticipants(CMeeting meeting, Set<CUser> participants) {
    meeting.setParticipants(participants);
    return save(meeting);
}

// ✅ CORRECT: Use entity methods directly
// meeting.setParticipants(participants);
// meetingService.save(meeting);
```

#### 3. Lazy Field Initialization Best Practices
```java
// ✅ CORRECT: Minimal lazy initialization
@Override
protected void initializeLazyFields(final CMeeting entity) {
    if (entity == null) {
        return;
    }
    super.initializeLazyFields(entity);
    
    // Only initialize what JOIN FETCH can't handle
    initializeLazyRelationship(entity.getParticipants());
    initializeLazyRelationship(entity.getAttendees());
}

// ❌ INCORRECT: Initializing eagerly loaded fields
@Override
protected void initializeLazyFields(final CMeeting entity) {
    initializeLazyRelationship(entity.getStatus()); // Already EAGER
    initializeLazyRelationship(entity.getMeetingType()); // Already EAGER
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

#### 1. Monitor N+1 Query Problems
```java
// ✅ CORRECT: Log and monitor queries in development
# application-dev.properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
```

#### 2. Use Pagination for Large Result Sets
```java
// ✅ CORRECT: Paginated queries
@Query("SELECT m FROM CMeeting m WHERE m.project = :project")
Page<CMeeting> findByProject(@Param("project") CProject project, Pageable pageable);
```

#### 3. Avoid Loading Unnecessary Data
```java
// ✅ CORRECT: Project-specific queries
@Query("SELECT m.id, m.name, m.status FROM CMeeting m WHERE m.project = :project")
List<Object[]> findBasicInfoByProject(@Param("project") CProject project);

// ❌ INCORRECT: Loading full entities when only basic info needed
@Query("SELECT m FROM CMeeting m WHERE m.project = :project")
List<CMeeting> findByProject(@Param("project") CProject project);
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

#### 1. Test Repository Queries
```java
@Test
void testFindByIdWithEagerLoading() {
    // Given
    CMeeting meeting = createTestMeeting();
    meetingRepository.save(meeting);
    
    // When
    Optional<CMeeting> result = meetingRepository.findByIdWithEagerLoading(meeting.getId());
    
    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getStatus()).isNotNull(); // Verify eager loading
}
```

#### 2. Test Service Layer Optimizations
```java
@Test
void testFindByIdUsesOptimizedQuery() {
    // Test that service uses the optimized repository method
    CMeeting meeting = meetingService.findById(1L);
    // Verify no additional queries are needed for associations
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

Following these coding rules ensures:
- Consistent code quality across the project
- Optimal performance for data access operations
- Maintainable and scalable codebase
- Clear documentation and examples for team members

These rules should be reviewed and updated regularly as the project evolves and new patterns emerge.