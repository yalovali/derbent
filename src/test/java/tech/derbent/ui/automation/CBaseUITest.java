package tech.derbent.ui.automation;

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

	private static final Class<CActivitiesView> CLASS = CActivitiesView.class;

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
	{
		final var cancelButtons = page
			.locator("vaadin-button:has-text('Cancel'), vaadin-button:has-text('Close')");
		assertTrue(cancelButtons.count() > 0, "Cancel button should be present");
		cancelButtons.first().click();
		wait_1000();
		takeScreenshot("users-workflow-cancelled");
	}

	private void checkAccessibilityElement(final String selector,
		final String description) {

		try {
			final var elements = page.locator(selector);

			if (elements.count() > 0) {
				LOGGER.info("✅ Found {} {} element(s)", elements.count(), description);
			}
			else {
				LOGGER.warn("⚠️ No {} found", description);
			}
		} catch (final Exception e) {
			LOGGER.warn("⚠️ Accessibility check failed for {}: {}", description,
				e.getMessage());
		}
	}

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

	protected void clickCancelButton() {
		// Close form
		final var cancelButtons = page.locator("vaadin-button:has-text('Cancel')");
		assertTrue(cancelButtons.count() > 0, "Cancel button should be present");
		cancelButtons.first().click();
		wait_1000();
	}

	/**
	 * Clicks on a grid row by index
	 */
	protected boolean clickGridRowByIndex(final int rowIndex) {

		try {
			final String selector = "vaadin-grid-cell-content";
			final var gridCells = page.locator(selector);

			if (gridCells.count() > rowIndex) {
				gridCells.nth(rowIndex).click();
				LOGGER.debug("Successfully clicked grid row at index: {}", rowIndex);
				return true;
			}
			else {
				LOGGER.warn("Grid row at index {} not found (total rows: {})", rowIndex,
					gridCells.count());
				return false;
			}
		} catch (final Exception e) {
			LOGGER.warn("Failed to click grid row at index {}: {}", rowIndex,
				e.getMessage());
			return false;
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

	protected void clickNewButton() {
		final var newButtons =
			page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");
		assertTrue(newButtons.count() > 0,
			"Should find 'New' or 'Add' button in Users view");
		newButtons.first().click();
		wait_1000();
	}

	protected void clickSaveButton() {
		final var saveButtons = page
			.locator("vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");
		assertTrue(saveButtons.count() > 0, "Save button should be present");
		saveButtons.first().click();
		wait_2000();
		takeScreenshot("users-workflow-saved");
		// Check if grid was updated
		final int finalRowCount = getGridRowCount();
		LOGGER.debug("Final grid has {} rows", finalRowCount);
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
		int firstDataCell = -1;

		for (int i = 0; i < gridCellCount; i++) {
			final Locator gridCell = gridCells.nth(i);

			if (gridCell.textContent().isEmpty()) {
				emptyCellCount++;
				emptyCells = true;
			}
			else {
				// logger.info("Grid cell text: {}", gridCell.textContent());

				if (emptyCells) {
					firstDataCell = i;
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

	/**
	 * Helper method to login to the application with default credentials
	 */
	protected void loginToApplication() {
		page.navigate(baseUrl);
		wait_loginscreen();
		performLogin("admin", "test123");
		wait_afterlogin();
	}

	protected boolean navigateToViewByClass(final Class<?> viewClass) {

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
			page.waitForTimeout(2000); // waits for 1 second
			return true;
		} catch (final Exception e) {
			LOGGER.warn("Failed to navigate to view {}: {}", viewClass.getSimpleName(),
				e.getMessage());
			return false;
		}
	}

	/**
	 * Performs login with specified credentials
	 */
	protected void performLogin(final String username, final String password) {

		try {
			/*
			 * page.locator("id=vaadin-text-field-0").fill("TST");
			 * page.locator("id=vaadin-text-field-1").fill("Test");
			 * page.locator("id=edit-save").click();
			 */
			LOGGER.info("Performing login with username: {}", username);
			page.fill("vaadin-text-field[name='username'] > input", username);
			page.fill("vaadin-password-field[name='password'] > input", password);
			page.click("vaadin-button");
			wait_afterlogin();
		} catch (final Exception e) {
			LOGGER.error("❌ Login failed: {}", e.getMessage());
			takeScreenshot("login-failed");
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
		loginToApplication();
		LOGGER.info("Playwright test setup completed. Application URL: {}", baseUrl);
	}

	protected void takeScreenshot(final String name) {

		try {
			final String screenshotPath =
				"target/screenshots/" + name + "-" + System.currentTimeMillis() + ".png";
			page.screenshot(
				new Page.ScreenshotOptions().setPath(Paths.get(screenshotPath)));
			LOGGER.info("📸 Screenshot saved: {}", screenshotPath);
		} catch (final Exception e) {
			LOGGER.warn("⚠️ Failed to take screenshot '{}': {}", name, e.getMessage());
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
		LOGGER.info("Playwright test cleanup completed");
	}

	/**
	 * Tests basic accessibility features
	 */
	protected void testAccessibilityBasics(final String viewName) {
		LOGGER.info("Testing accessibility basics for {}", viewName);
		// Check for proper heading structure
		final var headings = page.locator("h1, h2, h3, h4, h5, h6");
		LOGGER.debug("Found {} headings", headings.count());
		// Check for aria labels
		final var ariaLabeled = page.locator("[aria-label], [aria-labelledby]");
		LOGGER.debug("Found {} elements with aria labels", ariaLabeled.count());
		// Check for proper button roles
		final var buttons = page.locator("button, [role='button']");
		LOGGER.debug("Found {} button elements", buttons.count());
		takeScreenshot("accessibility-check-" + viewName.toLowerCase());
		LOGGER.info("Accessibility basics test completed for {}", viewName);
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
				takeScreenshot("combobox-options-" + comboBoxId);

				if ((optionCount > 0) && (expectedFirstOption != null)) {
					selectComboBoxOptionById(comboBoxId, expectedFirstOption);
				}
				// Close ComboBox by clicking elsewhere
				page.click("body");
				wait_500();
			}
		} catch (final Exception e) {
			LOGGER.warn("ComboBox test failed for ID '{}': {}", comboBoxId,
				e.getMessage());
		}
	}

	/**
	 * Tests CRUD operations on a grid view
	 */
	protected void testCRUDOperationsInView(final String viewName,
		final String newButtonId, final String saveButtonId,
		final String deleteButtonId) {
		LOGGER.info("Testing CRUD operations in {} view", viewName);

		// Test CREATE
		if (elementExistsById(newButtonId)) {
			LOGGER.debug("Testing CREATE operation");
			clickById(newButtonId);
			wait_1000();
			takeScreenshot("crud-create-form-" + viewName.toLowerCase());
			// Fill first text field if available
			fillFirstTextField(
				"Test " + viewName + " Entry " + System.currentTimeMillis());

			if (elementExistsById(saveButtonId)) {
				clickById(saveButtonId);
				wait_1000();
				takeScreenshot("crud-create-saved-" + viewName.toLowerCase());
			}
		}
		// Test READ - check grid has data
		final int rowCount = getGridRowCount();
		LOGGER.debug("Grid has {} rows for READ test", rowCount);

		if (rowCount > 0) {
			clickGridRowByIndex(0);
			wait_1000();
			takeScreenshot("crud-read-selected-" + viewName.toLowerCase());
		}

		// Test UPDATE - if edit possible
		if (rowCount > 0) {
			LOGGER.debug("Testing UPDATE operation");
			// Modify first text field if available
			fillFirstTextField(
				"Updated " + viewName + " Entry " + System.currentTimeMillis());

			if (elementExistsById(saveButtonId)) {
				clickById(saveButtonId);
				wait_1000();
				takeScreenshot("crud-update-saved-" + viewName.toLowerCase());
			}
		}

		// Test DELETE - with caution
		if (elementExistsById(deleteButtonId) && (rowCount > 0)) {
			LOGGER.debug("Testing DELETE operation");
			clickById(deleteButtonId);
			wait_1000();
			takeScreenshot("crud-delete-confirmation-" + viewName.toLowerCase());
			// Look for confirmation and click if available
			final var confirmButtons = page.locator(
				"vaadin-button:has-text('Yes'), vaadin-button:has-text('Confirm'), vaadin-button:has-text('Delete')");

			if (confirmButtons.count() > 0) {
				confirmButtons.first().click();
				wait_1000();
				takeScreenshot("crud-delete-completed-" + viewName.toLowerCase());
			}
		}
		LOGGER.info("CRUD operations test completed for {}", viewName);
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
					takeScreenshot("form-validation-triggered");
				}
				else {
					LOGGER.debug("No validation messages found");
				}
				return hasValidation;
			}
			else {
				LOGGER.warn("Could not click save button for validation test");
				return false;
			}
		} catch (final Exception e) {
			LOGGER.warn("Form validation test failed: {}", e.getMessage());
			return false;
		}
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

	protected void wait_1000() {
		page.waitForTimeout(1000);
	}

	/**
	 * Medium wait helper
	 */
	protected void wait_2000() {
		page.waitForTimeout(2000);
	}

	/**
	 * Short wait helper
	 */
	protected void wait_500() {
		page.waitForTimeout(500);
	}

	protected void wait_afterlogin() {
		wait_500();
		page.waitForSelector("vaadin-app-layout",
			new Page.WaitForSelectorOptions().setTimeout(10000));
	}

	protected void wait_loginscreen() {
		wait_500();
		page.waitForSelector("#input-vaadin-text-field-12",
			new Page.WaitForSelectorOptions().setTimeout(10000));
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
