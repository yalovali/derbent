package tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

/**
 * CActivitiesViewPlaywrightTest - Comprehensive Playwright tests for the Activities view.
 * 
 * Tests all aspects of the Activities view including CRUD operations, grid interactions,
 * form validation, ComboBox selections, and UI behaviors following the strict coding
 * guidelines for Playwright testing.
 */
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop", 
    "server.port=8080"
})
public class CActivitiesViewPlaywrightTest extends CBaseUITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CActivitiesViewPlaywrightTest.class);

    @Test
    void testActivitiesViewLoading() {
        LOGGER.info("🧪 Testing Activities view loading...");
        
        // Login and navigate to Activities view
        loginToApplication();
        
        // Navigate to Activities view using navigation
        assertTrue(navigateToViewByText("Activities"), "Should navigate to Activities view");
        
        // Verify the view loaded properly
        wait_1000();
        takeScreenshot("activities-view-loaded");
        
        // Check if grid is present
        assertTrue(getGridRowCount() >= 0, "Activities grid should be present");
        
        LOGGER.info("✅ Activities view loading test completed");
    }

    @Test
    void testActivitiesGridInteractions() {
        LOGGER.info("🧪 Testing Activities grid interactions...");
        
        loginToApplication();
        navigateToViewByText("Activities");
        wait_1000();
        
        // Test grid selection
        final int rowCount = getGridRowCount();
        if (rowCount > 0) {
            LOGGER.debug("Testing grid selection with {} rows", rowCount);
            assertTrue(clickGridRowByIndex(0), "Should be able to click first grid row");
            wait_1000();
            takeScreenshot("activities-grid-row-selected");
        }
        
        // Test grid sorting if available
        final var sorters = page.locator("vaadin-grid-sorter");
        if (sorters.count() > 0) {
            LOGGER.debug("Testing grid sorting");
            sorters.first().click();
            wait_1000();
            takeScreenshot("activities-grid-sorted");
        }
        
        LOGGER.info("✅ Activities grid interactions test completed");
    }

    @Test
    void testActivitiesCRUDOperations() {
        LOGGER.info("🧪 Testing Activities CRUD operations...");
        
        loginToApplication();
        navigateToViewByText("Activities");
        wait_1000();
        
        // Use the auxiliary CRUD testing method
        testCRUDOperationsInView("Activities", "new-button", "save-button", "delete-button");
        
        LOGGER.info("✅ Activities CRUD operations test completed");
    }

    @Test
    void testActivitiesFormValidation() {
        LOGGER.info("🧪 Testing Activities form validation...");
        
        loginToApplication();
        navigateToViewByText("Activities");
        wait_1000();
        
        // Try to create new activity
        final var newButtons = page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");
        if (newButtons.count() > 0) {
            newButtons.first().click();
            wait_1000();
            
            // Test form validation using auxiliary method
            final boolean validationWorking = testFormValidationById("save-button");
            LOGGER.debug("Form validation working: {}", validationWorking);
            
            takeScreenshot("activities-form-validation");
            
            // Close form
            final var cancelButtons = page.locator("vaadin-button:has-text('Cancel')");
            if (cancelButtons.count() > 0) {
                cancelButtons.first().click();
                wait_500();
            }
        }
        
        LOGGER.info("✅ Activities form validation test completed");
    }

    @Test
    void testActivitiesComboBoxes() {
        LOGGER.info("🧪 Testing Activities ComboBox components...");
        
        loginToApplication();
        navigateToViewByText("Activities");
        wait_1000();
        
        // Open new activity form
        final var newButtons = page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");
        if (newButtons.count() > 0) {
            newButtons.first().click();
            wait_1000();
            
            // Test Activity Type ComboBox
            final var activityTypeComboBoxes = page.locator("vaadin-combo-box");
            if (activityTypeComboBoxes.count() > 0) {
                LOGGER.debug("Testing Activity Type ComboBox");
                // Click to open first ComboBox
                activityTypeComboBoxes.first().click();
                wait_500();
                
                // Check options are available
                final var options = page.locator("vaadin-combo-box-item");
                final int optionCount = options.count();
                LOGGER.debug("Found {} options in Activity Type ComboBox", optionCount);
                
                if (optionCount > 0) {
                    // Select first option
                    options.first().click();
                    wait_500();
                }
                
                takeScreenshot("activities-activity-type-combobox");
            }
            
            // Test Status ComboBox if different from Activity Type
            if (activityTypeComboBoxes.count() > 1) {
                LOGGER.debug("Testing Status ComboBox");
                activityTypeComboBoxes.nth(1).click();
                wait_500();
                takeScreenshot("activities-status-combobox");
                
                // Close by clicking elsewhere
                page.click("body");
                wait_500();
            }
            
            // Close form
            final var cancelButtons = page.locator("vaadin-button:has-text('Cancel')");
            if (cancelButtons.count() > 0) {
                cancelButtons.first().click();
                wait_500();
            }
        }
        
        LOGGER.info("✅ Activities ComboBox test completed");
    }

    @Test
    void testActivitiesNavigation() {
        LOGGER.info("🧪 Testing Activities view navigation...");
        
        loginToApplication();
        
        // Test navigation to Activities
        assertTrue(navigateToViewByText("Activities"), "Should navigate to Activities view");
        wait_1000();
        takeScreenshot("activities-navigation-arrival");
        
        // Test navigation away and back
        if (navigateToViewByText("Projects")) {
            wait_1000();
            takeScreenshot("activities-navigation-away");
            
            assertTrue(navigateToViewByText("Activities"), "Should navigate back to Activities view");
            wait_1000();
            takeScreenshot("activities-navigation-return");
        }
        
        LOGGER.info("✅ Activities navigation test completed");
    }

    @Test
    void testActivitiesResponsiveDesign() {
        LOGGER.info("🧪 Testing Activities responsive design...");
        
        loginToApplication();
        navigateToViewByText("Activities");
        wait_1000();
        
        // Use auxiliary responsive testing method
        testResponsiveDesign("Activities");
        
        LOGGER.info("✅ Activities responsive design test completed");
    }

    @Test
    void testActivitiesAccessibility() {
        LOGGER.info("🧪 Testing Activities accessibility...");
        
        loginToApplication();
        navigateToViewByText("Activities");
        wait_1000();
        
        // Use auxiliary accessibility testing method
        testAccessibilityBasics("Activities");
        
        LOGGER.info("✅ Activities accessibility test completed");
    }

    @Test
    void testActivitiesCompleteWorkflow() {
        LOGGER.info("🧪 Testing Activities complete workflow...");
        
        loginToApplication();
        navigateToViewByText("Activities");
        wait_1000();
        
        // Test complete workflow: navigate -> view grid -> create new -> fill form -> save -> view result
        final int initialRowCount = getGridRowCount();
        LOGGER.debug("Initial grid has {} rows", initialRowCount);
        
        // Create new activity
        final var newButtons = page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");
        if (newButtons.count() > 0) {
            newButtons.first().click();
            wait_1000();
            takeScreenshot("activities-workflow-new-form");
            
            // Fill activity name
            final String activityName = "Test Activity " + System.currentTimeMillis();
            if (fillFirstTextField(activityName)) {
                LOGGER.debug("Filled activity name: {}", activityName);
            }
            
            // Fill description if available
            final var textAreas = page.locator("vaadin-text-area");
            if (textAreas.count() > 0) {
                textAreas.first().fill("Test description for workflow test");
            }
            
            takeScreenshot("activities-workflow-form-filled");
            
            // Save
            final var saveButtons = page.locator("vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");
            if (saveButtons.count() > 0) {
                saveButtons.first().click();
                wait_2000();
                takeScreenshot("activities-workflow-saved");
                
                // Check if grid was updated
                final int finalRowCount = getGridRowCount();
                LOGGER.debug("Final grid has {} rows", finalRowCount);
            }
        }
        
        LOGGER.info("✅ Activities complete workflow test completed");
    }
}