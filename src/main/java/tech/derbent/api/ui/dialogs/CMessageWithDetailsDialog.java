package tech.derbent.api.ui.dialogs;

import java.io.PrintWriter;
import java.io.StringWriter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextArea;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CButton;
import tech.derbent.api.views.dialogs.CDialog;

/** CMessageWithDetailsDialog - Dialog for displaying a user-friendly message with optional expandable exception details. Shows a clear message to the
 * user with a "Details" button that toggles visibility of exception stack trace in a scrollable text area. The dialog automatically adjusts its
 * height when details are shown or hidden. Layer: View (MVC) Usage: Use when you want to show a friendly message to users but also provide technical
 * details for troubleshooting. */
public final class CMessageWithDetailsDialog extends CDialog {

	private static final long serialVersionUID = 1L;
	private TextArea detailsArea;
	private Button detailsButton;
	private boolean detailsVisible = false;
	private final Exception exception;
	private final String message;
	private Div separator;

	/** Constructor for displaying a message with exception details.
	 * @param message   The user-friendly message to display
	 * @param exception The exception whose details can be expanded */
	public CMessageWithDetailsDialog(final String message, final Exception exception) {
		super();
		Check.notBlank(message, "Message cannot be empty");
		Check.notNull(exception, "Exception cannot be null");
		this.message = message;
		this.exception = exception;
		LOGGER.debug("CMessageWithDetailsDialog created with message: {} and exception: {}", message, exception.getClass().getSimpleName());
		try {
			setupDialog();
		} catch (final Exception e) {
			LOGGER.error("Error setting up message with details dialog", e);
		}
	}

	@Override
	public String getDialogTitleString() { return "Error Details"; }

	/** Formats the exception details including stack trace.
	 * @return Formatted exception details string */
	private String getExceptionDetails() {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		return sw.toString();
	}

	@Override
	protected Icon getFormIcon() { return VaadinIcon.EXCLAMATION_CIRCLE.create(); }

	@Override
	protected String getFormTitleString() { return "Error Details"; }

	/** Sets up the dialog buttons. */
	@Override
	protected void setupButtons() {
		// Details toggle button
		detailsButton = new Button("Show Details");
		detailsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		detailsButton.setIcon(VaadinIcon.ANGLE_DOWN.create());
		detailsButton.addClickListener(e -> toggleDetails());
		// OK button
		final CButton okButton = CButton.createPrimary("OK", null, e -> close());
		okButton.setAutofocus(true);
		buttonLayout.add(detailsButton, okButton);
	}

	/** Sets up the dialog content with message and expandable details area. */
	@Override
	protected void setupContent() {
		// User-friendly message
		final Div messageDiv = new Div();
		messageDiv.setText(message);
		messageDiv.getStyle().set("text-align", "center");
		messageDiv.getStyle().set("margin", "16px 0");
		messageDiv.getStyle().set("font-size", "16px");
		messageDiv.getStyle().set("color", "var(--lumo-body-text-color)");
		// Exception type hint (subtle)
		final Div exceptionTypeDiv = new Div();
		exceptionTypeDiv.setText("Exception: " + exception.getClass().getSimpleName());
		exceptionTypeDiv.getStyle().set("text-align", "center");
		exceptionTypeDiv.getStyle().set("margin", "8px 0");
		exceptionTypeDiv.getStyle().set("font-size", "12px");
		exceptionTypeDiv.getStyle().set("color", "var(--lumo-secondary-text-color)");
		exceptionTypeDiv.getStyle().set("font-style", "italic");
		// Details text area (initially hidden)
		detailsArea = new TextArea();
		detailsArea.setReadOnly(true);
		detailsArea.setWidthFull();
		detailsArea.setMaxHeight("300px");
		detailsArea.getStyle().set("font-family", "monospace");
		detailsArea.getStyle().set("font-size", "12px");
		detailsArea.setValue(getExceptionDetails());
		detailsArea.setVisible(false);
		// details are should also display horizontal scrollbar if needed
		detailsArea.getStyle().set("overflow", "auto");
		// Horizontal separator line (initially hidden)
		separator = new Div();
		separator.getStyle().set("border-top", "1px solid var(--lumo-contrast-10pct)");
		separator.getStyle().set("margin", "16px 0");
		separator.setVisible(false);
		mainLayout.add(messageDiv, exceptionTypeDiv, separator, detailsArea);
	}

	/** Toggles the visibility of the exception details. */
	private void toggleDetails() {
		detailsVisible = !detailsVisible;
		if (detailsVisible) {
			// Show details
			detailsButton.setText("Hide Details");
			detailsButton.setIcon(VaadinIcon.ANGLE_UP.create());
			detailsArea.setVisible(true);
			separator.setVisible(true);
			setHeight("600px"); // Expand dialog
		} else {
			// Hide details
			detailsButton.setText("Show Details");
			detailsButton.setIcon(VaadinIcon.ANGLE_DOWN.create());
			detailsArea.setVisible(false);
			separator.setVisible(false);
			setHeight("auto"); // Shrink dialog
		}
		// LOGGER.debug("Details visibility toggled to: {}", detailsVisible);
	}
}
