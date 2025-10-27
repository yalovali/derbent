package tech.derbent.app.gannt.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.domains.CEntityOfProject;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.views.grids.CGrid;
import tech.derbent.api.views.grids.CGridViewBaseProject;
import tech.derbent.app.gannt.domain.CGanntViewEntity;
import tech.derbent.app.gannt.service.CGanntViewEntityService;
import tech.derbent.base.session.service.ISessionService;

@Route ("cganntviewentityview")
@PageTitle ("Gannt Views Master Detail")
@Menu (order = 1.5, icon = "class:tech.derbent.app.gannt.view.CGanntViewEntityView", title = "Project.Gannt Entity View")
@PermitAll
public class CGanntViewEntityView extends CGridViewBaseProject<CGanntViewEntity> {

	public static final String DEFAULT_COLOR = "#fd7e14";
	public static final String DEFAULT_ICON = "vaadin:chart-timeline";
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Gannt View Entity Settings View";
	private final String ENTITY_ID_FIELD = "screen_id";

	protected CGanntViewEntityView(final CGanntViewEntityService entityService, final ISessionService sessionService,
			final CDetailSectionService screenService) {
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
	protected String getEntityRouteIdField() { // TODO Auto-generated method stub
		return null;
	}
}
