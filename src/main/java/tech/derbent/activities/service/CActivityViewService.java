package tech.derbent.activities.service;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.screens.service.CScreenLinesService;
import tech.derbent.users.service.CUserViewService;

public class CActivityViewService {
	public static final String BASE_VIEW_NAME = "Activity View";
	public static final String BASE_PANEL_NAME = "Activity Information";
	static EntityFieldInfo info;
	static Map<String, EntityFieldInfo> fields;
	private static Logger LOGGER = LoggerFactory.getLogger(CActivityViewService.class);

	public static CScreen createBasicView(final CProject project) {
		try {
			final CScreen scr = new CScreen();
			final Class<?> clazz = CActivity.class;
			final String entityType = clazz.getSimpleName().replaceFirst("^C", "");
			scr.setProject(project);
			scr.setEntityType(clazz.getSimpleName());
			scr.setHeaderText(entityType + " View");
			scr.setIsActive(Boolean.TRUE);
			scr.setScreenTitle(entityType + " View");
			scr.setName(BASE_VIEW_NAME);
			scr.setDescription(entityType + " View Details");
			// create screen lines
			scr.addScreenLine(CScreenLinesService.createSection(CUserViewService.BASE_PANEL_NAME));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "name"));
			/******************/
			scr.addScreenLine(CScreenLinesService.createSection("System Access"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating basic user view: {}", e.getMessage(), e);
			e.printStackTrace();
			return null;
		}
	}
}
