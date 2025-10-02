package tech.derbent.users.view;

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

/** Dialog for managing project assignments for a user (User->Project direction). This dialog allows selecting projects to assign to a specific
 * user. */
public class CUserProjectSettingsDialog extends CUserProjectRelationDialog<CUser, CProject> {

	private static final long serialVersionUID = 1L;

	public CUserProjectSettingsDialog(IContentOwner parentContent, final CUserService masterService, final CProjectService detailService,
			final CUserProjectSettingsService userProjectSettingsService, final CUserProjectSettings settings, final CUser user,
			final Consumer<CUserProjectSettings> onSave) throws Exception {
		super(parentContent, masterService, detailService, userProjectSettingsService, settings, user, onSave);
	}

	@Override
	protected List<String> getFormFields() { return List.of("project", "role", "permission"); }

	@Override
	protected String getEditDialogTitle() { return "Edit Project Assignment"; }

	@Override
	protected String getEditFormTitle() { return "Edit Project Assignment"; }

	@Override
	protected String getNewDialogTitle() { return "Add Project Assignment"; }

	@Override
	protected String getNewFormTitle() { return "Assign User to Project"; }

	@Override
	protected void setupEntityRelation(CUser user) {
		getEntity().setUser(user);
	}
}
