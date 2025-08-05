package tech.derbent.projects.view;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CInterfaceIconSet;
import tech.derbent.abstracts.views.CAbstractNamedEntityPage;
import tech.derbent.abstracts.views.CAccordionDBEntity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserService;

/**
 * CProjectsView - View for managing projects. Layer: View (MVC) Provides CRUD operations
 * for projects using the abstract master-detail pattern.
 */
@Route ("projects/:project_id?/:action?(edit)")
@PageTitle ("Project Master Detail")
@Menu (
	order = 1.1, icon = "class:tech.derbent.projects.view.CProjectsView",
	title = "Settings.Projects"
)
@PermitAll // When security is enabled, allow all authenticated users
public class CProjectsView extends CAbstractNamedEntityPage<CProject>
	implements CInterfaceIconSet {

	private static final long serialVersionUID = 1L;

	public static String getIconColorCode() {
		return CProject.getIconColorCode(); // Use the static method from CProject
	}

	public static String getIconFilename() { return CProject.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "project_id";

	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "projects/%s/edit";

	private CPanelProjectUsers projectUsersPanel;

	private final CUserService userService;

	public CProjectsView(final CProjectService entityService,
		final CSessionService sessionService, final CUserService userService) {
		super(CProject.class, entityService, sessionService);
		this.userService = userService;
		addClassNames("projects-view");
		LOGGER.info("CProjectsView initialized successfully");
	}

	@Override
	protected void createDetailsLayout() {
		CAccordionDBEntity<CProject> panel;
		panel = new CPanelProjectBasicInfo(getCurrentEntity(), getBinder(),
			(CProjectService) entityService);
		addAccordionPanel(panel);
		// Add the project users panel for managing users in this project
		projectUsersPanel = new CPanelProjectUsers(getCurrentEntity(), getBinder(),
			(CProjectService) entityService, (CProjectService) entityService,
			userService);
		addAccordionPanel(projectUsersPanel);
	}

	@Override
	protected Div createDetailsTabLeftContent() {
		// Create custom tab content for projects view
		final Div detailsTabLabel = new Div();
		detailsTabLabel.setText("Project Information");
		detailsTabLabel.setClassName("details-tab-label");
		return detailsTabLabel;
	}

	@Override
	protected void createGridForEntity() {
		grid.addShortTextColumn(CProject::getName, "Name", "name");
		grid.addColumn(CProject::getDescription, "Description", "description");
		grid.addColumn(CProject::getCreatedDate, "Created Date", "createdDate");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	protected void initPage() {
		// Initialize the page components and layout This method can be overridden to set
		// up the view's components
	}

	@Override
	protected void populateForm(final CProject value) {
		super.populateForm(value);
		LOGGER.info("Populating form with project data: {}",
			value != null ? value.getName() : "null");

		if (value != null) {
			// Load project with user settings to avoid lazy initialization issues
			final CProject projectWithUsers =
				((CProjectService) entityService).findByIdWithUserSettings(value.getId());
			projectUsersPanel.setCurrentProject(projectWithUsers);
			// supply the project settings from eager loaded project
			final Supplier<List<CUserProjectSettings>> supplier =
				() -> projectWithUsers.getUserSettings();
			// what to do when user settings are updated
			final Consumer<List<CUserProjectSettings>> consumer = users -> {
				projectWithUsers.getUserSettings().clear();
				projectWithUsers.getUserSettings().addAll(users);
				// Save the project when user settings are updated
				entityService.save(projectWithUsers);
			};
			final Runnable runnable = () -> {
				final CProject refreshedProject = ((CProjectService) entityService)
					.findByIdWithUserSettings(projectWithUsers.getId());
				populateForm(refreshedProject);
			};
			//
			projectUsersPanel.setProjectUsersAccessors(supplier, consumer, runnable);
		}
		else {
			projectUsersPanel.setCurrentProject(null);
			final Supplier<List<CUserProjectSettings>> supplier =
				() -> Collections.emptyList();
			final Consumer<List<CUserProjectSettings>> consumer = users -> {
				// Do nothing
			};
			final Runnable runnable = () -> {
				// Do nothing
			};
			projectUsersPanel.setProjectUsersAccessors(supplier, consumer, runnable);
		}
	}

	@Override
	protected void setupToolbar() {
		// TODO: Implement toolbar setup if needed
	}
}