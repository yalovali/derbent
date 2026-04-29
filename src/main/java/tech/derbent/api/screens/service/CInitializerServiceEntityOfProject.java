package tech.derbent.api.screens.service;

import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;

public abstract class CInitializerServiceEntityOfProject extends CInitializerServiceNamedEntity {

	public static void createBasicView(final CDetailSection scr, final Class<?> clazz, final CProject<?> project, final boolean newSection)
			throws NoSuchFieldException {
		CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, newSection);
	}
}
