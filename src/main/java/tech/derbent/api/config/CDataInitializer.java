package tech.derbent.api.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusInitializerService;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityInitializerService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.domain.CActivityPriority;
import tech.derbent.app.activities.domain.CActivityType;
import tech.derbent.app.activities.service.CActivityInitializerService;
import tech.derbent.app.activities.service.CActivityPriorityInitializerService;
import tech.derbent.app.activities.service.CActivityPriorityService;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.activities.service.CActivityTypeInitializerService;
import tech.derbent.app.activities.service.CActivityTypeService;
import tech.derbent.app.assets.asset.service.CAssetInitializerService;
import tech.derbent.app.assets.asset.service.CAssetService;
import tech.derbent.app.assets.assettype.service.CAssetTypeInitializerService;
import tech.derbent.app.assets.assettype.service.CAssetTypeService;
import tech.derbent.app.budgets.budget.service.CBudgetInitializerService;
import tech.derbent.app.budgets.budget.service.CBudgetService;
import tech.derbent.app.budgets.budgettype.service.CBudgetTypeInitializerService;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.app.comments.domain.CCommentPriority;
import tech.derbent.app.comments.service.CCommentInitializerService;
import tech.derbent.app.comments.service.CCommentPriorityService;
import tech.derbent.app.comments.service.CCommentService;
import tech.derbent.app.comments.view.CCommentPriorityInitializerService;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.companies.service.CCompanyInitializerService;
import tech.derbent.app.companies.service.CCompanyService;
import tech.derbent.app.components.component.service.CProjectComponentInitializerService;
import tech.derbent.app.components.component.service.CProjectComponentService;
import tech.derbent.app.components.componenttype.service.CProjectComponentTypeInitializerService;
import tech.derbent.app.components.componenttype.service.CProjectComponentTypeService;
import tech.derbent.app.components.componentversion.service.CProjectComponentVersionInitializerService;
import tech.derbent.app.components.componentversiontype.service.CProjectComponentVersionTypeInitializerService;
import tech.derbent.app.decisions.domain.CDecision;
import tech.derbent.app.decisions.domain.CDecisionType;
import tech.derbent.app.decisions.service.CDecisionInitializerService;
import tech.derbent.app.decisions.service.CDecisionService;
import tech.derbent.app.decisions.service.CDecisionTypeInitializerService;
import tech.derbent.app.decisions.service.CDecisionTypeService;
import tech.derbent.app.deliverables.deliverable.service.CDeliverableInitializerService;
import tech.derbent.app.deliverables.deliverable.service.CDeliverableService;
import tech.derbent.app.deliverables.deliverabletype.service.CDeliverableTypeInitializerService;
import tech.derbent.app.deliverables.deliverabletype.service.CDeliverableTypeService;
import tech.derbent.app.gannt.ganntitem.service.CGanntItemInitializerService;
import tech.derbent.app.gannt.ganntviewentity.service.CGanntViewEntityInitializerService;
import tech.derbent.app.gannt.ganntviewentity.service.CGanntViewEntityService;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.app.kanban.kanbanline.service.CKanbanColumnInitializerService;
import tech.derbent.app.kanban.kanbanline.service.CKanbanLineInitializerService;
import tech.derbent.app.kanban.kanbanline.service.CKanbanLineService;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.meetings.domain.CMeetingType;
import tech.derbent.app.meetings.service.CMeetingInitializerService;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.meetings.service.CMeetingTypeInitializerService;
import tech.derbent.app.meetings.service.CMeetingTypeService;
import tech.derbent.app.milestones.milestone.service.CMilestoneInitializerService;
import tech.derbent.app.milestones.milestone.service.CMilestoneService;
import tech.derbent.app.milestones.milestonetype.service.CMilestoneTypeInitializerService;
import tech.derbent.app.milestones.milestonetype.service.CMilestoneTypeService;
import tech.derbent.app.orders.approval.service.CApprovalStatusInitializerService;
import tech.derbent.app.orders.approval.service.COrderApprovalInitializerService;
import tech.derbent.app.orders.currency.service.CCurrencyInitializerService;
import tech.derbent.app.orders.currency.service.CCurrencyService;
import tech.derbent.app.orders.order.service.COrderInitializerService;
import tech.derbent.app.orders.order.service.COrderService;
import tech.derbent.app.orders.type.service.COrderTypeInitializerService;
import tech.derbent.app.orders.type.service.COrderTypeService;
import tech.derbent.app.page.service.CPageEntityInitializerService;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.products.product.service.CProductInitializerService;
import tech.derbent.app.products.product.service.CProductService;
import tech.derbent.app.products.producttype.service.CProductTypeInitializerService;
import tech.derbent.app.products.producttype.service.CProductTypeService;
import tech.derbent.app.products.productversion.service.CProductVersionInitializerService;
import tech.derbent.app.products.productversiontype.service.CProductVersionTypeInitializerService;
import tech.derbent.app.projectexpenses.projectexpense.domain.CProjectExpense;
import tech.derbent.app.projectexpenses.projectexpense.service.CProjectExpenseInitializerService;
import tech.derbent.app.projectexpenses.projectexpense.service.CProjectExpenseService;
import tech.derbent.app.projectexpenses.projectexpensetype.domain.CProjectExpenseType;
import tech.derbent.app.projectexpenses.projectexpensetype.service.CProjectExpenseTypeInitializerService;
import tech.derbent.app.projectexpenses.projectexpensetype.service.CProjectExpenseTypeService;
import tech.derbent.app.projectincomes.projectincome.domain.CProjectIncome;
import tech.derbent.app.projectincomes.projectincome.service.CProjectIncomeInitializerService;
import tech.derbent.app.projectincomes.projectincome.service.CProjectIncomeService;
import tech.derbent.app.projectincomes.projectincometype.domain.CProjectIncomeType;
import tech.derbent.app.projectincomes.projectincometype.service.CProjectIncomeTypeInitializerService;
import tech.derbent.app.projectincomes.projectincometype.service.CProjectIncomeTypeService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.projects.service.CProjectInitializerService;
import tech.derbent.app.projects.service.CProjectService;
import tech.derbent.app.providers.provider.service.CProviderInitializerService;
import tech.derbent.app.providers.provider.service.CProviderService;
import tech.derbent.app.providers.providertype.service.CProviderTypeInitializerService;
import tech.derbent.app.providers.providertype.service.CProviderTypeService;
import tech.derbent.app.risklevel.risklevel.service.CRiskLevelInitializerService;
import tech.derbent.app.risks.risk.service.CRiskInitializerService;
import tech.derbent.app.risks.risk.service.CRiskService;
import tech.derbent.app.risks.risktype.service.CRiskTypeInitializerService;
import tech.derbent.app.risks.risktype.service.CRiskTypeService;
import tech.derbent.app.roles.domain.CUserCompanyRole;
import tech.derbent.app.roles.domain.CUserProjectRole;
import tech.derbent.app.roles.service.CUserCompanyRoleInitializerService;
import tech.derbent.app.roles.service.CUserCompanyRoleService;
import tech.derbent.app.roles.service.CUserProjectRoleInitializerService;
import tech.derbent.app.roles.service.CUserProjectRoleService;
import tech.derbent.app.sprints.service.CSprintInitializerService;
import tech.derbent.app.sprints.service.CSprintItemInitializerService;
import tech.derbent.app.sprints.service.CSprintTypeInitializerService;
import tech.derbent.app.teams.team.domain.CTeam;
import tech.derbent.app.teams.team.service.CTeamInitializerService;
import tech.derbent.app.teams.team.service.CTeamService;
import tech.derbent.app.tickets.ticket.domain.CTicket;
import tech.derbent.app.tickets.ticket.service.CTicketInitializerService;
import tech.derbent.app.tickets.ticket.service.CTicketService;
import tech.derbent.app.tickets.tickettype.domain.CTicketType;
import tech.derbent.app.tickets.tickettype.service.CTicketTypeInitializerService;
import tech.derbent.app.tickets.tickettype.service.CTicketTypeService;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.domain.CWorkflowStatusRelation;
import tech.derbent.app.workflow.service.CWorkflowEntityInitializerService;
import tech.derbent.app.workflow.service.CWorkflowEntityService;
import tech.derbent.app.workflow.service.CWorkflowStatusRelationInitializerService;
import tech.derbent.app.workflow.service.CWorkflowStatusRelationService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.setup.service.CSystemSettingsInitializerService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserCompanySettingInitializerService;
import tech.derbent.base.users.service.CUserInitializerService;
import tech.derbent.base.users.service.CUserProjectSettingsInitializerService;
import tech.derbent.base.users.service.CUserProjectSettingsService;
import tech.derbent.base.users.service.CUserService;

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

	private static final String COMPANY_OF_DANISMANLIK = "Of Stratejik Danışmanlık";
	// Company Names
	private static final String COMPANY_OF_TEKNOLOJI = "Of Teknoloji Çözümleri";
	private static final Logger LOGGER = LoggerFactory.getLogger(CDataInitializer.class);
	// Standard password for all users as per coding guidelines
	private static final String STANDARD_PASSWORD = "test123";
	// User Login Names
	private static final String USER_ADMIN = "admin";
	private static final String USER_ADMIN2 = "yasin";
	private final CActivityPriorityService activityPriorityService;
	private final CActivityService activityService;
	private final CProjectItemStatusService activityStatusService;
	private final CActivityTypeService activityTypeService;
	private final CCommentPriorityService commentPriorityService;
	private final CCommentService commentService;
	private final CCompanyService companyService;
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
	private final CProjectItemStatusService projectItemStatusService;
	// Service dependencies - injected via constructor
	private final CProjectService projectService;
	private final CRiskService riskService;
	private final CRiskTypeService riskTypeService;
	private final CDetailLinesService screenLinesService;
	private final CDetailSectionService screenService;
	private final ISessionService sessionService;
	private final CTeamService teamService;
	private final CTicketService ticketService;
	private final CTicketTypeService ticketTypeService;
	private final CUserCompanyRoleService userCompanyRoleService;
	private final CUserProjectRoleService userProjectRoleService;
	private final CUserProjectSettingsService userProjectSettingsService;
	private final CUserService userService;
	private final CWorkflowEntityService workflowEntityService;
	private final CWorkflowStatusRelationService workflowStatusRelationService;

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
		CSpringContext.getBean(COrderService.class);
		orderTypeService = CSpringContext.getBean(COrderTypeService.class);
		companyService = CSpringContext.getBean(CCompanyService.class);
		commentService = CSpringContext.getBean(CCommentService.class);
		commentPriorityService = CSpringContext.getBean(CCommentPriorityService.class);
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
		projectItemStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
		workflowStatusRelationService = CSpringContext.getBean(CWorkflowStatusRelationService.class);
		kanbanLineService = CSpringContext.getBean(CKanbanLineService.class);
		jdbcTemplate = CSpringContext.getBean(JdbcTemplate.class);
		this.sessionService = sessionService;
		CSpringContext.getBean(CAssetService.class);
		CSpringContext.getBean(CAssetTypeService.class);
		CSpringContext.getBean(CMilestoneService.class);
		CSpringContext.getBean(CMilestoneTypeService.class);
		ticketService = CSpringContext.getBean(CTicketService.class);
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

	@Transactional
	private void clearSampleData() {
		LOGGER.warn("Clearing sample data from database (forced)");
		try {
			// ---- 1) PostgreSQL yolu: public şemadaki tabloları topla ve TRUNCATE et
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
					LOGGER.warn("Executing: {}", sql);
					jdbcTemplate.execute(sql);
					LOGGER.info("All public tables truncated (PostgreSQL).");
					return; // İş bitti
				}
				LOGGER.warn("No user tables found to truncate in public schema.");
			} catch (final Exception pgEx) {
				LOGGER.warn("PostgreSQL truncate path failed or DB is not PostgreSQL. Falling back to JPA deletes. Cause: {}", pgEx.getMessage());
			}
			// ---- 2) Fallback: JPA batch silme (FK sırasına dikkat)
			commentService.deleteAllInBatch();
			commentPriorityService.deleteAllInBatch();
			meetingService.deleteAllInBatch();
			projectItemStatusService.deleteAllInBatch();
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
			companyService.deleteAllInBatch();
			projectService.deleteAllInBatch();
			pageEntityService.deleteAllInBatch();
			ganntViewEntityService.deleteAllInBatch();
			userProjectRoleService.deleteAllInBatch();
			userCompanyRoleService.deleteAllInBatch();
			projectItemStatusService.deleteAllInBatch();
			kanbanLineService.deleteAllInBatch();
			LOGGER.info("Fallback JPA deleteAllInBatch completed.");
		} catch (final Exception e) {
			LOGGER.error("Error during sample data cleanup", e);
			throw e;
		}
	}

	/** Creates consulting company. */
	private void createConsultingCompany() {
		final CCompany consulting = new CCompany(COMPANY_OF_DANISMANLIK);
		consulting.setDescription("Yönetim danışmanlığı ve stratejik planlama hizmetleri");
		consulting.setAddress("Merkez Mahallesi, Gülbahar Sokağı No:7, Of/Trabzon");
		consulting.setPhone("+90-462-751-0303");
		consulting.setEmail("merhaba@ofdanismanlik.com.tr");
		consulting.setWebsite("https://www.ofdanismanlik.com.tr");
		consulting.setTaxNumber("TR-456789123");
		consulting.setCompanyTheme("lumo-light");
		consulting.setCompanyLogoUrl("/assets/logos/consulting-logo.svg");
		consulting.setPrimaryColor("#4caf50");
		consulting.setWorkingHoursStart("08:30");
		consulting.setWorkingHoursEnd("17:30");
		consulting.setCompanyTimezone("Europe/Istanbul");
		consulting.setDefaultLanguage("tr");
		consulting.setEnableNotifications(true);
		consulting.setNotificationEmail("bildirim@ofdanismanlik.com.tr");
		companyService.save(consulting);
	}

	private void createProjectDigitalTransformation(final CCompany company) {
		final CProject project = new CProject("Digital Transformation Initiative", company);
		project.setDescription("Comprehensive digital transformation for enhanced customer experience");
		project.setActive(true);
		projectService.save(project);
	}

        private void createProjectInfrastructureUpgrade(final CCompany company) {
                final CProject project = new CProject("Infrastructure Upgrade Project", company);
                project.setDescription("Upgrading IT infrastructure for improved performance and scalability");
                projectService.save(project);
        }

        private void assignDefaultKanbanLine(final CProject project) {
                Check.notNull(project, "Project cannot be null when assigning kanban line");
                final CCompany company = project.getCompany();
                Check.notNull(company, "Company cannot be null when assigning kanban line to project");
                final CKanbanLine defaultKanbanLine = kanbanLineService.findDefaultForCompany(company)
                                .orElseGet(() -> kanbanLineService.getRandom(company));
                project.setKanbanLine(defaultKanbanLine);
                projectService.save(project);
        }

	/** Create sample comments for a decision.
	 * @param decision the decision to create comments for */
	private void createSampleCommentsForDecision(final CDecision decision) {
		try {
			// Comments require an activity - create a simple activity related to this decision
			final CActivityType activityType = activityTypeService.getRandom(decision.getProject());
			final CUser user = userService.getRandom();
			final CActivity activity = new CActivity("Review Decision: " + decision.getName(), decision.getProject());
			activity.setDescription("Activity to track review and implementation of decision");
			activity.setEntityType(activityType);
			activity.setAssignedTo(user);
			// Set initial status from workflow
			if (activityType != null && activityType.getWorkflow() != null) {
				final List<CProjectItemStatus> initialStatuses = projectItemStatusService.getValidNextStatuses(activity);
				if (!initialStatuses.isEmpty()) {
					activity.setStatus(initialStatuses.get(0));
				}
			}
			activityService.save(activity);
			// Create 2 comments for this activity
			final CCommentPriority priority1 = commentPriorityService.getRandom(decision.getProject());
			final CCommentPriority priority2 = commentPriorityService.getRandom(decision.getProject());
			final CUser commenter1 = userService.getRandom();
			final CUser commenter2 = userService.getRandom();
			final CComment comment1 =
					new CComment("This decision looks promising. We should prioritize implementation.", activity, commenter1, priority1);
			commentService.save(comment1);
			final CComment comment2 =
					new CComment("Agreed. Let's schedule a follow-up meeting to discuss resource allocation.", activity, commenter2, priority2);
			commentService.save(comment2);
			// LOGGER.debug("Created sample activity and comments for decision ID: {}", decision.getId());
		} catch (final Exception e) {
			LOGGER.error("Error creating comments for decision: {}", decision.getName(), e);
		}
	}

	/** Create sample comments for a meeting.
	 * @param meeting the meeting to create comments for
	 * @param minimal */
	private void createSampleCommentsForMeeting(final CMeeting meeting, final boolean minimal) {
		try {
			// Comments require an activity - create a simple activity related to this meeting
			final CActivityType activityType = activityTypeService.getRandom(meeting.getProject());
			final CUser user = userService.getRandom();
			final CActivity activity = new CActivity("Follow-up: " + meeting.getName(), meeting.getProject());
			activity.setDescription("Activity to track action items from meeting");
			activity.setEntityType(activityType);
			activity.setAssignedTo(user);
			// Set initial status from workflow
			if (activityType != null && activityType.getWorkflow() != null) {
				final List<CProjectItemStatus> initialStatuses = projectItemStatusService.getValidNextStatuses(activity);
				if (!initialStatuses.isEmpty()) {
					activity.setStatus(initialStatuses.get(0));
				}
			}
			activityService.save(activity);
			// Create 2 comments for this activity
			final CCommentPriority priority1 = commentPriorityService.getRandom(meeting.getProject());
			final CUser commenter1 = userService.getRandom();
			final CComment comment1 = new CComment("Meeting was productive. Action items are clearly defined.", activity, commenter1, priority1);
			commentService.save(comment1);
			if (minimal) {
				return;
			}
			final CUser commenter2 = userService.getRandom();
			final CCommentPriority priority2 = commentPriorityService.getRandom(meeting.getProject());
			final CComment comment2 = new CComment("I'll take ownership of the first two action items. Expected completion in 2 weeks.", activity,
					commenter2, priority2);
			commentService.save(comment2);
			// LOGGER.debug("Created sample activity and comments for meeting ID: {}", meeting.getId());
		} catch (final Exception e) {
			LOGGER.error("Error creating comments for meeting: {}", meeting.getName(), e);
		}
	}

	/** Creates a single user for a company with specified username and details.
	 * @param company            The company to create user for
	 * @param username           The username for the user
	 * @param firstname          The first name for the user
	 * @param phone              The phone number for the user
	 * @param profilePictureFile The filename of the profile picture (e.g., "admin.svg") */
	@Transactional (readOnly = false)
	private void createSingleUserForCompany(final CCompany company, final String username, final String firstname, final String phone,
			final String profilePictureFile) {
		final String companyShortName = company.getName().toLowerCase().replaceAll("[^a-z0-9]", "");
		final String userEmail = username + "@" + companyShortName + ".com.tr";
		final CUserCompanyRole companyRole = userCompanyRoleService.getRandom(company);
		final CUser user = userService.createLoginUser(username, STANDARD_PASSWORD, firstname, userEmail, company, companyRole);
		// Set user profile directly on entity
		final byte[] profilePictureBytes = loadProfilePictureData(profilePictureFile);
		user.setLastname(company.getName() + " Yöneticisi");
		user.setPhone(phone);
		user.setProfilePictureData(profilePictureBytes);
		user.setAttributeDisplaySectionsAsTabs(true);
		userService.save(user);
		// LOGGER.info("Created user {} for company {} with profile picture {}", username, company.getName(), profilePictureFile);
	}

	/** Creates technology startup company. */
	private void createTechCompany() {
		final CCompany techStartup = new CCompany(COMPANY_OF_TEKNOLOJI);
		techStartup.setDescription("Dijital dönüşüm için yenilikçi teknoloji çözümleri");
		techStartup.setAddress("Cumhuriyet Mahallesi, Atatürk Caddesi No:15, Of/Trabzon");
		techStartup.setPhone("+90-462-751-0101");
		techStartup.setEmail("iletisim@ofteknoloji.com.tr");
		techStartup.setWebsite("https://www.ofteknoloji.com.tr");
		techStartup.setTaxNumber("TR-123456789");
		techStartup.setCompanyTheme("lumo-dark");
		techStartup.setCompanyLogoUrl("/assets/logos/tech-logo.svg");
		techStartup.setPrimaryColor("#1976d2");
		techStartup.setWorkingHoursStart("09:00");
		techStartup.setWorkingHoursEnd("18:00");
		techStartup.setCompanyTimezone("Europe/Istanbul");
		techStartup.setDefaultLanguage("tr");
		techStartup.setEnableNotifications(true);
		techStartup.setNotificationEmail("bildirim@ofteknoloji.com.tr");
		companyService.save(techStartup);
	}

	@Transactional (readOnly = false)
	private void createUserForCompany(final CCompany company) {
		// Create first admin user
		createSingleUserForCompany(company, USER_ADMIN, "Admin", "+90-462-751-1001", "admin.svg");
		// Create second admin user
		createSingleUserForCompany(company, USER_ADMIN2, USER_ADMIN2, "+90-462-751-1002", "admin.svg");
		LOGGER.info("Created 2 admin users for company {}", company.getName());
	}

	/** Initialize sample activities with parent-child relationships for hierarchy demonstration.
	 * @param project the project to create activities for
	 * @param minimal whether to create minimal sample data */
	private void initializeSampleActivities(final CProject project, final boolean minimal) {
		try {
			// Get random values from database for dependencies
			final CActivityType type1 = activityTypeService.getRandom(project);
			final CActivityPriority priority1 = activityPriorityService.getRandom(project);
			final CUser user1 = userService.getRandom();
			// Create parent activity
			final CActivity activity1 = new CActivity("Phase 1: Planning and Analysis", project);
			activity1.setDescription("Initial planning phase covering requirements and architecture design");
			activity1.setEntityType(type1);
			activity1.setPriority(priority1);
			activity1.setAssignedTo(user1);
			activity1.setStartDate(LocalDate.now().plusDays((int) (Math.random() * 250)));
			activity1.setDueDate(activity1.getStartDate().plusDays((long) (Math.random() * 150)));
			// Set initial status from workflow
			if (type1 != null && type1.getWorkflow() != null) {
				final List<CProjectItemStatus> initialStatuses = projectItemStatusService.getValidNextStatuses(activity1);
				if (!initialStatuses.isEmpty()) {
					activity1.setStatus(initialStatuses.get(0));
				}
			}
			activityService.save(activity1);
			// Create child activity 1
			final CActivityType type2 = activityTypeService.getRandom(project);
			final CActivityPriority priority2 = activityPriorityService.getRandom(project);
			final CUser user2 = userService.getRandom();
			final CActivity activity2 = new CActivity("Requirements Gathering", project);
			activity2.setDescription("Collect and document business requirements");
			activity2.setEntityType(type2);
			activity2.setPriority(priority2);
			activity2.setAssignedTo(user2);
			activity2.setStartDate(LocalDate.now().plusDays((int) (Math.random() * 250)));
			activity2.setDueDate(activity2.getStartDate().plusDays((long) (Math.random() * 50)));
			// Set parent relationship
			activity2.setParent(activity1);
			// Set initial status from workflow
			if (type2 != null && type2.getWorkflow() != null) {
				final List<CProjectItemStatus> initialStatuses = projectItemStatusService.getValidNextStatuses(activity2);
				if (!initialStatuses.isEmpty()) {
					activity2.setStatus(initialStatuses.get(0));
				}
			}
			activityService.save(activity2);
			if (minimal) {
				return;
			}
			// Create child activity 2
			final CActivityType type3 = activityTypeService.getRandom(project);
			final CActivityPriority priority3 = activityPriorityService.getRandom(project);
			final CUser user3 = userService.getRandom();
			final CActivity activity3 = new CActivity("System Architecture Design", project);
			activity3.setDescription("Design system architecture and component interactions");
			activity3.setEntityType(type3);
			activity3.setPriority(priority3);
			activity3.setAssignedTo(user3);
			activity3.setStartDate(LocalDate.now().plusDays((int) (Math.random() * 50)));
			activity3.setDueDate(activity3.getStartDate().plusDays((long) (Math.random() * 50)));
			// Set parent relationship
			activity3.setParent(activity1);
			// Set initial status from workflow
			if (type3 != null && type3.getWorkflow() != null) {
				final List<CProjectItemStatus> initialStatuses = projectItemStatusService.getValidNextStatuses(activity3);
				if (!initialStatuses.isEmpty()) {
					activity3.setStatus(initialStatuses.get(0));
				}
			}
			activityService.save(activity3);
			LOGGER.debug("Created sample activities with parent-child relationships for project: {}", project.getName());
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample activities for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample activities for project: " + project.getName(), e);
		}
	}

	private void initializeSampleCompanyRoles(final CCompany company, final boolean minimal) {
		try {
			// Create three company roles: Admin, User, Guest (one for each role type)
			final String[][] companyRoles = {
					{
							"Company Admin", "Administrative role with full company access", "true", "false", "false"
					}, {
							"Company User", "Standard user role with regular access", "false", "true", "false"
					}, {
							"Company Guest", "Guest role with limited access", "false", "false", "true"
					}
			};
			for (final String[] roleData : companyRoles) {
				final CUserCompanyRole role = new CUserCompanyRole(roleData[0], company);
				role.setDescription(roleData[1]);
				role.setIsAdmin(Boolean.parseBoolean(roleData[2]));
				role.setIsUser(Boolean.parseBoolean(roleData[3]));
				role.setIsGuest(Boolean.parseBoolean(roleData[4]));
				role.setColor(CColorUtils.getRandomColor(true));
				userCompanyRoleService.save(role);
				if (minimal) {
					return;
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating company roles for company: {}", company.getName(), e);
			throw new RuntimeException("Failed to initialize company roles for company: " + company.getName(), e);
		}
	}

	/** Initialize 2 sample decisions per project with all fields populated.
	 * @param project the project to create decisions for */
	private void initializeSampleDecisions(final CProject project, final boolean minimal) {
		try {
			// Get random values from database for dependencies
			final CDecisionType type1 = decisionTypeService.getRandom(project);
			final CProjectItemStatus status1 = activityStatusService.getRandom(project.getCompany());
			final CUser user1 = userService.getRandom();
			// Create first decision
			final CDecision decision1 = new CDecision("Adopt Cloud-Native Architecture", project);
			decision1.setDescription("Strategic decision to migrate to cloud-native architecture for improved scalability");
			decision1.setEntityType(type1);
			decision1.setStatus(status1);
			decision1.setAssignedTo(user1);
			decision1.setAccountableUser(user1);
			decision1.setEstimatedCost(new java.math.BigDecimal("50000.00"));
			decision1.setImplementationDate(java.time.LocalDateTime.now().plusDays(30));
			decision1.setReviewDate(java.time.LocalDateTime.now().plusDays(90));
			decisionService.save(decision1);
			// Create first decision comments
			createSampleCommentsForDecision(decision1);
			if (minimal) {
				return;
			}
			// Create second decision
			final CDecisionType type2 = decisionTypeService.getRandom(project);
			final CProjectItemStatus status2 = activityStatusService.getRandom(project.getCompany());
			final CUser user2 = userService.getRandom();
			final CDecision decision2 = new CDecision("Implement Agile Methodology", project);
			decision2.setDescription("Operational decision to transition from waterfall to agile development methodology");
			decision2.setEntityType(type2);
			decision2.setStatus(status2);
			decision2.setAssignedTo(user2);
			decision2.setAccountableUser(user1);
			decision2.setEstimatedCost(new java.math.BigDecimal("25000.00"));
			decision2.setImplementationDate(java.time.LocalDateTime.now().plusDays(15));
			decision2.setReviewDate(java.time.LocalDateTime.now().plusDays(60));
			decisionService.save(decision2);
			// Create second decision comments
			createSampleCommentsForDecision(decision2);
			LOGGER.debug("Created 2 sample decisions with comments for project: {}", project.getName());
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample decisions for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample decisions for project: " + project.getName(), e);
		}
	}

	/** Initialize 2 sample meetings per project with all fields populated.
	 * @param project the project to create meetings for */
	private void initializeSampleMeetings(final CProject project, final boolean minimal) {
		try {
			// Get random values from database for dependencies
			final CMeetingType type1 = meetingTypeService.getRandom(project);
			final CUser user1 = userService.getRandom();
			final CMeetingType type2 = meetingTypeService.getRandom(project);
			final CUser user2 = userService.getRandom();
			// Create first meeting
			final CMeeting meeting1 = new CMeeting("Q1 Planning Session", project, type1);
			meeting1.setDescription("Quarterly planning session to review goals and set priorities");
			// Set initial status from workflow
			if (type1 != null && type1.getWorkflow() != null) {
				final List<CProjectItemStatus> initialStatuses = projectItemStatusService.getValidNextStatuses(meeting1);
				if (!initialStatuses.isEmpty()) {
					meeting1.setStatus(initialStatuses.get(0));
				}
			}
			meeting1.setAssignedTo(user1);
			meeting1.setResponsible(user2);
			meeting1.setStartDate(LocalDate.now().plusDays((int) (Math.random() * 250)));
			meeting1.setEndDate(meeting1.getStartDate().plusDays((int) (Math.random() * 3)));
			meeting1.setLocation("Conference Room A / Virtual");
			meeting1.setAgenda("1. Review Q4 achievements\n2. Discuss Q1 objectives\n3. Resource allocation\n4. Budget planning");
			meeting1.addParticipant(user1);
			meeting1.addParticipant(user2);
			meetingService.save(meeting1);
			// Create first meeting comments
			createSampleCommentsForMeeting(meeting1, minimal);
			if (minimal) {
				return;
			}
			// Create second meeting
			final CMeeting meeting2 = new CMeeting("Technical Architecture Review", project, type2);
			meeting2.setDescription("Review and discuss technical architecture decisions and implementation approach");
			// Set initial status from workflow
			if (type2 != null && type2.getWorkflow() != null) {
				final List<CProjectItemStatus> initialStatuses = projectItemStatusService.getValidNextStatuses(meeting2);
				if (!initialStatuses.isEmpty()) {
					meeting2.setStatus(initialStatuses.get(0));
				}
			}
			meeting2.setAssignedTo(user2);
			meeting2.setResponsible(user1);
			meeting2.setStartDate(LocalDate.now().plusDays((int) (Math.random() * 150)));
			meeting2.setEndDate(meeting2.getStartDate().plusDays((int) (Math.random() * 2)));
			meeting2.setLocation("Engineering Lab / Teams");
			meeting2.setAgenda(
					"1. Architecture proposal presentation\n2. Security considerations\n3. Scalability discussion\n4. Technology stack decisions");
			meeting2.addParticipant(user1);
			meeting2.addParticipant(user2);
			meetingService.save(meeting2);
			// Create second meeting comments
			createSampleCommentsForMeeting(meeting2, minimal);
			// Create third meeting as a child of the first meeting (follow-up meeting)
			final CMeetingType type3 = meetingTypeService.getRandom(project);
			final CMeeting meeting3 = new CMeeting("Q1 Planning Follow-up", project, type3);
			meeting3.setDescription("Follow-up meeting to review action items from Q1 Planning Session");
			// Set initial status from workflow
			if (type3 != null && type3.getWorkflow() != null) {
				final List<CProjectItemStatus> initialStatuses = projectItemStatusService.getValidNextStatuses(meeting3);
				if (!initialStatuses.isEmpty()) {
					meeting3.setStatus(initialStatuses.get(0));
				}
			}
			meeting3.setAssignedTo(user1);
			meeting3.setResponsible(user2);
			meeting3.setStartDate(LocalDate.now().plusDays((int) (Math.random() * 250)));
			meeting3.setEndDate(meeting2.getStartDate().plusDays((int) (Math.random() * 2)));
			meeting3.setLocation("Conference Room B / Virtual");
			meeting3.setAgenda("1. Review action items from Q1 Planning\n2. Progress updates\n3. Blockers and challenges");
			meeting3.addParticipant(user1);
			meeting3.addParticipant(user2);
			// Set parent relationship to first meeting
			meeting3.setParent(meeting1);
			meetingService.save(meeting3);
			LOGGER.debug("Created 3 sample meetings with parent-child relationship for project: {}", project.getName());
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample meetings for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample meetings for project: " + project.getName(), e);
		}
	}

	private void initializeSampleProjectExpenses(final CProject project, final boolean minimal) {
		try {
			final CProjectExpenseTypeService expenseTypeService = CSpringContext.getBean(CProjectExpenseTypeService.class);
			final CProjectExpenseService expenseService = CSpringContext.getBean(CProjectExpenseService.class);
			final CProjectExpenseType type1 = expenseTypeService.getRandom(project);
			final CUser user1 = userService.getRandom();
			final CProjectExpense expense1 = new CProjectExpense("Cloud Hosting Services", project);
			expense1.setDescription("Monthly cloud infrastructure hosting costs");
			expense1.setEntityType(type1);
			expense1.setAssignedTo(user1);
			if (type1 != null && type1.getWorkflow() != null) {
				final List<CProjectItemStatus> initialStatuses = projectItemStatusService.getValidNextStatuses(expense1);
				if (!initialStatuses.isEmpty()) {
					expense1.setStatus(initialStatuses.get(0));
				}
			}
			expenseService.save(expense1);
			if (minimal) {
				return;
			}
			final CProjectExpenseType type2 = expenseTypeService.getRandom(project);
			final CUser user2 = userService.getRandom();
			final CProjectExpense expense2 = new CProjectExpense("External Development Team", project);
			expense2.setDescription("Contracted external development services");
			expense2.setEntityType(type2);
			expense2.setAssignedTo(user2);
			if (type2 != null && type2.getWorkflow() != null) {
				final List<CProjectItemStatus> initialStatuses = projectItemStatusService.getValidNextStatuses(expense2);
				if (!initialStatuses.isEmpty()) {
					expense2.setStatus(initialStatuses.get(0));
				}
			}
			expenseService.save(expense2);
			// LOGGER.debug("Created sample project expenses for project: {}", project.getName());
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample project expenses for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample project expenses for project: " + project.getName(), e);
		}
	}

	private void initializeSampleProjectIncomes(final CProject project, final boolean minimal) {
		try {
			final CProjectIncomeTypeService incomeTypeService = CSpringContext.getBean(CProjectIncomeTypeService.class);
			final CProjectIncomeService incomeService = CSpringContext.getBean(CProjectIncomeService.class);
			final CProjectIncomeType type1 = incomeTypeService.getRandom(project);
			final CUser user1 = userService.getRandom();
			final CProjectIncome income1 = new CProjectIncome("Software License Revenue", project);
			income1.setDescription("Revenue from software license sales");
			income1.setEntityType(type1);
			income1.setAssignedTo(user1);
			if (type1 != null && type1.getWorkflow() != null) {
				final List<CProjectItemStatus> initialStatuses = projectItemStatusService.getValidNextStatuses(income1);
				if (!initialStatuses.isEmpty()) {
					income1.setStatus(initialStatuses.get(0));
				}
			}
			incomeService.save(income1);
			if (minimal) {
				return;
			}
			final CProjectIncomeType type2 = incomeTypeService.getRandom(project);
			final CUser user2 = userService.getRandom();
			final CProjectIncome income2 = new CProjectIncome("Support Contract Revenue", project);
			income2.setDescription("Annual support and maintenance contracts");
			income2.setEntityType(type2);
			income2.setAssignedTo(user2);
			if (type2 != null && type2.getWorkflow() != null) {
				final List<CProjectItemStatus> initialStatuses = projectItemStatusService.getValidNextStatuses(income2);
				if (!initialStatuses.isEmpty()) {
					income2.setStatus(initialStatuses.get(0));
				}
			}
			incomeService.save(income2);
			// LOGGER.debug("Created sample project incomes for project: {}", project.getName());
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample project incomes for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample project incomes for project: " + project.getName(), e);
		}
	}

	/** Initializes comprehensive activity data with available fields populated. */
	private void initializeSampleTeams(final CProject project, final boolean minimal) {
		try {
			final CCompany company = project.getCompany();
			final CUser user1 = userService.getRandom();
			final CTeam team1 = new CTeam("Development Team", company);
			team1.setDescription("Core development team responsible for implementation");
			team1.setTeamManager(user1);
			teamService.save(team1);
			if (minimal) {
				return;
			}
			final CUser user2 = userService.getRandom();
			final CTeam team2 = new CTeam("QA Team", company);
			team2.setDescription("Quality assurance and testing team");
			team2.setTeamManager(user2);
			teamService.save(team2);
			// LOGGER.debug("Created sample teams for project: {}", project.getName());
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample teams for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample teams for project: " + project.getName(), e);
		}
	}

	private void initializeSampleTickets(final CProject project, final boolean minimal) {
		try {
			final CTicketType type1 = ticketTypeService.getRandom(project);
			final CUser user1 = userService.getRandom();
			final CTicket ticket1 = new CTicket("Login Authentication Bug", project);
			ticket1.setDescription("Users unable to login with correct credentials");
			ticket1.setEntityType(type1);
			ticket1.setAssignedTo(user1);
			if (type1 != null && type1.getWorkflow() != null) {
				final List<CProjectItemStatus> initialStatuses = projectItemStatusService.getValidNextStatuses(ticket1);
				if (!initialStatuses.isEmpty()) {
					ticket1.setStatus(initialStatuses.get(0));
				}
			}
			ticketService.save(ticket1);
			if (minimal) {
				return;
			}
			final CTicketType type2 = ticketTypeService.getRandom(project);
			final CUser user2 = userService.getRandom();
			final CTicket ticket2 = new CTicket("Dashboard Customization Feature", project);
			ticket2.setDescription("Allow users to customize their dashboard layout");
			ticket2.setEntityType(type2);
			ticket2.setAssignedTo(user2);
			if (type2 != null && type2.getWorkflow() != null) {
				final List<CProjectItemStatus> initialStatuses = projectItemStatusService.getValidNextStatuses(ticket2);
				if (!initialStatuses.isEmpty()) {
					ticket2.setStatus(initialStatuses.get(0));
				}
			}
			ticketService.save(ticket2);
			// LOGGER.debug("Created sample tickets for project: {}", project.getName());
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample tickets for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample tickets for project: " + project.getName(), e);
		}
	}

	/** Initialize sample user project settings to demonstrate user-project relationships. This creates one user per role type per project.
	 * @param project2 */
	private void initializeSampleUserProjectSettings(final CProject project, final boolean minimal) {
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

	private void initializeSampleWorkflow(final String name, final CProject project, final List<CProjectItemStatus> statuses,
			final List<CUserProjectRole> roles) {
		Check.notNull(name, "Workflow name cannot be null");
		Check.notNull(project, "Project cannot be null");
		Check.notNull(statuses, "Statuses list cannot be null");
		Check.notNull(roles, "Roles list cannot be null");
		Check.notEmpty(statuses, "Statuses list cannot be empty");
		Check.notEmpty(roles, "Roles list cannot be empty");
		final CWorkflowEntity activityWorkflow = new CWorkflowEntity(name, project);
		activityWorkflow.setDescription("Defines status transitions for activities based on user roles");
		activityWorkflow.setIsActive(true);
		workflowEntityService.save(activityWorkflow);
		// Add status relations to the activity workflow
		for (int i = 0; i < Math.min(statuses.size() - 1, 3); i++) {
			final CWorkflowStatusRelation relation = new CWorkflowStatusRelation();
			relation.setWorkflowEntity(activityWorkflow);
			relation.setFromStatus(statuses.get(i));
			relation.setToStatus(statuses.get(i + 1));
			// Mark the first status (Not Started) as initial
			if (i == 0) {
				relation.setInitialStatus(true);
			}
			// Add first role to the transition
			if (!roles.isEmpty()) {
				relation.getRoles().add(roles.get(0));
			}
			workflowStatusRelationService.save(relation);
		}
	}

	/** Initialize sample workflow entities to demonstrate workflow management.
	 * @param project the project to create workflow entities for
	 * @param minimal whether to create minimal sample data */
	private void initializeSampleWorkflowEntities(final CProject project, final boolean minimal) {
		try {
			// Get available statuses for this project
			final List<CProjectItemStatus> statuses = projectItemStatusService.list(Pageable.unpaged()).getContent();
			Check.notEmpty(statuses, "No project item statuses found for project: " + project.getName());
			final List<CUserProjectRole> roles = userProjectRoleService.list(Pageable.unpaged()).getContent();
			Check.notEmpty(roles, "No user project roles found for project: " + project.getName());
			initializeSampleWorkflow("Activity Status Workflow", project, statuses, roles);
			initializeSampleWorkflow("Decision Status Workflow", project, statuses, roles);
			initializeSampleWorkflow("Meeting Status Workflow", project, statuses, roles);
			initializeSampleWorkflow("Risk Status Workflow", project, statuses, roles);
			initializeSampleWorkflow("Project Status Workflow", project, statuses, roles);
			LOGGER.debug("Created sample workflow entities with status relations for project: {}", project.getName());
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample workflow entities for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample workflow entities for project: " + project.getName(), e);
		}
	}

	public boolean isDatabaseEmpty() {
		final long cnt = userService.count();
		LOGGER.info("User count = {}", cnt);
		return cnt == 0;
	}

	/** Loads profile picture data from the profile-pictures directory.
	 * @param filename The SVG filename to load
	 * @return byte array of the SVG content, or null if not found */
	private byte[] loadProfilePictureData(final String filename) {
		try {
			// Try direct file path first (since profile-pictures is in project root)
			final Path filePath = java.nio.file.Paths.get("profile-pictures", filename);
			Check.isTrue(!filename.contains(".."), "Invalid filename: " + filename); // Prevent path traversal
			if (Files.exists(filePath)) {
				return Files.readAllBytes(filePath);
			}
			// Fallback: Load from classpath resources
			final var resource = getClass().getClassLoader().getResourceAsStream("profile-pictures/" + filename);
			Check.notNull(resource, "Profile picture resource not found in classpath: " + filename);
			return resource.readAllBytes();
		} catch (final Exception e) {
			LOGGER.error("Error loading profile picture: {}", filename, e);
			return null;
		}
	}

	public void loadSampleData(final boolean minimal) throws Exception {
		try {
			// ========== NON-PROJECT RELATED INITIALIZATION PHASE ==========
			// **** CREATE COMPANY SAMPLES ****//
			createTechCompany();
			if (!minimal) {
				createConsultingCompany();
			}
			/* create sample projects */
			for (final CCompany company : companyService.list(Pageable.unpaged()).getContent()) {
                                initializeSampleCompanyRoles(company, minimal);
                                createProjectDigitalTransformation(company);
                                if (!minimal) {
                                        createProjectInfrastructureUpgrade(company);
                                }
                                createUserForCompany(company);
                                CKanbanLineInitializerService.initializeSample(company, minimal);
                                if (minimal) {
                                        break;
                                }
                                // createUserFor(company);
                        }
			// ========== PROJECT-SPECIFIC INITIALIZATION PHASE ==========
			for (final CCompany company : companyService.list(Pageable.unpaged()).getContent()) {
				// sessionService.setActiveCompany(company);
				// later implement better user randomization logic
				LOGGER.info("Setting active company to: id:{}:{}", company.getId(), company.getName());
				final CUser user = userService.getRandomByCompany(company);
				Check.notNull(user, "No user found for company: " + company.getName());
				// Use new atomic method to set both company and user
				Check.notNull(sessionService, "SessionService is not initialized");
				sessionService.setActiveUser(user); // Set company first, then user who is member of that company
				CProjectItemStatusInitializerService.initializeSample(company, minimal);
				CApprovalStatusInitializerService.initializeSample(company, minimal);
				final List<CProject> projects = projectService.list(Pageable.unpaged()).getContent();
				for (final CProject project : projects) {
                                        LOGGER.info("Initializing sample data for project: {}:{} (company: {}:{})", project.getId(), project.getName(), company.getId(),
                                                        company.getName());
                                        sessionService.setActiveProject(project);
                                        assignDefaultKanbanLine(project);
                                        CSystemSettingsInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
                                        // Core system entities required for project operation
					CActivityInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CUserInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CCompanyInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CDecisionInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CCommentInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CMeetingInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					COrderInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CProjectInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CRiskInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CRiskLevelInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CAssetInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CMilestoneInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CTicketInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CBudgetInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CProjectExpenseInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CProjectIncomeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CDeliverableInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CProviderInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CProductInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CProductVersionInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CProjectComponentInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CProjectComponentVersionInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CTeamInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CUserProjectRoleInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CUserCompanyRoleInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					// Type/Status InitializerServices
					CProjectItemStatusInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CRiskTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CAssetTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CMilestoneTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CTicketTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CBudgetTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CProjectExpenseTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CProjectIncomeTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CDeliverableTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CProviderTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CProductTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CProductVersionTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CProjectComponentTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CProjectComponentVersionTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CActivityTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CActivityPriorityInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CApprovalStatusInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CCommentPriorityInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CCurrencyInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CDecisionTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CMeetingTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					COrderTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
                                        CWorkflowEntityInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
                                        CWorkflowStatusRelationInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
                                        COrderApprovalInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
                                        CUserProjectSettingsInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
                                        CUserCompanySettingInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
                                        CGanntViewEntityInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
                                        CGanntItemInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
                                        // CGanntInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
                                        CGridEntityInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
                                        CPageEntityInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
                                        CSprintTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
                                        CSprintInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					/******************* SAMPLES **************************/
					// Project-specific type and configuration entities
					CCurrencyInitializerService.initializeSample(project, minimal);
					CUserProjectRoleInitializerService.initializeSample(project, minimal);
					// types
					initializeSampleWorkflowEntities(project, minimal);
					CMeetingTypeInitializerService.initializeSample(project, minimal);
					CDecisionTypeInitializerService.initializeSample(project, minimal);
					COrderTypeInitializerService.initializeSample(project, minimal);
					CActivityTypeInitializerService.initializeSample(project, minimal);
					CRiskTypeInitializerService.initializeSample(project, minimal);
					CAssetTypeInitializerService.initializeSample(project, minimal);
					CBudgetTypeInitializerService.initializeSample(project, minimal);
					CDeliverableTypeInitializerService.initializeSample(project, minimal);
					CMilestoneTypeInitializerService.initializeSample(project, minimal);
					CTicketTypeInitializerService.initializeSample(project, minimal);
                                        CProviderTypeInitializerService.initializeSample(project, minimal);
                                        CProductTypeInitializerService.initializeSample(project, minimal);
                                        CProjectComponentTypeInitializerService.initializeSample(project, minimal);
                                        CProjectExpenseTypeInitializerService.initializeSample(project, minimal);
                                        CProjectIncomeTypeInitializerService.initializeSample(project, minimal);
                                        CGanntViewEntityInitializerService.initializeSample(project, minimal);
                                        CGanntItemInitializerService.initializeSample(project, minimal);
					CActivityPriorityInitializerService.initializeSample(project, minimal);
					CCommentPriorityInitializerService.initializeSample(project, minimal);
					CSprintTypeInitializerService.initializeSample(project, minimal);
					initializeSampleUserProjectSettings(project, minimal);
					// entities
					initializeSampleDecisions(project, minimal);
					initializeSampleMeetings(project, minimal);
					initializeSampleActivities(project, minimal);
					CAssetInitializerService.initializeSample(project, minimal);
					CBudgetInitializerService.initializeSample(project, minimal);
					CDeliverableInitializerService.initializeSample(project, minimal);
					CMilestoneInitializerService.initializeSample(project, minimal);
					initializeSampleTickets(project, minimal);
					CProviderInitializerService.initializeSample(project, minimal);
					CProductInitializerService.initializeSample(project, minimal);
					CProjectComponentInitializerService.initializeSample(project, minimal);
					initializeSampleProjectExpenses(project, minimal);
                                   initializeSampleProjectIncomes(project, minimal);
                                   initializeSampleTeams(project, minimal);
                                   CRiskInitializerService.initializeSample(project, minimal);
                                   CSprintInitializerService.initializeSample(project, minimal);
                                   CSprintItemInitializerService.initializeSample(project, minimal);
					CKanbanLineInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CKanbanColumnInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
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
}
