package tech.derbent.users.view;

import java.util.List;
import java.util.function.Supplier;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.ui.dialogs.CWarningDialog;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.CComponentUserProjectBase;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;

/** Simplified component for managing a user's project assignments. This component displays all projects assigned to a specific user and allows: -
 * Adding new project assignments - Editing existing project roles/permissions - Removing project assignments The component automatically updates when
 * the current user changes and maintains data consistency through proper accessor patterns. */
public class CComponentUserProjectSettings extends CComponentUserProjectBase<CUser, CUserProjectSettings> {

	private static final long serialVersionUID = 1L;
	private CUser currentUser;
	private final CProjectService projectService;
	private final CUserProjectSettingsService userProjectSettingsService;

	public CComponentUserProjectSettings(IContentOwner parentContent, final CUser currentEntity, final CEnhancedBinder<CUser> beanValidationBinder,
			final CUserService entityService, final CUserTypeService userTypeService, final CCompanyService companyService,
			final CProjectService projectService, final CUserProjectSettingsService userProjectSettingsService) throws Exception {
		super("Project Settings", parentContent, beanValidationBinder, CUser.class, entityService, userProjectSettingsService);
		this.userProjectSettingsService = userProjectSettingsService;
		this.projectService = projectService;
		initPanel();
	}

	public List<CProject> getAvailableProjects() {
		Check.notNull(currentUser, "Current user must be selected to get available projects");
		return projectService.getAvailableProjectsForUser(getCurrentEntity().getId());
	}

	@Override
	protected void onSettingsSaved(final CUserProjectSettings settings) {
		LOGGER.debug("Saving user project settings: {}", settings);
		try {
			// Use the service layer to properly persist the relationship
			final CUserProjectSettings savedSettings;
			if (settings.getId() == null) {
				// New relationship - create it
				savedSettings = userProjectSettingsService.addUserToProject(settings.getUser(), settings.getProject(), settings.getRole(),
						settings.getPermission());
			} else {
				// Update existing relationship
				savedSettings = userProjectSettingsService.save(settings);
			}
			LOGGER.info("Successfully saved user project settings: {}", savedSettings);
			refresh();
		} catch (final Exception e) {
			LOGGER.error("Error saving user project settings: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to save user project settings: " + e.getMessage(), e);
		}
	}

	@Override
	protected void openAddDialog() throws Exception {
		LOGGER.debug("Opening add dialog for user project settings");
		final CUser user = getCurrentEntity();
		if (user == null) {
			new CWarningDialog("Please select a user first.").open();
			return;
		}
		currentUser = user;
		final CUserProjectSettingsDialog dialog = new CUserProjectSettingsDialog(this, (CUserService) entityService, projectService,
				userProjectSettingsService, null, user, this::onSettingsSaved);
		dialog.open();
	}

	@Override
	protected void openEditDialog() throws Exception {
		LOGGER.debug("Opening edit dialog for user project settings");
		final CUserProjectSettings selected = grid.asSingleSelect().getValue();
		if (selected == null) {
			new CWarningDialog("Please select a project setting to edit.").open();
			return;
		}
		final CUser user = getCurrentEntity();
		if (user == null) {
			new CWarningDialog("Current user is not available.").open();
			return;
		}
		currentUser = user;
		final CUserProjectSettingsDialog dialog = new CUserProjectSettingsDialog(this, (CUserService) entityService, projectService,
				userProjectSettingsService, selected, user, this::onSettingsSaved);
		dialog.open();
	}

	@Override
	public void initPanel() throws Exception {
		super.initPanel();
		// Set up the data accessor functions
		final Supplier<List<CUserProjectSettings>> getterFunction = () -> {
			final CUser entity = getCurrentEntity();
			if (entity == null) {
				LOGGER.debug("No current entity available, returning empty list");
				return List.of();
			}
			try {
				final List<CUserProjectSettings> settings = userProjectSettingsService.findByUser(entity);
				LOGGER.debug("Retrieved {} project settings for user: {}", settings.size(), entity.getName());
				return settings;
			} catch (final Exception e) {
				LOGGER.error("Error retrieving project settings for user: {}", e.getMessage(), e);
				return List.of();
			}
		};
		final Runnable saveEntityFunction = () -> {
			try {
				final CUser entity = getCurrentEntity();
				if (entity != null) {
					entityService.save(entity);
				}
			} catch (final Exception e) {
				LOGGER.error("Error saving entity: {}", e.getMessage(), e);
				throw new RuntimeException("Failed to save entity", e);
			}
		};
		setSettingsAccessors(getterFunction, saveEntityFunction);
		openPanel();
	}
}
