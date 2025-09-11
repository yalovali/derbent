package tech.derbent.screens.service;

import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CMasterSection;

public class CMasterSectionViewService extends CDetailLinesSampleBase {

	public static final String BASE_PANEL_NAME = "MasterSection Information";

	public static CDetailSection createBasicView(final CProject project) {
		try {
			final Class<?> clazz = CMasterSection.class;
			CDetailSection scr = createBaseScreenEntity(project, clazz);
			// create screen lines
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
