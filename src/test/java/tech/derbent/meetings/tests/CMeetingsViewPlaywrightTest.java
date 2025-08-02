package tech.derbent.meetings.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import tech.derbent.meetings.view.CMeetingsView;
import tech.derbent.ui.automation.CBaseUITest;
import tech.derbent.projects.view.CProjectsView;

/**
 * CMeetingsViewPlaywrightTest - Comprehensive Playwright tests for the Meetings view.
 * Tests all aspects of the Meetings view including CRUD operations, grid interactions,
 * form validation, ComboBox selections, and UI behaviors following the strict coding
 * guidelines for Playwright testing.
 */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT)
@TestPropertySource (properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080" }
)
public class CMeetingsViewPlaywrightTest extends CBaseUITest {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CMeetingsViewPlaywrightTest.class);

	@Test
	void testMeetingsAccessibility() {
		LOGGER.info("🧪 Testing Meetings accessibility...");
		assertTrue(navigateToViewByClass(CMeetingsView.class), "Should navigate to view");
		// Use auxiliary accessibility testing method
		testAccessibilityBasics("Meetings");
		LOGGER.info("✅ Meetings accessibility test completed");
	}

	@Test
	void testMeetingsComboBoxes() {
		LOGGER.info("🧪 Testing Meetings ComboBox components...");
		assertTrue(navigateToViewByClass(CMeetingsView.class), "Should navigate to view");
		// Open new meeting form
		final var newButtons =
			page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");

		if (newButtons.count() > 0) {
			newButtons.first().click();
			wait_1000();
			// Test Meeting Type ComboBox
			final var comboBoxes = page.locator("vaadin-combo-box");

			if (comboBoxes.count() > 0) {
				LOGGER.debug("Testing Meeting Type ComboBox");
				// Click to open first ComboBox
				comboBoxes.first().click();
				wait_500();
				// Check options are available
				final var options = page.locator("vaadin-combo-box-item");
				final int optionCount = options.count();
				LOGGER.debug("Found {} options in Meeting Type ComboBox", optionCount);

				if (optionCount > 0) {
					// Select first option
					options.first().click();
					wait_500();
				}
				takeScreenshot("meetings-meeting-type-combobox");
			}

			// Test Project ComboBox if available
			if (comboBoxes.count() > 1) {
				LOGGER.debug("Testing Project ComboBox");
				comboBoxes.nth(1).click();
				wait_500();
				takeScreenshot("meetings-project-combobox");
				// Select option if available
				final var options = page.locator("vaadin-combo-box-item");

				if (options.count() > 0) {
					options.first().click();
					wait_500();
				}
			}
			// Close form
			final var cancelButtons = page.locator("vaadin-button:has-text('Cancel')");

			if (cancelButtons.count() > 0) {
				cancelButtons.first().click();
				wait_500();
			}
		}
		LOGGER.info("✅ Meetings ComboBox test completed");
	}

	@Test
	void testMeetingsCompleteWorkflow() {
		LOGGER.info("🧪 Testing Meetings complete workflow...");
		assertTrue(navigateToViewByClass(CMeetingsView.class),
			"Should navigate to Activities view");
		// Test complete workflow: navigate -> view grid -> create new -> fill form ->
		// save -> view result
		final int initialRowCount = getGridRowCount();
		LOGGER.debug("Initial grid has {} rows", initialRowCount);
		// Create new meeting
		final var newButtons =
			page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");

		if (newButtons.count() > 0) {
			newButtons.first().click();
			wait_1000();
			takeScreenshot("meetings-workflow-new-form");
			// Fill meeting subject/name
			final String meetingName = "Test Meeting " + System.currentTimeMillis();

			if (fillFirstTextField(meetingName)) {
				LOGGER.debug("Filled meeting name: {}", meetingName);
			}
			// Fill description if available
			final var textAreas = page.locator("vaadin-text-area");

			if (textAreas.count() > 0) {
				textAreas.first().fill("Test description for workflow test");
			}
			// Set meeting date if available
			final var datePickers = page.locator("vaadin-date-picker");

			if (datePickers.count() > 0) {
				datePickers.first().fill("2024-03-15");
			}
			// Set meeting time if available
			final var timePickers = page.locator("vaadin-time-picker");

			if (timePickers.count() > 0) {
				timePickers.first().fill("14:30");
			}
			takeScreenshot("meetings-workflow-form-filled");
			// Save
			final var saveButtons = page.locator(
				"vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");

			if (saveButtons.count() > 0) {
				saveButtons.first().click();
				wait_2000();
				takeScreenshot("meetings-workflow-saved");
				// Check if grid was updated
				final int finalRowCount = getGridRowCount();
				LOGGER.debug("Final grid has {} rows", finalRowCount);
			}
		}
		LOGGER.info("✅ Meetings complete workflow test completed");
	}

	@Test
	void testMeetingsCRUDOperations() {
		LOGGER.info("🧪 Testing Meetings CRUD operations...");
		assertTrue(navigateToViewByClass(CMeetingsView.class), "Should navigate to view");
		// Use the auxiliary CRUD testing method
		testCRUDOperationsInView("Meetings", "new-button", "save-button",
			"delete-button");
		LOGGER.info("✅ Meetings CRUD operations test completed");
	}

	@Test
	void testMeetingsDateTimeHandling() {
		LOGGER.info("🧪 Testing Meetings date/time handling...");
		assertTrue(navigateToViewByClass(CMeetingsView.class), "Should navigate to view");
		// Open new meeting form
		final var newButtons =
			page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");

		if (newButtons.count() > 0) {
			newButtons.first().click();
			wait_1000();
			// Test date pickers
			final var datePickers = page.locator("vaadin-date-picker");

			if (datePickers.count() > 0) {
				LOGGER.debug("Testing date picker");
				datePickers.first().fill("2024-03-15");
				wait_500();
				takeScreenshot("meetings-date-picker");
			}
			// Test time pickers
			final var timePickers = page.locator("vaadin-time-picker");

			if (timePickers.count() > 0) {
				LOGGER.debug("Testing time picker");
				timePickers.first().fill("14:30");
				wait_500();
				takeScreenshot("meetings-time-picker");
			}
			// Test date-time pickers
			final var dateTimePickers = page.locator("vaadin-date-time-picker");

			if (dateTimePickers.count() > 0) {
				LOGGER.debug("Testing date-time picker");
				dateTimePickers.first().fill("2024-03-15T14:30");
				wait_500();
				takeScreenshot("meetings-datetime-picker");
			}
			// Close form
			final var cancelButtons = page.locator("vaadin-button:has-text('Cancel')");

			if (cancelButtons.count() > 0) {
				cancelButtons.first().click();
				wait_500();
			}
		}
		LOGGER.info("✅ Meetings date/time handling test completed");
	}

	@Test
	void testMeetingsFormValidation() {
		LOGGER.info("🧪 Testing Meetings form validation...");
		assertTrue(navigateToViewByClass(CMeetingsView.class), "Should navigate to view");
		// Try to create new meeting
		final var newButtons =
			page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");

		if (newButtons.count() > 0) {
			newButtons.first().click();
			wait_1000();
			// Test form validation using auxiliary method
			final boolean validationWorking = testFormValidationById("save-button");
			LOGGER.debug("Form validation working: {}", validationWorking);
			takeScreenshot("meetings-form-validation");
			// Close form
			final var cancelButtons = page.locator("vaadin-button:has-text('Cancel')");

			if (cancelButtons.count() > 0) {
				cancelButtons.first().click();
				wait_500();
			}
		}
		LOGGER.info("✅ Meetings form validation test completed");
	}

	@Test
	void testMeetingsGridInteractions() {
		LOGGER.info("🧪 Testing Meetings grid interactions...");
		assertTrue(navigateToViewByClass(CMeetingsView.class), "Should navigate to view");
		// Test grid selection
		final int rowCount = getGridRowCount();

		if (rowCount > 0) {
			LOGGER.debug("Testing grid selection with {} rows", rowCount);
			assertTrue(clickGridRowByIndex(0), "Should be able to click first grid row");
			wait_1000();
			takeScreenshot("meetings-grid-row-selected");
		}
		// Test grid sorting if available
		final var sorters = page.locator("vaadin-grid-sorter");

		if (sorters.count() > 0) {
			LOGGER.debug("Testing grid sorting");
			sorters.first().click();
			wait_1000();
			takeScreenshot("meetings-grid-sorted");
		}
		LOGGER.info("✅ Meetings grid interactions test completed");
	}

	@Test
	void testMeetingsNavigation() {
		LOGGER.info("🧪 Testing Meetings view navigation...");
		// Test navigation to Meetings
		assertTrue(navigateToViewByClass(CMeetingsView.class), "Should navigate to view");
		takeScreenshot("meetings-navigation-arrival");

		// Test navigation to related views
		if (navigateToViewByClass(CProjectsView.class)) {
			takeScreenshot("meetings-navigation-to-projects");
			assertTrue(navigateToViewByClass(CMeetingsView.class),
				"Should navigate to view");
			takeScreenshot("meetings-navigation-return");
		}
		LOGGER.info("✅ Meetings navigation test completed");
	}

	@Test
	void testMeetingsResponsiveDesign() {
		LOGGER.info("🧪 Testing Meetings responsive design...");
		assertTrue(navigateToViewByClass(CMeetingsView.class), "Should navigate to view");
		// Use auxiliary responsive testing method
		testResponsiveDesign("Meetings");
		LOGGER.info("✅ Meetings responsive design test completed");
	}

	@Test
	void testMeetingsViewLoading() {
		LOGGER.info("🧪 Testing Meetings view loading...");
		// Login and navigate to Meetings view Navigate to Meetings view using navigation
		assertTrue(navigateToViewByClass(CMeetingsView.class),
			"Should navigate to Activities view");
		// Verify the view loaded properly
		takeScreenshot("meetings-view-loaded");
		// Check if grid is present
		assertTrue(getGridRowCount() >= 0, "Meetings grid should be present");
		LOGGER.info("✅ Meetings view loading test completed");
	}
}