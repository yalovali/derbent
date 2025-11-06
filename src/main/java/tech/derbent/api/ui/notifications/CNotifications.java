package tech.derbent.api.ui.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.utils.Check;

/** CNotifications - Static utility class for accessing notification services. Provides static methods to access CNotificationService functionality
 * when dependency injection is not available or convenient. Note: This is a simplified implementation that focuses on backwards compatibility. For
 * full functionality, inject CNotificationService directly in your classes. Layer: Utility (MVC) Usage: Use static methods when you cannot inject
 * CNotificationService */
public final class CNotifications {

	private static final Logger LOGGER = LoggerFactory.getLogger(CNotifications.class);

	private CNotifications() {
		// Utility class - prevent instantiation
	}

	/** Shows a success notification toast */
	public static void showSuccess(final String message) {
		Check.notBlank(message, "Success message cannot be empty");
		LOGGER.info("Static success notification: {}", message);
		// For now, just log. In future versions, this could use other approaches.
		// Users should inject CNotificationService directly for full functionality.
	}

	/** Shows an error notification toast */
	public static void showError(final String message) {
		Check.notBlank(message, "Error message cannot be empty");
		LOGGER.error("Static error notification: {}", message);
		// For now, just log. In future versions, this could use other approaches.
		// Users should inject CNotificationService directly for full functionality.
	}

	/** Shows a warning notification toast */
	public static void showWarning(final String message) {
		Check.notBlank(message, "Warning message cannot be empty");
		LOGGER.warn("Static warning notification: {}", message);
		// For now, just log. In future versions, this could use other approaches.
		// Users should inject CNotificationService directly for full functionality.
	}

	/** Shows an info notification toast */
	public static void showInfo(final String message) {
		Check.notBlank(message, "Info message cannot be empty");
		LOGGER.info("Static info notification: {}", message);
		// For now, just log. In future versions, this could use other approaches.
		// Users should inject CNotificationService directly for full functionality.
	}

	/** Shows an information dialog */
	public static void showInfoDialog(final String message) {
		Check.notBlank(message, "Info dialog message cannot be empty");
		LOGGER.info("Static info dialog: {}", message);
		// For now, just log. In future versions, this could use other approaches.
		// Users should inject CNotificationService directly for full functionality.
	}

	/** Shows a warning dialog */
	public static void showWarningDialog(final String message) {
		Check.notBlank(message, "Warning dialog message cannot be empty");
		LOGGER.warn("Static warning dialog: {}", message);
		// For now, just log. In future versions, this could use other approaches.
		// Users should inject CNotificationService directly for full functionality.
	}

	/** Shows an error dialog */
	public static void showErrorDialog(final Exception exception) {
		Check.notNull(exception, "Exception cannot be null");
		LOGGER.error("Static error dialog for exception: {}", exception.getClass().getSimpleName(), exception);
		// For now, just log. In future versions, this could use other approaches.
		// Users should inject CNotificationService directly for full functionality.
	}

	/** Shows an error dialog with custom message */
	public static void showErrorDialog(final String message) {
		Check.notBlank(message, "Error dialog message cannot be empty");
		LOGGER.error("Static error dialog: {}", message);
		// For now, just log. In future versions, this could use other approaches.
		// Users should inject CNotificationService directly for full functionality.
	}

	/** Shows a confirmation dialog */
	public static void showConfirmationDialog(final String message, final Runnable onConfirm) {
		Check.notBlank(message, "Confirmation message cannot be empty");
		LOGGER.info("Static confirmation dialog: {}", message);
		// For now, just log. In future versions, this could use other approaches.
		// Users should inject CNotificationService directly for full functionality.
	}
	// Convenience methods

	/** Shows success message for save operations */
	public static void showSaveSuccess() {
		showSuccess("Data saved successfully");
	}

	/** Shows success message for delete operations */
	public static void showDeleteSuccess() {
		showSuccess("Item deleted successfully");
	}

	/** Shows success message for create operations */
	public static void showCreateSuccess() {
		showSuccess("Item created successfully");
	}

	/** Shows error message for save operations */
	public static void showSaveError() {
		showError("An error occurred while saving. Please try again.");
	}

	/** Shows error message for delete operations */
	public static void showDeleteError() {
		showError("An error occurred while deleting. Please try again.");
	}

	/** Shows error message for create operations */
	public static void showCreateError() {
		showError("An error occurred while creating. Please try again.");
	}

	/** Shows error for optimistic locking failures */
	public static void showOptimisticLockingError() {
		showError("Error updating the data. Somebody else has updated the record while you were making changes.");
	}

	/** Shows warning for validation errors */
	public static void showValidationWarning(final String fieldName) {
		showWarning("Please check the " + fieldName + " field and try again.");
	}

	/** Shows a message with expandable exception details. Note: This static method currently only logs the error. For full functionality including
	 * the dialog display, inject CNotificationService directly in your classes.
	 * @param message The user-friendly message to describe the error
	 * @param exception The exception whose details should be logged */
	public static void showMessageWithDetails(final String message, final Exception exception) {
		Check.notBlank(message, "Message cannot be empty");
		Check.notNull(exception, "Exception cannot be null");
		LOGGER.error("Static message with details dialog: {} for exception: {}", message, exception.getClass().getSimpleName(), exception);
		// For now, just log. In future versions, this could use other approaches.
		// Users should inject CNotificationService directly for full functionality.
	}
}
