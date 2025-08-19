package tech.derbent.projects.view;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import tech.derbent.abstracts.views.CDBRelationDialog;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserService;

/**
 * Dialog for adding users to a project (reverse direction). This dialog manages the relationship between projects and
 * users by creating and editing CUserProjectSettings entities. It allows project managers to: - Add new users to a
 * project with specific roles and permissions - Edit existing user assignments for the project Inherits common
 * relationship management logic from CDBRelationDialog.
 */
public class CProjectUserSettingsDialog extends CDBRelationDialog<CUserProjectSettings, CProject, CUser> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor for the dialog.
     * 
     * @param userService
     *            Service for user operations
     * @param settings
     *            Existing settings to edit, or null for new assignment
     * @param project
     *            The project to manage user assignments for
     * @param onSave
     *            Callback executed when settings are successfully saved
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    public CProjectUserSettingsDialog(final CProjectService masterService, final CUserService detailService,
            final CUserProjectSettings settings, final CProject project, final Consumer<CUserProjectSettings> onSave) {
        super(settings != null ? settings : new CUserProjectSettings(), project, masterService, detailService, onSave,
                settings == null);
        setupDialog();
        populateForm();
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
    protected String getPermissionFromRelationship(final CUserProjectSettings relationship) {
        return relationship.getPermission();
    }

    @Override
    protected String getRelatedEntityDisplayText(final CUser user) {
        return user.getName() + " " + (user.getLastname() != null ? user.getLastname() : "") + " (" + user.getLogin()
                + ")";
    }

    @Override
    protected CUser getRelatedEntityFromRelationship(final CUserProjectSettings relationship) {
        return relationship.getUser();
    }

    @Override
    protected String getRelatedEntitySelectionLabel() {
        return "User";
    }

    @Override
    protected String getRoleFromRelationship(final CUserProjectSettings relationship) {
        return relationship.getRole();
    }

    @Override
    protected String getSuccessCreateMessage() {
        return "User added to project successfully";
    }

    @Override
    protected String getSuccessUpdateMessage() {
        return "User project assignment updated successfully";
    }

    @Override
    protected void setMainEntityInRelationship(final CUserProjectSettings relationship, final CProject project) {
        relationship.setProject(project);
    }

    @Override
    protected void setPermissionInRelationship(final CUserProjectSettings relationship, final String permission) {
        relationship.setPermission(permission);
    }

    @Override
    protected void setRelatedEntityInRelationship(final CUserProjectSettings relationship, final CUser user) {
        relationship.setUser(user);
    }

    @Override
    protected void setRoleInRelationship(final CUserProjectSettings relationship, final String role) {
        relationship.setRole(role);
    }
}