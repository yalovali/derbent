package tech.derbent.api.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import tech.derbent.api.domains.CProjectItemStatus;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CGridInitializerService;
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
import tech.derbent.app.activities.service.CProjectItemStatusInitializerService;
import tech.derbent.app.activities.service.CProjectItemStatusService;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.app.comments.domain.CCommentPriority;
import tech.derbent.app.comments.service.CCommentPriorityService;
import tech.derbent.app.comments.service.CCommentService;
import tech.derbent.app.comments.view.CCommentPriorityInitializerService;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.companies.service.CCompanyInitializerService;
import tech.derbent.app.companies.service.CCompanyService;
import tech.derbent.app.decisions.domain.CDecision;
import tech.derbent.app.decisions.domain.CDecisionType;
import tech.derbent.app.decisions.service.CDecisionInitializerService;
import tech.derbent.app.decisions.service.CDecisionService;
import tech.derbent.app.decisions.service.CDecisionStatusInitializerService;
import tech.derbent.app.decisions.service.CDecisionStatusService;
import tech.derbent.app.decisions.service.CDecisionTypeInitializerService;
import tech.derbent.app.decisions.service.CDecisionTypeService;
import tech.derbent.app.gannt.service.CGanntViewEntityService;
import tech.derbent.app.meetings.domain.CMeetingStatus;
import tech.derbent.app.meetings.domain.CMeetingType;
import tech.derbent.app.meetings.service.CMeetingInitializerService;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.meetings.service.CMeetingStatusInitializerService;
import tech.derbent.app.meetings.service.CMeetingStatusService;
import tech.derbent.app.meetings.service.CMeetingTypeInitializerService;
import tech.derbent.app.meetings.service.CMeetingTypeService;
import tech.derbent.app.orders.domain.CApprovalStatus;
import tech.derbent.app.orders.domain.CCurrency;
import tech.derbent.app.orders.domain.COrderStatus;
import tech.derbent.app.orders.domain.COrderType;
import tech.derbent.app.orders.service.CApprovalStatusInitializerService;
import tech.derbent.app.orders.service.CApprovalStatusService;
import tech.derbent.app.orders.service.CCurrencyInitializerService;
import tech.derbent.app.orders.service.CCurrencyService;
import tech.derbent.app.orders.service.COrderInitializerService;
import tech.derbent.app.orders.service.COrderService;
import tech.derbent.app.orders.service.COrderStatusInitializerService;
import tech.derbent.app.orders.service.COrderStatusService;
import tech.derbent.app.orders.service.COrderTypeInitializerService;
import tech.derbent.app.orders.service.COrderTypeService;
import tech.derbent.app.page.service.CPageEntityInitializerService;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.projects.service.CProjectInitializerService;
import tech.derbent.app.projects.service.CProjectService;
import tech.derbent.app.risks.domain.CRiskStatus;
import tech.derbent.app.risks.service.CRiskInitializerService;
import tech.derbent.app.risks.service.CRiskService;
import tech.derbent.app.risks.service.CRiskStatusInitializerService;
import tech.derbent.app.risks.service.CRiskStatusService;
import tech.derbent.app.roles.domain.CUserCompanyRole;
import tech.derbent.app.roles.domain.CUserProjectRole;
import tech.derbent.app.roles.service.CUserCompanyRoleInitializerService;
import tech.derbent.app.roles.service.CUserCompanyRoleService;
import tech.derbent.app.roles.service.CUserProjectRoleInitializerService;
import tech.derbent.app.roles.service.CUserProjectRoleService;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.service.CWorkflowEntityInitializerService;
import tech.derbent.app.workflow.service.CWorkflowEntityService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.setup.service.CSystemSettingsInitializerService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserInitializerService;
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

	private static final String ACTIVITY_TYPE_DESIGN = "Design";
	// Activity Type Names
	private static final String ACTIVITY_TYPE_DEVELOPMENT = "Development";
	private static final String ACTIVITY_TYPE_DOCUMENTATION = "Documentation";
	private static final String ACTIVITY_TYPE_RESEARCH = "Research";
	private static final String ACTIVITY_TYPE_TESTING = "Testing";
	private static final String COMPANY_OF_DANISMANLIK = "Of Stratejik Danışmanlık";
	private static final String COMPANY_OF_ENDUSTRI = "Of Endüstri Dinamikleri";
	private static final String COMPANY_OF_SAGLIK = "Of Sağlık Teknolojileri";
	// Company Names
	private static final String COMPANY_OF_TEKNOLOJI = "Of Teknoloji Çözümleri";
	private static final Logger LOGGER = LoggerFactory.getLogger(CDataInitializer.class);
	// Standard password for all users as per coding guidelines
	private static final String STANDARD_PASSWORD = "test123";
	private static final String STATUS_CANCELLED = "Cancelled";
	private static final String STATUS_COMPLETED = "Completed";
	private static final String STATUS_IN_PROGRESS = "In Progress";
	// Status Names
	private static final String STATUS_NOT_STARTED = "Not Started";
	private static final String STATUS_ON_HOLD = "On Hold";
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
	private final CDecisionStatusService decisionStatusService;
	private final CDecisionTypeService decisionTypeService;
	@PersistenceContext
	private EntityManager em;
	private final CGanntViewEntityService ganntViewEntityService;
	private final CGridEntityService gridEntityService;
	private final JdbcTemplate jdbcTemplate;
	private final CMeetingService meetingService;
	private final CMeetingStatusService meetingStatusService;
	private final CMeetingTypeService meetingTypeService;
	private final COrderService orderService;
	private final COrderStatusService orderStatusService;
	private final COrderTypeService orderTypeService;
	private final CPageEntityService pageEntityService;
	// Service dependencies - injected via constructor
	private final CProjectService projectService;
	private final CRiskService riskService;
	private final CRiskStatusService riskStatusService;
	private final CDetailLinesService screenLinesService;
	private final CDetailSectionService screenService;
	private final ISessionService sessionService;
	private final CUserCompanyRoleService userCompanyRoleService;
	private final CUserProjectRoleService userProjectRoleService;
	private final CUserProjectSettingsService userProjectSettingsService;
	private final CUserService userService;
	private final CWorkflowEntityService workflowEntityService;

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
		meetingTypeService = CSpringContext.getBean(CMeetingTypeService.class);
		orderService = CSpringContext.getBean(COrderService.class);
		orderTypeService = CSpringContext.getBean(COrderTypeService.class);
		orderStatusService = CSpringContext.getBean(COrderStatusService.class);
		companyService = CSpringContext.getBean(CCompanyService.class);
		commentService = CSpringContext.getBean(CCommentService.class);
		commentPriorityService = CSpringContext.getBean(CCommentPriorityService.class);
		meetingService = CSpringContext.getBean(CMeetingService.class);
		riskService = CSpringContext.getBean(CRiskService.class);
		meetingStatusService = CSpringContext.getBean(CMeetingStatusService.class);
		decisionStatusService = CSpringContext.getBean(CDecisionStatusService.class);
		decisionTypeService = CSpringContext.getBean(CDecisionTypeService.class);
		activityStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
		decisionService = CSpringContext.getBean(CDecisionService.class);
		currencyService = CSpringContext.getBean(CCurrencyService.class);
		screenService = CSpringContext.getBean(CDetailSectionService.class);
		screenLinesService = CSpringContext.getBean(CDetailLinesService.class);
		pageEntityService = CSpringContext.getBean(CPageEntityService.class);
		ganntViewEntityService = CSpringContext.getBean(CGanntViewEntityService.class);
		riskStatusService = CSpringContext.getBean(CRiskStatusService.class);
		userProjectRoleService = CSpringContext.getBean(CUserProjectRoleService.class);
		userCompanyRoleService = CSpringContext.getBean(CUserCompanyRoleService.class);
		workflowEntityService = CSpringContext.getBean(CWorkflowEntityService.class);
		Check.notNull(activityService, "ActivityService bean not found");
		Check.notNull(activityPriorityService, "ActivityPriorityService bean not found");
		Check.notNull(activityStatusService, "ProjectItemStatusService bean not found");
		Check.notNull(activityTypeService, "ActivityTypeService bean not found");
		Check.notNull(commentService, "CommentService bean not found");
		Check.notNull(commentPriorityService, "CommentPriorityService bean not found");
		Check.notNull(companyService, "CompanyService bean not found");
		Check.notNull(currencyService, "CurrencyService bean not found");
		Check.notNull(decisionService, "DecisionService bean not found");
		Check.notNull(decisionStatusService, "DecisionStatusService bean not found");
		Check.notNull(decisionTypeService, "DecisionTypeService bean not found");
		Check.notNull(meetingService, "MeetingService bean not found");
		Check.notNull(meetingStatusService, "MeetingStatusService bean not found");
		Check.notNull(meetingTypeService, "MeetingTypeService bean not found");
		Check.notNull(orderService, "OrderService bean not found");
		Check.notNull(orderStatusService, "OrderStatusService bean not found");
		Check.notNull(orderTypeService, "OrderTypeService bean not found");
		Check.notNull(pageEntityService, "PageEntityService bean not found");
		Check.notNull(projectService, "ProjectService bean not found");
		Check.notNull(riskService, "RiskService bean not found");
		Check.notNull(riskStatusService, "RiskStatusService bean not found");
		Check.notNull(screenService, "ScreenService bean not found");
		Check.notNull(screenLinesService, "ScreenLinesService bean not found");
		Check.notNull(userService, "UserService bean not found");
		Check.notNull(userProjectRoleService, "UserProjectRoleService bean not found");
		Check.notNull(userCompanyRoleService, "UserCompanyRoleService bean not found");
		Check.notNull(userProjectSettingsService, "UserProjectSettingsService bean not found");
		Check.notNull(workflowEntityService, "WorkflowEntityService bean not found");
		LOGGER.info("All service beans obtained successfully");
		final DataSource ds = CSpringContext.getBean(DataSource.class);
		jdbcTemplate = new JdbcTemplate(ds);
		this.sessionService = sessionService;
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
				} else {
					LOGGER.warn("No user tables found to truncate in public schema.");
				}
			} catch (final Exception pgEx) {
				LOGGER.warn("PostgreSQL truncate path failed or DB is not PostgreSQL. Falling back to JPA deletes. Cause: {}", pgEx.getMessage());
			}
			// ---- 2) Fallback: JPA batch silme (FK sırasına dikkat)
			commentService.deleteAllInBatch();
			commentPriorityService.deleteAllInBatch();
			meetingService.deleteAllInBatch();
			meetingStatusService.deleteAllInBatch();
			meetingTypeService.deleteAllInBatch();
			decisionService.deleteAllInBatch();
			decisionStatusService.deleteAllInBatch();
			// decisionTypeService.deleteAllInBatch(); // ekleyeceksen
			activityService.deleteAllInBatch();
			activityPriorityService.deleteAllInBatch();
			activityStatusService.deleteAllInBatch();
			activityTypeService.deleteAllInBatch();
			riskService.deleteAllInBatch();
			screenLinesService.deleteAllInBatch();
			screenService.deleteAllInBatch();
			currencyService.deleteAllInBatch();
			orderTypeService.deleteAllInBatch();
			orderStatusService.deleteAllInBatch();
			userService.deleteAllInBatch();
			companyService.deleteAllInBatch();
			projectService.deleteAllInBatch();
			pageEntityService.deleteAllInBatch();
			ganntViewEntityService.deleteAllInBatch();
			riskStatusService.deleteAllInBatch();
			userProjectRoleService.deleteAllInBatch();
			userCompanyRoleService.deleteAllInBatch();
			LOGGER.info("Fallback JPA deleteAllInBatch completed.");
		} catch (final Exception e) {
			LOGGER.error("Error during sample data cleanup", e);
			throw e;
		}
	}

	private void createActivityPriority(final CProject project, final String name, final String description, final String color,
			final Integer priorityLevel, final boolean isDefault, final int sortOrder) {
		final CActivityPriority priority = new CActivityPriority(name, project, color, sortOrder);
		priority.setDescription(description);
		priority.setPriorityLevel(priorityLevel);
		priority.setIsDefault(isDefault);
		activityPriorityService.save(priority);
	}

	private void createApprovalStatus(final String name, final CProject project, final String description, final String color, final boolean isFinal,
			final int sortOrder) {
		final CApprovalStatus status = new CApprovalStatus(name, project);
		status.setDescription(description);
		status.setColor(color);
		status.setSortOrder(sortOrder);
		CSpringContext.getBean(CApprovalStatusService.class).save(status);
	}

	/** Creates backend development activity. */
	private void createCommentPriority(final CProject project, final String name, final String description, final String color,
			final Integer priorityLevel, final boolean isDefault, final int sortOrder) {
		final CCommentPriority priority = new CCommentPriority(name, project, color, sortOrder);
		priority.setDescription(description);
		priority.setPriorityLevel(priorityLevel);
		priority.setDefault(isDefault);
		commentPriorityService.save(priority);
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

	/** Create a currency entity with proper validation. */
	private void createCurrency(final CProject project, final String code, final String name, final String symbol) {
		try {
			final CCurrency currency = new CCurrency(project, name);
			currency.setCurrencyCode(code);
			currency.setCurrencySymbol(symbol);
			// Add business description
			currency.setDescription(String.format("%s (%s) - International currency for business transactions", name, code));
			currencyService.save(currency);
		} catch (final Exception e) {
			LOGGER.error("Error creating currency: {} ({})", name, code, e);
			throw new RuntimeException("Failed to create currency: " + code, e);
		}
	}

	/** Creates healthcare company. */
	private void createHealthcareCompany() {
		final CCompany healthcare = new CCompany(COMPANY_OF_SAGLIK);
		healthcare.setDescription("İleri tıp teknolojisi ve sağlık çözümleri");
		healthcare.setAddress("Yeni Mahalle, Sağlık Sokağı No:21, Of/Trabzon");
		healthcare.setPhone("+90-462-751-0404");
		healthcare.setEmail("iletisim@ofsaglik.com.tr");
		healthcare.setWebsite("https://www.ofsaglik.com.tr");
		healthcare.setTaxNumber("TR-789123456");
		healthcare.setCompanyTheme("lumo-light");
		healthcare.setCompanyLogoUrl("/assets/logos/healthcare-logo.svg");
		healthcare.setPrimaryColor("#f44336");
		healthcare.setWorkingHoursStart("07:00");
		healthcare.setWorkingHoursEnd("19:00");
		healthcare.setCompanyTimezone("Europe/Istanbul");
		healthcare.setDefaultLanguage("tr");
		healthcare.setEnableNotifications(true);
		healthcare.setNotificationEmail("acil@ofsaglik.com.tr");
		companyService.save(healthcare);
	}

	/** Creates manufacturing company. */
	private void createManufacturingCompany() {
		final CCompany manufacturing = new CCompany(COMPANY_OF_ENDUSTRI);
		manufacturing.setDescription("Hassas mühendislik bileşenlerinde lider üretici");
		manufacturing.setAddress("Sanayi Mahallesi, İstiklal Caddesi No:42, Of/Trabzon");
		manufacturing.setPhone("+90-462-751-0202");
		manufacturing.setEmail("bilgi@ofendüstri.com.tr");
		manufacturing.setWebsite("https://www.ofendüstri.com.tr");
		manufacturing.setTaxNumber("TR-987654321");
		manufacturing.setCompanyTheme("lumo-dark");
		manufacturing.setCompanyLogoUrl("/assets/logos/manufacturing-logo.svg");
		manufacturing.setPrimaryColor("#ff9800");
		manufacturing.setWorkingHoursStart("06:00");
		manufacturing.setWorkingHoursEnd("18:00");
		manufacturing.setCompanyTimezone("Europe/Istanbul");
		manufacturing.setDefaultLanguage("tr");
		manufacturing.setEnableNotifications(true);
		manufacturing.setNotificationEmail("operasyon@ofendüstri.com.tr");
		companyService.save(manufacturing);
	}

	private void createMeetingStatus(final String name, final CProject project, final String description, final String color, final boolean isFinal,
			final int sortOrder) {
		final CMeetingStatus status = new CMeetingStatus(name, project);
		status.setDescription(description);
		status.setColor(color);
		status.setFinalStatus(isFinal);
		status.setSortOrder(sortOrder);
		meetingStatusService.save(status);
	}

	/** Create order status entity. */
	private void createOrderStatus(final String name, final CProject project, final String description, final String color, final boolean isFinal,
			final int sortOrder) {
		try {
			final COrderStatus status = new COrderStatus(name, project);
			status.setDescription(description);
			status.setColor(color);
			status.setSortOrder(sortOrder);
			// Note: isFinal parameter is not used as COrderStatus doesn't have this field
			orderStatusService.save(status);
		} catch (final Exception e) {
			LOGGER.error("Error creating order status: {} for project: {}", name, project.getName(), e);
			throw new RuntimeException("Failed to create order status: " + name, e);
		}
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

	private void createProjectItemStatus(final String name, final CProject project, final String description, final String color,
			final boolean isFinal, final int sortOrder) {
		final CProjectItemStatus status = new CProjectItemStatus(name, project);
		status.setDescription(description);
		status.setColor(color);
		status.setSortOrder(sortOrder);
		activityStatusService.save(status);
	}

	private void createProjectProductDevelopment(final CCompany company) {
		final CProject project = new CProject("New Product Development", company);
		project.setDescription("Development of innovative products to expand market reach");
		projectService.save(project);
	}

	private void createRiskStatus(final String name, final CProject project, final String description, final String color, final boolean isFinal,
			final int sortOrder) {
		final CRiskStatus status = new CRiskStatus(name, project);
		status.setDescription(description);
		status.setColor(color);
		status.setSortOrder(sortOrder);
		riskStatusService.save(status);
	}

	/** Create sample comments for a decision.
	 * @param decision the decision to create comments for */
	private void createSampleCommentsForDecision(final tech.derbent.app.decisions.domain.CDecision decision) {
		try {
			// Comments require an activity - create a simple activity related to this decision
			final CActivityType activityType = activityTypeService.getRandom(decision.getProject());
			final CProjectItemStatus activityStatus = activityStatusService.getRandom(decision.getProject());
			final CUser user = userService.getRandom();
			final CActivity activity = new CActivity("Review Decision: " + decision.getName(), decision.getProject());
			activity.setDescription("Activity to track review and implementation of decision");
			activity.setActivityType(activityType);
			activity.setStatus(activityStatus);
			activity.setAssignedTo(user);
			activityService.save(activity);
			// Create 2 comments for this activity
			final CCommentPriority priority1 = commentPriorityService.getRandom(decision.getProject());
			final CCommentPriority priority2 = commentPriorityService.getRandom(decision.getProject());
			final CUser commenter1 = userService.getRandom();
			final CUser commenter2 = userService.getRandom();
			final tech.derbent.app.comments.domain.CComment comment1 = new tech.derbent.app.comments.domain.CComment(
					"This decision looks promising. We should prioritize implementation.", activity, commenter1, priority1);
			commentService.save(comment1);
			final tech.derbent.app.comments.domain.CComment comment2 = new tech.derbent.app.comments.domain.CComment(
					"Agreed. Let's schedule a follow-up meeting to discuss resource allocation.", activity, commenter2, priority2);
			commentService.save(comment2);
			LOGGER.debug("Created sample activity and comments for decision ID: {}", decision.getId());
		} catch (final Exception e) {
			LOGGER.error("Error creating comments for decision: {}", decision.getName(), e);
		}
	}

	/** Create sample comments for a meeting.
	 * @param meeting the meeting to create comments for
	 * @param minimal */
	private void createSampleCommentsForMeeting(final tech.derbent.app.meetings.domain.CMeeting meeting, boolean minimal) {
		try {
			// Comments require an activity - create a simple activity related to this meeting
			final CActivityType activityType = activityTypeService.getRandom(meeting.getProject());
			final CProjectItemStatus activityStatus = activityStatusService.getRandom(meeting.getProject());
			final CUser user = userService.getRandom();
			final CActivity activity = new CActivity("Follow-up: " + meeting.getName(), meeting.getProject());
			activity.setDescription("Activity to track action items from meeting");
			activity.setActivityType(activityType);
			activity.setStatus(activityStatus);
			activity.setAssignedTo(user);
			activityService.save(activity);
			// Create 2 comments for this activity
			final CCommentPriority priority1 = commentPriorityService.getRandom(meeting.getProject());
			final CUser commenter1 = userService.getRandom();
			final CComment comment1 = new tech.derbent.app.comments.domain.CComment("Meeting was productive. Action items are clearly defined.",
					activity, commenter1, priority1);
			commentService.save(comment1);
			if (minimal) {
				return;
			}
			final CUser commenter2 = userService.getRandom();
			final CCommentPriority priority2 = commentPriorityService.getRandom(meeting.getProject());
			final CComment comment2 = new tech.derbent.app.comments.domain.CComment(
					"I'll take ownership of the first two action items. Expected completion in 2 weeks.", activity, commenter2, priority2);
			commentService.save(comment2);
			LOGGER.debug("Created sample activity and comments for meeting ID: {}", meeting.getId());
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
		userService.save(user);
		LOGGER.info("Created user {} for company {} with profile picture {}", username, company.getName(), profilePictureFile);
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

	private void initializeSampleActivityPriorities(final CProject project, boolean minimal) {
		try {
			createActivityPriority(project, "Critical", "Critical priority - immediate attention required", CColorUtils.getRandomColor(true), 1,
					false, 1);
			if (minimal) {
				return;
			}
			createActivityPriority(project, "High", "High priority - urgent attention needed", CColorUtils.getRandomColor(true), 2, false, 2);
			createActivityPriority(project, "Medium", "Medium priority - normal workflow", CColorUtils.getRandomColor(true), 3, true, 3);
			createActivityPriority(project, "Low", "Low priority - can be scheduled later", CColorUtils.getRandomColor(true), 4, false, 4);
			createActivityPriority(project, "Lowest", "Lowest priority - no immediate action needed", CColorUtils.getRandomColor(true), 5, false, 5);
		} catch (final Exception e) {
			LOGGER.error("Error initializing activity priorities for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize activity priorities for project: " + project.getName(), e);
		}
	}

	private void initializeSampleActivityTypes(final CProject project, boolean minimal) {
		try {
			final String[][] activityTypes = {
					{
							ACTIVITY_TYPE_DEVELOPMENT, "Software development and coding tasks"
					}, {
							ACTIVITY_TYPE_TESTING, "Quality assurance and testing activities"
					}, {
							ACTIVITY_TYPE_DESIGN, "UI/UX design and system architecture"
					}, {
							ACTIVITY_TYPE_DOCUMENTATION, "Technical writing and documentation"
					}, {
							ACTIVITY_TYPE_RESEARCH, "Research and analysis activities"
					}
			};
			for (final String[] typeData : activityTypes) {
				final CActivityType item = activityTypeService.newEntity(typeData[0], project);
				item.setDescription(typeData[1]);
				item.setColor(CColorUtils.getRandomFromWebColors(true));
				activityTypeService.save(item);
				if (minimal) {
					return;
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating activity types", e);
			throw e;
		}
	}

	private void initializeSampleApprovalStatuses(final CProject project, boolean minimal) {
		try {
			createApprovalStatus("Draft", project, "Approval is in draft state", CColorUtils.getRandomColor(true), false, 1);
			if (minimal) {
				return;
			}
			createApprovalStatus("Submitted", project, "Approval has been submitted", CColorUtils.getRandomColor(true), false, 2);
			createApprovalStatus("Approved", project, "Approval has been approved", CColorUtils.getRandomColor(true), true, 3);
			createApprovalStatus("Rejected", project, "Approval has been rejected", CColorUtils.getRandomColor(true), true, 4);
		} catch (final Exception e) {
			LOGGER.error("Error initializing approval statuses for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize approval statuses for project: " + project.getName(), e);
		}
	}

	private void initializeSampleCommentPriorities(final CProject project, boolean minimal) {
		try {
			createCommentPriority(project, "Low", "Low priority comment", CColorUtils.getRandomColor(true), 1, false, 1);
			if (minimal) {
				return;
			}
			createCommentPriority(project, "Medium", "Medium priority comment", CColorUtils.getRandomColor(true), 2, true, 2);
			createCommentPriority(project, "High", "High priority comment", CColorUtils.getRandomColor(true), 3, false, 3);
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample comment priorities for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample comment priorities for project: " + project.getName(), e);
		}
	}

	private void initializeSampleCompanyRoles(final CCompany company, boolean minimal) {
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

	private void initializeSampleCurrencies(final CProject project, boolean minimal) {
		try {
			createCurrency(project, "USD", "US Dollar", "$ ");
			if (minimal) {
				return;
			}
			createCurrency(project, "EUR", "Euro", "€");
			createCurrency(project, "TRY", "Turkish Lira", "₺");
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample currencies for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample currencies for project: " + project.getName(), e);
		}
	}

	/** Initialize 2 sample decisions per project with all fields populated.
	 * @param project the project to create decisions for */
	private void initializeSampleDecisions(final CProject project, boolean minimal) {
		try {
			// Get random values from database for dependencies
			final CDecisionType type1 = decisionTypeService.getRandom(project);
			final CDecisionStatus status1 = decisionStatusService.getRandom(project);
			final CUser user1 = userService.getRandom();
			// Create first decision
			final CDecision decision1 = new CDecision("Adopt Cloud-Native Architecture", project);
			decision1.setDescription("Strategic decision to migrate to cloud-native architecture for improved scalability");
			decision1.setDecisionType(type1);
			decision1.setDecisionStatus(status1);
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
			final CDecisionStatus status2 = decisionStatusService.getRandom(project);
			final CUser user2 = userService.getRandom();
			final CDecision decision2 = new CDecision("Implement Agile Methodology", project);
			decision2.setDescription("Operational decision to transition from waterfall to agile development methodology");
			decision2.setDecisionType(type2);
			decision2.setDecisionStatus(status2);
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

	private void initializeSampleDecisionStatuses(final CProject project, boolean minimal) {
		try {
			createDecisionStatus("Draft", project, "Decision is in draft state", CColorUtils.getRandomColor(true), false, 1);
			if (minimal) {
				return;
			}
			createDecisionStatus("Under Review", project, "Decision is under review", CColorUtils.getRandomColor(true), false, 2);
			createDecisionStatus("Approved", project, "Decision has been approved", CColorUtils.getRandomColor(true), true, 3);
			createDecisionStatus("Implemented", project, "Decision has been implemented", CColorUtils.getRandomColor(true), true, 4);
			createDecisionStatus("Rejected", project, "Decision has been rejected", CColorUtils.getRandomColor(true), true, 5);
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample decision statuses for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample decision statuses for project: " + project.getName(), e);
		}
	}

	private void initializeSampleDecisionTypes(final CProject project, boolean minimal) {
		try {
			final String[][] decisionTypes = {
					{
							"Strategic", "High-level strategic decisions affecting organization direction"
					}, {
							"Tactical", "Mid-level tactical decisions for project execution"
					}, {
							"Operational", "Day-to-day operational decisions"
					}, {
							"Technical", "Technology and implementation related decisions"
					}, {
							"Budget", "Financial and budgeting decisions"
					}, {
							"Resource", "Human resource and allocation decisions"
					}, {
							"Timeline", "Schedule and milestone related decisions"
					}, {
							"Quality", "Quality assurance and standards decisions"
					}
			};
			for (final String[] typeData : decisionTypes) {
				final CDecisionType item = decisionTypeService.newEntity(typeData[0], project);
				item.setDescription(typeData[1]);
				item.setColor(CColorUtils.getRandomColor(true));
				decisionTypeService.save(item);
				if (minimal) {
					return;
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating decision types for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize decision types for project: " + project.getName(), e);
		}
	}

	/** Initialize 2 sample meetings per project with all fields populated.
	 * @param project the project to create meetings for */
	private void initializeSampleMeetings(final CProject project, boolean minimal) {
		try {
			// Get random values from database for dependencies
			final CMeetingType type1 = meetingTypeService.getRandom(project);
			final CMeetingStatus status1 = meetingStatusService.getRandom(project);
			final CUser user1 = userService.getRandom();
			final CMeetingType type2 = meetingTypeService.getRandom(project);
			final CMeetingStatus status2 = meetingStatusService.getRandom(project);
			final CUser user2 = userService.getRandom();
			// Create first meeting
			final tech.derbent.app.meetings.domain.CMeeting meeting1 =
					new tech.derbent.app.meetings.domain.CMeeting("Q1 Planning Session", project, type1);
			meeting1.setDescription("Quarterly planning session to review goals and set priorities");
			meeting1.setStatus(status1);
			meeting1.setAssignedTo(user1);
			meeting1.setResponsible(user2);
			meeting1.setMeetingDate(java.time.LocalDateTime.now().plusDays(7));
			meeting1.setEndDate(java.time.LocalDateTime.now().plusDays(7).plusHours(2));
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
			final tech.derbent.app.meetings.domain.CMeeting meeting2 =
					new tech.derbent.app.meetings.domain.CMeeting("Technical Architecture Review", project, type2);
			meeting2.setDescription("Review and discuss technical architecture decisions and implementation approach");
			meeting2.setStatus(status2);
			meeting2.setAssignedTo(user2);
			meeting2.setResponsible(user1);
			meeting2.setMeetingDate(java.time.LocalDateTime.now().plusDays(14));
			meeting2.setEndDate(java.time.LocalDateTime.now().plusDays(14).plusHours(3));
			meeting2.setLocation("Engineering Lab / Teams");
			meeting2.setAgenda(
					"1. Architecture proposal presentation\n2. Security considerations\n3. Scalability discussion\n4. Technology stack decisions");
			meeting2.addParticipant(user1);
			meeting2.addParticipant(user2);
			meetingService.save(meeting2);
			// Create second meeting comments
			createSampleCommentsForMeeting(meeting2, minimal);
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample meetings for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample meetings for project: " + project.getName(), e);
		}
	}

	private void initializeSampleMeetingStatuses(final CProject project, boolean minimal) {
		try {
			createMeetingStatus("Scheduled", project, "Meeting is scheduled but not yet started", "#3498db", false, 1);
			if (minimal) {
				return;
			}
			createMeetingStatus("In Progress", project, "Meeting is currently in progress", "#f39c12", false, 2);
			createMeetingStatus("Completed", project, "Meeting has been completed successfully", "#27ae60", true, 3);
			createMeetingStatus("Cancelled", project, "Meeting has been cancelled", "#e74c3c", true, 4);
			createMeetingStatus("Postponed", project, "Meeting has been postponed to a later date", "#9b59b6", false, 5);
		} catch (final Exception e) {
			LOGGER.error("Error initializing meeting statuses for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize meeting statuses for project: " + project.getName(), e);
		}
	}

	private void initializeSampleMeetingTypes(final CProject project, boolean minimal) {
		try {
			final String[][] meetingTypes = {
					{
							"Daily Standup", "Daily team synchronization meetings"
					}, {
							"Sprint Planning", "Sprint planning and estimation meetings"
					}, {
							"Sprint Review", "Sprint review and demonstration meetings"
					}, {
							"Sprint Retrospective", "Sprint retrospective and improvement meetings"
					}, {
							"Project Review", "Project review and status meetings"
					}, {
							"Technical Review", "Technical design and code review meetings"
					}, {
							"Stakeholder Meeting", "Meetings with project stakeholders"
					}, {
							"Training Session", "Training and knowledge sharing sessions"
					}
			};
			for (final String[] typeData : meetingTypes) {
				final CMeetingType meetingType = meetingTypeService.newEntity(typeData[0], project);
				meetingType.setDescription(typeData[1]);
				meetingType.setColor(CColorUtils.getRandomColor(true));
				meetingTypeService.save(meetingType);
				if (minimal) {
					return;
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating meeting types for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize meeting types for project: " + project.getName(), e);
		}
	}

	private void initializeSampleOrderStatuses(final CProject project, boolean minimal) {
		try {
			createOrderStatus("Draft", project, "Order is in draft state", "#95a5a6", false, 1);
			if (minimal) {
				return;
			}
			createOrderStatus("Submitted", project, "Order has been submitted for approval", "#3498db", false, 2);
			createOrderStatus("Approved", project, "Order has been approved", "#27ae60", false, 3);
			createOrderStatus("Processing", project, "Order is being processed", "#f39c12", false, 4);
			createOrderStatus("Delivered", project, "Order has been delivered", "#2ecc71", true, 5);
			createOrderStatus("Cancelled", project, "Order has been cancelled", "#e74c3c", true, 6);
		} catch (final Exception e) {
			LOGGER.error("Error initializing order statuses for project: {} {}", project.getName(), e.getMessage());
			throw e;
		}
	}

	private void initializeSampleOrderTypes(final CProject project, boolean minimal) {
		try {
			final String[][] orderTypes = {
					{
							"Hardware", "Hardware procurement orders"
					}, {
							"Software", "Software licensing and subscription orders"
					}, {
							"Service", "Professional services and consulting orders"
					}, {
							"Training", "Training and certification orders"
					}, {
							"Maintenance", "Maintenance and support service orders"
					}, {
							"Infrastructure", "Infrastructure and hosting service orders"
					}, {
							"Equipment", "Equipment rental and leasing orders"
					}, {
							"Supplies", "Office supplies and materials orders"
					}
			};
			for (final String[] typeData : orderTypes) {
				final COrderType orderType = orderTypeService.newEntity(typeData[0], project);
				orderType.setDescription(typeData[1]);
				orderType.setColor(CColorUtils.getRandomColor(true));
				orderTypeService.save(orderType);
				if (minimal) {
					return;
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating order types for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize order types for project: " + project.getName(), e);
		}
	}

	/** Initializes comprehensive activity data with available fields populated. */
	private void initializeSampleProjectItemStatuses(final CProject project, boolean minimal) {
		try {
			createProjectItemStatus(STATUS_NOT_STARTED, project, "Activity has not been started yet", "#95a5a6", false, 1);
			if (minimal) {
				return;
			}
			createProjectItemStatus(STATUS_IN_PROGRESS, project, "Activity is currently in progress", "#3498db", false, 2);
			createProjectItemStatus(STATUS_ON_HOLD, project, "Activity is temporarily on hold", "#f39c12", false, 3);
			createProjectItemStatus(STATUS_COMPLETED, project, "Activity has been completed", "#27ae60", true, 4);
			createProjectItemStatus(STATUS_CANCELLED, project, "Activity has been cancelled", "#e74c3c", true, 5);
		} catch (final Exception e) {
			LOGGER.error("Error initializing activity statuses for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize activity statuses for project: " + project.getName(), e);
		}
	}

	private void initializeSampleProjectRoles(final CProject project, boolean minimal) {
		try {
			// Create three project roles: Admin, User, Guest (one for each role type)
			final String[][] projectRoles = {
					{
							"Project Admin", "Administrative role with full project access", "true", "false", "false"
					}, {
							"Project User", "Standard user role with regular access", "false", "true", "false"
					}, {
							"Project Guest", "Guest role with limited access", "false", "false", "true"
					}
			};
			for (final String[] roleData : projectRoles) {
				final CUserProjectRole role = new CUserProjectRole(roleData[0], project);
				role.setDescription(roleData[1]);
				role.setIsAdmin(Boolean.parseBoolean(roleData[2]));
				role.setIsUser(Boolean.parseBoolean(roleData[3]));
				role.setIsGuest(Boolean.parseBoolean(roleData[4]));
				role.setColor(CColorUtils.getRandomColor(true));
				userProjectRoleService.save(role);
				if (minimal) {
					return;
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating project roles for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize project roles for project: " + project.getName(), e);
		}
	}

	/** Creates high priority technical risk. */
	private void initializeSampleRiskStatuses(final CProject project, boolean minimal) {
		try {
			createRiskStatus("Identified", project, "Risk has been identified", CColorUtils.getRandomColor(true), false, 1);
			if (minimal) {
				return;
			}
			createRiskStatus("Assessed", project, "Risk has been assessed", CColorUtils.getRandomColor(true), false, 2);
			createRiskStatus("Mitigated", project, "Risk mitigation actions taken", CColorUtils.getRandomColor(true), false, 3);
			createRiskStatus("Closed", project, "Risk is closed", CColorUtils.getRandomColor(true), true, 4);
		} catch (final Exception e) {
			LOGGER.error("Error initializing risk statuses for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize risk statuses for project: " + project.getName(), e);
		}
	}

	/** Initialize sample user project settings to demonstrate user-project relationships. This creates one user per role type per project.
	 * @param project2 */
	private void initializeSampleUserProjectSettings(final CProject project, boolean minimal) {
		try {
			for (final CUser user : userService.findAll()) {
				userProjectSettingsService.addUserToProject(user, project, userProjectRoleService.getRandom(project), "write");
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
	 * @param project the project to create workflow entities for
	 * @param minimal whether to create minimal sample data */
	private void initializeSampleWorkflowEntities(final CProject project, boolean minimal) {
		try {
			// Create a basic workflow for activity status transitions
			final CWorkflowEntity activityWorkflow = new CWorkflowEntity("Activity Status Workflow", project);
			activityWorkflow.setDescription("Defines status transitions for activities based on user roles");
			activityWorkflow.setIsActive(true);
			workflowEntityService.save(activityWorkflow);
			if (minimal) {
				return;
			}
			// Create workflow for meeting status transitions
			final CWorkflowEntity meetingWorkflow = new CWorkflowEntity("Meeting Status Workflow", project);
			meetingWorkflow.setDescription("Defines status transitions for meetings based on user roles");
			meetingWorkflow.setIsActive(true);
			workflowEntityService.save(meetingWorkflow);
			// Create workflow for decision approval process
			final CWorkflowEntity decisionWorkflow = new CWorkflowEntity("Decision Approval Workflow", project);
			decisionWorkflow.setDescription("Defines approval workflow for strategic decisions");
			decisionWorkflow.setIsActive(true);
			workflowEntityService.save(decisionWorkflow);
			LOGGER.debug("Created sample workflow entities for project: {}", project.getName());
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

	public void loadSampleData(boolean minimal) throws Exception {
		try {
			// ========== NON-PROJECT RELATED INITIALIZATION PHASE ==========
			// **** CREATE COMPANY SAMPLES ****//
			createTechCompany();
			if (!minimal) {
				createConsultingCompany();
				createHealthcareCompany();
				createManufacturingCompany();
			}
			/* create sample projects */
			for (final CCompany company : companyService.list(Pageable.unpaged()).getContent()) {
				initializeSampleCompanyRoles(company, minimal);
				createProjectDigitalTransformation(company);
				if (!minimal) {
					createProjectInfrastructureUpgrade(company);
					createProjectProductDevelopment(company);
				}
				createUserForCompany(company);
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
				final List<CProject> projects = projectService.list(Pageable.unpaged()).getContent();
				for (final CProject project : projects) {
					LOGGER.info("Initializing sample data for project: {}:{} (company: {}:{})", project.getId(), project.getName(), company.getId(),
							company.getName());
					sessionService.setActiveProject(project);
					CSystemSettingsInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					// Core system entities required for project operation
					CActivityInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CUserInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CCompanyInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CDecisionInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CMeetingInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					COrderInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CProjectInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CRiskInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CUserProjectRoleInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CUserCompanyRoleInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					// Type/Status InitializerServices
					CProjectItemStatusInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CActivityTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CActivityPriorityInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CApprovalStatusInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CCommentPriorityInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CCurrencyInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CDecisionStatusInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CDecisionTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CMeetingStatusInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CMeetingTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					COrderStatusInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					COrderTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CRiskStatusInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CWorkflowEntityInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CGridInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					CPageEntityInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
					// TODO: Add similar calls for all other InitializerServices (user types, priorities, etc.)
					// Project-specific type and configuration entities
					initializeSampleMeetingStatuses(project, minimal);
					initializeSampleProjectItemStatuses(project, minimal);
					initializeSampleOrderStatuses(project, minimal);
					initializeSampleApprovalStatuses(project, minimal);
					initializeSampleRiskStatuses(project, minimal);
					// types
					initializeSampleProjectRoles(project, minimal);
					initializeSampleMeetingTypes(project, minimal);
					initializeSampleDecisionTypes(project, minimal);
					initializeSampleOrderTypes(project, minimal);
					initializeSampleActivityTypes(project, minimal);
					initializeSampleActivityPriorities(project, minimal);
					// Removed sample data entity creation methods (activities, meetings, decisions, orders, risks)
					// to follow minimal sample data pattern
					initializeSampleDecisionStatuses(project, minimal);
					initializeSampleCommentPriorities(project, minimal);
					initializeSampleCurrencies(project, minimal);
					initializeSampleUserProjectSettings(project, minimal);
					initializeSampleWorkflowEntities(project, minimal);
					// Create sample entities (decisions and meetings with comments)
					initializeSampleDecisions(project, minimal);
					initializeSampleMeetings(project, minimal);
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

	public void reloadForced(boolean minimal) throws Exception {
		LOGGER.info("Sample data reload (forced) started");
		clearSampleData(); // <<<<< ÖNCE TEMİZLE
		loadSampleData(minimal); // <<<<< SONRA YENİDEN OLUŞTUR
		LOGGER.info("Sample data reload (forced) finished");
	}
}
