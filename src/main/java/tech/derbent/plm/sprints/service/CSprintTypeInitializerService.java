package tech.derbent.plm.sprints.service;

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
import tech.derbent.plm.sprints.domain.CSprintType;

public class CSprintTypeInitializerService extends CEntityTypeInitializerService {

	private static final Class<?> clazz = CSprintType.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CSprintTypeInitializerService.class);
	private static final String menuOrder = Menu_Order_TYPES + ".8";
	private static final String menuTitle = MenuTitle_TYPES + "." + MenuTitle_TYPES_PROJECT + ".Sprint Types";
	private static final String pageDescription = "Manage sprint type categories";
	private static final String pageTitle = "Sprint Type Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			return CEntityNamedInitializerService.createTypeEntityView(project, clazz, "Display Configuration", true);
		} catch (final Exception e) {
			LOGGER.error("Error creating sprint type view.");
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
						"Planning Sprint", "Sprint planning and preparation phase"
				}, {
						"Development Sprint", "Active development and implementation sprint"
				}, {
						"Testing Sprint", "Quality assurance and testing focused sprint"
				}, {
						"Release Sprint", "Final preparations and release activities"
				}, {
						"Hardening Sprint", "Bug fixing and system stabilization sprint"
				}
		};
		final CCompany company = project.getCompany();
		initializeCompanyEntity(nameAndDescriptions,
				(CEntityOfCompanyService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)),
				company, minimal, null);
		// Ensure sprint types get a dedicated sprint workflow so newly created sprints start in Planning (and can move to Started/Done/Canceled).
		final CWorkflowEntityService workflowService = CSpringContext.getBean(CWorkflowEntityService.class);
		final CWorkflowEntity sprintWorkflow =
				workflowService.findByNameAndCompany("Sprint Workflow", company).orElse(null);
		if (sprintWorkflow == null) {
			return;
		}
		final CSprintTypeService sprintTypeService = CSpringContext.getBean(CSprintTypeService.class);
		sprintTypeService.listByCompany(company).stream().filter((final CSprintType type) -> type.getWorkflow() == null || type.getWorkflow().getId() == null
				|| !type.getWorkflow().getId().equals(sprintWorkflow.getId())).forEach((final CSprintType type) -> {
type.setWorkflow(sprintWorkflow);
sprintTypeService.save(type);
});
	}
}
