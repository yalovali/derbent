package tech.derbent.projects.view;

import java.util.List;
import java.util.function.Consumer;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.views.dialogs.CDBRelationDialog;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;

/** Dialog for adding users to a project (reverse direction). This dialog manages the relationship between projects and users by creating and editing
 * CUserProjectSettings entities. It allows project managers to: - Add new users to a project with specific roles and permissions - Edit existing user
 * assignments for the project Inherits common relationship management logic from CDBRelationDialog. */
public class CProjectUserSettingsDialog extends CDBRelationDialog<CUserProjectSettings, CProject, CUser> {

	private static final long serialVersionUID = 1L;

	public CProjectUserSettingsDialog(IContentOwner parentContent, final CProjectService masterService, final CUserService detailService,
			final CUserProjectSettingsService relationService, final CUserProjectSettings settings, final CProject project,
			final Consumer<CUserProjectSettings> onSave) throws Exception {
		super(parentContent, settings != null ? settings : new CUserProjectSettings(), project, masterService, detailService, relationService, onSave,
				settings == null);
		getEntity().setProject(project);
		setupDialog();
		populateForm();
	}

	@Override
	public String getDialogTitleString() { return isNew ? "Add User to Project" : "Edit User Assignment"; }

	@Override
	protected String getFormTitleString() { return isNew ? "Add User to Project" : "Edit User Project Assignment"; }

	@Override
	protected List<String> getFormFields() {
		// Project-centric: select user, role, permission
		return List.of("user", "role", "permission");
	}
}
