# Using GitHub Copilot with Selenium Tests

This guide shows how GitHub Copilot and AI assistants can help generate Selenium test code for the Derbent application.

## Overview

Selenium tests in this project are designed to be **Copilot-friendly**, making it easy for AI assistants to understand the codebase and generate accurate test code.

## Why Selenium Works Better with Copilot

1. **Widely Recognized**: Selenium is in Copilot's training data extensively
2. **Standard API**: Uses familiar WebDriver API patterns
3. **Clear Method Names**: Descriptive names that Copilot understands
4. **Good Examples**: Multiple working tests to learn from
5. **Comprehensive Base Class**: All common operations available

## Example: Asking Copilot to Generate Tests

### Prompt 1: Basic CRUD Test

**You ask Copilot:**
```
Create a Selenium test for Meeting CRUD operations in the Derbent application
```

**Copilot generates:**
```java
package automated_tests.tech.derbent.ui.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = tech.derbent.Application.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "server.port=0"
})
@DisplayName("📅 Selenium CRUD Test - Meetings")
public class CSeleniumMeetingCrudTest extends CSeleniumBaseUITest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CSeleniumMeetingCrudTest.class);
    
    @Test
    @DisplayName("✅ Complete CRUD workflow for Meetings")
    void testMeetingCrudOperations() {
        LOGGER.info("📅 Starting Selenium CRUD test for Meetings");
        
        // Login
        loginToApplication();
        takeScreenshot("selenium-meeting-01-logged-in");
        
        // Navigate to Meetings view
        navigateToViewByText("Meetings");
        wait_2000();
        takeScreenshot("selenium-meeting-02-meetings-view");
        
        // Perform CRUD workflow
        performCRUDWorkflow("Meeting");
        
        LOGGER.info("✅ Meeting CRUD test completed!");
    }
}
```

### Prompt 2: Specific Test Scenario

**You ask Copilot:**
```
Create a Selenium test that logs in, creates a new project with specific fields,
verifies it appears in the grid, and takes screenshots at each step
```

**Copilot generates:**
```java
@Test
@DisplayName("✅ Create project with specific fields")
void testCreateProjectWithDetails() {
    LOGGER.info("🚀 Starting detailed project creation test");
    
    // Step 1: Login
    loginToApplication();
    takeScreenshot("01-logged-in");
    
    // Step 2: Navigate to Projects
    navigateTo("/project-overview");
    wait_2000();
    takeScreenshot("02-projects-view");
    
    // Step 3: Click New button
    clickNew();
    wait_1000();
    takeScreenshot("03-new-project-dialog");
    
    // Step 4: Fill project details
    String projectName = "Integration Test Project " + System.currentTimeMillis();
    fillFirstTextField(projectName);
    takeScreenshot("04-name-filled");
    
    fillFirstTextArea("This is a detailed description for the test project");
    takeScreenshot("05-description-filled");
    
    // Select from comboboxes
    selectFirstComboBoxOption();
    wait_500();
    takeScreenshot("06-options-selected");
    
    // Step 5: Save the project
    clickSave();
    wait_2000();
    takeScreenshot("07-project-saved");
    
    // Step 6: Verify in grid
    boolean projectExists = verifyGridHasData();
    if (projectExists) {
        LOGGER.info("✅ Project successfully created and visible");
        int rowCount = getGridRowCount();
        LOGGER.info("📊 Grid now contains {} rows", rowCount);
    }
    takeScreenshot("08-project-in-grid");
    
    LOGGER.info("✅ Test completed successfully!");
}
```

### Prompt 3: Navigation Test

**You ask Copilot:**
```
Create a Selenium test that navigates through all main views
and takes a screenshot of each one
```

**Copilot generates:**
```java
@Test
@DisplayName("✅ Navigate through all main views")
void testNavigateThroughViews() {
    LOGGER.info("🧭 Starting navigation test");
    
    // Login first
    loginToApplication();
    takeScreenshot("nav-00-dashboard");
    
    // Define views to navigate
    String[] views = {
        "Projects",
        "Activities", 
        "Meetings",
        "Users",
        "Companies"
    };
    
    // Navigate to each view
    for (int i = 0; i < views.length; i++) {
        String viewName = views[i];
        LOGGER.info("🔍 Navigating to: {}", viewName);
        
        if (navigateToViewByText(viewName)) {
            wait_1000();
            takeScreenshot(String.format("nav-%02d-%s", i + 1, 
                viewName.toLowerCase()));
            LOGGER.info("✅ Successfully viewed: {}", viewName);
        } else {
            LOGGER.warn("⚠️ Could not navigate to: {}", viewName);
        }
    }
    
    LOGGER.info("✅ Navigation test completed!");
}
```

### Prompt 4: Form Validation Test

**You ask Copilot:**
```
Create a Selenium test that tries to save a project without required fields
and verifies error messages appear
```

**Copilot generates:**
```java
@Test
@DisplayName("✅ Test form validation for required fields")
void testFormValidation() {
    LOGGER.info("🔍 Starting form validation test");
    
    // Login and navigate
    loginToApplication();
    navigateTo("/project-overview");
    wait_2000();
    
    // Try to create project without filling required fields
    clickNew();
    wait_1000();
    takeScreenshot("validation-01-empty-form");
    
    // Try to save without filling anything
    clickSave();
    wait_1000();
    takeScreenshot("validation-02-error-shown");
    
    // Check if validation message appears
    // (Would need custom method to check for error messages)
    LOGGER.info("✅ Form validation test completed");
}
```

## Tips for Best Results with Copilot

### 1. Be Specific in Your Prompts

❌ Bad: "Create a test"
✅ Good: "Create a Selenium test for Activity CRUD operations that logs in, creates an activity, edits it, and deletes it"

### 2. Reference Existing Tests

❌ Bad: "Write a user test"
✅ Good: "Create a test like CSeleniumProjectCrudDemoTest but for Users"

### 3. Ask for Specific Patterns

Examples:
- "Create a test that uses the performCRUDWorkflow method"
- "Write a test that takes screenshots at each step"
- "Generate a test that verifies grid data after operations"

### 4. Request Documentation

Ask Copilot to add:
- Javadoc comments
- Inline explanations
- Logger statements
- DisplayName annotations

Example prompt:
```
Create a well-documented Selenium test for Company management with detailed
comments explaining each step
```

### 5. Iterate and Refine

If Copilot's first attempt isn't perfect:
- Ask it to "fix" or "improve" specific parts
- Request additional features
- Ask for error handling

Example:
```
Add error handling to the previous test to handle cases where elements
are not found
```

## Common Copilot Patterns for Selenium

### Pattern 1: Basic Test Structure

Copilot recognizes this structure:
```java
@Test
@DisplayName("Description")
void testMethodName() {
    // Login
    loginToApplication();
    
    // Navigate
    navigateTo("/path");
    
    // Perform actions
    clickNew();
    fillFirstTextField("value");
    clickSave();
    
    // Verify
    verifyGridHasData();
    
    // Screenshot
    takeScreenshot("test-name");
}
```

### Pattern 2: CRUD Workflow

Copilot can generate complete CRUD:
```java
@Test
void testFullCrud() {
    loginToApplication();
    navigateToViewByText("Entity");
    performCRUDWorkflow("Entity");
}
```

### Pattern 3: Step-by-Step with Screenshots

Copilot understands this pattern:
```java
@Test
void testWithScreenshots() {
    // Step 1: Setup
    loginToApplication();
    takeScreenshot("01-setup");
    
    // Step 2: Action
    clickNew();
    takeScreenshot("02-action");
    
    // Step 3: Verify
    verifyGridHasData();
    takeScreenshot("03-verify");
}
```

## Copilot Quick Commands

### Generate Full Test Class
Prompt: `Create a Selenium test class for [Entity] CRUD operations`

### Add Test Method
Prompt: `Add a test method that [specific action]`

### Generate Helper Method
Prompt: `Create a helper method to [specific task]`

### Add Documentation
Prompt: `Add detailed Javadoc to this test class`

### Generate Assertions
Prompt: `Add assertions to verify [expected behavior]`

## Advanced Copilot Usage

### 1. Generate Test Data

Prompt:
```
Create a helper method that generates random test data for a Project
```

Result:
```java
private String generateTestProjectName() {
    return "Test Project " + System.currentTimeMillis();
}

private String generateTestDescription() {
    return "Test description created at " + 
           java.time.LocalDateTime.now();
}
```

### 2. Generate Wait Conditions

Prompt:
```
Add a method to wait until an element with specific text appears
```

### 3. Generate Custom Verifications

Prompt:
```
Create a method to verify that a success message appears after saving
```

## Best Practices When Using Copilot

1. ✅ **Always review generated code** - Copilot makes suggestions, you decide
2. ✅ **Test generated code** - Run tests to verify they work
3. ✅ **Extend the base class** - Use CSeleniumBaseUITest for all tests
4. ✅ **Follow naming conventions** - Use descriptive test method names
5. ✅ **Add logging** - Include LOGGER statements for debugging
6. ✅ **Take screenshots** - Document test execution visually
7. ✅ **Handle errors** - Add try-catch where appropriate

## Troubleshooting Copilot-Generated Tests

### Issue: Test doesn't compile
**Solution**: Check imports and make sure test extends CSeleniumBaseUITest

### Issue: Element not found
**Solution**: Add wait_1000() or wait_2000() before interacting with elements

### Issue: Test fails in headless mode
**Solution**: Add more wait time or check if element is actually visible

### Issue: Screenshots not saved
**Solution**: Ensure target/screenshots directory exists or test creates it

## Examples of Copilot-Friendly Comments

Good comments help Copilot understand context:

```java
// Navigate to the main Projects view where all projects are listed
navigateTo("/project-overview");

// Click the New button to open the project creation dialog
clickNew();

// Fill in the project name field with a unique test value
fillFirstTextField("Test Project " + System.currentTimeMillis());

// Save the project and wait for the form to close
clickSave();
wait_2000();

// Verify that the new project appears in the grid
boolean projectCreated = verifyGridHasData();
```

## Conclusion

Selenium tests in Derbent are designed to work seamlessly with GitHub Copilot:

- ✅ Standard Selenium API that Copilot knows well
- ✅ Clear, descriptive method names
- ✅ Comprehensive base class with helper methods
- ✅ Multiple working examples to learn from
- ✅ Good documentation and comments

**Result**: Copilot can generate accurate, working Selenium tests with minimal manual editing!

---

Happy testing with Copilot! 🤖🚀
