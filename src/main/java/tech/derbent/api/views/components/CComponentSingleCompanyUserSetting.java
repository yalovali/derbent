package tech.derbent.api.views.components;

import org.springframework.context.ApplicationContext;
import tech.derbent.api.ui.dialogs.CWarningDialog;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserCompanySetting;
import tech.derbent.users.service.CUserService;

/** Component for displaying and editing a user's single company setting. This component provides a nice visual layout with icons and colors for the
 * CUserCompanySettings field, allowing users to view and edit their company membership and role through an attractive interface. */
public class CComponentSingleCompanyUserSetting extends CComponentUserCompanyBase<CUser, CUserCompanySetting> {

	private static final long serialVersionUID = 1L;
	private final CCompanyService companyService;

	public CComponentSingleCompanyUserSetting(CUserService entityService, ApplicationContext applicationContext) throws Exception {
		super("Company Setting", CUser.class, entityService, applicationContext);
		companyService = applicationContext.getBean(CCompanyService.class);
		initComponent();
	}

	@Override
	protected void openAddDialog() throws Exception {
		try {
			new CUserCompanySettingsDialog(this, (CUserService) entityService, companyService, userCompanySettingsService, null, getCurrentEntity(),
					this::onSettingsSaved).open();
		} catch (Exception e) {
			new CWarningDialog("Failed to open add dialog: " + e.getMessage()).open();
			throw e;
		}
	}

	@Override
	protected void openEditDialog() throws Exception {
		try {
			new CUserCompanySettingsDialog(this, (CUserService) entityService, companyService, userCompanySettingsService, getSelectedSetting(),
					getCurrentEntity(), this::onSettingsSaved).open();
		} catch (Exception e) {
			new CWarningDialog("Failed to open edit dialog: " + e.getMessage()).open();
			throw e;
		}
	}

	@Override
	protected void setupDataAccessors() {
		createStandardDataAccessors(() -> userCompanySettingsService.findByUser(getCurrentEntity()), () -> entityService.save(getCurrentEntity()));
	}
}
