package tech.derbent.activities.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.domains.IDisplayEntity;
import tech.derbent.abstracts.views.components.CVerticalLayout;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.abstracts.views.grids.CGridViewBaseProject;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.service.CActivityStatusService;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

/** CActivityStatusView - View for managing activity statuses. Layer: View (MVC) Provides CRUD operations for activity statuses using the abstract
 * master-detail pattern. Allows users to create, read, update, and delete activity status definitions. */
@Route ("cactivitystatusview")
@PageTitle ("Activity Statuses")
@Menu (order = 2.1, icon = "class:tech.derbent.activities.view.CActivityStatusView", title = "Types.Activity Statuses")
@PermitAll
public class CActivityStatusView extends CGridViewBaseProject<CActivityStatus> {

	private static final long serialVersionUID = 1L;

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() {
		return CActivityStatus.getIconColorCode(); // Use the static method from
													// CActivityStatus
	}

	public static String getIconFilename() { return IDisplayEntity.resolveIconFilename(CActivityStatus.class); }

	private final String ENTITY_ID_FIELD = "activity_status_id";

	public CActivityStatusView(final CActivityStatusService entityService, final CSessionService sessionService, final CScreenService screenService) {
		super(CActivityStatus.class, entityService, sessionService, screenService);
	}

	@Override
	protected void createDetailsComponent() throws Exception {
		// TODO Auto-generated method stub
	}

	/** Creates the grid for displaying activity status entities. Sets up columns for name and description with appropriate headers and sorting. Also
	 * includes a color-aware column to show the status colors. Now uses the refactored color-aware functionality from CGrid.addStatusColumn(). */
	@Override
	public void createGridForEntity(final CGrid<CActivityStatus> grid) {
		// Color-aware status column using refactored CGrid functionality
		grid.addStatusColumn(status -> status, "Status", "status");
		grid.addShortTextColumn(CActivityStatus::getName, "Status Name", "name");
		grid.addLongTextColumn(CActivityStatus::getDescription, "Description", "description");
		grid.addShortTextColumn(CActivityStatus::getColor, "Color Code", "color");
		grid.addBooleanColumn(CActivityStatus::getFinalStatus, "Is Final", "Final", "Not Final");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	public void updateDetailsComponent() throws Exception {
		final CVerticalLayout detailsLayout = CEntityFormBuilder.buildForm(CActivityStatus.class, getBinder());
		getBaseDetailsLayout().add(detailsLayout);
	}
}
