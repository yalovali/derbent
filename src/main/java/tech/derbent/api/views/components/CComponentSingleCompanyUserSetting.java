package tech.derbent.api.views.components;

import java.util.List;
import org.springframework.context.ApplicationContext;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.ui.dialogs.CWarningDialog;
import tech.derbent.api.utils.Check;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserService;

/** Component for displaying and editing a user's single company setting. This component provides a nice visual layout with icons and colors for the
 * CUserCompanySettings field, allowing users to view and edit their company membership and role through an attractive interface. */
public class CComponentSingleCompanyUserSetting extends CComponentDBEntity<CUser> {

	private static final long serialVersionUID = 1L;
	private final CCompanyService companyService;
	private CFormBuilder<CUserProjectSettings> formBuilder = new CFormBuilder<CUserProjectSettings>();

	public CComponentSingleCompanyUserSetting(CUserService entityService, ApplicationContext applicationContext) throws Exception {
		super("Company Setting", CUser.class, applicationContext);
		companyService = applicationContext.getBean(CCompanyService.class);
		initComponent();
	}

	/** Default implementation of populateForm using the binder. Child classes can override. */
	@Override
	protected void populateForm() {
		Check.notNull(binder, "Binder must be initialized before populating the form");
		binder.readBean(getCurrentEntity());
	}

	public List<CCompany> getAvailableCompanyForUser() { return companyService.getAvailableCompanyForUser(getCurrentEntity().getId()); }

	protected void openAddDialog() throws Exception {
		try {
			new CUserCompanySettingsDialog(this, (CUserService) entityService, companyService, userCompanySettingsService, null, getCurrentEntity(),
					this::onSettingsSaved).open();
		} catch (Exception e) {
			new CWarningDialog("Failed to open add dialog: " + e.getMessage()).open();
			throw e;
		}
	}

	protected void openEditDialog() throws Exception {
		try {
			new CUserCompanySettingsDialog(this, (CUserService) entityService, companyService, userCompanySettingsService, getSelectedSetting(),
					getCurrentEntity(), this::onSettingsSaved).open();
		} catch (Exception e) {
			new CWarningDialog("Failed to open edit dialog: " + e.getMessage()).open();
			throw e;
		}
	}

	protected List<String> getFormFields() { // TODO Auto-generated method stub
		return List.of("company", "role", "ownershipLevel");
	}

	protected void setupDataAccessors() {
		createStandardDataAccessors(() -> userCompanySettingsService.findByUser(getCurrentEntity()), () -> entityService.save(getCurrentEntity()));
	}

	@Override
	protected void initPanel() throws Exception {
		formBuilder = new CFormBuilder<>(this, entityClass, getBinder(), getFormFields());
		getDialogLayout().add(formBuilder.getFormLayout());
	}

	@Override
	protected void updatePanelEntityFields() {
		// TODO Auto-generated method stub
	}

	/** Abstract methods that subclasses must implement */
	protected void onSettingsSaved(final CUserProjectSettings settings) {
		Check.notNull(settings, "Settings cannot be null when saving");
		LOGGER.debug("Saving user project settings: {}", settings);
		try {
			final CUserProjectSettings savedSettings = settings.getId() == null ? userProjectSettingsService.addUserToProject(settings.getUser(),
					settings.getProject(), settings.getRole(), settings.getPermission()) : userProjectSettingsService.save(settings);
			LOGGER.info("Successfully saved user project settings: {}", savedSettings);
			populateForm();
		} catch (final Exception e) {
			LOGGER.error("Error saving user project settings: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to save user project settings: " + e.getMessage(), e);
		}
	}
}
