package tech.derbent.screens.service;

import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;

public abstract class CDetailLinesSampleBase {

	protected static CDetailSection createBaseScreenEntity(CProject project, Class<?> clazz) throws Exception {
		// get baseview name from class static String VIEW_NAME with reflection
		try {
			// get base class from static method :
			// public static Class<?> getViewClassStatic() { return CUsersView.class; }
			Class<?> baseViewClass = (Class<?>) clazz.getMethod("getViewClassStatic").invoke(null);
			// now get VIEW_NAME from baseClass
			String baseViewName = (String) baseViewClass.getField("VIEW_NAME").get(null);
			return createBaseScreenEntity(project, clazz, baseViewName, 0);
		} catch (Exception e) {
			throw new Exception("Error accessing VIEW_NAME or getViewClassStatic field in class " + clazz.getName(), e);
		}
	}

	protected static CDetailSection createBaseScreenEntity(CProject project, Class<?> clazz, String baseViewName, int dummy) {
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
