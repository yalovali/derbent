package tech.derbent.users.view;

import java.time.Clock;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.views.CAbstractMDPage;
import tech.derbent.base.ui.component.ViewToolbar;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;

@Route("user-list-view")
@PageTitle("User List View")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Settings.User Test")
@PermitAll // When security is enabled, allow all authenticated users
public class CUsersTestView extends CAbstractMDPage<CUser> {

	private static final long serialVersionUID = 1L;
	// private final CUserService service;
	TextField name;
	// Grid<CUser> grid;
	Clock clock;
	Button createBtn;

	public CUsersTestView(final CUserService service, final Clock clock) {
		super(CUser.class, service);
		this.clock = clock;
		// this.service = service; grid = new Grid<>(); grid.setItems(query ->
		// service.list(toSpringPageRequest(query)).stream()); grid.setSizeFull();
		add(grid);
	}

	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void createDetailsLayout(final SplitLayout splitLayout) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void createGridForEntity() {
		grid.addColumn(CUser::getName).setHeader("Description");
	}

	private void createTask() {
		((CUserService) entityService).createEntity(name.getValue());
		grid.getDataProvider().refreshAll();
		name.clear();
		Notification.show("Entity added", 3000, Notification.Position.BOTTOM_END).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
	}

	@Override
	protected String getEntityRouteIdField() { // TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getEntityRouteTemplateEdit() { // TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void initPage() {
		// Initialize the page components and layout
		setSizeFull();
		addClassNames("task-list-view");
	}

	@Override
	protected void setupContent() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void setupToolbar() {
		name = new TextField();
		name.setPlaceholder("name please?");
		name.setAriaLabel("Entity name");
		name.setMaxLength(CEntityDB.MAX_LENGTH_NAME);
		name.setMinWidth("20em");
		createBtn = new Button("Create", event -> createTask());
		createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		add(new ViewToolbar("Task List", ViewToolbar.group(name, createBtn)));
	}
}
