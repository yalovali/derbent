package tech.derbent.app.sprints.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.utils.Check;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.sprints.domain.CSprintType;

public class CSprintTypeInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CSprintType.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CSprintTypeInitializerService.class);
	private static final String menuOrder = Menu_Order_TYPES + ".8";
	private static final String menuTitle = MenuTitle_TYPES + ".Sprint Types";
	private static final String pageDescription = "Manage sprint type categories";
	private static final String pageTitle = "Sprint Type Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		Check.notNull(project, "project cannot be null");
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "workflow"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Display Configuration"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "color"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sortOrder"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "attributeNonDeletable"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
   
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
   
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating sprint type view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "color", "sortOrder", "active", "project"));
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
		final String[][] nameAndDescriptions = {
				{
						"Planning Sprint", "Sprint planning and preparation phase"
				}, {
						"Development Sprint", "Active development and implementation sprint"
				}, {
						"Testing Sprint", "Quality assurance and testing focused sprint"
				}, {
						"Release Sprint", "Final preparations and release activities"
				}, {
						"Hardening Sprint", "Bug fixing and system stabilization sprint"
				}
		};
		initializeProjectEntity(nameAndDescriptions,
				(CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project, minimal, null);
	}
}
