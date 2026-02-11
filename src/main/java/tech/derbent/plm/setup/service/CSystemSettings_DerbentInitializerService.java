package tech.derbent.plm.setup.service;

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
import tech.derbent.plm.setup.domain.CSystemSettings_Derbent;

/** CSystemSettings_DerbentInitializerService - Derbent PLM system settings initializer service. Layer: Service (MVC) Active when: default profile or
 * 'derbent' profile (NOT 'bab' profile) Provides initialization and setup for comprehensive PLM system settings configuration interface. */
public final class CSystemSettings_DerbentInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CSystemSettings_Derbent.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CSystemSettings_DerbentInitializerService.class);

	/** Create comprehensive detail section for Derbent PLM settings. */
	private static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		// Application Identity section
		scr.addScreenLine(CDetailLinesService.createSection("Application Configuration"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "applicationName"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "applicationDescription"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "applicationVersion"));
		// Project Management Features section
		scr.addScreenLine(CDetailLinesService.createSection("Project Management Features"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableProjectTemplates"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableKanbanBoards"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableTimeTracking"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableGanttCharts"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableResourcePlanning"));
		// Reporting and Analytics section
		scr.addScreenLine(CDetailLinesService.createSection("Reporting and Analytics"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableAdvancedReporting"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "reportGenerationTimeoutMinutes"));
		// Enhanced Security section
		scr.addScreenLine(CDetailLinesService.createSection("Security and Authentication"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sessionTimeoutMinutes"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maxLoginAttempts"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "requireStrongPasswords"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableTwoFactorAuth"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "accountLockoutDurationMinutes"));
		// LDAP Authentication section
		scr.addScreenLine(CDetailLinesService.createSection("LDAP Authentication"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableLdapAuthentication"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "ldapServerUrl"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "ldapBindDn"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "ldapBindPassword"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "ldapSearchBase"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "ldapUserFilter"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentLdapTest"));
		// Audit and Compliance section
		scr.addScreenLine(CDetailLinesService.createSection("Audit and Compliance"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableAuditLogging"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "auditLogRetentionDays"));
		// Integration and API section
		scr.addScreenLine(CDetailLinesService.createSection("Integration and API"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableRestApi"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "apiRateLimitPerMinute"));
		// File Management section
		scr.addScreenLine(CDetailLinesService.createSection("File Management"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maxFileUploadSizeMb"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maxTotalStorageGb"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "allowedFileExtensions"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableFileVersioning"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableFileCompression"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableFileVirusScanning"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "fileStoragePath"));
		// Notification System section
		scr.addScreenLine(CDetailLinesService.createSection("Notification System"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableEmailNotifications"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enablePushNotifications"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "notificationBatchSize"));
		// Email Configuration section
		scr.addScreenLine(CDetailLinesService.createSection("Email Configuration"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "systemEmailFrom"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "supportEmail"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "smtpServer"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "smtpPort"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "smtpUseTls"));
		// Database and Performance section
		scr.addScreenLine(CDetailLinesService.createSection("Database and Performance"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "databaseName"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "databaseConnectionPoolSize"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableDatabaseLogging"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableCaching"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "cacheTtlMinutes"));
		// Backup and Maintenance section
		scr.addScreenLine(CDetailLinesService.createSection("Backup and Maintenance"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableAutomaticBackups"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "backupScheduleCron"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "backupRetentionDays"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maintenanceModeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maintenanceMessage"));
		// UI and Theming section
		scr.addScreenLine(CDetailLinesService.createSection("UI and Theming"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "defaultSystemTheme"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "fontSizeScale"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableDarkMode"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "defaultLoginView"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastVisitedView"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "showSystemInfo"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "autoLoginEnabled"));
		return scr;
	}

	/** Create grid entity for Derbent settings. */
	private static CGridEntity createGridEntity(final CProject<?> project) throws Exception {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		// Derbent system settings is typically a singleton, but show key configuration info
		grid.setColumnFields(List.of("applicationName", "applicationVersion", "enableProjectTemplates", "enableAdvancedReporting",
				"maintenanceModeEnabled", "lastModified"));
		return grid;
	}

	/** Initialize Derbent system settings with proper project-based pattern. */
	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final String menuTitle = "System Settings";
		final String pageTitle = "System Configuration";
		final String description = "Configure comprehensive system settings for PLM functionality";
		final String menuOrder = "95.00"; // Near end of menu, system administration
		final boolean showInQuickToolbar = false;
		// Create basic view for Derbent settings
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		// Initialize with proper signature
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle, description,
				showInQuickToolbar, menuOrder);
		LOGGER.info("Derbent PLM system settings initialization completed");
	}

	/** Initialize sample Derbent system settings. Creates default PLM configuration for project.
	 * @param project the project to initialize for
	 * @param minimal whether to create minimal sample data */
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		LOGGER.info("Initializing Derbent system settings sample data for project: {}", project.getName());
		final CSystemSettings_DerbentService service = CSpringContext.getBean(CSystemSettings_DerbentService.class);
		// Check if Derbent system settings already exists for this project
		final List<CSystemSettings_Derbent> existingSettings = service.findAll();
		if (!existingSettings.isEmpty()) {
			LOGGER.info("Derbent system settings already exists for project: {}", project.getName());
			return;
		}
		// Create sample Derbent PLM settings
		final CSystemSettings_Derbent settings = service.newEntity();
		settings.setApplicationName("Derbent PLM System");
		settings.setApplicationDescription("Comprehensive Project Lifecycle Management platform for enterprise teams");
		service.save(settings);
		LOGGER.info("Derbent system settings sample data initialized successfully");
	}

	private CSystemSettings_DerbentInitializerService() {
		// Utility class - no instantiation
	}
}
