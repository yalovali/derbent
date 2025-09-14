package tech.derbent.gannt.view;

import java.util.Optional;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.abstracts.views.grids.CGridViewBaseProject;
import tech.derbent.gannt.domain.CGanntViewEntity;
import tech.derbent.gannt.service.CGanntViewEntityService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.session.service.CSessionService;

@Route ("cganntviewentityview")
@PageTitle ("Gannt Views Master Detail")
@Menu (order = 1.5, icon = "class:tech.derbent.gannt.view.CGanntViewEntityView", title = "Setup.UI.GanntViews")
@PermitAll
public class CGanntViewEntityView extends CGridViewBaseProject<CGanntViewEntity> {

	private static final long serialVersionUID = 1L;

	public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }

	public static String getStaticIconColorCode() {
		return CGanntViewEntity.getStaticIconColorCode(); // Use the static method from CScreen
	}

	public static String getStaticIconFilename() {
		return Optional.ofNullable(CGanntViewEntity.getStaticIconFilename()).orElseThrow(() -> new IllegalArgumentException("Value cannot be null"));
	}

	private final String ENTITY_ID_FIELD = "screen_id";

	protected CGanntViewEntityView(final CGanntViewEntityService entityService, final CSessionService sessionService,
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
