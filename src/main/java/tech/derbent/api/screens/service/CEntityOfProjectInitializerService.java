package tech.derbent.api.screens.service;

import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;

public abstract class CEntityOfProjectInitializerService extends CEntityNamedInitializerService {

	@SuppressWarnings ("hiding")
	public static final String BASE_PANEL_NAME = "Description";

	public static CDetailSection createBasicView(final CProject<?> project, final Class<?> clazz) throws Exception {
		// createScreenLines(detailSection, clazz, project, newSection);
		return createBaseScreenEntity(project, clazz);
	}

	protected static void createScreenLines(final CDetailSection scr, final Class<?> clazz, final CProject<?> project,
			final boolean newSection) throws NoSuchFieldException {
		if (newSection) {
			scr.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
		}
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "id", true, ""));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
	}
}
