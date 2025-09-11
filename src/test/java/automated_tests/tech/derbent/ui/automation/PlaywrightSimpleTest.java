package automated_tests.tech.derbent.ui.automation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

/** Simple Playwright test to demonstrate screenshot functionality This test uses a minimal setup to avoid browser download issues */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.url=jdbc:h2:mem:testdb", "spring.jpa.hibernate.ddl-auto=create-drop",
		"server.port=8080"
})
public class PlaywrightSimpleTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(PlaywrightSimpleTest.class);

	@Test
	void testBasicScreenshotFunctionality() {
		LOGGER.info("🧪 Testing basic screenshot functionality...");
		try {
			Files.createDirectories(java.nio.file.Paths.get("target/screenshots"));
			final Playwright playwright = Playwright.create();
			final BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(true);
			// Check if Chromium browser is available in the system path
			if (Paths.get("/usr/bin/chromium-browser").toFile().exists()) {
				options.setExecutablePath(java.nio.file.Paths.get("/usr/bin/chromium-browser"));
				LOGGER.info("Using system Chromium browser for Playwright tests");
			} else if (Paths.get("/usr/bin/google-chrome").toFile().exists()) {
				options.setExecutablePath(java.nio.file.Paths.get("/usr/bin/google-chrome"));
				LOGGER.info("Using system google chrome browser for Playwright tests");
			} else {
				LOGGER.warn("No compatible browser found in system path, using default Chromium");
			}
			final Browser browser = playwright.chromium().launch(options);
			final BrowserContext context = browser.newContext();
			final Page page = context.newPage();
			// Navigate to a simple page
			page.navigate("data:text/html,<html><body><h1>Playwright Test</h1><p>Screenshot test successful!</p></body></html>");
			// Take a screenshot
			final String screenshotPath = "target/screenshots/test-screenshot-" + System.currentTimeMillis() + ".png";
			page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(screenshotPath)));
			LOGGER.info("📸 Screenshot saved: {}", screenshotPath);
			LOGGER.info("✅ Basic screenshot functionality test completed");
			context.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		// Initialize Playwright with system browser
	}

	@Test
	void testScreenshotDirectoryCreation() {
		LOGGER.info("🧪 Testing screenshot directory creation...");
		try {
			// Create screenshots directory
			final java.nio.file.Path screenshotDir = java.nio.file.Paths.get("target/screenshots");
			java.nio.file.Files.createDirectories(screenshotDir);
			// Verify directory exists
			if (java.nio.file.Files.exists(screenshotDir)) {
				LOGGER.info("📁 Screenshots directory created successfully: {}", screenshotDir);
			} else {
				LOGGER.warn("❌ Failed to create screenshots directory");
			}
			// Create a test file to verify write permissions
			final java.nio.file.Path testFile = screenshotDir.resolve("test-file.txt");
			java.nio.file.Files.write(testFile, "Test content".getBytes());
			if (java.nio.file.Files.exists(testFile)) {
				LOGGER.info("✅ Screenshot directory is writable");
				java.nio.file.Files.delete(testFile);
			}
			LOGGER.info("✅ Screenshot directory creation test completed");
		} catch (final Exception e) {
			LOGGER.error("Failed screenshot directory test: {}", e.getMessage());
		}
	}
}
