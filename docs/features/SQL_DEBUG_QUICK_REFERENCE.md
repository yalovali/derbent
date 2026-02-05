# Quick Reference: SQL Debugging Commands

This is a quick reference card for debugging database queries in the Derbent application.

For complete documentation, see: [Database Query Debugging Guide](DATABASE_QUERY_DEBUGGING.md)

## Start Application with SQL Logging

### Development (H2 Database)
```bash
mvn spring-boot:run -Dspring.profiles.active=h2
```
✅ **Best for**: Daily development work  
✅ **Database**: H2 in-memory  
✅ **SQL Logging**: Fully enabled with parameters

### Production Debugging (PostgreSQL)
```bash
mvn spring-boot:run -Dspring.profiles.active=sql-debug
```
✅ **Best for**: Production environment debugging  
✅ **Database**: PostgreSQL  
✅ **SQL Logging**: Fully enabled with parameters

### One-Time SQL Logging (Any Profile)
```bash
mvn spring-boot:run -Dlogging.level.org.hibernate.SQL=DEBUG
```
✅ **Best for**: Quick SQL check without changing profiles

## Quick Commands

| Command | Purpose |
|---------|---------|
| `mvn spring-boot:run -Dspring.profiles.active=h2` | Development with SQL logging (H2) |
| `mvn spring-boot:run -Dspring.profiles.active=sql-debug` | Production debugging with SQL (PostgreSQL) |
| `mvn spring-boot:run -Dlogging.level.org.hibernate.SQL=DEBUG` | Enable basic SQL logging |
| `mvn spring-boot:run -Dlogging.level.org.hibernate.orm.jdbc.bind=TRACE` | See query parameters |

## Configuration Properties

### Minimal SQL Logging
```properties
logging.level.org.hibernate.SQL=DEBUG
```

### SQL with Parameters
```properties
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.orm.jdbc.bind=TRACE
```

### Complete SQL Debugging
```properties
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.orm.jdbc.bind=TRACE
logging.level.org.hibernate.orm.jdbc.extract=TRACE
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true
```

## Example Output

When SQL logging is enabled, you'll see output like:

```
10:15:30.456 DEBUG (CActivityService.java:78) Hibernate: 
    /* load tech.derbent.activities.CActivity */ select
        a1_0.id,
        a1_0.company_id,
        a1_0.created_at,
        a1_0.description,
        a1_0.name,
        a1_0.project_id,
        a1_0.status_id,
        a1_0.type_id,
        a1_0.updated_at 
    from
        activities a1_0 
    where
        a1_0.id=?
10:15:30.457 TRACE (BasicBinder.java:64) binding parameter [1] as [BIGINT] - [42]
```

## Available Profiles

| Profile | Database | SQL Logging | Use Case |
|---------|----------|-------------|----------|
| `default` | PostgreSQL | ❌ Disabled | Production |
| `h2` | H2 in-memory | ✅ Enabled | Development |
| `sql-debug` | PostgreSQL | ✅ Enabled | Production debugging |
| `test` | H2 in-memory | ⚠️ Minimal | Testing |

## Common Debugging Scenarios

### Debug Slow Queries
```properties
spring.jpa.properties.hibernate.session.events.log.LOG_QUERIES_SLOWER_THAN_MS=200
```

### Debug Transactions
```properties
logging.level.org.springframework.transaction=TRACE
logging.level.org.springframework.orm.jpa=DEBUG
```

### Debug Connection Pool
```properties
logging.level.com.zaxxer.hikari=DEBUG
```

## Troubleshooting

**SQL queries not appearing?**
1. Check active profile: Look for "The following profiles are active: h2" in startup logs
2. Verify logging level: `logging.level.org.hibernate.SQL=DEBUG`
3. Make sure root logging level is not ERROR: `logging.level.root=INFO`

**Too much output?**
1. Use file logging: `logging.file.name=logs/sql-queries.log`
2. Filter by package: `logging.level.tech.derbent=DEBUG`
3. Log only slow queries: Set `LOG_QUERIES_SLOWER_THAN_MS`

## Additional Resources

- **Full Documentation**: [DATABASE_QUERY_DEBUGGING.md](DATABASE_QUERY_DEBUGGING.md)
- **Spring Boot Logging**: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging
- **Hibernate Logging**: https://docs.jboss.org/hibernate/orm/6.2/userguide/html_single/Hibernate_User_Guide.html#logging
