# Database Query Debugging Guide

This guide explains how to debug and monitor SQL queries executed by the Derbent application in any database transaction.

## Overview

Derbent uses Spring Data JPA with Hibernate as the ORM layer. There are multiple approaches to debug database queries:

1. **Logging Configuration** - Enable Hibernate SQL logging (recommended for development)
2. **Spring Profiles** - Use pre-configured profiles for different scenarios
3. **Application Properties** - Runtime configuration without code changes
4. **External Tools** - Database profilers and monitoring tools

---

## Quick Start - Enable SQL Logging

### Option 1: Use H2 Development Profile (Recommended for Development)

The easiest way to see all SQL queries during development is to use the H2 profile which has SQL logging pre-configured:

```bash
# Start with H2 profile - SQL logging is already enabled
mvn spring-boot:run -Dspring.profiles.active=h2
```

You'll immediately see formatted SQL queries in the console:
```
Hibernate: 
    select
        c1_0.id,
        c1_0.company_id,
        c1_0.created_at,
        c1_0.description,
        c1_0.name,
        c1_0.updated_at 
    from
        projects c1_0 
    where
        c1_0.company_id=?
```

### Option 2: Use SQL Debug Profile (For Production Debugging)

Use the dedicated SQL debugging profile that works with any database:

```bash
# For PostgreSQL with SQL debugging enabled
mvn spring-boot:run -Dspring.profiles.active=sql-debug
```

This profile enables SQL logging without changing the database configuration.

---

## Configuration Details

### Hibernate SQL Logging Levels

Add these properties to enable different levels of SQL debugging:

#### Basic SQL Logging
```properties
# Show SQL statements
logging.level.org.hibernate.SQL=DEBUG

# Format SQL for readability
spring.jpa.properties.hibernate.format_sql=true

# Show where queries originated from
spring.jpa.properties.hibernate.use_sql_comments=true
```

#### Parameter Binding (See Query Parameters)
```properties
# Hibernate 6 (Spring Boot 3.x)
logging.level.org.hibernate.orm.jdbc.bind=TRACE

# Hibernate 5 (older versions)
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

#### Complete SQL Debugging (All Details)
```properties
# SQL statements
logging.level.org.hibernate.SQL=DEBUG

# Bind parameters (values in queries)
logging.level.org.hibernate.orm.jdbc.bind=TRACE

# Extract results
logging.level.org.hibernate.orm.jdbc.extract=TRACE

# Format and comment SQL
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true
```

---

## Available Profiles

### 1. Default Profile (application.properties)
- **Database**: PostgreSQL
- **SQL Logging**: DISABLED (production mode)
- **Use Case**: Production deployment

**Start Command:**
```bash
mvn spring-boot:run
# or
java -jar target/derbent-1.0-SNAPSHOT.jar
```

### 2. H2 Development Profile (application-h2-local-development.properties)
- **Database**: H2 in-memory
- **SQL Logging**: ENABLED with full details
- **Use Case**: Local development and testing
- **Pre-configured**: ✅ SQL logging, parameter binding, result extraction

**Start Command:**
```bash
mvn spring-boot:run -Dspring.profiles.active=h2
```

**What You'll See:**
```
09:30:15.123 DEBUG (CActivityService.java:45) Hibernate: 
    select
        a1_0.id,
        a1_0.name,
        a1_0.description,
        a1_0.project_id,
        a1_0.status
    from
        activities a1_0
    where
        a1_0.project_id=?
09:30:15.124 TRACE (BasicBinder.java:64) binding parameter [1] as [BIGINT] - [123]
```

### 3. SQL Debug Profile (application-sql-debug.properties)
- **Database**: PostgreSQL (uses default database)
- **SQL Logging**: ENABLED with full details
- **Use Case**: Production debugging without changing to H2

**Start Command:**
```bash
mvn spring-boot:run -Dspring.profiles.active=sql-debug
```

---

## Runtime Configuration (Without Restart)

### Temporary Enable SQL Logging

You can enable SQL logging temporarily by passing properties as command-line arguments:

```bash
# Basic SQL logging
mvn spring-boot:run -Dlogging.level.org.hibernate.SQL=DEBUG

# With parameter binding
mvn spring-boot:run \
  -Dlogging.level.org.hibernate.SQL=DEBUG \
  -Dlogging.level.org.hibernate.orm.jdbc.bind=TRACE
```

### Environment Variables

Set logging levels via environment variables:

```bash
# Linux/Mac
export LOGGING_LEVEL_ORG_HIBERNATE_SQL=DEBUG
export LOGGING_LEVEL_ORG_HIBERNATE_ORM_JDBC_BIND=TRACE
mvn spring-boot:run

# Windows
set LOGGING_LEVEL_ORG_HIBERNATE_SQL=DEBUG
set LOGGING_LEVEL_ORG_HIBERNATE_ORM_JDBC_BIND=TRACE
mvn spring-boot:run
```

---

## Advanced Debugging Scenarios

### 1. Debug Specific Transaction

Enable SQL logging only for specific packages:

```properties
# Only log SQL for activity-related operations
logging.level.tech.derbent.activities=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

### 2. Performance Debugging

Identify slow queries:

```properties
# Log slow queries (time in milliseconds)
spring.jpa.properties.hibernate.session.events.log.LOG_QUERIES_SLOWER_THAN_MS=200

# Show query statistics
logging.level.org.hibernate.stat=DEBUG
spring.jpa.properties.hibernate.generate_statistics=true
```

### 3. Connection Pool Monitoring

Debug database connection issues:

```properties
# HikariCP connection pool logging
logging.level.com.zaxxer.hikari=DEBUG
logging.level.com.zaxxer.hikari.HikariConfig=DEBUG
```

### 4. Transaction Debugging

Monitor transaction boundaries:

```properties
# Spring transaction management
logging.level.org.springframework.transaction=TRACE
logging.level.org.springframework.orm.jpa=DEBUG
```

---

## Understanding SQL Output

### Example Output with Full Logging

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
10:15:30.459 TRACE (BasicExtractor.java:60) extracted value ([company_id] : [BIGINT]) - [1]
```

**Explanation:**
1. **Comment**: `/* load tech.derbent.activities.CActivity */` - Shows what entity is being loaded
2. **SQL Statement**: Formatted SELECT query
3. **Parameter Binding**: Shows the actual value (42) being bound to parameter 1
4. **Result Extraction**: Shows values being read from the result set

---

## External Tools

### 1. PostgreSQL Query Logging

Enable query logging directly in PostgreSQL:

**Edit `postgresql.conf`:**
```conf
log_statement = 'all'              # Log all queries
log_min_duration_statement = 100   # Log queries taking > 100ms
```

**Restart PostgreSQL:**
```bash
sudo systemctl restart postgresql
```

**View logs:**
```bash
tail -f /var/log/postgresql/postgresql-*.log
```

### 2. pgAdmin Query Tool

Use pgAdmin to monitor active queries in real-time:
1. Connect to your database
2. Tools → Server Status
3. View active queries and their execution time

### 3. Database Profilers

#### pg_stat_statements (PostgreSQL)
```sql
-- Enable extension
CREATE EXTENSION pg_stat_statements;

-- View query statistics
SELECT query, calls, total_time, mean_time 
FROM pg_stat_statements 
ORDER BY mean_time DESC 
LIMIT 20;
```

---

## Troubleshooting

### SQL Not Appearing in Logs

**Problem:** SQL queries are not being logged even after configuration.

**Solutions:**

1. **Check logging level hierarchy:**
   ```properties
   # Make sure root level doesn't override
   logging.level.root=INFO  # Not WARN or ERROR
   logging.level.org.hibernate.SQL=DEBUG
   ```

2. **Verify profile is active:**
   ```bash
   # Check startup logs for active profiles
   # Should see: "The following profiles are active: h2"
   mvn spring-boot:run -Dspring.profiles.active=h2
   ```

3. **Use spring.jpa.show-sql as fallback:**
   ```properties
   # Direct SQL output (not recommended, use logging instead)
   spring.jpa.show-sql=true
   ```

### Too Much Output

**Problem:** Too many SQL statements cluttering the logs.

**Solutions:**

1. **Filter by package:**
   ```properties
   # Only show application queries, not framework queries
   logging.level.org.hibernate.SQL=ERROR
   logging.level.tech.derbent=DEBUG
   ```

2. **Use file output:**
   ```properties
   # Send SQL to separate file
   logging.file.name=logs/sql-queries.log
   logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
   ```

3. **Increase minimum duration:**
   ```properties
   # Only log slow queries
   spring.jpa.properties.hibernate.session.events.log.LOG_QUERIES_SLOWER_THAN_MS=500
   ```

---

## Best Practices

### Development
✅ **DO:**
- Use `h2` profile for local development with full SQL logging
- Enable parameter binding to see actual query values
- Use formatted SQL for readability
- Keep SQL logging enabled during feature development

❌ **DON'T:**
- Commit `show-sql=true` (use logging instead)
- Leave TRACE level in committed properties files
- Log SQL in production by default

### Production
✅ **DO:**
- Use `sql-debug` profile for temporary debugging
- Log slow queries only (with duration threshold)
- Monitor via external tools (pgAdmin, pg_stat_statements)
- Use structured logging for better analysis

❌ **DON'T:**
- Enable TRACE level logging in production
- Log all queries in high-traffic applications
- Forget to disable debug logging after troubleshooting

### Testing
✅ **DO:**
- Use test profile with minimal SQL logging (WARN level)
- Enable SQL logging in failing tests only
- Use H2 for unit tests (faster, isolated)

❌ **DON'T:**
- Enable SQL logging for all tests (slows down test execution)
- Mix production database with test queries

---

## Quick Reference

### Commands Cheat Sheet

```bash
# Start with H2 and SQL logging (development)
mvn spring-boot:run -Dspring.profiles.active=h2

# Start with PostgreSQL and SQL logging (production debugging)
mvn spring-boot:run -Dspring.profiles.active=sql-debug

# One-time SQL logging without profile
mvn spring-boot:run -Dlogging.level.org.hibernate.SQL=DEBUG

# Full SQL debugging with parameters
mvn spring-boot:run \
  -Dlogging.level.org.hibernate.SQL=DEBUG \
  -Dlogging.level.org.hibernate.orm.jdbc.bind=TRACE \
  -Dspring.jpa.properties.hibernate.format_sql=true
```

### Properties Cheat Sheet

```properties
# Minimal SQL logging
logging.level.org.hibernate.SQL=DEBUG

# SQL with parameters
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.orm.jdbc.bind=TRACE

# Complete SQL debugging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.orm.jdbc.bind=TRACE
logging.level.org.hibernate.orm.jdbc.extract=TRACE
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true

# Performance monitoring
spring.jpa.properties.hibernate.session.events.log.LOG_QUERIES_SLOWER_THAN_MS=200
spring.jpa.properties.hibernate.generate_statistics=true
logging.level.org.hibernate.stat=DEBUG

# Transaction debugging
logging.level.org.springframework.transaction=TRACE
logging.level.org.springframework.orm.jpa=DEBUG

# Connection pool debugging
logging.level.com.zaxxer.hikari=DEBUG
```

---

## Related Documentation

- [Spring Boot Logging Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging)
- [Hibernate Logging Guide](https://docs.jboss.org/hibernate/orm/6.2/userguide/html_single/Hibernate_User_Guide.html#logging)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)

---

## Summary

To debug database queries in Derbent:

1. **For Development**: Use `mvn spring-boot:run -Dspring.profiles.active=h2` (SQL logging pre-configured)
2. **For Production Debugging**: Use `mvn spring-boot:run -Dspring.profiles.active=sql-debug`
3. **For One-Time Debugging**: Add `-Dlogging.level.org.hibernate.SQL=DEBUG` to any command
4. **For External Monitoring**: Use PostgreSQL logs or pgAdmin

The H2 profile is the recommended approach for day-to-day development as it provides complete SQL visibility with minimal configuration.
