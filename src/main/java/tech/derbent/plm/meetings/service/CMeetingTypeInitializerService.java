package tech.derbent.plm.meetings.service;

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
import tech.derbent.plm.meetings.domain.CMeetingType;

public class CMeetingTypeInitializerService extends CEntityTypeInitializerService {

	private static final Class<?> clazz = CMeetingType.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingTypeInitializerService.class);
	private static final String menuOrder = Menu_Order_TYPES + ".5";
	private static final String menuTitle = MenuTitle_TYPES + "." + MenuTitle_TYPES_PROJECT + ".Meeting Types";
	private static final String pageDescription = "Manage meeting type categories";
	private static final String pageTitle = "Meeting Type Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			return CEntityNamedInitializerService.createTypeEntityView(project, clazz, "Display Configuration", true,
					"level");
		} catch (final Exception e) {
			LOGGER.error("Error creating meeting type view.");
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
						"Daily Standup", "Daily team synchronization meetings"
				}, {
						"Sprint Planning", "Sprint planning and estimation meetings"
				}, {
						"Sprint Review", "Sprint review and demonstration meetings"
				}, {
						"Sprint Retrospective", "Sprint retrospective and improvement meetings"
				}, {
						"Project Review", "Project review and status meetings"
				}, {
						"Technical Review", "Technical design and code review meetings"
				}, {
						"Stakeholder Meeting", "Meetings with project stakeholders"
				}, {
						"Training Session", "Training and knowledge sharing sessions"
				}
		};
		final CCompany company = project.getCompany();
		final CMeetingTypeService meetingTypeService = CSpringContext.getBean(CMeetingTypeService.class);
		initializeCompanyEntity(nameAndDescriptions, meetingTypeService, company, minimal, null);
		// Assign the Agile Item Workflow so meetings can move from backlog to kanban columns via valid transitions.
		// Same pattern as Epic/Feature/UserStory/Activity/Issue type initializers.
		final CWorkflowEntityService workflowService = CSpringContext.getBean(CWorkflowEntityService.class);
		final CWorkflowEntity agileWorkflow =
				workflowService.findByNameAndCompany("Agile Item Workflow", company).orElse(null);
		if (agileWorkflow != null) {
			meetingTypeService.listByCompany(company).stream().filter(type -> type.getWorkflow() == null)
					.forEach(type -> {
						type.setWorkflow(agileWorkflow);
						meetingTypeService.save(type);
					});
		} else {
			LOGGER.warn("Agile Item Workflow not found for company '{}' — meeting types will have no workflow assigned",
					company.getName());
		}
	}
}
