package tech.derbent.screens.service;

import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CScreen;

public abstract class CScreenLineSampleBase {

	protected static CScreen createBaseScreenEntity(CProject project, Class<?> clazz, String baseViewName) {
		final CScreen scr = new CScreen();
		scr.setProject(project);
		scr.setEntityType(clazz.getSimpleName());
		final String entityType = clazz.getSimpleName().replaceFirst("^C", "");
		scr.setHeaderText(entityType + " View");
		scr.setIsActive(Boolean.TRUE);
		scr.setScreenTitle(entityType + " View");
		scr.setName(baseViewName);
		scr.setDescription(entityType + " View Details");
		return scr;
	}
}
