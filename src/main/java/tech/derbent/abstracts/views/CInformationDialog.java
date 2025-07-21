package tech.derbent.abstracts.views;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * CInformationDialog - Dialog for displaying informational messages to users.
 * Layer: View (MVC)
 * Used to provide helpful information or confirmation of successful actions.
 */
public final class CInformationDialog extends CBaseDialog {

	private static final long serialVersionUID = 1L;

	/**
	 * @param message The information message to display
	 */
	public CInformationDialog(final String message) {
		super("Information", message);
		LOGGER.debug("CInformationDialog created with message: {}", message);
	}

	/**
	 * @param title   Custom information title
	 * @param message The information message to display
	 */
	public CInformationDialog(final String title, final String message) {
		super(title, message);
		LOGGER.debug("CInformationDialog created with title: {} and message: {}", title, message);
	}

	@Override
	protected Icon getDialogIcon() {
		final Icon icon = VaadinIcon.INFO_CIRCLE.create();
		icon.setColor("var(--lumo-primary-color)");
		return icon;
	}

	@Override
	protected ButtonVariant getOkButtonVariant() {
		return ButtonVariant.LUMO_PRIMARY;
	}
}