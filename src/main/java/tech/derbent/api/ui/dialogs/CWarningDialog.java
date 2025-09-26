// package tech.derbent.api.views;
package tech.derbent.api.ui.dialogs;

import com.vaadin.flow.component.icon.VaadinIcon;

/** CWarningDialog - Dialog for displaying warning messages to users. Layer: View (MVC) Used when an action cannot be performed due to missing
 * prerequisites or invalid state. */
public final class CWarningDialog extends CBaseInfoDialog {

	private static final long serialVersionUID = 1L;

	/** @param message The warning message to display */
	public CWarningDialog(final String message) {
		super("Warning", message, VaadinIcon.WARNING.create());
		LOGGER.debug("CWarningDialog created with message: {}", message);
	}
}
