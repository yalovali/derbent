package tech.derbent.companies.view;

import java.util.List;
import org.springframework.context.ApplicationContext;
import tech.derbent.api.ui.dialogs.CWarningDialog;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CComponentUserCompanyBase;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserCompanySetting;
import tech.derbent.users.service.CUserService;

public class CComponentCompanyUserSettings extends CComponentUserCompanyBase<CCompany, CUserCompanySetting> {

	private static final long serialVersionUID = 1L;
	private final CUserService userService;

	public CComponentCompanyUserSettings(final CCompanyService entityService, ApplicationContext applicationContext) throws Exception {
		super("User Settings", CCompany.class, entityService, applicationContext);
		Check.notNull(relationService, "User company settings service cannot be null");
		userService = applicationContext.getBean(CUserService.class);
		Check.notNull(userService, "User service cannot be null");
		initPanel();
	}

	public List<CUser> getAvailableUsersForCompany() {
		try {
			// Get all users that are not yet members of this company
			return userService.getAvailableUsersForCompany(getCurrentEntity().getId());
		} catch (Exception e) {
			LOGGER.error("Failed to get available users for company {}: {}", getCurrentEntity().getId(), e.getMessage(), e);
			throw new RuntimeException("Failed to get available users", e);
		}
	}

	@Override
	public void initPanel() throws Exception {
		try {
			super.initPanel();
			setupDataAccessors();
			openPanel();
		} catch (Exception e) {
			LOGGER.error("Failed to initialize panel: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to initialize panel", e);
		}
	}

	@Override
	protected void onSettingsSaved(final CUserCompanySetting settings) {
		Check.notNull(settings, "Settings cannot be null when saving");
		LOGGER.debug("Saving user company settings: {}", settings);
		try {
			final CUserCompanySetting savedSettings = settings.getId() == null ? userCompanySettingsService.addUserToCompany(settings.getUser(),
					settings.getCompany(), settings.getOwnershipLevel(), settings.getRole()) : userCompanySettingsService.save(settings);
			LOGGER.info("Successfully saved user company settings: {}", savedSettings);
			populateForm();
		} catch (final Exception e) {
			LOGGER.error("Error saving user company settings: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to save user company settings: " + e.getMessage(), e);
		}
	}

	@Override
	protected void openAddDialog() throws Exception {
		try {
			LOGGER.debug("Opening add dialog for company user settings");
			final CCompany company = getCurrentEntity();
			Check.notNull(company, "Please select a company first.");
			final CCompanyUserSettingsDialog dialog = new CCompanyUserSettingsDialog(this, (CCompanyService) entityService, userService,
					userCompanySettingsService, null, company, this::onSettingsSaved);
			dialog.open();
		} catch (Exception e) {
			LOGGER.error("Failed to open add dialog: {}", e.getMessage(), e);
			new CWarningDialog("Failed to open add dialog: " + e.getMessage()).open();
			throw e;
		}
	}

	@Override
	protected void openEditDialog() throws Exception {
		try {
			LOGGER.debug("Opening edit dialog for company user settings");
			final CUserCompanySetting selected = grid.asSingleSelect().getValue();
			Check.notNull(selected, "Please select a user setting to edit.");
			final CCompany company = getCurrentEntity();
			Check.notNull(company, "Current company is not available.");
			final CCompanyUserSettingsDialog dialog = new CCompanyUserSettingsDialog(this, (CCompanyService) entityService, userService,
					userCompanySettingsService, selected, company, this::onSettingsSaved);
			dialog.open();
		} catch (Exception e) {
			LOGGER.error("Failed to open edit dialog: {}", e.getMessage(), e);
			new CWarningDialog("Failed to open edit dialog: " + e.getMessage()).open();
			throw e;
		}
	}

	@Override
	protected void setupDataAccessors() {
		createStandardDataAccessors(() -> userCompanySettingsService.findByCompany(getCurrentEntity()), () -> entityService.save(getCurrentEntity()));
	}
}
