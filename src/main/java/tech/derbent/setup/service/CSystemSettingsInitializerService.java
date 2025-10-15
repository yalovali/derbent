package tech.derbent.setup.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.utils.Check;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailLinesService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.screens.service.CInitializerServiceBase;
import tech.derbent.setup.domain.CSystemSettings;

/** CSystemSettingsInitializerService - Initializer service for CSystemSettings entities. This service creates the dynamic page configuration for
 * system-wide settings management, including grid and detail section definitions. Since system settings are global (not project-related), this
 * service extends CNonProjectInitializerServiceBase. */
public class CSystemSettingsInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "System Settings Information";
	static final Class<?> clazz = CSystemSettings.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CSystemSettingsInitializerService.class);
	private static final String menuTitle = "System.Settings";
	private static final String pageTitle = "System Settings Management";
	private static final String pageDescription = "System Settings";
	private static final String menuOrder = "10.1";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			// Application Info
			detailSection.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "applicationName"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "applicationVersion"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "applicationDescription"));
			// Security
			detailSection.addScreenLine(CDetailLinesService.createSection("Security"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "accountLockoutDurationMinutes"));
			// File Management
			detailSection.addScreenLine(CDetailLinesService.createSection("File Management"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "allowedFileExtensions"));
			// Auto-Login
			detailSection.addScreenLine(CDetailLinesService.createSection("Auto-Login"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "autoLoginEnabled"));
			// Backup
			detailSection.addScreenLine(CDetailLinesService.createSection("Backup and Maintenance"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableAutomaticBackups"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "backupRetentionDays"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "backupScheduleCron"));
			// Caching
			detailSection.addScreenLine(CDetailLinesService.createSection("Caching"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableCaching"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "cacheTtlMinutes"));
			// Database
			detailSection.addScreenLine(CDetailLinesService.createSection("Database"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "databaseConnectionPoolSize"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "databaseName"));
			// UI/Theming
			detailSection.addScreenLine(CDetailLinesService.createSection("UI and Theming"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "defaultSystemTheme"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableDarkMode"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "defaultLoginView"));
			// Additional
			detailSection.addScreenLine(CDetailLinesService.createSection("Additional Information"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "id"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isActive"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating system settings view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setSelectedFields(
				"applicationName,applicationVersion,accountLockoutDurationMinutes,enableAutomaticBackups,enableCaching,defaultSystemTheme,isActive");
		return grid;
	}

	public static CGridEntity createGridEntity(final CProject project, final boolean attributeNone) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		// Set attributeNone to hide grid for single entity display
		grid.setAttributeNone(attributeNone);
		grid.setSelectedFields(
				"applicationName,applicationVersion,accountLockoutDurationMinutes,enableAutomaticBackups,enableCaching,defaultSystemTheme,isActive");
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		Check.notNull(project, "project cannot be null");
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
		// Create a single system settings page (like company single view)
		final CGridEntity singleGrid = createGridEntity(project, true);
		singleGrid.setName("System Settings Single View");
		gridEntityService.save(singleGrid);
		final CPageEntity singlePage = createPageEntity(clazz, project, singleGrid, detailSection, "System.Current Settings", "System Settings",
				"System-wide configuration settings", "1.1");
		pageEntityService.save(singlePage);
	}
}
