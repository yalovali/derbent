package tech.derbent.bab.setup.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.bab.setup.domain.CSystemSettings_Bab;

/** CSystemSettings_BabInitializerService - BAB IoT Gateway system settings initializer service. Layer: Service (MVC) Active when: 'bab' profile is
 * active Provides initialization and setup for BAB gateway system settings configuration interface. */
public final class CSystemSettings_BabInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CSystemSettings_Bab.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CSystemSettings_BabInitializerService.class);

	/** Create basic detail section for BAB gateway settings. */
	private static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		// Application Identity section
		scr.addScreenLine(CDetailLinesService.createSection("Application Configuration"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "applicationName"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "applicationDescription"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "applicationVersion"));
		// Gateway Configuration section
		scr.addScreenLine(CDetailLinesService.createSection("Gateway Network Configuration"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "gatewayIpAddress"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "gatewayPort"));
		// Device Management section
		scr.addScreenLine(CDetailLinesService.createSection("Device Management"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "deviceScanIntervalSeconds"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maxConcurrentConnections"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableDeviceAutoDiscovery"));
		// Essential Security section (minimal for IoT gateway)
		scr.addScreenLine(CDetailLinesService.createSection("Security Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sessionTimeoutMinutes"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maxLoginAttempts"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "requireStrongPasswords"));
		// Basic File Management section (simplified for gateway)
		scr.addScreenLine(CDetailLinesService.createSection("File Management"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maxFileUploadSizeMb"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "allowedFileExtensions"));
		// System Preferences section
		scr.addScreenLine(CDetailLinesService.createSection("System Preferences"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "defaultSystemTheme"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "showSystemInfo"));
		return scr;
	}

	/** Create grid entity for BAB settings. */
	private static CGridEntity createGridEntity(final CProject<?> project) throws Exception {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		// BAB system settings is typically a singleton, so grid may be minimal
		// Focus on essential identification columns
		grid.setColumnFields(List.of("applicationName", "gatewayIpAddress", "gatewayPort", "lastModified"));
		return grid;
	}

	/** Initialize BAB system settings with proper project-based pattern. */
	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		LOGGER.info("Initializing BAB Gateway system settings");
		final String menuTitle = "BAB Gateway Settings";
		final String pageTitle = "BAB Gateway Configuration";
		final String description = "Configure IoT gateway and device management settings";
		final String menuOrder = "95.50"; // Near end of menu, after main system settings
		final boolean showInQuickToolbar = false;
		// Create basic view for BAB settings
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		// Initialize with proper signature
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle, description,
				showInQuickToolbar, menuOrder);
		LOGGER.info("BAB Gateway system settings initialization completed");
	}

	/** Initialize sample BAB system settings. Creates default BAB gateway configuration for project.
	 * @param project the project to initialize for
	 * @param minimal whether to create minimal sample data */
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		LOGGER.info("Initializing BAB system settings sample data for project: {}", project.getName());
		final CSystemSettings_BabService service = CSpringContext.getBean(CSystemSettings_BabService.class);
		// Check if BAB system settings already exists for this project
		final List<CSystemSettings_Bab> existingSettings = service.findAll();
		if (!existingSettings.isEmpty()) {
			LOGGER.info("BAB system settings already exists for project: {}", project.getName());
			return;
		}
		// Create sample BAB gateway settings
		final CSystemSettings_Bab settings = service.newEntity();
		settings.setApplicationName("BAB IoT Gateway");
		settings.setApplicationDescription("Industrial IoT Gateway for device communication and data collection");
		settings.setApplicationVersion("1.0.0");
		// Gateway network configuration
		settings.setGatewayIpAddress("192.168.1.100");
		settings.setGatewayPort(8080);
		// Device management settings
		settings.setDeviceScanIntervalSeconds(30);
		settings.setMaxConcurrentConnections(50);
		settings.setEnableDeviceAutoDiscovery(true);
		// Security settings
		settings.setSessionTimeoutMinutes(60);
		settings.setMaxLoginAttempts(5);
		settings.setRequireStrongPasswords(true);
		// File management
		settings.setMaxFileUploadSizeMb(new java.math.BigDecimal("25"));
		settings.setAllowedFileExtensions(".txt,.csv,.log,.json,.xml");
		// System preferences
		settings.setDefaultSystemTheme("lumo-light");
		settings.setShowSystemInfo(true);
		service.save(settings);
		LOGGER.info("BAB system settings sample data initialized successfully");
	}

	private CSystemSettings_BabInitializerService() {
		// Utility class - no instantiation
	}
}
