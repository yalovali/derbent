package tech.derbent.users.view;

import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/** Dialog for managing user-project assignments from the user perspective. This dialog handles both creating new project assignments and editing
 * existing ones. It allows users to be assigned to projects with specific roles and permissions. Inherits common relationship management logic from
 * CDBRelationDialog. */
public class CUserProjectSettingsDialog extends CDBRelationDialog<CUserProjectSettings, CUser, CProject> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserProjectSettingsDialog.class);
	private static final long serialVersionUID = 1L;
	private CUserProjectSettingsService userProjectSettingsService;

	public CUserProjectSettingsDialog(IContentOwner parentContent, final CUserService masterService, final CProjectService detailService,
			final CUserProjectSettingsService userProjectSettingsService, final CUserProjectSettings settings, final CUser user,
			final Consumer<CUserProjectSettings> onSave) throws Exception {
		super(parentContent, settings != null ? settings : new CUserProjectSettings(), user, masterService, detailService, onSave, settings == null);
		this.userProjectSettingsService = userProjectSettingsService;
		
		// Ensure the user is set in the relationship entity immediately for new entities
		if (settings == null && user != null) {
			getEntity().setUser(user);
		}
		
		setupDialog();
		populateForm();
	}

	@Override
	protected List<String> getFormFields() {
		// User-centric: select project, role, permission
		return List.of("project", "role", "permission");
	}

	@Override
	public String getDialogTitleString() { return isNew ? "Add Project Assignment" : "Edit Project Assignment"; }

	@Override
	protected Icon getFormIcon() { return VaadinIcon.USER_CHECK.create(); }

	@Override
	protected String getFormTitleString() { return isNew ? "Assign User to Project" : "Edit Project Assignment"; }

	@Override
	protected String getSuccessCreateMessage() { return "Project assignment created successfully"; }

	@Override
	protected String getSuccessUpdateMessage() { return "Project assignment updated successfully"; }

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
	protected void setRelatedEntityInRelationship(final CUserProjectSettings relationship, final CProject project) {
		relationship.setProject(project);
	}

	@Override
	protected void setRoleInRelationship(CUserProjectSettings relationship, CRole<?> role) {
		relationship.setRole((CUserProjectRole) role);
	}

	/** Gets the current user's ID for use in data provider parameter methods.
	 * @return the user ID, or null if no user is set */
	public Long getUserId() { return mainEntity != null ? mainEntity.getId() : null; }
}
