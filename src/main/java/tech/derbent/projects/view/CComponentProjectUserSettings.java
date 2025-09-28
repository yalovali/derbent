package tech.derbent.projects.view;

import java.util.List;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.ui.dialogs.CWarningDialog;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.CComponentUserProjectRelationBase;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;

/** Component for managing users within a project (Project->User direction). This component displays all users assigned to a specific project and
 * allows: - Adding new user assignments - Editing existing user roles/permissions - Removing user assignments The component automatically updates
 * when the current project changes. */
public class CComponentProjectUserSettings extends CComponentUserProjectRelationBase<CProject, CUserProjectSettings> {

	private static final long serialVersionUID = 1L;
	private CProject currentProject;
	private final CUserService userService;

	public CComponentProjectUserSettings(IContentOwner parentContent, final CProject currentEntity,
			final CEnhancedBinder<CProject> beanValidationBinder, final CProjectService entityService, final CUserService userService,
			final CUserProjectSettingsService userProjectSettingsService) throws Exception {
		super("User Settings", parentContent, beanValidationBinder, CProject.class, entityService, userProjectSettingsService);
		Check.notNull(userService, "User service cannot be null");
		this.userService = userService;
		initPanel();
	}

	public List<CUser> getAvailableUsers() {
		Check.notNull(currentProject, "Current project must be selected to get available users");
		try {
			return userService.getAvailableUsersForProject(getCurrentEntity().getId());
		} catch (Exception e) {
			LOGGER.error("Failed to get available users for project {}: {}", getCurrentEntity().getId(), e.getMessage(), e);
			throw new RuntimeException("Failed to get available users", e);
		}
	}

	@Override
	protected void onSettingsSaved(final CUserProjectSettings settings) {
		Check.notNull(settings, "Settings cannot be null when saving");
		LOGGER.debug("Saving user project settings: {}", settings);
		try {
			final CUserProjectSettings savedSettings = settings.getId() == null ? userProjectSettingsService.addUserToProject(settings.getUser(),
					settings.getProject(), settings.getRole(), settings.getPermission()) : userProjectSettingsService.save(settings);
			LOGGER.info("Successfully saved user project settings: {}", savedSettings);
			populateForm();
		} catch (final Exception e) {
			LOGGER.error("Error saving user project settings: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to save user project settings: " + e.getMessage(), e);
		}
	}

	@Override
	protected void openAddDialog() throws Exception {
		try {
			LOGGER.debug("Opening add dialog for project user settings");
			final CProject project = getCurrentEntity();
			Check.notNull(project, "Please select a project first.");
			currentProject = project;
			final CProjectUserSettingsDialog dialog = new CProjectUserSettingsDialog(this, (CProjectService) entityService, userService,
					userProjectSettingsService, null, project, this::onSettingsSaved);
			dialog.open();
		} catch (Exception e) {
			LOGGER.error("Failed to open add dialog: {}", e.getMessage(), e);
			new CWarningDialog("Failed to open add dialog: " + e.getMessage()).open();
			throw e;
		}
	}

	@Override
	protected void openEditDialog() throws Exception {
		try {
			LOGGER.debug("Opening edit dialog for project user settings");
			final CUserProjectSettings selected = grid.asSingleSelect().getValue();
			Check.notNull(selected, "Please select a user setting to edit.");
			final CProject project = getCurrentEntity();
			Check.notNull(project, "Current project is not available.");
			currentProject = project;
			final CProjectUserSettingsDialog dialog = new CProjectUserSettingsDialog(this, (CProjectService) entityService, userService,
					userProjectSettingsService, selected, project, this::onSettingsSaved);
			dialog.open();
		} catch (Exception e) {
			LOGGER.error("Failed to open edit dialog: {}", e.getMessage(), e);
			new CWarningDialog("Failed to open edit dialog: " + e.getMessage()).open();
			throw e;
		}
	}

	@Override
	protected void setupDataAccessors() {
		createStandardDataAccessors(() -> userProjectSettingsService.findByProject(getCurrentEntity()), () -> entityService.save(getCurrentEntity()));
	}
}
