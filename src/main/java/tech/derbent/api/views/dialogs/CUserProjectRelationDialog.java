package tech.derbent.api.views.dialogs;

import java.util.List;
import java.util.function.Consumer;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;

/** Generic base class for User-Project relationship dialogs. This class provides common functionality for both User->Project and Project->User
 * relationship dialogs, reducing code duplication while maintaining flexibility for specific implementations.
 * @param <MasterEntity> The main entity type (CUser for user-centric, CProject for project-centric)
 * @param <DetailEntity> The detail entity type (CProject for user-centric, CUser for project-centric) */
public abstract class CUserProjectRelationDialog<MasterEntity extends CEntityDB<MasterEntity>, DetailEntity extends CEntityDB<DetailEntity>>
		extends CDBRelationDialog<CUserProjectSettings, MasterEntity, DetailEntity> {

	private static final long serialVersionUID = 1L;
	protected final CUserService userService;
	protected final CProjectService projectService;
	protected final CUserProjectSettingsService userProjectSettingsService;

	/** Constructor for user-project relationship dialogs.
	 * @param parentContent              Parent content owner
	 * @param masterService              Service for the master entity
	 * @param detailService              Service for the detail entity
	 * @param userProjectSettingsService Service for relationship management
	 * @param settings                   Existing settings or null for new
	 * @param masterEntity               The master entity instance
	 * @param onSave                     Callback when save is successful
	 * @throws Exception if initialization fails */
	@SuppressWarnings ("unchecked")
	public CUserProjectRelationDialog(IContentOwner parentContent, Object masterService, Object detailService,
			CUserProjectSettingsService userProjectSettingsService, CUserProjectSettings settings, MasterEntity masterEntity,
			Consumer<CUserProjectSettings> onSave) throws Exception {
		super(parentContent, settings != null ? settings : new CUserProjectSettings(), masterEntity,
				(tech.derbent.api.services.CAbstractService<MasterEntity>) masterService,
				(tech.derbent.api.services.CAbstractService<DetailEntity>) detailService, userProjectSettingsService, onSave, settings == null);
		// Store services for easy access
		this.userService = masterService instanceof CUserService ? (CUserService) masterService : (CUserService) detailService;
		this.projectService = masterService instanceof CProjectService ? (CProjectService) masterService : (CProjectService) detailService;
		this.userProjectSettingsService = userProjectSettingsService;
		// Set the appropriate entity reference
		setupEntityRelation(masterEntity);
		setupDialog();
		populateForm();
	}

	/** Sets up the entity relation based on the master entity type. Subclasses can override this for custom behavior. */
	protected abstract void setupEntityRelation(MasterEntity masterEntity);

	/** Returns the form fields for this dialog. Default implementation includes the basic fields, subclasses can override. */
	@Override
	protected List<String> getFormFields() { return getDefaultFormFields(); }

	/** Returns the default form fields based on the direction of the relationship. User-centric dialogs show project selection, Project-centric
	 * dialogs show user selection. */
	protected abstract List<String> getDefaultFormFields();

	/** Returns the dialog title based on whether this is a new or edit operation. */
	@Override
	public String getDialogTitleString() { return isNew ? getNewDialogTitle() : getEditDialogTitle(); }

	/** Returns the form title based on whether this is a new or edit operation. */
	@Override
	protected String getFormTitleString() { return isNew ? getNewFormTitle() : getEditFormTitle(); }

	/** Abstract methods for subclasses to provide specific titles */
	protected abstract String getNewDialogTitle();
	protected abstract String getEditDialogTitle();
	protected abstract String getNewFormTitle();
	protected abstract String getEditFormTitle();
}
