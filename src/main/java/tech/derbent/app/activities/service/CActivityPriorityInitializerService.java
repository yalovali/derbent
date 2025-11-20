package tech.derbent.app.activities.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.domain.CActivityPriority;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;

public class CActivityPriorityInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CActivityPriority.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CActivityPriorityInitializerService.class);
	private static final String menuOrder = Menu_Order_TYPES + ".3";
	private static final String menuTitle = MenuTitle_TYPES + ".Activity Priorities";
	private static final String pageDescription = "Manage activity priority definitions for projects";
	private static final String pageTitle = "Activity Priority Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) {
		Check.notNull(project, "Project cannot be null");
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			scr.addScreenLine(CDetailLinesService.createSection("Display Configuration"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "color"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sortOrder"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "priorityLevel"));
			scr.addScreenLine(CDetailLinesService.createSection("Behavior"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isDefault"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "attributeNonDeletable"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			scr.addScreenLine(CDetailLinesService.createSection("Audit"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			final String errorMsg = "Error creating activity priority view: " + e.getMessage();
			LOGGER.error(errorMsg, e);
			throw new CInitializationException(errorMsg, e);
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "priorityLevel", "isDefault", "color", "sortOrder", "active", "project"));
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		Check.notNull(project, "project cannot be null");
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
		final String[][] nameAndDescriptions = {
				{
						"Critical", "Critical priority - immediate attention required"
				}, {
						"High", "High priority - urgent attention needed"
				}, {
						"Medium", "Medium priority - normal workflow"
				}, {
						"Low", "Low priority - can be scheduled later"
				}, {
						"Lowest", "Lowest priority - no immediate action needed"
				}
		};
		initializeProjectEntity(nameAndDescriptions,
				(CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project, minimal, null);
	}
}
