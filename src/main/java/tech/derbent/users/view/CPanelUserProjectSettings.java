package tech.derbent.users.view;

import java.util.List;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.views.CPanelUserProjectBase;
import tech.derbent.base.ui.dialogs.CWarningDialog;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;

/**
 * Simplified panel for managing a user's project assignments.
 * 
 * This panel displays all projects assigned to a specific user and allows:
 * - Adding new project assignments
 * - Editing existing project roles/permissions  
 * - Removing project assignments
 * 
 * The panel automatically updates when the current user changes and maintains
 * data consistency through proper accessor patterns.
 */
public class CPanelUserProjectSettings extends CPanelUserProjectBase<CUser> {

	private static final long serialVersionUID = 1L;

	private CUser currentUser;
	private final CUserTypeService userTypeService;
	private final CCompanyService companyService;

	public CPanelUserProjectSettings(final CUser currentEntity,
		final CEnhancedBinder<CUser> beanValidationBinder,
		final CUserService entityService, final CUserTypeService userTypeService,
		final CCompanyService companyService, final CProjectService projectService) {
		super("Project Settings", currentEntity, beanValidationBinder, CUser.class,
			entityService, projectService);
		this.userTypeService = userTypeService;
		this.companyService = companyService;
	}

	@Override
	protected String
		createDeleteConfirmationMessage(final CUserProjectSettings selected) {
		final String projectName = getProjectName(selected);
		return String.format(
			"Are you sure you want to delete the project setting for '%s'? This action cannot be undone.",
			projectName);
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

	/**
	 * Validates preconditions and opens dialog for adding new project assignment.
	 */
	@Override
	protected void openAddDialog() {
		if (!validateUserAndService()) {
			return;
		}
		
		final CUserProjectSettingsDialog dialog = new CUserProjectSettingsDialog(
			projectService, null, currentUser, this::onSettingsSaved);
		dialog.open();
	}

	/**
	 * Validates preconditions and opens dialog for editing selected project assignment.
	 */
	@Override
	protected void openEditDialog() {
		final CUserProjectSettings selected = grid.asSingleSelect().getValue();
		
		if (selected == null) {
			new CWarningDialog("Please select a project setting to edit.").open();
			return;
		}
		
		if (currentUser == null) {
			new CWarningDialog("Current user information is not available. Please refresh the page.").open();
			return;
		}
		
		final CUserProjectSettingsDialog dialog = new CUserProjectSettingsDialog(
			projectService, selected, currentUser, this::onSettingsSaved);
		dialog.open();
	}

	/**
	 * Validates that user and project service are available for operations.
	 * 
	 * @return true if both user and service are available, false otherwise
	 */
	private boolean validateUserAndService() {
		if (currentUser == null) {
			new CWarningDialog("Please select a user first before adding project settings.").open();
			return false;
		}

		if (projectService == null) {
			new CWarningDialog("Project service is not available. Please try again later.").open();
			return false;
		}
		
		return true;
	}

	public void setCurrentUser(final CUser user) { this.currentUser = user; }

	public void setProjectSettingsAccessors(
		final java.util.function.Supplier<List<CUserProjectSettings>> getProjectSettings,
		final java.util.function.Consumer<List<CUserProjectSettings>> setProjectSettings,
		final Runnable saveEntity) {
		LOGGER.debug("Setting project settings accessors");
		setSettingsAccessors(getProjectSettings, setProjectSettings, saveEntity);
	}

	@Override
	protected void setupGrid() {
		// Add columns for project name, roles, and permissions
		grid.addColumn(CUserProjectSettings::getId).setHeader("ID").setAutoWidth(true);
		grid.addComponentColumn(this::getUserWithAvatar).setHeader("User")
			.setAutoWidth(true);
		grid.addColumn(this::getProjectName).setHeader("Project Name").setAutoWidth(true)
			.setSortable(true);
		grid.addColumn(this::getRoleAsString).setHeader("Role").setAutoWidth(true);
		grid.addColumn(this::getPermissionAsString).setHeader("Permission")
			.setAutoWidth(true);
		grid.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE);
		getBaseLayout().add(grid);
	}
}