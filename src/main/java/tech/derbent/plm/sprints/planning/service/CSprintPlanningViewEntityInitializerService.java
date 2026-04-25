package tech.derbent.plm.sprints.planning.service;

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
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.plm.sprints.planning.domain.CSprintPlanningViewEntity;

/**
 * Initializer for the new sprint planning board view entity.
 *
 * <p>We create two pages, mirroring the Gnnt board approach:
 * 1) a normal CRUD page, and
 * 2) a dedicated board page where the grid chrome is hidden.</p>
 */
public class CSprintPlanningViewEntityInitializerService extends CInitializerServiceBase {

	public static final String BOARD_PAGE_NAME = "Sprint Planning Board";
	public static final String BOARD_PAGE_TITLE = "Sprint Planning";

	private static final Class<?> clazz = CSprintPlanningViewEntity.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CSprintPlanningViewEntityInitializerService.class);
	private static final String MENU_ORDER = Menu_Order_DEVELOPMENT + "31";
	private static final String MENU_TITLE = MenuTitle_DEVELOPMENT + ".Sprint Planning (v2)";
	private static final String PAGE_DESCRIPTION = "Timeline + drag/drop sprint planning board (new v2 implementation)";
	private static final String PAGE_TITLE = "Sprint Planning Views";
	private static final boolean SHOW_IN_QUICK_TOOLBAR = true;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
		CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "backlogGridType"));
		detailSection.addScreenLine(CDetailLinesService.createSection("Sprint Planning Board"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sprintPlanningBoard"));
		return detailSection;
	}

	private static CDetailSection createBoardView(final CProject<?> project) throws Exception {
		final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
		detailSection.addScreenLine(CDetailLinesService.createSection("Sprint Planning Board"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sprintPlanningBoard"));
		return detailSection;
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "backlogGridType", "description", "project", "active"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, MENU_TITLE, PAGE_TITLE,
				PAGE_DESCRIPTION, SHOW_IN_QUICK_TOOLBAR, MENU_ORDER);

		final CDetailSection boardSection = createBoardView(project);
		final CGridEntity boardGrid = createGridEntity(project);
		boardSection.setName(CSprintPlanningViewEntity.ENTITY_TITLE_SINGULAR);
		boardGrid.setName(BOARD_PAGE_NAME);
		// Dedicated board page does not need grid chrome; it is a single-entity experience.
		boardGrid.setAttributeNone(true);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, boardSection, boardGrid,
				MENU_TITLE + ".Board", BOARD_PAGE_TITLE, "Dedicated sprint planning board page", true, MENU_ORDER + ".1");
	}

	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		final String[][] sampleViews = {
				{
						"Default Sprint Planning", "Day-to-day sprint planning board with backlog + sprint timelines (drag/drop + dialog)."
				},
				{
						"Release Planning", "High-level planning view for mapping Features/Epics into upcoming sprints (leaf-only rule enforced)."
				},
				{
						"Hotfix Planning", "Fast triage view for moving leaf issues between active sprints during production incidents."
				}
		};
		final CSprintPlanningViewEntityService service = CSpringContext.getBean(CSprintPlanningViewEntityService.class);
		initializeProjectEntity(sampleViews, service, project, minimal, null);
		LOGGER.debug("Initialized sample sprint planning views for project {}", project.getName());
	}
}
