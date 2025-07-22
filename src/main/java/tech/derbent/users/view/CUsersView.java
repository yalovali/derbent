package tech.derbent.users.view;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.views.CAbstractMDPage;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserType;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;

@Route("users/:user_id?/:action?(edit)")
@PageTitle("User Master Detail")
@Menu(order = 0, icon = "vaadin:users", title = "Settings.Users")
@PermitAll // When security is enabled, allow all authenticated users
public class CUsersView extends CAbstractMDPage<CUser> {

    private static final long serialVersionUID = 1L;
    private final String ENTITY_ID_FIELD = "user_id";
    private final String ENTITY_ROUTE_TEMPLATE_EDIT = "users/%s/edit";
    private final CUserProjectSettingsGrid projectSettingsGrid;
    private final CUserTypeService userTypeService;
    private PasswordField passwordField;

    // private final TextField name; • Annotate the CUsersView constructor with
    // @Autowired to let Spring inject dependencies.
    @Autowired
    public CUsersView(final CUserService entityService, final CProjectService projectService,
            final CUserTypeService userTypeService) {
        super(CUser.class, entityService);
        addClassNames("users-view");
        this.userTypeService = userTypeService;
        projectSettingsGrid = new CUserProjectSettingsGrid(projectService);
    }

    @Override
    protected void createDetailsLayout() {
        LOGGER.info("Creating details layout for CUsersView");
        final Div detailsLayout = new Div();
        detailsLayout.setClassName("editor-layout");
        // Create data provider for ComboBoxes
        final CEntityFormBuilder.ComboBoxDataProvider dataProvider = new CEntityFormBuilder.ComboBoxDataProvider() {

            @Override
            @SuppressWarnings("unchecked")
            public <T extends CEntityDB> java.util.List<T> getItems(final Class<T> entityType) {
                if (entityType == CUserType.class) {
                    return (java.util.List<T>) userTypeService.list(org.springframework.data.domain.Pageable.unpaged());
                }
                return java.util.Collections.emptyList();
            }
        };
        detailsLayout.add(CEntityFormBuilder.buildForm(CUser.class, getBinder(), dataProvider));
        
        // Add password field for editing
        passwordField = new PasswordField("Password");
        passwordField.setPlaceholder("Enter new password (leave empty to keep current)");
        passwordField.setWidthFull();
        passwordField.setHelperText("Password will be encrypted when saved");
        detailsLayout.add(passwordField);
        
        // Note: Buttons are now automatically added to the details tab by the parent class
        detailsLayout.add(projectSettingsGrid);
        getBaseDetailsLayout().add(detailsLayout);
    }

    @Override
    protected Div createDetailsTabLeftContent() {
        // Create custom tab content for users view
        final Div detailsTabLabel = new Div();
        detailsTabLabel.setText("User Details");
        detailsTabLabel.setClassName("details-tab-label");
        return detailsTabLabel;
    }

    @Override
    protected void createGridForEntity() {
        LOGGER.info("Creating grid for users");
        // Add columns for key user information
        grid.addColumn(CUser::getName).setAutoWidth(true).setHeader("Name").setSortable(true);
        grid.addColumn(CUser::getLastname).setAutoWidth(true).setHeader("Last Name").setSortable(true);
        grid.addColumn(CUser::getLogin).setAutoWidth(true).setHeader("Login").setSortable(true);
        grid.addColumn(CUser::getEmail).setAutoWidth(true).setHeader("Email").setSortable(true);
        grid.addColumn(user -> user.isEnabled() ? "Enabled" : "Disabled").setAutoWidth(true).setHeader("Status").setSortable(true);
        grid.addColumn(user -> user.getUserType() != null ? user.getUserType().getName() : "").setAutoWidth(true).setHeader("User Type").setSortable(true);
        grid.addColumn(CUser::getRoles).setAutoWidth(true).setHeader("Roles").setSortable(true);
        grid.setItems(query -> entityService.list(VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
    }

    @Override
    protected String getEntityRouteIdField() {
        return ENTITY_ID_FIELD;
    }

    @Override
    protected String getEntityRouteTemplateEdit() {
        return ENTITY_ROUTE_TEMPLATE_EDIT;
    }

    @Override
    protected void initPage() {
        // TODO Auto-generated method stub
    }

    @Override
    protected CUser newEntity() {
        return new CUser();
    }

    @Override
    protected void populateForm(final CUser value) {
        super.populateForm(value);
        // Clear password field when populating form (for security)
        if (passwordField != null) {
            passwordField.clear();
        }
        // Update the project settings grid when a user is selected
        if (value != null) {
            // Load user with project settings to avoid lazy initialization issues
            final CUser userWithSettings = ((CUserService) entityService).getUserWithProjects(value.getId());
            projectSettingsGrid.setCurrentUser(userWithSettings);
            projectSettingsGrid.setProjectSettingsAccessors(() -> userWithSettings.getProjectSettings() != null
                    ? userWithSettings.getProjectSettings()
                    : java.util.Collections.emptyList(), (settings) -> {
                        userWithSettings.setProjectSettings(settings);
                        // Save the user when project settings are updated
                        entityService.save(userWithSettings);
                    }, () -> {
                        // Refresh the current entity after save
                        try {
                            final CUser refreshedUser = ((CUserService) entityService)
                                    .getUserWithProjects(userWithSettings.getId());
                            populateForm(refreshedUser);
                        } catch (final Exception e) {
                            LOGGER.error("Error refreshing user after project settings update", e);
                        }
                    });
        } else {
            projectSettingsGrid.setCurrentUser(null);
            projectSettingsGrid.setProjectSettingsAccessors(() -> java.util.Collections.emptyList(), (settings) -> {
            }, () -> {
            });
        }
    }

    @Override
    protected void setupToolbar() {
        // TODO Auto-generated method stub
    }
    
    @Override
    protected tech.derbent.abstracts.views.CButton createSaveButton(final String buttonText) {
        LOGGER.info("Creating custom save button for CUsersView");
        final tech.derbent.abstracts.views.CButton save = tech.derbent.abstracts.views.CButton.createPrimary(buttonText, e -> {
            try {
                if (currentEntity == null) {
                    currentEntity = newEntity();
                }
                getBinder().writeBean(currentEntity);
                
                // Handle password update if a new password was entered
                if (passwordField != null && !passwordField.isEmpty()) {
                    final String newPassword = passwordField.getValue();
                    if (currentEntity.getLogin() != null && !currentEntity.getLogin().isEmpty()) {
                        ((CUserService) entityService).updatePassword(currentEntity.getLogin(), newPassword);
                        LOGGER.info("Password updated for user: {}", currentEntity.getLogin());
                    }
                }
                
                entityService.save(currentEntity);
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                // Navigate back to the current view (list mode)
                UI.getCurrent().navigate(getClass());
            } catch (final ValidationException validationException) {
                new tech.derbent.abstracts.views.CWarningDialog("Validation Error",
                        "Failed to save the data. Please check that all required fields are filled and values are valid.")
                        .open();
            } catch (final Exception exception) {
                LOGGER.error("Unexpected error during save operation", exception);
                new tech.derbent.abstracts.views.CWarningDialog("Save Error", "An unexpected error occurred while saving. Please try again.").open();
            }
        });
        return save;
    }
}
