package tech.derbent.api.views.components;

import java.util.List;
import org.springframework.context.ApplicationContext;
import tech.derbent.api.ui.dialogs.CWarningDialog;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.projects.view.CProjectUserSettingsDialog;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserService;

/** Component for managing users within a project (Project->User direction). This component displays all users assigned to a specific project and
 * allows: - Adding new user assignments - Editing existing user roles/permissions - Removing user assignments The component automatically updates
 * when the current project changes. */
public class CComponentProjectUserSettings extends CComponentUserProjectRelationBase<CProject, CUserProjectSettings> {

	private static final long serialVersionUID = 1L;
	private final CUserService userService;

	public CComponentProjectUserSettings(final CProjectService entityService, ApplicationContext applicationContext) throws Exception {
		super("User Settings", CProject.class, entityService, applicationContext);
		userService = applicationContext.getBean(CUserService.class);
		initComponent();
	}

	public List<CUser> getAvailableUsers(CProject project) {
		// called from annotation
		return userService.getAvailableUsersForProject(project.getCompanyId(), getCurrentEntity().getId());
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
