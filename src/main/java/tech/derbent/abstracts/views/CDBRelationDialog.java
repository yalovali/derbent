package tech.derbent.abstracts.views;

import java.util.List;
import java.util.function.Consumer;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Abstract base class for relationship dialogs.
 * 
 * This class provides common functionality for dialogs that manage relationships
 * between two entity types. It handles the common patterns of:
 * - Entity selection via ComboBox
 * - Role and permission management
 * - Form validation with proper error messages
 * - Bidirectional relationship management
 * 
 * Child classes must implement the abstract methods to provide specific behavior
 * for their entity types.
 * 
 * @param <RelationshipClass> The relationship entity type (e.g., CUserProjectSettings)
 * @param <MainEntityClass> The main entity type that owns the relationship
 * @param <RelatedEntityClass> The related entity type being selected
 */
public abstract class CDBRelationDialog<RelationshipClass, MainEntityClass, RelatedEntityClass>
        extends CDBEditDialog<RelationshipClass> {

    private static final long serialVersionUID = 1L;

    protected final MainEntityClass mainEntity;
    
    // Common form components
    protected ComboBox<RelatedEntityClass> entityComboBox;
    protected TextField rolesField;
    protected TextField permissionsField;

    /**
     * Constructor for relationship dialogs.
     * 
     * @param relationship The relationship entity to edit, or null for new
     * @param mainEntity The main entity that owns the relationship
     * @param onSave Callback executed when relationship is saved
     * @param isNew True for new relationships, false for editing existing ones
     */
    public CDBRelationDialog(final RelationshipClass relationship, final MainEntityClass mainEntity,
            final Consumer<RelationshipClass> onSave, final boolean isNew) {
        super(relationship, onSave, isNew);
        this.mainEntity = mainEntity;
        // Child classes must call setupDialog() and populateForm() in their constructor
    }

    /**
     * Creates a new instance of the relationship class.
     * Child classes must implement this to provide proper instantiation.
     */
    protected abstract RelationshipClass createNewRelationship();

    /**
     * Returns the list of available related entities for selection.
     * Child classes must implement this to provide the appropriate entity list.
     */
    protected abstract List<RelatedEntityClass> getAvailableRelatedEntities();

    /**
     * Returns the display text for related entities in the ComboBox.
     * Child classes must implement this to provide appropriate display formatting.
     */
    protected abstract String getRelatedEntityDisplayText(RelatedEntityClass entity);

    /**
     * Returns the label text for the related entity selection field.
     * Child classes must implement this to provide appropriate labeling.
     */
    protected abstract String getRelatedEntitySelectionLabel();

    /**
     * Gets the related entity from the relationship object.
     * Child classes must implement this to extract the related entity.
     */
    protected abstract RelatedEntityClass getRelatedEntityFromRelationship(RelationshipClass relationship);

    /**
     * Gets the role from the relationship object.
     * Child classes must implement this to extract the role.
     */
    protected abstract String getRoleFromRelationship(RelationshipClass relationship);

    /**
     * Gets the permission from the relationship object.
     * Child classes must implement this to extract the permission.
     */
    protected abstract String getPermissionFromRelationship(RelationshipClass relationship);

    /**
     * Sets the related entity in the relationship object.
     * Child classes must implement this to set the related entity.
     */
    protected abstract void setRelatedEntityInRelationship(RelationshipClass relationship, 
            RelatedEntityClass relatedEntity);

    /**
     * Sets the role in the relationship object.
     * Child classes must implement this to set the role.
     */
    protected abstract void setRoleInRelationship(RelationshipClass relationship, String role);

    /**
     * Sets the permission in the relationship object.
     * Child classes must implement this to set the permission.
     */
    protected abstract void setPermissionInRelationship(RelationshipClass relationship, String permission);

    /**
     * Sets the main entity in the relationship object.
     * Child classes must implement this to set the main entity.
     */
    protected abstract void setMainEntityInRelationship(RelationshipClass relationship, MainEntityClass mainEntity);

    /**
     * Common form population logic extracted from child classes.
     * Creates and configures the common form fields.
     */
    @Override
    protected void populateForm() {
        LOGGER.debug("Populating form for {}", getClass().getSimpleName());
        
        validateFormDependencies();
        createCommonFormFields();
        populateExistingData();
    }

    /**
     * Validates that required dependencies are available before form creation.
     */
    protected void validateFormDependencies() {
        if (mainEntity == null) {
            throw new IllegalStateException("Main entity must be initialized before populating form");
        }
    }

    /**
     * Creates and configures the common form input fields.
     */
    protected void createCommonFormFields() {
        createEntitySelectionField();
        createRoleField();
        createPermissionField();
        
        formLayout.add(entityComboBox, rolesField, permissionsField);
    }

    /**
     * Creates the related entity selection dropdown.
     */
    protected void createEntitySelectionField() {
        entityComboBox = new ComboBox<>(getRelatedEntitySelectionLabel());
        entityComboBox.setAllowCustomValue(false);
        entityComboBox.setItemLabelGenerator(this::getRelatedEntityDisplayText);
        entityComboBox.setItems(getAvailableRelatedEntities());
        entityComboBox.setRequired(true);
        entityComboBox.setEnabled(isNew); // Only allow changing entity when creating new relationship
    }

    /**
     * Creates the role input field with validation and helpful hints.
     */
    protected void createRoleField() {
        rolesField = new TextField("Roles");
        rolesField.setPlaceholder("Enter roles separated by commas (e.g., DEVELOPER, MANAGER)");
        rolesField.setHelperText("Comma-separated list of roles");
        rolesField.setRequired(true);
    }

    /**
     * Creates the permission input field with validation and helpful hints.
     */
    protected void createPermissionField() {
        permissionsField = new TextField("Permissions");
        permissionsField.setPlaceholder("Enter permissions separated by commas (e.g., READ, WRITE, DELETE)");
        permissionsField.setHelperText("Comma-separated list of permissions");
        permissionsField.setRequired(true);
    }

    /**
     * Populates form fields with existing data when editing.
     */
    protected void populateExistingData() {
        if (!isNew) {
            populateEntityField();
            populateRoleField();
            populatePermissionField();
        }
    }

    /**
     * Sets the related entity field value for editing mode.
     */
    protected void populateEntityField() {
        final RelatedEntityClass relatedEntity = getRelatedEntityFromRelationship(data);
        if (relatedEntity != null) {
            entityComboBox.setValue(relatedEntity);
        }
    }

    /**
     * Sets the role field value from existing data.
     */
    protected void populateRoleField() {
        final String role = getRoleFromRelationship(data);
        if (role != null) {
            rolesField.setValue(role);
        }
    }

    /**
     * Sets the permission field value from existing data.
     */
    protected void populatePermissionField() {
        final String permission = getPermissionFromRelationship(data);
        if (permission != null) {
            permissionsField.setValue(permission);
        }
    }

    /**
     * Common form validation logic extracted from child classes.
     * Validates all required fields and updates the data object.
     */
    @Override
    protected void validateForm() {
        validateEntitySelection();
        validateRoleField();
        validatePermissionField();
        updateRelationshipData();
    }

    /**
     * Validates that a related entity has been selected.
     */
    protected void validateEntitySelection() {
        if (entityComboBox.getValue() == null) {
            throw new IllegalArgumentException("Please select a " + getRelatedEntitySelectionLabel().toLowerCase());
        }
    }

    /**
     * Validates the role field is not empty.
     */
    protected void validateRoleField() {
        final String role = rolesField.getValue();
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role is required and cannot be empty");
        }
    }

    /**
     * Validates the permission field is not empty.
     */
    protected void validatePermissionField() {
        final String permission = permissionsField.getValue();
        if (permission == null || permission.trim().isEmpty()) {
            throw new IllegalArgumentException("Permission is required and cannot be empty");
        }
    }

    /**
     * Updates the relationship data object with validated form values.
     */
    protected void updateRelationshipData() {
        setMainEntityInRelationship(data, mainEntity);
        setRelatedEntityInRelationship(data, entityComboBox.getValue());
        setRoleInRelationship(data, rolesField.getValue().trim());
        setPermissionInRelationship(data, permissionsField.getValue().trim());
    }
}
