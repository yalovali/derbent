package tech.derbent.api.roles.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.roles.domain.CUserProjectRole;
import tech.derbent.api.utils.Check;
import tech.derbent.companies.service.CCompanyInitializerService;
import tech.derbent.page.domain.CPageEntity;
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

	public static CDetailSection createBasicView(final CProject project) {
		try {
			CDetailSection scr = createBaseScreenEntity(project, clazz);
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

	public static CGridEntity createGridEntity(final CProject project, boolean attributeNone) {
		CGridEntity grid = createBaseGridEntity(project, clazz);
		// hide grid actions for companies
		grid.setAttributeNone(attributeNone);
		grid.setSelectedFields("id,name,description");
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
		CPageEntity page = createPageEntity(clazz, project, grid, detailSection, "Setup.Roles", "User Project Roles Management",
				"User Projet Roles management with contact details", "1.1");
		page.setAttributeShowInQuickToolbar(showInQuickToolbar);
		pageEntityService.save(page);
	}
}
