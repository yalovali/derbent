package tech.derbent.projects.view;

import java.util.List;
import java.util.function.Supplier;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.ui.dialogs.CWarningDialog;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.CComponentUserProjectBase;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;

/** Simplified component for managing users within a project. This component displays all users assigned to a specific project and allows: -
 * Adding new user assignments - Editing existing user roles/permissions - Removing user assignments The component automatically updates when
 * the current project changes and maintains data consistency through proper accessor patterns. */
public class CComponentProjectUserSettings extends CComponentUserProjectBase<CProject, CUserProjectSettings> {

	private static final long serialVersionUID = 1L;
	private CProject currentProject;
	private final CUserService userService;
	private final CUserProjectSettingsService userProjectSettingsService;

	public CComponentProjectUserSettings(IContentOwner parentContent, final CProject currentEntity, final CEnhancedBinder<CProject> beanValidationBinder,
			final CProjectService entityService, final CUserService userService, final CUserProjectSettingsService userProjectSettingsService) throws Exception {
		super("User Settings", parentContent, beanValidationBinder, CProject.class, entityService, userProjectSettingsService);
		Check.notNull(userService, "User service cannot be null");
		Check.notNull(userProjectSettingsService, "User project settings service cannot be null");
		this.userProjectSettingsService = userProjectSettingsService;
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
	public void initPanel() throws Exception {
		try {
			super.initPanel();
			setupDataAccessors();
			openPanel();
		} catch (Exception e) {
			LOGGER.error("Failed to initialize panel: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to initialize panel", e);
		}
	}

	private void setupDataAccessors() {
		try {
			final Supplier<List<CUserProjectSettings>> getterFunction = () -> {
				final CProject entity = getCurrentEntity();
				if (entity == null) {
					LOGGER.debug("No current entity available, returning empty list");
					return List.of();
				}
				try {
					final List<CUserProjectSettings> settings = userProjectSettingsService.findByProject(entity);
					LOGGER.debug("Retrieved {} user settings for project: {}", settings.size(), entity.getName());
					return settings;
				} catch (final Exception e) {
					LOGGER.error("Error retrieving user settings for project: {}", e.getMessage(), e);
					return List.of();
				}
			};
			final Runnable saveEntityFunction = () -> {
				try {
					final CProject entity = getCurrentEntity();
					Check.notNull(entity, "Current entity cannot be null when saving");
					entityService.save(entity);
				} catch (final Exception e) {
					LOGGER.error("Error saving entity: {}", e.getMessage(), e);
					throw new RuntimeException("Failed to save entity", e);
				}
			};
			setSettingsAccessors(getterFunction, saveEntityFunction);
		} catch (Exception e) {
			LOGGER.error("Failed to setup data accessors: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to setup data accessors", e);
		}
	}
}