package tech.derbent.projects.service;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailLines;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailLinesService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.screens.service.CInitializerServiceBase;

public class CProjectInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Project Information";
	static final Class<?> clazz = CProject.class;
	static Map<String, EntityFieldInfo> fields;
	static EntityFieldInfo info;
	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".1";
	private static final String menuTitle = MenuTitle_PROJECT + ".Projects";
	private static final String pageDescription = "Comprehensive project management with full CRUD operations";
	private static final String pageTitle = "Project Management";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			scr.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isActive"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "company"));
			scr.addScreenLine(CDetailLinesService.createSection("Audit"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			final CDetailLines line = CDetailLinesService.createLineFromDefaults(clazz, "userSettings");
			line.setRelationFieldName("userSettings");
			line.setFieldCaption("userSettings");
			line.setProperty("Component:createProjectUserSettingsComponent");
			line.setDataProviderBean("CProjectService");
			scr.addScreenLine(line);
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating project view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setSelectedFields("id,name,description,isActive,createdDate,lastModifiedDate");
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
