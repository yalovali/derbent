package automated_tests.tech.derbent.ui.automation;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import com.microsoft.playwright.Locator;

/** Test for CMeeting dynamic page grid selection and form population issue */
@SpringBootTest (webEnvironment = WebEnvironment.RANDOM_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", 
		"spring.datasource.username=sa", 
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", 
		"spring.jpa.hibernate.ddl-auto=create-drop", 
		"server.port=0"
})
public class CMeetingDynamicPageTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingDynamicPageTest.class);

	@Test
	public void testMeetingGridSelectionPopulatesForm() {
		LOGGER.info("=== Testing Meeting Grid Selection and Form Population with Date/Time Fields ===");
		try {
			// Login
			loginToApplication("admin", "test123");
			wait_afterlogin();
			LOGGER.info("✅ Logged in successfully");
			
			// Navigate to Meetings view
			navigateToViewByText("Meetings");
			wait_1000();
			takeScreenshot("meeting-page-initial", false);
			LOGGER.info("✅ Navigated to Meetings view");
			
			// Check if grid has items
			Locator grid = page.locator("vaadin-grid").first();
			Locator gridCells = grid.locator("vaadin-grid-cell-content");
			int rowCount = gridCells.count();
			LOGGER.info("📊 Grid row count: {}", rowCount);
			
			if (rowCount == 0) {
				LOGGER.info("⚠️ No meetings in grid, creating one first");
				// Click New button
				clickNew();
				wait_500();
				takeScreenshot("meeting-after-new", false);
				
				// Fill in meeting name
				Locator nameField = page.locator("vaadin-text-field").first();
				if (nameField.count() > 0) {
					nameField.fill("Test Meeting " + System.currentTimeMillis());
					wait_500();
					takeScreenshot("meeting-filled", false);
					LOGGER.info("✅ Filled meeting name");
					
					// Save
					clickSave();
					wait_1000();
					takeScreenshot("meeting-after-save", false);
					LOGGER.info("✅ Saved meeting");
				} else {
					LOGGER.error("❌ No name field found after clicking New");
					takeScreenshot("meeting-no-name-field", true);
				}
			}
			
			// Now click first grid row
			LOGGER.info("📊 Clicking first grid row");
			clickFirstGridRow();
			wait_1000();
			takeScreenshot("meeting-after-grid-click", false);
			
			// Check if form fields are populated
			Locator textFields = page.locator("vaadin-text-field");
			int fieldCount = textFields.count();
			LOGGER.info("📝 Text field count after grid selection: {}", fieldCount);
			
			if (fieldCount > 0) {
				String nameValue = textFields.first().inputValue();
				LOGGER.info("📝 Name field value: {}", nameValue);
				
				if (nameValue != null && !nameValue.isEmpty()) {
					LOGGER.info("✅ Form populated correctly with value: {}", nameValue);
				} else {
					LOGGER.error("❌ BUG CONFIRMED: Form field is empty after grid selection");
					takeScreenshot("meeting-bug-empty-form", true);
				}
			} else {
				LOGGER.error("❌ BUG CONFIRMED: No form fields visible after grid selection");
				takeScreenshot("meeting-bug-no-fields", true);
			}
			
			LOGGER.info("=== Meeting Grid Selection Test Completed ===");
		} catch (Exception e) {
			LOGGER.error("❌ Meeting test failed", e);
			takeScreenshot("meeting-test-error", true);
			throw new AssertionError("Meeting test failed", e);
		}
	}

	@Test
	public void testMeetingDateTimeFields() {
		LOGGER.info("=== Testing Meeting Date and Time Field Components ===");
		try {
			// Login
			loginToApplication("admin", "test123");
			wait_afterlogin();
			LOGGER.info("✅ Logged in successfully");
			
			// Navigate to Meetings view
			navigateToViewByText("Meetings");
			wait_1000();
			LOGGER.info("✅ Navigated to Meetings view");
			
			// Click New button
			LOGGER.info("➕ Clicking New button to test date/time components");
			clickNew();
			wait_1000();
			takeScreenshot("meeting-new-form-with-datetime", false);
			
			// Check for DatePicker components (for startDate and endDate)
			Locator datePickers = page.locator("vaadin-date-picker");
			int datePickerCount = datePickers.count();
			LOGGER.info("📅 DatePicker count: {}", datePickerCount);
			
			if (datePickerCount >= 2) {
				LOGGER.info("✅ SUCCESS: Found DatePicker components (expected: startDate, endDate)");
			} else {
				LOGGER.error("❌ FAIL: Expected at least 2 DatePicker components, found: {}", datePickerCount);
			}
			
			// Check for TimePicker components (for startTime and endTime)
			Locator timePickers = page.locator("vaadin-time-picker");
			int timePickerCount = timePickers.count();
			LOGGER.info("⏰ TimePicker count: {}", timePickerCount);
			
			if (timePickerCount >= 2) {
				LOGGER.info("✅ SUCCESS: Found TimePicker components (expected: startTime, endTime)");
			} else {
				LOGGER.error("❌ FAIL: Expected at least 2 TimePicker components, found: {}", timePickerCount);
			}
			
			// Test filling date picker
			if (datePickerCount > 0) {
				LOGGER.info("📅 Testing DatePicker interaction");
				// DatePicker typically has an input field inside
				Locator firstDatePicker = datePickers.first();
				Locator dateInput = firstDatePicker.locator("input").first();
				if (dateInput.count() > 0) {
					// Try to set a date (format may vary by locale)
					dateInput.fill("01/15/2025");
					wait_500();
					takeScreenshot("meeting-date-filled", false);
					LOGGER.info("✅ Filled date picker");
				}
			}
			
			// Test filling time picker
			if (timePickerCount > 0) {
				LOGGER.info("⏰ Testing TimePicker interaction");
				Locator firstTimePicker = timePickers.first();
				Locator timeInput = firstTimePicker.locator("input").first();
				if (timeInput.count() > 0) {
					// Try to set a time
					timeInput.fill("14:30");
					wait_500();
					takeScreenshot("meeting-time-filled", false);
					LOGGER.info("✅ Filled time picker");
				}
			}
			
			// Fill meeting name to enable save
			Locator nameField = page.locator("vaadin-text-field").first();
			if (nameField.count() > 0) {
				nameField.fill("Test Meeting DateTime " + System.currentTimeMillis());
				wait_500();
			}
			
			// Save and verify
			clickSave();
			wait_1000();
			takeScreenshot("meeting-datetime-after-save", false);
			LOGGER.info("✅ Saved meeting with date/time fields");
			
			LOGGER.info("=== Meeting Date/Time Fields Test Completed ===");
			LOGGER.info("Summary: DatePickers={}, TimePickers={}", datePickerCount, timePickerCount);
		} catch (Exception e) {
			LOGGER.error("❌ Meeting date/time test failed", e);
			takeScreenshot("meeting-datetime-test-error", true);
			throw new AssertionError("Meeting date/time test failed", e);
		}
	}
}
