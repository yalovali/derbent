package tech.derbent.activities.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.abstracts.views.grids.CGridViewBaseProject;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.activities.service.CActivityTypeService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.session.service.CSessionService;

/** CActivityTypeView - View for managing activity types. Layer: View (MVC) Provides CRUD operations for activity types using the abstract
 * master-detail pattern with project awareness. */
@Route ("cactivitytypeview")
@PageTitle ("Activity Types")
@Menu (order = 10.4, icon = "class:tech.derbent.activities.view.CActivityTypeView", title = "Types.Activity Types")
@PermitAll
public class CActivityTypeView extends CGridViewBaseProject<CActivityType> {

	private static final long serialVersionUID = 1L;

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() { return CActivityType.getIconColorCode(); }

	public static String getIconFilename() { return CActivityType.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "activity_type_id";

	public CActivityTypeView(final CActivityTypeService entityService, final CSessionService sessionService, final CDetailSectionService screenService) {
		super(CActivityType.class, entityService, sessionService, screenService);
	}

	@Override
	public void createGridForEntity(final CGrid<CActivityType> grid) {
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
		grid.addStatusColumn(type -> type, "Type", "type");
		grid.addShortTextColumn(CActivityType::getColor, "Color", "color");
		grid.addBooleanColumn(CEntityNamed::getIsActive, "Active", "Active", "Inactive");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }
}
