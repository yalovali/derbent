package tech.derbent.abstracts.views;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * CWarningDialog - Dialog for displaying warning messages to users.
 * Layer: View (MVC)
 * Used when an action cannot be performed due to missing prerequisites or invalid state.
 */
public final class CWarningDialog extends CBaseDialog {

	private static final long serialVersionUID = 1L;

	/**
	 * @param message The warning message to display
	 */
	public CWarningDialog(final String message) {
		super("Warning", message);
		LOGGER.debug("CWarningDialog created with message: {}", message);
	}

	/**
	 * @param title   Custom warning title
	 * @param message The warning message to display
	 */
	public CWarningDialog(final String title, final String message) {
		super(title, message);
		LOGGER.debug("CWarningDialog created with title: {} and message: {}", title, message);
	}

	@Override
	protected Icon getDialogIcon() {
		final Icon icon = VaadinIcon.WARNING.create();
		icon.setColor("var(--lumo-warning-color)");
		return icon;
	}

	@Override
	protected ButtonVariant getOkButtonVariant() {
		return ButtonVariant.LUMO_TERTIARY;
	}
}