package tech.derbent.users.view;

import java.util.List;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;

/**
 * CPanelUserSystemAccess - Panel for grouping system access related fields of CUser
 * entity. Layer: View (MVC) Groups fields: password, roles, userRole, enabled
 */
public class CPanelUserSystemAccess extends CPanelUserBase {

	private static final long serialVersionUID = 1L;

	public CPanelUserSystemAccess(final CUser currentEntity,
		final CEnhancedBinder<CUser> beanValidationBinder,
		final CUserService entityService, final CUserTypeService userTypeService,
		final CCompanyService companyService) {
		super("System Access", currentEntity, beanValidationBinder, entityService,
			userTypeService, companyService);
		initPanel();
	}

	@Override
	protected void updatePanelEntityFields() {
		// System Access fields - authentication and authorization
		setEntityFields(List.of("roles", "userRole", "enabled"));
	}
}