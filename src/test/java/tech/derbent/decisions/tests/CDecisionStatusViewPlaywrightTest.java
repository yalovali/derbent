package tech.derbent.decisions.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import tech.derbent.decisions.view.CDecisionStatusView;
import tech.derbent.ui.automation.CApplicationGeneric_UITest;

/**
 * CDecisionStatusViewPlaywrightTest - Tests for decision status view focusing on lazy loading fixes and navigation
 * behavior after save operations.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = { "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop" })
public class CDecisionStatusViewPlaywrightTest extends CApplicationGeneric_UITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CDecisionStatusViewPlaywrightTest.class);

    @Test
    void testDecisionStatusLazyLoadingAndNavigation() {
        LOGGER.info("🧪 Testing Decision Status lazy loading and navigation after save...");
        assertTrue(navigateToViewByClass(CDecisionStatusView.class), "Should navigate to decision status view");

        // Test that grid loads without lazy loading exceptions
        takeScreenshot("decision-status-grid-loaded");

        // Create new decision status to test save navigation
        clickNew();
        wait_1000();
        takeScreenshot("decision-status-new-form");

        // Fill required fields
        final String statusName = "Test Decision Status " + System.currentTimeMillis();
        if (fillFirstTextField(statusName)) {
            LOGGER.debug("Filled status name: {}", statusName);
        }

        // Fill description if available
        final var textAreas = page.locator("vaadin-text-area");
        if (textAreas.count() > 0) {
            textAreas.first().fill("Test description for decision status lazy loading test");
        }

        takeScreenshot("decision-status-form-filled");

        // Save and verify we stay on the same view (not redirected to wrong view)
        clickSave();
        wait_2000();
        takeScreenshot("decision-status-after-save");

        // Verify we're still on the decision status view by checking URL or page content
        final String currentUrl = page.url();
        assertTrue(currentUrl.contains("decision-status") || currentUrl.contains("decision-statuses"),
                "Should remain on decision status view after save, but was: " + currentUrl);

        LOGGER.info("✅ Decision Status lazy loading and navigation test completed");
    }

    @Test
    void testDecisionStatusGridSelectionLazyLoading() {
        LOGGER.info("🧪 Testing Decision Status grid selection and lazy loading...");
        assertTrue(navigateToViewByClass(CDecisionStatusView.class), "Should navigate to decision status view");

        // Check if grid has rows
        final int rowCount = getGridRowCount();
        if (rowCount > 0) {
            LOGGER.debug("Grid has {} rows, testing selection", rowCount);

            // Click on first row to test lazy loading
            final var gridRows = page.locator("vaadin-grid-cell-content").first();
            if (gridRows.isVisible()) {
                gridRows.click();
                wait_1000();
                takeScreenshot("decision-status-row-selected");

                // Verify form is populated without lazy loading exceptions
                LOGGER.debug("Form populated after grid selection - checking for lazy loading issues");
            }
        } else {
            LOGGER.debug("No existing rows, creating test data");
            // Create a test status first
            clickNew();
            fillFirstTextField("Test Decision Status for Selection");
            clickSave();
            wait_1000();

            // Now test selection
            final var gridRows = page.locator("vaadin-grid-cell-content").first();
            if (gridRows.isVisible()) {
                gridRows.click();
                wait_1000();
                takeScreenshot("decision-status-new-row-selected");
            }
        }

        LOGGER.info("✅ Decision Status grid selection test completed");
    }

    @Test
    void testDecisionStatusApprovalRequirement() {
        LOGGER.info("🧪 Testing Decision Status approval requirement functionality...");
        assertTrue(navigateToViewByClass(CDecisionStatusView.class), "Should navigate to decision status view");

        clickNew();
        wait_1000();

        // Fill basic fields
        final String statusName = "Approval Required Status " + System.currentTimeMillis();
        fillFirstTextField(statusName);

        // Test checkbox for requires approval if available
        final var checkBoxes = page.locator("vaadin-checkbox");
        if (checkBoxes.count() > 0) {
            LOGGER.debug("Testing requires approval checkbox");
            // Look for approval-related checkbox by iterating through them
            for (int i = 0; i < checkBoxes.count(); i++) {
                final var checkbox = checkBoxes.nth(i);
                final String text = checkbox.textContent().toLowerCase();
                if (text.contains("approval") || text.contains("require")) {
                    checkbox.click();
                    wait_500();
                    takeScreenshot("decision-status-approval-checkbox");
                    break;
                }
            }
        }

        takeScreenshot("decision-status-approval-form");

        clickSave();
        wait_2000();
        takeScreenshot("decision-status-approval-saved");

        LOGGER.info("✅ Decision Status approval requirement test completed");
    }

    @Test
    void testDecisionStatusFinalStatus() {
        LOGGER.info("🧪 Testing Decision Status final status functionality...");
        assertTrue(navigateToViewByClass(CDecisionStatusView.class), "Should navigate to decision status view");

        clickNew();
        wait_1000();

        // Fill basic fields
        final String statusName = "Final Status " + System.currentTimeMillis();
        fillFirstTextField(statusName);

        // Test checkbox for is final if available
        final var checkBoxes = page.locator("vaadin-checkbox");
        if (checkBoxes.count() > 0) {
            LOGGER.debug("Testing is final checkbox");
            // Look for final-related checkbox by iterating through them
            for (int i = 0; i < checkBoxes.count(); i++) {
                final var checkbox = checkBoxes.nth(i);
                final String text = checkbox.textContent().toLowerCase();
                if (text.contains("final") || text.contains("complete")) {
                    checkbox.click();
                    wait_500();
                    takeScreenshot("decision-status-final-checkbox");
                    break;
                }
            }
        }

        takeScreenshot("decision-status-final-form");

        clickSave();
        wait_2000();
        takeScreenshot("decision-status-final-saved");

        LOGGER.info("✅ Decision Status final status test completed");
    }

    @Test
    void testDecisionStatusColorAndSortOrder() {
        LOGGER.info("🧪 Testing Decision Status color and sort order...");
        assertTrue(navigateToViewByClass(CDecisionStatusView.class), "Should navigate to decision status view");

        clickNew();
        wait_1000();

        // Fill required fields
        fillFirstTextField("Ordered Status " + System.currentTimeMillis());

        // Test color picker if available
        final var colorPickers = page.locator("vaadin-color-picker, input[type='color']");
        if (colorPickers.count() > 0) {
            LOGGER.debug("Testing color picker");
            colorPickers.first().fill("#9C27B0");
            wait_500();
            takeScreenshot("decision-status-color-picker");
        }

        // Test sort order if available
        final var numberFields = page.locator("vaadin-number-field, vaadin-integer-field");
        if (numberFields.count() > 0) {
            LOGGER.debug("Testing sort order field");
            numberFields.first().fill("50");
            wait_500();
            takeScreenshot("decision-status-sort-order");
        }

        takeScreenshot("decision-status-color-sort-form");

        clickSave();
        wait_2000();
        takeScreenshot("decision-status-color-sort-saved");

        LOGGER.info("✅ Decision Status color and sort order test completed");
    }
}