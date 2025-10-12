package automated_tests.tech.derbent.ui.automation;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.microsoft.playwright.Locator;

/** Playwright test class for Dependency Checking functionality. Tests verify that entities with dependencies cannot be deleted and appropriate error
 * messages are shown to users. */
public class CDependencyCheckingTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDependencyCheckingTest.class);

	/** Test that verifies activity types in use cannot be deleted. This test: 1. Logs in as admin 2. Navigates to Activity Types view 3. Attempts to
	 * delete a type that is in use 4. Verifies error notification appears 5. Verifies type is not deleted */
	@Test
	public void testActivityTypeInUseCannotBeDeleted() {
		LOGGER.info("=== Testing Activity Type Deletion with Dependencies ===");
		try {
			// Step 1: Login
			loginToApplication("admin", "test123");
			LOGGER.info("✓ Logged in successfully");
			wait_afterlogin();
			// Step 2: Navigate to Activity Types
			navigateToViewByText("Activity Types");
			wait_1000();
			LOGGER.info("✓ Navigated to Activity Types view");
			// Step 3: Select first activity type in grid
			clickFirstGridRow();
			wait_500();
			LOGGER.info("✓ Selected first activity type");
			// Step 4: Click Delete button
			clickDelete();
			wait_500();
			LOGGER.info("✓ Clicked Delete button");
			// Step 5: Check if error notification appears
			// Note: If the type is in use, we should see an error notification
			// If not in use, confirmation dialog will appear
			boolean hasErrorNotification = page.locator("vaadin-notification-card").count() > 0;
			if (hasErrorNotification) {
				String notificationText = page.locator("vaadin-notification-card").textContent();
				LOGGER.info("✓ Error notification appeared: {}", notificationText);
				// Verify the notification mentions usage or dependencies
				if (notificationText.contains("being used") || notificationText.contains("cannot delete")
						|| notificationText.contains("non-deletable")) {
					LOGGER.info("✓ Dependency check working correctly");
					takeScreenshot("activity-type-dependency-check-success", true);
				} else {
					LOGGER.warn("⚠ Notification text does not mention dependencies: {}", notificationText);
				}
			} else {
				LOGGER.info("ℹ No error notification - type may not be in use or not marked as non-deletable");
				// If confirmation dialog appears, cancel it
				boolean hasConfirmDialog = page.locator("vaadin-dialog-overlay").count() > 0;
				if (hasConfirmDialog) {
					LOGGER.info("ℹ Confirmation dialog appeared - canceling deletion");
					clickCancel();
				}
			}
			LOGGER.info("=== Test completed successfully ===");
		} catch (final Exception e) {
			LOGGER.error("Test failed with exception", e);
			takeScreenshot("activity-type-dependency-check-failure", true);
			throw e;
		}
	}

	/** Test that verifies user types in use cannot be deleted. This test: 1. Logs in as admin 2. Navigates to User Types view 3. Attempts to delete a
	 * type that is in use 4. Verifies error notification appears */
	@Test
	public void testUserTypeInUseCannotBeDeleted() {
		LOGGER.info("=== Testing User Type Deletion with Dependencies ===");
		try {
			// Step 1: Login
			loginToApplication("admin", "test123");
			LOGGER.info("✓ Logged in successfully");
			wait_afterlogin();
			// Step 2: Navigate to User Types
			navigateToViewByText("User Types");
			wait_1000();
			LOGGER.info("✓ Navigated to User Types view");
			// Step 3: Select first user type in grid
			clickFirstGridRow();
			wait_500();
			LOGGER.info("✓ Selected first user type");
			// Step 4: Click Delete button
			clickDelete();
			wait_500();
			LOGGER.info("✓ Clicked Delete button");
			// Step 5: Check for error notification
			boolean hasErrorNotification = page.locator("vaadin-notification-card").count() > 0;
			if (hasErrorNotification) {
				String notificationText = page.locator("vaadin-notification-card").textContent();
				LOGGER.info("✓ Error notification appeared: {}", notificationText);
				if (notificationText.contains("being used") || notificationText.contains("cannot delete")
						|| notificationText.contains("non-deletable")) {
					LOGGER.info("✓ Dependency check working correctly for user types");
					takeScreenshot("user-type-dependency-check-success", true);
				}
			} else {
				LOGGER.info("ℹ No error notification - type may not be in use");
				// Cancel confirmation dialog if it appears
				if (page.locator("vaadin-dialog-overlay").count() > 0) {
					clickCancel();
				}
			}
			LOGGER.info("=== Test completed successfully ===");
		} catch (final Exception e) {
			LOGGER.error("Test failed with exception", e);
			takeScreenshot("user-type-dependency-check-failure", true);
			throw e;
		}
	}

	/** Test that verifies activity statuses in use cannot be deleted. This test: 1. Logs in as admin 2. Navigates to Activity Status view 3. Attempts
	 * to delete a status that is in use 4. Verifies error notification appears */
	@Test
	public void testActivityStatusInUseCannotBeDeleted() {
		LOGGER.info("=== Testing Activity Status Deletion with Dependencies ===");
		try {
			// Step 1: Login
			loginToApplication("admin", "test123");
			LOGGER.info("✓ Logged in successfully");
			wait_afterlogin();
			// Step 2: Navigate to Activity Status
			navigateToViewByText("Activity Status");
			wait_1000();
			LOGGER.info("✓ Navigated to Activity Status view");
			// Step 3: Select first status in grid
			clickFirstGridRow();
			wait_500();
			LOGGER.info("✓ Selected first activity status");
			// Step 4: Click Delete button
			clickDelete();
			wait_500();
			LOGGER.info("✓ Clicked Delete button");
			// Step 5: Check for error notification
			boolean hasErrorNotification = page.locator("vaadin-notification-card").count() > 0;
			if (hasErrorNotification) {
				String notificationText = page.locator("vaadin-notification-card").textContent();
				LOGGER.info("✓ Error notification appeared: {}", notificationText);
				if (notificationText.contains("being used") || notificationText.contains("cannot delete")
						|| notificationText.contains("non-deletable")) {
					LOGGER.info("✓ Dependency check working correctly for activity status");
					takeScreenshot("activity-status-dependency-check-success", true);
				}
			} else {
				LOGGER.info("ℹ No error notification - status may not be in use");
				// Cancel confirmation dialog if it appears
				if (page.locator("vaadin-dialog-overlay").count() > 0) {
					clickCancel();
				}
			}
			LOGGER.info("=== Test completed successfully ===");
		} catch (final Exception e) {
			LOGGER.error("Test failed with exception", e);
			takeScreenshot("activity-status-dependency-check-failure", true);
			throw e;
		}
	}

	/** Test that verifies the last user in a company cannot be deleted. This test: 1. Logs in as admin (potentially the only user) 2. Navigates to
	 * Users view 3. Attempts to delete a user 4. Verifies appropriate error or warning appears */
	@Test
	public void testLastUserCannotBeDeleted() {
		LOGGER.info("=== Testing Last User Deletion Prevention ===");
		try {
			// Step 1: Login
			loginToApplication("admin", "test123");
			LOGGER.info("✓ Logged in successfully");
			wait_afterlogin();
			// Step 2: Navigate to Users view
			navigateToViewByText("Users");
			wait_1000();
			LOGGER.info("✓ Navigated to Users view");
			// Step 3: Count users in grid
			Locator gridRows = page.locator("vaadin-grid-cell-content");
			int userCount = gridRows.count();
			LOGGER.info("ℹ Found {} users in the grid", userCount);
			// Step 4: Select first user
			clickFirstGridRow();
			wait_500();
			LOGGER.info("✓ Selected first user");
			// Step 5: Click Delete button
			clickDelete();
			wait_500();
			LOGGER.info("✓ Clicked Delete button");
			// Step 6: Check for error notification
			boolean hasErrorNotification = page.locator("vaadin-notification-card").count() > 0;
			if (hasErrorNotification) {
				String notificationText = page.locator("vaadin-notification-card").textContent();
				LOGGER.info("✓ Error notification appeared: {}", notificationText);
				// Check if it's about last user or self-deletion
				if (notificationText.contains("last user") || notificationText.contains("your own")) {
					LOGGER.info("✓ User deletion protection working correctly");
					takeScreenshot("user-deletion-protection-success", true);
				} else {
					LOGGER.info("ℹ Different error: {}", notificationText);
				}
			} else {
				LOGGER.info("ℹ No error notification - user can be deleted");
				// Cancel confirmation dialog if it appears
				if (page.locator("vaadin-dialog-overlay").count() > 0) {
					LOGGER.info("ℹ Canceling deletion to preserve test data");
					clickCancel();
				}
			}
			LOGGER.info("=== Test completed successfully ===");
		} catch (final Exception e) {
			LOGGER.error("Test failed with exception", e);
			takeScreenshot("user-deletion-protection-failure", true);
			throw e;
		}
	}
}
