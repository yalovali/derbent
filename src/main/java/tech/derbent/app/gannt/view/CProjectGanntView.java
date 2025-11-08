package tech.derbent.app.gannt.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.services.pageservice.CPageService;
import tech.derbent.api.views.grids.CGrid;
import tech.derbent.api.views.grids.CGridViewBaseGannt;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.gannt.domain.CGanntViewEntity;
import tech.derbent.app.gannt.service.CGanntViewEntityService;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.base.session.service.ISessionService;

@Route ("cprojectganntview")
@PageTitle ("Project Gannt View")
@Menu (order = 3.1001, icon = "class:tech.derbent.app.gannt.view.CProjectGanntView", title = "Project.Project Gannt Chart")
@PermitAll
public class CProjectGanntView extends CGridViewBaseGannt<CGanntViewEntity> {

	public static final String DEFAULT_COLOR = "#31701F";
	public static final String DEFAULT_ICON = "vaadin:progressbar";
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "GanntEntity View";
	private final String ENTITY_ID_FIELD = "ganntview_id";

	protected CProjectGanntView(final CGanntViewEntityService entityService, final ISessionService sessionService,
			final CDetailSectionService screenService, final CActivityService activityService, final CMeetingService meetingService,
			final tech.derbent.app.page.service.CPageEntityService pageEntityService) throws Exception {
		super(CGanntViewEntity.class, entityService, sessionService, screenService, activityService, meetingService, pageEntityService);
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
	public CEntityDB<?> createNewEntityInstance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	public CPageService<CGanntViewEntity> getPageService() { // TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISessionService getSessionService() { // TODO Auto-generated method stub
		return null;
	}

	@Override
	public void selectFirstInGrid() {
		// TODO Auto-generated method stub
	}

	@Override
	public void setCurrentEntity(CEntityDB<?> entity) {
		super.setCurrentEntity(entity);
	}
}
