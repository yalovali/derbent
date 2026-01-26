package tech.derbent.api.config;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusInitializerService;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.page.service.CPageEntityInitializerService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.projects.service.CProjectService;
import tech.derbent.api.projects.service.CProjectTypeInitializerService;
import tech.derbent.api.projects.service.CProjectTypeService;
import tech.derbent.api.roles.service.CUserCompanyRoleInitializerService;
import tech.derbent.api.roles.service.CUserCompanyRoleService;
import tech.derbent.api.roles.service.CUserProjectRoleInitializerService;
import tech.derbent.api.roles.service.CUserProjectRoleService;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityInitializerService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CMasterInitializerService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.service.CWorkflowEntityInitializerService;
import tech.derbent.api.workflow.service.CWorkflowEntityService;
import tech.derbent.api.workflow.service.CWorkflowStatusRelationService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.setup.service.CSystemSettingsInitializerService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserInitializerService;
import tech.derbent.base.users.service.CUserProjectSettingsService;
import tech.derbent.base.users.service.CUserService;
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.activities.domain.CActivityPriority;
import tech.derbent.plm.activities.domain.CActivityType;
import tech.derbent.plm.activities.service.CActivityInitializerService;
import tech.derbent.plm.activities.service.CActivityPriorityInitializerService;
import tech.derbent.plm.activities.service.CActivityPriorityService;
import tech.derbent.plm.activities.service.CActivityService;
import tech.derbent.plm.activities.service.CActivityTypeInitializerService;
import tech.derbent.plm.activities.service.CActivityTypeService;
import tech.derbent.plm.agile.domain.CEpic;
import tech.derbent.plm.agile.domain.CEpicType;
import tech.derbent.plm.agile.domain.CFeature;
import tech.derbent.plm.agile.domain.CFeatureType;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.agile.domain.CUserStoryType;
import tech.derbent.plm.agile.service.CEpicInitializerService;
import tech.derbent.plm.agile.service.CEpicService;
import tech.derbent.plm.agile.service.CEpicTypeInitializerService;
import tech.derbent.plm.agile.service.CEpicTypeService;
import tech.derbent.plm.agile.service.CFeatureInitializerService;
import tech.derbent.plm.agile.service.CFeatureService;
import tech.derbent.plm.agile.service.CFeatureTypeInitializerService;
import tech.derbent.plm.agile.service.CFeatureTypeService;
import tech.derbent.plm.agile.service.CUserStoryInitializerService;
import tech.derbent.plm.agile.service.CUserStoryService;
import tech.derbent.plm.agile.service.CUserStoryTypeInitializerService;
import tech.derbent.plm.agile.service.CUserStoryTypeService;
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
import tech.derbent.plm.decisions.domain.CDecision;
import tech.derbent.plm.decisions.domain.CDecisionType;
import tech.derbent.plm.decisions.service.CDecisionInitializerService;
import tech.derbent.plm.decisions.service.CDecisionService;
import tech.derbent.plm.decisions.service.CDecisionTypeInitializerService;
import tech.derbent.plm.decisions.service.CDecisionTypeService;
import tech.derbent.plm.deliverables.deliverable.service.CDeliverableInitializerService;
import tech.derbent.plm.deliverables.deliverable.service.CDeliverableService;
import tech.derbent.plm.deliverables.deliverabletype.service.CDeliverableTypeInitializerService;
import tech.derbent.plm.deliverables.deliverabletype.service.CDeliverableTypeService;
import tech.derbent.plm.gannt.ganntviewentity.service.CGanntViewEntityInitializerService;
import tech.derbent.plm.gannt.ganntviewentity.service.CGanntViewEntityService;
import tech.derbent.plm.issues.issue.service.CIssueInitializerService;
import tech.derbent.plm.issues.issuetype.service.CIssueTypeInitializerService;
import tech.derbent.plm.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.plm.kanban.kanbanline.service.CKanbanLineInitializerService;
import tech.derbent.plm.kanban.kanbanline.service.CKanbanLineService;
import tech.derbent.plm.meetings.domain.CMeeting;
import tech.derbent.plm.meetings.domain.CMeetingType;
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
import tech.derbent.plm.orders.order.service.COrderService;
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
import tech.derbent.plm.projectexpenses.projectexpense.domain.CProjectExpense;
import tech.derbent.plm.projectexpenses.projectexpense.service.CProjectExpenseInitializerService;
import tech.derbent.plm.projectexpenses.projectexpense.service.CProjectExpenseService;
import tech.derbent.plm.projectexpenses.projectexpensetype.domain.CProjectExpenseType;
import tech.derbent.plm.projectexpenses.projectexpensetype.service.CProjectExpenseTypeInitializerService;
import tech.derbent.plm.projectexpenses.projectexpensetype.service.CProjectExpenseTypeService;
import tech.derbent.plm.projectincomes.projectincome.domain.CProjectIncome;
import tech.derbent.plm.projectincomes.projectincome.service.CProjectIncomeInitializerService;
import tech.derbent.plm.projectincomes.projectincome.service.CProjectIncomeService;
import tech.derbent.plm.projectincomes.projectincometype.domain.CProjectIncomeType;
import tech.derbent.plm.projectincomes.projectincometype.service.CProjectIncomeTypeInitializerService;
import tech.derbent.plm.projectincomes.projectincometype.service.CProjectIncomeTypeService;
import tech.derbent.plm.providers.provider.service.CProviderInitializerService;
import tech.derbent.plm.providers.provider.service.CProviderService;
import tech.derbent.plm.providers.providertype.service.CProviderTypeInitializerService;
import tech.derbent.plm.providers.providertype.service.CProviderTypeService;
import tech.derbent.plm.risklevel.risklevel.service.CRiskLevelInitializerService;
import tech.derbent.plm.risks.risk.service.CRiskInitializerService;
import tech.derbent.plm.risks.risk.service.CRiskService;
import tech.derbent.plm.risks.risktype.service.CRiskTypeInitializerService;
import tech.derbent.plm.risks.risktype.service.CRiskTypeService;
import tech.derbent.plm.sprints.service.CSprintInitializerService;
import tech.derbent.plm.sprints.service.CSprintTypeInitializerService;
import tech.derbent.plm.storage.storage.service.CStorageInitializerService;
import tech.derbent.plm.storage.storageitem.service.CStorageItemInitializerService;
import tech.derbent.plm.storage.storageitem.service.CStorageItemTypeInitializerService;
import tech.derbent.plm.storage.storagetype.service.CStorageTypeInitializerService;
import tech.derbent.plm.storage.transaction.service.CStorageTransactionInitializerService;
import tech.derbent.plm.teams.team.domain.CTeam;
import tech.derbent.plm.teams.team.service.CTeamInitializerService;
import tech.derbent.plm.teams.team.service.CTeamService;
import tech.derbent.plm.tickets.servicedepartment.service.CTicketServiceDepartmentInitializerService;
import tech.derbent.plm.tickets.ticket.domain.CTicket;
import tech.derbent.plm.tickets.ticket.service.CTicketInitializerService;
import tech.derbent.plm.tickets.ticket.service.CTicketService;
import tech.derbent.plm.tickets.ticketpriority.domain.CTicketPriority;
import tech.derbent.plm.tickets.ticketpriority.service.CTicketPriorityService;
import tech.derbent.plm.tickets.tickettype.domain.CTicketType;
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
	private final CEpicService epicService;
	private final CEpicTypeService epicTypeService;
	private final CFeatureService featureService;
	private final CFeatureTypeService featureTypeService;
	private final CUserStoryService userStoryService;
	private final CUserStoryTypeService userStoryTypeService;
	private final CCurrencyService currencyService;
	private final CDecisionService decisionService;
	private final CDecisionTypeService decisionTypeService;
	@PersistenceContext
	private EntityManager em;
	private final CGanntViewEntityService ganntViewEntityService;
	private final CGridEntityService gridEntityService;
	private final JdbcTemplate jdbcTemplate;
	private final CKanbanLineService kanbanLineService;
	private final CMeetingService meetingService;
	private final CMeetingTypeService meetingTypeService;
	private final COrderTypeService orderTypeService;
	private final CPageEntityService pageEntityService;
	private final CProjectItemStatusService statusService;
	// Service dependencies - injected via constructor
	private final CProjectService<CProject_Derbent> projectService;
	private final CRiskService riskService;
	private final CRiskTypeService riskTypeService;
	private final CDetailLinesService screenLinesService;
	private final CDetailSectionService screenService;
	private final ISessionService sessionService;
	private final CTeamService teamService;
	private final CTicketPriorityService ticketPriorityService;
	private final CTicketService ticketService;
	private final CTicketTypeService ticketTypeService;
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
		CSpringContext.getBean(CProjectTypeService.class);
		userService = CSpringContext.getBean(CUserService.class);
		userProjectSettingsService = CSpringContext.getBean(CUserProjectSettingsService.class);
		activityService = CSpringContext.getBean(CActivityService.class);
		activityPriorityService = CSpringContext.getBean(CActivityPriorityService.class);
		activityTypeService = CSpringContext.getBean(CActivityTypeService.class);
		epicService = CSpringContext.getBean(CEpicService.class);
		epicTypeService = CSpringContext.getBean(CEpicTypeService.class);
		featureService = CSpringContext.getBean(CFeatureService.class);
		featureTypeService = CSpringContext.getBean(CFeatureTypeService.class);
		userStoryService = CSpringContext.getBean(CUserStoryService.class);
		userStoryTypeService = CSpringContext.getBean(CUserStoryTypeService.class);
		riskTypeService = CSpringContext.getBean(CRiskTypeService.class);
		meetingTypeService = CSpringContext.getBean(CMeetingTypeService.class);
		CSpringContext.getBean(COrderService.class);
		orderTypeService = CSpringContext.getBean(COrderTypeService.class);
		meetingService = CSpringContext.getBean(CMeetingService.class);
		riskService = CSpringContext.getBean(CRiskService.class);
		decisionTypeService = CSpringContext.getBean(CDecisionTypeService.class);
		activityStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
		decisionService = CSpringContext.getBean(CDecisionService.class);
		currencyService = CSpringContext.getBean(CCurrencyService.class);
		screenService = CSpringContext.getBean(CDetailSectionService.class);
		screenLinesService = CSpringContext.getBean(CDetailLinesService.class);
		pageEntityService = CSpringContext.getBean(CPageEntityService.class);
		ganntViewEntityService = CSpringContext.getBean(CGanntViewEntityService.class);
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
		ticketService = CSpringContext.getBean(CTicketService.class);
		ticketPriorityService = CSpringContext.getBean(CTicketPriorityService.class);
		ticketTypeService = CSpringContext.getBean(CTicketTypeService.class);
		CSpringContext.getBean(CBudgetService.class);
		CSpringContext.getBean(CDeliverableService.class);
		CSpringContext.getBean(CDeliverableTypeService.class);
		CSpringContext.getBean(CProviderService.class);
		CSpringContext.getBean(CProviderTypeService.class);
		CSpringContext.getBean(CProductService.class);
		CSpringContext.getBean(CProductTypeService.class);
		CSpringContext.getBean(CProjectComponentService.class);
		CSpringContext.getBean(CProjectComponentTypeService.class);
		teamService = CSpringContext.getBean(CTeamService.class);
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
			ganntViewEntityService.deleteAllInBatch();
			userProjectRoleService.deleteAllInBatch();
			userCompanyRoleService.deleteAllInBatch();
			statusService.deleteAllInBatch();
			kanbanLineService.deleteAllInBatch();
			LOGGER.info("Fallback JPA deleteAllInBatch completed.");
		} catch (final Exception e) {
			LOGGER.error("Error during sample data cleanup", e);
			throw e;
		}
	}



	/** Initialize sample user project settings to demonstrate user-project relationships. This creates one user per role type per project.
	 * @param project2 */
	private void initializeSampleUserProjectSettings(final CProject<?> project, final boolean minimal) {
		try {
			for (final CUser user : userService.findAll()) {
				userProjectSettingsService.addUserToProject(user, project, "write");
				if (minimal) {
					return;
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample user project settings.");
			throw e;
		}
	}

	/** Initialize sample workflow entities to demonstrate workflow management.
	 * @param company the company to create workflow entities for
	 * @param minimal whether to create minimal sample data */
	private void initializeSampleWorkflowEntities(final CCompany company, final boolean minimal) {
		// Delegate to CWorkflowEntityInitializerService for workflow initialization
		CWorkflowEntityInitializerService.initializeSampleWorkflowEntities(company, minimal, statusService, userProjectRoleService,
				workflowEntityService, workflowStatusRelationService);
	}

	public boolean isDatabaseEmpty() {
		final long cnt = userService.count();
		LOGGER.info("User count = {}", cnt);
		return cnt == 0;
	}

	public void loadSampleData(final boolean minimal) throws Exception {
		try {
			// ========== NON-PROJECT RELATED INITIALIZATION PHASE ==========
			// **** CREATE COMPANY SAMPLES ****//
			CCompanyInitializerService.initializeSample(minimal);
			/* create sample projects */
			for (final CCompany company : CSpringContext.getBean(CCompanyService.class).list(Pageable.unpaged()).getContent()) {
				sessionService.setActiveCompany(company);
				CUserCompanyRoleInitializerService.initializeSample(company, minimal);
				CUserProjectRoleInitializerService.initializeSample(company, minimal);
				CUserInitializerService.initializeSample(company, minimal);
				CTicketServiceDepartmentInitializerService.initializeSample(company, minimal);
				if (minimal) {
					break;
				}
			}
			// ========== PROJECT-SPECIFIC INITIALIZATION PHASE ==========
			for (final CCompany company : CSpringContext.getBean(CCompanyService.class).list(Pageable.unpaged()).getContent()) {
				// sessionService.setActiveCompany(company);
				// later implement better user randomization logic
				LOGGER.info("Setting active company to: id:{}:{}", company.getId(), company.getName());
				final CUser user = userService.getRandomByCompany(company);
				Check.notNull(user, "No user found for company: " + company.getName());
				// Use new atomic method to set both company and user
				Check.notNull(sessionService, "SessionService is not initialized");
				sessionService.setActiveCompany(company);
				sessionService.setActiveUser(user); // Set company first, then user who is member of that company
				CProjectItemStatusInitializerService.initializeSample(company, minimal);
				CKanbanLineInitializerService.initializeSample(company, minimal); // must be after status
				CApprovalStatusInitializerService.initializeSample(company, minimal);
				initializeSampleWorkflowEntities(company, minimal);
				CProjectTypeInitializerService.initializeSample(company, minimal);
				CProject_DerbentInitializerService.initializeSample(company, minimal);
				final List<CProject_Derbent> projects = projectService.listByCompany(company);
				if (projects.isEmpty()) {
					LOGGER.warn("No projects found for company: {}. Skipping project-specific initialization.", company.getName());
					if (minimal) {
						break;
					}
					continue;
				}
				final CProject_Derbent sampleProject = projects.get(0);
				for (final CProject_Derbent project : projects) {
					LOGGER.info("Initializing sample data for project: {}:{} (company: {}:{})", project.getId(), project.getName(), company.getId(),
							company.getName());
					sessionService.setActiveProject(project);
					Check.instanceOf(project, CProject_Derbent.class, "Derbent initializer requires CProject_Derbent");
					final CProject_Derbent derbentProject = project;
					assignDefaultKanbanLine(derbentProject);
					CSystemSettingsInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					// Core system entities required for project operation
					CActivityInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CEpicInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CUserStoryInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CFeatureInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CUserInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CCompanyInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CDecisionInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CMeetingInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					COrderInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CProject_DerbentInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CRiskInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CRiskLevelInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CAssetInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CMilestoneInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CTicketInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CTicketServiceDepartmentInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CIssueInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CBudgetInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CProjectExpenseInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CProjectIncomeInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CDeliverableInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CProviderInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CProductInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CProductVersionInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CProjectComponentInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CProjectComponentVersionInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CStorageInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CStorageItemInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CStorageTransactionInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CTeamInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CUserProjectRoleInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CUserCompanyRoleInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					// Type/Status InitializerServices
					CProjectItemStatusInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CRiskTypeInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CAssetTypeInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CMilestoneTypeInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CTicketTypeInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CIssueTypeInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CBudgetTypeInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CProjectExpenseTypeInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CProjectIncomeTypeInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CDeliverableTypeInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CProviderTypeInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CProductTypeInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CProductVersionTypeInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CProjectComponentTypeInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CProjectComponentVersionTypeInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CStorageTypeInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CStorageItemTypeInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CProjectTypeInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CActivityTypeInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CEpicTypeInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CUserStoryTypeInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CFeatureTypeInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CActivityPriorityInitializerService.initialize(derbentProject, gridEntityService, screenService, pageEntityService);
					CApprovalStatusInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CCurrencyInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CDecisionTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CMeetingTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					COrderTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CWorkflowEntityInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					COrderApprovalInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CGanntViewEntityInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CGridEntityInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CMasterInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CPageEntityInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CSprintTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CSprintInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					/******************* TEST CASES **************************/
					CValidationCaseTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CValidationSuiteInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CValidationCaseInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CValidationSessionInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					/******************* SAMPLES **************************/
					// Project-specific type and configuration entities
					CSystemSettingsInitializerService.initializeSample(project, minimal);
					CGridEntityInitializerService.initializeSample(project, minimal);
					CMasterInitializerService.initializeSample(project, minimal);
					CCurrencyInitializerService.initializeSample(project, minimal);
					// types
					if (project.getId().equals(sampleProject.getId())) {
						CMeetingTypeInitializerService.initializeSample(sampleProject, minimal);
						CDecisionTypeInitializerService.initializeSample(sampleProject, minimal);
						COrderTypeInitializerService.initializeSample(sampleProject, minimal);
						CActivityTypeInitializerService.initializeSample(sampleProject, minimal);
						CEpicTypeInitializerService.initializeSample(sampleProject, minimal);
						CUserStoryTypeInitializerService.initializeSample(sampleProject, minimal);
						CFeatureTypeInitializerService.initializeSample(sampleProject, minimal);
						CIssueTypeInitializerService.initializeSample(sampleProject, minimal);
						CRiskTypeInitializerService.initializeSample(sampleProject, minimal);
						CAssetTypeInitializerService.initializeSample(sampleProject, minimal);
						CBudgetTypeInitializerService.initializeSample(sampleProject, minimal);
						CDeliverableTypeInitializerService.initializeSample(sampleProject, minimal);
						CMilestoneTypeInitializerService.initializeSample(sampleProject, minimal);
						CTicketTypeInitializerService.initializeSample(sampleProject, minimal);
						CProviderTypeInitializerService.initializeSample(sampleProject, minimal);
						CProductTypeInitializerService.initializeSample(sampleProject, minimal);
						CProductVersionTypeInitializerService.initializeSample(sampleProject, minimal);
						CProjectComponentTypeInitializerService.initializeSample(sampleProject, minimal);
						CProjectComponentVersionTypeInitializerService.initializeSample(sampleProject, minimal);
						CStorageTypeInitializerService.initializeSample(sampleProject, minimal);
						CStorageItemTypeInitializerService.initializeSample(sampleProject, minimal);
						CProjectExpenseTypeInitializerService.initializeSample(sampleProject, minimal);
						CProjectIncomeTypeInitializerService.initializeSample(sampleProject, minimal);
						CActivityPriorityInitializerService.initializeSample(sampleProject, minimal);
						CSprintTypeInitializerService.initializeSample(sampleProject, minimal);
						CValidationCaseTypeInitializerService.initializeSample(sampleProject, minimal);
					}
					CGanntViewEntityInitializerService.initializeSample(project, minimal);
					CStorageInitializerService.initializeSample(project, minimal);
					CStorageItemInitializerService.initializeSample(project, minimal);
					initializeSampleUserProjectSettings(project, minimal);
					// entities
					CDecisionInitializerService.initializeSample(project, minimal);
					CMeetingInitializerService.initializeSample(project, minimal);
					// Agile hierarchy - MUST be in order: Epic (root) → Feature → UserStory → Activity (leaf)
					final CEpic[] epics = CEpicInitializerService.initializeSample(project, minimal);
					final CFeature[] features = CFeatureInitializerService.initializeSample(project, minimal, epics[0], epics[1]);
					final CUserStory[] userStories = CUserStoryInitializerService.initializeSample(project, minimal, features[0], features[1]);
					CActivityInitializerService.initializeSample(project, minimal, userStories[0], userStories[1]);
					CAssetInitializerService.initializeSample(project, minimal);
					CBudgetInitializerService.initializeSample(project, minimal);
					CDeliverableInitializerService.initializeSample(project, minimal);
					CMilestoneInitializerService.initializeSample(project, minimal);
					CTicketInitializerService.initializeSample(project, minimal);
					CProviderInitializerService.initializeSample(project, minimal);
					CProductInitializerService.initializeSample(project, minimal);
					CProjectComponentInitializerService.initializeSample(project, minimal);
					CProjectExpenseInitializerService.initializeSample(project, minimal);
					CProjectIncomeInitializerService.initializeSample(project, minimal);
					COrderInitializerService.initializeSample(project, minimal);
					COrderApprovalInitializerService.initializeSample(project, minimal);
					CTeamInitializerService.initializeSample(project.getCompany(), minimal);
					CRiskInitializerService.initializeSample(project, minimal);
					CIssueInitializerService.initializeSample(project, minimal);
					CSprintInitializerService.initializeSample(project, minimal);
					// test management - must be after types are initialized
					CValidationSuiteInitializerService.initializeSample(project, minimal);
					CValidationCaseInitializerService.initializeSample(project, minimal);
					CValidationSessionInitializerService.initializeSample(project, minimal);
					CKanbanLineInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					if (minimal) {
						break;
					}
				}
				if (minimal) {
					break;
				}
			}
			LOGGER.info("Sample data initialization completed successfully");
		} catch (final Exception e) {
			LOGGER.error("Error loading sample data: {}", e.getMessage());
			throw e;
		}
	}

	public void reloadForced(final boolean minimal) throws Exception {
		LOGGER.info("Sample data reload (forced) started");
		clearSampleData(); // <<<<< ÖNCE TEMİZLE
		loadSampleData(minimal); // <<<<< SONRA YENİDEN OLUŞTUR
		LOGGER.info("Sample data reload (forced) finished");
	}

	private boolean teamExistsInCompany(final CCompany company, final String teamName) {
		Check.notNull(company, "Company cannot be null when checking team names");
		Check.notNull(teamName, "Team name cannot be null when checking team names");
		return teamService.findByCompany(company).stream().anyMatch(team -> teamName.equalsIgnoreCase(team.getName()));
	}
}
