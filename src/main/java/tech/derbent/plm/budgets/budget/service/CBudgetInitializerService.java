package tech.derbent.plm.budgets.budget.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CEntityOfProjectInitializerService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CProjectItemInitializerService;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.budgets.budget.domain.CBudget;
import tech.derbent.plm.comments.service.CCommentInitializerService;

public class CBudgetInitializerService extends CProjectItemInitializerService {

	private static final Class<?> clazz = CBudget.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBudgetInitializerService.class);
	private static final String menuOrder = Menu_Order_FINANCE + ".20";
	private static final String menuTitle = MenuTitle_FINANCE + ".Budgets";
	private static final String pageDescription = "Budget management";
	private static final String pageTitle = "Budget Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection detailSection = CEntityOfProjectInitializerService.createBasicView(project, clazz);
			CProjectItemInitializerService.createScreenLines(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			
			// Budget Amounts
			detailSection.addScreenLine(CDetailLinesService.createSection("Budget Amounts"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "currency"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "budgetAmount"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "actualCost"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "alertThreshold"));
			// PMBOK Earned Value Management (EVM)
			detailSection.addScreenLine(CDetailLinesService.createSection("Earned Value Management (PMBOK)"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "plannedValue"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "earnedValue"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			
			// Attachments section - standard section for ALL entities
			CAttachmentInitializerService.addDefaultSection(detailSection, clazz);
			// Comments section - standard section for discussion entities
			CCommentInitializerService.addDefaultSection(detailSection, clazz);
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating budget view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(
				List.of("id", "name", "description", "status", "project", "assignedTo", "createdBy", "createdDate"));
		grid.setEditableColumnFields(List.of("name", "assignedTo", "status"));
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
						"Q1 2026 Development Budget", "First quarter development budget with EVM tracking"
				}, {
						"Annual Infrastructure Budget 2026", "Infrastructure and hosting costs for the year"
				}
		};
		initializeProjectEntity(nameAndDescriptions,
				(CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)),
				project, minimal, (item, index) -> {
					final CBudget budget = (CBudget) item;
					final CUser user = CSpringContext.getBean(CUserService.class).getRandom(project.getCompany());
					budget.setAssignedTo(user);
					// PMBOK EVM - Add sample Earned Value Management data
					switch (index) {
					case 0 -> {
						budget.setBudgetAmount(new java.math.BigDecimal("250000.00"));
						budget.setPlannedValue(new java.math.BigDecimal("250000.00")); // PV = Budget Baseline
						budget.setEarnedValue(new java.math.BigDecimal("215000.00")); // EV = 86% complete
						budget.setActualCost(new java.math.BigDecimal("195000.00")); // AC = Efficient spending
						budget.setAlertThreshold(new java.math.BigDecimal("85.00")); // Alert at 85%
					}
					case 1 -> {
						budget.setBudgetAmount(new java.math.BigDecimal("120000.00"));
						budget.setPlannedValue(new java.math.BigDecimal("120000.00")); // PV = Budget Baseline
						budget.setEarnedValue(new java.math.BigDecimal("60000.00")); // EV = 50% complete
						budget.setActualCost(new java.math.BigDecimal("60000.00")); // AC = On track
						budget.setAlertThreshold(new java.math.BigDecimal("80.00")); // Alert at 80%
					}
					default -> throw new IllegalArgumentException("Unsupported budget sample index: " + index);
					}
				});
	}
}
