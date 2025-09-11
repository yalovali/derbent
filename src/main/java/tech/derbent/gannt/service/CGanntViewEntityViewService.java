package tech.derbent.gannt.service;

import tech.derbent.gannt.domain.CGanntViewEntity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.service.CDetailLinesSampleBase;
import tech.derbent.screens.service.CDetailLinesService;

/* NOT USED YET - FOR FUTURE DEVELOPMENT */
public class CGanntViewEntityViewService extends CDetailLinesSampleBase {

	public static final String BASE_PANEL_NAME = ">Gannsts Information";

	public static CDetailSection createBasicView(final CProject project) {
		try {
			final Class<?> clazz = CGanntViewEntity.class;
			CDetailSection scr = createBaseScreenEntity(project, clazz);
			// create screen lines
			scr.addScreenLine(CDetailLinesService.createSection(CGanntViewEntityViewService.BASE_PANEL_NAME));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
