package tech.derbent.users.view;

import java.util.List;

import com.vaadin.flow.component.textfield.PasswordField;
import tech.derbent.abstracts.components.CEnhancedBinder;

import tech.derbent.companies.service.CCompanyService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;

public class CPanelUserDescription extends CPanelUserBase {

    private static final long serialVersionUID = 1L;

    private PasswordField passwordField;

    /**
     * Default constructor for CUserDescriptionPanel.
     * 
     * @param currentEntity
     *            the current user entity
     * @param beanValidationBinder
     *            the validation binder
     * @param entityService
     *            the user service
     * @param userTypeService
     *            the user type service
     * @param companyService
     *            the company service
     */
    public CPanelUserDescription(final CUser currentEntity, final CEnhancedBinder<CUser> beanValidationBinder,
            final CUserService entityService, final CUserTypeService userTypeService,
            final CCompanyService companyService) {
        super("Basic Information", currentEntity, beanValidationBinder, entityService, userTypeService, companyService);
        // open the panel by default using the new convenience method
        openPanel();
    }

    @Override
    protected void createPanelContent() {
        super.createPanelContent();
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
        super.populateForm(entity);
    }

    @Override
    public void saveEventHandler() {

        // Handle password update if a new password was entered
        if ((passwordField != null) && !passwordField.isEmpty()) {
            final String newPassword = passwordField.getValue();

            if ((getCurrentEntity().getLogin() != null) && !getCurrentEntity().getLogin().isEmpty()) {
                ((CUserService) entityService).updatePassword(getCurrentEntity().getLogin(), newPassword);
                LOGGER.info("Password updated for user: {}", getCurrentEntity().getLogin());
            }
        }
    }

    @Override
    protected void updatePanelEntityFields() {
        // Basic Information fields - essential user identity
        setEntityFields(List.of("name", "lastname", "login", "password"));
    }
}
