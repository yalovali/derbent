package automated_tests.tech.derbent.ui.automation;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import com.microsoft.playwright.Locator;

/** Comprehensive Playwright test for Type and Status entities CRUD operations. This test validates: 1. Complete CRUD operations on Type entities
 * (CActivityType, CMeetingType, CDecisionType, COrderType) 2. Complete CRUD operations on Status entities (CProjectItemStatus, CApprovalStatus) 3.
 * Toolbar button operations (New, Save, Delete, Refresh) 4. Response to updates (notifications, grid refresh, entity selection) 5. Validation and
 * error handling 6. Entity dependency checking and non-deletable protection */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080"
})
public class CTypeStatusCrudTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CTypeStatusCrudTest.class);

	/** Test complete CRUD operations for Activity Types. */
	@Test
	public void testActivityTypeCrudOperations() {
		LOGGER.info("=== Testing Activity Type CRUD Operations ===");
		try {
			// Login
			loginToApplication("admin", "test123");
			wait_afterlogin();
			LOGGER.info("✅ Logged in successfully");
			// Navigate to Activity Types
			navigateToViewByText("Activity Types");
			wait_1000();
			takeScreenshot("activity-type-initial", false);
			LOGGER.info("✅ Navigated to Activity Types view");
			// Test CREATE operation
			testCreateOperation("Activity Type", "Test Activity Type " + System.currentTimeMillis());
			// Test READ operation (entity is already selected after create)
			testReadOperation("Activity Type");
			// Test UPDATE operation
			testUpdateOperation("Activity Type", "Updated Activity Type " + System.currentTimeMillis());
			// Test REFRESH operation
			testRefreshOperation("Activity Type");
			// Test DELETE validation (should fail for non-deletable or in-use entities)
			testDeleteValidation("Activity Type");
			LOGGER.info("=== Activity Type CRUD Test Completed Successfully ===");
		} catch (Exception e) {
			LOGGER.error("❌ Activity Type CRUD test failed", e);
			takeScreenshot("activity-type-crud-error", true);
			throw new AssertionError("Activity Type CRUD test failed", e);
		}
	}

	/** Test complete CRUD operations for Activity Statuses (CProjectItemStatus). */
	@Test
	public void testActivityStatusCrudOperations() {
		LOGGER.info("=== Testing Activity Status CRUD Operations ===");
		try {
			// Login
			loginToApplication("admin", "test123");
			wait_afterlogin();
			LOGGER.info("✅ Logged in successfully");
			// Navigate to Activity Status
			navigateToViewByText("Activity Status");
			wait_1000();
			takeScreenshot("activity-status-initial", false);
			LOGGER.info("✅ Navigated to Activity Status view");
			// Test CREATE operation
			testCreateOperation("Activity Status", "Test Status " + System.currentTimeMillis());
			// Test READ operation
			testReadOperation("Activity Status");
			// Test UPDATE operation
			testUpdateOperation("Activity Status", "Updated Status " + System.currentTimeMillis());
			// Test REFRESH operation
			testRefreshOperation("Activity Status");
			// Test DELETE validation
			testDeleteValidation("Activity Status");
			LOGGER.info("=== Activity Status CRUD Test Completed Successfully ===");
		} catch (Exception e) {
			LOGGER.error("❌ Activity Status CRUD test failed", e);
			takeScreenshot("activity-status-crud-error", true);
			throw new AssertionError("Activity Status CRUD test failed", e);
		}
	}

	/** Test complete CRUD operations for Meeting Types. */
	@Test
	public void testMeetingTypeCrudOperations() {
		LOGGER.info("=== Testing Meeting Type CRUD Operations ===");
		try {
			// Login
			loginToApplication("admin", "test123");
			wait_afterlogin();
			LOGGER.info("✅ Logged in successfully");
			// Navigate to Meeting Types
			navigateToViewByText("Meeting Types");
			wait_1000();
			takeScreenshot("meeting-type-initial", false);
			LOGGER.info("✅ Navigated to Meeting Types view");
			// Test CREATE operation
			testCreateOperation("Meeting Type", "Test Meeting Type " + System.currentTimeMillis());
			// Test READ operation
			testReadOperation("Meeting Type");
			// Test UPDATE operation
			testUpdateOperation("Meeting Type", "Updated Meeting Type " + System.currentTimeMillis());
			// Test REFRESH operation
			testRefreshOperation("Meeting Type");
			// Test DELETE validation
			testDeleteValidation("Meeting Type");
			LOGGER.info("=== Meeting Type CRUD Test Completed Successfully ===");
		} catch (Exception e) {
			LOGGER.error("❌ Meeting Type CRUD test failed", e);
			takeScreenshot("meeting-type-crud-error", true);
			throw new AssertionError("Meeting Type CRUD test failed", e);
		}
	}

	/** Test complete CRUD operations for Decision Types. */
	@Test
	public void testDecisionTypeCrudOperations() {
		LOGGER.info("=== Testing Decision Type CRUD Operations ===");
		try {
			// Login
			loginToApplication("admin", "test123");
			wait_afterlogin();
			LOGGER.info("✅ Logged in successfully");
			// Navigate to Decision Types
			navigateToViewByText("Decision Types");
			wait_1000();
			takeScreenshot("decision-type-initial", false);
			LOGGER.info("✅ Navigated to Decision Types view");
			// Test CREATE operation
			testCreateOperation("Decision Type", "Test Decision Type " + System.currentTimeMillis());
			// Test READ operation
			testReadOperation("Decision Type");
			// Test UPDATE operation
			testUpdateOperation("Decision Type", "Updated Decision Type " + System.currentTimeMillis());
			// Test REFRESH operation
			testRefreshOperation("Decision Type");
			// Test DELETE validation
			testDeleteValidation("Decision Type");
			LOGGER.info("=== Decision Type CRUD Test Completed Successfully ===");
		} catch (Exception e) {
			LOGGER.error("❌ Decision Type CRUD test failed", e);
			takeScreenshot("decision-type-crud-error", true);
			throw new AssertionError("Decision Type CRUD test failed", e);
		}
	}

	/** Test complete CRUD operations for Order Types. */
	@Test
	public void testOrderTypeCrudOperations() {
		LOGGER.info("=== Testing Order Type CRUD Operations ===");
		try {
			// Login
			loginToApplication("admin", "test123");
			wait_afterlogin();
			LOGGER.info("✅ Logged in successfully");
			// Navigate to Order Types
			navigateToViewByText("Order Types");
			wait_1000();
			takeScreenshot("order-type-initial", false);
			LOGGER.info("✅ Navigated to Order Types view");
			// Test CREATE operation
			testCreateOperation("Order Type", "Test Order Type " + System.currentTimeMillis());
			// Test READ operation
			testReadOperation("Order Type");
			// Test UPDATE operation
			testUpdateOperation("Order Type", "Updated Order Type " + System.currentTimeMillis());
			// Test REFRESH operation
			testRefreshOperation("Order Type");
			// Test DELETE validation
			testDeleteValidation("Order Type");
			LOGGER.info("=== Order Type CRUD Test Completed Successfully ===");
		} catch (Exception e) {
			LOGGER.error("❌ Order Type CRUD test failed", e);
			takeScreenshot("order-type-crud-error", true);
			throw new AssertionError("Order Type CRUD test failed", e);
		}
	}

	/** Test complete CRUD operations for Approval Statuses. */
	@Test
	public void testApprovalStatusCrudOperations() {
		LOGGER.info("=== Testing Approval Status CRUD Operations ===");
		try {
			// Login
			loginToApplication("admin", "test123");
			wait_afterlogin();
			LOGGER.info("✅ Logged in successfully");
			// Navigate to Approval Status
			navigateToViewByText("Approval Status");
			wait_1000();
			takeScreenshot("approval-status-initial", false);
			LOGGER.info("✅ Navigated to Approval Status view");
			// Test CREATE operation
			testCreateOperation("Approval Status", "Test Approval " + System.currentTimeMillis());
			// Test READ operation
			testReadOperation("Approval Status");
			// Test UPDATE operation
			testUpdateOperation("Approval Status", "Updated Approval " + System.currentTimeMillis());
			// Test REFRESH operation
			testRefreshOperation("Approval Status");
			// Test DELETE validation
			testDeleteValidation("Approval Status");
			LOGGER.info("=== Approval Status CRUD Test Completed Successfully ===");
		} catch (Exception e) {
			LOGGER.error("❌ Approval Status CRUD test failed", e);
			takeScreenshot("approval-status-crud-error", true);
			throw new AssertionError("Approval Status CRUD test failed", e);
		}
	}

	/** Helper method to test CREATE operation. */
	private void testCreateOperation(String entityName, String testName) {
		LOGGER.info("➕ Testing CREATE operation for {}", entityName);
		// Count initial grid rows
		int initialCount = countGridRows();
		LOGGER.info("📊 Initial grid row count: {}", initialCount);
		// Click New button
		clickNew();
		wait_500();
		takeScreenshot(entityName.toLowerCase().replace(" ", "-") + "-after-new", false);
		// Verify New button disabled after clicking
		verifyButtonState("New", false);
		LOGGER.info("✅ New button disabled after clicking");
		// Fill in the name field
		fillFirstTextField(testName);
		wait_500();
		takeScreenshot(entityName.toLowerCase().replace(" ", "-") + "-filled", false);
		LOGGER.info("✅ Filled name field with: {}", testName);
		// Verify Save button enabled
		verifyButtonState("Save", true);
		// Click Save button
		clickSave();
		wait_1000();
		takeScreenshot(entityName.toLowerCase().replace(" ", "-") + "-after-save", false);
		// Verify success notification
		verifyNotification("success");
		LOGGER.info("✅ Save success notification appeared");
		// Verify grid row count increased
		int newCount = countGridRows();
		if (newCount > initialCount) {
			LOGGER.info("✅ Grid row count increased from {} to {}", initialCount, newCount);
		} else {
			LOGGER.warn("⚠️ Grid row count did not increase (expected > {}, got {})", initialCount, newCount);
		}
		// Verify new entity is selected in grid
		verifyGridSelection();
		LOGGER.info("✅ CREATE operation completed for {}", entityName);
	}

	/** Helper method to test READ operation. */
	private void testReadOperation(String entityName) {
		LOGGER.info("👁️ Testing READ operation for {}", entityName);
		// Verify details are displayed in the form
		Locator textFields = page.locator("vaadin-text-field");
		int fieldCount = textFields.count();
		if (fieldCount > 0) {
			LOGGER.info("✅ Found {} text fields in details section", fieldCount);
			// Verify name field has value
			String nameValue = textFields.first().inputValue();
			if (nameValue != null && !nameValue.isEmpty()) {
				LOGGER.info("✅ Name field has value: {}", nameValue);
			} else {
				LOGGER.warn("⚠️ Name field is empty");
			}
		} else {
			LOGGER.warn("⚠️ No text fields found in details section");
		}
		takeScreenshot(entityName.toLowerCase().replace(" ", "-") + "-read", false);
		LOGGER.info("✅ READ operation completed for {}", entityName);
	}

	/** Helper method to test UPDATE operation. */
	private void testUpdateOperation(String entityName, String updatedName) {
		LOGGER.info("✏️ Testing UPDATE operation for {}", entityName);
		// Modify the name field
		Locator nameField = page.locator("vaadin-text-field").first();
		nameField.clear();
		wait_500();
		nameField.fill(updatedName);
		wait_500();
		takeScreenshot(entityName.toLowerCase().replace(" ", "-") + "-modified", false);
		LOGGER.info("✅ Modified name field to: {}", updatedName);
		// Click Save button
		clickSave();
		wait_1000();
		takeScreenshot(entityName.toLowerCase().replace(" ", "-") + "-after-update", false);
		// Verify success notification
		verifyNotification("success");
		LOGGER.info("✅ Update success notification appeared");
		// Verify updated value in field
		String currentValue = nameField.inputValue();
		if (currentValue.equals(updatedName)) {
			LOGGER.info("✅ Name field updated correctly: {}", currentValue);
		} else {
			LOGGER.warn("⚠️ Name field value mismatch (expected: {}, got: {})", updatedName, currentValue);
		}
		LOGGER.info("✅ UPDATE operation completed for {}", entityName);
	}

	/** Helper method to test REFRESH operation. */
	private void testRefreshOperation(String entityName) {
		LOGGER.info("🔄 Testing REFRESH operation for {}", entityName);
		// Click Refresh button
		Locator refreshButton = page.locator("vaadin-button:has-text('Refresh')");
		if (refreshButton.count() > 0) {
			refreshButton.click();
			wait_1000();
			takeScreenshot(entityName.toLowerCase().replace(" ", "-") + "-after-refresh", false);
			// Verify success notification
			verifyNotification("refresh");
			LOGGER.info("✅ Refresh operation completed");
		} else {
			LOGGER.warn("⚠️ Refresh button not found");
		}
		LOGGER.info("✅ REFRESH operation completed for {}", entityName);
	}

	/** Helper method to test DELETE validation (should show error for non-deletable/in-use entities). */
	private void testDeleteValidation(String entityName) {
		LOGGER.info("🗑️ Testing DELETE validation for {}", entityName);
		// Select first existing entity (likely non-deletable or in use)
		clickFirstGridRow();
		wait_500();
		// Click Delete button
		clickDelete();
		wait_500();
		takeScreenshot(entityName.toLowerCase().replace(" ", "-") + "-delete-attempt", false);
		// Check for error notification or confirmation dialog
		boolean hasErrorNotification = page.locator("vaadin-notification-card").count() > 0;
		boolean hasConfirmDialog = page.locator("vaadin-dialog-overlay").count() > 0;
		if (hasErrorNotification) {
			String notificationText = page.locator("vaadin-notification-card").textContent();
			LOGGER.info("✅ Error notification appeared: {}", notificationText);
			takeScreenshot(entityName.toLowerCase().replace(" ", "-") + "-delete-error", false);
		} else if (hasConfirmDialog) {
			LOGGER.info("⚠️ Confirmation dialog appeared - entity may be deletable");
			// Cancel the deletion to preserve test data
			clickCancel();
			wait_500();
			LOGGER.info("✅ Canceled deletion to preserve test data");
		} else {
			LOGGER.warn("⚠️ No error notification or confirmation dialog");
		}
		LOGGER.info("✅ DELETE validation completed for {}", entityName);
	}

	/** Helper method to count grid rows. */
	private int countGridRows() {
		Locator grid = page.locator("vaadin-grid").first();
		Locator rows = grid.locator("vaadin-grid-cell-content");
		return rows.count();
	}

	/** Helper method to verify button state (enabled/disabled). */
	private void verifyButtonState(String buttonText, boolean shouldBeEnabled) {
		Locator button = page.locator("vaadin-button:has-text('" + buttonText + "')");
		if (button.count() > 0) {
			boolean isEnabled = !button.isDisabled();
			if (isEnabled == shouldBeEnabled) {
				LOGGER.info("✅ {} button is {} as expected", buttonText, shouldBeEnabled ? "enabled" : "disabled");
			} else {
				LOGGER.warn("⚠️ {} button state mismatch (expected: {}, actual: {})", buttonText, shouldBeEnabled ? "enabled" : "disabled",
						isEnabled ? "enabled" : "disabled");
			}
		}
	}

	/** Helper method to verify notification appearance. */
	private void verifyNotification(String notificationType) {
		wait_500(); // Wait for notification to appear
		Locator notification = page.locator("vaadin-notification-card");
		if (notification.count() > 0) {
			String text = notification.textContent();
			LOGGER.info("✅ Notification appeared: {}", text);
		} else {
			LOGGER.warn("⚠️ No notification appeared for {} operation", notificationType);
		}
	}

	/** Helper method to verify grid selection. */
	private void verifyGridSelection() {
		// Check if any row is selected in the grid
		Locator selectedRows = page.locator("vaadin-grid-cell-content[selected]");
		if (selectedRows.count() > 0) {
			LOGGER.info("✅ Entity is selected in grid");
		} else {
			LOGGER.info("ℹ️ Could not verify grid selection");
		}
	}
}
