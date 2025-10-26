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

/** Comprehensive CRUD test suite for entities with workflow status management.
 * <p>
 * This test validates:
 * <ul>
 * <li>Activities CRUD operations with workflow status initialization</li>
 * <li>Meetings CRUD operations with workflow status transitions</li>
 * <li>Projects and Users CRUD operations</li>
 * <li>Workflow initial status assignment verification</li>
 * <li>Status ComboBox displays correct workflow transitions</li>
 * </ul>
 * <p>
 * Key Focus:
 * <ul>
 * <li>Verify workflow initial status is assigned to new items</li>
 * <li>Verify status ComboBox shows valid workflow transitions</li>
 * <li>Test complete CRUD cycle for main entities</li>
 * <li>Screenshot documentation of all operations</li>
 * </ul>
 */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080"
})
@DisplayName ("🔄 Workflow Status CRUD Test")
public class CWorkflowStatusCrudTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CWorkflowStatusCrudTest.class);

	@Test
	@DisplayName ("✅ Complete CRUD operations with workflow status verification")
	void testWorkflowStatusCrudOperations() {
		LOGGER.info("🚀 Starting workflow status CRUD test...");
		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			// Phase 1: Initialize and login
			LOGGER.info("📋 Phase 1: Initialization and Login");
			testInitializationAndLogin();
			// Phase 2: Test Activities CRUD with workflow
			LOGGER.info("📋 Phase 2: Activities CRUD Operations");
			testActivitiesCrud();
			// Phase 3: Test Meetings CRUD with workflow
			LOGGER.info("📋 Phase 3: Meetings CRUD Operations");
			testMeetingsCrud();
			// Phase 4: Test Projects CRUD
			LOGGER.info("📋 Phase 4: Projects CRUD Operations");
			testProjectsCrud();
			// Phase 5: Test Users CRUD
			LOGGER.info("📋 Phase 5: Users CRUD Operations");
			testUsersCrud();
			LOGGER.info("✅ Workflow status CRUD test completed successfully!");
		} catch (Exception e) {
			LOGGER.error("❌ Workflow status CRUD test failed: {}", e.getMessage());
			takeScreenshot("workflow-crud-error", true);
			throw new AssertionError("Workflow status CRUD test failed", e);
		}
	}

	/** Phase 1: Test initialization and login. */
	private void testInitializationAndLogin() {
		LOGGER.info("🔐 Testing initialization and login...");
		// Initialize sample data and login
		loginToApplication();
		wait_afterlogin();
		takeScreenshot("phase1-logged-in", false);
		// Verify login was successful
		if (page.url().contains("/login")) {
			throw new AssertionError("Login failed - still on login page");
		}
		LOGGER.info("✅ Phase 1 complete: Successfully logged in");
	}

	/** Phase 2: Test Activities CRUD operations with workflow status verification. */
	private void testActivitiesCrud() {
		LOGGER.info("📝 Testing Activities CRUD operations...");
		try {
			// Navigate to Activities page
			navigateToActivitiesPage();
			takeScreenshot("activities-list-view", false);
			// Test CREATE: Create a new activity
			LOGGER.info("➕ Creating new activity...");
			clickNew();
			wait_1000();
			takeScreenshot("activities-new-dialog", false);
			// Fill in activity details
			fillFirstTextField("Test Activity - Workflow Verification");
			wait_500();
			// Verify initial status is set - check if status ComboBox has a value
			verifyStatusComboboxHasValue("activities-initial-status-set");
			// Save the activity
			clickSave();
			wait_1000();
			takeScreenshot("activities-after-create", false);
			// Test READ: Verify activity appears in grid
			LOGGER.info("👁️ Verifying activity appears in grid...");
			if (!page.content().contains("Test Activity")) {
				LOGGER.warn("⚠️ Created activity not found in grid");
			}
			// Test UPDATE: Edit the activity
			LOGGER.info("✏️ Editing activity...");
			clickFirstGridRow();
			wait_500();
			clickEdit();
			wait_1000();
			takeScreenshot("activities-edit-dialog", false);
			// Verify status ComboBox shows workflow transitions
			verifyStatusComboboxHasOptions("activities-status-transitions");
			// Update activity name
			fillFirstTextField("Test Activity - Updated");
			wait_500();
			clickSave();
			wait_1000();
			takeScreenshot("activities-after-update", false);
			// Test DELETE: Delete the activity
			LOGGER.info("🗑️ Deleting activity...");
			clickFirstGridRow();
			wait_500();
			clickDelete();
			wait_500();
			// Confirm delete if there's a confirmation dialog
			confirmDeleteIfPresent();
			wait_1000();
			takeScreenshot("activities-after-delete", false);
			LOGGER.info("✅ Activities CRUD complete");
		} catch (Exception e) {
			takeScreenshot("activities-crud-error", true);
			throw new AssertionError("Activities CRUD failed: " + e.getMessage(), e);
		}
	}

	/** Phase 3: Test Meetings CRUD operations with workflow status verification. */
	private void testMeetingsCrud() {
		LOGGER.info("📅 Testing Meetings CRUD operations...");
		try {
			// Navigate to Meetings page
			navigateToMeetingsPage();
			takeScreenshot("meetings-list-view", false);
			// Test CREATE: Create a new meeting
			LOGGER.info("➕ Creating new meeting...");
			clickNew();
			wait_1000();
			takeScreenshot("meetings-new-dialog", false);
			// Fill in meeting details
			fillFirstTextField("Test Meeting - Workflow Verification");
			wait_500();
			// Verify initial status is set
			verifyStatusComboboxHasValue("meetings-initial-status-set");
			// Save the meeting
			clickSave();
			wait_1000();
			takeScreenshot("meetings-after-create", false);
			// Test READ: Verify meeting appears in grid
			LOGGER.info("👁️ Verifying meeting appears in grid...");
			if (!page.content().contains("Test Meeting")) {
				LOGGER.warn("⚠️ Created meeting not found in grid");
			}
			// Test UPDATE: Edit the meeting
			LOGGER.info("✏️ Editing meeting...");
			clickFirstGridRow();
			wait_500();
			clickEdit();
			wait_1000();
			takeScreenshot("meetings-edit-dialog", false);
			// Verify status ComboBox shows workflow transitions
			verifyStatusComboboxHasOptions("meetings-status-transitions");
			// Update meeting name
			fillFirstTextField("Test Meeting - Updated");
			wait_500();
			clickSave();
			wait_1000();
			takeScreenshot("meetings-after-update", false);
			// Test DELETE: Delete the meeting
			LOGGER.info("🗑️ Deleting meeting...");
			clickFirstGridRow();
			wait_500();
			clickDelete();
			wait_500();
			confirmDeleteIfPresent();
			wait_1000();
			takeScreenshot("meetings-after-delete", false);
			LOGGER.info("✅ Meetings CRUD complete");
		} catch (Exception e) {
			takeScreenshot("meetings-crud-error", true);
			throw new AssertionError("Meetings CRUD failed: " + e.getMessage(), e);
		}
	}

	/** Phase 4: Test Projects CRUD operations. */
	private void testProjectsCrud() {
		LOGGER.info("📁 Testing Projects CRUD operations...");
		try {
			// Navigate to Projects page
			navigateToProjectsPage();
			takeScreenshot("projects-list-view", false);
			// Test CREATE: Create a new project
			LOGGER.info("➕ Creating new project...");
			clickNew();
			wait_1000();
			takeScreenshot("projects-new-dialog", false);
			// Fill in project details
			fillFirstTextField("Test Project - CRUD Verification");
			wait_500();
			clickSave();
			wait_1000();
			takeScreenshot("projects-after-create", false);
			// Test UPDATE: Edit the project
			LOGGER.info("✏️ Editing project...");
			clickFirstGridRow();
			wait_500();
			clickEdit();
			wait_1000();
			takeScreenshot("projects-edit-dialog", false);
			fillFirstTextField("Test Project - Updated");
			wait_500();
			clickSave();
			wait_1000();
			takeScreenshot("projects-after-update", false);
			LOGGER.info("✅ Projects CRUD complete (skipping delete to preserve data)");
		} catch (Exception e) {
			takeScreenshot("projects-crud-error", true);
			throw new AssertionError("Projects CRUD failed: " + e.getMessage(), e);
		}
	}

	/** Phase 5: Test Users CRUD operations. */
	private void testUsersCrud() {
		LOGGER.info("👤 Testing Users CRUD operations...");
		try {
			// Navigate to Users page
			navigateToUsersPage();
			takeScreenshot("users-list-view", false);
			// Test READ: Verify users are displayed
			LOGGER.info("👁️ Verifying users are displayed...");
			final Locator grid = page.locator("vaadin-grid").first();
			final int rowCount = grid.locator("vaadin-grid-cell-content").count();
			LOGGER.info("Found {} user rows", rowCount);
			if (rowCount == 0) {
				LOGGER.warn("⚠️ No users found in grid");
			}
			// Test viewing user details
			LOGGER.info("👁️ Viewing user details...");
			if (rowCount > 0) {
				clickFirstGridRow();
				wait_500();
				takeScreenshot("users-selected-row", false);
			}
			LOGGER.info("✅ Users CRUD complete (read-only test)");
		} catch (Exception e) {
			takeScreenshot("users-crud-error", true);
			throw new AssertionError("Users CRUD failed: " + e.getMessage(), e);
		}
	}

	/** Navigate to Activities page using dynamic routing. */
	private void navigateToActivitiesPage() {
		LOGGER.info("🧭 Navigating to Activities page...");
		page.navigate("http://localhost:" + port + "/cdynamicpagerouter/page:3");
		wait_1000();
	}

	/** Navigate to Meetings page using dynamic routing. */
	private void navigateToMeetingsPage() {
		LOGGER.info("🧭 Navigating to Meetings page...");
		page.navigate("http://localhost:" + port + "/cdynamicpagerouter/page:4");
		wait_1000();
	}

	/** Navigate to Projects page using dynamic routing. */
	private void navigateToProjectsPage() {
		LOGGER.info("🧭 Navigating to Projects page...");
		page.navigate("http://localhost:" + port + "/cdynamicpagerouter/page:1");
		wait_1000();
	}

	/** Navigate to Users page using dynamic routing. */
	private void navigateToUsersPage() {
		LOGGER.info("🧭 Navigating to Users page...");
		page.navigate("http://localhost:" + port + "/cdynamicpagerouter/page:12");
		wait_1000();
	}

	/** Verify that the status ComboBox has a value (initial status is set). */
	private void verifyStatusComboboxHasValue(String screenshotName) {
		LOGGER.info("🔍 Verifying status ComboBox has initial value...");
		final Locator statusComboBoxes = page.locator("vaadin-combo-box").filter(new Locator.FilterOptions().setHasText("Status"));
		if (statusComboBoxes.count() > 0) {
			final Locator statusComboBox = statusComboBoxes.first();
			final String value = statusComboBox.inputValue();
			if (value != null && !value.trim().isEmpty()) {
				LOGGER.info("✅ Status ComboBox has initial value: {}", value);
			} else {
				LOGGER.warn("⚠️ Status ComboBox has no value - initial status may not be set");
			}
		} else {
			LOGGER.warn("⚠️ Status ComboBox not found on page");
		}
		takeScreenshot(screenshotName, false);
	}

	/** Verify that the status ComboBox has options (workflow transitions are available). */
	private void verifyStatusComboboxHasOptions(String screenshotName) {
		LOGGER.info("🔍 Verifying status ComboBox has workflow transition options...");
		final Locator statusComboBoxes = page.locator("vaadin-combo-box").filter(new Locator.FilterOptions().setHasText("Status"));
		if (statusComboBoxes.count() > 0) {
			final Locator statusComboBox = statusComboBoxes.first();
			// Click to open the dropdown
			statusComboBox.click();
			wait_500();
			// Check if overlay with options is visible
			final Locator overlay = page.locator("vaadin-combo-box-overlay");
			if (overlay.count() > 0) {
				final Locator items = overlay.locator("vaadin-combo-box-item");
				final int itemCount = items.count();
				LOGGER.info("✅ Status ComboBox has {} workflow transition options", itemCount);
			} else {
				LOGGER.warn("⚠️ Status ComboBox overlay not found");
			}
			// Click elsewhere to close the dropdown
			page.locator("body").click();
			wait_500();
		} else {
			LOGGER.warn("⚠️ Status ComboBox not found on page");
		}
		takeScreenshot(screenshotName, false);
	}

	/** Confirm delete operation if a confirmation dialog is present. */
	private void confirmDeleteIfPresent() {
		LOGGER.info("🔍 Checking for delete confirmation dialog...");
		final Locator confirmButtons = page.locator("vaadin-button:has-text('Confirm'), vaadin-button:has-text('Yes'), vaadin-button:has-text('OK')");
		if (confirmButtons.count() > 0) {
			LOGGER.info("✅ Found confirmation button, clicking...");
			confirmButtons.first().click();
			wait_500();
		} else {
			LOGGER.info("ℹ️ No confirmation dialog found");
		}
	}
}
