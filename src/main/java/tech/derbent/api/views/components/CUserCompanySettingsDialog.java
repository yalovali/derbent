package tech.derbent.api.views.components;

import java.util.List;
import java.util.function.Consumer;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.companies.view.CUserCompanyRelationDialog;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserCompanySetting;
import tech.derbent.users.service.CUserCompanySettingsService;
import tech.derbent.users.service.CUserService;

public class CUserCompanySettingsDialog extends CUserCompanyRelationDialog<CUser, CCompany> {

	private static final long serialVersionUID = 1L;

	public CUserCompanySettingsDialog(IContentOwner parentContent, final CUserService masterService, final CCompanyService detailService,
			final CUserCompanySettingsService relationService, final CUserCompanySetting settings, final CUser company,
			final Consumer<CUserCompanySetting> onSave) throws Exception {
		super(parentContent, masterService, detailService, relationService, settings, company, onSave);
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
}
