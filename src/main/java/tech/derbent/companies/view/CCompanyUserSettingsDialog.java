package tech.derbent.companies.view;

import java.util.List;
import java.util.function.Consumer;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserCompanySetting;
import tech.derbent.users.service.CUserCompanySettingsService;
import tech.derbent.users.service.CUserService;

/** Dialog for adding users to a company (reverse direction). This dialog manages the relationship between companies and users by creating and editing
 * CUserCompanySetting entities. It allows company managers to: - Add new users to a company with specific roles, departments, and ownership levels -
 * Edit existing user assignments for the company Inherits common relationship management logic from CDBRelationDialog. */
public class CCompanyUserSettingsDialog extends CUserCompanyRelationDialog<CUser, CCompany> {

	private static final long serialVersionUID = 1L;

	public CCompanyUserSettingsDialog(IContentOwner parentContent, final CCompanyService masterService, final CUserService detailService,
			final CUserCompanySettingsService relationService, final CUserCompanySetting settings, final CUser user, final CCompany company,
			final Consumer<CUserCompanySetting> onSave) throws Exception {
		super(parentContent, masterService, detailService, relationService, settings, user, onSave);
	}

	@Override
	protected List<String> getDefaultFormFields() { return List.of("company", "role", "permission"); }

	@Override
	protected String getEditDialogTitle() { return "Edit User Assignment"; }

	@Override
	protected String getEditFormTitle() { return "Edit Cuser Assignment"; }

	@Override
	protected String getNewDialogTitle() { return "Add User Assignment"; }

	@Override
	protected String getNewFormTitle() { return "Assign User to Company"; }

	@Override
	protected void setupEntityRelation(CUser user) {
		getEntity().setUser(user);
	}
}
