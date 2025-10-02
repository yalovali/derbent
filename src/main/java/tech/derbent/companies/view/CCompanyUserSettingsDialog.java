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

public class CCompanyUserSettingsDialog extends CUserCompanyRelationDialog<CCompany, CUser> {

	private static final long serialVersionUID = 1L;

	public CCompanyUserSettingsDialog(IContentOwner parentContent, final CCompanyService masterService, final CUserService detailService,
			final CUserCompanySettingsService relationService, final CUserCompanySetting settings, final CCompany company,
			final Consumer<CUserCompanySetting> onSave) throws Exception {
		super(parentContent, masterService, detailService, relationService, settings, company, onSave);
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
}
