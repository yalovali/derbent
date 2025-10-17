package tech.derbent.users.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailLines;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailLinesService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.screens.service.CInitializerServiceBase;
import tech.derbent.users.domain.CUser;

public class CUserInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "User Information";
	static final Class<?> clazz = CUser.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CUserInitializerService.class);
	private static final String menuOrder = Menu_Order_SYSTEM + ".10";
	private static final String menuTitle = MenuTitle_SYSTEM + ".Users";
	private static final String pageDescription = "User management for system access and permissions";
	private static final String pageTitle = "User Management";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			// create screen lines
			scr.addScreenLine(CDetailLinesService.createSection(CUserInitializerService.BASE_PANEL_NAME));
			// for test purposes only
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "activities"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastname"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "login"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "email"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "phone"));
			scr.addScreenLine(CDetailLinesService.createSection("System Access"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enabled"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "password"));
			scr.addScreenLine(CDetailLinesService.createSection("Project & Company Relations"));
			final CDetailLines line = CDetailLinesService.createLineFromDefaults(clazz, "projectSettings");
			line.setRelationFieldName("projectSettings");
			line.setFieldCaption("projectSettings");
			line.setProperty("Component:createUserProjectSettingsComponent");
			line.setDataProviderBean("CUserService");
			scr.addScreenLine(line);
			scr.addScreenLine(CDetailLinesService.createSection("Organization"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "company"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "companyRole"));
			scr.addScreenLine(CDetailLinesService.createSection("Profile"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "profilePictureData"));
			scr.addScreenLine(CDetailLinesService.createSection("Additional Information"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "id"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isActive"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating user view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setSelectedFields("name,lastname,login,email,phone,projectSettings,enabled,createdDate,lastModifiedDate");
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
