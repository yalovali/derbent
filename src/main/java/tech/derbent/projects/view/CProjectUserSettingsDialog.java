package tech.derbent.projects.view;

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
import tech.derbent.users.service.CUserService;

/**
 * Dialog for adding users to a project (reverse direction).
 * Inherits generic dialog logic from CDBEditDialog.
 */
public class CProjectUserSettingsDialog extends CDBEditDialog<CUserProjectSettings> {

    private static final long serialVersionUID = 1L;

    private final CProjectService projectService;
    private CUserService userService;
    private final CProject project;

    // Form components
    private ComboBox<CUser> userComboBox;
    private TextField rolesField;
    private TextField permissionsField;

    public CProjectUserSettingsDialog(final CProjectService projectService, 
            final CUserProjectSettings settings,
            final CProject project, final Consumer<CUserProjectSettings> onSave) {
        // Call parent constructor with provided settings or new instance if null
        super(settings != null ? settings : new CUserProjectSettings(), onSave, settings == null);
        this.projectService = projectService;
        this.project = project;
        
        // We'll need to inject or find the user service
        // For now, we'll handle this in setupDialog
        
        setupDialog();
        populateForm();
    }

    /** Returns available users for selection. */
    private List<CUser> getAvailableUsers() {
        // TODO: Get this from a UserService - for now return empty list
        // This would normally be injected or retrieved from application context
        return java.util.Collections.emptyList();
    }

    @Override
    protected Icon getFormIcon() {
        return VaadinIcon.USERS.create();
    }

    @Override
    protected String getFormTitle() {
        return isNew ? "Add User to Project" : "Edit User Project Assignment";
    }

    @Override
    public String getHeaderTitle() {
        return isNew ? "Add User to Project" : "Edit User Assignment";
    }

    @Override
    protected String getSuccessCreateMessage() {
        return "User added to project successfully";
    }

    @Override
    protected String getSuccessUpdateMessage() {
        return "User project assignment updated successfully";
    }

    /** Populates form fields from data. */
    @Override
    protected void populateForm() {
        LOGGER.debug("Populating form for {}", getClass().getSimpleName());

        if ((projectService == null) || (project == null)) {
            throw new IllegalStateException("ProjectService and Project must be initialized before populating form");
        }

        // User selection
        userComboBox = new ComboBox<>("User");
        userComboBox.setAllowCustomValue(false);
        userComboBox.setItemLabelGenerator(user -> user.getName() + " " + 
            (user.getLastname() != null ? user.getLastname() : "") + " (" + user.getLogin() + ")");
        userComboBox.setItems(getAvailableUsers());
        userComboBox.setRequired(true);
        userComboBox.setEnabled(isNew); // Only allow changing user when creating new assignment

        // Roles field
        rolesField = new TextField("Roles");
        rolesField.setPlaceholder("Enter roles separated by commas (e.g., DEVELOPER, MANAGER)");
        rolesField.setHelperText("Comma-separated list of roles for this project");

        // Permissions field
        permissionsField = new TextField("Permissions");
        permissionsField.setPlaceholder("Enter permissions separated by commas (e.g., READ, WRITE, DELETE)");
        permissionsField.setHelperText("Comma-separated list of permissions for this project");

        formLayout.add(userComboBox, rolesField, permissionsField);

        if (!isNew) {
            if (data.getUser() != null) {
                userComboBox.setValue(data.getUser());
            }

            if (data.getRole() != null) {
                rolesField.setValue(data.getRole());
            }

            if (data.getPermission() != null) {
                permissionsField.setValue(data.getPermission());
            }
        }
    }

    /** Validates form fields. Throws exception if invalid. */
    @Override
    protected void validateForm() {
        if (userComboBox.getValue() == null) {
            throw new IllegalArgumentException("Please select a user");
        }

        // Set project and user
        data.setProject(project);
        final CUser selectedUser = userComboBox.getValue();
        if (selectedUser != null) {
            data.setUser(selectedUser);
        }

        data.setRole(rolesField.getValue());
        data.setPermission(permissionsField.getValue());
    }

    /**
     * Sets the user service for getting available users
     * This is a temporary solution until proper dependency injection is set up
     */
    public void setUserService(final CUserService userService) {
        this.userService = userService;
        if (userComboBox != null) {
            // Refresh user list if combo box is already created
            userComboBox.setItems(getAvailableUsers());
        }
    }
}