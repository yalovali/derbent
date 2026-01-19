package tech.derbent.api.reports.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.service.CEntityFieldService;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.utils.Check;

/**
 * Dialog for selecting entity fields to include in a CSV report.
 * Groups fields into simple fields and complex/relation fields with checkboxes.
 * Follows UI coding rules: max-width 600px, 2-column layout for 6+ items, custom gaps.
 */
public class CDialogReportFieldSelection extends Dialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(CDialogReportFieldSelection.class);
    private static final long serialVersionUID = 1L;

    private final Class<?> entityClass;
    private final Map<String, Checkbox> checkboxMap = new HashMap<>();
    private Button buttonSelectAll;
    private boolean allSelected = false;
    private final Consumer<List<EntityFieldInfo>> onConfirm;

    /**
     * Creates a field selection dialog for CSV report.
     * 
     * @param entityClass the entity class
     * @param onConfirm callback when user confirms field selection
     * @throws Exception if dialog setup fails
     */
    public CDialogReportFieldSelection(final Class<?> entityClass, final Consumer<List<EntityFieldInfo>> onConfirm)
            throws Exception {
        Check.notNull(entityClass, "Entity class cannot be null");
        Check.notNull(onConfirm, "Confirmation callback cannot be null");

        this.entityClass = entityClass;
        this.onConfirm = onConfirm;

        setupDialog();
    }

    /**
     * Sets up the dialog UI.
     */
    private void setupDialog() throws Exception {
        // Dialog configuration
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
        setModal(true);
        setDraggable(true);
        setResizable(false);

        // Main layout with max-width and responsive width
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMaxWidth("600px");
        mainLayout.setWidthFull();
        mainLayout.setSpacing(false);
        mainLayout.getStyle().set("gap", "12px");
        mainLayout.setPadding(true);

        // Title
        final String entityTitle = CEntityRegistry.getEntityTitleSingular(entityClass);
        final H4 title = new H4("Select Fields for " + (entityTitle != null ? entityTitle : entityClass.getSimpleName()) + " Report");
        title.getStyle().set("margin", "0");

        // Select All button
        buttonSelectAll = new Button("Select All", event -> toggleSelectAll());
        buttonSelectAll.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        // Header with title and select all
        final HorizontalLayout headerLayout = new HorizontalLayout(title, buttonSelectAll);
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerLayout.getStyle().set("margin-bottom", "8px");

        // Get entity fields
        final String entityType = entityClass.getSimpleName();
        final List<EntityFieldInfo> allFields = CEntityFieldService.getEntityFields(entityType);
        final List<EntityFieldInfo> simpleFields = new ArrayList<>();
        final List<EntityFieldInfo> complexFields = new ArrayList<>();

        // Separate simple and complex fields
        for (final EntityFieldInfo field : allFields) {
            if (field.isHidden()) {
                continue; // Skip hidden fields
            }

            if (CEntityFieldService.isRelationType(field.getFieldTypeClass())) {
                complexFields.add(field);
            } else {
                simpleFields.add(field);
            }
        }

        // Create field sections
        mainLayout.add(headerLayout);

        if (!simpleFields.isEmpty()) {
            final Div simpleFieldsSection = createFieldSection("Base Fields", simpleFields, entityType);
            mainLayout.add(simpleFieldsSection);
        }

        if (!complexFields.isEmpty()) {
            final Div complexFieldsSection = createFieldSection("Related Fields", complexFields, entityType);
            mainLayout.add(complexFieldsSection);
        }

        // Buttons
        final Button confirmButton = new Button("Generate Report", event -> onConfirmClicked());
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        final Button cancelButton = new Button("Cancel", event -> close());

        final HorizontalLayout buttonLayout = new HorizontalLayout(confirmButton, cancelButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setWidthFull();
        buttonLayout.getStyle().set("margin-top", "8px");

        mainLayout.add(buttonLayout);
        add(mainLayout);

        LOGGER.debug("Created field selection dialog for entity: {}", entityClass.getSimpleName());
    }

    /**
     * Creates a field section with checkboxes.
     * Uses 2-column layout for 6+ checkboxes.
     */
    private Div createFieldSection(final String sectionTitle, final List<EntityFieldInfo> fields, final String entityType) {
        final Div section = new Div();
        section.getStyle().set("background", "var(--lumo-contrast-5pct)");
        section.getStyle().set("border-radius", "8px");
        section.getStyle().set("padding", "12px");

        final H4 header = new H4(sectionTitle);
        header.getStyle().set("margin", "0 0 8px 0");
        header.getStyle().set("font-size", "var(--lumo-font-size-m)");
        section.add(header);

        // Determine layout: 1 or 2 columns
        if (fields.size() >= 6) {
            // Use 2-column layout
            final HorizontalLayout columnsLayout = new HorizontalLayout();
            columnsLayout.setWidthFull();
            columnsLayout.setSpacing(false);
            columnsLayout.getStyle().set("gap", "16px");

            final VerticalLayout leftColumn = new VerticalLayout();
            leftColumn.setSpacing(false);
            leftColumn.getStyle().set("gap", "8px");
            leftColumn.setPadding(false);
            leftColumn.setWidthFull();

            final VerticalLayout rightColumn = new VerticalLayout();
            rightColumn.setSpacing(false);
            rightColumn.getStyle().set("gap", "8px");
            rightColumn.setPadding(false);
            rightColumn.setWidthFull();

            // Distribute fields evenly
            for (int i = 0; i < fields.size(); i++) {
                final EntityFieldInfo field = fields.get(i);
                final Checkbox checkbox = createFieldCheckbox(field);

                if (i < (fields.size() + 1) / 2) {
                    leftColumn.add(checkbox);
                } else {
                    rightColumn.add(checkbox);
                }
            }

            columnsLayout.add(leftColumn, rightColumn);
            section.add(columnsLayout);
        } else {
            // Use 1-column layout
            final VerticalLayout singleColumn = new VerticalLayout();
            singleColumn.setSpacing(false);
            singleColumn.getStyle().set("gap", "8px");
            singleColumn.setPadding(false);

            for (final EntityFieldInfo field : fields) {
                final Checkbox checkbox = createFieldCheckbox(field);
                singleColumn.add(checkbox);
            }

            section.add(singleColumn);
        }

        return section;
    }

    /**
     * Creates a checkbox for a field.
     */
    private Checkbox createFieldCheckbox(final EntityFieldInfo field) {
        final Checkbox checkbox = new Checkbox(field.getDisplayName());
        checkbox.setValue(true); // Select all by default
        checkboxMap.put(field.getFieldName(), checkbox);
        return checkbox;
    }

    /**
     * Toggles selection of all checkboxes.
     */
    private void toggleSelectAll() {
        allSelected = !allSelected;
        final boolean checkValue = allSelected;

        buttonSelectAll.setText(allSelected ? "Deselect All" : "Select All");

        // Apply same value to all checkboxes
        for (final Checkbox checkbox : checkboxMap.values()) {
            checkbox.setValue(checkValue);
        }

        LOGGER.debug("Toggled all checkboxes to: {}", checkValue);
    }

    /**
     * Handles confirm button click.
     */
    private void onConfirmClicked() {
        try {
            final List<EntityFieldInfo> selectedFields = getSelectedFields();

            if (selectedFields.isEmpty()) {
                LOGGER.warn("No fields selected for report");
                // Could show a warning notification here
                return;
            }

            LOGGER.info("User confirmed field selection: {} fields selected", selectedFields.size());
            close();
            onConfirm.accept(selectedFields);
        } catch (final Exception e) {
            LOGGER.error("Error processing field selection: {}", e.getMessage(), e);
        }
    }

    /**
     * Gets the list of selected fields.
     */
    private List<EntityFieldInfo> getSelectedFields() throws Exception {
        final List<EntityFieldInfo> selectedFields = new ArrayList<>();
        final String entityType = entityClass.getSimpleName();
        final List<EntityFieldInfo> allFields = CEntityFieldService.getEntityFields(entityType);

        for (final EntityFieldInfo field : allFields) {
            final Checkbox checkbox = checkboxMap.get(field.getFieldName());
            if (checkbox != null && checkbox.getValue()) {
                selectedFields.add(field);
            }
        }

        return selectedFields;
    }
}
