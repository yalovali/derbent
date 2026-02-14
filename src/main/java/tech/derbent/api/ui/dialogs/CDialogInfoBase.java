package tech.derbent.api.ui.dialogs;

import java.lang.reflect.InvocationTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.icon.Icon;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.constants.CUIConstants;
import tech.derbent.api.utils.Check;

public abstract class CDialogInfoBase extends CDialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogInfoBase.class);
	private static final long serialVersionUID = 1L;
	private final Icon icon;
	private final String message;
	private final String title;

	/** @param title The dialog title
	 * @param message The message to display to the user
	 * @throws Exception
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException */
	public CDialogInfoBase(final String title, final String message, final Icon icon) {
		Check.notNull(title, "Title cannot be null");
		Check.notNull(message, "Message cannot be null");
		Check.notNull(icon, "Icon cannot be null");
		this.title = title;
		this.message = message;
		this.icon = icon;
		try {
			icon.setColor("var(--lumo-warning-color)");
			setupDialog();// call setupDialog() to initialize the dialog
		} catch (final Exception e) {
			LOGGER.error("Error setting up dialog. {}", e.getMessage());
		}
	}

	@Override
	public String getDialogTitleString() { return title; }

	@Override
	protected Icon getFormIcon() { return icon; }

	@Override
	protected String getFormTitleString() { return title; }

	/** Sets up the OK button. */
	@Override
	protected void setupButtons() {
		final CButton okButton = CButton.createPrimary("OK", null, event -> close());
		okButton.setAutofocus(true);
		buttonLayout.add(okButton);
	}

	/** Sets up the dialog content with icon and message. */
	@Override
	protected void setupContent() {
		// Header with icon and title (title already added by CDialog) message content
		final CDiv messageArea = createScrollableResultArea("dialog-info-message-area", CUIConstants.TEXTAREA_HEIGHT_STANDARD);
		messageArea.setText(message);
		messageArea.getStyle().set("margin", "16px 0");
		messageArea.getStyle().set("text-align", "left");
		messageArea.getStyle().set("white-space", "pre-wrap");
		messageArea.getStyle().set("overflow-wrap", "anywhere");
		messageArea.getStyle().set("word-break", "break-word");
		messageArea.getStyle().set("overflow", "auto");
		mainLayout.add(messageArea);
	}
}
