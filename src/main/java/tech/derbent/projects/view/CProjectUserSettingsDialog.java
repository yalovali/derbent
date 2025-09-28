package tech.derbent.projects.view;

import java.util.List;
import java.util.function.Consumer;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.views.dialogs.CUserProjectRelationDialog;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;

/** Dialog for managing user assignments for a project (Project->User direction). This dialog allows selecting users to assign to a specific project.
 * Inherits common relationship management logic from CUserProjectRelationDialog. */
public class CProjectUserSettingsDialog extends CUserProjectRelationDialog<CProject, CUser> {

	private static final long serialVersionUID = 1L;

	public CProjectUserSettingsDialog(IContentOwner parentContent, final CProjectService masterService, final CUserService detailService,
			final CUserProjectSettingsService relationService, final CUserProjectSettings settings, final CProject project,
			final Consumer<CUserProjectSettings> onSave) throws Exception {
		super(parentContent, masterService, detailService, relationService, settings, project, onSave);
	}

	@Override
	protected void setupEntityRelation(CProject project) {
		getEntity().setProject(project);
	}

	@Override
	protected List<String> getDefaultFormFields() {
		// Project-centric: select user, role, permission
		return List.of("user", "role", "permission");
	}

	@Override
	protected String getNewDialogTitle() { return "Add User to Project"; }

	@Override
	protected String getEditDialogTitle() { return "Edit User Assignment"; }

	@Override
	protected String getNewFormTitle() { return "Add User to Project"; }

	@Override
	protected String getEditFormTitle() { return "Edit User Project Assignment"; }
}
