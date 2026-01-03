package tech.derbent.app.kanban.kanbanline.service;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanColumn;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;

public class CKanbanLineInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CKanbanLine.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CKanbanLineInitializerService.class);
	private static final String menuOrder = Menu_Order_SETUP + ".90";
	private static final String menuTitle = MenuTitle_SETUP + ".Kanban Lines";
	private static final String pageDescription = "Kanban line definitions and their columns";
	private static final String pageTitle = "Kanban Lines";
	private static final boolean showInQuickToolbar = true;

	/** Builds the standard detail view for kanban lines. */
	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "company"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "kanbanColumns"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Kanban Board"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "kanbanBoard"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating Kanban line view.");
			throw e;
		}
	}

	/** Creates a kanban column with specific statuses by name. This ensures each status is only assigned to one column, preventing the status overlap
	 * validation error. Statuses are looked up by name to ensure predictable kanban board configurations that match common agile workflows.
	 * @param name          Column display name
	 * @param company       Company context for status lookup
	 * @param statusService Service for finding statuses
	 * @param line          Parent kanban line
	 * @param statusNames   Array of status names to include in this column
	 * @return Created column with assigned statuses */
	private static CKanbanColumn createColumn(final String name, final CCompany company, final CProjectItemStatusService statusService,
			final CKanbanLine line, final String... statusNames) {
		final CKanbanColumn item = new CKanbanColumn(name, line);
		item.setColor(CColorUtils.getRandomFromWebColors(false));
		// Look up statuses by name to ensure predictable, non-overlapping assignments
		final List<CProjectItemStatus> statuses = new java.util.ArrayList<>();
		for (final String statusName : statusNames) {
			try {
				final Optional<CProjectItemStatus> statusOpt = statusService.findByNameAndCompany(statusName, company);
				if (statusOpt.isPresent()) {
					final CProjectItemStatus status = statusOpt.get();
					statuses.add(status);
					// LOGGER.debug("[KanbanInit] Assigning status '{}' (ID: {}) to column '{}'", statusName, status.getId(), name);
				} else {
					LOGGER.warn("[KanbanInit] Status '{}' not found for column '{}', skipping", statusName, name);
				}
			} catch (final Exception e) {
				LOGGER.error("[KanbanInit] Error looking up status '{}': {}", statusName, e.getMessage());
			}
		}
		if (!statuses.isEmpty()) {
			item.setIncludedStatuses(statuses);
		} else {
			LOGGER.warn("[KanbanInit] Column '{}' has no valid statuses assigned", name);
		}
		line.addKanbanColumn(item);
		return item;
	}

	/** Builds the grid configuration for kanban line list views. */
	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "company", "active"));
		return grid;
	}

	/** Builds the board-focused detail view for kanban lines. */
	private static CDetailSection createKanbanView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			detailSection.addScreenLine(CDetailLinesService.createSection("Kanban Board"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "kanbanBoard"));
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "company"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "kanbanColumns"));
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating Kanban line view.");
			throw e;
		}
	}

	/** Registers kanban line pages and grids for a project. */
	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
		final CDetailSection kanbanDetailSection = createKanbanView(project);
		final CGridEntity kanbanGrid = createGridEntity(project);
		kanbanDetailSection.setName("Kanban Board Section");
		kanbanGrid.setName("Kanban Board Grid");
		kanbanGrid.setAttributeNone(true);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, kanbanDetailSection, kanbanGrid,
				menuTitle + " (Kanban Board View)", pageTitle + " (Kanban Board View)", pageDescription + " (Kanban Board View)", true,
				menuOrder + ".1");
	}

	/** Seeds default kanban lines and columns for a company. Creates two example kanban boards following standard Agile/Scrum methodology: 1. "Scrum
	 * Board" - Standard Scrum workflow with 5 columns 2. "Simple Kanban" - Basic 3-column Kanban workflow Each status is assigned to exactly ONE
	 * column to prevent status overlap validation errors. The column-to-status mapping follows Agile best practices: - Backlog/To Do: Items ready to
	 * start - In Progress: Active work in development - Review/Testing: Work being reviewed or tested - Blocked: Work waiting on dependencies - Done:
	 * Completed work (default column for unmapped statuses) IMPORTANT: All columns MUST have at least one status mapped to prevent display issues. */
	public static void initializeSample(final CCompany company, final boolean minimal) throws Exception {
		final String[][] sampleLines = {
				{
						"Scrum Board", "Standard Scrum workflow: Backlog → In Progress → In Review → Blocked → Done"
				}, {
						"Simple Kanban", "Basic Kanban workflow: To Do → Doing → Done"
				}
		};
		final CProjectItemStatusService statusService =
				(CProjectItemStatusService) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(CProjectItemStatus.class));
		LOGGER.info("[KanbanInit] Initializing sample kanban lines for company '{}' (ID: {})", company.getName(), company.getId());
		initializeCompanyEntity(sampleLines, (CEntityOfCompanyService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)),
				company, minimal, (entity, index) -> {
					Check.instanceOf(entity, CKanbanLine.class, "Expected Kanban line for column initialization");
					final CKanbanLine line = (CKanbanLine) entity;
					if (index == 0) {
						// Scrum Board: 5-column standard Scrum workflow
						// Each status is assigned to EXACTLY ONE column to prevent overlap
						// LOGGER.info("[KanbanInit] Creating Scrum Board with 5 columns");
						// createColumn("Backlog", company, statusService, line, "To Do");
						createColumn("In Progress", company, statusService, line, "In Progress");
						createColumn("In Review", company, statusService, line, "In Review");
						createColumn("Blocked", company, statusService, line, "Blocked");
						createColumn("Done", company, statusService, line, "Done", "Cancelled").setDefaultColumn(true);
					} else {
						// Simple Kanban: 3-column simplified workflow
						// Groups multiple statuses into broader categories
						// LOGGER.info("[KanbanInit] Creating Simple Kanban with 3 columns");
						createColumn("To Do", company, statusService, line, "To Do", "Blocked");
						createColumn("Doing", company, statusService, line, "In Progress", "In Review");
						createColumn("Done", company, statusService, line, "Done", "Cancelled").setDefaultColumn(true);
					}
					// Validate that no columns have empty status lists
					for (final CKanbanColumn column : line.getKanbanColumns()) {
						if (column.getIncludedStatuses() == null || column.getIncludedStatuses().isEmpty()) {
							LOGGER.error("[KanbanInit] VALIDATION ERROR: Column '{}' in line '{}' has NO statuses mapped!", column.getName(),
									line.getName());
							throw new IllegalStateException("Kanban column '" + column.getName()
									+ "' must have at least one status mapped. Empty status lists cause display issues.");
						} else {
							LOGGER.debug("[KanbanInit] Column '{}' has {} status(es) mapped: {}", column.getName(),
									column.getIncludedStatuses().size(),
									column.getIncludedStatuses().stream().map(s -> s.getName()).collect(java.util.stream.Collectors.joining(", ")));
						}
					}
					LOGGER.info("[KanbanInit] Completed initialization of kanban line '{}' with {} columns", line.getName(),
							line.getKanbanColumns().size());
				});
	}
}
