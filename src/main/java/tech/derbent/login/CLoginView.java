package tech.derbent.login;

import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Login view for user authentication. This view presents a login form for users
 * to enter their credentials. Authentication Flow Integration: 1. User
 * navigates to protected resource while unauthenticated 2. Spring Security
 * redirects to this login view (/login) 3. User enters username and password in
 * the LoginForm 4. Form submits to /login endpoint (configured in setAction) 5.
 * Spring Security intercepts the POST request 6.
 * CLoginUserService.loadUserByUsername() is called to find user 7. Password is
 * verified using BCryptPasswordEncoder 8. If successful, user is authenticated
 * and redirected to original resource 9. If failed, user is redirected back
 * here with ?error parameter Error Handling: - Authentication failures are
 * indicated by ?error query parameter - LoginForm displays error message to
 * user - User can retry with correct credentials
 */
// Disables auto layout to prevent the login view from being embedded in a
// router layout.
@Route(value = "login", autoLayout = false)
@PageTitle("Login")
@AnonymousAllowed // Allows anonymous access so users can access the login page without
					// authentication.
public class CLoginView extends Main implements BeforeEnterObserver {

	private static final long serialVersionUID = 1L;
	private final LoginForm login;

	/**
	 * Constructor sets up the login form and page layout. Form Configuration: -
	 * Centered on page using Lumo utility classes - Full size to utilize available
	 * space - Action set to "login" endpoint for Spring Security processing
	 */
	public CLoginView() {
		// Apply CSS classes for centering the login form
		addClassNames(LumoUtility.Display.FLEX, LumoUtility.JustifyContent.CENTER, LumoUtility.AlignItems.CENTER);
		setSizeFull();
		login = new LoginForm();
		// Create login form component
		final LoginOverlay loginOverlay = new LoginOverlay();
		// Welcome and password hint text
		final Paragraph welcomeText = new Paragraph("Welcome to the secure area! Please log in to continue.\n" + "If you are a new user, use the default credentials below.");
		welcomeText.addClassName(LumoUtility.TextAlignment.CENTER);
		final Paragraph passwordHint = new Paragraph("Default username: user\nDefault password: test123\n" + "Never share your password with anyone.");
		passwordHint.addClassName(LumoUtility.TextAlignment.CENTER);
		loginOverlay.getFooter().add(welcomeText, passwordHint);
		final Paragraph text = new Paragraph("check:spring.jpa.hibernate.ddl-auto = create to create default user");
		text.addClassName(LumoUtility.TextAlignment.CENTER);
		loginOverlay.getFooter().add(text);
		// Set form action to Spring Security's login processing endpoint This tells the
		// form to POST credentials to /login for authentication
		login.setAction("login");
		login.setForgotPasswordButtonVisible(false); // Hide forgot password button
		// Show the overlay what !!!!!!
		loginOverlay.setOpened(true);
		add(login);
	}

	/**
	 * Handles navigation events before entering the view. Checks for authentication
	 * failure indicators and displays error messages. Error Parameter Handling: -
	 * Spring Security adds ?error parameter on authentication failure - This method
	 * detects the parameter and shows error state in login form - User sees visual
	 * feedback that their credentials were incorrect
	 * @param event the navigation event containing query parameters
	 */
	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		// Check if the URL contains an error parameter
		if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
			// Show error state in login form This provides visual feedback that
			// authentication failed
			login.setError(true);
		}
	}
}