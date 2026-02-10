package tech.derbent.api.users.view;

import java.util.List;
import java.util.function.Consumer;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.ui.dialogs.CDialogUserProjectRelation;
import tech.derbent.api.projects.service.CProjectService;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.domain.CUserProjectSettings;
import tech.derbent.api.users.service.CUserProjectSettingsService;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.plm.project.domain.CProject_Derbent;

/** Dialog for managing project assignments for a user (User->Project direction). This dialog allows selecting projects to assign to a specific
 * user. */
public class CDialogUserProjectSettings extends CDialogUserProjectRelation<CUser, CProject_Derbent> {

	private static final long serialVersionUID = 1L;

	public CDialogUserProjectSettings(IContentOwner parentContent, final CUserService masterService,
			final CProjectService<CProject_Derbent> detailService, final CUserProjectSettingsService userProjectSettingsService,
			final CUserProjectSettings settings, final CUser user, final Consumer<CUserProjectSettings> onSave) throws Exception {
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
}
