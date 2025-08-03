package tech.derbent.companies.view;

import java.util.List;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;

/**
 * CPanelCompanyBasicInfo - Panel for grouping basic information fields of CCompany entity. Layer: View (MVC) Groups
 * fields: name, description
 */
public class CPanelCompanyDescription extends CPanelCompanyBase {

    private static final long serialVersionUID = 1L;

    public CPanelCompanyDescription(final CCompany currentEntity,
            final BeanValidationBinder<CCompany> beanValidationBinder, final CCompanyService entityService) {
        super("Basic Information", currentEntity, beanValidationBinder, entityService);
        // only open this panel
        openPanel();
    }

    @Override
    protected void updatePanelEntityFields() {
        // Basic Information fields - company identity
        setEntityFields(List.of("name", "description"));
    }
}