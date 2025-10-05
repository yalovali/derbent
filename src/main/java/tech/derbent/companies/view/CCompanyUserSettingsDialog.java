package tech.derbent.companies.view;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.roles.domain.CUserCompanyRole;
import tech.derbent.api.roles.service.CUserCompanyRoleService;
import tech.derbent.api.utils.Check;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.config.CSpringContext;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserCompanySetting;
import tech.derbent.users.service.CUserCompanySettingsService;
import tech.derbent.users.service.CUserService;

public class CCompanyUserSettingsDialog extends CUserCompanyRelationDialog<CCompany, CUser> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCompanyUserSettingsDialog.class);
	private static final long serialVersionUID = 1L;
	private final CUserCompanyRoleService roleService;
	private final CCompany masterCompany;

	public CCompanyUserSettingsDialog(IContentOwner parentContent, final CCompanyService masterService, final CUserService detailService,
			final CUserCompanySettingsService relationService, final CUserCompanySetting settings, final CCompany company,
			final Consumer<CUserCompanySetting> onSave) throws Exception {
		super(parentContent, masterService, detailService, relationService, settings, company, onSave);
		this.masterCompany = company;
		// Get the role service from the application context
		this.roleService = CSpringContext.getBean(CUserCompanyRoleService.class);
		Check.notNull(roleService, "Role service cannot be null");
		setupDialog();
		populateForm();
	}

	@Override
	protected String getEditDialogTitle() { return "Edit User Assignment"; }

	@Override
	protected String getEditFormTitle() { return "Edit User Assignment"; }

	@Override
	protected String getNewDialogTitle() { return "Add User to Company"; }

	@Override
	protected String getNewFormTitle() { return "Assign User to Company"; }

	@Override
	protected void setupEntityRelation(CCompany company) {
		getEntity().setCompany(company);
	}

	@Override
	protected List<String> getFormFields() { // TODO Auto-generated method stub
		return List.of("user", "role", "ownershipLevel");
	}

	@Override
	public void setupDialog() throws Exception {
		super.setupDialog();
		setupRoleFilteringForCompany();
	}

	/** Setup role ComboBox to show only roles for the current company. In this dialog, the company is fixed (master entity), so we only need to
	 * filter the role list at initialization. */
	@SuppressWarnings ("unchecked")
	private void setupRoleFilteringForCompany() {
		try {
			// Get the role ComboBox from the form builder
			Component roleComponent = formBuilder.getComponent("role");
			if (roleComponent instanceof ComboBox && masterCompany != null) {
				ComboBox<CUserCompanyRole> roleComboBox = (ComboBox<CUserCompanyRole>) roleComponent;
				// Filter roles for the current company (excluding guest roles)
				updateRoleComboBox(roleComboBox, masterCompany);
				LOGGER.debug("Filtered role ComboBox for company: {}", masterCompany.getName());
			} else {
				LOGGER.warn("Could not find role ComboBox in form builder or master company is null");
			}
		} catch (Exception e) {
			LOGGER.error("Error setting up role filtering: {}", e.getMessage(), e);
		}
	}

	/** Update the role ComboBox with roles for the company.
	 * @param roleComboBox the role ComboBox to update
	 * @param company      the company */
	private void updateRoleComboBox(ComboBox<CUserCompanyRole> roleComboBox, CCompany company) {
		try {
			// Get roles for the company, excluding guest roles
			List<CUserCompanyRole> companyRoles = roleService.findAll().stream()
					.filter(role -> role.getCompany() != null && role.getCompany().getId().equals(company.getId())).filter(role -> !role.isGuest()) // Exclude
																																					// guest
																																					// roles
					.collect(Collectors.toList());
			// Update the ComboBox items
			roleComboBox.setItems(companyRoles);
			// Clear current selection if it's not valid for the company
			CUserCompanyRole currentRole = roleComboBox.getValue();
			if (currentRole != null && !companyRoles.contains(currentRole)) {
				roleComboBox.clear();
			}
			LOGGER.debug("Updated role ComboBox with {} roles for company: {}", companyRoles.size(), company.getName());
		} catch (Exception e) {
			LOGGER.error("Error updating role ComboBox: {}", e.getMessage(), e);
		}
	}
}
