package tech.derbent.app.activities.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.entityOfProject.domain.CProjectItemStatus;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;

public class CProjectItemStatusInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Status Information";
	private static final Class<?> clazz = CProjectItemStatus.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectItemStatusInitializerService.class);
	private static final String menuOrder = Menu_Order_TYPES + ".2";
	private static final String menuTitle = MenuTitle_TYPES + ".Statuses";
	private static final String pageDescription = "Manage status definitions for projects";
	private static final String pageTitle = "Status Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			detailSection.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "id"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "color"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sortOrder"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "finalStatus"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating status view");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "color", "sortOrder", "finalStatus", "active", "project"));
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
		final String[][] data = {
				{
						"Not Started", "Activity has not been started yet", "#95a5a6", "false", "1"
				}, {
						"In Progress", "Activity is currently in progress", "#3498db", "false", "2"
				}, {
						"On Hold", "Activity is temporarily on hold", "#f39c12", "false", "3"
				}, {
						"Completed", "Activity has been completed", "#27ae60", "true", "4"
				}, {
						"Cancelled", "Activity has been cancelled", "#e74c3c", "true", "5"
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
