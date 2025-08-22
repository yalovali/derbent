package tech.derbent.risks.service;

import tech.derbent.projects.domain.CProject;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.service.CScreenLinesService;

public class CRiskViewService {

	public static final String BASE_VIEW_NAME = "Risk View";
	public static final String BASE_PANEL_NAME = "Risk Information";

	public static CScreen createBasicView(final CProject project) {
		try {
			final CScreen scr = new CScreen();
			final Class<?> clazz = CRisk.class;
			final String entityType = clazz.getSimpleName().replaceFirst("^C", "");
			scr.setProject(project);
			scr.setEntityType(clazz.getSimpleName());
			scr.setHeaderText(entityType + " View");
			scr.setIsActive(Boolean.TRUE);
			scr.setScreenTitle(entityType + " View");
			scr.setName(BASE_VIEW_NAME);
			scr.setDescription(entityType + " View Details");
			// create screen lines
			scr.addScreenLine(CScreenLinesService.createSection(CRiskViewService.BASE_PANEL_NAME));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(CRisk.class, "name"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(CRisk.class, "description"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(CRisk.class, "riskSeverity"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(CRisk.class, "status"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(CRisk.class, "project"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(CRisk.class, "assignedTo"));
			scr.addScreenLine(CScreenLinesService.createSection("Dates"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(CRisk.class, "createdBy"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(CRisk.class, "createdDate"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(CRisk.class, "lastModifiedDate"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
