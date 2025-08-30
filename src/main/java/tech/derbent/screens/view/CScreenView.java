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
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CScreenLinesService;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.screens.service.CViewsService;
import tech.derbent.session.service.CSessionService;

@Route ("cscreensview/:screen_id?/:action?(edit)")
@PageTitle ("Screen Master Detail")
@Menu (order = 1.5, icon = "class:tech.derbent.screens.view.CScreenView", title = "Project.Screens")
@PermitAll
public final class CScreenView extends CGridViewBaseProject<CScreen> {
	private static final long serialVersionUID = 1L;

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() {
		return CScreen.getIconColorCode(); // Use the static method from CScreen
	}

	public static String getIconFilename() { return CScreen.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "screen_id";
	private final CScreenLinesService screenLinesService;
	private final CEntityFieldService entityFieldService;
	private final CViewsService viewsService;

	public CScreenView(final CScreenService entityService, final CSessionService sessionService, final CScreenLinesService screenLinesService,
			final CEntityFieldService entityFieldService, final CViewsService viewsService, final CScreenService screenService) {
		super(CScreen.class, entityService, sessionService, screenService);
		this.screenLinesService = screenLinesService;
		this.entityFieldService = entityFieldService;
		this.viewsService = viewsService;
	}

	@Override
	public void createGridForEntity(final CGrid<CScreen> grid) {
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
		grid.addDateTimeColumn(CEntityNamed::getCreatedDate, "Created", null);
		grid.addShortTextColumn(CScreen::getEntityType, "Entity Type", "entityType");
		grid.addShortTextColumn(CScreen::getScreenTitle, "Screen Title", "screenTitle");
		grid.addColumn(screen -> screen.getIsActive() ? "Active" : "Inactive", "Status", null);
		grid.addColumn(screen -> {
			try {
				return String.valueOf(screenLinesService.countByScreen(screen));
			} catch (final Exception e) {
				return "0";
			}
		}, "Lines Count", null);
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected void updateDetailsComponent() throws Exception {
		addAccordionPanel(new CPanelScreenBasicInfo(getCurrentEntity(), getBinder(), (CScreenService) entityService));
		addAccordionPanel(new CPanelScreenLines(getCurrentEntity(), getBinder(), (CScreenService) entityService, screenLinesService,
				entityFieldService, viewsService));
		addAccordionPanel(new CPanelScreenPreview(getCurrentEntity(), getBinder(), (CScreenService) entityService));
	}
}
