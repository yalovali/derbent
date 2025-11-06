package tech.derbent.api.ui.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import tech.derbent.api.ui.dialogs.CConfirmationDialog;
import tech.derbent.api.ui.dialogs.CExceptionDialog;
import tech.derbent.api.ui.dialogs.CInformationDialog;
import tech.derbent.api.ui.dialogs.CMessageWithDetailsDialog;
import tech.derbent.api.ui.dialogs.CWarningDialog;
import tech.derbent.api.utils.Check;

/** CNotificationService - Centralized service for all notifications and user messages. Provides consistent styling, positioning, and behavior for: -
 * Toast notifications (temporary messages) - Modal dialogs (information, warnings, errors, confirmations) - Success/error feedback messages Layer:
 * Service (MVC) Usage: Inject this service into views and components to show user messages */
@Service
public class CNotificationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CNotificationService.class);
	private static final int LONG_DURATION = 8000;
	private static final int MEDIUM_DURATION = 5000;
	// Standard durations in milliseconds
	private static final int SHORT_DURATION = 2000;

	/** Shows a message with expandable exception details (modal with Details toggle and OK button). The dialog displays a user-friendly message and
	 * allows users to expand/collapse technical exception details.
	 * @param message   The user-friendly message to display
	 * @param exception The exception whose details can be expanded */
	public static void showException(final String message, final Exception exception) {
		Check.notBlank(message, "Message cannot be empty");
		Check.notNull(exception, "Exception cannot be null");
		LOGGER.debug("Showing message with details dialog: {} for exception: {}", message, exception.getClass().getSimpleName(), exception);
		final CMessageWithDetailsDialog dialog = new CMessageWithDetailsDialog(message, exception);
		dialog.open();
	}

	/** Shows a confirmation dialog (modal with Yes/No buttons)
	 * @throws Exception */
	public void showConfirmationDialog(final String message, final Runnable onConfirm) throws Exception {
		Check.notBlank(message, "Confirmation message cannot be empty");
		LOGGER.debug("Showing confirmation dialog: {}", message);
		final CConfirmationDialog dialog = new CConfirmationDialog(message, onConfirm);
		dialog.open();
	}

	/** Shows a confirmation dialog with custom action text
	 * @throws Exception */
	public void showConfirmationDialog(final String message, final String confirmText, final Runnable onConfirm) throws Exception {
		Check.notBlank(message, "Confirmation message cannot be empty");
		Check.notBlank(confirmText, "Confirm text cannot be empty");
		LOGGER.debug("Showing confirmation dialog: {} with confirm text: {}", message, confirmText);
		// Note: Current CConfirmationDialog doesn't support custom button text
		// Using standard confirmation for now - this could be enhanced later
		final CConfirmationDialog dialog = new CConfirmationDialog(message, onConfirm);
		dialog.open();
	}
	// Convenience methods for common operations

	/** Shows error message for create operations */
	public void showCreateError() {
		showError("An error occurred while creating. Please try again.");
	}

	/** Shows success message for create operations */
	public void showCreateSuccess() {
		showSuccess("Item created successfully");
	}

	/** Shows a custom notification with specified duration and position */
	public void showCustom(final String message, final int durationMs, final Notification.Position position, final NotificationVariant... variants) {
		Check.notBlank(message, "Custom message cannot be empty");
		Check.notNull(position, "Position cannot be null");
		LOGGER.debug("Showing custom notification: {} at {} for {}ms", message, position, durationMs);
		final Notification notification = Notification.show(message, durationMs, position);
		if ((variants != null) && (variants.length > 0)) {
			notification.addThemeVariants(variants);
		}
	}

	/** Shows error message for delete operations */
	public void showDeleteError() {
		showError("An error occurred while deleting. Please try again.");
	}

	/** Shows success message for delete operations */
	public void showDeleteSuccess() {
		showSuccess("Item deleted successfully");
	}

	/** Shows an error notification toast (red, middle, long duration) */
	public void showError(final String message) {
		Check.notBlank(message, "Error message cannot be empty");
		LOGGER.debug("Showing error notification: {}", message);
		final Notification notification = Notification.show(message, LONG_DURATION, Notification.Position.MIDDLE);
		notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
	}

	/** Shows an error dialog (modal with OK button) */
	public void showErrorDialog(final Exception exception) {
		Check.notNull(exception, "Exception cannot be null");
		LOGGER.debug("Showing error dialog for exception: {}", exception.getClass().getSimpleName());
		final CExceptionDialog dialog = new CExceptionDialog(exception);
		dialog.open();
	}

	/** Shows an error dialog with custom message */
	public void showErrorDialog(final String message) {
		Check.notBlank(message, "Error dialog message cannot be empty");
		LOGGER.debug("Showing error dialog: {}", message);
		final CExceptionDialog dialog = new CExceptionDialog(new RuntimeException(message));
		dialog.open();
	}

	/** Shows an info notification toast (blue, bottom-start, medium duration) */
	public void showInfo(final String message) {
		Check.notBlank(message, "Info message cannot be empty");
		LOGGER.debug("Showing info notification: {}", message);
		final Notification notification = Notification.show(message, MEDIUM_DURATION, Notification.Position.BOTTOM_START);
		notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
	}

	/** Shows an information dialog (modal with OK button) */
	public void showInfoDialog(final String message) {
		Check.notBlank(message, "Info dialog message cannot be empty");
		LOGGER.debug("Showing info dialog: {}", message);
		final CInformationDialog dialog = new CInformationDialog(message);
		dialog.open();
	}

	/** Shows error for optimistic locking failures */
	public void showOptimisticLockingError() {
		showError("Error updating the data. Somebody else has updated the record while you were making changes.");
	}

	/** Shows error message for save operations */
	public void showSaveError() {
		showError("An error occurred while saving. Please try again.");
	}

	/** Shows success message for save operations */
	public void showSaveSuccess() {
		showSuccess("Data saved successfully");
	}

	/** Shows a success notification toast (green, bottom-start, short duration) */
	public void showSuccess(final String message) {
		Check.notBlank(message, "Success message cannot be empty");
		LOGGER.debug("Showing success notification: {}", message);
		final Notification notification = Notification.show(message, SHORT_DURATION, Notification.Position.BOTTOM_START);
		notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
	}

	/** Shows warning for validation errors */
	public void showValidationWarning(final String fieldName) {
		showWarning("Please check the " + fieldName + " field and try again.");
	}

	/** Shows a warning notification toast (orange, top-center, medium duration) */
	public void showWarning(final String message) {
		Check.notBlank(message, "Warning message cannot be empty");
		LOGGER.debug("Showing warning notification: {}", message);
		final Notification notification = Notification.show(message, MEDIUM_DURATION, Notification.Position.TOP_CENTER);
		notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
	}

	/** Shows a warning dialog (modal with OK button) */
	public void showWarningDialog(final String message) {
		Check.notBlank(message, "Warning dialog message cannot be empty");
		LOGGER.debug("Showing warning dialog: {}", message);
		final CWarningDialog dialog = new CWarningDialog(message);
		dialog.open();
	}
}
