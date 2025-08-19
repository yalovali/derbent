package tech.derbent.activities.view;

import java.lang.reflect.InvocationTargetException;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.domains.CInterfaceIconSet;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.abstracts.views.CVerticalLayout;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.service.CActivityStatusService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

/**
 * CActivityStatusView - View for managing activity statuses. Layer: View (MVC) Provides
 * CRUD operations for activity statuses using the abstract master-detail pattern. Allows
 * users to create, read, update, and delete activity status definitions.
 */
@Route ("cactivitystatusview/:activity_status_id?/:action?(edit)")
@PageTitle ("Activity Statuses")
@Menu (
	order = 2.1, icon = "class:tech.derbent.activities.view.CActivityStatusView",
	title = "Types.Activity Statuses"
)
@PermitAll
public class CActivityStatusView extends CProjectAwareMDPage<CActivityStatus>
	implements CInterfaceIconSet {

	private static final long serialVersionUID = 1L;

	public static String getIconColorCode() {
		return CActivityStatus.getIconColorCode(); // Use the static method from
													// CActivityStatus
	}

	public static String getIconFilename() { return CActivityStatus.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "activity_status_id";

	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "cactivitystatusview/%s/edit";

	/**
	 * Constructor for CActivityStatusView.
	 * @param entityService  the service for activity status operations
	 * @param sessionService
	 */
	public CActivityStatusView(final CActivityStatusService entityService,
		final CSessionService sessionService) {
		super(CActivityStatus.class, entityService, sessionService);
	}

	@Override
	public void createDetailsLayout() throws NoSuchMethodException, SecurityException,
		IllegalAccessException, InvocationTargetException {
		final CVerticalLayout detailsLayout =
			CEntityFormBuilder.buildForm(CActivityStatus.class, getBinder());
		getBaseDetailsLayout().add(detailsLayout);
	}

	/**
	 * Creates the grid for displaying activity status entities. Sets up columns for name
	 * and description with appropriate headers and sorting. Also includes a color-aware
	 * column to show the status colors. Now uses the refactored color-aware functionality
	 * from CGrid.addStatusColumn().
	 */
	@Override
	protected void createGridForEntity() {
		// Color-aware status column using refactored CGrid functionality
		grid.addStatusColumn(status -> status, "Status", "status");
		grid.addShortTextColumn(CActivityStatus::getName, "Status Name", "name");
		grid.addLongTextColumn(CActivityStatus::getDescription, "Description",
			"description");
		grid.addShortTextColumn(CActivityStatus::getColor, "Color Code", "color");
		grid.addBooleanColumn(CActivityStatus::getFinalStatus, "Is Final", "Final",
			"Not Final");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	public void setProjectForEntity(final CActivityStatus entity,
		final CProject project) {
		assert entity != null : "Entity must not be null";
		assert project != null : "Project must not be null";
		entity.setProject(project);
	}

	@Override
	protected void setupToolbar() {
		// Toolbar setup is handled by the parent class
	}

	/**
	 * Test method to expose populateForm for testing binding issues. This method should
	 * only be used in tests.
	 */
	@Override
	public void testPopulateForm(final CActivityStatus entity) {
		populateForm(entity);
	}
}