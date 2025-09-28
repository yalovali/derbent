package tech.derbent.api.views.dialogs;

import java.util.List;
import java.util.function.Consumer;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.utils.Check;
import tech.derbent.projects.service.CProjectService;
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
	 * @param detailService              Service for the detail entity /** Constructor for user-project relationship dialogs with enforced parameter
	 *                                   validation. Ensures all required dependencies are provided and terminates with exceptions otherwise.
	 * @param parentContent              Parent content owner - must not be null
	 * @param masterService              Service for the master entity - must not be null
	 * @param detailService              Service for the detail entity - must not be null
	 * @param userProjectSettingsService Service for relationship management - must not be null
	 * @param settings                   Existing settings or null for new
	 * @param masterEntity               The master entity instance - must not be null
	 * @param onSave                     Callback when save is successful - must not be null
	 * @throws Exception                if initialization fails
	 * @throws IllegalArgumentException if any required parameter is null or invalid */
	@SuppressWarnings ("unchecked")
	public CUserProjectRelationDialog(IContentOwner parentContent, Object masterService, Object detailService,
			CUserProjectSettingsService userProjectSettingsService, CUserProjectSettings settings, MasterEntity masterEntity,
			Consumer<CUserProjectSettings> onSave) throws Exception {
		super(parentContent, settings != null ? settings : new CUserProjectSettings(), masterEntity,
				(tech.derbent.api.services.CAbstractService<MasterEntity>) masterService,
				(tech.derbent.api.services.CAbstractService<DetailEntity>) detailService, userProjectSettingsService, onSave, settings == null);
		// Enforce strict parameter validation - terminate with exceptions on any missing parameter
		Check.notNull(parentContent, "Parent content cannot be null - relation dialog requires a parent content owner");
		Check.notNull(masterService, "Master service cannot be null - relation dialog requires a master entity service");
		Check.notNull(detailService, "Detail service cannot be null - relation dialog requires a detail entity service");
		Check.notNull(userProjectSettingsService, "User project settings service cannot be null - relation dialog requires a relationship service");
		Check.notNull(masterEntity, "Master entity cannot be null - relation dialog requires a master entity instance");
		Check.notNull(onSave, "OnSave callback cannot be null - relation dialog requires a save callback");
		// Store services for easy access
		this.userService = masterService instanceof CUserService ? (CUserService) masterService : (CUserService) detailService;
		this.projectService = masterService instanceof CProjectService ? (CProjectService) masterService : (CProjectService) detailService;
		this.userProjectSettingsService = userProjectSettingsService;
		// Set the appropriate entity reference
		setupEntityRelation(masterEntity);
		// Apply colorful styling to make the dialog more visually appealing
		enhanceDialogStyling();
		setupDialog();
		populateForm();
	}

	/** Enhances dialog styling with colors and visual improvements. Makes the dialog more colorful and visually appealing. */
	private void enhanceDialogStyling() {
		try {
			// Add colorful border and background to make dialog more appealing
			getElement().getStyle().set("border", "2px solid #1976D2");
			getElement().getStyle().set("border-radius", "12px");
			getElement().getStyle().set("box-shadow", "0 4px 20px rgba(25, 118, 210, 0.3)");
			// Set a subtle gradient background
			getElement().getStyle().set("background", "linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)");
		} catch (Exception e) {
			LOGGER.error("Failed to apply dialog styling: {}", e.getMessage(), e);
		}
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
