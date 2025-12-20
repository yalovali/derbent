package tech.derbent.api.ui.dialogs;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.progressbar.ProgressBar;
import tech.derbent.api.utils.Check;

/** CDialogProgress - Dialog for displaying a modal progress indicator without actions. Layer: View (MVC) Used for long-running operations where the
 * user should wait until completion. */
public final class CDialogProgress extends CDialog {

	private static final long serialVersionUID = 1L;
	private final String message;
	private final String title;

	public CDialogProgress(final String title, final String message) {
		super();
		Check.notBlank(title, "Progress dialog title cannot be blank");
		Check.notBlank(message, "Progress dialog message cannot be blank");
		this.title = title;
		this.message = message;
		try {
			setupDialog();
			setCloseOnEsc(false);
			setCloseOnOutsideClick(false);
		} catch (final Exception e) {
			LOGGER.error("Error setting up progress dialog", e);
		}
	}

	@Override
	public String getDialogTitleString() { return title; }

	@Override
	protected Icon getFormIcon() { return VaadinIcon.SPINNER.create(); }

	@Override
	protected String getFormTitleString() { return title; }

	@Override
	protected void setupButtons() {
		// No buttons for progress dialog.
	}

	@Override
	protected void setupContent() {
		final Div messageDiv = new Div();
		messageDiv.setText(message);
		messageDiv.getStyle().set("text-align", "center");
		messageDiv.getStyle().set("margin", "16px 0");
		final ProgressBar progressBar = new ProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setWidthFull();
		mainLayout.add(messageDiv, progressBar);
	}
}
