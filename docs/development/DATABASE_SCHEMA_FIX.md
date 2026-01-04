# Database Schema Fix for Composition Pattern Refactoring

## Problem

After refactoring CActivity/CMeeting to use composition pattern with CSprintItem, the PostgreSQL database still has the old `item_id` column with a NOT NULL constraint. This causes data initialization errors:

```
ERROR: null value in column "item_id" of relation "csprint_items" violates not-null constraint
```

## Simple Solution: Drop and Recreate Database

**EASIEST FIX**: Use the reset-db profile to drop and recreate the entire PostgreSQL database:

```bash
# WARNING: This will DELETE ALL DATA and recreate schema!
mvn spring-boot:run -Dspring-boot.run.profiles=reset-db
```

This uses `spring.jpa.hibernate.ddl-auto=create-drop` to:
1. Drop all tables
2. Recreate schema from current entities
3. Initialize sample data
4. No old columns remain

**When to use**: Development environment when you want a clean database with the new schema.

## Alternative: Manual SQL Commands (Keep Data)

If you need to preserve existing data:

```sql
-- Connect to your database and run:
ALTER TABLE csprint_items DROP COLUMN IF EXISTS item_id CASCADE;
ALTER TABLE csprint_items DROP COLUMN IF EXISTS item_type CASCADE;
ALTER TABLE csprint_items ALTER COLUMN sprint_id DROP NOT NULL;
```

## Temporary Change (One-Time Reset)

Edit `application.properties` temporarily:
```properties
# Change from:
spring.jpa.hibernate.ddl-auto=update

# To (temporarily):
spring.jpa.hibernate.ddl-auto=create-drop
```

Run the application once, then change it back to `update`.

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
