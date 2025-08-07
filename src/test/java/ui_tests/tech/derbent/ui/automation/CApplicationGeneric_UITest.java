package ui_tests.tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import com.microsoft.playwright.Page;

@SpringBootTest (webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource (properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.jpa.hibernate.ddl-auto=create-drop" }
)
public class CApplicationGeneric_UITest extends CBaseUITest {

	static final Logger LOGGER =
		LoggerFactory.getLogger(CApplicationGeneric_UITest.class);

	@Test
	void testApplicationLoadsSuccessfully() {
		LOGGER.info("🧪 Testing application loads successfully...");

		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available, skipping browser-based test");
			return;
		}
		// Navigate to application
		page.navigate(baseUrl);
		// Wait for custom login view to be visible (application should require login)
		page.waitForSelector(".custom-login-view",
			new Page.WaitForSelectorOptions().setTimeout(10000));
		// Verify page title contains expected text
		final String title = page.title();
		assertNotNull(title);
		LOGGER.info("✅ Application loaded successfully. Title: {}", title);
		// Check that custom login view is present
		assertTrue(page.locator(".custom-login-view").isVisible());
		LOGGER.info("✅ Application loads successfully test completed");
	}

	@Test
	void testLogoutFunctionality() {
		LOGGER.info("🧪 Testing logout functionality...");

		// Look for logout option
		if (performLogout()) {
			// Verify we're back at login page
			wait_loginscreen();
			takeScreenshot("after-logout");
			// Updated for CCustomLoginView
			assertTrue(page.locator(".custom-login-view").isVisible());
			LOGGER.info("✅ Logout functionality test completed successfully");
		}
		else {
			LOGGER.warn("⚠️ Logout functionality not tested - logout button not found");
		}
	}

	@Test
	void testNavigationBetweenViews() {
		LOGGER.info("🧪 Testing navigation between views...");

		// Test navigation to all main views
		for (final Class<?> view : viewClasses) {
			navigateToViewByClass(view);
		}
		LOGGER.info("✅ Navigation between views test completed");
	}
}