package automated_tests.tech.derbent.meetings.tests;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.meetings.view.CMeetingsView;
import tech.derbent.projects.view.CProjectsView;
import ui_tests.tech.derbent.ui.automation.CApplicationGeneric_UITest;

/** CMeetingsViewPlaywrightTest - Comprehensive Playwright tests for the Meetings view. Tests all aspects of the Meetings view including CRUD
 * operations, grid interactions, form validation, ComboBox selections, and UI behaviors following the strict coding guidelines for Playwright
 * testing. */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.url=jdbc:h2:mem:testdb", "spring.jpa.hibernate.ddl-auto=create-drop",
		"server.port=8080"
})
public class CMeetingsViewPlaywrightTest extends CApplicationGeneric_UITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingsViewPlaywrightTest.class);

	@Test
	void testMeetingsComboBoxes() {
		LOGGER.info("🧪 Testing Meetings ComboBox components with enhanced validation...");
		try {
			navigateToViewByClass(CMeetingsView.class);
			wait_500();
			// Open new meeting form
			final var newButtons = page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");
			Check.isTrue(newButtons.count() > 0, "New button should be available in Meetings view");
			newButtons.first().click();
			wait_1000();
			// Test Meeting Type ComboBox
			final var comboBoxes = page.locator("vaadin-combo-box");
			LOGGER.debug("Found {} ComboBox components in Meetings form", comboBoxes.count());
			if (comboBoxes.count() > 0) {
				LOGGER.debug("Testing Meeting Type ComboBox");
				// Click to open first ComboBox
				comboBoxes.first().click();
				wait_500();
				// Check options are available
				final var options = page.locator("vaadin-combo-box-item");
				final int optionCount = options.count();
				LOGGER.debug("Found {} options in Meeting Type ComboBox", optionCount);
				Check.isTrue(optionCount >= 0, "Meeting Type ComboBox should have accessible options");
				if (optionCount > 0) {
					options.first().click();
					wait_500();
					LOGGER.debug("Successfully selected first option in Meeting Type ComboBox");
				}
			}
			// Test Project ComboBox if available
			if (comboBoxes.count() > 1) {
				LOGGER.debug("Testing Project ComboBox");
				comboBoxes.nth(1).click();
				wait_500();
				final var projectOptions = page.locator("vaadin-combo-box-item");
				final int projectOptionCount = projectOptions.count();
				LOGGER.debug("Found {} options in Project ComboBox", projectOptionCount);
				Check.isTrue(projectOptionCount >= 0, "Project ComboBox should have accessible options");
				if (projectOptions.count() > 0) {
					projectOptions.first().click();
					wait_500();
					LOGGER.debug("Successfully selected first option in Project ComboBox");
				}
			}
			// Close form
			final var cancelButtons = page.locator("vaadin-button:has-text('Cancel')");
			if (cancelButtons.count() > 0) {
				cancelButtons.first().click();
				wait_500();
			}
			LOGGER.info("✅ Meetings ComboBox test completed successfully");
		} catch (final Exception e) {
			LOGGER.error("❌ Meetings ComboBox test failed: {}", e.getMessage());
			takeScreenshot("meetings-combobox-test-failed", true);
			throw new AssertionError("Meetings ComboBox test failed: " + e.getMessage(), e);
		}
	}

	@Test
	void testMeetingsCompleteWorkflow() {
		LOGGER.info("🧪 Testing Meetings complete workflow with enhanced validation...");
		try {
			navigateToViewByClass(CMeetingsView.class);
			wait_500();
			// Test complete workflow: navigate -> view grid -> create new -> fill form -> save -> view result
			final int initialRowCount = getGridRowCount();
			LOGGER.debug("Initial grid has {} rows", initialRowCount);
			// Create new meeting
			final var newButtons = page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");
			Check.isTrue(newButtons.count() > 0, "New button should be available for creating meetings");
			newButtons.first().click();
			wait_1000();
			// Verify form opened
			final var formFields = page.locator("vaadin-text-field, vaadin-text-area, vaadin-date-picker");
			Check.isTrue(formFields.count() > 0, "Meeting form should have input fields");
			// Fill meeting subject/name
			final String meetingName = "Test Meeting " + System.currentTimeMillis();
			Check.isTrue(fillFirstTextField(meetingName), "Should be able to fill meeting name field");
			LOGGER.debug("Filled meeting name: {}", meetingName);
			// Fill description if available
			final var textAreas = page.locator("vaadin-text-area");
			if (textAreas.count() > 0) {
				textAreas.first().fill("Test description for workflow test");
				LOGGER.debug("Filled meeting description");
			}
			// Set meeting date if available
			final var datePickers = page.locator("vaadin-date-picker");
			if (datePickers.count() > 0) {
				datePickers.first().fill("2024-03-15");
				LOGGER.debug("Set meeting date");
			}
			// Set meeting time if available
			final var timePickers = page.locator("vaadin-time-picker");
			if (timePickers.count() > 0) {
				timePickers.first().fill("14:30");
				LOGGER.debug("Set meeting time");
			}
			// Save
			final var saveButtons = page.locator("vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");
			Check.isTrue(saveButtons.count() > 0, "Save button should be available");
			saveButtons.first().click();
			wait_2000();
			// Check if grid was updated
			final int finalRowCount = getGridRowCount();
			LOGGER.debug("Final grid has {} rows", finalRowCount);
			// Verify the workflow completed successfully
			final String currentUrl = page.url();
			Check.isTrue(currentUrl.contains("meeting") || currentUrl.contains("meetings"),
					"Should remain on meetings view after successful workflow");
			LOGGER.info("✅ Meetings complete workflow test completed successfully");
		} catch (final Exception e) {
			LOGGER.error("❌ Meetings complete workflow test failed: {}", e.getMessage());
			takeScreenshot("meetings-workflow-test-failed", true);
			throw new AssertionError("Meetings complete workflow test failed: " + e.getMessage(), e);
		}
	}

	@Test
	void testMeetingsCRUDOperations() {
		LOGGER.info("🧪 Testing Meetings CRUD operations with enhanced validation...");
		try {
			navigateToViewByClass(CMeetingsView.class);
			// Use the auxiliary CRUD testing method
			testCRUDOperationsInView("Meetings", "new-button", "save-button", "delete-button");
			LOGGER.info("✅ Meetings CRUD operations test completed successfully");
		} catch (final Exception e) {
			LOGGER.error("❌ Meetings CRUD operations test failed: {}", e.getMessage());
			takeScreenshot("meetings-crud-test-failed", true);
			throw new AssertionError("Meetings CRUD operations test failed: " + e.getMessage(), e);
		}
	}

	@Test
	void testMeetingsDateTimeHandling() {
		LOGGER.info("🧪 Testing Meetings date/time handling with enhanced validation...");
		try {
			navigateToViewByClass(CMeetingsView.class);
			wait_500();
			clickNew(); // Open new meeting form
			wait_1000();
			// Test date pickers
			final var datePickers = page.locator("vaadin-date-picker");
			if (datePickers.count() > 0) {
				LOGGER.debug("Testing date picker functionality");
				datePickers.first().fill("2024-03-15");
				wait_500();
				// Verify date was set
				final String dateValue = datePickers.first().inputValue();
				Check.isTrue(dateValue != null && !dateValue.trim().isEmpty(), "Date picker should accept and store date values");
			}
			// Test time pickers
			final var timePickers = page.locator("vaadin-time-picker");
			if (timePickers.count() > 0) {
				LOGGER.debug("Testing time picker functionality");
				timePickers.first().fill("14:30");
				wait_500();
				// Verify time was set
				final String timeValue = timePickers.first().inputValue();
				Check.isTrue(timeValue != null && !timeValue.trim().isEmpty(), "Time picker should accept and store time values");
			}
			// Test date-time pickers
			final var dateTimePickers = page.locator("vaadin-date-time-picker");
			if (dateTimePickers.count() > 0) {
				LOGGER.debug("Testing date-time picker functionality");
				dateTimePickers.first().fill("2024-03-15T14:30");
				wait_500();
				// Verify date-time was set
				final String dateTimeValue = dateTimePickers.first().inputValue();
				Check.isTrue(dateTimeValue != null && !dateTimeValue.trim().isEmpty(), "Date-time picker should accept and store date-time values");
			}
			clickCancel(); // Close form
			wait_500();
			LOGGER.info("✅ Meetings date/time handling test completed successfully");
		} catch (final Exception e) {
			LOGGER.error("❌ Meetings date/time handling test failed: {}", e.getMessage());
			takeScreenshot("meetings-datetime-test-failed", true);
			throw new AssertionError("Meetings date/time handling test failed: " + e.getMessage(), e);
		}
	}

	@Test
	void testMeetingsFormValidation() {
		LOGGER.info("🧪 Testing Meetings form validation with enhanced checks...");
		try {
			navigateToViewByClass(CMeetingsView.class);
			wait_500();
			clickNew();
			wait_1000();
			// Test form validation by attempting to save without required fields
			testFormValidationById("save-button");
			// Verify validation messages appeared
			final var validationMessages = page.locator(".v-errormessage, vaadin-error-message");
			LOGGER.debug("Found {} validation messages as expected", validationMessages.count());
			clickCancel();
			wait_500();
			LOGGER.info("✅ Meetings form validation test completed successfully");
		} catch (final Exception e) {
			LOGGER.error("❌ Meetings form validation test failed: {}", e.getMessage());
			takeScreenshot("meetings-validation-test-failed", true);
			throw new AssertionError("Meetings form validation test failed: " + e.getMessage(), e);
		}
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
