package tech.derbent.api.views.components;

import java.util.List;
import org.springframework.context.ApplicationContext;
import tech.derbent.api.ui.dialogs.CWarningDialog;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.projects.service.CProjectService;
import tech.derbent.app.projects.view.CProjectUserSettingsDialog;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.domain.CUserProjectSettings;
import tech.derbent.base.users.service.CUserService;

/** Component for managing users within a project (Project->User direction). This component displays all users assigned to a specific project and
 * allows: - Adding new user assignments - Editing existing user roles/permissions - Removing user assignments The component automatically updates
 * when the current project changes. */
public class CComponentProjectUserSettings extends CComponentUserProjectRelationBase<CProject, CUserProjectSettings> {

	private static final long serialVersionUID = 1L;
	private final CUserService userService;

	public CComponentProjectUserSettings(final CProjectService entityService, ISessionService sessionService, ApplicationContext applicationContext)
			throws Exception {
		super("User Settings", CProject.class, entityService, sessionService, applicationContext);
		userService = applicationContext.getBean(CUserService.class);
		initComponent();
	}

	public List<CUser> getAvailableUsers() {
		final CProject project = getCurrentEntity();
		LOGGER.debug("Getting available users for project: {}", project != null ? project.getName() : "null");
		if (project == null) {
			LOGGER.warn("Current project is null, returning empty user list");
			return List.of();
		}
		return userService.getAvailableUsersForProject(project.getCompanyId(), project.getId());
	}

	@Override
	protected void openAddDialog() throws Exception {
		try {
			new CProjectUserSettingsDialog(this, (CProjectService) entityService, userService, userProjectSettingsService, null, getCurrentEntity(),
					this::onSettingsSaved).open();
		} catch (Exception e) {
			new CWarningDialog("Failed to open add dialog: " + e.getMessage()).open();
			throw e;
		}
	}

	@Override
	protected void openEditDialog() throws Exception {
		try {
			new CProjectUserSettingsDialog(this, (CProjectService) entityService, userService, userProjectSettingsService, getSelectedSetting(),
					getCurrentEntity(), this::onSettingsSaved).open();
		} catch (Exception e) {
			new CWarningDialog("Failed to open edit dialog: " + e.getMessage()).open();
			throw e;
		}
	}

	@Override
	protected void setupDataAccessors() {
		createStandardDataAccessors(() -> userProjectSettingsService.findByProject(getCurrentEntity()), () -> entityService.save(getCurrentEntity()));
	}
}
