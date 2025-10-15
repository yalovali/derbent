package tech.derbent.gannt.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.views.grids.CGrid;
import tech.derbent.api.views.grids.CGridViewBaseGannt;
import tech.derbent.gannt.domain.CGanntViewEntity;
import tech.derbent.gannt.service.CGanntViewEntityService;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.session.service.ISessionService;

@Route ("cprojectganntview")
@PageTitle ("Project Gannt View")
@Menu (order = 1.101, icon = "class:tech.derbent.gannt.domain.CGanntViewEntity", title = "Project.Project Gannt Chart")
@PermitAll
public class CProjectGanntView extends CGridViewBaseGannt<CGanntViewEntity> {

	public static final String DEFAULT_COLOR = "#fd7e14";
	public static final String DEFAULT_ICON = "vaadin:timeline";
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "GanntEntity View";
	private final String ENTITY_ID_FIELD = "ganntview_id";

	protected CProjectGanntView(final CGanntViewEntityService entityService, final ISessionService sessionService,
			final CDetailSectionService screenService, final CActivityService activityService, final CMeetingService meetingService) {
		super(CGanntViewEntity.class, entityService, sessionService, screenService, activityService, meetingService);
		final CGanntViewEntity viewEntity =
				entityService.listByProject(sessionService.getActiveProject().orElse(null)).stream().findFirst().orElse(null);
		setCurrentEntity(viewEntity);
		// createDetailsLayout();
	}

	@Override
	public void createGridForEntity(final CGrid<CGanntViewEntity> grid) {
		LOGGER.debug("Creating grid for CGanntViewEntity");
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }
}
