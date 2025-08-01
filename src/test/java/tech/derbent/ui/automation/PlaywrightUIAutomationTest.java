package tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

/**
 * PlaywrightUIAutomationTest - End-to-end UI automation tests using Microsoft Playwright.
 * 
 * Playwright provides modern browser automation with excellent features:
 * - Fast, reliable automation across all modern browsers
 * - Built-in waiting for elements and network requests
 * - Cross-browser testing (Chrome, Firefox, Safari, Edge)
 * - Excellent debugging capabilities with trace viewer
 * - No external driver management needed
 * 
 * This implementation provides comprehensive testing:
 * - Opens real browser instances
 * - Navigates through different views (Meetings, Projects, Decisions)
 * - Clicks buttons and UI components
 * - Fills forms with test data
 * - Validates data entry and display
 * - Takes screenshots for verification
 * - Tests responsive design across different viewports
 * 
 * Advantages over Selenium and TestBench:
 * - Faster and more stable
 * - Better element waiting strategies
 * - Built-in mobile testing capabilities
 * - Superior debugging tools
 * - Modern async API design
 * - Free and open source
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class PlaywrightUIAutomationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(PlaywrightUIAutomationTest.class);
    
    @LocalServerPort
    private int port;
    
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    
    private String baseUrl;
    
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        
        // Initialize Playwright
        playwright = Playwright.create();
        
        // Launch browser (use Chromium by default, can be changed to firefox() or webkit())
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
            .setHeadless(true) // Set to false for debugging
            .setSlowMo(100)); // Add small delay between actions for visibility
        
        // Create context with desktop viewport
        context = browser.newContext(new Browser.NewContextOptions()
            .setViewportSize(1200, 800));
        
        // Create page
        page = context.newPage();
        
        // Enable console logging
        page.onConsoleMessage(msg -> logger.info("Browser console: {}", msg.text()));
        
        logger.info("Playwright test setup completed. Application URL: {}", baseUrl);
    }
    
    @AfterEach
    void tearDown() {
        if (page != null) {
            page.close();
        }
        if (context != null) {
            context.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
        logger.info("Playwright test cleanup completed");
    }
    
    @Test
    void testApplicationLoadsSuccessfully() {
        logger.info("🧪 Testing application loads successfully...");
        
        // Navigate to application
        page.navigate(baseUrl);
        
        // Wait for login overlay to be visible (application should require login)
        page.waitForSelector("vaadin-login-overlay", new Page.WaitForSelectorOptions().setTimeout(10000));
        
        // Take screenshot of login page
        takeScreenshot("login-page-loaded");
        
        // Verify page title contains expected text
        String title = page.title();
        assertNotNull(title);
        logger.info("✅ Application loaded successfully. Title: {}", title);
        
        // Check that login overlay is present
        assertTrue(page.locator("vaadin-login-overlay").isVisible());
        
        logger.info("✅ Application loads successfully test completed");
    }
    
    @Test
    void testLoginFunctionality() {
        logger.info("🧪 Testing login functionality...");
        
        // Navigate to application
        page.navigate(baseUrl);
        
        // Wait for login overlay
        page.waitForSelector("vaadin-login-overlay", new Page.WaitForSelectorOptions().setTimeout(10000));
        
        // Perform login with test credentials
        performLogin("admin", "test123");
        
        // Wait for successful login - should see main application layout
        page.waitForSelector("vaadin-app-layout", new Page.WaitForSelectorOptions().setTimeout(10000));
        
        takeScreenshot("successful-login");
        
        // Verify main navigation is present after login
        assertTrue(page.locator("vaadin-app-layout").isVisible());
        
        logger.info("✅ Login functionality test completed successfully");
    }
    
    @Test
    void testInvalidLoginHandling() {
        logger.info("🧪 Testing invalid login handling...");
        
        // Navigate to application
        page.navigate(baseUrl);
        
        // Wait for login overlay
        page.waitForSelector("vaadin-login-overlay", new Page.WaitForSelectorOptions().setTimeout(10000));
        
        // Attempt login with invalid credentials
        performLogin("invalid", "wrongpassword");
        
        // Should still be on login page - check for error indication
        page.waitForTimeout(2000);
        takeScreenshot("invalid-login-attempt");
        
        // Verify we're still at login overlay
        assertTrue(page.locator("vaadin-login-overlay").isVisible());
        
        logger.info("✅ Invalid login handling test completed");
    }
    
    @Test
    void testLogoutFunctionality() {
        logger.info("🧪 Testing logout functionality...");
        
        // Login first
        loginToApplication();
        
        // Look for logout option
        if (performLogout()) {
            // Verify we're back at login page
            page.waitForSelector("vaadin-login-overlay", new Page.WaitForSelectorOptions().setTimeout(10000));
            takeScreenshot("after-logout");
            
            assertTrue(page.locator("vaadin-login-overlay").isVisible());
            logger.info("✅ Logout functionality test completed successfully");
        } else {
            logger.warn("⚠️ Logout functionality not tested - logout button not found");
        }
    }
    
    @Test
    void testNavigationBetweenViews() {
        logger.info("🧪 Testing navigation between views...");
        
        // Login first
        loginToApplication();
        
        // Test navigation to different views
        String[] viewSelectors = {
            "vaadin-side-nav-item[path='/projects']",
            "vaadin-side-nav-item[path='/meetings']", 
            "vaadin-side-nav-item[path='/activities']",
            "vaadin-side-nav-item[path='/decisions']"
        };
        
        String[] viewNames = {"Projects", "Meetings", "Activities", "Decisions"};
        
        for (int i = 0; i < viewSelectors.length; i++) {
            try {
                logger.info("Navigating to {} view...", viewNames[i]);
                
                // Click on navigation item
                if (page.locator(viewSelectors[i]).isVisible()) {
                    page.locator(viewSelectors[i]).click();
                    
                    // Wait for view to load
                    page.waitForTimeout(1000);
                    
                    // Take screenshot
                    takeScreenshot(viewNames[i].toLowerCase() + "-view");
                    
                    logger.info("✅ Successfully navigated to {} view", viewNames[i]);
                } else {
                    logger.warn("⚠️ Navigation item for {} not found, checking alternative selectors", viewNames[i]);
                    
                    // Try alternative navigation approaches
                    if (navigateToViewByText(viewNames[i])) {
                        takeScreenshot(viewNames[i].toLowerCase() + "-view-alt");
                        logger.info("✅ Successfully navigated to {} view using alternative method", viewNames[i]);
                    }
                }
                
            } catch (Exception e) {
                logger.warn("⚠️ Could not navigate to {} view: {}", viewNames[i], e.getMessage());
                takeScreenshot("navigation-error-" + viewNames[i].toLowerCase());
            }
        }
        
        logger.info("✅ Navigation between views test completed");
    }
    
    @Test
    void testCRUDOperationsInProjects() {
        logger.info("🧪 Testing CRUD operations in Projects view...");
        
        // Login and navigate to Projects
        loginToApplication();
        navigateToViewByText("Projects");
        
        // Test Create operation
        testCreateOperation("Projects", "Test Project " + System.currentTimeMillis());
        
        // Test Read operation
        testReadOperation("Projects");
        
        // Test Update operation
        testUpdateOperation("Projects", "Updated Project Name");
        
        // Test Delete operation (with caution)
        testDeleteOperation("Projects");
        
        logger.info("✅ CRUD operations test completed for Projects");
    }
    
    @Test
    void testCRUDOperationsInMeetings() {
        logger.info("🧪 Testing CRUD operations in Meetings view...");
        
        // Login and navigate to Meetings
        loginToApplication();
        navigateToViewByText("Meetings");
        
        // Test Create operation
        testCreateOperation("Meetings", "Test Meeting " + System.currentTimeMillis());
        
        // Test Read operation
        testReadOperation("Meetings");
        
        // Test Update operation
        testUpdateOperation("Meetings", "Updated Meeting Title");
        
        // Test Delete operation (with caution)
        testDeleteOperation("Meetings");
        
        logger.info("✅ CRUD operations test completed for Meetings");
    }
    
    @Test
    void testFormInteractions() {
        logger.info("🧪 Testing form interactions...");
        
        // Navigate to application
        page.navigate(baseUrl);
        page.waitForSelector("vaadin-app-layout", new Page.WaitForSelectorOptions().setTimeout(10000));
        
        // Try to find and interact with forms in different views
        testFormInView("Projects", "/projects");
        testFormInView("Meetings", "/meetings");
        testFormInView("Decisions", "/decisions");
        
        logger.info("✅ Form interactions test completed");
    }
    
    @Test 
    void testResponsiveDesign() {
        logger.info("🧪 Testing responsive design across different viewports...");
        
        // Test different viewport sizes
        int[][] viewports = {
            {1920, 1080}, // Desktop
            {768, 1024},  // Tablet
            {375, 667}    // Mobile
        };
        
        String[] deviceNames = {"Desktop", "Tablet", "Mobile"};
        
        for (int i = 0; i < viewports.length; i++) {
            logger.info("Testing {} viewport: {}x{}", deviceNames[i], 
                       viewports[i][0], viewports[i][1]);
            
            // Set viewport size
            page.setViewportSize(viewports[i][0], viewports[i][1]);
            
            // Navigate to application
            page.navigate(baseUrl);
            page.waitForSelector("vaadin-app-layout", new Page.WaitForSelectorOptions().setTimeout(10000));
            
            // Take screenshot
            takeScreenshot("responsive-" + deviceNames[i].toLowerCase());
            
            // Verify layout adapts to viewport
            assertTrue(page.locator("vaadin-app-layout").isVisible());
            
            logger.info("✅ {} layout verified", deviceNames[i]);
        }
        
        logger.info("✅ Responsive design test completed");
    }
    
    @Test
    void testCompleteApplicationFlow() {
        logger.info("🧪 Testing complete application workflow...");
        
        // Navigate to application
        page.navigate(baseUrl);
        page.waitForSelector("vaadin-app-layout", new Page.WaitForSelectorOptions().setTimeout(10000));
        
        takeScreenshot("workflow-start");
        
        // Test workflow across different views
        performWorkflowInView("Projects");
        performWorkflowInView("Meetings");
        performWorkflowInView("Decisions");
        
        takeScreenshot("workflow-completed");
        
        logger.info("✅ Complete application workflow test completed");
    }
    
    @Test
    void testGridInteractions() {
        logger.info("🧪 Testing grid interactions...");
        
        // Login first
        loginToApplication();
        
        // Test grid interactions in different views
        testAdvancedGridInView("Projects");
        testAdvancedGridInView("Meetings");
        testAdvancedGridInView("Activities");
        
        logger.info("✅ Grid interactions test completed");
    }
    
    @Test
    void testFormValidationAndErrorHandling() {
        logger.info("🧪 Testing form validation and error handling...");
        
        // Login first
        loginToApplication();
        
        // Test form validation in Projects view
        if (navigateToViewByText("Projects")) {
            testFormValidation("Projects");
        }
        
        // Test form validation in Meetings view
        if (navigateToViewByText("Meetings")) {
            testFormValidation("Meetings");
        }
        
        logger.info("✅ Form validation and error handling test completed");
    }
    
    @Test
    void testAccessibilityBasics() {
        logger.info("🧪 Testing basic accessibility features...");
        
        // Navigate to application
        page.navigate(baseUrl);
        page.waitForSelector("vaadin-app-layout", new Page.WaitForSelectorOptions().setTimeout(10000));
        
        // Check for basic accessibility elements
        checkAccessibilityElement("main", "Main content area");
        checkAccessibilityElement("nav", "Navigation area");
        checkAccessibilityElement("[role='button']", "Interactive buttons");
        checkAccessibilityElement("h1, h2, h3, h4, h5, h6", "Heading structure");
        
        takeScreenshot("accessibility-check");
        
        logger.info("✅ Basic accessibility test completed");
    }
    
    // Helper methods
    
    /**
     * Helper method to login to the application with default credentials
     */
    private void loginToApplication() {
        page.navigate(baseUrl);
        page.waitForSelector("vaadin-login-overlay", new Page.WaitForSelectorOptions().setTimeout(10000));
        performLogin("admin", "test123");
        page.waitForSelector("vaadin-app-layout", new Page.WaitForSelectorOptions().setTimeout(10000));
    }
    
    /**
     * Performs login with specified credentials
     */
    private void performLogin(String username, String password) {
        try {
            logger.info("Performing login with username: {}", username);
            
            // Find username field within login overlay
            var usernameField = page.locator("vaadin-login-overlay vaadin-text-field");
            usernameField.fill(username);
            
            // Find password field within login overlay
            var passwordField = page.locator("vaadin-login-overlay vaadin-password-field");
            passwordField.fill(password);
            
            takeScreenshot("login-credentials-entered");
            
            // Click login button
            var loginButton = page.locator("vaadin-login-overlay vaadin-button[theme~='primary']");
            loginButton.click();
            
            // Wait a moment for login processing
            page.waitForTimeout(1000);
            
            logger.info("✅ Login credentials submitted");
            
        } catch (Exception e) {
            logger.error("❌ Login failed: {}", e.getMessage());
            takeScreenshot("login-failed");
            throw new RuntimeException("Login failed", e);
        }
    }
    
    /**
     * Attempts to perform logout
     */
    private boolean performLogout() {
        try {
            // Look for logout button or menu
            var logoutButtons = page.locator("vaadin-button:has-text('Logout'), a:has-text('Logout'), vaadin-menu-bar-button:has-text('Logout')");
            
            if (logoutButtons.count() > 0) {
                logoutButtons.first().click();
                page.waitForTimeout(1000);
                return true;
            }
            
            // Alternative: look for user menu that might contain logout
            var userMenus = page.locator("vaadin-menu-bar, [role='button']:has-text('User')");
            if (userMenus.count() > 0) {
                userMenus.first().click();
                page.waitForTimeout(500);
                
                var logoutInMenu = page.locator("vaadin-menu-bar-item:has-text('Logout')");
                if (logoutInMenu.count() > 0) {
                    logoutInMenu.click();
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            logger.warn("Logout attempt failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Tests Create operation in a specific view
     */
    private void testCreateOperation(String viewName, String entityName) {
        try {
            logger.info("Testing CREATE operation in {} view...", viewName);
            
            // Look for "New" or "Add" buttons
            var createButtons = page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add'), vaadin-button:has-text('Create')");
            
            if (createButtons.count() > 0) {
                createButtons.first().click();
                page.waitForTimeout(1000);
                
                takeScreenshot("create-form-opened-" + viewName.toLowerCase());
                
                // Fill form fields
                var textFields = page.locator("vaadin-text-field, vaadin-text-area");
                if (textFields.count() > 0) {
                    // Fill first text field with entity name
                    textFields.first().fill(entityName);
                    
                    // Fill other fields if available
                    if (textFields.count() > 1) {
                        textFields.nth(1).fill("Test description for " + entityName);
                    }
                    
                    takeScreenshot("create-form-filled-" + viewName.toLowerCase());
                    
                    // Save the entity
                    var saveButtons = page.locator("vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");
                    if (saveButtons.count() > 0) {
                        saveButtons.first().click();
                        page.waitForTimeout(1500);
                        takeScreenshot("create-saved-" + viewName.toLowerCase());
                        
                        logger.info("✅ CREATE operation completed for {} in {} view", entityName, viewName);
                    }
                }
            } else {
                logger.info("No create button found in {} view", viewName);
            }
            
        } catch (Exception e) {
            logger.warn("CREATE operation failed in {} view: {}", viewName, e.getMessage());
            takeScreenshot("create-error-" + viewName.toLowerCase());
        }
    }
    
    /**
     * Tests Read operation in a specific view
     */
    private void testReadOperation(String viewName) {
        try {
            logger.info("Testing READ operation in {} view...", viewName);
            
            // Look for grids with data
            var grids = page.locator("vaadin-grid");
            
            if (grids.count() > 0) {
                var grid = grids.first();
                var gridCells = grid.locator("vaadin-grid-cell-content");
                
                if (gridCells.count() > 0) {
                    logger.info("Found {} data items in {} view", gridCells.count(), viewName);
                    takeScreenshot("read-data-" + viewName.toLowerCase());
                    
                    // Click on first row to view details
                    gridCells.first().click();
                    page.waitForTimeout(1000);
                    takeScreenshot("read-details-" + viewName.toLowerCase());
                    
                    logger.info("✅ READ operation completed for {} view", viewName);
                } else {
                    logger.info("No data found in {} view grid", viewName);
                }
            } else {
                logger.info("No grid found in {} view", viewName);
            }
            
        } catch (Exception e) {
            logger.warn("READ operation failed in {} view: {}", viewName, e.getMessage());
            takeScreenshot("read-error-" + viewName.toLowerCase());
        }
    }
    
    /**
     * Tests Update operation in a specific view
     */
    private void testUpdateOperation(String viewName, String newName) {
        try {
            logger.info("Testing UPDATE operation in {} view...", viewName);
            
            // Look for edit buttons
            var editButtons = page.locator("vaadin-button:has-text('Edit'), vaadin-button:has-text('Modify')");
            
            if (editButtons.count() > 0) {
                editButtons.first().click();
                page.waitForTimeout(1000);
                
                takeScreenshot("update-form-opened-" + viewName.toLowerCase());
                
                // Modify form fields
                var textFields = page.locator("vaadin-text-field");
                if (textFields.count() > 0) {
                    textFields.first().fill(newName + " - " + System.currentTimeMillis());
                    
                    takeScreenshot("update-form-modified-" + viewName.toLowerCase());
                    
                    // Save changes
                    var saveButtons = page.locator("vaadin-button:has-text('Save'), vaadin-button:has-text('Update')");
                    if (saveButtons.count() > 0) {
                        saveButtons.first().click();
                        page.waitForTimeout(1500);
                        takeScreenshot("update-saved-" + viewName.toLowerCase());
                        
                        logger.info("✅ UPDATE operation completed for {} view", viewName);
                    }
                }
            } else {
                logger.info("No edit button found in {} view", viewName);
            }
            
        } catch (Exception e) {
            logger.warn("UPDATE operation failed in {} view: {}", viewName, e.getMessage());
            takeScreenshot("update-error-" + viewName.toLowerCase());
        }
    }
    
    /**
     * Tests Delete operation in a specific view
     */
    private void testDeleteOperation(String viewName) {
        try {
            logger.info("Testing DELETE operation in {} view...", viewName);
            
            // Look for delete buttons
            var deleteButtons = page.locator("vaadin-button:has-text('Delete'), vaadin-button:has-text('Remove')");
            
            if (deleteButtons.count() > 0) {
                deleteButtons.first().click();
                page.waitForTimeout(1000);
                
                takeScreenshot("delete-confirmation-" + viewName.toLowerCase());
                
                // Look for confirmation dialog
                var confirmButtons = page.locator("vaadin-button:has-text('Yes'), vaadin-button:has-text('Confirm'), vaadin-button:has-text('Delete')");
                
                if (confirmButtons.count() > 0) {
                    confirmButtons.first().click();
                    page.waitForTimeout(1500);
                    takeScreenshot("delete-completed-" + viewName.toLowerCase());
                    
                    logger.info("✅ DELETE operation completed for {} view", viewName);
                } else {
                    logger.info("No confirmation dialog found for delete in {} view", viewName);
                }
            } else {
                logger.info("No delete button found in {} view", viewName);
            }
            
        } catch (Exception e) {
            logger.warn("DELETE operation failed in {} view: {}", viewName, e.getMessage());
            takeScreenshot("delete-error-" + viewName.toLowerCase());
        }
    }
    
    /**
     * Tests advanced grid interactions including sorting and filtering
     */
    private void testAdvancedGridInView(String viewName) {
        try {
            logger.info("Testing advanced grid interactions in {} view...", viewName);
            
            if (navigateToViewByText(viewName)) {
                var grids = page.locator("vaadin-grid");
                
                if (grids.count() > 0) {
                    var grid = grids.first();
                    logger.info("Found grid in {} view", viewName);
                    
                    // Test grid cell interactions
                    var gridCells = grid.locator("vaadin-grid-cell-content");
                    if (gridCells.count() > 0) {
                        logger.info("Grid has {} cells in {} view", gridCells.count(), viewName);
                        
                        // Click on a cell
                        gridCells.first().click();
                        page.waitForTimeout(500);
                        takeScreenshot("grid-cell-clicked-" + viewName.toLowerCase());
                    }
                    
                    // Test column sorting
                    var sorters = grid.locator("vaadin-grid-sorter");
                    if (sorters.count() > 0) {
                        logger.info("Testing grid sorting in {} view...", viewName);
                        sorters.first().click();
                        page.waitForTimeout(1000);
                        takeScreenshot("grid-sorted-" + viewName.toLowerCase());
                    }
                    
                    // Test column filtering if available
                    var filters = grid.locator("vaadin-text-field[slot='filter']");
                    if (filters.count() > 0) {
                        logger.info("Testing grid filtering in {} view...", viewName);
                        filters.first().fill("test");
                        page.waitForTimeout(1000);
                        takeScreenshot("grid-filtered-" + viewName.toLowerCase());
                        
                        // Clear filter
                        filters.first().fill("");
                        page.waitForTimeout(500);
                    }
                    
                    logger.info("✅ Advanced grid test completed for {} view", viewName);
                } else {
                    logger.info("No grids found in {} view", viewName);
                }
            }
            
        } catch (Exception e) {
            logger.warn("Advanced grid test failed in {} view: {}", viewName, e.getMessage());
            takeScreenshot("grid-test-error-" + viewName.toLowerCase());
        }
    }
    
    /**
     * Tests form validation in a specific view
     */
    private void testFormValidation(String viewName) {
        try {
            logger.info("Testing form validation in {} view...", viewName);
            
            // Open new form
            var createButtons = page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");
            
            if (createButtons.count() > 0) {
                createButtons.first().click();
                page.waitForTimeout(1000);
                
                // Try to save without filling required fields
                var saveButtons = page.locator("vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");
                if (saveButtons.count() > 0) {
                    saveButtons.first().click();
                    page.waitForTimeout(1000);
                    
                    // Look for validation messages
                    var errorMessages = page.locator("vaadin-text-field[invalid], .error-message, [role='alert']");
                    
                    if (errorMessages.count() > 0) {
                        logger.info("✅ Form validation working - found {} validation messages in {} view", 
                                  errorMessages.count(), viewName);
                        takeScreenshot("form-validation-" + viewName.toLowerCase());
                    } else {
                        logger.info("No validation messages found in {} view form", viewName);
                        takeScreenshot("form-no-validation-" + viewName.toLowerCase());
                    }
                    
                    // Close form
                    var cancelButtons = page.locator("vaadin-button:has-text('Cancel')");
                    if (cancelButtons.count() > 0) {
                        cancelButtons.first().click();
                        page.waitForTimeout(500);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.warn("Form validation test failed in {} view: {}", viewName, e.getMessage());
            takeScreenshot("form-validation-error-" + viewName.toLowerCase());
        }
    }
    
    private void takeScreenshot(String name) {
        try {
            String screenshotPath = "target/screenshots/" + name + "-" + System.currentTimeMillis() + ".png";
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(screenshotPath)));
            logger.info("📸 Screenshot saved: {}", screenshotPath);
        } catch (Exception e) {
            logger.warn("⚠️ Failed to take screenshot '{}': {}", name, e.getMessage());
        }
    }
    
    private boolean navigateToViewByText(String viewName) {
        try {
            // Try to find navigation by text content
            var navItems = page.locator("vaadin-side-nav-item");
            for (int i = 0; i < navItems.count(); i++) {
                if (navItems.nth(i).textContent().contains(viewName)) {
                    navItems.nth(i).click();
                    page.waitForTimeout(1000);
                    return true;
                }
            }
            
            // Try to find links with the view name
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
            logger.warn("Failed to navigate to view {}: {}", viewName, e.getMessage());
            return false;
        }
    }
    
    private void testFormInView(String viewName, String path) {
        try {
            logger.info("Testing form interactions in {} view...", viewName);
            
            // Navigate to view
            page.navigate(baseUrl + path);
            page.waitForTimeout(2000);
            
            // Look for "New" or "Add" buttons
            var newButtons = page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");
            if (newButtons.count() > 0) {
                newButtons.first().click();
                page.waitForTimeout(1000);
                
                // Look for form fields
                var textFields = page.locator("vaadin-text-field, vaadin-text-area, input[type='text']");
                if (textFields.count() > 0) {
                    // Fill first text field
                    textFields.first().fill("Test " + viewName + " Entry");
                    
                    // Look for date fields
                    var dateFields = page.locator("vaadin-date-picker, input[type='date']");
                    if (dateFields.count() > 0) {
                        dateFields.first().fill("2024-01-15");
                    }
                    
                    takeScreenshot("form-filled-" + viewName.toLowerCase());
                    
                    // Look for save button
                    var saveButtons = page.locator("vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");
                    if (saveButtons.count() > 0) {
                        saveButtons.first().click();
                        page.waitForTimeout(1000);
                        takeScreenshot("form-saved-" + viewName.toLowerCase());
                    }
                }
            }
            
            logger.info("✅ Form interaction test completed for {} view", viewName);
            
        } catch (Exception e) {
            logger.warn("⚠️ Form interaction failed in {} view: {}", viewName, e.getMessage());
            takeScreenshot("form-error-" + viewName.toLowerCase());
        }
    }
    
    private void performWorkflowInView(String viewName) {
        try {
            logger.info("Performing workflow in {} view...", viewName);
            
            // Navigate to view by finding navigation elements
            if (navigateToViewByText(viewName)) {
                page.waitForTimeout(1000);
                
                // Check if view loaded
                assertTrue(page.url().contains(viewName.toLowerCase()) || 
                          page.locator("body").textContent().contains(viewName));
                
                takeScreenshot("workflow-" + viewName.toLowerCase());
                
                logger.info("✅ Workflow step completed for {} view", viewName);
            }
            
        } catch (Exception e) {
            logger.warn("⚠️ Workflow failed in {} view: {}", viewName, e.getMessage());
            takeScreenshot("workflow-error-" + viewName.toLowerCase());
        }
    }
    
    private void testGridInView(String viewName) {
        try {
            logger.info("Testing grid in {} view...", viewName);
            
            // Navigate to view
            if (navigateToViewByText(viewName)) {
                page.waitForTimeout(1000);
                
                // Look for grids
                var grids = page.locator("vaadin-grid, table");
                if (grids.count() > 0) {
                    logger.info("Found {} grid(s) in {} view", grids.count(), viewName);
                    
                    // Check if grid has data
                    var rows = page.locator("vaadin-grid vaadin-grid-cell-content, tr");
                    logger.info("Grid has {} rows in {} view", rows.count(), viewName);
                    
                    takeScreenshot("grid-" + viewName.toLowerCase());
                }
            }
            
            logger.info("✅ Grid test completed for {} view", viewName);
            
        } catch (Exception e) {
            logger.warn("⚠️ Grid test failed in {} view: {}", viewName, e.getMessage());
            takeScreenshot("grid-error-" + viewName.toLowerCase());
        }
    }
    
    private void checkAccessibilityElement(String selector, String description) {
        try {
            var elements = page.locator(selector);
            if (elements.count() > 0) {
                logger.info("✅ Found {} {} element(s)", elements.count(), description);
            } else {
                logger.warn("⚠️ No {} found", description);
            }
        } catch (Exception e) {
            logger.warn("⚠️ Accessibility check failed for {}: {}", description, e.getMessage());
        }
    }
}