# Database Query Debugging - Implementation Summary

## Problem Statement
The user needed to know how to debug which queries are run in any database transaction of the Derbent project.

## Solution Implemented

### 1. Documentation Created

#### Primary Documentation: `docs/DATABASE_QUERY_DEBUGGING.md`
A comprehensive guide covering:
- Quick start instructions for enabling SQL logging
- Configuration details for different logging levels
- Available Spring profiles for SQL debugging
- Runtime configuration options
- Advanced debugging scenarios (performance, transactions, connections)
- Understanding SQL output format
- External tools (PostgreSQL logging, pgAdmin, pg_stat_statements)
- Troubleshooting common issues
- Best practices for development, production, and testing
- Quick reference commands and properties

#### Quick Reference: `docs/SQL_DEBUG_QUICK_REFERENCE.md`
A condensed cheat sheet providing:
- Quick commands for different scenarios
- Configuration properties reference
- Example output format
- Available profiles comparison table
- Common debugging scenarios
- Troubleshooting tips

### 2. New Spring Profile Created

#### File: `src/main/resources/application-sql-debug.properties`
A dedicated profile for production debugging that:
- Uses PostgreSQL database (production-like environment)
- Enables comprehensive SQL logging with:
  - `logging.level.org.hibernate.SQL=DEBUG` - Shows SQL statements
  - `logging.level.org.hibernate.orm.jdbc.bind=TRACE` - Shows parameter values
  - `logging.level.org.hibernate.orm.jdbc.extract=TRACE` - Shows result extraction
  - `spring.jpa.properties.hibernate.format_sql=true` - Formats SQL for readability
  - `spring.jpa.properties.hibernate.use_sql_comments=true` - Shows query origin
- Includes optional configurations for:
  - Query statistics
  - Slow query logging
  - Transaction debugging
  - Connection pool debugging

**Usage:**
```bash
mvn spring-boot:run -Dspring.profiles.active=sql-debug
```

### 3. Interactive Demo Script

#### File: `sql-debug-demo.sh`
An interactive bash script that:
- Presents users with 3 SQL debugging options
- Guides them through the process
- Executes the appropriate command
- Provides helpful context and explanations

**Features:**
- Development mode (H2 with SQL logging)
- Production debugging (PostgreSQL with SQL logging)
- One-time SQL logging (any profile)

### 4. Documentation Updates

#### Updated: `README.md`
Added reference to the new Database Query Debugging documentation in the main documentation table.

## How It Works

### Existing SQL Logging Configuration

The project already had SQL logging configured in the H2 development profile:

**File: `application-h2-local-development.properties`**
```properties
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.level.org.hibernate.orm.jdbc.bind=TRACE
logging.level.org.hibernate.orm.jdbc.extract=TRACE
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true
```

### New SQL Debugging for Production

The new `sql-debug` profile provides the same SQL logging capabilities but with PostgreSQL instead of H2, making it suitable for debugging production-like environments.

## Usage Examples

### For Development
```bash
# Use H2 profile - SQL logging already configured
mvn spring-boot:run -Dspring.profiles.active=h2
```

### For Production Debugging
```bash
# Use new sql-debug profile
mvn spring-boot:run -Dspring.profiles.active=sql-debug
```

### For One-Time Debugging
```bash
# Enable SQL logging without changing profiles
mvn spring-boot:run -Dlogging.level.org.hibernate.SQL=DEBUG
```

### For Complete SQL Debugging
```bash
# Enable all SQL debugging features
mvn spring-boot:run \
  -Dlogging.level.org.hibernate.SQL=DEBUG \
  -Dlogging.level.org.hibernate.orm.jdbc.bind=TRACE \
  -Dspring.jpa.properties.hibernate.format_sql=true
```

## Example Output

When SQL logging is enabled, the console will show:

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

This shows:
1. The SQL query being executed
2. The actual parameter values being bound
3. The values being extracted from the result set
4. Where in the code the query originated (via SQL comments)

## Files Modified/Created

### Created Files:
1. `docs/DATABASE_QUERY_DEBUGGING.md` - Comprehensive debugging guide
2. `docs/SQL_DEBUG_QUICK_REFERENCE.md` - Quick reference cheat sheet
3. `src/main/resources/application-sql-debug.properties` - SQL debug profile
4. `sql-debug-demo.sh` - Interactive demo script

### Modified Files:
1. `README.md` - Added reference to new documentation

## Benefits

1. **Clear Documentation**: Users now have comprehensive guidance on SQL debugging
2. **Multiple Options**: Support for different debugging scenarios (dev, prod, one-time)
3. **Easy to Use**: Simple commands with clear examples
4. **Non-Invasive**: No code changes required, only configuration
5. **Production-Safe**: Separate profile for production debugging
6. **Interactive Demo**: Helps users learn through hands-on experience

## Testing

The implementation was tested by:
1. Verifying all configuration files are valid
2. Confirming the build succeeds with `mvn clean compile`
3. Ensuring proper formatting with `mvn spotless:apply`
4. Validating all profiles exist and have correct SQL logging configuration

## Answer to Original Question

**"How can I debug which query is run in any database transaction of the project?"**

**Answer:**

Use one of these three approaches:

1. **Recommended for Development:**
   ```bash
   mvn spring-boot:run -Dspring.profiles.active=h2
   ```

2. **For Production-Like Debugging:**
   ```bash
   mvn spring-boot:run -Dspring.profiles.active=sql-debug
   ```

3. **For Quick One-Time Check:**
   ```bash
   mvn spring-boot:run -Dlogging.level.org.hibernate.SQL=DEBUG
   ```

All SQL queries will be printed to the console with:
- Formatted SQL statements
- Parameter values
- Result extraction
- Source location comments

For complete details, see: `docs/DATABASE_QUERY_DEBUGGING.md`

## Future Enhancements

Potential improvements that could be added:
1. SQL query logging to separate log file
2. Integration with APM tools (e.g., Spring Boot Admin, Prometheus)
3. Query performance metrics dashboard
4. Automated slow query alerts
5. Database query replay tool for debugging

## References

- Spring Boot Logging: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging
- Hibernate Logging: https://docs.jboss.org/hibernate/orm/6.2/userguide/html_single/Hibernate_User_Guide.html#logging
- HikariCP Configuration: https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby
