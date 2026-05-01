package tech.derbent.plm.projectexpenses.projectexpense.service;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.orders.currency.domain.CCurrency;
import tech.derbent.plm.orders.currency.service.CCurrencyService;
import tech.derbent.plm.projectexpenses.projectexpense.domain.CProjectExpense;

public class CProjectExpenseInitializerService extends CProjectItemInitializerService {

	private static final Class<?> clazz = CProjectExpense.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectExpenseInitializerService.class);
	private static final String menuOrder = Menu_Order_FINANCE + ".20";
	private static final String menuTitle = MenuTitle_FINANCE + ".Project Expenses";
	private static final String pageDescription = "Project Expense management";
	private static final String pageTitle = "Project Expense Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection detailSection =
					CEntityOfProjectInitializerService.createBasicView(project, clazz);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "amount"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "currency"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "expenseDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
			// Attachments section
			CAttachmentInitializerService.addDefaultSection(detailSection, clazz);
			// Comments section
			CCommentInitializerService.addDefaultSection(detailSection, clazz);
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating projectexpense view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "amount", "currency", "expenseDate", "status",
				"project", "assignedTo", "createdBy", "createdDate"));
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
		LOGGER.info("Creating comprehensive project expense sample data...");
		final String[][] nameAndDescriptions = {
				{
						"Cloud Infrastructure - January 2026", "AWS infrastructure costs for January"
				}, {
						"Software Licenses - Annual", "Annual software development tool licenses"
				}, {
						"Contractor Payment - Backend Development", "Payment for backend development services"
				}, {
						"Office Supplies & Equipment", "Development workstations and peripherals"
				}, {
						"Third-party API Costs", "Payment processing and mapping API usage"
				}, {
						"Training & Certification", "Team member technical certifications"
				}, {
						"Travel Expenses - Client Meeting", "Airfare and accommodation for client presentations"
				}, {
						"Marketing & Advertising", "Digital marketing campaign expenses"
				}
		};
		initializeProjectEntity(nameAndDescriptions,
				(CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)),
				project, minimal, (item, index) -> {
					final CProjectExpense expense = (CProjectExpense) item;
					final CUser user = CSpringContext.getBean(CUserService.class).getRandom(project.getCompany());
					expense.setAssignedTo(user);
					final CCurrency currency = CSpringContext.getBean(CCurrencyService.class).getRandom(project);
					expense.setCurrency(currency);
					// Set realistic amounts and dates for different expense types
					switch (index) {
					case 0 -> {
						expense.setAmount(new BigDecimal("3500.00"));
						expense.setExpenseDate(LocalDate.now().minusDays(25));
					}
					case 1 -> {
						expense.setAmount(new BigDecimal("12000.00"));
						expense.setExpenseDate(LocalDate.now().minusDays(60));
					}
					case 2 -> {
						expense.setAmount(new BigDecimal("8500.00"));
						expense.setExpenseDate(LocalDate.now().minusDays(15));
					}
					case 3 -> {
						expense.setAmount(new BigDecimal("4200.00"));
						expense.setExpenseDate(LocalDate.now().minusDays(45));
					}
					case 4 -> {
						expense.setAmount(new BigDecimal("750.00"));
						expense.setExpenseDate(LocalDate.now().minusDays(10));
					}
					case 5 -> {
						expense.setAmount(new BigDecimal("2500.00"));
						expense.setExpenseDate(LocalDate.now().minusDays(30));
					}
					case 6 -> {
						expense.setAmount(new BigDecimal("1800.00"));
						expense.setExpenseDate(LocalDate.now().minusDays(20));
					}
					case 7 -> {
						expense.setAmount(new BigDecimal("5000.00"));
						expense.setExpenseDate(LocalDate.now().minusDays(35));
					}
					default -> throw new IllegalArgumentException("Unsupported project expense sample index: " + index);
					}
				});
	}
}
