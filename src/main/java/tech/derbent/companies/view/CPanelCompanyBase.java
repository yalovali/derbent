package tech.derbent.companies.view;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.views.CAccordionDescription;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;

/**
 * CPanelCompanyBase - Abstract base class for all CCompany-related accordion panels.
 * Layer: View (MVC)
 * Provides common functionality for company entity panels following the same pattern as CPanelActivityBase.
 */
public abstract class CPanelCompanyBase extends CAccordionDescription<CCompany> {

	private static final long serialVersionUID = 1L;

	public CPanelCompanyBase(final CCompany currentEntity,
		final BeanValidationBinder<CCompany> beanValidationBinder,
		final CCompanyService entityService) {
		super(currentEntity, beanValidationBinder, CCompany.class, entityService);
		createPanelContent();
		closePanel();
	}

	/**
	 * Constructor with custom panel title.
	 * @param title custom title for the panel
	 * @param currentEntity current company entity
	 * @param beanValidationBinder validation binder
	 * @param entityService company service
	 */
	public CPanelCompanyBase(final String title, final CCompany currentEntity,
		final BeanValidationBinder<CCompany> beanValidationBinder,
		final CCompanyService entityService) {
		super(title, currentEntity, beanValidationBinder, CCompany.class, entityService);
		createPanelContent();
		closePanel();
	}

	@Override
	protected void createPanelContent() {
		updatePanelEntityFields(); // Set the entity fields first
		getBaseLayout().add(CEntityFormBuilder.buildForm(CCompany.class, getBinder(),
			getEntityFields()));
	}
}