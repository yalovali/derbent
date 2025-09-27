package tech.derbent.users.view;

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

public class CUserProjectSettingsDialog extends CDBRelationDialog<CUserProjectSettings, CUser, CProject> {

	private static final long serialVersionUID = 1L;

	public CUserProjectSettingsDialog(IContentOwner parentContent, final CUserService masterService, final CProjectService detailService,
			final CUserProjectSettingsService userProjectSettingsService, final CUserProjectSettings settings, final CUser user,
			final Consumer<CUserProjectSettings> onSave) throws Exception {
		super(parentContent, settings != null ? settings : new CUserProjectSettings(), user, masterService, detailService, userProjectSettingsService,
				onSave, settings == null);
		getEntity().setUser(user);
		setupDialog();
		populateForm();
	}

	@Override
	protected List<String> getFormFields() { return List.of("project", "role", "permission"); }

	@Override
	public String getDialogTitleString() { return isNew ? "Add Project Assignment" : "Edit Project Assignment"; }

	@Override
	protected String getFormTitleString() { return isNew ? "Assign User to Project" : "Edit Project Assignment"; }
}
