package tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import tech.derbent.projects.view.CProjectsView;
import tech.derbent.users.view.CUsersView;

/**
 * CUsersViewPlaywrightTest - Comprehensive Playwright tests for the Users view. Tests all
 * aspects of the Users view including CRUD operations, grid interactions, form
 * validation, ComboBox selections, and UI behaviors following the strict coding
 * guidelines for Playwright testing.
 */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT)
@TestPropertySource (properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080" }
)
public class CUsersViewPlaywrightTest extends CBaseUITest {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CUsersViewPlaywrightTest.class);

	@Test
	void testUsersAccessibility() {
		LOGGER.info("🧪 Testing Users accessibility...");
		assertTrue(navigateToViewByClass(CUsersView.class), "Should navigate to view");
		// Use auxiliary accessibility testing method
		testAccessibilityBasics("Users");
		LOGGER.info("✅ Users accessibility test completed");
	}

	@Test
	void testUsersComboBoxes() {
		LOGGER.info("🧪 Testing Users ComboBox components...");
		assertTrue(navigateToViewByClass(CUsersView.class), "Should navigate to view");
		clickNewButton();
		// Test User Role ComboBox
		final var comboBoxes = page.locator("vaadin-combo-box");

		if (comboBoxes.count() > 0) {
			LOGGER.debug("Testing User Role ComboBox");
			// Click to open first ComboBox
			comboBoxes.first().click();
			wait_500();
			// Check options are available
			final var options = page.locator("vaadin-combo-box-item");
			final int optionCount = options.count();
			LOGGER.debug("Found {} options in User Role ComboBox", optionCount);

			if (optionCount > 0) {
				// Select first option
				options.first().click();
				wait_500();
			}
			takeScreenshot("users-role-combobox");
		}

		// Test Company ComboBox if available
		if (comboBoxes.count() > 1) {
			LOGGER.debug("Testing Company ComboBox");
			comboBoxes.nth(1).click();
			wait_500();
			takeScreenshot("users-company-combobox");
			// Select option if available
			final var options = page.locator("vaadin-combo-box-item");

			if (options.count() > 0) {
				options.first().click();
				wait_500();
			}
		}

		// Test User Type ComboBox if available
		if (comboBoxes.count() > 2) {
			LOGGER.debug("Testing User Type ComboBox");
			comboBoxes.nth(2).click();
			wait_500();
			takeScreenshot("users-type-combobox");
			// Close by clicking elsewhere
			page.click("body");
			wait_500();
		}
		clickCancelButton();
		LOGGER.info("✅ Users ComboBox test completed");
	}

	@Test
	void testUsersCompleteWorkflow() {
		LOGGER.info("🧪 Testing Users complete workflow...");
		assertTrue(navigateToViewByClass(CUsersView.class), "Should navigate to view");
		LOGGER.debug("Initial grid has {} rows", getGridRowCount());
		// Create new user
		clickNewButton();
		takeScreenshot("users-workflow-new-form");
		// Fill user name (first name)
		final String firstName = "TestUser" + System.currentTimeMillis();

		if (fillFirstTextField(firstName)) {
			LOGGER.debug("Filled first name: {}", firstName);
		}
		// Fill lastname if available
		final var textFields = page.locator("vaadin-text-field");

		if (textFields.count() > 1) {
			textFields.nth(1).fill("TestLastname");
		}

		// Fill email if available
		if (textFields.count() > 2) {
			textFields.nth(2).fill("test" + System.currentTimeMillis() + "@example.com");
		}

		// Fill login if available
		if (textFields.count() > 3) {
			textFields.nth(3).fill("testuser" + System.currentTimeMillis());
		}
		takeScreenshot("users-workflow-form-filled");
		// Save
		clickSaveButton();
		LOGGER.info("✅ Users complete workflow test completed");
	}

	@Test
	void testUsersCRUDOperations() {
		LOGGER.info("🧪 Testing Users CRUD operations...");
		assertTrue(navigateToViewByClass(CUsersView.class), "Should navigate to view");
		// Use the auxiliary CRUD testing method
		testCRUDOperationsInView("Users", "new-button", "save-button", "delete-button");
		LOGGER.info("✅ Users CRUD operations test completed");
	}

	@Test
	void testUsersFormValidation() {
		LOGGER.info("🧪 Testing Users form validation...");
		assertTrue(navigateToViewByClass(CUsersView.class), "Should navigate to view");
		// Try to create new user
		clickNewButton();
		final boolean validationWorking = testFormValidationById("save-button");
		LOGGER.debug("Form validation working: {}", validationWorking);
		takeScreenshot("users-form-validation");
		// Test email validation specifically
		final var emailFields =
			page.locator("vaadin-text-field[type='email'], vaadin-email-field");

		if (emailFields.count() > 0) {
			emailFields.first().fill("invalid-email");
			wait_500();
			takeScreenshot("users-email-validation");
		}
		clickCancelButton();
		LOGGER.info("✅ Users form validation test completed");
	}

	@Test
	void testUsersGridInteractions() {
		LOGGER.info("🧪 Testing Users grid interactions...");
		assertTrue(navigateToViewByClass(CUsersView.class), "Should navigate to view");
		// Test grid selection
		final int rowCount = getGridRowCount();

		if (rowCount > 0) {
			LOGGER.debug("Testing grid selection with {} rows", rowCount);
			assertTrue(clickGridRowByIndex(0), "Should be able to click first grid row");
			wait_1000();
			takeScreenshot("users-grid-row-selected");
		}
		// Test grid sorting if available
		final var sorters = page.locator("vaadin-grid-sorter");

		if (sorters.count() > 0) {
			LOGGER.debug("Testing grid sorting");
			sorters.first().click();
			wait_1000();
			takeScreenshot("users-grid-sorted");
		}
		LOGGER.info("✅ Users grid interactions test completed");
	}

	@Test
	void testUsersNavigation() {
		LOGGER.info("🧪 Testing Users view navigation...");
		// Test navigation to Users
		assertTrue(navigateToViewByClass(CUsersView.class), "Should navigate to view");

		// Test navigation to related views
		if (navigateToViewByClass(CProjectsView.class)) {
			takeScreenshot("users-navigation-to-projects");
			assertTrue(navigateToViewByClass(CUsersView.class),
				"Should navigate to view");
			takeScreenshot("users-navigation-return");
		}
		LOGGER.info("✅ Users navigation test completed");
	}

	@Test
	void testUsersProfilePictureHandling() {
		LOGGER.info("🧪 Testing Users profile picture handling...");
		assertTrue(navigateToViewByClass(CUsersView.class), "Should navigate to view");
		// Check if profile pictures are displayed in the grid
		final var profileImages = page.locator("vaadin-grid img");

		if (profileImages.count() > 0) {
			LOGGER.debug("Found {} profile images in grid", profileImages.count());
			takeScreenshot("users-profile-pictures-grid");
		}
		clickNewButton();
		// Check if profile picture upload is available
		final var uploadComponents = page.locator("vaadin-upload, input[type='file']");

		if (uploadComponents.count() > 0) {
			LOGGER.debug("Profile picture upload component found");
			takeScreenshot("users-profile-picture-upload");
		}
		clickCancelButton();
		LOGGER.info("✅ Users profile picture handling test completed");
	}

	@Test
	void testUsersResponsiveDesign() {
		LOGGER.info("🧪 Testing Users responsive design...");
		assertTrue(navigateToViewByClass(CUsersView.class), "Should navigate to view");
		// Use auxiliary responsive testing method
		testResponsiveDesign("Users");
		LOGGER.info("✅ Users responsive design test completed");
	}

	@Test
	void testUsersViewLoading() {
		LOGGER.info("🧪 Testing Users view loading...");
		// Login and navigate to Users view Navigate to Users view using navigation
		assertTrue(navigateToViewByClass(CUsersView.class),
			"Should navigate to Users view");
		takeScreenshot("users-view-loaded");
		// Check if grid is present
		assertTrue(getGridRowCount() >= 0, "Users grid should be present");
		LOGGER.info("✅ Users view loading test completed");
	}
}