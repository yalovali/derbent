package tech.derbent.login.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Custom login view using basic Vaadin components instead of LoginOverlay.
 * This provides an alternative login interface for testing purposes.
 */
@Route(value = "custom-login", autoLayout = false)
@PageTitle("Custom Login")
@AnonymousAllowed
public class CCustomLoginView extends Main implements BeforeEnterObserver {

    private static final long serialVersionUID = 1L;

    private final TextField usernameField = new TextField("Username");
    private final PasswordField passwordField = new PasswordField("Password");
    private final Button loginButton = new Button("Login");
    private final Div errorMessage = new Div();

    /**
     * Constructor sets up the custom login form with basic Vaadin components.
     */
    public CCustomLoginView() {
        addClassNames("custom-login-view");
        setSizeFull();
        setupForm();
    }

    private void setupForm() {
        // Create main container
        VerticalLayout container = new VerticalLayout();
        container.addClassNames(
            LumoUtility.JustifyContent.CENTER,
            LumoUtility.AlignItems.CENTER,
            LumoUtility.Height.FULL,
            LumoUtility.Padding.LARGE
        );
        container.setSpacing(false);

        // Create form card
        VerticalLayout formCard = new VerticalLayout();
        formCard.addClassNames(
            LumoUtility.Background.BASE,
            LumoUtility.BorderRadius.LARGE,
            LumoUtility.BoxShadow.LARGE,
            LumoUtility.Padding.LARGE
        );
        formCard.setSpacing(true);
        formCard.setWidth("400px");

        // Application icon/logo (simple text for now)
        Image icon = new Image("themes/default/images/background1.png", "Derbent Logo");
        icon.setWidth("60px");
        icon.setHeight("60px");
        icon.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

        // Title
        H1 title = new H1("Derbent");
        title.addClassNames(LumoUtility.TextAlignment.CENTER, LumoUtility.Margin.Bottom.SMALL);

        // Subtitle
        Paragraph subtitle = new Paragraph("Control the progress");
        subtitle.addClassNames(LumoUtility.TextAlignment.CENTER, LumoUtility.TextColor.SECONDARY);

        // Setup form fields
        setupFormFields();

        // Error message display
        errorMessage.addClassNames(
            LumoUtility.TextColor.ERROR,
            LumoUtility.TextAlignment.CENTER,
            LumoUtility.FontSize.SMALL
        );
        errorMessage.setVisible(false);

        // Password hint
        Paragraph passwordHint = new Paragraph("Default: admin/test123");
        passwordHint.addClassNames(
            LumoUtility.TextAlignment.CENTER,
            LumoUtility.TextColor.SECONDARY,
            LumoUtility.FontSize.SMALL
        );

        // Back to original login link
        RouterLink backLink = new RouterLink("Back to original login", CLoginView.class);
        backLink.addClassNames(LumoUtility.TextAlignment.CENTER, LumoUtility.FontSize.SMALL);

        // Add components to form card
        formCard.add(
            icon,
            title,
            subtitle,
            usernameField,
            passwordField,
            errorMessage,
            loginButton,
            passwordHint,
            backLink
        );

        container.add(formCard);
        add(container);
    }

    private void setupFormFields() {
        // Username field setup
        usernameField.setWidthFull();
        usernameField.setRequired(true);
        usernameField.setRequiredIndicatorVisible(true);
        usernameField.setId("custom-username-input");

        // Password field setup
        passwordField.setWidthFull();
        passwordField.setRequired(true);
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.setId("custom-password-input");

        // Login button setup
        loginButton.setWidthFull();
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.setId("custom-submit-button");
        
        // Add click listener to login button
        loginButton.addClickListener(e -> handleLogin());

        // Add enter key listener to password field
        passwordField.addKeyPressListener(com.vaadin.flow.component.Key.ENTER, e -> handleLogin());
    }

    private void handleLogin() {
        String username = usernameField.getValue();
        String password = passwordField.getValue();

        // Clear previous error
        errorMessage.setVisible(false);

        // Basic validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        // Create form and submit to Spring Security endpoint
        getElement().executeJs(
            "const form = document.createElement('form');" +
            "form.method = 'POST';" +
            "form.action = 'login';" +
            "const usernameInput = document.createElement('input');" +
            "usernameInput.type = 'hidden';" +
            "usernameInput.name = 'username';" +
            "usernameInput.value = $0;" +
            "form.appendChild(usernameInput);" +
            "const passwordInput = document.createElement('input');" +
            "passwordInput.type = 'hidden';" +
            "passwordInput.name = 'password';" +
            "passwordInput.value = $1;" +
            "form.appendChild(passwordInput);" +
            "document.body.appendChild(form);" +
            "form.submit();",
            username, password
        );
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
    }

    /**
     * Handles navigation events before entering the view.
     * Checks for authentication failure indicators and displays error messages.
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Check if the URL contains an error parameter
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            showError("Invalid username or password");
        }
    }
}