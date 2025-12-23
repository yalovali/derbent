package tech.derbent.api.screens.view;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.screens.domain.CGridEntity.FieldSelection;
import tech.derbent.api.screens.service.CEntityFieldService;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.ui.component.enhanced.CComponentFieldSelection;
import tech.derbent.api.ui.dialogs.CDialogDBEdit;
import tech.derbent.api.utils.Check;

/** Dialog for selecting and ordering grid column fields. Uses CComponentFieldSelection with FormBuilder patterns for the selection interface. */
public class CDialogFieldSelection extends CDialogDBEdit<List<FieldSelection>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogFieldSelection.class);
	private static final long serialVersionUID = 1L;
	private CComponentFieldSelection<Object, EntityFieldInfo> componentFieldSelection;
	private final List<String> currentSelections;
	private final String entityType;

	/** Creates a new field selection dialog.
	 * @param entityType          The entity type to show fields for
	 * @param currentColumnFields Current field selections as string
	 * @param onSave              Callback when selection is saved
	 * @throws Exception */
	public CDialogFieldSelection(String entityType, List<String> currentColumnFields, Consumer<List<FieldSelection>> onSave) throws Exception {
		super(null, onSave, false);
		this.entityType = entityType;
		this.currentSelections = currentColumnFields;
		setupDialog();
		populateForm();
	}

	private void createFormFields() throws Exception {
		// Create the field selection component directly without FormBuilder
		// Using null for dataProviderResolver and contentOwner since we'll set items manually
		componentFieldSelection = new CComponentFieldSelection<>(null, null, null, "Available Fields", "Selected Fields");
		// Set item label generator for EntityFieldInfo
		componentFieldSelection.setItemLabelGenerator(item -> {
			final EntityFieldInfo fieldInfo = item;
			return fieldInfo.getDisplayName() + " (" + fieldInfo.getFieldName() + ")";
		});
		// Get all available fields for the entity type
		final List<EntityFieldInfo> allFields = CEntityFieldService.getEntityFields(entityType);
		Check.notNull(allFields, "Failed to get entity fields for type: " + entityType);
		LOGGER.debug("Loaded {} fields for entity type '{}'", allFields.size(), entityType);
		// Set source items directly
		componentFieldSelection.setSourceItems(allFields);
		getDialogLayout().add(componentFieldSelection);
	}

	@Override
	public String getDialogTitleString() { return "Select Grid Columns"; }

	@Override
	public List<FieldSelection> getEntity() {
		final List<EntityFieldInfo> selectedFields = componentFieldSelection.getValue();
		final List<FieldSelection> result = new ArrayList<>();
		for (int i = 0; i < selectedFields.size(); i++) {
			result.add(new FieldSelection(selectedFields.get(i), i));
		}
		return result;
	}

	@Override
	protected Icon getFormIcon() { return VaadinIcon.GRID_V.create(); }

	@Override
	protected String getFormTitleString() { return "Select and Order Grid Columns"; }

	@Override
	protected String getSuccessUpdateMessage() { return "Grid columns updated successfully"; }

	@Override
	protected void populateForm() {
		if (currentSelections != null && !currentSelections.isEmpty()) {
			// Get all available fields
			final List<EntityFieldInfo> allFields = CEntityFieldService.getEntityFields(entityType);
			// Filter to get selected fields in order
			final List<EntityFieldInfo> selectedFields = currentSelections.stream()
					.map(fieldName -> allFields.stream().filter(f -> f.getFieldName().equals(fieldName)).findFirst().orElse(null))
					.filter(f -> f != null).collect(Collectors.toList());
			LOGGER.debug("Populating form with {} selected fields", selectedFields.size());
			componentFieldSelection.setValue(selectedFields);
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
