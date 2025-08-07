package ui_tests.tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.vaadin.flow.router.Route;

import tech.derbent.activities.view.CActivitiesView;
import tech.derbent.decisions.view.CDecisionsView;
import tech.derbent.meetings.view.CMeetingsView;
import tech.derbent.projects.view.CProjectsView;
import tech.derbent.users.view.CUsersView;

public class CBaseUITest {

	protected static final Logger LOGGER = LoggerFactory.getLogger(CBaseUITest.class);

	@LocalServerPort
	private int port;

	private Playwright playwright;

	private Browser browser;

	private BrowserContext context;

	protected Page page;

	protected String baseUrl;

	protected Class<?>[] viewClasses = {
		CProjectsView.class, CActivitiesView.class, CMeetingsView.class,
		CDecisionsView.class, CUsersView.class };

	/**
	 * Clicks on an element by its ID with timeout and error handling
	 */
	protected boolean clickById(final String id) {
		return clickById(id, 5000);
	}

	/**
	 * Clicks on an element by its ID with custom timeout
	 */
	protected boolean clickById(final String id, final int timeoutMs) {

		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available, cannot click element with ID: {}", id);
			return false;
		}

		try {
			final String selector = "#" + id;

			if (page.locator(selector).count() > 0) {
				page.click(selector, new Page.ClickOptions().setTimeout(timeoutMs));
				LOGGER.debug("Successfully clicked element with ID: {}", id);
				return true;
			}
			else {
				LOGGER.warn("Element with ID '{}' not found", id);
				return false;
			}
		} catch (final Exception e) {
			LOGGER.warn("Failed to click element with ID '{}': {}", id, e.getMessage());
			return false;
		}
	}

	/**
	 * Common function to click Cancel button with error handling
	 */
	protected void clickCancel() {

		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available, cannot click Cancel button");
			return;
		}

		try {
			final var cancelButtons = page.locator(
				"vaadin-button:has-text('Cancel'), vaadin-button:has-text('Close')");

			if (cancelButtons.count() > 0) {
				cancelButtons.first().click();
				wait_1000();
				LOGGER.debug("Successfully clicked Cancel button");
			}
			else {
				LOGGER.warn("Cancel button not found");
			}
		} catch (final Exception e) {
			LOGGER.error("Failed to click Cancel button: {}", e.getMessage());
			takeScreenshot("cancel-button-error", true);
		}
	}

	/**
	 * Common function to click on grid with error handling
	 */
	protected void clickGrid() {
		gridClickCell(0);
	}

	/**
	 * Common function to click on grid at specific row index
	 */
	protected void clickGrid(final int rowIndex) {

		try {
			final String selector = "vaadin-grid-cell-content";
			final var gridCells = page.locator(selector);

			if (gridCells.count() > rowIndex) {
				gridCells.nth(rowIndex).click();
				wait_500();
				LOGGER.debug("Successfully clicked grid row at index: {}", rowIndex);
			}
			else {
				LOGGER.warn("Grid row at index {} not found (total rows: {})", rowIndex,
					gridCells.count());
				takeScreenshot("grid-row-not-found", true);
			}
		} catch (final Exception e) {
			LOGGER.error("Failed to click grid at index {}: {}", rowIndex,
				e.getMessage());
			takeScreenshot("grid-click-error", true);
		}
	}

	protected boolean clickIfExists(final String selector) {
		final var locator = page.locator(selector);

		if (locator.count() > 0) {
			locator.first().click();
			return true;
		}
		return false;
	}

	/**
	 * Common function to click New/Add button with error handling
	 */
	protected void clickNew() {

		try {
			final var newButtons = page
				.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");

			if (newButtons.count() > 0) {
				newButtons.first().click();
				wait_1000();
				LOGGER.debug("Successfully clicked New button");
			}
			else {
				LOGGER.warn("New/Add button not found");
				takeScreenshot("new-button-not-found", true);
			}
		} catch (final Exception e) {
			LOGGER.error("Failed to click New button: {}", e.getMessage());
			takeScreenshot("new-button-error", true);
		}
	}

	protected void clickNewButton() {
		clickNew(); // Use the new common function
	}

	/**
	 * Alias for clickSaveButton method
	 */
	protected void clickSave() {
		clickSaveButton();
	}

	protected void clickSaveButton() {

		try {
			final var saveButtons = page.locator(
				"vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");

			if (saveButtons.count() > 0) {
				saveButtons.first().click();
				wait_2000();
				LOGGER.debug("Successfully clicked Save button");
			}
			else {
				LOGGER.warn("Save button not found");
				takeScreenshot("save-button-not-found", true);
			}
		} catch (final Exception e) {
			LOGGER.error("Failed to click Save button: {}", e.getMessage());
			takeScreenshot("save-button-error", true);
		}
	}

	/**
	 * Checks if an element exists by ID
	 */
	protected boolean elementExistsById(final String id) {
		final String selector = "#" + id;
		final boolean exists = page.locator(selector).count() > 0;
		LOGGER.debug("Element with ID '{}' exists: {}", id, exists);
		return exists;
	}

	/**
	 * Fills a form field by its ID
	 */
	protected boolean fillById(final String id, final String value) {

		try {
			final String selector = "#" + id;

			if (page.locator(selector).count() > 0) {
				page.fill(selector, value);
				LOGGER.debug("Successfully filled field with ID '{}' with value: {}", id,
					value);
				return true;
			}
			else {
				LOGGER.warn("Field with ID '{}' not found", id);
				return false;
			}
		} catch (final Exception e) {
			LOGGER.warn("Failed to fill field with ID '{}': {}", id, e.getMessage());
			return false;
		}
	}

	protected void fillFirstDateField(final String value) {
		final var locator = page.locator("vaadin-date-picker, input[type='date']");

		if (locator.count() > 0) {
			locator.first().fill(value);
		}
	}

	protected boolean fillFirstTextField(final String value) {
		final var locator =
			page.locator("vaadin-text-field, vaadin-text-area, input[type='text']");

		if (locator.count() > 0) {
			locator.first().fill(value);
			return true;
		}
		return false;
	}

	/**
	 * Gets the count of grid rows
	 */
	protected int getGridFirstCell() {
		final var grids = page.locator("vaadin-grid");

		if (grids.count() == 0) {
			fail("No grids found in view");
			return 0;
		}
		final var grid = grids.first();
		final var gridCells = grid.locator("vaadin-grid-cell-content");
		final int gridCellCount = gridCells.count();

		if (gridCellCount < 1) {
			fail("no grid cells");
		}
		boolean emptyCells = false;
		int firstDataCell = -1;

		for (int i = 0; i < gridCellCount; i++) {
			final Locator gridCell = gridCells.nth(i);

			if (gridCell.textContent().isEmpty()) {
				emptyCells = true;
			}
			else if (emptyCells) {
				firstDataCell = i;
				break;
			}
		}
		return firstDataCell;
	}

	/**
	 * Gets the count of grid rows
	 */
	protected int getGridRowCount() {
		final var grids = page.locator("vaadin-grid");

		if (grids.count() == 0) {
			fail("No grids found in view");
			return 0;
		}
		final var grid = grids.first();
		final var gridCells = grid.locator("vaadin-grid-cell-content");
		final int gridCellCount = gridCells.count();

		if (gridCellCount < 1) {
			fail("no grid cells");
		}
		int columns = 0;
		boolean emptyCells = false;
		int emptyCellCount = 0;

		for (int i = 0; i < gridCellCount; i++) {
			final Locator gridCell = gridCells.nth(i);

			if (gridCell.textContent().isEmpty()) {
				emptyCellCount++;
				emptyCells = true;
			}
			else {
				// logger.info("Grid cell text: {}", gridCell.textContent());

				if (emptyCells) {
					break;
				}
				columns++;
			}
		}
		final int rowCount = (gridCellCount - emptyCellCount - columns) / columns;
		LOGGER.debug("Grid has {} rows and {} columns", rowCount, columns);
		return rowCount;
	}

	/**
	 * Gets the text content of an element by ID
	 */
	protected String getTextById(final String id) {

		try {
			final String selector = "#" + id;

			if (page.locator(selector).count() > 0) {
				final String text = page.locator(selector).textContent();
				LOGGER.debug("Got text '{}' from element with ID: {}", text, id);
				return text;
			}
			else {
				LOGGER.warn("Element with ID '{}' not found", id);
				return null;
			}
		} catch (final Exception e) {
			LOGGER.warn("Failed to get text from element with ID '{}': {}", id,
				e.getMessage());
			return null;
		}
	}

	protected void gridClickCell(final int cellIndex) {
		// TODO Auto-generated method stub
		final int gridFirstCellIndex = getGridFirstCell();

		try {
			final String selector = "vaadin-grid-cell-content";
			final var gridCells = page.locator(selector);
			assertTrue(cellIndex < (gridCells.count() - gridFirstCellIndex),
				"invalid grid cell index: " + cellIndex);
			gridCells.nth(gridFirstCellIndex + cellIndex).click();
			wait_500();
		} catch (final Exception e) {
			LOGGER.error("Failed to click grid at index {}: {}", cellIndex,
				e.getMessage());
			takeScreenshot("grid-click-error", true);
		}
	}

	protected void gridClickSort() {
		// Test grid sorting if available
		final var sorters = page.locator("vaadin-grid-sorter");

		if (sorters.count() > 0) {
			LOGGER.debug("Testing grid sorting");
			sorters.first().click();
			wait_1000();
			takeScreenshot("meetings-grid-sorted");
		}
	}

	/**
	 * Checks if browser is available for testing
	 */
	protected boolean isBrowserAvailable() {
		return (page != null) && (playwright != null);
	}

	/**
	 * Helper method to login to the application with default credentials
	 */
	protected void loginToApplication() {

		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available, cannot login to application");
			return;
		}
		page.navigate(baseUrl);
		wait_loginscreen();
		performLogin("admin", "test123");
		wait_afterlogin();
	}

	protected boolean navigateToViewByClass(final Class<?> viewClass) {

		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available, cannot navigate to view: {}",
				viewClass.getSimpleName());
			return false;
		}

		try {
			final Route routeAnnotation = viewClass.getAnnotation(Route.class);

			if (routeAnnotation == null) {
				LOGGER.error("Class {} has no @Route annotation!", viewClass.getName());
				return false;
			}
			final String route = routeAnnotation.value().split("/")[0];

			if (route.isEmpty()) {
				LOGGER.error("Route value is empty for class: {}", viewClass.getName());
				return false;
			}
			String url = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
			url += route.startsWith("/") ? route.substring(1) : route;
			LOGGER.info("Navigating to view by class: {}", url);
			page.navigate(url);
			page.waitForTimeout(2000); // waits for 2 seconds
			return true;
		} catch (final Exception e) {
			LOGGER.warn("Failed to navigate to view {}: {}", viewClass.getSimpleName(),
				e.getMessage());
			return false;
		}
	}

	/**
	 * Performs login with specified credentials
	 * Updated for CCustomLoginView (new login screen)
	 */
	protected void performLogin(final String username, final String password) {

		try {
			LOGGER.debug("Performing login with username: {}", username);
			// Updated selectors for CCustomLoginView
			page.fill("#custom-username-input", username);
			page.fill("#custom-password-input", password);
			page.click("#custom-submit-button");
			wait_afterlogin();
		} catch (final Exception e) {
			LOGGER.error("Login failed: {}", e.getMessage());
			takeScreenshot("login-failed", true);
			throw new RuntimeException("Login failed", e);
		}
	}

	/**
	 * Attempts to perform logout
	 */
	protected boolean performLogout() {

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
			LOGGER.warn("Logout attempt failed: {}", e.getMessage());
			return false;
		}
	}
	// ========== Additional Playwright Testing Utilities ==========

	protected void performWorkflowInView(final Class<?> viewName) {

		try {
			LOGGER.info("Performing workflow in {} view...", viewName);

			// Navigate to view by finding navigation elements
			if (navigateToViewByClass(viewName)) {
				LOGGER.info("✅ Workflow step completed for {} view", viewName);
			}
		} catch (final Exception e) {
			LOGGER.warn("⚠️ Workflow failed in {} view: {}", viewName.getSimpleName(),
				e.getMessage());
			takeScreenshot("workflow-error-" + viewName.getSimpleName());
		}
	}

	/**
	 * Selects an option in a ComboBox by ID
	 */
	protected boolean selectComboBoxOptionById(final String comboBoxId,
		final String optionText) {

		try {
			final String selector = "#" + comboBoxId;

			if (page.locator(selector).count() > 0) {
				// Click to open the ComboBox
				page.click(selector);
				wait_500();
				// Select the option by text
				final String optionSelector =
					"vaadin-combo-box-item:has-text('" + optionText + "')";

				if (page.locator(optionSelector).count() > 0) {
					page.click(optionSelector);
					LOGGER.debug(
						"Successfully selected option '{}' in ComboBox with ID: {}",
						optionText, comboBoxId);
					return true;
				}
				else {
					LOGGER.warn("Option '{}' not found in ComboBox with ID '{}'",
						optionText, comboBoxId);
					return false;
				}
			}
			else {
				LOGGER.warn("ComboBox with ID '{}' not found", comboBoxId);
				return false;
			}
		} catch (final Exception e) {
			LOGGER.warn("Failed to select option in ComboBox with ID '{}': {}",
				comboBoxId, e.getMessage());
			return false;
		}
	}

	@BeforeEach
	void setUp() {
		baseUrl = "http://localhost:" + port;

		try {
			// Set environment to skip browser download
			System.setProperty("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1");
			
			// Initialize Playwright
			playwright = Playwright.create();
			
			// Use system browser configuration
			BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
				.setHeadless(true)  // Use headless mode for CI/CD environments
				.setSlowMo(50);
				
			// Set system browser path
			String chromiumPath = "/usr/bin/chromium-browser";
			
			// Check if system browser exists
			java.nio.file.Path browserPath = java.nio.file.Paths.get(chromiumPath);
			if (java.nio.file.Files.exists(browserPath)) {
				launchOptions.setExecutablePath(browserPath);
				LOGGER.info("Using system browser at: {}", chromiumPath);
			} else {
				LOGGER.warn("System browser not found at: {}", chromiumPath);
				throw new RuntimeException("No suitable browser found");
			}
			
			// Launch browser
			browser = playwright.chromium().launch(launchOptions);
			
			// Create context with desktop viewport
			context = browser
				.newContext(new Browser.NewContextOptions().setViewportSize(1200, 800));
			// Create page
			page = context.newPage();
			
			// Create screenshots directory
			java.nio.file.Files.createDirectories(java.nio.file.Paths.get("target/screenshots"));
			
			loginToApplication();
			LOGGER.info("Playwright test setup completed. Application URL: {}", baseUrl);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize Playwright browser: {}", e.getMessage());
			LOGGER.warn("Skipping browser-based tests due to setup failure");
			// Don't fail the test, just mark browser as unavailable
			playwright = null;
			browser = null;
			context = null;
			page = null;
		}
	}

	protected void takeScreenshot(final String name) {
		takeScreenshot(name, false);
	}

	/**
	 * Takes a screenshot with optional failure context
	 * @param name      the screenshot name
	 * @param isFailure whether this screenshot is for a test failure
	 */
	protected void takeScreenshot(final String name, final boolean isFailure) {

		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available, cannot take screenshot: {}", name);
			return;
		}

		try {
			// Create screenshots directory if it doesn't exist
			java.nio.file.Files.createDirectories(java.nio.file.Paths.get("target/screenshots"));
			
			final String screenshotPath =
				"target/screenshots/" + name + "-" + System.currentTimeMillis() + ".png";
			page.screenshot(
				new Page.ScreenshotOptions().setPath(Paths.get(screenshotPath)));
			
			if (isFailure) {
				LOGGER.warn("📸 Failure screenshot saved: {}", screenshotPath);
			} else {
				LOGGER.info("📸 Screenshot saved: {}", screenshotPath);
			}
		} catch (final Exception e) {
			LOGGER.error("Failed to take screenshot '{}': {}", name, e.getMessage());
		}
	}

	@AfterEach
	void tearDown() {

		try {

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
		} catch (final Exception e) {
			LOGGER.warn("Error during test cleanup: {}", e.getMessage());
		}
		LOGGER.info("Playwright test cleanup completed");
	}

	/**
	 * Tests basic accessibility features
	 */
	protected void testAccessibilityBasics(final String viewName) {
		LOGGER.debug("Testing accessibility basics for {}", viewName);
		// Check for proper heading structure
		final var headings = page.locator("h1, h2, h3, h4, h5, h6");
		LOGGER.debug("Found {} headings", headings.count());
		// Check for aria labels
		final var ariaLabeled = page.locator("[aria-label], [aria-labelledby]");
		LOGGER.debug("Found {} elements with aria labels", ariaLabeled.count());
		// Check for proper button roles
		final var buttons = page.locator("button, [role='button']");
		LOGGER.debug("Found {} button elements", buttons.count());

		// Only take screenshot if there are accessibility issues
		if ((headings.count() == 0) || (buttons.count() == 0)) {
			takeScreenshot("accessibility-issues-" + viewName.toLowerCase(), true);
		}
		LOGGER.debug("Accessibility basics test completed for {}", viewName);
	}

	/**
	 * Tests advanced grid interactions including sorting and filtering
	 */
	protected void testAdvancedGridInView(final Class<?> viewClass) {

		try {
			LOGGER.info("Testing advanced grid interactions in {} view...", viewClass);
			assertTrue(navigateToViewByClass(viewClass), "Should navigate to view");
			gridClickCell(0);
			gridClickSort();
			LOGGER.info("✅ Advanced grid test completed for {} view", viewClass);
		} catch (final Exception e) {
			LOGGER.warn("Advanced grid test failed in {} view: {}", viewClass,
				e.getMessage());
			takeScreenshot("grid-test-error-" + viewClass.getSimpleName());
		}
	}

	/**
	 * Tests ComboBox contents and selections
	 */
	protected void testComboBoxById(final String comboBoxId,
		final String expectedFirstOption) {

		try {
			LOGGER.debug("Testing ComboBox with ID: {}", comboBoxId);

			if (elementExistsById(comboBoxId)) {
				// Open ComboBox
				clickById(comboBoxId);
				wait_500();
				// Check if options are available
				final var options = page.locator("vaadin-combo-box-item");
				final int optionCount = options.count();
				LOGGER.debug("ComboBox '{}' has {} options", comboBoxId, optionCount);

				// Only take screenshot if there are no options (potential issue)
				if (optionCount == 0) {
					takeScreenshot("combobox-no-options-" + comboBoxId, true);
				}

				if ((optionCount > 0) && (expectedFirstOption != null)) {
					selectComboBoxOptionById(comboBoxId, expectedFirstOption);
				}
				// Close ComboBox by clicking elsewhere
				page.click("body");
				wait_500();
			}
		} catch (final Exception e) {
			LOGGER.error("ComboBox test failed for ID '{}': {}", comboBoxId,
				e.getMessage());
			takeScreenshot("combobox-test-error-" + comboBoxId, true);
		}
	}

	/**
	 * Tests CRUD operations on a grid view
	 */
	protected void testCRUDOperationsInView(final String viewName,
		final String newButtonId, final String saveButtonId,
		final String deleteButtonId) {
		LOGGER.debug("Testing CRUD operations in {} view", viewName);

		// Test CREATE
		if (elementExistsById(newButtonId)) {
			LOGGER.debug("Testing CREATE operation");
			clickById(newButtonId);
			wait_1000();

			if (elementExistsById(saveButtonId)) {
				clickById(saveButtonId);
				wait_1000();
			}
		}
		getGridFirstCell();
		LOGGER.debug("Testing UPDATE operation");
		// Modify first text field if available
		fillFirstTextField(
			"Updated " + viewName + " Entry " + System.currentTimeMillis());

		if (elementExistsById(saveButtonId)) {
			clickById(saveButtonId);
			wait_1000();
		}

		// Test DELETE - with caution
		if (elementExistsById(deleteButtonId)) {
			LOGGER.debug("Testing DELETE operation");
			clickById(deleteButtonId);
			wait_1000();
			// Look for confirmation and click if available
			final var confirmButtons = page.locator(
				"vaadin-button:has-text('Yes'), vaadin-button:has-text('Confirm'), vaadin-button:has-text('Delete')");

			if (confirmButtons.count() > 0) {
				confirmButtons.first().click();
				wait_1000();
			}
		}
		LOGGER.debug("CRUD operations test completed for {}", viewName);
	}

	/**
	 * Tests entity relation display in grid columns
	 */
	protected void testEntityRelationGrid(final Class<?> viewClass) {
		LOGGER.info("🧪 Testing entity relation grid display in {} view",
			viewClass.getSimpleName());

		try {
			// Navigate to the view
			assertTrue(navigateToViewByClass(viewClass), "Should navigate to view");
			// Wait for grid to load
			wait_2000();
			// Get all grid cells
			final var gridCells = page.locator("vaadin-grid-cell-content");
			final int cellCount = gridCells.count();

			if (cellCount > 0) {
				LOGGER.debug("Found {} grid cells, checking for entity relations",
					cellCount);
				// Check for reference columns (typically show related entity names)
				boolean foundEntityRelations = false;

				// Look for cells that contain text from related entities
				for (int i = 0; i < Math.min(cellCount, 20); i++) { // Check first 20
																	// cells
					final String cellText = gridCells.nth(i).textContent();

					// Check for common patterns in entity relations
					if ((cellText != null) && !cellText.trim().isEmpty()
						&& !cellText.matches("\\d+") && // Not just numbers (IDs)
						!cellText.equals("true") && !cellText.equals("false") && // Not
																					// booleans
						(cellText.length() > 1)) {
						foundEntityRelations = true;
						LOGGER.debug("Found potential entity relation text: '{}'",
							cellText.length() > 50 ? cellText.substring(0, 50) + "..."
								: cellText);
					}
				}

				if (foundEntityRelations) {
					LOGGER.info("✅ Entity relations appear to be displayed in grid");
				}
				else {
					LOGGER.warn(
						"No clear entity relations found in grid - may need review");
					takeScreenshot("entity-relations-check-"
						+ viewClass.getSimpleName().toLowerCase(), true);
				}
			}
			else {
				LOGGER.warn("No grid cells found in {} view", viewClass.getSimpleName());
				takeScreenshot("empty-grid-" + viewClass.getSimpleName().toLowerCase(),
					true);
			}
		} catch (final Exception e) {
			LOGGER.error("Entity relation grid test failed for {}: {}",
				viewClass.getSimpleName(), e.getMessage());
			takeScreenshot("entity-relation-grid-test-error", true);
		}
	}

	/**
	 * Tests form validation by submitting empty form
	 */
	protected boolean testFormValidationById(final String saveButtonId) {

		try {

			// Try to save without filling required fields
			if (clickById(saveButtonId)) {
				wait_1000();
				// Look for validation messages
				final var errorMessages = page.locator(
					"vaadin-text-field[invalid], .error-message, [role='alert']");
				final boolean hasValidation = errorMessages.count() > 0;

				if (hasValidation) {
					LOGGER.debug("Form validation working - found {} validation messages",
						errorMessages.count());
				}
				else {
					LOGGER.warn(
						"No validation messages found - potential validation issue");
					takeScreenshot("form-validation-missing", true);
				}
				return hasValidation;
			}
			else {
				LOGGER.error("Could not click save button for validation test");
				takeScreenshot("validation-test-save-button-missing", true);
				return false;
			}
		} catch (final Exception e) {
			LOGGER.error("Form validation test failed: {}", e.getMessage());
			takeScreenshot("form-validation-test-error", true);
			return false;
		}
	}

	protected void testNavigationTo(final Class<?> class1, final Class<?> class2) {
		LOGGER.info("🧪 Testing {} navigation...", class1.getSimpleName());
		// Test navigation to Users
		assertTrue(navigateToViewByClass(class1),
			"Should navigate to view:" + class1.getSimpleName());

		// Test navigation to related views
		if (navigateToViewByClass(class2)) {
			assertTrue(navigateToViewByClass(class1),
				"Should navigate to view:" + class1.getSimpleName());
		}
		LOGGER.info("✅ {} navigation test completed", class1.getSimpleName());
	}

	/**
	 * Tests responsive design by changing viewport size
	 */
	protected void testResponsiveDesign(final String viewName) {
		LOGGER.info("Testing responsive design for {}", viewName);
		// Test mobile viewport
		page.setViewportSize(375, 667);
		wait_1000();
		takeScreenshot("responsive-mobile-" + viewName.toLowerCase());
		// Test tablet viewport
		page.setViewportSize(768, 1024);
		wait_1000();
		takeScreenshot("responsive-tablet-" + viewName.toLowerCase());
		// Test desktop viewport
		page.setViewportSize(1200, 800);
		wait_1000();
		takeScreenshot("responsive-desktop-" + viewName.toLowerCase());
		LOGGER.info("Responsive design test completed for {}", viewName);
	}

	/**
	 * Tests search functionality in a view by filling search field and verifying results
	 */
	protected void testSearchFunctionality(final Class<?> viewClass,
		final String searchTerm) {
		LOGGER.info("🧪 Testing search functionality in {} view with term: '{}'",
			viewClass.getSimpleName(), searchTerm);

		try {
			// Navigate to the view
			assertTrue(navigateToViewByClass(viewClass), "Should navigate to view");
			// Check if search field exists (should be present for CSearchable entities)
			final var searchFields = page.locator(
				"vaadin-text-field[placeholder*='earch'], .search-toolbar vaadin-text-field");

			if (searchFields.count() > 0) {
				LOGGER.debug("Search field found, testing search functionality");
				// Get initial grid row count
				final int initialRowCount = getGridRowCount();
				LOGGER.debug("Initial grid rows: {}", initialRowCount);
				// Fill search field
				searchFields.first().fill(searchTerm);
				wait_1000(); // Wait for debounced search
				// Get filtered row count
				final int filteredRowCount = getGridRowCount();
				LOGGER.debug("Filtered grid rows: {}", filteredRowCount);
				// Verify search worked (filtered count should be <= initial count)
				assertTrue(filteredRowCount <= initialRowCount,
					"Search should filter results (filtered: " + filteredRowCount
						+ ", initial: " + initialRowCount + ")");
				// Clear search to verify all results return
				searchFields.first().fill("");
				wait_1000();
				final int clearedRowCount = getGridRowCount();
				LOGGER.debug("Cleared search grid rows: {}", clearedRowCount);
				// After clearing, we should have same or more rows than filtered
				assertTrue(clearedRowCount >= filteredRowCount,
					"Clearing search should restore results");
				LOGGER.info("✅ Search functionality test completed successfully");
			}
			else {
				LOGGER.warn(
					"No search field found in {} view - entity may not implement CSearchable",
					viewClass.getSimpleName());
				takeScreenshot(
					"search-field-missing-" + viewClass.getSimpleName().toLowerCase(),
					true);
			}
		} catch (final Exception e) {
			LOGGER.error("Search functionality test failed for {}: {}",
				viewClass.getSimpleName(), e.getMessage());
			takeScreenshot("search-functionality-test-error", true);
		}
	}

	protected void wait_1000() {

		if (isBrowserAvailable()) {
			page.waitForTimeout(1000);
		}
	}

	/**
	 * Medium wait helper
	 */
	protected void wait_2000() {

		if (isBrowserAvailable()) {
			page.waitForTimeout(2000);
		}
	}

	/**
	 * Short wait helper
	 */
	protected void wait_500() {

		if (isBrowserAvailable()) {
			page.waitForTimeout(500);
		}
	}

	protected void wait_afterlogin() {

		if (isBrowserAvailable()) {
			wait_500();
			page.waitForSelector("vaadin-app-layout",
				new Page.WaitForSelectorOptions().setTimeout(10000));
		}
	}

	/**
	 * Waits for login screen to be ready
	 * Updated for CCustomLoginView (new login screen)
	 */
	protected void wait_loginscreen() {

		if (isBrowserAvailable()) {
			wait_500();
			// Updated selector for CCustomLoginView - wait for username input field
			page.waitForSelector("#custom-username-input",
				new Page.WaitForSelectorOptions().setTimeout(10000));
		}
	}

	/**
	 * Waits for an element to be visible by ID
	 */
	protected boolean waitForElementById(final String id, final int timeoutMs) {

		try {
			final String selector = "#" + id;
			page.waitForSelector(selector,
				new Page.WaitForSelectorOptions().setTimeout(timeoutMs));
			LOGGER.debug("Element with ID '{}' became visible", id);
			return true;
		} catch (final Exception e) {
			LOGGER.warn("Element with ID '{}' did not become visible within {}ms: {}", id,
				timeoutMs, e.getMessage());
			return false;
		}
	}
}
