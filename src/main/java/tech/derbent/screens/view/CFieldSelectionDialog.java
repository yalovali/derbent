package tech.derbent.screens.view;

import java.util.List;
import java.util.function.Consumer;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.views.dialogs.CDBEditDialog;
import tech.derbent.screens.domain.CGridEntity.FieldSelection;

/** Dialog for selecting and ordering grid column fields. Uses CFieldSelectionComponent for the selection interface. */
public class CFieldSelectionDialog extends CDBEditDialog<List<FieldSelection>> {

	private static final long serialVersionUID = 1L;
	private List<String> currentSelections;
	private String entityType;
	private CFieldSelectionComponent fieldSelectionComponent;

	/** Creates a new field selection dialog.
	 * @param entityType          The entity type to show fields for
	 * @param currentColumnFields Current field selections as string
	 * @param onSave              Callback when selection is saved
	 * @throws Exception */
	public CFieldSelectionDialog(String entityType, List<String> currentColumnFields, Consumer<List<FieldSelection>> onSave) throws Exception {
		super(null, onSave, false);
		this.entityType = entityType;
		this.currentSelections = currentColumnFields;
		setupDialog();
		populateForm();
	}

	private void createFormFields() {
		fieldSelectionComponent = new CFieldSelectionComponent("Available Fields", entityType);
		getDialogLayout().add(fieldSelectionComponent);
	}

	@Override
	public String getDialogTitleString() { return "Select Grid Columns"; }

	@Override
	public List<FieldSelection> getEntity() { return fieldSelectionComponent.getSelectedFields(); }

	@Override
	protected Icon getFormIcon() { return VaadinIcon.GRID_V.create(); }

	@Override
	protected String getFormTitleString() { return "Select and Order Grid Columns"; }

	@Override
	protected String getSuccessUpdateMessage() { return "Grid columns updated successfully"; }

	@Override
	protected void populateForm() {
		if (currentSelections != null && !currentSelections.isEmpty()) {
			fieldSelectionComponent.setColumnFieldsFromString(currentSelections);
		}
	}

	@Override
	protected void setupContent() throws Exception {
		super.setupContent();
		setWidth("800px");
		setHeight("600px");
		setResizable(true);
		createFormFields();
	}

	@Override
	protected void validateForm() {
		// No specific validation needed for field selection
	}
}
