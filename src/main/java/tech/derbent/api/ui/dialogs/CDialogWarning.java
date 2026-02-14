// package tech.derbent.api.views;
package tech.derbent.api.ui.dialogs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.icon.VaadinIcon;

/** CWarningDialog - Dialog for displaying warning messages to users. Layer: View (MVC) Used when an action cannot be performed due to missing
 * prerequisites or invalid state. */
public final class CDialogWarning extends CDialogInfoBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogWarning.class);
	private static final long serialVersionUID = 1L;

	/** @param message The warning message to display
	 * @throws Exception */
	public CDialogWarning(final String message) {
		this("Warning", message);
	}

	/** @param title   The dialog title
	 * @param message The warning message to display */
	public CDialogWarning(final String title, final String message) {
		super(title, message, VaadinIcon.WARNING.create());
		LOGGER.debug("CWarningDialog created with message: {}", message);
	}
}
