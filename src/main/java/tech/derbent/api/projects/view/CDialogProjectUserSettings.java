package tech.derbent.api.projects.view;

import java.util.List;
import java.util.function.Consumer;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.ui.dialogs.CDialogUserProjectRelation;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.projects.service.CProjectService;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.domain.CUserProjectSettings;
import tech.derbent.api.users.service.CUserProjectSettingsService;
import tech.derbent.api.users.service.CUserService;

/** Dialog for managing user assignments for a project (Project->User direction). This dialog allows selecting users to assign to a specific project.
 * Inherits common relationship management logic from CUserProjectRelationDialog. */
public class CDialogProjectUserSettings<ProjectClass extends CProject<ProjectClass>> extends CDialogUserProjectRelation<ProjectClass, CUser> {

	private static final long serialVersionUID = 1L;

	public CDialogProjectUserSettings(IContentOwner parentContent, final CProjectService<ProjectClass> masterService,
			final CUserService detailService, final CUserProjectSettingsService relationService, final CUserProjectSettings settings,
			final ProjectClass project, final Consumer<CUserProjectSettings> onSave) throws Exception {
		super(parentContent, masterService, detailService, relationService, settings, project, onSave);
	}

	@Override
	protected List<String> getFormFields() { return List.of("user", "role", "permission"); }

	@Override
	protected String getNewDialogTitle() { return "Add User to Project"; }

	@Override
	protected String getEditDialogTitle() { return "Edit User Assignment"; }

	@Override
	protected String getNewFormTitle() { return "Add User to Project"; }

	@Override
	protected String getEditFormTitle() { return "Edit User Project Assignment"; }
}
