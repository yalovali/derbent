package tech.derbent.api.views.components;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationContext;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.roles.domain.CUserCompanyRole;
import tech.derbent.api.roles.service.CUserCompanyRoleService;
import tech.derbent.api.ui.dialogs.CWarningDialog;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserCompanySetting;
import tech.derbent.users.service.CUserCompanySettingsService;
import tech.derbent.users.service.CUserService;

/** Component for displaying and editing a user's single company setting. This component provides a nice visual layout with icons and colors for the
 * CUserCompanySettings field, allowing users to view and edit their company membership and role through an attractive interface. */
public class CComponentSingleCompanyUserSetting extends CComponentDBEntity<CUser> {

	private static final long serialVersionUID = 1L;
	private Button changeButton;
	private final CCompanyService companyService;
	private CHorizontalLayout displayContainer;
	private final CUserCompanyRoleService userCompanyRoleService;
	private final CUserCompanySettingsService userCompanySettingsService;

	public CComponentSingleCompanyUserSetting(CUserService entityService, ApplicationContext applicationContext) throws Exception {
		super("Company Setting", CUser.class, applicationContext);
		companyService = applicationContext.getBean(CCompanyService.class);
		userCompanySettingsService = applicationContext.getBean(CUserCompanySettingsService.class);
		userCompanyRoleService = applicationContext.getBean(CUserCompanyRoleService.class);
		initComponent();
	}

	public List<CCompany> getAvailableCompanyForUser() {
		return companyService.getAvailableCompanyForUser(getCurrentEntity() != null ? getCurrentEntity().getId() : null);
	}

	/** Get available company roles for the user's company. Only returns member roles (non-guest roles).
	 * @return list of available company roles */
	public List<CUserCompanyRole> getAvailableCompanyRolesForUser() {
		CUser user = getCurrentEntity();
		Check.notNull(user, "User cannot be null when fetching available company roles");
		CUserCompanySetting settings = user.getCompanySettingsInstance(userCompanySettingsService);
		if (settings == null) {
			return List.of();
		}
		CCompany company = settings.getCompany();
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
	protected void initPanel() throws Exception {
		CHorizontalLayout mainLayout = new CHorizontalLayout();
		displayContainer = new CHorizontalLayout();
		// Create change button
		changeButton = CButton.createPrimary("Change", VaadinIcon.EDIT.create(), e -> {
			try {
				openChangeDialog();
			} catch (Exception ex) {
				LOGGER.error("Error opening change dialog: {}", ex.getMessage(), ex);
			}
		});
		mainLayout.add(displayContainer, changeButton);
		add(mainLayout);
	}

	/** Callback when settings are saved from the dialog */
	protected void onSettingsSaved(final CUserCompanySetting settings) {
		Check.notNull(settings, "Settings cannot be null when saving");
		LOGGER.debug("Saving user company settings: {}", settings);
		try {
			CUser user = getCurrentEntity();
			Check.notNull(user, "User cannot be null when saving settings");
			// Save or update the setting
			CUserCompanySetting savedSettings;
			if (settings.getId() == null) {
				// New setting
				savedSettings = userCompanySettingsService.addUserToCompany(settings.getUser(), settings.getCompany(), settings.getRole(),
						settings.getOwnershipLevel());
			} else {
				// Update existing setting
				savedSettings = userCompanySettingsService.save(settings);
			}
			// Update the user's company setting reference
			user.setCompanySettings(savedSettings);
			entityService.save(user);
			populateForm();
		} catch (final Exception e) {
			LOGGER.error("Error saving user company settings: {}", e.getMessage(), e);
			new CWarningDialog("Failed to save company settings: " + e.getMessage()).open();
			throw new RuntimeException("Failed to save user company settings: " + e.getMessage(), e);
		}
	}

	protected void openChangeDialog() throws Exception {
		try {
			CUser user = getCurrentEntity();
			Check.notNull(user, "User cannot be null when opening change dialog");
			CUserCompanySetting currentSetting = user.getCompanySettingsInstance(userCompanySettingsService);
			Check.notNull(currentSetting, "Current setting cannot be null when opening change dialog");
			new CUserCompanySettingsDialog(this, (CUserService) entityService, companyService, userCompanySettingsService, currentSetting, user,
					this::onSettingsSaved).open();
		} catch (Exception e) {
			LOGGER.error("Failed to open change dialog: {}", e.getMessage(), e);
			new CWarningDialog("Failed to open change dialog: " + e.getMessage()).open();
			throw e;
		}
	}

	@Override
	public void populateForm() {
		super.populateForm();
		displayContainer.removeAll();
		// Reload user with company setting eagerly loaded to avoid LazyInitializationException
		if (getCurrentEntity() == null) {
			displayContainer.add(new Span("No user selected"));
			changeButton.setEnabled(false);
			return;
		}
		changeButton.setEnabled(true);
		Optional<CUser> userWithCompanySetting = ((CUserService) entityService).findByIdWithCompanySetting(getCurrentEntity().getId());
		CUser user = userWithCompanySetting
				.orElseThrow(() -> new IllegalStateException("Failed to load user with company setting for user id: " + getCurrentEntity().getId()));
		// get settings for eager loading by id
		CUserCompanySetting setting = user.getCompanySettingsInstance(userCompanySettingsService);
		Check.notNull(setting, "User company setting cannot be null when populating form");
		if (setting == null) {
			displayContainer.add(new Span("No company assigned. Click 'Change' to assign a company."));
			return;
		}
		// Create visual display with company and role
		// Display company with icon and color
		CCompany company = setting.getCompany();
		Check.notNull(company, "Company in user company setting cannot be null");
		CUserCompanyRole role = setting.getRole();
		Check.notNull(role, "Role in user company setting cannot be null");
		/**/
		CHorizontalLayout layoutRoleRelation = new CHorizontalLayout();
		layoutRoleRelation.add(CColorUtils.getEntityWithIcon(company));
		layoutRoleRelation.add(new CSpan("|", 40));
		layoutRoleRelation.add(CColorUtils.getEntityWithIcon(role));
		layoutRoleRelation.add(new CSpan("|", 40));
		layoutRoleRelation.add(new Span(setting.getOwnershipLevel()));
		displayContainer.add(layoutRoleRelation);
	}

	@Override
	protected void updatePanelEntityFields() {
		// No fields needed for this component - it's display only with change button
	}
}
