package tech.derbent.gannt.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.gannt.domain.CGanntViewEntity;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailLinesService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.screens.service.CInitializerServiceBase;

/* NOT USED YET - FOR FUTURE DEVELOPMENT */
public class CGanntInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Gantt View Information";
	private static final Logger LOGGER = LoggerFactory.getLogger(CGanntInitializerService.class);
	private static final Class<?> clazz = CGanntViewEntity.class;
	private static final String menuTitle = "Project.Gantt Views";
	private static final String pageTitle = "Gantt View Configuration";
	private static final String pageDescription = "Manage project-specific Gantt view layouts";
	private static final String menuOrder = "1.4";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			detailSection.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isActive"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating Gantt view configuration.");
			return null;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setSelectedFields("id,name,description,project,isActive");
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}
}
