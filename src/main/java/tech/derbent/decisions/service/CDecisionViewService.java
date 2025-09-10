package tech.derbent.decisions.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.service.CDetailLinesService;

public class CDecisionViewService {

	public static final String BASE_VIEW_NAME = "Decisions View";
	public static final String BASE_PANEL_NAME = "Decisions Information";
	private static Logger LOGGER = LoggerFactory.getLogger(CDecisionViewService.class);

	public static CDetailSection createBasicView(final CProject project) {
		try {
			final CDetailSection scr = new CDetailSection();
			final Class<?> clazz = CDecision.class;
			final String entityType = clazz.getSimpleName().replaceFirst("^C", "");
			scr.setProject(project);
			scr.setEntityType(clazz.getSimpleName());
			scr.setHeaderText(entityType + " View");
			scr.setIsActive(Boolean.TRUE);
			scr.setScreenTitle(entityType + " View");
			scr.setName(BASE_VIEW_NAME);
			scr.setDescription(entityType + " View Details");
			// create screen lines
			scr.addScreenLine(CDetailLinesService.createSection(CDecisionViewService.BASE_PANEL_NAME));
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
			LOGGER.error("Error creating basic user view: {}", e.getMessage(), e);
			e.printStackTrace();
			return null;
		}
	}
}
