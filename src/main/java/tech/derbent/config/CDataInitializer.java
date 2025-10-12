package tech.derbent.config;

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
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.activities.service.CActivityInitializerService;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.activities.service.CActivityStatusInitializerService;
import tech.derbent.activities.service.CActivityStatusService;
import tech.derbent.activities.service.CActivityTypeInitializerService;
import tech.derbent.activities.service.CActivityTypeService;
import tech.derbent.api.roles.domain.CUserCompanyRole;
import tech.derbent.api.roles.domain.CUserProjectRole;
import tech.derbent.api.roles.service.CUserCompanyRoleInitializerService;
import tech.derbent.api.roles.service.CUserCompanyRoleService;
import tech.derbent.api.roles.service.CUserProjectRoleInitizerService;
import tech.derbent.api.roles.service.CUserProjectRoleService;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.comments.domain.CCommentPriority;
import tech.derbent.comments.service.CCommentPriorityService;
import tech.derbent.comments.service.CCommentService;
import tech.derbent.comments.view.CCommentPriorityInitializerService;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyInitializerService;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.decisions.domain.CDecisionStatus;
import tech.derbent.decisions.domain.CDecisionType;
import tech.derbent.decisions.service.CDecisionInitializerService;
import tech.derbent.decisions.service.CDecisionService;
import tech.derbent.decisions.service.CDecisionStatusInitializerService;
import tech.derbent.decisions.service.CDecisionStatusService;
import tech.derbent.decisions.service.CDecisionTypeInitializerService;
import tech.derbent.decisions.service.CDecisionTypeService;
import tech.derbent.gannt.service.CGanntViewEntityService;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.meetings.domain.CMeetingType;
import tech.derbent.meetings.service.CMeetingInitializerService;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.meetings.service.CMeetingStatusInitializerService;
import tech.derbent.meetings.service.CMeetingStatusService;
import tech.derbent.meetings.service.CMeetingTypeInitializerService;
import tech.derbent.meetings.service.CMeetingTypeService;
import tech.derbent.orders.domain.CApprovalStatus;
import tech.derbent.orders.domain.CCurrency;
import tech.derbent.orders.domain.COrderStatus;
import tech.derbent.orders.domain.COrderType;
import tech.derbent.orders.service.CApprovalStatusInitializerService;
import tech.derbent.orders.service.CApprovalStatusService;
import tech.derbent.orders.service.CCurrencyInitializerService;
import tech.derbent.orders.service.CCurrencyService;
import tech.derbent.orders.service.COrderInitializerService;
import tech.derbent.orders.service.COrderService;
import tech.derbent.orders.service.COrderStatusInitializerService;
import tech.derbent.orders.service.COrderStatusService;
import tech.derbent.orders.service.COrderTypeInitializerService;
import tech.derbent.orders.service.COrderTypeService;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectInitializerService;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.risks.domain.CRiskStatus;
import tech.derbent.risks.service.CRiskInitializerService;
import tech.derbent.risks.service.CRiskService;
import tech.derbent.risks.service.CRiskStatusInitializerService;
import tech.derbent.risks.service.CRiskStatusService;
import tech.derbent.screens.service.CDetailLinesService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.session.service.ISessionService;
import tech.derbent.setup.service.CSystemSettingsInitializerService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserType;
import tech.derbent.users.service.CUserInitializerService;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeInitializerService;
import tech.derbent.users.service.CUserTypeService;

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
	// Profile picture filenames mapping for users
	private static final java.util.Map<String, String> PROFILE_PICTURE_MAPPING = java.util.Map.of("admin", "admin.svg", "mkaradeniz",
			"michael_chen.svg", "msahin", "sophia_brown.svg", "bozkan", "david_kim.svg", "ademir", "emma_wilson.svg");
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
	private final CActivityService activityService;
	private final CActivityStatusService activityStatusService;
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
	private final CUserTypeService userTypeService;

	public CDataInitializer(ISessionService sessionService) {
		LOGGER.info("DataInitializer starting - obtaining service beans from application context");
		Check.notNull(sessionService, "SessionService cannot be null");
		gridEntityService = CSpringContext.getBean(CGridEntityService.class);
		projectService = CSpringContext.getBean(CProjectService.class);
		userService = CSpringContext.getBean(CUserService.class);
		userProjectSettingsService = CSpringContext.getBean(CUserProjectSettingsService.class);
		activityService = CSpringContext.getBean(CActivityService.class);
		userTypeService = CSpringContext.getBean(CUserTypeService.class);
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
		activityStatusService = CSpringContext.getBean(CActivityStatusService.class);
		decisionService = CSpringContext.getBean(CDecisionService.class);
		currencyService = CSpringContext.getBean(CCurrencyService.class);
		screenService = CSpringContext.getBean(CDetailSectionService.class);
		screenLinesService = CSpringContext.getBean(CDetailLinesService.class);
		pageEntityService = CSpringContext.getBean(CPageEntityService.class);
		ganntViewEntityService = CSpringContext.getBean(CGanntViewEntityService.class);
		riskStatusService = CSpringContext.getBean(CRiskStatusService.class);
		userProjectRoleService = CSpringContext.getBean(CUserProjectRoleService.class);
		userCompanyRoleService = CSpringContext.getBean(CUserCompanyRoleService.class);
		Check.notNull(activityService, "ActivityService bean not found");
		Check.notNull(activityStatusService, "ActivityStatusService bean not found");
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
		Check.notNull(userTypeService, "UserTypeService bean not found");
		Check.notNull(userProjectRoleService, "UserProjectRoleService bean not found");
		Check.notNull(userCompanyRoleService, "UserCompanyRoleService bean not found");
		Check.notNull(userProjectSettingsService, "UserProjectSettingsService bean not found");
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
			activityStatusService.deleteAllInBatch();
			activityTypeService.deleteAllInBatch();
			riskService.deleteAllInBatch();
			screenLinesService.deleteAllInBatch();
			screenService.deleteAllInBatch();
			currencyService.deleteAllInBatch();
			orderTypeService.deleteAllInBatch();
			orderStatusService.deleteAllInBatch();
			userService.deleteAllInBatch();
			userTypeService.deleteAllInBatch();
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
			throw new RuntimeException("Failed to clear sample data", e);
		}
	}

	private void createActivityStatus(final String name, final CProject project, final String description, final String color, final boolean isFinal,
			final int sortOrder) {
		final CActivityStatus status = new CActivityStatus(name, project);
		status.setDescription(description);
		status.setColor(color);
		status.setSortOrder(sortOrder);
		activityStatusService.save(status);
	}
	/** Creates additional activities for Customer Experience Enhancement project. */
	// Additional meeting creation methods

	/** Creates additional activities for Digital Transformation Initiative project. */
	/** Creates additional activities for Infrastructure Modernization project. */
	/** Creates additional activities for Product Development Phase 2 project. */
	/** Creates system administrator user for a specific company. Each company gets its own admin user with company-specific username.
	 * @param company the company to create admin user for */
	@Transactional (readOnly = false)
	private void createUserForCompany(CCompany company) {
		// Create unique admin username per company (e.g., admin-ofteknoloji, admin-ofdanismanlik)
		String companyShortName = company.getName().toLowerCase().replaceAll("[^a-z0-9]", "");
		String uniqueAdminLogin = USER_ADMIN;
		String adminEmail = USER_ADMIN + "@" + companyShortName + ".com.tr";
		CUserCompanyRole companyRole = userCompanyRoleService.getRandom(company);
		final CUser user = userService.createLoginUser(uniqueAdminLogin, STANDARD_PASSWORD, "Admin", adminEmail, company, companyRole);
		// Set user profile directly on entity
		final String profilePictureFile = PROFILE_PICTURE_MAPPING.getOrDefault(USER_ADMIN, "default.svg");
		final byte[] profilePictureBytes = loadProfilePictureData(profilePictureFile);
		user.setLastname(company.getName() + " Yöneticisi");
		user.setPhone("+90-462-751-1001");
		user.setProfilePictureData(profilePictureBytes);
		userService.save(user);
		LOGGER.info("Created admin user {} for company {}", uniqueAdminLogin, company.getName());
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
	private void createCommentPriority(CProject project, final String name, final String description, final String color, final Integer priorityLevel,
			final boolean isDefault, final int sortOrder) {
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
		consulting.setEnabled(true);
		// Company Configuration Settings
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

	private void createDecisionStatus(final String name, final CProject project, final String description, final String color, final boolean isFinal,
			final int sortOrder) {
		final CDecisionStatus status = new CDecisionStatus(name, project);
		status.setDescription(description);
		status.setColor(color);
		status.setFinal(isFinal);
		status.setSortOrder(sortOrder);
		decisionStatusService.save(status);
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
		healthcare.setEnabled(true);
		// Company Configuration Settings
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
		manufacturing.setEnabled(true);
		// Company Configuration Settings
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

	private void createProjectDigitalTransformation(CCompany company) {
		final CProject project = new CProject("Digital Transformation Initiative", company);
		project.setDescription("Comprehensive digital transformation for enhanced customer experience");
		project.setIsActive(true);
		projectService.save(project);
	}

	private void createProjectInfrastructureUpgrade(CCompany company) {
		final CProject project = new CProject("Infrastructure Upgrade Project", company);
		project.setDescription("Upgrading IT infrastructure for improved performance and scalability");
		projectService.save(project);
	}

	private void createProjectProductDevelopment(CCompany company) {
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

	/** Creates technology startup company. */
	private void createTechCompany() {
		final CCompany techStartup = new CCompany(COMPANY_OF_TEKNOLOJI);
		techStartup.setDescription("Dijital dönüşüm için yenilikçi teknoloji çözümleri");
		techStartup.setAddress("Cumhuriyet Mahallesi, Atatürk Caddesi No:15, Of/Trabzon");
		techStartup.setPhone("+90-462-751-0101");
		techStartup.setEmail("iletisim@ofteknoloji.com.tr");
		techStartup.setWebsite("https://www.ofteknoloji.com.tr");
		techStartup.setTaxNumber("TR-123456789");
		techStartup.setEnabled(true);
		// Company Configuration Settings
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

	/** Initializes comprehensive activity data with available fields populated. */
	private void initializeSampleActivityStatuses(final CProject project) {
		try {
			createActivityStatus(STATUS_NOT_STARTED, project, "Activity has not been started yet", "#95a5a6", false, 1);
			createActivityStatus(STATUS_IN_PROGRESS, project, "Activity is currently in progress", "#3498db", false, 2);
			createActivityStatus(STATUS_ON_HOLD, project, "Activity is temporarily on hold", "#f39c12", false, 3);
			createActivityStatus(STATUS_COMPLETED, project, "Activity has been completed", "#27ae60", true, 4);
			createActivityStatus(STATUS_CANCELLED, project, "Activity has been cancelled", "#e74c3c", true, 5);
		} catch (final Exception e) {
			LOGGER.error("Error initializing activity statuses for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize activity statuses for project: " + project.getName(), e);
		}
	}

	private void initializeSampleActivityTypes(final CProject project) {
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
				final CActivityType item = activityTypeService.createEntity(typeData[0], project);
				item.setDescription(typeData[1]);
				item.setColor(CColorUtils.getRandomColor(true));
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating activity types", e);
			throw new RuntimeException("Failed to initialize activity types", e);
		}
	}

	private void initializeSampleApprovalStatuses(final CProject project) {
		try {
			createApprovalStatus("Draft", project, "Approval is in draft state", CColorUtils.getRandomColor(true), false, 1);
			createApprovalStatus("Submitted", project, "Approval has been submitted", CColorUtils.getRandomColor(true), false, 2);
			createApprovalStatus("Approved", project, "Approval has been approved", CColorUtils.getRandomColor(true), true, 3);
			createApprovalStatus("Rejected", project, "Approval has been rejected", CColorUtils.getRandomColor(true), true, 4);
		} catch (final Exception e) {
			LOGGER.error("Error initializing approval statuses for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize approval statuses for project: " + project.getName(), e);
		}
	}

	private void initializeSampleCommentPriorities(final CProject project) {
		try {
			createCommentPriority(project, "Low", "Low priority comment", CColorUtils.getRandomColor(true), 1, false, 1);
			createCommentPriority(project, "Medium", "Medium priority comment", CColorUtils.getRandomColor(true), 2, true, 2);
			createCommentPriority(project, "High", "High priority comment", CColorUtils.getRandomColor(true), 3, false, 3);
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample comment priorities for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample comment priorities for project: " + project.getName(), e);
		}
	}

	private void initializeSampleCompanyRoles(CCompany company) {
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
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating company roles for company: {}", company.getName(), e);
			throw new RuntimeException("Failed to initialize company roles for company: " + company.getName(), e);
		}
	}

	private void initializeSampleCurrencies(final CProject project) {
		try {
			createCurrency(project, "USD", "US Dollar", "$ ");
			createCurrency(project, "EUR", "Euro", "€");
			createCurrency(project, "TRY", "Turkish Lira", "₺");
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample currencies for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample currencies for project: " + project.getName(), e);
		}
	}

	private void initializeSampleDecisionStatuses(final CProject project) {
		try {
			createDecisionStatus("Draft", project, "Decision is in draft state", CColorUtils.getRandomColor(true), false, 1);
			createDecisionStatus("Under Review", project, "Decision is under review", CColorUtils.getRandomColor(true), false, 2);
			createDecisionStatus("Approved", project, "Decision has been approved", CColorUtils.getRandomColor(true), true, 3);
			createDecisionStatus("Implemented", project, "Decision has been implemented", CColorUtils.getRandomColor(true), true, 4);
			createDecisionStatus("Rejected", project, "Decision has been rejected", CColorUtils.getRandomColor(true), true, 5);
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample decision statuses for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample decision statuses for project: " + project.getName(), e);
		}
	}

	private void initializeSampleDecisionTypes(final CProject project) {
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
				final CDecisionType item = decisionTypeService.createEntity(typeData[0], project);
				item.setDescription(typeData[1]);
				item.setColor(CColorUtils.getRandomColor(true));
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating decision types for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize decision types for project: " + project.getName(), e);
		}
	}

	private void initializeSampleMeetingStatuses(final CProject project) {
		try {
			createMeetingStatus("Scheduled", project, "Meeting is scheduled but not yet started", "#3498db", false, 1);
			createMeetingStatus("In Progress", project, "Meeting is currently in progress", "#f39c12", false, 2);
			createMeetingStatus("Completed", project, "Meeting has been completed successfully", "#27ae60", true, 3);
			createMeetingStatus("Cancelled", project, "Meeting has been cancelled", "#e74c3c", true, 4);
			createMeetingStatus("Postponed", project, "Meeting has been postponed to a later date", "#9b59b6", false, 5);
		} catch (final Exception e) {
			LOGGER.error("Error initializing meeting statuses for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize meeting statuses for project: " + project.getName(), e);
		}
	}

	private void initializeSampleMeetingTypes(final CProject project) {
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
				final CMeetingType meetingType = meetingTypeService.createEntity(typeData[0], project);
				meetingType.setDescription(typeData[1]);
				meetingType.setColor(CColorUtils.getRandomColor(true));
				meetingTypeService.save(meetingType);
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating meeting types for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize meeting types for project: " + project.getName(), e);
		}
	}

	private void initializeSampleOrderStatuses(final CProject project) {
		try {
			createOrderStatus("Draft", project, "Order is in draft state", "#95a5a6", false, 1);
			createOrderStatus("Submitted", project, "Order has been submitted for approval", "#3498db", false, 2);
			createOrderStatus("Approved", project, "Order has been approved", "#27ae60", false, 3);
			createOrderStatus("Processing", project, "Order is being processed", "#f39c12", false, 4);
			createOrderStatus("Delivered", project, "Order has been delivered", "#2ecc71", true, 5);
			createOrderStatus("Cancelled", project, "Order has been cancelled", "#e74c3c", true, 6);
		} catch (final Exception e) {
			LOGGER.error("Error initializing order statuses for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize order statuses for project: " + project.getName(), e);
		}
	}

	private void initializeSampleOrderTypes(final CProject project) {
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
				final COrderType orderType = orderTypeService.createEntity(typeData[0], project);
				orderType.setDescription(typeData[1]);
				orderType.setColor(CColorUtils.getRandomColor(true));
				orderTypeService.save(orderType);
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating order types for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize order types for project: " + project.getName(), e);
		}
	}

	private void initializeSampleProjectRoles(final CProject project) {
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
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating project roles for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize project roles for project: " + project.getName(), e);
		}
	}

	/** Creates high priority technical risk. */
	private void initializeSampleRiskStatuses(final CProject project) {
		try {
			createRiskStatus("Identified", project, "Risk has been identified", CColorUtils.getRandomColor(true), false, 1);
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
	private void initializeSampleUserProjectSettings(CProject project) {
		try {
			for (CUser user : userService.findAll()) {
				userProjectSettingsService.addUserToProject(user, project, userProjectRoleService.getRandom(project), "write");
			}
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample user project settings.");
			throw new RuntimeException("Failed to initialize sample user project settings", e);
		}
	}

	private void initializeSampleUserTypes(final CProject project) {
		try {
			final String[][] userTypes = {
					{
							"Administrator", "System administrators with full access"
					}, {
							"Project Manager", "Project managers responsible for project coordination"
					}, {
							"Team Lead", "Team leaders responsible for team management"
					}, {
							"Developer", "Software developers and engineers"
					}, {
							"Analyst", "Business and system analysts"
					}, {
							"Tester", "Quality assurance and testing specialists"
					}, {
							"Designer", "UI/UX designers and architects"
					}, {
							"Stakeholder", "Project stakeholders and decision makers"
					}
			};
			for (final String[] typeData : userTypes) {
				final CUserType userType = userTypeService.createEntity(typeData[0], project);
				userType.setDescription(typeData[1]);
				userType.setColor(CColorUtils.getRandomColor(true));
				userTypeService.save(userType);
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating user types for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize user types for project: " + project.getName(), e);
		}
		// for each user distribute random user types
		final List<CUser> users = userService.list(Pageable.unpaged()).getContent();
		for (final CUser user : users) {
			user.setUserType(userTypeService.getRandom());
			userService.save(user);
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

	public void loadSampleData() {
		try {
			// ========== NON-PROJECT RELATED INITIALIZATION PHASE ==========
			// **** CREATE COMPANY SAMPLES ****//
			createTechCompany();
			createConsultingCompany();
			createHealthcareCompany();
			createManufacturingCompany();
			/* create sample projects */
			for (CCompany company : companyService.list(Pageable.unpaged()).getContent()) {
				initializeSampleCompanyRoles(company);
				createProjectDigitalTransformation(company);
				createProjectInfrastructureUpgrade(company);
				createProjectProductDevelopment(company);
				createUserForCompany(company);
				// createUserFor(company);
			}
			// ========== PROJECT-SPECIFIC INITIALIZATION PHASE ==========
			for (CCompany company : companyService.list(Pageable.unpaged()).getContent()) {
				// sessionService.setActiveCompany(company);
				// later implement better user randomization logic
				LOGGER.info("Setting active company to: id:{}:{}", company.getId(), company.getName());
				CUser user = userService.getRandomByCompany(company);
				Check.notNull(user, "No user found for company: " + company.getName());
				// Use new atomic method to set both company and user
				Check.notNull(sessionService, "SessionService is not initialized");
				sessionService.setActiveUser(user); // Set company first, then user who is member of that company
				final List<CProject> projects = projectService.list(Pageable.unpaged()).getContent();
				for (final CProject project : projects) {
					LOGGER.info("Initializing sample data for project: {}:{} (company: {}:{})", project.getId(), project.getName(), company.getId(),
							company.getName());
					sessionService.setActiveProject(project);
					CSystemSettingsInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, true);
					// Core system entities required for project operation
					CActivityInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, true);
					CUserInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, true);
					CCompanyInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
					CDecisionInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
					CMeetingInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, true);
					COrderInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
					CProjectInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, true);
					CRiskInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
					CUserProjectRoleInitizerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
					CUserCompanyRoleInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
					// Type/Status InitializerServices
					CActivityStatusInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
					CActivityTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
					CApprovalStatusInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
					CCommentPriorityInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
					CCurrencyInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
					CDecisionStatusInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
					CDecisionTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
					CMeetingStatusInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
					CMeetingTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
					COrderStatusInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
					COrderTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
					CRiskStatusInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
					CUserTypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService, false);
					// TODO: Add similar calls for all other InitializerServices (user types, priorities, etc.)
					// Project-specific type and configuration entities
					initializeSampleMeetingStatuses(project);
					initializeSampleActivityStatuses(project);
					initializeSampleOrderStatuses(project);
					initializeSampleApprovalStatuses(project);
					initializeSampleRiskStatuses(project);
					// types
					initializeSampleUserTypes(project);
					initializeSampleProjectRoles(project);
					initializeSampleMeetingTypes(project);
					initializeSampleDecisionTypes(project);
					initializeSampleOrderTypes(project);
					initializeSampleActivityTypes(project);
					// Removed sample data entity creation methods (activities, meetings, decisions, orders, risks)
					// to follow minimal sample data pattern
					initializeSampleDecisionStatuses(project);
					initializeSampleCommentPriorities(project);
					initializeSampleCurrencies(project);
					initializeSampleUserProjectSettings(project);
				}
			}
			// Initialize company roles (non-project specific)
			// createSampleOrders(); // Temporarily disabled due to missing dependencies
			LOGGER.info("Sample data initialization completed successfully");
		} catch (final Exception e) {
			LOGGER.error("Error loading sample data", e);
			throw new RuntimeException("Failed to load sample data", e);
		}
	}

	public void reloadForced() {
		LOGGER.info("Sample data reload (forced) started");
		clearSampleData(); // <<<<< ÖNCE TEMİZLE
		loadSampleData(); // <<<<< SONRA YENİDEN OLUŞTUR
		LOGGER.info("Sample data reload (forced) finished");
	}
	// ========================================================================
	// SYSTEM INITIALIZATION METHODS - Base entities required for operation
	// ========================================================================
}
