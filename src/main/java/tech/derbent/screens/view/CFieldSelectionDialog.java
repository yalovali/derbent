package tech.derbent.screens.view;

import java.util.List;
import java.util.function.Consumer;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.abstracts.views.dialogs.CDBEditDialog;
import tech.derbent.screens.domain.CGridEntity.FieldSelection;

/** Dialog for selecting and ordering grid column fields. Uses CFieldSelectionComponent for the selection interface. */
public class CFieldSelectionDialog extends CDBEditDialog<List<FieldSelection>> {

	private static final long serialVersionUID = 1L;
	private CFieldSelectionComponent fieldSelectionComponent;
	private String entityType;
	private String currentSelections;

	/** Creates a new field selection dialog.
	 * @param entityType        The entity type to show fields for
	 * @param currentSelections Current field selections as string
	 * @param onSave            Callback when selection is saved
	 * @throws Exception */
	public CFieldSelectionDialog(String entityType, String currentSelections, Consumer<List<FieldSelection>> onSave) throws Exception {
		super(null, onSave, false);
		this.entityType = entityType;
		this.currentSelections = currentSelections;
		setupDialog();
		populateForm();
	}

	@Override
	public String getDialogTitleString() { return "Select Grid Columns"; }

	@Override
	protected Icon getFormIcon() { return VaadinIcon.GRID_V.create(); }

	@Override
	protected String getFormTitleString() { return "Select and Order Grid Columns"; }

	@Override
	protected void setupContent() throws Exception {
		super.setupContent();
		setWidth("800px");
		setHeight("600px");
		setResizable(true);
		createFormFields();
	}

	private void createFormFields() {
		fieldSelectionComponent = new CFieldSelectionComponent("Available Fields");
		fieldSelectionComponent.setEntityType(entityType);
		getDialogLayout().add(fieldSelectionComponent);
	}

	@Override
	protected void populateForm() {
		if (currentSelections != null && !currentSelections.trim().isEmpty()) {
			fieldSelectionComponent.setSelectedFieldsFromString(currentSelections);
		}
	}

	@Override
	protected void validateForm() {
		// No specific validation needed for field selection
	}

	@Override
	public List<FieldSelection> getEntity() { return fieldSelectionComponent.getSelectedFields(); }

	@Override
	protected String getSuccessUpdateMessage() { return "Grid columns updated successfully"; }
}
