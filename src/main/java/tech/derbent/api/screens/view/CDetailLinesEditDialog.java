package tech.derbent.api.screens.view;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
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
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CDiv;
import tech.derbent.api.views.dialogs.CDBEditDialog;
import tech.derbent.api.screens.domain.CDetailLines;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CEntityFieldService;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;

/** Dialog for editing screen field descriptions (detailSection entities). Extends CDBEditDialog to provide a consistent dialog experience. */
public class CDetailLinesEditDialog extends CDBEditDialog<CDetailLines> {

	private static final long serialVersionUID = 1L;
	private final CEnhancedBinder<CDetailLines> binder;
	private ComboBox<String> cmbFieldClass;
	// Div to
	private ComboBox<String> cmbFieldProperties;
	private final CDiv divJavaType = new CDiv();
	private final CDiv divScreenType = new CDiv();
	private final CFormBuilder<CDetailLines> formClassType;
	private final CFormBuilder<CDetailLines> formEntity;
	private final CFormBuilder<CDetailLines> formSection;
	private final CDetailSection screen;
	Tab tabEntity;
	Span tabEntitySpan;
	Tab tabSection;
	Span tabSectionSpan;
	TabSheet tabsOfDialog;

	public CDetailLinesEditDialog(final CDetailLines entity, final Consumer<CDetailLines> onSave, final boolean isNew, final CDetailSection screen)
			throws Exception {
		super(entity, onSave, isNew);
		binder = CBinderFactory.createEnhancedBinder(CDetailLines.class);
		this.screen = screen;
		formEntity = new CFormBuilder<CDetailLines>();
		formSection = new CFormBuilder<CDetailLines>();
		formClassType = new CFormBuilder<CDetailLines>();
		tabsOfDialog = new TabSheet();
		tabEntitySpan = new Span();
		tabSectionSpan = new Span();
		tabSection = tabsOfDialog.add("X", tabSectionSpan);
		tabEntity = tabsOfDialog.add("Y", tabEntitySpan);
		tabsOfDialog.getElement().executeJs("this.querySelector('vaadin-tabs').style.display='none';");
		setupDialog();
		populateForm();
	}

	@SuppressWarnings ("unchecked")
	private void createFormFields() throws Exception {
		try {
			LOGGER.debug("Setting up detail lines edit dialog form fields");
			// create the combobox to select the field class
			getDialogLayout().add(divScreenType);
			getDialogLayout().add(formClassType.build(CDetailLines.class, binder, List.of("relationFieldName")));
			// add tab here
			getDialogLayout().add(tabsOfDialog);
			// BUILD ENTITY TAB
			tabEntitySpan.add(formEntity.build(CDetailLines.class, binder,
					List.of("entityProperty", "lineOrder", "fieldCaption", "fieldDescription", "isRequired", "isReadonly", "isHidden",
							"isCaptionVisible", "defaultValue", "relatedEntityType", "dataProviderBean", "maxLength", "active")));
			// BUILD SECTION TAB
			tabSectionSpan.add(formSection.build(CDetailLines.class, binder, List.of("sectionName", "fieldCaption", "active")));
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
			throw e;
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
		try {
			LOGGER.debug("Updating entity property based on selected class");
			final String relationFieldName = getEntity().getRelationFieldName();
			Check.notBlank(relationFieldName, "Relation field name must not be blank");
			LOGGER.debug("Selected field class: {}", relationFieldName);
			List<EntityFieldInfo> fieldProperties = null;
			// this class is a special case, we need to get all fields of the screen's entity
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
				// if the field is a collection, we cannot reference any item, so skip fields all...
				// no item can be referenced it is a collection, skip fields all...
				if (Collection.class.isAssignableFrom(info.getFieldTypeClass())) {
					// Initialize empty list for collections since no fields can be referenced
					fieldProperties = new ArrayList<>();
				} else {
					// single object types
					fieldProperties = CEntityFieldService.getEntitySimpleFields(info.getJavaType(), null);
				}
			}
			if (!relationFieldName.equals(CEntityFieldService.SECTION) && !relationFieldName.equals(CEntityFieldService.THIS_CLASS)) {
				// Ensure fieldProperties is not null before proceeding
				Check.notNull(fieldProperties, "Field properties list must not be null");
				final EntityFieldInfo info_data = CEntityFieldService.getEntityFieldInfo(screen.getEntityType().toString(), relationFieldName);
				Check.notNull(info_data, "Entity field info must not be null for relation field: " + relationFieldName);
				final String createComponentMethod = info_data.getCreateComponentMethod();
				if ((createComponentMethod != null) && !createComponentMethod.trim().isEmpty()) {
					// if the components methods are not empty add them to list
					final String[] methods = createComponentMethod.split(",");
					for (final String method : methods) {
						if ((method != null) && !method.trim().isEmpty()) {
							final String trimmedMethod = method.trim();
							// create a fake EntityFieldInfo to hold the method name
							final EntityFieldInfo componentField = new EntityFieldInfo();
							componentField.setDisplayName(relationFieldName);
							componentField.setFieldName(CEntityFieldService.COMPONENT + ":" + trimmedMethod);
							componentField.setJavaType(CEntityFieldService.COMPONENT);
							fieldProperties.add(componentField);
						}
					}
				}
			}
			final ComboBox<String> cmbFieldProperties = ((ComboBox<String>) formEntity.getComponent("entityProperty"));
			Check.notNull(cmbFieldProperties, "Entity property combobox must not be null");
			cmbFieldProperties.setItems(fieldProperties.stream().map(CEntityFieldService.EntityFieldInfo::getFieldName).toList());
		} catch (final Exception e) {
			LOGGER.error("Error updating entity property based on class:{}", e.getMessage());
			throw e;
		}
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
		} else if (selectedProperty.startsWith(CEntityFieldService.COMPONENT + ":")) {
			getEntity().setProperty(selectedProperty);
			getEntity().setDefaultValue(null);
			getEntity().setMaxLength(0);
			getEntity().setDataProviderBean(null);
			getEntity().setDescription(selectedProperty);
			getEntity().setRelatedEntityType(null);
			getEntity().setFieldCaption(selectedProperty);
			getEntity().setIsReadonly(false);
			getEntity().setIsRequired(false);
			getEntity().setIsHidden(false);
			getEntity().setActive(true);
			getEntity().setIsCaptionVisible(true);
			getEntity().setFieldCaption(relationFieldName);
			getEntity().setDataProviderBean(CAuxillaries.getEntityServiceClasses(screen.getEntityType()).getSimpleName());
			binder.readBean(getEntity());
			return;
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

	// java
	@Override
	protected void validateForm() {
		binder.validateBean(getEntity());
	}
}
