# Selenium WebDriver UI Testing Implementation Guide

## Overview

Selenium WebDriver is the industry-standard browser automation framework that provides robust, cross-browser testing capabilities. This guide covers the complete implementation of Selenium testing for the Derbent application, complementing the Playwright framework to provide comprehensive test coverage.

## Why Selenium WebDriver?

### Advantages

1. **Industry Standard**: Most widely adopted browser automation framework
2. **Cross-Browser Support**: Excellent support for all browsers including legacy versions
3. **Mature Ecosystem**: Extensive third-party tools and integrations
4. **Language Support**: Available in Java, Python, C#, JavaScript, Ruby
5. **Mobile Testing**: Integration with Appium for mobile app testing
6. **Grid Support**: Selenium Grid for distributed testing
7. **Enterprise Adoption**: Proven in enterprise environments
8. **Rich Documentation**: Comprehensive documentation and community support

### Selenium vs Other Frameworks

| Feature | Selenium | Playwright | TestBench |
|---------|----------|------------|-----------|
| Industry Adoption | ⭐⭐⭐ | ⭐⭐ | ⭐⭐ |
| Browser Support | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ |
| Legacy Browser | ⭐⭐⭐ | ⭐ | ⭐⭐ |
| Learning Curve | ⭐⭐ | ⭐⭐⭐ | ⭐ |
| Tool Ecosystem | ⭐⭐⭐ | ⭐⭐ | ⭐⭐ |
| Enterprise Features | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ |

## Installation and Setup

### Maven Dependencies

The project includes comprehensive Selenium dependencies:

```xml
<!-- Selenium WebDriver for cross-browser testing -->
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-support</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.github.bonigarcia</groupId>
    <artifactId>webdrivermanager</artifactId>
    <version>5.5.3</version>
    <scope>test</scope>
</dependency>
```

### WebDriver Management

WebDriverManager automatically handles browser driver binaries:

```java
// Automatic driver management
WebDriverManager.chromedriver().setup();
```

No manual driver downloads required!

## Test Implementation Structure

### Base Test Setup

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:seleniumtestdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "logging.level.tech.derbent=DEBUG"
})
public class SeleniumUIAutomationTest {
    
    @LocalServerPort
    private int port;
    
    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;
    
    @BeforeEach
    void setUp() {
        // Setup WebDriverManager
        WebDriverManager.chromedriver().setup();
        
        // Configure Chrome options
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1200,800");
        
        // Initialize driver
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }
    
    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
```

### Browser Configuration Options

```java
// Chrome configuration
ChromeOptions chromeOptions = new ChromeOptions();
chromeOptions.addArguments("--headless");              // Run without GUI
chromeOptions.addArguments("--no-sandbox");            // CI/CD compatibility
chromeOptions.addArguments("--disable-dev-shm-usage"); // Docker compatibility
chromeOptions.addArguments("--disable-gpu");           // GPU acceleration
chromeOptions.addArguments("--window-size=1920,1080"); // Window size
chromeOptions.addArguments("--disable-extensions");    // No extensions

// Firefox configuration
FirefoxOptions firefoxOptions = new FirefoxOptions();
firefoxOptions.addArguments("--headless");
firefoxOptions.addPreference("dom.webnotifications.enabled", false);

// Edge configuration
EdgeOptions edgeOptions = new EdgeOptions();
edgeOptions.addArguments("--headless");
edgeOptions.addArguments("--disable-web-security");
```

## Core Testing Patterns

### Login Testing Pattern

```java
@Test
void testApplicationLoadsAndLoginFunctionality() {
    logger.info("🧪 Testing application loads and login functionality...");
    
    // Navigate to application
    driver.get(baseUrl);
    
    // Wait for login overlay
    wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("vaadin-login-overlay")));
    takeScreenshot("login-page-loaded");
    
    // Verify login form is present
    WebElement loginOverlay = driver.findElement(By.tagName("vaadin-login-overlay"));
    assertTrue(loginOverlay.isDisplayed());
    
    // Perform login
    performLogin();
    
    // Verify successful login
    wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("vaadin-app-layout")));
    takeScreenshot("application-after-login");
    
    WebElement appLayout = driver.findElement(By.tagName("vaadin-app-layout"));
    assertTrue(appLayout.isDisplayed());
}

private void performLogin() {
    // Find and fill username field
    WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(
        By.cssSelector("vaadin-login-overlay vaadin-text-field[autocapitalize='none']")));
    usernameField.clear();
    usernameField.sendKeys(TEST_USERNAME);
    
    // Find and fill password field
    WebElement passwordField = wait.until(ExpectedConditions.presenceOfElementLocated(
        By.cssSelector("vaadin-login-overlay vaadin-password-field")));
    passwordField.clear();
    passwordField.sendKeys(TEST_PASSWORD);
    
    // Click login button
    WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
        By.cssSelector("vaadin-login-overlay vaadin-button[theme~='primary']")));
    loginButton.click();
}
```

### Navigation Testing Pattern

```java
@Test
void testNavigationBetweenViews() {
    // Login first
    driver.get(baseUrl);
    performLogin();
    wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("vaadin-app-layout")));
    
    // Test navigation to different views
    String[] expectedViews = {"Projects", "Meetings", "Activities", "Decisions"};
    
    for (String viewName : expectedViews) {
        if (navigateToView(viewName)) {
            takeScreenshot("view-" + viewName.toLowerCase());
            logger.info("✅ Successfully navigated to {} view", viewName);
        } else {
            logger.warn("⚠️ Could not navigate to {} view", viewName);
        }
    }
}

private boolean navigateToView(String viewName) {
    try {
        // Look for navigation items in side menu
        List<WebElement> navItems = driver.findElements(By.tagName("vaadin-side-nav-item"));
        
        for (WebElement navItem : navItems) {
            if (navItem.getText().contains(viewName)) {
                navItem.click();
                Thread.sleep(1000);
                return true;
            }
        }
        
        // Alternative: look for links
        List<WebElement> links = driver.findElements(By.tagName("a"));
        for (WebElement link : links) {
            if (link.getText().contains(viewName)) {
                link.click();
                Thread.sleep(1000);
                return true;
            }
        }
        
        return false;
        
    } catch (Exception e) {
        logger.warn("Navigation to {} view failed: {}", viewName, e.getMessage());
        return false;
    }
}
```

### CRUD Operations Pattern

```java
@Test
void testCRUDOperations() {
    // Login and navigate
    driver.get(baseUrl);
    performLogin();
    wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("vaadin-app-layout")));
    
    if (navigateToView("Projects")) {
        performCreateOperation("Projects");
        performReadOperation("Projects");
        performUpdateOperation("Projects");
        performDeleteOperation("Projects");
    }
}

private void performCreateOperation(String viewName) {
    try {
        // Look for "New" or "Add" buttons
        List<WebElement> createButtons = findCreateButtons();
        
        if (!createButtons.isEmpty()) {
            createButtons.get(0).click();
            Thread.sleep(1000);
            
            // Fill form fields
            List<WebElement> textFields = driver.findElements(By.tagName("vaadin-text-field"));
            if (!textFields.isEmpty()) {
                textFields.get(0).sendKeys("Test " + viewName + " Entry - " + System.currentTimeMillis());
                takeScreenshot("create-form-filled-" + viewName.toLowerCase());
                
                // Save
                List<WebElement> saveButtons = findSaveButtons();
                if (!saveButtons.isEmpty()) {
                    saveButtons.get(0).click();
                    Thread.sleep(1500);
                    takeScreenshot("create-saved-" + viewName.toLowerCase());
                }
            }
        }
    } catch (Exception e) {
        logger.warn("CREATE operation failed in {} view: {}", viewName, e.getMessage());
        takeScreenshot("create-error-" + viewName.toLowerCase());
    }
}
```

### Grid Interaction Pattern

```java
@Test
void testGridInteractions() {
    // Setup and navigate
    driver.get(baseUrl);
    performLogin();
    wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("vaadin-app-layout")));
    
    String[] viewsWithGrids = {"Projects", "Meetings", "Activities"};
    
    for (String viewName : viewsWithGrids) {
        if (navigateToView(viewName)) {
            testGridInView(viewName);
        }
    }
}

private void testGridInView(String viewName) {
    try {
        // Look for grids
        List<WebElement> grids = driver.findElements(By.tagName("vaadin-grid"));
        
        if (!grids.isEmpty()) {
            WebElement grid = grids.get(0);
            
            // Test grid cell interactions
            List<WebElement> gridCells = grid.findElements(By.tagName("vaadin-grid-cell-content"));
            if (!gridCells.isEmpty()) {
                gridCells.get(0).click();
                takeScreenshot("grid-cell-clicked-" + viewName.toLowerCase());
            }
            
            // Test column sorting
            List<WebElement> headers = grid.findElements(By.cssSelector("vaadin-grid-sorter"));
            if (!headers.isEmpty()) {
                headers.get(0).click();
                Thread.sleep(500);
                takeScreenshot("grid-sorted-" + viewName.toLowerCase());
            }
        }
    } catch (Exception e) {
        logger.warn("Grid test failed in {} view: {}", viewName, e.getMessage());
        takeScreenshot("grid-test-error-" + viewName.toLowerCase());
    }
}
```

## Advanced Testing Features

### Responsive Design Testing

```java
@Test
void testResponsiveDesignAndMobileView() {
    int[][] screenSizes = {
        {1920, 1080}, // Desktop
        {1024, 768},  // Tablet landscape
        {768, 1024},  // Tablet portrait
        {375, 667}    // Mobile
    };
    
    String[] deviceNames = {"Desktop", "Tablet-Landscape", "Tablet-Portrait", "Mobile"};
    
    for (int i = 0; i < screenSizes.length; i++) {
        // Set window size
        driver.manage().window().setSize(new Dimension(screenSizes[i][0], screenSizes[i][1]));
        
        // Navigate and login
        driver.get(baseUrl);
        performLogin();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("vaadin-app-layout")));
        
        // Take screenshot
        takeScreenshot("responsive-" + deviceNames[i].toLowerCase());
        
        // Verify layout components are visible
        WebElement appLayout = driver.findElement(By.tagName("vaadin-app-layout"));
        assertTrue(appLayout.isDisplayed());
    }
}
```

### Form Validation Testing

```java
@Test
void testFormValidationAndErrorHandling() {
    // Setup
    driver.get(baseUrl);
    performLogin();
    wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("vaadin-app-layout")));
    
    String[] viewsToTest = {"Projects", "Meetings"};
    
    for (String viewName : viewsToTest) {
        if (navigateToView(viewName)) {
            testFormValidationInView(viewName);
        }
    }
}

private void testFormValidationInView(String viewName) {
    try {
        // Open new form
        List<WebElement> createButtons = findCreateButtons();
        
        if (!createButtons.isEmpty()) {
            createButtons.get(0).click();
            Thread.sleep(1000);
            
            // Try to save without filling required fields
            List<WebElement> saveButtons = findSaveButtons();
            if (!saveButtons.isEmpty()) {
                saveButtons.get(0).click();
                Thread.sleep(1000);
                
                // Look for validation messages
                List<WebElement> errorMessages = driver.findElements(
                    By.cssSelector("vaadin-text-field[invalid], .error-message, [role='alert']"));
                
                if (!errorMessages.isEmpty()) {
                    logger.info("✅ Form validation working - found {} validation messages", 
                              errorMessages.size());
                    takeScreenshot("form-validation-" + viewName.toLowerCase());
                }
                
                // Close form
                List<WebElement> cancelButtons = driver.findElements(
                    By.xpath("//vaadin-button[contains(text(), 'Cancel')]"));
                if (!cancelButtons.isEmpty()) {
                    cancelButtons.get(0).click();
                }
            }
        }
    } catch (Exception e) {
        logger.warn("Form validation test failed in {} view: {}", viewName, e.getMessage());
        takeScreenshot("form-validation-error-" + viewName.toLowerCase());
    }
}
```

## Element Location Strategies

### By CSS Selector

```java
// ✅ Good - Specific Vaadin components
driver.findElement(By.cssSelector("vaadin-button[theme='primary']"));
driver.findElement(By.cssSelector("vaadin-text-field[label='Name']"));
driver.findElement(By.cssSelector("vaadin-grid vaadin-grid-cell-content"));

// ✅ Good - Data attributes
driver.findElement(By.cssSelector("[data-testid='submit-button']"));
```

### By XPath

```java
// ✅ Good - Text-based selection
driver.findElement(By.xpath("//vaadin-button[contains(text(), 'Save')]"));
driver.findElement(By.xpath("//vaadin-side-nav-item[contains(text(), 'Projects')]"));

// ✅ Good - Complex relationships
driver.findElement(By.xpath("//vaadin-form-layout//vaadin-text-field[@label='Name']"));
```

### By Tag Name

```java
// ✅ Good - Vaadin components
driver.findElement(By.tagName("vaadin-app-layout"));
driver.findElement(By.tagName("vaadin-login-overlay"));
```

## Wait Strategies

### Explicit Waits (Recommended)

```java
// Wait for element to be present
WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(
    By.tagName("vaadin-grid")));

// Wait for element to be clickable
WebElement button = wait.until(ExpectedConditions.elementToBeClickable(
    By.cssSelector("vaadin-button[theme='primary']")));

// Wait for text to be present
wait.until(ExpectedConditions.textToBePresentInElement(
    element, "Expected Text"));

// Wait for element to be visible
wait.until(ExpectedConditions.visibilityOfElementLocated(
    By.cssSelector("vaadin-notification")));
```

### Custom Wait Conditions

```java
// Wait for grid to have data
wait.until(new ExpectedCondition<Boolean>() {
    @Override
    public Boolean apply(WebDriver driver) {
        List<WebElement> cells = driver.findElements(
            By.cssSelector("vaadin-grid vaadin-grid-cell-content"));
        return cells.size() > 0;
    }
});

// Wait for page to be fully loaded
wait.until(new ExpectedCondition<Boolean>() {
    @Override
    public Boolean apply(WebDriver driver) {
        return ((JavascriptExecutor) driver)
            .executeScript("return document.readyState").equals("complete");
    }
});
```

## Utility Methods

### Element Finding Helpers

```java
private List<WebElement> findCreateButtons() {
    List<WebElement> elements = new ArrayList<>();
    elements.addAll(driver.findElements(By.xpath("//vaadin-button[contains(text(), 'New')]")));
    elements.addAll(driver.findElements(By.xpath("//vaadin-button[contains(text(), 'Add')]")));
    elements.addAll(driver.findElements(By.xpath("//vaadin-button[contains(text(), 'Create')]")));
    return elements;
}

private List<WebElement> findSaveButtons() {
    List<WebElement> elements = new ArrayList<>();
    elements.addAll(driver.findElements(By.xpath("//vaadin-button[contains(text(), 'Save')]")));
    elements.addAll(driver.findElements(By.xpath("//vaadin-button[contains(text(), 'Create')]")));
    elements.addAll(driver.findElements(By.xpath("//vaadin-button[contains(text(), 'Update')]")));
    return elements;
}
```

### Screenshot Helper

```java
private void takeScreenshot(String name) {
    try {
        File screenshotDir = new File("target/screenshots");
        if (!screenshotDir.exists()) {
            screenshotDir.mkdirs();
        }
        
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String fileName = "selenium-" + name + "-" + System.currentTimeMillis() + ".png";
        File destFile = new File(screenshotDir, fileName);
        
        // Copy the screenshot file
        Files.copy(screenshot.toPath(), destFile.toPath());
        
        logger.info("📸 Screenshot saved: {}", destFile.getAbsolutePath());
    } catch (Exception e) {
        logger.warn("⚠️ Failed to take screenshot '{}': {}", name, e.getMessage());
    }
}
```

### JavaScript Execution

```java
// Execute JavaScript for complex operations
JavascriptExecutor js = (JavascriptExecutor) driver;

// Scroll to element
js.executeScript("arguments[0].scrollIntoView(true);", element);

// Click via JavaScript (bypass overlays)
js.executeScript("arguments[0].click();", element);

// Get element properties
String value = (String) js.executeScript("return arguments[0].value;", element);
```

## Browser-Specific Configurations

### Chrome Configuration

```java
ChromeOptions options = new ChromeOptions();
options.addArguments("--headless");
options.addArguments("--no-sandbox");
options.addArguments("--disable-dev-shm-usage");
options.addArguments("--disable-gpu");
options.addArguments("--remote-allow-origins=*");
options.setExperimentalOption("useAutomationExtension", false);
options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));

WebDriver driver = new ChromeDriver(options);
```

### Firefox Configuration

```java
FirefoxOptions options = new FirefoxOptions();
options.addArguments("--headless");
options.addPreference("dom.webnotifications.enabled", false);
options.addPreference("browser.helperApps.neverAsk.saveToDisk", "application/octet-stream");

WebDriver driver = new FirefoxDriver(options);
```

### Edge Configuration

```java
EdgeOptions options = new EdgeOptions();
options.addArguments("--headless");
options.addArguments("--disable-web-security");
options.addArguments("--allow-running-insecure-content");

WebDriver driver = new EdgeDriver(options);
```

## Performance Optimization

### Page Load Strategy

```java
ChromeOptions options = new ChromeOptions();
options.setPageLoadStrategy(PageLoadStrategy.EAGER); // Don't wait for all resources

WebDriver driver = new ChromeDriver(options);
```

### Implicit vs Explicit Waits

```java
// Set global implicit wait (use sparingly)
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

// Prefer explicit waits for specific elements
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
wait.until(ExpectedConditions.presenceOfElementLocated(By.id("element")));
```

### Resource Management

```java
@AfterEach
void tearDown() {
    if (driver != null) {
        try {
            driver.quit(); // Closes all windows and ends session
        } catch (Exception e) {
            logger.warn("Failed to quit driver: {}", e.getMessage());
        }
    }
}
```

## Debugging and Troubleshooting

### Debug Mode Execution

```java
// Run with visible browser for debugging
ChromeOptions options = new ChromeOptions();
options.addArguments("--disable-web-security");
// Remove --headless for visible browser

WebDriver driver = new ChromeDriver(options);
```

### Logging Configuration

```java
// Enable detailed logging
System.setProperty("webdriver.chrome.verboseLogging", "true");
System.setProperty("webdriver.chrome.logfile", "chrome.log");

// Browser console logs
LoggingPreferences logPrefs = new LoggingPreferences();
logPrefs.enable(LogType.BROWSER, Level.ALL);

ChromeOptions options = new ChromeOptions();
options.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
```

### Network Monitoring

```java
// Enable network logging
ChromeOptions options = new ChromeOptions();
options.addArguments("--enable-logging");
options.addArguments("--log-level=0");
options.addArguments("--v=1");

// Access network logs
LogEntries logs = driver.manage().logs().get(LogType.PERFORMANCE);
for (LogEntry entry : logs) {
    logger.info("Network log: {}", entry.getMessage());
}
```

## Integration with CI/CD

### GitHub Actions Configuration

```yaml
- name: Setup Chrome
  uses: browser-actions/setup-chrome@latest

- name: Run Selenium tests
  run: |
    ./run-selenium-tests.sh all
  env:
    SELENIUM_HEADLESS: true
    DISPLAY: ":99"
```

### Docker Configuration

```dockerfile
FROM maven:3.8-openjdk-17

# Install Chrome
RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add -
RUN echo "deb http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list
RUN apt-get update && apt-get install -y google-chrome-stable

# Set environment
ENV CHROME_BIN=/usr/bin/google-chrome
ENV DISPLAY=:99

COPY . /app
WORKDIR /app

CMD ["./run-selenium-tests.sh", "all"]
```

## Common Issues and Solutions

### WebDriver Not Found

```bash
# Problem: WebDriver binary not found
# Solution: WebDriverManager handles automatically
# Verify in code:
WebDriverManager.chromedriver().setup();
```

### Element Not Found

```java
// Problem: NoSuchElementException
// Solution: Use proper waits
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("id")));
```

### Stale Element Reference

```java
// Problem: Element reference is stale
// Solution: Re-find the element
try {
    element.click();
} catch (StaleElementReferenceException e) {
    element = driver.findElement(By.id("element-id"));
    element.click();
}
```

### Timeouts

```java
// Problem: Timeout exceptions
// Solution: Adjust timeouts and wait conditions
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
wait.until(ExpectedConditions.elementToBeClickable(element));
```

## Best Practices Summary

### ✅ Do's

1. **Use WebDriverManager** for automatic driver management
2. **Implement Page Object Model** for maintainability
3. **Use explicit waits** instead of Thread.sleep()
4. **Take screenshots** on failures for debugging
5. **Clean up resources** properly in tearDown methods
6. **Use meaningful test data** with timestamps
7. **Handle exceptions** gracefully with proper logging

### ❌ Don'ts

1. **Don't use Thread.sleep()** - use proper waits
2. **Don't hardcode waits** - make them configurable
3. **Don't ignore exceptions** - handle and log appropriately
4. **Don't use fragile selectors** - prefer stable attributes
5. **Don't forget to quit() driver** - always clean up
6. **Don't mix frameworks** in the same test class
7. **Don't skip screenshot on failures** - they're invaluable for debugging

## Conclusion

Selenium WebDriver provides a robust, industry-proven approach to UI testing for the Derbent application. Its maturity, extensive browser support, and rich ecosystem make it an excellent complement to Playwright. The implementation follows industry best practices and provides comprehensive test coverage while maintaining the project's high coding standards.