# Lazy Loading and Eager Loading Best Practices Guide

## Overview

This document outlines the best practices for lazy loading and eager loading patterns in the Derbent project. Proper implementation of these patterns is crucial for application performance and preventing N+1 query problems.

## Fetch Strategy Guidelines

### When to Use EAGER Loading

Use `FetchType.EAGER` for:
- **Small lookup entities** (e.g., status, type, priority entities)
- **Frequently accessed associations** that are needed in most use cases
- **@ManyToOne relationships** to small, rarely changing entities
- **Associations displayed in list views** where the data is always needed

Examples:
```java
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "status_id")
private CActivityStatus status;

@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "type_id")
private CActivityType activityType;
```

### When to Use LAZY Loading

Use `FetchType.LAZY` (default) for:
- **Large collections** (@OneToMany, @ManyToMany)
- **Optional associations** that may not always be needed
- **Hierarchical relationships** (parent-child structures)
- **Heavy objects** that should only be loaded when explicitly accessed

Examples:
```java
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(name = "meeting_participants")
private Set<CUser> participants = new HashSet<>();

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "parent_activity_id")
private CActivity parentActivity;
```

## Repository Query Optimization

### JOIN FETCH Queries

Always use JOIN FETCH in repository queries to prevent N+1 problems:

```java
@Query("SELECT m FROM CMeeting m " +
       "LEFT JOIN FETCH m.meetingType " +
       "LEFT JOIN FETCH m.status " +
       "LEFT JOIN FETCH m.responsible " +
       "WHERE m.id = :id")
Optional<CMeeting> findByIdWithEagerLoading(@Param("id") Long id);
```

### Separate Queries for Different Use Cases

Create specific repository methods for different scenarios:

```java
// For list views - load minimal associations
@Query("SELECT m FROM CMeeting m " +
       "LEFT JOIN FETCH m.status " +
       "LEFT JOIN FETCH m.project " +
       "WHERE m.project = :project")
List<CMeeting> findByProjectForListView(@Param("project") CProject project);

// For detail views - load everything needed
@Query("SELECT m FROM CMeeting m " +
       "LEFT JOIN FETCH m.meetingType " +
       "LEFT JOIN FETCH m.status " +
       "LEFT JOIN FETCH m.responsible " +
       "LEFT JOIN FETCH m.relatedActivity " +
       "WHERE m.id = :id")
Optional<CMeeting> findByIdWithEagerLoading(@Param("id") Long id);
```

## Service Layer Best Practices

### Optimized findById Methods

Override `findById()` in services to use optimized repository queries:

```java
@Override
public CMeeting findById(final Long id) {
    if (id == null) {
        return null;
    }
    return ((CMeetingRepository) repository).findByIdWithEagerLoading(id).orElse(null);
}
```

### Lazy Field Initialization

Only initialize lazy fields when necessary and when JOIN FETCH queries aren't sufficient:

```java
@Override
protected void initializeLazyFields(final CMeeting entity) {
    if (entity == null) {
        return;
    }
    super.initializeLazyFields(entity);
    
    // Only initialize collections that can't be handled with JOIN FETCH
    initializeLazyRelationship(entity.getParticipants());
    initializeLazyRelationship(entity.getAttendees());
}
```

## Common Anti-Patterns to Avoid

### ❌ Avoid Manual Updates Instead of Entity Operations

Don't create service methods that duplicate entity functionality:

```java
// BAD - Redundant service method
@Deprecated
public CMeeting setParticipants(CMeeting meeting, Set<CUser> participants) {
    meeting.getParticipants().clear();
    participants.forEach(meeting::addParticipant);
    return save(meeting);
}

// GOOD - Use entity methods directly
meeting.setParticipants(participants);
meetingService.save(meeting);
```

### ❌ Avoid N+1 Query Problems

Don't access associations in loops without proper eager loading:

```java
// BAD - Causes N+1 queries
List<CMeeting> meetings = meetingRepository.findAll();
for (CMeeting meeting : meetings) {
    System.out.println(meeting.getStatus().getName()); // Lazy load for each meeting
}

// GOOD - Use JOIN FETCH
List<CMeeting> meetings = meetingRepository.findAllWithStatus();
for (CMeeting meeting : meetings) {
    System.out.println(meeting.getStatus().getName()); // Already loaded
}
```

### ❌ Avoid Unnecessary EAGER Loading

Don't use EAGER for large collections or rarely accessed associations:

```java
// BAD - Will always load all participants
@OneToMany(fetch = FetchType.EAGER)
private Set<CUser> participants;

// GOOD - Load only when needed
@OneToMany(fetch = FetchType.LAZY)
private Set<CUser> participants;
```

## Testing and Performance

### Monitor Query Execution

Enable Hibernate SQL logging in development:

```properties
# application-dev.properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### Test Common Scenarios

Always test:
1. **List views** - Ensure no N+1 queries when displaying entity lists
2. **Detail views** - Verify all needed associations are loaded efficiently
3. **Collection access** - Test lazy collection initialization
4. **Transactional boundaries** - Ensure lazy loading works within transaction scope

## Entity-Specific Guidelines

### CMeeting Entity
- ✅ EAGER: meetingType, status, responsible (small lookup entities)
- ✅ LAZY: participants, attendees (collections)
- ✅ Use JOIN FETCH in repository queries for detail views

### CActivity Entity  
- ✅ EAGER: activityType, status, priority, assignedTo
- ✅ LAZY: parentActivity (hierarchical relationship)
- ✅ Custom repository queries with JOIN FETCH

### CUser Entity
- ✅ EAGER: userType, company (small lookup entities)
- ✅ Use optimized queries for dropdown/selection components

## Performance Monitoring

### Key Metrics to Watch
- Query count per request
- Query execution time
- Memory usage for entity loading
- LazyInitializationException occurrences

### Tools for Monitoring
- Hibernate Statistics
- Spring Boot Actuator metrics
- Application Performance Monitoring (APM) tools
- Database query logs

## Migration Strategy

When optimizing existing code:

1. **Identify** N+1 query problems using SQL logging
2. **Add** JOIN FETCH queries to repositories
3. **Update** service methods to use optimized queries
4. **Remove** redundant helper methods
5. **Test** performance improvements
6. **Document** changes and patterns used

## Conclusion

Following these lazy loading best practices will:
- Improve application performance
- Reduce database query count
- Prevent common JPA pitfalls
- Make code more maintainable
- Provide consistent patterns across the application

Remember: The goal is to load exactly what you need, when you need it, in the most efficient way possible.