package tech.derbent.users.view;

import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.annotations.CFormBuilder.ComboBoxDataProvider;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.views.CAccordionDBEntity;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserType;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;

/** CPanelUserBase - Abstract base class for all CUser-related accordion panels. Layer: View (MVC) Provides common functionality for user entity
 * panels following the same pattern as CPanelActivityBase. */
public abstract class CPanelUserBase extends CAccordionDBEntity<CUser> {

	private static final long serialVersionUID = 1L;
	private final CUserTypeService userTypeService;
	private final CCompanyService companyService;

	/** Constructor with custom panel title.
	 * @param title                custom title for the panel
	 * @param currentEntity        current user entity
	 * @param beanValidationBinder validation binder
	 * @param entityService        user service */
	public CPanelUserBase(final String title, IContentOwner parentContent, final CUser currentEntity,
			final CEnhancedBinder<CUser> beanValidationBinder, final CUserService entityService, final CUserTypeService userTypeService,
			final CCompanyService companyService) {
		super(title, parentContent,beanValidationBinder, CUser.class, entityService);
		this.userTypeService = userTypeService;
		this.companyService = companyService;
	}

	@Override
	protected ComboBoxDataProvider createComboBoxDataProvider() {
		final CFormBuilder.ComboBoxDataProvider dataProvider = new CFormBuilder.ComboBoxDataProvider() {

			@Override
			@SuppressWarnings ("unchecked")
			public <T extends CEntityDB<T>> java.util.List<T> getItems(final Class<T> entityType) {
				LOGGER.debug("Getting items for entity type: {}", entityType.getSimpleName());
				if (entityType == CUserType.class) {
					final java.util.List<T> userTypes = (java.util.List<T>) userTypeService.list(org.springframework.data.domain.Pageable.unpaged());
					LOGGER.debug("Retrieved {} user types", userTypes.size());
					return userTypes;
				} else if (entityType == CCompany.class) {
					final java.util.List<T> companies = (java.util.List<T>) companyService.findEnabledCompanies();
					LOGGER.debug("Retrieved {} enabled companies", companies.size());
					return companies;
				}
				LOGGER.warn("No data provider available for entity type: {}", entityType.getSimpleName());
				return java.util.Collections.emptyList();
			}
		};
		return dataProvider;
	}
}
