package tech.derbent.projects.view;

import java.util.List;
import java.util.function.Consumer;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.roles.domain.CRole;
import tech.derbent.api.roles.domain.CUserProjectRole;
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
	private CUserProjectSettingsService userProjectSettingsService;

	/** Constructor for the dialog with service support.
	 * @param masterService              Service for project operations
	 * @param detailService              Service for user operations
	 * @param userProjectSettingsService Service for user-project relationship operations
	 * @param settings                   Existing settings to edit, or null for new assignment
	 * @param project                    The project to manage user assignments for
	 * @param onSave                     Callback executed when settings are successfully saved
	 * @throws Exception */
	public CProjectUserSettingsDialog(IContentOwner parentContent, final CProjectService masterService, final CUserService detailService,
			final CUserProjectSettingsService userProjectSettingsService, final CUserProjectSettings settings, final CProject project,
			final Consumer<CUserProjectSettings> onSave) throws Exception {
		super(parentContent, settings != null ? settings : new CUserProjectSettings(), project, masterService, detailService, onSave,
				settings == null);
		this.userProjectSettingsService = userProjectSettingsService;
		// Ensure the project is set in the relationship entity immediately for new entities
		if (settings == null && project != null) {
			getEntity().setProject(project);
		}
		setupDialog();
		populateForm();
	}

	@Override
	public String getDialogTitleString() { return isNew ? "Add User to Project" : "Edit User Assignment"; }

	@Override
	protected Icon getFormIcon() { return VaadinIcon.USERS.create(); }

	@Override
	protected String getFormTitleString() { return isNew ? "Add User to Project" : "Edit User Project Assignment"; }

	@Override
	protected List<String> getFormFields() {
		// Project-centric: select user, role, permission
		return List.of("user", "role", "permission");
	}

	@Override
	protected String getSuccessCreateMessage() { return "User added to project successfully"; }

	@Override
	protected String getSuccessUpdateMessage() { return "User project assignment updated successfully"; }

	@Override
	protected void performSave() {
		// If onSave callback is provided, use it (callback pattern)
		if (onSave != null) {
			onSave.accept(getEntity());
		} else if (userProjectSettingsService != null) {
			// No callback provided, save directly using service
			CUserProjectSettings savedEntity = userProjectSettingsService.save(getEntity());
			LOGGER.info("Entity saved successfully using service: {}", savedEntity.getId());
		} else {
			LOGGER.warn("No onSave callback or service available for saving");
		}
	}

	@Override
	protected void setRelatedEntityInRelationship(final CUserProjectSettings relationship, final CUser user) {
		relationship.setUser(user);
	}

	@Override
	protected void setRoleInRelationship(final CUserProjectSettings relationship, final CRole<?> role) {
		relationship.setRole((CUserProjectRole) role);
	}

	/** Gets the current project's ID for use in data provider parameter methods.
	 * @return the project ID, or null if no project is set */
	public Long getProjectId() { return mainEntity != null ? mainEntity.getId() : null; }
}
