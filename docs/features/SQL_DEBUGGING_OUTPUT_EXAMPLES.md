# SQL Debugging Output Examples

This document shows real examples of what you'll see when SQL debugging is enabled in Derbent.

## Example 1: Simple Entity Query

### Service Code
```java
@Service
public class CActivityService extends CProjectItemService<CActivity> {
    public CActivity findById(Long id) {
        return repository.findById(id).orElse(null);
    }
}
```

### Console Output (with SQL debugging enabled)
```
10:15:30.456 DEBUG (CActivityService.java:78) Hibernate: 
    /* load tech.derbent.activities.CActivity */ select
        c1_0.id,
        c1_0.company_id,
        c1_0.created_at,
        c1_0.description,
        c1_0.name,
        c1_0.project_id,
        c1_0.status_id,
        c1_0.type_id,
        c1_0.updated_at 
    from
        activities c1_0 
    where
        c1_0.id=?
10:15:30.457 TRACE (BasicBinder.java:64) binding parameter [1] as [BIGINT] - [42]
10:15:30.459 TRACE (BasicExtractor.java:60) extracted value ([id] : [BIGINT]) - [42]
10:15:30.460 TRACE (BasicExtractor.java:60) extracted value ([company_id] : [BIGINT]) - [1]
10:15:30.461 TRACE (BasicExtractor.java:60) extracted value ([created_at] : [TIMESTAMP]) - [2025-10-17 10:00:00.0]
10:15:30.462 TRACE (BasicExtractor.java:60) extracted value ([description] : [VARCHAR]) - [Sample activity description]
10:15:30.463 TRACE (BasicExtractor.java:60) extracted value ([name] : [VARCHAR]) - [Development Task]
```

## Example 2: Join Query with Relationships

### Service Code
```java
@Service
public class CActivityService extends CProjectItemService<CActivity> {
    public List<CActivity> findByProjectWithStatus(CProject project) {
        return repository.findByProject(project);
    }
}
```

### Console Output
```
10:20:15.123 DEBUG (CActivityService.java:92) Hibernate: 
    /* <criteria query> */ select
        c1_0.id,
        c1_0.company_id,
        c1_0.created_at,
        c1_0.description,
        c1_0.name,
        c1_0.project_id,
        s1_0.id,
        s1_0.color,
        s1_0.name,
        t1_0.id,
        t1_0.name,
        c1_0.updated_at 
    from
        activities c1_0 
    left join
        status s1_0 
            on s1_0.id=c1_0.status_id 
    left join
        activity_types t1_0 
            on t1_0.id=c1_0.type_id 
    where
        c1_0.project_id=?
10:20:15.124 TRACE (BasicBinder.java:64) binding parameter [1] as [BIGINT] - [123]
```

## Example 3: Insert Operation

### Service Code
```java
@Service
public class CActivityService extends CProjectItemService<CActivity> {
    @Transactional
    public CActivity save(CActivity activity) {
        return repository.save(activity);
    }
}
```

### Console Output
```
10:25:42.789 DEBUG (CActivityService.java:105) Hibernate: 
    /* insert for
        tech.derbent.activities.CActivity */insert 
    into
        activities (company_id, created_at, description, name, project_id, status_id, type_id, updated_at) 
    values
        (?, ?, ?, ?, ?, ?, ?, ?)
10:25:42.790 TRACE (BasicBinder.java:64) binding parameter [1] as [BIGINT] - [1]
10:25:42.791 TRACE (BasicBinder.java:64) binding parameter [2] as [TIMESTAMP] - [2025-10-17 10:25:42.788]
10:25:42.792 TRACE (BasicBinder.java:64) binding parameter [3] as [VARCHAR] - [Implement new feature]
10:25:42.793 TRACE (BasicBinder.java:64) binding parameter [4] as [VARCHAR] - [Feature X Development]
10:25:42.794 TRACE (BasicBinder.java:64) binding parameter [5] as [BIGINT] - [123]
10:25:42.795 TRACE (BasicBinder.java:64) binding parameter [6] as [BIGINT] - [1]
10:25:42.796 TRACE (BasicBinder.java:64) binding parameter [7] as [BIGINT] - [2]
10:25:42.797 TRACE (BasicBinder.java:64) binding parameter [8] as [TIMESTAMP] - [2025-10-17 10:25:42.788]
```

## Example 4: Update Operation

### Service Code
```java
@Service
public class CActivityService extends CProjectItemService<CActivity> {
    @Transactional
    public void updateDescription(Long id, String newDescription) {
        CActivity activity = repository.findById(id).orElseThrow();
        activity.setDescription(newDescription);
        repository.save(activity);
    }
}
```

### Console Output
```
10:30:18.456 DEBUG (CActivityService.java:78) Hibernate: 
    /* load tech.derbent.activities.CActivity */ select
        c1_0.id,
        c1_0.company_id,
        c1_0.created_at,
        c1_0.description,
        c1_0.name,
        c1_0.project_id,
        c1_0.status_id,
        c1_0.type_id,
        c1_0.updated_at 
    from
        activities c1_0 
    where
        c1_0.id=?
10:30:18.457 TRACE (BasicBinder.java:64) binding parameter [1] as [BIGINT] - [42]

10:30:18.520 DEBUG (CActivityService.java:82) Hibernate: 
    /* update
        tech.derbent.activities.CActivity */ update
        activities 
    set
        description=?,
        updated_at=? 
    where
        id=?
10:30:18.521 TRACE (BasicBinder.java:64) binding parameter [1] as [VARCHAR] - [Updated description text]
10:30:18.522 TRACE (BasicBinder.java:64) binding parameter [2] as [TIMESTAMP] - [2025-10-17 10:30:18.520]
10:30:18.523 TRACE (BasicBinder.java:64) binding parameter [3] as [BIGINT] - [42]
```

## Example 5: Delete Operation

### Service Code
```java
@Service
public class CActivityService extends CProjectItemService<CActivity> {
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
```

### Console Output
```
10:35:05.123 DEBUG (CActivityService.java:90) Hibernate: 
    /* delete tech.derbent.activities.CActivity */ delete 
    from
        activities 
    where
        id=?
10:35:05.124 TRACE (BasicBinder.java:64) binding parameter [1] as [BIGINT] - [42]
```

## Example 6: Complex Query with Multiple Joins

### Service Code
```java
@Service
public class CActivityService extends CProjectItemService<CActivity> {
    public List<CActivity> findActivitiesWithAllDetails(CProject project) {
        return repository.findByProjectWithUsers(project);
    }
}
```

### Console Output
```
10:40:22.789 DEBUG (CActivityService.java:115) Hibernate: 
    /* <criteria query> */ select
        c1_0.id,
        c1_0.company_id,
        c1_0.created_at,
        c1_0.description,
        c1_0.name,
        p1_0.id,
        p1_0.company_id,
        p1_0.name,
        p1_0.description,
        s1_0.id,
        s1_0.color,
        s1_0.name,
        s1_0.order_index,
        t1_0.id,
        t1_0.name,
        t1_0.description,
        u1_0.activity_id,
        u2_0.id,
        u2_0.email,
        u2_0.first_name,
        u2_0.last_name,
        c1_0.updated_at 
    from
        activities c1_0 
    left join
        projects p1_0 
            on p1_0.id=c1_0.project_id 
    left join
        status s1_0 
            on s1_0.id=c1_0.status_id 
    left join
        activity_types t1_0 
            on t1_0.id=c1_0.type_id 
    left join
        activity_users u1_0 
            on c1_0.id=u1_0.activity_id 
    left join
        users u2_0 
            on u2_0.id=u1_0.user_id 
    where
        c1_0.project_id=?
10:40:22.790 TRACE (BasicBinder.java:64) binding parameter [1] as [BIGINT] - [123]
```

## Example 7: Transaction Debugging

When transaction debugging is enabled with:
```properties
logging.level.org.springframework.transaction=TRACE
```

### Console Output
```
10:45:10.100 TRACE (TransactionInterceptor.java:367) Getting transaction for [tech.derbent.activities.CActivityService.save]
10:45:10.101 DEBUG (CActivityService.java:105) Hibernate: 
    /* insert for
        tech.derbent.activities.CActivity */insert 
    into
        activities (company_id, created_at, description, name, project_id, status_id, type_id, updated_at) 
    values
        (?, ?, ?, ?, ?, ?, ?, ?)
10:45:10.102 TRACE (BasicBinder.java:64) binding parameter [1] as [BIGINT] - [1]
... [parameter bindings] ...
10:45:10.150 TRACE (TransactionInterceptor.java:391) Completing transaction for [tech.derbent.activities.CActivityService.save]
```

## Example 8: Connection Pool Activity

When connection pool debugging is enabled with:
```properties
logging.level.com.zaxxer.hikari=DEBUG
```

### Console Output
```
10:50:05.100 DEBUG (HikariPool.java:421) HikariPool-1 - Before adding connection to pool
10:50:05.101 DEBUG (PoolBase.java:216) HikariPool-1 - Added connection conn0: url=jdbc:postgresql://localhost:5432/derbent user=postgres
10:50:05.102 DEBUG (HikariPool.java:424) HikariPool-1 - After adding connection to pool stats (total=10, active=2, idle=8, waiting=0)
```

## Example 9: Slow Query Detection

When slow query logging is enabled with:
```properties
spring.jpa.properties.hibernate.session.events.log.LOG_QUERIES_SLOWER_THAN_MS=200
```

### Console Output
```
10:55:30.100 WARN (SessionEventListenerManagerImpl.java:123) Slow query detected! Execution time: 345ms
10:55:30.101 DEBUG Hibernate: 
    /* slow query */ select
        c1_0.id,
        ... [many columns] ...
    from
        activities c1_0 
    left join
        ... [many joins] ...
    where
        ... [complex conditions] ...
```

## How to Enable SQL Debugging

### Quick Start
```bash
# Development (H2 with SQL logging)
mvn spring-boot:run -Dspring.profiles.active=h2

# Production debugging (PostgreSQL with SQL logging)
mvn spring-boot:run -Dspring.profiles.active=sql-debug

# One-time debugging
mvn spring-boot:run -Dlogging.level.org.hibernate.SQL=DEBUG
```

### What You'll See

With SQL debugging enabled, you'll see:
1. **SQL Comments** - Shows where the query originated (entity class, method)
2. **Formatted SQL** - Easy-to-read SQL with proper indentation
3. **Parameter Bindings** - Actual values being used in queries
4. **Result Extraction** - Values being read from database
5. **Transaction Boundaries** - When transactions start/commit (if transaction logging enabled)

## Understanding the Output

### Line-by-Line Breakdown

```
10:15:30.456 DEBUG (CActivityService.java:78) Hibernate: 
    ^^^^^^^^^^^       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    Timestamp         Source location in code
    
    /* load tech.derbent.activities.CActivity */ select
    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    SQL comment showing operation and entity
    
    where
        c1_0.id=?
                ^
                Parameter placeholder
                
10:15:30.457 TRACE (BasicBinder.java:64) binding parameter [1] as [BIGINT] - [42]
                                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                                          Actual value: parameter 1 = 42 (BIGINT type)
```

## See Also

- [Complete Debugging Guide](DATABASE_QUERY_DEBUGGING.md)
- [Quick Reference](SQL_DEBUG_QUICK_REFERENCE.md)
- [Implementation Summary](../IMPLEMENTATION_SUMMARY_SQL_DEBUGGING.md)
