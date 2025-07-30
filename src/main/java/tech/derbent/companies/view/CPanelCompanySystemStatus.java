package tech.derbent.companies.view;

import java.util.List;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;

/**
 * CPanelCompanySystemStatus - Panel for grouping system status and administrative fields
 * of CCompany entity. Layer: View (MVC) Groups fields: enabled, taxNumber
 */
public class CPanelCompanySystemStatus extends CPanelCompanyBase {

	private static final long serialVersionUID = 1L;

	public CPanelCompanySystemStatus(final CCompany currentEntity,
		final BeanValidationBinder<CCompany> beanValidationBinder,
		final CCompanyService entityService) {
		super("System Status", currentEntity, beanValidationBinder, entityService);
	}

	@Override
	protected void updatePanelEntityFields() {
		// System Status fields - administrative and status information
		setEntityFields(List.of("enabled", "taxNumber"));
	}
}