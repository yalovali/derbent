package tech.derbent.screens.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.utils.Check;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CGridEntity;

public class CGridInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Grid Information";
	private static final Logger LOGGER = LoggerFactory.getLogger(CGridInitializerService.class);
	private static final Class<?> clazz = CGridEntity.class;

	public static CDetailSection createBasicView(final CProject project) {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			detailSection.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isActive"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "attributeNone"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Data Provider"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "dataServiceBeanName"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "selectedFields"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "attributeNonDeletable"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating grid entity view.");
			return null;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setSelectedFields("id,name,description,dataServiceBeanName,selectedFields,attributeNonDeletable,project");
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		Check.notNull(project, "project cannot be null");
		Check.notNull(gridEntityService, "gridEntityService cannot be null");
		Check.notNull(detailSectionService, "detailSectionService cannot be null");
		Check.notNull(pageEntityService, "pageEntityService cannot be null");
		final CDetailSection detailSection = createBasicView(project);
		detailSectionService.save(detailSection);
		final CGridEntity grid = createGridEntity(project);
		gridEntityService.save(grid);
		final tech.derbent.page.domain.CPageEntity page = createPageEntity(clazz, project, grid, detailSection, "System.Grids",
				"Grid Configuration Management", "Manage reusable grid metadata definitions", "1.1");
		pageEntityService.save(page);
	}

	public static CGridEntity createMasterView(final CProject project) {
		return createGridEntity(project);
	}
}
