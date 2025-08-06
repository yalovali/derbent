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
import tech.derbent.users.service.CUserProjectSettingsService;

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
	private final CUserProjectSettingsService userProjectSettingsService;

	public CPanelProjectUsers(final CProject currentEntity,
		final CEnhancedBinder<CProject> beanValidationBinder,
		final CProjectService entityService, final CProjectService projectService,
		final CUserService userService, final CUserProjectSettingsService userProjectSettingsService) {
		super("Project Users", currentEntity, beanValidationBinder, CProject.class,
			entityService, projectService);
		this.userService = userService;
		this.userProjectSettingsService = userProjectSettingsService;
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
		
		try {
			// Use the service layer to properly persist the relationship
			final CUserProjectSettings savedSettings;
			
			if (settings.getId() == null) {
				// New relationship - create it
				savedSettings = userProjectSettingsService.save(settings);
				LOGGER.debug("Created new user project settings with ID: {}", savedSettings.getId());
			} else {
				// Existing relationship - update it
				savedSettings = userProjectSettingsService.save(settings);
				LOGGER.debug("Updated user project settings with ID: {}", savedSettings.getId());
			}
			
			// Update the local collection if accessors are available
			if (getSettings != null && setSettings != null) {
				final List<CUserProjectSettings> settingsList = getSettings.get();
				boolean found = false;
				
				// Find and update existing entry or add new one
				for (int i = 0; i < settingsList.size(); i++) {
					final CUserProjectSettings existing = settingsList.get(i);
					
					if ((existing.getId() != null) && existing.getId().equals(savedSettings.getId())) {
						settingsList.set(i, savedSettings);
						found = true;
						break;
					}
				}
				
				if (!found) {
					settingsList.add(savedSettings);
				}
				
				setSettings.accept(settingsList);
			}
			
			// Save the parent entity if needed
			if (saveEntity != null) {
				saveEntity.run();
			}
			
			refresh();
			
		} catch (Exception e) {
			LOGGER.error("Error saving user project settings", e);
			throw new RuntimeException("Failed to save user project settings: " + e.getMessage(), e);
		}
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
			userService, null, currentProject, this::onSettingsSaved);
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
			userService, selected, currentProject, this::onSettingsSaved);
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