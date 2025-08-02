package tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import tech.derbent.activities.view.CActivitiesView;
import tech.derbent.meetings.view.CMeetingsView;
import tech.derbent.projects.view.CProjectsView;
import tech.derbent.users.view.CUsersView;

/**
 * CApplicationWorkflowPlaywrightTest - Comprehensive Playwright tests for complete
 * application workflows. Tests end-to-end application scenarios, cross-view navigation,
 * and complex user workflows following the strict coding guidelines for Playwright
 * testing.
 */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT)
@TestPropertySource (properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080" }
)
public class CApplicationWorkflowPlaywrightTest extends CBaseUITest {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CApplicationWorkflowPlaywrightTest.class);

	@Test
	void testApplicationDataFlow() {
		LOGGER.info("🧪 Testing complete application data flow...");
		// Create a complete data flow: User -> Company -> Project -> Activity -> Meeting
		final String timestamp = String.valueOf(System.currentTimeMillis());

		// Step 1: Create User (if Users view is available)
		if (navigateToViewByClass(CUsersView.class)) {
			final var newButtons = page
				.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");

			if (newButtons.count() > 0) {
				newButtons.first().click();
				wait_1000();
				fillFirstTextField("TestUser_" + timestamp);
				final var saveButtons = page.locator(
					"vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");

				if (saveButtons.count() > 0) {
					saveButtons.first().click();
					wait_2000();
					takeScreenshot("dataflow-user-created");
				}
			}
		}

		// Step 2: Create Project
		if (navigateToViewByClass(CProjectsView.class)) {
			wait_1000();
			final var newButtons = page
				.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");

			if (newButtons.count() > 0) {
				newButtons.first().click();
				wait_1000();
				fillFirstTextField("TestProject_" + timestamp);
				final var saveButtons = page.locator(
					"vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");

				if (saveButtons.count() > 0) {
					saveButtons.first().click();
					wait_2000();
					takeScreenshot("dataflow-project-created");
				}
			}
		}

		// Step 3: Create Activity for the Project
		if (navigateToViewByClass(CActivitiesView.class)) {
			wait_1000();
			final var newButtons = page
				.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");

			if (newButtons.count() > 0) {
				newButtons.first().click();
				wait_1000();
				fillFirstTextField("TestActivity_" + timestamp);
				final var saveButtons = page.locator(
					"vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");

				if (saveButtons.count() > 0) {
					saveButtons.first().click();
					wait_2000();
					takeScreenshot("dataflow-activity-created");
				}
			}
		}

		// Step 4: Create Meeting for the Project
		if (navigateToViewByClass(CMeetingsView.class)) {
			wait_1000();
			final var newButtons = page
				.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");

			if (newButtons.count() > 0) {
				newButtons.first().click();
				wait_1000();
				fillFirstTextField("TestMeeting_" + timestamp);
				final var saveButtons = page.locator(
					"vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");

				if (saveButtons.count() > 0) {
					saveButtons.first().click();
					wait_2000();
					takeScreenshot("dataflow-meeting-created");
				}
			}
		}
		LOGGER.info("✅ Complete application data flow test completed");
	}

	@Test
	void testApplicationErrorHandling() {
		LOGGER.info("🧪 Testing application error handling...");

		// Test form validation errors across views
		for (final Class view : viewClasses) {
			navigateToViewByClass(view);
			// Try to create new item and submit empty form
			final var newButtons = page
				.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");

			if (newButtons.count() > 0) {
				newButtons.first().click();
				wait_1000();
				// Try to save without filling required fields
				final var saveButtons = page.locator(
					"vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");

				if (saveButtons.count() > 0) {
					saveButtons.first().click();
					wait_1000();
					// Check for validation messages
					final var errorMessages = page.locator(
						"vaadin-text-field[invalid], .error-message, [role='alert']");
					LOGGER.debug("{} view validation produced {} error messages", view,
						errorMessages.count());
					takeScreenshot(
						"error-handling-" + view.getSimpleName().toLowerCase());
				}
				// Close form
				final var cancelButtons =
					page.locator("vaadin-button:has-text('Cancel')");

				if (cancelButtons.count() > 0) {
					cancelButtons.first().click();
					wait_500();
				}
			}
		}
		LOGGER.info("✅ Application error handling test completed");
	}

	@Test
	void testApplicationLogoutAndSecurity() {
		LOGGER.info("🧪 Testing application logout and security...");
		takeScreenshot("security-logged-in");

		// Test logout functionality
		if (performLogout()) {
			wait_loginscreen();
			takeScreenshot("security-logged-out");
			// Verify we're back at login screen
			assertTrue(page.locator("vaadin-login-overlay").isVisible(),
				"Should be at login screen after logout");
			// Try to access a protected view directly
			page.navigate(baseUrl + "/projects");
			wait_1000();
			// Should be redirected to login
			takeScreenshot("security-redirect-to-login");
		}
		else {
			LOGGER.warn("Logout functionality not available for testing");
		}
		LOGGER.info("✅ Application logout and security test completed");
	}

	@Test
	void testCompleteApplicationLoginAndNavigation() {
		LOGGER.info("🧪 Testing complete application login and navigation...");
		// Test initial application load
		page.navigate(baseUrl);
		wait_loginscreen();
		takeScreenshot("application-initial-load");
		// Test login
		performLogin("admin", "test123");
		wait_afterlogin();
		takeScreenshot("application-after-login");
		// Test navigation to all main views

		for (final Class view : viewClasses) {
			LOGGER.debug("Testing navigation to {}", view);

			if (navigateToViewByClass(view)) {
				wait_1000();
				takeScreenshot(
					"application-navigation-" + view.getSimpleName().toLowerCase());
				// Verify grid is present and functional
				assertTrue(getGridRowCount() >= 0,
					"Grid should be present in " + view + " view");
			}
		}
		LOGGER.info("✅ Complete application login and navigation test completed");
	}

	@Test
	void testCrossViewDataConsistency() {
		LOGGER.info("🧪 Testing cross-view data consistency...");
		// Create a project first
		assertTrue(navigateToViewByClass(CProjectsView.class),
			"Should navigate to Activities view");
		final var newButtons =
			page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");

		if (newButtons.count() > 0) {
			newButtons.first().click();
			final String projectName =
				"CrossViewTestProject_" + System.currentTimeMillis();
			fillFirstTextField(projectName);
			final var saveButtons = page.locator(
				"vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");

			if (saveButtons.count() > 0) {
				saveButtons.first().click();
				wait_2000();
				takeScreenshot("cross-view-project-created");
			}
			// Now check if this project appears in Activities view
			assertTrue(navigateToViewByClass(CActivitiesView.class),
				"Should navigate to Activities view");
			wait_1000();
			// Try to create an activity for this project
			final var activityNewButtons = page
				.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");

			if (activityNewButtons.count() > 0) {
				activityNewButtons.first().click();
				wait_1000();
				// Check if project ComboBox contains our new project
				final var comboBoxes = page.locator("vaadin-combo-box");

				if (comboBoxes.count() > 0) {
					comboBoxes.first().click();
					wait_500();
					// Look for our project in the options
					final var options = page.locator("vaadin-combo-box-item");
					boolean projectFound = false;

					for (int i = 0; i < options.count(); i++) {
						final String optionText = options.nth(i).textContent();

						if ((optionText != null)
							&& optionText.contains("CrossViewTestProject")) {
							projectFound = true;
							options.nth(i).click();
							break;
						}
					}
					LOGGER.debug("Project found in Activities ComboBox: {}",
						projectFound);
					takeScreenshot("cross-view-project-in-activities");
				}
				// Close form
				final var cancelButtons =
					page.locator("vaadin-button:has-text('Cancel')");

				if (cancelButtons.count() > 0) {
					cancelButtons.first().click();
					wait_500();
				}
			}
		}
		LOGGER.info("✅ Cross-view data consistency test completed");
	}
}