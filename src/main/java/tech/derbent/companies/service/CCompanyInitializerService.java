package tech.derbent.companies.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.utils.Check;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.page.domain.CPageEntity;
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

	public static CDetailSection createBasicView(final CProject project) {
		try {
			CDetailSection scr = createBaseScreenEntity(project, clazz);
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

	public static CGridEntity createGridEntity(final CProject project, boolean attributeNone) {
		CGridEntity grid = createBaseGridEntity(project, clazz);
		// hide grid actions for companies
		grid.setAttributeNone(attributeNone);
		grid.setSelectedFields("id,name,description,address,phone,email,website,companyTheme,primaryColor,enableNotifications,enabled");
		return grid;
	}

	public static void initialize(CProject project, CGridEntityService gridEntityService, CDetailSectionService detailSectionService,
			CPageEntityService pageEntityService, boolean showInQuickToolbar) throws Exception {
		Check.notNull(project, "project cannot be null");
		Check.notNull(gridEntityService, "gridEntityService cannot be null");
		Check.notNull(detailSectionService, "detailSectionService cannot be null");
		Check.notNull(pageEntityService, "pageEntityService cannot be null");
		CDetailSection detailSection = createBasicView(project);
		detailSectionService.save(detailSection);
		CGridEntity grid = createGridEntity(project, false);
		gridEntityService.save(grid);
		CPageEntity page = createPageEntity(clazz, project, grid, detailSection, "System.Companies", "Company Management",
				"Company management with contact details");
		page.setAttributeShowInQuickToolbar(showInQuickToolbar);
		pageEntityService.save(page);
		// create a single company page
		grid = createGridEntity(project, true);
		grid.setName("Company Single View");
		gridEntityService.save(grid);
		page = createPageEntity(clazz, project, grid, detailSection, "System.Current Company", "Company Management",
				"Company management with contact details");
		pageEntityService.save(page);
	}
}
