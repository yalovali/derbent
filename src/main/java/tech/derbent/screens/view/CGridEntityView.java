package tech.derbent.screens.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.abstracts.views.grids.CGridViewBaseProject;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.session.service.CSessionService;

@Route ("cgridentityview")
@PageTitle ("Grids Detail")
@Menu (order = 1.5, icon = "class:tech.derbent.screens.view.CGridEntityView", title = "UI.Grids")
@PermitAll
public class CGridEntityView extends CGridViewBaseProject<CGridEntity> {

	private static final long serialVersionUID = 1L;

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() {
		return CGridEntity.getIconColorCode(); // Use the static method from CScreen
	}

	public static String getIconFilename() { return CGridEntity.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "grid_entity_id";

	public CGridEntityView(final CGridEntityService entityService, final CSessionService sessionService, final CDetailSectionService screenService) {
		super(CGridEntity.class, entityService, sessionService, screenService);
	}

	@Override
	public void createGridForEntity(final CGrid<CGridEntity> grid) {
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }
}
