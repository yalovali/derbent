package tech.derbent.users.view;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;

/**
 * CPanelUserContactInfo - Panel for grouping contact information fields of CUser entity. Layer: View (MVC) Groups
 * fields: email, phone
 */
public class CPanelUserContactInfo extends CPanelUserBase {

    private static final long serialVersionUID = 1L;

    public CPanelUserContactInfo(final CUser currentEntity, final CEnhancedBinder<CUser> beanValidationBinder,
            final CUserService entityService, final CUserTypeService userTypeService,
            final CCompanyService companyService)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
        super("Contact Information", currentEntity, beanValidationBinder, entityService, userTypeService,
                companyService);
        initPanel();
    }

    @Override
    protected void updatePanelEntityFields() {
        // Contact Information fields - communication details
        setEntityFields(List.of("email", "phone"));
    }
}