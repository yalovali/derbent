package tech.derbent.bab.dashboard.dashboardinterfaces.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.bab.dashboard.dashboardinterfaces.domain.CDashboardInterfaces;

public class CDashboardInterfaces_InitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CDashboardInterfaces.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CDashboardInterfaces_InitializerService.class);
	private static final String menuOrder = Menu_Order_SETUP + ".190";
	public static final String menuTitle = "BAB Interfaces Dashboard";
	private static final String pageDescription = "BAB Interfaces monitoring dashboard with system metrics, CPU usage, and network status.";
	private static final String pageTitle = "BAB Interfaces Dashboard";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		scr.addScreenLine(CDetailLinesService.createSection("Network Monitoring"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentInterfaceSummary"));
		scr.addScreenLine(CDetailLinesService.createSection("Interface Setup"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentCanInterfaces"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentEthernetInterfaces"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentSerialInterfaces"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentRosNodes"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentModbusInterfaces"));
		return scr;
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "project", "isActive"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		LOGGER.info("Initializing BAB Dashboard Projects for project: {}", project.getName());
		// second view
		final CDetailSection detailSection2 = createBasicView(project);
		detailSection2.setName("BAB Interfaces Setup");
		final CGridEntity grid2 = createGridEntity(project);
		grid2.setName("BAB Interfaces Setup");
		grid2.setAttributeNone(true); // dont show grid
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection2, grid2, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder + ".1");
	}

	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		final String[][] nameAndDescriptions = {
				{
						"Gateway Interface Dashboard", "Summary dashboard for BAB interfaces showing overall status and key metrics."
				}
		};
		initializeProjectEntity(nameAndDescriptions,
				(CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project, minimal,
				(item, index) -> {
					final CDashboardInterfaces dashboard = (CDashboardInterfaces) item;
					dashboard.setIsActive(true);
				});
	}

	private CDashboardInterfaces_InitializerService() {
		// Utility class - no instantiation
	}
}
