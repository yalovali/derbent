package tech.derbent.api.ui.dialogs;

import java.lang.reflect.InvocationTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.ui.component.basic.CButton;

/** CConfirmationDialog - Dialog for user confirmations with Yes/No options. Layer: View (MVC) Used for dangerous operations that require user
 * confirmation like deletions. */
public final class CDialogConfirmation extends CDialogInfoBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogConfirmation.class);
	private static final long serialVersionUID = 1L;
	private final Runnable onConfirm;

	/** @param message The confirmation message to display
	 * @param onConfirm Action to execute when user confirms
	 * @throws Exception
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException */
	public CDialogConfirmation(final String message, final Runnable onConfirm) throws Exception {
		super("Confirm Action", message, VaadinIcon.QUESTION_CIRCLE.create());
		this.onConfirm = onConfirm;
		LOGGER.debug("CConfirmationDialog created with message: {}", message);
	}

	@Override
	protected void setupButtons() {
		final CButton yesButton = CButton.createPrimary("Yes", null, e -> {
			close();
			if (onConfirm != null) {
				onConfirm.run();
			}
		});
		yesButton.setAutofocus(false);
		final CButton noButton = CButton.createTertiary("No", null, e -> close());
		noButton.setAutofocus(true);
		buttonLayout.add(yesButton, noButton);
	}
}
