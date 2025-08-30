package tech.derbent.users.view;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.abstracts.views.dialogs.CDBRelationDialog;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserService;

/**
 * Dialog for managing user-project assignments from the user perspective. This dialog handles both creating new project
 * assignments and editing existing ones. It allows users to be assigned to projects with specific roles and
 * permissions. Inherits common relationship management logic from CDBRelationDialog.
 */
public class CUserProjectSettingsDialog extends CDBRelationDialog<CUserProjectSettings, CUser, CProject> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor for the dialog.
     * 
     * @param projectService
     *            Service for project operations
     * @param settings
     *            Existing settings to edit, or null for new assignment
     * @param user
     *            The user being assigned to projects
     * @param onSave
     *            Callback executed when settings are successfully saved
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    public CUserProjectSettingsDialog(final CUserService masterService, final CProjectService detailService,
            final CUserProjectSettings settings, final CUser user, final Consumer<CUserProjectSettings> onSave) {
        super(settings != null ? settings : new CUserProjectSettings(), user, masterService, detailService, onSave,
                settings == null);
        setupDialog();
        populateForm();
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
    protected String getPermissionFromRelationship(final CUserProjectSettings relationship) {
        return relationship.getPermission();
    }

    @Override
    protected String getRelatedEntityDisplayText(final CProject project) {
        return project.getName();
    }

    @Override
    protected CProject getRelatedEntityFromRelationship(final CUserProjectSettings relationship) {
        return relationship.getProject();
    }

    @Override
    protected String getRelatedEntitySelectionLabel() {
        return "Project";
    }

    @Override
    protected String getRoleFromRelationship(final CUserProjectSettings relationship) {
        return relationship.getRole();
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
     * Override to provide project-specific validation in populateEntityField.
     */
    @Override
    protected void populateEntityField() {
        final CProject project = getRelatedEntityFromRelationship(entity);

        if (project != null) {
            detailService.getById(project.getId()).ifPresent(entityComboBox::setValue);
        }
    }

    @Override
    protected void setMainEntityInRelationship(final CUserProjectSettings relationship, final CUser user) {
        relationship.setUser(user);
    }

    @Override
    protected void setPermissionInRelationship(final CUserProjectSettings relationship, final String permission) {
        relationship.setPermission(permission);
    }

    @Override
    protected void setRelatedEntityInRelationship(final CUserProjectSettings relationship, final CProject project) {
        relationship.setProject(project);
    }

    @Override
    protected void setRoleInRelationship(final CUserProjectSettings relationship, final String role) {
        relationship.setRole(role);
    }
}