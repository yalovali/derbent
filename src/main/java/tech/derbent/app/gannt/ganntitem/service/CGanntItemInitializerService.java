package tech.derbent.app.gannt.ganntitem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.app.gannt.ganntitem.domain.CGanntItem;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;

public class CGanntItemInitializerService extends CInitializerServiceBase {
	@SuppressWarnings ("unused")
	private static final Class<?> clazz = CGanntItem.class;
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CGanntItemInitializerService.class);
	@SuppressWarnings ("unused")
	private static final String menuOrder = Menu_Order_PROJECT + ".96";
	@SuppressWarnings ("unused")
	private static final String menuTitle = MenuTitle_PROJECT + ".Gantt Items";
	@SuppressWarnings ("unused")
	private static final String pageDescription = "Project items prepared for Gantt timelines";
	@SuppressWarnings ("unused")
	private static final String pageTitle = "Gantt Items";
	@SuppressWarnings ("unused")
	private static final boolean showInQuickToolbar = false;

	@SuppressWarnings ("unused")
	public static CDetailSection createBasicView(final CProject project) throws Exception {
		/* DONT INITIALIZE ANYTHING HERE YET */
		return null;
	}

	@SuppressWarnings ("unused")
	public static CGridEntity createGridEntity(final CProject project) {
		/* DONT INITIALIZE ANYTHING HERE YET */
		return null;
	}

	@SuppressWarnings ("unused")
	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		/* DONT INITIALIZE ANYTHING HERE YET */
	}

	@SuppressWarnings ("unused")
	public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
		/* DONT INITIALIZE ANYTHING HERE YET */
	}
}
