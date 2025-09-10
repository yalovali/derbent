package tech.derbent.screens.service;

import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;

public abstract class CDetailLinesSampleBase {

	protected static CDetailSection createBaseScreenEntity(CProject project, Class<?> clazz, String baseViewName) {
		final CDetailSection scr = new CDetailSection();
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
