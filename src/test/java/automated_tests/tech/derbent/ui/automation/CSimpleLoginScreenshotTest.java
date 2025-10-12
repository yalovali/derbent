package automated_tests.tech.derbent.ui.automation;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

/** Simple Playwright test that demonstrates headless browser operation, login, and screenshot capture.
 * This test focuses on core functionality without requiring full navigation menu to be present. */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080"
})
@DisplayName ("📸 Simple Login Screenshot Test")
public class CSimpleLoginScreenshotTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSimpleLoginScreenshotTest.class);

	@Test
	@DisplayName ("✅ Login and capture screenshots")
	void loginAndCaptureScreenshots() {
		LOGGER.info("📸 Starting simple login screenshot test...");
		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			
			// Capture initial login page
			LOGGER.info("📸 Capturing login page screenshot...");
			takeScreenshot("01-login-page", true);
			
			// Fill in login credentials
			LOGGER.info("🔐 Filling in login credentials...");
			// Use the login method that doesn't try to prime navigation
			ensureLoginViewLoaded();
			if (fillLoginField("vaadin-login-form", "#vaadin-login-username", "username field", "admin", "input[name='username']") &&
					fillLoginField("vaadin-login-form", "#vaadin-login-password", "password field", "test123", "input[name='password']")) {
				takeScreenshot("02-credentials-entered", true);
			}
			
			// Click login button
			LOGGER.info("🔘 Clicking login button...");
			clickLoginButton();
			
			// Wait for page to load after login
			LOGGER.info("⏳ Waiting for post-login page to load...");
			page.waitForTimeout(3000); // Give time for the page to fully load
			
			// Capture post-login state
			LOGGER.info("📸 Capturing post-login page...");
			takeScreenshot("03-post-login-page", true);
			
			// Check if we're still on login page or redirected
			String currentUrl = page.url();
			LOGGER.info("📍 Current URL: {}", currentUrl);
			takeScreenshot("04-final-state", true);
			
			// Try to capture any visible content
			LOGGER.info("📝 Page title: {}", page.title());
			
			LOGGER.info("✅ Login screenshot test completed successfully");
		} catch (Exception e) {
			LOGGER.error("❌ Login screenshot test failed.");
			takeScreenshot("error-state", true);
			throw new AssertionError("Login screenshot test failed", e);
		}
	}
}
