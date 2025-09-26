package tech.derbent.screens.view;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.components.CBinderFactory;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CDiv;
import tech.derbent.api.views.dialogs.CDBEditDialog;
import tech.derbent.screens.domain.CDetailLines;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

/** Dialog for editing screen field descriptions (detailSection entities). Extends CDBEditDialog to provide a consistent dialog experience. */
public class CDetailLinesEditDialog extends CDBEditDialog<CDetailLines> {

	private static final long serialVersionUID = 1L;
	private final CEnhancedBinder<CDetailLines> binder;
	private final CDetailSection screen;
	private final CFormBuilder<CDetailLines> formEntity;
	private final CFormBuilder<CDetailLines> formSection;
	private final CFormBuilder<CDetailLines> formClassType;
	private final CDiv divScreenType = new CDiv();
	private final CDiv divJavaType = new CDiv();
	// Div to
	private ComboBox<String> cmbFieldProperties;
	private ComboBox<String> cmbFieldClass;
	Tab tabSection;
	Tab tabEntity;
	Span tabSectionSpan;
	Span tabEntitySpan;
	TabSheet tabsOfDialog;

	public CDetailLinesEditDialog(final CDetailLines entity, final Consumer<CDetailLines> onSave, final boolean isNew, final CDetailSection screen)
			throws Exception {
		super(entity, onSave, isNew);
		this.binder = CBinderFactory.createEnhancedBinder(CDetailLines.class);
		this.screen = screen;
		this.formEntity = new CFormBuilder<CDetailLines>();
		this.formSection = new CFormBuilder<CDetailLines>();
		this.formClassType = new CFormBuilder<CDetailLines>();
		this.tabsOfDialog = new TabSheet();
		this.tabEntitySpan = new Span();
		this.tabSectionSpan = new Span();
		this.tabSection = tabsOfDialog.add("X", tabSectionSpan);
		this.tabEntity = tabsOfDialog.add("Y", tabEntitySpan);
		tabsOfDialog.getElement().executeJs("this.querySelector('vaadin-tabs').style.display='none';");
		setupDialog();
		populateForm();
	}

	@SuppressWarnings ("unchecked")
	private void createFormFields() throws Exception {
		try {
			// create the combobox to select the field class
			getDialogLayout().add(divScreenType);
			getDialogLayout().add(formClassType.build(CDetailLines.class, binder, List.of("relationFieldName")));
			// add tab here
			getDialogLayout().add(tabsOfDialog);
			// BUILD ENTITY TAB
			tabEntitySpan.add(formEntity.build(CDetailLines.class, binder, List.of("entityProperty", "lineOrder", "fieldCaption", "fieldDescription",
					"isRequired", "isReadonly", "isHidden", "defaultValue", "relatedEntityType", "dataProviderBean", "maxLength", "isActive")));
			// BUILD SECTION TAB
			tabSectionSpan.add(formSection.build(CDetailLines.class, binder, List.of("sectionName", "fieldCaption", "isActive")));
			// SETUP ENTITY TAB COMBOXBOXES
			cmbFieldClass = ((ComboBox<String>) formClassType.getComponent("relationFieldName"));
			cmbFieldClass.addValueChangeListener(event -> {
				final String selectedType = event.getValue();
				getEntity().setRelationFieldName(selectedType);
				if ((selectedType == null) || selectedType.isEmpty()) {
					return; // No
				}
				if (selectedType.equals(CEntityFieldService.SECTION)) {
					// activate section tab
					tabsOfDialog.setSelectedTab(tabSection);
				} else {
					// activate section tab
					tabsOfDialog.setSelectedTab(tabEntity);
					updateEntityPropertyBasedOnClass();
				}
			});
			cmbFieldProperties = ((ComboBox<String>) formEntity.getComponent("entityProperty"));
			Check.notNull(cmbFieldProperties, "Entity property combobox must not be null");
			cmbFieldProperties.addValueChangeListener(event -> {
				final String selectedProperty = event.getValue();
				if ((selectedProperty != null) && !selectedProperty.isEmpty()) {
					// Update the entity property based on the selected value
					getEntity().setProperty(selectedProperty);
					updatePropertyDefaultValues(selectedProperty);
				}
			});
			formEntity.getHorizontalLayout("entityProperty").add(divJavaType);
		} catch (final NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
			LOGGER.error("Error setting up dialog", e);
			throw new RuntimeException("Failed to setup dialog", e);
		}
	}

	@Override
	public String getDialogTitleString() { return getFormTitleString(); }

	@Override
	protected Icon getFormIcon() { return VaadinIcon.EDIT.create(); }

	@Override
	protected String getFormTitleString() { return isNew ? "Add Screen Field" : "Edit Screen Field"; }

	@Override
	protected String getSuccessCreateMessage() { return "Screen field added successfully"; }

	@Override
	protected String getSuccessUpdateMessage() { return "Screen field updated successfully"; }

	@Override
	protected void populateForm() {
		// print screen type:
		Check.notNull(screen, "Screen must not be null");
		divScreenType.setText("Screen type: " + screen.getEntityType());
		// Initialize ComboBox items before calling readBean to prevent binding errors
		updateEntityClassComboboxEntries();
		if (getEntity() != null) {
			// Now populate entityProperty ComboBox if relationFieldName is already set
			if ((getEntity().getRelationFieldName() != null) && !getEntity().getRelationFieldName().isEmpty()) {
				updateEntityPropertyBasedOnClass();
			}
			binder.readBean(getEntity());
		}
	}

	/** Sets up the main layout and form layout.
	 * @throws Exception */
	@Override
	protected void setupContent() throws Exception {
		super.setupContent();
		setWidth("700px");
		setHeight("800px");
		setResizable(true);
		createFormFields();
	}

	private void updateEntityClassComboboxEntries() {
		Check.notNull(screen, "Screen must not be null");
		// add additional field info for "this class"
		final List<EntityFieldInfo> listOfAdditionalFields = new ArrayList<>();
		final EntityFieldInfo infoSection = new EntityFieldInfo();
		infoSection.setFieldName(CEntityFieldService.SECTION);
		listOfAdditionalFields.add(infoSection);
		// get all fields + additional "this class" field
		final EntityFieldInfo infoThisClass = new EntityFieldInfo();
		infoThisClass.setFieldName(CEntityFieldService.THIS_CLASS);
		listOfAdditionalFields.add(infoThisClass);
		final List<String> entityLineTypes = CEntityFieldService.getEntityRelationFields(screen.getEntityType(), listOfAdditionalFields).stream()
				.map(CEntityFieldService.EntityFieldInfo::getFieldName).toList();
		cmbFieldClass.setItems(entityLineTypes);
	}

	@SuppressWarnings ("unchecked")
	private void updateEntityPropertyBasedOnClass() {
		final String relationFieldName = getEntity().getRelationFieldName();
		LOGGER.debug("Selected field class: {}", relationFieldName);
		if ((relationFieldName == null) || relationFieldName.isEmpty()) {
			return;
		}
		List<EntityFieldInfo> fieldProperties = null;
		// this class is a special case, we need to get all fields of the screen's entity
		// type
		if (relationFieldName.equals(CEntityFieldService.SECTION)) {
			cmbFieldProperties.setItems(List.of(CEntityFieldService.SECTION));
			getEntity().setProperty(CEntityFieldService.SECTION);
			return;
		} else if (relationFieldName.equals(CEntityFieldService.THIS_CLASS)) {
			fieldProperties = CEntityFieldService.getEntitySimpleFields(screen.getEntityType(), null);
		} else {
			// Get field properties for the selected class of relation
			final EntityFieldInfo info = CEntityFieldService.getEntityFieldInfo(screen.getEntityType().toString(), relationFieldName);
			Check.notNull(info, "Field info must not be null for field class: " + relationFieldName);
			fieldProperties = CEntityFieldService.getEntitySimpleFields(info.getJavaType(), null);
		}
		final ComboBox<String> cmbFieldProperties = ((ComboBox<String>) formEntity.getComponent("entityProperty"));
		Check.notNull(cmbFieldProperties, "Entity property combobox must not be null");
		cmbFieldProperties.setItems(fieldProperties.stream().map(CEntityFieldService.EntityFieldInfo::getFieldName).toList());
	}

	private void updatePropertyDefaultValues(final String selectedProperty) {
		LOGGER.debug("Selected property: {}", selectedProperty);
		// default values
		divJavaType.setText(" ");
		if ((selectedProperty == null) || selectedProperty.isEmpty()) {
			return;
		}
		final String relationFieldName = getEntity().getRelationFieldName();
		if ((relationFieldName == null) || relationFieldName.isEmpty()) {
			return;
		}
		EntityFieldInfo info;
		if (relationFieldName.equals(CEntityFieldService.SECTION)) {
			return;
		} else if (relationFieldName.equals(CEntityFieldService.THIS_CLASS)) {
			info = CEntityFieldService.getEntityFieldInfo(screen.getEntityType().toString(), selectedProperty);
		} else {
			info = CEntityFieldService.getEntityFieldInfo(screen.getEntityType().toString(), relationFieldName);
			info = CEntityFieldService.getEntityFieldInfo(info.getJavaType(), selectedProperty);
		}
		Check.notNull(info, "Field info must not be null for property: " + selectedProperty);
		getEntity().setDefaultValue(info.getDefaultValue());
		getEntity().setMaxLength(info.getMaxLength());
		getEntity().setDataProviderBean(info.getDataProviderBean());
		getEntity().setDescription(info.getDescription());
		divJavaType.setText("Java type: " + info.getJavaType());
	}

	@Override
	protected void validateForm() {
		if (!binder.isValid()) {
			throw new IllegalStateException("Please fill in all required fields correctly");
		}
		// Write bean data back to entity
		binder.writeBeanIfValid(getEntity());
	}
}
