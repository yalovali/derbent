package tech.derbent.bab.dashboard.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.bab.dashboard.domain.CDashboardProject_Bab;

/** CDashboardProject_BabInitializerService - Initializer for BAB dashboard projects. Layer: Service (MVC) Following Derbent pattern: Concrete
 * initializer service. */
@Service
@Profile ("bab")
public final class CDashboardProject_BabInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CDashboardProject_Bab.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CDashboardProject_BabInitializerService.class);
	private static final String menuOrder = Menu_Order_SETUP + ".190";
	private static final String menuTitle = MenuTitle_SETUP + ".190 BAB Dashboard Projects";
	private static final String pageDescription = "Basic dashboard projects for BAB gateway monitoring and visualization.";
	private static final String pageTitle = "BAB Dashboard Projects";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		// Basic Information Section
		scr.addScreenLine(CDetailLinesService.createSection("Basic Information"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isActive"));
		// Dashboard Configuration Section
		scr.addScreenLine(CDetailLinesService.createSection("Dashboard Configuration"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "dashboardType"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "dashboardWidget"));
		// Project Item Standard Sections
		scr.addScreenLine(CDetailLinesService.createSection("Assignment & Status"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
		// Standard composition sections - simplified to avoid missing methods
		LOGGER.debug("Skipping composition sections for compilation");
		// Standard sections from parent
		LOGGER.debug("Skipping audit fields section for compilation");
		return scr;
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "project", "active"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		LOGGER.info("Initializing BAB Dashboard Projects for project: {}", project.getName());
		// Create detail section
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, false, menuOrder);
		// second view
		final CDetailSection detailSection2 = createBasicView(project);
		detailSection2.setName("BAB Dashboard Projects - View 2");
		final CGridEntity grid2 = createGridEntity(project);
		grid2.setName("BAB Dashboard Projects Grid - View 2");
		grid2.setAttributeNone(true); // dont show grid
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection2, grid, "Bab Dashboard", pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	private CDashboardProject_BabInitializerService() {
		// Utility class - no instantiation
	}
}
