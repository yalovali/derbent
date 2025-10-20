package tech.derbent.api.views.components;

import java.util.List;
import org.springframework.context.ApplicationContext;
import tech.derbent.api.ui.dialogs.CWarningDialog;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.projects.service.CProjectService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.domain.CUserProjectSettings;
import tech.derbent.base.users.service.CUserService;
import tech.derbent.base.users.view.CUserProjectSettingsDialog;

/** Component for managing a user's project assignments (User->Project direction). This component displays all projects assigned to a specific user
 * and allows: - Adding new project assignments - Editing existing project roles/permissions - Removing project assignments The component
 * automatically updates when the current user changes. */
public class CComponentUserProjectSettings extends CComponentUserProjectRelationBase<CUser, CUserProjectSettings> {

	private static final long serialVersionUID = 1L;
	private final CProjectService projectService;

	public CComponentUserProjectSettings(final CUserService entityService, ISessionService sessionService, ApplicationContext applicationContext)
			throws Exception {
		super("Project Settings", CUser.class, entityService, sessionService, applicationContext);
		projectService = applicationContext.getBean(CProjectService.class);
		initComponent();
	}

	public List<CProject> getAvailableProjects() {
		// called from annotation
		return projectService.getAvailableProjectsForUser(getCurrentEntity().getId());
	}

	@Override
	protected void openAddDialog() throws Exception {
		try {
			new CUserProjectSettingsDialog(this, (CUserService) entityService, projectService, userProjectSettingsService, null, getCurrentEntity(),
					this::onSettingsSaved).open();
		} catch (Exception e) {
			new CWarningDialog("Failed to open add dialog: " + e.getMessage()).open();
			throw e;
		}
	}

	@Override
	protected void openEditDialog() throws Exception {
		try {
			new CUserProjectSettingsDialog(this, (CUserService) entityService, projectService, userProjectSettingsService, getSelectedSetting(),
					getCurrentEntity(), this::onSettingsSaved).open();
		} catch (Exception e) {
			new CWarningDialog("Failed to open edit dialog: " + e.getMessage()).open();
			throw e;
		}
	}

	@Override
	protected void setupDataAccessors() {
		createStandardDataAccessors(() -> userProjectSettingsService.findByUser(getCurrentEntity()), () -> entityService.save(getCurrentEntity()));
	}
}
