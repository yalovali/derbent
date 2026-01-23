package tech.derbent.plm.kanban.kanbanline.view;

import java.util.List;
import java.util.function.Consumer;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.components.CBinderFactory;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.ui.dialogs.CDialogDBEdit;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.kanban.kanbanline.domain.CKanbanColumn;

public class CDialogKanbanColumnEdit extends CDialogDBEdit<CKanbanColumn> {

	private static final long serialVersionUID = 1L;
	private final CEnhancedBinder<CKanbanColumn> binder;
	private final CFormBuilder<CKanbanColumn> formBuilder;

	/** Creates the edit dialog for a kanban column. */
	public CDialogKanbanColumnEdit(final CKanbanColumn entity, final Consumer<CKanbanColumn> onSave, final boolean isNew) throws Exception {
		super(entity, onSave, isNew);
		binder = CBinderFactory.createEnhancedBinder(CKanbanColumn.class);
		formBuilder = new CFormBuilder<>();
		setupDialog();
		populateForm();
	}

	/** Builds and attaches the form fields. */
	private void createFormFields() throws Exception {
		Check.notNull(getDialogLayout(), "Dialog layout must be initialized before building form fields");
		getDialogLayout()
				.add(formBuilder.build(CKanbanColumn.class, binder, List.of("name", "color", "itemOrder", "defaultColumn", "includedStatuses")));
	}

	/** Returns the dialog title. */
	@Override
	public String getDialogTitleString() { return getFormTitleString(); }

	/** Returns the dialog icon. */
	@Override
	protected Icon getFormIcon() { return VaadinIcon.TABLE.create(); }

	/** Returns the form title text. */
	@Override
	protected String getFormTitleString() { return isNew ? "Add Kanban Column" : "Edit Kanban Column"; }

	/** Returns the success message for create. */
	@Override
	protected String getSuccessCreateMessage() { return "Kanban column created successfully"; }

	/** Returns the success message for update. */
	@Override
	protected String getSuccessUpdateMessage() { return "Kanban column updated successfully"; }

	/** Loads the entity into the binder. */
	@Override
	protected void populateForm() {
		if (getEntity() != null) {
			binder.readBean(getEntity());
		}
	}

	/** Configures dialog size and content. */
	@Override
	protected void setupContent() throws Exception {
		super.setupContent();
		// Width handled by CDialog base class (responsive pattern)
		setHeight("550px");
		setResizable(true);
		createFormFields();
	}

	/** Validates the form and writes to the entity. */
	@Override
	protected void validateForm() {
		Check.notNull(getEntity(), "Kanban column cannot be null when validating");
		try {
			binder.writeBean(getEntity());
		} catch (final Exception e) {
			throw new IllegalStateException("Failed to validate kanban column", e);
		}
	}
}
