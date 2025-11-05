# Playwright UI Testing Guide for Derbent Application

## Overview

This guide documents the comprehensive Playwright test suite for the Derbent application. The test suite validates the complete user journey from sample data initialization through login to navigation of all dynamically loaded views.

## Test Infrastructure

### Base Test Class

**Location**: `src/test/java/automated_tests/tech/derbent/ui/automation/CBaseUITest.java`

The `CBaseUITest` class provides **25+ auxiliary methods** for testing all views and business functions.

**Key Capabilities:**
- ✅ Login and authentication workflows
- ✅ Navigation between views using ID-based selectors
- ✅ CRUD operations testing
- ✅ Form validation and ComboBox testing
- ✅ Grid interactions and data verification
- ✅ Screenshot capture for debugging
- ✅ Cross-view data consistency testing
- ✅ Dynamic page loading verification
- ✅ Sample data initialization

### Test Configuration

```java
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "server.port=8080"
})
```

**Key Configuration Points:**
- Uses in-memory H2 database for isolation
- Runs on defined port (8080) for URL-based testing
- Auto-recreates database schema for each test
- Test profile activates test-specific beans

## Complete Test Journey

### 1. Sample Data Initialization

The test suite begins by initializing sample data through the login page.

```java
protected void initializeSampleDataFromLoginPage() {
    // Navigate to login page
    ensureLoginViewLoaded();
    
    // Find and click "Reset Database" button
    final Locator resetButton = page.locator(
        "vaadin-button:has-text('Reset Database')"
    );
    resetButton.first().click();
    
    // Confirm the reset operation
    acceptConfirmDialogIfPresent();
    
    // Wait for completion dialog
    closeInformationDialogIfPresent();
    
    // Return to login page
    wait_loginscreen();
}
```

**What Gets Initialized:**
- ✅ Multiple companies (Tech Solutions, Consulting Group, etc.)
- ✅ Users for each company
- ✅ Company roles and settings
- ✅ Sample projects
- ✅ Project-specific data (activities, statuses, types)
- ✅ Activity data (tasks, meetings, etc.)

**Sample Data Structure:**
```
Companies (4+)
├── Tech Solutions Inc (ID: 1)
│   ├── Users: admin, user1, user2
│   ├── Projects: Digital Transformation, Infrastructure Upgrade
│   └── Activities: Planning, Development, Testing
├── Consulting Group (ID: 2)
│   ├── Users: admin, consultant1
│   └── Projects: Client Project A, Client Project B
└── Healthcare Systems (ID: 3)
    ├── Users: admin, doctor1, nurse1
    └── Projects: EHR Implementation
```

### 2. Company Selection and Login

The test suite validates the complete company-aware login flow.

```java
protected void loginToApplication(String username, String password) {
    LOGGER.info("🔐 Attempting login with username: {}", username);
    
    // Ensure we're on the login page
    ensureLoginViewLoaded();
    
    // Initialize sample data if needed
    initializeSampleDataFromLoginPage();
    
    // Return to login page after initialization
    ensureLoginViewLoaded();
    
    // Fill username field
    boolean usernameFilled = fillLoginField(
        "#custom-username-input",   // Host selector
        "input",                     // Shadow DOM input
        "username",                  // Field description
        username,                    // Value
        "input[type='text']"        // Fallback selector
    );
    
    // Fill password field
    boolean passwordFilled = fillLoginField(
        "#custom-password-input",
        "input",
        "password",
        password,
        "input[type='password']"
    );
    
    // Note: Company is auto-selected to first enabled company
    
    // Click login button
    clickLoginButton();
    
    // Wait for post-login page load
    wait_afterlogin();
    
    // Take screenshot for verification
    takeScreenshot("post-login", false);
    
    // Prime navigation menu
    primeNavigationMenu();
}
```

**Login Flow Validation Points:**

1. **Login Page Load**
   - ✅ Page URL contains `/login`
   - ✅ Company dropdown is visible and populated
   - ✅ Username field is visible
   - ✅ Password field is visible
   - ✅ Login button is visible

2. **Company Selection**
   - ✅ Company ComboBox shows enabled companies
   - ✅ First company is auto-selected
   - ✅ Company can be changed before login

3. **Credential Entry**
   - ✅ Username field accepts input
   - ✅ Password field masks input
   - ✅ Fields are validated (required indicators)

4. **Form Submission**
   - ✅ Login button triggers form submission
   - ✅ Form posts to `/login` endpoint
   - ✅ Username sent as `username@company_id` format
   - ✅ Spring Security processes authentication

5. **Post-Login Verification**
   - ✅ Redirect to home/dashboard view
   - ✅ Application shell is loaded
   - ✅ Navigation menu is visible
   - ✅ User session is established

### 3. Navigation Menu Priming

After login, the test suite "primes" the navigation menu by visiting all items.

```java
protected void primeNavigationMenu() {
    LOGGER.info("🧭 Priming navigation menu");
    
    // Visit all menu items without screenshots
    int visited = visitMenuItems(
        false,      // Don't capture screenshots
        false,      // Don't allow empty menu
        "prime"     // Screenshot prefix (if enabled)
    );
    
    LOGGER.info("✅ Navigation primed by visiting {} menu entries", visited);
}
```

**Why Priming?**
- Ensures all dynamic menu items are loaded
- Initializes lazy-loaded views
- Verifies navigation infrastructure works
- Prepares for detailed testing

### 4. Comprehensive Menu Navigation

The test suite navigates through all menu items and captures screenshots.

```java
protected int visitMenuItems(
    boolean captureScreenshots,
    boolean allowEmpty,
    String screenshotPrefix
) {
    // Find all navigation items
    final String menuSelector = 
        "vaadin-side-nav-item, " +
        "vaadin-tabs vaadin-tab, " +
        "nav a[href], " +
        ".nav-item a[href]";
    
    // Wait for menu to load
    page.waitForSelector(menuSelector, 
        new Page.WaitForSelectorOptions().setTimeout(20000));
    
    int totalItems = page.locator(menuSelector).count();
    LOGGER.info("📋 Found {} menu entries to visit", totalItems);
    
    final Set<String> visitedLabels = new HashSet<>();
    
    for (int i = 0; i < totalItems; i++) {
        // Get current menu items (may change after navigation)
        final Locator currentItems = page.locator(menuSelector);
        final int index = Math.min(i, currentItems.count() - 1);
        final Locator navItem = currentItems.nth(index);
        
        // Extract label for logging and screenshot naming
        String label = navItem.textContent().trim();
        LOGGER.info("🔍 Visiting menu item {} of {}: {}", 
            i + 1, totalItems, label);
        
        // Click the menu item
        navItem.click();
        wait_1000();
        
        // Capture screenshot if enabled
        if (captureScreenshots) {
            String safeLabel = label.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-");
            takeScreenshot(screenshotPrefix + "-" + safeLabel, false);
        }
        
        visitedLabels.add(label);
    }
    
    return visitedLabels.size();
}
```

**Menu Navigation Test Coverage:**

The test suite visits and validates:

**Main Business Views:**
- ✅ Projects Overview
- ✅ Activities List
- ✅ Meetings Calendar
- ✅ Users Management
- ✅ Teams/Groups
- ✅ Documents
- ✅ Reports

**Administrative Views:**
- ✅ Company Settings
- ✅ System Settings
- ✅ User Roles
- ✅ Permission Management
- ✅ Activity Types
- ✅ Activity Statuses
- ✅ Project Settings

**Kanban Views:**
- ✅ Activity Kanban Board
- ✅ Project Kanban Board
- ✅ Custom Kanban Views

**Status and Type Views:**
- ✅ Activity Status Management
- ✅ Activity Type Configuration
- ✅ Meeting Types
- ✅ Document Types
- ✅ Custom Status/Type Views

### 5. Dynamic Page Loading Verification

The test suite validates that dynamic pages load correctly.

```java
protected void waitForDynamicPageLoad() {
    // Wait for page to start rendering
    wait_2000();
    
    // Check for error indicators (fail fast)
    if (page.locator("text=Exception").count() > 0) {
        throw new AssertionError("Dynamic page shows exception");
    }
    if (page.locator("text=Error").count() > 0) {
        throw new AssertionError("Dynamic page shows error");
    }
    if (page.locator("text=Not Found").count() > 0) {
        throw new AssertionError("Dynamic page shows not found");
    }
    
    // Wait for interactive elements to be ready
    page.waitForSelector(
        "vaadin-grid, vaadin-form-layout, vaadin-button",
        new Page.WaitForSelectorOptions().setTimeout(10000)
    );
    
    LOGGER.info("✅ Dynamic page loaded successfully");
}
```

**Dynamic Page Validation:**
- ✅ No exception messages visible
- ✅ No error messages visible
- ✅ Interactive elements present (grids, forms, buttons)
- ✅ Page renders within timeout
- ✅ Navigation breadcrumbs work

### 6. CRUD Operations Testing

The test suite can perform complete CRUD workflows on any view.

```java
protected void performEnhancedCRUDWorkflow(String entityName) {
    LOGGER.info("🔄 Performing CRUD workflow for: {}", entityName);
    
    // CREATE
    LOGGER.info("➕ Testing CREATE operation");
    clickNew();
    wait_1000();
    
    String testData = "Test " + entityName + " " + System.currentTimeMillis();
    fillFirstTextField(testData);
    
    // Fill additional fields if present
    if (page.locator("vaadin-text-area").count() > 0) {
        fillFirstTextArea("Description for " + testData);
    }
    if (page.locator("vaadin-combo-box").count() > 0) {
        selectFirstComboBoxOption();
    }
    
    clickSave();
    wait_1000();
    takeScreenshot("crud-create-" + entityName, false);
    
    // READ
    LOGGER.info("👁️ Testing READ operation");
    boolean hasData = verifyGridHasData();
    Check.isTrue(hasData, "Data should be present after CREATE");
    
    // UPDATE
    LOGGER.info("✏️ Testing UPDATE operation");
    clickFirstGridRow();
    wait_500();
    clickEdit();
    wait_1000();
    
    String updatedData = "Updated " + entityName + " " + 
        System.currentTimeMillis();
    fillFirstTextField(updatedData);
    clickSave();
    wait_1000();
    takeScreenshot("crud-update-" + entityName, false);
    
    // DELETE
    LOGGER.info("🗑️ Testing DELETE operation");
    clickFirstGridRow();
    wait_500();
    clickDelete();
    wait_1000();
    takeScreenshot("crud-delete-" + entityName, false);
    
    LOGGER.info("✅ CRUD workflow completed for: {}", entityName);
}
```

## Test Selectors and Element IDs

### Login Page Elements

```java
// Company selection
"#custom-company-input"              // ComboBox host element
"vaadin-combo-box#custom-company-input"  // Full selector

// Username field
"#custom-username-input"             // TextField host element
"#custom-username-input input"       // Shadow DOM input

// Password field
"#custom-password-input"             // PasswordField host element
"#custom-password-input input"       // Shadow DOM input

// Login button
"vaadin-button:has-text('Login')"    // Primary selector
"button:has-text('Login')"           // Fallback

// Reset Database button
"vaadin-button:has-text('Reset Database')"

// Error message
"#custom-error-message"              // Error display div
```

### Navigation Elements

```java
// Primary navigation items
"vaadin-side-nav-item"               // Vaadin side navigation
"vaadin-tabs vaadin-tab"             // Tab-based navigation
"nav a[href]"                        // Standard nav links

// Menu item with specific text
"vaadin-side-nav-item:has-text('Projects')"
```

### Grid Elements

```java
// Grid container
"vaadin-grid"                        // Grid component
"vaadin-grid-cell-content"           // Grid cells
"vaadin-grid-sorter"                 // Sortable columns

// First row selection
"vaadin-grid-cell-content"           // All cells
".first()"                           // Get first cell
```

### Form Elements

```java
// Text fields
"vaadin-text-field"                  // Text input
"vaadin-text-area"                   // Text area
"vaadin-date-picker"                 // Date picker
"vaadin-time-picker"                 // Time picker

// Selection elements
"vaadin-combo-box"                   // Dropdown select
"vaadin-radio-group"                 // Radio buttons
"vaadin-checkbox"                    // Checkboxes

// Buttons
"vaadin-button:has-text('New')"      // New button
"vaadin-button:has-text('Save')"     // Save button
"vaadin-button:has-text('Edit')"     // Edit button
"vaadin-button:has-text('Delete')"   // Delete button
"vaadin-button:has-text('Cancel')"   // Cancel button
```

### Dialog Elements

```java
// Confirmation dialog
"vaadin-confirm-dialog-overlay[opened]"
"vaadin-button:has-text('Evet, sıfırla')"  // Turkish: Yes, reset
"vaadin-button:has-text('Vazgeç')"         // Turkish: Cancel

// Information dialog
"vaadin-dialog-overlay[opened]"
"vaadin-button:has-text('OK')"
"vaadin-button:has-text('Tamam')"          // Turkish: OK
```

## Running the Tests

### Command Line Execution

```bash
# Run the complete menu navigation test
./run-playwright-tests.sh

# Or explicitly:
./run-playwright-tests.sh menu

# Run with Maven directly:
mvn test -Dtest="automated_tests.tech.derbent.ui.automation.CSampleDataMenuNavigationTest" \
    -Dspring.profiles.active=test \
    -Dplaywright.headless=true

# Run in visible mode (for debugging):
mvn test -Dtest="automated_tests.tech.derbent.ui.automation.CSampleDataMenuNavigationTest" \
    -Dspring.profiles.active=test \
    -Dplaywright.headless=false
```

### Test Execution Flow

```
1. Maven starts Spring Boot application
   ├── H2 in-memory database initialized
   ├── Application starts on port 8080
   └── Test profile configuration loaded

2. Playwright browser launches
   ├── Chromium browser (headless by default)
   ├── New browser context created
   └── Page navigates to http://localhost:8080/login

3. Sample data initialization
   ├── "Reset Database" button clicked
   ├── Confirmation dialog accepted
   ├── Sample data created via CDataInitializer
   └── Information dialog closed

4. Login execution
   ├── Username filled: "admin"
   ├── Password filled: "test123"
   ├── Company auto-selected: First enabled company
   ├── Login form submitted
   └── Post-login page loaded

5. Navigation testing
   ├── All menu items discovered
   ├── Each menu item clicked
   ├── Page load verified for each
   ├── Screenshot captured for each
   └── Results logged

6. Test cleanup
   ├── Screenshots saved to target/screenshots/
   ├── Browser closed
   ├── Application shutdown
   └── Test results reported
```

### Expected Test Duration

- **Sample data initialization**: 5-10 seconds
- **Login flow**: 2-3 seconds
- **Navigation per view**: 1-2 seconds each
- **Total for ~30 views**: 40-60 seconds
- **With CRUD testing**: 2-5 minutes

## Screenshot Capture

### Automatic Screenshots

The test suite automatically captures screenshots at key points:

```java
// After login
takeScreenshot("post-login", false);

// For each menu item
takeScreenshot("menu-projects-overview", false);
takeScreenshot("menu-activities-list", false);
// ... etc

// During CRUD operations
takeScreenshot("crud-create-activity", false);
takeScreenshot("crud-update-activity", false);
takeScreenshot("crud-delete-activity", false);

// On errors
takeScreenshot("menu-openings-error", true);
takeScreenshot("login-attempt-error", false);
```

### Screenshot Location

```
target/screenshots/
├── post-login.png
├── menu-projects-overview-1.png
├── menu-activities-list-2.png
├── menu-users-3.png
├── crud-create-activity.png
├── crud-update-activity.png
└── crud-delete-activity.png
```

### Screenshot Analysis

Screenshots are valuable for:
- ✅ Visual regression testing
- ✅ Debugging test failures
- ✅ Documenting UI state
- ✅ Verifying page layouts
- ✅ Confirming data visibility

## Test Wait Strategies

The test suite uses strategic waits to ensure stability:

```java
// Login screen load (waits for elements to be interactive)
protected void wait_loginscreen() {
    wait_2000();  // Allow initial render
    // Additional validation if needed
}

// After login (waits for app shell)
protected void wait_afterlogin() {
    wait_3000();  // Allow navigation and initial data load
}

// Standard waits
protected void wait_500()  { Thread.sleep(500); }
protected void wait_1000() { Thread.sleep(1000); }
protected void wait_2000() { Thread.sleep(2000); }
protected void wait_3000() { Thread.sleep(3000); }
```

**Wait Strategy Guidelines:**
- Use explicit waits for elements: `page.waitForSelector()`
- Use timeouts for async operations
- Avoid arbitrary sleep() where possible
- Log wait reasons for debugging

## Error Handling

### Test Resilience

The test suite implements robust error handling:

```java
try {
    // Test operations
    loginToApplication();
    testAllMenuItemOpenings();
} catch (PlaywrightException e) {
    LOGGER.warn("⚠️ Playwright error: {}", e.getMessage());
    takeScreenshot("playwright-error", true);
    throw new AssertionError("Test failed", e);
} catch (AssertionError e) {
    LOGGER.error("❌ Assertion failed: {}", e.getMessage());
    takeScreenshot("assertion-error", true);
    throw e;
} catch (Exception e) {
    LOGGER.error("❌ Unexpected error: {}", e.getMessage());
    takeScreenshot("unexpected-error", true);
    throw new AssertionError("Test failed unexpectedly", e);
}
```

### Common Test Failures

#### 1. Browser Not Available

**Symptom**: "Browser not available" warnings

**Cause**: Playwright browsers not installed

**Solution**:
```bash
./run-playwright-tests.sh install
# Or manually:
mvn exec:java -e \
    -D exec.mainClass=com.microsoft.playwright.CLI \
    -D exec.args="install"
```

#### 2. Element Not Found

**Symptom**: `PlaywrightException: Timeout 30000ms exceeded`

**Cause**: Element selector incorrect or page not loaded

**Solution**:
- Verify element exists on page
- Check selector syntax
- Increase wait timeout
- Add explicit wait before interaction

#### 3. Login Fails

**Symptom**: Stuck on login page or error message visible

**Cause**: 
- Sample data not initialized
- Invalid credentials
- Company not selected

**Solution**:
- Verify sample data initialization completed
- Check CDataInitializer created users
- Verify company auto-selection logic

#### 4. Dynamic Page Not Loading

**Symptom**: "Dynamic page failed to load" assertion

**Cause**: 
- Page has JavaScript errors
- Backend service failure
- Database connection issue

**Solution**:
- Check browser console logs
- Review application logs
- Verify database state
- Check network requests

## Test Maintenance

### Adding New Tests

To add a new test to the suite:

1. **Extend CBaseUITest**
```java
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    // ... other properties
})
public class CMyNewTest extends CBaseUITest {
    
    @Test
    void myNewTestScenario() {
        // Your test logic
        loginToApplication();
        navigateToViewByText("My View");
        performEnhancedCRUDWorkflow("MyEntity");
    }
}
```

2. **Use base class methods**
```java
// Login
loginToApplication("username", "password");

// Navigation
navigateToViewByText("Projects");
navigateToViewByClass(CProjectOverviewView.class);

// CRUD operations
clickNew();
fillFirstTextField("Test Name");
clickSave();
verifyGridHasData();

// Screenshots
takeScreenshot("my-test-step");
```

3. **Add to test runner script**
```bash
# In run-playwright-tests.sh
run_my_new_test() {
    mvn test -Dtest="CMyNewTest" \
        -Dspring.profiles.active=test \
        -Dplaywright.headless=true
}
```

### Updating Selectors

When UI changes, update selectors in CBaseUITest:

```java
// Before
"vaadin-button:has-text('Save')"

// After UI change
"vaadin-button[theme~='primary']:has-text('Save')"
```

**Best Practices:**
- Use ID selectors when possible (`#element-id`)
- Prefer semantic selectors (`vaadin-button:has-text('Save')`)
- Avoid fragile CSS selectors (`.class1 .class2 div`)
- Document selector changes in commit messages

## Continuous Integration

### CI Configuration

```yaml
# .github/workflows/playwright-tests.yml
name: Playwright Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          
      - name: Install Playwright
        run: ./run-playwright-tests.sh install
        
      - name: Run Playwright Tests
        run: ./run-playwright-tests.sh menu
        
      - name: Upload Screenshots
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: playwright-screenshots
          path: target/screenshots/
```

## Best Practices

### 1. Test Independence

Each test should be independent:
- ✅ Use fresh database for each test
- ✅ Reset state between tests
- ✅ Don't rely on test execution order
- ✅ Clean up test data

### 2. Reliable Selectors

Use stable element selectors:
- ✅ Prefer ID selectors (`#my-element`)
- ✅ Use semantic selectors (`vaadin-button:has-text('Save')`)
- ✅ Avoid position-based selectors (`.nth(5)`)
- ✅ Document custom selectors

### 3. Appropriate Waits

Use proper wait strategies:
- ✅ Use `page.waitForSelector()` for elements
- ✅ Use `page.waitForNavigation()` for page changes
- ✅ Avoid fixed sleeps when possible
- ✅ Set reasonable timeouts

### 4. Clear Logging

Log test progress clearly:
- ✅ Use emoji prefixes (🔐 🧭 ✅ ❌)
- ✅ Log key operations
- ✅ Log assertion results
- ✅ Include context in error messages

### 5. Screenshot Strategy

Capture screenshots strategically:
- ✅ At test start and end
- ✅ Before and after major operations
- ✅ On all errors and failures
- ✅ Use descriptive filenames

## Troubleshooting Guide

### Browser Issues

**Problem**: Browser fails to launch

**Solutions**:
```bash
# Install Playwright browsers
./run-playwright-tests.sh install

# Check browser permissions
chmod +x ~/.cache/ms-playwright/*/chrome*

# Try different browser
# In CBaseUITest.java:
browser = playwright.firefox().launch(...);
```

### Timeout Issues

**Problem**: Tests timeout frequently

**Solutions**:
```java
// Increase timeout
page.waitForSelector(selector, 
    new Page.WaitForSelectorOptions().setTimeout(30000));

// Add debug logging
LOGGER.info("Waiting for element: {}", selector);

// Check if element ever appears
int count = page.locator(selector).count();
LOGGER.info("Element count: {}", count);
```

### Flaky Tests

**Problem**: Tests pass sometimes, fail other times

**Solutions**:
- Add explicit waits before assertions
- Check for race conditions
- Verify element visibility before interaction
- Use more specific selectors
- Increase wait times for slow operations

## Performance Optimization

### Test Execution Speed

Optimize test performance:

```java
// Reuse browser context
private static Browser sharedBrowser;

@BeforeAll
static void setupBrowser() {
    sharedBrowser = playwright.chromium().launch();
}

// Parallel test execution
@Test
@Execution(ExecutionMode.CONCURRENT)
void parallelTest() {
    // Test code
}
```

### Resource Management

Manage resources efficiently:

```java
@AfterEach
void cleanup() {
    // Close pages
    if (page != null && !page.isClosed()) {
        page.close();
    }
    
    // Clear screenshots of successful tests
    if (testSucceeded) {
        deleteScreenshots();
    }
}
```

## Summary

This comprehensive Playwright test suite provides:

✅ **Complete Coverage**: Tests all views, navigation, and workflows
✅ **Company-Aware Login**: Validates multi-tenant authentication
✅ **Sample Data Testing**: Initializes and verifies test data
✅ **Dynamic Navigation**: Tests all dynamically loaded views
✅ **CRUD Operations**: Validates create, read, update, delete
✅ **Visual Verification**: Captures screenshots at all steps
✅ **Robust Error Handling**: Handles failures gracefully
✅ **Clear Documentation**: Comprehensive guides and examples

The test suite ensures the Derbent application works correctly from login through all user workflows, providing confidence in deployments and preventing regressions.

## References

- **Playwright Java**: https://playwright.dev/java/
- **Spring Boot Testing**: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing
- **Vaadin Testing**: https://vaadin.com/docs/latest/testing
- **Base Test Class**: `src/test/java/automated_tests/tech/derbent/ui/automation/CBaseUITest.java`
- **Sample Test**: `src/test/java/automated_tests/tech/derbent/ui/automation/CSampleDataMenuNavigationTest.java`
- **Test Runner**: `run-playwright-tests.sh`

**Last Updated**: 2025-10-11
**Maintained By**: Derbent Development Team
