package tech.derbent.base.users.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;

/** Test page to demonstrate user icon functionality with CLabelEntity. Shows how user icons are displayed in the application using CLabelEntity. */
@Route ("user-icon-test")
@PageTitle ("User Icon Test")
@AnonymousAllowed
public class CUserIconTestPage extends Div {

	private static final long serialVersionUID = 1L;

	public CUserIconTestPage(@Autowired final CUserService userService) {
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setPadding(true);
		final H2 title = new H2("User Icon Test - CLabelEntity Display");
		layout.add(title);
		// Get first user from database (typically admin)
		try {
			final java.util.List<CUser> users = userService.findAll();
			if (!users.isEmpty()) {
				final CUser user = users.get(0);  // Get first user
				// Display user with CLabelEntity - this is the standard way to show users in the app
				final CLabelEntity userLabel = CLabelEntity.createUserLabel(user);
				// Add descriptive text
				final Div description = new Div();
				description.setText("User icon displayed using CLabelEntity.createUserLabel():");
				description.getStyle().set("margin-top", "20px").set("margin-bottom", "10px");
				layout.add(description);
				layout.add(userLabel);
				// Add details about the user
				final Div details = new Div();
				details.setText("User: " + user.getName() + " | Login: " + user.getLogin() + " | Initials: " + user.getInitials());
				details.getStyle().set("margin-top", "10px").set("color", "#666");
				layout.add(details);
			} else {
				layout.add(new Div("No users found in database."));
			}
		} catch (final Exception e) {
			layout.add(new Div("Error loading user: " + e.getMessage()));
		}
		add(layout);
	}
}
