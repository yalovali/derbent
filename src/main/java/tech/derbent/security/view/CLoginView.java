package tech.derbent.security.view;

import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.views.CAbstractMDPage;
import tech.derbent.security.domain.CLogin;
import tech.derbent.security.service.CLoginService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;

@Route("logins/:login_id?/:action?(edit)")
@PageTitle("Login Management")
@Menu(order = 1, icon = "vaadin:key", title = "Settings.Login Users")
@PermitAll
public class CLoginView extends CAbstractMDPage<CLogin> {

    private static final long serialVersionUID = 1L;
    private final String ENTITY_ID_FIELD = "login_id";
    private final String ENTITY_ROUTE_TEMPLATE_EDIT = "logins/%s/edit";
    
    private TextField username;
    private PasswordField password;
    private PasswordField confirmPassword;
    private TextField roles;
    private Checkbox enabled;
    private ComboBox<CUser> userComboBox;
    private Button cancel;
    private Button save;
    private Button delete;
    
    private final CUserService userService;

    public CLoginView(CLoginService loginService, CUserService userService) {
        super(CLogin.class, loginService);
        this.userService = userService;
        addClassNames("login-view");
        binder.bindInstanceFields(this);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel = new Button("Cancel");
        save = new Button("Save");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete = new Button("Delete");
        delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        delete.addClickListener(e -> {
            if (currentEntity != null) {
                entityService.delete(currentEntity);
                refreshGrid();
                Notification.show("Login deleted successfully");
                UI.getCurrent().navigate(CLoginView.class);
            }
        });
        
        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });
        
        save.addClickListener(e -> {
            try {
                // Validate password confirmation
                if (password.getValue() != null && !password.getValue().isEmpty()) {
                    if (!password.getValue().equals(confirmPassword.getValue())) {
                        Notification.show("Passwords do not match!");
                        return;
                    }
                }
                
                // Check username uniqueness for new entities
                if (currentEntity == null || currentEntity.getId() == null) {
                    if (((CLoginService) entityService).existsByUsername(username.getValue())) {
                        Notification.show("Username already exists!");
                        return;
                    }
                    currentEntity = new CLogin();
                }
                
                // Save the entity
                binder.writeBean(currentEntity);
                
                // Handle password update
                if (password.getValue() != null && !password.getValue().isEmpty()) {
                    ((CLoginService) entityService).updatePassword(currentEntity, password.getValue());
                } else {
                    entityService.save(currentEntity);
                }
                
                clearForm();
                refreshGrid();
                Notification.show("Login saved successfully");
                UI.getCurrent().navigate(CLoginView.class);
                
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show("Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });
        
        buttonLayout.add(save, cancel, delete);
        editorLayoutDiv.add(buttonLayout);
    }

    @Override
    protected void createDetailsLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");
        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);
        
        FormLayout formLayout = new FormLayout();
        username = new TextField("Username");
        password = new PasswordField("Password");
        confirmPassword = new PasswordField("Confirm Password");
        roles = new TextField("Roles");
        roles.setHelperText("Comma-separated roles (e.g., USER,ADMIN)");
        roles.setValue("USER"); // Default value
        enabled = new Checkbox("Enabled");
        enabled.setValue(true); // Default value
        
        userComboBox = new ComboBox<>("Associated User");
        userComboBox.setItemLabelGenerator(user -> user.getName() + " " + user.getLastname() + " (" + user.getEmail() + ")");
        userComboBox.setItems(userService.list(org.springframework.data.domain.Pageable.unpaged()));
        
        formLayout.add(username, password, confirmPassword, roles, enabled, userComboBox);
        editorDiv.add(formLayout);
        
        createButtonLayout(editorLayoutDiv);
        splitLayout.addToSecondary(editorLayoutDiv);
    }

    @Override
    protected void createGridForEntity() {
        grid.addColumn("username").setAutoWidth(true);
        grid.addColumn("roles").setAutoWidth(true);
        grid.addColumn("enabled").setAutoWidth(true);
        grid.addColumn(login -> login.getUser() != null ? login.getUser().getName() + " " + login.getUser().getLastname() : "")
            .setHeader("User").setAutoWidth(true);
        
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(ENTITY_ROUTE_TEMPLATE_EDIT, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(CLoginView.class);
            }
        });
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
        // Initialize the page components and layout
    }

    @Override
    protected void setupContent() {
        // Setup content if needed
    }

    @Override
    protected void setupToolbar() {
        // Setup toolbar if needed
    }
}