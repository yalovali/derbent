package tech.derbent.api.views.components;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.roles.domain.CUserCompanyRole;
import tech.derbent.api.roles.service.CUserCompanyRoleService;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.companies.view.CUserCompanyRelationDialog;
import tech.derbent.config.CSpringContext;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserCompanySetting;
import tech.derbent.users.service.CUserCompanySettingsService;
import tech.derbent.users.service.CUserService;

public class CUserCompanySettingsDialog extends CUserCompanyRelationDialog<CUser, CCompany> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserCompanySettingsDialog.class);
	private static final long serialVersionUID = 1L;
	private final CUserCompanyRoleService roleService;

	public CUserCompanySettingsDialog(IContentOwner parentContent, final CUserService masterService, final CCompanyService detailService,
			final CUserCompanySettingsService relationService, final CUserCompanySetting settings, final CUser company,
			final Consumer<CUserCompanySetting> onSave) throws Exception {
		super(parentContent, masterService, detailService, relationService, settings, company, onSave);
		// Get the role service from the application context
		this.roleService = CSpringContext.getBean(CUserCompanyRoleService.class);
	}

	@Override
	protected String getEditDialogTitle() { return "Edit Company Assignment"; }

	@Override
	protected String getEditFormTitle() { return "Edit Company Assignment"; }

	@Override
	protected String getNewDialogTitle() { return "Add Company to user"; }

	@Override
	protected String getNewFormTitle() { return "Assign User to company"; }

	@Override
	protected void setupEntityRelation(CUser user) {
		getEntity().setUser(user);
	}

	@Override
	protected List<String> getFormFields() { // TODO Auto-generated method stub
		return List.of("company", "role", "ownershipLevel");
	}

	@Override
	public void setupDialog() throws Exception {
		super.setupDialog();
		setupDynamicRoleFiltering();
	}

	/** Setup dynamic filtering for role ComboBox based on selected company. */
	@SuppressWarnings ("unchecked")
	private void setupDynamicRoleFiltering() {
		try {
			// Get the company and role ComboBoxes from the form builder
			Component companyComponent = formBuilder.getComponent("company");
			Component roleComponent = formBuilder.getComponent("role");
			if (companyComponent instanceof ComboBox && roleComponent instanceof ComboBox) {
				ComboBox<CCompany> companyComboBox = (ComboBox<CCompany>) companyComponent;
				ComboBox<CUserCompanyRole> roleComboBox = (ComboBox<CUserCompanyRole>) roleComponent;
				// Initially disable role ComboBox if no company is selected
				if (companyComboBox.getValue() == null) {
					roleComboBox.setEnabled(false);
					roleComboBox.clear();
					updateSaveButtonState(false);
				} else {
					updateRoleComboBox(roleComboBox, companyComboBox.getValue());
					updateSaveButtonState(true);
				}
				// Add listener to company ComboBox to update role ComboBox
				companyComboBox.addValueChangeListener(event -> {
					CCompany selectedCompany = event.getValue();
					if (selectedCompany != null) {
						// Company selected: enable role ComboBox and update its items
						roleComboBox.setEnabled(true);
						updateRoleComboBox(roleComboBox, selectedCompany);
						updateSaveButtonState(true);
						LOGGER.debug("Company selected: {}. Updated role list for company.", selectedCompany.getName());
					} else {
						// No company selected: disable role ComboBox and clear selection
						roleComboBox.setEnabled(false);
						roleComboBox.clear();
						roleComboBox.setItems(List.of());
						updateSaveButtonState(false);
						LOGGER.debug("Company deselected. Role ComboBox disabled.");
					}
				});
			} else {
				LOGGER.warn("Could not find company or role ComboBox in form builder");
			}
		} catch (Exception e) {
			LOGGER.error("Error setting up dynamic role filtering: {}", e.getMessage(), e);
		}
	}

	/** Update the role ComboBox with roles for the selected company.
	 * @param roleComboBox    the role ComboBox to update
	 * @param selectedCompany the selected company */
	private void updateRoleComboBox(ComboBox<CUserCompanyRole> roleComboBox, CCompany selectedCompany) {
		try {
			// Get roles for the selected company, excluding guest roles
			List<CUserCompanyRole> companyRoles = roleService.findAll().stream()
					.filter(role -> role.getCompany() != null && role.getCompany().getId().equals(selectedCompany.getId()))
					.filter(role -> !role.isGuest()) // Exclude guest roles
					.collect(Collectors.toList());
			// Update the ComboBox items
			roleComboBox.setItems(companyRoles);
			// Clear current selection if it's not valid for the new company
			CUserCompanyRole currentRole = roleComboBox.getValue();
			if (currentRole != null && !companyRoles.contains(currentRole)) {
				roleComboBox.clear();
			}
			LOGGER.debug("Updated role ComboBox with {} roles for company: {}", companyRoles.size(), selectedCompany.getName());
		} catch (Exception e) {
			LOGGER.error("Error updating role ComboBox: {}", e.getMessage(), e);
		}
	}

	/** Update the save button enabled state based on whether a company is selected.
	 * @param enabled true to enable the save button, false to disable */
	private void updateSaveButtonState(boolean enabled) {
		try {
			// Get the save button from the button layout
			buttonLayout.getChildren().forEach(component -> {
				if (component instanceof com.vaadin.flow.component.button.Button) {
					com.vaadin.flow.component.button.Button button = (com.vaadin.flow.component.button.Button) component;
					// Check if this is the save button by its text or theme
					String text = button.getText();
					if (text != null && (text.equals("Save") || text.equals("OK"))) {
						button.setEnabled(enabled);
						LOGGER.debug("Save button enabled state set to: {}", enabled);
					}
				}
			});
		} catch (Exception e) {
			LOGGER.error("Error updating save button state: {}", e.getMessage(), e);
		}
	}
}
