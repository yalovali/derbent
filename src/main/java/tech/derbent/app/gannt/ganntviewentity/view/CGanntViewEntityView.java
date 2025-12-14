package tech.derbent.app.gannt.ganntviewentity.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.grid.view.CGridViewBaseProject;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.services.pageservice.CPageService;
import tech.derbent.api.views.CDetailsBuilder;
import tech.derbent.app.gannt.ganntviewentity.domain.CGanntViewEntity;
import tech.derbent.app.gannt.ganntviewentity.service.CGanntViewEntityService;
import tech.derbent.base.session.service.ISessionService;

@Route ("cganntviewentityview")
@PageTitle ("Gannt Views Master Detail")
@Menu (order = 1.5, icon = "class:tech.derbent.app.gannt.ganntviewentity.view.CGanntViewEntityView", title = "Project.Gannt Entity View")
@PermitAll
public class CGanntViewEntityView extends CGridViewBaseProject<CGanntViewEntity> {

	public static final String DEFAULT_COLOR = "#4B4382"; // CDE Titlebar Purple - gantt chart view
	public static final String DEFAULT_ICON = "vaadin:chart-timeline";
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Gannt View Entity Settings View";
	private final String ENTITY_ID_FIELD = "screen_id";

	protected CGanntViewEntityView(final CGanntViewEntityService entityService, final ISessionService sessionService,
			final CDetailSectionService screenService) throws Exception {
		super(CGanntViewEntity.class, entityService, sessionService, screenService);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createGridForEntity(final CGrid<CGanntViewEntity> grid) {
		LOGGER.debug("Creating grid for CGanntViewEntity");
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
	}

	@Override
	public CEntityDB<?> createNewEntityInstance() throws Exception {
		
		return null;
	}

	@Override
	protected String getEntityRouteIdField() { 
		return null;
	}

	@Override
	public CPageService<CGanntViewEntity> getPageService() { 
		return null;
	}

	@Override
	public ISessionService getSessionService() { 
		return null;
	}

	@Override
	public void selectFirstInGrid() {
		
	}

	@Override
	public void setCurrentEntity(CEntityDB<?> entity) {
		super.setCurrentEntity(entity);
	}

	@Override
	public CDetailsBuilder getDetailsBuilder() { 
	return null; }
}
