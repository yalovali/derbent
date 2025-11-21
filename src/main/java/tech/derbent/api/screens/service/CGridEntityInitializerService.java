package tech.derbent.api.screens.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.utils.Check;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;

public class CGridEntityInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CGridEntity.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CGridEntityInitializerService.class);
	private static final String menuOrder = Menu_Order_SYSTEM + ".10";
	private static final String menuTitle = MenuTitle_SETUP + ".UI.Grids";
	private static final String pageDescription = "Grid management for system ";
	private static final String pageTitle = "Grid Management";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		Check.notNull(project, "project cannot be null");
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "attributeNone"));
			scr.addScreenLine(CDetailLinesService.createSection("Data Provider"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "dataServiceBeanName"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "columnFields"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "attributeNonDeletable"));
			scr.addScreenLine(CDetailLinesService.createSectionEnd());
			scr.addScreenLine(CDetailLinesService.createSection("Audit"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			scr.addScreenLine(CDetailLinesService.createSectionEnd());
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating grid entity view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "dataServiceBeanName", "attributeNonDeletable", "project"));
		return grid;
	}

	public static CGridEntity createMasterView(final CProject project) {
		return createGridEntity(project);
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}
}
