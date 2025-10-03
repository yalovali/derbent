package tech.derbent.companies.view;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationContext;
import tech.derbent.api.roles.domain.CUserCompanyRole;
import tech.derbent.api.roles.service.CUserCompanyRoleService;
import tech.derbent.api.ui.dialogs.CWarningDialog;
import tech.derbent.api.views.components.CComponentUserCompanyBase;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserCompanySetting;
import tech.derbent.users.service.CUserService;

public class CComponentCompanyUserSettings extends CComponentUserCompanyBase<CCompany, CUserCompanySetting> {

	private static final long serialVersionUID = 1L;
	private final CUserCompanyRoleService userCompanyRoleService;
	private final CUserService userService;

	public CComponentCompanyUserSettings(final CCompanyService entityService, ApplicationContext applicationContext) throws Exception {
		super("User Settings", CCompany.class, entityService, applicationContext);
		userService = applicationContext.getBean(CUserService.class);
		userCompanyRoleService = applicationContext.getBean(CUserCompanyRoleService.class);
		initComponent();
	}

	public List<CUser> getAvailableUsersForCompany() {
		// called from annotation
		// Get all users that are not yet members of this company
		return userService.getAvailableUsersForCompany(getCurrentEntity().getId());
	}

	/** Get available company roles for the current company. Only returns member roles (non-guest roles).
	 * @return list of available company roles */
	public List<CUserCompanyRole> getAvailableCompanyRolesForUser() {
		CCompany company = getCurrentEntity();
		if (company == null) {
			return List.of();
		}
		// Get all roles for the company and filter out guest roles
		return userCompanyRoleService.findAll().stream()
				.filter(role -> role.getCompany() != null && role.getCompany().getId().equals(company.getId())).filter(role -> !role.isGuest()) // Exclude
																																				// guest
																																				// roles
				.collect(Collectors.toList());
	}

	@Override
	protected void openAddDialog() throws Exception {
		try {
			new CCompanyUserSettingsDialog(this, (CCompanyService) entityService, userService, userCompanySettingsService, null, getCurrentEntity(),
					this::onSettingsSaved).open();
		} catch (Exception e) {
			new CWarningDialog("Failed to open add dialog: " + e.getMessage()).open();
			throw e;
		}
	}

	@Override
	protected void openEditDialog() throws Exception {
		try {
			new CCompanyUserSettingsDialog(this, (CCompanyService) entityService, userService, userCompanySettingsService, getSelectedSetting(),
					getCurrentEntity(), this::onSettingsSaved).open();
		} catch (Exception e) {
			new CWarningDialog("Failed to open edit dialog: " + e.getMessage()).open();
			throw e;
		}
	}

	@Override
	protected void setupDataAccessors() {
		createStandardDataAccessors(() -> userCompanySettingsService.findByCompany(getCurrentEntity()), () -> entityService.save(getCurrentEntity()));
	}
}
