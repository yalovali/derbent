package tech.derbent.users.view;

import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.views.CAccordionDescription;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserType;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;

public class CPanelUserDescription extends CAccordionDescription<CUser> {

	private static final long serialVersionUID = 1L;
	private PasswordField passwordField;
	private final CUserTypeService userTypeService;

	/**
	 * Default constructor for CUserDescriptionPanel.
	 * @param userTypeService
	 * @param beanValidationBinder
	 */
	public CPanelUserDescription(final CUser currentEntity,
		final BeanValidationBinder<CUser> beanValidationBinder,
		final CUserService entityService, final CUserTypeService userTypeService) {
		super(currentEntity, beanValidationBinder, CUser.class, entityService);
		this.userTypeService = userTypeService;
		createPanelContent();
		// open the panel by default
		open(0);
	}

	@Override
	protected void createPanelContent() {
		// Create data provider for ComboBoxes
		final CEntityFormBuilder.ComboBoxDataProvider dataProvider =
			new CEntityFormBuilder.ComboBoxDataProvider() {

				@Override
				@SuppressWarnings("unchecked")
				public <T extends CEntityDB> java.util.List<T>
					getItems(final Class<T> entityType) {
					if (entityType == CUserType.class) {
						return (java.util.List<T>) userTypeService
							.list(org.springframework.data.domain.Pageable.unpaged());
					}
					return java.util.Collections.emptyList();
				}
			};
		getBaseLayout()
			.add(CEntityFormBuilder.buildForm(CUser.class, getBinder(), dataProvider));
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
