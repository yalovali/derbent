package tech.derbent.decisions.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailLinesService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.screens.service.CInitializerServiceBase;

public class CDecisionInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Decisions Information";
	private static final Logger LOGGER = LoggerFactory.getLogger(CDecisionInitializerService.class);
	private static final Class<?> clazz = CDecision.class;
	private static final String menuTitle = MenuTitle_PROJECT + ".Decisions";
	private static final String pageTitle = "Decision Management";
	private static final String pageDescription = "Decision tracking and accountability";
	private static final String menuOrder = Menu_Order_PROJECT + ".3";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			// create screen lines
			scr.addScreenLine(CDetailLinesService.createSection(CDecisionInitializerService.BASE_PANEL_NAME));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			scr.addScreenLine(CDetailLinesService.createSection("Schedule"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "reviewDate"));
			scr.addScreenLine(CDetailLinesService.createSection("Associations"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "accountableUser"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "decisionType"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "estimatedCost"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "decisionStatus"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "implementationDate"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating decision view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setSelectedFields(
				"name,description,project,decisionType,decisionStatus,accountableUser,assignedTo,createdBy,createdDate,implementationDate");
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
