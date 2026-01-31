package tech.derbent.bab.setup.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.companies.domain.CCompany;
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
 * active Provides initialization and setup for BAB gateway system settings configuration interface. Following standard Derbent initializer pattern
 * with two views: 1. Standard management view (with grid) 2. Configuration-focused view (no grid, full-screen component) */
public final class CSystemSettings_BabInitializerService extends CInitializerServiceBase {
	private static final Class<?> clazz = CSystemSettings_Bab.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CSystemSettings_BabInitializerService.class);
	private static final String menuOrder = Menu_Order_SETUP + ".95";
	private static final String menuTitle = "BAB Gateway Settings";
	private static final String pageDescription = "Configure IoT gateway and device management settings";
	private static final String pageTitle = "BAB Gateway Configuration";
	private static final boolean showInQuickToolbar = false;

	/** Builds the standard detail view for BAB gateway settings. */
	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			// Application Identity section
			scr.addScreenLine(CDetailLinesService.createSection("Application Configuration"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_ccomponentCalimeroStatus"));
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
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating BAB gateway settings view: {}", e.getMessage(), e);
			throw e;
		}
	}

	/** Builds the configuration-focused detail view for BAB gateway settings. This view shows only the main configuration component without grid for
	 * streamlined access. */
	private static CDetailSection createConfigurationView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			detailSection.addScreenLine(CDetailLinesService.createSection("Application Configuration"));
			// Minimal header - just application identity
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "applicationName"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "applicationDescription"));
			// Calimero Status Component (full-screen)
			detailSection.addScreenLine(CDetailLinesService.createSection("Calimero Service"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_ccomponentCalimeroStatus"));
			// Essential gateway configuration (inline, no separate sections)
			detailSection.addScreenLine(CDetailLinesService.createSection("Gateway Configuration"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "gatewayIpAddress"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "gatewayPort"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "deviceScanIntervalSeconds"));
			LOGGER.debug("Created configuration view for BAB gateway settings");
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating BAB gateway configuration view: {}", e.getMessage(), e);
			throw e;
		}
	}

	/** Builds the grid configuration for BAB settings list views. */
	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		// BAB system settings is typically a singleton, so grid may be minimal
		// Focus on essential identification columns
		grid.setColumnFields(List.of("applicationName", "gatewayIpAddress", "gatewayPort", "lastModifiedDate"));
		return grid;
	}

	/** Registers BAB gateway settings pages and grids for a project. */
	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		LOGGER.info("Initializing BAB Gateway system settings");
		// View 1: Standard CRUD for settings management
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, MenuTitle_DEVELOPMENT + menuTitle,
				pageTitle, pageDescription, showInQuickToolbar, menuOrder);
		// View 2: Single-page configuration view (no grid)
		final CDetailSection configSection = createConfigurationView(project);
		final CGridEntity configGrid = createGridEntity(project);
		// Set unique names for configuration view
		configSection.setName("BAB Gateway Configuration Section");
		configGrid.setName("BAB Gateway Configuration Grid");
		// CRITICAL: Hide grid to show only configuration component
		configGrid.setAttributeNone(true);
		// Register single-page configuration view as separate menu item
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, configSection, configGrid,
				MenuTitle_SETUP + "." + menuTitle, "BAB Gateway Configuration", // Page title
				"Configure Calimero service and gateway network settings", // Description
				true, // Show in quick toolbar (for quick access)
				menuOrder + ".1"); // Submenu order
	}

	/** Initialize sample BAB system settings for a company. Following standard pattern: takes CCompany, not CProject.
	 * @param company the company to initialize for
	 * @param minimal whether to create minimal sample data */
	public static void initializeSample(final CCompany company, final boolean minimal) throws Exception {
		LOGGER.info("Initializing BAB system settings sample data for company: {}", company.getName());
		final CSystemSettings_BabService service = CSpringContext.getBean(CSystemSettings_BabService.class);
		// Check if BAB system settings already exists for this company
		final List<CSystemSettings_Bab> existingSettings = service.findAll();
		if (!existingSettings.isEmpty()) {
			LOGGER.info("BAB system settings already exists for company: {}", company.getName());
			return;
		}
		// Create sample BAB gateway settings
		final CSystemSettings_Bab settings = service.newEntity();
		// Application identity
		settings.setApplicationName("BAB IoT Gateway");
		settings.setApplicationDescription("Industrial IoT Gateway for device communication and data collection");
		settings.setApplicationVersion("1.0.0");
		// Gateway network configuration
		settings.setGatewayIpAddress("127.0.0.1");
		settings.setCalimeroExecutablePath("~/git/calimero/build/calimero");
		settings.setEnableCalimeroService(true);
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
		LOGGER.info("BAB system settings sample data initialized successfully for company: {}", company.getName());
	}

	private CSystemSettings_BabInitializerService() {
		// Utility class - no instantiation
	}
}
