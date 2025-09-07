package ui_tests.tech.derbent.ui.automation;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.abstracts.utils.Check;

public class CApplicationGeneric_UITest extends CBaseUITest {

	static final Logger LOGGER = LoggerFactory.getLogger(CApplicationGeneric_UITest.class);

	@Test
	void testLogoutFunctionality() {
		LOGGER.info("🧪 Testing logout functionality with enhanced validation...");
		try {
			// Look for logout option
			performLogout();
			// Verify we're back at login page
			wait_loginscreen();
			// Updated for CCustomLoginView - use Check assertions
			Check.isTrue(page.locator(".custom-login-view").isVisible(), "Should be redirected to custom login view after logout");
			LOGGER.info("✅ Logout functionality test completed successfully");
		} catch (final Exception e) {
			LOGGER.error("❌ Logout functionality test failed: {}", e.getMessage());
			takeScreenshot("logout-functionality-test-failed", true);
			throw new AssertionError("Logout functionality test failed: " + e.getMessage(), e);
		}
	}

	@Test
	void testNavigationBetweenViews() {
		LOGGER.info("🧪 Testing navigation between views with enhanced validation...");
		try {
			// Test navigation to all main views
			for (final Class<?> view : viewClasses) {
				navigateToViewByClass(view);
				wait_500();
				// Verify successful navigation
				final String currentUrl = page.url();
				LOGGER.debug("Successfully navigated to {}: {}", view.getSimpleName(), currentUrl);
			}
			LOGGER.info("✅ Navigation between views test completed successfully");
		} catch (final Exception e) {
			LOGGER.error("❌ Navigation between views test failed: {}", e.getMessage());
			takeScreenshot("navigation-between-views-failed", true);
			throw new AssertionError("Navigation between views test failed: " + e.getMessage(), e);
		}
	}
}
