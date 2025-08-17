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
@SpringBootTest (
	webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class
)
@TestPropertySource (properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa",
	"spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver",
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080" }
)
public class PlaywrightUIAutomationTest extends CBaseUITest {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(PlaywrightUIAutomationTest.class);

	@Test
	void testAccessibilityBasics() {
		LOGGER.info("🧪 Testing accessibility basics for all views...");

		// Test accessibility for ALL views now, not just the basic ones
		for (final Class<?> view : allViewClasses) {
			navigateToViewByClass(view);
			// Take screenshot of each view for documentation/verification
			takeScreenshot("accessibility-" + view.getSimpleName().toLowerCase(), false);
			testAccessibilityBasics(view.getSimpleName());
		}
		LOGGER.info("✅ Accessibility basics test completed for {} views",
			allViewClasses.length);
	}

	@Test
	void testAllAdministrativeViews() {
		LOGGER.info("🧪 Testing all administrative views...");

		for (final Class<?> view : adminViewClasses) {
			LOGGER.info("Testing admin view: {}", view.getSimpleName());
			navigateToViewByClass(view);
			takeScreenshot("admin-" + view.getSimpleName().toLowerCase(), false);
		}
		LOGGER.info("✅ All administrative views test completed for {} views",
			adminViewClasses.length);
	}

	@Test
	void testAllKanbanViews() {
		LOGGER.info("🧪 Testing all Kanban board views...");

		for (final Class<?> view : kanbanViewClasses) {
			LOGGER.info("Testing kanban view: {}", view.getSimpleName());
			navigateToViewByClass(view);
			takeScreenshot("kanban-" + view.getSimpleName().toLowerCase(), false);
		}
		LOGGER.info("✅ All Kanban views test completed for {} views",
			kanbanViewClasses.length);
	}

	@Test
	void testAllMainBusinessViews() {
		LOGGER.info("🧪 Testing all main business views...");

		for (final Class<?> view : mainViewClasses) {
			LOGGER.info("Testing main view: {}", view.getSimpleName());
			navigateToViewByClass(view);
			takeScreenshot("main-business-" + view.getSimpleName().toLowerCase(), false);
		}
		LOGGER.info("✅ All main business views test completed for {} views",
			mainViewClasses.length);
	}

	@Test
	void testAllStatusAndTypeViews() {
		LOGGER.info("🧪 Testing all status and type views...");

		for (final Class<?> view : statusAndTypeViewClasses) {
			LOGGER.info("Testing status/type view: {}", view.getSimpleName());
			navigateToViewByClass(view);
			takeScreenshot("status-type-" + view.getSimpleName().toLowerCase(), false);
		}
		LOGGER.info("✅ All status and type views test completed for {} views",
			statusAndTypeViewClasses.length);
	}

	@Test
	void testCompleteApplicationFlow() {
		LOGGER.info("🧪 Testing complete application flow...");
		// Test a complete workflow across the application 1. Navigate to Users and verify
		navigateToViewByClass(CUsersView.class);
		navigateToViewByClass(CProjectsView.class);
		navigateToViewByClass(CActivitiesView.class);
		navigateToViewByClass(CMeetingsView.class);
		navigateToViewByClass(CDecisionsView.class);
		navigateToViewByClass(CProjectsView.class);
		clickNew();
		takeScreenshot("workflow-new-project-form", false);

		// Fill project name if form is available
		if (fillFirstTextField("Test Project " + System.currentTimeMillis())) {
			LOGGER.debug("Filled project name in workflow test");
			takeScreenshot("workflow-project-form-filled", false);
		}
		clickSave();
		wait_2000();
		takeScreenshot("workflow-after-save", false);
		getGridRowCount();
		LOGGER.info("✅ Complete application flow test completed");
	}

	@Test
	void testComprehensiveAllViewsNavigation() {
		LOGGER.info("🧪 Testing navigation to ALL views ({} total)...",
			allViewClasses.length);
		int successCount = 0;

		for (final Class<?> view : allViewClasses) {

			try {
				LOGGER.debug("Navigating to view: {}", view.getSimpleName());
				navigateToViewByClass(view);
				takeScreenshot("all-views-nav-" + view.getSimpleName().toLowerCase(),
					false);
				successCount++;
			} catch (final Exception e) {
				LOGGER.warn("⚠️ Failed to navigate to view {}: {}", view.getSimpleName(),
					e.getMessage());
			}
		}
		LOGGER.info("✅ Comprehensive navigation test completed: {}/{} views accessed",
			successCount, allViewClasses.length);
	}

	@Test
	void testCRUDOperationsInMeetings() {
		LOGGER.info("🧪 Testing CRUD operations in Meetings...");
		navigateToViewByClass(CMeetingsView.class);
		testCRUDOperationsInView("Meetings", "new-button", "save-button",
			"delete-button");
		LOGGER.info("✅ Meetings CRUD operations test completed");
	}

	@Test
	void testCRUDOperationsInProjects() {
		LOGGER.info("🧪 Testing CRUD operations in Projects...");
		navigateToViewByClass(CProjectsView.class);
		testCRUDOperationsInView("Projects", "new-button", "save-button",
			"delete-button");
		LOGGER.info("✅ Projects CRUD operations test completed");
	}

	@Test
	void testEntityRelationGrids() {
		LOGGER.info("🧪 Testing entity relation grids across views...");
		// Test entity relations in Users grid (User Type, Company, etc.)
		testEntityRelationGrid(CUsersView.class);
		// Test entity relations in Projects grid
		testEntityRelationGrid(CProjectsView.class);
		LOGGER.info("✅ Entity relation grids test completed");
	}

	@Test
	void testFormValidationAndErrorHandling() {
		LOGGER.info("🧪 Testing form validation and error handling...");

		// Test form validation in different views
		for (final Class<?> view : viewClasses) {
			navigateToViewByClass(view);
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
		performLogout();
		wait_loginscreen();
		// Try invalid credentials
		performLogin("invaliduser", "wrongpassword");
		// Check for error message (exact selector may vary)
		final boolean hasError =
			page.locator("vaadin-notification, [role='alert'], .error").count() > 0;
		assertTrue(hasError, "Should show error message for invalid login");
		// Login with correct credentials to restore state
		performLogin("admin", "test123");
	}

	@Test
	void testLoginFunctionality() {
		LOGGER.info("🧪 Testing login functionality...");
		assertBrowserAvailable();
		// Login is handled in setUp(), verify we're logged in
		assertTrue(page.locator("vaadin-app-layout").isVisible(),
			"Should be logged in and see app layout");
		LOGGER.info("✅ Login functionality test completed");
	}

	@Test
	void testLogoutFunctionality() {
		LOGGER.info("🧪 Testing logout functionality...");
		performLogout();
		wait_loginscreen();
		assertTrue(page.locator(".custom-login-view").isVisible(),
			"Should be back at login page");
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

	@Test
	void testResponsiveDesign() {
		LOGGER.info("🧪 Testing responsive design...");

		// Test responsive design for each view
		for (final Class<?> view : viewClasses) {
			navigateToViewByClass(view);
			testResponsiveDesign(view.getSimpleName());
		}
		LOGGER.info("✅ Responsive design test completed");
	}

	@Test
	void testSearchFunctionality() {
		LOGGER.info("🧪 Testing search functionality across views...");
		// Test search in Users view (CSearchable implementation)
		testSearchFunctionality(CUsersView.class, "admin");
		// Test search in Projects view (CSearchable implementation)
		testSearchFunctionality(CProjectsView.class, "Test");
		LOGGER.info("✅ Search functionality test completed");
	}
}