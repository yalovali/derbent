package tech.derbent.api.screens.service;

import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.utils.Check;

public abstract class CInitializerServiceProjectItem extends CInitializerServiceEntityOfProject {

	public static void createBasicView(final CDetailSection scr, final Class<?> clazz, final CProject<?> project, final boolean newSection)
			throws NoSuchFieldException {
		CInitializerServiceEntityOfProject.createBasicView(scr, clazz, project, newSection);
		// check clazz has entityType field
		Check.fieldExists(clazz, "entityType");
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
		Check.fieldExists(clazz, "status");
		
	}
}
