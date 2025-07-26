package tech.derbent.users.view;

import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.views.CAccordionDescription;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserType;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;

public class CPanelUserDescription extends CAccordionDescription<CUser> {

	private static final long serialVersionUID = 1L;

	private PasswordField passwordField;

	private final CUserTypeService userTypeService;

	private final CCompanyService companyService;

	/**
	 * Default constructor for CUserDescriptionPanel.
	 * @param currentEntity        the current user entity
	 * @param beanValidationBinder the validation binder
	 * @param entityService        the user service
	 * @param userTypeService      the user type service
	 * @param companyService       the company service
	 */
	public CPanelUserDescription(final CUser currentEntity,
		final BeanValidationBinder<CUser> beanValidationBinder,
		final CUserService entityService, final CUserTypeService userTypeService,
		final CCompanyService companyService) {
		super(currentEntity, beanValidationBinder, CUser.class, entityService);
		this.userTypeService = userTypeService;
		this.companyService = companyService;
		LOGGER.info(
			"CPanelUserDescription initialized with user type and company services");
		createPanelContent();
		// open the panel by default using the new convenience method
		openPanel();
	}

	@Override
	protected void createPanelContent() {
		// Create data provider for ComboBoxes
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
		getBaseLayout().add(CEntityFormBuilder.buildForm(CUser.class, getBinder(),
			dataProvider, getEntityFields()));
		// Add password field for editing
		passwordField = new PasswordField("Password");
		passwordField.setPlaceholder("Enter new password (leave empty to keep current)");
		passwordField.setWidthFull();
		passwordField.setHelperText("Password will be encrypted when saved");
		getBaseLayout().add(passwordField);
	}

	@Override
	public void populateForm(final CUser entity) {

		// Clear password field when populating form (for security)
		if (passwordField != null) {
			passwordField.clear();
		}
		currentEntity = entity;
	}

	@Override
	public void saveEventHandler() {

		// Handle password update if a new password was entered
		if ((passwordField != null) && !passwordField.isEmpty()) {
			final String newPassword = passwordField.getValue();

			if ((currentEntity.getLogin() != null)
				&& !currentEntity.getLogin().isEmpty()) {
				((CUserService) entityService).updatePassword(currentEntity.getLogin(),
					newPassword);
				LOGGER.info("Password updated for user: {}", currentEntity.getLogin());
			}
		}
	}
}
