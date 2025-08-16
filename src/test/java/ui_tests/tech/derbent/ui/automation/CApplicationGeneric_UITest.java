package ui_tests.tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CApplicationGeneric_UITest extends CBaseUITest {

	static final Logger LOGGER =
		LoggerFactory.getLogger(CApplicationGeneric_UITest.class);

	@Test
	void testLogoutFunctionality() {
		LOGGER.info("🧪 Testing logout functionality...");
		// Look for logout option
		performLogout();
		// Verify we're back at login page
		wait_loginscreen();
		// Updated for CCustomLoginView
		assertTrue(page.locator(".custom-login-view").isVisible());
		LOGGER.info("✅ Logout functionality test completed successfully");
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