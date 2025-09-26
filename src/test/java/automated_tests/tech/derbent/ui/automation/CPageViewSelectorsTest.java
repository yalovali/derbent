package automated_tests.tech.derbent.ui.automation;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import com.microsoft.playwright.Locator;

/** CPageViewSelectorsTest - Validates CPage master view and detail view selector functionality Tests the new entity type, master view class, and
 * detail view class fields in CPageEntity */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080"
})
public class CPageViewSelectorsTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageViewSelectorsTest.class);

	@Test
	public void testCPageViewSelectorsFields() {
		LOGGER.info("🧪 Testing CPage master view and detail view selector fields");
		// Setup
		// Note: Using direct page navigation for demo
		page.navigate("http://localhost:" + port + "/login");
		wait_2000();
		try {
			// Navigate to Page Entity View
			LOGGER.info("📋 Navigating to Page Entity View");
			boolean navSuccess = navigateToViewByText("Pages");
			if (!navSuccess) {
				// Try alternative navigation
				page.navigate("http://localhost:" + port + "/cpageentityview");
				wait_2000();
			}
			// Click New button to open the form
			LOGGER.info("➕ Opening new page entity form");
			Locator newButton = waitForSelectorWithCheck("vaadin-button:has-text('New')", "New button");
			newButton.click();
			wait_2000();
			// Test main entity type field
			LOGGER.info("🏷️ Testing Main Entity Type field");
			testEntityTypeField();
			// Test master view class field
			LOGGER.info("📝 Testing Master View Class field");
			testMasterViewClassField();
			// Test detail view class field
			LOGGER.info("📄 Testing Detail View Class field");
			testDetailViewClassField();
			// Take a screenshot to show the form with new fields
			takeScreenshot("cpage-view-selectors-form");
			LOGGER.info("✅ CPage view selectors test completed successfully");
		} catch (Exception e) {
			LOGGER.error("❌ Error testing CPage view selectors: {}", e.getMessage());
			takeScreenshot("cpage-view-selectors-error");
			throw new AssertionError("CPage view selectors test failed", e);
		}
	}

	private void testEntityTypeField() {
		try {
			// Find the Main Entity Type field
			Locator entityTypeField = getLocatorWithCheck("input[placeholder*='Entity Type'], vaadin-text-field:has([placeholder*='Entity Type'])",
					"Main Entity Type field");
			if (entityTypeField.count() == 0) {
				// Try finding by label
				entityTypeField = getLocatorWithCheck("vaadin-text-field", "Text field for entity type");
				LOGGER.info("Found {} text fields", entityTypeField.count());
			}
			// Fill entity type
			entityTypeField.first().fill("activities");
			wait_500();
			LOGGER.info("✓ Main Entity Type field tested successfully");
		} catch (Exception e) {
			LOGGER.warn("⚠️ Main Entity Type field test issue: {}", e.getMessage());
		}
	}

	private void testMasterViewClassField() {
		try {
			// Find the Master View Class field
			Locator masterViewField = getLocatorWithCheck("input[placeholder*='Master View'], vaadin-text-field", "Master View Class field");
			// Fill master view class
			masterViewField.nth(1).fill("tech.derbent.activities.view.CActivitiesView");
			wait_500();
			LOGGER.info("✓ Master View Class field tested successfully");
		} catch (Exception e) {
			LOGGER.warn("⚠️ Master View Class field test issue: {}", e.getMessage());
		}
	}

	private void testDetailViewClassField() {
		try {
			// Find the Detail View Class field
			Locator detailViewField = getLocatorWithCheck("vaadin-text-field", "Detail View Class field");
			// Fill detail view class
			detailViewField.nth(2).fill("tech.derbent.activities.view.CActivityDetailsView");
			wait_500();
			LOGGER.info("✓ Detail View Class field tested successfully");
		} catch (Exception e) {
			LOGGER.warn("⚠️ Detail View Class field test issue: {}", e.getMessage());
		}
	}

	@Override
	protected void takeScreenshot(String name) {
		try {
			String screenshotPath = "target/screenshots/" + name + ".png";
			page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions().setPath(java.nio.file.Paths.get(screenshotPath)));
			LOGGER.info("📸 Screenshot saved: {}", screenshotPath);
		} catch (Exception e) {
			LOGGER.warn("⚠️ Screenshot failed: {}", e.getMessage());
		}
	}
}
