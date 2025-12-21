package tech.derbent.app.kanban.kanbanline.service;

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
import tech.derbent.app.kanban.kanbanline.domain.CKanbanColumn;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;

/** CKanbanColumnInitializerService - Initializes screen metadata and sample data for Kanban columns. */
public final class CKanbanColumnInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CKanbanColumn.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CKanbanColumnInitializerService.class);
	private static final String menuOrder = Menu_Order_SETUP + ".95";
	private static final String menuTitle = MenuTitle_SETUP + ".Kanban Columns";
	private static final String pageDescription = "Kanban column definitions for Kanban line configuration";
	private static final String pageTitle = "Kanban Columns";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		Check.notNull(project, "Project cannot be null");
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "itemOrder"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "kanbanLine"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating kanban column view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "itemOrder", "kanbanLine", "active"));
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final CKanbanLine line, final int sampleIndex, final boolean minimal) {
		Check.notNull(line, "Kanban line cannot be null when seeding columns");
		final String[][] minimalSamples = {
				{
						"Backlog", "Done"
				}, {
						"To Do", "Done"
				}
		};
		final String[][] fullSamples = {
				{
						"Backlog", "In Progress", "Done"
				}, {
						"To Do", "Doing", "Review", "Done"
				}
		};
		final String[][] sampleSets = minimal ? minimalSamples : fullSamples;
		final int index = Math.max(0, Math.min(sampleIndex, sampleSets.length - 1));
		for (final String columnName : sampleSets[index]) {
			line.addKanbanColumn(new CKanbanColumn(columnName, line));
		}
	}

	private CKanbanColumnInitializerService() {}
}
