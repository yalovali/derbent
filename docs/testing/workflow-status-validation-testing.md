# Workflow Status and Name Validation Testing

## Overview

This document describes the automated Playwright tests for the workflow status management and name field validation features added to the Derbent application.

## Test Suite: CWorkflowStatusAndValidationTest

Location: `src/test/java/automated_tests/tech/derbent/ui/automation/CWorkflowStatusAndValidationTest.java`

### Features Tested

1. **Workflow Status Combobox Display**
   - Verifies that status combobox appears in CRUD toolbar for workflow entities
   - Tests status combobox shows valid workflow transitions
   - Validates combobox functionality across multiple entity types

2. **Name Field Validation**
   - Tests that save button is disabled when name field is empty
   - Verifies save button is enabled when name field has content
   - Tests validation behavior across multiple entity types

### Test Methods

#### 1. testWorkflowStatusComboboxAppears()

**Purpose:** Verify that workflow entities display a status combobox in the CRUD toolbar.

**Workflow:**
1. Login to the application
2. Navigate to each workflow entity route (Activities, Meetings, Products, etc.)
3. Click first row in grid to select an entity
4. Verify status combobox is present
5. Open combobox to verify options are available
6. Capture screenshots at each step

**Expected Results:**
- Status combobox should appear for all workflow entities
- Combobox should contain valid status transition options

**Tested Entity Routes:**
- Activities (page:3)
- Meetings (page:4)
- Products (page:5)
- Components (page:6)
- Risks (page:7)
- Deliverables (page:8)
- Assets (page:9)
- Milestones (page:10)
- Tickets (page:11)

#### 2. testSaveButtonDisabledWithEmptyName()

**Purpose:** Verify that the save button is disabled when the name field is empty.

**Workflow:**
1. Login to the application
2. Navigate to Activities page
3. Click "New" button to create new entity
4. Fill name field with text
5. Verify save button is enabled
6. Clear name field completely
7. Verify save button becomes disabled
8. Refill name field
9. Verify save button is re-enabled

**Expected Results:**
- Save button should be enabled when name has content
- Save button should be disabled when name is empty
- Save button should be re-enabled when name is filled again

#### 3. testNameValidationOnMultipleEntities()

**Purpose:** Test name field validation works consistently across different entity types.

**Workflow:**
1. Login to the application
2. For each test entity (Activities, Meetings, Products):
   - Navigate to entity page
   - Click "New" button
   - Clear name field
   - Verify save button is disabled
   - Capture screenshot
   - Cancel and return

**Expected Results:**
- Name validation should work consistently across all entity types
- Save button disabled behavior should be uniform

## Running the Tests

### Command Line

Run the workflow validation test suite:

```bash
./run-playwright-tests.sh workflow-validation
```

### With Custom Configuration

```bash
# Run with visible browser and slow motion for debugging
PLAYWRIGHT_SLOWMO=500 ./run-playwright-tests.sh workflow-validation

# Run in headless mode without screenshots (fast)
PLAYWRIGHT_HEADLESS=true PLAYWRIGHT_SKIP_SCREENSHOTS=true ./run-playwright-tests.sh workflow-validation

# Run with interactive configuration menu
INTERACTIVE_MODE=true ./run-playwright-tests.sh workflow-validation
```

### Environment Variables

- `PLAYWRIGHT_HEADLESS` - Set to 'true' for headless mode (default: false)
- `PLAYWRIGHT_SHOW_CONSOLE` - Set to 'true' to show console output (default: true)
- `PLAYWRIGHT_SKIP_SCREENSHOTS` - Set to 'true' to disable screenshots (default: false)
- `PLAYWRIGHT_SLOWMO` - Delay in ms between actions for debugging (default: 0)
- `PLAYWRIGHT_VIEWPORT_WIDTH` - Browser width in pixels (default: 1920)
- `PLAYWRIGHT_VIEWPORT_HEIGHT` - Browser height in pixels (default: 1080)

## Expected Test Duration

- **Quick run (headless, no screenshots):** ~1 minute
- **Full run (visible browser, with screenshots):** ~2-3 minutes

## Screenshots

Screenshots are saved to `target/screenshots/` when enabled (default).

### Screenshot Naming Convention

- `###-login-success.png` - After successful login
- `###-status-combobox-*.png` - Status combobox for each entity
- `###-status-options-*.png` - Status combobox options opened
- `###-name-filled-save-enabled.png` - Save button enabled with name
- `###-name-empty-save-disabled.png` - Save button disabled when empty
- `###-name-refilled-save-reenabled.png` - Save button re-enabled
- `###-validation-*.png` - Validation test for each entity

## Integration with CI/CD

### Windows

The test suite is compatible with Windows runners. Ensure:
- Java 21 is installed
- Maven is configured
- Playwright browsers are installed (run `./run-playwright-tests.sh install`)

### Linux

The test suite runs natively on Linux. Dependencies:
- Java 21
- Maven 3.9+
- Playwright browsers (auto-installed on first run)

### GitHub Actions

Example workflow configuration:

```yaml
- name: Run Workflow Validation Tests
  run: |
    chmod +x run-playwright-tests.sh
    PLAYWRIGHT_HEADLESS=true PLAYWRIGHT_SKIP_SCREENSHOTS=false ./run-playwright-tests.sh workflow-validation
  
- name: Upload Test Screenshots
  if: always()
  uses: actions/upload-artifact@v3
  with:
    name: workflow-validation-screenshots
    path: target/screenshots/
```

## Troubleshooting

### Common Issues

1. **Browser not found**
   - Run: `./run-playwright-tests.sh install`
   - Or manually: `mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"`

2. **Test timeout**
   - Increase timeout in test configuration
   - Check application starts correctly
   - Verify database initialization completes

3. **Save button not disabling**
   - Verify name field validation is properly configured in CPageService
   - Check that getCrudToolbar() is available in the view
   - Review console logs for validation errors

4. **Status combobox not appearing**
   - Verify entity implements IHasStatusAndWorkflow
   - Check CPageService implements IPageServiceHasStatusAndWorkflow
   - Verify CProjectItemStatusService is initialized

## Test Maintenance

### Adding New Entity Tests

To add tests for a new workflow entity:

1. Add the entity route to `WORKFLOW_ENTITY_ROUTES` list in the test class
2. Run the test to verify it works
3. Update this documentation with the new route

### Modifying Selectors

If UI selectors change, update the following constants in the test class:
- Status combobox selector patterns
- Name field selector patterns
- Save button selector patterns

## Related Documentation

- [Copilot Guidelines](../development/copilot-guidelines.md)
- [Comprehensive Page Testing](comprehensive-page-testing.md)
- [Playwright Test Infrastructure](playwright-test-infrastructure.md)

## Features Covered

### Workflow Status Support
- Status combobox in CRUD toolbar for IHasStatusAndWorkflow entities
- Valid workflow transitions based on CProjectItemStatusService
- Status change validation and persistence

### Name Field Validation
- Automatic validation in CPageService base class
- Save button state management via CCrudToolbar
- Extensible validation pattern for custom rules

## Test Coverage

- ✅ Status combobox display for workflow entities
- ✅ Status combobox options population
- ✅ Save button disabled with empty name
- ✅ Save button enabled with name content
- ✅ Save button re-enabled after clearing and refilling
- ✅ Cross-entity validation consistency
- ✅ Screenshot capture at key validation points

## Success Criteria

Tests pass when:
1. Status combobox appears for at least 80% of workflow entities
2. Save button correctly disables when name is empty
3. Save button correctly enables when name has content
4. No exceptions are thrown during test execution
5. Screenshots capture key validation states

## Notes

- Tests use H2 in-memory database for fast execution
- Sample data is automatically initialized on first login
- Tests are designed to be idempotent and can run in any order
- Screenshots are optional but recommended for debugging
