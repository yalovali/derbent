# Playwright Testing Best Practices for Derbent

## Overview

This document provides best practices, patterns, and guidelines for writing and maintaining Playwright UI tests in the Derbent application.

## Quick Reference

### Test Execution Commands

```bash
# Run all tests
./run-playwright-tests.sh all

# Run specific test suites
./run-playwright-tests.sh menu           # Menu navigation
./run-playwright-tests.sh login          # Company login pattern
./run-playwright-tests.sh comprehensive  # Dynamic views

# Clean up test artifacts
./run-playwright-tests.sh clean

# Install Playwright browsers
./run-playwright-tests.sh install
```

### Test Files

| Test File | Purpose | Duration |
|-----------|---------|----------|
| `CSampleDataMenuNavigationTest.java` | Complete menu navigation with screenshots | ~40 seconds |
| `CCompanyAwareLoginTest.java` | Company-aware login pattern validation | ~30 seconds |
| `CComprehensiveDynamicViewsTest.java` | Dynamic views, CRUD, grids, forms | ~2-3 minutes |

## Test Architecture

### Base Test Class Pattern

All tests extend `CBaseUITest` which provides 25+ helper methods:

```java
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "server.port=8080"
})
public class MyNewTest extends CBaseUITest {
    @Test
    void myTest() {
        loginToApplication();
        navigateToViewByText("My View");
        performEnhancedCRUDWorkflow("MyEntity");
    }
}
```

**Key Benefits:**
- Consistent test structure
- Reusable helper methods
- Automatic browser lifecycle management
- Built-in screenshot capture
- H2 in-memory database for isolation

## Best Practices

### 1. Test Independence

**✅ DO:**
```java
@Test
void testFeature() {
    // Each test initializes its own state
    loginToApplication();
    initializeSampleData();
    // ... test logic
}
```

**❌ DON'T:**
```java
// Don't rely on test execution order
@Test
@Order(1)
void setupData() { ... }

@Test
@Order(2)
void testFeature() { ... }  // Depends on setupData()
```

### 2. Reliable Selectors

**Selector Priority (Best to Worst):**

1. **ID Selectors** (Most Reliable)
```java
"#custom-username-input"
"#custom-company-input"
```

2. **Semantic Selectors**
```java
"vaadin-button:has-text('Save')"
"vaadin-text-field[label='Name']"
```

3. **Type Selectors**
```java
"vaadin-grid"
"vaadin-form-layout"
```

4. **Position-Based** (Avoid when possible)
```java
"vaadin-button:nth(2)"  // Fragile!
```

**✅ DO:**
```java
// Use specific, stable selectors
page.locator("#custom-username-input").fill("admin");
page.locator("vaadin-button:has-text('Login')").click();
```

**❌ DON'T:**
```java
// Avoid fragile selectors
page.locator("div > div > button:nth(2)").click();  // Too fragile!
```

### 3. Appropriate Waits

**✅ DO:**
```java
// Use explicit waits for elements
page.waitForSelector("vaadin-grid", 
    new Page.WaitForSelectorOptions().setTimeout(10000));

// Use helper wait methods
wait_afterlogin();  // Semantic wait
waitForDynamicPageLoad();  // Custom wait with validation
```

**❌ DON'T:**
```java
// Avoid arbitrary sleeps
Thread.sleep(5000);  // What are we waiting for?
```

**Wait Strategy Guidelines:**
- Use `page.waitForSelector()` when waiting for specific elements
- Use semantic helper methods like `wait_afterlogin()`
- Add waits after navigation or state changes
- Set reasonable timeouts (5-10 seconds for most operations)

### 4. Error Handling

**✅ DO:**
```java
@Test
void testFeature() {
    try {
        loginToApplication();
        performOperations();
        LOGGER.info("✅ Test passed");
    } catch (Exception e) {
        LOGGER.error("❌ Test failed: {}", e.getMessage());
        takeScreenshot("test-error", true);
        throw new AssertionError("Test failed", e);
    }
}
```

**❌ DON'T:**
```java
@Test
void testFeature() {
    loginToApplication();
    // No error handling - failures are hard to debug
    performOperations();
}
```

### 5. Screenshot Strategy

**When to Capture Screenshots:**

1. **Always:**
   - At test start and end
   - On errors or failures
   - After major operations (login, CRUD operations)

2. **Optionally:**
   - During navigation (menu items)
   - At verification points
   - For documentation purposes

**✅ DO:**
```java
takeScreenshot("01-login-page", false);      // Document state
loginToApplication();
takeScreenshot("02-logged-in", false);       // Document result
```

**Screenshot Naming Convention:**
```
{phase}-{description}.png
01-login-page.png
02-logged-in.png
menu-projects-1.png
crud-create-activity.png
error-login-failed.png
```

### 6. Logging

**Use Consistent Logging:**

```java
// Use emoji prefixes for visual scanning
LOGGER.info("🔐 Starting login test...");      // Info
LOGGER.debug("📊 Found {} items", count);       // Debug
LOGGER.warn("⚠️ No data found");                // Warning
LOGGER.error("❌ Test failed: {}", message);    // Error
LOGGER.info("✅ Test completed successfully");  // Success
```

**Emoji Guide:**
- 🔐 Authentication/Login
- 🧭 Navigation
- 📊 Data/Metrics
- ➕ Create operations
- ✏️ Update operations
- 🗑️ Delete operations
- 📸 Screenshots
- ⚠️ Warnings
- ❌ Errors
- ✅ Success

## Common Test Patterns

### Pattern 1: Login and Navigate

```java
@Test
void testViewAccess() {
    // Login
    loginToApplication();
    wait_afterlogin();
    
    // Navigate
    navigateToViewByText("Projects");
    waitForDynamicPageLoad();
    
    // Verify
    verifyGridHasData();
    takeScreenshot("projects-view");
}
```

### Pattern 2: CRUD Testing

```java
@Test
void testEntityCrud() {
    loginToApplication();
    navigateToViewByText("Activities");
    
    // Use enhanced CRUD workflow
    performEnhancedCRUDWorkflow("Activity");
    // Automatically tests Create, Read, Update, Delete
}
```

### Pattern 3: Form Validation

```java
@Test
void testFormValidation() {
    loginToApplication();
    navigateToViewByText("Users");
    
    // Open form
    clickNew();
    wait_1000();
    
    // Test required fields
    Locator requiredFields = page.locator("[required]");
    assertTrue(requiredFields.count() > 0, "Form should have required fields");
    
    // Try to save without filling
    clickSave();
    
    // Verify validation messages appear
    // ...
    
    takeScreenshot("form-validation");
}
```

### Pattern 4: Grid Testing

```java
@Test
void testGridFunctionality() {
    loginToApplication();
    navigateToViewByText("Projects");
    
    // Verify grid exists
    Locator grid = page.locator("vaadin-grid").first();
    assertTrue(grid.count() > 0, "Grid should be present");
    
    // Test sorting
    Locator sorters = grid.locator("vaadin-grid-sorter");
    if (sorters.count() > 0) {
        sorters.first().click();
        wait_1000();
        takeScreenshot("grid-sorted");
    }
    
    // Test row selection
    clickFirstGridRow();
    takeScreenshot("grid-row-selected");
}
```

### Pattern 5: Multi-Step Workflow

```java
@Test
void testCompleteWorkflow() {
    LOGGER.info("🚀 Starting complete workflow test");
    
    try {
        // Phase 1: Setup
        LOGGER.info("📋 Phase 1: Setup");
        loginToApplication();
        testDatabaseInitialization();
        
        // Phase 2: Create
        LOGGER.info("📋 Phase 2: Create data");
        createTestProject();
        
        // Phase 3: Verify
        LOGGER.info("📋 Phase 3: Verification");
        verifyProjectExists();
        
        LOGGER.info("✅ Workflow test completed");
    } catch (Exception e) {
        LOGGER.error("❌ Workflow test failed", e);
        takeScreenshot("workflow-error", true);
        throw new AssertionError("Workflow test failed", e);
    }
}
```

## Testing Checklist

Before submitting tests, verify:

### Code Quality
- [ ] Test extends `CBaseUITest`
- [ ] Uses appropriate Spring Boot test annotations
- [ ] Has descriptive `@DisplayName` annotations
- [ ] Includes proper error handling
- [ ] Uses helper methods from base class
- [ ] Has consistent logging with emojis
- [ ] Captures screenshots at key points

### Selector Robustness
- [ ] Uses ID selectors where available
- [ ] Uses semantic selectors (text-based)
- [ ] Avoids position-based selectors
- [ ] Waits for elements before interaction
- [ ] Handles dynamic content properly

### Test Independence
- [ ] Test doesn't depend on other tests
- [ ] Initializes its own test data
- [ ] Cleans up after itself
- [ ] Works in any execution order
- [ ] Uses H2 in-memory database

### Documentation
- [ ] Test purpose is clear from name and description
- [ ] Comments explain complex logic
- [ ] Screenshots are named descriptively
- [ ] Logging messages are informative

## Debugging Failed Tests

### Step 1: Check Screenshots

```bash
# View captured screenshots
ls -lt target/screenshots/

# Look for error screenshots
ls target/screenshots/*error*.png
```

### Step 2: Review Logs

```bash
# Filter test output for errors
mvn test -Dtest=MyTest 2>&1 | grep "ERROR\|FAIL\|❌"

# Look for specific patterns
mvn test -Dtest=MyTest 2>&1 | grep "⚠️"  # Warnings
```

### Step 3: Run in Visible Mode

```bash
# Run with visible browser
mvn test -Dtest=MyTest \
    -Dspring.profiles.active=test \
    -Dplaywright.headless=false
```

### Step 4: Add Debug Logging

```java
// Add temporary debug logging
LOGGER.info("🔍 Current URL: {}", page.url());
LOGGER.info("🔍 Page title: {}", page.title());
LOGGER.info("🔍 Element count: {}", page.locator(selector).count());
takeScreenshot("debug-step-" + stepNumber);
```

### Step 5: Verify Element Existence

```java
// Check if element exists before interaction
if (page.locator(selector).count() == 0) {
    LOGGER.error("❌ Element not found: {}", selector);
    takeScreenshot("element-not-found");
    throw new AssertionError("Element not found: " + selector);
}
```

## Performance Optimization

### 1. Reduce Wait Times

```java
// Instead of fixed waits
wait_2000();

// Use conditional waits
page.waitForSelector(selector, 
    new Page.WaitForSelectorOptions().setTimeout(2000));
```

### 2. Parallel Test Execution

```java
// Enable parallel execution (use with caution)
@Execution(ExecutionMode.CONCURRENT)
public class MyTest extends CBaseUITest {
    // Tests can run in parallel
}
```

### 3. Reuse Browser Context

```java
// For multiple tests in same class
private static Browser sharedBrowser;

@BeforeAll
static void setupBrowser() {
    // Share browser across tests
    sharedBrowser = playwright.chromium().launch();
}

@BeforeEach
void setupContext() {
    // But use fresh context per test
    context = sharedBrowser.newContext();
    page = context.newPage();
}
```

### 4. Skip Unnecessary Screenshots

```java
// Don't capture screenshots for every navigation step
visitMenuItems(false, false, "prime");  // No screenshots

// Only capture for final verification
takeScreenshot("final-state");
```

## CI/CD Integration

### GitHub Actions Example

```yaml
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
          
      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
          
      - name: Install Playwright
        run: ./run-playwright-tests.sh install
        
      - name: Run Tests
        run: ./run-playwright-tests.sh all
        
      - name: Upload Screenshots
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: test-screenshots
          path: target/screenshots/
```

## Troubleshooting Guide

### Problem: Browser Installation Fails

**Solution:**
```bash
# Manual installation
mvn exec:java -e \
    -D exec.mainClass=com.microsoft.playwright.CLI \
    -D exec.args="install chromium"

# Or install all browsers
./run-playwright-tests.sh install
```

### Problem: Tests Timeout

**Symptoms:**
- Tests hang indefinitely
- "Timeout exceeded" errors

**Solutions:**
```java
// Increase timeout for slow operations
page.waitForSelector(selector, 
    new Page.WaitForSelectorOptions().setTimeout(30000));

// Check for infinite waits
// Replace: while(true) { wait(); }
// With: for(int i=0; i<maxAttempts; i++) { wait(); }
```

### Problem: Flaky Tests

**Symptoms:**
- Tests pass sometimes, fail other times
- Inconsistent behavior

**Solutions:**
1. Add explicit waits before assertions
2. Verify element visibility before interaction
3. Use more specific selectors
4. Check for race conditions
5. Increase wait times for slow operations

### Problem: Element Not Found

**Symptoms:**
- `PlaywrightException: Timeout waiting for selector`

**Solutions:**
```java
// Debug: Check if element exists
LOGGER.info("Element count: {}", page.locator(selector).count());

// Debug: Try alternative selectors
String[] selectors = {"#id", ".class", "vaadin-component"};
for (String sel : selectors) {
    LOGGER.info("Selector '{}': {} found", sel, page.locator(sel).count());
}

// Fix: Use correct selector or wait for page load
waitForDynamicPageLoad();
page.waitForSelector(selector);
```

## Summary

### Key Takeaways

1. **Always extend `CBaseUITest`** - Provides essential test infrastructure
2. **Use reliable selectors** - Prefer IDs and semantic selectors
3. **Wait appropriately** - Use explicit waits, avoid arbitrary sleeps
4. **Handle errors gracefully** - Capture screenshots, log details
5. **Keep tests independent** - Each test should work in isolation
6. **Document with screenshots** - Visual verification is valuable
7. **Log consistently** - Use emojis for visual scanning
8. **Test real workflows** - Simulate actual user journeys

### Testing Philosophy

> **"Tests should be deterministic, maintainable, and provide confidence in deployments."**

- Tests validate functionality, not implementation
- Tests should be readable like documentation
- Tests should fail for the right reasons
- Tests should be easy to debug

## References

- **Base Test Class**: `src/test/java/automated_tests/tech/derbent/ui/automation/CBaseUITest.java`
- **Example Tests**: 
  - `CSampleDataMenuNavigationTest.java`
  - `CCompanyAwareLoginTest.java`
  - `CComprehensiveDynamicViewsTest.java`
- **Test Runner**: `run-playwright-tests.sh`
- **Playwright Docs**: https://playwright.dev/java/
- **Complete Test Guide**: `docs/implementation/PLAYWRIGHT_TEST_GUIDE.md`

---

**Last Updated**: 2025-10-11  
**Maintained By**: Derbent Development Team
