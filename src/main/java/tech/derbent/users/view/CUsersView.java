package tech.derbent.users.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.views.CAbstractMDPage;
import tech.derbent.abstracts.views.CEntityProjectsGrid;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;

@Route("users/:user_id?/:action?(edit)")
@PageTitle("User Master Detail")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Settings.Users")
@PermitAll // When security is enabled, allow all authenticated users
public class CUsersView extends CAbstractMDPage<CUser> {

	private static final long serialVersionUID = 1L;
	private final String ENTITY_ID_FIELD = "user_id";
	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "users/%s/edit";
	private final CEntityProjectsGrid<CUser> projectsGrid;

	public CUsersView(final CUserService entityService, final CProjectService projectService) {
		super(CUser.class, entityService);
		addClassNames("users-view");
		projectsGrid = new CEntityProjectsGrid<CUser>(projectService);
		add(projectsGrid);
	}

	@Override
	protected Component createDetailsLayout() {
		LOGGER.info("Creating details layout for CUsersView");
		final Div detailsLayout = new Div();
		detailsLayout.setClassName("editor-layout");
		detailsLayout.add(CEntityFormBuilder.buildForm(CUser.class, getBinder()));
		createButtonLayout(detailsLayout);
		return detailsLayout;
	}

	@Override
	protected void createGridForEntity() {}

	@Override
	protected String getEntityRouteIdField() { // TODO Auto-generated method stub
		return ENTITY_ID_FIELD;
	}

	@Override
	protected String getEntityRouteTemplateEdit() { // TODO Auto-generated method stub
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
	protected void setupToolbar() {
		// TODO Auto-generated method stub
	}
}
