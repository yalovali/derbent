# Type and Status CRUD Testing Guide

## Overview

This guide documents the comprehensive CRUD testing implementation for Type and Status entities in the Derbent application. The tests validate complete Create, Read, Update, Delete, and Refresh operations on all type and status entities.

## Test Coverage

### Type Entities Tested

1. **Activity Types** (CActivityType)
   - Menu: "Activity Types"
   - Route: `/cactivitytypeview`
   - Tests: Create, Read, Update, Delete validation, Refresh

2. **Meeting Types** (CMeetingType)
   - Menu: "Meeting Types"
   - Route: `/cmeetingtypeview`
   - Tests: Create, Read, Update, Delete validation, Refresh

3. **Decision Types** (CDecisionType)
   - Menu: "Decision Types"
   - Route: `/cdecisiontypeview`
   - Tests: Create, Read, Update, Delete validation, Refresh

4. **Order Types** (COrderType)
   - Menu: "Order Types"
   - Route: `/cordertypeview`
   - Tests: Create, Read, Update, Delete validation, Refresh

### Status Entities Tested

1. **Activity Status** (CProjectItemStatus)
   - Menu: "Activity Status"
   - Route: `/cprojectitemstatusview`
   - Tests: Create, Read, Update, Delete validation, Refresh

2. **Approval Status** (CApprovalStatus)
   - Menu: "Approval Status"
   - Route: `/capprovalstatusview`
   - Tests: Create, Read, Update, Delete validation, Refresh

## Test Implementation

### Test File Location

```
src/test/java/automated_tests/tech/derbent/ui/automation/CTypeStatusCrudTest.java
```

### Test Methods

Each entity type has a dedicated test method:

```java
@Test
public void testActivityTypeCrudOperations()

@Test
public void testActivityStatusCrudOperations()

@Test
public void testMeetingTypeCrudOperations()

@Test
public void testDecisionTypeCrudOperations()

@Test
public void testOrderTypeCrudOperations()

@Test
public void testApprovalStatusCrudOperations()
```

## Operations Tested

### 1. CREATE Operation

**Steps:**
1. Count initial grid rows
2. Click "New" button
3. Verify "New" button becomes disabled
4. Fill name field with unique test data
5. Verify "Save" button is enabled
6. Click "Save" button
7. Verify success notification appears
8. Verify grid row count increased
9. Verify new entity is selected in grid

**Screenshots:**
- `{entity}-after-new.png` - After clicking New button
- `{entity}-filled.png` - After filling form
- `{entity}-after-save.png` - After saving

### 2. READ Operation

**Steps:**
1. Verify details section displays entity data
2. Count text fields in details section
3. Verify name field has value
4. Capture screenshot of entity details

**Screenshots:**
- `{entity}-read.png` - Entity details displayed

### 3. UPDATE Operation

**Steps:**
1. Modify name field with new unique value
2. Click "Save" button
3. Verify success notification appears
4. Verify field shows updated value

**Screenshots:**
- `{entity}-modified.png` - After modifying field
- `{entity}-after-update.png` - After saving update

### 4. REFRESH Operation

**Steps:**
1. Click "Refresh" button
2. Verify refresh notification appears
3. Verify data is reloaded

**Screenshots:**
- `{entity}-after-refresh.png` - After refresh operation

### 5. DELETE Validation

**Steps:**
1. Select first existing entity (likely non-deletable or in-use)
2. Click "Delete" button
3. Check for error notification or confirmation dialog
4. If confirmation dialog appears, cancel to preserve data
5. Verify appropriate error handling

**Screenshots:**
- `{entity}-delete-attempt.png` - Delete button clicked
- `{entity}-delete-error.png` - Error notification (if applicable)

## Running the Tests

### Using the Test Script (Recommended)

```bash
# Run Type and Status CRUD tests only
./run-playwright-tests.sh status-types

# Run all Playwright tests (includes Type and Status CRUD)
./run-playwright-tests.sh all

# Clean up test artifacts
./run-playwright-tests.sh clean
```

### Using Maven Directly

```bash
# Run all Type and Status CRUD tests
mvn test \
  -Dtest="automated_tests.tech.derbent.ui.automation.CTypeStatusCrudTest" \
  -Dspring.profiles.active=test \
  -Dplaywright.headless=true

# Run a specific test method
mvn test \
  -Dtest="automated_tests.tech.derbent.ui.automation.CTypeStatusCrudTest#testActivityTypeCrudOperations" \
  -Dspring.profiles.active=test \
  -Dplaywright.headless=true
```

### Prerequisites

1. **Playwright Browsers Installed:**
   ```bash
   ./run-playwright-tests.sh install
   ```

2. **Java 17+ and Maven 3.9+:**
   ```bash
   java -version    # Should show Java 17+
   mvn -version     # Should show Maven 3.9+
   ```

3. **Application Dependencies:**
   ```bash
   mvn clean compile
   ```

## Test Configuration

### Spring Boot Test Configuration

The tests use the following configuration:

```java
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, 
                classes = tech.derbent.Application.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "server.port=8080"
})
```

### Test Database

- **Database:** H2 in-memory database
- **URL:** `jdbc:h2:mem:testdb`
- **Username:** `sa`
- **Password:** (empty)
- **Schema:** Auto-created with sample data

### Browser Configuration

- **Browser:** Chromium (Playwright)
- **Mode:** Headless (for CI/CD)
- **Resolution:** Default (configurable in CBaseUITest)

## Screenshots

All screenshots are saved to: `target/screenshots/`

### Screenshot Naming Convention

- `{entity-name}-initial.png` - Initial view
- `{entity-name}-after-new.png` - After New button
- `{entity-name}-filled.png` - Form filled
- `{entity-name}-after-save.png` - After save
- `{entity-name}-read.png` - Entity details
- `{entity-name}-modified.png` - Modified form
- `{entity-name}-after-update.png` - After update
- `{entity-name}-after-refresh.png` - After refresh
- `{entity-name}-delete-attempt.png` - Delete attempted
- `{entity-name}-delete-error.png` - Delete error
- `{entity-name}-crud-error.png` - Test error

## Test Assertions

### Success Criteria

✅ All toolbar buttons present and functional
✅ New button creates new entity
✅ Save button saves entity with validation
✅ Delete button shows error for protected entities
✅ Refresh button reloads data
✅ Notifications appear for all operations
✅ Grid refreshes after operations
✅ Entity selection works correctly
✅ Form fields update properly

### Validation Checks

- Button state (enabled/disabled)
- Notification appearance and content
- Grid row count changes
- Entity selection in grid
- Form field values
- Error handling for invalid operations

## Troubleshooting

### Browser Installation Issues

If you encounter browser installation errors:

```bash
# Install Playwright browsers manually
./run-playwright-tests.sh install

# Set environment variables
export PLAYWRIGHT_BROWSERS_PATH="$HOME/.cache/ms-playwright"
export PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=true
```

### Application Startup Issues

If the application fails to start:

```bash
# Check logs
tail -f target/test-logs/test.log

# Verify H2 profile is active
grep "H2" target/test-logs/test.log

# Clean and rebuild
mvn clean compile
```

### Test Failures

If tests fail:

1. Check screenshots in `target/screenshots/`
2. Review test logs in `target/surefire-reports/`
3. Verify sample data was initialized
4. Ensure no port conflicts (port 8080)
5. Check browser version compatibility

### Headless Mode Issues

If tests fail in headless mode:

```bash
# Run with visible browser for debugging
mvn test \
  -Dtest="automated_tests.tech.derbent.ui.automation.CTypeStatusCrudTest" \
  -Dspring.profiles.active=test \
  -Dplaywright.headless=false
```

## Best Practices

### When Writing New Tests

1. **Follow Existing Patterns:** Use `CBaseUITest` helper methods
2. **Use Descriptive Names:** Clear test and screenshot names
3. **Add Screenshots:** Capture key states for debugging
4. **Handle Errors:** Proper try-catch with error screenshots
5. **Clean Up:** Don't delete data that other tests need
6. **Verify State:** Check button states and notifications

### Test Data Management

- Use unique timestamps in test data: `System.currentTimeMillis()`
- Don't delete sample data entities
- Cancel delete operations on existing entities
- Use NEW entities for delete tests

### Performance Optimization

- Run specific tests during development
- Use `status-types` script for focused testing
- Clean screenshots regularly
- Increase timeouts for slow environments

## Integration with CI/CD

### GitHub Actions Example

```yaml
- name: Run Type and Status CRUD Tests
  run: |
    ./run-playwright-tests.sh status-types
  timeout-minutes: 10

- name: Upload Screenshots
  if: always()
  uses: actions/upload-artifact@v3
  with:
    name: playwright-screenshots
    path: target/screenshots/
```

### Jenkins Example

```groovy
stage('Type and Status CRUD Tests') {
    steps {
        sh './run-playwright-tests.sh status-types'
    }
    post {
        always {
            archiveArtifacts artifacts: 'target/screenshots/*.png'
            junit 'target/surefire-reports/*.xml'
        }
    }
}
```

## Related Documentation

- `docs/testing/playwright-*.md` - Playwright testing strategies
- `MANUAL_TESTING_GUIDE.md` - Manual testing procedures
- `README.md` - Project overview and setup
- `.github/copilot-instructions.md` - Development guidelines

## Support

For issues or questions:
1. Check existing screenshots in `target/screenshots/`
2. Review test logs in `target/surefire-reports/`
3. Consult the troubleshooting section above
4. Review similar tests in `CDependencyCheckingTest`
5. Check the CBaseUITest class for helper methods

## Summary

The Type and Status CRUD test suite provides comprehensive coverage of all CRUD operations for Type and Status entities in the Derbent application. The tests follow established patterns, include proper error handling, and generate screenshots for debugging. They are ready for execution in CI/CD environments and can be run individually or as part of the full test suite.
