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
}
