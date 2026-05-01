package tech.derbent.plm.issues.issuetype.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CEntityNamedInitializerService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.services.CEntityTypeInitializerService;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.CWorkflowEntityService;
import tech.derbent.plm.issues.issuetype.domain.CIssueType;

public class CIssueTypeInitializerService extends CEntityTypeInitializerService {

	private static final Class<?> clazz = CIssueType.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CIssueTypeInitializerService.class);
	private static final String menuOrder = Menu_Order_TYPES + ".15";
	private static final String menuTitle = MenuTitle_TYPES + "." + MenuTitle_TYPES_PROJECT + ".Issue Types";
	private static final String pageDescription = "Manage issue type categories";
	private static final String pageTitle = "Issue Types";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			return CEntityNamedInitializerService.createTypeEntityView(project, clazz, "Display Configuration", false,
					"level");
		} catch (final Exception e) {
			LOGGER.error("Error creating issue type view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "color", "icon", "workflow"));
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
						"Bug", "Software defect or error"
				}, {
						"Improvement", "Enhancement to existing functionality"
				}, {
						"Task", "Work item or technical task"
				}, {
						"Feature Request", "New feature suggestion"
				}, {
						"Documentation", "Documentation update or issue"
				}
		};
		final CCompany company = project.getCompany();
		final CIssueTypeService issueTypeService = CSpringContext.getBean(CIssueTypeService.class);
		initializeCompanyEntity(nameAndDescriptions, issueTypeService, company, minimal, null);
		// Assign the Agile Item Workflow so issues can move from backlog to "To Do" kanban column.
		// Same pattern as Epic/Feature/UserStory/Activity type initializers.
		final CWorkflowEntityService workflowService = CSpringContext.getBean(CWorkflowEntityService.class);
		final CWorkflowEntity agileWorkflow =
				workflowService.findByNameAndCompany("Agile Item Workflow", company).orElse(null);
		if (agileWorkflow != null) {
			issueTypeService.listByCompany(company).stream().filter(type -> type.getWorkflow() == null).forEach(type -> {
				type.setWorkflow(agileWorkflow);
				issueTypeService.save(type);
			});
		} else {
			LOGGER.warn("Agile Item Workflow not found for company '{}' — issue types will have no workflow assigned",
					company.getName());
		}
	}
}
