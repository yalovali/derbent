package tech.derbent.api.screens.service;

import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;

public abstract class CEntityOfProjectInitializerService extends CEntityNamedInitializerService {

	public static CDetailSection createBasicView(final CProject<?> project, final Class<?> clazz) throws Exception {
		// createScreenLines(detailSection, clazz, project, newSection);
		return createBaseScreenEntity(project, clazz);
	}

	protected static void createScreenLines(final CDetailSection scr, final Class<?> clazz, final CProject<?> project,
			final boolean newSection) throws NoSuchFieldException {
		CEntityNamedInitializerService.createScreenLines(scr, clazz, project, false);
		// CAuxillaries.hasField(clazz, "project");
	}
}
