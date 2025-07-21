package tech.derbent.abstracts.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * CConfirmationDialog - Dialog for user confirmations with Yes/No options.
 * Layer: View (MVC)
 * Used for dangerous operations that require user confirmation like deletions.
 */
public final class CConfirmationDialog extends CBaseDialog {

	private static final long serialVersionUID = 1L;
	private final Runnable onConfirm;

	/**
	 * @param message   The confirmation message to display
	 * @param onConfirm Action to execute when user confirms
	 */
	public CConfirmationDialog(final String message, final Runnable onConfirm) {
		super("Confirm Action", message);
		this.onConfirm = onConfirm;
		LOGGER.debug("CConfirmationDialog created with message: {}", message);
	}

	/**
	 * @param title     Custom confirmation title
	 * @param message   The confirmation message to display
	 * @param onConfirm Action to execute when user confirms
	 */
	public CConfirmationDialog(final String title, final String message, final Runnable onConfirm) {
		super(title, message);
		this.onConfirm = onConfirm;
		LOGGER.debug("CConfirmationDialog created with title: {} and message: {}", title, message);
	}

	@Override
	protected Icon getDialogIcon() {
		final Icon icon = VaadinIcon.QUESTION_CIRCLE.create();
		icon.setColor("var(--lumo-warning-color)");
		return icon;
	}

	@Override
	protected void setupButtons() {
		final Button yesButton = new Button("Yes", e -> {
			if (onConfirm != null) {
				onConfirm.run();
			}
			close();
		});
		yesButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		yesButton.setAutofocus(false);

		final Button noButton = new Button("No", e -> close());
		noButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		noButton.setAutofocus(true);

		buttonLayout = new HorizontalLayout(noButton, yesButton);
		buttonLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.CENTER);
		buttonLayout.getStyle().set("margin-top", "16px");

		mainLayout.add(buttonLayout);
	}
}