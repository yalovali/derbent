package tech.derbent.api.screens.service;

import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;

public abstract class CInitializerServiceNamedEntity extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Description";

	@SuppressWarnings ("unused")
	public static void createBasicView(final CDetailSection scr, final Class<?> clazz, final CProject<?> project, final boolean newSection)
			throws NoSuchFieldException {
		if (newSection) {
			scr.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
		}
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "id", true, "10%"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name", false, "100%"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
	}
}
