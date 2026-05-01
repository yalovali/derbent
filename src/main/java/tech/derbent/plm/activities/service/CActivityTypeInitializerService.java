package tech.derbent.plm.activities.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CEntityNamedInitializerService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.services.CEntityTypeInitializerService;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.CWorkflowEntityService;
import tech.derbent.plm.activities.domain.CActivityType;

public class CActivityTypeInitializerService extends CEntityTypeInitializerService {

	private static final Class<?> clazz = CActivityType.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CActivityTypeInitializerService.class);
	private static final String menuOrder = Menu_Order_TYPES + ".1";
	private static final String menuTitle = MenuTitle_TYPES + "." + MenuTitle_TYPES_PROJECT + ".Activity Types";
	private static final String pageDescription = "Manage activity type categories for planning";
	private static final String pageTitle = "Activity Type Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			return CEntityNamedInitializerService.createTypeEntityView(project, clazz, "Display Configuration", true,
					"level");
		} catch (final Exception e) {
			LOGGER.error("Error creating activity type view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "color", "sortOrder", "active", "company"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService)
			throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid,
				menuTitle, pageTitle, pageDescription, showInQuickToolbar, menuOrder, null);
	}

	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		final String[][] nameAndDescriptions = {
				{
						"Epic", "Large body of work that can be broken down into smaller user stories"
				}, {
						"User Story", "User-facing feature or requirement from end-user perspective"
				}, {
						"Feature", "Distinct functionality or capability to be implemented"
				}, {
						"Task", "Individual work item or technical implementation task"
				}, {
						"Development", "Software development and coding tasks"
				}, {
						"Testing", "Quality assurance and testing activities"
				}, {
						"Design", "UI/UX design and system architecture"
				}, {
						"Documentation", "Technical writing and documentation"
				}, {
						"Research", "Research and analysis activities"
				}
		};
		final CCompany company = project.getCompany();
		final CActivityTypeService activityTypeService =
				(CActivityTypeService) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz));
		initializeCompanyEntity(nameAndDescriptions, activityTypeService, company, minimal, null);
		// Assign the Agile Item Workflow so new activities start in "To Do" (first Kanban column) when sprint planning populates the backlog.
		// Activities are leaf-level items in the Epic→Feature→UserStory→Activity hierarchy and appear on the Kanban board alongside user stories.
		// Same pattern as CSprintTypeInitializerService assigning the Sprint Workflow to sprint types.
		final CWorkflowEntityService workflowService = CSpringContext.getBean(CWorkflowEntityService.class);
		final CWorkflowEntity agileWorkflow =
				workflowService.findByNameAndCompany("Agile Item Workflow", company).orElse(null);
		if (agileWorkflow != null) {
			activityTypeService.listByCompany(company).stream().filter(type -> type.getWorkflow() == null)
					.forEach(type -> {
						type.setWorkflow(agileWorkflow);
						activityTypeService.save(type);
					});
		} else {
			LOGGER.warn(
					"Agile Item Workflow not found for company '{}' — activity types will have no workflow assigned",
					company.getName());
		}
	}
}
