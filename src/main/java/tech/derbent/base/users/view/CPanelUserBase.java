package tech.derbent.base.users.view;

import java.util.Collections;
import java.util.List;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.annotations.CFormBuilder.IComboBoxDataProvider;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.views.CAccordionDBEntity;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.companies.service.CCompanyService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;

/** CPanelUserBase - Abstract base class for all CUser-related accordion panels. Layer: View (MVC) Provides common functionality for user entity
 * panels following the same pattern as CPanelActivityBase. */
public abstract class CPanelUserBase extends CAccordionDBEntity<CUser> {

	private static final long serialVersionUID = 1L;
	private final CCompanyService companyService;

	/** Constructor with custom panel title.
	 * @param title                custom title for the panel
	 * @param currentEntity        current user entity
	 * @param beanValidationBinder validation binder
	 * @param entityService        user service */
	public CPanelUserBase(final String title, IContentOwner parentContent, final CUser currentEntity,
			final CEnhancedBinder<CUser> beanValidationBinder, final CUserService entityService, final CCompanyService companyService) {
		super(title, parentContent, beanValidationBinder, CUser.class, entityService);
		this.companyService = companyService;
	}

	@Override
	protected IComboBoxDataProvider createComboBoxDataProvider() {
		final CFormBuilder.IComboBoxDataProvider dataProvider = new CFormBuilder.IComboBoxDataProvider() {

			@Override
			@SuppressWarnings ("unchecked")
			public <T extends CEntityDB<T>> List<T> getItems(final Class<T> entityType) {
				LOGGER.debug("Getting items for entity type: {}", entityType.getSimpleName());
				if (entityType == CCompany.class) {
					final List<T> companies = (List<T>) companyService.findActiveCompanies();
					LOGGER.debug("Retrieved {} active companies", companies.size());
					return companies;
				}
				LOGGER.warn("No data provider available for entity type: {}", entityType.getSimpleName());
				return Collections.emptyList();
			}
		};
		return dataProvider;
	}
}
