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
import tech.derbent.plm.agile.domain.CFeatureType;

/** Initializer for feature type screens and seeds.
 * <p>
 * Feature types sit in the middle of the default hierarchy, so the screen exposes both level and child capability to make that relationship explicit.
 * </p>
 */
public class CFeatureTypeInitializerService extends CEntityTypeInitializerService {

	private static final Class<?> clazz = CFeatureType.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CFeatureTypeInitializerService.class);
	private static final String menuOrder = Menu_Order_TYPES + ".12";
	private static final String menuTitle = MenuTitle_TYPES + "." + MenuTitle_TYPES_PROJECT + ".Feature Types";
	private static final String pageDescription = "Manage feature type categories for agile planning";
	private static final String pageTitle = "Feature Type Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			return CEntityNamedInitializerService.createTypeEntityView(project, clazz, "Hierarchy Configuration", true,
					"level", "canHaveChildren");
		} catch (final Exception e) {
			LOGGER.error("Error creating feature type view.");
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
						"Core Feature", "Essential product functionality"
				}, {
						"Enhancement", "Improvement to existing functionality"
				}, {
						"Integration", "Integration with external systems"
				}, {
						"Platform Feature", "Platform-level capabilities"
				}
		};
		final CCompany company = project.getCompany();
		// Use the concrete service bean to keep this initializer type-safe (no unchecked casts).
		final CFeatureTypeService featureTypeService = CSpringContext.getBean(CFeatureTypeService.class);
		initializeCompanyEntity(nameAndDescriptions, featureTypeService, company, minimal, (featureType, index) -> {
			featureType.setLevel(1);
			featureType.setCanHaveChildren(true);
		});
		// Assign the Agile Item Workflow so new features start in "To Do" (first Kanban column) when sprint planning populates the backlog.
		// Same pattern as CSprintTypeInitializerService assigning the Sprint Workflow to sprint types.
		final CWorkflowEntityService workflowService = CSpringContext.getBean(CWorkflowEntityService.class);
		final CWorkflowEntity agileWorkflow =
				workflowService.findByNameAndCompany("Agile Item Workflow", company).orElse(null);
		if (agileWorkflow != null) {
			featureTypeService.listByCompany(company).stream().filter(type -> type.getWorkflow() == null)
					.forEach(type -> {
						type.setWorkflow(agileWorkflow);
						featureTypeService.save(type);
					});
		} else {
			LOGGER.warn("Agile Item Workflow not found for company '{}' — feature types will have no workflow assigned",
					company.getName());
		}
	}
}
