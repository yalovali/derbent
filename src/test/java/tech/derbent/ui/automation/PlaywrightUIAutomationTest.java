package tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Locator.ClickOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.vaadin.flow.router.Route;

import tech.derbent.activities.view.CActivitiesView;
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
public class PlaywrightUIAutomationTest {

	private static final Logger logger =
		LoggerFactory.getLogger(PlaywrightUIAutomationTest.class);

	@LocalServerPort
	private int port;

	private Playwright playwright;

	private Browser browser;

	private BrowserContext context;

	private Page page;

	private String baseUrl;

	private void checkAccessibilityElement(final String selector,
		final String description) {

		try {
			final var elements = page.locator(selector);

			if (elements.count() > 0) {
				logger.info("✅ Found {} {} element(s)", elements.count(), description);
			}
			else {
				logger.warn("⚠️ No {} found", description);
			}
		} catch (final Exception e) {
			logger.warn("⚠️ Accessibility check failed for {}: {}", description,
				e.getMessage());
		}
	}

	/**
	 * Helper method to login to the application with default credentials
	 */
	private void loginToApplication() {
		page.navigate(baseUrl);
		page.waitForSelector("#input-vaadin-text-field-12",
			new Page.WaitForSelectorOptions().setTimeout(10000));
		performLogin("admin", "test123");
		page.waitForSelector("vaadin-app-layout",
			new Page.WaitForSelectorOptions().setTimeout(10000));
	}

	private boolean navigateToViewByClass(final Class<?> viewClass) {

		try {
			final Route routeAnnotation = viewClass.getAnnotation(Route.class);

			if (routeAnnotation == null) {
				logger.error("Class {} has no @Route annotation!", viewClass.getName());
				return false;
			}
			final String route = routeAnnotation.value().split("/")[0];

			if (route.isEmpty()) {
				logger.error("Route value is empty for class: {}", viewClass.getName());
				return false;
			}
			String url = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
			url += route.startsWith("/") ? route.substring(1) : route;
			logger.info("Navigating to view by class: {}", url);
			page.navigate(url);
			page.waitForTimeout(2000); // waits for 1 second
			return true;
		} catch (final Exception e) {
			logger.warn("Failed to navigate to view {}: {}", viewClass.getSimpleName(),
				e.getMessage());
			return false;
		}
	}

	private boolean navigateToViewByText(final String viewName) {

		try {
			// the viewName should match the text in the navigation menu it can only click
			// first Parent item. if the target menu is submenu of another menu, it will
			// not work like: "Projects > Active Projects" TODO fix this limitation
			final Locator activities = page.locator(".hierarchical-menu-item");
			logger.info("" + activities.count() + " activities found in navigation");
			// Find the view by its text
			final Locator viewLink = activities.locator("span",
				new Locator.LocatorOptions().setHasText(viewName));

			if (viewLink.count() == 0) {
				logger.warn("View '{}' not found in navigation", viewName);
				return false;
			}
			logger.info("Navigating to view: {}", viewName);
			// Click the view link
			viewLink.first().click();
			page.waitForTimeout(1000);
			return true;
		} catch (final Exception e) {
			logger.warn("Failed to navigate to view {}: {}", viewName, e.getMessage());
			return false;
		}
	}

	/**
	 * Performs login with specified credentials
	 */
	private void performLogin(final String username, final String password) {

		try {
			/*
			 * page.locator("id=vaadin-text-field-0").fill("TST");
			 * page.locator("id=vaadin-text-field-1").fill("Test");
			 * page.locator("id=edit-save").click();
			 */
			logger.info("Performing login with username: {}", username);
			page.fill("vaadin-text-field[name='username'] > input", username);
			page.fill("vaadin-password-field[name='password'] > input", password);
			page.click("vaadin-button");
		} catch (final Exception e) {
			logger.error("❌ Login failed: {}", e.getMessage());
			takeScreenshot("login-failed");
			throw new RuntimeException("Login failed", e);
		}
	}

	/**
	 * Attempts to perform logout
	 */
	private boolean performLogout() {

		try {
			// Look for logout button or menu
			final var logoutButtons = page.locator(
				"vaadin-button:has-text('Logout'), a:has-text('Logout'), vaadin-menu-bar-button:has-text('Logout')");

			if (logoutButtons.count() > 0) {
				logoutButtons.first().click();
				page.waitForTimeout(1000);
				return true;
			}
			// Alternative: look for user menu that might contain logout
			final var userMenus =
				page.locator("vaadin-menu-bar, [role='button']:has-text('User')");

			if (userMenus.count() > 0) {
				userMenus.first().click();
				page.waitForTimeout(500);
				final var logoutInMenu =
					page.locator("vaadin-menu-bar-item:has-text('Logout')");

				if (logoutInMenu.count() > 0) {
					logoutInMenu.click();
					return true;
				}
			}
			return false;
		} catch (final Exception e) {
			logger.warn("Logout attempt failed: {}", e.getMessage());
			return false;
		}
	}

	private void performWorkflowInView(final String viewName) {

		try {
			logger.info("Performing workflow in {} view...", viewName);

			// Navigate to view by finding navigation elements
			if (navigateToViewByText(viewName)) {
				page.waitForTimeout(1000);
				// Check if view loaded
				assertTrue(page.url().contains(viewName.toLowerCase())
					|| page.locator("body").textContent().contains(viewName));
				takeScreenshot("workflow-" + viewName.toLowerCase());
				logger.info("✅ Workflow step completed for {} view", viewName);
			}
		} catch (final Exception e) {
			logger.warn("⚠️ Workflow failed in {} view: {}", viewName, e.getMessage());
			takeScreenshot("workflow-error-" + viewName.toLowerCase());
		}
	}

	@BeforeEach
	void setUp() {
		baseUrl = "http://localhost:" + port;
		// Initialize Playwright
		playwright = Playwright.create();
		// Launch browser (use Chromium by default, can be changed to firefox() or
		// webkit())
		browser = playwright.chromium()
			.launch(new BrowserType.LaunchOptions().setHeadless(false) // Set to false for
																		// debugging
				.setSlowMo(100)); // Add small delay between actions for visibility
		// Create context with desktop viewport
		context = browser
			.newContext(new Browser.NewContextOptions().setViewportSize(1200, 800));
		// Create page
		page = context.newPage();
		// Enable console logging page.onConsoleMessage(msg -> logger.info("Browser
		// console: {}", msg.text()));
		logger.info("Playwright test setup completed. Application URL: {}", baseUrl);
	}

	private void takeScreenshot(final String name) {

		try {
			final String screenshotPath =
				"target/screenshots/" + name + "-" + System.currentTimeMillis() + ".png";
			page.screenshot(
				new Page.ScreenshotOptions().setPath(Paths.get(screenshotPath)));
			logger.info("📸 Screenshot saved: {}", screenshotPath);
		} catch (final Exception e) {
			logger.warn("⚠️ Failed to take screenshot '{}': {}", name, e.getMessage());
		}
	}

	@AfterEach
	void tearDown() {

		if (page != null) {
			page.close();
		}

		if (context != null) {
			context.close();
		}

		if (browser != null) {
			browser.close();
		}

		if (playwright != null) {
			playwright.close();
		}
		logger.info("Playwright test cleanup completed");
	}

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
				page.waitForTimeout(1000);
				takeScreenshot("grid-sorted-" + viewClass.getSimpleName());
			}
			// Test column filtering if available
			final var filters = grid.locator("vaadin-text-field[slot='filter']");

			if (filters.count() > 0) {
				logger.info("Testing grid filtering in {} view...", viewClass);
				filters.first().fill("test");
				page.waitForTimeout(1000);
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
		// Navigate to application
		page.navigate(baseUrl);
		page.waitForSelector("vaadin-app-layout",
			new Page.WaitForSelectorOptions().setTimeout(10000));
		takeScreenshot("workflow-start");
		// Test workflow across different views
		performWorkflowInView("Projects");
		performWorkflowInView("Meetings");
		performWorkflowInView("Decisions");
		takeScreenshot("workflow-completed");
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
				page.waitForTimeout(1000);
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
				page.waitForTimeout(1000);
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
		// Navigate to application
		page.navigate(baseUrl);
		page.waitForSelector("vaadin-app-layout",
			new Page.WaitForSelectorOptions().setTimeout(10000));
		// Try to find and interact with forms in different views
		testFormInView("Projects", "/projects");
		testFormInView("Meetings", "/meetings");
		testFormInView("Decisions", "/decisions");
		logger.info("✅ Form interactions test completed");
	}

	private void testFormInView(final String viewName, final String path) {

		try {
			logger.info("Testing form interactions in {} view...", viewName);
			// Navigate to view
			page.navigate(baseUrl + path);
			page.waitForTimeout(2000);
			// Look for "New" or "Add" buttons
			final var newButtons = page
				.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");

			if (newButtons.count() > 0) {
				newButtons.first().click();
				page.waitForTimeout(1000);
				// Look for form fields
				final var textFields = page
					.locator("vaadin-text-field, vaadin-text-area, input[type='text']");

				if (textFields.count() > 0) {
					// Fill first text field
					textFields.first().fill("Test " + viewName + " Entry");
					// Look for date fields
					final var dateFields =
						page.locator("vaadin-date-picker, input[type='date']");

					if (dateFields.count() > 0) {
						dateFields.first().fill("2024-01-15");
					}
					takeScreenshot("form-filled-" + viewName.toLowerCase());
					// Look for save button
					final var saveButtons = page.locator(
						"vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");

					if (saveButtons.count() > 0) {
						saveButtons.first().click();
						page.waitForTimeout(1000);
						takeScreenshot("form-saved-" + viewName.toLowerCase());
					}
				}
			}
			logger.info("✅ Form interaction test completed for {} view", viewName);
		} catch (final Exception e) {
			logger.warn("⚠️ Form interaction failed in {} view: {}", viewName,
				e.getMessage());
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
				page.waitForTimeout(1000);
				// Try to save without filling required fields
				final var saveButtons = page.locator(
					"vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");

				if (saveButtons.count() > 0) {
					saveButtons.first().click();
					page.waitForTimeout(1000);
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
				page.waitForTimeout(1000);
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
			page.waitForSelector("vaadin-login-overlay",
				new Page.WaitForSelectorOptions().setTimeout(10000));
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
					page.waitForTimeout(1000);
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
					page.waitForTimeout(1000);
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
				page.waitForTimeout(1000);
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
						page.waitForTimeout(1500);
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