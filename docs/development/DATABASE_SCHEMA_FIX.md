# Database Reset for Development

## Problem

After major refactoring (like the composition pattern changes), the PostgreSQL database may have old columns or constraints that cause errors:

```
ERROR: null value in column "item_id" of relation "csprint_items" violates not-null constraint
```

## Solution: Drop and Recreate Database

**Simple fix**: Let Hibernate drop and recreate the entire database:

```bash
# WARNING: This will DELETE ALL DATA and recreate schema!
mvn spring-boot:run -Dspring-boot.run.profiles=reset-db
```

**What happens**:
1. Hibernate drops ALL tables
2. Hibernate recreates schema from entities
3. Sample data is initialized automatically
4. Clean database with correct schema

**That's it!** No SQL scripts, no migrations, just Hibernate doing its job.

## When to Use

✅ **Development**: After major code changes, testing, or when schema is broken  
❌ **Production**: Never! Use proper migration tools (Flyway/Liquibase)

## Configuration

The `reset-db` profile in `application-reset-db.properties`:
```properties
spring.jpa.hibernate.ddl-auto=create-drop  # Drop and recreate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

That's all you need!

## What Changed in the Refactoring

### Old Design (Before)
```
csprint_items table:
- sprint_item_id (PK)
- item_id (NOT NULL) ← PROBLEM: This no longer exists
- item_type (NOT NULL) ← PROBLEM: This no longer exists  
- sprint_id
- item_order
```

**Pattern**: Join table connecting Sprint to Activities/Meetings

### New Design (After - Composition Pattern)
```
csprint_items table:
- sprint_item_id (PK)
- sprint_id (nullable) ← NULL means backlog
- item_order
- story_point
- progress_percentage
- start_date, due_date, completion_date
- responsible_id

cactivity table:
- activity_id (PK)
- sprintitem_id (FK, NOT NULL) ← Points to csprint_items
- ... business fields ...

cmeeting table:
- meeting_id (PK)
- sprintitem_id (FK, NOT NULL) ← Points to csprint_items
- ... business fields ...
```

**Pattern**: Composition - CActivity/CMeeting OWN their CSprintItem

## Verification

After applying the fix, verify the schema:

```sql
-- Check csprint_items columns
\d csprint_items

-- Should NOT have item_id or item_type columns
-- Should have: sprint_item_id, sprint_id (nullable), item_order, progress fields
```

Run the application:
```bash
mvn spring-boot:run
```

You should see successful data initialization without the `item_id` error.

## Understanding the Composition Pattern

The refactoring implements **Database Table Composition**:

1. **CActivity/CMeeting** (parent entities):
   - Contain business event data
   - Have `@OneToOne` relationship to CSprintItem
   - Foreign key: `sprintitem_id` (NOT NULL)

2. **CSprintItem** (child entity):
   - Contains progress tracking data
   - Owned by parent via CASCADE.ALL
   - Has optional reference to Sprint (`sprint_id` nullable)
   - When `sprint_id` is NULL → item is in backlog

3. **Benefits**:
   - Clear separation: business data vs progress tracking
   - No duplicate fields between Activity and Meeting
   - Single source of truth for progress information
   - Easy to add new progress fields

For more details, see: `docs/architecture/DATABASE_COMPOSITION_PATTERN.md`

## Troubleshooting

### Still Getting item_id Error?

1. **Check which database you're connected to**:
   ```bash
   psql -U postgres -d derbent -c "SELECT current_database();"
   ```

2. **Verify the column is gone**:
   ```sql
   SELECT column_name FROM information_schema.columns 
   WHERE table_name = 'csprint_items' AND column_name = 'item_id';
   ```
   Should return no rows.

3. **Check application.properties**:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/derbent
   spring.jpa.hibernate.ddl-auto=update
   ```

4. **Restart the application** after running the SQL fix.

### Connection Refused Error?

If you see "Connection to localhost:5432 refused":
- Ensure PostgreSQL is running: `sudo systemctl status postgresql`
- Check connection settings in `application.properties`
- Verify database exists: `psql -U postgres -l | grep derbent`

## For Production Deployment

When deploying to production, use proper database migration tools:

1. **Flyway** or **Liquibase** for versioned migrations
2. Create a migration script like `V1.1__refactor_sprintitem_composition.sql`
3. Never use `create-drop` in production
4. Always backup before schema changes

For development, the provided SQL script is sufficient.
