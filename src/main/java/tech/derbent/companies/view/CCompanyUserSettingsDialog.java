package tech.derbent.companies.view;

import java.util.List;
import java.util.function.Consumer;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.views.dialogs.CDBRelationDialog;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserCompanySettings;
import tech.derbent.users.service.CUserCompanySettingsService;
import tech.derbent.users.service.CUserService;

/** Dialog for adding users to a company (reverse direction). This dialog manages the relationship between companies and users by creating and editing
 * CUserCompanySettings entities. It allows company managers to: - Add new users to a company with specific roles, departments, and ownership levels - Edit existing user
 * assignments for the company Inherits common relationship management logic from CDBRelationDialog. */
public class CCompanyUserSettingsDialog extends CDBRelationDialog<CUserCompanySettings, CCompany, CUser> {

	private static final long serialVersionUID = 1L;

	public CCompanyUserSettingsDialog(IContentOwner parentContent, final CCompanyService masterService, final CUserService detailService,
			final CUserCompanySettingsService relationService, final CUserCompanySettings settings, final CCompany company,
			final Consumer<CUserCompanySettings> onSave) throws Exception {
		super(parentContent, settings != null ? settings : new CUserCompanySettings(), company, masterService, detailService, relationService, onSave,
				settings == null);
		getEntity().setCompany(company);
		setupDialog();
		populateForm();
	}

	@Override
	public String getDialogTitleString() { return isNew ? "Add User to Company" : "Edit User Assignment"; }

	@Override
	protected String getFormTitleString() { return isNew ? "Add User to Company" : "Edit User Company Assignment"; }

	@Override
	protected List<String> getFormFields() {
		// Company-centric: select user, role, department, ownership level
		return List.of("user", "role", "department", "ownershipLevel", "isPrimaryCompany");
	}
}