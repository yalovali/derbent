package tech.derbent.companies.view;

import java.util.List;
import java.util.function.Consumer;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.dialogs.CDBRelationDialog;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.users.domain.CUserCompanySetting;
import tech.derbent.users.service.CUserCompanySettingsService;
import tech.derbent.users.service.CUserService;

public abstract class CUserCompanyRelationDialog<MasterEntity extends CEntityDB<MasterEntity>, DetailEntity extends CEntityDB<DetailEntity>>
		extends CDBRelationDialog<CUserCompanySetting, MasterEntity, DetailEntity> {

	private static final long serialVersionUID = 1L;
	protected final CCompanyService companyService;
	protected final CUserCompanySettingsService settingsService;
	protected final CUserService userService;

	@SuppressWarnings ("unchecked")
	public CUserCompanyRelationDialog(IContentOwner parentContent, Object masterService, Object detailService,
			CUserCompanySettingsService settingService, CUserCompanySetting settings, MasterEntity masterEntity, Consumer<CUserCompanySetting> onSave)
			throws Exception {
		super(parentContent, settings != null ? settings : new CUserCompanySetting(), masterEntity,
				(tech.derbent.api.services.CAbstractService<MasterEntity>) masterService,
				(tech.derbent.api.services.CAbstractService<DetailEntity>) detailService, settingService, onSave, settings == null);
		// Enforce strict parameter validation - terminate with exceptions on any missing parameter
		Check.notNull(parentContent, "Parent content cannot be null - relation dialog requires a parent content owner");
		Check.notNull(masterService, "Master service cannot be null - relation dialog requires a master entity service");
		Check.notNull(detailService, "Detail service cannot be null - relation dialog requires a detail entity service");
		Check.notNull(settingService, "User company settings service cannot be null - relation dialog requires a relationship service");
		Check.notNull(masterEntity, "Master entity cannot be null - relation dialog requires a master entity instance");
		Check.notNull(onSave, "OnSave callback cannot be null - relation dialog requires a save callback");
		// Store services for easy access
		userService = masterService instanceof CUserService ? (CUserService) masterService : (CUserService) detailService;
		companyService = masterService instanceof CCompanyService ? (CCompanyService) masterService : (CCompanyService) detailService;
		settingsService = settingService;
		// Set the appropriate entity reference
		setupEntityRelation(masterEntity);
		// Apply colorful styling to make the dialog more visually appealing
		setupDialog();
		populateForm();
	}

	/** Returns the default form fields based on the direction of the relationship. User-centric dialogs show project selection, Project-centric
	 * dialogs show user selection. */
	protected abstract List<String> getDefaultFormFields();

	/** Returns the form fields for this dialog. Default implementation includes the basic fields, subclasses can override. */
	@Override
	protected List<String> getFormFields() { return getDefaultFormFields(); }

	/** Abstract methods for subclasses to provide specific titles */
	/** Sets up the entity relation based on the master entity type. Subclasses can override this for custom behavior. */
	protected abstract void setupEntityRelation(MasterEntity masterEntity);
}
