package tech.derbent.api.ui.dialogs;

import java.util.function.Consumer;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.app.projects.service.CProjectService;
import tech.derbent.base.users.domain.CUserProjectSettings;
import tech.derbent.base.users.service.CUserProjectSettingsService;
import tech.derbent.base.users.service.CUserService;

/** Generic base class for User-Project relationship dialogs. This class provides common functionality for both User->Project and Project->User
 * relationship dialogs, reducing code duplication while maintaining flexibility for specific implementations.
 * @param <MasterEntity> The main entity type (CUser for user-centric, CProject for project-centric)
 * @param <DetailEntity> The detail entity type (CProject for user-centric, CUser for project-centric) */
public abstract class CUserProjectRelationDialog<MasterEntity extends CEntityDB<MasterEntity>, DetailEntity extends CEntityDB<DetailEntity>>
		extends CDBRelationDialog<CUserProjectSettings, MasterEntity, DetailEntity> {

	private static final long serialVersionUID = 1L;
	protected final CProjectService projectService;
	protected final CUserProjectSettingsService userProjectSettingsService;
	protected final CUserService userService;

	@SuppressWarnings ("unchecked")
	public CUserProjectRelationDialog(IContentOwner parentContent, Object masterService, Object detailService,
			CUserProjectSettingsService userProjectSettingsService, CUserProjectSettings settings, MasterEntity masterEntity,
			Consumer<CUserProjectSettings> onSave) throws Exception {
		super(parentContent, settings != null ? settings : new CUserProjectSettings(), masterEntity,
				(tech.derbent.api.entity.service.CAbstractService<MasterEntity>) masterService,
				(tech.derbent.api.entity.service.CAbstractService<DetailEntity>) detailService, userProjectSettingsService, onSave, settings == null);
		// Store services for easy access
		userService = masterService instanceof CUserService ? (CUserService) masterService : (CUserService) detailService;
		projectService = masterService instanceof CProjectService ? (CProjectService) masterService : (CProjectService) detailService;
		this.userProjectSettingsService = userProjectSettingsService;
		// Set the appropriate entity reference using reflection-based method from parent
		setupEntityRelation(masterEntity);
		// Apply colorful styling to make the dialog more visually appealing
		setupDialog();
		populateForm();
	}

	/** Returns the dialog title based on whether this is a new or edit operation. */
	@Override
	public String getDialogTitleString() { return isNew ? getNewDialogTitle() : getEditDialogTitle(); }

	@Override
	protected abstract String getEditDialogTitle();
	@Override
	protected abstract String getEditFormTitle();

	/** Returns the form title based on whether this is a new or edit operation. */
	@Override
	protected String getFormTitleString() { return isNew ? getNewFormTitle() : getEditFormTitle(); }

	/** Abstract methods for subclasses to provide specific titles */
	@Override
	protected abstract String getNewDialogTitle();
	@Override
	protected abstract String getNewFormTitle();
}
