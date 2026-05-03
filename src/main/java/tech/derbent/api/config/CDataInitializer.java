package tech.derbent.api.config;

import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.companies.service.CCompanyInitializerService;
import tech.derbent.api.companies.service.CCompanyService;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusInitializerService;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.imports.service.CDataImportInitializerService;
import tech.derbent.api.imports.service.CSystemInitExcelBootstrapService;
import tech.derbent.api.page.service.CPageEntityInitializerService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.projects.service.CProjectService;
import tech.derbent.api.projects.service.CProjectTypeInitializerService;
import tech.derbent.api.roles.service.CUserCompanyRoleInitializerService;
import tech.derbent.api.roles.service.CUserCompanyRoleService;
import tech.derbent.api.roles.service.CUserProjectRoleInitializerService;
import tech.derbent.api.roles.service.CUserProjectRoleService;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityInitializerService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CMasterInitializerService;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.CUserInitializerService;
import tech.derbent.api.users.service.CUserProjectSettingsService;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.service.CWorkflowEntityInitializerService;
import tech.derbent.api.workflow.service.CWorkflowEntityService;
import tech.derbent.api.workflow.service.CWorkflowStatusRelationService;
import tech.derbent.plm.activities.service.CActivityInitializerService;
import tech.derbent.plm.activities.service.CActivityPriorityInitializerService;
import tech.derbent.plm.activities.service.CActivityPriorityService;
import tech.derbent.plm.activities.service.CActivityService;
import tech.derbent.plm.activities.service.CActivityTypeInitializerService;
import tech.derbent.plm.activities.service.CActivityTypeService;
import tech.derbent.plm.agile.domain.CEpic;
import tech.derbent.plm.agile.domain.CFeature;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.agile.service.CEpicInitializerService;
import tech.derbent.plm.agile.service.CEpicTypeInitializerService;
import tech.derbent.plm.agile.service.CFeatureInitializerService;
import tech.derbent.plm.agile.service.CFeatureTypeInitializerService;
import tech.derbent.plm.agile.service.CUserStoryInitializerService;
import tech.derbent.plm.agile.service.CUserStoryTypeInitializerService;
import tech.derbent.plm.assets.asset.service.CAssetInitializerService;
import tech.derbent.plm.assets.asset.service.CAssetService;
import tech.derbent.plm.assets.assettype.service.CAssetTypeInitializerService;
import tech.derbent.plm.assets.assettype.service.CAssetTypeService;
import tech.derbent.plm.budgets.budget.service.CBudgetInitializerService;
import tech.derbent.plm.budgets.budget.service.CBudgetService;
import tech.derbent.plm.budgets.budgettype.service.CBudgetTypeInitializerService;
import tech.derbent.plm.components.component.service.CProjectComponentInitializerService;
import tech.derbent.plm.components.component.service.CProjectComponentService;
import tech.derbent.plm.components.componenttype.service.CProjectComponentTypeInitializerService;
import tech.derbent.plm.components.componenttype.service.CProjectComponentTypeService;
import tech.derbent.plm.components.componentversion.service.CProjectComponentVersionInitializerService;
import tech.derbent.plm.components.componentversiontype.service.CProjectComponentVersionTypeInitializerService;
import tech.derbent.plm.decisions.service.CDecisionInitializerService;
import tech.derbent.plm.decisions.service.CDecisionService;
import tech.derbent.plm.decisions.service.CDecisionTypeInitializerService;
import tech.derbent.plm.deliverables.deliverable.service.CDeliverableInitializerService;
import tech.derbent.plm.deliverables.deliverable.service.CDeliverableService;
import tech.derbent.plm.deliverables.deliverabletype.service.CDeliverableTypeInitializerService;
import tech.derbent.plm.deliverables.deliverabletype.service.CDeliverableTypeService;
import tech.derbent.plm.gnnt.gnntviewentity.service.CGnntViewEntityInitializerService;
import tech.derbent.plm.issues.issue.service.CIssueInitializerService;
import tech.derbent.plm.issues.issuetype.service.CIssueTypeInitializerService;
import tech.derbent.plm.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.plm.kanban.kanbanline.service.CKanbanLineInitializerService;
import tech.derbent.plm.kanban.kanbanline.service.CKanbanLineService;
import tech.derbent.plm.meetings.service.CMeetingInitializerService;
import tech.derbent.plm.meetings.service.CMeetingService;
import tech.derbent.plm.meetings.service.CMeetingTypeInitializerService;
import tech.derbent.plm.meetings.service.CMeetingTypeService;
import tech.derbent.plm.milestones.milestone.service.CMilestoneInitializerService;
import tech.derbent.plm.milestones.milestone.service.CMilestoneService;
import tech.derbent.plm.milestones.milestonetype.service.CMilestoneTypeInitializerService;
import tech.derbent.plm.milestones.milestonetype.service.CMilestoneTypeService;
import tech.derbent.plm.orders.approval.service.CApprovalStatusInitializerService;
import tech.derbent.plm.orders.approval.service.COrderApprovalInitializerService;
import tech.derbent.plm.orders.currency.service.CCurrencyInitializerService;
import tech.derbent.plm.orders.currency.service.CCurrencyService;
import tech.derbent.plm.orders.order.service.COrderInitializerService;
import tech.derbent.plm.orders.type.service.COrderTypeInitializerService;
import tech.derbent.plm.orders.type.service.COrderTypeService;
import tech.derbent.plm.products.product.service.CProductInitializerService;
import tech.derbent.plm.products.product.service.CProductService;
import tech.derbent.plm.products.producttype.service.CProductTypeInitializerService;
import tech.derbent.plm.products.producttype.service.CProductTypeService;
import tech.derbent.plm.products.productversion.service.CProductVersionInitializerService;
import tech.derbent.plm.products.productversiontype.service.CProductVersionTypeInitializerService;
import tech.derbent.plm.project.domain.CProject_Derbent;
import tech.derbent.plm.project.service.CProject_DerbentInitializerService;
import tech.derbent.plm.projectexpenses.projectexpense.service.CProjectExpenseInitializerService;
import tech.derbent.plm.projectexpenses.projectexpensetype.service.CProjectExpenseTypeInitializerService;
import tech.derbent.plm.projectincomes.projectincome.service.CProjectIncomeInitializerService;
import tech.derbent.plm.projectincomes.projectincometype.service.CProjectIncomeTypeInitializerService;
import tech.derbent.plm.providers.provider.service.CProviderInitializerService;
import tech.derbent.plm.providers.provider.service.CProviderService;
import tech.derbent.plm.providers.providertype.service.CProviderTypeInitializerService;
import tech.derbent.plm.providers.providertype.service.CProviderTypeService;
import tech.derbent.plm.requirements.requirement.service.CRequirementInitializerService;
import tech.derbent.plm.requirements.requirement.service.CRequirementService;
import tech.derbent.plm.requirements.requirementtype.service.CRequirementTypeInitializerService;
import tech.derbent.plm.requirements.requirementtype.service.CRequirementTypeService;
import tech.derbent.plm.risklevel.risklevel.service.CRiskLevelInitializerService;
import tech.derbent.plm.risks.risk.service.CRiskInitializerService;
import tech.derbent.plm.risks.risk.service.CRiskService;
import tech.derbent.plm.risks.risktype.service.CRiskTypeInitializerService;
import tech.derbent.plm.risks.risktype.service.CRiskTypeService;
import tech.derbent.plm.setup.service.CSystemSettings_DerbentInitializerService;
import tech.derbent.plm.sprints.planning.service.CSprintPlanningViewEntityInitializerService;
import tech.derbent.plm.sprints.service.CSprintInitializerService;
import tech.derbent.plm.sprints.service.CSprintTypeInitializerService;
import tech.derbent.plm.storage.storage.service.CStorageInitializerService;
import tech.derbent.plm.storage.storageitem.service.CStorageItemInitializerService;
import tech.derbent.plm.storage.storageitem.service.CStorageItemTypeInitializerService;
import tech.derbent.plm.storage.storagetype.service.CStorageTypeInitializerService;
import tech.derbent.plm.storage.transaction.service.CStorageTransactionInitializerService;
import tech.derbent.plm.teams.team.service.CTeamInitializerService;
import tech.derbent.plm.teams.team.service.CTeamService;
import tech.derbent.plm.tickets.servicedepartment.service.CTicketServiceDepartmentInitializerService;
import tech.derbent.plm.tickets.ticket.service.CTicketInitializerService;
import tech.derbent.plm.tickets.ticket.service.CTicketService;
import tech.derbent.plm.tickets.ticketpriority.service.CTicketPriorityService;
import tech.derbent.plm.tickets.tickettype.service.CTicketTypeInitializerService;
import tech.derbent.plm.tickets.tickettype.service.CTicketTypeService;
import tech.derbent.plm.validation.validationcase.service.CValidationCaseInitializerService;
import tech.derbent.plm.validation.validationcasetype.service.CValidationCaseTypeInitializerService;
import tech.derbent.plm.validation.validationsession.service.CValidationSessionInitializerService;
import tech.derbent.plm.validation.validationsuite.service.CValidationSuiteInitializerService;

/** CSampleDataInitializer - System Bootstrap and Sample Data Initialization This class serves dual purposes: 1. SYSTEM INITIALIZATION: Creates
 * essential base entities required for system operation - Companies, Projects, Users (core business entities) - Status entities (Activity, Meeting,
 * Decision statuses) - Type entities (Activity, Meeting, Decision, Order, User types) - System infrastructure (Screens, Grids, Pages) 2. SAMPLE DATA
 * CREATION: Populates demonstration data for user experience - Sample activities, meetings, decisions - Sample currencies and business data -
 * Realistic project data with relationships Key Design Principles: - Base system entities are marked as non-deletable for system stability - All
 * type/status entities have project-scoped unique constraints - Constants are used instead of magic strings for maintainability - Methods follow
 * consistent naming: initializeXxx() for system, initializeXxx(project) for project-specific
 * @author Derbent Team
 * @version 2.0 - Refactored for better organization and maintainability */
public class CDataInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDataInitializer.class);

	private static boolean isH2(final DataSource dataSource) {
		if (dataSource == null) {
			return false;
		}
		try (Connection connection = dataSource.getConnection()) {
			final String product = connection.getMetaData().getDatabaseProductName();
			return product != null && product.toLowerCase().contains("h2");
		} catch (final Exception e) {
			LOGGER.debug("Unable to detect database product for sample data cleanup: {}", e.getMessage());
			return false;
		}
	}

	private static boolean isPostgreSql(final DataSource dataSource) {
		if (dataSource == null) {
			return false;
		}
		try (Connection connection = dataSource.getConnection()) {
			final String product = connection.getMetaData().getDatabaseProductName();
			return product != null && product.toLowerCase().contains("postgresql");
		} catch (final Exception e) {
			LOGGER.debug("Unable to detect database product for sample data cleanup: {}", e.getMessage());
			return false;
		}
	}

	private final CActivityPriorityService activityPriorityService;
	private final CActivityService activityService;
	private final CProjectItemStatusService activityStatusService;
	private final CActivityTypeService activityTypeService;
	private final CCurrencyService currencyService;
	private final CDecisionService decisionService;
	@PersistenceContext
	private EntityManager em;
	private final CGridEntityService gridEntityService;
	private final JdbcTemplate jdbcTemplate;
	private final CKanbanLineService kanbanLineService;
	private final CMeetingService meetingService;
	private final CMeetingTypeService meetingTypeService;
	private final COrderTypeService orderTypeService;
	private final CPageEntityService pageEntityService;
	// Service dependencies - injected via constructor
	private final CProjectService<CProject_Derbent> projectService;
	private final CRiskService riskService;
	private final CRiskTypeService riskTypeService;
	private final CDetailLinesService screenLinesService;
	private final CDetailSectionService screenService;
	private final ISessionService sessionService;
	private final CProjectItemStatusService statusService;
	private final CUserCompanyRoleService userCompanyRoleService;
	private final CUserProjectRoleService userProjectRoleService;
	private final CUserProjectSettingsService userProjectSettingsService;
	private final CUserService userService;
	private final CWorkflowEntityService workflowEntityService;
	private final CWorkflowStatusRelationService workflowStatusRelationService;

	@SuppressWarnings ("unchecked")
	public CDataInitializer(final ISessionService sessionService) {
		LOGGER.info("DataInitializer starting - obtaining service beans from application context");
		Check.notNull(sessionService, "SessionService cannot be null");
		gridEntityService = CSpringContext.getBean(CGridEntityService.class);
		projectService = CSpringContext.getBean(CProjectService.class);
		userService = CSpringContext.getBean(CUserService.class);
		userProjectSettingsService = CSpringContext.getBean(CUserProjectSettingsService.class);
		activityService = CSpringContext.getBean(CActivityService.class);
		activityPriorityService = CSpringContext.getBean(CActivityPriorityService.class);
		activityTypeService = CSpringContext.getBean(CActivityTypeService.class);
		riskTypeService = CSpringContext.getBean(CRiskTypeService.class);
		meetingTypeService = CSpringContext.getBean(CMeetingTypeService.class);
		orderTypeService = CSpringContext.getBean(COrderTypeService.class);
		meetingService = CSpringContext.getBean(CMeetingService.class);
		riskService = CSpringContext.getBean(CRiskService.class);
		activityStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
		decisionService = CSpringContext.getBean(CDecisionService.class);
		currencyService = CSpringContext.getBean(CCurrencyService.class);
		screenService = CSpringContext.getBean(CDetailSectionService.class);
		screenLinesService = CSpringContext.getBean(CDetailLinesService.class);
		pageEntityService = CSpringContext.getBean(CPageEntityService.class);
		userProjectRoleService = CSpringContext.getBean(CUserProjectRoleService.class);
		userCompanyRoleService = CSpringContext.getBean(CUserCompanyRoleService.class);
		workflowEntityService = CSpringContext.getBean(CWorkflowEntityService.class);
		statusService = CSpringContext.getBean(CProjectItemStatusService.class);
		workflowStatusRelationService = CSpringContext.getBean(CWorkflowStatusRelationService.class);
		kanbanLineService = CSpringContext.getBean(CKanbanLineService.class);
		jdbcTemplate = CSpringContext.getBean(JdbcTemplate.class);
		this.sessionService = sessionService;
		CSpringContext.getBean(CAssetService.class);
		CSpringContext.getBean(CAssetTypeService.class);
		CSpringContext.getBean(CMilestoneService.class);
		CSpringContext.getBean(CMilestoneTypeService.class);
		CSpringContext.getBean(CTicketService.class);
		CSpringContext.getBean(CTicketPriorityService.class);
		CSpringContext.getBean(CTicketTypeService.class);
		CSpringContext.getBean(CBudgetService.class);
		CSpringContext.getBean(CDeliverableService.class);
		CSpringContext.getBean(CDeliverableTypeService.class);
		CSpringContext.getBean(CProviderService.class);
		CSpringContext.getBean(CProviderTypeService.class);
		CSpringContext.getBean(CProductService.class);
		CSpringContext.getBean(CProductTypeService.class);
		CSpringContext.getBean(CProjectComponentService.class);
		CSpringContext.getBean(CProjectComponentTypeService.class);
		CSpringContext.getBean(CRequirementService.class);
		CSpringContext.getBean(CRequirementTypeService.class);
		CSpringContext.getBean(CTeamService.class);
		LOGGER.info("All service beans obtained successfully");
	}
	// ========================================================================
	// PUBLIC API METHODS - Main entry points for initialization
	// ========================================================================

	private void assignDefaultKanbanLine(final CProject_Derbent project) {
		Check.notNull(project, "Project cannot be null when assigning kanban line");
		final CCompany company = project.getCompany();
		Check.notNull(company, "Company cannot be null when assigning kanban line to project");
		final CKanbanLine defaultKanbanLine = kanbanLineService.findDefaultForCompany(company).orElseGet(() -> kanbanLineService.getRandom(company));
		project.setKanbanLine(defaultKanbanLine);
		projectService.save(project);
	}

	@Transactional
	private void clearSampleData() {
		// LOGGER.debug("Clearing sample data from database (forced)");
		try {
			// ---- 1) PostgreSQL yolu: public şemadaki tabloları topla ve TRUNCATE et
			if (isPostgreSql(jdbcTemplate.getDataSource())) {
				try {
					final List<String> tableNames = jdbcTemplate.queryForList("""
							SELECT tablename
							FROM pg_tables
							WHERE schemaname = 'public'
							  AND tablename NOT IN ('flyway_schema_history')
							""", String.class);
					if (!tableNames.isEmpty()) {
						// Tabloları güvenli biçimde tırnakla ve join et
						final List<String> quoted = tableNames.stream().map(t -> "\"" + t + "\"").toList();
						final String joined = String.join(", ", quoted);
						final String sql = "TRUNCATE TABLE " + joined + " RESTART IDENTITY CASCADE";
						// LOGGER.debug("Executing: {}", sql);
						jdbcTemplate.execute(sql);
						LOGGER.info("All public tables truncated (PostgreSQL).");
						return; // İş bitti
					}
					LOGGER.warn("No user tables found to truncate in public schema.");
				} catch (final Exception pgEx) {
					LOGGER.warn("PostgreSQL truncate path failed. Falling back to JPA deletes. Cause: {}", pgEx.getMessage());
				}
			} else {
				LOGGER.debug("Skipping PostgreSQL truncate path; database is not PostgreSQL.");
			}
			if (isH2(jdbcTemplate.getDataSource())) {
				try {
					final List<String> tableNames = jdbcTemplate.queryForList("""
							SELECT TABLE_NAME
							FROM INFORMATION_SCHEMA.TABLES
							WHERE TABLE_SCHEMA = 'PUBLIC'
							  AND TABLE_TYPE = 'BASE TABLE'
							  AND TABLE_NAME NOT IN ('FLYWAY_SCHEMA_HISTORY')
							""", String.class);
					if (!tableNames.isEmpty()) {
						jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
						try {
							for (final String tableName : tableNames) {
								jdbcTemplate.execute("DELETE FROM \"" + tableName + "\"");
							}
						} finally {
							jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
						}
						LOGGER.info("All public tables cleared (H2).");
						return;
					}
					LOGGER.warn("No user tables found to clear in H2 PUBLIC schema.");
				} catch (final Exception h2Ex) {
					LOGGER.warn("H2 cleanup path failed. Falling back to JPA deletes. Cause: {}", h2Ex.getMessage());
				}
			}
			// ---- 2) Fallback: JPA batch silme (FK sırasına dikkat)
			meetingService.deleteAllInBatch();
			statusService.deleteAllInBatch();
			meetingTypeService.deleteAllInBatch();
			decisionService.deleteAllInBatch();
			activityService.deleteAllInBatch();
			activityPriorityService.deleteAllInBatch();
			activityStatusService.deleteAllInBatch();
			activityTypeService.deleteAllInBatch();
			riskTypeService.deleteAllInBatch();
			riskService.deleteAllInBatch();
			screenLinesService.deleteAllInBatch();
			screenService.deleteAllInBatch();
			currencyService.deleteAllInBatch();
			orderTypeService.deleteAllInBatch();
			userService.deleteAllInBatch();
			projectService.deleteAllInBatch();
			pageEntityService.deleteAllInBatch();
			userProjectRoleService.deleteAllInBatch();
			userCompanyRoleService.deleteAllInBatch();
			statusService.deleteAllInBatch();
			kanbanLineService.deleteAllInBatch();
			LOGGER.info("Fallback JPA deleteAllInBatch completed.");
		} catch (final Exception e) {
			LOGGER.error("Error during sample data cleanup reason={}", e.getMessage());
			throw e;
		}
	}

	public boolean isDatabaseEmpty() {
		final long cnt = userService.count();
		LOGGER.info("User count = {}", cnt);
		return cnt == 0;
	}

	/**
	 * @deprecated Sample creator methods are retired; use {@link #reloadForcedExcel(boolean)} which bootstraps from committed Excel templates.
	 */
	@Deprecated(forRemoval = true)
	public void loadSampleData(final boolean minimal) throws Exception {
		throw new UnsupportedOperationException("Sample data initialization via Java initializers is disabled. Use reloadForcedExcel(minimal) to import system_init.xlsx.");
	}

	public void reloadForced(final boolean minimal) throws Exception {
		// Backward-compatible entrypoint: sample creator methods are deprecated; Excel is the canonical source of seed data.
		reloadForcedExcel(minimal);
	}

	/**
	 * Excel-first DB reset.
	 *
	 * WHY: initializer-service sample creators are being retired; system_init.xlsx is the canonical source of sample data.
	 */
	public void reloadForcedExcel(final boolean minimal) throws Exception {
		LOGGER.info("DB reset (Excel-first) started");
		clearSampleData();
		final CSystemInitExcelBootstrapService excelBootstrap = CSpringContext.getBean(CSystemInitExcelBootstrapService.class);
		final var summary = excelBootstrap.bootstrapAfterReset(minimal);
		LOGGER.info("DB reset (Excel-first) finished: {}", summary.toUiSummary());
	}
}
