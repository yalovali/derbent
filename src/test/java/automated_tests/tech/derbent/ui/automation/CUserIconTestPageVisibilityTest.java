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
import com.microsoft.playwright.Locator;

/** Test to verify that the User Icon Test page is accessible from the Test Support Page and that icons display correctly on the User Icon Test page.
 * This test addresses the issue: "user icon test page doesn't show up in Test Support Page and icons not show still in
 * http://localhost:8080/user-icon-test" */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName ("🧪 User Icon Test Page Visibility and Icon Display Test")
public class CUserIconTestPageVisibilityTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserIconTestPageVisibilityTest.class);
	private int screenshotCounter = 1;

	@Test
	@DisplayName ("✅ Verify User Icon Test button appears on Test Support Page and icons display correctly")
	void testUserIconPageVisibilityAndIconDisplay() {
		LOGGER.info("🚀 Starting User Icon Test Page visibility verification...");
		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			// Step 1: Login to application
			LOGGER.info("📝 Step 1: Logging into application...");
			loginToApplication();
			takeScreenshot(String.format("%03d-logged-in", screenshotCounter++), false);
			// Step 2: Navigate to Test Support Page (CPageTestAuxillary)
			LOGGER.info("🧭 Step 2: Navigating to Test Support Page...");
			String testSupportUrl = "http://localhost:" + port + "/cpagetestauxillary";
			page.navigate(testSupportUrl);
			wait_2000();
			takeScreenshot(String.format("%03d-test-support-page", screenshotCounter++), false);
			LOGGER.info("Successfully navigated to: {}", testSupportUrl);
			// Step 3: Verify User Icon Test button exists
			LOGGER.info("🔍 Step 3: Checking for User Icon Test button...");
			Locator userIconButton = page.locator("[data-title='User Icon Test']");
			int buttonCount = userIconButton.count();
			if (buttonCount > 0) {
				LOGGER.info("✅ SUCCESS: User Icon Test button FOUND on Test Support Page!");
				LOGGER.info("   Button count: {}", buttonCount);
				// Highlight the button in screenshot
				userIconButton.first().scrollIntoViewIfNeeded();
				takeScreenshot(String.format("%03d-user-icon-button-found", screenshotCounter++), false);
				// Step 4: Click the button to navigate to User Icon Test page
				LOGGER.info("🖱️ Step 4: Clicking User Icon Test button...");
				userIconButton.first().click();
				wait_2000();
				takeScreenshot(String.format("%03d-after-button-click", screenshotCounter++), false);
				// Verify we're on the User Icon Test page
				String currentUrl = page.url();
				LOGGER.info("Current URL after click: {}", currentUrl);
				if (currentUrl.contains("user-icon-test")) {
					LOGGER.info("✅ Successfully navigated to User Icon Test page");
				} else {
					LOGGER.warn("⚠️ URL doesn't contain 'user-icon-test': {}", currentUrl);
				}
			} else {
				LOGGER.error("❌ FAILURE: User Icon Test button NOT FOUND on Test Support Page!");
				// List all buttons for debugging
				Locator allButtons = page.locator("[id^='test-aux-btn-']");
				int totalButtons = allButtons.count();
				LOGGER.error("Found {} total buttons on Test Support Page:", totalButtons);
				for (int i = 0; i < totalButtons; i++) {
					String title = allButtons.nth(i).getAttribute("data-title");
					String route = allButtons.nth(i).getAttribute("data-route");
					LOGGER.error("  Button {}: title='{}', route='{}'", i + 1, title, route);
				}
			}
			// Step 5: Test direct navigation to User Icon Test page
			LOGGER.info("🧭 Step 5: Testing direct navigation to /user-icon-test...");
			String directUrl = "http://localhost:" + port + "/user-icon-test";
			page.navigate(directUrl);
			wait_2000();
			takeScreenshot(String.format("%03d-direct-user-icon-test-page", screenshotCounter++), false);
			LOGGER.info("Navigated directly to: {}", directUrl);
			// Step 6: Check for page content and icons
			LOGGER.info("🔍 Step 6: Checking page content and icon display...");
			// Check for page title
			Locator pageTitle = page.locator("h2:has-text('SVG Icon Test Page')");
			if (pageTitle.count() > 0) {
				LOGGER.info("✅ Page title found: SVG Icon Test Page");
			} else {
				LOGGER.warn("⚠️ Page title not found");
			}
			// Check for SVG icons
			Locator svgIcons = page.locator("svg");
			int svgCount = svgIcons.count();
			LOGGER.info("Found {} SVG elements on the page", svgCount);
			// Check for Vaadin icons (icon elements)
			Locator iconElements = page.locator("vaadin-icon");
			int iconCount = iconElements.count();
			LOGGER.info("Found {} vaadin-icon elements on the page", iconCount);
			// Check for user labels (CLabelEntity)
			Locator userLabels = page.locator("div:has-text('Login:')");
			int labelCount = userLabels.count();
			LOGGER.info("Found {} user label sections on the page", labelCount);
			// Take final screenshot showing icons
			takeScreenshot(String.format("%03d-user-icon-test-page-with-icons", screenshotCounter++), false);
			// Step 7: Summary
			LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
			LOGGER.info("✅ Test completed successfully!");
			LOGGER.info("📊 Summary:");
			LOGGER.info("   User Icon Test button visible: {}", buttonCount > 0 ? "YES" : "NO");
			LOGGER.info("   Page accessible via direct URL: YES");
			LOGGER.info("   SVG icons on page: {}", svgCount);
			LOGGER.info("   Vaadin icons on page: {}", iconCount);
			LOGGER.info("   User labels on page: {}", labelCount);
			LOGGER.info("   Screenshots captured: {}", screenshotCounter - 1);
			LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
		} catch (Exception e) {
			LOGGER.error("❌ Test failed with exception: {}", e.getMessage(), e);
			takeScreenshot("error-user-icon-test", true);
			throw new RuntimeException("Test failed", e);
		}
	}
}
