package tech.derbent.api.screens.service;

import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.app.projects.domain.CProject;

public abstract class CInitializerServiceProjectItem extends CInitializerServiceEntityOfProject {

	@SuppressWarnings ("hiding")
	public static final String BASE_PANEL_NAME = "Description";

	public static void createBasicView(final CDetailSection scr, final Class<?> clazz, final CProject project, final boolean newSection)
			throws NoSuchFieldException {
		if (newSection) {
			scr.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
		}
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "id"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
	}
}
