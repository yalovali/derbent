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

/** Focused project activation test that validates project change activations, status tracking, and related functionality. Tests project lifecycle
 * management and change notifications. */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080"
})
@DisplayName ("🔄 Project Activation Test")
public class CProjectActivationTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectActivationTest.class);

	@Test
	@DisplayName ("🔄 Test Project Activation Functionality")
	void testProjectActivationFunctionality() {
		LOGGER.info("🧪 Starting project activation functionality test...");
		try {
			// Create screenshots directory
			Files.createDirectories(Paths.get("target/screenshots"));
			// Login to application
			loginToApplication();
			// Test project activation
			testProjectActivation();
			LOGGER.info("✅ Project activation functionality test completed successfully");
		} catch (Exception e) {
			LOGGER.error("❌ Project activation functionality test failed: {}", e.getMessage());
			takeScreenshot("project-activation-error", true);
			throw new AssertionError("Project activation functionality test failed", e);
		}
	}

	@Test
	@DisplayName ("📝 Test Project Change Tracking")
	void testProjectChangeTrackingFunctionality() {
		LOGGER.info("🧪 Starting project change tracking test...");
		try {
			// Create screenshots directory
			Files.createDirectories(Paths.get("target/screenshots"));
			// Login to application
			loginToApplication();
			// Test project change tracking
			testProjectChangeTracking();
			LOGGER.info("✅ Project change tracking test completed successfully");
		} catch (Exception e) {
			LOGGER.error("❌ Project change tracking test failed: {}", e.getMessage());
			takeScreenshot("project-change-tracking-error", true);
			throw new AssertionError("Project change tracking test failed", e);
		}
	}

	@Test
	@DisplayName ("🔄 Test Project Status Changes")
	void testProjectStatusChanges() {
		LOGGER.info("🧪 Starting project status changes test...");
		try {
			// Create screenshots directory
			Files.createDirectories(Paths.get("target/screenshots"));
			// Login to application
			loginToApplication();
			// Navigate to Projects
			navigateToProjects();
			wait_1000();
			// Create a test project first
			LOGGER.info("➕ Creating test project for status change testing");
			clickNew();
			wait_1000();
			String testProjectName = "Status Test Project " + System.currentTimeMillis();
			fillFirstTextField(testProjectName);
			// Look for status or state fields
			if (page.locator("vaadin-combo-box[label*='Status'], vaadin-combo-box[label*='State']").count() > 0) {
				LOGGER.info("📊 Found status field, selecting initial status");
				selectFirstComboBoxOption();
			}
			clickSave();
			wait_1000();
			takeScreenshot("project-status-created", false);
			// Now test status changes
			if (verifyGridHasData()) {
				clickFirstGridRow();
				wait_500();
				clickEdit();
				wait_1000();
				// Test changing status if status field exists
				if (page.locator("vaadin-combo-box[label*='Status'], vaadin-combo-box[label*='State']").count() > 0) {
					LOGGER.info("🔄 Testing status change");
					page.locator("vaadin-combo-box[label*='Status'], vaadin-combo-box[label*='State']").first().click();
					wait_500();
					// Select different option
					if (page.locator("vaadin-combo-box-item").count() > 1) {
						page.locator("vaadin-combo-box-item").nth(1).click();
						wait_500();
					}
					clickSave();
					wait_1000();
					takeScreenshot("project-status-changed", false);
					LOGGER.info("✅ Project status change completed");
				} else {
					LOGGER.info("ℹ️ No status field found, testing general field changes");
					fillFirstTextField(testProjectName + " - Modified");
					clickSave();
					wait_1000();
					takeScreenshot("project-modified", false);
				}
			}
			LOGGER.info("✅ Project status changes test completed successfully");
		} catch (Exception e) {
			LOGGER.error("❌ Project status changes test failed: {}", e.getMessage());
			takeScreenshot("project-status-changes-error", true);
			throw new AssertionError("Project status changes test failed", e);
		}
	}

	@Test
	@DisplayName ("🔔 Test Project Activation Notifications")
	void testProjectActivationNotifications() {
		LOGGER.info("🧪 Starting project activation notifications test...");
		try {
			// Create screenshots directory
			Files.createDirectories(Paths.get("target/screenshots"));
			// Login to application
			loginToApplication();
			// Navigate to Projects
			navigateToProjects();
			wait_1000();
			// Look for notification areas or status messages
			LOGGER.info("🔍 Checking for notification areas");
			// Common notification selectors
			if (page.locator("vaadin-notification, .notification, .alert, .message").count() > 0) {
				LOGGER.info("📢 Found notification area");
				takeScreenshot("notifications-area", false);
			} else {
				LOGGER.info("ℹ️ No notification area found");
			}
			// Test creating a project to see if notifications appear
			clickNew();
			wait_1000();
			String testProjectName = "Notification Test " + System.currentTimeMillis();
			fillFirstTextField(testProjectName);
			clickSave();
			wait_2000(); // Wait longer for potential notifications
			// Check for success notifications
			if (page.locator("vaadin-notification, .notification, .alert, .success").count() > 0) {
				LOGGER.info("✅ Found success notification after project creation");
				takeScreenshot("project-creation-notification", false);
			} else {
				LOGGER.info("ℹ️ No notification found after project creation");
			}
			LOGGER.info("✅ Project activation notifications test completed successfully");
		} catch (Exception e) {
			LOGGER.error("❌ Project activation notifications test failed: {}", e.getMessage());
			takeScreenshot("project-notifications-error", true);
			throw new AssertionError("Project activation notifications test failed", e);
		}
	}

	@Test
	@DisplayName ("📊 Test Project Lifecycle Management")
	void testProjectLifecycleManagement() {
		LOGGER.info("🧪 Starting project lifecycle management test...");
		try {
			// Create screenshots directory
			Files.createDirectories(Paths.get("target/screenshots"));
			// Login to application
			loginToApplication();
			// Navigate to Projects
			navigateToProjects();
			wait_1000();
			// Test complete project lifecycle
			LOGGER.info("🔄 Testing complete project lifecycle");
			// 1. Create new project
			LOGGER.info("➕ Step 1: Creating new project");
			clickNew();
			wait_1000();
			String lifecycleProjectName = "Lifecycle Test " + System.currentTimeMillis();
			fillFirstTextField(lifecycleProjectName);
			// Fill additional fields if present
			if (page.locator("vaadin-text-area").count() > 0) {
				fillFirstTextArea("Project description for lifecycle testing");
			}
			if (page.locator("vaadin-combo-box").count() > 0) {
				selectFirstComboBoxOption();
			}
			clickSave();
			wait_1000();
			takeScreenshot("lifecycle-step1-created", false);
			// 2. Activate/Start project
			LOGGER.info("🟢 Step 2: Activating project");
			if (verifyGridHasData()) {
				clickFirstGridRow();
				wait_500();
				// Look for activation buttons
				if (page.locator("vaadin-button:has-text('Activate'), vaadin-button:has-text('Start')").count() > 0) {
					page.locator("vaadin-button:has-text('Activate'), vaadin-button:has-text('Start')").first().click();
					wait_1000();
					takeScreenshot("lifecycle-step2-activated", false);
				} else {
					LOGGER.info("ℹ️ No activation button found, testing edit instead");
					clickEdit();
					wait_1000();
					fillFirstTextField(lifecycleProjectName + " - Activated");
					clickSave();
					wait_1000();
					takeScreenshot("lifecycle-step2-modified", false);
				}
			}
			// 3. Update project
			LOGGER.info("✏️ Step 3: Updating project");
			clickFirstGridRow();
			wait_500();
			clickEdit();
			wait_1000();
			fillFirstTextField(lifecycleProjectName + " - Updated");
			clickSave();
			wait_1000();
			takeScreenshot("lifecycle-step3-updated", false);
			// 4. Complete/Deactivate project
			LOGGER.info("🔴 Step 4: Completing project");
			clickFirstGridRow();
			wait_500();
			if (page.locator("vaadin-button:has-text('Complete'), vaadin-button:has-text('Deactivate')").count() > 0) {
				page.locator("vaadin-button:has-text('Complete'), vaadin-button:has-text('Deactivate')").first().click();
				wait_1000();
				takeScreenshot("lifecycle-step4-completed", false);
			} else {
				LOGGER.info("ℹ️ No completion button found, testing final edit");
				clickEdit();
				wait_1000();
				fillFirstTextField(lifecycleProjectName + " - Completed");
				clickSave();
				wait_1000();
				takeScreenshot("lifecycle-step4-final", false);
			}
			LOGGER.info("✅ Project lifecycle management test completed successfully");
		} catch (Exception e) {
			LOGGER.error("❌ Project lifecycle management test failed: {}", e.getMessage());
			takeScreenshot("project-lifecycle-error", true);
			throw new AssertionError("Project lifecycle management test failed", e);
		}
	}
}
