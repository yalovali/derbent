package tech.derbent.users.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.splitlayout.SplitLayout;
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
	// private final BeanValidationBinder<CUser> binder; private final CUserService
	// userService; private final Grid<CUser> grid;// = new Grid<>(CUser.class,
	// false);

	public CUsersView(final CUserService entityService, final CProjectService projectService) {
		super(CUser.class, entityService);
		addClassNames("users-view");
		// Configure Form Bind fields. This is where you'd define e.g. validation rules
		// getBinder().bindInstanceFields(this);
		projectsGrid = new CEntityProjectsGrid<CUser>(projectService);
		add(projectsGrid);
	}

	@Override
	protected void createDetailsLayout(final SplitLayout splitLayout) {
		LOGGER.info("Creating details layout for CUsersView");
		final Div editorLayoutDiv = new Div();
		editorLayoutDiv.setClassName("editor-layout");
		editorLayoutDiv.add(CEntityFormBuilder.buildForm(CUser.class, getBinder()));
		createButtonLayout(editorLayoutDiv);
		splitLayout.addToSecondary(editorLayoutDiv);
	}

	@Override
	protected void createGridForEntity() {
		LOGGER.info("Creating grid for CUsersView");
		// property name must match the field name in CUser
		grid.addColumn("name").setAutoWidth(true);
		// when a row is selected or deselected, populate form
		grid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				currentEntity = ((CUserService) entityService).getUserWithProjects(event.getValue().getId());
				projectsGrid.setProjectAccessors(currentEntity::getProjects, currentEntity::setProjects, () -> entityService.save(currentEntity));
				projectsGrid.refresh();
				UI.getCurrent().navigate(String.format(ENTITY_ROUTE_TEMPLATE_EDIT, currentEntity.getId()));
			}
			else {
				clearForm();
				projectsGrid.setProjectAccessors(null, null, null);
				projectsGrid.refresh();
				UI.getCurrent().navigate(CUsersView.class);
			}
		});
	}

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
		// Initialize the page components and layout This method can be overridden to
		// set up the view's components
	}

	@Override
	protected CUser newEntity() {
		return new CUser();
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
