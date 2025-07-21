package tech.derbent.abstracts.views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * CBaseDialog - Abstract base class for simple message dialogs.
 * Layer: View (MVC)
 * Provides common functionality for warning, information, and exception dialogs.
 * These dialogs are for showing messages to users, not for data editing.
 */
public abstract class CBaseDialog extends Dialog {

	private static final long serialVersionUID = 1L;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	protected final String message;
	protected final String title;
	protected VerticalLayout mainLayout;
	protected HorizontalLayout buttonLayout;

	/**
	 * @param title   The dialog title
	 * @param message The message to display to the user
	 */
	public CBaseDialog(final String title, final String message) {
		LOGGER.debug("CBaseDialog constructor called for {}", getClass().getSimpleName());
		this.title = title;
		this.message = message;
		setupDialog();
		setupContent();
		setupButtons();
	}

	/**
	 * Child classes must implement to provide appropriate icon for the dialog type.
	 */
	protected abstract Icon getDialogIcon();

	/**
	 * Child classes can override to provide different button variants.
	 */
	protected ButtonVariant getOkButtonVariant() {
		return ButtonVariant.LUMO_PRIMARY;
	}

	/**
	 * Sets up the dialog content with icon, title and message.
	 */
	protected void setupContent() {
		mainLayout = new VerticalLayout();
		mainLayout.setPadding(true);
		mainLayout.setSpacing(true);
		mainLayout.setAlignItems(VerticalLayout.Alignment.CENTER);

		// Header with icon and title
		final HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.setAlignItems(HorizontalLayout.Alignment.CENTER);
		headerLayout.setSpacing(true);
		
		final Icon icon = getDialogIcon();
		icon.setSize("24px");
		
		final H3 titleComponent = new H3(title);
		titleComponent.getStyle().set("margin", "0");
		
		headerLayout.add(icon, titleComponent);

		// Message content
		final Div messageDiv = new Div();
		messageDiv.setText(message);
		messageDiv.getStyle().set("text-align", "center");
		messageDiv.getStyle().set("margin", "16px 0");

		mainLayout.add(headerLayout, messageDiv);
		add(mainLayout);
	}

	/**
	 * Sets up the dialog properties (modal, size, etc.)
	 */
	protected void setupDialog() {
		setModal(true);
		setCloseOnEsc(true);
		setCloseOnOutsideClick(false);
		setWidth("400px");
		setResizable(false);
	}

	/**
	 * Sets up the OK button.
	 */
	protected void setupButtons() {
		final Button okButton = new Button("OK", e -> close());
		okButton.addThemeVariants(getOkButtonVariant());
		okButton.setAutofocus(true);
		
		buttonLayout = new HorizontalLayout(okButton);
		buttonLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.CENTER);
		buttonLayout.getStyle().set("margin-top", "16px");
		
		mainLayout.add(buttonLayout);
	}
}