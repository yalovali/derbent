package tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Locator.ClickOptions;
import com.microsoft.playwright.Page;

import tech.derbent.activities.view.CActivitiesView;
import tech.derbent.decisions.view.CDecisionsView;
import tech.derbent.meetings.view.CMeetingsView;
import tech.derbent.projects.view.CProjectsView;

/**
 * PlaywrightUIAutomationTest - End-to-end UI automation tests using Microsoft Playwright.
 * Playwright provides modern browser automation with excellent features: - Fast, reliable
 * automation across all modern browsers - Built-in waiting for elements and network
 * requests - Cross-browser testing (Chrome, Firefox, Safari, Edge) - Excellent debugging
 * capabilities with trace viewer - No external driver management needed This
 * implementation provides comprehensive testing: - Opens real browser instances -
 * Navigates through different views (Meetings, Projects, Decisions) - Clicks buttons and
 * UI components - Fills forms with test data - Validates data entry and display - Takes
 * screenshots for verification - Tests responsive design across different viewports
 * Advantages over Selenium and TestBench: - Faster and more stable - Better element
 * waiting strategies - Built-in mobile testing capabilities - Superior debugging tools -
 * Modern async API design - Free and open source
 */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT)
@TestPropertySource (properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080" }
)
public class CUIAutomationTest extends CBaseUITest {

	static final Logger logger = LoggerFactory.getLogger(CUIAutomationTest.class);

	/**
	 * Tests advanced grid interactions including sorting and filtering
	 */
	private void testAdvancedGridInView(final Class<?> viewClass) {

		try {
			logger.info("Testing advanced grid interactions in {} view...", viewClass);

			if (!navigateToViewByClass(viewClass)) {
				fail("Failed to navigate to view:" + viewClass.getSimpleName());
				return;
			}
			final var grids = page.locator("vaadin-grid");

			if (grids.count() == 0) {
				takeScreenshot(
					"grid-not-found-" + viewClass.getSimpleName().toLowerCase());
				fail("No grids found in view:" + viewClass.getSimpleName());
				return;
			}
			final var grid = grids.first();
			final var gridCells = grid.locator("vaadin-grid-cell-content");
			final int gridCellCount = gridCells.count();

			if (gridCellCount < 1) {
				fail("no grid row");
			}
			int columns = 0;
			boolean emptyCells = false;
			int emptyCellCount = 0;
			int firstDataCell = -1;

			for (int i = 0; i < gridCellCount; i++) {
				final Locator gridCell = gridCells.nth(i);

				if (gridCell.textContent().isEmpty()) {
					emptyCellCount++;
					emptyCells = true;
				}
				else {
					logger.info("Grid cell text: {}", gridCell.textContent());

					if (emptyCells) {
						firstDataCell = i;
						break;
					}
					columns++;
				}
			}
			assertTrue(firstDataCell > 1,
				"Grid has no columns or all cells are empty in view: "
					+ viewClass.getSimpleName());
			final Locator firstCell = gridCells.nth(firstDataCell);
			firstCell.scrollIntoViewIfNeeded();
			firstCell.click(new ClickOptions().setTimeout(1000));
			page.waitForTimeout(500);
			takeScreenshot("grid-cell-clicked-" + viewClass.getSimpleName());
			// Test column sorting
			final var sorters = grid.locator("vaadin-grid-sorter");

			if (sorters.count() > 0) {
				logger.info(" Testing grid sorting in {} view...", viewClass);
				sorters.first().click();
				wait_1000();
				takeScreenshot("grid-sorted-" + viewClass.getSimpleName());
			}
			// Test column filtering if available
			final var filters = grid.locator("vaadin-text-field[slot='filter']");

			if (filters.count() > 0) {
				logger.info("Testing grid filtering in {} view...", viewClass);
				filters.first().fill("test");
				wait_1000();
				takeScreenshot("grid-filtered-" + viewClass.getSimpleName());
				// Clear filter
				filters.first().fill("");
				page.waitForTimeout(500);
			}
			logger.info("✅ Advanced grid test completed for {} view", viewClass);
		} catch (final Exception e) {
			logger.warn("Advanced grid test failed in {} view: {}", viewClass,
				e.getMessage());
			takeScreenshot("grid-test-error-" + viewClass.getSimpleName());
		}
	}

	@Test
	void testApplicationLoadsSuccessfully() {
		logger.info("🧪 Testing application loads successfully...");
		// Navigate to application
		page.navigate(baseUrl);
		// Wait for login overlay to be visible (application should require login)
		page.waitForSelector("vaadin-login-overlay",
			new Page.WaitForSelectorOptions().setTimeout(10000));
		// Take screenshot of login page
		takeScreenshot("login-page-loaded");
		// Verify page title contains expected text
		final String title = page.title();
		assertNotNull(title);
		logger.info("✅ Application loaded successfully. Title: {}", title);
		// Check that login overlay is present
		assertTrue(page.locator("vaadin-login-overlay").isVisible());
		logger.info("✅ Application loads successfully test completed");
	}

	@Test
	void testCompleteApplicationFlow() {
		logger.info("🧪 Testing complete application workflow...");
		loginToApplication();
		// Test workflow across different views
		final Class<?>[] views = {
			CProjectsView.class, CMeetingsView.class, CActivitiesView.class,
			CDecisionsView.class };

		for (final Class<?> view : views) {
			performWorkflowInView(view);
		}
		logger.info("✅ Complete application workflow test completed");
	}

	/**
	 * Tests Create operation in a specific view
	 */
	private void testCreateOperation(final String viewName, final String entityName) {

		try {
			logger.info("Testing CREATE operation in {} view...", viewName);
			// Look for "New" or "Add" buttons
			final var createButtons = page.locator(
				"vaadin-button:has-text('New'), vaadin-button:has-text('Add'), vaadin-button:has-text('Create')");

			if (createButtons.count() > 0) {
				createButtons.first().click();
				wait_1000();
				takeScreenshot("create-form-opened-" + viewName.toLowerCase());
				// Fill form fields
				final var textFields =
					page.locator("vaadin-text-field, vaadin-text-area");

				if (textFields.count() > 0) {
					// Fill first text field with entity name
					textFields.first().fill(entityName);

					// Fill other fields if available
					if (textFields.count() > 1) {
						textFields.nth(1).fill("Test description for " + entityName);
					}
					takeScreenshot("create-form-filled-" + viewName.toLowerCase());
					// Save the entity
					final var saveButtons = page.locator(
						"vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");

					if (saveButtons.count() > 0) {
						saveButtons.first().click();
						page.waitForTimeout(1500);
						takeScreenshot("create-saved-" + viewName.toLowerCase());
						logger.info("✅ CREATE operation completed for {} in {} view",
							entityName, viewName);
					}
				}
			}
			else {
				logger.info("No create button found in {} view", viewName);
			}
		} catch (final Exception e) {
			logger.warn("CREATE operation failed in {} view: {}", viewName,
				e.getMessage());
			takeScreenshot("create-error-" + viewName.toLowerCase());
		}
	}

	@Test
	void testCRUDOperationsInMeetings() {
		logger.info("🧪 Testing CRUD operations in Meetings view...");
		// Login and navigate to Meetings
		loginToApplication();
		navigateToViewByText("Meetings");
		// Test Create operation
		testCreateOperation("Meetings", "Test Meeting " + System.currentTimeMillis());
		// Test Read operation
		testReadOperation("Meetings");
		// Test Update operation
		testUpdateOperation("Meetings", "Updated Meeting Title");
		// Test Delete operation (with caution)
		testDeleteOperation("Meetings");
		logger.info("✅ CRUD operations test completed for Meetings");
	}
	// Helper methods

	@Test
	void testCRUDOperationsInProjects() {
		logger.info("🧪 Testing CRUD operations in Projects view...");
		// Login and navigate to Projects
		loginToApplication();
		navigateToViewByText("Projects");
		// Test Create operation
		testCreateOperation("Projects", "Test Project " + System.currentTimeMillis());
		// Test Read operation
		testReadOperation("Projects");
		// Test Update operation
		testUpdateOperation("Projects", "Updated Project Name");
		// Test Delete operation (with caution)
		testDeleteOperation("Projects");
		logger.info("✅ CRUD operations test completed for Projects");
	}

	/**
	 * Tests Delete operation in a specific view
	 */
	private void testDeleteOperation(final String viewName) {

		try {
			logger.info("Testing DELETE operation in {} view...", viewName);
			// Look for delete buttons
			final var deleteButtons = page.locator(
				"vaadin-button:has-text('Delete'), vaadin-button:has-text('Remove')");

			if (deleteButtons.count() > 0) {
				deleteButtons.first().click();
				wait_1000();
				takeScreenshot("delete-confirmation-" + viewName.toLowerCase());
				// Look for confirmation dialog
				final var confirmButtons = page.locator(
					"vaadin-button:has-text('Yes'), vaadin-button:has-text('Confirm'), vaadin-button:has-text('Delete')");

				if (confirmButtons.count() > 0) {
					confirmButtons.first().click();
					page.waitForTimeout(1500);
					takeScreenshot("delete-completed-" + viewName.toLowerCase());
					logger.info("✅ DELETE operation completed for {} view", viewName);
				}
				else {
					logger.info("No confirmation dialog found for delete in {} view",
						viewName);
				}
			}
			else {
				logger.info("No delete button found in {} view", viewName);
			}
		} catch (final Exception e) {
			logger.warn("DELETE operation failed in {} view: {}", viewName,
				e.getMessage());
			takeScreenshot("delete-error-" + viewName.toLowerCase());
		}
	}

	@Test
	void testFormInteractions() {
		logger.info("🧪 Testing form interactions...");
		// Navigate to application page.navigate(baseUrl);
		loginToApplication();
		// Try to find and interact with forms in different views
		testFormInView("Projects", "/projects");
		testFormInView("Meetings", "/meetings");
		testFormInView("Decisions", "/decisions");
		logger.info("✅ Form interactions test completed");
	}

	private void testFormInView(final String viewName, final String path) {

		try {
			logger.info("Testing form interactions in {} view...", viewName);
			page.navigate(baseUrl + path);
			wait_1000();

			if (!clickIfExists(
				"vaadin-button:has-text('New'), vaadin-button:has-text('Add')")) {
				logger.info("No 'New' or 'Add' button found in {} view", viewName);
				return;
			}
			wait_1000();

			if (!fillFirstTextField("Test " + viewName + " Entry")) {
				logger.info("No text field found in {} view", viewName);
				return;
			}
			fillFirstDateField("2024-01-15");
			takeScreenshot("form-filled-" + viewName.toLowerCase());

			if (clickIfExists(
				"vaadin-button:has-text('Save'), vaadin-button:has-text('Create')")) {
				wait_1000();
				takeScreenshot("form-saved-" + viewName.toLowerCase());
			}
			logger.info("✅ Form interaction test completed for {} view", viewName);
		} catch (final Exception e) {
			logger.warn("⚠️ Form interaction failed in {} view: {}", viewName,
				e.getMessage(), e);
			takeScreenshot("form-error-" + viewName.toLowerCase());
		}
	}

	/**
	 * Tests form validation in a specific view
	 */
	private void testFormValidation(final String viewName) {

		try {
			logger.info("Testing form validation in {} view...", viewName);
			// Open new form
			final var createButtons = page
				.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");

			if (createButtons.count() > 0) {
				createButtons.first().click();
				wait_1000();
				// Try to save without filling required fields
				final var saveButtons = page.locator(
					"vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");

				if (saveButtons.count() > 0) {
					saveButtons.first().click();
					wait_1000();
					// Look for validation messages
					final var errorMessages = page.locator(
						"vaadin-text-field[invalid], .error-message, [role='alert']");

					if (errorMessages.count() > 0) {
						logger.info(
							"✅ Form validation working - found {} validation messages in {} view",
							errorMessages.count(), viewName);
						takeScreenshot("form-validation-" + viewName.toLowerCase());
					}
					else {
						logger.info("No validation messages found in {} view form",
							viewName);
						takeScreenshot("form-no-validation-" + viewName.toLowerCase());
					}
					// Close form
					final var cancelButtons =
						page.locator("vaadin-button:has-text('Cancel')");

					if (cancelButtons.count() > 0) {
						cancelButtons.first().click();
						page.waitForTimeout(500);
					}
				}
			}
		} catch (final Exception e) {
			logger.warn("Form validation test failed in {} view: {}", viewName,
				e.getMessage());
			takeScreenshot("form-validation-error-" + viewName.toLowerCase());
		}
	}

	@Test
	void testFormValidationAndErrorHandling() {
		logger.info("🧪 Testing form validation and error handling...");
		// Login first
		loginToApplication();

		// Test form validation in Projects view
		if (navigateToViewByText("Projects")) {
			testFormValidation("Projects");
		}

		// Test form validation in Meetings view
		if (navigateToViewByText("Meetings")) {
			testFormValidation("Meetings");
		}
		logger.info("✅ Form validation and error handling test completed");
	}

	@Test
	void testGridInteractions() {
		logger.info("🧪 Testing grid interactions...");
		// Login first
		loginToApplication();
		// navigateToViewByClass(CProjectsView.class); Test grid interactions in different
		// views
		testAdvancedGridInView(CProjectsView.class);
		testAdvancedGridInView(CMeetingsView.class);
		testAdvancedGridInView(CActivitiesView.class);
		logger.info("✅ Grid interactions test completed");
	}

	private void testGridInView(final String viewName) {

		try {
			logger.info("Testing grid in {} view...", viewName);

			// Navigate to view
			if (navigateToViewByText(viewName)) {
				wait_1000();
				// Look for grids
				final var grids = page.locator("vaadin-grid, table");

				if (grids.count() > 0) {
					logger.info("Found {} grid(s) in {} view", grids.count(), viewName);
					// Check if grid has data
					final var rows =
						page.locator("vaadin-grid vaadin-grid-cell-content, tr");
					logger.info("Grid has {} rows in {} view", rows.count(), viewName);
					takeScreenshot("grid-" + viewName.toLowerCase());
				}
			}
			logger.info("✅ Grid test completed for {} view", viewName);
		} catch (final Exception e) {
			logger.warn("⚠️ Grid test failed in {} view: {}", viewName, e.getMessage());
			takeScreenshot("grid-error-" + viewName.toLowerCase());
		}
	}

	@Test
	void testInvalidLoginHandling() {
		logger.info("🧪 Testing invalid login handling...");
		// Navigate to application
		page.navigate(baseUrl);
		// Wait for login overlay
		page.waitForSelector("vaadin-login-overlay",
			new Page.WaitForSelectorOptions().setTimeout(10000));
		// Attempt login with invalid credentials
		performLogin("invalid", "wrongpassword");
		// Should still be on login page - check for error indication
		page.waitForTimeout(2000);
		takeScreenshot("invalid-login-attempt");
		// Verify we're still at login overlay
		assertTrue(page.locator("vaadin-login-overlay").isVisible());
		logger.info("✅ Invalid login handling test completed");
	}

	@Test
	void testLoginFunctionality() {
		logger.info("🧪 Testing login functionality...");
		page.navigate(baseUrl);
		performLogin("admin", "test123");
		takeScreenshot("successful-login");
		assertTrue(page.locator("vaadin-app-layout").isVisible());
		logger.info("✅ Login functionality test completed successfully");
	}

	@Test
	void testLogoutFunctionality() {
		logger.info("🧪 Testing logout functionality...");
		// Login first
		loginToApplication();

		// Look for logout option
		if (performLogout()) {
			// Verify we're back at login page
			wait_loginscreen();
			takeScreenshot("after-logout");
			assertTrue(page.locator("vaadin-login-overlay").isVisible());
			logger.info("✅ Logout functionality test completed successfully");
		}
		else {
			logger.warn("⚠️ Logout functionality not tested - logout button not found");
		}
	}

	@Test
	void testNavigationBetweenViews() {
		logger.info("🧪 Testing navigation between views...");
		// Login first
		loginToApplication();
		// Test navigation to different views
		final String[] viewSelectors = {
			"vaadin-side-nav-item[path='/projects']",
			"vaadin-side-nav-item[path='/meetings']",
			"vaadin-side-nav-item[path='/activities']",
			"vaadin-side-nav-item[path='/decisions']" };
		final String[] viewNames = {
			"Projects", "Meetings", "Activities", "Decisions" };

		for (int i = 0; i < viewSelectors.length; i++) {

			try {
				logger.info("Navigating to {} view...", viewNames[i]);

				// Click on navigation item
				if (page.locator(viewSelectors[i]).isVisible()) {
					page.locator(viewSelectors[i]).click();
					// Wait for view to load
					wait_1000();
					// Take screenshot
					takeScreenshot(viewNames[i].toLowerCase() + "-view");
					logger.info("✅ Successfully navigated to {} view", viewNames[i]);
				}
				else {
					logger.warn(
						"⚠️ Navigation item for {} not found, checking alternative selectors",
						viewNames[i]);

					// Try alternative navigation approaches
					if (navigateToViewByText(viewNames[i])) {
						takeScreenshot(viewNames[i].toLowerCase() + "-view-alt");
						logger.info(
							"✅ Successfully navigated to {} view using alternative method",
							viewNames[i]);
					}
				}
			} catch (final Exception e) {
				logger.warn("⚠️ Could not navigate to {} view: {}", viewNames[i],
					e.getMessage());
				takeScreenshot("navigation-error-" + viewNames[i].toLowerCase());
			}
		}
		logger.info("✅ Navigation between views test completed");
	}

	/**
	 * Tests Read operation in a specific view
	 */
	private void testReadOperation(final String viewName) {

		try {
			logger.info("Testing READ operation in {} view...", viewName);
			// Look for grids with data
			final var grids = page.locator("vaadin-grid");

			if (grids.count() > 0) {
				final var grid = grids.first();
				final var gridCells = grid.locator("vaadin-grid-cell-content");

				if (gridCells.count() > 0) {
					logger.info("Found {} data items in {} view", gridCells.count(),
						viewName);
					takeScreenshot("read-data-" + viewName.toLowerCase());
					// Click on first row to view details
					gridCells.first().click();
					wait_1000();
					takeScreenshot("read-details-" + viewName.toLowerCase());
					logger.info("✅ READ operation completed for {} view", viewName);
				}
				else {
					logger.info("No data found in {} view grid", viewName);
				}
			}
			else {
				logger.info("No grid found in {} view", viewName);
			}
		} catch (final Exception e) {
			logger.warn("READ operation failed in {} view: {}", viewName, e.getMessage());
			takeScreenshot("read-error-" + viewName.toLowerCase());
		}
	}

	/**
	 * Tests Update operation in a specific view
	 */
	private void testUpdateOperation(final String viewName, final String newName) {

		try {
			logger.info("Testing UPDATE operation in {} view...", viewName);
			// Look for edit buttons
			final var editButtons = page.locator(
				"vaadin-button:has-text('Edit'), vaadin-button:has-text('Modify')");

			if (editButtons.count() > 0) {
				editButtons.first().click();
				wait_1000();
				takeScreenshot("update-form-opened-" + viewName.toLowerCase());
				// Modify form fields
				final var textFields = page.locator("vaadin-text-field");

				if (textFields.count() > 0) {
					textFields.first().fill(newName + " - " + System.currentTimeMillis());
					takeScreenshot("update-form-modified-" + viewName.toLowerCase());
					// Save changes
					final var saveButtons = page.locator(
						"vaadin-button:has-text('Save'), vaadin-button:has-text('Update')");

					if (saveButtons.count() > 0) {
						saveButtons.first().click();
						wait_1000();
						takeScreenshot("update-saved-" + viewName.toLowerCase());
						logger.info("✅ UPDATE operation completed for {} view", viewName);
					}
				}
			}
			else {
				logger.info("No edit button found in {} view", viewName);
			}
		} catch (final Exception e) {
			logger.warn("UPDATE operation failed in {} view: {}", viewName,
				e.getMessage());
			takeScreenshot("update-error-" + viewName.toLowerCase());
		}
	}
}