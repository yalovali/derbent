package tech.derbent.screens.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.domains.CEntityOfProject;
import tech.derbent.api.views.grids.CGrid;
import tech.derbent.api.views.grids.CGridViewBaseProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.service.CDetailLinesService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CViewsService;
import tech.derbent.session.service.ISessionService;

@Route ("cdetailsectionview")
@PageTitle ("Detail Master View")
@Menu (order = 1.5, icon = "class:tech.derbent.screens.view.CDetailSectionView", title = "Setup.UI.Detail Sections")
@PermitAll
public final class CDetailSectionView extends CGridViewBaseProject<CDetailSection> {

	public static final String DEFAULT_COLOR = "#00141b";
	public static final String DEFAULT_ICON = "vaadin:clipboard";
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Detail Section View";
	private final String ENTITY_ID_FIELD = "screen_id";
	private final CEntityFieldService entityFieldService;
	private final CDetailLinesService screenLinesService;
	private final CViewsService viewsService;

	public CDetailSectionView(final CDetailSectionService entityService, final ISessionService sessionService,
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
		addAccordionPanel(new CPanelDetailSectionBasicInfo(this, getCurrentEntity(), getBinder(), (CDetailSectionService) entityService));
		addAccordionPanel(new CPanelDetailLines(this, getCurrentEntity(), getBinder(), (CDetailSectionService) entityService, screenLinesService,
				entityFieldService, viewsService));
		addAccordionPanel(new CPanelDetailSectionPreview(this, getCurrentEntity(), getBinder(), (CDetailSectionService) entityService));
	}
}
