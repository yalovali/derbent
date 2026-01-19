package tech.derbent.app.projectincomes.projectincome.service;
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
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.app.projectincomes.projectincome.domain.CProjectIncome;
import tech.derbent.app.orders.currency.service.CCurrencyService;
import tech.derbent.app.orders.currency.domain.CCurrency;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;
import tech.derbent.app.attachments.service.CAttachmentInitializerService;
import tech.derbent.app.comments.service.CCommentInitializerService;

public class CProjectIncomeInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CProjectIncome.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectIncomeInitializerService.class);
	private static final String menuOrder = Menu_Order_FINANCE + ".20";
	private static final String menuTitle = MenuTitle_FINANCE + ".Project Incomes";
	private static final String pageDescription = "Project Income management";
	private static final String pageTitle = "Project Income Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "amount"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "currency"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "incomeDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));

			// Attachments section
			CAttachmentInitializerService.addAttachmentsSection(detailSection, clazz);

			// Comments section
			CCommentInitializerService.addCommentsSection(detailSection, clazz);

			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating projectincome view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "amount", "currency", "incomeDate",
				"status", "project", "assignedTo", "createdBy", "createdDate"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		LOGGER.info("Creating comprehensive project income sample data...");

		final String[][] nameAndDescriptions = {
				{ "Alpha Milestone Payment", "Payment received for Alpha release milestone completion" },
				{ "Beta Milestone Payment", "Payment received for Beta release milestone completion" },
				{ "License Revenue - Enterprise Tier", "Annual enterprise license subscription revenue" },
				{ "Consulting Services - Q1", "Q1 professional consulting services revenue" },
				{ "Support & Maintenance - Annual", "Annual support and maintenance contract revenue" },
				{ "Custom Development Project", "Custom feature development project revenue" },
				{ "Training Services", "On-site training and knowledge transfer revenue" },
				{ "Integration Services", "Third-party system integration services revenue" }
		};

		initializeProjectEntity(nameAndDescriptions,
				(CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project, minimal,
				(item, index) -> {
					final CProjectIncome income = (CProjectIncome) item;
					final CUser user = CSpringContext.getBean(CUserService.class).getRandom(project.getCompany());
					income.setAssignedTo(user);

					final CCurrency currency = CSpringContext.getBean(CCurrencyService.class).getRandom(project);
					income.setCurrency(currency);

					// Set realistic amounts and dates for different income types
						switch (index) {
							case 0: // Alpha Milestone
								income.setAmount(new BigDecimal("21600.00")); // Matches invoice with tax
								income.setIncomeDate(LocalDate.now().minusDays(40));
								break;
						case 1: // Beta Milestone
							income.setAmount(new BigDecimal("26400.00")); // Matches invoice with tax
							income.setIncomeDate(LocalDate.now().minusDays(5));
							break;
						case 2: // License Revenue
							income.setAmount(new BigDecimal("50000.00"));
							income.setIncomeDate(LocalDate.now().minusDays(90));
							break;
						case 3: // Consulting Q1
							income.setAmount(new BigDecimal("18000.00"));
							income.setIncomeDate(LocalDate.now().minusDays(75));
							break;
						case 4: // Support & Maintenance
							income.setAmount(new BigDecimal("24000.00"));
							income.setIncomeDate(LocalDate.now().minusDays(60));
							break;
						case 5: // Custom Development
							income.setAmount(new BigDecimal("15000.00"));
							income.setIncomeDate(LocalDate.now().minusDays(30));
							break;
						case 6: // Training
							income.setAmount(new BigDecimal("8500.00"));
							income.setIncomeDate(LocalDate.now().minusDays(45));
							break;
							case 7: // Integration
								income.setAmount(new BigDecimal("12000.00"));
								income.setIncomeDate(LocalDate.now().minusDays(55));
								break;
							default:
								throw new IllegalArgumentException("Unsupported project income sample index: " + index);
						}
					});

		LOGGER.info("Successfully created {} project income records", nameAndDescriptions.length);
	}
}
