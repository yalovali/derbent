package tech.derbent.api.screens.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entity.service.CPageServiceEntityDB;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.grid.view.CGridViewBaseProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.services.pageservice.CPageService;
import tech.derbent.api.views.CDetailsBuilder;
import tech.derbent.base.session.service.ISessionService;

@Route ("cdetailsectionview")
@PageTitle ("Detail Master View")
@Menu (order = 1.5, icon = "class:tech.derbent.api.screens.view.CDetailSectionView", title = "Setup.UI.Detail Sections")
@PermitAll
public final class CDetailSectionView extends CGridViewBaseProject<CDetailSection> {
	public static final String DEFAULT_COLOR = "#808000"; // X11 Olive - sections (darker)
	public static final String DEFAULT_ICON = "vaadin:clipboard";
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Detail Section View";
	private final String ENTITY_ID_FIELD = "screen_id";
	private final CPageService<CDetailSection> pageService;
	private final CDetailLinesService screenLinesService;

	public CDetailSectionView(final CDetailSectionService entityService, final ISessionService sessionService,
			final CDetailLinesService screenLinesService, final CDetailSectionService screenService) throws Exception {
		super(CDetailSection.class, entityService, sessionService, screenService);
		this.screenLinesService = screenLinesService;
		pageService = new CPageServiceEntityDB<CDetailSection>(this);
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
		grid.addColumn(screen -> screen.getActive() ? "Active" : "Inactive", "Status", null);
		grid.addColumn(screen -> {
			try {
				return String.valueOf(getScreenLinesService().countByMaster(screen));
			} catch (final Exception e) {
				return "0";
			}
		}, "Lines Count", null);
	}

	@Override
	public CEntityDB<?> createNewEntityInstance() throws Exception {
		return null;
	}

	@Override
	public CDetailsBuilder getDetailsBuilder() { return null; }

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	public CPageService<CDetailSection> getPageService() { return pageService; }

	public CDetailLinesService getScreenLinesService() { return screenLinesService; }

	@Override
	public ISessionService getSessionService() { return sessionService; }

	@Override
	public void selectFirstInGrid() {
		/***/
	}

	@Override
	public void setValue(final CEntityDB<?> entity) {
		super.setValue(entity);
	}

	@Override
	protected void updateDetailsComponent() throws Exception {
		addAccordionPanel(new CPanelDetailSectionBasicInfo(this, getBinder(), (CDetailSectionService) getEntityService()));
		addAccordionPanel(new CPanelDetailLines(this, getBinder(), (CDetailSectionService) getEntityService(), getScreenLinesService()));
		addAccordionPanel(new CPanelDetailSectionPreview(this, getBinder(), (CDetailSectionService) getEntityService(), getSessionService()));
	}
}
