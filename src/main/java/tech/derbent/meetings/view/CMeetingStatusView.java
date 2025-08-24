package tech.derbent.meetings.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CSpringAuxillaries;
import tech.derbent.abstracts.components.CGridCell;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.domains.CInterfaceIconSet;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.meetings.service.CMeetingStatusService;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

/** CMeetingStatusView - View for managing meeting statuses. Layer: View (MVC) Provides CRUD operations for meeting statuses using the abstract
 * master-detail pattern. Manages different status types that meetings can have throughout their lifecycle. */
@Route ("cmeetingstatusview/:meeting_status_id?/:action?(edit)")
@PageTitle ("Meeting Statuses")
@Menu (order = 11.3, icon = "class:tech.derbent.meetings.view.CMeetingStatusView", title = "Types.Meeting Statuses")
@PermitAll
public class CMeetingStatusView extends CProjectAwareMDPage<CMeetingStatus> implements CInterfaceIconSet {
	private static final long serialVersionUID = 1L;

	public static String getIconColorCode() {
		return CMeetingStatus.getIconColorCode(); // Use the static method from
													// CMeetingStatus
	}

	public static String getIconFilename() { return CMeetingStatus.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "meeting_status_id";
	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "cmeetingstatusview/%s/edit";

	/** Constructor for CMeetingStatusView.
	 * @param entityService  the service for meeting status operations
	 * @param sessionService */
	public CMeetingStatusView(final CMeetingStatusService entityService, final CSessionService sessionService, final CScreenService screenService) {
		super(CMeetingStatus.class, entityService, sessionService, screenService);
		addClassNames("meeting-statuses-view");
		LOGGER.info("CMeetingStatusView initialized with route: " + CSpringAuxillaries.getRoutePath(this.getClass()));
	}

	@Override
	protected void createGridForEntity() {
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
		grid.addDateTimeColumn(CEntityNamed::getCreatedDate, "Created", null);
		grid.addStatusColumn(status -> status, "Status", "status");
		grid.addShortTextColumn(entity -> entity.getColor(), "Color", "color");
		grid.addComponentColumn(entity -> {
			final CGridCell statusCell = new CGridCell();
			statusCell.setFinalActiveValue(entity.getFinalStatus());
			return statusCell;
		}).setHeader("Type").setWidth("100px").setFlexGrow(0);
		grid.addShortTextColumn(entity -> String.valueOf(entity.getSortOrder()), "Order", "sortOrder");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	protected void setupToolbar() {
		// Toolbar setup is handled by the parent class
	}
}
