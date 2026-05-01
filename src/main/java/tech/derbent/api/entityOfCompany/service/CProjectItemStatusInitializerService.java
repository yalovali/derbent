package tech.derbent.api.entityOfCompany.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CEntityNamedInitializerService;
import tech.derbent.api.screens.service.CGridEntityService;

public class CProjectItemStatusInitializerService extends CStatusInitializerService {

	private static final Class<?> clazz = CProjectItemStatus.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectItemStatusInitializerService.class);
	private static final String menuOrder = Menu_Order_TYPES + ".2";
	private static final String menuTitle = MenuTitle_TYPES + "." + MenuTitle_TYPES_PROJECT + ".Statuses";
	private static final String pageDescription = "Manage status definitions for projects";
	private static final String pageTitle = "Status Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			CEntityNamedInitializerService.createScreenLines(scr, clazz, project, true);
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "color"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sortOrder"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "finalStatus"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "company"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating status view");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(
				List.of("id", "name", "description", "color", "sortOrder", "finalStatus", "active", "company"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService)
			throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid,
				menuTitle, pageTitle, pageDescription, showInQuickToolbar, menuOrder, null);
	}

	public static void initializeSample(final CCompany company, final boolean minimal) throws Exception {
		// Status data: [name, description, color, isFinalStatus, sortOrder]
		// Sort orders use increments of 10 so new statuses can be inserted without renumbering.
		// Agile item statuses (10-40): used by Epic/Feature/UserStory/Activity workflows.
		// Sprint-specific statuses (50-80): used only by the Sprint Workflow.
		// Terminal statuses (70-90): Done/Canceled/Cancelled end any workflow.
		final String[][] data = {
				{
						"To Do", "Work item is ready to be started — sits in the backlog/first Kanban column", "#95a5a6",
						"false", "10"
				}, {
						"In Progress", "Work item is actively being worked on", "#3498db", "false", "20"
				}, {
						"In Review", "Work item is under peer review or QA testing", "#9b59b6", "false", "30"
				}, {
						"Blocked", "Work item is blocked by an unresolved dependency or issue", "#e67e22", "false", "40"
				}, {
						// Sprint-specific: used by the Sprint Workflow (Planning → Started → Done/Canceled).
						"Planning", "Sprint is being prepared — backlog items can be added or removed", "#16a085",
						"false", "50"
				}, {
						"Started", "Sprint is active — team is executing committed backlog items", "#2980b9", "false",
						"60"
				}, {
						"Done", "Work item or sprint has been completed successfully", "#27ae60", "true", "70"
				}, {
						// Sprint-specific terminal status; kept separate from 'Cancelled' for sprint retrospective filtering.
						"Canceled", "Sprint was canceled before reaching its goal", "#c0392b", "true", "80"
				}, {
						"Cancelled", "Work item has been cancelled or abandoned", "#e74c3c", "true", "90"
				}
		};
		initializeCompanyEntity(data,
				(CEntityOfCompanyService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)),
				company, minimal, (item, index) -> {
					final CProjectItemStatus status = (CProjectItemStatus) item;
					status.setColor(data[index][2]);
					status.setFinalStatus(Boolean.parseBoolean(data[index][3]));
					status.setSortOrder(Integer.parseInt(data[index][4]));
				});
	}
}
