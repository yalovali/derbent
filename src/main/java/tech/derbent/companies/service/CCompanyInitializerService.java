package tech.derbent.companies.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailLinesService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.screens.service.CInitializerServiceBase;

public class CCompanyInitializerService extends CInitializerServiceBase {
	public static final String BASE_PANEL_NAME = "Company Information";
	static final Class<?> clazz = CCompany.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CCompanyInitializerService.class);
	private static final String menuTitle = "System.Companies";
	private static final String pageTitle = "Company Management";
	private static final String pageDescription = "Company management with contact details";
	private static final String menuOrder = "1.1";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) {
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			// Basic Company Information
			scr.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "address"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "phone"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "email"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "website"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "taxNumber"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enabled"));
			// Company Branding & UI Settings
			scr.addScreenLine(CDetailLinesService.createSection("Company Branding & UI"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "companyTheme"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "companyLogoUrl"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "primaryColor"));
			// Business Operations
			scr.addScreenLine(CDetailLinesService.createSection("Business Operations"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "workingHoursStart"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "workingHoursEnd"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "companyTimezone"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "defaultLanguage"));
			// Notification Settings
			scr.addScreenLine(CDetailLinesService.createSection("Notification Settings"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableNotifications"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "notificationEmail"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating company view.");
			throw new RuntimeException("Failed to create company view", e);
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setSelectedFields("id,name,description,address,phone,email,website,companyTheme,primaryColor,enableNotifications,enabled");
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
		// create a single company page
		grid = createGridEntity(project);
		grid.setAttributeNone(true);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, "System.Current Company", pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}
}
