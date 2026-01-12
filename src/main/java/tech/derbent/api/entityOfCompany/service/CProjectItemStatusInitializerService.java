package tech.derbent.api.entityOfCompany.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;

public class CProjectItemStatusInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CProjectItemStatus.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectItemStatusInitializerService.class);
	private static final String menuOrder = Menu_Order_TYPES + ".2";
	private static final String menuTitle = MenuTitle_TYPES + ".Statuses";
	private static final String pageDescription = "Manage status definitions for projects";
	private static final String pageTitle = "Status Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "color"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sortOrder"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "finalStatus"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "company"));
			
			scr.addScreenLine(CDetailLinesService.createSection("Audit"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating status view");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "color", "sortOrder", "finalStatus", "active", "company"));
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final CCompany company, final boolean minimal) throws Exception {
		// Status data: [name, description, color, isFinalStatus, sortOrder]
		// Following standard Agile/Scrum terminology
		final String[][] data = {
				{
						"To Do", "Work item is ready to be started", "#95a5a6", "false", "1"
				}, {
						"In Progress", "Work item is actively being worked on", "#3498db", "false", "2"
				}, {
						"In Review", "Work item is being reviewed or tested", "#9b59b6", "false", "3"
				}, {
						"Blocked", "Work item is blocked by dependencies or issues", "#e67e22", "false", "4"
				}, {
						"Done", "Work item has been completed successfully", "#27ae60", "true", "5"
				}, {
						"Cancelled", "Work item has been cancelled or abandoned", "#e74c3c", "true", "6"
				}
		};
		initializeCompanyEntity(data, (CEntityOfCompanyService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), company,
				minimal, (item, index) -> {
					final CProjectItemStatus status = (CProjectItemStatus) item;
					status.setColor(data[index][2]);
					status.setFinalStatus(Boolean.parseBoolean(data[index][3]));
					status.setSortOrder(Integer.parseInt(data[index][4]));
				});
	}
}
