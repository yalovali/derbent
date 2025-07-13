package tech.derbent.users.view;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.views.CAbstractMDPage;

@Route("users/:user_id?/:action?(edit)")
@PageTitle("User Master Detail")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Settings.Users")
@PermitAll // When security is enabled, allow all authenticated users
public class CUsersView extends CAbstractMDPage {

	private static final long serialVersionUID = 1L;

	public CUsersView() {
		super();
		addClassNames("users-view");
		setSizeFull();
	}

	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void initPage() {
		// Initialize the page components and layout This method can be overridden to
		// set up the view's components
	}

	@Override
	protected void setupContent() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void setupToolbar() {
		// TODO Auto-generated method stub
	}
}
