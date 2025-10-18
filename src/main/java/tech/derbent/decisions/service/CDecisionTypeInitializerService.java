package tech.derbent.decisions.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.decisions.domain.CDecisionType;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailLinesService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.screens.service.CInitializerServiceBase;

public class CDecisionTypeInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Decision Type Information";
	private static final Class<?> clazz = CDecisionType.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CDecisionTypeInitializerService.class);
	private static final String menuTitle = MenuTitle_TYPES + ".Decision Types";
	private static final String pageTitle = "Decision Type Management";
	private static final String pageDescription = "Manage decision type categories";
	private static final String menuOrder = Menu_Order_TYPES + ".7";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			detailSection.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Decision Configuration"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "requiresApproval"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Display Configuration"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "color"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sortOrder"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "attributeNonDeletable"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isActive"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating decision type view.");
			return null;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields("id,name,description,requiresApproval,color,sortOrder,isActive,project");
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
