package tech.derbent.decisions.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.service.CScreenLinesService;

public class CDecisionViewService {

	public static final String BASE_VIEW_NAME = "Decisions View";
	public static final String BASE_PANEL_NAME = "Decisions Information";
	private static Logger LOGGER = LoggerFactory.getLogger(CDecisionViewService.class);

	public static CScreen createBasicView(final CProject project) {
		try {
			final CScreen scr = new CScreen();
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
			scr.addScreenLine(CScreenLinesService.createSection(CDecisionViewService.BASE_PANEL_NAME));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "name"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "description"));
			// scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "project"));
			scr.addScreenLine(CScreenLinesService.createSection("Schedule"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "createdBy"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "createdDate"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "decisionType"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "estimatedCost"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "decisionStatus"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "accountableUser"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "teamMembers"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "implementationDate"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "reviewDate"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "project"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "assignedTo"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating basic user view: {}", e.getMessage(), e);
			e.printStackTrace();
			return null;
		}
	}
}
