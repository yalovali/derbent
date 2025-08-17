package tech.derbent.screens.view;

import java.util.List;
import java.util.function.Consumer;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.components.CBinderFactory;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.views.CDBEditDialog;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.domain.CScreenLines;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CViewsService;

/**
 * Dialog for editing screen field descriptions (CScreenLines entities). Extends
 * CDBEditDialog to provide a consistent dialog experience.
 */
public class CScreenLinesEditDialog extends CDBEditDialog<CScreenLines> {

	private static final long serialVersionUID = 1L;

	private final CEntityFieldService entityFieldService;

	private final CViewsService viewsService;

	private final CEnhancedBinder<CScreenLines> binder;

	private final CScreen screen;

	private ComboBox<String> entityLineTypeComboBox;

	private ComboBox<String> entityFieldNameComboBox;

	public CScreenLinesEditDialog(final CScreenLines screenLine,
		final Consumer<CScreenLines> onSave, final boolean isNew,
		final CEntityFieldService entityFieldService, final CScreen screen,
		final CViewsService viewsService) {
		super(screenLine, onSave, isNew);
		this.entityFieldService = entityFieldService;
		this.viewsService = viewsService;
		this.binder = CBinderFactory.createEnhancedBinder(CScreenLines.class);
		this.screen = screen;
		setupDialog();
		createFormFields();
		populateForm();
	}

	private void createEntityFieldNameComboBox(final VerticalLayout layout) {
		entityFieldNameComboBox = new ComboBox<>("Entity Field Name");
		entityFieldNameComboBox
			.setHelperText("Select the field from the chosen entity type");
		entityFieldNameComboBox.setRequired(true);
		entityFieldNameComboBox.setAllowCustomValue(false);
		// Set up value change listener
		entityFieldNameComboBox.addValueChangeListener(event -> {
			updateFieldTypeBasedOnSelection();
		});
		// Bind to entity
		binder.forField(entityFieldNameComboBox).bind(CScreenLines::getEntityFieldName,
			CScreenLines::setEntityFieldName);
		layout.add(entityFieldNameComboBox);
	}

	private void createEntityLineTypeComboBox(final VerticalLayout layout) {
		entityLineTypeComboBox = new ComboBox<>("Entity Line Type");
		entityLineTypeComboBox.setHelperText(
			"Select the entity type: the class itself or a related entity");
		entityLineTypeComboBox.setRequired(false);
		entityLineTypeComboBox.setAllowCustomValue(false);

		// Get available entity line types based on screen's entity type
		if ((screen != null) && (screen.getEntityType() != null)) {
			final List<String> entityLineTypes =
				viewsService.getAvailableEntityLineTypes(screen.getEntityType());
			entityLineTypeComboBox.setItems(entityLineTypes);
		}
		// Set up value change listener
		entityLineTypeComboBox.addValueChangeListener(event -> {
			final String selectedLineType = event.getValue();
			updateEntityFieldNameOptions(selectedLineType);
			updateFieldTypeBasedOnSelection();
		});
		// Bind to entity
		binder.forField(entityLineTypeComboBox).bind(CScreenLines::getEntityLineType,
			CScreenLines::setEntityLineType);
		layout.add(entityLineTypeComboBox);
	}

	private void createFormFields() {
		// Create the form layout manually to have better control over field order and
		// behavior
		final VerticalLayout customFormLayout = new VerticalLayout();
		customFormLayout.setPadding(false);
		customFormLayout.setSpacing(true);
		// Use CEntityFormBuilder for basic fields but add custom dropdowns
		final Div basicFormContent =
			CEntityFormBuilder.buildForm(CScreenLines.class, binder);
		customFormLayout.add(basicFormContent);
		// Add custom entity line type dropdown
		createEntityLineTypeComboBox(customFormLayout);
		// Add custom entity field name dropdown
		createEntityFieldNameComboBox(customFormLayout);
		formLayout.add(customFormLayout);
	}

	@Override
	protected Icon getFormIcon() { return VaadinIcon.EDIT.create(); }

	@Override
	protected String getFormTitle() {
		return isNew ? "Add Screen Field" : "Edit Screen Field";
	}

	@Override
	public String getHeaderTitle() { return getFormTitle(); }

	@Override
	protected String getSuccessCreateMessage() {
		return "Screen field added successfully";
	}

	@Override
	protected String getSuccessUpdateMessage() {
		return "Screen field updated successfully";
	}

	@Override
	protected void populateForm() {

		if (data != null) {
			binder.readBean(data);

			// Trigger entity line type change to populate field names
			if (data.getEntityLineType() != null) {
				updateEntityFieldNameOptions(data.getEntityLineType());
			}
		}
		else // Set default entity line type to the screen's entity type for new records
		if ((screen != null) && (screen.getEntityType() != null)) {
			entityLineTypeComboBox.setValue(screen.getEntityType());
		}
	}

	@Override
	protected void setupDialog() {
		super.setupDialog(); // Call parent setup first
		setWidth("700px");
		setHeight("800px");
		setResizable(true);
	}

	private void updateEntityFieldNameOptions(final String entityLineType) {

		if ((entityLineType == null) || entityLineType.isEmpty()) {
			entityFieldNameComboBox.setItems();
			return;
		}
		// Get the actual entity class name for the selected line type
		final String entityClassName =
			viewsService.getEntityClassNameForLineType(entityLineType);
		// Get available fields for this entity type
		final List<CEntityFieldService.EntityFieldInfo> fieldInfos =
			entityFieldService.getEntityFields(entityClassName);
		// Extract field names and set them in the combobox
		final List<String> fieldNames =
			fieldInfos.stream().filter(info -> !info.isHidden()) // Only show non-hidden
																	// fields
				.map(CEntityFieldService.EntityFieldInfo::getFieldName).toList();
		entityFieldNameComboBox.setItems(fieldNames);
	}

	private void updateFieldTypeBasedOnSelection() {
		final String entityLineType = entityLineTypeComboBox.getValue();
		final String fieldName = entityFieldNameComboBox.getValue();

		if ((entityLineType == null) || (fieldName == null)) {
			return;
		}
		// Get the actual entity class name for the selected line type
		final String entityClassName =
			viewsService.getEntityClassNameForLineType(entityLineType);
		// Get field information to determine the field type
		final List<CEntityFieldService.EntityFieldInfo> fieldInfos =
			entityFieldService.getEntityFields(entityClassName);
		fieldInfos.stream().filter(info -> info.getFieldName().equals(fieldName))
			.findFirst().ifPresent(fieldInfo -> {

				// Update the field type in the entity (it will be readonly in the UI)
				if (data != null) {
					data.setFieldType(fieldInfo.getFieldType());
					// Refresh the binder to update the readonly field display
					binder.readBean(data);
				}
			});
	}

	@Override
	protected void validateForm() {

		if (!binder.isValid()) {
			throw new IllegalStateException(
				"Please fill in all required fields correctly");
		}
		// Write bean data back to entity
		binder.writeBeanIfValid(data);
	}
}