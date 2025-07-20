package tech.derbent.projects.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.views.CAbstractMDPage;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;

@Route("projects/:project_id?/:action?(edit)")
@PageTitle("Project Master Detail")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Settings.Projects")
@PermitAll // When security is enabled, allow all authenticated users
public class CProjectsView extends CAbstractMDPage<CProject> {

	private static final long serialVersionUID = 1L;
	private final String ENTITY_ID_FIELD = "project_id";
	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "projects/%s/edit";
	private TextField name;

	public CProjectsView(final CProjectService entityService) {
		super(CProject.class, entityService);
		addClassNames("projects-view");
		// Configure Form Bind fields. This is where you'd define e.g. validation rules
		createDetailsLayout();
		getBinder().bindInstanceFields(this);
	}

	@Override
	protected void createDetailsLayout() {
		final Div editorLayoutDiv = new Div();
		editorLayoutDiv.setClassName("editor-layout");
		final Div editorDiv = new Div();
		editorDiv.setClassName("editor");
		editorLayoutDiv.add(editorDiv);
		final FormLayout formLayout = new FormLayout();
		name = new TextField("Name");
		formLayout.add(name);
		editorDiv.add(formLayout);
		createButtonLayout(editorLayoutDiv);
		getBaseDetailsLayout().add(editorLayoutDiv);
	}

	@Override
	protected void createGridForEntity() {
		// property name must match the field name in CProject
		grid.addColumn("name").setAutoWidth(true);
		// when a row is selected or deselected, populate form
		grid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				UI.getCurrent().navigate(String.format(ENTITY_ROUTE_TEMPLATE_EDIT, event.getValue().getId()));
			}
			else {
				clearForm();
				UI.getCurrent().navigate(CProjectsView.class);
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
	protected CProject newEntity() {
		return new CProject();
	}

	@Override
	protected void setupToolbar() {
		// TODO Auto-generated method stub
	}
}
