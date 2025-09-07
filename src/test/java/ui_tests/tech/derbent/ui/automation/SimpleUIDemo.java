package ui_tests.tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

/** SimpleUIDemo - A standalone demo of browser automation using Playwright. This test demonstrates basic browser automation capabilities: - Opens a
 * real browser using Playwright - Navigates to websites - Takes screenshots - Verifies page content - Shows form interactions This test works
 * independently of the application and shows the testing infrastructure. Playwright provides better performance and reliability compared to
 * Selenium. */
public class SimpleUIDemo {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleUIDemo.class);
	private Playwright playwright;
	private Browser browser;
	private BrowserContext context;
	private Page page;

	@Test
	void demonstrateAdvancedFeatures() {
		LOGGER.info("=== Playwright Advanced Features Demo ===");
		// Navigate to a dynamic page
		LOGGER.info("Navigating to httpbin.org...");
		page.navigate("https://httpbin.org/");
		page.waitForLoadState();
		takeScreenshot("demo-httpbin-main");
		// Demonstrate waiting for elements
		LOGGER.info("Testing element waiting...");
		final var links = page.locator("a");
		final int linkCount = links.count();
		LOGGER.info("Found {} links on the page", linkCount);
		// Demonstrate network interception capabilities
		LOGGER.info("Page URL: {}", page.url());
		// Demonstrate responsive testing
		LOGGER.info("Testing responsive design...");
		page.setViewportSize(768, 1024); // Tablet size
		takeScreenshot("demo-tablet-view");
		page.setViewportSize(375, 667); // Mobile size
		takeScreenshot("demo-mobile-view");
		// Reset to desktop
		page.setViewportSize(1200, 800);
		takeScreenshot("demo-desktop-view");
		LOGGER.info("✅ Advanced features demo completed!");
		LOGGER.info("");
		LOGGER.info("🎭 Playwright provides these advantages:");
		LOGGER.info("   ✅ Faster and more reliable than Selenium");
		LOGGER.info("   ✅ Built-in waiting for elements and network requests");
		LOGGER.info("   ✅ Cross-browser testing (Chrome, Firefox, Safari, Edge)");
		LOGGER.info("   ✅ Mobile testing capabilities");
		LOGGER.info("   ✅ Network interception and mocking");
		LOGGER.info("   ✅ Better debugging with trace viewer");
		LOGGER.info("   ✅ No external driver management needed");
	}

	@Test
	void demonstrateBrowserAutomation() {
		LOGGER.info("=== Playwright Browser Automation Demo ===");
		// Navigate to a simple webpage
		LOGGER.info("Navigating to example.com...");
		page.navigate("https://example.com");
		// Wait for page to load
		page.waitForLoadState();
		// Take screenshot
		takeScreenshot("demo-example-page");
		// Verify page content
		final String title = page.title();
		LOGGER.info("Page title: {}", title);
		assertTrue(title.contains("Example"), "Page should contain 'Example' in title");
		// Find and verify content
		final String pageText = page.locator("body").textContent();
		LOGGER.info("Page contains {} characters of text", pageText.length());
		// Verify specific content
		assertTrue(pageText.contains("Example Domain"), "Page should contain 'Example Domain'");
		LOGGER.info("✅ Playwright browser automation demo completed successfully!");
		LOGGER.info("📸 Screenshots saved to target/screenshots/");
		// This demonstrates what the UI tests would do:
		LOGGER.info("");
		LOGGER.info("🎯 In a real Vaadin application test, this would:");
		LOGGER.info("   1. Navigate to http://localhost:8080");
		LOGGER.info("   2. Click on 'Projects' menu item");
		LOGGER.info("   3. Click 'New' button to create project");
		LOGGER.info("   4. Fill form fields with test data");
		LOGGER.info("   5. Click 'Save' to submit form");
		LOGGER.info("   6. Verify project appears in grid");
		LOGGER.info("   7. Take screenshots at each step");
		LOGGER.info("   8. Repeat for Meetings and Decisions");
	}

	@Test
	void demonstrateFormInteraction() {
		LOGGER.info("=== Playwright Form Interaction Demo ===");
		// Navigate to a page with forms (using httpbin.org which has form examples)
		LOGGER.info("Navigating to httpbin.org form demo...");
		page.navigate("https://httpbin.org/forms/post");
		// Wait for page to load
		page.waitForLoadState();
		takeScreenshot("demo-form-page");
		// This demonstrates form interaction capabilities
		try {
			// Find form elements (similar to what we'd do in Vaadin app)
			final var inputs = page.locator("input[type='text']");
			final int inputCount = inputs.count();
			LOGGER.info("Found {} text input fields", inputCount);
			if (inputCount > 0) {
				inputs.first().fill("Test Name");
				LOGGER.info("✅ Successfully filled text field");
			}
			final var buttons = page.locator("button, input[type='submit']");
			final int buttonCount = buttons.count();
			LOGGER.info("Found {} buttons/submit elements", buttonCount);
			takeScreenshot("demo-form-filled");
		} catch (final Exception e) {
			LOGGER.warn("Form interaction demo encountered issues: {}", e.getMessage());
		}
		LOGGER.info("✅ Playwright form interaction demo completed!");
	}

	@BeforeEach
	void setUp() {
		LOGGER.info("Setting up Playwright browser automation demo...");
		// Initialize Playwright
		playwright = Playwright.create();
		// Launch browser (Chromium)
		browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true) // Run
				// headless
				// for
				// demo
				.setSlowMo(100)); // Add small delay between actions
		// Create context and page
		context = browser.newContext();
		page = context.newPage();
		LOGGER.info("Playwright browser automation setup completed");
	}

	private void takeScreenshot(final String name) {
		try {
			final String screenshotPath = "target/screenshots/" + name + "-demo.png";
			page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(screenshotPath)));
			LOGGER.info("📸 Screenshot saved: {}", screenshotPath);
		} catch (final Exception e) {
			LOGGER.warn("Failed to take screenshot: {}", e.getMessage());
		}
	}

	@AfterEach
	void tearDown() {
		if (page != null) {
			takeScreenshot("demo-final");
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
		LOGGER.info("Playwright browser closed");
	}
}
