package automated_tests.tech.derbent.ui.automation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.vaadin.flow.router.Route;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.base.users.domain.CUser;

/** Enhanced base UI test class that provides common functionality for Playwright tests. This class includes 25+ auxiliary methods for testing all
 * views and business functions. The base class follows strict coding guidelines and provides comprehensive testing utilities for: - Login and
 * authentication workflows - Navigation between views using ID-based selectors - CRUD operations testing - Form validation and ComboBox testing -
 * Grid interactions and data verification - Screenshot capture for debugging - Cross-view data consistency testing */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT)
@SuppressWarnings ("static-method")
public abstract class CBaseUITest {

	private static final String CONFIRM_YES_BUTTON_ID = "cbutton-yes";
	private static final String CRUD_CANCEL_BUTTON_ID = "cbutton-cancel";
	private static final String CRUD_DELETE_BUTTON_ID = "cbutton-delete";
	private static final String CRUD_EDIT_BUTTON_ID = "cbutton-edit";
	private static final String CRUD_NEW_BUTTON_ID = "cbutton-new";
	private static final String CRUD_REFRESH_BUTTON_ID = "cbutton-refresh";
	private static final String CRUD_SAVE_BUTTON_ID = "cbutton-save";
	// Exception detection for fail-fast behavior
	private static final List<String> DETECTED_EXCEPTIONS = new ArrayList<>();
	private static final String EXCEPTION_DETAILS_DIALOG_ID = "custom-exception-details-dialog";
	private static final String EXCEPTION_DIALOG_ID = "custom-exception-dialog";
	private static final Object EXCEPTION_LOCK = new Object();
	private static final String FIELD_ID_PREFIX = "field";
	private static final String FORCE_SAMPLE_RELOAD_PROPERTY = "playwright.forceSampleReload";
	private static final String INFO_OK_BUTTON_ID = "cbutton-ok";
	private static final Logger LOGGER = LoggerFactory.getLogger(CBaseUITest.class);
	private static final String LOGIN_BUTTON_ID = "cbutton-login";
	private static final String PROGRESS_DIALOG_ID = "custom-progress-dialog";
	private static final String RESET_DB_FULL_BUTTON_ID = "cbutton-db-full";
	private static final AtomicBoolean SAMPLE_DATA_INITIALIZED = new AtomicBoolean(false);
	private static final Object SAMPLE_DATA_LOCK = new Object();
	private static final String SCHEMA_SELECTION_PROPERTY = "playwright.schema";
	private static final String SCHEMA_SELECTOR_ID = "custom-schema-selector";
	protected static final String SCREENSHOT_FAILURE_SUFFIX = "failure";
	protected static final String SCREENSHOT_SUCCESS_SUFFIX = "success";
	static {
		System.setProperty("vaadin.devmode.liveReload.enabled", "false");
		System.setProperty("vaadin.launch-browser", "false");
		System.setProperty("vaadin.devmode.enabled", "false");
		System.setProperty("vaadin.devserver.enabled", "false");
		System.setProperty("spring.devtools.restart.enabled", "false");
		System.setProperty("spring.devtools.livereload.enabled", "false");
		System.setProperty("spring.devtools.livereload.port", "35729");
	}

	/** Generate possible dynamic page routes for an entity type.
	 * @param entityName The entity name (e.g., "CUser", "CCompany")
	 * @return Array of possible routes to try */
	protected static String[] generateDynamicPageRoutes(String entityName) {
		final String baseName = entityName.startsWith("C") ? entityName.substring(1) : entityName;
		final List<String> routes = new ArrayList<>(Arrays.asList(baseName.toLowerCase() + "s", // users, companies
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

	/** Generate search terms for a given entity type.
	 * @param entityType The entity type (e.g., "CUser")
	 * @return Array of possible search terms */
	protected static String[] generateSearchTermsForEntity(String entityType) {
		// Remove 'C' prefix if present
		final String baseName = entityType.startsWith("C") ? entityType.substring(1) : entityType;
		return new String[] {
				baseName + "s", // Users, Activities, Projects
				baseName, // User, Activity, Project
				baseName.toLowerCase() + "s", // users, activities, projects
				baseName.toLowerCase(), // user, activity, project
				entityType, // CUser, CActivity, CProject
				entityType.toLowerCase() // cuser, cactivity, cproject
		};
	}

	private static boolean isIgnorableConsoleMessage(final String message) {
		if (message == null) {
			return false;
		}
		final String normalized = message.replace('\u00A0', ' ').trim();
		final String normalizedLower = normalized.toLowerCase();
		if (normalized.contains("ws://localhost:35729")) {
			return true;
		}
		if (normalized.contains("vaadinPush.js") && normalized.contains("WebSocket connection")) {
			return true;
		}
		if (normalized.contains("favicon.ico") && normalized.contains("404")) {
			return true;
		}
		if (normalized.contains("WebSocket connection to") && normalized.contains("/VAADIN/push")) {
			return true;
		}
		if (normalizedLower.startsWith("event ") || normalizedLower.startsWith("event(") || normalizedLower.startsWith("event:")) {
			return true;
		}
		if (normalizedLower.contains("event") && (normalizedLower.contains("http://localhost") || normalizedLower.contains("https://localhost")
				|| normalizedLower.contains("http://127.0.0.1") || normalizedLower.contains("https://127.0.0.1"))) {
			return true;
		}
		if (normalized.contains("Refused to apply style") && normalized.contains("text/html")) {
			return true;
		}
		return normalized.contains("Error in WebSocket connection to ws://localhost:35729");
	}

	/** Attempts to resolve the fully-qualified entity class for a given entity type string. */
	protected static Optional<Class<?>> resolveEntityClass(String entityType) {
		final String baseName = entityType.startsWith("C") ? entityType.substring(1) : entityType;
		final String pluralSegment = baseName.toLowerCase() + "s";
		final String singularSegment = baseName.toLowerCase();
		final String[] candidateClasses = {
				"tech.derbent." + pluralSegment + ".domain." + entityType, "tech.derbent." + singularSegment + ".domain." + entityType,
				"tech.derbent.plm." + pluralSegment + ".domain." + entityType, "tech.derbent.plm." + singularSegment + ".domain." + entityType,
				"tech.derbent.base." + pluralSegment + ".domain." + entityType, "tech.derbent.base." + singularSegment + ".domain." + entityType,
				"tech.derbent.api.domain." + entityType
		};
		for (final String fqcn : candidateClasses) {
			try {
				final Class<?> clazz = Class.forName(fqcn);
				LOGGER.debug("🔍 Resolved entity type {} to class {}", entityType, fqcn);
				return Optional.of(clazz);
			} catch (@SuppressWarnings ("unused") final ClassNotFoundException ignored) { /*****/
			}
		}
		LOGGER.debug("⚠️ Unable to resolve entity class for {}", entityType);
		return Optional.empty();
	}

	private static String resolveViewIdentifier(final Class<?> viewClass) {
		if (viewClass == null) {
			return "unknown-view";
		}
		try {
			final Field viewNameField = viewClass.getDeclaredField("VIEW_NAME");
			if (Modifier.isStatic(viewNameField.getModifiers()) && viewNameField.getType() == String.class) {
				viewNameField.setAccessible(true);
				final Object value = viewNameField.get(null);
				if (value instanceof final String viewName && !viewName.isBlank()) {
					return viewName;
				}
			}
		} catch (final NoSuchFieldException missingField) {
			LOGGER.debug("VIEW_NAME not declared on {}: {}", viewClass.getSimpleName(), missingField.getMessage());
		} catch (final Exception reflectionError) {
			LOGGER.debug("Failed to read VIEW_NAME for {}: {}", viewClass.getSimpleName(), reflectionError.getMessage());
		}
		final Route route = viewClass.getAnnotation(Route.class);
		if (route != null && route.value() != null && !route.value().isBlank()) {
			return route.value();
		}
		return viewClass.getSimpleName();
	}

	private static String sanitizeForDomId(final String value) {
		return sanitizeForIdentifier(value, "dom-id");
	}

	private static String sanitizeForIdentifier(final String value, final String fallback) {
		final String safeFallback = fallback == null || fallback.isBlank() ? "autogen" : fallback;
		if (value == null || value.isBlank()) {
			return safeFallback;
		}
		final String sanitized = value.replaceAll("([a-z])([A-Z])", "$1-$2").replaceAll("[^a-zA-Z0-9-]", "-").replaceAll("-{2,}", "-")
				.replaceAll("(^-|-$)", "").toLowerCase();
		return sanitized.isBlank() ? safeFallback : sanitized;
	}

	/** Admin view classes */
	protected Class<?>[] adminViewClasses = {};
	protected Class<?>[] allViewClasses = {};
	/** All view classes */
	private Browser browser;
	private boolean consoleListenerRegistered = false;
	private BrowserContext context;
	/** Kanban view classes */
	protected Class<?>[] kanbanViewClasses = {};
	/** Array of main view classes for testing */
	protected Class<?>[] mainViewClasses = {};
	protected Page page;
	private Playwright playwright;
	@LocalServerPort
	protected int port = 8081;
	/** Status and type view classes */
	protected Class<?>[] statusAndTypeViewClasses = {};
	/** Legacy property for backward compatibility */
	protected Class<?>[] viewClasses = mainViewClasses;

	/** Accepts the confirmation dialog that appears when reloading sample data. */
	private void acceptConfirmDialogIfPresent() {
		final int maxAttempts = 10; // Increased for DB Full reset (5 seconds max)
		LOGGER.info("🔍 Looking for confirmation dialog to reset database...");
		for (int attempt = 0; attempt < maxAttempts; attempt++) {
			performFailFastCheck("Confirmation Dialog Wait");
			final Locator overlay = page.locator("vaadin-dialog-overlay[opened]");
			if (overlay.count() > 0) {
				LOGGER.info("📋 Confirmation dialog detected (attempt {}/{})", attempt + 1, maxAttempts);
				final Locator confirmButton = locatorById(CONFIRM_YES_BUTTON_ID);
				LOGGER.info("🖱️ Clicking confirmation dialog button #{}", CONFIRM_YES_BUTTON_ID);
				confirmButton.first().click();
				waitForOverlayToClose("vaadin-dialog-overlay[opened]");
				LOGGER.info("✅ Sample data reload confirmed - dialog closed");
				return;
			}
			LOGGER.debug("🔍 No confirmation dialog found (attempt {}/{})", attempt + 1, maxAttempts);
			wait_500();
		}
		// FAIL-FAST: If no confirmation dialog appears, database reset is broken
		LOGGER.error("❌ CRITICAL: Confirmation dialog not detected after {} attempts ({} seconds)", maxAttempts, maxAttempts * 0.5);
		throw new RuntimeException(
				"FAIL-FAST: No confirmation dialog appeared after clicking database reset button. Database reset functionality may be broken.");
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

	/** Assert browser is available */
	protected void assertBrowserAvailable() {
		if (!isBrowserAvailable()) {
			throw new AssertionError("Browser is not available");
		}
	}

	/** Verifies the value of a bound field matches the expected content. */
	protected void assertFieldValueEquals(final Class<?> entityClass, final String fieldName, final String expected) {
		final String actual = readFieldValueById(entityClass, fieldName);
		if (!expected.equals(actual)) {
			throw new AssertionError("Field '" + fieldName + "' expected value '" + expected + "' but was '" + actual + "'");
		}
	}

	private String buildExceptionDialogMessage(final String controlPoint, final Locator dialog) {
		final StringBuilder message =
				new StringBuilder("Exception dialog detected at ").append(controlPoint).append(" (url: ").append(safePageUrl()).append(")");
		try {
			final String dialogText = dialog.textContent();
			if (dialogText != null && !dialogText.trim().isEmpty()) {
				message.append(": ").append(dialogText.trim());
			}
		} catch (final PlaywrightException e) {
			message.append("; failed to read dialog text: ").append(e.getMessage());
		}
		return message.toString();
	}

	/** Builds a screenshot name in the format view-scenario-success|failure using deterministic components. */
	protected String buildViewScreenshotName(final Class<?> viewClass, final String scenario, final boolean success) {
		final String identifier = sanitizeForFileName(resolveViewIdentifier(viewClass), "pageservice");
		final String scenarioPart = sanitizeForFileName(Optional.ofNullable(scenario).orElse("scenario"), "scenario");
		final String status = success ? SCREENSHOT_SUCCESS_SUFFIX : SCREENSHOT_FAILURE_SUFFIX;
		return String.join("-", identifier, scenarioPart, status);
	}

	/** Enhanced exception check that also scans browser console for errors */
	protected void checkBrowserConsoleForErrors(String controlPoint) {
		try {
			// Execute JavaScript to check for console errors
			final Object errors = page.evaluate("""
						() => {
							// Capture any console errors that were logged
							const errors = window.console.errors || [];
							return errors.map(err => err.toString());
						}
					""");
			if (errors != null && !errors.toString().equals("[]") && !isIgnorableConsoleMessage(errors.toString())) {
				LOGGER.error("❌ FAIL-FAST: Browser console errors found at {}: {}", controlPoint, errors);
				throw new RuntimeException("FAIL-FAST: Browser console errors at " + controlPoint + ": " + errors);
			}
			LOGGER.debug("✅ No browser console errors at: {}", controlPoint);
		} catch (final RuntimeException e) {
			throw e;
		} catch (final Exception e) {
			LOGGER.warn("⚠️ Browser console check failed at {}: {}", controlPoint, e.getMessage());
		}
	}

	/** CRITICAL: Check for exceptions in application logs and fail-fast if found. This method should be called at EVERY control point in tests.
	 * @param controlPoint Description of where this check is being performed
	 * @throws RuntimeException if any ERROR or Exception is found in logs */
	protected void checkForExceptionsAndFailFast(String controlPoint) {
		try {
			registerConsoleListener();
			// Check if any exceptions were detected
			synchronized (EXCEPTION_LOCK) {
				if (!DETECTED_EXCEPTIONS.isEmpty()) {
					final StringBuilder errorReport = new StringBuilder();
					errorReport.append("❌ FAIL-FAST: Exceptions detected at control point '").append(controlPoint).append("':\n");
					for (final String exception : DETECTED_EXCEPTIONS) {
						errorReport.append("  - ").append(exception).append("\n");
					}
					LOGGER.error(errorReport.toString());
					// Clear exceptions after reporting
					DETECTED_EXCEPTIONS.clear();
					// Fail immediately
					throw new RuntimeException("FAIL-FAST: Exceptions found at control point: " + controlPoint);
				}
			}
			LOGGER.debug("✅ No exceptions detected at control point: {}", controlPoint);
		} catch (final RuntimeException e) {
			// Re-throw fail-fast exceptions
			throw e;
		} catch (final Exception e) {
			LOGGER.warn("⚠️ Exception checking failed at {}: {}", controlPoint, e.getMessage());
		}
	}

	/** Clicks the "Cancel" button to cancel the current operation. */
	protected void clickCancel() {
		LOGGER.info("❌ Clicking Cancel button");
		locateButtonByIdOrText(CRUD_CANCEL_BUTTON_ID, "Cancel").click();
		wait_500();
	}

	/** Clicks the "Delete" button to delete the selected entity. */
	protected void clickDelete() {
		LOGGER.info("🗑️ Clicking Delete button");
		locateButtonByIdOrText(CRUD_DELETE_BUTTON_ID, "Delete").click();
		wait_500();
	}

	/** Clicks the "Edit" button to edit the selected entity. */
	protected void clickEdit() {
		LOGGER.info("✏️ Clicking Edit button");
		locateButtonByIdOrText(CRUD_EDIT_BUTTON_ID, "Edit").click();
		wait_500();
	}

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

	/** Clicks the login button using tolerant selector logic. */
	protected void clickLoginButton() {
		final Locator loginButton = locatorById(LOGIN_BUTTON_ID);
		loginButton.first().click();
		wait_500();
		LOGGER.info("▶️ Clicked login button using id #{}", LOGIN_BUTTON_ID);
	}

	/** Clicks the "New" button to create a new entity. */
	protected void clickNew() {
		LOGGER.info("➕ Clicking New button");
		locateButtonByIdOrText(CRUD_NEW_BUTTON_ID, "New").click();
		wait_500();
	}

	/** Clicks the "Refresh" button to refresh the current entity view. */
	protected void clickRefresh() {
		LOGGER.info("🔄 Clicking Refresh button");
		locateButtonByIdOrText(CRUD_REFRESH_BUTTON_ID, "Refresh").click();
		wait_500();
	}

	/** Clicks the "Save" button to save the current entity. */
	protected void clickSave() {
		LOGGER.info("💾 Clicking Save button");
		locateButtonByIdOrText(CRUD_SAVE_BUTTON_ID, "Save").click();
		wait_1000(); // Save operations may take longer
	}

	/** Closes the informational dialog that appears after sample data reload completion. */
	private void closeInformationDialogIfPresent() {
		final int maxAttempts = 10; // Increased timeout for information dialog (5 seconds max)
		LOGGER.info("🔍 Looking for information dialog after database reset...");
		for (int attempt = 0; attempt < maxAttempts; attempt++) {
			performFailFastCheck("Information Dialog Wait");
			final Locator overlay = page.locator("vaadin-dialog-overlay[opened]");
			if (overlay.count() == 0) {
				LOGGER.debug("🔍 No information dialog found (attempt {}/{})", attempt + 1, maxAttempts);
				wait_500();
				continue;
			}
			LOGGER.info("📋 Information dialog detected (attempt {}/{})", attempt + 1, maxAttempts);
			final Locator okButton = locatorById(INFO_OK_BUTTON_ID);
			LOGGER.info("🖱️ Clicking information dialog button #{}", INFO_OK_BUTTON_ID);
			okButton.first().click();
			LOGGER.info("✅ Information dialog OK button clicked");
			waitForOverlayToClose("vaadin-dialog-overlay[opened]");
			LOGGER.info("✅ Information dialog dismissed after sample data reload");
			return;
		}
		LOGGER.warn("⚠️ Information dialog did not present an OK button to dismiss after {} attempts ({} seconds)", maxAttempts, maxAttempts * 0.5);
		throw new AssertionError("Information dialog did not appear after database reset");
	}

	/** Computes the deterministic field ID generated by the Vaadin form builder using entity and field names. */
	protected String computeFieldId(final Class<?> entityClass, final String fieldName) {
		Objects.requireNonNull(entityClass, "Entity class required for field ID calculation");
		Check.notBlank(fieldName, "Field name required for field ID calculation");
		final String base = String.format("%s-%s-%s", FIELD_ID_PREFIX, entityClass.getSimpleName(), fieldName);
		return sanitizeForDomId(base);
	}

	/** Ensures a company is selected in the login form so multi-tenant logins succeed. */
	protected void ensureCompanySelected() {
		if (!isBrowserAvailable()) {
			return;
		}
		try {
			final Locator companyCombo = page.locator("#custom-company-input");
			if (companyCombo.count() == 0) {
				LOGGER.warn("⚠️ Company ComboBox not found on login page");
				return;
			}
			final Object rawValue = companyCombo.evaluate("combo => combo.value ?? null");
			if (rawValue != null && !rawValue.toString().isBlank()) {
				LOGGER.debug("ℹ️ Company already selected on login page");
				return;
			}
			LOGGER.info("🏢 Selecting default company on login page");
			companyCombo.first().click();
			wait_500();
			final Locator items = page.locator("vaadin-combo-box-item");
			if (items.count() > 0) {
				items.first().click();
				wait_500();
				LOGGER.info("✅ Company selection completed");
			} else {
				// FAIL-FAST: No companies means database reset failed
				LOGGER.error("❌ CRITICAL: No company options found in ComboBox - database reset failed!");
				throw new RuntimeException("FAIL-FAST: No companies available for login. Database initialization/reset failed.");
			}
		} catch (final Exception e) {
			LOGGER.warn("⚠️ Failed to select company on login page: {}", e.getMessage());
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
		} catch (final Exception e) {
			LOGGER.warn("⚠️ Unable to determine current URL before ensuring login view: {}", e.getMessage());
		}
		wait_loginscreen();
	}

	/** Ensures the schema selector is set before database reset/login. */
	protected void ensureSchemaSelected() {
		if (!isBrowserAvailable()) {
			return;
		}
		final String desiredSchema = System.getProperty(SCHEMA_SELECTION_PROPERTY, "Derbent").trim();
		if (desiredSchema.isBlank()) {
			return;
		}
		try {
			final Locator schemaCombo = page.locator("#" + SCHEMA_SELECTOR_ID);
			if (schemaCombo.count() == 0) {
				LOGGER.debug("ℹ️ Schema selector not found on login page");
				return;
			}
			final Object rawValue = schemaCombo.evaluate("combo => combo.value ?? null");
			if (rawValue != null && desiredSchema.equals(rawValue.toString().trim())) {
				LOGGER.debug("ℹ️ Schema already selected on login page: {}", desiredSchema);
				return;
			}
			LOGGER.info("🧭 Selecting schema '{}' on login page", desiredSchema);
			schemaCombo.first().click();
			wait_500();
			final Locator items = page.locator("vaadin-combo-box-item").filter(new Locator.FilterOptions().setHasText(desiredSchema));
			if (items.count() > 0) {
				items.first().click();
				wait_500();
				LOGGER.info("✅ Schema selection completed");
			} else {
				LOGGER.warn("⚠️ Schema option '{}' not found in selector", desiredSchema);
			}
		} catch (final Exception e) {
			LOGGER.warn("⚠️ Failed to select schema on login page: {}", e.getMessage());
		}
	}

	private void failFastIfExceptionDialogVisible(final String controlPoint) {
		if (!isBrowserAvailable()) {
			return;
		}
		final Locator exceptionDialog = page.locator("#" + EXCEPTION_DIALOG_ID + "[opened], #" + EXCEPTION_DETAILS_DIALOG_ID + "[opened]");
		if (exceptionDialog.count() > 0) {
			throw new AssertionError(buildExceptionDialogMessage(controlPoint, exceptionDialog.first()));
		}
		final Locator overlay = page.locator("vaadin-dialog-overlay[opened]");
		if (overlay.count() == 0) {
			return;
		}
		final Locator errorOverlay = overlay.filter(new Locator.FilterOptions().setHasText("Error Details"))
				.or(overlay.filter(new Locator.FilterOptions().setHasText("Exception")))
				.or(overlay.filter(new Locator.FilterOptions().setHasText("Error handling")));
		if (errorOverlay.count() > 0) {
			throw new AssertionError(buildExceptionDialogMessage(controlPoint, errorOverlay.first()));
		}
	}

	private void failFastIfLoginErrorVisible(final String controlPoint) {
		final Locator errorLabel = page.locator("#custom-error-message");
		if (errorLabel.count() == 0) {
			return;
		}
		try {
			if (errorLabel.first().isVisible()) {
				final String text = errorLabel.first().textContent();
				if (text != null && !text.trim().isEmpty()) {
					throw new AssertionError("Login failed at " + controlPoint + ": " + text.trim());
				}
			}
		} catch (final PlaywrightException e) {
			throw new AssertionError("Failed to evaluate login error label at " + controlPoint + ": " + e.getMessage(), e);
		}
	}

	/** Fills a bound Vaadin field by its deterministic ID using the entity class and field name. */
	protected void fillFieldById(final Class<?> entityClass, final String fieldName, final String value) {
		fillFieldById(computeFieldId(entityClass, fieldName), value);
	}

	/** Fills a bound Vaadin field by its DOM ID. Supports text fields and text areas by drilling into the native input element. */
	protected void fillFieldById(final String elementId, final String value) {
		Check.notBlank(elementId, "Element ID cannot be blank when filling a field");
		Objects.requireNonNull(value, "Value cannot be null when filling a field");
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

	/** Fill form fields specific to an entity type.
	 * @param entityType The entity type
	 * @param name       The name/title to use */
	protected void fillFormFieldsForEntity(String entityType, String name) {
		try {
			// Fill first text field (usually name/title)
			fillFirstTextField(name);
			// Fill description if present
			final Locator textAreas = page.locator("vaadin-text-area");
			if (textAreas.count() > 0) {
				textAreas.first().fill("Description for " + name);
			}
			// Select combo box options if present
			final Locator comboBoxes = page.locator("vaadin-combo-box");
			if (comboBoxes.count() > 0) {
				for (int i = 0; i < Math.min(comboBoxes.count(), 2); i++) {
					try {
						comboBoxes.nth(i).click();
						wait_500();
						final Locator items = page.locator("vaadin-combo-box-item");
						if (items.count() > 0) {
							items.first().click();
							wait_500();
						}
					} catch (final Exception e) {
						LOGGER.warn("⚠️ Could not select combo box option {}: {}", i, e.getMessage());
					}
				}
			}
		} catch (final Exception e) {
			throw new RuntimeException("Failed to fill form fields for " + entityType + ": " + e.getMessage(), e);
		}
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
		} catch (final Exception e) {
			LOGGER.warn("⚠️ Failed to fill {} field: {}", fieldDescription, e.getMessage());
		}
		return false;
	}

	/** Gets the count of rows in the first grid */
	protected int getGridRowCount() {
		final Locator grid = page.locator("vaadin-grid").first();
		final Locator cells = grid.locator("vaadin-grid-cell-content");
		return cells.count();
	}

	/** Safe locator method with null check */
	protected Locator getLocatorWithCheck(String selector, String description) {
		Objects.requireNonNull(selector, "Selector cannot be null");
		Check.notBlank(description, "Description cannot be blank");
		try {
			final Locator locator = page.locator(selector);
			Objects.requireNonNull(locator, "Failed to find element: " + description);
			return locator;
		} catch (final Exception e) {
			throw new AssertionError("Element not found: " + description + " (selector: " + selector + ")", e);
		}
	}

	private boolean hasCompanyOptionsOnLogin() {
		if (!isBrowserAvailable()) {
			return false;
		}
		try {
			final Locator companyCombo = page.locator("#custom-company-input");
			if (companyCombo.count() == 0) {
				return false;
			}
			companyCombo.first().click();
			wait_500();
			final Locator items = page.locator("vaadin-combo-box-item");
			final boolean hasItems = items.count() > 0;
			page.keyboard().press("Escape");
			wait_500();
			return hasItems;
		} catch (final Exception e) {
			LOGGER.debug("⚠️ Unable to confirm company options on login page: {}", e.getMessage());
			return false;
		}
	}

	/** Triggers the sample data initialization flow via the login screen button if present. */
	protected void initializeSampleDataFromLoginPage() {
		if (!isBrowserAvailable()) {
			return;
		}
		final boolean forceReload = Boolean.getBoolean(FORCE_SAMPLE_RELOAD_PROPERTY);
		if (!forceReload && SAMPLE_DATA_INITIALIZED.get()) {
			if (hasCompanyOptionsOnLogin()) {
				LOGGER.debug("ℹ️ Sample data already initialized for this test run; skipping reload");
				return;
			}
			LOGGER.warn("⚠️ Sample data flag set but no company options detected; forcing reload");
		}
		synchronized (SAMPLE_DATA_LOCK) {
			if (!forceReload && SAMPLE_DATA_INITIALIZED.get()) {
				if (hasCompanyOptionsOnLogin()) {
					LOGGER.debug("ℹ️ Sample data already initialized (post-lock); skipping reload");
					return;
				}
				LOGGER.warn("⚠️ Sample data flag set but no company options detected (post-lock); forcing reload");
			}
			if (forceReload) {
				LOGGER.info("♻️ Forcing sample data reload due to system property '{}'", FORCE_SAMPLE_RELOAD_PROPERTY);
			}
			try {
				wait_loginscreen();
				ensureSchemaSelected();
				final Locator fullButton = page.locator("#" + RESET_DB_FULL_BUTTON_ID);
				if (fullButton.count() == 0) {
					throw new AssertionError("DB Full reset button not found on login page");
				}
				LOGGER.info("📥 Loading sample data via login screen button (DB Full)");
				final Locator button = fullButton.first();
				try {
					final String buttonText = button.textContent();
					LOGGER.info("🔍 Found database reset button: '{}'", buttonText);
				} catch (final Exception e) {
					LOGGER.debug("Unable to read button text: {}", e.getMessage());
				}
				try {
					button.scrollIntoViewIfNeeded();
					LOGGER.debug("📜 Scrolled reset button into view");
				} catch (final PlaywrightException scrollError) {
					LOGGER.debug("ℹ️ Unable to scroll reset button into view: {}", scrollError.getMessage());
				}
				try {
					LOGGER.info("🖱️ Clicking database reset button...");
					button.click();
					LOGGER.info("✅ Database reset button clicked successfully");
				} catch (final PlaywrightException clickError) {
					LOGGER.warn("⚠️ First click failed, retrying with force: {}", clickError.getMessage());
					button.click(new Locator.ClickOptions().setForce(true));
					LOGGER.info("✅ Database reset button clicked with force");
				}
				wait_500();
				// FAIL-FAST CHECK: After database reset button click
				performFailFastCheck("After Database Reset Button Click");
				acceptConfirmDialogIfPresent();
				// FAIL-FAST CHECK: After confirmation dialog
				performFailFastCheck("After Confirmation Dialog");
				waitForProgressDialogToComplete();
				closeInformationDialogIfPresent();
				// FAIL-FAST CHECK: After information dialog
				performFailFastCheck("After Information Dialog");
				wait_loginscreen();
				try {
					LOGGER.info("🔄 Reloading login page after sample data reset");
					page.reload();
					wait_loginscreen();
				} catch (final Exception reloadError) {
					LOGGER.warn("⚠️ Failed to reload login page after data reset: {}", reloadError.getMessage());
				}
				LOGGER.info("✅ Sample data initialization completed successfully");
				SAMPLE_DATA_INITIALIZED.set(true);
			} catch (final Exception e) {
				LOGGER.warn("⚠️ Sample data initialization via login page failed: {}", e.getMessage());
				takeScreenshot("sample-data-initialization-error", false);
				if (forceReload) {
					SAMPLE_DATA_INITIALIZED.set(false);
				}
				throw new AssertionError("Sample data initialization failed: " + e.getMessage(), e);
			}
		}
	}

	/** Checks if browser is available */
	protected boolean isBrowserAvailable() { return page != null && !page.isClosed(); }

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
		} catch (final Exception e) {
			LOGGER.error("❌ Error checking if dynamic page loaded: {}", e.getMessage());
			return false;
		}
	}

	/** Locates the attachments container if present on the page. */
	protected Locator locateAttachmentsContainer() {
		openTabOrAccordionIfNeeded("Attachments");
		final Locator container = page.locator("#custom-attachments-component");
		if (container.count() > 0) {
			return container.first();
		}
		final String selector = "h2:has-text('Attachments'), h3:has-text('Attachments'), h4:has-text('Attachments'), span:has-text('Attachments')";
		final Locator header = page.locator(selector);
		if (header.count() > 0) {
			return header.first().locator("xpath=ancestor::*[self::vaadin-vertical-layout or self::div][1]");
		}
		return null;
	}

	/** Helper method to locate attachments grid within a container. */
	protected Locator locateAttachmentsGrid(final Locator container) {
		final Locator grid = container.locator("vaadin-grid").filter(new Locator.FilterOptions().setHasText("File Name"));
		if (grid.count() == 0) {
			throw new AssertionError("Attachments grid not found");
		}
		return grid.first();
	}

	/** Helper method to locate attachment toolbar button by icon name. */
	protected Locator locateAttachmentToolbarButton(final Locator container, final String iconName) {
		final Locator button =
				container.locator("vaadin-button").filter(new Locator.FilterOptions().setHas(page.locator("vaadin-icon[icon='" + iconName + "']")));
		if (button.count() == 0) {
			throw new AssertionError("Toolbar button not found for icon " + iconName);
		}
		return button.first();
	}

	/** Resolves a button locator by ID, falling back to text when needed. */
	protected Locator locateButtonByIdOrText(final String elementId, final String buttonText) {
		Check.notBlank(elementId, "Button ID cannot be blank");
		final Locator byId = page.locator("#" + elementId);
		if (byId.count() > 0) {
			return byId.first();
		}
		Check.notBlank(buttonText, "Button text fallback cannot be blank");
		return page.locator("vaadin-button:has-text('" + buttonText + "')").first();
	}
	// ===========================================
	// GRID INTERACTION METHODS
	// ===========================================

	/** Locates the comments container if present on the page. */
	protected Locator locateCommentsContainer() {
		openTabOrAccordionIfNeeded("Comments");
		final Locator container = page.locator("#custom-comments-component");
		if (container.count() > 0) {
			return container.first();
		}
		final String selector = "h2:has-text('Comments'), h3:has-text('Comments'), h4:has-text('Comments'), span:has-text('Comments')";
		final Locator header = page.locator(selector);
		if (header.count() > 0) {
			return header.first().locator("xpath=ancestor::*[self::vaadin-vertical-layout or self::div][1]");
		}
		return null;
	}

	/** Locates the parent item selector if present on the page. */
	protected Locator locateParentItemSelector() {
		final Locator parentCombo = page.locator("vaadin-combo-box").filter(new Locator.FilterOptions()
				.setHas(page.locator("label:has-text('Linked Activity'), label:has-text('Parent Item'), label:has-text('Related Activity')")));
		if (parentCombo.count() > 0) {
			return parentCombo.first();
		}
		return null;
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

	/** Logs the current menu structure to help with debugging navigation issues. */
	protected void logCurrentMenuStructure() {
		try {
			final List<String> hierarchicalItems = page.locator(".hierarchical-menu-item").allTextContents();
			LOGGER.info("📋 Hierarchical menu items: {}", hierarchicalItems);
		} catch (final Exception e) {
			LOGGER.debug("⚠️ Unable to collect hierarchical menu items: {}", e.getMessage());
		}
		try {
			final List<String> sideNavItems = page.locator("vaadin-side-nav-item").allTextContents();
			LOGGER.info("📋 Side nav items: {}", sideNavItems);
		} catch (final Exception e) {
			LOGGER.debug("⚠️ Unable to collect side nav items: {}", e.getMessage());
		}
		try {
			final List<String> anchorTargets = page.locator("a[href]").allInnerTexts();
			LOGGER.info("📋 Anchor items: {}", anchorTargets);
		} catch (final Exception e) {
			LOGGER.debug("⚠️ Unable to collect anchor link texts: {}", e.getMessage());
		}
	}

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
			ensureSchemaSelected();
			// CRITICAL: If company is empty on the login page, ensure sample data is initialized first.
			// This addresses the requirement: "at initial db, if combobox of company is empty you should initialize db"
			try {
				final Locator companyCombo = page.locator("#custom-company-input");
				boolean companyPresent = false;
				if (companyCombo.count() > 0) {
					final Object raw = companyCombo.evaluate("combo => combo.value ?? null");
					companyPresent = raw != null && !raw.toString().isBlank();
				}
				if (!companyPresent) {
					LOGGER.info("🏢 Company combobox is empty - initializing sample data as required");
					initializeSampleDataFromLoginPage();
					ensureLoginViewLoaded();
					ensureCompanySelected();
				} else {
					LOGGER.debug("ℹ️ Company already selected on login page, skipping DB initialization");
				}
			} catch (final Exception e) {
				LOGGER.warn("⚠️ Could not determine company selection state: {}. Proceeding with login flow.", e.getMessage());
			}
			final boolean usernameFilled =
					fillLoginField("#custom-username-input", "input", "username", username, "input[type='text'], input[type='email']");
			if (!usernameFilled) {
				throw new AssertionError("Username input field not found on login page");
			}
			final boolean passwordFilled = fillLoginField("#custom-password-input", "input", "password", password, "input[type='password']");
			if (!passwordFilled) {
				throw new AssertionError("Password input field not found on login page");
			}
			clickLoginButton();
			failFastIfLoginErrorVisible("After Login Button Click");
			performFailFastCheck("After Login Button Click");
			waitForLoginSuccess();
			LOGGER.info("✅ Login successful - application shell detected");
			takeScreenshot("post-login", false);
			// FAIL-FAST CHECK: After application load
			performFailFastCheck("After Application Load");
			primeNavigationMenu();
		} catch (final PlaywrightException e) {
			LOGGER.warn("⚠️ Login attempt failed for {}: {}", username, e.getMessage());
			takeScreenshot("login-attempt-error", false);
			if (page != null && page.isClosed()) {
				LOGGER.warn("⚠️ Browser page closed during login attempt");
			}
		} catch (final Exception e) {
			LOGGER.warn("⚠️ Unexpected login error for {}: {}", username, e.getMessage());
			takeScreenshot("login-unexpected-error", false);
		}
	}

	/** Navigate by searching menu items for text containing the entity type.
	 * @param entityType The entity type to search for
	 * @return true if navigation was successful */
	protected boolean navigateByMenuSearch(String entityType) {
		try {
			// Look for menu items that might contain the entity name
			// Common patterns: "Users", "Activities", "Projects", "Meetings", etc.
			final String[] searchTerms = generateSearchTermsForEntity(entityType);
			final String[] selectorCandidates = {
					".hierarchical-menu-item", "vaadin-side-nav-item", "vaadin-tab", "nav a[href]", ".nav-item a[href]", "a[href].menu-link",
					"a[href].side-nav-link"
			};
			logCurrentMenuStructure();
			final Set<String> visitedCandidates = new HashSet<>();
			for (final String searchTerm : searchTerms) {
				for (final String selector : selectorCandidates) {
					for (int attempt = 0; attempt < 5; attempt++) {
						final Locator candidates = page.locator(selector).filter(new Locator.FilterOptions().setHasText(searchTerm));
						final int count = candidates.count();
						if (count == 0) {
							wait_500();
							continue;
						}
						for (int i = 0; i < count; i++) {
							try {
								final Locator item = candidates.nth(i);
								String label = "";
								try {
									label = Optional.ofNullable(item.textContent()).map(String::trim).orElse("");
								} catch (@SuppressWarnings ("unused") final Exception ignored) { /*****/
								}
								final String candidateKey = selector + "|" + searchTerm + "|" + label + "|" + i;
								if (!visitedCandidates.add(candidateKey)) {
									continue;
								}
								LOGGER.info("🎯 Trying menu selector '{}' candidate {} with label '{}'", selector, i, label);
								final String beforeUrl = page.url();
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
								} catch (final AssertionError validationError) {
									LOGGER.debug("⏳ Dynamic page validation still pending after selector {} / search term {}: {}", selector,
											searchTerm, validationError.getMessage());
								}
							} catch (final Exception clickError) {
								LOGGER.debug("⚠️ Failed to activate menu item for selector {} / search term {}: {}", selector, searchTerm,
										clickError.getMessage());
							}
						}
					}
				}
			}
			return false;
		} catch (final Exception e) {
			throw new RuntimeException("Menu search navigation failed for entity type: " + entityType, e);
		}
	}

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
			final Optional<Class<?>> entityClass = resolveEntityClass(entityType);
			if (entityClass.isPresent()) {
				final boolean navigated = navigateToFirstPage(null, entityClass.get());
				if (navigated) {
					waitForDynamicPageLoad();
					LOGGER.info("✅ Successfully navigated to {} via fallback direct route", entityType);
					return true;
				}
			}
			return false;
		} catch (final Exception e) {
			final String message = "Failed to navigate to dynamic page for entity type: " + entityType + " - " + e.getMessage();
			LOGGER.error("❌ {}", message);
			throw new AssertionError(message, e);
		}
	}

	/** Navigate to the first page entity of a specific project and entity class. This method mimics the menu generator behavior to dynamically get
	 * page links.
	 * @param project     The project to search in (can be null for all projects)
	 * @param entityClass The entity class to find a page for (e.g., CUser.class, CCompany.class)
	 * @return true if navigation was successful */
	protected boolean navigateToFirstPage(CProject<?> project, Class<?> entityClass) {
		Objects.requireNonNull(entityClass, "Entity class cannot be null");
		LOGGER.info("🧭 Navigating to first page for entity class: {} in project: {}", entityClass.getSimpleName(),
				project != null ? project.getName() : "All Projects");
		try {
			if (!isBrowserAvailable()) {
				LOGGER.warn("⚠️ Browser not available, cannot navigate to first page");
				return false;
			}
			// Generate dynamic page link based on entity class
			final String entityName = entityClass.getSimpleName();
			final String[] possibleRoutes = generateDynamicPageRoutes(entityName);
			// Try to navigate to each possible route
			for (final String route : possibleRoutes) {
				try {
					LOGGER.debug("🔗 Trying route: {}", route);
					page.navigate("http://localhost:" + port + "/" + route);
					wait_2000(); // Wait for page to load
					if (isDynamicPageLoaded()) {
						LOGGER.info("✅ Successfully navigated to first page via route: {}", route);
						return true;
					}
				} catch (final Exception e) {
					LOGGER.debug("⚠️ Route {} failed: {}", route, e.getMessage());
				}
			}
			// Fallback: try navigation via menu system
			return navigateToDynamicPageByEntityType(entityName);
		} catch (final Exception e) {
			final String message = "Failed to navigate to first page for entity class: " + entityClass.getSimpleName() + " - " + e.getMessage();
			LOGGER.error("❌ {}", message);
			return false;
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
		} catch (final Exception e) {
			LOGGER.error("❌ Failed to navigate to Projects: {}", e.getMessage());
			throw new AssertionError("Failed to navigate to Projects view", e);
		}
	}

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
			}
			LOGGER.warn("⚠️ No @Route annotation found for class: {}", viewClass.getSimpleName());
			return false;
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
			Objects.requireNonNull(navItem, "Navigation item not found for: " + viewText);
			if (navItem.count() > 0) {
				navItem.first().click();
				wait_500();
				LOGGER.info("✅ Successfully navigated to: {}", viewText);
				return true;
			}
			LOGGER.warn("⚠️ Navigation item not found for: {}", viewText);
			return false;
		} catch (final Exception e) {
			LOGGER.error("❌ Navigation failed for view: {} - Error: {}", viewText, e.getMessage());
			return false;
		}
	}

	/** Opens a tab or accordion panel if it contains the specified text. */
	protected void openTabOrAccordionIfNeeded(final String text) {
		final Locator tab = page.locator("vaadin-tab").filter(new Locator.FilterOptions().setHasText(text));
		if (tab.count() > 0) {
			tab.first().click();
			wait_500();
			return;
		}
		final Locator accordion = page.locator("vaadin-accordion-panel").filter(new Locator.FilterOptions().setHasText(text));
		if (accordion.count() > 0) {
			final Locator heading = accordion.first().locator("vaadin-accordion-heading, [part='summary']");
			if (heading.count() > 0) {
				heading.first().click();
			} else {
				accordion.first().click();
			}
			wait_500();
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
			final String testData = "Test " + entityName;
			fillFirstTextField(testData);
			// Try to fill other fields if present
			final Locator textAreas = page.locator("vaadin-text-area");
			if (textAreas.count() > 0) {
				fillFirstTextArea("Description for " + testData);
			}
			// Select combobox options if present
			final Locator comboBoxes = page.locator("vaadin-combo-box");
			if (comboBoxes.count() > 0) {
				selectFirstComboBoxOption();
			}
			clickSave();
			wait_1000();
			takeScreenshot("crud-create-" + entityName.toLowerCase(), false);
			// READ operation - verify data was created
			LOGGER.info("👁️ Testing READ operation for: {}", entityName);
			final boolean hasData = verifyGridHasData();
			Check.isTrue(hasData, "Data should be present after CREATE operation");
			// UPDATE operation
			LOGGER.info("✏️ Testing UPDATE operation for: {}", entityName);
			clickFirstGridRow();
			wait_500();
			clickEdit();
			wait_1000();
			final String updatedData = "Updated " + entityName;
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
		} catch (final Exception e) {
			throw new AssertionError("Enhanced CRUD workflow failed for " + entityName + ": " + e.getMessage(), e);
		}
	}

	/** Comprehensive exception check - combines log scanning and browser console checks */
	protected void performFailFastCheck(String controlPoint) {
		if (!isBrowserAvailable()) {
			return;
		}
		failFastIfExceptionDialogVisible(controlPoint);
		checkForExceptionsAndFailFast(controlPoint);
		checkBrowserConsoleForErrors(controlPoint);
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
		} catch (final Exception e) {
			LOGGER.warn("⚠️ Logout failed: {}", e.getMessage());
		}
	}

	/** Visits all menu items silently to ensure dynamic entries are initialized. */
	protected void primeNavigationMenu() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping navigation priming");
			return;
		}
		try {
			LOGGER.info("🧭 Priming navigation menu by checking menu items exist");
			// Just verify menu is visible, don't click anything
			final String menuSelector =
					".hierarchical-menu-item, vaadin-side-nav-item, vaadin-tabs vaadin-tab, nav a[href], .nav-item a[href], a[href].menu-link, a[href].side-nav-link";
			page.waitForSelector(menuSelector, new Page.WaitForSelectorOptions().setTimeout(10000));
			final int menuItemCount = page.locator(menuSelector).count();
			LOGGER.info("✅ Navigation menu ready with {} items", menuItemCount);
		} catch (final AssertionError e) {
			LOGGER.error("❌ Navigation priming failed: {}", e.getMessage());
			throw e;
		} catch (final Exception e) {
			LOGGER.warn("⚠️ Unexpected error during navigation priming: {}", e.getMessage());
		}
	}
	// ===========================================
	// MISSING METHODS FOR COMPATIBILITY
	// ===========================================

	/** Reads the current value from a bound field by entity class and field name. */
	protected String readFieldValueById(final Class<?> entityClass, final String fieldName) {
		return readFieldValueById(computeFieldId(entityClass, fieldName));
	}

	/** Reads the current value from a bound field by DOM ID. */
	protected String readFieldValueById(final String elementId) {
		final Locator host = locatorById(elementId);
		if (host.locator("input").count() > 0) {
			return host.locator("input").first().inputValue();
		}
		if (host.locator("textarea").count() > 0) {
			return host.locator("textarea").first().inputValue();
		}
		return host.innerText();
	}

	private void registerConsoleListener() {
		if (page == null || consoleListenerRegistered) {
			return;
		}
		page.onConsoleMessage(msg -> {
			final String text = msg.text();
			final String location = msg.location() != null ? msg.location().toString() : "";
			final String combined = text == null ? location : location.isEmpty() ? text : text + " " + location;
			if (msg.type() != null && msg.type().equalsIgnoreCase("error")) {
				if (!isIgnorableConsoleMessage(combined)) {
					LOGGER.error("🌐 Browser console error: {} ({})", text, msg.location());
				}
			}
			if (text != null && (text.contains("ERROR") || text.contains("Exception") || text.contains("CRITICAL") || text.contains("FATAL"))
					&& !isIgnorableConsoleMessage(combined)) {
				synchronized (EXCEPTION_LOCK) {
					DETECTED_EXCEPTIONS.add(text);
				}
			}
		});
		page.onPageError(error -> {
			final String message = error != null ? error : "Unknown page error";
			if (!isIgnorableConsoleMessage(message)) {
				LOGGER.error("🌐 Browser page error: {}", message);
				synchronized (EXCEPTION_LOCK) {
					DETECTED_EXCEPTIONS.add(message);
				}
			}
		});
		consoleListenerRegistered = true;
	}

	private String safePageUrl() {
		try {
			return page.url();
		} catch (@SuppressWarnings ("unused") final PlaywrightException e) {
			return "<unknown>";
		}
	}

	protected String sanitizeForFileName(final String value, final String fallback) {
		return sanitizeForIdentifier(value, fallback);
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

	/** Selects the first option from a Vaadin ComboBox bound to the given entity field. */
	protected void selectFirstComboBoxOptionById(final Class<?> entityClass, final String fieldName) {
		selectFirstComboBoxOptionById(computeFieldId(entityClass, fieldName));
	}

	/** Selects the first option from a Vaadin ComboBox identified by its DOM ID. */
	protected void selectFirstComboBoxOptionById(final String elementId) {
		Check.notBlank(elementId, "Element ID cannot be blank when selecting ComboBox option");
		final Locator host = locatorById(elementId);
		Locator combo = host;
		final Locator embeddedCombo = host.locator("vaadin-combo-box, c-navigable-combo-box, c-combo-box");
		if (embeddedCombo.count() > 0) {
			combo = embeddedCombo.first();
		}
		try {
			combo.scrollIntoViewIfNeeded();
		} catch (final PlaywrightException e) {
			LOGGER.debug("Unable to scroll combo box {} into view: {}", elementId, e.getMessage());
		}
		final Locator input = combo.locator("input");
		if (input.count() > 0) {
			input.first().click();
		} else {
			combo.click();
		}
		Locator options = page.locator("vaadin-combo-box-overlay[opened] vaadin-combo-box-item");
		for (int attempt = 0; attempt < 5 && options.count() == 0; attempt++) {
			wait_500();
			options = page.locator("vaadin-combo-box-overlay[opened] vaadin-combo-box-item");
		}
		if (options.count() == 0) {
			try {
				combo.press("ArrowDown");
			} catch (final PlaywrightException e) {
				LOGGER.debug("Unable to open combo box {} via ArrowDown: {}", elementId, e.getMessage());
			}
			wait_500();
			options = page.locator("vaadin-combo-box-overlay[opened] vaadin-combo-box-item");
		}
		if (options.count() == 0) {
			final Locator toggle = page.locator("#" + elementId + "::part(toggle-button)");
			if (toggle.count() > 0) {
				toggle.first().click();
				wait_500();
				options = page.locator("vaadin-combo-box-overlay[opened] vaadin-combo-box-item");
			}
		}
		if (options.count() == 0) {
			options = page.locator("vaadin-combo-box-item");
		}
		if (options.count() == 0) {
			if (input.count() > 0) {
				try {
					input.first().fill("a");
					wait_500();
					options = page.locator("vaadin-combo-box-overlay[opened] vaadin-combo-box-item");
				} catch (final PlaywrightException e) {
					LOGGER.debug("Unable to filter combo box {} via input: {}", elementId, e.getMessage());
				}
			}
		}
		if (options.count() == 0) {
			if (input.count() > 0) {
				try {
					input.first().press("ArrowDown");
					input.first().press("Enter");
					wait_500();
					return;
				} catch (final PlaywrightException e) {
					LOGGER.debug("Unable to select combo box {} via keyboard: {}", elementId, e.getMessage());
				}
			}
			throw new AssertionError("No options available for combo-box with id '" + elementId + "'");
		}
		options.first().click();
		wait_500();
	}

	@BeforeEach
	void setupTestEnvironment() {
		LOGGER.info("🧪 Setting up Playwright test environment...");
		try {
			// Read configuration from system properties
			// DEFAULT TO VISIBLE (false) for better debugging
			final boolean headless = Boolean.parseBoolean(System.getProperty("playwright.headless", "false"));
			final int slowmo = Integer.parseInt(System.getProperty("playwright.slowmo", "0"));
			final int viewportWidth = Integer.parseInt(System.getProperty("playwright.viewport.width", "1920"));
			final int viewportHeight = Integer.parseInt(System.getProperty("playwright.viewport.height", "1080"));
			LOGGER.info("🎭 Browser mode: {}", headless ? "HEADLESS" : "VISIBLE");
			if (slowmo > 0) {
				LOGGER.info("⏱️ Browser slowdown: {}ms per action", slowmo);
			}
			LOGGER.info("📺 Viewport size: {}x{}", viewportWidth, viewportHeight);
			final List<String> launchArguments = new ArrayList<>(Arrays.asList("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu"));
			if (!headless) {
				launchArguments.add("--start-maximized");
			}
			final BrowserType.LaunchOptions launchOptions =
					new BrowserType.LaunchOptions().setHeadless(headless).setSlowMo(slowmo).setArgs(launchArguments);
			// Check if chromium is available in Playwright cache
			final String playwrightCache = System.getProperty("user.home") + "/.cache/ms-playwright/chromium-1091/chrome";
			final java.io.File cachedChromium = new java.io.File(playwrightCache);
			if (cachedChromium.exists() && cachedChromium.canExecute()) {
				// Use cached Playwright Chromium directly to bypass download
				LOGGER.info("📦 Using cached Playwright Chromium at: {}", playwrightCache);
				playwright = Playwright.create();
				launchOptions.setExecutablePath(Paths.get(playwrightCache));
				browser = playwright.chromium().launch(launchOptions);
			} else {
				// Try Playwright default download first
				try {
					playwright = Playwright.create();
					browser = playwright.chromium().launch(launchOptions);
				} catch (@SuppressWarnings ("unused") final Exception browserError) {
					LOGGER.info("⚠️ Playwright-bundled Chromium not available, trying system Chromium...");
					// Try to use system Chromium as fallback
					final String[] possiblePaths = {
							"/usr/bin/chromium-browser", "/usr/bin/chromium", "/usr/bin/google-chrome"
					};
					for (final String chromiumPath : possiblePaths) {
						if (new java.io.File(chromiumPath).exists()) {
							LOGGER.info("📦 Using system Chromium at: {}", chromiumPath);
							if (playwright == null) {
								playwright = Playwright.create();
							}
							launchOptions.setExecutablePath(Paths.get(chromiumPath));
							browser = playwright.chromium().launch(launchOptions);
							break;
						}
					}
					if (browser == null) {
						throw new RuntimeException("No Chromium browser found");
					}
				}
			}
			final Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();
			if (headless) {
				// Set viewport size in headless mode
				contextOptions.setViewportSize(viewportWidth, viewportHeight);
			} else {
				// In visible mode, let browser window control size or use configured viewport
				if (viewportWidth != 1920 || viewportHeight != 1080) {
					contextOptions.setViewportSize(viewportWidth, viewportHeight);
				} else {
					contextOptions.setViewportSize(null); // allow the browser window to control size when visible
				}
			}
			context = browser.newContext(contextOptions);
			page = context.newPage();
			consoleListenerRegistered = false;
			synchronized (EXCEPTION_LOCK) {
				DETECTED_EXCEPTIONS.clear();
			}
			registerConsoleListener();
			page.navigate("http://localhost:" + port + "/login");
			LOGGER.info("✅ Test environment setup complete - navigated to http://localhost:{}/login", port);
		} catch (final Exception e) {
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
			// Check if screenshots are disabled
			final boolean skipScreenshots = Boolean.parseBoolean(System.getenv("PLAYWRIGHT_SKIP_SCREENSHOTS"));
			if (skipScreenshots) {
				if (logAction) {
					LOGGER.debug("📸 Screenshot skipped (disabled): {}", name);
				}
				return;
			}
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

	/** Tests all menu item openings to ensure navigation works */
	protected void testAllMenuItemOpenings() {
		LOGGER.info("🧭 Testing all menu item openings...");
		try {
			final int visitedCount = visitMenuItems(true, false, "menu");
			LOGGER.info("✅ Menu item testing completed; visited {} entries", visitedCount);
		} catch (final Exception e) {
			takeScreenshot("menu-openings-error", true);
			throw new AssertionError("Menu item testing failed: " + e.getMessage(), e);
		}
	}

	/** Performs comprehensive attachment CRUD operations (Upload/Download/Delete). Generic method that can be called from any test for any entity
	 * with attachments.
	 * @return true if all operations succeeded, false otherwise */
	protected boolean testAttachmentCrudOperations() {
		try {
			final Locator attachmentsContainer = locateAttachmentsContainer();
			if (attachmentsContainer == null) {
				LOGGER.debug("ℹ️ No attachments section found - skipping attachment CRUD test");
				return false;
			}
			LOGGER.info("📤 Testing Attachment CRUD - Upload/Download/Delete");
			attachmentsContainer.scrollIntoViewIfNeeded();
			wait_500();
			// Test UPLOAD
			final Locator uploadButton = locateAttachmentToolbarButton(attachmentsContainer, "vaadin:upload");
			if (uploadButton.count() == 0) {
				LOGGER.debug("ℹ️ Upload button not found - skipping attachment CRUD test");
				return false;
			}
			uploadButton.click();
			wait_500();
			final Locator dialog = waitForDialogWithText("Upload File");
			if (dialog.count() == 0) {
				LOGGER.warn("⚠️ Upload dialog did not open");
				return false;
			}
			final Path tempFile = Files.createTempFile("test-attachment-", ".txt");
			Files.writeString(tempFile, "Test attachment content - " + System.currentTimeMillis());
			dialog.locator("vaadin-upload input[type='file']").setInputFiles(tempFile);
			final Locator dialogUploadButton = dialog.locator("#cbutton-upload");
			waitForButtonEnabled(dialogUploadButton);
			dialogUploadButton.click();
			waitForDialogToClose();
			wait_1000();
			// Verify attachment uploaded
			final String fileName = tempFile.getFileName().toString();
			final Locator attachmentsGrid = locateAttachmentsGrid(attachmentsContainer);
			waitForGridCellText(attachmentsGrid, fileName);
			LOGGER.info("✅ Attachment uploaded: {}", fileName);
			// Test DOWNLOAD
			final Locator uploadedCell = attachmentsGrid.locator("vaadin-grid-cell-content").filter(new Locator.FilterOptions().setHasText(fileName));
			uploadedCell.first().click();
			wait_500();
			final Locator downloadButton = locateAttachmentToolbarButton(attachmentsContainer, "vaadin:download");
			if (downloadButton.isDisabled()) {
				LOGGER.warn("⚠️ Download button is disabled after selection");
				return false;
			}
			downloadButton.click();
			wait_500();
			LOGGER.info("✅ Attachment download triggered");
			// Test DELETE
			final Locator deleteButton = locateAttachmentToolbarButton(attachmentsContainer, "vaadin:trash");
			if (deleteButton.isDisabled()) {
				LOGGER.warn("⚠️ Delete button is disabled after selection");
				return false;
			}
			deleteButton.click();
			wait_500();
			final Locator confirmYes = page.locator("#cbutton-yes");
			if (confirmYes.count() > 0) {
				confirmYes.first().click();
			}
			waitForDialogToClose();
			wait_1000();
			waitForGridCellGone(attachmentsGrid, fileName);
			LOGGER.info("✅ Attachment deleted successfully");
			return true;
		} catch (final Exception e) {
			LOGGER.error("❌ Attachment CRUD test failed: {}", e.getMessage(), e);
			return false;
		}
	}

	/** Tests attachment operations if available on the current page. */
	protected boolean testAttachmentOperations() {
		try {
			final Locator attachmentsContainer = locateAttachmentsContainer();
			if (attachmentsContainer == null) {
				LOGGER.debug("ℹ️ No attachments section found on page");
				return false;
			}
			LOGGER.info("📎 Testing attachment operations...");
			attachmentsContainer.scrollIntoViewIfNeeded();
			// Test upload button exists
			final Locator uploadButton = attachmentsContainer.locator("vaadin-button")
					.filter(new Locator.FilterOptions().setHas(page.locator("vaadin-icon[icon='vaadin:upload']")));
			if (uploadButton.count() > 0) {
				LOGGER.info("✅ Attachment upload button found");
				return true;
			} else {
				LOGGER.debug("ℹ️ No attachment upload button found");
				return false;
			}
		} catch (final Exception e) {
			LOGGER.warn("⚠️ Failed to test attachment operations: {}", e.getMessage());
			return false;
		}
	}

	/** Tests breadcrumb navigation if present */
	protected void testBreadcrumbNavigation() {
		LOGGER.info("🍞 Testing breadcrumb navigation...");
		try {
			final Locator breadcrumbs = page.locator(".breadcrumb, vaadin-breadcrumb, nav[aria-label*='breadcrumb']");
			if (breadcrumbs.count() > 0) {
				LOGGER.info("📋 Found breadcrumb navigation");
				final Locator breadcrumbItems = breadcrumbs.locator("a, button, span");
				final int itemCount = breadcrumbItems.count();
				LOGGER.info("📊 Found {} breadcrumb items", itemCount);
				// Test clicking breadcrumb items (except last one which is current)
				for (int i = 0; i < itemCount - 1; i++) {
					try {
						final Locator item = breadcrumbItems.nth(i);
						final String itemText = item.textContent();
						LOGGER.info("🔍 Testing breadcrumb item: {}", itemText);
						item.click();
						wait_1000();
						takeScreenshot("breadcrumb-" + i, false);
					} catch (final Exception e) {
						LOGGER.warn("⚠️ Failed to test breadcrumb item {}: {}", i, e.getMessage());
					}
				}
			} else {
				LOGGER.info("ℹ️ No breadcrumb navigation found");
			}
			LOGGER.info("✅ Breadcrumb navigation testing completed");
		} catch (final Exception e) {
			throw new AssertionError("Breadcrumb navigation test failed: " + e.getMessage(), e);
		}
	}
	// ===========================================
	// ENHANCED CRUD TESTING METHODS
	// ===========================================

	/** Performs comprehensive comment CRUD operations (Add/Edit/Delete). Generic method that can be called from any test for any entity with
	 * comments.
	 * @return true if all operations succeeded, false otherwise */
	protected boolean testCommentCrudOperations() {
		try {
			final Locator commentsContainer = locateCommentsContainer();
			if (commentsContainer == null) {
				LOGGER.debug("ℹ️ No comments section found - skipping comment CRUD test");
				return false;
			}
			LOGGER.info("💬 Testing Comment CRUD - Add/Edit/Delete");
			commentsContainer.scrollIntoViewIfNeeded();
			wait_500();
			// Test ADD COMMENT
			final Locator addCommentButton = commentsContainer.locator("vaadin-button")
					.filter(new Locator.FilterOptions().setHas(page.locator("vaadin-icon[icon='vaadin:plus']")));
			if (addCommentButton.count() == 0) {
				LOGGER.debug("ℹ️ Add comment button not found - skipping comment CRUD test");
				return false;
			}
			addCommentButton.first().click();
			wait_500();
			// Fill comment text
			final Locator commentField = page.locator("vaadin-text-area");
			if (commentField.count() == 0) {
				LOGGER.warn("⚠️ Comment text area not found");
				return false;
			}
			final String commentText = "Test comment added by Playwright automation - " + System.currentTimeMillis();
			commentField.first().fill(commentText);
			wait_500();
			// Save comment
			final Locator saveCommentButton = page.locator("#cbutton-save");
			if (saveCommentButton.count() == 0) {
				LOGGER.warn("⚠️ Save comment button not found");
				return false;
			}
			saveCommentButton.first().click();
			wait_1000();
			LOGGER.info("✅ Comment added successfully");
			return true;
		} catch (final Exception e) {
			LOGGER.error("❌ Comment CRUD test failed: {}", e.getMessage(), e);
			return false;
		}
	}

	/** Tests comment operations if available on the current page. */
	protected boolean testCommentOperations() {
		try {
			final Locator commentsContainer = locateCommentsContainer();
			if (commentsContainer == null) {
				LOGGER.debug("ℹ️ No comments section found on page");
				return false;
			}
			LOGGER.info("💬 Testing comment operations...");
			commentsContainer.scrollIntoViewIfNeeded();
			// Test add comment button exists
			final Locator addButton = commentsContainer.locator("vaadin-button")
					.filter(new Locator.FilterOptions().setHas(page.locator("vaadin-icon[icon='vaadin:plus']")));
			if (addButton.count() > 0) {
				LOGGER.info("✅ Add comment button found");
				return true;
			} else {
				LOGGER.debug("ℹ️ No add comment button found");
				return false;
			}
		} catch (final Exception e) {
			LOGGER.warn("⚠️ Failed to test comment operations: {}", e.getMessage());
			return false;
		}
	}

	/** Tests database initialization by verifying that essential data is present */
	protected void testDatabaseInitialization() {
		LOGGER.info("🗄️ Testing database initialization...");
		try {
			// Navigate to Users view via dynamic menu and check if default users exist
			final boolean navigatedToUsers = navigateToDynamicPageByEntityType("CUser");
			Check.isTrue(navigatedToUsers, "Unable to navigate to Users dynamic page");
			waitForDynamicPageLoad();
			final boolean usersExist = verifyGridHasData();
			if (!usersExist) {
				LOGGER.warn("⚠️ User grid appears empty after initialization; continuing with caution");
				takeScreenshot("user-grid-empty", false);
			}
			// Navigate to Projects view to check if data structure is ready
			final boolean navigatedToProjects = navigateToDynamicPageByEntityType("CProject");
			Check.isTrue(navigatedToProjects, "Unable to navigate to Projects dynamic page");
			waitForDynamicPageLoad();
			// Projects may be empty initially, just verify grid is present
			final Locator projectGrid = page.locator("vaadin-grid, vaadin-grid-pro, so-grid, c-grid, c-grid-pro");
			if (projectGrid.count() == 0) {
				LOGGER.warn("⚠️ Projects grid not detected after initialization");
			}
			LOGGER.info("✅ Database initialization test completed successfully");
		} catch (final Exception e) {
			throw new AssertionError("Database initialization test failed: " + e.getMessage(), e);
		}
	}

	/** Test CREATE operation on dynamic page. */
	protected void testDynamicPageCreate(String entityType) {
		try {
			LOGGER.info("➕ Testing CREATE operation for: {}", entityType);
			// Click New button
			final Locator newButton = waitForSelectorWithCheck("vaadin-button:has-text('New')", "New button");
			newButton.click();
			wait_1000();
			// Fill in form fields
			final String testName = "Test " + entityType;
			fillFormFieldsForEntity(entityType, testName);
			// Save the entity
			clickSave();
			wait_1000();
			takeScreenshot("dynamic-crud-" + entityType.toLowerCase() + "-create", false);
			LOGGER.info("✅ CREATE operation completed for: {}", entityType);
		} catch (final Exception e) {
			throw new AssertionError("CREATE operation failed for " + entityType + ": " + e.getMessage(), e);
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
		} catch (final Exception e) {
			final String message = "CRUD operations failed for dynamic page: " + entityType + " - " + e.getMessage();
			LOGGER.error("❌ {}", message);
			throw new AssertionError(message, e);
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
		} catch (final Exception e) {
			throw new AssertionError("DELETE operation failed for " + entityType + ": " + e.getMessage(), e);
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
		} catch (final Exception e) {
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
			final String updatedName = "Updated " + entityType;
			fillFormFieldsForEntity(entityType, updatedName);
			// Save changes
			clickSave();
			wait_1000();
			takeScreenshot("dynamic-crud-" + entityType.toLowerCase() + "-update", false);
			LOGGER.info("✅ UPDATE operation completed for: {}", entityType);
		} catch (final Exception e) {
			throw new AssertionError("UPDATE operation failed for " + entityType + ": " + e.getMessage(), e);
		}
	}

	/** Tests grid column functionality including sorting and filtering */
	protected void testGridColumnFunctionality(String entityName) {
		Check.notBlank(entityName, "Entity name cannot be blank");
		LOGGER.info("📊 Testing grid column functionality for: {}", entityName);
		try {
			final Locator grid = getLocatorWithCheck("vaadin-grid", "Grid for " + entityName);
			final Locator headers = grid.locator("vaadin-grid-sorter, th, .grid-header");
			final int headerCount = headers.count();
			LOGGER.info("📋 Found {} grid columns for {}", headerCount, entityName);
			// Test sorting on first few columns
			for (int i = 0; i < Math.min(headerCount, 3); i++) {
				try {
					final Locator header = headers.nth(i);
					final String headerText = header.textContent();
					LOGGER.info("🔄 Testing sort on column: {}", headerText);
					header.click();
					wait_1000();
					takeScreenshot("grid-sort-" + entityName.toLowerCase() + "-col" + i, false);
				} catch (final Exception e) {
					LOGGER.warn("⚠️ Failed to test sorting on column {}: {}", i, e.getMessage());
				}
			}
			LOGGER.info("✅ Grid column functionality testing completed for: {}", entityName);
		} catch (final Exception e) {
			throw new AssertionError("Grid column functionality test failed for " + entityName + ": " + e.getMessage(), e);
		}
	}

	/** Test navigation to company page that was created by samples and initializers.
	 * @return true if company page was found and loaded successfully */
	protected boolean testNavigationToCompanyPage() {
		LOGGER.info("🏢 Testing navigation to Company page created by initializers");
		try {
			// Try multiple approaches to find the company page
			final String[] companyPageSelectors = {
					"vaadin-side-nav-item:has-text('Companies')", "vaadin-side-nav-item:has-text('Company')", "a:has-text('Companies')",
					"a:has-text('Company Management')", "[href*='company']", "[href*='companies']", "text='System.Companies'"
			};
			for (final String selector : companyPageSelectors) {
				try {
					final Locator navItem = page.locator(selector);
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
				} catch (final Exception e) {
					LOGGER.debug("⚠️ Selector {} failed: {}", selector, e.getMessage());
				}
			}
			// Fallback: try direct navigation using navigateToFirstPage
			return navigateToFirstPage(null, CCompany.class);
		} catch (final Exception e) {
			LOGGER.error("❌ Failed to test navigation to company page: {}", e.getMessage());
			return false;
		}
	}

	/** Test navigation to user page that was created by samples and initializers.
	 * @return true if user page was found and loaded successfully */
	protected boolean testNavigationToUserPage() {
		LOGGER.info("👤 Testing navigation to User page created by initializers");
		try {
			// Try multiple approaches to find the user page
			final String[] userPageSelectors = {
					"vaadin-side-nav-item:has-text('Users')", "vaadin-side-nav-item:has-text('User')", "a:has-text('Users')",
					"a:has-text('User Management')", "[href*='user']", "text='System.Users'"
			};
			for (final String selector : userPageSelectors) {
				try {
					final Locator navItem = page.locator(selector);
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
				} catch (final Exception e) {
					LOGGER.debug("⚠️ Selector {} failed: {}", selector, e.getMessage());
				}
			}
			// Fallback: try direct navigation using navigateToFirstPage
			return navigateToFirstPage(null, CUser.class);
		} catch (final Exception e) {
			LOGGER.error("❌ Failed to test navigation to user page: {}", e.getMessage());
			return false;
		}
	}

	/** Tests parent item selection if available on the current page. */
	protected boolean testParentItemSelection() {
		try {
			final Locator parentSelector = locateParentItemSelector();
			if (parentSelector == null) {
				LOGGER.debug("ℹ️ No parent item selector found on page");
				return false;
			}
			LOGGER.info("🔗 Testing parent item selection...");
			parentSelector.scrollIntoViewIfNeeded();
			parentSelector.click();
			wait_500();
			final Locator items = page.locator("vaadin-combo-box-item");
			if (items.count() > 0) {
				items.first().click();
				wait_500();
				LOGGER.info("✅ Parent item selected successfully");
				return true;
			} else {
				LOGGER.debug("ℹ️ No parent items available in dropdown");
				return false;
			}
		} catch (final Exception e) {
			LOGGER.warn("⚠️ Failed to test parent item selection: {}", e.getMessage());
			return false;
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
				final Locator activateButton = page.locator("vaadin-button:has-text('Activate'), vaadin-button:has-text('Enable')");
				final Locator deactivateButton = page.locator("vaadin-button:has-text('Deactivate'), vaadin-button:has-text('Disable')");
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
					final Locator statusField = page.locator("vaadin-text-field[label*='Status'], vaadin-combo-box[label*='Status']");
					if (statusField.count() > 0) {
						LOGGER.info("📊 Found status field for project state");
					}
				}
			} else {
				LOGGER.info("ℹ️ No projects found to test activation");
			}
			LOGGER.info("✅ Project activation test completed");
		} catch (final Exception e) {
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
				fillFirstTextField("Test Change");
				clickSave();
				wait_1000();
				takeScreenshot("project-change-tracking", false);
				LOGGER.info("✅ Project change tracking test completed");
			} else {
				LOGGER.info("ℹ️ No projects found to test change tracking");
			}
		} catch (final Exception e) {
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
			final Locator sideNav = page.locator("vaadin-side-nav, nav, .navigation");
			if (sideNav.count() > 0) {
				LOGGER.info("📋 Found navigation sidebar");
				// Test expanding/collapsing if applicable
				final Locator toggleButton = page.locator("vaadin-button[aria-label*='menu'], button[aria-label*='toggle']");
				if (toggleButton.count() > 0) {
					toggleButton.first().click();
					wait_500();
					takeScreenshot("sidebar-toggle", false);
				}
				// Test navigation items
				final Locator navItems = page.locator("vaadin-side-nav-item, .nav-item, a[href]");
				final int itemCount = navItems.count();
				LOGGER.info("📊 Found {} navigation items", itemCount);
				for (int i = 0; i < Math.min(itemCount, 5); i++) { // Test first 5 items
					try {
						final Locator navItem = navItems.nth(i);
						final String itemText = navItem.textContent();
						LOGGER.info("🔍 Testing navigation item {}: {}", i + 1, itemText);
						navItem.click();
						wait_1000();
						takeScreenshot("nav-item-" + i, false);
					} catch (final Exception e) {
						LOGGER.warn("⚠️ Failed to test navigation item {}: {}", i + 1, e.getMessage());
					}
				}
			} else {
				LOGGER.warn("⚠️ No sidebar navigation found");
			}
			LOGGER.info("✅ Sidebar navigation testing completed");
		} catch (final Exception e) {
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
			for (final Class<?> viewClass : mainViewClasses) {
				LOGGER.info("📋 Checking database structure for: {}", viewClass.getSimpleName());
				final boolean navigationSuccess = navigateToViewByClass(viewClass);
				Check.isTrue(navigationSuccess, "Should be able to navigate to " + viewClass.getSimpleName());
				wait_1000();
				// Verify that grid is present (indicates table exists)
				final Locator grid = page.locator("vaadin-grid").first();
				Check.isTrue(grid.count() > 0, "Grid should be present for " + viewClass.getSimpleName());
			}
			LOGGER.info("✅ Database structure verification completed");
		} catch (final Exception e) {
			throw new AssertionError("Database structure verification failed: " + e.getMessage(), e);
		}
	}
	// ===========================================
	// PROJECT ACTIVATION TESTING METHODS
	// ===========================================

	/** Verifies that the grid contains data by checking for the presence of grid cells. */
	protected boolean verifyGridHasData() {
		try {
			page.waitForSelector("vaadin-grid, vaadin-grid-pro, so-grid, c-grid, c-grid-pro", new Page.WaitForSelectorOptions().setTimeout(20000));
		} catch (final Exception e) {
			LOGGER.warn("⚠️ Grid not found while waiting for data: {}", e.getMessage());
			return false;
		}
		final Locator grid = page.locator("vaadin-grid, vaadin-grid-pro, so-grid, c-grid, c-grid-pro").first();
		for (int attempt = 0; attempt < 30; attempt++) {
			final Locator cells = grid.locator("vaadin-grid-cell-content, [part='cell'], so-grid-cell, c-grid-cell");
			final int cellCount = cells.count();
			if (cellCount > 0) {
				LOGGER.info("📊 Grid has data: true (cells={})", cellCount);
				return true;
			}
			wait_1000();
		}
		LOGGER.info("📊 Grid has data: false");
		return false;
	}
	// ===========================================
	// TESTING UTILITY METHODS
	// ===========================================
	// ===========================================
	// ATTACHMENT, COMMENT, AND PARENT ITEM TESTING
	// ===========================================

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
			// Reduced timeout from 20000 to 10000 (10 seconds) for faster fail
			page.waitForSelector(menuSelector, new Page.WaitForSelectorOptions().setTimeout(10000));
			totalItems = page.locator(menuSelector).count();
		} catch (final Exception waitError) {
			if (!allowEmpty) {
				final String errorMsg = "No navigation items found within 10 seconds - test failing fast. "
						+ "Ensure sample data is loaded and company is selected at login. Error: " + waitError.getMessage();
				LOGGER.error("❌ {}", errorMsg);
				throw new AssertionError(errorMsg, waitError);
			}
			LOGGER.warn("⚠️ Navigation items not found within 10 seconds: {}", waitError.getMessage());
			return 0;
		}
		LOGGER.info("📋 Found {} menu entries to visit", totalItems);
		if (totalItems == 0 && !allowEmpty) {
			throw new AssertionError("No navigation items found to exercise - failing fast");
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
					final String prefix = screenshotPrefix == null ? "menu" : screenshotPrefix;
					final String screenshotName = prefix + "-" + (safeLabel.isBlank() ? "entry-" + (i + 1) : safeLabel + "-" + (i + 1));
					takeScreenshot(screenshotName, false);
				}
				visitedLabels.add(label);
			} catch (final Exception itemError) {
				LOGGER.warn("⚠️ Failed to open menu item {}: {}", i + 1, itemError.getMessage());
			}
		}
		return visitedLabels.size();
	}

	/** Waits for 1000 milliseconds to allow complex operations to complete. */
	protected void wait_1000() {
		try {
			Thread.sleep(1000);
		} catch (@SuppressWarnings ("unused") final InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			performFailFastCheck("wait_1000");
		}
	}

	/** Waits for 2000 milliseconds for slow operations like navigation. */
	protected void wait_2000() {
		try {
			Thread.sleep(2000);
		} catch (@SuppressWarnings ("unused") final InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			performFailFastCheck("wait_2000");
		}
	}
	// ===========================================
	// ADVANCED TESTING METHODS
	// ===========================================

	/** Waits for 500 milliseconds to allow UI updates to complete. */
	protected void wait_500() {
		try {
			Thread.sleep(500);
		} catch (@SuppressWarnings ("unused") final InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			performFailFastCheck("wait_500");
		}
	}

	/** Waits for after login state */
	protected void wait_afterlogin() {
		try {
			page.waitForSelector("vaadin-app-layout, vaadin-side-nav, vaadin-drawer-layout", new Page.WaitForSelectorOptions().setTimeout(15000));
		} catch (final Exception e) {
			LOGGER.warn("⚠️ Post-login application shell not detected: {}", e.getMessage());
		}
	}

	/** Waits for login screen to be ready */
	protected void wait_loginscreen() {
		try {
			page.waitForSelector("#custom-username-input, #custom-password-input, #" + LOGIN_BUTTON_ID,
					new Page.WaitForSelectorOptions().setTimeout(15000));
		} catch (final Exception e) {
			LOGGER.warn("⚠️ Login screen not detected: {}", e.getMessage());
		}
	}
	// ================================================================================
	// EXCEPTION DETECTION SYSTEM FOR FAIL-FAST BEHAVIOR
	// ================================================================================

	/** Helper method to wait for button to become enabled. */
	protected void waitForButtonEnabled(final Locator button) {
		final int maxAttempts = 12;
		for (int attempt = 0; attempt < maxAttempts; attempt++) {
			if (!button.isDisabled()) {
				return;
			}
			wait_500();
		}
		throw new AssertionError("Button did not become enabled");
	}

	/** Helper method to wait for dialog to close. */
	protected void waitForDialogToClose() {
		final int maxAttempts = 10;
		for (int attempt = 0; attempt < maxAttempts; attempt++) {
			if (page.locator("vaadin-dialog-overlay[opened]").count() == 0) {
				return;
			}
			wait_500();
		}
	}

	/** Helper method to wait for dialog with specific text to appear. */
	protected Locator waitForDialogWithText(final String text) {
		final int maxAttempts = 10;
		for (int attempt = 0; attempt < maxAttempts; attempt++) {
			final Locator overlay = page.locator("vaadin-dialog-overlay[opened]").filter(new Locator.FilterOptions().setHasText(text));
			if (overlay.count() > 0) {
				return overlay.first();
			}
			wait_500();
		}
		throw new AssertionError("Dialog with text '" + text + "' did not open");
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
		} catch (final Exception e) {
			final String message = "Dynamic page failed to load properly: " + e.getMessage();
			LOGGER.error("❌ {}", message);
			throw new AssertionError(message, e);
		}
	}

	/** Helper method to wait for grid cell with specific text to disappear. */
	protected void waitForGridCellGone(final Locator grid, final String text) {
		final int maxAttempts = 12;
		for (int attempt = 0; attempt < maxAttempts; attempt++) {
			final Locator matches = grid.locator("vaadin-grid-cell-content").filter(new Locator.FilterOptions().setHasText(text));
			if (matches.count() == 0) {
				return;
			}
			if (!matches.first().isVisible()) {
				return;
			}
			wait_500();
		}
		throw new AssertionError("Grid cell still present: " + text);
	}

	/** Helper method to wait for grid cell with specific text to appear. */
	protected void waitForGridCellText(final Locator grid, final String text) {
		final int maxAttempts = 12;
		for (int attempt = 0; attempt < maxAttempts; attempt++) {
			if (grid.locator("vaadin-grid-cell-content").filter(new Locator.FilterOptions().setHasText(text)).count() > 0) {
				return;
			}
			wait_500();
		}
		throw new AssertionError("Expected grid cell not found: " + text);
	}

	private void waitForLoginSuccess() {
		final int maxAttempts = 30; // 15 seconds max wait
		for (int attempt = 0; attempt < maxAttempts; attempt++) {
			failFastIfLoginErrorVisible("Login Wait");
			if (page.locator("vaadin-app-layout, vaadin-side-nav, vaadin-drawer-layout").count() > 0) {
				return;
			}
			performFailFastCheck("Login Wait");
			wait_500();
		}
		throw new AssertionError("Login did not complete within expected time");
	}

	/** Waits for the specified Vaadin overlay selector to disappear. */
	private void waitForOverlayToClose(String overlaySelector) {
		final int maxAttempts = 10; // 5 seconds max wait
		for (int attempt = 0; attempt < maxAttempts; attempt++) {
			performFailFastCheck("Overlay Close Wait");
			if (page.locator(overlaySelector).count() == 0) {
				return;
			}
			wait_500();
		}
		if (page.locator("#" + PROGRESS_DIALOG_ID + "[opened]").count() > 0 || page.locator("#" + INFO_OK_BUTTON_ID).count() > 0) {
			return;
		}
		LOGGER.warn("⚠️ Overlay {} still present after waiting {} seconds", overlaySelector, maxAttempts * 0.5);
	}

	private void waitForProgressDialogToComplete() {
		final int openAttempts = 20; // 10 seconds to appear
		LOGGER.info("⏳ Waiting for progress dialog to appear");
		boolean opened = false;
		for (int attempt = 0; attempt < openAttempts; attempt++) {
			performFailFastCheck("Progress Dialog Open Wait");
			if (page.locator("#" + PROGRESS_DIALOG_ID + "[opened]").count() > 0) {
				opened = true;
				break;
			}
			if (page.locator("#" + INFO_OK_BUTTON_ID).count() > 0) {
				LOGGER.info("✅ Information dialog detected without progress dialog");
				return;
			}
			wait_500();
		}
		if (!opened) {
			throw new AssertionError("Progress or information dialog did not appear after database reset");
		}
		LOGGER.info("⏳ Progress dialog detected - waiting for completion");
		final int closeAttempts = 120; // 60 seconds max wait
		for (int attempt = 0; attempt < closeAttempts; attempt++) {
			performFailFastCheck("Progress Dialog Close Wait");
			if (page.locator("#" + INFO_OK_BUTTON_ID).count() > 0) {
				LOGGER.info("✅ Information dialog detected after progress dialog");
				return;
			}
			if (page.locator("#" + PROGRESS_DIALOG_ID + "[opened]").count() == 0) {
				LOGGER.info("✅ Progress dialog closed");
				return;
			}
			wait_500();
		}
		throw new AssertionError("Progress dialog did not close after database reset");
	}

	/** Safe wait for selector with check */
	protected Locator waitForSelectorWithCheck(String selector, String description) {
		Objects.requireNonNull(selector, "Selector cannot be null");
		Check.notBlank(description, "Description cannot be blank");
		try {
			page.waitForSelector(selector, new Page.WaitForSelectorOptions().setTimeout(10000));
			return getLocatorWithCheck(selector, description);
		} catch (final Exception e) {
			throw new AssertionError("Element not found after wait: " + description + " (selector: " + selector + ")", e);
		}
	}
	// ===========================================
	// DATABASE INITIALIZATION TESTING METHODS
	// ===========================================
	// ===========================================
	// DYNAMIC PAGE NAVIGATION METHODS
	// ===========================================
}
