package tech.derbent.decisions.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.utils.Check;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.page.domain.CPageEntity;
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
	private static Logger LOGGER = LoggerFactory.getLogger(CDecisionInitializerService.class);
	private static final Class<?> clazz = CDecision.class;

	public static CDetailSection createBasicView(final CProject project) {
		try {
			CDetailSection scr = createBaseScreenEntity(project, clazz);
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
			throw new RuntimeException("Failed to create decision view", e);
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setSelectedFields(
				"name,description,project,decisionType,decisionStatus,accountableUser,assignedTo,createdBy,createdDate,implementationDate");
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
		CGridEntity grid = createGridEntity(project);
		gridEntityService.save(grid);
		CPageEntity page = createPageEntity(clazz, project, grid, detailSection, "Project.Decisions", "Decision Management",
				"Decision tracking and accountability", "1.1");
		page.setAttributeShowInQuickToolbar(showInQuickToolbar);
		pageEntityService.save(page);
	}
}
