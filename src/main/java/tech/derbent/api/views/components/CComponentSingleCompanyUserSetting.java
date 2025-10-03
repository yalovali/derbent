package tech.derbent.api.views.components;

import java.util.List;
import org.springframework.context.ApplicationContext;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
	private final CCompanyService companyService;
	private final CUserCompanySettingsService userCompanySettingsService;
	private Div displayContainer;
	private Button changeButton;

	public CComponentSingleCompanyUserSetting(CUserService entityService, ApplicationContext applicationContext) throws Exception {
		super("Company Setting", CUser.class, applicationContext);
		companyService = applicationContext.getBean(CCompanyService.class);
		userCompanySettingsService = applicationContext.getBean(CUserCompanySettingsService.class);
		initComponent();
	}

	public List<CCompany> getAvailableCompanyForUser() {
		return companyService.getAvailableCompanyForUser(getCurrentEntity() != null ? getCurrentEntity().getId() : null);
	}

	protected void openChangeDialog() throws Exception {
		try {
			CUser user = getCurrentEntity();
			Check.notNull(user, "User cannot be null when opening change dialog");
			// Get the current setting (may be null if none exists yet)
			CUserCompanySetting currentSetting = user.getCompanySettings();
			new CUserCompanySettingsDialog(this, (CUserService) entityService, companyService, userCompanySettingsService, currentSetting, user,
					this::onSettingsSaved).open();
		} catch (Exception e) {
			LOGGER.error("Failed to open change dialog: {}", e.getMessage(), e);
			new CWarningDialog("Failed to open change dialog: " + e.getMessage()).open();
			throw e;
		}
	}

	@Override
	protected void initPanel() throws Exception {
		// Create display container for the single company setting
		displayContainer = new Div();
		displayContainer.addClassName("single-company-setting-display");
		displayContainer.setWidthFull();
		// Create change button
		changeButton = CButton.createPrimary("Change", com.vaadin.flow.component.icon.VaadinIcon.EDIT.create(), e -> {
			try {
				openChangeDialog();
			} catch (Exception ex) {
				LOGGER.error("Error opening change dialog: {}", ex.getMessage(), ex);
			}
		});
		// Add components to layout
		add(displayContainer, changeButton);
	}

	@Override
	public void populateForm() {
		super.populateForm();
		updateDisplay();
	}

	private void updateDisplay() {
		displayContainer.removeAll();
		CUser user = getCurrentEntity();
		if (user == null) {
			displayContainer.add(new Span("No user selected"));
			changeButton.setEnabled(false);
			return;
		}
		changeButton.setEnabled(true);
		CUserCompanySetting setting = user.getCompanySettings();
		if (setting == null) {
			displayContainer.add(new Span("No company assigned. Click 'Change' to assign a company."));
			return;
		}
		// Create visual display with company and role
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		layout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
		try {
			// Display company with icon and color
			CCompany company = setting.getCompany();
			if (company != null) {
				HorizontalLayout companyDisplay = CColorUtils.getEntityWithIcon(company);
				layout.add(companyDisplay);
			}
			// Add separator
			layout.add(new Span(" | "));
			// Display role with label
			String role = setting.getRole();
			if (role != null && !role.trim().isEmpty()) {
				Span roleLabel = new Span("Role: ");
				roleLabel.getStyle().set("font-weight", "bold");
				Span roleValue = new Span(role);
				layout.add(roleLabel, roleValue);
			} else {
				layout.add(new Span("Role: (not set)"));
			}
			// Add separator
			layout.add(new Span(" | "));
			// Display ownership level
			String ownershipLevel = setting.getOwnershipLevel();
			if (ownershipLevel != null && !ownershipLevel.trim().isEmpty()) {
				Span ownershipLabel = new Span("Level: ");
				ownershipLabel.getStyle().set("font-weight", "bold");
				Span ownershipValue = new Span(ownershipLevel);
				layout.add(ownershipLabel, ownershipValue);
			}
		} catch (Exception e) {
			LOGGER.error("Error creating display: {}", e.getMessage(), e);
			layout.add(new Span("Error displaying company setting"));
		}
		displayContainer.add(layout);
	}

	@Override
	protected void updatePanelEntityFields() {
		// No fields needed for this component - it's display only with change button
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
				savedSettings = userCompanySettingsService.addUserToCompany(settings.getUser(), settings.getCompany(), settings.getOwnershipLevel(),
						settings.getRole());
			} else {
				// Update existing setting
				savedSettings = userCompanySettingsService.save(settings);
			}
			// Update the user's company setting reference
			user.setCompanySettings(savedSettings);
			entityService.save(user);
			LOGGER.info("Successfully saved user company settings: {}", savedSettings);
			// Refresh display
			updateDisplay();
		} catch (final Exception e) {
			LOGGER.error("Error saving user company settings: {}", e.getMessage(), e);
			new CWarningDialog("Failed to save company settings: " + e.getMessage()).open();
			throw new RuntimeException("Failed to save user company settings: " + e.getMessage(), e);
		}
	}
}
