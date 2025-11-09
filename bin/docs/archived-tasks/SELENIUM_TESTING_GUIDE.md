# Selenium UI Testing for Derbent

This document describes how to run Selenium-based UI tests for the Derbent project management application.

## Overview

The Derbent project now supports **Selenium WebDriver** for UI automation testing, in addition to the existing Playwright tests. Selenium tests provide better integration with Copilot and other AI-powered testing tools.

### Why Selenium?

- **Better Copilot Integration**: Selenium is widely recognized by AI assistants and coding tools
- **Industry Standard**: Most widely used browser automation framework
- **Flexible Browser Support**: Works with Chrome, Firefox, Edge, Safari
- **Rich Ecosystem**: Extensive community support and documentation
- **Dual Mode Operation**: Support for both headless and visible browser testing

## Test Infrastructure

### Base Class: `CSeleniumBaseUITest`

All Selenium tests extend `CSeleniumBaseUITest` which provides:

- **Browser Setup**: Automatic ChromeDriver setup using WebDriverManager
- **Authentication**: Login helpers with sample data initialization
- **Navigation**: Methods to navigate between views
- **CRUD Operations**: Helper methods for Create, Read, Update, Delete operations
- **Form Interactions**: Fill text fields, select combobox options
- **Grid Operations**: Click rows, verify data, count rows
- **Screenshot Capture**: Automatic screenshot generation for test documentation
- **Wait Utilities**: Smart waiting for page loads and element availability

### Demo Test: `CSeleniumProjectCrudDemoTest`

A comprehensive demo test showcasing complete CRUD workflow:
1. Login with admin credentials
2. Navigate to Projects view
3. Create a new project
4. Verify project in grid
5. Update project details
6. Delete project
7. Capture screenshots at each step

## Running Selenium Tests

### Prerequisites

- Java 17+
- Maven 3.9+
- Chrome browser installed (ChromeDriver downloaded automatically)

### Quick Start

```bash
# Run all Selenium tests in headless mode (default)
./run-selenium-tests.sh

# Run with visible browser (watch tests execute)
./run-selenium-tests.sh --visible

# Run specific test class
./run-selenium-tests.sh -t CSeleniumProjectCrudDemoTest

# Run specific test with visible browser
./run-selenium-tests.sh --visible -t CSeleniumProjectCrudDemoTest
```

### Using Maven Directly

```bash
# Headless mode (default)
mvn test -Dtest=automated_tests.tech.derbent.ui.selenium.CSeleniumProjectCrudDemoTest

# Visible browser mode
mvn test -Dtest=automated_tests.tech.derbent.ui.selenium.CSeleniumProjectCrudDemoTest -Dselenium.headless=false

# Run all Selenium tests
mvn test -Dtest=automated_tests.tech.derbent.ui.selenium.*

# Run all Selenium tests with visible browser
mvn test -Dtest=automated_tests.tech.derbent.ui.selenium.* -Dselenium.headless=false
```

## Configuration Options

### Browser Mode

- **Headless (Default)**: Tests run without visible browser window
  - Faster execution
  - Ideal for CI/CD pipelines
  - Lower resource usage
  - Set with `-Dselenium.headless=true` (default)

- **Visible**: Browser window is visible during test execution
  - Great for development and debugging
  - Watch tests execute in real-time
  - Easier to understand test failures
  - Set with `-Dselenium.headless=false`

### Database Configuration

Tests use H2 in-memory database by default:
- `spring.datasource.url=jdbc:h2:mem:testdb`
- `spring.datasource.username=sa`
- `spring.datasource.password=` (empty)

### Application Port

Tests use `RANDOM_PORT` configuration to avoid port conflicts during test execution.

## Test Output

### Screenshots

All tests generate screenshots saved to `target/screenshots/`:
- Named with `selenium-` prefix for easy identification
- Captured at key points in test workflow
- Useful for debugging test failures
- Include timestamp in filenames for test runs

Example screenshots:
- `selenium-01-logged-in.png` - After successful login
- `selenium-02-projects-view.png` - Projects view loaded
- `selenium-03-new-project-dialog.png` - New project dialog
- `selenium-04-filled-project-form.png` - Form filled with test data
- ... and more for each step

### Test Reports

Maven Surefire generates test reports in:
- `target/surefire-reports/` - JUnit XML and text reports
- Console output with emoji indicators for easy scanning

## Writing New Selenium Tests

### Basic Test Structure

```java
package automated_tests.tech.derbent.ui.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, 
                classes = tech.derbent.Application.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("My Custom Selenium Test")
public class CMySeleniumTest extends CSeleniumBaseUITest {

    @Test
    @DisplayName("Test something specific")
    void testMyFeature() {
        // Login
        loginToApplication();
        
        // Navigate to view
        navigateTo("/my-view");
        
        // Perform actions
        clickNew();
        fillFirstTextField("Test Data");
        clickSave();
        
        // Verify results
        boolean hasData = verifyGridHasData();
        // assertions...
        
        // Capture screenshot
        takeScreenshot("my-test-result");
    }
}
```

### Available Helper Methods

**Navigation:**
- `navigateToLogin()` - Go to login page
- `navigateTo(String path)` - Navigate to specific URL path
- `navigateToViewByText(String text)` - Click menu item by text
- `waitForPageLoad()` - Wait for page to fully load

**Authentication:**
- `loginToApplication()` - Login with default admin credentials
- `loginToApplication(String username, String password)` - Login with custom credentials
- `initializeSampleDataFromLoginPage()` - Load sample data

**Button Actions:**
- `clickNew()` - Click "New" button
- `clickSave()` - Click "Save" button
- `clickEdit()` - Click "Edit" button
- `clickDelete()` - Click "Delete" button
- `clickCancel()` - Click "Cancel" button

**Form Fields:**
- `fillFirstTextField(String value)` - Fill first text field
- `fillFirstTextArea(String value)` - Fill first text area
- `selectFirstComboBoxOption()` - Select first option in combobox

**Grid Operations:**
- `clickFirstGridRow()` - Click first row in grid
- `verifyGridHasData()` - Check if grid contains data
- `getGridRowCount()` - Get number of rows in grid

**CRUD Workflow:**
- `performCRUDWorkflow(String entityName)` - Complete CRUD workflow

**Screenshots:**
- `takeScreenshot(String name)` - Capture and save screenshot

**Wait Utilities:**
- `wait_500()` - Wait 500ms
- `wait_1000()` - Wait 1 second
- `wait_2000()` - Wait 2 seconds

## Comparison: Selenium vs Playwright

### Selenium Advantages
- ✅ Better AI/Copilot recognition and support
- ✅ Larger community and more resources
- ✅ Industry-standard tool
- ✅ More browser driver options
- ✅ Easier to find examples and solutions

### Playwright Advantages
- ✅ Modern API design
- ✅ Better handling of dynamic content
- ✅ Built-in auto-waiting
- ✅ Better debugging tools
- ✅ Faster execution in some cases

**Both frameworks are supported in this project** - use whichever fits your needs better!

## Troubleshooting

### ChromeDriver Issues

If you encounter ChromeDriver issues:

1. **WebDriverManager handles downloads automatically**, but if needed:
   ```bash
   # Update ChromeDriver
   mvn dependency:resolve
   ```

2. **Check Chrome version compatibility**:
   ```bash
   google-chrome --version
   ```

3. **Clear WebDriverManager cache** if problems persist:
   ```bash
   rm -rf ~/.m2/repository/webdriver
   ```

### Test Failures

1. **Check screenshots** in `target/screenshots/` - they show exact point of failure
2. **Run in visible mode** to watch test execution:
   ```bash
   ./run-selenium-tests.sh --visible -t YourTestClass
   ```
3. **Check console output** for detailed error messages
4. **Verify application is starting correctly** - check for port conflicts

### Headless Mode Issues

If headless mode fails but visible mode works:
- May be graphics driver issues in CI environment
- Try adding more Chrome options in `CSeleniumBaseUITest.setupSeleniumEnvironment()`
- Check Docker/CI environment has required dependencies

## CI/CD Integration

### GitHub Actions Example

```yaml
- name: Run Selenium Tests
  run: |
    ./run-selenium-tests.sh --headless
  env:
    SELENIUM_HEADLESS: true

- name: Upload Screenshots
  if: always()
  uses: actions/upload-artifact@v3
  with:
    name: selenium-screenshots
    path: target/screenshots/selenium-*.png
```

### Jenkins Example

```groovy
stage('Selenium Tests') {
    steps {
        sh './run-selenium-tests.sh --headless'
    }
    post {
        always {
            archiveArtifacts artifacts: 'target/screenshots/selenium-*.png',
                            allowEmptyArchive: true
            junit 'target/surefire-reports/*.xml'
        }
    }
}
```

## Best Practices

1. **Always extend `CSeleniumBaseUITest`** - inherit all helper methods
2. **Use descriptive test names** - helps identify what's being tested
3. **Take screenshots at key points** - aids in debugging failures
4. **Use wait methods appropriately** - ensure elements are ready
5. **Clean up test data** - each test should be independent
6. **Run in headless mode for CI** - faster and more reliable
7. **Run in visible mode for debugging** - easier to understand failures
8. **Check grid data after CRUD operations** - verify expected state

## Support

For issues or questions:
- Check existing Selenium tests for examples
- Review `CSeleniumBaseUITest` for available methods
- Consult Selenium documentation: https://www.selenium.dev/documentation/
- Ask Copilot - it's great at generating Selenium test code!

## Future Enhancements

Planned improvements:
- [ ] Support for Firefox and Edge browsers
- [ ] Parallel test execution
- [ ] Page Object Model pattern implementation
- [ ] Video recording of test execution
- [ ] Integration with test reporting tools
- [ ] More entity-specific CRUD tests

---

**Happy Testing with Selenium! 🚀**
