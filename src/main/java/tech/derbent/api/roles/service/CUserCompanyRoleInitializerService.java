package tech.derbent.api.roles.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.roles.domain.CUserCompanyRole;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailLinesService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.screens.service.CInitializerServiceBase;

public class CUserCompanyRoleInitializerService extends CInitializerServiceBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(CUserCompanyRoleInitializerService.class);
	public static final String BASE_PANEL_NAME = "Company Role Information";
	static final Class<?> clazz = CUserCompanyRole.class;
	private static final String menuTitle = "Setup.Roles";
	private static final String pageTitle = "Company Roles Management";
	private static final String pageDescription = "Company Roles management";
	private static final String menuOrder = "1.1";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) {
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			// Basic Company Role Information
			scr.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "color"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sortOrder"));
			// Role Type Attributes
			scr.addScreenLine(CDetailLinesService.createSection("Role Type"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isAdmin"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isUser"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isGuest"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating company role view.");
			throw new RuntimeException("Failed to create company role view", e);
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setSelectedFields("id,name,description,isAdmin,isUser,isGuest,color,sortOrder");
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}
}
