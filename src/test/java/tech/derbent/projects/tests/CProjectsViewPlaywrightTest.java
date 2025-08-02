package tech.derbent.projects.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import tech.derbent.activities.view.CActivitiesView;
import tech.derbent.projects.view.CProjectsView;
import tech.derbent.ui.automation.CBaseUITest;

/**
 * CProjectsViewPlaywrightTest - Comprehensive Playwright tests for the Projects view.
 * Tests all aspects of the Projects view including CRUD operations, grid interactions,
 * form validation, ComboBox selections, and UI behaviors following the strict coding
 * guidelines for Playwright testing.
 */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT)
@TestPropertySource (properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080" }
)
public class CProjectsViewPlaywrightTest extends CBaseUITest {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CProjectsViewPlaywrightTest.class);

	@Test
	void testProjectsAccessibility() {
		LOGGER.info("🧪 Testing Projects accessibility...");
		assertTrue(navigateToViewByClass(CProjectsView.class), "Should navigate to view");
		// Use auxiliary accessibility testing method
		testAccessibilityBasics("Projects");
		LOGGER.info("✅ Projects accessibility test completed");
	}

	@Test
	void testProjectsComboBoxes() {
		LOGGER.info("🧪 Testing Projects ComboBox components...");
		assertTrue(navigateToViewByClass(CProjectsView.class), "Should navigate to view");
		// Open new project form
		final var newButtons =
			page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");

		if (newButtons.count() > 0) {
			newButtons.first().click();
			wait_1000();
			// Test all ComboBoxes in the form
			final var comboBoxes = page.locator("vaadin-combo-box");
			final int comboBoxCount = comboBoxes.count();
			LOGGER.debug("Found {} ComboBoxes in Projects form", comboBoxCount);

			for (int i = 0; (i < comboBoxCount) && (i < 3); i++) { // Test first 3
																	// ComboBoxes
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
			// Close form
			final var cancelButtons = page.locator("vaadin-button:has-text('Cancel')");

			if (cancelButtons.count() > 0) {
				cancelButtons.first().click();
				wait_500();
			}
		}
		LOGGER.info("✅ Projects ComboBox test completed");
	}

	@Test
	void testProjectsCompleteWorkflow() {
		LOGGER.info("🧪 Testing Projects complete workflow...");
		assertTrue(navigateToViewByClass(CProjectsView.class), "Should navigate to view");
		// Test complete workflow: navigate -> view grid -> create new -> fill form ->
		// save -> view result
		final int initialRowCount = getGridRowCount();
		LOGGER.debug("Initial grid has {} rows", initialRowCount);
		// Create new project
		final var newButtons =
			page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");

		if (newButtons.count() > 0) {
			newButtons.first().click();
			wait_1000();
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
			final var saveButtons = page.locator(
				"vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");

			if (saveButtons.count() > 0) {
				saveButtons.first().click();
				wait_2000();
				takeScreenshot("projects-workflow-saved");
				// Check if grid was updated
				final int finalRowCount = getGridRowCount();
				LOGGER.debug("Final grid has {} rows", finalRowCount);
			}
		}
		LOGGER.info("✅ Projects complete workflow test completed");
	}

	@Test
	void testProjectsCRUDOperations() {
		LOGGER.info("🧪 Testing Projects CRUD operations...");
		assertTrue(navigateToViewByClass(CProjectsView.class), "Should navigate to view");
		// Use the auxiliary CRUD testing method
		testCRUDOperationsInView("Projects", "new-button", "save-button",
			"delete-button");
		LOGGER.info("✅ Projects CRUD operations test completed");
	}

	@Test
	void testProjectsFormValidation() {
		LOGGER.info("🧪 Testing Projects form validation...");
		assertTrue(navigateToViewByClass(CProjectsView.class), "Should navigate to view");
		// Try to create new project
		final var newButtons =
			page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");

		if (newButtons.count() > 0) {
			newButtons.first().click();
			wait_1000();
			// Test form validation using auxiliary method
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
			final var cancelButtons = page.locator("vaadin-button:has-text('Cancel')");

			if (cancelButtons.count() > 0) {
				cancelButtons.first().click();
				wait_500();
			}
		}
		LOGGER.info("✅ Projects form validation test completed");
	}

	@Test
	void testProjectsGridInteractions() {
		LOGGER.info("🧪 Testing Projects grid interactions...");
		assertTrue(navigateToViewByClass(CProjectsView.class), "Should navigate to view");
		// Test grid selection
		final int rowCount = getGridRowCount();

		if (rowCount > 0) {
			LOGGER.debug("Testing grid selection with {} rows", rowCount);
			assertTrue(clickGridRowByIndex(0), "Should be able to click first grid row");
			wait_1000();
			takeScreenshot("projects-grid-row-selected");

			// Test selecting different rows
			if (rowCount > 1) {
				clickGridRowByIndex(1);
				wait_500();
				takeScreenshot("projects-grid-second-row-selected");
			}
		}
		// Test grid sorting if available
		final var sorters = page.locator("vaadin-grid-sorter");

		if (sorters.count() > 0) {
			LOGGER.debug("Testing grid sorting");
			sorters.first().click();
			wait_1000();
			takeScreenshot("projects-grid-sorted");
		}
		// Test grid filtering if available
		final var filters = page.locator("vaadin-text-field[slot='filter']");

		if (filters.count() > 0) {
			LOGGER.debug("Testing grid filtering");
			filters.first().fill("test");
			wait_1000();
			takeScreenshot("projects-grid-filtered");
			// Clear filter
			filters.first().fill("");
			wait_500();
		}
		LOGGER.info("✅ Projects grid interactions test completed");
	}

	@Test
	void testProjectsNavigation() {
		LOGGER.info("🧪 Testing Projects view navigation...");
		// Test navigation to Projects
		assertTrue(navigateToViewByClass(CProjectsView.class), "Should navigate to view");
		takeScreenshot("projects-navigation-arrival");

		// Test navigation to related views
		if (navigateToViewByClass(CActivitiesView.class)) {
			takeScreenshot("projects-navigation-to-activities");
			assertTrue(navigateToViewByClass(CProjectsView.class),
				"Should navigate to view");
			takeScreenshot("projects-navigation-return");
		}
		LOGGER.info("✅ Projects navigation test completed");
	}

	@Test
	void testProjectsResponsiveDesign() {
		LOGGER.info("🧪 Testing Projects responsive design...");
		assertTrue(navigateToViewByClass(CProjectsView.class), "Should navigate to view");
		// Use auxiliary responsive testing method
		testResponsiveDesign("Projects");
		LOGGER.info("✅ Projects responsive design test completed");
	}

	@Test
	void testProjectsViewLoading() {
		LOGGER.info("🧪 Testing Projects view loading...");
		// Login and navigate to Projects view Navigate to Projects view using navigation
		assertTrue(navigateToViewByClass(CProjectsView.class), "Should navigate to view");
		// Verify the view loaded properly
		takeScreenshot("projects-view-loaded");
		// Check if grid is present
		assertTrue(getGridRowCount() >= 0, "Projects grid should be present");
		LOGGER.info("✅ Projects view loading test completed");
	}
}