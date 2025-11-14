package tech.derbent.api.ui.dialogs;

import java.util.function.Consumer;
import tech.derbent.api.ui.component.CButton;
import tech.derbent.api.ui.component.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;

/** Abstract base class for data-aware dialogs. Uses generics to allow any data type. Handles dialog setup, form layout, and save/cancel button logic.
 * Child classes must implement form population and validation. */
public abstract class CDBEditDialog<EntityClass> extends CDialog {

	private static final long serialVersionUID = 1L;
	private CVerticalLayout dialogLayout;
	private final EntityClass entity;
	protected final boolean isNew;
	protected final Consumer<EntityClass> onSave;

	/** @param entity The data object to edit or create.
	 * @param onSave Callback to execute on save.
	 * @param isNew  True if creating new, false if editing. */
	public CDBEditDialog(final EntityClass entity, final Consumer<EntityClass> onSave, final boolean isNew) {
		super();
		LOGGER.debug("CDialog constructor called for {}", getClass().getSimpleName());
		this.entity = entity;
		this.onSave = onSave;
		this.isNew = isNew;
		// dont populate form here, as fields may not be initialized yet populateForm();
	}

	public CVerticalLayout getDialogLayout() { return dialogLayout; }

	public EntityClass getEntity() { return entity; }

	/** Child can override: success message for create. */
	protected String getSuccessCreateMessage() { return "Entity created successfully"; }

	/** Child can override: success message for update. */
	protected String getSuccessUpdateMessage() { return "Entity updated successfully"; }

	/** Child must implement: populate form fields from data. */
	protected abstract void populateForm();

	/** Called when Save is pressed. Handles validation and callback.
	 * @throws Exception */
	protected void save() throws Exception {
		try {
			LOGGER.debug("Saving data: {}", getEntity());
			validateForm();
			if (onSave != null) {
				onSave.accept(getEntity());
			}
			close();
			CNotificationService.showSuccess(isNew ? getSuccessCreateMessage() : getSuccessUpdateMessage());
		} catch (final Exception e) {
			CNotificationService.showError("Error: " + e.getMessage());
		}
	}

	/** Sets up Save and Cancel buttons with keyboard shortcuts. */
	@Override
	protected void setupButtons() {
		final CButton saveButton = CButton.createSaveButton("Save", e -> {
			try {
				save();
			} catch (Exception e1) {
				CNotificationService.showException("Error saving entity", e1);
			}
		});
		// Add Enter key shortcut for save button
		saveButton.addClickShortcut(com.vaadin.flow.component.Key.ENTER);
		final CButton cancelButton = CButton.createCancelButton("Cancel", e -> close());
		// Esc key is already handled by setCloseOnEsc(true) in dialog setup
		buttonLayout.add(saveButton, cancelButton);
	}

	/** Sets up the main layout and form layout.
	 * @throws Exception */
	@Override
	protected void setupContent() throws Exception {
		dialogLayout = new CVerticalLayout();
		mainLayout.add(dialogLayout);
	}

	/** Child must implement: validate form fields. */
	protected abstract void validateForm();
}
