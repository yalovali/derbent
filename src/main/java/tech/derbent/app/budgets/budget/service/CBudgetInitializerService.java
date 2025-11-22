package tech.derbent.app.budgets.budget.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.app.budgets.budget.domain.CBudget;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;

public class CBudgetInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CBudget.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBudgetInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".20";
	private static final String menuTitle = MenuTitle_PROJECT + ".Budgets";
	private static final String pageDescription = "Budget management";
	private static final String pageTitle = "Budget Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
   
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating budget view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "status", "project", "assignedTo", "createdBy", "createdDate"));
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
		final String[][] nameAndDescriptions = {
				{
						"Q1 2024 Development Budget", "First quarter development budget allocation"
				}, {
						"Annual Marketing Budget 2024", "Annual marketing and promotional budget"
				}
		};
		initializeProjectEntity(nameAndDescriptions,
				(CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project, minimal,
				(item, index) -> {
					final CBudget budget = (CBudget) item;
					final CUser user = CSpringContext.getBean(CUserService.class).getRandom();
					budget.setAssignedTo(user);
				});
	}
}
