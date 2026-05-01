package tech.derbent.api.screens.service;

import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;

public abstract class CProjectItemInitializerService extends CEntityOfProjectInitializerService {

	public static void createScreenLines(final CDetailSection scr, final Class<?> clazz, final CProject<?> project,
			final boolean newSection) throws NoSuchFieldException {
		CEntityOfProjectInitializerService.createScreenLines(scr, clazz, project, newSection);
		scr.addScreenLine("Type", CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
		
	}
}
