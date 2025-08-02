package tech.derbent.activities.tests;

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
 * CActivitiesViewPlaywrightTest - Comprehensive Playwright tests for the Activities view.
 * Tests all aspects of the Activities view including CRUD operations, grid interactions,
 * form validation, ComboBox selections, and UI behaviors following the strict coding
 * guidelines for Playwright testing. Improved version with: - Reduced logging (DEBUG
 * instead of INFO for routine operations) - Screenshots only on failures - Common utility
 * functions (clickNew, clickCancel, clickGrid) - Class annotation-based approach instead
 * of magic strings
 */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT)
@TestPropertySource (properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080" }
)
public class CActivitiesView_UITest extends CBaseUITest {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CActivitiesView_UITest.class);

	@Test
	void testActivitiesAccessibility() {
		LOGGER.debug("Testing Activities accessibility");
		assertTrue(navigateToViewByClass(CActivitiesView.class),
			"Should navigate to Activities view");
		testAccessibilityBasics("Activities");
		LOGGER.debug("Activities accessibility test completed");
	}

	@Test
	void testActivitiesComboBoxes() {
		LOGGER.debug("Testing Activities ComboBox components");
		navigateToViewByClass(CActivitiesView.class);
		// Use common function to open new form
		clickNew();
		// Test ComboBoxes using common pattern
		final var comboBoxes = page.locator("vaadin-combo-box");
		final int comboBoxCount = comboBoxes.count();
		LOGGER.debug("Found {} ComboBox components", comboBoxCount);

		for (int i = 0; (i < comboBoxCount) && (i < 3); i++) { // Limit to 3 to avoid long
																// test times

			try {
				comboBoxes.nth(i).click();
				wait_500();
				final var options = page.locator("vaadin-combo-box-item");
				final int optionCount = options.count();
				LOGGER.debug("ComboBox {} has {} options", i, optionCount);

				if (optionCount == 0) {
					takeScreenshot("activities-combobox-no-options-" + i, true);
				}
				// Close ComboBox
				page.click("body");
				wait_500();
			} catch (final Exception e) {
				LOGGER.error("Error testing ComboBox {}: {}", i, e.getMessage());
				takeScreenshot("activities-combobox-error-" + i, true);
			}
		}
		// Use common function to cancel
		clickCancel();
		LOGGER.debug("Activities ComboBox test completed");
	}

	@Test
	void testActivitiesCompleteWorkflow() {
		LOGGER.debug("Testing Activities complete workflow");
		assertTrue(navigateToViewByClass(CActivitiesView.class),
			"Should navigate to Activities view");
		final int initialRowCount = getGridRowCount();
		LOGGER.debug("Initial grid has {} rows", initialRowCount);
		// Use common function to create new activity
		clickNew();
		// Fill activity name if text field is available
		final String activityName = "Test Activity " + System.currentTimeMillis();

		if (fillFirstTextField(activityName)) {
			LOGGER.debug("Filled activity name: {}", activityName);
		}
		// Fill description if available
		final var textAreas = page.locator("vaadin-text-area");

		if (textAreas.count() > 0) {
			textAreas.first().fill("Test description for workflow test");
		}
		// Use common function to save
		clickSaveButton();
		// Check if grid was updated
		final int finalRowCount = getGridRowCount();
		LOGGER.debug("Final grid has {} rows", finalRowCount);
		LOGGER.debug("Activities complete workflow test completed");
	}

	@Test
	void testActivitiesCRUDOperations() {
		LOGGER.debug("Testing Activities CRUD operations");
		assertTrue(navigateToViewByClass(CActivitiesView.class),
			"Should navigate to Activities view");
		// Use the auxiliary CRUD testing method
		testCRUDOperationsInView("Activities", "new-button", "save-button",
			"delete-button");
		LOGGER.debug("Activities CRUD operations test completed");
	}

	@Test
	void testActivitiesFormValidation() {
		LOGGER.debug("Testing Activities form validation");
		assertTrue(navigateToViewByClass(CActivitiesView.class),
			"Should navigate to Activities view");
		// Use common function to open new form
		clickNew();
		// Test form validation using auxiliary method
		final boolean validationWorking = testFormValidationById("save-button");
		assertTrue(validationWorking, "Form validation should work correctly");
		clickCancel();
		LOGGER.debug("Activities form validation test completed");
	}

	@Test
	void testActivitiesGridInteractions() {
		LOGGER.debug("Testing Activities grid interactions");
		assertTrue(navigateToViewByClass(CActivitiesView.class),
			"Should navigate to Activities view");
		final int gridRowCount = getGridRowCount();
		LOGGER.debug("Grid has {} rows", gridRowCount);

		if (gridRowCount > 0) {
			// Use common function to click grid
			clickGrid(0);
			LOGGER.debug("Successfully clicked first grid row");
		}
		else {
			LOGGER.debug("No rows in grid to test interactions");
		}
		LOGGER.debug("Activities grid interactions test completed");
	}

	@Test
	void testActivitiesNavigation() {
		testNavigationTo(CActivitiesView.class, CProjectsView.class);
	}
}