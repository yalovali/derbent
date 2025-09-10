package tech.derbent.activities.service;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.screens.service.CDetailLinesService;
import tech.derbent.users.service.CUserViewService;

public class CActivityViewService {
	public static final String BASE_VIEW_NAME = "Activity View";
	public static final String BASE_PANEL_NAME = "Activity Information";
	static EntityFieldInfo info;
	static Map<String, EntityFieldInfo> fields;
	private static Logger LOGGER = LoggerFactory.getLogger(CActivityViewService.class);

	public static CDetailSection createBasicView(final CProject project) {
		try {
			final CDetailSection scr = new CDetailSection();
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
			scr.addScreenLine(CDetailLinesService.createSection(CUserViewService.BASE_PANEL_NAME));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			/******************/
			scr.addScreenLine(CDetailLinesService.createSection("System Access"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating basic user view: {}", e.getMessage(), e);
			e.printStackTrace();
			return null;
		}
	}
}
