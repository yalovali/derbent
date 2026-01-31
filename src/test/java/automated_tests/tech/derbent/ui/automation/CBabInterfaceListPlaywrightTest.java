package automated_tests.tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import automated_tests.tech.derbent.ui.automation.components.CBabInterfaceListComponentTester;

/**
 * Standalone Playwright test for BAB Dashboard Network Interface List component.
 * Tests the CComponentInterfaceList component that displays network interfaces
 * from Calimero server on the BAB Dashboard.
 * 
 * <p>Test Coverage:
 * <ul>
 *   <li>Component visibility and initialization</li>
 *   <li>Grid display with all columns</li>
 *   <li>Refresh button functionality</li>
 *   <li>Status column color coding</li>
 *   <li>Error handling when Calimero unavailable</li>
 * </ul>
 * 
 * <p>Prerequisites:
 * <ul>
 *   <li>BAB application ALREADY RUNNING on port 8080</li>
 *   <li>Calimero server running on port 8077</li>
 *   <li>BAB Dashboard Project created with Calimero connection configured</li>
 * </ul>
 * 
 * <p>This is a standalone test - it does NOT start Spring Boot application.
 * Run the BAB application separately before running this test:
 * <pre>mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=bab"</pre>
 */
@DisplayName("🌐 BAB Network Interface List Component Test")
public class CBabInterfaceListPlaywrightTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabInterfaceListPlaywrightTest.class);
	private static final int PORT = 8080;
	private static final String BASE_URL = "http://localhost:" + PORT;
	private static final String SYSTEM_SETTINGS_URL = BASE_URL + "/csystemsettingsview";
	private static final String CALIMERO_STATUS_SELECTOR = "#calimero-status-indicator";
	private static final String CALIMERO_RESTART_BUTTON_SELECTOR = "#cbutton-calimero-restart";
	
	private int screenshotCounter = 1;
	private final CBabInterfaceListComponentTester interfaceListTester = new CBabInterfaceListComponentTester();
	private Playwright playwright;
	private Browser browser;
	private BrowserContext context;
	private Page page;
	
	@BeforeEach
	void setUp() {
		// Initialize Playwright
		playwright = Playwright.create();
		browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
			.setHeadless(false)
			.setSlowMo(100));
		context = browser.newContext();
		page = context.newPage();
	}
	
	@AfterEach
	void tearDown() {
		if (page != null) page.close();
		if (context != null) context.close();
		if (browser != null) browser.close();
		if (playwright != null) playwright.close();
	}
	
	/**
	 * Test that the interface list component displays correctly on the BAB Dashboard.
	 * Verifies component structure, grid initialization, and data display.
	 */
	@Test
	@DisplayName("✅ BAB Dashboard Interface List - Component Display")
	void testInterfaceListComponentDisplay() {
		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			
			LOGGER.info("🚀 Starting BAB Dashboard Interface List test");
			
			// Step 1: Login to BAB application
			LOGGER.info("Step 1: Logging in to BAB application");
			loginToBabApplication();
			takeScreenshot("bab-login");
			wait_1000();
			
			// Step 2: Ensure Calimero service is running
			LOGGER.info("Step 2: Ensuring Calimero service is running");
			ensureCalimeroServiceRunning();
			
			// Step 3: Navigate to BAB Dashboard Projects page
			LOGGER.info("Step 3: Navigating to BAB Dashboard Projects");
			boolean navigated = navigateToBabDashboardProjects();
			assertTrue(navigated, "Failed to navigate to BAB Dashboard Projects page");
			takeScreenshot("bab-dashboard-list");
			wait_1000();
			
			// Step 4: Open first dashboard project
			LOGGER.info("Step 4: Opening first BAB Dashboard Project");
			clickFirstGridRow();
			wait_2000(); // Wait for page to load
			takeScreenshot("bab-dashboard-opened");
			
			// Step 5: Run component-level tests using the shared tester
			LOGGER.info("Step 5: Running network interface component tests");
			assertTrue(interfaceListTester.canTest(page), "Interface list component should be visible");
			takeScreenshot("component-visible");
			interfaceListTester.test(page);
			takeScreenshot("component-final");
			
			LOGGER.info("✅ BAB Dashboard Interface List test completed successfully");
			
		} catch (final Exception e) {
			LOGGER.error("❌ Test failed with exception: {}", e.getMessage(), e);
			try {
				takeScreenshot("test-failure");
			} catch (final Exception screenshotEx) {
				LOGGER.error("Failed to take failure screenshot", screenshotEx);
			}
			fail("Test failed: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Login to BAB application.
	 */
	private void loginToBabApplication() {
		page.navigate(BASE_URL);
		wait_1000();
		
		// Fill login form (adjust selectors based on actual login page)
		page.locator("input[name='username']").fill("admin");
		page.locator("input[name='company']").fill("1");
		page.locator("input[name='password']").fill("admin");
		page.locator("#cbutton-login").click();
		
		wait_2000();
	}

	/**
	 * Ensure the Calimero background service is running via the gateway settings view.
	 */
	private void ensureCalimeroServiceRunning() {
		try {
			page.navigate(SYSTEM_SETTINGS_URL);
			wait_1000();
			final Locator status = page.locator(CALIMERO_STATUS_SELECTOR);
			if (status.count() == 0) {
				LOGGER.warn("Calimero status indicator not found at {}", SYSTEM_SETTINGS_URL);
				return;
			}
			if (isServiceRunning(status)) {
				LOGGER.info("Calimero service already running");
				return;
			}
			final Locator restartButton = page.locator(CALIMERO_RESTART_BUTTON_SELECTOR);
			if (restartButton.count() == 0) {
				LOGGER.warn("Calimero restart button not available");
				return;
			}
			LOGGER.info("Restarting Calimero service via system settings view");
			restartButton.first().click();
			waitForServiceRunning(status);
		} catch (final Exception e) {
			LOGGER.error("Failed to ensure Calimero service is running", e);
		}
	}
	
	/**
	 * Click first row in grid.
	 */
	private void clickFirstGridRow() {
		Locator firstRow = page.locator("vaadin-grid-cell-content").first();
		firstRow.click();
		wait_1000();
	}

	private boolean isServiceRunning(final Locator status) {
		try {
			final String attr = status.getAttribute("data-running");
			if (attr != null) {
				return Boolean.parseBoolean(attr);
			}
			final String text = status.textContent();
			return text != null && text.toLowerCase().contains("running");
		} catch (final Exception e) {
			LOGGER.debug("Unable to determine Calimero service state: {}", e.getMessage());
			return false;
		}
	}

	private void waitForServiceRunning(final Locator status) {
		for (int attempt = 0; attempt < 20; attempt++) {
			if (isServiceRunning(status)) {
				LOGGER.info("Calimero service reported as running");
				return;
			}
			wait_500();
		}
		LOGGER.warn("Calimero service did not report running after restart attempt");
	}
	
	/**
	 * Navigate to BAB Dashboard Projects page using test auxiliary button.
	 * @return true if navigation successful
	 */
	private boolean navigateToBabDashboardProjects() {
		try {
			// Navigate to test auxiliary page
			page.navigate(BASE_URL + "/cpagetestauxillary");
			wait_500();
			
			// Find button for BAB Dashboard Projects - View 2
			// Button ID format: test-aux-btn-{sanitized-title}-{index}
			String buttonId = "test-aux-btn-bab-dashboard-projects-view-2-0";
			Locator button = page.locator("#" + buttonId);
			
			if (!button.isVisible()) {
				LOGGER.warn("Button #{} not visible, trying alternative selector", buttonId);
				// Try finding by text
				button = page.locator("vaadin-button")
					.filter(new Locator.FilterOptions().setHasText("BAB Dashboard Projects - View 2"))
					.first();
			}
			
			if (button.isVisible()) {
				button.click();
				wait_1000();
				return true;
			}
			
			LOGGER.error("Could not find BAB Dashboard Projects button");
			return false;
			
		} catch (final Exception e) {
			LOGGER.error("Failed to navigate to BAB Dashboard Projects: {}", e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * Take screenshot and save to target/screenshots.
	 */
	private void takeScreenshot(String name) {
		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			page.screenshot(new Page.ScreenshotOptions()
				.setPath(Paths.get("target/screenshots/" + name + ".png")));
			LOGGER.debug("Screenshot saved: {}", name);
		} catch (Exception e) {
			LOGGER.error("Failed to take screenshot: {}", e.getMessage(), e);
		}
	}
	
	/**
	/**
	 * Wait helper - 500ms.
	 */
	private void wait_500() {
		try {
			Thread.sleep(500);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * Wait helper - 1000ms.
	 */
	private void wait_1000() {
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * Wait helper - 2000ms.
	 */
	private void wait_2000() {
		try {
			Thread.sleep(2000);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
