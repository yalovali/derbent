package tech.derbent.api.screens.service;

import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;

public abstract class CInitializerServiceProjectItem extends CInitializerServiceEntityOfProject {

	public static void createBasicView(final CDetailSection scr, final Class<?> clazz, final CProject<?> project, final boolean newSection)
			throws NoSuchFieldException {
		CInitializerServiceEntityOfProject.createBasicView(scr, clazz, project, newSection);
	}
}
