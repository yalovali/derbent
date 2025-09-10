package tech.derbent.projects.service;

import java.util.Map;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.users.domain.CUser;

public class CProjectViewService {

	public static final String BASE_VIEW_NAME = "Project View";
	public static final String BASE_PANEL_NAME = "Project Information";
	static EntityFieldInfo info;
	static Map<String, EntityFieldInfo> fields;

	public static CDetailSection createBasicView(final CProject project) {
		try {
			final CDetailSection scr = new CDetailSection();
			scr.setProject(project);
			scr.setEntityType(CUser.class.getSimpleName());
			scr.setHeaderText("User View");
			scr.setIsActive(Boolean.TRUE);
			scr.setScreenTitle("User View");
			scr.setName(BASE_VIEW_NAME);
			scr.setDescription("View details for user");
			// create screen lines
			return scr;
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
