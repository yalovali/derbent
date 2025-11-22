package tech.derbent.app.page.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.utils.Check;
import tech.derbent.app.page.domain.CPageEntity;
import tech.derbent.app.projects.domain.CProject;

public class CPageEntityInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CPageEntity.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CPageEntityInitializerService.class);
	private static final String menuOrder = Menu_Order_SYSTEM + ".1";
	private static final String menuTitle = MenuTitle_SYSTEM + ".Dynamic Page Management";
	private static final String pageDescription = "Page Settings";
	private static final String pageTitle = "Dynamic Page Management";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "pageService"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Navigation"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "menuTitle"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "menuOrder"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "pageTitle"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "icon"));
   
			detailSection.addScreenLine(CDetailLinesService.createSection("Layout Configuration"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "gridEntity"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "detailSection"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "content"));
   
			detailSection.addScreenLine(CDetailLinesService.createSection("Security & Behavior"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "requiresAuthentication"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "attributeShowInQuickToolbar"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "attributeReadonly"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "attributeNonDeletable"));
   
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
   
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating page entity view. {}", e.getMessage());
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "menuTitle", "pageTitle", "menuOrder", "requiresAuthentication", "attributeShowInQuickToolbar",
				"attributeReadonly", "attributeNonDeletable"));
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		Check.notNull(project, "project cannot be null");
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}
}
