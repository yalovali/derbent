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
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.service.CDetailLinesService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CViewsService;
import tech.derbent.session.service.CSessionService;

@Route ("cdetailsectionview")
@PageTitle ("Detail Master View")
@Menu (order = 1.5, icon = "class:tech.derbent.screens.view.CDetailSectionView", title = "Setup.UI.Detail Sections")
@PermitAll
public final class CDetailSectionView extends CGridViewBaseProject<CDetailSection> {

	private static final long serialVersionUID = 1L;

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() {
		return CDetailSection.getIconColorCode(); // Use the static method from CScreen
	}

	public static String getIconFilename() { return CDetailSection.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "screen_id";
	private final CDetailLinesService screenLinesService;
	private final CEntityFieldService entityFieldService;
	private final CViewsService viewsService;

	public CDetailSectionView(final CDetailSectionService entityService, final CSessionService sessionService,
			final CDetailLinesService screenLinesService, final CEntityFieldService entityFieldService, final CViewsService viewsService,
			final CDetailSectionService screenService) {
		super(CDetailSection.class, entityService, sessionService, screenService);
		this.screenLinesService = screenLinesService;
		this.entityFieldService = entityFieldService;
		this.viewsService = viewsService;
	}

	@Override
	public void createGridForEntity(final CGrid<CDetailSection> grid) {
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
		grid.addDateTimeColumn(CEntityNamed::getCreatedDate, "Created", null);
		grid.addShortTextColumn(CDetailSection::getEntityType, "Entity Type", "entityType");
		grid.addShortTextColumn(CDetailSection::getScreenTitle, "Screen Title", "screenTitle");
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
		addAccordionPanel(new CPanelDetailSectionBasicInfo(getCurrentEntity(), getBinder(), (CDetailSectionService) entityService));
		addAccordionPanel(new CPanelDetailLines(getCurrentEntity(), getBinder(), (CDetailSectionService) entityService, screenLinesService,
				entityFieldService, viewsService));
		addAccordionPanel(new CPanelDetailSectionPreview(getCurrentEntity(), getBinder(), (CDetailSectionService) entityService));
	}
}
