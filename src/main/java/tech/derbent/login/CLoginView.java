package tech.derbent.login;

import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;

//Disables auto layout to prevent the login view from being embedded in a router layout.
@Route(value = "login", autoLayout = false)
@PageTitle("Login")
@AnonymousAllowed // Allows anonymous access so users can access the login page without
					// authentication.
public class CLoginView extends Main implements BeforeEnterObserver {

	private final LoginForm login;

	public CLoginView() {
		addClassNames(LumoUtility.Display.FLEX, LumoUtility.JustifyContent.CENTER, LumoUtility.AlignItems.CENTER);
		setSizeFull();
		login = new LoginForm();
		login.setAction("login"); // Instructs the login form to send a POST request to /login for authentication.
		add(login);
	}

	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
			// Handles login failures by checking for the ?error query parameter and
			// displaying an error message.
			login.setError(true);
		}
	}
}