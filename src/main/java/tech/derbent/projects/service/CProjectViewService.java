package tech.derbent.projects.service;

import java.util.Map;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.service.CDetailLinesSampleBase;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

public class CProjectViewService extends CDetailLinesSampleBase {

	public static final String BASE_PANEL_NAME = "Project Information";
	static EntityFieldInfo info;
	static Map<String, EntityFieldInfo> fields;

	public static CDetailSection createBasicView(final CProject project) {
		try {
			final Class<?> clazz = CProject.class;
			CDetailSection scr = createBaseScreenEntity(project, clazz);
			// create screen lines
			return scr;
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
