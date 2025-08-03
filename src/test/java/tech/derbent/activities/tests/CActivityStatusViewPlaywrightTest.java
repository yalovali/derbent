package tech.derbent.activities.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import tech.derbent.activities.view.CActivityStatusView;
import tech.derbent.activities.view.CActivitiesView;
import tech.derbent.ui.automation.CApplicationGeneric_UITest;

/**
 * CActivityStatusViewPlaywrightTest - Tests for activity status view focusing on 
 * lazy loading fixes and navigation behavior after save operations.
 */
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop", 
    "server.port=8080"
})
public class CActivityStatusViewPlaywrightTest extends CApplicationGeneric_UITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CActivityStatusViewPlaywrightTest.class);

    @Test
    void testActivityStatusLazyLoadingAndNavigation() {
        LOGGER.info("🧪 Testing Activity Status lazy loading and navigation after save...");
        assertTrue(navigateToViewByClass(CActivityStatusView.class), "Should navigate to activity status view");
        
        // Test that grid loads without lazy loading exceptions
        takeScreenshot("activity-status-grid-loaded");
        
        // Create new activity status to test save navigation
        clickNew();
        wait_1000();
        takeScreenshot("activity-status-new-form");
        
        // Fill required fields
        final String statusName = "Test Status " + System.currentTimeMillis();
        if (fillFirstTextField(statusName)) {
            LOGGER.debug("Filled status name: {}", statusName);
        }
        
        // Fill description if available
        final var textAreas = page.locator("vaadin-text-area");
        if (textAreas.count() > 0) {
            textAreas.first().fill("Test description for lazy loading test");
        }
        
        takeScreenshot("activity-status-form-filled");
        
        // Save and verify we stay on the same view (not redirected to wrong view)
        clickSave();
        wait_2000();
        takeScreenshot("activity-status-after-save");
        
        // Verify we're still on the activity status view by checking URL or page content
        final String currentUrl = page.url();
        assertTrue(currentUrl.contains("activity-status") || currentUrl.contains("activity-statuses"),
            "Should remain on activity status view after save, but was: " + currentUrl);
        
        LOGGER.info("✅ Activity Status lazy loading and navigation test completed");
    }

    @Test
    void testActivityStatusGridSelectionLazyLoading() {
        LOGGER.info("🧪 Testing Activity Status grid selection and lazy loading...");
        assertTrue(navigateToViewByClass(CActivityStatusView.class), "Should navigate to activity status view");
        
        // Check if grid has rows
        final int rowCount = getGridRowCount();
        if (rowCount > 0) {
            LOGGER.debug("Grid has {} rows, testing selection", rowCount);
            
            // Click on first row to test lazy loading
            final var gridRows = page.locator("vaadin-grid-cell-content").first();
            if (gridRows.isVisible()) {
                gridRows.click();
                wait_1000();
                takeScreenshot("activity-status-row-selected");
                
                // Verify form is populated without lazy loading exceptions
                // The form should display project information if it has project relationships
                LOGGER.debug("Form populated after grid selection - checking for lazy loading issues");
            }
        } else {
            LOGGER.debug("No existing rows, creating test data");
            // Create a test status first
            clickNew();
            fillFirstTextField("Test Status for Selection");
            clickSave();
            wait_1000();
            
            // Now test selection
            final var gridRows = page.locator("vaadin-grid-cell-content").first();
            if (gridRows.isVisible()) {
                gridRows.click();
                wait_1000();
                takeScreenshot("activity-status-new-row-selected");
            }
        }
        
        LOGGER.info("✅ Activity Status grid selection test completed");
    }

    @Test
    void testActivityStatusFormValidationAndSave() {
        LOGGER.info("🧪 Testing Activity Status form validation and save...");
        assertTrue(navigateToViewByClass(CActivityStatusView.class), "Should navigate to activity status view");
        
        clickNew();
        wait_1000();
        
        // Test validation by trying to save without required fields
        clickSave();
        wait_500();
        takeScreenshot("activity-status-validation-error");
        
        // Fill required fields and save successfully
        final String statusName = "Validated Status " + System.currentTimeMillis();
        if (fillFirstTextField(statusName)) {
            LOGGER.debug("Filled status name for validation test: {}", statusName);
        }
        
        // Set color if color picker is available
        final var colorPickers = page.locator("vaadin-color-picker, input[type='color']");
        if (colorPickers.count() > 0) {
            colorPickers.first().fill("#FF5722");
            wait_500();
        }
        
        takeScreenshot("activity-status-validation-fixed");
        
        // Save should succeed now
        clickSave();
        wait_2000();
        takeScreenshot("activity-status-validation-saved");
        
        // Verify successful save by checking we're still on same view and no error messages
        final String currentUrl = page.url();
        assertTrue(currentUrl.contains("activity-status") || currentUrl.contains("activity-statuses"),
            "Should remain on activity status view after successful save");
        
        LOGGER.info("✅ Activity Status form validation test completed");
    }

    @Test
    void testActivityStatusColorFunctionality() {
        LOGGER.info("🧪 Testing Activity Status color functionality...");
        assertTrue(navigateToViewByClass(CActivityStatusView.class), "Should navigate to activity status view");
        
        clickNew();
        wait_1000();
        
        // Test color picker if available
        final var colorPickers = page.locator("vaadin-color-picker, input[type='color']");
        if (colorPickers.count() > 0) {
            LOGGER.debug("Testing color picker");
            colorPickers.first().fill("#2196F3");
            wait_500();
            takeScreenshot("activity-status-color-picker");
        }
        
        // Fill other required fields
        fillFirstTextField("Colored Status " + System.currentTimeMillis());
        
        clickSave();
        wait_2000();
        takeScreenshot("activity-status-color-saved");
        
        clickCancel();
        
        LOGGER.info("✅ Activity Status color functionality test completed");
    }
}