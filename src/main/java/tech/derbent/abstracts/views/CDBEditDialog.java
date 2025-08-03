package tech.derbent.abstracts.views;

import java.util.function.Consumer;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;

/**
 * Abstract base class for data-aware dialogs. Uses generics to allow any data type. Handles dialog setup, form layout,
 * and save/cancel button logic. Child classes must implement form population and validation.
 */
public abstract class CDBEditDialog<T> extends CDialog {

    private static final long serialVersionUID = 1L;
    protected final T data;
    protected final Consumer<T> onSave;
    protected final boolean isNew;
    protected FormLayout formLayout;

    /**
     * @param data
     *            The data object to edit or create.
     * @param onSave
     *            Callback to execute on save.
     * @param isNew
     *            True if creating new, false if editing.
     */
    public CDBEditDialog(final T data, final Consumer<T> onSave, final boolean isNew) {
        super();
        LOGGER.debug("CDialog constructor called for {}", getClass().getSimpleName());
        this.data = data;
        this.onSave = onSave;
        this.isNew = isNew;
        // dont populate form here, as fields may not be initialized yet populateForm();
    }

    /** Child can override: success message for create. */
    protected String getSuccessCreateMessage() {
        return "Created successfully";
    }

    /** Child can override: success message for update. */
    protected String getSuccessUpdateMessage() {
        return "Updated successfully";
    }

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
    @Override
    protected void setupButtons() {
        final CButton saveButton = CButton.createPrimary("Save", e -> save());
        final CButton cancelButton = CButton.createTertiary("Cancel", e -> close());
        buttonLayout.add(saveButton, cancelButton);
    }

    /** Sets up the main layout and form layout. */
    @Override
    protected void setupContent() {
        formLayout = new FormLayout();
        mainLayout.add(formLayout);
    }

    /** Child must implement: validate form fields. */
    protected abstract void validateForm();
}
