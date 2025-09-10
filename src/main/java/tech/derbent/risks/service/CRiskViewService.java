package tech.derbent.risks.service;

import tech.derbent.projects.domain.CProject;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.service.CDetailLinesService;

public class CRiskViewService {

	public static final String BASE_VIEW_NAME = "Risk View";
	public static final String BASE_PANEL_NAME = "Risk Information";

	public static CDetailSection createBasicView(final CProject project) {
		try {
			final CDetailSection scr = new CDetailSection();
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
			scr.addScreenLine(CDetailLinesService.createSection(CRiskViewService.BASE_PANEL_NAME));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(CRisk.class, "name"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(CRisk.class, "description"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(CRisk.class, "riskSeverity"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(CRisk.class, "status"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(CRisk.class, "project"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(CRisk.class, "assignedTo"));
			scr.addScreenLine(CDetailLinesService.createSection("Dates"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(CRisk.class, "createdBy"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(CRisk.class, "createdDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(CRisk.class, "lastModifiedDate"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
