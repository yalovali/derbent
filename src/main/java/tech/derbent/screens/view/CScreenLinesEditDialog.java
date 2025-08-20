package tech.derbent.screens.view;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Consumer;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.components.CBinderFactory;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.CDBEditDialog;
import tech.derbent.abstracts.views.CDiv;
import tech.derbent.abstracts.views.CVerticalLayout;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.domain.CScreenLines;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

/**
 * Dialog for editing screen field descriptions (CScreenLines entities). Extends CDBEditDialog to provide a consistent
 * dialog experience.
 */
public class CScreenLinesEditDialog extends CDBEditDialog<CScreenLines> {

    private static final long serialVersionUID = 1L;

    private final CEntityFieldService entityFieldService;

    private final CEnhancedBinder<CScreenLines> binder;

    private final CScreen screen;

    private final CEntityFormBuilder<CScreenLines> form;

    private final CDiv divScreenType = new CDiv();

    private final CDiv divJavaType = new CDiv();

    // Div to
    private ComboBox<String> cmbFieldProperties;

    private ComboBox<String> cmbFieldClass;

    public CScreenLinesEditDialog(final CScreenLines entity, final Consumer<CScreenLines> onSave, final boolean isNew,
            final CEntityFieldService entityFieldService, final CScreen screen) {
        super(entity, onSave, isNew);
        this.entityFieldService = entityFieldService;
        this.binder = CBinderFactory.createEnhancedBinder(CScreenLines.class);
        this.screen = screen;
        this.form = new CEntityFormBuilder<CScreenLines>();
        setupDialog();
        populateForm();
    }

    @SuppressWarnings("unchecked")
    private void createFormFields() {

        try {
            // Create the form layout manually to have better control over field order and
            // behavior
            final CVerticalLayout customFormLayout = new CVerticalLayout(false, true, false);
            customFormLayout.add(divScreenType);
            // Use CEntityFormBuilder for basic fields, but exclude the fields we handle
            // manually to prevent duplicate bindings that cause incomplete binding errors
            final List<String> fieldsToInclude = List.of("fieldClass", "entityProperty", "lineOrder", "fieldCaption",
                    "fieldDescription", "isRequired", "isReadonly", "isHidden", "defaultValue", "relatedEntityType",
                    "dataProviderBean", "maxLength", "isActive");
            final CVerticalLayout formLayout = form.build(CScreenLines.class, binder, fieldsToInclude);
            customFormLayout.add(formLayout);
            // createEntityFieldNameComboBox(customFormLayout);
            getDialogLayout().add(customFormLayout);
            // setup the comboboxes
            cmbFieldClass = ((ComboBox<String>) form.getComponent("fieldClass"));
            Check.notNull(cmbFieldClass, "Field class combobox must not be null");
            cmbFieldClass.addValueChangeListener(event -> {
                final String selectedType = event.getValue();

                if (selectedType != null && !selectedType.isEmpty()) {
                    // Update the entity class based on the selected type
                    entity.setFieldClass(selectedType);
                    // Update the field type based on the selection
                    updateEntityPropertyBasedOnClass();
                }
            });
            cmbFieldProperties = ((ComboBox<String>) form.getComponent("entityProperty"));
            Check.notNull(cmbFieldProperties, "Entity property combobox must not be null");
            cmbFieldProperties.addValueChangeListener(event -> {
                final String selectedProperty = event.getValue();

                if (selectedProperty != null && !selectedProperty.isEmpty()) {
                    // Update the entity property based on the selected value
                    entity.setEntityProperty(selectedProperty);
                    updatePropertyDefaultValues(selectedProperty);
                }
            });
            form.getHorizontalLayout("entityProperty").add(divJavaType);
        } catch (final NoSuchMethodException | SecurityException | IllegalAccessException
                | InvocationTargetException e) {
            LOGGER.error("Error setting up dialog", e);
            throw new RuntimeException("Failed to setup dialog", e);
        }
    }

    @Override
    protected Icon getFormIcon() {
        return VaadinIcon.EDIT.create();
    }

    @Override
    protected String getFormTitle() {
        return isNew ? "Add Screen Field" : "Edit Screen Field";
    }

    @Override
    public String getHeaderTitle() {
        return getFormTitle();
    }

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
        // print screen type:
        Check.notNull(screen, "Screen must not be null");
        divScreenType.setText("Screen type: " + screen.getEntityType());

        // Initialize ComboBox items before calling readBean to prevent binding errors
        updateEntityClassComboboxEntries();

        if (entity != null) {
            // Now populate entityProperty ComboBox if fieldClass is already set
            if (entity.getFieldClass() != null && !entity.getFieldClass().isEmpty()) {
                updateEntityPropertyBasedOnClass();
            }
            binder.readBean(entity);
        }
    }

    @Override
    protected void setupDialog() {
        super.setupDialog(); // Call parent setup first
        setWidth("700px");
        setHeight("800px");
        setResizable(true);
        createFormFields();
    }

    private void updateEntityClassComboboxEntries() {
        Check.notNull(screen, "Screen must not be null");
        // add additional field info for "this class"
        final List<EntityFieldInfo> listOfAdditionalFields;
        final EntityFieldInfo additionalFieldInfo = new EntityFieldInfo();
        additionalFieldInfo.setFieldName(CEntityFieldService.THIS_CLASS);
        listOfAdditionalFields = List.of(additionalFieldInfo);
        // get all fields + additional "this class" field
        final List<String> entityLineTypes = entityFieldService
                .getEntityRelationFields(screen.getEntityType(), listOfAdditionalFields).stream()
                .map(CEntityFieldService.EntityFieldInfo::getFieldName).toList();
        cmbFieldClass.setItems(entityLineTypes);
    }

    @SuppressWarnings("unchecked")
    private void updateEntityPropertyBasedOnClass() {
        final String fieldClass = entity.getFieldClass();
        LOGGER.debug("Selected field class: {}", fieldClass);

        if (fieldClass == null || fieldClass.isEmpty()) {
            return;
        }
        List<EntityFieldInfo> fieldProperties = null;

        // this class is a special case, we need to get all fields of the screen's entity
        // type
        if (fieldClass.equals(CEntityFieldService.THIS_CLASS)) {
            fieldProperties = entityFieldService.getEntityFields(screen.getEntityType());
        } else {
            // Get field properties for the selected class of relation
            final EntityFieldInfo info = entityFieldService.getEntityFieldInfo(screen.getEntityType().toString(),
                    fieldClass);
            fieldProperties = entityFieldService.getEntitySimpleFields(info.getJavaType(), null);
        }
        final ComboBox<String> cmbFieldProperties = ((ComboBox<String>) form.getComponent("entityProperty"));
        Check.notNull(cmbFieldProperties, "Entity property combobox must not be null");
        cmbFieldProperties
                .setItems(fieldProperties.stream().map(CEntityFieldService.EntityFieldInfo::getFieldName).toList());
    }

    private void updatePropertyDefaultValues(final String selectedProperty) {
        LOGGER.debug("Selected property: {}", selectedProperty);
        // default values
        divJavaType.setText(" ");

        if (selectedProperty == null || selectedProperty.isEmpty()) {
            return;
        }
        final String fieldClass = entity.getFieldClass();

        if (fieldClass == null || fieldClass.isEmpty()) {
            return;
        }
        EntityFieldInfo info;

        if (fieldClass.equals(CEntityFieldService.THIS_CLASS)) {
            info = entityFieldService.getEntityFieldInfo(screen.getEntityType().toString(), selectedProperty);
        } else {
            info = entityFieldService.getEntityFieldInfo(screen.getEntityType().toString(), fieldClass);
            info = entityFieldService.getEntityFieldInfo(info.getJavaType(), selectedProperty);
        }
        Check.notNull(info, "Field info must not be null for property: " + selectedProperty);
        entity.setDefaultValue(info.getDefaultValue());
        entity.setMaxLength(info.getMaxLength());
        entity.setDataProviderBean(info.getDataProviderBean());
        entity.setFieldDescription(info.getDescription());
        divJavaType.setText("Java type: " + info.getJavaType());
    }

    @Override
    protected void validateForm() {

        if (!binder.isValid()) {
            throw new IllegalStateException("Please fill in all required fields correctly");
        }
        // Write bean data back to entity
        binder.writeBeanIfValid(entity);
    }
}