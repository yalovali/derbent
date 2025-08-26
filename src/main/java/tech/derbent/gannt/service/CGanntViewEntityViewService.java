package tech.derbent.gannt.service;

import tech.derbent.abstracts.services.CViewService;
import tech.derbent.gannt.domain.CGanntViewEntity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.service.CScreenLinesService;

/* NOT USED YET - FOR FUTURE DEVELOPMENT */
public class CGanntViewEntityViewService extends CViewService {

	public static final String BASE_VIEW_NAME = "Gannt View";
	public static final String BASE_PANEL_NAME = ">Gannsts Information";

	public static CScreen createBasicView(final CProject project) {
		try {
			final CScreen scr = new CScreen();
			final Class<?> clazz = CGanntViewEntity.class;
			final String entityType = clazz.getSimpleName().replaceFirst("^C", "");
			scr.setProject(project);
			scr.setEntityType(clazz.getSimpleName());
			scr.setHeaderText(entityType + " View");
			scr.setIsActive(Boolean.TRUE);
			scr.setScreenTitle(entityType + " View");
			scr.setName(BASE_VIEW_NAME);
			scr.setDescription(entityType + " View Details");
			// create screen lines
			scr.addScreenLine(CScreenLinesService.createSection(CGanntViewEntityViewService.BASE_PANEL_NAME));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "name"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "description"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "project"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void createDefaultViews(final CProject project) {
		final CScreen scr = new CScreen();
		final Class<?> clazz = CGanntViewEntity.class;
		final String entityType = clazz.getSimpleName().replaceFirst("^C", "");
		scr.setProject(project);
		scr.setEntityType(clazz.getSimpleName());
		scr.setHeaderText(entityType + " View");
		scr.setIsActive(Boolean.TRUE);
		scr.setScreenTitle(entityType + " View");
		scr.setName(BASE_VIEW_NAME);
		scr.setDescription(entityType + " View Details");
	}
}
