package tech.derbent.api.roles.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.roles.domain.CUserCompanyRole;
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

public class CUserCompanyRoleInitializerService extends CInitializerServiceBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserCompanyRoleInitializerService.class);
	public static final String BASE_PANEL_NAME = "Company Role Information";
	static final Class<?> clazz = CUserCompanyRole.class;

	public static CDetailSection createBasicView(final CProject project) {
		try {
			CDetailSection scr = createBaseScreenEntity(project, clazz);
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
			// Page Access Permissions
			scr.addScreenLine(CDetailLinesService.createSection("Page Access Permissions"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "readAccessPages"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "writeAccessPages"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating company role view: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to create company role view", e);
		}
	}

	public static CGridEntity createGridEntity(final CProject project, boolean attributeNone) {
		CGridEntity grid = createBaseGridEntity(project, clazz);
		// hide grid actions for company roles
		grid.setAttributeNone(attributeNone);
		grid.setSelectedFields("id,name,description,isAdmin,isUser,isGuest,color,sortOrder");
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
		CPageEntity page = createPageEntity(clazz, project, grid, detailSection, "Setup.Company Roles", "User Company Roles Management",
				"User Company Roles management with role types and page access control");
		page.setAttributeShowInQuickToolbar(showInQuickToolbar);
		pageEntityService.save(page);
	}
}
