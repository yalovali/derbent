package tech.derbent.projects.view;

import java.util.List;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.views.CPanelUserProjectBase;
import tech.derbent.base.ui.dialogs.CWarningDialog;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.view.CUserProjectSettingsDialog;

/**
 * Panel for managing users within a project (reverse direction of CPanelUserProjectSettings).
 * This panel allows editing which users have access to a specific project and their roles/permissions.
 */
public class CPanelProjectUsers extends CPanelUserProjectBase<CProject> {

    private static final long serialVersionUID = 1L;

    private CProject currentProject;

    public CPanelProjectUsers(final CProject currentEntity,
            final CEnhancedBinder<CProject> beanValidationBinder,
            final CProjectService entityService, final CProjectService projectService) {
        super("Project Users", currentEntity, beanValidationBinder, CProject.class, entityService, projectService);
    }

    @Override
    protected String createDeleteConfirmationMessage(final CUserProjectSettings selected) {
        final String userName = selected.getUser() != null ? 
            selected.getUser().getName() + " " + (selected.getUser().getLastname() != null ? selected.getUser().getLastname() : "") :
            "Unknown User";
        return String.format(
            "Are you sure you want to remove user '%s' from this project? This action cannot be undone.",
            userName);
    }

    @Override
    protected void onSettingsSaved(final CUserProjectSettings settings) {
        if ((getSettings != null) && (setSettings != null)) {
            final List<CUserProjectSettings> settingsList = getSettings.get();
            // Check if this is an update or a new addition
            boolean found = false;

            for (int i = 0; i < settingsList.size(); i++) {
                final CUserProjectSettings existing = settingsList.get(i);

                if ((existing.getId() != null)
                    && existing.getId().equals(settings.getId())) {
                    settingsList.set(i, settings);
                    found = true;
                    break;
                }
            }

            if (!found) {
                settingsList.add(settings);
            }
            setSettings.accept(settingsList);

            if (saveEntity != null) {
                saveEntity.run();
            }
            refresh();
        }
    }

    @Override
    protected void openAddDialog() {
        if (currentProject == null) {
            new CWarningDialog(
                "Please select a project first before adding users.").open();
            return;
        }

        if (projectService == null) {
            new CWarningDialog(
                "Project service is not available. Please try again later.").open();
            return;
        }
        
        // Create a dialog for adding users to project (reverse mode)
        final CProjectUserSettingsDialog dialog =
            new CProjectUserSettingsDialog(projectService, null, // null for new settings
                currentProject, this::onSettingsSaved);
        dialog.open();
    }

    @Override
    protected void openEditDialog() {
        final CUserProjectSettings selected = grid.asSingleSelect().getValue();

        if (selected == null) {
            new CWarningDialog("Please select a user to edit.").open();
            return;
        }

        if (currentProject == null) {
            new CWarningDialog(
                "Current project information is not available. Please refresh the page.")
                .open();
            return;
        }
        
        final CProjectUserSettingsDialog dialog = new CProjectUserSettingsDialog(
            projectService, selected, currentProject, this::onSettingsSaved);
        dialog.open();
    }

    public void setCurrentProject(final CProject project) { 
        this.currentProject = project; 
    }

    public void setProjectUsersAccessors(
        final java.util.function.Supplier<List<CUserProjectSettings>> getProjectUsers,
        final java.util.function.Consumer<List<CUserProjectSettings>> setProjectUsers,
        final Runnable saveEntity) {
        LOGGER.debug("Setting project users accessors");
        setSettingsAccessors(getProjectUsers, setProjectUsers, saveEntity);
    }

    @Override
    protected void setupGrid() {
        // Add columns for user name with avatar, roles, and permissions
        grid.addColumn(CUserProjectSettings::getId).setHeader("ID").setAutoWidth(true);
        grid.addComponentColumn(this::getUserWithAvatar).setHeader("User")
            .setAutoWidth(true).setSortable(false);
        grid.addColumn(this::getRoleAsString).setHeader("Role").setAutoWidth(true);
        grid.addColumn(this::getPermissionAsString).setHeader("Permission")
            .setAutoWidth(true);
        grid.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE);
        getBaseLayout().add(grid);
    }
}