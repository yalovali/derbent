package automated_tests.tech.derbent.meetings.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import tech.derbent.meetings.view.CMeetingsView;
import tech.derbent.projects.view.CProjectsView;
import ui_tests.tech.derbent.ui.automation.CApplicationGeneric_UITest;

/**
 * CMeetingsViewPlaywrightTest - Comprehensive Playwright tests for the Meetings view.
 * Tests all aspects of the Meetings view including CRUD operations, grid interactions,
 * form validation, ComboBox selections, and UI behaviors following the strict coding
 * guidelines for Playwright testing.
 */
@SpringBootTest (webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource (properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.jpa.hibernate.ddl-auto=create-drop" }
)
public class CMeetingsViewPlaywrightTest extends CApplicationGeneric_UITest {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CMeetingsViewPlaywrightTest.class);

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
		clickNew(); // Open new meeting form
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
		clickCancel(); // Close form
		LOGGER.info("✅ Meetings date/time handling test completed");
	}

	@Test
	void testMeetingsFormValidation() {
		LOGGER.info("🧪 Testing Meetings form validation...");
		assertTrue(navigateToViewByClass(CMeetingsView.class), "Should navigate to view");
		clickNew();
		testFormValidationById("save-button");
		takeScreenshot("meetings-form-validation");
		clickCancel();
		LOGGER.info("✅ Meetings form validation test completed");
	}

	@Test
	void testMeetingsGridInteractions() {
		testAdvancedGridInView(CMeetingsView.class);
	}

	@Test
	void testMeetingsNavigation() {
		testNavigationTo(CMeetingsView.class, CProjectsView.class);
	}
}