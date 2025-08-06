package tech.derbent.login.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
import org.springframework.beans.factory.annotation.Autowired;

import tech.derbent.base.data.DatabaseResetService;

/**
 * Custom login view using basic Vaadin components instead of LoginOverlay. This provides
 * an alternative login interface for testing purposes.
 */
@Route (value = "login", autoLayout = false)
@PageTitle ("Login")
@AnonymousAllowed
public class CCustomLoginView extends Main implements BeforeEnterObserver {

	private static final long serialVersionUID = 1L;

	private final TextField usernameField = new TextField("Username");

	private final PasswordField passwordField = new PasswordField("Password");

	private final Button loginButton = new Button("Login");

	private final Button resetDbButton = new Button("Reset Database");

	private final Div errorMessage = new Div();

	private final DatabaseResetService databaseResetService;

	/**
	 * Constructor sets up the custom login form with basic Vaadin components.
	 */
	public CCustomLoginView(DatabaseResetService databaseResetService) {
		this.databaseResetService = databaseResetService;
		addClassNames("custom-login-view");
		setSizeFull();
		setupForm();
	}

	/**
	 * Handles navigation events before entering the view. Checks for authentication
	 * failure indicators and displays error messages.
	 */
	@Override
	public void beforeEnter(final BeforeEnterEvent event) {

		// Check if the URL contains an error parameter
		if (event.getLocation().getQueryParameters().getParameters()
			.containsKey("error")) {
			showError("Invalid username or password");
		}
	}

	private void handleLogin() {
		final String username = usernameField.getValue();
		final String password = passwordField.getValue();
		// Clear previous error
		errorMessage.setVisible(false);

		// Basic validation
		if (username.isEmpty() || password.isEmpty()) {
			showError("Please enter both username and password");
			return;
		}
		// Create form and submit to Spring Security endpoint
		getElement().executeJs(
			"const form = document.createElement('form');" + "form.method = 'POST';"
				+ "form.action = 'login';"
				+ "const usernameInput = document.createElement('input');"
				+ "usernameInput.type = 'hidden';" + "usernameInput.name = 'username';"
				+ "usernameInput.value = $0;" + "form.appendChild(usernameInput);"
				+ "const passwordInput = document.createElement('input');"
				+ "passwordInput.type = 'hidden';" + "passwordInput.name = 'password';"
				+ "passwordInput.value = $1;" + "form.appendChild(passwordInput);"
				+ "document.body.appendChild(form);" + "form.submit();",
			username, password);
	}
	
	private void handleDatabaseReset() {
		try {
			databaseResetService.resetDatabase();
			Notification.show("Database reset successfully - sample data loaded", 3000, 
				Notification.Position.MIDDLE);
		} catch (final Exception e) {
			Notification.show("Error resetting database: " + e.getMessage(), 3000, 
				Notification.Position.MIDDLE);
		}
	}

	private void setupForm() {
		// Create main container
		final VerticalLayout container = new VerticalLayout();
		container.addClassNames(LumoUtility.JustifyContent.CENTER,
			LumoUtility.AlignItems.CENTER, LumoUtility.Height.FULL,
			LumoUtility.Padding.LARGE);
		container.setSpacing(false);
		
		// Create form card with minimal design
		final VerticalLayout formCard = new VerticalLayout();
		formCard.addClassNames(LumoUtility.Background.BASE,
			LumoUtility.BorderRadius.MEDIUM, LumoUtility.BoxShadow.SMALL,
			LumoUtility.Padding.LARGE);
		formCard.setSpacing(true);
		formCard.setWidth("500px");
		
		// Application icon - using a simple Vaadin icon instead of image
		final var icon = VaadinIcon.BUILDING.create();
		icon.setSize("48px");
		icon.addClassNames(LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.TextColor.PRIMARY);
		
		// Title
		final H1 title = new H1("Derbent");
		title.addClassNames(LumoUtility.TextAlignment.CENTER,
			LumoUtility.Margin.Bottom.SMALL);
		
		// Subtitle
		final Paragraph subtitle = new Paragraph("Control the progress");
		subtitle.addClassNames(LumoUtility.TextAlignment.CENTER,
			LumoUtility.TextColor.SECONDARY);
		
		// Setup form fields horizontally
		final HorizontalLayout usernameLayout = createHorizontalField("Username:", usernameField);
		final HorizontalLayout passwordLayout = createHorizontalField("Password:", passwordField);
		
		// Setup form fields
		setupFormFields();
		
		// Database reset button setup
		resetDbButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
		resetDbButton.addClickListener(e -> handleDatabaseReset());
		
		// Error message display
		errorMessage.addClassNames(LumoUtility.TextColor.ERROR,
			LumoUtility.TextAlignment.CENTER, LumoUtility.FontSize.SMALL);
		errorMessage.setVisible(false);
		
		// Password hint
		final Paragraph passwordHint = new Paragraph("Default: admin/test123");
		passwordHint.addClassNames(LumoUtility.TextAlignment.CENTER,
			LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
		
		// Back to original login link
		final RouterLink backLink =
			new RouterLink("Back to original login", CLoginView.class);
		backLink.addClassNames(LumoUtility.TextAlignment.CENTER,
			LumoUtility.FontSize.SMALL);
		
		// Add components to form card
		formCard.add(icon, title, subtitle, usernameLayout, passwordLayout, errorMessage,
			loginButton, resetDbButton, passwordHint, backLink);
		container.add(formCard);
		add(container);
	}
	
	private HorizontalLayout createHorizontalField(String labelText, com.vaadin.flow.component.Component field) {
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setWidthFull();
		layout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
		
		final Paragraph label = new Paragraph(labelText);
		label.addClassNames(LumoUtility.FontWeight.MEDIUM);
		label.setWidth("120px");
		
		field.getElement().getStyle().set("flex", "1");
		
		layout.add(field, label);
		return layout;
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
		passwordField.addKeyPressListener(com.vaadin.flow.component.Key.ENTER,
			e -> handleLogin());
	}

	private void showError(final String message) {
		errorMessage.setText(message);
		errorMessage.setVisible(true);
	}
}