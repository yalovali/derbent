package tech.derbent.page.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.service.CDetailLinesSampleBase;
import tech.derbent.screens.service.CDetailLinesService;

public class CPageEntityViewService extends CDetailLinesSampleBase {

	public static final String BASE_VIEW_NAME = "Page View";
	public static final String BASE_PANEL_NAME = "Page Information";
	private static Logger LOGGER = LoggerFactory.getLogger(CPageEntityViewService.class);

	public static CDetailSection createBasicView(final CProject project) {
		try {
			final Class<?> clazz = CPageEntity.class;
			CDetailSection scr = createBaseScreenEntity(project, clazz, BASE_VIEW_NAME);
			// create screen lines
			scr.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "id"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			scr.addScreenLine(CDetailLinesService.createSection("Route Information"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "route"));
			/******************/
			scr.addScreenLine(CDetailLinesService.createSection("System Access"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating basic user view: {}", e.getMessage(), e);
			e.printStackTrace();
			return null;
		}
	}
}
