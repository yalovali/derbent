package tech.derbent.companies.view;

import java.util.List;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;

/**
 * CPanelCompanyContactDetails - Panel for grouping contact information fields of CCompany
 * entity. Layer: View (MVC) Groups fields: address, phone, email, website
 */
public class CPanelCompanyContactDetails extends CPanelCompanyBase {

	private static final long serialVersionUID = 1L;

	public CPanelCompanyContactDetails(final CCompany currentEntity,
		final BeanValidationBinder<CCompany> beanValidationBinder,
		final CCompanyService entityService) {
		super("Contact Details", currentEntity, beanValidationBinder, entityService);
	}

	@Override
	protected void updatePanelEntityFields() {
		// Contact Details fields - communication and location information
		setEntityFields(List.of("address", "phone", "email", "website"));
	}
}