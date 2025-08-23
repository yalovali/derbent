package tech.derbent.base.ui.dialogs;

import java.lang.reflect.InvocationTargetException;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.CButton;
import tech.derbent.abstracts.views.CDialog;

public abstract class CBaseInfoDialog extends CDialog {

	private static final long serialVersionUID = 1L;
	private final String message;
	private final String title;
	private final Icon icon;

	/** @param title The dialog title
	 * @param message The message to display to the user
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException */
	public CBaseInfoDialog(final String title, final String message, final Icon icon) {
		super();
		Check.notNull(title, "Title cannot be null");
		Check.notNull(message, "Message cannot be null");
		Check.notNull(icon, "Icon cannot be null");
		this.title = title;
		this.message = message;
		this.icon = icon;
		icon.setColor("var(--lumo-warning-color)");
		try {
			setupDialog();// call setupDialog() to initialize the dialog
		} catch (final Exception e) {
			LOGGER.error("Error setting up dialog: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to setup dialog", e);
		}
	}

	@Override
	protected Icon getFormIcon() { return icon; }

	@Override
	protected String getFormTitle() { return title; }

	@Override
	public String getHeaderTitle() { return title; }

	/** Sets up the OK button. */
	@Override
	protected void setupButtons() {
		final CButton okButton = CButton.createPrimary("OK", null, e -> close());
		okButton.setAutofocus(true);
		buttonLayout.add(okButton);
	}

	/** Sets up the dialog content with icon and message. */
	@Override
	protected void setupContent() {
		// Header with icon and title (title already added by CDialog) Message content
		final Div messageDiv = new Div();
		messageDiv.setText(message);
		messageDiv.getStyle().set("text-align", "center");
		messageDiv.getStyle().set("margin", "16px 0");
		mainLayout.add(messageDiv);
	}
}
