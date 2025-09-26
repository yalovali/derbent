package tech.derbent.companies.view;

import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.views.CAccordionDBEntity;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;

/** CPanelCompanyBase - Abstract base class for all CCompany-related accordion panels. Layer: View (MVC) Provides common functionality for company
 * entity panels following the same pattern as CPanelActivityBase. */
public abstract class CPanelCompanyBase extends CAccordionDBEntity<CCompany> {

	private static final long serialVersionUID = 1L;

	/** Constructor with custom panel title.
	 * @param title                custom title for the panel
	 * @param currentEntity        current company entity
	 * @param beanValidationBinder validation binder
	 * @param entityService        company service */
	public CPanelCompanyBase(final String title, IContentOwner parentContent, final CEnhancedBinder<CCompany> beanValidationBinder,
			final CCompanyService entityService) {
		super(title, parentContent, beanValidationBinder, CCompany.class, entityService);
	}
}
