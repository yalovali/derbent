package tech.derbent.companies.view;

import java.util.List;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;

/**
 * CPanelCompanyUsers - Panel for grouping user relationship fields of CCompany entity.
 * Layer: View (MVC) Groups fields: users
 */
public class CPanelCompanyUsers extends CPanelCompanyBase {

	private static final long serialVersionUID = 1L;

	public CPanelCompanyUsers(final CCompany currentEntity,
		final BeanValidationBinder<CCompany> beanValidationBinder,
		final CCompanyService entityService) {
		super("Users", currentEntity, beanValidationBinder, entityService);
	}

	@Override
	protected void updatePanelEntityFields() {
		// Users fields - employee relationships
		setEntityFields(List.of("users"));
	}
}