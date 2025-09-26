package tech.derbent.api.ui.dialogs;

import com.vaadin.flow.component.icon.VaadinIcon;

/** CExceptionDialog - Dialog for displaying error messages and exceptions to users. Layer: View (MVC) Used when an error occurs that the user should
 * be informed about. */
public final class CExceptionDialog extends CBaseInfoDialog {

	private static final long serialVersionUID = 1L;

	/** Convenience constructor for displaying exception information.
	 * @param exception The exception to display */
	public CExceptionDialog(final Exception exception) {
		super("Error", exception.getMessage() != null ? exception.getMessage() : "An unexpected error occurred",
				VaadinIcon.EXCLAMATION_CIRCLE.create());
		LOGGER.debug("CExceptionDialog created for exception: {}", exception.getClass().getSimpleName());
	}
}
