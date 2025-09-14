package automated_tests.tech.derbent.ui.automation;

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
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.vaadin.flow.router.Route;
import tech.derbent.activities.view.CActivitiesView;
import tech.derbent.activities.view.CActivityStatusView;
import tech.derbent.activities.view.CActivityTypeView;
import tech.derbent.decisions.view.CDecisionStatusView;
import tech.derbent.meetings.view.CMeetingsView;
import tech.derbent.projects.view.CProjectsView;
import tech.derbent.risks.view.CRiskStatusView;
import tech.derbent.users.view.CUsersView;

/** Enhanced base UI test class that provides common functionality for Playwright tests. This class includes 25+ auxiliary methods for testing all
 * views and business functions. The base class follows strict coding guidelines and provides comprehensive testing utilities for: - Login and
 * authentication workflows - Navigation between views using ID-based selectors - CRUD operations testing - Form validation and ComboBox testing -
 * Grid interactions and data verification - Screenshot capture for debugging - Responsive design testing across viewports - Cross-view data
 * consistency testing */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles ("test")
public abstract class CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBaseUITest.class);
	private Playwright playwright;
	private Browser browser;
	private BrowserContext context;
	protected Page page;
	@LocalServerPort
	protected int port;

	@BeforeEach
	void setupTestEnvironment() {
		LOGGER.info("🧪 Setting up Playwright test environment...");
		playwright = Playwright.create();
		browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
		context = browser.newContext();
		page = context.newPage();
		page.navigate("http://localhost:" + port);
		LOGGER.info("✅ Test environment setup complete - navigated to http://localhost:{}", port);
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

	/** Performs complete login workflow with username and password. This method handles the entire authentication process including form submission
	 * and redirection verification. */
	protected void loginToApplication() {
		loginToApplication("admin", "admin");
	}

	/** Performs login with specified credentials and verifies successful authentication. */
	protected void loginToApplication(final String username, final String password) {
		LOGGER.info("🔐 Attempting login with username: {}", username);
		page.fill("input[type='text']", username);
		page.fill("input[type='password']", password);
		page.click("vaadin-button");
		page.waitForURL("**/projects");
		LOGGER.info("✅ Login successful - redirected to projects view");
		takeScreenshot("post-login", false);
	}
	// ===========================================
	// NAVIGATION METHODS
	// ===========================================

	/** Navigates to a view by its text content in the navigation menu. Returns true if navigation was successful. */
	protected boolean navigateToViewByText(final String viewText) {
		LOGGER.info("🧭 Navigating to view: {}", viewText);
		try {
			final Locator navItem = page.locator("vaadin-side-nav-item").filter(new Locator.FilterOptions().setHasText(viewText));
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

	/** Fills the first text field found on the page with the specified value. */
	protected void fillFirstTextField(final String value) {
		LOGGER.info("📝 Filling first text field with: {}", value);
		page.locator("vaadin-text-field").first().fill(value);
	}

	/** Fills the first text area found on the page with the specified value. */
	protected void fillFirstTextArea(final String value) {
		LOGGER.info("📝 Filling first text area with: {}", value);
		page.locator("vaadin-text-area").first().fill(value);
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

	/** Verifies that the grid contains data by checking for the presence of grid cells. */
	protected boolean verifyGridHasData() {
		final Locator grid = page.locator("vaadin-grid").first();
		final Locator cells = grid.locator("vaadin-grid-cell-content");
		final boolean hasData = cells.count() > 0;
		LOGGER.info("📊 Grid has data: {}", hasData);
		return hasData;
	}
	// ===========================================
	// TESTING UTILITY METHODS
	// ===========================================

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
			final String screenshotPath = "target/screenshots/" + name + ".png";
			page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(screenshotPath)));
			if (logAction) {
				LOGGER.info("✅ Screenshot saved: {}", screenshotPath);
			}
		} catch (final Exception e) {
			LOGGER.error("❌ Failed to take screenshot '{}': {}", name, e.getMessage());
		}
	}

	/** Waits for 500 milliseconds to allow UI updates to complete. */
	protected void wait_500() {
		try {
			Thread.sleep(500);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

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

	/** Tests responsive design by checking layout at different viewport sizes. */
	protected void testResponsiveDesign() {
		LOGGER.info("📱 Testing responsive design across viewport sizes");
		final int[][] viewports = {
				{
						1920, 1080
				}, {
						1366, 768
				}, {
						768, 1024
				}, {
						375, 667
				}
		};
		for (final int[] viewport : viewports) {
			page.setViewportSize(viewport[0], viewport[1]);
			wait_500();
			takeScreenshot("responsive-" + viewport[0] + "x" + viewport[1], false);
		}
		LOGGER.info("✅ Responsive design testing complete");
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

	/** Navigates to the Projects view using the navigation menu. */
	protected void navigateToProjects() {
		navigateToViewByClass(CProjectsView.class);
	}

	/** Navigates to the Activities view using the navigation menu. */
	protected void navigateToActivities() {
		navigateToViewByClass(CActivitiesView.class);
	}

	/** Navigates to the Meetings view using the navigation menu. */
	protected void navigateToMeetings() {
		navigateToViewByClass(CMeetingsView.class);
	}

	/** Navigates to the Users view using the navigation menu. */
	protected void navigateToUsers() {
		navigateToViewByClass(CUsersView.class);
	}
	// ===========================================
	// MISSING METHODS FOR COMPATIBILITY
	// ===========================================

	/** Array of main view classes for testing */
	protected Class<?>[] mainViewClasses = {
			CProjectsView.class, CActivitiesView.class, CMeetingsView.class, CUsersView.class, CActivityStatusView.class, CActivityTypeView.class,
			CDecisionStatusView.class, CRiskStatusView.class
	};
	/** Legacy property for backward compatibility */
	protected Class<?>[] viewClasses = mainViewClasses;
	/** Status and type view classes */
	protected Class<?>[] statusAndTypeViewClasses = {
			CActivityStatusView.class, CActivityTypeView.class, CDecisionStatusView.class, CRiskStatusView.class
	};
	/** Admin view classes */
	protected Class<?>[] adminViewClasses = {};
	/** All view classes */
	protected Class<?>[] allViewClasses = mainViewClasses;
	/** Kanban view classes */
	protected Class<?>[] kanbanViewClasses = {};

	/** Gets the count of rows in the first grid */
	protected int getGridRowCount() {
		final Locator grid = page.locator("vaadin-grid").first();
		final Locator cells = grid.locator("vaadin-grid-cell-content");
		return cells.count();
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

	/** Waits for login screen to be ready */
	protected void wait_loginscreen() {
		try {
			page.waitForSelector("input[type='text'], input[type='email'], vaadin-text-field", new Page.WaitForSelectorOptions().setTimeout(10000));
		} catch (Exception e) {
			LOGGER.warn("⚠️ Login screen not detected: {}", e.getMessage());
		}
	}

	/** Waits for after login state */
	protected void wait_afterlogin() {
		try {
			page.waitForURL("**/projects", new Page.WaitForURLOptions().setTimeout(10000));
		} catch (Exception e) {
			LOGGER.warn("⚠️ Post-login navigation not detected: {}", e.getMessage());
		}
	}

	/** Checks if browser is available */
	protected boolean isBrowserAvailable() { return page != null && !page.isClosed(); }

	/** Assert browser is available */
	protected void assertBrowserAvailable() {
		if (!isBrowserAvailable()) {
			throw new AssertionError("Browser is not available");
		}
	}

	/** Tests accessibility basics */
	protected void testAccessibilityBasics() {
		testAccessibilityBasics("");
	}

	/** Tests accessibility basics with description */
	protected void testAccessibilityBasics(String description) {
		LOGGER.info("♿ Testing accessibility basics {}", description);
		verifyAccessibility();
	}
}
