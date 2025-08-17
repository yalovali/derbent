package ui_tests.tech.derbent.projects.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.derbent.meetings.view.CMeetingsView;
import tech.derbent.projects.view.CProjectsView;
import ui_tests.tech.derbent.ui.automation.CBaseUITest;

/**
 * CProjectsViewPlaywrightTest - Comprehensive Playwright tests for the Projects view. Tests all aspects of the Projects
 * view including CRUD operations, grid interactions, form validation, ComboBox selections, and UI behaviors following
 * the strict coding guidelines for Playwright testing.
 */
public class CProjectsView_UITest extends CBaseUITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CProjectsView_UITest.class);

    @Test
    void testProjectsComboBoxes() {
        LOGGER.info("🧪 Testing Projects ComboBox components...");
        navigateToViewByClass(CProjectsView.class);
        clickNew(); // Test all ComboBoxes in the form
        final var comboBoxes = page.locator("vaadin-combo-box");
        final int comboBoxCount = comboBoxes.count();
        LOGGER.debug("Found {} ComboBoxes in Projects form", comboBoxCount);

        for (int i = 0; (i < comboBoxCount) && (i < 3); i++) { // Test first 3 ComboBoxes
            LOGGER.debug("Testing ComboBox {}", i + 1);
            comboBoxes.nth(i).click();
            wait_500();
            // Check options are available
            final var options = page.locator("vaadin-combo-box-item");
            final int optionCount = options.count();
            LOGGER.debug("ComboBox {} has {} options", i + 1, optionCount);

            if (optionCount > 0) {
                // Select first option
                options.first().click();
                wait_500();
            }
            takeScreenshot("projects-combobox-" + (i + 1));
        }
        clickCancel(); // Close form
        LOGGER.info("✅ Projects ComboBox test completed");
    }

    @Test
    void testProjectsCompleteWorkflow() {
        LOGGER.info("🧪 Testing Projects complete workflow...");
        navigateToViewByClass(CProjectsView.class);
        clickNew(); // Open new project form
        takeScreenshot("projects-workflow-new-form");
        // Fill project name
        final String projectName = "Test Project " + System.currentTimeMillis();

        if (fillFirstTextField(projectName)) {
            LOGGER.debug("Filled project name: {}", projectName);
        }
        // Fill description if available
        final var textAreas = page.locator("vaadin-text-area");

        if (textAreas.count() > 0) {
            textAreas.first().fill("Test description for workflow test");
        }
        // Set dates if available
        final var datePickers = page.locator("vaadin-date-picker");

        if (datePickers.count() > 0) {
            // Set start date
            datePickers.first().fill("2024-01-01");

            if (datePickers.count() > 1) {
                // Set end date
                datePickers.nth(1).fill("2024-12-31");
            }
        }
        takeScreenshot("projects-workflow-form-filled");
        // Save
        clickSave();
        LOGGER.info("✅ Projects complete workflow test completed");
    }

    @Test
    void testProjectsCRUDOperations() {
        LOGGER.info("🧪 Testing Projects CRUD operations...");
        navigateToViewByClass(CProjectsView.class);
        // Use the auxiliary CRUD testing method
        testCRUDOperationsInView("Projects", "new-button", "save-button", "delete-button");
        LOGGER.info("✅ Projects CRUD operations test completed");
    }

    @Test
    void testProjectsEntityRelationGrid() {
        LOGGER.info("🧪 Testing Projects entity relation grid display...");
        // Test that project relations are displayed in grid
        testEntityRelationGrid(CProjectsView.class);
        LOGGER.info("✅ Projects entity relation grid test completed");
    }

    @Test
    void testProjectsFormValidation() {
        LOGGER.info("🧪 Testing Projects form validation...");
        navigateToViewByClass(CProjectsView.class);
        clickNew(); // Open new project form
        final boolean validationWorking = testFormValidationById("save-button");
        LOGGER.debug("Form validation working: {}", validationWorking);
        takeScreenshot("projects-form-validation");
        // Test specific field validation
        final var nameField = page.locator("vaadin-text-field").first();

        if (nameField.count() > 0) {
            // Fill and clear to trigger validation
            nameField.fill("Test");
            nameField.fill("");
            wait_500();
            takeScreenshot("projects-field-validation");
        }
        // Close form
        clickCancel();
        LOGGER.info("✅ Projects form validation test completed");
    }

    @Test
    void testProjectsGridInteractions() {
        LOGGER.info("🧪 Testing Projects grid interactions...");
        testAdvancedGridInView(CProjectsView.class);
        // Additional grid interaction testing for projects
        navigateToViewByClass(CProjectsView.class);
        // Test grid selection changes
        final int gridRowCount = getGridRowCount();

        if (gridRowCount > 0) {
            LOGGER.debug("Testing grid selection with {} rows", gridRowCount);
            clickGrid(0); // Select first row
            wait_500();
            // Test that selection triggers form population
            final var textFields = page.locator("vaadin-text-field");

            if (textFields.count() > 0) {
                final String firstFieldValue = textFields.first().inputValue();
                LOGGER.debug("First field populated with: {}", firstFieldValue);
                assertTrue(firstFieldValue != null && !firstFieldValue.trim().isEmpty(),
                        "Grid selection should populate form fields");
            }
        }
        LOGGER.info("✅ Projects grid interactions test completed");
    }

    @Test
    void testProjectsNavigation() {
        testNavigationTo(CProjectsView.class, CMeetingsView.class);
    }

    @Test
    void testProjectsSearchFunctionality() {
        LOGGER.info("🧪 Testing Projects search functionality...");
        // Test search with common project fields
        testSearchFunctionality(CProjectsView.class, "Test");
        LOGGER.info("✅ Projects search functionality test completed");
    }
}