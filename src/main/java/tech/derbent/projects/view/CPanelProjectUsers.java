package tech.derbent.projects.view;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.views.CPanelUserProjectBase;
import tech.derbent.base.ui.dialogs.CWarningDialog;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserService;

/**
 * Simplified panel for managing users within a project. This is the reverse direction of
 * CPanelUserProjectSettings - instead of showing which projects a user has access to,
 * this shows which users have access to a specific project and allows management of their
 * roles/permissions. Key features: - Add new users to the project - Edit existing user
 * roles/permissions - Remove users from the project - Automatic data synchronization with
 * parent entity
 */
public class CPanelProjectUsers extends CPanelUserProjectBase<CProject> {

	private static final long serialVersionUID = 1L;

	private CProject currentProject;

	private final CUserService userService;

	public CPanelProjectUsers(final CProject currentEntity,
		final CEnhancedBinder<CProject> beanValidationBinder,
		final CProjectService entityService, final CProjectService projectService,
		final CUserService userService) {
		super("Project Users", currentEntity, beanValidationBinder, CProject.class,
			entityService, projectService);
		this.userService = userService;
		openPanel();
	}

	@Override
	protected String
		createDeleteConfirmationMessage(final CUserProjectSettings selected) {
		final String userName = selected.getUser() != null ? selected.getUser().getName()
			+ " " + (selected.getUser().getLastname() != null
				? selected.getUser().getLastname() : "")
			: "Unknown User";
		return String.format(
			"Are you sure you want to remove user '%s' from this project? This action cannot be undone.",
			userName);
	}

	protected void onSave(final CProject item) {
		LOGGER.debug("Saving user project settings: {}", item);
	}

	@Override
	protected void onSettingsSaved(final CUserProjectSettings settings) {
		LOGGER.debug("Saving user project settings: {}", settings);
		// assert that we have accessors for getting and setting settings
		assert getSettings != null;
		assert setSettings != null;
		final List<CUserProjectSettings> settingsList = getSettings.get();
		// Check if this is an update or a new addition
		boolean found = false;

		for (int i = 0; i < settingsList.size(); i++) {
			final CUserProjectSettings existing = settingsList.get(i);

			if ((existing.getId() != null) && existing.getId().equals(settings.getId())) {
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

	/**
	 * Validates preconditions and opens dialog for adding new user to project.
	 */
	@Override
	protected void openAddDialog() {

		if (!validateProjectSelection() || !validateServiceAvailability("Project")) {
			return;
		}
		final CProjectUserSettingsDialog dialog = new CProjectUserSettingsDialog(
			projectService, userService, null, currentProject, this::onSettingsSaved);
		dialog.open();
	}

	/**
	 * Validates preconditions and opens dialog for editing selected user's project
	 * access.
	 */
	@Override
	protected void openEditDialog() {

		if (!validateGridSelection("edit") || !validateProjectSelection()) {
			return;
		}
		final CUserProjectSettings selected = grid.asSingleSelect().getValue();
		final CProjectUserSettingsDialog dialog = new CProjectUserSettingsDialog(
			projectService, userService, selected, currentProject, this::onSettingsSaved);
		dialog.open();
	}

	public void setCurrentProject(final CProject project) {
		this.currentProject = project;
	}

	public void setProjectUsersAccessors(
		final Supplier<List<CUserProjectSettings>> getProjectUsers,
		final Consumer<List<CUserProjectSettings>> setProjectUsers,
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

	/**
	 * Validates that a project is currently selected.
	 * @return true if currentProject is not null, false otherwise
	 */
	private boolean validateProjectSelection() {

		if (currentProject == null) {
			new CWarningDialog("Please select a project first before managing users.")
				.open();
			return false;
		}
		return true;
	}
}