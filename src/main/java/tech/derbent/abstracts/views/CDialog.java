package tech.derbent.abstracts.views;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Abstract base class for data-aware dialogs. Uses generics to allow any data
 * type. Handles dialog setup, form layout, and save/cancel button logic. Child
 * classes must implement form population and validation.
 */
public abstract class CDialog<T> extends Dialog {

	private static final long serialVersionUID = 1L;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	protected final T data;
	protected final Consumer<T> onSave;
	protected final boolean isNew;
	protected VerticalLayout mainLayout;
	protected FormLayout formLayout;

	/**
	 * @param data   The data object to edit or create.
	 * @param onSave Callback to execute on save.
	 * @param isNew  True if creating new, false if editing.
	 */
	public CDialog(final T data, final Consumer<T> onSave, final boolean isNew) {
		LOGGER.debug("CDialog constructor called for {}", getClass().getSimpleName());
		this.data = data;
		this.onSave = onSave;
		this.isNew = isNew;
		setupDialog();
		setupForm();
		setupButtons();
		// dont populate form here, as fields may not be initialized yet populateForm();
	}

	/** Child must implement: form title. */
	protected abstract String getFormTitle();

	/** Child must implement: dialog header title. */
	@Override
	public abstract String getHeaderTitle();

	/** Child can override: success message for create. */
	protected String getSuccessCreateMessage() { return "Created successfully"; }

	/** Child can override: success message for update. */
	protected String getSuccessUpdateMessage() { return "Updated successfully"; }

	/** Child must implement: populate form fields from data. */
	protected abstract void populateForm();

	/** Called when Save is pressed. Handles validation and callback. */
	protected void save() {
		try {
			LOGGER.debug("Saving data: {}", data);
			validateForm();
			if (onSave != null) {
				onSave.accept(data);
			}
			close();
			Notification.show(isNew ? getSuccessCreateMessage() : getSuccessUpdateMessage());
		} catch (final Exception e) {
			Notification.show("Error: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
		}
	}

	/** Sets up Save and Cancel buttons. */
	protected void setupButtons() {
		final CButton saveButton = CButton.createPrimary("Save", e -> save());
		final CButton cancelButton = CButton.createTertiary("Cancel", e -> close());
		final HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
		buttonLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
		getFooter().add(buttonLayout);
	}

	/** Sets up dialog properties (title, modal, size, etc.) */
	protected void setupDialog() {
		setHeaderTitle(getHeaderTitle());
		setModal(true);
		setCloseOnEsc(true);
		setCloseOnOutsideClick(false);
		setWidth("500px");
	}

	/** Sets up the main layout and form layout. */
	protected void setupForm() {
		mainLayout = new VerticalLayout();
		mainLayout.setPadding(false);
		mainLayout.setSpacing(true);
		mainLayout.add(new H3(getFormTitle()));
		formLayout = new FormLayout();
		mainLayout.add(formLayout);
		add(mainLayout);
	}

	/** Child must implement: validate form fields. */
	protected abstract void validateForm();
}
