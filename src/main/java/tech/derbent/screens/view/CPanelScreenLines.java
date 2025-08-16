package tech.derbent.screens.view;

import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.binder.Binder;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.domain.CScreenLines;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.screens.service.CScreenLinesService;
import tech.derbent.screens.service.CEntityFieldService;

public class CPanelScreenLines extends CPanelScreenBase {

    private static final long serialVersionUID = 1L;

    private final CScreenLinesService screenLinesService;
    private final CEntityFieldService entityFieldService;
    private Grid<CScreenLines> linesGrid;
    private CScreenLines selectedLine;
    private Binder<CScreenLines> linesBinder;
    
    // Form fields for screen line editing
    private TextField lineOrderField;
    private TextField fieldCaptionField;
    private ComboBox<String> entityTypeCombo;
    private ComboBox<CEntityFieldService.EntityFieldInfo> entityFieldCombo;
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

    public CPanelScreenLines(final CScreen currentEntity,
                            final CEnhancedBinder<CScreen> beanValidationBinder,
                            final CScreenService entityService,
                            final CScreenLinesService screenLinesService,
                            final CEntityFieldService entityFieldService) {
        super("Screen Lines", currentEntity, beanValidationBinder, entityService);
        this.screenLinesService = screenLinesService;
        this.entityFieldService = entityFieldService;
        this.linesBinder = new Binder<>(CScreenLines.class);
        initPanel();
        createScreenLinesLayout();
    }

    @Override
    protected void updatePanelEntityFields() {
        // This panel doesn't use the standard entity fields approach
        // as it manages CScreenLines separately
        setEntityFields(List.of());
    }

    private void createScreenLinesLayout() {
        final VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        // Title
        final H3 title = new H3("Screen Field Definitions");
        layout.add(title);

        // Lines grid (create first before toolbar so it's available)
        createLinesGrid();

        // Toolbar
        final HorizontalLayout toolbar = createLinesToolbar();
        layout.add(toolbar);

        // Grid and form in horizontal layout
        final HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);

        // Lines grid (left side)
        final Div gridWrapper = new Div(linesGrid);
        gridWrapper.setWidth("60%");
        gridWrapper.setHeightFull();

        // Line form (right side)
        final VerticalLayout formLayout = createLineForm();
        formLayout.setWidth("40%");

        mainLayout.add(gridWrapper, formLayout);
        layout.add(mainLayout);

        // Set content for the accordion panel
        addToContent(layout);
        
        refreshLinesGrid();
    }

    private HorizontalLayout createLinesToolbar() {
        final HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setSpacing(true);

        final Button addButton = new Button("Add Line", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> addNewLine());

        final Button deleteButton = new Button("Delete", VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> deleteSelectedLine());
        deleteButton.setEnabled(false);

        final Button moveUpButton = new Button("Move Up", VaadinIcon.ARROW_UP.create());
        moveUpButton.addClickListener(e -> moveLineUp());
        moveUpButton.setEnabled(false);

        final Button moveDownButton = new Button("Move Down", VaadinIcon.ARROW_DOWN.create());
        moveDownButton.addClickListener(e -> moveLineDown());
        moveDownButton.setEnabled(false);

        // Enable/disable buttons based on selection
        linesGrid.asSingleSelect().addValueChangeListener(e -> {
            final boolean hasSelection = e.getValue() != null;
            deleteButton.setEnabled(hasSelection);
            moveUpButton.setEnabled(hasSelection);
            moveDownButton.setEnabled(hasSelection);
            
            if (hasSelection) {
                selectedLine = e.getValue();
                populateLineForm(selectedLine);
            } else {
                selectedLine = null;
                clearLineForm();
            }
        });

        toolbar.add(addButton, deleteButton, moveUpButton, moveDownButton);
        return toolbar;
    }

    private void createLinesGrid() {
        linesGrid = new Grid<>(CScreenLines.class, false);
        linesGrid.setHeightFull();

        linesGrid.addColumn(CScreenLines::getLineOrder).setHeader("Order").setWidth("80px");
        linesGrid.addColumn(CScreenLines::getFieldCaption).setHeader("Caption").setAutoWidth(true);
        linesGrid.addColumn(CScreenLines::getEntityFieldName).setHeader("Field Name").setAutoWidth(true);
        linesGrid.addColumn(CScreenLines::getFieldType).setHeader("Type").setWidth("100px");
        linesGrid.addColumn(line -> line.getIsRequired() ? "Yes" : "No").setHeader("Required").setWidth("80px");
        linesGrid.addColumn(line -> line.getIsActive() ? "Active" : "Inactive").setHeader("Status").setWidth("80px");
    }

    private VerticalLayout createLineForm() {
        final VerticalLayout formLayout = new VerticalLayout();
        formLayout.setPadding(true);
        formLayout.setSpacing(true);

        final H3 formTitle = new H3("Line Details");
        formLayout.add(formTitle);

        // Create form fields
        lineOrderField = new TextField("Line Order");
        lineOrderField.setReadOnly(true);
        
        fieldCaptionField = new TextField("Field Caption");
        fieldCaptionField.setRequired(true);
        
        // Entity type selection
        entityTypeCombo = new ComboBox<>("Entity Type");
        entityTypeCombo.setItems(entityFieldService.getAvailableEntityTypes());
        entityTypeCombo.setRequired(true);
        entityTypeCombo.addValueChangeListener(e -> updateEntityFieldCombo(e.getValue()));
        
        // Entity field selection (populated based on entity type)
        entityFieldCombo = new ComboBox<>("Entity Field");
        entityFieldCombo.setRequired(true);
        entityFieldCombo.setItemLabelGenerator(field -> field.toString());
        entityFieldCombo.addValueChangeListener(e -> populateFieldFromSelection(e.getValue()));
        
        fieldDescriptionField = new TextField("Field Description");
        
        fieldTypeCombo = new ComboBox<>("Field Type");
        fieldTypeCombo.setItems("TEXT", "NUMBER", "DATE", "BOOLEAN", "REFERENCE");
        fieldTypeCombo.setRequired(true);
        fieldTypeCombo.setValue("TEXT");
        
        isRequiredField = new Checkbox("Required");
        isReadonlyField = new Checkbox("Read Only");
        isHiddenField = new Checkbox("Hidden");
        
        defaultValueField = new TextField("Default Value");
        relatedEntityTypeField = new TextField("Related Entity Type");
        
        dataProviderBeanCombo = new ComboBox<>("Data Provider Bean");
        dataProviderBeanCombo.setItems(entityFieldService.getDataProviderBeans());
        
        maxLengthField = new TextField("Max Length");
        
        isActiveField = new Checkbox("Active");
        isActiveField.setValue(true);

        // Bind fields
        linesBinder.forField(fieldCaptionField).bind(CScreenLines::getFieldCaption, CScreenLines::setFieldCaption);
        linesBinder.forField(fieldDescriptionField).bind(CScreenLines::getFieldDescription, CScreenLines::setFieldDescription);
        linesBinder.forField(fieldTypeCombo).bind(CScreenLines::getFieldType, CScreenLines::setFieldType);
        linesBinder.forField(isRequiredField).bind(CScreenLines::getIsRequired, CScreenLines::setIsRequired);
        linesBinder.forField(isReadonlyField).bind(CScreenLines::getIsReadonly, CScreenLines::setIsReadonly);
        linesBinder.forField(isHiddenField).bind(CScreenLines::getIsHidden, CScreenLines::setIsHidden);
        linesBinder.forField(defaultValueField).bind(CScreenLines::getDefaultValue, CScreenLines::setDefaultValue);
        linesBinder.forField(relatedEntityTypeField).bind(CScreenLines::getRelatedEntityType, CScreenLines::setRelatedEntityType);
        linesBinder.forField(dataProviderBeanCombo).bind(CScreenLines::getDataProviderBean, CScreenLines::setDataProviderBean);
        linesBinder.forField(maxLengthField).asRequired().withConverter(
            value -> value.isEmpty() ? null : Integer.valueOf(value),
            value -> value == null ? "" : value.toString()
        ).bind(CScreenLines::getMaxLength, CScreenLines::setMaxLength);
        linesBinder.forField(isActiveField).bind(CScreenLines::getIsActive, CScreenLines::setIsActive);

        // Custom binding for entity field name (derived from combo selection)
        linesBinder.forField(entityFieldCombo)
            .withConverter(
                fieldInfo -> fieldInfo != null ? fieldInfo.getFieldName() : null,
                fieldName -> {
                    if (fieldName != null && entityTypeCombo.getValue() != null) {
                        return entityFieldService.getEntityFields(entityTypeCombo.getValue())
                            .stream()
                            .filter(f -> f.getFieldName().equals(fieldName))
                            .findFirst()
                            .orElse(null);
                    }
                    return null;
                }
            )
            .bind(CScreenLines::getEntityFieldName, CScreenLines::setEntityFieldName);

        // Save button
        final Button saveButton = new Button("Save Line", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveCurrentLine());

        formLayout.add(
            lineOrderField, fieldCaptionField, entityTypeCombo, entityFieldCombo, fieldDescriptionField,
            fieldTypeCombo, isRequiredField, isReadonlyField, isHiddenField,
            defaultValueField, relatedEntityTypeField, dataProviderBeanCombo, maxLengthField,
            isActiveField, saveButton
        );

        return formLayout;
    }

    private void addNewLine() {
        if (getCurrentEntity() == null || getCurrentEntity().getId() == null) {
            Notification.show("Please save the screen first before adding lines", 3000, Notification.Position.MIDDLE);
            return;
        }

        final CScreenLines newLine = screenLinesService.newEntity(getCurrentEntity(), "New Field", "newField");
        selectedLine = newLine;
        populateLineForm(newLine);
        fieldCaptionField.focus();
    }

    private void deleteSelectedLine() {
        if (selectedLine != null && selectedLine.getId() != null) {
            try {
                screenLinesService.delete(selectedLine);
                refreshLinesGrid();
                clearLineForm();
                Notification.show("Line deleted successfully", 3000, Notification.Position.BOTTOM_START);
            } catch (Exception e) {
                Notification.show("Error deleting line: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        }
    }

    private void moveLineUp() {
        if (selectedLine != null) {
            try {
                screenLinesService.moveLineUp(selectedLine);
                refreshLinesGrid();
                Notification.show("Line moved up", 2000, Notification.Position.BOTTOM_START);
            } catch (Exception e) {
                Notification.show("Error moving line: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        }
    }

    private void moveLineDown() {
        if (selectedLine != null) {
            try {
                screenLinesService.moveLineDown(selectedLine);
                refreshLinesGrid();
                Notification.show("Line moved down", 2000, Notification.Position.BOTTOM_START);
            } catch (Exception e) {
                Notification.show("Error moving line: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        }
    }

    private void saveCurrentLine() {
        if (selectedLine != null && linesBinder.writeBeanIfValid(selectedLine)) {
            try {
                screenLinesService.save(selectedLine);
                refreshLinesGrid();
                Notification.show("Line saved successfully", 3000, Notification.Position.BOTTOM_START);
            } catch (Exception e) {
                Notification.show("Error saving line: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        } else {
            Notification.show("Please fill in all required fields", 3000, Notification.Position.MIDDLE);
        }
    }

    private void populateLineForm(final CScreenLines line) {
        if (line != null) {
            linesBinder.readBean(line);
            lineOrderField.setValue(String.valueOf(line.getLineOrder()));
            
            // Try to determine entity type from the existing line
            if (line.getEntityFieldName() != null && getCurrentEntity() != null && getCurrentEntity().getEntityType() != null) {
                entityTypeCombo.setValue(getCurrentEntity().getEntityType());
                updateEntityFieldCombo(getCurrentEntity().getEntityType());
                
                // Find and select the field in the combo
                entityFieldCombo.getDataProvider().refreshAll();
                final String fieldName = line.getEntityFieldName();
                entityFieldCombo.setValue(
                    entityFieldService.getEntityFields(getCurrentEntity().getEntityType())
                        .stream()
                        .filter(f -> f.getFieldName().equals(fieldName))
                        .findFirst()
                        .orElse(null)
                );
            }
        }
    }

    private void clearLineForm() {
        linesBinder.readBean(null);
        lineOrderField.clear();
        entityTypeCombo.clear();
        entityFieldCombo.clear();
    }

    private void refreshLinesGrid() {
        if (getCurrentEntity() != null) {
            final List<CScreenLines> lines = screenLinesService.findByScreenOrderByLineOrder(getCurrentEntity());
            linesGrid.setItems(lines);
        } else {
            linesGrid.setItems();
        }
    }

    /**
     * Update the entity field combo when entity type changes.
     */
    private void updateEntityFieldCombo(String entityType) {
        if (entityType != null) {
            final List<CEntityFieldService.EntityFieldInfo> fields = entityFieldService.getEntityFields(entityType);
            entityFieldCombo.setItems(fields);
            entityFieldCombo.clear();
        } else {
            entityFieldCombo.clear();
            entityFieldCombo.setItems();
        }
    }

    /**
     * Populate form fields from the selected entity field.
     */
    private void populateFieldFromSelection(CEntityFieldService.EntityFieldInfo fieldInfo) {
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
}