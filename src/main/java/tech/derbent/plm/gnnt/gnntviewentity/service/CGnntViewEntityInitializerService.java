package tech.derbent.plm.gnnt.gnntviewentity.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CEntityOfProjectInitializerService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntViewEntity;
import tech.derbent.plm.gnnt.gnntviewentity.domain.EGnntGridType;

public class CGnntViewEntityInitializerService extends CEntityOfProjectInitializerService {

	public static final String BOARD_PAGE_NAME = "Gannt Board View";
	public static final String BOARD_PAGE_TITLE = "Gannt Board";
	private static final Class<?> clazz = CGnntViewEntity.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CGnntViewEntityInitializerService.class);
	private static final String MENU_ORDER = Menu_Order_PROJECT + ".90";
	private static final String MENU_TITLE = MenuTitle_PROJECT + "." + CGnntViewEntity.ENTITY_TITLE_PLURAL;
	private static final String PAGE_DESCRIPTION = "Project Gannt view definitions and dedicated timeline boards";
	private static final String PAGE_TITLE = CGnntViewEntity.ENTITY_TITLE_PLURAL;
	private static final boolean SHOW_IN_QUICK_TOOLBAR = true;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection detailSection = CEntityOfProjectInitializerService.createBasicView(project, clazz);
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "gridType"));
		detailSection.addScreenLine(CDetailLinesService.createSection("Gnnt Board"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "gnntBoard"));
		return detailSection;
	}

	private static CDetailSection createGnntBoardView(final CProject<?> project) throws Exception {
		final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
		detailSection.addScreenLine(CDetailLinesService.createSection("Gnnt Board"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "gnntBoard"));
		return detailSection;
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "gridType", "description", "project", "active"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService)
			throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid,
				MENU_TITLE, PAGE_TITLE, PAGE_DESCRIPTION, SHOW_IN_QUICK_TOOLBAR, MENU_ORDER, null);
		final CDetailSection boardSection = createGnntBoardView(project);
		final CGridEntity boardGrid = createGridEntity(project);
		boardSection.setName(CGnntViewEntity.ENTITY_TITLE_SINGULAR);
		boardGrid.setName(BOARD_PAGE_NAME);
		// hide grid-level controls for the board page since it has only one item and they don't make sense in that context
		boardGrid.setAttributeNone(true);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, boardSection, boardGrid,
				MenuTitle_PROJECT + "." + CGnntViewEntity.ENTITY_TITLE_SINGULAR, BOARD_PAGE_TITLE,
				"Dedicated Gnnt board page", true, MENU_ORDER + ".1", null);
	}

	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		final String[][] sampleViews = {
				{
						"Delivery Timeline",
						"Project-wide Gnnt board focused on the agile delivery hierarchy and current iteration flow."
				}, {
						"Release Roadmap",
						"Second Gnnt board for roadmap-style review across epics, features, stories, and execution items."
				}
		};
		final CGnntViewEntityService service = CSpringContext.getBean(CGnntViewEntityService.class);
		initializeProjectEntity(sampleViews, service, project, minimal, null);
		service.listByProject(project).forEach((final CGnntViewEntity gnntViewEntity) -> {
			gnntViewEntity.setGridType(EGnntGridType.TREE);
			service.save(gnntViewEntity);
		});
		LOGGER.debug("Initialized sample Gnnt views for project {}", project.getName());
	}
}
