package tech.derbent.base.ui.dialogs;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * CInformationDialog - Dialog for displaying informational messages to users.
 * Layer: View (MVC) Used to provide helpful information or confirmation of
 * successful actions.
 */
public final class CInformationDialog extends CBaseInfoDialog {

	private static final long serialVersionUID = 1L;

	/**
	 * @param message The information message to display
	 */
	public CInformationDialog(final String message) {
		super("Information", message, VaadinIcon.INFO_CIRCLE.create());
		LOGGER.debug("CInformationDialog created with message: {}", message);
	}

	@Override
	protected ButtonVariant getOkButtonVariant() { return ButtonVariant.LUMO_PRIMARY; }
}