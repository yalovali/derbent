# Testing Module Class Renaming - Standards Compliance

## Executive Summary

To achieve full ISO 29119 and ISTQB standards compliance and eliminate developer confusion, we will rename classes, tables, and fields to match industry-standard testing terminology.

## Renaming Strategy

### Priority 1: CValidationSuite → CTestSuite
**Rationale**: "Test Suite" is the ISO 29119 standard term for a collection of test cases.

**Changes Required**:
- Class: `CValidationSuite` → `CTestSuite`
- Table: `ctestscenario` → `ctestsuite`
- Field references: `testScenario` → `testSuite`
- Service: `CValidationSuiteService` → `CTestSuiteService`
- Repository: `IValidationSuiteRepository` → `ITestSuiteRepository`
- Page Service: `CPageServiceTestScenario` → `CPageServiceTestSuite`
- Initializer: `CValidationSuiteInitializerService` → `CTestSuiteInitializerService`
- Foreign keys: `testscenario_id` → `testsuite_id`

**Files to Update**: ~15 files
**Database Migration**: Required (ALTER TABLE, foreign key updates)

### Priority 2: CValidationSession → CTestSession
**Rationale**: "Test Session" is the ISTQB standard term and commonly used in testing tools.

**Changes Required**:
- Class: `CValidationSession` → `CTestSession`
- Table: `ctestrun` → `ctestsession`
- Field references: `testRun` → `testSession`
- Service: `CValidationSessionService` → `CTestSessionService`
- Repository: `IValidationSessionRepository` → `ITestSessionRepository`
- Page Service: `CPageServiceTestRun` → `CPageServiceTestSession`
- Initializer: `CValidationSessionInitializerService` → `CTestSessionInitializerService`
- Result entities: `CValidationCaseResult` → update `testRun` field to `testSession`
- Foreign keys: `testrun_id` → `testsession_id`

**Files to Update**: ~20 files
**Database Migration**: Required

### Priority 3: CValidationCaseType → CTestClassification (Optional)
**Rationale**: "Classification" is more formal but "Type" is acceptable in standards.

**Decision**: SKIP - "Type" is acceptable and widely used. Not critical for compliance.

## Database Migration Scripts

### Migration 1: Rename CValidationSuite → CTestSuite

```sql
-- Rename table
ALTER TABLE ctestscenario RENAME TO ctestsuite;

-- Rename primary key column
ALTER TABLE ctestsuite RENAME COLUMN testscenario_id TO testsuite_id;

-- Update foreign keys in ctestcase
ALTER TABLE ctestcase RENAME COLUMN testscenario_id TO testsuite_id;

-- Update foreign keys in ctestrun (will be renamed to ctestsession)
ALTER TABLE ctestrun RENAME COLUMN testscenario_id TO testsuite_id;

-- Update sequences (if using PostgreSQL)
ALTER SEQUENCE IF EXISTS ctestscenario_testscenario_id_seq RENAME TO ctestsuite_testsuite_id_seq;
```

### Migration 2: Rename CValidationSession → CTestSession

```sql
-- Rename table
ALTER TABLE ctestrun RENAME TO ctestsession;

-- Rename primary key column
ALTER TABLE ctestsession RENAME COLUMN testrun_id TO testsession_id;

-- Update foreign keys in ctestcaseresult
ALTER TABLE ctestcaseresult RENAME COLUMN testrun_id TO testsession_id;

-- Update foreign keys in attachments (if linking to test runs)
UPDATE cattachment SET entity_type = 'CTestSession' WHERE entity_type = 'CValidationSession';

-- Update sequences
ALTER SEQUENCE IF EXISTS ctestrun_testrun_id_seq RENAME TO ctestsession_testsession_id_seq;
```

## File Renaming Order

### Phase 1: Domain Entities
1. Rename domain class files
2. Update class names
3. Update table names in @Table annotations
4. Update column names in @Column and @JoinColumn
5. Update constants (ENTITY_TITLE_*, VIEW_NAME)

### Phase 2: Services
1. Rename service class files
2. Update class names and references
3. Update Spring bean names
4. Update getInitializerServiceClass() returns

### Phase 3: Repositories
1. Rename repository interface files
2. Update interface names
3. Update query entity references

### Phase 4: Page Services
1. Rename page service files
2. Update class names
3. Update constructor parameters

### Phase 5: Initializers
1. Rename initializer service files
2. Update class names
3. Update static references

### Phase 6: Test Files
1. Rename test class files
2. Update test references

### Phase 7: Cross-References
1. Update all imports across codebase
2. Update CDataInitializer references
3. Update entity registry references
4. Update documentation

## Implementation Plan

### Step 1: Backup and Preparation
```bash
# Create feature branch
git checkout -b refactor/test-entities-standards-naming

# Backup database (if testing with data)
pg_dump derbent_db > backup_before_test_refactor.sql
```

### Step 2: Automated Renaming Script
We'll create a bash script to perform consistent renaming:

```bash
#!/bin/bash
# rename-test-entities.sh

# Phase 1: CValidationSuite → CTestSuite
find . -type f -name "*.java" -exec sed -i 's/CValidationSuite/CTestSuite/g' {} +
find . -type f -name "*.java" -exec sed -i 's/testScenario/testSuite/g' {} +
find . -type f -name "*.java" -exec sed -i 's/ctestscenario/ctestsuite/g' {} +
find . -type f -name "*.java" -exec sed -i 's/testscenario_id/testsuite_id/g' {} +

# Rename files
find . -type f -name "*TestScenario*" | while read file; do
    mv "$file" "${file//TestScenario/TestSuite}"
done

# Phase 2: CValidationSession → CTestSession
find . -type f -name "*.java" -exec sed -i 's/CValidationSession/CTestSession/g' {} +
find . -type f -name "*.java" -exec sed -i 's/testRun/testSession/g' {} +
find . -type f -name "*.java" -exec sed -i 's/ctestrun/ctestsession/g' {} +
find . -type f -name "*.java" -exec sed -i 's/testrun_id/testsession_id/g' {} +

# Rename files
find . -type f -name "*TestRun*" | while read file; do
    mv "$file" "${file//TestRun/TestSession}"
done
```

### Step 3: Database Migration
Create Flyway migration scripts:

**File**: `src/main/resources/db/migration/V2026_01_17__rename_test_entities.sql`

```sql
-- Rename CValidationSuite to CTestSuite
ALTER TABLE ctestscenario RENAME TO ctestsuite;
ALTER TABLE ctestsuite RENAME COLUMN testscenario_id TO testsuite_id;
ALTER TABLE ctestcase RENAME COLUMN testscenario_id TO testsuite_id;
ALTER TABLE ctestrun RENAME COLUMN testscenario_id TO testsuite_id;

-- Rename CValidationSession to CTestSession
ALTER TABLE ctestrun RENAME TO ctestsession;
ALTER TABLE ctestsession RENAME COLUMN testrun_id TO testsession_id;
ALTER TABLE ctestcaseresult RENAME COLUMN testrun_id TO testsession_id;

-- Update sequences (PostgreSQL)
ALTER SEQUENCE IF EXISTS ctestscenario_testscenario_id_seq RENAME TO ctestsuite_testsuite_id_seq;
ALTER SEQUENCE IF EXISTS ctestrun_testrun_id_seq RENAME TO ctestsession_testsession_id_seq;
```

### Step 4: Update Spring Configuration
Update any hardcoded bean names or entity references in configuration files.

### Step 5: Update Documentation
1. Update all `docs/` files with new terminology
2. Update README and coding standards
3. Update backlog Excel
4. Update API documentation

### Step 6: Testing
```bash
# Compile and verify
./mvnw clean compile

# Run tests
./mvnw test

# Run Spotless
./mvnw spotless:apply

# Verify database migration
./mvnw flyway:migrate
```

## Risk Assessment

### High Risk
- **Database migration**: Existing data must be preserved
- **Foreign key constraints**: Must be updated correctly
- **Cascading changes**: Many files affected

### Medium Risk
- **Import statements**: Must be updated everywhere
- **Spring bean names**: Dynamic lookups might break
- **Serialization**: If entities are serialized

### Low Risk
- **Documentation**: Can be updated post-refactor
- **Comments**: Can be fixed gradually

## Rollback Plan

If issues arise:
```sql
-- Rollback database changes
ALTER TABLE ctestsuite RENAME TO ctestscenario;
ALTER TABLE ctestsuite RENAME COLUMN testsuite_id TO testscenario_id;
-- ... reverse all changes
```

```bash
# Rollback code
git reset --hard HEAD
git checkout main
```

## Post-Refactoring Verification

### Checklist
- [ ] All Java files compile without errors
- [ ] All tests pass
- [ ] Database migrations run successfully
- [ ] No hardcoded old names remain
- [ ] Documentation updated
- [ ] Backlog updated
- [ ] Sample data initialization works
- [ ] UI displays correct terminology
- [ ] Foreign key relationships intact
- [ ] Spring beans resolve correctly

### Manual Testing
1. Create new project
2. Verify test entities initialize
3. Create test suite
4. Create test cases in suite
5. Create test session
6. Execute test session
7. Verify relationships work
8. Check menu items display correctly

## Timeline Estimate

- **Automated renaming**: 30 minutes
- **Manual fixes**: 2-3 hours
- **Database migration**: 30 minutes
- **Testing**: 2 hours
- **Documentation update**: 1 hour
- **Total**: 6-7 hours

## Alternative: Gradual Migration

If immediate full refactoring is too risky:

### Phase 1: Add aliases (Week 1)
- Create `CTestSuite extends CValidationSuite` (deprecated)
- Create `CTestSession extends CValidationSession` (deprecated)
- Mark old classes as `@Deprecated`

### Phase 2: Migrate code (Week 2)
- Update all new code to use new names
- Update documentation

### Phase 3: Database migration (Week 3)
- Run database rename scripts
- Update old classes to point to new tables

### Phase 4: Remove deprecated (Week 4)
- Remove old class aliases
- Clean up deprecated warnings

## Recommendation

**Proceed with full refactoring** because:
1. ✅ Clean break - no technical debt
2. ✅ Consistent with standards from the start
3. ✅ No deprecated code to maintain
4. ✅ Simpler for new developers
5. ✅ Module is new - low risk of breaking existing usage

**Timing**: Do this now before any production data exists.
