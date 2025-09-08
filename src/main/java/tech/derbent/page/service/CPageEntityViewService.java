package tech.derbent.page.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.service.CScreenLineSampleBase;
import tech.derbent.screens.service.CScreenLinesService;

public class CPageEntityViewService extends CScreenLineSampleBase {

	public static final String BASE_VIEW_NAME = "Page View";
	public static final String BASE_PANEL_NAME = "Page Information";
	private static Logger LOGGER = LoggerFactory.getLogger(CPageEntityViewService.class);

	public static CScreen createBasicView(final CProject project) {
		try {
			final Class<?> clazz = CPageEntity.class;
			CScreen scr = createBaseScreenEntity(project, clazz, BASE_VIEW_NAME);
			// create screen lines
			scr.addScreenLine(CScreenLinesService.createSection(BASE_PANEL_NAME));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "id"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "name"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "description"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "project"));
			scr.addScreenLine(CScreenLinesService.createSection("Route Information"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "route"));
			/******************/
			scr.addScreenLine(CScreenLinesService.createSection("System Access"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "assignedTo"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "createdBy"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "createdDate"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating basic user view: {}", e.getMessage(), e);
			e.printStackTrace();
			return null;
		}
	}
}
