package tech.derbent.api.views.components;

import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.roles.domain.CUserCompanyRole;
import tech.derbent.api.roles.service.CUserCompanyRoleService;
import tech.derbent.api.utils.Check;
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
		Check.notNull(roleService, "Role service cannot be null");
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

	@Override
	protected void populateForm() {
		prepareRoleComboBoxForPopulation();
		super.populateForm();
	}

	/** Prepares the role ComboBox by populating its items based on the current entity's company. This must be called before binder.readBean() to
	 * avoid BindingException when the entity has a role value that isn't in the ComboBox's (empty) items list. */
	@SuppressWarnings ("unchecked")
	private void prepareRoleComboBoxForPopulation() {
		try {
			// Get the company from the entity being edited
			CUserCompanySetting entity = getEntity();
			if (entity == null || entity.getCompany() == null) {
				// No company set yet, role ComboBox will remain empty
				LOGGER.debug("No company set in entity, role ComboBox will remain empty");
				return;
			}
			// Get the role ComboBox from the form builder
			Component roleComponent = formBuilder.getComponent("role");
			if (roleComponent instanceof ComboBox) {
				ComboBox<CUserCompanyRole> roleComboBox = (ComboBox<CUserCompanyRole>) roleComponent;
				// Populate the role ComboBox with items for the entity's company
				updateRoleComboBox(roleComboBox, entity.getCompany());
				LOGGER.debug("Pre-populated role ComboBox with items for company: {}", entity.getCompany().getName());
			}
		} catch (Exception e) {
			LOGGER.error("Error preparing role ComboBox for population: {}", e.getMessage(), e);
			// Don't throw - let populateForm() proceed, it may still work or provide better error info
		}
	}

	/** Setup dynamic filtering for role ComboBox based on selected company. */
	@SuppressWarnings ("unchecked")
	private void setupDynamicRoleFiltering() {
		try {
			// Get the company and role ComboBoxes from the form builder
			Component companyComponent = formBuilder.getComponent("company");
			Component roleComponent = formBuilder.getComponent("role");
			Check.notNull(companyComponent, "Company component cannot be null");
			Check.notNull(roleComponent, "Role component cannot be null");
			ComboBox<CCompany> companyComboBox = (ComboBox<CCompany>) companyComponent;
			ComboBox<CUserCompanyRole> roleComboBox = (ComboBox<CUserCompanyRole>) roleComponent;
			Check.notNull(companyComboBox, "Company ComboBox cannot be null");
			Check.notNull(roleComboBox, "Role ComboBox cannot be null");
			// Initially disable role ComboBox if no company is selected
			if (companyComboBox.getValue() == null) {
				roleComboBox.setEnabled(false);
				roleComboBox.clear();
				updateSaveButtonState(false);
				return;
			}
			updateRoleComboBox(roleComboBox, companyComboBox.getValue());
			updateSaveButtonState(true);
			// Add listener to company ComboBox to update role ComboBox
			companyComboBox.addValueChangeListener(event -> {
				CCompany selectedCompany = event.getValue();
				if (selectedCompany != null) {
					// Company selected: enable role ComboBox and update its items
					roleComboBox.setEnabled(true);
					updateRoleComboBox(roleComboBox, selectedCompany);
					updateSaveButtonState(true);
					LOGGER.debug("Company selected: {}. Updated role list for company.", selectedCompany.getName());
					return;
				}
				// No company selected: disable role ComboBox and clear selection
				roleComboBox.setEnabled(false);
				roleComboBox.clear();
				roleComboBox.setItems(List.of());
				updateSaveButtonState(false);
				LOGGER.debug("Company deselected. Role ComboBox disabled.");
			});
		} catch (Exception e) {
			LOGGER.error("Error setting up dynamic role filtering: {}", e.getMessage(), e);
		}
	}

	/** Update the role ComboBox with roles for the selected company.
	 * @param roleComboBox    the role ComboBox to update
	 * @param selectedCompany the selected company */
	private void updateRoleComboBox(ComboBox<CUserCompanyRole> roleComboBox, CCompany selectedCompany) {
		try {
			Check.notNull(roleComboBox, "Role ComboBox cannot be null");
			Check.notNull(roleService, "Role service cannot be null");
			if (selectedCompany == null) {
				roleComboBox.setItems(List.of());
				roleComboBox.clear();
				return;
			}
			// Get roles for the selected company, excluding guest roles
			List<CUserCompanyRole> companyRoles = roleService.findByCompany(selectedCompany);
			roleComboBox.setItems(companyRoles);
			// Clear current selection if it's not valid for the new company
			CUserCompanyRole currentRole = roleComboBox.getValue();
			if (currentRole != null && !companyRoles.contains(currentRole)) {
				roleComboBox.clear();
			}
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
				if (component instanceof Button) {
					Button button = (Button) component;
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
