package tech.derbent.app.kanban.kanbanline.view;

import java.util.List;
import java.util.function.Consumer;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.components.CBinderFactory;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.ui.dialogs.CDialogDBEdit;
import tech.derbent.api.utils.Check;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanColumn;

public class CDialogKanbanColumnEdit extends CDialogDBEdit<CKanbanColumn> {

	private static final long serialVersionUID = 1L;
	private final CEnhancedBinder<CKanbanColumn> binder;
	private final CFormBuilder<CKanbanColumn> formBuilder;

	public CDialogKanbanColumnEdit(final CKanbanColumn entity, final Consumer<CKanbanColumn> onSave, final boolean isNew) throws Exception {
		super(entity, onSave, isNew);
		binder = CBinderFactory.createEnhancedBinder(CKanbanColumn.class);
		formBuilder = new CFormBuilder<>();
		setupDialog();
		populateForm();
	}

	private void createFormFields() throws Exception {
		Check.notNull(getDialogLayout(), "Dialog layout must be initialized before building form fields");
		getDialogLayout().add(formBuilder.build(CKanbanColumn.class, binder,
				List.of("name", "description", "itemOrder", "active", "includedStatuses", "excludedStatuses")));
	}

	@Override
	public String getDialogTitleString() { return getFormTitleString(); }

	@Override
	protected Icon getFormIcon() { return VaadinIcon.TABLE.create(); }

	@Override
	protected String getFormTitleString() { return isNew ? "Add Kanban Column" : "Edit Kanban Column"; }

	@Override
	protected String getSuccessCreateMessage() { return "Kanban column created successfully"; }

	@Override
	protected String getSuccessUpdateMessage() { return "Kanban column updated successfully"; }

	@Override
	protected void populateForm() {
		if (getEntity() != null) {
			binder.readBean(getEntity());
		}
	}

	@Override
	protected void setupContent() throws Exception {
		super.setupContent();
		setWidth("500px");
		setHeight("450px");
		setResizable(true);
		createFormFields();
	}

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
