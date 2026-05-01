package tech.derbent.plm.components.componentversion.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CEntityOfProjectInitializerService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CProjectItemInitializerService;
import tech.derbent.plm.components.componentversion.domain.CProjectComponentVersion;

public class CProjectComponentVersionInitializerService extends CProjectItemInitializerService {

	private static final Class<?> clazz = CProjectComponentVersion.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectComponentVersionInitializerService.class);
	private static final String menuOrder = Menu_Order_PRODUCTS + ".30";
	private static final String menuTitle = MenuTitle_PRODUCTS + ".ComponentVersions";
	private static final String pageDescription = "Component Version management";
	private static final String pageTitle = "Component Version Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection detailSection = CEntityOfProjectInitializerService.createBasicView(project, clazz);
			CProjectItemInitializerService.createScreenLines(detailSection, clazz, project, false);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "projectComponent"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating componentversion view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "projectComponent", "name", "description", "status", "project", "assignedTo",
				"createdBy", "createdDate"));
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
}
