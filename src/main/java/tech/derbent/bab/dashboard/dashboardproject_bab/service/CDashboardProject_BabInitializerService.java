package tech.derbent.bab.dashboard.dashboardproject_bab.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
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
import tech.derbent.bab.dashboard.dashboardproject_bab.domain.CDashboardProject_Bab;

/** CDashboardProject_BabInitializerService - Initializer for BAB dashboard projects. Layer: Service (MVC) Following Derbent pattern: Concrete
 * initializer service. */
@Service
@Profile ("bab")
public final class CDashboardProject_BabInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CDashboardProject_Bab.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CDashboardProject_BabInitializerService.class);
	private static final String menuOrder = "10";
	public static final String menuTitle = "BAB Dashboard";
	private static final String pageDescription = "BAB Gateway monitoring dashboard with system metrics, CPU usage, and network status.";
	private static final String pageTitle = "BAB Dashboard";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		scr.addScreenLine(CDetailLinesService.createSection("System Monitoring"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentSystemMetrics"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentDiskUsage"));
		scr.addScreenLine(CDetailLinesService.createSection("Network Monitoring"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentInterfaceList"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentRoutingTable"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentDnsConfiguration"));
		scr.addScreenLine(CDetailLinesService.createSection("System Management"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentSystemServices"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentSystemProcessList"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentWebServiceDiscovery"));
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
		// Create detail section
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid,
				MenuTitle_DEVELOPMENT + menuTitle + "_devel", pageTitle, pageDescription, false, Menu_Order_DEVELOPMENT + menuOrder);
		// second view
		final CDetailSection detailSection2 = createBasicView(project);
		detailSection2.setName("BAB Setup");
		final CGridEntity grid2 = createGridEntity(project);
		grid2.setName("BAB Setup");
		grid2.setAttributeNone(true); // dont show grid
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection2, grid2, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder + ".1");
	}

	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		final String[][] nameAndDescriptions = {
				{
						"Gateway Overview Dashboard", "Summary dashboard for BAB gateway monitoring and alerts"
				}, {
						"Device Status Dashboard", "Live device status view with connectivity and signal health"
				}, {
						"Telemetry Snapshot", "Quick telemetry snapshot for gateway sensors and buses"
				}
		};
		initializeProjectEntity(nameAndDescriptions,
				(CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project, minimal,
				(item, index) -> {
					final CDashboardProject_Bab dashboard = (CDashboardProject_Bab) item;
					dashboard.setIsActive(true);
					dashboard.setDashboardType(index % 2 == 0 ? "monitoring" : "reporting");
					dashboard.setDashboardWidget(index == 1 ? "bab_device_status" : "bab_gateway_monitor");
				});
	}

	private CDashboardProject_BabInitializerService() {
		// Utility class - no instantiation
	}
}
