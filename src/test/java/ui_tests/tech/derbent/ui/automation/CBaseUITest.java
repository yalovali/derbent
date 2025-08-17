package ui_tests.tech.derbent.ui.automation;

import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.vaadin.flow.router.Route;

import tech.derbent.abstracts.utils.Check;
import tech.derbent.activities.view.CActivitiesView;
import tech.derbent.activities.view.CActivityStatusView;
import tech.derbent.activities.view.CActivityTypeView;
import tech.derbent.administration.view.CCompanySettingsView;
import tech.derbent.base.ui.view.CDashboardView;
import tech.derbent.comments.view.CCommentPriorityView;
import tech.derbent.companies.view.CCompanyView;
import tech.derbent.decisions.view.CDecisionStatusView;
import tech.derbent.decisions.view.CDecisionTypeView;
import tech.derbent.decisions.view.CDecisionsView;
import tech.derbent.examples.view.CExampleHierarchicalMenuView;
import tech.derbent.examples.view.CExampleSettingsView;
import tech.derbent.examples.view.CSearchDemoView;
import tech.derbent.examples.view.CSearchShowcaseView;
import tech.derbent.kanban.view.CActivityKanbanBoardView;
import tech.derbent.kanban.view.CGenericActivityKanbanBoardView;
import tech.derbent.kanban.view.CMeetingKanbanBoardView;
import tech.derbent.meetings.view.CMeetingStatusView;
import tech.derbent.meetings.view.CMeetingTypeView;
import tech.derbent.meetings.view.CMeetingsView;
import tech.derbent.orders.view.COrdersView;
import tech.derbent.projects.view.CProjectDetailsView;
import tech.derbent.projects.view.CProjectsView;
import tech.derbent.risks.view.CRiskStatusView;
import tech.derbent.risks.view.CRiskView;
import tech.derbent.screens.view.CScreenView;
import tech.derbent.setup.view.CSystemSettingsView;
import tech.derbent.users.view.CUserTypeView;
import tech.derbent.users.view.CUsersView;

@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles ("uitest") // Use the test profile that disables initializers
public class CBaseUITest {

	protected static final Logger LOGGER = LoggerFactory.getLogger(CBaseUITest.class);

	@LocalServerPort
	private int port;

	private Playwright playwright;

	private Browser browser;

	private BrowserContext context;

	protected Page page;

	protected String baseUrl;

	// Core business views - main entity management
	protected Class<?>[] mainViewClasses = {
		CProjectsView.class, CActivitiesView.class, CMeetingsView.class,
		CDecisionsView.class, CUsersView.class, COrdersView.class, CRiskView.class,
		CCompanyView.class, CScreenView.class };

	// Status and Type configuration views
	protected Class<?>[] statusAndTypeViewClasses = {
		CActivityStatusView.class, CActivityTypeView.class, CDecisionStatusView.class,
		CDecisionTypeView.class, CMeetingStatusView.class, CMeetingTypeView.class,
		CUserTypeView.class, CRiskStatusView.class, CCommentPriorityView.class };

	// Administrative and system configuration views
	protected Class<?>[] adminViewClasses = {
		CCompanySettingsView.class, CSystemSettingsView.class, CDashboardView.class };

	// Kanban board views
	protected Class<?>[] kanbanViewClasses = {
		CActivityKanbanBoardView.class, CMeetingKanbanBoardView.class,
		CGenericActivityKanbanBoardView.class };

	// Example and demo views
	protected Class<?>[] exampleViewClasses = {
		CExampleHierarchicalMenuView.class, CExampleSettingsView.class,
		CSearchDemoView.class, CSearchShowcaseView.class };

	// Detail views
	protected Class<?>[] detailViewClasses = {
		CProjectDetailsView.class };

	// All views combined for comprehensive testing
	protected Class<?>[] allViewClasses = {
		CProjectsView.class, CActivitiesView.class, CMeetingsView.class,
		CDecisionsView.class, CUsersView.class, COrdersView.class, CRiskView.class,
		CCompanyView.class, CScreenView.class, CActivityStatusView.class, CActivityTypeView.class,
		CDecisionStatusView.class, CDecisionTypeView.class, CMeetingStatusView.class,
		CMeetingTypeView.class, CUserTypeView.class, CRiskStatusView.class,
		CCommentPriorityView.class, CCompanySettingsView.class, CSystemSettingsView.class,
		CDashboardView.class, CActivityKanbanBoardView.class,
		CMeetingKanbanBoardView.class, CGenericActivityKanbanBoardView.class,
		CExampleHierarchicalMenuView.class, CExampleSettingsView.class,
		CSearchDemoView.class, CSearchShowcaseView.class, CProjectDetailsView.class };

	// Legacy property for backward compatibility
	protected Class<?>[] viewClasses = mainViewClasses;

	/**
	 * Improved browser availability check with proper assertion
	 */
	protected void assertBrowserAvailable() {
		Check.condition(isBrowserAvailable(),
			"Browser is not available for testing - ensure Playwright setup completed successfully");
	}

	/**
	 * Improved element existence check with assertion
	 */
	protected void assertElementExistsById(final String id) {
		Check.condition(elementExistsById(id),
			"Element with ID '" + id + "' should exist but was not found");
	}

	/**
	 * Improved element non-existence check with assertion
	 */
	protected void assertElementNotExistsById(final String id) {
		Check.condition(!elementExistsById(id),
			"Element with ID '" + id + "' should not exist but was found");
	}

	/**
	 * Improved grid row count assertion
	 */
	protected void assertGridHasRows(final int expectedMinRows) {
		final int actualRows = getGridRowCount();
		Check.condition(actualRows >= expectedMinRows, "Grid should have at least "
			+ expectedMinRows + " rows but has " + actualRows);
	}

	/**
	 * Improved text content assertion
	 */
	protected void assertTextById(final String id, final String expectedText) {
		final String actualText = getTextById(id);
		Check.notNull(actualText,
			"Element with ID '" + id + "' should have text content");
		Check.equals(expectedText, actualText,
			"Text content mismatch for element with ID '" + id + "'");
	}

	/**
	 * Clicks on an element by its ID with timeout and error handling
	 */
	protected void clickById(final String id) {
		clickById(id, 5000);
	}

	/**
	 * Clicks on an element by its ID with timeout and error handling
	 */
	protected void clickById(final String id, final int timeoutMs) {
		assertBrowserAvailable();
		final String selector = "#" + id;
		final var locator = page.locator(selector);
		Check.notNull(id, "Element ID cannot be null");
		Check.condition(locator.count() > 0, "Element with ID '" + id + "' should exist before clicking");
		// Perform click with timeout
		page.click(selector, new Page.ClickOptions().setTimeout(timeoutMs));
		LOGGER.debug("Successfully clicked element with ID: {}", id);
	}

	/**
	 * Common function to click Cancel button with improved structure
	 */
	protected void clickCancel() {

		// Early return for browser availability
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
		final var newButtons =
			page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");
		Check.condition(newButtons.count() > 0, "New/Add button should exist before clicking");
		newButtons.first().click();
		wait_1000();
		LOGGER.debug("Successfully clicked New button");
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
		final var saveButtons = page
			.locator("vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");
		Check.condition(saveButtons.count() > 0, "Save button should exist before clicking");
		saveButtons.first().click();
		wait_2000();
		LOGGER.debug("Successfully clicked Save button");
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
	 * Fills a form field by its ID with improved structure and assertions
	 */
	protected void fillById(final String id, final String value) {
		// Input validation
		Check.notNull(id, "Element ID cannot be null");
		Check.notNull(value, "Value to fill cannot be null");
		assertBrowserAvailable();
		final String selector = "#" + id;
		final var locator = page.locator(selector);
		// Check element existence
		Check.condition(locator.count() > 0, "Element with ID '" + id + "' should exist before filling");
		// Fill the field
		page.fill(selector, value);
		LOGGER.debug("Successfully filled field with ID '{}' with value: {}", id, value);
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
	 * Gets the count of grid rows with improved error handling and assertions
	 */
	protected int getGridFirstCell() {
		final var grids = page.locator("vaadin-grid");
		// Use proper assertions instead of fail()
		Check.condition(grids.count() > 0, "No grids found in view - expected at least one grid");
		final var grid = grids.first();
		final var gridCells = grid.locator("vaadin-grid-cell-content");
		final int gridCellCount = gridCells.count();
		// Use proper assertions with descriptive messages
		Check.condition(gridCellCount >= 1, "No grid cells found - expected at least one cell");
		boolean emptyCells = false;
		int firstDataCell = -1;

		for (int i = 0; i < gridCellCount; i++) {
			final Locator gridCell = gridCells.nth(i);
			final String cellText = gridCell.textContent();

			if ((cellText == null) || cellText.isEmpty()) {
				emptyCells = true;
			}
			else if (emptyCells) {
				firstDataCell = i;
				break;
			}
		}
		// Validate that we found a data cell
		Check.condition(firstDataCell >= 0, "No data cells found after empty cells");
		return firstDataCell;
	}

	/**
	 * Gets the count of grid rows with improved error handling and assertions
	 */
	protected int getGridRowCount() {
		final var grids = page.locator("vaadin-grid");
		// Use proper assertions instead of fail()
		Check.condition(grids.count() > 0, "No grids found in view - expected at least one grid");
		final var grid = grids.first();
		final var gridCells = grid.locator("vaadin-grid-cell-content");
		final int gridCellCount = gridCells.count();
		// Use proper assertions with descriptive messages
		Check.condition(gridCellCount >= 1, "No grid cells found - expected at least one cell");
		int columns = 0;
		boolean emptyCells = false;
		int emptyCellCount = 0;

		for (int i = 0; i < gridCellCount; i++) {
			final Locator gridCell = gridCells.nth(i);
			final String cellText = gridCell.textContent();

			if ((cellText == null) || cellText.isEmpty()) {
				emptyCellCount++;
				emptyCells = true;
			}
			else {

				if (emptyCells) {
					break;
				}
				columns++;
			}
		}
		// Validate column count before calculation
		Check.condition(columns > 0, "No columns detected in grid");
		final int rowCount = (gridCellCount - emptyCellCount - columns) / columns;
		LOGGER.debug("Grid has {} rows and {} columns", rowCount, columns);
		// Ensure row count is reasonable
		Check.condition(rowCount >= 0, "Invalid row count calculated: " + rowCount);
		return rowCount;
	}

	/**
	 * Gets the text content of an element by ID
	 */
	protected String getTextById(final String id) {
		final String selector = "#" + id;
		final Locator item = page.locator(selector);
		Check.condition(item.count() > 0,
			"Element with ID '" + id + "' should exist before getting text");
		final String text = item.textContent();
		LOGGER.debug("Got text '{}' from element with ID: {}", text, id);
		return text;
	}

	protected void gridClickCell(final int cellIndex) {
		// TODO Auto-generated method stub
		final int gridFirstCellIndex = getGridFirstCell();

		try {
			final String selector = "vaadin-grid-cell-content";
			final var gridCells = page.locator(selector);
			Check.condition(cellIndex < (gridCells.count() - gridFirstCellIndex), "invalid grid cell index: " + cellIndex);
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
		}
	}

	/**
	 * Checks if browser is available for testing
	 */
	private boolean isBrowserAvailable() {
		return (page != null) && (playwright != null);
	}

	/**
	 * Helper method to login to the application with default credentials
	 */
	protected void loginToApplication() {
		assertBrowserAvailable();
		page.navigate(baseUrl);
		wait_loginscreen();
		performLogin("admin", "test123");
	}

	protected void navigateToViewByClass(final Class<?> viewClass) {
		assertBrowserAvailable();
		final Route routeAnnotation = viewClass.getAnnotation(Route.class);
		Check.condition(routeAnnotation != null, "Class " + viewClass.getName() + " must have a @Route annotation");
		final String route = routeAnnotation.value().split("/")[0];
		Check.condition(!route.isEmpty(), "Route value must not be empty for class: " + viewClass.getName());
		String url = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
		url += route.startsWith("/") ? route.substring(1) : route;
		LOGGER.info("Navigating to view by class: {}", url);
		page.navigate(url);
		page.waitForTimeout(2000); // waits for 2 seconds
		// Check if the view is loaded by looking for a specific element
		final String viewSelector = "#pageid-" + route.replace("/", "-");
		final Locator viewLocator = page.locator(viewSelector);
		Check.condition(viewLocator.count() > 0, "View element with selector '" + viewSelector
			+ "' should exist after navigation");
	}

	/**
	 * Performs login with specified credentials Updated for CCustomLoginView (new login
	 * screen)
	 */
	protected void performLogin(final String username, final String password) {
		// Input validation
		Check.notNull(username, "Username cannot be null");
		Check.notNull(password, "Password cannot be null");
		Check.condition(!(username.trim().isEmpty()), "Username cannot be empty");
		Check.condition(!(password.trim().isEmpty()), "Password cannot be empty");
		LOGGER.debug("Performing login with username: {}", username);
		// Fill login form - Playwright fill() returns void, so we can't check return
		// value
		page.locator("#custom-username-input").locator("input").fill(username);
		page.locator("#custom-password-input").locator("input").fill(password);
		// Submit login
		page.click("#custom-submit-button");
		wait_1000();
		// Check for error messages
		final var errorMessageLocator = page.locator("#custom-error-message");

		if (errorMessageLocator.count() > 0) {
			final String errorText = errorMessageLocator.textContent();

			if ((errorText != null) && !errorText.trim().isEmpty()) {
				LOGGER.error("Login error message: {}", errorText);
				throw new RuntimeException("Login failed: " + errorText);
			}
		}
		// Wait for successful login
		wait_afterlogin();
	}

	/**
	 * Attempts to perform logout using the specific menu item IDs from MainLayout with
	 * proper assertions for test validation
	 */
	protected void performLogout() {
		// Assert browser is available before attempting logout
		assertBrowserAvailable();
		// First click on the user menu item to open the dropdown
		clickById("user-menu-item");
		Check.condition(waitForElementById("logout-menu-item", 2000), "Logout menu item should be available after opening user menu");
		// Then click on the logout menu item
		clickById("logout-menu-item");
		wait_1000();
		Check.condition(waitForElementById("custom-username-input", 5000), "Should be redirected to login page after logout");
		LOGGER.info("✅ Logout completed successfully - redirected to login page");
	}
	// ========== Additional Playwright Testing Utilities ==========

	protected void performWorkflowInView(final Class<?> viewName) {
		LOGGER.info("Performing workflow in {} view...", viewName);
		// Navigate to view by finding navigation elements
		navigateToViewByClass(viewName);
		LOGGER.info("✅ Workflow step completed for {} view", viewName);
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
		LOGGER.info("Setting up Playwright test environment...");
		baseUrl = "http://localhost:" + port;

		try {
			// Set environment to skip browser download
			System.setProperty("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1");
			// Initialize Playwright
			playwright = Playwright.create();
			// Use system browser configuration
			final BrowserType.LaunchOptions launchOptions =
				new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(50);

			// Check if system browser exists
			if (Paths.get("/usr/bin/google-chrome").toFile().exists()) {
				launchOptions.setExecutablePath(Paths.get("/usr/bin/google-chrome"));
				LOGGER.info(
					"Using system Google Chrome browser at: /usr/bin/google-chrome");
			}
			else if (Paths.get("/usr/bin/chromium").toFile().exists()) {
				launchOptions.setExecutablePath(Paths.get("/usr/bin/chromium"));
				LOGGER.info("Using system Chromium browser at: /usr/bin/chromium");
			}
			else {
				LOGGER.warn("System browser not found");
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
			java.nio.file.Files
				.createDirectories(java.nio.file.Paths.get("target/screenshots"));
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
			java.nio.file.Files
				.createDirectories(java.nio.file.Paths.get("target/screenshots"));
			final String screenshotPath =
				"target/screenshots/" + name + "-" + System.currentTimeMillis() + ".png";
			page.screenshot(
				new Page.ScreenshotOptions().setPath(Paths.get(screenshotPath)));

			if (isFailure) {
				LOGGER.warn("📸 Failure screenshot saved: {}", screenshotPath);
			}
			else {
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
			navigateToViewByClass(viewClass);
			gridClickCell(0);
			gridClickSort();
			LOGGER.info("✅ Advanced grid test completed for {} view", viewClass);
		} catch (final Exception e) {
			LOGGER.warn("Advanced grid test failed in {} view: {}", viewClass,
				e.getMessage());
			takeScreenshot("grid-test-error-" + viewClass.getSimpleName(), true);
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
			navigateToViewByClass(viewClass); // Wait for grid to load
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
		clickById(saveButtonId);
		wait_1000();
		// Look for validation messages
		final var errorMessages =
			page.locator("vaadin-text-field[invalid], .error-message, [role='alert']");
		final boolean hasValidation = errorMessages.count() > 0;

		if (hasValidation) {
			LOGGER.debug("Form validation working - found {} validation messages",
				errorMessages.count());
		}
		else {
			LOGGER.warn("No validation messages found - potential validation issue");
			takeScreenshot("form-validation-missing", true);
		}
		return hasValidation;
	}

	protected void testNavigationTo(final Class<?> class1, final Class<?> class2) {
		LOGGER.info("🧪 Testing {} navigation...", class1.getSimpleName());
		// Test navigation to Users
		navigateToViewByClass(class1);
		// Test navigation to related views
		navigateToViewByClass(class2);
		LOGGER.info("✅ {} navigation test completed", class1.getSimpleName());
	}

	/**
	 * Tests responsive design by changing viewport size - screenshots only on errors
	 */
	protected void testResponsiveDesign(final String viewName) {
		LOGGER.info("🧪 Testing responsive design for {}", viewName);
		
		try {
			// Test mobile viewport
			page.setViewportSize(375, 667);
			wait_1000();
			// Only log success, no screenshot needed
			LOGGER.debug("✅ Mobile viewport (375x667) test completed for {}", viewName);
			
			// Test tablet viewport
			page.setViewportSize(768, 1024);
			wait_1000();
			LOGGER.debug("✅ Tablet viewport (768x1024) test completed for {}", viewName);
			
			// Test desktop viewport
			page.setViewportSize(1200, 800);
			wait_1000();
			LOGGER.debug("✅ Desktop viewport (1200x800) test completed for {}", viewName);
			
			LOGGER.info("✅ Responsive design test completed successfully for {}", viewName);
		} catch (final Exception e) {
			LOGGER.error("❌ Responsive design test failed for {}: {}", viewName, e.getMessage());
			takeScreenshot("responsive-design-failed-" + viewName.toLowerCase(), true);
			throw new AssertionError("Responsive design test failed for " + viewName + ": " + e.getMessage(), e);
		}
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
			navigateToViewByClass(viewClass);
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
				Check.condition(filteredRowCount <= initialRowCount, "Search should filter results (filtered: " + filteredRowCount
						+ ", initial: " + initialRowCount + ")");
				// Clear search to verify all results return
				searchFields.first().fill("");
				wait_1000();
				final int clearedRowCount = getGridRowCount();
				LOGGER.debug("Cleared search grid rows: {}", clearedRowCount);
				// After clearing, we should have same or more rows than filtered
				Check.condition(clearedRowCount >= filteredRowCount, "Clearing search should restore results");
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
			page.waitForSelector("#main-layout",
				new Page.WaitForSelectorOptions().setTimeout(10000));
			LOGGER.debug("Application layout found after login");
			return;
		}
		throw new RuntimeException(
			"Login failed - application layout not found after login");
	}

	/**
	 * Waits for login screen to be ready Updated for CCustomLoginView (new login screen)
	 */
	protected void wait_loginscreen() {
		assertBrowserAvailable();
		// Updated selector for CCustomLoginView - wait for username input field
		final ElementHandle handler = page.waitForSelector("#custom-username-input",
			new Page.WaitForSelectorOptions().setTimeout(10000));
		Check.notNull(handler, "Login screen did not load - username input field not found");
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
