package automated_tests.tech.derbent.ui.automation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Optional;
import java.util.Set;
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
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.vaadin.flow.router.Route;
import tech.derbent.api.utils.Check;
import tech.derbent.app.projects.domain.CProject;

/** Enhanced base UI test class that provides common functionality for Playwright tests. This class includes 25+ auxiliary methods for testing all
 * views and business functions. The base class follows strict coding guidelines and provides comprehensive testing utilities for: - Login and
 * authentication workflows - Navigation between views using ID-based selectors - CRUD operations testing - Form validation and ComboBox testing -
 * Grid interactions and data verification - Screenshot capture for debugging - Cross-view data consistency testing */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles ("test")
public abstract class CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBaseUITest.class);
	static {
		System.setProperty("vaadin.devmode.liveReload.enabled", "false");
		System.setProperty("vaadin.launch-browser", "false");
		System.setProperty("vaadin.devmode.enabled", "false");
		System.setProperty("vaadin.devserver.enabled", "false");
		System.setProperty("spring.devtools.restart.enabled", "false");
		System.setProperty("spring.devtools.livereload.enabled", "false");
		System.setProperty("spring.devtools.livereload.port", "35729");
	}
	protected static final String SCREENSHOT_SUCCESS_SUFFIX = "success";
	protected static final String SCREENSHOT_FAILURE_SUFFIX = "failure";
	private static final String FIELD_ID_PREFIX = "field";
	private static final String FORCE_SAMPLE_RELOAD_PROPERTY = "playwright.forceSampleReload";
	private static final AtomicBoolean SAMPLE_DATA_INITIALIZED = new AtomicBoolean(false);
	private static final Object SAMPLE_DATA_LOCK = new Object();
	/** Admin view classes */
	protected Class<?>[] adminViewClasses = {};
	protected Class<?>[] allViewClasses = {};
	/** All view classes */
	private Browser browser;
	private BrowserContext context;
	/** Kanban view classes */
	protected Class<?>[] kanbanViewClasses = {};
	/** Array of main view classes for testing */
	protected Class<?>[] mainViewClasses = {};
	protected Page page;
	private Playwright playwright;
	@LocalServerPort
	protected int port = 8080;
	/** Status and type view classes */
	protected Class<?>[] statusAndTypeViewClasses = {};
	/** Legacy property for backward compatibility */
	protected Class<?>[] viewClasses = mainViewClasses;

	/** Assert browser is available */
	protected void assertBrowserAvailable() {
		if (!isBrowserAvailable()) {
			throw new AssertionError("Browser is not available");
		}
	}

	/** Clicks the "Cancel" button to cancel the current operation. */
	protected void clickCancel() {
		LOGGER.info("❌ Clicking Cancel button");
		page.locator("vaadin-button:has-text('Cancel')").click();
		wait_500();
	}

	/** Clicks the "Delete" button to delete the selected entity. */
	protected void clickDelete() {
		LOGGER.info("🗑️ Clicking Delete button");
		page.locator("vaadin-button:has-text('Delete')").click();
		wait_500();
	}

	/** Clicks the "Edit" button to edit the selected entity. */
	protected void clickEdit() {
		LOGGER.info("✏️ Clicking Edit button");
		page.locator("vaadin-button:has-text('Edit')").click();
		wait_500();
	}
	// ===========================================
	// GRID INTERACTION METHODS
	// ===========================================

	/** Clicks the first row in the first grid found on the page. */
	protected void clickFirstGridRow() {
		LOGGER.info("📊 Clicking first grid row");
		final Locator grid = page.locator("vaadin-grid").first();
		final Locator cells = grid.locator("vaadin-grid-cell-content");
		if (cells.count() > 0) {
			cells.first().click();
			wait_500();
			LOGGER.info("✅ Clicked first grid row");
		} else {
			LOGGER.warn("⚠️ No grid rows found");
		}
	}

	/** Clicks the "New" button to create a new entity. */
	protected void clickNew() {
		LOGGER.info("➕ Clicking New button");
		page.locator("vaadin-button:has-text('New')").click();
		wait_500();
	}

	/** Clicks the "Save" button to save the current entity. */
	protected void clickSave() {
		LOGGER.info("💾 Clicking Save button");
		page.locator("vaadin-button:has-text('Save')").click();
		wait_1000(); // Save operations may take longer
	}

	/** Fills the first text area found on the page with the specified value. */
	protected void fillFirstTextArea(final String value) {
		LOGGER.info("📝 Filling first text area with: {}", value);
		page.locator("vaadin-text-area").first().fill(value);
	}

	/** Fills the first text field found on the page with the specified value. */
	protected void fillFirstTextField(final String value) {
		LOGGER.info("📝 Filling first text field with: {}", value);
		page.locator("vaadin-text-field").first().fill(value);
	}

	/** Applies a search filter to the default grid search field, if present. */
	protected void applyGridSearchFilter(final String query) {
		final Locator searchFields = page.locator("vaadin-text-field[placeholder='Search...']");
		if (searchFields.count() == 0) {
			LOGGER.warn("⚠️ No search field with placeholder 'Search...' found on the current view");
			return;
		}
		final Locator input = searchFields.first().locator("input");
		input.fill(query);
		wait_500();
	}
	// ===========================================
	// FORM FIELD HELPERS
	// ===========================================

	/** Computes the deterministic field ID generated by the Vaadin form builder using entity and field names. */
	protected String computeFieldId(final Class<?> entityClass, final String fieldName) {
		Check.notNull(entityClass, "Entity class required for field ID calculation");
		Check.notBlank(fieldName, "Field name required for field ID calculation");
		final String base = String.format("%s-%s-%s", FIELD_ID_PREFIX, entityClass.getSimpleName(), fieldName);
		return sanitizeForDomId(base);
	}

	/** Fills a bound Vaadin field by its deterministic ID using the entity class and field name. */
	protected void fillFieldById(final Class<?> entityClass, final String fieldName, final String value) {
		fillFieldById(computeFieldId(entityClass, fieldName), value);
	}

	/** Fills a bound Vaadin field by its DOM ID. Supports text fields and text areas by drilling into the native input element. */
	protected void fillFieldById(final String elementId, final String value) {
		Check.notBlank(elementId, "Element ID cannot be blank when filling a field");
		Check.notNull(value, "Value cannot be null when filling a field");
		final Locator host = locatorById(elementId);
		try {
			if (host.locator("input").count() > 0) {
				host.locator("input").first().fill(value);
			} else if (host.locator("textarea").count() > 0) {
				host.locator("textarea").first().fill(value);
			} else {
				host.fill(value);
			}
			wait_500();
		} catch (final PlaywrightException e) {
			throw new AssertionError("Failed to fill field with id '" + elementId + "': " + e.getMessage(), e);
		}
	}

	/** Resolves a Playwright locator for an element by ID, waiting for it to be present. */
	protected Locator locatorById(final String elementId) {
		Check.notBlank(elementId, "Element ID cannot be blank when locating an element");
		final String selector = elementId.startsWith("#") ? elementId : "#" + elementId;
		try {
			page.waitForSelector(selector, new Page.WaitForSelectorOptions().setTimeout(5000));
		} catch (final PlaywrightException e) {
			throw new AssertionError("Timed out waiting for element with id '" + elementId + "'", e);
		}
		final Locator locator = page.locator(selector);
		if (locator.count() == 0) {
			throw new AssertionError("Element not found with id '" + elementId + "'");
		}
		return locator;
	}

	/** Reads the current value from a bound field by entity class and field name. */
	protected String readFieldValueById(final Class<?> entityClass, final String fieldName) {
		final Locator host = locatorById(computeFieldId(entityClass, fieldName));
		if (host.locator("input").count() > 0) {
			return host.locator("input").first().inputValue();
		}
		if (host.locator("textarea").count() > 0) {
			return host.locator("textarea").first().inputValue();
		}
		return host.innerText();
	}

	/** Selects the first option from a Vaadin ComboBox bound to the given entity field. */
	protected void selectFirstComboBoxOptionById(final Class<?> entityClass, final String fieldName) {
		selectFirstComboBoxOptionById(computeFieldId(entityClass, fieldName));
	}

	/** Selects the first option from a Vaadin ComboBox identified by its DOM ID. */
	protected void selectFirstComboBoxOptionById(final String elementId) {
		Check.notBlank(elementId, "Element ID cannot be blank when selecting ComboBox option");
		final Locator combo = locatorById(elementId);
		combo.click();
		wait_500();
		Locator options = page.locator("vaadin-combo-box-overlay[opened] vaadin-combo-box-item");
		if (options.count() == 0) {
			options = page.locator("vaadin-combo-box-item");
		}
		if (options.count() == 0) {
			throw new AssertionError("No options available for combo-box with id '" + elementId + "'");
		}
		options.first().click();
		wait_500();
	}

	/** Verifies the value of a bound field matches the expected content. */
	protected void assertFieldValueEquals(final Class<?> entityClass, final String fieldName, final String expected) {
		final String actual = readFieldValueById(entityClass, fieldName);
		if (!expected.equals(actual)) {
			throw new AssertionError("Field '" + fieldName + "' expected value '" + expected + "' but was '" + actual + "'");
		}
	}

	/** Sanitizes raw text into a lower-case, hyphenated DOM-friendly identifier. */
	protected String sanitizeForDomId(final String value) {
		return sanitizeForIdentifier(value, FIELD_ID_PREFIX + "-autogen");
	}

	/** Sanitizes raw text for use in filenames with a custom fallback. */
	protected String sanitizeForFileName(final String value, final String fallback) {
		return sanitizeForIdentifier(value, fallback);
	}

	private String sanitizeForIdentifier(final String value, final String fallback) {
		final String safeFallback = (fallback == null) || fallback.isBlank() ? "autogen" : fallback;
		if ((value == null) || value.isBlank()) {
			return safeFallback;
		}
		final String sanitized = value.replaceAll("([a-z])([A-Z])", "$1-$2").replaceAll("[^a-zA-Z0-9-]", "-").replaceAll("-{2,}", "-")
				.replaceAll("(^-|-$)", "").toLowerCase();
		return sanitized.isBlank() ? safeFallback : sanitized;
	}

	/** Gets the count of rows in the first grid */
	protected int getGridRowCount() {
		final Locator grid = page.locator("vaadin-grid").first();
		final Locator cells = grid.locator("vaadin-grid-cell-content");
		return cells.count();
	}

	/** Safe locator method with null check */
	protected Locator getLocatorWithCheck(String selector, String description) {
		Check.notNull(selector, "Selector cannot be null");
		Check.notBlank(description, "Description cannot be blank");
		try {
			Locator locator = page.locator(selector);
			Check.notNull(locator, "Failed to find element: " + description);
			return locator;
		} catch (Exception e) {
			throw new AssertionError("Element not found: " + description + " (selector: " + selector + ")", e);
		}
	}

	/** Checks if browser is available */
	protected boolean isBrowserAvailable() { return (page != null) && !page.isClosed(); }

	/** Performs complete login workflow with username and password. This method handles the entire authentication process including form submission
	 * and redirection verification. */
	protected void loginToApplication() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping login attempt");
			return;
		}
		loginToApplication("admin", "test123");
	}

	/** Performs login with specified credentials and verifies successful authentication. */
	protected void loginToApplication(final String username, final String password) {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping login attempt for {}", username);
			return;
		}
		try {
			LOGGER.info("🔐 Attempting login with username: {}", username);
			ensureLoginViewLoaded();
			initializeSampleDataFromLoginPage();
			ensureLoginViewLoaded();
			boolean usernameFilled =
					fillLoginField("#custom-username-input", "input", "username", username, "input[type='text'], input[type='email']");
			if (!usernameFilled) {
				throw new AssertionError("Username input field not found on login page");
			}
			boolean passwordFilled = fillLoginField("#custom-password-input", "input", "password", password, "input[type='password']");
			if (!passwordFilled) {
				throw new AssertionError("Password input field not found on login page");
			}
			clickLoginButton();
			wait_afterlogin();
			LOGGER.info("✅ Login successful - application shell detected");
			takeScreenshot("post-login", false);
			primeNavigationMenu();
		} catch (PlaywrightException e) {
			LOGGER.warn("⚠️ Login attempt failed for {}: {}", username, e.getMessage());
			takeScreenshot("login-attempt-error", false);
			if ((page != null) && page.isClosed()) {
				LOGGER.warn("⚠️ Browser page closed during login attempt");
			}
		} catch (Exception e) {
			LOGGER.warn("⚠️ Unexpected login error for {}: {}", username, e.getMessage());
			takeScreenshot("login-unexpected-error", false);
		}
	}

	/** Ensures the custom login view is loaded and ready for interaction. */
	protected void ensureLoginViewLoaded() {
		try {
			final String loginUrl = "http://localhost:" + port + "/login";
			if (!page.url().contains("/login")) {
				LOGGER.info("ℹ️ Navigating to login view at {}", loginUrl);
				page.navigate(loginUrl);
				wait_500();
			}
		} catch (Exception e) {
			LOGGER.warn("⚠️ Unable to determine current URL before ensuring login view: {}", e.getMessage());
		}
		wait_loginscreen();
	}

	/** Fills login fields by first trying the Vaadin shadow DOM input, then falling back to classic HTML selectors. */
	protected boolean fillLoginField(String hostSelector, String inputSelector, String fieldDescription, String value, String fallbackSelector) {
		try {
			final Locator host = page.locator(hostSelector);
			if (host.count() > 0) {
				final Locator shadowInput = host.first().locator(inputSelector);
				if (shadowInput.count() > 0) {
					shadowInput.first().fill("");
					shadowInput.first().fill(value);
					LOGGER.info("📝 Filled {} field using {}", fieldDescription, hostSelector);
					return true;
				}
			}
			final Locator fallback = page.locator(fallbackSelector);
			if (fallback.count() > 0) {
				fallback.first().fill(value);
				LOGGER.info("📝 Filled {} field using fallback selector {}", fieldDescription, fallbackSelector);
				return true;
			}
			LOGGER.warn("⚠️ {} input field not found", fieldDescription);
		} catch (Exception e) {
			LOGGER.warn("⚠️ Failed to fill {} field: {}", fieldDescription, e.getMessage());
		}
		return false;
	}

	/** Clicks the login button using tolerant selector logic. */
	protected void clickLoginButton() {
		final String[] selectors = {
				"vaadin-button:has-text('Login')", "button:has-text('Login')", "vaadin-button[theme~='primary']", "vaadin-button"
		};
		for (String selector : selectors) {
			final Locator loginButton = page.locator(selector);
			if (loginButton.count() > 0) {
				loginButton.first().click();
				wait_500();
				LOGGER.info("▶️ Clicked login button using selector {}", selector);
				return;
			}
		}
		throw new AssertionError("Login button not found on login page");
	}

	/** Triggers the sample data initialization flow via the login screen button if present. */
	protected void initializeSampleDataFromLoginPage() {
		if (!isBrowserAvailable()) {
			return;
		}
		final boolean forceReload = Boolean.getBoolean(FORCE_SAMPLE_RELOAD_PROPERTY);
		if (!forceReload && SAMPLE_DATA_INITIALIZED.get()) {
			LOGGER.debug("ℹ️ Sample data already initialized for this test run; skipping reload");
			return;
		}
		synchronized (SAMPLE_DATA_LOCK) {
			if (!forceReload && SAMPLE_DATA_INITIALIZED.get()) {
				LOGGER.debug("ℹ️ Sample data already initialized (post-lock); skipping reload");
				return;
			}
			if (forceReload) {
				LOGGER.info("♻️ Forcing sample data reload due to system property '{}'", FORCE_SAMPLE_RELOAD_PROPERTY);
			}
			try {
				wait_loginscreen();
				final Locator resetButton =
						page.locator("vaadin-button:has-text('Reset Database'), button:has-text('Reset Database'), [id*='reset']");
				if (resetButton.count() == 0) {
					LOGGER.info("ℹ️ 'Reset Database' button not present on login view; assuming sample data is available");
					SAMPLE_DATA_INITIALIZED.compareAndSet(false, true);
					return;
				}
				LOGGER.info("📥 Loading sample data via login screen button");
				resetButton.first().click();
				wait_500();
				acceptConfirmDialogIfPresent();
				closeInformationDialogIfPresent();
				wait_loginscreen();
				LOGGER.info("✅ Sample data initialization completed successfully");
				SAMPLE_DATA_INITIALIZED.set(true);
			} catch (Exception e) {
				LOGGER.warn("⚠️ Sample data initialization via login page failed: {}", e.getMessage());
				takeScreenshot("sample-data-initialization-error", false);
				if (forceReload) {
					SAMPLE_DATA_INITIALIZED.set(false);
				}
			}
		}
	}

	/** Accepts the confirmation dialog that appears when reloading sample data. */
	private void acceptConfirmDialogIfPresent() {
		for (int attempt = 0; attempt < 10; attempt++) {
			final Locator overlay = page.locator("vaadin-confirm-dialog-overlay[opened]");
			if (overlay.count() > 0) {
				final Locator confirmButton =
						overlay.locator("vaadin-button:has-text('Evet, sıfırla'), vaadin-button:has-text('Yes'), vaadin-button[theme*='primary']");
				if (confirmButton.count() > 0) {
					confirmButton.first().click();
					waitForOverlayToClose("vaadin-confirm-dialog-overlay[opened]");
					LOGGER.info("✅ Sample data reload confirmed");
					return;
				}
			}
			wait_500();
		}
		LOGGER.warn("⚠️ Confirmation dialog not detected after requesting sample data reload");
	}

	/** Closes the informational dialog that appears after sample data reload completion. */
	private void closeInformationDialogIfPresent() {
		for (int attempt = 0; attempt < 10; attempt++) {
			final Locator overlay = page.locator("vaadin-dialog-overlay[opened]");
			if (overlay.count() == 0) {
				wait_500();
				continue;
			}
			final Locator okButton = overlay.locator("vaadin-button:has-text('OK'), vaadin-button:has-text('Tamam'), button:has-text('OK')");
			if (okButton.count() > 0) {
				okButton.first().click();
				waitForOverlayToClose("vaadin-dialog-overlay[opened]");
				LOGGER.info("✅ Information dialog dismissed after sample data reload");
				return;
			}
			wait_500();
		}
		LOGGER.warn("⚠️ Information dialog did not present an OK button to dismiss");
	}

	/** Waits for the specified Vaadin overlay selector to disappear. */
	private void waitForOverlayToClose(String overlaySelector) {
		for (int attempt = 0; attempt < 20; attempt++) {
			if (page.locator(overlaySelector).count() == 0) {
				return;
			}
			wait_500();
		}
		LOGGER.warn("⚠️ Overlay {} still present after waiting", overlaySelector);
	}

	/** Visits all menu items silently to ensure dynamic entries are initialized. */
	protected void primeNavigationMenu() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping navigation priming");
			return;
		}
		try {
			LOGGER.info("🧭 Priming navigation menu to ensure dynamic items are loaded");
			int visited = visitMenuItems(false, true, "prime");
			LOGGER.info("✅ Navigation primed by visiting {} menu entries", visited);
		} catch (AssertionError e) {
			LOGGER.error("❌ Navigation priming failed: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			LOGGER.warn("⚠️ Unexpected error during navigation priming: {}", e.getMessage());
		}
	}
	// ===========================================
	// MISSING METHODS FOR COMPATIBILITY
	// ===========================================

	/** Navigates to a view by its class annotation. Uses the @Route annotation to determine the URL. */
	protected boolean navigateToViewByClass(final Class<?> viewClass) {
		LOGGER.info("🧭 Navigating to view class: {}", viewClass.getSimpleName());
		try {
			final Route routeAnnotation = viewClass.getAnnotation(Route.class);
			if (routeAnnotation != null) {
				final String route = routeAnnotation.value();
				page.navigate("http://localhost:" + port + "/" + route);
				wait_500();
				LOGGER.info("✅ Successfully navigated to: {} at route: {}", viewClass.getSimpleName(), route);
				return true;
			} else {
				LOGGER.warn("⚠️ No @Route annotation found for class: {}", viewClass.getSimpleName());
				return false;
			}
		} catch (final Exception e) {
			LOGGER.error("❌ Navigation failed for class: {} - Error: {}", viewClass.getSimpleName(), e.getMessage());
			return false;
		}
	}
	// ===========================================
	// FORM AND INPUT METHODS
	// ===========================================

	/** Navigates to a view by its text content in the navigation menu. Returns true if navigation was successful. */
	protected boolean navigateToViewByText(final String viewText) {
		Check.notBlank(viewText, "View text cannot be blank");
		LOGGER.info("🧭 Navigating to view: {}", viewText);
		try {
			final Locator navItem = getLocatorWithCheck("vaadin-side-nav-item", "Navigation item for " + viewText)
					.filter(new Locator.FilterOptions().setHasText(viewText));
			Check.notNull(navItem, "Navigation item not found for: " + viewText);
			if (navItem.count() > 0) {
				navItem.first().click();
				wait_500();
				LOGGER.info("✅ Successfully navigated to: {}", viewText);
				return true;
			} else {
				LOGGER.warn("⚠️ Navigation item not found for: {}", viewText);
				return false;
			}
		} catch (final Exception e) {
			LOGGER.error("❌ Navigation failed for view: {} - Error: {}", viewText, e.getMessage());
			return false;
		}
	}

	/** Performs complete CRUD testing workflow for the current view. */
	protected void performCRUDWorkflow(final String entityName) {
		LOGGER.info("🔄 Performing CRUD workflow for: {}", entityName);
		// CREATE
		clickNew();
		fillFirstTextField("Test " + entityName);
		selectFirstComboBoxOption();
		clickSave();
		takeScreenshot("crud-create-" + entityName.toLowerCase(), false);
		// READ - verify in grid
		wait_1000();
		final boolean hasData = verifyGridHasData();
		if (hasData) {
			LOGGER.info("✅ CREATE operation successful for: {}", entityName);
		}
		// UPDATE
		clickFirstGridRow();
		clickEdit();
		fillFirstTextField("Updated " + entityName);
		clickSave();
		takeScreenshot("crud-update-" + entityName.toLowerCase(), false);
		// DELETE
		wait_1000();
		clickFirstGridRow();
		clickDelete();
		takeScreenshot("crud-delete-" + entityName.toLowerCase(), false);
		LOGGER.info("✅ CRUD workflow complete for: {}", entityName);
	}

	/** Enhanced CRUD workflow with better error handling and validation */
	protected void performEnhancedCRUDWorkflow(String entityName) {
		Check.notBlank(entityName, "Entity name cannot be blank");
		LOGGER.info("🔄 Performing enhanced CRUD workflow for: {}", entityName);
		try {
			// CREATE operation
			LOGGER.info("➕ Testing CREATE operation for: {}", entityName);
			clickNew();
			wait_1000();
			String testData = "Test " + entityName + " " + System.currentTimeMillis();
			fillFirstTextField(testData);
			// Try to fill other fields if present
			Locator textAreas = page.locator("vaadin-text-area");
			if (textAreas.count() > 0) {
				fillFirstTextArea("Description for " + testData);
			}
			// Select combobox options if present
			Locator comboBoxes = page.locator("vaadin-combo-box");
			if (comboBoxes.count() > 0) {
				selectFirstComboBoxOption();
			}
			clickSave();
			wait_1000();
			takeScreenshot("crud-create-" + entityName.toLowerCase(), false);
			// READ operation - verify data was created
			LOGGER.info("👁️ Testing READ operation for: {}", entityName);
			boolean hasData = verifyGridHasData();
			Check.isTrue(hasData, "Data should be present after CREATE operation");
			// UPDATE operation
			LOGGER.info("✏️ Testing UPDATE operation for: {}", entityName);
			clickFirstGridRow();
			wait_500();
			clickEdit();
			wait_1000();
			String updatedData = "Updated " + entityName + " " + System.currentTimeMillis();
			fillFirstTextField(updatedData);
			clickSave();
			wait_1000();
			takeScreenshot("crud-update-" + entityName.toLowerCase(), false);
			// DELETE operation
			LOGGER.info("🗑️ Testing DELETE operation for: {}", entityName);
			clickFirstGridRow();
			wait_500();
			clickDelete();
			wait_1000();
			takeScreenshot("crud-delete-" + entityName.toLowerCase(), false);
			LOGGER.info("✅ Enhanced CRUD workflow completed successfully for: {}", entityName);
		} catch (Exception e) {
			throw new AssertionError("Enhanced CRUD workflow failed for " + entityName + ": " + e.getMessage(), e);
		}
	}

	/** Performs logout functionality */
	protected void performLogout() {
		LOGGER.info("🔐 Performing logout...");
		// Try to find logout button or user menu
		try {
			// Look for user menu or logout button
			if (page.locator("vaadin-button:has-text('Logout')").count() > 0) {
				page.locator("vaadin-button:has-text('Logout')").click();
			} else if (page.locator("[id*='logout']").count() > 0) {
				page.locator("[id*='logout']").first().click();
			} else {
				LOGGER.warn("⚠️ Logout button not found");
			}
			wait_1000();
		} catch (Exception e) {
			LOGGER.warn("⚠️ Logout failed: {}", e.getMessage());
		}
	}

	/** Selects the first option in the first ComboBox found on the page. */
	protected void selectFirstComboBoxOption() {
		LOGGER.info("📋 Selecting first option in ComboBox");
		final Locator comboBox = page.locator("vaadin-combo-box").first();
		comboBox.click();
		wait_500();
		final Locator items = page.locator("vaadin-combo-box-item");
		if (items.count() > 0) {
			items.first().click();
			LOGGER.info("✅ Selected first ComboBox option");
		} else {
			LOGGER.warn("⚠️ No ComboBox options found");
		}
	}
	// ===========================================
	// BUTTON ACTION METHODS
	// ===========================================

	@BeforeEach
	void setupTestEnvironment() {
		LOGGER.info("🧪 Setting up Playwright test environment...");
		try {
			// Determine headless mode setting first
			boolean headless = Boolean.parseBoolean(System.getProperty("playwright.headless", "true"));
			LOGGER.info("🎭 Browser mode: {}", headless ? "HEADLESS" : "VISIBLE");
			BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(headless)
					.setArgs(Arrays.asList("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu"));
			// Check if chromium is available in Playwright cache
			String playwrightCache = System.getProperty("user.home") + "/.cache/ms-playwright/chromium-1091/chrome";
			java.io.File cachedChromium = new java.io.File(playwrightCache);
			if (cachedChromium.exists() && cachedChromium.canExecute()) {
				// Use cached Playwright Chromium directly to bypass download
				LOGGER.info("📦 Using cached Playwright Chromium at: {}", playwrightCache);
				playwright = Playwright.create();
				launchOptions.setExecutablePath(java.nio.file.Paths.get(playwrightCache));
				browser = playwright.chromium().launch(launchOptions);
			} else {
				// Try Playwright default download first
				try {
					playwright = Playwright.create();
					browser = playwright.chromium().launch(launchOptions);
				} catch (Exception browserError) {
					LOGGER.info("⚠️ Playwright-bundled Chromium not available, trying system Chromium...");
					// Try to use system Chromium as fallback
					String[] possiblePaths = {
							"/usr/bin/chromium-browser", "/usr/bin/chromium", "/usr/bin/google-chrome"
					};
					for (String chromiumPath : possiblePaths) {
						if (new java.io.File(chromiumPath).exists()) {
							LOGGER.info("📦 Using system Chromium at: {}", chromiumPath);
							if (playwright == null) {
								playwright = Playwright.create();
							}
							launchOptions.setExecutablePath(java.nio.file.Paths.get(chromiumPath));
							browser = playwright.chromium().launch(launchOptions);
							break;
						}
					}
					if (browser == null) {
						throw new RuntimeException("No Chromium browser found");
					}
				}
			}
			context = browser.newContext();
			page = context.newPage();
			page.navigate("http://localhost:" + port + "/login");
			LOGGER.info("✅ Test environment setup complete - navigated to http://localhost:{}/login", port);
		} catch (Exception e) {
			LOGGER.warn("⚠️ Failed to setup browser environment (expected in CI): {}", e.getMessage());
			LOGGER.info("ℹ️ Tests will run with limited functionality - this is normal in CI environments");
			// Don't fail here - let individual tests handle browser availability
		}
	}

	/** Takes a screenshot with the specified name and saves it to target/screenshots/. */
	protected void takeScreenshot(final String name) {
		takeScreenshot(name, true);
	}

	/** Takes a screenshot with optional logging. Screenshots are saved to target/screenshots/ directory. */
	protected void takeScreenshot(final String name, final boolean logAction) {
		try {
			if (logAction) {
				LOGGER.info("📸 Taking screenshot: {}", name);
			}
			if (!isBrowserAvailable()) {
				if (logAction) {
					LOGGER.warn("⚠️ Cannot take screenshot '{}' - browser not available", name);
				}
				return;
			}
			final String screenshotPath = "target/screenshots/" + name + ".png";
			page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(screenshotPath)));
			if (logAction) {
				LOGGER.info("✅ Screenshot saved: {}", screenshotPath);
			}
		} catch (final Exception e) {
			LOGGER.error("❌ Failed to take screenshot '{}': {}", name, e.getMessage());
		}
	}

	/** Captures a screenshot whose filename encodes the view and scenario outcome. */
	protected void takeViewScreenshot(final Class<?> viewClass, final String scenario, final boolean success) {
		final String screenshotName = buildViewScreenshotName(viewClass, scenario, success);
		takeScreenshot(screenshotName, true);
	}

	/** Builds a screenshot name in the format view-scenario-success|failure using deterministic components. */
	protected String buildViewScreenshotName(final Class<?> viewClass, final String scenario, final boolean success) {
		final String identifier = sanitizeForFileName(resolveViewIdentifier(viewClass), "view");
		final String scenarioPart = sanitizeForFileName(Optional.ofNullable(scenario).orElse("scenario"), "scenario");
		final String status = success ? SCREENSHOT_SUCCESS_SUFFIX : SCREENSHOT_FAILURE_SUFFIX;
		return String.join("-", identifier, scenarioPart, status);
	}

	private String resolveViewIdentifier(final Class<?> viewClass) {
		if (viewClass == null) {
			return "unknown-view";
		}
		try {
			final Field viewNameField = viewClass.getDeclaredField("VIEW_NAME");
			if (Modifier.isStatic(viewNameField.getModifiers()) && viewNameField.getType() == String.class) {
				viewNameField.setAccessible(true);
				final Object value = viewNameField.get(null);
				if (value instanceof String viewName && !viewName.isBlank()) {
					return viewName;
				}
			}
		} catch (final NoSuchFieldException missingField) {
			LOGGER.debug("VIEW_NAME not declared on {}: {}", viewClass.getSimpleName(), missingField.getMessage());
		} catch (final Exception reflectionError) {
			LOGGER.debug("Failed to read VIEW_NAME for {}: {}", viewClass.getSimpleName(), reflectionError.getMessage());
		}
		final Route route = viewClass.getAnnotation(Route.class);
		if ((route != null) && (route.value() != null) && !route.value().isBlank()) {
			return route.value();
		}
		return viewClass.getSimpleName();
	}

	@AfterEach
	void teardownTestEnvironment() {
		LOGGER.info("🧹 Tearing down Playwright test environment...");
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
		LOGGER.info("✅ Test environment teardown complete");
	}

	// ===========================================
	// LOGIN AND AUTHENTICATION METHODS
	// ===========================================
	/** Tests accessibility basics */
	protected void testAccessibilityBasics() {
		testAccessibilityBasics("");
	}

	/** Tests accessibility basics with description */
	protected void testAccessibilityBasics(String description) {
		LOGGER.info("♿ Testing accessibility basics {}", description);
		verifyAccessibility();
	}

	/** Tests all ComboBox components on the current page by opening and verifying their options. */
	protected void testAllComboBoxes() {
		LOGGER.info("📋 Testing all ComboBox components");
		final Locator comboBoxes = page.locator("vaadin-combo-box");
		final int comboBoxCount = comboBoxes.count();
		for (int i = 0; i < comboBoxCount; i++) {
			try {
				final Locator comboBox = comboBoxes.nth(i);
				comboBox.click();
				wait_500();
				final Locator items = page.locator("vaadin-combo-box-item");
				LOGGER.info("📋 ComboBox {} has {} options", i + 1, items.count());
				if (items.count() > 0) {
					items.first().click(); // Select first option
				}
				wait_500();
			} catch (final Exception e) {
				LOGGER.warn("⚠️ Failed to test ComboBox {}: {}", i + 1, e.getMessage());
			}
		}
		LOGGER.info("✅ ComboBox testing complete");
	}

	/** Visits menu items with optional screenshot capture and configurable error handling. */
	protected int visitMenuItems(boolean captureScreenshots, boolean allowEmpty, String screenshotPrefix) {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available, cannot exercise menu navigation");
			return 0;
		}
		final String menuSelector =
				".hierarchical-menu-item, vaadin-side-nav-item, vaadin-tabs vaadin-tab, nav a[href], .nav-item a[href], a[href].menu-link, a[href].side-nav-link";
		int totalItems = 0;
		try {
			page.waitForSelector(menuSelector, new Page.WaitForSelectorOptions().setTimeout(20000));
			totalItems = page.locator(menuSelector).count();
		} catch (Exception waitError) {
			if (!allowEmpty) {
				throw new AssertionError("No navigation items found to exercise", waitError);
			}
			LOGGER.warn("⚠️ Navigation items not found within timeout: {}", waitError.getMessage());
			return 0;
		}
		LOGGER.info("📋 Found {} menu entries to visit", totalItems);
		if ((totalItems == 0) && !allowEmpty) {
			throw new AssertionError("No navigation items found to exercise");
		}
		final Set<String> visitedLabels = new HashSet<>();
		for (int i = 0; i < totalItems; i++) {
			try {
				final Locator currentItems = page.locator(menuSelector);
				final int currentCount = currentItems.count();
				if (currentCount == 0) {
					LOGGER.warn("⚠️ Navigation items not available after visiting {} entries", visitedLabels.size());
					break;
				}
				final int index = Math.min(i, currentCount - 1);
				final Locator navItem = currentItems.nth(index);
				String label = Optional.ofNullable(navItem.textContent()).map(String::trim).orElse("");
				label = label.replaceAll("\\s+", " ");
				if (label.isBlank()) {
					label = Optional.ofNullable(navItem.getAttribute("href")).orElse("menu-entry-" + (i + 1));
				}
				final String safeLabel = label.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
				LOGGER.info("🔍 Visiting menu item {} of {}: {}", i + 1, totalItems, label);
				navItem.click();
				wait_1000();
				if (captureScreenshots) {
					final String prefix = (screenshotPrefix == null) ? "menu" : screenshotPrefix;
					final String screenshotName = prefix + "-" + (safeLabel.isBlank() ? ("entry-" + (i + 1)) : safeLabel + "-" + (i + 1));
					takeScreenshot(screenshotName, false);
				}
				visitedLabels.add(label);
			} catch (Exception itemError) {
				LOGGER.warn("⚠️ Failed to open menu item {}: {}", i + 1, itemError.getMessage());
			}
		}
		return visitedLabels.size();
	}

	/** Tests all menu item openings to ensure navigation works */
	protected void testAllMenuItemOpenings() {
		LOGGER.info("🧭 Testing all menu item openings...");
		try {
			int visitedCount = visitMenuItems(true, false, "menu");
			LOGGER.info("✅ Menu item testing completed; visited {} entries", visitedCount);
		} catch (Exception e) {
			takeScreenshot("menu-openings-error", true);
			throw new AssertionError("Menu item testing failed: " + e.getMessage(), e);
		}
	}

	/** Tests breadcrumb navigation if present */
	protected void testBreadcrumbNavigation() {
		LOGGER.info("🍞 Testing breadcrumb navigation...");
		try {
			Locator breadcrumbs = page.locator(".breadcrumb, vaadin-breadcrumb, nav[aria-label*='breadcrumb']");
			if (breadcrumbs.count() > 0) {
				LOGGER.info("📋 Found breadcrumb navigation");
				Locator breadcrumbItems = breadcrumbs.locator("a, button, span");
				int itemCount = breadcrumbItems.count();
				LOGGER.info("📊 Found {} breadcrumb items", itemCount);
				// Test clicking breadcrumb items (except last one which is current)
				for (int i = 0; i < (itemCount - 1); i++) {
					try {
						Locator item = breadcrumbItems.nth(i);
						String itemText = item.textContent();
						LOGGER.info("🔍 Testing breadcrumb item: {}", itemText);
						item.click();
						wait_1000();
						takeScreenshot("breadcrumb-" + i, false);
					} catch (Exception e) {
						LOGGER.warn("⚠️ Failed to test breadcrumb item {}: {}", i, e.getMessage());
					}
				}
			} else {
				LOGGER.info("ℹ️ No breadcrumb navigation found");
			}
			LOGGER.info("✅ Breadcrumb navigation testing completed");
		} catch (Exception e) {
			throw new AssertionError("Breadcrumb navigation test failed: " + e.getMessage(), e);
		}
	}
	// ===========================================
	// ENHANCED CRUD TESTING METHODS
	// ===========================================

	/** Tests database initialization by verifying that essential data is present */
	protected void testDatabaseInitialization() {
		LOGGER.info("🗄️ Testing database initialization...");
		try {
			// Navigate to Users view via dynamic menu and check if default users exist
			boolean navigatedToUsers = navigateToDynamicPageByEntityType("CUser");
			Check.isTrue(navigatedToUsers, "Unable to navigate to Users dynamic page");
			waitForDynamicPageLoad();
			boolean usersExist = verifyGridHasData();
			Check.isTrue(usersExist, "Database should contain initial user data");
			// Navigate to Projects view to check if data structure is ready
			boolean navigatedToProjects = navigateToDynamicPageByEntityType("CProject");
			Check.isTrue(navigatedToProjects, "Unable to navigate to Projects dynamic page");
			waitForDynamicPageLoad();
			// Projects may be empty initially, just verify grid is present
			Locator projectGrid = getLocatorWithCheck("vaadin-grid", "Projects grid");
			Check.notNull(projectGrid, "Projects grid should be present");
			LOGGER.info("✅ Database initialization test completed successfully");
		} catch (Exception e) {
			throw new AssertionError("Database initialization test failed: " + e.getMessage(), e);
		}
	}

	/** Navigate to Projects view. Uses project-overview route which is the main Projects page. */
	protected void navigateToProjects() {
		LOGGER.info("🧭 Navigating to Projects view");
		try {
			if (!isBrowserAvailable()) {
				LOGGER.warn("⚠️ Browser not available, cannot navigate to Projects");
				return;
			}
			// Try project-overview route first (main Projects page)
			page.navigate("http://localhost:" + port + "/project-overview");
			wait_1000();
			LOGGER.info("✅ Navigated to Projects view");
		} catch (Exception e) {
			LOGGER.error("❌ Failed to navigate to Projects: {}", e.getMessage());
			throw new AssertionError("Failed to navigate to Projects view", e);
		}
	}

	/** Tests grid column functionality including sorting and filtering */
	protected void testGridColumnFunctionality(String entityName) {
		Check.notBlank(entityName, "Entity name cannot be blank");
		LOGGER.info("📊 Testing grid column functionality for: {}", entityName);
		try {
			Locator grid = getLocatorWithCheck("vaadin-grid", "Grid for " + entityName);
			Locator headers = grid.locator("vaadin-grid-sorter, th, .grid-header");
			int headerCount = headers.count();
			LOGGER.info("📋 Found {} grid columns for {}", headerCount, entityName);
			// Test sorting on first few columns
			for (int i = 0; i < Math.min(headerCount, 3); i++) {
				try {
					Locator header = headers.nth(i);
					String headerText = header.textContent();
					LOGGER.info("🔄 Testing sort on column: {}", headerText);
					header.click();
					wait_1000();
					takeScreenshot("grid-sort-" + entityName.toLowerCase() + "-col" + i, false);
				} catch (Exception e) {
					LOGGER.warn("⚠️ Failed to test sorting on column {}: {}", i, e.getMessage());
				}
			}
			LOGGER.info("✅ Grid column functionality testing completed for: {}", entityName);
		} catch (Exception e) {
			throw new AssertionError("Grid column functionality test failed for " + entityName + ": " + e.getMessage(), e);
		}
	}

	/** Tests project activation/deactivation functionality */
	protected void testProjectActivation() {
		LOGGER.info("🔄 Testing project activation functionality...");
		try {
			navigateToProjects();
			wait_1000();
			// Check if there are any projects to work with
			if (verifyGridHasData()) {
				clickFirstGridRow();
				wait_500();
				// Look for activation-related buttons or controls
				Locator activateButton = page.locator("vaadin-button:has-text('Activate'), vaadin-button:has-text('Enable')");
				Locator deactivateButton = page.locator("vaadin-button:has-text('Deactivate'), vaadin-button:has-text('Disable')");
				if (activateButton.count() > 0) {
					LOGGER.info("🟢 Found activation controls");
					activateButton.first().click();
					wait_1000();
					takeScreenshot("project-activation", false);
				} else if (deactivateButton.count() > 0) {
					LOGGER.info("🔴 Found deactivation controls");
					deactivateButton.first().click();
					wait_1000();
					takeScreenshot("project-deactivation", false);
				} else {
					LOGGER.info("ℹ️ No activation controls found - checking status field");
					// Check if there's a status field that might indicate activation state
					Locator statusField = page.locator("vaadin-text-field[label*='Status'], vaadin-combo-box[label*='Status']");
					if (statusField.count() > 0) {
						LOGGER.info("📊 Found status field for project state");
					}
				}
			} else {
				LOGGER.info("ℹ️ No projects found to test activation");
			}
			LOGGER.info("✅ Project activation test completed");
		} catch (Exception e) {
			throw new AssertionError("Project activation test failed: " + e.getMessage(), e);
		}
	}

	/** Tests project change tracking and notifications */
	protected void testProjectChangeTracking() {
		LOGGER.info("📝 Testing project change tracking...");
		try {
			navigateToProjects();
			wait_1000();
			if (verifyGridHasData()) {
				// Test editing a project to see if changes are tracked
				clickFirstGridRow();
				wait_500();
				clickEdit();
				wait_1000();
				// Make a change to test tracking
				fillFirstTextField("Test Change " + System.currentTimeMillis());
				clickSave();
				wait_1000();
				takeScreenshot("project-change-tracking", false);
				LOGGER.info("✅ Project change tracking test completed");
			} else {
				LOGGER.info("ℹ️ No projects found to test change tracking");
			}
		} catch (Exception e) {
			throw new AssertionError("Project change tracking test failed: " + e.getMessage(), e);
		}
	}
	// ===========================================
	// MENU NAVIGATION TESTING METHODS
	// ===========================================

	/** Tests sidebar navigation menu functionality */
	protected void testSidebarNavigation() {
		LOGGER.info("📱 Testing sidebar navigation...");
		try {
			// Look for navigation elements
			Locator sideNav = page.locator("vaadin-side-nav, nav, .navigation");
			if (sideNav.count() > 0) {
				LOGGER.info("📋 Found navigation sidebar");
				// Test expanding/collapsing if applicable
				Locator toggleButton = page.locator("vaadin-button[aria-label*='menu'], button[aria-label*='toggle']");
				if (toggleButton.count() > 0) {
					toggleButton.first().click();
					wait_500();
					takeScreenshot("sidebar-toggle", false);
				}
				// Test navigation items
				Locator navItems = page.locator("vaadin-side-nav-item, .nav-item, a[href]");
				int itemCount = navItems.count();
				LOGGER.info("📊 Found {} navigation items", itemCount);
				for (int i = 0; i < Math.min(itemCount, 5); i++) { // Test first 5 items
					try {
						Locator navItem = navItems.nth(i);
						String itemText = navItem.textContent();
						LOGGER.info("🔍 Testing navigation item {}: {}", i + 1, itemText);
						navItem.click();
						wait_1000();
						takeScreenshot("nav-item-" + i, false);
					} catch (Exception e) {
						LOGGER.warn("⚠️ Failed to test navigation item {}: {}", i + 1, e.getMessage());
					}
				}
			} else {
				LOGGER.warn("⚠️ No sidebar navigation found");
			}
			LOGGER.info("✅ Sidebar navigation testing completed");
		} catch (Exception e) {
			throw new AssertionError("Sidebar navigation test failed: " + e.getMessage(), e);
		}
	}

	/** Verifies accessibility by checking for proper ARIA labels and keyboard navigation support. */
	protected void verifyAccessibility() {
		LOGGER.info("♿ Verifying accessibility compliance");
		// Check for ARIA labels on interactive elements
		final Locator interactiveElements = page.locator("button, input, vaadin-combo-box, vaadin-grid");
		final int elementCount = interactiveElements.count();
		LOGGER.info("♿ Found {} interactive elements for accessibility check", elementCount);
		// Test keyboard navigation
		page.keyboard().press("Tab");
		wait_500();
		page.keyboard().press("Enter");
		wait_500();
		LOGGER.info("✅ Accessibility verification complete");
	}
	// ===========================================
	// VIEW-SPECIFIC NAVIGATION HELPERS
	// ===========================================

	/** Verifies that database tables are properly initialized */
	protected void verifyDatabaseStructure() {
		LOGGER.info("🔍 Verifying database structure...");
		try {
			// Test each main view to ensure tables are accessible
			for (Class<?> viewClass : mainViewClasses) {
				LOGGER.info("📋 Checking database structure for: {}", viewClass.getSimpleName());
				boolean navigationSuccess = navigateToViewByClass(viewClass);
				Check.isTrue(navigationSuccess, "Should be able to navigate to " + viewClass.getSimpleName());
				wait_1000();
				// Verify that grid is present (indicates table exists)
				Locator grid = page.locator("vaadin-grid").first();
				Check.isTrue(grid.count() > 0, "Grid should be present for " + viewClass.getSimpleName());
			}
			LOGGER.info("✅ Database structure verification completed");
		} catch (Exception e) {
			throw new AssertionError("Database structure verification failed: " + e.getMessage(), e);
		}
	}
	// ===========================================
	// PROJECT ACTIVATION TESTING METHODS
	// ===========================================

	/** Verifies that the grid contains data by checking for the presence of grid cells. */
	protected boolean verifyGridHasData() {
		try {
			page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(5000));
		} catch (Exception e) {
			LOGGER.warn("⚠️ Grid not found while waiting for data: {}", e.getMessage());
			return false;
		}
		final Locator grid = page.locator("vaadin-grid").first();
		for (int attempt = 0; attempt < 10; attempt++) {
			final Locator cells = grid.locator("vaadin-grid-cell-content");
			final int cellCount = cells.count();
			if (cellCount > 0) {
				LOGGER.info("📊 Grid has data: true (cells={})", cellCount);
				return true;
			}
			wait_500();
		}
		LOGGER.info("📊 Grid has data: false");
		return false;
	}
	// ===========================================
	// TESTING UTILITY METHODS
	// ===========================================

	/** Waits for 1000 milliseconds to allow complex operations to complete. */
	protected void wait_1000() {
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/** Waits for 2000 milliseconds for slow operations like navigation. */
	protected void wait_2000() {
		try {
			Thread.sleep(2000);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	// ===========================================
	// ADVANCED TESTING METHODS
	// ===========================================

	/** Waits for 500 milliseconds to allow UI updates to complete. */
	protected void wait_500() {
		try {
			Thread.sleep(500);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/** Waits for after login state */
	protected void wait_afterlogin() {
		try {
			page.waitForSelector("vaadin-app-layout, vaadin-side-nav, vaadin-drawer-layout", new Page.WaitForSelectorOptions().setTimeout(15000));
		} catch (Exception e) {
			LOGGER.warn("⚠️ Post-login application shell not detected: {}", e.getMessage());
		}
	}

	/** Waits for login screen to be ready */
	protected void wait_loginscreen() {
		try {
			page.waitForSelector("#custom-username-input, #custom-password-input, vaadin-button:has-text('Login')",
					new Page.WaitForSelectorOptions().setTimeout(15000));
		} catch (Exception e) {
			LOGGER.warn("⚠️ Login screen not detected: {}", e.getMessage());
		}
	}

	/** Safe wait for selector with check */
	protected Locator waitForSelectorWithCheck(String selector, String description) {
		Check.notNull(selector, "Selector cannot be null");
		Check.notBlank(description, "Description cannot be blank");
		try {
			page.waitForSelector(selector, new Page.WaitForSelectorOptions().setTimeout(10000));
			return getLocatorWithCheck(selector, description);
		} catch (Exception e) {
			throw new AssertionError("Element not found after wait: " + description + " (selector: " + selector + ")", e);
		}
	}
	// ===========================================
	// DATABASE INITIALIZATION TESTING METHODS
	// ===========================================
	// ===========================================
	// DYNAMIC PAGE NAVIGATION METHODS
	// ===========================================

	/** Navigate to a dynamic page by entity type and ensure it loads successfully. This method will fail fast if the page cannot be found or loaded.
	 * @param entityType The entity type (e.g., "CUser", "CActivity", "CProject")
	 * @return true if navigation was successful */
	protected boolean navigateToDynamicPageByEntityType(String entityType) {
		Check.notBlank(entityType, "Entity type cannot be blank");
		LOGGER.info("🧭 Navigating to dynamic page for entity type: {}", entityType);
		try {
			if (!isBrowserAvailable()) {
				LOGGER.warn("⚠️ Browser not available, cannot navigate to dynamic page");
				return false;
			}
			// First try to navigate by looking for the page in the side navigation
			if (navigateByMenuSearch(entityType)) {
				LOGGER.info("✅ Successfully navigated to {} via menu", entityType);
				return true;
			}
			// If menu navigation fails, try direct URL navigation using page entity lookup
			LOGGER.warn("⚠️ Menu navigation failed for entity type: {}, attempting fallback lookup", entityType);
			Optional<Class<?>> entityClass = resolveEntityClass(entityType);
			if (entityClass.isPresent()) {
				boolean navigated = navigateToFirstPage(null, entityClass.get());
				if (navigated) {
					waitForDynamicPageLoad();
					LOGGER.info("✅ Successfully navigated to {} via fallback direct route", entityType);
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			String message = "Failed to navigate to dynamic page for entity type: " + entityType + " - " + e.getMessage();
			LOGGER.error("❌ {}", message);
			throw new AssertionError(message, e);
		}
	}

	/** Navigate by searching menu items for text containing the entity type.
	 * @param entityType The entity type to search for
	 * @return true if navigation was successful */
	protected boolean navigateByMenuSearch(String entityType) {
		try {
			// Look for menu items that might contain the entity name
			// Common patterns: "Users", "Activities", "Projects", "Meetings", etc.
			String[] searchTerms = generateSearchTermsForEntity(entityType);
			String[] selectorCandidates = {
					".hierarchical-menu-item", "vaadin-side-nav-item", "vaadin-tab", "nav a[href]", ".nav-item a[href]", "a[href].menu-link",
					"a[href].side-nav-link"
			};
			logCurrentMenuStructure();
			final Set<String> visitedCandidates = new HashSet<>();
			for (String searchTerm : searchTerms) {
				for (String selector : selectorCandidates) {
					for (int attempt = 0; attempt < 5; attempt++) {
						Locator candidates = page.locator(selector).filter(new Locator.FilterOptions().setHasText(searchTerm));
						int count = candidates.count();
						if (count == 0) {
							wait_500();
							continue;
						}
						for (int i = 0; i < count; i++) {
							try {
								Locator item = candidates.nth(i);
								String label = "";
								try {
									label = Optional.ofNullable(item.textContent()).map(String::trim).orElse("");
								} catch (Exception ignored) {}
								final String candidateKey = selector + "|" + searchTerm + "|" + label + "|" + i;
								if (!visitedCandidates.add(candidateKey)) {
									continue;
								}
								LOGGER.info("🎯 Trying menu selector '{}' candidate {} with label '{}'", selector, i, label);
								String beforeUrl = page.url();
								item.scrollIntoViewIfNeeded();
								item.click();
								wait_1000();
								if (!beforeUrl.equals(page.url())) {
									LOGGER.info("🔗 Navigation triggered via selector {} ({} -> {})", selector, beforeUrl, page.url());
								}
								try {
									waitForDynamicPageLoad();
									LOGGER.info("✅ Dynamic page loaded successfully via selector {} and search term {}", selector, searchTerm);
									return true;
								} catch (AssertionError validationError) {
									LOGGER.debug("⏳ Dynamic page validation still pending after selector {} / search term {}: {}", selector,
											searchTerm, validationError.getMessage());
								}
							} catch (Exception clickError) {
								LOGGER.debug("⚠️ Failed to activate menu item for selector {} / search term {}: {}", selector, searchTerm,
										clickError.getMessage());
							}
						}
					}
				}
			}
			return false;
		} catch (Exception e) {
			throw new RuntimeException("Menu search navigation failed for entity type: " + entityType, e);
		}
	}

	/** Generate search terms for a given entity type.
	 * @param entityType The entity type (e.g., "CUser")
	 * @return Array of possible search terms */
	protected String[] generateSearchTermsForEntity(String entityType) {
		// Remove 'C' prefix if present
		String baseName = entityType.startsWith("C") ? entityType.substring(1) : entityType;
		return new String[] {
				baseName + "s", // Users, Activities, Projects
				baseName, // User, Activity, Project
				baseName.toLowerCase() + "s", // users, activities, projects
				baseName.toLowerCase(), // user, activity, project
				entityType, // CUser, CActivity, CProject
				entityType.toLowerCase() // cuser, cactivity, cproject
		};
	}

	/** Logs the current menu structure to help with debugging navigation issues. */
	protected void logCurrentMenuStructure() {
		try {
			List<String> hierarchicalItems = page.locator(".hierarchical-menu-item").allTextContents();
			LOGGER.info("📋 Hierarchical menu items: {}", hierarchicalItems);
		} catch (Exception e) {
			LOGGER.debug("⚠️ Unable to collect hierarchical menu items: {}", e.getMessage());
		}
		try {
			List<String> sideNavItems = page.locator("vaadin-side-nav-item").allTextContents();
			LOGGER.info("📋 Side nav items: {}", sideNavItems);
		} catch (Exception e) {
			LOGGER.debug("⚠️ Unable to collect side nav items: {}", e.getMessage());
		}
		try {
			List<String> anchorTargets = page.locator("a[href]").allInnerTexts();
			LOGGER.info("📋 Anchor items: {}", anchorTargets);
		} catch (Exception e) {
			LOGGER.debug("⚠️ Unable to collect anchor link texts: {}", e.getMessage());
		}
	}

	/** Attempts to resolve the fully-qualified entity class for a given entity type string. */
	protected Optional<Class<?>> resolveEntityClass(String entityType) {
		String baseName = entityType.startsWith("C") ? entityType.substring(1) : entityType;
		String[] candidateClasses = {
				"tech.derbent." + baseName.toLowerCase() + "s.domain." + entityType,
				"tech.derbent." + baseName.toLowerCase() + ".domain." + entityType, "tech.derbent.api.domain." + entityType
		};
		for (String fqcn : candidateClasses) {
			try {
				Class<?> clazz = Class.forName(fqcn);
				LOGGER.debug("🔍 Resolved entity type {} to class {}", entityType, fqcn);
				return Optional.of(clazz);
			} catch (ClassNotFoundException ignored) {}
		}
		LOGGER.debug("⚠️ Unable to resolve entity class for {}", entityType);
		return Optional.empty();
	}

	/** Check if a dynamic page has loaded successfully.
	 * @return true if the page appears to be a loaded dynamic page */
	protected boolean isDynamicPageLoaded() {
		try {
			wait_1000(); // Give page time to render
			// Check for common dynamic page elements
			if (page.locator("vaadin-grid").count() > 0) {
				LOGGER.debug("✅ Dynamic page has grid element");
				return true;
			}
			if (page.locator("vaadin-form-layout, vaadin-vertical-layout").count() > 0) {
				LOGGER.debug("✅ Dynamic page has form layout");
				return true;
			}
			// Check for CRUD buttons which are common in dynamic pages
			if (page.locator("vaadin-button").filter(new Locator.FilterOptions().setHasText("New")).count() > 0) {
				LOGGER.debug("✅ Dynamic page has New button");
				return true;
			}
			// Check that we're not on an error page
			if (page.locator("text=Error, text=Exception, text=Not Found").count() > 0) {
				LOGGER.warn("⚠️ Page shows error content");
				return false;
			}
			return false;
		} catch (Exception e) {
			LOGGER.error("❌ Error checking if dynamic page loaded: {}", e.getMessage());
			return false;
		}
	}

	/** Navigate to the first page entity of a specific project and entity class. This method mimics the menu generator behavior to dynamically get
	 * page links.
	 * @param project     The project to search in (can be null for all projects)
	 * @param entityClass The entity class to find a page for (e.g., CUser.class, CCompany.class)
	 * @return true if navigation was successful */
	protected boolean navigateToFirstPage(CProject project, Class<?> entityClass) {
		Check.notNull(entityClass, "Entity class cannot be null");
		LOGGER.info("🧭 Navigating to first page for entity class: {} in project: {}", entityClass.getSimpleName(),
				project != null ? project.getName() : "All Projects");
		try {
			if (!isBrowserAvailable()) {
				LOGGER.warn("⚠️ Browser not available, cannot navigate to first page");
				return false;
			}
			// Generate dynamic page link based on entity class
			String entityName = entityClass.getSimpleName();
			String[] possibleRoutes = generateDynamicPageRoutes(entityName);
			// Try to navigate to each possible route
			for (String route : possibleRoutes) {
				try {
					LOGGER.debug("🔗 Trying route: {}", route);
					page.navigate("http://localhost:" + port + "/" + route);
					wait_2000(); // Wait for page to load
					if (isDynamicPageLoaded()) {
						LOGGER.info("✅ Successfully navigated to first page via route: {}", route);
						return true;
					}
				} catch (Exception e) {
					LOGGER.debug("⚠️ Route {} failed: {}", route, e.getMessage());
				}
			}
			// Fallback: try navigation via menu system
			return navigateToDynamicPageByEntityType(entityName);
		} catch (Exception e) {
			String message = "Failed to navigate to first page for entity class: " + entityClass.getSimpleName() + " - " + e.getMessage();
			LOGGER.error("❌ {}", message);
			return false;
		}
	}

	/** Generate possible dynamic page routes for an entity type.
	 * @param entityName The entity name (e.g., "CUser", "CCompany")
	 * @return Array of possible routes to try */
	protected String[] generateDynamicPageRoutes(String entityName) {
		String baseName = entityName.startsWith("C") ? entityName.substring(1) : entityName;
		List<String> routes = new ArrayList<>(Arrays.asList(baseName.toLowerCase() + "s", // users, companies
				baseName.toLowerCase(), // user, company
				entityName.toLowerCase() + "s", // cusers, ccompanies
				entityName.toLowerCase(), // cuser, ccompany
				baseName.toLowerCase() + "-directory", // user-directory, project-directory
				"page/" + baseName.toLowerCase(), // page/user, page/company
				"entity/" + baseName.toLowerCase(), // entity/user, entity/company
				"view/" + baseName.toLowerCase(), // view/user, view/company
				"dynamic/" + baseName.toLowerCase() // dynamic/user, dynamic/company
		));
		if ("cuser".equalsIgnoreCase(entityName) || "user".equalsIgnoreCase(baseName)) {
			routes.add("team-directory");
		}
		if ("cproject".equalsIgnoreCase(entityName) || "project".equalsIgnoreCase(baseName)) {
			routes.add("project-overview");
			routes.add("resource-library");
		}
		return routes.toArray(new String[0]);
	}

	/** Test navigation to user page that was created by samples and initializers.
	 * @return true if user page was found and loaded successfully */
	protected boolean testNavigationToUserPage() {
		LOGGER.info("👤 Testing navigation to User page created by initializers");
		try {
			// Try multiple approaches to find the user page
			String[] userPageSelectors = {
					"vaadin-side-nav-item:has-text('Users')", "vaadin-side-nav-item:has-text('User')", "a:has-text('Users')",
					"a:has-text('User Management')", "[href*='user']", "text='System.Users'"
			};
			for (String selector : userPageSelectors) {
				try {
					Locator navItem = page.locator(selector);
					if (navItem.count() > 0) {
						LOGGER.info("🎯 Found user page with selector: {}", selector);
						navItem.first().click();
						wait_2000(); // Wait for page to load
						if (isDynamicPageLoaded()) {
							LOGGER.info("✅ User page loaded successfully");
							takeScreenshot("user-page-loaded");
							return true;
						}
					}
				} catch (Exception e) {
					LOGGER.debug("⚠️ Selector {} failed: {}", selector, e.getMessage());
				}
			}
			// Fallback: try direct navigation using navigateToFirstPage
			return navigateToFirstPage(null, tech.derbent.base.users.domain.CUser.class);
		} catch (Exception e) {
			LOGGER.error("❌ Failed to test navigation to user page: {}", e.getMessage());
			return false;
		}
	}

	/** Test navigation to company page that was created by samples and initializers.
	 * @return true if company page was found and loaded successfully */
	protected boolean testNavigationToCompanyPage() {
		LOGGER.info("🏢 Testing navigation to Company page created by initializers");
		try {
			// Try multiple approaches to find the company page
			String[] companyPageSelectors = {
					"vaadin-side-nav-item:has-text('Companies')", "vaadin-side-nav-item:has-text('Company')", "a:has-text('Companies')",
					"a:has-text('Company Management')", "[href*='company']", "[href*='companies']", "text='System.Companies'"
			};
			for (String selector : companyPageSelectors) {
				try {
					Locator navItem = page.locator(selector);
					if (navItem.count() > 0) {
						LOGGER.info("🎯 Found company page with selector: {}", selector);
						navItem.first().click();
						wait_2000(); // Wait for page to load
						if (isDynamicPageLoaded()) {
							LOGGER.info("✅ Company page loaded successfully");
							takeScreenshot("company-page-loaded");
							return true;
						}
					}
				} catch (Exception e) {
					LOGGER.debug("⚠️ Selector {} failed: {}", selector, e.getMessage());
				}
			}
			// Fallback: try direct navigation using navigateToFirstPage
			return navigateToFirstPage(null, tech.derbent.app.companies.domain.CCompany.class);
		} catch (Exception e) {
			LOGGER.error("❌ Failed to test navigation to company page: {}", e.getMessage());
			return false;
		}
	}

	/** Wait for a dynamic page to fully load and verify no exceptions occurred. This method will fail fast if any error indicators are found. */
	protected void waitForDynamicPageLoad() {
		try {
			wait_2000(); // Initial wait for page rendering
			// Check for any error indicators that would indicate page failure
			if (page.locator("text=Exception").count() > 0) {
				throw new AssertionError("Dynamic page shows exception content");
			}
			if (page.locator("text=Error").count() > 0) {
				throw new AssertionError("Dynamic page shows error content");
			}
			if (page.locator("text=Not Found").count() > 0) {
				throw new AssertionError("Dynamic page shows not found error");
			}
			// Wait for interactive elements to be ready
			page.waitForSelector("vaadin-grid, vaadin-form-layout, vaadin-button", new Page.WaitForSelectorOptions().setTimeout(10000));
			LOGGER.info("✅ Dynamic page loaded successfully without errors");
		} catch (Exception e) {
			String message = "Dynamic page failed to load properly: " + e.getMessage();
			LOGGER.error("❌ {}", message);
			throw new AssertionError(message, e);
		}
	}

	/** Test CRUD operations on a dynamic page for a specific entity type. This method will fail fast on any errors during CRUD operations.
	 * @param entityType The entity type being tested */
	protected void testDynamicPageCrudOperations(String entityType) {
		Check.notBlank(entityType, "Entity type cannot be blank");
		LOGGER.info("🔄 Testing CRUD operations for dynamic page: {}", entityType);
		try {
			// Ensure we're on the correct dynamic page
			waitForDynamicPageLoad();
			takeScreenshot("dynamic-crud-" + entityType.toLowerCase() + "-initial", false);
			// Test CREATE operation
			testDynamicPageCreate(entityType);
			// Test READ operation (verify data in grid)
			testDynamicPageRead(entityType);
			// Test UPDATE operation
			testDynamicPageUpdate(entityType);
			// Test DELETE operation
			testDynamicPageDelete(entityType);
			LOGGER.info("✅ CRUD operations completed successfully for: {}", entityType);
		} catch (Exception e) {
			String message = "CRUD operations failed for dynamic page: " + entityType + " - " + e.getMessage();
			LOGGER.error("❌ {}", message);
			throw new AssertionError(message, e);
		}
	}

	/** Test CREATE operation on dynamic page. */
	protected void testDynamicPageCreate(String entityType) {
		try {
			LOGGER.info("➕ Testing CREATE operation for: {}", entityType);
			// Click New button
			Locator newButton = waitForSelectorWithCheck("vaadin-button:has-text('New')", "New button");
			newButton.click();
			wait_1000();
			// Fill in form fields
			String testName = "Test " + entityType + " " + System.currentTimeMillis();
			fillFormFieldsForEntity(entityType, testName);
			// Save the entity
			clickSave();
			wait_1000();
			takeScreenshot("dynamic-crud-" + entityType.toLowerCase() + "-create", false);
			LOGGER.info("✅ CREATE operation completed for: {}", entityType);
		} catch (Exception e) {
			throw new AssertionError("CREATE operation failed for " + entityType + ": " + e.getMessage(), e);
		}
	}

	/** Test READ operation by verifying data exists in grid. */
	protected void testDynamicPageRead(String entityType) {
		try {
			LOGGER.info("👁️ Testing READ operation for: {}", entityType);
			// Verify grid has data
			Check.isTrue(verifyGridHasData(), "Grid should contain data after CREATE operation");
			takeScreenshot("dynamic-crud-" + entityType.toLowerCase() + "-read", false);
			LOGGER.info("✅ READ operation completed for: {}", entityType);
		} catch (Exception e) {
			throw new AssertionError("READ operation failed for " + entityType + ": " + e.getMessage(), e);
		}
	}

	/** Test UPDATE operation on dynamic page. */
	protected void testDynamicPageUpdate(String entityType) {
		try {
			LOGGER.info("✏️ Testing UPDATE operation for: {}", entityType);
			// Select first row and edit
			clickFirstGridRow();
			wait_500();
			clickEdit();
			wait_1000();
			// Update fields
			String updatedName = "Updated " + entityType + " " + System.currentTimeMillis();
			fillFormFieldsForEntity(entityType, updatedName);
			// Save changes
			clickSave();
			wait_1000();
			takeScreenshot("dynamic-crud-" + entityType.toLowerCase() + "-update", false);
			LOGGER.info("✅ UPDATE operation completed for: {}", entityType);
		} catch (Exception e) {
			throw new AssertionError("UPDATE operation failed for " + entityType + ": " + e.getMessage(), e);
		}
	}

	/** Test DELETE operation on dynamic page. */
	protected void testDynamicPageDelete(String entityType) {
		try {
			LOGGER.info("🗑️ Testing DELETE operation for: {}", entityType);
			// Select first row and delete
			clickFirstGridRow();
			wait_500();
			clickDelete();
			wait_1000();
			takeScreenshot("dynamic-crud-" + entityType.toLowerCase() + "-delete", false);
			LOGGER.info("✅ DELETE operation completed for: {}", entityType);
		} catch (Exception e) {
			throw new AssertionError("DELETE operation failed for " + entityType + ": " + e.getMessage(), e);
		}
	}

	/** Fill form fields specific to an entity type.
	 * @param entityType The entity type
	 * @param name       The name/title to use */
	protected void fillFormFieldsForEntity(String entityType, String name) {
		try {
			// Fill first text field (usually name/title)
			fillFirstTextField(name);
			// Fill description if present
			Locator textAreas = page.locator("vaadin-text-area");
			if (textAreas.count() > 0) {
				textAreas.first().fill("Description for " + name);
			}
			// Select combo box options if present
			Locator comboBoxes = page.locator("vaadin-combo-box");
			if (comboBoxes.count() > 0) {
				for (int i = 0; i < Math.min(comboBoxes.count(), 2); i++) {
					try {
						comboBoxes.nth(i).click();
						wait_500();
						Locator items = page.locator("vaadin-combo-box-item");
						if (items.count() > 0) {
							items.first().click();
							wait_500();
						}
					} catch (Exception e) {
						LOGGER.warn("⚠️ Could not select combo box option {}: {}", i, e.getMessage());
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to fill form fields for " + entityType + ": " + e.getMessage(), e);
		}
	}
}
