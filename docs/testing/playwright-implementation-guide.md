# Playwright UI Testing Implementation Guide

## Overview

Playwright is a modern browser automation framework that provides fast, reliable, and capable testing for web applications. This guide covers the complete implementation of Playwright testing for the Derbent application.

## Why Playwright?

### Advantages

1. **Speed**: Significantly faster than traditional tools
2. **Reliability**: Built-in waiting strategies reduce flaky tests  
3. **Modern Architecture**: Async/await API design
4. **Cross-Browser**: Chrome, Firefox, Safari, Edge support
5. **Mobile Testing**: Built-in mobile device emulation
6. **Network Interception**: Advanced debugging capabilities
7. **Auto-Screenshots**: Automatic failure screenshots
8. **Trace Viewer**: Excellent debugging tools

### Comparison with Other Tools

| Feature | Playwright | Selenium | TestBench |
|---------|------------|----------|-----------|
| Speed | ⚡⚡⚡ | ⚡⚡ | ⚡ |
| Reliability | ⚡⚡⚡ | ⚡⚡ | ⚡⚡ |
| Setup Complexity | ⚡⚡⚡ | ⚡⚡ | ⚡ |
| Browser Support | ⚡⚡⚡ | ⚡⚡⚡ | ⚡⚡ |
| Mobile Testing | ⚡⚡⚡ | ⚡ | ⚡ |
| Debugging Tools | ⚡⚡⚡ | ⚡⚡ | ⚡⚡ |

## Installation and Setup

### Maven Dependencies

The project includes the necessary Playwright dependency:

```xml
<dependency>
    <groupId>com.microsoft.playwright</groupId>
    <artifactId>playwright</artifactId>
    <version>1.40.0</version>
    <scope>test</scope>
</dependency>
```

### Browser Installation

```bash
# Install Playwright browsers
./run-playwright-tests.sh install

# Or manually
mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
```

## Test Implementation Structure

### Base Test Setup

```java
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa",
	"spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver",
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080" }
)
public class PlaywrightUIAutomationTest {
    
    @LocalServerPort
    private int port;
    
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    
    @BeforeEach
    void setUp() {
        // Initialize Playwright components
        playwright = Playwright.create();
        browser = playwright.chromium().launch(options);
        context = browser.newContext(contextOptions);
        page = context.newPage();
    }
    
    @AfterEach
    void tearDown() {
        // Cleanup resources
        if (page != null) page.close();
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}
```

### Browser Configuration

```java
// Launch options
BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
    .setHeadless(true)           // Run without GUI
    .setSlowMo(100);             // Slow down for visibility

// Context options  
Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
    .setViewportSize(1200, 800)  // Set viewport size
    .setRecordVideo(recordVideoOptions); // Optional video recording
```

## Core Testing Patterns

> **⚠️ Important Update**: The login screen has been updated from `LoginOverlay` to `CCustomLoginView`. All login-related tests have been updated to use the new selectors and approach.

### Login Screen Architecture

The application now uses a custom login view (`CCustomLoginView`) instead of Vaadin's `LoginOverlay`:

- **Route**: `/login` (mapped to `CCustomLoginView`)
- **Alternative Route**: `/overlay-login` (mapped to `CLoginView` for testing)
- **Security Configuration**: Uses `CCustomLoginView` as default login view
- **Element IDs**: 
  - Username: `#custom-username-input`
  - Password: `#custom-password-input`
  - Submit Button: `#custom-submit-button`
  - Container: `.custom-login-view`

### Login Testing Pattern

```java
@Test
void testLoginFunctionality() {
    // Navigate to application
    page.navigate(baseUrl);
    
    // Wait for custom login view (updated for new login screen)
    page.waitForSelector(".custom-login-view");
    
    // Perform login using specific element IDs
    page.locator("#custom-username-input").fill("admin");
    page.locator("#custom-password-input").fill("test123");
    page.locator("#custom-submit-button").click();
    
    // Verify successful login
    page.waitForSelector("vaadin-app-layout");
    assertTrue(page.locator("vaadin-app-layout").isVisible());
}
```

### Navigation Testing Pattern

```java
@Test
void testNavigationBetweenViews() {
    loginToApplication();
    
    String[] views = {"Projects", "Meetings", "Activities"};
    
    for (String viewName : views) {
        // Navigate to view
        if (navigateToViewByText(viewName)) {
            takeScreenshot(viewName.toLowerCase() + "-view");
            // Verify navigation successful
            assertTrue(page.url().contains(viewName.toLowerCase()) || 
                      page.textContent("body").contains(viewName));
        }
    }
}
```

### CRUD Operations Pattern

```java
@Test
void testCRUDOperationsInProjects() {
    loginToApplication();
    navigateToViewByText("Projects");
    
    // CREATE
    page.locator("vaadin-button:has-text('New')").click();
    page.locator("vaadin-text-field").first().fill("Test Project");
    page.locator("vaadin-button:has-text('Save')").click();
    
    // READ
    var grid = page.locator("vaadin-grid").first();
    var cells = grid.locator("vaadin-grid-cell-content");
    assertTrue(cells.count() > 0);
    
    // UPDATE
    cells.first().click();
    page.locator("vaadin-button:has-text('Edit')").click();
    page.locator("vaadin-text-field").first().fill("Updated Project");
    page.locator("vaadin-button:has-text('Save')").click();
    
    // DELETE (with confirmation)
    page.locator("vaadin-button:has-text('Delete')").click();
    page.locator("vaadin-button:has-text('Confirm')").click();
}
```

### Grid Interaction Pattern

```java
@Test
void testGridInteractions() {
    loginToApplication();
    navigateToViewByText("Projects");
    
    var grid = page.locator("vaadin-grid").first();
    
    // Test sorting
    var sorters = grid.locator("vaadin-grid-sorter");
    if (sorters.count() > 0) {
        sorters.first().click(); // Sort ascending
        page.waitForTimeout(500);
        sorters.first().click(); // Sort descending
    }
    
    // Test filtering
    var filters = grid.locator("vaadin-text-field[slot='filter']");
    if (filters.count() > 0) {
        filters.first().fill("test");
        page.waitForTimeout(1000);
        filters.first().fill(""); // Clear filter
    }
    
    // Test row selection
    var cells = grid.locator("vaadin-grid-cell-content");
    if (cells.count() > 0) {
        cells.first().click();
        takeScreenshot("grid-row-selected");
    }
}
```

## Advanced Testing Features

### Responsive Design Testing

```java
@Test
void testResponsiveDesign() {
    int[][] viewports = {
        {1920, 1080}, // Desktop
        {768, 1024},  // Tablet
        {375, 667}    // Mobile
    };
    
    for (int[] viewport : viewports) {
        page.setViewportSize(viewport[0], viewport[1]);
        page.navigate(baseUrl);
        loginToApplication();
        
        // Verify layout adapts
        assertTrue(page.locator("vaadin-app-layout").isVisible());
        takeScreenshot("responsive-" + viewport[0] + "x" + viewport[1]);
    }
}
```

### Form Validation Testing

```java
@Test
void testFormValidation() {
    loginToApplication();
    navigateToViewByText("Projects");
    
    // Open new form
    page.locator("vaadin-button:has-text('New')").click();
    
    // Try to save without filling required fields
    page.locator("vaadin-button:has-text('Save')").click();
    
    // Check for validation messages
    var errorMessages = page.locator("vaadin-text-field[invalid]");
    assertTrue(errorMessages.count() > 0, "Validation messages should appear");
    
    takeScreenshot("form-validation-errors");
}
```

### Accessibility Testing

```java
@Test
void testAccessibilityBasics() {
    loginToApplication();
    
    // Check for semantic elements
    assertTrue(page.locator("main").count() > 0, "Main content area should exist");
    assertTrue(page.locator("nav").count() > 0, "Navigation should exist");
    
    // Check heading hierarchy
    var headings = page.locator("h1, h2, h3, h4, h5, h6");
    assertTrue(headings.count() > 0, "Headings should be present");
    
    // Check for ARIA labels
    var ariaElements = page.locator("[aria-label], [aria-labelledby], [role]");
    assertTrue(ariaElements.count() > 0, "ARIA attributes should be present");
}
```

## Helper Methods and Utilities

### Centralized Login Helper

```java
private void loginToApplication() {
    page.navigate(baseUrl);
    // Wait for custom login view (updated for new login screen)
    page.waitForSelector(".custom-login-view");
    performLogin("admin", "test123");
    page.waitForSelector("vaadin-app-layout");
}

private void performLogin(String username, String password) {
    // Updated selectors for CCustomLoginView
    page.locator("#custom-username-input").fill(username);
    page.locator("#custom-password-input").fill(password);
    page.locator("#custom-submit-button").click();
    page.waitForTimeout(1000);
}
```

### Navigation Helper

```java
private boolean navigateToViewByText(String viewName) {
    try {
        // Try side navigation first
        var navItems = page.locator("vaadin-side-nav-item");
        for (int i = 0; i < navItems.count(); i++) {
            if (navItems.nth(i).textContent().contains(viewName)) {
                navItems.nth(i).click();
                page.waitForTimeout(1000);
                return true;
            }
        }
        
        // Try regular links
        var links = page.locator("a");
        for (int i = 0; i < links.count(); i++) {
            if (links.nth(i).textContent().contains(viewName)) {
                links.nth(i).click();
                page.waitForTimeout(1000);
                return true;
            }
        }
        
        return false;
    } catch (Exception e) {
        logger.warn("Navigation failed: {}", e.getMessage());
        return false;
    }
}
```

### Screenshot Helper

```java
private void takeScreenshot(String name) {
    try {
        String screenshotPath = "target/screenshots/playwright-" + name + "-" + 
                               System.currentTimeMillis() + ".png";
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(screenshotPath)));
        logger.info("📸 Screenshot saved: {}", screenshotPath);
    } catch (Exception e) {
        logger.warn("⚠️ Failed to take screenshot '{}': {}", name, e.getMessage());
    }
}
```

## Best Practices

### Waiting Strategies

```java
// ✅ Good - Wait for specific element
page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(10000));

// ✅ Good - Wait for network idle
page.waitForLoadState(LoadState.NETWORKIDLE);

// ❌ Avoid - Fixed timeouts
page.waitForTimeout(5000); // Use sparingly
```

### Element Selection

```java
// ✅ Good - Specific selectors
page.locator("vaadin-button:has-text('Save')");
page.locator("vaadin-text-field[label='Name']");

// ✅ Good - Data attributes
page.locator("[data-testid='submit-button']");

// ❌ Avoid - Fragile selectors
page.locator("div.v-button.v-widget.v-has-width");
```

### Error Handling

```java
@Test
void testWithErrorHandling() {
    try {
        loginToApplication();
        // Test operations
        
    } catch (Exception e) {
        logger.error("Test failed: {}", e.getMessage());
        takeScreenshot("test-failure");
        throw e; // Re-throw to fail the test
    }
}
```

## Debugging and Troubleshooting

### Debug Mode

```java
// Run with headed browser for debugging
browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
    .setHeadless(false)
    .setSlowMo(1000)); // 1 second delay between actions
```

### Console Logging

```java
// Enable console message logging
page.onConsoleMessage(msg -> 
    logger.info("Browser console: {}", msg.text()));
```

### Network Monitoring

```java
// Monitor network requests
page.onRequest(request -> 
    logger.info("Request: {} {}", request.method(), request.url()));

page.onResponse(response -> 
    logger.info("Response: {} {}", response.status(), response.url()));
```

### Trace Recording

```java
// Enable tracing for debugging
context.tracing().start(new Tracing.StartOptions()
    .setScreenshots(true)
    .setSnapshots(true));

// ... run tests ...

// Save trace
context.tracing().stop(new Tracing.StopOptions()
    .setPath(Paths.get("trace.zip")));
```

## Integration with CI/CD

### GitHub Actions Configuration

```yaml
- name: Install Playwright
  run: |
    mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps"

- name: Run Playwright tests
  run: |
    ./run-playwright-tests.sh all
  env:
    PLAYWRIGHT_HEADLESS: true
```

### Jenkins Pipeline

```groovy
stage('Playwright Tests') {
    steps {
        sh 'mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"'
        sh './run-playwright-tests.sh all'
    }
    post {
        always {
            archiveArtifacts artifacts: 'target/screenshots/playwright-*.png'
        }
    }
}
```

## Performance Optimization

### Parallel Execution

```java
// Configure parallel execution in surefire plugin
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>methods</parallel>
        <threadCount>4</threadCount>
    </configuration>
</plugin>
```

### Browser Context Reuse

```java
// Reuse context for related tests
@BeforeAll
static void setupClass() {
    playwright = Playwright.create();
    browser = playwright.chromium().launch();
}

@BeforeEach
void setupTest() {
    context = browser.newContext();
    page = context.newPage();
}
```

### Resource Management

```java
// Ensure proper cleanup
@AfterEach
void cleanup() {
    if (page != null) page.close();
    if (context != null) context.close();
}

@AfterAll
static void cleanupClass() {
    if (browser != null) browser.close();
    if (playwright != null) playwright.close();
}
```

## Common Issues and Solutions

### Browser Installation Issues

```bash
# Problem: Browsers not installed
# Solution: Manual installation
mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
```

### Timeout Issues

```java
// Problem: Elements not found
// Solution: Increase timeout
page.waitForSelector("selector", new Page.WaitForSelectorOptions().setTimeout(30000));
```

### Flaky Tests

```java
// Problem: Intermittent failures
// Solution: Better waiting strategies
page.waitForFunction("() => document.querySelector('vaadin-grid').items.length > 0");
```

### Memory Issues

```bash
# Problem: Out of memory
# Solution: Increase heap size
export MAVEN_OPTS="-Xmx2g"
```

## Conclusion

Playwright provides a modern, efficient approach to UI testing for the Derbent application. Its speed, reliability, and excellent debugging capabilities make it an ideal choice for comprehensive test automation. The implementation follows the project's coding standards and provides a robust foundation for ensuring application quality through automated testing.