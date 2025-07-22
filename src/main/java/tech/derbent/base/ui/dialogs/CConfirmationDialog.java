package tech.derbent.base.ui.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * CConfirmationDialog - Dialog for user confirmations with Yes/No options.
 * Layer: View (MVC) Used for dangerous operations that require user
 * confirmation like deletions.
 */
public final class CConfirmationDialog extends CBaseInfoDialog {

	private static final long serialVersionUID = 1L;
	private final Runnable onConfirm;

	/**
	 * @param message   The confirmation message to display
	 * @param onConfirm Action to execute when user confirms
	 */
	public CConfirmationDialog(final String message, final Runnable onConfirm) {
		super("Confirm Action", message, VaadinIcon.QUESTION_CIRCLE.create());
		this.onConfirm = onConfirm;
		LOGGER.debug("CConfirmationDialog created with message: {}", message);
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