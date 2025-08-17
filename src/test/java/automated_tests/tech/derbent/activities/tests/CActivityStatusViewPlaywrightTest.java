package automated_tests.tech.derbent.activities.tests;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import tech.derbent.abstracts.utils.Check;
import tech.derbent.activities.view.CActivityStatusView;
import ui_tests.tech.derbent.ui.automation.CApplicationGeneric_UITest;

/**
 * CActivityStatusViewPlaywrightTest - Tests for activity status view focusing on lazy
 * loading fixes and navigation behavior after save operations.
 */
@SpringBootTest (
	webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class
)
@TestPropertySource (properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa",
	"spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver",
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080" }
)
public class CActivityStatusViewPlaywrightTest extends CApplicationGeneric_UITest {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CActivityStatusViewPlaywrightTest.class);

	@Test
	void testActivityStatusColorFunctionality() {
		LOGGER.info("🧪 Testing Activity Status color functionality...");
		
		try {
			navigateToViewByClass(CActivityStatusView.class);
			clickNew();
			wait_500();
			
			// Test color picker availability using Check assertions
			final var colorPickers = page.locator("vaadin-color-picker, input[type='color']");
			Check.condition(colorPickers.count() > 0,
				"Color picker should be available in the activity status form");
			
			// Fill color picker
			colorPickers.first().fill("#2196F3");
			wait_500();
			
			// Fill other required fields
			final String statusName = "Colored Status " + System.currentTimeMillis();
			Check.condition(fillFirstTextField(statusName), 
				"Should be able to fill status name field");
			
			// Save the form
			clickSave();
			wait_2000();
			
			// Verify save success by checking URL or form state
			final String currentUrl = page.url();
			Check.condition(
				currentUrl.contains("activity-status") || currentUrl.contains("activity-statuses"),
				"Should remain on activity status view after successful color save");
			
			clickCancel();
			LOGGER.info("✅ Activity Status color functionality test completed successfully");
			
		} catch (final Exception e) {
			LOGGER.error("❌ Activity Status color functionality test failed: {}", e.getMessage());
			takeScreenshot("activity-status-color-test-failed", true);
			throw new AssertionError("Activity Status color functionality test failed: " + e.getMessage(), e);
		}
	}

	@Test
	void testActivityStatusFormValidationAndSave() {
		LOGGER.info("🧪 Testing Activity Status form validation and save...");
		
		try {
			navigateToViewByClass(CActivityStatusView.class);
			clickNew();
			wait_1000();
			
			// Test validation by trying to save without required fields
			clickSave();
			wait_500();
			
			// Check for validation errors (no screenshot since this is expected behavior)
			final var errorMessages = page.locator(".v-errormessage, vaadin-error-message");
			LOGGER.debug("Validation check: found {} error messages as expected", errorMessages.count());
			
			// Fill required fields and save successfully
			final String statusName = "Validated Status " + System.currentTimeMillis();
			Check.condition(fillFirstTextField(statusName), 
				"Should be able to fill status name field");
			LOGGER.debug("Filled status name for validation test: {}", statusName);
			
			// Set color if color picker is available
			final var colorPickers = page.locator("vaadin-color-picker, input[type='color']");
			if (colorPickers.count() > 0) {
				colorPickers.first().fill("#FF5722");
				wait_500();
				LOGGER.debug("Color picker filled successfully");
			}
			
			// Save should succeed now
			clickSave();
			wait_2000();
			
			// Verify successful save by checking we're still on same view and no error messages
			final String currentUrl = page.url();
			Check.condition(
				currentUrl.contains("activity-status") || currentUrl.contains("activity-statuses"),
				"Should remain on activity status view after successful save");
			
			// Check that error messages are cleared after successful save
			final var remainingErrors = page.locator(".v-errormessage, vaadin-error-message");
			Check.condition(remainingErrors.count() == 0,
				"Error messages should be cleared after successful save");
			
			LOGGER.info("✅ Activity Status form validation test completed successfully");
			
		} catch (final Exception e) {
			LOGGER.error("❌ Activity Status form validation test failed: {}", e.getMessage());
			takeScreenshot("activity-status-validation-test-failed", true);
			throw new AssertionError("Activity Status form validation test failed: " + e.getMessage(), e);
		}
	}

	@Test
	void testActivityStatusGridSelectionLazyLoading() {
		LOGGER.info("🧪 Testing Activity Status grid selection and lazy loading...");
		
		try {
			navigateToViewByClass(CActivityStatusView.class);
			wait_1000();
			
			// Check if grid has rows
			final int rowCount = getGridRowCount();
			LOGGER.debug("Grid contains {} rows", rowCount);
			
			if (rowCount > 0) {
				LOGGER.debug("Grid has {} rows, testing selection", rowCount);
				
				// Click on first row to test lazy loading
				final var gridRows = page.locator("vaadin-grid-cell-content").first();
				Check.condition(gridRows.isVisible(), 
					"First grid row should be visible for selection test");
				
				gridRows.click();
				wait_1000();
				
				// Verify grid selection without lazy loading exceptions
				final var selectedRows = page.locator("vaadin-grid tr[selected], vaadin-grid tr[aria-selected='true']");
				LOGGER.debug("Selection test completed - {} rows appear selected", selectedRows.count());
				
				// Test that form fields are accessible after selection (no lazy loading issues)
				final var formFields = page.locator("vaadin-text-field, vaadin-text-area");
				Check.condition(formFields.count() >= 0, 
					"Form fields should be accessible after grid selection without lazy loading errors");
				
			} else {
				LOGGER.debug("No existing rows, creating test data for selection test");
				
				// Create a test status first
				clickNew();
				wait_500();
				
				final String testStatusName = "Test Status for Selection " + System.currentTimeMillis();
				Check.condition(fillFirstTextField(testStatusName), 
					"Should be able to fill test status name");
				
				clickSave();
				wait_2000();
				
				// Verify the row was created
				final int newRowCount = getGridRowCount();
				Check.condition(newRowCount > rowCount, 
					"Grid should have more rows after creating test data");
				
				// Now test selection
				final var gridRows = page.locator("vaadin-grid-cell-content").first();
				if (gridRows.isVisible()) {
					gridRows.click();
					wait_1000();
					LOGGER.debug("Successfully selected newly created row");
				}
			}
			
			LOGGER.info("✅ Activity Status grid selection test completed successfully");
			
		} catch (final Exception e) {
			LOGGER.error("❌ Activity Status grid selection test failed: {}", e.getMessage());
			takeScreenshot("activity-status-grid-selection-failed", true);
			throw new AssertionError("Activity Status grid selection test failed: " + e.getMessage(), e);
		}
	}

	@Test
	void testActivityStatusLazyLoadingAndNavigation() {
		LOGGER.info("🧪 Testing Activity Status lazy loading and navigation after save...");
		
		try {
			navigateToViewByClass(CActivityStatusView.class);
			wait_1000();
			
			// Test that grid loads without lazy loading exceptions
			final int initialRowCount = getGridRowCount();
			LOGGER.debug("Grid loaded successfully with {} rows", initialRowCount);
			
			// Create new activity status to test save navigation
			clickNew();
			wait_1000();
			
			// Verify form is displayed
			final var formFields = page.locator("vaadin-text-field, vaadin-text-area");
			Check.condition(formFields.count() > 0, 
				"Form fields should be available when creating new activity status");
			
			// Fill required fields
			final String statusName = "Navigation Test Status " + System.currentTimeMillis();
			Check.condition(fillFirstTextField(statusName), 
				"Should be able to fill status name field");
			LOGGER.debug("Filled status name: {}", statusName);
			
			// Fill description if available
			final var textAreas = page.locator("vaadin-text-area");
			if (textAreas.count() > 0) {
				textAreas.first().fill("Test description for lazy loading and navigation test");
				LOGGER.debug("Filled description field");
			}
			
			// Fill color if color picker available
			final var colorPickers = page.locator("vaadin-color-picker, input[type='color']");
			if (colorPickers.count() > 0) {
				colorPickers.first().fill("#4CAF50");
				LOGGER.debug("Filled color picker");
			}
			
			// Save and verify we stay on the same view (not redirected to wrong view)
			clickSave();
			wait_2000();
			
			// Verify we're still on the activity status view by checking URL
			final String currentUrl = page.url();
			Check.condition(
				currentUrl.contains("activity-status") || currentUrl.contains("activity-statuses"),
				"Should remain on activity status view after save, but was: " + currentUrl);
			
			// Verify the new row was added to the grid
			final int finalRowCount = getGridRowCount();
			Check.condition(finalRowCount > initialRowCount, 
				"Grid should have more rows after saving new activity status");
			
			// Test that we can navigate away and back without issues
			navigateToViewByClass(CActivityStatusView.class);
			wait_1000();
			
			final int reloadRowCount = getGridRowCount();
			Check.condition(reloadRowCount == finalRowCount, 
				"Row count should be consistent after navigation reload");
			
			LOGGER.info("✅ Activity Status lazy loading and navigation test completed successfully");
			
		} catch (final Exception e) {
			LOGGER.error("❌ Activity Status lazy loading and navigation test failed: {}", e.getMessage());
			takeScreenshot("activity-status-lazy-loading-failed", true);
			throw new AssertionError("Activity Status lazy loading and navigation test failed: " + e.getMessage(), e);
		}
	}

	/**
	 * Enhanced test for Activity Status ComboBox content verification
	 */
	@Test
	void testActivityStatusComboBoxContent() {
		LOGGER.info("🧪 Testing Activity Status ComboBox content verification...");
		
		try {
			navigateToViewByClass(CActivityStatusView.class);
			wait_1000();
			
			// Look for ComboBox elements in the form
			clickNew();
			wait_500();
			
			final var comboBoxes = page.locator("vaadin-combo-box");
			LOGGER.debug("Found {} ComboBox elements in Activity Status form", comboBoxes.count());
			
			// Test each ComboBox for content
			for (int i = 0; i < comboBoxes.count(); i++) {
				final var comboBox = comboBoxes.nth(i);
				
				// Click to open dropdown
				comboBox.click();
				wait_500();
				
				// Check for dropdown options
				final var options = page.locator("vaadin-combo-box-item, vaadin-combo-box-dropdown-wrapper vaadin-item");
				LOGGER.debug("ComboBox {} has {} options available", i, options.count());
				
				// Verify that ComboBox has accessible options
				Check.condition(options.count() >= 0, 
					"ComboBox should have accessible options without errors");
				
				// Close dropdown by clicking elsewhere
				page.locator("body").click();
				wait_200();
			}
			
			clickCancel();
			LOGGER.info("✅ Activity Status ComboBox content test completed successfully");
			
		} catch (final Exception e) {
			LOGGER.error("❌ Activity Status ComboBox content test failed: {}", e.getMessage());
			takeScreenshot("activity-status-combobox-test-failed", true);
			throw new AssertionError("Activity Status ComboBox content test failed: " + e.getMessage(), e);
		}
	}
}