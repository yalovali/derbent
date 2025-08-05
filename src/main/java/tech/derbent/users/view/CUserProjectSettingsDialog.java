package tech.derbent.users.view;

import java.util.List;
import java.util.function.Consumer;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;

import tech.derbent.abstracts.views.CDBEditDialog;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;

/**
 * Simplified dialog for managing user-project assignments.
 * 
 * This dialog handles both creating new project assignments and editing existing ones.
 * The form automatically adapts based on whether it's in "new" or "edit" mode.
 * 
 * Key responsibilities:
 * - Project selection (disabled in edit mode to prevent data inconsistency)
 * - Role and permission management with validation
 * - Form population and validation with clear error messages
 */
public class CUserProjectSettingsDialog extends CDBEditDialog<CUserProjectSettings> {

    private static final long serialVersionUID = 1L;

    private final CProjectService projectService;
    private final CUser user;

    // Form components - organized for clarity
    private ComboBox<CProject> projectComboBox;
    private TextField rolesField;
    private TextField permissionsField;

    /**
     * Constructor for the dialog. Supports both new assignments and editing existing ones.
     * 
     * @param projectService Service for project operations
     * @param settings Existing settings to edit, or null for new assignment
     * @param user The user being assigned to projects
     * @param onSave Callback executed when settings are successfully saved
     */
    public CUserProjectSettingsDialog(final CProjectService projectService, final CUserProjectSettings settings,
            final CUser user, final Consumer<CUserProjectSettings> onSave) {
        // Initialize with existing settings or create new instance for new assignments
        super(settings != null ? settings : new CUserProjectSettings(), onSave, settings == null);
        this.projectService = projectService;
        this.user = user;
        
        setupDialog();
        populateForm();
    }

    /** Returns available projects for selection. */
    private List<CProject> getAvailableProjects() {
        final List<CProject> allProjects = projectService.findAll();

        if (!isNew && (data.getProject() != null)) {
            projectService.getById(data.getProject().getId()).ifPresent(project -> {

                if (!allProjects.contains(project)) {
                    allProjects.add(project);
                }
            });
        }
        return allProjects;
    }

    @Override
    protected Icon getFormIcon() {
        return VaadinIcon.USER_CHECK.create();
    }

    @Override
    protected String getFormTitle() {
        return isNew ? "Assign User to Project" : "Edit Project Assignment";
    }

    @Override
    public String getHeaderTitle() {
        return isNew ? "Add Project Assignment" : "Edit Project Assignment";
    }

    @Override
    protected String getSuccessCreateMessage() {
        return "Project assignment created successfully";
    }

    @Override
    protected String getSuccessUpdateMessage() {
        return "Project assignment updated successfully";
    }

    /**
     * Simplified form population that creates and configures all form fields.
     * Separated into logical sections for better maintainability.
     */
    @Override
    protected void populateForm() {
        LOGGER.debug("Populating form for {}", getClass().getSimpleName());
        
        validateFormDependencies();
        createFormFields();
        populateExistingData();
    }

    /**
     * Validates that required dependencies are available before form creation.
     */
    private void validateFormDependencies() {
        if (projectService == null || user == null) {
            throw new IllegalStateException("ProjectService and User must be initialized before populating form");
        }
    }

    /**
     * Creates and configures all form input fields with appropriate settings.
     */
    private void createFormFields() {
        createProjectSelectionField();
        createRoleField();
        createPermissionField();
        
        formLayout.add(projectComboBox, rolesField, permissionsField);
    }

    /**
     * Creates the project selection dropdown with appropriate restrictions.
     */
    private void createProjectSelectionField() {
        projectComboBox = new ComboBox<>("Project");
        projectComboBox.setAllowCustomValue(false); // Enforce selection-only per coding guidelines
        projectComboBox.setItemLabelGenerator(CProject::getName);
        projectComboBox.setItems(getAvailableProjects());
        projectComboBox.setRequired(true);
        projectComboBox.setEnabled(isNew); // Disable in edit mode to prevent data inconsistency
    }

    /**
     * Creates the role input field with validation and helpful hints.
     */
    private void createRoleField() {
        rolesField = new TextField("Roles");
        rolesField.setPlaceholder("Enter roles separated by commas (e.g., DEVELOPER, MANAGER)");
        rolesField.setHelperText("Comma-separated list of roles for this project");
        rolesField.setRequired(true);
    }

    /**
     * Creates the permission input field with validation and helpful hints.
     */
    private void createPermissionField() {
        permissionsField = new TextField("Permissions");
        permissionsField.setPlaceholder("Enter permissions separated by commas (e.g., READ, WRITE, DELETE)");
        permissionsField.setHelperText("Comma-separated list of permissions for this project");
        permissionsField.setRequired(true);
    }

    /**
     * Populates form fields with existing data when editing.
     */
    private void populateExistingData() {
        if (!isNew) {
            populateProjectField();
            populateRoleField();
            populatePermissionField();
        }
    }

    /**
     * Sets the project field value for editing mode.
     */
    private void populateProjectField() {
        if (data.getProject() != null) {
            projectService.getById(data.getProject().getId()).ifPresent(projectComboBox::setValue);
        }
    }

    /**
     * Sets the role field value from existing data.
     */
    private void populateRoleField() {
        if (data.getRole() != null) {
            rolesField.setValue(data.getRole());
        }
    }

    /**
     * Sets the permission field value from existing data.
     */
    private void populatePermissionField() {
        if (data.getPermission() != null) {
            permissionsField.setValue(data.getPermission());
        }
    }

    /**
     * Simplified form validation with clear error messages.
     * Validates all required fields and updates the data object.
     */
    @Override
    protected void validateForm() {
        validateProjectSelection();
        validateRoleField();
        validatePermissionField();
        updateDataObject();
    }

    /**
     * Validates that a project has been selected.
     */
    private void validateProjectSelection() {
        if (projectComboBox.getValue() == null) {
            throw new IllegalArgumentException("Please select a project");
        }
    }

    /**
     * Validates the role field is not empty.
     */
    private void validateRoleField() {
        final String role = rolesField.getValue();
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role is required and cannot be empty");
        }
    }

    /**
     * Validates the permission field is not empty.
     */
    private void validatePermissionField() {
        final String permission = permissionsField.getValue();
        if (permission == null || permission.trim().isEmpty()) {
            throw new IllegalArgumentException("Permission is required and cannot be empty");
        }
    }

    /**
     * Updates the data object with validated form values.
     */
    private void updateDataObject() {
        data.setUser(user);
        data.setProject(projectComboBox.getValue());
        data.setRole(rolesField.getValue().trim());
        data.setPermission(permissionsField.getValue().trim());
    }
}