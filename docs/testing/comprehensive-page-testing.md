# CPageTestAuxillary Comprehensive Test Suite

## Overview

The CPageTestAuxillary Comprehensive Test Suite is a Playwright-based automated testing framework that dynamically tests all pages accessible via navigation buttons on the CPageTestAuxillary page. It provides complete coverage of the application's dynamic pages without hardcoding page references.

## Architecture

### Design Principles

1. **Dynamic Discovery**: Automatically discovers all navigation buttons without hardcoding
2. **Generic Testing**: Reusable test functions work with any page type
3. **Conditional Validation**: Tests adapt based on page content (grids, CRUD toolbars, attachments, comments, links)
4. **Fast Execution**: Efficient timeouts and minimal waits
5. **Complete Coverage**: Tests every single button, no skipping
6. **Detailed Logging**: Clear progress indicators and error messages

### Components

#### 1. CPageTestAuxillary (Enhanced)

**Purpose**: Serves as a navigation hub for Playwright tests with metadata support.

**Enhancements**:
- **Button IDs**: Each button gets a unique, stable ID: `test-aux-btn-{sanitized-title}-{index}`
- **Metadata Div**: Hidden div (`#test-auxillary-metadata`) with button count
- **Data Attributes**: Each button has:
  - `data-route`: Target route
  - `data-title`: Button title
  - `data-button-index`: Button index

**Example Button HTML**:
```html
<vaadin-button 
  id="test-aux-btn-projects-0" 
  data-route="cdynamicpagerouter/page:1"
  data-title="Projects"
  data-button-index="0">
  Projects
</vaadin-button>
```

#### 2. CPageTestAuxillaryComprehensiveTest

**Purpose**: Main test class that orchestrates comprehensive page testing.

**Key Methods**:

##### Navigation Methods
- `navigateToTestAuxillaryPage()`: Navigate to CPageTestAuxillary
- `discoverNavigationButtons()`: Dynamically find all test buttons
- `testNavigationButton()`: Test a single button's target page

##### Check Functions (Generic Validators)
- `checkGridExists()`: Check if page has a grid
- `checkGridHasData()`: Verify grid contains data
- `checkGridIsSortable()`: Check for sortable columns
- `checkCrudToolbarExists()`: Check for CRUD buttons
- `checkCrudButtonExists(buttonText)`: Check specific CRUD button

##### Test Execution Functions
- `runGridTests(pageName)`: Execute all grid-related tests
- `runCrudToolbarTests(pageName)`: Execute all CRUD toolbar tests
- `testGridSorting(pageName)`: Test column sorting
- `testGridRowSelection(pageName)`: Test row selection
- `testNewButton(pageName)`: Test New button functionality
- `testEditButton(pageName)`: Test Edit button functionality

##### Component Testers
- `CAttachmentComponentTester`: Validates attachment add/download/delete flows
- `CCommentComponentTester`: Validates comment add/edit/delete flows
- `CLinkComponentTester`: Validates link add/edit/delete flows

## Component Tester Pattern (MANDATORY)

All future UI automation updates must follow the component tester pattern below to keep tests consistent and composable.

### Declaration Pattern

```java
private final CAttachmentComponentTester attachmentTester = new CAttachmentComponentTester();
private final CCommentComponentTester commentTester = new CCommentComponentTester();
private final CLinkComponentTester linkTester = new CLinkComponentTester();
```

### Execution Pattern

```java
if (attachmentTester.canTest(page)) {
    attachmentTester.test(page);
}
if (commentTester.canTest(page)) {
    commentTester.test(page);
}
if (linkTester.canTest(page)) {
    linkTester.test(page);
}
```

### Design Rules

- **Always implement `IComponentTester`** and extend `CBaseComponentTester` for new component testers.
- **Detection must live in `canTest(page)`**, never in the orchestration class.
- **UI opening logic lives inside the tester** (tabs/accordions), not in the test class.
- **Test classes only orchestrate**, they do not contain component-specific selectors or CRUD flows.

## Test Workflow

### High-Level Flow

```
1. Login to application
2. Navigate to CPageTestAuxillary page
3. Discover all navigation buttons dynamically
4. For each button:
   a. Click button to navigate to target page
   b. Analyze page content (grid, CRUD toolbar, attachments, comments, links)
   c. Run appropriate tests based on content
   d. Capture screenshots at key points
   e. Return to test page for next button
5. Generate summary statistics
```

### Detailed Test Sequence

For each discovered button:

```
1. Click button → navigate to target page
2. Take initial screenshot
3. Check for grid:
   - If present: Run grid tests
     * Check if grid has data
     * Check if grid is sortable
     * Count grid rows
     * Test sorting functionality
     * Test row selection
4. Check for CRUD toolbar:
   - If present: Run CRUD tests
     * Identify available buttons (New, Edit, Delete, Save, Cancel)
     * Test New button (click → check form/dialog → close)
     * Test Edit button (select row → click → check form → close)
5. Check for attachments/comments/links:
   - If present: Run component tests via `CAttachmentComponentTester`, `CCommentComponentTester`, `CLinkComponentTester`
   - Opens the relevant tab/accordion and validates add/edit/delete flows where available
6. Take final screenshot
7. Navigate back to test page
```

## Usage

### Running the Comprehensive Test

#### Using Maven

```bash
# Run with Playwright in headless mode
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dplaywright.headless=true

# Run with visible browser (for debugging)
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dplaywright.headless=false
```

#### Using the Convenience Script

```bash
# Run the comprehensive test
./run-comprehensive-test.sh
```

### Output

The test generates:
- **Console logs**: Detailed progress with emojis and clear formatting
- **Screenshots**: Saved to `target/screenshots/` with sequential numbering
- **Summary statistics**: Total buttons, pages visited, pages with grids/CRUD
- **Coverage result sheets**: CSV + Markdown written to `test-results/playwright/coverage/`
- **JaCoCo reports**: `target/site/jacoco/` (HTML + XML)

### Coverage Metrics

The comprehensive test calculates per-page coverage metrics and writes results as:
- `page-coverage-<timestamp>.csv` (raw metrics)
- `page-coverage-<timestamp>.md` (summary report)

Metrics include: visited flag, grid presence, CRUD toolbar presence, kanban presence, and CRUD button availability.
Attachment/comment/link component coverage is logged in the console and exercised by component testers when detected.

### Screenshot Naming Convention

Screenshots follow a sequential naming pattern:

```
001-after-login.png                    # After successful login
002-test-auxillary-page.png            # CPageTestAuxillary page
003-page-projects-initial.png          # Initial state of Projects page
004-page-projects-sorted.png           # Projects page after sorting
005-page-projects-grid-tested.png      # After grid tests
006-page-projects-new-clicked.png      # New button clicked
007-page-projects-crud-tested.png      # After CRUD tests
008-page-projects-final.png            # Final state of Projects page
...
```

## Adding New Pages to Test

### Automatic Detection

Pages are automatically included in the test when they appear in the CHierarchicalSideMenu. No code changes required!

### Manual Addition

To manually add a page to CPageTestAuxillary:

```java
// In any service or initializer
@Autowired
private CPageTestAuxillaryService pageTestAuxillaryService;

// Add a route
pageTestAuxillaryService.addRoute(
    "My New Page",           // Title
    "vaadin:file",           // Icon name
    "#4CAF50",              // Icon color
    "my-new-page-route"     // Route
);
```

## Check Function Reference

### Grid Check Functions

| Function | Returns | Description |
|----------|---------|-------------|
| `checkGridExists()` | boolean | True if any grid element is present |
| `checkGridHasData()` | boolean | True if grid contains cell content |
| `checkGridIsSortable()` | boolean | True if grid has sortable columns |
| `getGridRowCount()` | int | Number of rows in the grid |

### CRUD Check Functions

| Function | Returns | Description |
|----------|---------|-------------|
| `checkCrudToolbarExists()` | boolean | True if 2+ CRUD buttons exist |
| `checkCrudButtonExists(text)` | boolean | True if specific button exists |

## Test Execution Functions

### Grid Test Functions

| Function | Purpose | Actions |
|----------|---------|---------|
| `runGridTests(pageName)` | Execute all grid tests | Data check, sorting, row count, selection |
| `testGridSorting(pageName)` | Test column sorting | Click sorter twice (asc → desc) |
| `testGridRowSelection(pageName)` | Test row selection | Click first grid row |

### CRUD Test Functions

| Function | Purpose | Actions |
|----------|---------|---------|
| `runCrudToolbarTests(pageName)` | Execute all CRUD tests | Test available buttons |
| `testNewButton(pageName)` | Test New functionality | Click New → check form → close |
| `testEditButton(pageName)` | Test Edit functionality | Select row → click Edit → check form → close |

## Extending the Test Suite

### Adding New Check Functions

To add a new generic check function:

```java
/** Check if page has a specific feature.
 * @return true if feature is present */
private boolean checkMyFeatureExists() {
    try {
        Locator elements = page.locator("my-feature-selector");
        return elements.count() > 0;
    } catch (Exception e) {
        LOGGER.debug("Error checking for my feature: {}", e.getMessage());
        return false;
    }
}
```

### Adding New Test Functions

To add a new test function:

```java
/** Run tests for my feature.
 * @param pageName Page name for screenshots */
private void runMyFeatureTests(String pageName) {
    try {
        LOGGER.info("   🧪 Testing my feature...");
        
        // Perform tests
        boolean hasFeature = checkMyFeatureExists();
        LOGGER.info("      ✓ Feature exists: {}", hasFeature);
        
        if (hasFeature) {
            // Test feature functionality
            // ...
            takeScreenshot(String.format("%03d-page-%s-feature-tested", 
                screenshotCounter++, pageName), false);
        }
    } catch (Exception e) {
        LOGGER.warn("⚠️  My feature tests failed: {}", e.getMessage());
    }
}
```

### Integrating New Tests

Add the new test to `testNavigationButton()`:

```java
// Check what's on the page
boolean hasMyFeature = checkMyFeatureExists();
LOGGER.info("   My feature present: {}", hasMyFeature);

// Run conditional tests
if (hasMyFeature) {
    LOGGER.info("🧪 Running my feature tests...");
    runMyFeatureTests(pageNameSafe);
}
```

## Troubleshooting

### Common Issues

#### 1. Test Hangs During Navigation

**Symptom**: Test gets stuck after clicking a button

**Solution**: Check page load timeouts in `testNavigationButton()`:
```java
wait_2000(); // Increase if pages load slowly
```

#### 2. Grid Not Detected

**Symptom**: Grid exists but `checkGridExists()` returns false

**Solution**: Add your grid selector to the locator:
```java
Locator grids = page.locator("vaadin-grid, vaadin-grid-pro, so-grid, c-grid, my-custom-grid");
```

#### 3. CRUD Buttons Not Found

**Symptom**: CRUD toolbar exists but not detected

**Solution**: Verify button text matches exactly (case-sensitive):
```java
Locator newButton = page.locator("vaadin-button:has-text('New')");
// vs
Locator newButton = page.locator("vaadin-button:has-text('NEW')");
```

#### 4. Screenshots Not Generated

**Symptom**: Tests run but no screenshots created

**Solution**: Ensure screenshot directory exists:
```bash
mkdir -p target/screenshots
```

## Performance Considerations

### Timeouts

The test uses these timeouts:
- `wait_500()`: 500ms - UI updates
- `wait_1000()`: 1s - Form loads, dialog appearances
- `wait_2000()`: 2s - Page navigation, data loading

### Optimization Tips

1. **Reduce screenshot frequency**: Comment out intermediate screenshots for faster runs
2. **Parallel execution**: Run multiple test instances (not implemented yet)
3. **Selective testing**: Filter buttons by route pattern (not implemented yet)

## Integration with CI/CD

### GitHub Actions Example

```yaml
- name: Run Comprehensive Tests
  run: |
    source ./setup-java-env.sh
    mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dplaywright.headless=true

- name: Upload Screenshots
  if: always()
  uses: actions/upload-artifact@v3
  with:
    name: test-screenshots
    path: target/screenshots/
```

## Coding Standards Compliance

This test suite follows the Derbent coding standards:

- ✅ All classes prefixed with 'C'
- ✅ Uses CBaseUITest as base class
- ✅ Comprehensive logging with SLF4J
- ✅ Detailed JavaDoc comments
- ✅ Check utility for assertions
- ✅ Follows MVC pattern
- ✅ No direct Notification.show() usage

## Future Enhancements

Potential improvements for future versions:

1. **Parallel Testing**: Run multiple buttons concurrently
2. **Test Filtering**: Filter buttons by route pattern or title
3. **Performance Metrics**: Track page load times
4. **Accessibility Testing**: Add WCAG compliance checks
5. **Data Validation**: Verify data consistency across pages
6. **Error Recovery**: Retry failed navigations
7. **Test Reports**: Generate HTML reports with statistics
8. **Deep CRUD Testing**: Full create/read/update/delete cycles

## References

- Base Test Class: `CBaseUITest.java`
- Test Page: `CPageTestAuxillary.java`
- Service: `CPageTestAuxillaryService.java`
- Coding Standards: `docs/architecture/coding-standards.md`
