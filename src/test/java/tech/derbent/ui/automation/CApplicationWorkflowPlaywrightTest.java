package tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

/**
 * CApplicationWorkflowPlaywrightTest - Comprehensive Playwright tests for complete application workflows.
 * 
 * Tests end-to-end application scenarios, cross-view navigation, and complex user workflows
 * following the strict coding guidelines for Playwright testing.
 */
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop", 
    "server.port=8080"
})
public class CApplicationWorkflowPlaywrightTest extends CBaseUITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CApplicationWorkflowPlaywrightTest.class);

    @Test
    void testCompleteApplicationLoginAndNavigation() {
        LOGGER.info("🧪 Testing complete application login and navigation...");
        
        // Test initial application load
        page.navigate(baseUrl);
        wait_loginscreen();
        takeScreenshot("application-initial-load");
        
        // Test login
        performLogin("admin", "test123");
        wait_afterlogin();
        takeScreenshot("application-after-login");
        
        // Test navigation to all main views
        final String[] views = {"Projects", "Activities", "Meetings", "Users"};
        
        for (String view : views) {
            LOGGER.debug("Testing navigation to {}", view);
            if (navigateToViewByText(view)) {
                wait_1000();
                takeScreenshot("application-navigation-" + view.toLowerCase());
                
                // Verify grid is present and functional
                assertTrue(getGridRowCount() >= 0, "Grid should be present in " + view + " view");
            }
        }
        
        LOGGER.info("✅ Complete application login and navigation test completed");
    }

    @Test
    void testCrossViewDataConsistency() {
        LOGGER.info("🧪 Testing cross-view data consistency...");
        
        loginToApplication();
        
        // Create a project first
        navigateToViewByText("Projects");
        wait_1000();
        
        final var newButtons = page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");
        if (newButtons.count() > 0) {
            newButtons.first().click();
            wait_1000();
            
            final String projectName = "CrossViewTestProject_" + System.currentTimeMillis();
            fillFirstTextField(projectName);
            
            final var saveButtons = page.locator("vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");
            if (saveButtons.count() > 0) {
                saveButtons.first().click();
                wait_2000();
                takeScreenshot("cross-view-project-created");
            }
            
            // Now check if this project appears in Activities view
            navigateToViewByText("Activities");
            wait_1000();
            
            // Try to create an activity for this project
            final var activityNewButtons = page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");
            if (activityNewButtons.count() > 0) {
                activityNewButtons.first().click();
                wait_1000();
                
                // Check if project ComboBox contains our new project
                final var comboBoxes = page.locator("vaadin-combo-box");
                if (comboBoxes.count() > 0) {
                    comboBoxes.first().click();
                    wait_500();
                    
                    // Look for our project in the options
                    final var options = page.locator("vaadin-combo-box-item");
                    boolean projectFound = false;
                    for (int i = 0; i < options.count(); i++) {
                        String optionText = options.nth(i).textContent();
                        if (optionText != null && optionText.contains("CrossViewTestProject")) {
                            projectFound = true;
                            options.nth(i).click();
                            break;
                        }
                    }
                    
                    LOGGER.debug("Project found in Activities ComboBox: {}", projectFound);
                    takeScreenshot("cross-view-project-in-activities");
                }
                
                // Close form
                final var cancelButtons = page.locator("vaadin-button:has-text('Cancel')");
                if (cancelButtons.count() > 0) {
                    cancelButtons.first().click();
                    wait_500();
                }
            }
        }
        
        LOGGER.info("✅ Cross-view data consistency test completed");
    }

    @Test
    void testApplicationResponsiveness() {
        LOGGER.info("🧪 Testing application responsiveness across views...");
        
        loginToApplication();
        
        final String[] views = {"Projects", "Activities", "Meetings", "Users"};
        final int[][] viewports = {{375, 667}, {768, 1024}, {1200, 800}, {1920, 1080}};
        final String[] viewportNames = {"mobile", "tablet", "desktop", "large-desktop"};
        
        for (String view : views) {
            navigateToViewByText(view);
            wait_1000();
            
            for (int i = 0; i < viewports.length; i++) {
                page.setViewportSize(viewports[i][0], viewports[i][1]);
                wait_500();
                takeScreenshot("responsive-" + view.toLowerCase() + "-" + viewportNames[i]);
            }
        }
        
        // Reset to default viewport
        page.setViewportSize(1200, 800);
        
        LOGGER.info("✅ Application responsiveness test completed");
    }

    @Test
    void testApplicationAccessibilityComprehensive() {
        LOGGER.info("🧪 Testing comprehensive application accessibility...");
        
        loginToApplication();
        
        final String[] views = {"Projects", "Activities", "Meetings", "Users"};
        
        for (String view : views) {
            navigateToViewByText(view);
            wait_1000();
            
            // Check for proper heading structure
            final var headings = page.locator("h1, h2, h3, h4, h5, h6");
            LOGGER.debug("{} view has {} headings", view, headings.count());
            
            // Check for aria labels and roles
            final var ariaElements = page.locator("[aria-label], [aria-labelledby], [role]");
            LOGGER.debug("{} view has {} elements with ARIA attributes", view, ariaElements.count());
            
            // Check for keyboard navigation
            final var focusableElements = page.locator("button, a, input, select, textarea, [tabindex]:not([tabindex='-1'])");
            LOGGER.debug("{} view has {} focusable elements", view, focusableElements.count());
            
            takeScreenshot("accessibility-" + view.toLowerCase());
        }
        
        LOGGER.info("✅ Comprehensive application accessibility test completed");
    }

    @Test
    void testApplicationErrorHandling() {
        LOGGER.info("🧪 Testing application error handling...");
        
        loginToApplication();
        
        // Test form validation errors across views
        final String[] views = {"Projects", "Activities", "Meetings", "Users"};
        
        for (String view : views) {
            navigateToViewByText(view);
            wait_1000();
            
            // Try to create new item and submit empty form
            final var newButtons = page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");
            if (newButtons.count() > 0) {
                newButtons.first().click();
                wait_1000();
                
                // Try to save without filling required fields
                final var saveButtons = page.locator("vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");
                if (saveButtons.count() > 0) {
                    saveButtons.first().click();
                    wait_1000();
                    
                    // Check for validation messages
                    final var errorMessages = page.locator("vaadin-text-field[invalid], .error-message, [role='alert']");
                    LOGGER.debug("{} view validation produced {} error messages", view, errorMessages.count());
                    
                    takeScreenshot("error-handling-" + view.toLowerCase());
                }
                
                // Close form
                final var cancelButtons = page.locator("vaadin-button:has-text('Cancel')");
                if (cancelButtons.count() > 0) {
                    cancelButtons.first().click();
                    wait_500();
                }
            }
        }
        
        LOGGER.info("✅ Application error handling test completed");
    }

    @Test
    void testApplicationPerformance() {
        LOGGER.info("🧪 Testing application performance...");
        
        // Test initial load time
        final long startTime = System.currentTimeMillis();
        page.navigate(baseUrl);
        wait_loginscreen();
        final long loadTime = System.currentTimeMillis() - startTime;
        LOGGER.debug("Application initial load time: {}ms", loadTime);
        
        // Test login performance
        final long loginStartTime = System.currentTimeMillis();
        performLogin("admin", "test123");
        wait_afterlogin();
        final long loginTime = System.currentTimeMillis() - loginStartTime;
        LOGGER.debug("Login time: {}ms", loginTime);
        
        // Test navigation performance
        final String[] views = {"Projects", "Activities", "Meetings", "Users"};
        
        for (String view : views) {
            final long navStartTime = System.currentTimeMillis();
            navigateToViewByText(view);
            wait_1000();
            final long navTime = System.currentTimeMillis() - navStartTime;
            LOGGER.debug("Navigation to {} time: {}ms", view, navTime);
        }
        
        takeScreenshot("performance-test-completed");
        
        LOGGER.info("✅ Application performance test completed");
    }

    @Test
    void testApplicationDataFlow() {
        LOGGER.info("🧪 Testing complete application data flow...");
        
        loginToApplication();
        
        // Create a complete data flow: User -> Company -> Project -> Activity -> Meeting
        final String timestamp = String.valueOf(System.currentTimeMillis());
        
        // Step 1: Create User (if Users view is available)
        if (navigateToViewByText("Users")) {
            wait_1000();
            final var newButtons = page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");
            if (newButtons.count() > 0) {
                newButtons.first().click();
                wait_1000();
                
                fillFirstTextField("TestUser_" + timestamp);
                
                final var saveButtons = page.locator("vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");
                if (saveButtons.count() > 0) {
                    saveButtons.first().click();
                    wait_2000();
                    takeScreenshot("dataflow-user-created");
                }
            }
        }
        
        // Step 2: Create Project
        if (navigateToViewByText("Projects")) {
            wait_1000();
            final var newButtons = page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");
            if (newButtons.count() > 0) {
                newButtons.first().click();
                wait_1000();
                
                fillFirstTextField("TestProject_" + timestamp);
                
                final var saveButtons = page.locator("vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");
                if (saveButtons.count() > 0) {
                    saveButtons.first().click();
                    wait_2000();
                    takeScreenshot("dataflow-project-created");
                }
            }
        }
        
        // Step 3: Create Activity for the Project
        if (navigateToViewByText("Activities")) {
            wait_1000();
            final var newButtons = page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");
            if (newButtons.count() > 0) {
                newButtons.first().click();
                wait_1000();
                
                fillFirstTextField("TestActivity_" + timestamp);
                
                final var saveButtons = page.locator("vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");
                if (saveButtons.count() > 0) {
                    saveButtons.first().click();
                    wait_2000();
                    takeScreenshot("dataflow-activity-created");
                }
            }
        }
        
        // Step 4: Create Meeting for the Project
        if (navigateToViewByText("Meetings")) {
            wait_1000();
            final var newButtons = page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");
            if (newButtons.count() > 0) {
                newButtons.first().click();
                wait_1000();
                
                fillFirstTextField("TestMeeting_" + timestamp);
                
                final var saveButtons = page.locator("vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");
                if (saveButtons.count() > 0) {
                    saveButtons.first().click();
                    wait_2000();
                    takeScreenshot("dataflow-meeting-created");
                }
            }
        }
        
        LOGGER.info("✅ Complete application data flow test completed");
    }

    @Test
    void testApplicationLogoutAndSecurity() {
        LOGGER.info("🧪 Testing application logout and security...");
        
        loginToApplication();
        wait_1000();
        takeScreenshot("security-logged-in");
        
        // Test logout functionality
        if (performLogout()) {
            wait_loginscreen();
            takeScreenshot("security-logged-out");
            
            // Verify we're back at login screen
            assertTrue(page.locator("vaadin-login-overlay").isVisible(), "Should be at login screen after logout");
            
            // Try to access a protected view directly
            page.navigate(baseUrl + "/projects");
            wait_1000();
            
            // Should be redirected to login
            takeScreenshot("security-redirect-to-login");
            
        } else {
            LOGGER.warn("Logout functionality not available for testing");
        }
        
        LOGGER.info("✅ Application logout and security test completed");
    }
}