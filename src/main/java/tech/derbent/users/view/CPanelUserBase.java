package tech.derbent.users.view;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.annotations.CEntityFormBuilder.ComboBoxDataProvider;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.views.CAccordionDBEntity;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserType;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;

/**
 * CPanelUserBase - Abstract base class for all CUser-related accordion panels. Layer:
 * View (MVC) Provides common functionality for user entity panels following the same
 * pattern as CPanelActivityBase.
 */
public abstract class CPanelUserBase extends CAccordionDBEntity<CUser> {

	private static final long serialVersionUID = 1L;

	private final CUserTypeService userTypeService;

	private final CCompanyService companyService;

	/**
	 * Constructor with custom panel title.
	 * @param title                custom title for the panel
	 * @param currentEntity        current user entity
	 * @param beanValidationBinder validation binder
	 * @param entityService        user service
	 */
	public CPanelUserBase(final String title, final CUser currentEntity,
		final BeanValidationBinder<CUser> beanValidationBinder,
		final CUserService entityService, final CUserTypeService userTypeService,
		final CCompanyService companyService) {
		super(title, currentEntity, beanValidationBinder, CUser.class, entityService);
		this.userTypeService = userTypeService;
		this.companyService = companyService;
		createPanelContent();
		closePanel();
	}

	@Override
	protected ComboBoxDataProvider createComboBoxDataProvider() {
		final CEntityFormBuilder.ComboBoxDataProvider dataProvider =
			new CEntityFormBuilder.ComboBoxDataProvider() {

				@Override
				@SuppressWarnings ("unchecked")
				public <T extends CEntityDB> java.util.List<T>
					getItems(final Class<T> entityType) {
					LOGGER.debug("Getting items for entity type: {}",
						entityType.getSimpleName());

					if (entityType == CUserType.class) {
						final java.util.List<T> userTypes =
							(java.util.List<T>) userTypeService
								.list(org.springframework.data.domain.Pageable.unpaged());
						LOGGER.debug("Retrieved {} user types", userTypes.size());
						return userTypes;
					}
					else if (entityType == CCompany.class) {
						final java.util.List<T> companies =
							(java.util.List<T>) companyService.findEnabledCompanies();
						LOGGER.debug("Retrieved {} enabled companies", companies.size());
						return companies;
					}
					LOGGER.warn("No data provider available for entity type: {}",
						entityType.getSimpleName());
					return java.util.Collections.emptyList();
				}
			};
		return dataProvider;
	}

	@Override
	protected void createPanelContent() {
		getBaseLayout().add(CEntityFormBuilder.buildForm(CUser.class, getBinder(),
			getDetailsDataProvider(), getEntityFields()));
	}
}