package tech.derbent.api.roles.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.roles.domain.CUserProjectRole;
import tech.derbent.companies.service.CCompanyInitializerService;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailLinesService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.screens.service.CInitializerServiceBase;

public class CUserProjectRoleInitizerService extends CInitializerServiceBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(CCompanyInitializerService.class);
	public static final String BASE_PANEL_NAME = "Company Information";
	static final Class<?> clazz = CUserProjectRole.class;
	private static final String menuTitle = "Setup.Roles";
	private static final String pageTitle = "Project Roles Management";
	private static final String pageDescription = "User Project Roles management";
	private static final String menuOrder = "1.1";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) {
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			// Basic Company Information
			scr.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating company view.");
			throw new RuntimeException("Failed to create company view", e);
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setSelectedFields("id,name,description");
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
