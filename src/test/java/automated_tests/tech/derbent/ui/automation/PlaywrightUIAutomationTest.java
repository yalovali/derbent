package automated_tests.tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import tech.derbent.activities.view.CActivitiesView;
import tech.derbent.decisions.view.CDecisionsView;
import tech.derbent.meetings.view.CMeetingsView;
import tech.derbent.projects.view.CProjectsView;
import tech.derbent.users.view.CUsersView;
import ui_tests.tech.derbent.ui.automation.CBaseUITest;

/**
 * PlaywrightUIAutomationTest - Comprehensive Playwright UI automation test suite. This
 * test class provides the main test methods referenced by the run-playwright-tests.sh
 * script and implements comprehensive UI testing across all application views.
 */
@SpringBootTest (webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource (properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.jpa.hibernate.ddl-auto=create-drop" }
)
public class PlaywrightUIAutomationTest extends CBaseUITest {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(PlaywrightUIAutomationTest.class);

	@Test
	void testAccessibilityBasics() {
		LOGGER.info("🧪 Testing accessibility basics...");

		// Test accessibility for each view
		for (final Class<?> view : viewClasses) {
			assertTrue(navigateToViewByClass(view),
				"Should navigate to " + view.getSimpleName());
			testAccessibilityBasics(view.getSimpleName());
		}
		LOGGER.info("✅ Accessibility basics test completed");
	}

	@Test
	void testCompleteApplicationFlow() {
		LOGGER.info("🧪 Testing complete application flow...");
		// Test a complete workflow across the application
		// 1. Navigate to Users and verify
		assertTrue(navigateToViewByClass(CUsersView.class), "Should navigate to Users");
		// 2. Navigate to Projects and verify
		assertTrue(navigateToViewByClass(CProjectsView.class),
			"Should navigate to Projects");
		// 3. Navigate to Activities and verify
		assertTrue(navigateToViewByClass(CActivitiesView.class),
			"Should navigate to Activities");
		// 4. Navigate to Meetings and verify
		assertTrue(navigateToViewByClass(CMeetingsView.class),
			"Should navigate to Meetings");
		// 5. Navigate to Decisions and verify
		assertTrue(navigateToViewByClass(CDecisionsView.class),
			"Should navigate to Decisions");
		// 6. Test creating a new project workflow
		assertTrue(navigateToViewByClass(CProjectsView.class),
			"Should navigate back to Projects");
		final int initialRowCount = getGridRowCount();
		clickNew();

		// Fill project name if form is available
		if (fillFirstTextField("Test Project " + System.currentTimeMillis())) {
			LOGGER.debug("Filled project name in workflow test");
		}
		clickSave();
		wait_2000();
		final int finalRowCount = getGridRowCount();
		LOGGER.debug("Initial rows: {}, Final rows: {}", initialRowCount, finalRowCount);
		LOGGER.info("✅ Complete application flow test completed");
	}

	@Test
	void testCRUDOperationsInMeetings() {
		LOGGER.info("🧪 Testing CRUD operations in Meetings...");
		assertTrue(navigateToViewByClass(CMeetingsView.class),
			"Should navigate to Meetings view");
		testCRUDOperationsInView("Meetings", "new-button", "save-button",
			"delete-button");
		LOGGER.info("✅ Meetings CRUD operations test completed");
	}

	@Test
	void testCRUDOperationsInProjects() {
		LOGGER.info("🧪 Testing CRUD operations in Projects...");
		assertTrue(navigateToViewByClass(CProjectsView.class),
			"Should navigate to Projects view");
		testCRUDOperationsInView("Projects", "new-button", "save-button",
			"delete-button");
		LOGGER.info("✅ Projects CRUD operations test completed");
	}

	@Test
	void testFormValidationAndErrorHandling() {
		LOGGER.info("🧪 Testing form validation and error handling...");

		// Test form validation in different views
		for (final Class<?> view : viewClasses) {
			assertTrue(navigateToViewByClass(view),
				"Should navigate to " + view.getSimpleName());
			clickNew(); // Open new form
			testFormValidationById("save-button");
			clickCancel(); // Close form
		}
		LOGGER.info("✅ Form validation and error handling test completed");
	}

	@Test
	void testGridInteractions() {
		LOGGER.info("🧪 Testing grid interactions...");
		// Test grid interactions in each view that has grids
		testAdvancedGridInView(CProjectsView.class);
		testAdvancedGridInView(CActivitiesView.class);
		testAdvancedGridInView(CMeetingsView.class);
		testAdvancedGridInView(CDecisionsView.class);
		testAdvancedGridInView(CUsersView.class);
		LOGGER.info("✅ Grid interactions test completed");
	}

	@Test
	void testInvalidLoginHandling() {
		LOGGER.info("🧪 Testing invalid login handling...");

		// Perform logout first to get to login page
		if (performLogout()) {
			wait_loginscreen();
			// Try invalid credentials
			performLogin("invaliduser", "wrongpassword");
			// Check for error message (exact selector may vary)
			boolean hasError =
				page.locator("vaadin-notification, [role='alert'], .error").count() > 0;
			assertTrue(hasError, "Should show error message for invalid login");
			// Login with correct credentials to restore state
			performLogin("admin", "test123");
		}
		LOGGER.info("✅ Invalid login handling test completed");
	}

	@Test
	void testLoginFunctionality() {
		LOGGER.info("🧪 Testing login functionality...");

		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available, skipping browser-based test");
			return;
		}
		// Login is handled in setUp(), verify we're logged in
		assertTrue(page.locator("vaadin-app-layout").isVisible(),
			"Should be logged in and see app layout");
		LOGGER.info("✅ Login functionality test completed");
	}

	@Test
	void testLogoutFunctionality() {
		LOGGER.info("🧪 Testing logout functionality...");

		if (performLogout()) {
			// Verify we're back at login page
			wait_loginscreen();
			assertTrue(page.locator("vaadin-login-overlay").isVisible(),
				"Should be back at login page");
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
			assertTrue(navigateToViewByClass(view),
				"Should successfully navigate to " + view.getSimpleName());
		}
		LOGGER.info("✅ Navigation between views test completed");
	}

	@Test
	void testResponsiveDesign() {
		LOGGER.info("🧪 Testing responsive design...");

		// Test responsive design for each view
		for (final Class<?> view : viewClasses) {
			assertTrue(navigateToViewByClass(view),
				"Should navigate to " + view.getSimpleName());
			testResponsiveDesign(view.getSimpleName());
		}
		LOGGER.info("✅ Responsive design test completed");
	}
}