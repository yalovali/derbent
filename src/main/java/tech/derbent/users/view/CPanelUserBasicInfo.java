package tech.derbent.users.view;

import java.util.List;

import tech.derbent.abstracts.components.CEnhancedBinder;

import tech.derbent.companies.service.CCompanyService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;

/**
 * CPanelUserBasicInfo - Panel for grouping basic information fields of CUser entity. Layer: View (MVC) Groups fields:
 * name, lastname, login
 */
public class CPanelUserBasicInfo extends CPanelUserBase {

    private static final long serialVersionUID = 1L;

    public CPanelUserBasicInfo(final CUser currentEntity, final CEnhancedBinder<CUser> beanValidationBinder,
            final CUserService entityService, final CUserTypeService userTypeService,
            final CCompanyService companyService) {
        super("Basic Information", currentEntity, beanValidationBinder, entityService, userTypeService, companyService);
        // only open this panel
        openPanel();
    }

    @Override
    protected void updatePanelEntityFields() {
        // Basic Information fields - essential user identity
        setEntityFields(List.of("name", "lastname", "login"));
    }
}