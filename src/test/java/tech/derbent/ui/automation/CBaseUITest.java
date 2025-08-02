package tech.derbent.ui.automation;

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

public class CBaseUITest {

	protected static final Logger logger = LoggerFactory.getLogger(CBaseUITest.class);

	@LocalServerPort
	private int port;

	private Playwright playwright;

	private Browser browser;

	private BrowserContext context;

	protected Page page;

	protected String baseUrl;

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

	protected boolean clickIfExists(final String selector) {
		final var locator = page.locator(selector);

		if (locator.count() > 0) {
			locator.first().click();
			return true;
		}
		return false;
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

	protected boolean navigateToViewByText(final String viewName) {

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
	protected void performLogin(final String username, final String password) {

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
			wait_afterlogin();
		} catch (final Exception e) {
			logger.error("❌ Login failed: {}", e.getMessage());
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
			logger.warn("Logout attempt failed: {}", e.getMessage());
			return false;
		}
	}

	protected void performWorkflowInView(final Class<?> viewName) {

		try {
			logger.info("Performing workflow in {} view...", viewName);

			// Navigate to view by finding navigation elements
			if (navigateToViewByClass(viewName)) {
				logger.info("✅ Workflow step completed for {} view", viewName);
			}
		} catch (final Exception e) {
			logger.warn("⚠️ Workflow failed in {} view: {}", viewName.getSimpleName(),
				e.getMessage());
			takeScreenshot("workflow-error-" + viewName.getSimpleName());
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

	protected void takeScreenshot(final String name) {

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

	protected void wait_1000() {
		page.waitForTimeout(1000);
	}

	protected void wait_afterlogin() {
		page.waitForTimeout(500);
		page.waitForSelector("vaadin-app-layout",
			new Page.WaitForSelectorOptions().setTimeout(10000));
	}

	protected void wait_loginscreen() {
		page.waitForTimeout(500);
		page.waitForSelector("#input-vaadin-text-field-12",
			new Page.WaitForSelectorOptions().setTimeout(10000));
	}

	// ========== Additional Playwright Testing Utilities ==========

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
				logger.debug("Successfully clicked element with ID: {}", id);
				return true;
			} else {
				logger.warn("Element with ID '{}' not found", id);
				return false;
			}
		} catch (final Exception e) {
			logger.warn("Failed to click element with ID '{}': {}", id, e.getMessage());
			return false;
		}
	}

	/**
	 * Fills a form field by its ID
	 */
	protected boolean fillById(final String id, final String value) {
		try {
			final String selector = "#" + id;
			if (page.locator(selector).count() > 0) {
				page.fill(selector, value);
				logger.debug("Successfully filled field with ID '{}' with value: {}", id, value);
				return true;
			} else {
				logger.warn("Field with ID '{}' not found", id);
				return false;
			}
		} catch (final Exception e) {
			logger.warn("Failed to fill field with ID '{}': {}", id, e.getMessage());
			return false;
		}
	}

	/**
	 * Selects an option in a ComboBox by ID
	 */
	protected boolean selectComboBoxOptionById(final String comboBoxId, final String optionText) {
		try {
			final String selector = "#" + comboBoxId;
			if (page.locator(selector).count() > 0) {
				// Click to open the ComboBox
				page.click(selector);
				wait_500();
				
				// Select the option by text
				final String optionSelector = "vaadin-combo-box-item:has-text('" + optionText + "')";
				if (page.locator(optionSelector).count() > 0) {
					page.click(optionSelector);
					logger.debug("Successfully selected option '{}' in ComboBox with ID: {}", optionText, comboBoxId);
					return true;
				} else {
					logger.warn("Option '{}' not found in ComboBox with ID '{}'", optionText, comboBoxId);
					return false;
				}
			} else {
				logger.warn("ComboBox with ID '{}' not found", comboBoxId);
				return false;
			}
		} catch (final Exception e) {
			logger.warn("Failed to select option in ComboBox with ID '{}': {}", comboBoxId, e.getMessage());
			return false;
		}
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
				logger.debug("Successfully clicked grid row at index: {}", rowIndex);
				return true;
			} else {
				logger.warn("Grid row at index {} not found (total rows: {})", rowIndex, gridCells.count());
				return false;
			}
		} catch (final Exception e) {
			logger.warn("Failed to click grid row at index {}: {}", rowIndex, e.getMessage());
			return false;
		}
	}

	/**
	 * Gets the text content of an element by ID
	 */
	protected String getTextById(final String id) {
		try {
			final String selector = "#" + id;
			if (page.locator(selector).count() > 0) {
				final String text = page.locator(selector).textContent();
				logger.debug("Got text '{}' from element with ID: {}", text, id);
				return text;
			} else {
				logger.warn("Element with ID '{}' not found", id);
				return null;
			}
		} catch (final Exception e) {
			logger.warn("Failed to get text from element with ID '{}': {}", id, e.getMessage());
			return null;
		}
	}

	/**
	 * Waits for an element to be visible by ID
	 */
	protected boolean waitForElementById(final String id, final int timeoutMs) {
		try {
			final String selector = "#" + id;
			page.waitForSelector(selector, new Page.WaitForSelectorOptions().setTimeout(timeoutMs));
			logger.debug("Element with ID '{}' became visible", id);
			return true;
		} catch (final Exception e) {
			logger.warn("Element with ID '{}' did not become visible within {}ms: {}", id, timeoutMs, e.getMessage());
			return false;
		}
	}

	/**
	 * Checks if an element exists by ID
	 */
	protected boolean elementExistsById(final String id) {
		final String selector = "#" + id;
		final boolean exists = page.locator(selector).count() > 0;
		logger.debug("Element with ID '{}' exists: {}", id, exists);
		return exists;
	}

	/**
	 * Gets the count of grid rows
	 */
	protected int getGridRowCount() {
		final var gridCells = page.locator("vaadin-grid-cell-content");
		final int count = gridCells.count();
		logger.debug("Grid has {} cells", count);
		return count;
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
				final var errorMessages = page.locator("vaadin-text-field[invalid], .error-message, [role='alert']");
				final boolean hasValidation = errorMessages.count() > 0;
				
				if (hasValidation) {
					logger.debug("Form validation working - found {} validation messages", errorMessages.count());
					takeScreenshot("form-validation-triggered");
				} else {
					logger.debug("No validation messages found");
				}
				
				return hasValidation;
			} else {
				logger.warn("Could not click save button for validation test");
				return false;
			}
		} catch (final Exception e) {
			logger.warn("Form validation test failed: {}", e.getMessage());
			return false;
		}
	}

	/**
	 * Tests CRUD operations on a grid view
	 */
	protected void testCRUDOperationsInView(final String viewName, final String newButtonId, 
		final String saveButtonId, final String deleteButtonId) {
		
		logger.info("Testing CRUD operations in {} view", viewName);
		
		// Test CREATE
		if (elementExistsById(newButtonId)) {
			logger.debug("Testing CREATE operation");
			clickById(newButtonId);
			wait_1000();
			takeScreenshot("crud-create-form-" + viewName.toLowerCase());
			
			// Fill first text field if available
			fillFirstTextField("Test " + viewName + " Entry " + System.currentTimeMillis());
			
			if (elementExistsById(saveButtonId)) {
				clickById(saveButtonId);
				wait_1000();
				takeScreenshot("crud-create-saved-" + viewName.toLowerCase());
			}
		}
		
		// Test READ - check grid has data
		final int rowCount = getGridRowCount();
		logger.debug("Grid has {} rows for READ test", rowCount);
		if (rowCount > 0) {
			clickGridRowByIndex(0);
			wait_1000();
			takeScreenshot("crud-read-selected-" + viewName.toLowerCase());
		}
		
		// Test UPDATE - if edit possible
		if (rowCount > 0) {
			logger.debug("Testing UPDATE operation");
			// Modify first text field if available
			fillFirstTextField("Updated " + viewName + " Entry " + System.currentTimeMillis());
			
			if (elementExistsById(saveButtonId)) {
				clickById(saveButtonId);
				wait_1000();
				takeScreenshot("crud-update-saved-" + viewName.toLowerCase());
			}
		}
		
		// Test DELETE - with caution
		if (elementExistsById(deleteButtonId) && rowCount > 0) {
			logger.debug("Testing DELETE operation");
			clickById(deleteButtonId);
			wait_1000();
			takeScreenshot("crud-delete-confirmation-" + viewName.toLowerCase());
			
			// Look for confirmation and click if available
			final var confirmButtons = page.locator("vaadin-button:has-text('Yes'), vaadin-button:has-text('Confirm'), vaadin-button:has-text('Delete')");
			if (confirmButtons.count() > 0) {
				confirmButtons.first().click();
				wait_1000();
				takeScreenshot("crud-delete-completed-" + viewName.toLowerCase());
			}
		}
		
		logger.info("CRUD operations test completed for {}", viewName);
	}

	/**
	 * Tests ComboBox contents and selections
	 */
	protected void testComboBoxById(final String comboBoxId, final String expectedFirstOption) {
		try {
			logger.debug("Testing ComboBox with ID: {}", comboBoxId);
			
			if (elementExistsById(comboBoxId)) {
				// Open ComboBox
				clickById(comboBoxId);
				wait_500();
				
				// Check if options are available
				final var options = page.locator("vaadin-combo-box-item");
				final int optionCount = options.count();
				
				logger.debug("ComboBox '{}' has {} options", comboBoxId, optionCount);
				takeScreenshot("combobox-options-" + comboBoxId);
				
				if (optionCount > 0 && expectedFirstOption != null) {
					selectComboBoxOptionById(comboBoxId, expectedFirstOption);
				}
				
				// Close ComboBox by clicking elsewhere
				page.click("body");
				wait_500();
			}
		} catch (final Exception e) {
			logger.warn("ComboBox test failed for ID '{}': {}", comboBoxId, e.getMessage());
		}
	}

	/**
	 * Short wait helper
	 */
	protected void wait_500() {
		page.waitForTimeout(500);
	}

	/**
	 * Medium wait helper
	 */
	protected void wait_2000() {
		page.waitForTimeout(2000);
	}

	/**
	 * Tests responsive design by changing viewport size
	 */
	protected void testResponsiveDesign(final String viewName) {
		logger.info("Testing responsive design for {}", viewName);
		
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
		
		logger.info("Responsive design test completed for {}", viewName);
	}

	/**
	 * Tests basic accessibility features
	 */
	protected void testAccessibilityBasics(final String viewName) {
		logger.info("Testing accessibility basics for {}", viewName);
		
		// Check for proper heading structure
		final var headings = page.locator("h1, h2, h3, h4, h5, h6");
		logger.debug("Found {} headings", headings.count());
		
		// Check for aria labels
		final var ariaLabeled = page.locator("[aria-label], [aria-labelledby]");
		logger.debug("Found {} elements with aria labels", ariaLabeled.count());
		
		// Check for proper button roles
		final var buttons = page.locator("button, [role='button']");
		logger.debug("Found {} button elements", buttons.count());
		
		takeScreenshot("accessibility-check-" + viewName.toLowerCase());
		logger.info("Accessibility basics test completed for {}", viewName);
	}
}
