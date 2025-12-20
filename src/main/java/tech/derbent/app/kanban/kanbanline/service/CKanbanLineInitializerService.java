package tech.derbent.app.kanban.kanbanline.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.utils.Check;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanColumn;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;

public class CKanbanLineInitializerService extends CInitializerServiceBase {
	private static final Class<?> clazz = CKanbanLine.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CKanbanLineInitializerService.class);
	private static final String menuOrder = Menu_Order_SETUP + ".90";
	private static final String menuTitle = MenuTitle_SETUP + ".Kanban Lines";
	private static final String pageDescription = "Kanban line definitions and their columns";
	private static final String pageTitle = "Kanban Lines";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "company"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "kanbanColumns"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating Kanban line view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "company", "active"));
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final CCompany company, final boolean minimal) throws Exception {
		final String[][] sampleLines = {
				{
						"Default Kanban", "Backlog to done overview"
				}, {
						"Team Swimlanes", "Single row Kanban board per team"
				}
		};
		initializeCompanyEntity(sampleLines, (CEntityOfCompanyService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)),
				company, minimal, (entity, index) -> {
					Check.instanceOf(entity, CKanbanLine.class, "Expected Kanban line for column initialization");
					final CKanbanLine line = (CKanbanLine) entity;
					if (index == 0) {
						line.addKanbanColumn(new CKanbanColumn("Backlog", line));
						line.addKanbanColumn(new CKanbanColumn("In Progress", line));
						line.addKanbanColumn(new CKanbanColumn("Done", line));
					} else {
						line.addKanbanColumn(new CKanbanColumn("To Do", line));
						line.addKanbanColumn(new CKanbanColumn("Doing", line));
						line.addKanbanColumn(new CKanbanColumn("Review", line));
						line.addKanbanColumn(new CKanbanColumn("Done", line));
					}
				});
	}
}
