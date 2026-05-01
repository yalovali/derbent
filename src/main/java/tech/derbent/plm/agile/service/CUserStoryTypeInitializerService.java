package tech.derbent.plm.agile.service;

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
import tech.derbent.plm.agile.domain.CUserStoryType;

/** Initializer for user story type screens and sample data.
 * <p>
 * User-story types are the default bridge between agile planning and execution work, so the screen exposes both hierarchy level and child capability
 * to make that behavior explicit to administrators.
 * </p>
 */
public class CUserStoryTypeInitializerService extends CEntityTypeInitializerService {

	private static final Class<?> clazz = CUserStoryType.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CUserStoryTypeInitializerService.class);
	private static final String menuOrder = Menu_Order_TYPES + ".11";
	private static final String menuTitle = MenuTitle_TYPES + "." + MenuTitle_TYPES_PROJECT + ".User Story Types";
	private static final String pageDescription = "Manage user story type categories for agile planning";
	private static final String pageTitle = "User Story Type Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			return CEntityNamedInitializerService.createTypeEntityView(project, clazz, "Hierarchy Configuration", true,
					"level", "canHaveChildren");
		} catch (final Exception e) {
			LOGGER.error("Error creating user story type view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "level", "canHaveChildren", "color", "sortOrder",
				"active", "company"));
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
						"Feature Story", "User-facing feature that delivers business value"
				}, {
						"Technical Story", "Technical improvement or infrastructure work"
				}, {
						"Bug Fix", "Defect or issue that needs to be resolved"
				}, {
						"Spike", "Research or investigation story"
				}
		};
		final CCompany company = project.getCompany();
		// Use the concrete service bean to keep this initializer type-safe (no unchecked casts).
		final CUserStoryTypeService userStoryTypeService = CSpringContext.getBean(CUserStoryTypeService.class);
		initializeCompanyEntity(nameAndDescriptions, userStoryTypeService, company, minimal, (userStoryType, index) -> {
			userStoryType.setLevel(2);
			userStoryType.setCanHaveChildren(true);
		});
		// Assign the Agile Item Workflow so new user stories start in "To Do" (first Kanban column) when sprint planning populates the backlog.
		// Same pattern as CSprintTypeInitializerService assigning the Sprint Workflow to sprint types.
		final CWorkflowEntityService workflowService = CSpringContext.getBean(CWorkflowEntityService.class);
		final CWorkflowEntity agileWorkflow =
				workflowService.findByNameAndCompany("Agile Item Workflow", company).orElse(null);
		if (agileWorkflow != null) {
			userStoryTypeService.listByCompany(company).stream().filter(type -> type.getWorkflow() == null)
					.forEach(type -> {
						type.setWorkflow(agileWorkflow);
						userStoryTypeService.save(type);
					});
		} else {
			LOGGER.warn(
					"Agile Item Workflow not found for company '{}' — user story types will have no workflow assigned",
					company.getName());
		}
	}
}
