package tech.derbent.api.ui.component.enhanced;

import java.util.List;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.projects.service.CProjectService;
import tech.derbent.app.projects.view.CDialogProjectUserSettings;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.domain.CUserProjectSettings;
import tech.derbent.base.users.service.CUserService;
import tech.derbent.api.ui.notifications.CNotificationService;

/** Component for managing users within a project (Project->User direction). This component displays all users assigned to a specific project and
 * allows: - Adding new user assignments - Editing existing user roles/permissions - Removing user assignments The component automatically updates
 * when the current project changes. */
public class CComponentProjectUserSettings extends CComponentUserProjectRelationBase<CProject, CUserProjectSettings> {

	private static final long serialVersionUID = 1L;
	private final CUserService userService;

	public CComponentProjectUserSettings(final CProjectService entityService, ISessionService sessionService) throws Exception {
		super("User Settings", CProject.class, entityService, sessionService);
		userService = CSpringContext.getBean(CUserService.class);
		initComponent();
	}

	@Override
	public CEntityDB<?> createNewEntityInstance() throws Exception {
		
		return null;
	}

	public List<CUser> getAvailableUsers() {
		final CProject project = getValue();
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
			new CDialogProjectUserSettings(this, (CProjectService) entityService, userService, userProjectSettingsService, null, getValue(),
					this::onSettingsSaved).open();
		} catch (Exception e) {
			CNotificationService.showWarning("Failed to open add dialog: " + e.getMessage());
			throw e;
		}
	}

	@Override
	protected void openEditDialog() throws Exception {
		try {
			new CDialogProjectUserSettings(this, (CProjectService) entityService, userService, userProjectSettingsService, getSelectedSetting(),
					getValue(), this::onSettingsSaved).open();
		} catch (Exception e) {
			CNotificationService.showWarning("Failed to open edit dialog: " + e.getMessage());
			throw e;
		}
	}

	@Override
	protected void setupDataAccessors() {
		createStandardDataAccessors(() -> userProjectSettingsService.findByProject(getValue()), () -> entityService.save(getValue()));
	}
}
