package tech.derbent.app.roles.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.roles.domain.CUserCompanyRole;

public class CUserCompanyRoleInitializerService extends CInitializerServiceBase {

	static final Class<?> clazz = CUserCompanyRole.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CUserCompanyRoleInitializerService.class);
	private static final String menuOrder = Menu_Order_ROLES + ".1";
	private static final String menuTitle = MenuTitle_ROLES + ".User Company Roles";
	private static final String pageDescription = "Company Roles management";
	private static final String pageTitle = "User Company Roles Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			// Basic Company Role Information
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "color"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sortOrder"));
			// Role Type Attributes
			detailSection.addScreenLine(CDetailLinesService.createSection("Role Type"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isAdmin"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isUser"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isGuest"));
   
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating company role view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "isAdmin", "isUser", "isGuest", "color", "sortOrder"));
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
