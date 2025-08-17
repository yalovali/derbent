package tech.derbent.screens.view;

import java.util.function.Consumer;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import tech.derbent.abstracts.views.CDBEditDialog;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.domain.CScreenLines;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CFieldServiceBase;

/**
 * Dialog for editing screen field descriptions (CScreenLines entities). Extends
 * CDBEditDialog to provide a consistent dialog experience.
 */
public class CScreenLinesEditDialog extends CDBEditDialog<CScreenLines> {

	private static final long serialVersionUID = 1L;

	private final CEntityFieldService entityFieldService;

	private final Binder<CScreenLines> binder;

	// Form fields
	private TextField fieldCaptionField;

	private ComboBox<String> entityTypeCombo;

	private TextField fieldDescriptionField;

	private ComboBox<String> fieldTypeCombo;

	private Checkbox isRequiredField;

	private Checkbox isReadonlyField;

	private Checkbox isHiddenField;

	private TextField defaultValueField;

	private TextField relatedEntityTypeField;

	private ComboBox<String> dataProviderBeanCombo;

	private TextField maxLengthField;

	private Checkbox isActiveField;

	private final CScreen screen;

	public CScreenLinesEditDialog(final CScreenLines screenLine,
		final Consumer<CScreenLines> onSave, final boolean isNew,
		final CEntityFieldService entityFieldService, final CScreen screen) {
		super(screenLine, onSave, isNew);
		this.entityFieldService = entityFieldService;
		this.binder = new Binder<>(CScreenLines.class);
		this.screen = screen;
		setupDialog();
		createFormFields();
		setupFieldBindings();
		populateForm();
	}

	private void createFormFields() {
		fieldCaptionField = new TextField("Field Caption");
		fieldCaptionField.setRequired(true);
		fieldCaptionField.setWidthFull();
		// Entity type selection
		entityTypeCombo = new ComboBox<>("Entity Type");
		entityTypeCombo.setItems(entityFieldService.getAvailableEntityTypes());
		entityTypeCombo.setRequired(true);
		entityTypeCombo.setWidthFull();
		entityTypeCombo.addValueChangeListener(e -> updateEntityFieldCombo(e.getValue()));
		// Entity field selection (populated based on entity type)
		populateFieldFromSelection(
			CFieldServiceBase.getEntityClass(screen.getEntityType()));
		fieldDescriptionField = new TextField("Field Description");
		fieldDescriptionField.setWidthFull();
		fieldTypeCombo = new ComboBox<>("Field Type");
		fieldTypeCombo.setItems("TEXT", "NUMBER", "DATE", "BOOLEAN", "REFERENCE");
		fieldTypeCombo.setRequired(true);
		fieldTypeCombo.setValue("TEXT");
		fieldTypeCombo.setWidthFull();
		isRequiredField = new Checkbox("Required");
		isReadonlyField = new Checkbox("Read Only");
		isHiddenField = new Checkbox("Hidden");
		defaultValueField = new TextField("Default Value");
		defaultValueField.setWidthFull();
		relatedEntityTypeField = new TextField("Related Entity Type");
		relatedEntityTypeField.setWidthFull();
		dataProviderBeanCombo = new ComboBox<>("Data Provider Bean");
		dataProviderBeanCombo.setItems(entityFieldService.getDataProviderBeans());
		dataProviderBeanCombo.setWidthFull();
		maxLengthField = new TextField("Max Length");
		maxLengthField.setWidthFull();
		isActiveField = new Checkbox("Active");
		isActiveField.setValue(true);
		// Add fields to form layout
		formLayout.add(fieldCaptionField, entityTypeCombo, fieldDescriptionField,
			fieldTypeCombo, isRequiredField, isReadonlyField, isHiddenField,
			defaultValueField, relatedEntityTypeField, dataProviderBeanCombo,
			maxLengthField, isActiveField);
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

	/**
	 * Populate form fields from the selected entity field.
	 */
	private void
		populateFieldFromSelection(final CEntityFieldService.EntityFieldInfo fieldInfo) {

		if (fieldInfo != null) {
			fieldCaptionField.setValue(fieldInfo.getDisplayName());
			fieldDescriptionField.setValue(fieldInfo.getDescription());
			fieldTypeCombo.setValue(fieldInfo.getFieldType());
			isRequiredField.setValue(fieldInfo.isRequired());
			isReadonlyField.setValue(fieldInfo.isReadOnly());
			isHiddenField.setValue(fieldInfo.isHidden());
			defaultValueField.setValue(fieldInfo.getDefaultValue());
			maxLengthField.setValue(String.valueOf(fieldInfo.getMaxLength()));

			if (!fieldInfo.getDataProviderBean().isEmpty()) {
				dataProviderBeanCombo.setValue(fieldInfo.getDataProviderBean());
				relatedEntityTypeField.setValue(fieldInfo.getJavaType());
			}
		}
	}

	@Override
	protected void populateForm() {

		if (data != null) {
			binder.readBean(data);

			// Set entity type if screen has one
			if ((data.getScreen() != null)
				&& (data.getScreen().getEntityType() != null)) {
				entityTypeCombo.setValue(data.getScreen().getEntityType());
				updateEntityFieldCombo(data.getScreen().getEntityType());
			}
		}
	}

	@Override
	protected void setupDialog() {
		super.setupDialog(); // Call parent setup first
		setWidth("600px");
		setHeight("700px");
		setResizable(true);
	}

	private void setupFieldBindings() {
		binder.forField(fieldCaptionField).asRequired("Field caption is required")
			.bind(CScreenLines::getFieldCaption, CScreenLines::setFieldCaption);
		binder.forField(fieldDescriptionField).bind(CScreenLines::getFieldDescription,
			CScreenLines::setFieldDescription);
		binder.forField(fieldTypeCombo).asRequired("Field type is required")
			.bind(CScreenLines::getFieldType, CScreenLines::setFieldType);
		binder.forField(isRequiredField).bind(CScreenLines::getIsRequired,
			CScreenLines::setIsRequired);
		binder.forField(isReadonlyField).bind(CScreenLines::getIsReadonly,
			CScreenLines::setIsReadonly);
		binder.forField(isHiddenField).bind(CScreenLines::getIsHidden,
			CScreenLines::setIsHidden);
		binder.forField(defaultValueField).bind(CScreenLines::getDefaultValue,
			CScreenLines::setDefaultValue);
		binder.forField(relatedEntityTypeField).bind(CScreenLines::getRelatedEntityType,
			CScreenLines::setRelatedEntityType);
		binder.forField(dataProviderBeanCombo).bind(CScreenLines::getDataProviderBean,
			CScreenLines::setDataProviderBean);
		binder.forField(maxLengthField)
			.withConverter(value -> value.isEmpty() ? null : Integer.valueOf(value),
				value -> value == null ? "" : value.toString())
			.bind(CScreenLines::getMaxLength, CScreenLines::setMaxLength);
		binder.forField(isActiveField).bind(CScreenLines::getIsActive,
			CScreenLines::setIsActive);
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