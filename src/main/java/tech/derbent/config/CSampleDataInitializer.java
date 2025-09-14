package tech.derbent.config;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import tech.derbent.abstracts.components.CTimer;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.activities.service.CActivityStatusService;
import tech.derbent.activities.service.CActivityTypeService;
import tech.derbent.activities.service.CActivityViewService;
import tech.derbent.activities.view.CActivitiesView;
import tech.derbent.activities.view.CActivityStatusView;
import tech.derbent.activities.view.CActivityTypeView;
import tech.derbent.comments.domain.CCommentPriority;
import tech.derbent.comments.service.CCommentPriorityService;
import tech.derbent.comments.service.CCommentService;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.companies.view.CCompanyView;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.decisions.domain.CDecisionStatus;
import tech.derbent.decisions.service.CDecisionService;
import tech.derbent.decisions.service.CDecisionStatusService;
import tech.derbent.decisions.service.CDecisionViewService;
import tech.derbent.decisions.view.CDecisionsView;
import tech.derbent.gannt.service.CGanntViewEntityService;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.meetings.service.CMeetingStatusService;
import tech.derbent.meetings.service.CMeetingTypeService;
import tech.derbent.meetings.service.CMeetingViewService;
import tech.derbent.meetings.view.CMeetingTypeView;
import tech.derbent.meetings.view.CMeetingsView;
import tech.derbent.orders.domain.CCurrency;
import tech.derbent.orders.service.CCurrencyService;
import tech.derbent.orders.service.COrderTypeService;
import tech.derbent.orders.service.COrdersViewService;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.page.service.CPageEntityViewService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.projects.view.CProjectsView;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.risks.domain.ERiskSeverity;
import tech.derbent.risks.service.CRiskService;
import tech.derbent.risks.service.CRiskViewService;
import tech.derbent.risks.view.CRiskView;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailLinesService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.EUserRole;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;
import tech.derbent.users.service.CUserViewService;
import tech.derbent.users.view.CUsersView;

public class CSampleDataInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSampleDataInitializer.class);
	// Profile picture filenames mapping for users
	private static final java.util.Map<String, String> PROFILE_PICTURE_MAPPING = java.util.Map.of("admin", "admin.svg", "mkaradeniz",
			"michael_chen.svg", "msahin", "sophia_brown.svg", "bozkan", "david_kim.svg", "ademir", "emma_wilson.svg");
	// Standard password for all users as per coding guidelines
	private static final String STANDARD_PASSWORD = "test123";
	private final CActivityService activityService;
	private final CActivityStatusService activityStatusService;
	private final CActivityTypeService activityTypeService;
	private final CCommentPriorityService commentPriorityService;
	private final CCommentService commentService;
	private final CCompanyService companyService;
	private final CCurrencyService currencyService;
	private final CDecisionService decisionService;
	private final CDecisionStatusService decisionStatusService;
	@PersistenceContext
	private EntityManager em;
	private final CGanntViewEntityService ganntViewEntityService;
	private final CGridEntityService gridEntityService;
	private final JdbcTemplate jdbcTemplate;
	private final CMeetingService meetingService;
	private final CMeetingStatusService meetingStatusService;
	private final CMeetingTypeService meetingTypeService;
	private final COrderTypeService orderTypeService;
	private final CPageEntityService pageEntityService;
	// Service dependencies - injected via constructor
	private final CProjectService projectService;
	private final CRiskService riskService;
	private final CDetailLinesService screenLinesService;
	private final CDetailSectionService screenService;
	private final CUserService userService;
	private final CUserTypeService userTypeService;

	public CSampleDataInitializer() {
		gridEntityService = CSpringContext.getBean(CGridEntityService.class);
		projectService = CSpringContext.getBean(CProjectService.class);
		userService = CSpringContext.getBean(CUserService.class);
		activityService = CSpringContext.getBean(CActivityService.class);
		userTypeService = CSpringContext.getBean(CUserTypeService.class);
		activityTypeService = CSpringContext.getBean(CActivityTypeService.class);
		meetingTypeService = CSpringContext.getBean(CMeetingTypeService.class);
		orderTypeService = CSpringContext.getBean(COrderTypeService.class);
		companyService = CSpringContext.getBean(CCompanyService.class);
		commentService = CSpringContext.getBean(CCommentService.class);
		commentPriorityService = CSpringContext.getBean(CCommentPriorityService.class);
		meetingService = CSpringContext.getBean(CMeetingService.class);
		riskService = CSpringContext.getBean(CRiskService.class);
		meetingStatusService = CSpringContext.getBean(CMeetingStatusService.class);
		decisionStatusService = CSpringContext.getBean(CDecisionStatusService.class);
		activityStatusService = CSpringContext.getBean(CActivityStatusService.class);
		decisionService = CSpringContext.getBean(CDecisionService.class);
		currencyService = CSpringContext.getBean(CCurrencyService.class);
		screenService = CSpringContext.getBean(CDetailSectionService.class);
		screenLinesService = CSpringContext.getBean(CDetailLinesService.class);
		pageEntityService = CSpringContext.getBean(CPageEntityService.class);
		ganntViewEntityService = CSpringContext.getBean(CGanntViewEntityService.class);
		final DataSource ds = CSpringContext.getBean(DataSource.class);
		jdbcTemplate = new JdbcTemplate(ds);
	}

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
			// orderStatusService.deleteAllInBatch(); // ekleyeceksen
			userService.deleteAllInBatch();
			userTypeService.deleteAllInBatch();
			companyService.deleteAllInBatch();
			projectService.deleteAllInBatch();
			pageEntityService.deleteAllInBatch();
			ganntViewEntityService.deleteAllInBatch();
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
	private void createAdditionalCustomerExperienceActivities(final CProject project) {
		Check.notNull(project, "Project 'Customer Experience Enhancement' not found");
		// User Research Activity
		final CActivity userResearch = new CActivity("User Research & Analysis", project);
		final CActivityType researchType = activityTypeService.findByNameAndProject("Research", project).orElse(null);
		Check.notNull(researchType, "Research activity type not found for project");
		userResearch.setActivityType(researchType);
		userResearch.setDescription("Conduct user interviews and analyze customer feedback");
		final CUser analyst = userService.findByLogin("ademir");
		final CUser manager = userService.findByLogin("mkaradeniz");
		userResearch.setAssignedTo(analyst);
		userResearch.setCreatedBy(manager);
		userResearch.setEstimatedHours(new BigDecimal("22.00"));
		userResearch.setActualHours(new BigDecimal("22.00"));
		userResearch.setRemainingHours(new BigDecimal("0.00"));
		userResearch.setStartDate(LocalDate.now().minusDays(20));
		userResearch.setDueDate(LocalDate.now().minusDays(10));
		userResearch.setCompletionDate(LocalDate.now().minusDays(10));
		final CActivityStatus completedStatus = activityStatusService.findByNameAndProject("Completed", project).orElseThrow();
		userResearch.setStatus(completedStatus);
		userResearch.setProgressPercentage(100);
		final CActivity uxDesign = new CActivity("UI/UX Design Improvements", project);
		final CActivityType designType = activityTypeService.findByNameAndProject("Design", project).orElse(null);
		uxDesign.setActivityType(designType);
		uxDesign.setDescription("Design improved user interface based on research findings");
		final CUser dev2 = userService.findByLogin("msahin");
		uxDesign.setAssignedTo(dev2);
		uxDesign.setCreatedBy(analyst);
		uxDesign.setEstimatedHours(new BigDecimal("28.00"));
		uxDesign.setActualHours(new BigDecimal("15.00"));
		uxDesign.setRemainingHours(new BigDecimal("13.00"));
		uxDesign.setStartDate(LocalDate.now().minusDays(8));
		uxDesign.setDueDate(LocalDate.now().plusDays(5));
		final CActivityStatus inProgressStatus = activityStatusService.findByNameAndProject("In Progress", project).orElseThrow();
		uxDesign.setStatus(inProgressStatus);
		uxDesign.setProgressPercentage(55);
		activityService.save(userResearch);
		uxDesign.setParent(userResearch); // Set parent-child relationship)
		activityService.save(uxDesign);
		commentService.createComment("Design system updated with new patterns", uxDesign, dev2);
		commentService.createComment("Wireframes created for key user journeys", uxDesign, dev2);
		commentService.createComment("Prototypes ready for user testing", uxDesign, analyst);
		commentService.createComment("User research methodology defined", userResearch, manager);
		commentService.createComment("Conducted 15 user interviews", userResearch, analyst);
		commentService.createComment("Analysis complete, insights documented", userResearch, analyst);
		// UI/UX Design Activity
	}
	// Additional meeting creation methods

	/** Creates additional activities for Digital Transformation Initiative project. */
	private void createAdditionalDigitalTransformationActivities(final CProject project) {
		// Frontend Development Activity
		final CActivity frontendDev = new CActivity("Frontend Development", project);
		final CActivityType developmentType = activityTypeService.findByNameAndProject("Development", project).orElse(null);
		frontendDev.setActivityType(developmentType);
		frontendDev.setDescription("Develop responsive user interface components using modern frameworks");
		final CUser dev1 = userService.findByLogin("msahin");
		final CUser manager = userService.findByLogin("mkaradeniz");
		frontendDev.setAssignedTo(dev1);
		frontendDev.setCreatedBy(manager);
		frontendDev.setEstimatedHours(new BigDecimal("32.00"));
		frontendDev.setActualHours(new BigDecimal("28.00"));
		frontendDev.setRemainingHours(new BigDecimal("4.00"));
		frontendDev.setStartDate(LocalDate.now().minusDays(12));
		frontendDev.setDueDate(LocalDate.now().plusDays(8));
		final CActivityStatus inProgressStatus = activityStatusService.findByNameAndProject("In Progress", project).orElseThrow();
		frontendDev.setStatus(inProgressStatus);
		frontendDev.setProgressPercentage(70);
		activityService.save(frontendDev);
		commentService.createComment("Frontend development started with React components", frontendDev, dev1);
		commentService.createComment("Implemented responsive design patterns", frontendDev, dev1);
		commentService.createComment("Working on integration with backend APIs", frontendDev, manager);
		// Database Migration Activity
		final CActivity dbMigration = new CActivity("Database Migration", project);
		dbMigration.setActivityType(developmentType);
		dbMigration.setDescription("Migrate legacy data to new database schema");
		final CUser admin = userService.findByLogin("admin");
		dbMigration.setAssignedTo(admin);
		dbMigration.setCreatedBy(manager);
		dbMigration.setEstimatedHours(new BigDecimal("20.00"));
		dbMigration.setActualHours(new BigDecimal("5.00"));
		dbMigration.setRemainingHours(new BigDecimal("15.00"));
		dbMigration.setStartDate(LocalDate.now().plusDays(5));
		dbMigration.setDueDate(LocalDate.now().plusDays(15));
		final CActivityStatus notStartedStatus = activityStatusService.findByNameAndProject("Not Started", project).orElseThrow();
		dbMigration.setStatus(notStartedStatus);
		dbMigration.setProgressPercentage(0);
		activityService.save(dbMigration);
		commentService.createComment("Database migration plan prepared", dbMigration, admin);
		commentService.createComment("Waiting for backend API completion", dbMigration, manager);
	}

	/** Creates additional activities for Infrastructure Modernization project. */
	private void createAdditionalInfrastructureActivities(final CProject project) {
		// Security Audit Activity
		final CActivity securityAudit = new CActivity("Security Audit", project);
		final CActivityType researchType = activityTypeService.findByNameAndProject("Research", project).orElseThrow();
		securityAudit.setActivityType(researchType);
		securityAudit.setDescription("Comprehensive security assessment and vulnerability analysis");
		final CUser admin = userService.findByLogin("admin");
		final CUser manager = userService.findByLogin("mkaradeniz");
		securityAudit.setAssignedTo(admin);
		securityAudit.setCreatedBy(manager);
		securityAudit.setEstimatedHours(new BigDecimal("25.00"));
		securityAudit.setActualHours(new BigDecimal("0.00"));
		securityAudit.setRemainingHours(new BigDecimal("25.00"));
		securityAudit.setStartDate(LocalDate.now().plusDays(10));
		securityAudit.setDueDate(LocalDate.now().plusDays(18));
		final CActivityStatus notStartedStatus = activityStatusService.findByNameAndProject("Not Started", project).orElseThrow();
		securityAudit.setStatus(notStartedStatus);
		securityAudit.setProgressPercentage(0);
		activityService.save(securityAudit);
		commentService.createComment("Security audit requirements defined", securityAudit, admin);
		commentService.createComment("External security firm selected for audit", securityAudit, manager);
		// Server Migration Activity
		final CActivity serverMigration = new CActivity("Server Migration", project);
		final CActivityType developmentType = activityTypeService.findByNameAndProject("Development", project).orElseThrow();
		serverMigration.setActivityType(developmentType);
		serverMigration.setDescription("Migrate applications to new server infrastructure");
		final CUser dev1 = userService.findByLogin("bozkan");
		serverMigration.setAssignedTo(dev1);
		serverMigration.setCreatedBy(admin);
		serverMigration.setEstimatedHours(new BigDecimal("35.00"));
		serverMigration.setActualHours(new BigDecimal("20.00"));
		serverMigration.setRemainingHours(new BigDecimal("15.00"));
		serverMigration.setStartDate(LocalDate.now().minusDays(8));
		serverMigration.setDueDate(LocalDate.now().plusDays(12));
		final CActivityStatus onHoldStatus = activityStatusService.findByNameAndProject("On Hold", project).orElseThrow();
		serverMigration.setStatus(onHoldStatus);
		serverMigration.setProgressPercentage(55);
		serverMigration.setParent(securityAudit); // Set parent-child relationship
		activityService.save(serverMigration);
		commentService.createComment("Server migration plan created", serverMigration, dev1);
		commentService.createComment("Testing environment successfully migrated", serverMigration, dev1);
		commentService.createComment("Production migration on hold pending approval", serverMigration, admin);
	}

	/** Creates additional activities for Product Development Phase 2 project. */
	private void createAdditionalProductDevelopmentActivities(final CProject project) {
		// Code Review Activity
		final CActivity codeReview = new CActivity("Code Review Process", project);
		final CActivityType testingType = activityTypeService.findByNameAndProject("Testing", project).orElseThrow();
		codeReview.setActivityType(testingType);
		codeReview.setDescription("Comprehensive code review and quality assurance");
		final CUser analyst = userService.findByLogin("ademir");
		final CUser manager = userService.findByLogin("mkaradeniz");
		codeReview.setAssignedTo(analyst);
		codeReview.setCreatedBy(manager);
		codeReview.setEstimatedHours(new BigDecimal("12.00"));
		codeReview.setActualHours(new BigDecimal("12.00"));
		codeReview.setRemainingHours(new BigDecimal("0.00"));
		codeReview.setStartDate(LocalDate.now().minusDays(3));
		codeReview.setDueDate(LocalDate.now().minusDays(1));
		codeReview.setCompletionDate(LocalDate.now().minusDays(1));
		final CActivityStatus completedStatus = activityStatusService.findByNameAndProject("Completed", project).orElseThrow();
		codeReview.setStatus(completedStatus);
		codeReview.setProgressPercentage(100);
		activityService.save(codeReview);
		commentService.createComment("Code review process initiated", codeReview, manager);
		commentService.createComment("Found minor issues, created fix recommendations", codeReview, analyst);
		commentService.createComment("All issues resolved, code approved", codeReview, analyst);
		// Performance Testing Activity
		final CActivity perfTesting = new CActivity("Performance Testing", project);
		perfTesting.setActivityType(testingType);
		perfTesting.setDescription("Load testing and performance optimization");
		final CUser dev2 = userService.findByLogin("bozkan");
		perfTesting.setAssignedTo(dev2);
		perfTesting.setCreatedBy(manager);
		perfTesting.setEstimatedHours(new BigDecimal("18.00"));
		perfTesting.setActualHours(new BigDecimal("10.00"));
		perfTesting.setRemainingHours(new BigDecimal("8.00"));
		perfTesting.setStartDate(LocalDate.now().minusDays(5));
		perfTesting.setDueDate(LocalDate.now().plusDays(2));
		final CActivityStatus inProgressStatus = activityStatusService.findByNameAndProject("In Progress", project).orElseThrow();
		perfTesting.setStatus(inProgressStatus);
		perfTesting.setProgressPercentage(60);
		perfTesting.setParent(codeReview); // Set parent-child relationship
		activityService.save(perfTesting);
		commentService.createComment("Performance testing framework setup", perfTesting, dev2);
		commentService.createComment("Baseline performance metrics collected", perfTesting, dev2);
	}

	/** Creates system administrator user. */
	private void createAdminUser() {
		final CUser admin = userService.createLoginUser("admin", STANDARD_PASSWORD, "Ahmet", "admin@of.gov.tr", "ADMIN,USER");
		// Set user profile directly on entity
		final String profilePictureFile = PROFILE_PICTURE_MAPPING.get("admin");
		final byte[] profilePictureBytes = loadProfilePictureData(profilePictureFile);
		admin.setLastname("Yılmaz");
		admin.setPhone("+90-462-751-1001");
		admin.setProfilePictureData(profilePictureBytes);
		// Set user role directly on entity
		admin.setUserRole(EUserRole.ADMIN);
		admin.setRoles("ADMIN,USER");
		// Set company association directly on entity
		final CCompany company = companyService.findByName("Of Teknoloji Çözümleri").orElseThrow();
		admin.setCompany(company);
		userService.save(admin);
	}

	/** Creates backend development activity. */
	private void createBackendDevActivity(final CProject project) {
		// Create the activity using new auxiliary methods
		final CActivity backendDev = new CActivity("Backend API Development", project);
		// Find and set the activity type
		final CActivityType developmentType = activityTypeService.findByNameAndProject("Development", project).orElseThrow();
		if (developmentType == null) {
			LOGGER.warn("Development activity type not found for project, using null");
		}
		// Set activity type and description using auxiliary method
		backendDev.setActivityType(developmentType);
		backendDev.setDescription("Develop REST API endpoints for user management and authentication");
		// Set assigned users using auxiliary method
		final CUser manager = userService.findByLogin("mkaradeniz");
		final CUser admin = userService.findByLogin("admin");
		backendDev.setAssignedTo(manager);
		backendDev.setCreatedBy(admin);
		// Set time tracking using entity methods
		backendDev.setEstimatedHours(new BigDecimal("40.00"));
		backendDev.setActualHours(new BigDecimal("35.50"));
		backendDev.setRemainingHours(new BigDecimal("4.50"));
		// Set date information using auxiliary method
		backendDev.setStartDate(LocalDate.now().minusDays(10));
		backendDev.setDueDate(LocalDate.now().plusDays(5));
		// Set status and priority using auxiliary method
		final CActivityStatus inProgressStatus = activityStatusService.findByNameAndProject("In Progress", project).orElseThrow();
		backendDev.setStatus(inProgressStatus);
		backendDev.setProgressPercentage(75);
		activityService.save(backendDev);
		// Create comments
		commentService.createComment("Initial backend API development started", backendDev, admin);
		commentService.createComment("API endpoints for user registration and login implemented", backendDev, admin);
		commentService.createComment("Working on authentication and authorization features", backendDev, manager);
	}

	private void createCommentPriority(final String name, final String description, final String color, final Integer priorityLevel,
			final boolean isDefault, final int sortOrder) {
		final CProject project = projectService.findByName("Digital Transformation Initiative").orElseThrow();
		final CCommentPriority priority = new CCommentPriority(name, project, color, sortOrder);
		priority.setDescription(description);
		priority.setPriorityLevel(priorityLevel);
		priority.setDefault(isDefault);
		commentPriorityService.save(priority);
	}

	/** Creates consulting company. */
	private void createConsultingCompany() {
		final CCompany consulting = new CCompany("Of Stratejik Danışmanlık");
		consulting.setDescription("Yönetim danışmanlığı ve stratejik planlama hizmetleri");
		consulting.setAddress("Merkez Mahallesi, Gülbahar Sokağı No:7, Of/Trabzon");
		consulting.setPhone("+90-462-751-0303");
		consulting.setEmail("merhaba@ofdanismanlik.com.tr");
		consulting.setWebsite("https://www.ofdanismanlik.com.tr");
		consulting.setTaxNumber("TR-456789123");
		consulting.setEnabled(true);
		companyService.save(consulting);
	}

	private void createCriticalSecurityRisk() {
		final CProject project = projectService.findByName("Customer Experience Enhancement").orElseThrow();
		final CRisk risk = new CRisk("Data Privacy Compliance Gaps", project);
		risk.setRiskSeverity(ERiskSeverity.CRITICAL);
		risk.setDescription("Current implementation may not fully comply with GDPR and data protection regulations");
		riskService.save(risk);
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
		final CCompany healthcare = new CCompany("Of Sağlık Teknolojileri");
		healthcare.setDescription("İleri tıp teknolojisi ve sağlık çözümleri");
		healthcare.setAddress("Yeni Mahalle, Sağlık Sokağı No:21, Of/Trabzon");
		healthcare.setPhone("+90-462-751-0404");
		healthcare.setEmail("iletisim@ofsaglik.com.tr");
		healthcare.setWebsite("https://www.ofsaglik.com.tr");
		healthcare.setTaxNumber("TR-789123456");
		healthcare.setEnabled(true);
		companyService.save(healthcare);
	}

	/** Creates high priority technical risk. */
	private void createHighPriorityTechnicalRisk() {
		final CProject project = projectService.findByName("Digital Transformation Initiative").orElseThrow();
		final CRisk risk = new CRisk("Legacy System Integration Challenges", project);
		risk.setRiskSeverity(ERiskSeverity.HIGH);
		risk.setDescription("Integration with legacy systems may cause compatibility issues and performance bottlenecks");
		riskService.save(risk);
	}

	/** Creates low priority resource risk. */
	private void createLowPriorityResourceRisk() {
		final CProject project = projectService.findByName("Infrastructure Modernization").orElseThrow();
		final CRisk risk = new CRisk("Team Member Vacation Scheduling Conflicts", project);
		risk.setRiskSeverity(ERiskSeverity.LOW);
		risk.setDescription("Overlapping vacation schedules may temporarily reduce team capacity");
		riskService.save(risk);
	}

	/** Creates low priority schedule risk. */
	private void createLowPriorityScheduleRisk() {
		final CProject project = projectService.findByName("Digital Transformation Initiative").orElseThrow();
		final CRisk risk = new CRisk("Minor Delays in Third-Party Integrations", project);
		risk.setRiskSeverity(ERiskSeverity.LOW);
		risk.setDescription("External vendor may experience minor delays in API delivery");
		riskService.save(risk);
	}

	/** Creates manufacturing company. */
	private void createManufacturingCompany() {
		final CCompany manufacturing = new CCompany("Of Endüstri Dinamikleri");
		manufacturing.setDescription("Hassas mühendislik bileşenlerinde lider üretici");
		manufacturing.setAddress("Sanayi Mahallesi, İstiklal Caddesi No:42, Of/Trabzon");
		manufacturing.setPhone("+90-462-751-0202");
		manufacturing.setEmail("bilgi@ofendüstri.com.tr");
		manufacturing.setWebsite("https://www.ofendüstri.com.tr");
		manufacturing.setTaxNumber("TR-987654321");
		manufacturing.setEnabled(true);
		companyService.save(manufacturing);
	}

	/** Creates medium priority budget risk. */
	private void createMediumPriorityBudgetRisk() {
		final CProject project = projectService.findByName("Product Development Phase 2").orElseThrow();
		final CRisk risk = new CRisk("Budget Overrun Due to Scope Creep", project);
		risk.setRiskSeverity(ERiskSeverity.MEDIUM);
		risk.setDescription("Uncontrolled feature additions may cause budget to exceed allocated resources");
		riskService.save(risk);
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

	/** Creates project manager user. */
	private void createProjectManagerUser() {
		LOGGER.info("createProjectManagerUser called - creating project manager");
		final CUser manager =
				userService.createLoginUser("mkaradeniz", STANDARD_PASSWORD, "Mehmet Emin", "mehmet.karadeniz@ofteknoloji.com.tr", "MANAGER,USER");
		// Set user profile directly on entity
		final String profilePictureFile = PROFILE_PICTURE_MAPPING.get("mkaradeniz");
		final byte[] profilePictureBytes = loadProfilePictureData(profilePictureFile);
		manager.setLastname("Karadeniz");
		manager.setPhone("+90-462-751-1002");
		manager.setProfilePictureData(profilePictureBytes);
		// Set user role directly on entity
		manager.setUserRole(EUserRole.PROJECT_MANAGER);
		manager.setRoles("MANAGER,USER");
		// Set company association directly on entity
		final CCompany company = companyService.findByName("Of Teknoloji Çözümleri").orElseThrow();
		manager.setCompany(company);
		userService.save(manager);
	}

	private void createProjectWithDescription(final String name, final String description) {
		final CProject project = new CProject(name);
		project.setDescription(description);
		projectService.save(project);
		CGanntViewEntityService.createSample(ganntViewEntityService, project);
	}

	/** Creates sample currencies for order management. */
	private void createSampleCurrencies(final CProject project) {
		try {
			// Create basic currencies
			final CCurrency usd = new CCurrency(project, "US Dollar");
			usd.setDescription("US Dollar");
			usd.setCurrencyCode("USD");
			usd.setCurrencySymbol("$");
			currencyService.save(usd);
			final CCurrency eur = new CCurrency(project, "Euro");
			eur.setDescription("Euro");
			eur.setCurrencyCode("EUR");
			eur.setCurrencySymbol("€");
			currencyService.save(eur);
			final CCurrency try_ = new CCurrency(project, "Turkish Lira");
			try_.setDescription("Turkish Lira");
			try_.setCurrencyCode("TRY");
			try_.setCurrencySymbol("₺");
			currencyService.save(try_);
			final CCurrency gbp = new CCurrency(project, "British Pound");
			gbp.setDescription("British Pound");
			gbp.setCurrencyCode("GBP");
			gbp.setCurrencySymbol("£");
			currencyService.save(gbp);
		} catch (final Exception e) {
			LOGGER.error("Error creating sample currencies", e);
			throw new RuntimeException("Failed to create sample currencies", e);
		}
	}

	/** Creates sample decisions for project management. */
	private void createSampleDecisions() {
		try {
			final CProject project1 = projectService.findByName("Digital Transformation Initiative").orElseThrow();
			final CProject project2 = projectService.findByName("Product Development Phase 2").orElseThrow();
			if ((project1 == null) || (project2 == null)) {
				LOGGER.warn("Projects not found for decisions");
				return;
			}
			final CUser manager = userService.findByLogin("mkaradeniz");
			final CUser admin = userService.findByLogin("admin");
			// Decision 1: Technology Stack
			final CDecision techStackDecision = new CDecision("Technology Stack Selection", project1);
			techStackDecision.setDescription(
					"Decision on the primary technology stack for the digital transformation initiative including frontend framework, backend services, and database choices");
			techStackDecision.setImplementationDate(LocalDate.now().minusDays(15).atStartOfDay());
			if (manager != null) {
				techStackDecision.setAccountableUser(manager);
			}
			decisionService.save(techStackDecision);
			// Decision 2: Cloud Provider
			final CDecision cloudDecision = new CDecision("Cloud Provider Selection", project1);
			cloudDecision.setDescription("Strategic decision on cloud infrastructure provider for hosting and scalability requirements");
			cloudDecision.setImplementationDate(LocalDate.now().minusDays(10).atStartOfDay());
			if (admin != null) {
				cloudDecision.setAccountableUser(admin);
			}
			decisionService.save(cloudDecision);
			// Decision 3: Development Methodology
			final CDecision methodologyDecision = new CDecision("Development Methodology", project2);
			methodologyDecision.setDescription("Decision on agile development methodology and sprint structure for product development phase 2");
			methodologyDecision.setImplementationDate(LocalDate.now().minusDays(5).atStartOfDay());
			if (manager != null) {
				methodologyDecision.setAccountableUser(manager);
			}
			decisionService.save(methodologyDecision);
			// Decision 4: Security Framework
			final CDecision securityDecision = new CDecision("Security Framework Implementation", project2);
			securityDecision.setDescription(
					"Decision on comprehensive security framework including authentication, authorization, and data protection measures");
			securityDecision.setImplementationDate(LocalDate.now().minusDays(3).atStartOfDay());
			if (admin != null) {
				securityDecision.setAccountableUser(admin);
			}
			decisionService.save(securityDecision);
		} catch (final Exception e) {
			LOGGER.error("Error creating sample decisions", e);
			throw new RuntimeException("Failed to create sample decisions", e);
		}
	}

	/** Creates sample planning meeting. */
	private void createSamplePlanningMeeting(final CProject project) {
		final CMeeting meeting = new CMeeting("Sprint Planning - Q1 2024", project);
		meeting.setDescription("Planning for next sprint with story estimation and task assignment");
		meeting.setMeetingDate(LocalDateTime.now().plusDays(3).withHour(14).withMinute(0));
		meeting.setEndDate(LocalDateTime.now().plusDays(3).withHour(16).withMinute(0));
		meeting.setLocation("Meeting Room B");
		// Add participants
		final Set<CUser> participants = new HashSet<>();
		final CUser manager = userService.findByLogin("mkaradeniz");
		final CUser analyst = userService.findByLogin("ademir");
		if (manager != null) {
			participants.add(manager);
		}
		if (analyst != null) {
			participants.add(analyst);
		}
		meeting.setParticipants(participants);
		meetingService.save(meeting);
	}

	/** Creates sample project meeting using auxiliary service methods. Demonstrates the use of auxiliary meeting service methods. */
	private void createSampleProjectMeeting(final CProject project) {
		// Create the meeting using new auxiliary methods
		final CMeeting meeting = new CMeeting("Weekly Project Status Meeting", project);
		meeting.setDescription("Weekly status update on project progress, blockers discussion, and next steps planning");
		// Set meeting details using entity methods
		meeting.setMeetingDate(LocalDateTime.now().plusDays(1).withHour(14).withMinute(0));
		meeting.setEndDate(LocalDateTime.now().plusDays(1).withHour(15).withMinute(0));
		meeting.setLocation("Conference Room A");
		// Set meeting content using entity methods
		final CUser responsible = userService.findByLogin("mkaradeniz");
		meeting.setAgenda("Weekly status update on project progress, blockers discussion, and next steps planning");
		meeting.setResponsible(responsible);
		// Set participants using auxiliary method
		final Set<CUser> participants = new HashSet<>();
		participants.add(userService.findByLogin("admin"));
		participants.add(userService.findByLogin("mkaradeniz"));
		participants.add(userService.findByLogin("bozkan"));
		participants.add(userService.findByLogin("msahin"));
		meeting.setParticipants(participants);
		// Set meeting status using proper status entity
		final CMeetingStatus scheduledStatus = meetingStatusService.findByNameAndProject("Scheduled", project).orElseThrow();
		meeting.setStatus(scheduledStatus);
		meeting.setMinutes("Meeting agenda prepared");
		meeting.setLinkedElement("Project management system");
	}

	/** Creates sample retrospective meeting. */
	private void createSampleRetrospectiveMeeting(final CProject project) {
		final CMeeting meeting = new CMeeting("Sprint Retrospective", project);
		meeting.setDescription("Team reflection on what went well, what could be improved, and action items");
		meeting.setMeetingDate(LocalDateTime.now().minusDays(7).withHour(15).withMinute(0));
		meeting.setEndDate(LocalDateTime.now().minusDays(7).withHour(16).withMinute(0));
		meeting.setLocation("Conference Room C");
		// Set proper status for completed meeting
		final CMeetingStatus completedStatus = meetingStatusService.findByNameAndProject("Completed", project).orElseThrow();
		meeting.setStatus(completedStatus);
		// Add participants and attendees
		final Set<CUser> participants = new HashSet<>();
		final Set<CUser> attendees = new HashSet<>();
		final CUser manager = userService.findByLogin("mkaradeniz");
		final CUser dev1 = userService.findByLogin("ademir");
		final CUser dev2 = userService.findByLogin("msahin");
		if (manager != null) {
			participants.add(manager);
			attendees.add(manager);
		}
		if (dev1 != null) {
			participants.add(dev1);
			attendees.add(dev1);
		}
		if (dev2 != null) {
			participants.add(dev2);
		}
		meeting.setParticipants(participants);
		meeting.setAttendees(attendees);
		meetingService.save(meeting);
	}

	/** Creates sample review meeting. */
	private void createSampleReviewMeeting(final CProject project) {
		final CMeeting meeting = new CMeeting("Code Review Session", project);
		meeting.setDescription("Review of architectural changes and code quality improvements");
		meeting.setMeetingDate(LocalDateTime.now().minusDays(2).withHour(10).withMinute(0));
		meeting.setEndDate(LocalDateTime.now().minusDays(2).withHour(11).withMinute(30));
		meeting.setLocation("Virtual - Zoom");
		// Add participants
		final Set<CUser> participants = new HashSet<>();
		final CUser manager = userService.findByLogin("mkaradeniz");
		final CUser dev = userService.findByLogin("msahin");
		if (manager != null) {
			participants.add(manager);
		}
		if (dev != null) {
			participants.add(dev);
		}
		meeting.setParticipants(participants);
		meetingService.save(meeting);
	}

	/** Creates sample standup meeting. */
	private void createSampleStandupMeeting(final CProject project) {
		final CMeeting meeting = new CMeeting("Daily Standup - Sprint 3", project);
		meeting.setDescription("Daily progress sync and impediment discussion");
		meeting.setMeetingDate(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0));
		meeting.setEndDate(LocalDateTime.now().plusDays(1).withHour(9).withMinute(30));
		meeting.setLocation("Conference Room A");
		// Set proper status
		final CMeetingStatus scheduledStatus = meetingStatusService.findByNameAndProject("Scheduled", project).orElseThrow();
		meeting.setStatus(scheduledStatus);
		// Add participants
		final Set<CUser> participants = new HashSet<>();
		final CUser manager = userService.findByLogin("mkaradeniz");
		final CUser dev1 = userService.findByLogin("ademir");
		final CUser dev2 = userService.findByLogin("msahin");
		if (manager != null) {
			participants.add(manager);
		}
		if (dev1 != null) {
			participants.add(dev1);
		}
		if (dev2 != null) {
			participants.add(dev2);
		}
		meeting.setParticipants(participants);
		meetingService.save(meeting);
	}

	private void initializeScreenWithFields(final CProject project) throws Exception {
		// Create the screen
		CTimer.stamp();
		//
		screenService.save(CUserViewService.createBasicView(project));
		screenService.save(CRiskViewService.createBasicView(project));
		screenService.save(CMeetingViewService.createBasicView(project));
		screenService.save(CDecisionViewService.createBasicView(project));
		screenService.save(COrdersViewService.createBasicView(project));
		screenService.save(CActivityViewService.createBasicView(project));
		screenService.save(CPageEntityViewService.createBasicView(project));
		CTimer.print();
	}

	/** Creates system architecture design activity. */
	private void createSystemArchitectureActivity(final CProject project) {
		// Create the activity using new auxiliary methods
		final CActivity archDesign = new CActivity("System Architecture Design", project);
		// Find and set the activity type
		final CActivityType designType = activityTypeService.findByNameAndProject("Design", project).orElseThrow();
		if (designType == null) {
			LOGGER.warn("Design activity type not found for project, using null");
		}
		// Set activity type and description using auxiliary method
		archDesign.setActivityType(designType);
		archDesign.setDescription("Design scalable system architecture for infrastructure modernization");
		// Set assigned users using auxiliary method
		final CUser teamMember2 = userService.findByLogin("bozkan");
		final CUser admin = userService.findByLogin("admin");
		archDesign.setAssignedTo(teamMember2);
		archDesign.setCreatedBy(admin);
		// Set time tracking using entity methods
		archDesign.setEstimatedHours(new BigDecimal("60.00"));
		archDesign.setActualHours(new BigDecimal("45.00"));
		archDesign.setRemainingHours(new BigDecimal("15.00"));
		// Set date information using auxiliary method
		archDesign.setStartDate(LocalDate.now().minusDays(15));
		archDesign.setDueDate(LocalDate.now().plusDays(10));
		// Set status and priority using auxiliary method
		final CActivityStatus onHoldStatus = activityStatusService.findByNameAndProject("On Hold", project).orElseThrow();
		archDesign.setStatus(onHoldStatus);
		archDesign.setProgressPercentage(65);
		activityService.save(archDesign);
		// Create comments
		commentService.createComment("Initial system architecture design phase started", archDesign, admin);
		commentService.createComment("Completed high-level architecture diagrams and component definitions", archDesign, admin);
		commentService.createComment("Reviewed architecture with team and incorporated feedback", archDesign, teamMember2);
		commentService.createComment("Activity on hold pending stakeholder approval of design changes", archDesign, teamMember2);
		LOGGER.info("System architecture activity created successfully");
	}

	/** Creates team member Alice Davis. */
	private void createTeamMemberAlice() {
		final CUser analyst = userService.createLoginUser("ademir", STANDARD_PASSWORD, "Ayşe", "ayse.demir@ofsaglik.com.tr", "USER");
		// Set user profile directly on entity
		final String profilePictureFile = PROFILE_PICTURE_MAPPING.get("ademir");
		final byte[] profilePictureBytes = loadProfilePictureData(profilePictureFile);
		analyst.setLastname("Demir");
		analyst.setPhone("+90-462-751-1005");
		analyst.setProfilePictureData(profilePictureBytes);
		// Set user role directly on entity
		analyst.setUserRole(EUserRole.TEAM_MEMBER);
		analyst.setRoles("USER");
		// Set company association directly on entity
		final CCompany company = companyService.findByName("Of Sağlık Teknolojileri").orElseThrow();
		analyst.setCompany(company);
		userService.save(analyst);
	}

	/** Creates team member Bob Wilson. */
	private void createTeamMemberBob() {
		final CUser developer = userService.createLoginUser("bozkan", STANDARD_PASSWORD, "Burak", "burak.ozkan@ofdanismanlik.com.tr", "USER");
		// Set user profile directly on entity
		final String profilePictureFile = PROFILE_PICTURE_MAPPING.get("bozkan");
		final byte[] profilePictureBytes = loadProfilePictureData(profilePictureFile);
		developer.setLastname("Özkan");
		developer.setPhone("+90-462-751-1004");
		developer.setProfilePictureData(profilePictureBytes);
		// Set user role directly on entity
		developer.setUserRole(EUserRole.TEAM_MEMBER);
		developer.setRoles("USER");
		// Set company association directly on entity
		final CCompany company = companyService.findByName("Of Stratejik Danışmanlık").orElseThrow();
		developer.setCompany(company);
		userService.save(developer);
	}

	/** Creates team member Mary Johnson. */
	private void createTeamMemberMary() {
		final CUser teamMember = userService.createLoginUser("msahin", STANDARD_PASSWORD, "Merve", "merve.sahin@ofendüstri.com.tr", "USER");
		// Set user profile directly on entity
		final String profilePictureFile = PROFILE_PICTURE_MAPPING.get("msahin");
		final byte[] profilePictureBytes = loadProfilePictureData(profilePictureFile);
		teamMember.setLastname("Şahin");
		teamMember.setPhone("+90-462-751-1003");
		teamMember.setProfilePictureData(profilePictureBytes);
		// Set user role directly on entity
		teamMember.setUserRole(EUserRole.TEAM_MEMBER);
		teamMember.setRoles("USER");
		// Set company association directly on entity
		final CCompany company = companyService.findByName("Of Endüstri Dinamikleri").orElseThrow();
		teamMember.setCompany(company);
		userService.save(teamMember);
	}

	/** Creates team member users across different companies. */
	private void createTeamMemberUsers() {
		createTeamMemberMary();
		createTeamMemberBob();
		createTeamMemberAlice();
	}

	/** Creates technology startup company. */
	private void createTechCompany() {
		final CCompany techStartup = new CCompany("Of Teknoloji Çözümleri");
		techStartup.setDescription("Dijital dönüşüm için yenilikçi teknoloji çözümleri");
		techStartup.setAddress("Cumhuriyet Mahallesi, Atatürk Caddesi No:15, Of/Trabzon");
		techStartup.setPhone("+90-462-751-0101");
		techStartup.setEmail("iletisim@ofteknoloji.com.tr");
		techStartup.setWebsite("https://www.ofteknoloji.com.tr");
		techStartup.setTaxNumber("TR-123456789");
		techStartup.setEnabled(true);
		companyService.save(techStartup);
	}

	/** Creates technical documentation activity. */
	private void createTechnicalDocumentationActivity(final CProject project) {
		// Create the activity using new auxiliary methods
		final CActivity techDoc = new CActivity("Technical Documentation Update", project);
		// Find and set the activity type
		final CActivityType documentationType = activityTypeService.findByNameAndProject("Documentation", project).orElseThrow();
		if (documentationType == null) {
			LOGGER.warn("Documentation activity type not found for project, using null");
		}
		// Set activity type and description using auxiliary method
		techDoc.setActivityType(documentationType);
		techDoc.setDescription("Update and enhance technical documentation for customer experience features");
		// Set assigned users using auxiliary method
		final CUser analyst = userService.findByLogin("ademir");
		final CUser manager = userService.findByLogin("mkaradeniz");
		techDoc.setAssignedTo(analyst);
		techDoc.setCreatedBy(manager);
		// Set time tracking using entity methods (completed activity)
		techDoc.setEstimatedHours(new BigDecimal("16.00"));
		techDoc.setActualHours(new BigDecimal("16.00"));
		techDoc.setRemainingHours(new BigDecimal("0.00"));
		// Set date information using entity methods (completed activity)
		techDoc.setStartDate(LocalDate.now().minusDays(5));
		techDoc.setDueDate(LocalDate.now().minusDays(1));
		techDoc.setCompletionDate(LocalDate.now().minusDays(1));
		// Set status and priority using auxiliary method (completed activity)
		final CActivityStatus completedStatus = activityStatusService.findByNameAndProject("Completed", project).orElseThrow();
		techDoc.setStatus(completedStatus);
		techDoc.setProgressPercentage(100);
		activityService.save(techDoc);
		// Create comments
		commentService.createComment("Initial technical documentation review and updates started", techDoc, manager);
		commentService.createComment("Completed updates to user guides and API documentation", techDoc, analyst);
		commentService.createComment("Reviewed documentation with team and incorporated feedback", techDoc, analyst);
		commentService.createComment("Documentation successfully updated and approved by stakeholders", techDoc, manager);
	}

	/** Creates UI testing activity. */
	private void createUITestingActivity(final CProject project) {
		// Create the activity using new auxiliary methods
		final CActivity uiTesting = new CActivity("User Interface Testing", project);
		// Find and set the activity type
		final CActivityType testingType = activityTypeService.findByNameAndProject("Testing", project).orElseThrow();
		if (testingType == null) {
			LOGGER.warn("Testing activity type not found for project, using null");
		}
		// Set activity type and description using auxiliary method
		uiTesting.setActivityType(testingType);
		uiTesting.setDescription("Comprehensive testing of user interface components and workflows");
		// Set assigned users using auxiliary method
		final CUser teamMember1 = userService.findByLogin("msahin");
		final CUser manager = userService.findByLogin("mkaradeniz");
		uiTesting.setAssignedTo(teamMember1);
		uiTesting.setCreatedBy(manager);
		// Set time tracking using entity methods
		uiTesting.setEstimatedHours(new BigDecimal("24.00"));
		uiTesting.setActualHours(new BigDecimal("20.00"));
		uiTesting.setRemainingHours(new BigDecimal("4.00"));
		// Set date information using auxiliary method
		uiTesting.setStartDate(LocalDate.now().minusDays(7));
		uiTesting.setDueDate(LocalDate.now().plusDays(3));
		// Set status and priority using auxiliary method
		final CActivityStatus inProgressStatus = activityStatusService.findByNameAndProject("In Progress", project).orElseThrow();
		uiTesting.setStatus(inProgressStatus);
		uiTesting.setProgressPercentage(85);
		activityService.save(uiTesting);
		// Create comments
		commentService.createComment("UI testing activity initiated with comprehensive test plan", uiTesting, manager);
		commentService.createComment("Completed responsive design testing across multiple devices", uiTesting, teamMember1);
		commentService.createComment("Working on accessibility testing and user experience validation", uiTesting, teamMember1);
	}

	/** Initializes comprehensive activity data with available fields populated. */
	private void initializeActivities(final CProject project) {
		try {
			// Create at least 3 activities per project
			createBackendDevActivity(project);
			createUITestingActivity(project);
			createSystemArchitectureActivity(project);
			createTechnicalDocumentationActivity(project);
			// Additional activities to meet 3+ per project requirement
			createAdditionalInfrastructureActivities(project);
			createAdditionalDigitalTransformationActivities(project);
			createAdditionalProductDevelopmentActivities(project);
			createAdditionalCustomerExperienceActivities(project);
			// LOGGER.info("Successfully created comprehensive activity samples");
		} catch (final Exception e) {
			LOGGER.error("Error creating sample activities", e);
			throw new RuntimeException("Failed to initialize activities", e);
		}
	}

	/** Initialize activity status entities with comprehensive sample data. */
	private void initializeActivityStatuses() {
		try {
			// Get all projects
			final String[] projectNames = {
					"Digital Transformation Initiative", "Product Development Phase 2", "Infrastructure Modernization",
					"Customer Experience Enhancement"
			};
			// Define meeting types to create for each project
			for (final String projectName : projectNames) {
				final CProject project = projectService.findByName(projectName).orElseThrow();
				createActivityStatus("Not Started", project, "Activity has not been started yet", "#95a5a6", false, 1);
				createActivityStatus("In Progress", project, "Activity is currently in progress", "#3498db", false, 2);
				createActivityStatus("On Hold", project, "Activity is temporarily on hold", "#f39c12", false, 3);
				createActivityStatus("Completed", project, "Activity has been completed", "#27ae60", true, 4);
				createActivityStatus("Cancelled", project, "Activity has been cancelled", "#e74c3c", true, 5);
			}
		} catch (final Exception e) {
			LOGGER.error("Error initializing activity statuses", e);
			throw new RuntimeException("Failed to initialize activity statuses", e);
		}
	}

	/** Initializes activity types for categorizing different kinds of work. Creates types for all projects to ensure project-specific
	 * categorization. */
	private void initializeActivityTypes(final CProject project) {
		try {
			// Define activity types to create for each project
			final String[][] activityTypes = {
					{
							"Development", "Software development and coding tasks"
					}, {
							"Testing", "Quality assurance and testing activities"
					}, {
							"Design", "UI/UX design and system architecture"
					}, {
							"Documentation", "Technical writing and documentation"
					}, {
							"Research", "Research and analysis activities"
					}
			};
			for (final String[] typeData : activityTypes) {
				final CActivityType item = activityTypeService.createEntity(typeData[0], project);
				item.setDescription(typeData[1]);
			}
			LOGGER.info("Successfully created activity types for all projects");
		} catch (final Exception e) {
			LOGGER.error("Error creating activity types", e);
			throw new RuntimeException("Failed to initialize activity types", e);
		}
	}

	/** Initialize comment priority entities with comprehensive sample data. */
	private void initializeCommentPriorities() {
		try {
			createCommentPriority("Critical", "Critical priority requiring immediate attention", "#e74c3c", 1, false, 1);
			createCommentPriority("High", "High priority requiring urgent attention", "#f39c12", 2, false, 2);
			createCommentPriority("Normal", "Normal priority for standard processing", "#3498db", 3, true, 3);
			createCommentPriority("Low", "Low priority for non-urgent matters", "#95a5a6", 4, false, 4);
			createCommentPriority("Info", "Informational priority for reference", "#27ae60", 5, false, 5);
			LOGGER.info("Comment priorities initialized successfully");
		} catch (final Exception e) {
			LOGGER.error("Error initializing comment priorities", e);
			throw new RuntimeException("Failed to initialize comment priorities", e);
		}
	}

	/** Initializes sample companies with full details. */
	private void initializeCompanies() {
		try {
			createTechCompany();
			createManufacturingCompany();
			createConsultingCompany();
			createHealthcareCompany();
			// LOGGER.info("Successfully created 4 sample companies");
		} catch (final Exception e) {
			LOGGER.error("Error creating sample companies", e);
			throw new RuntimeException("Failed to initialize companies", e);
		}
	}

	/** Initialize decision status entities with comprehensive sample data. */
	private void initializeDecisionStatuses() {
		try {
			// Get all projects
			final String[] projectNames = {
					"Digital Transformation Initiative", "Product Development Phase 2", "Infrastructure Modernization",
					"Customer Experience Enhancement"
			};
			// Define meeting types to create for each project
			for (final String projectName : projectNames) {
				final CProject project = projectService.findByName(projectName).orElseThrow();
				createDecisionStatus("Draft", project, "Decision is in draft state", "#95a5a6", false, 1);
				createDecisionStatus("Under Review", project, "Decision is being reviewed", "#f39c12", false, 2);
				createDecisionStatus("Approved", project, "Decision has been approved", "#27ae60", false, 3);
				createDecisionStatus("Implemented", project, "Decision has been implemented", "#2ecc71", true, 4);
				createDecisionStatus("Rejected", project, "Decision has been rejected", "#e74c3c", true, 5);
			}
			LOGGER.info("Decision statuses initialized successfully");
		} catch (final Exception e) {
			LOGGER.error("Error initializing decision statuses", e);
			throw new RuntimeException("Failed to initialize decision statuses", e);
		}
	}
	// Risk creation methods

	/** Initializes decision types for categorizing different kinds of decisions. Creates types for all projects to ensure project-specific
	 * categorization. */
	private void initializeDecisionTypes() {
		// TODO fix
	}

	private void initializeGridEntity(CProject project) {
		try {
			// Create sample grid entity for Activities
			CGridEntity grid = new CGridEntity(CActivitiesView.VIEW_NAME, project);
			grid.setDescription("Default grid for " + CActivitiesView.VIEW_NAME);
			grid.setDataServiceBeanName("CActivityService");
			grid.setSelectedFields("name:1,activityType:2,status:3,estimatedHours:4,progressPercentage:5");
			gridEntityService.save(grid);
			// Create sample grid entity for Meetings
			grid = new CGridEntity(CMeetingsView.VIEW_NAME, project);
			grid.setDescription("Default grid for " + CMeetingsView.VIEW_NAME);
			grid.setDataServiceBeanName("CMeetingService");
			grid.setSelectedFields("name:1,status:2,meetingType:3,meetingDate:4,location:5");
			gridEntityService.save(grid);
			// Create sample grid entity for Users
			grid = new CGridEntity(CUsersView.VIEW_NAME, project);
			grid.setDescription("User management grid with role and contact information");
			grid.setDataServiceBeanName("CUserService");
			grid.setSelectedFields("name:1,lastname:2,userRole:3,email:4,phone:5");
			gridEntityService.save(grid);
			// Create sample grid entity for Projects
			grid = new CGridEntity(CProjectsView.VIEW_NAME, project);
			grid.setDescription("Project management grid with basic project information");
			grid.setDataServiceBeanName("CProjectService");
			grid.setSelectedFields("name:1,description:2,createdBy:3,createdDate:4");
			gridEntityService.save(grid);
			// Create sample grid entity for Risks
			grid = new CGridEntity(CRiskView.VIEW_NAME, project);
			grid.setDescription("Risk management grid with severity and status tracking");
			grid.setDataServiceBeanName("CRiskService");
			grid.setSelectedFields("name:1,riskSeverity:2,description:3,createdBy:4,createdDate:5");
			gridEntityService.save(grid);
			// Create sample grid entity for Decisions
			grid = new CGridEntity(CDecisionsView.VIEW_NAME, project);
			grid.setDescription("Decision tracking grid with status and accountability");
			grid.setDataServiceBeanName("CDecisionService");
			grid.setSelectedFields("name:1,status:2,accountableUser:3,implementationDate:4,description:5");
			gridEntityService.save(grid);
			// Create sample grid entity for Companies
			grid = new CGridEntity(CCompanyView.VIEW_NAME, project);
			grid.setDescription("Company directory grid with contact and business information");
			grid.setDataServiceBeanName("CCompanyService");
			grid.setSelectedFields("name:1,email:2,phone:3,website:4,enabled:5");
			gridEntityService.save(grid);
			// Create sample grid entity for Activity Types
			grid = new CGridEntity(CActivityTypeView.VIEW_NAME, project);
			grid.setDescription("Activity type management grid with color coding");
			grid.setDataServiceBeanName("CActivityTypeService");
			grid.setSelectedFields("name:1,description:2,color:3,sortOrder:4");
			gridEntityService.save(grid);
			// Create sample grid entity for Activity Status
			grid = new CGridEntity(CActivityStatusView.VIEW_NAME, project);
			grid.setDescription("Activity status management grid with workflow tracking");
			grid.setDataServiceBeanName("CActivityStatusService");
			grid.setSelectedFields("name:1,description:2,color:3,sortOrder:4");
			gridEntityService.save(grid);
			// Create sample grid entity for Meeting Types
			grid = new CGridEntity(CMeetingTypeView.VIEW_NAME, project);
			grid.setDescription("Meeting type configuration grid");
			grid.setDataServiceBeanName("CMeetingTypeService");
			grid.setSelectedFields("name:1,description:2,sortOrder:3");
			gridEntityService.save(grid);
			// Log completion
			LOGGER.info("Successfully created sample grid entities for project: {}", project.getName());
		} catch (final Exception e) {
			LOGGER.error("Error creating sample grid entity for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize grid entity for project: " + project.getName(), e);
		}
	}

	/** Initializes sample meetings with participants and content. */
	private void initializeMeetings(final CProject project) {
		try {
			createSampleProjectMeeting(project);
			createSampleStandupMeeting(project);
			createSamplePlanningMeeting(project);
			createSampleReviewMeeting(project);
			createSampleRetrospectiveMeeting(project);
			LOGGER.info("Successfully created 5 sample meetings");
		} catch (final Exception e) {
			LOGGER.error("Error creating sample meetings", e);
			throw new RuntimeException("Failed to initialize meetings", e);
		}
	}

	/** Initialize meeting status entities with comprehensive sample data. */
	private void initializeMeetingStatuses() {
		try {
			// Get all projects
			final String[] projectNames = {
					"Digital Transformation Initiative", "Product Development Phase 2", "Infrastructure Modernization",
					"Customer Experience Enhancement"
			};
			// Define meeting types to create for each project
			for (final String projectName : projectNames) {
				final CProject project = projectService.findByName(projectName).orElseThrow();
				createMeetingStatus("Scheduled", project, "Meeting is scheduled but not yet started", "#3498db", false, 1);
				createMeetingStatus("In Progress", project, "Meeting is currently in progress", "#f39c12", false, 2);
				createMeetingStatus("Completed", project, "Meeting has been completed successfully", "#27ae60", true, 3);
				createMeetingStatus("Cancelled", project, "Meeting has been cancelled", "#e74c3c", true, 4);
				createMeetingStatus("Postponed", project, "Meeting has been postponed to a later date", "#9b59b6", false, 5);
			}
			LOGGER.info("Meeting statuses initialized successfully");
		} catch (final Exception e) {
			LOGGER.error("Error initializing meeting statuses", e);
			throw new RuntimeException("Failed to initialize meeting statuses", e);
		}
	}

	/** Initializes meeting types for categorizing different kinds of meetings. Creates types for all projects to ensure project-specific
	 * categorization. */
	private void initializeMeetingTypes() {
		try {
			// Get all projects
			final String[] projectNames = {
					"Digital Transformation Initiative", "Product Development Phase 2", "Infrastructure Modernization",
					"Customer Experience Enhancement"
			};
			// Define meeting types to create for each project
			final String[][] meetingTypes = {
					{
							"Standup"
					}, {
							"Planning"
					}, {
							"Review"
					}, {
							"Retrospective"
					}, {
							"One-on-One"
					}
			};
			// Create meeting types for each project
			for (final String projectName : projectNames) {
				final CProject project = projectService.findByName(projectName).orElseThrow();
				for (final String[] typeData : meetingTypes) {
					meetingTypeService.createEntity(typeData[0], project);
				}
			}
			LOGGER.info("Successfully created meeting types for all projects");
		} catch (final Exception e) {
			LOGGER.error("Error creating meeting types", e);
			throw new RuntimeException("Failed to initialize meeting types", e);
		}
	}

	/** Initialize order status entities with comprehensive sample data. */
	private void initializeOrderStatuses() {}

	/** Initializes order types for categorizing different kinds of orders. Creates types for all projects to ensure project-specific
	 * categorization. */
	private void initializeOrderTypes() {
		try {
			// Get all projects
			final String[] projectNames = {
					"Digital Transformation Initiative", "Product Development Phase 2", "Infrastructure Modernization",
					"Customer Experience Enhancement"
			};
			// Define order types to create for each project
			final String[][] orderTypes = {
					{
							"Service Order"
					}, {
							"Purchase Order"
					}, {
							"Maintenance Order"
					}, {
							"Change Order"
					}, {
							"Support Order"
					}
			};
			// Create order types for each project
			for (final String projectName : projectNames) {
				final CProject project = projectService.findByName(projectName).orElseThrow();
				for (final String[] typeData : orderTypes) {
					orderTypeService.createEntity(typeData[0], project);
				}
			}
			LOGGER.info("Successfully created order types for all projects");
		} catch (final Exception e) {
			LOGGER.error("Error creating order types", e);
			throw new RuntimeException("Failed to initialize order types", e);
		}
	}

	private void initializePageEntities(final CProject project) {
		try {
			LOGGER.info("Creating comprehensive sample page entities for project: {}", project.getName());
			// Page 1: Project Overview (Main navigation)
			final CPageEntity projectOverview =
					new CPageEntity("Project Overview", "Project Overview", "project-overview-" + project.getId(), project);
			projectOverview.setDescription("Comprehensive project overview with objectives, current status, and key milestones");
			projectOverview.setTitle("Project.Overview");
			projectOverview.setMenuOrder("5.1");
			projectOverview.setIcon("vaadin:dashboard");
			projectOverview.setContent("<div style='max-width: 1200px; padding: 20px;'>"
					+ "<h2 style='color: #1976d2; border-bottom: 2px solid #1976d2; padding-bottom: 10px;'>📊 Project Overview</h2>"
					+ "<div style='background: #f5f5f5; padding: 15px; margin: 15px 0; border-left: 4px solid #4caf50;'>"
					+ "<h3>🎯 Project Objectives</h3>" + "<p><strong>" + project.getName()
					+ "</strong> aims to deliver innovative solutions that transform how we work.</p>" + "<ul style='margin: 10px 0;'>"
					+ "<li>✅ Enhance operational efficiency by 40%</li>" + "<li>✅ Improve user experience across all touchpoints</li>"
					+ "<li>✅ Implement scalable technology solutions</li>" + "<li>✅ Establish best practices for future projects</li>" + "</ul></div>"
					+ "<div style='background: #e3f2fd; padding: 15px; margin: 15px 0; border-left: 4px solid #2196f3;'>"
					+ "<h3>📈 Current Status</h3>" + "<p>The project is currently <strong>65% complete</strong> with active development underway.</p>"
					+ "<div style='background: #fff; padding: 10px; margin: 10px 0; border-radius: 4px;'>"
					+ "<div style='background: #4caf50; height: 20px; width: 65%; border-radius: 10px; position: relative;'></div>"
					+ "<p style='margin: 5px 0; font-size: 14px;'>Progress: 65% Complete</p>" + "</div></div>"
					+ "<div style='background: #fff3e0; padding: 15px; margin: 15px 0; border-left: 4px solid #ff9800;'>"
					+ "<h3>🗓️ Key Milestones</h3>" + "<table style='width: 100%; border-collapse: collapse;'>"
					+ "<tr style='background: #fafafa;'><th style='padding: 8px; text-align: left; border: 1px solid #ddd;'>Milestone</th><th style='padding: 8px; text-align: left; border: 1px solid #ddd;'>Status</th><th style='padding: 8px; text-align: left; border: 1px solid #ddd;'>Date</th></tr>"
					+ "<tr><td style='padding: 8px; border: 1px solid #ddd;'>✅ Requirements Analysis</td><td style='padding: 8px; border: 1px solid #ddd; color: #4caf50;'>Completed</td><td style='padding: 8px; border: 1px solid #ddd;'>Jan 2024</td></tr>"
					+ "<tr><td style='padding: 8px; border: 1px solid #ddd;'>🔄 System Design</td><td style='padding: 8px; border: 1px solid #ddd; color: #2196f3;'>In Progress</td><td style='padding: 8px; border: 1px solid #ddd;'>Feb 2024</td></tr>"
					+ "<tr><td style='padding: 8px; border: 1px solid #ddd;'>⏳ Development Phase</td><td style='padding: 8px; border: 1px solid #ddd; color: #ff9800;'>Planned</td><td style='padding: 8px; border: 1px solid #ddd;'>Mar 2024</td></tr>"
					+ "<tr><td style='padding: 8px; border: 1px solid #ddd;'>🧪 Testing & QA</td><td style='padding: 8px; border: 1px solid #ddd; color: #9e9e9e;'>Planned</td><td style='padding: 8px; border: 1px solid #ddd;'>Apr 2024</td></tr>"
					+ "</table></div></div>");
			pageEntityService.save(projectOverview);
			// Page 2: Team Directory (Team management)
			final CPageEntity teamDirectory = new CPageEntity("Team Directory", "Team Directory", "team-directory-" + project.getId(), project);
			teamDirectory.setDescription("Comprehensive team member directory with roles, skills, and contact information");
			teamDirectory.setTitle("Project.Team.Directory");
			teamDirectory.setMenuOrder("5.2");
			teamDirectory.setIcon("vaadin:group");
			teamDirectory.setContent("<div style='max-width: 1200px; padding: 20px;'>"
					+ "<h2 style='color: #7b1fa2; border-bottom: 2px solid #7b1fa2; padding-bottom: 10px;'>👥 Team Directory</h2>"
					+ "<p style='margin-bottom: 30px;'>Our dedicated project team brings together diverse expertise and experience to ensure project success.</p>"
					+ "<div style='display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px;'>"
					+ "<div style='background: #fff; border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>"
					+ "<div style='display: flex; align-items: center; margin-bottom: 15px;'>"
					+ "<div style='width: 60px; height: 60px; background: #1976d2; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-size: 24px; font-weight: bold; margin-right: 15px;'>MK</div>"
					+ "<div><h4 style='margin: 0; color: #1976d2;'>Mehmet Karadeniz</h4><p style='margin: 5px 0; color: #666;'>Project Manager</p></div></div>"
					+ "<p><strong>📧 Email:</strong> mkaradeniz@derbent.tech</p>" + "<p><strong>📱 Phone:</strong> +90-462-751-1002</p>"
					+ "<p><strong>🎯 Responsibilities:</strong> Project coordination, stakeholder management, timeline oversight</p>"
					+ "<div style='background: #e3f2fd; padding: 10px; margin-top: 10px; border-radius: 4px;'>"
					+ "<strong>💼 Skills:</strong> Agile, Scrum, Risk Management, Leadership</div></div>"
					+ "<div style='background: #fff; border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>"
					+ "<div style='display: flex; align-items: center; margin-bottom: 15px;'>"
					+ "<div style='width: 60px; height: 60px; background: #388e3c; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-size: 24px; font-weight: bold; margin-right: 15px;'>MS</div>"
					+ "<div><h4 style='margin: 0; color: #388e3c;'>Merve Şahin</h4><p style='margin: 5px 0; color: #666;'>Lead Developer</p></div></div>"
					+ "<p><strong>📧 Email:</strong> msahin@derbent.tech</p>" + "<p><strong>📱 Phone:</strong> +90-462-751-1003</p>"
					+ "<p><strong>🎯 Responsibilities:</strong> Technical leadership, architecture design, code review</p>"
					+ "<div style='background: #e8f5e8; padding: 10px; margin-top: 10px; border-radius: 4px;'>"
					+ "<strong>💼 Skills:</strong> Java, Spring Boot, Vaadin, Microservices</div></div>"
					+ "<div style='background: #fff; border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>"
					+ "<div style='display: flex; align-items: center; margin-bottom: 15px;'>"
					+ "<div style='width: 60px; height: 60px; background: #f57c00; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-size: 24px; font-weight: bold; margin-right: 15px;'>AD</div>"
					+ "<div><h4 style='margin: 0; color: #f57c00;'>Ayşe Demir</h4><p style='margin: 5px 0; color: #666;'>System Analyst</p></div></div>"
					+ "<p><strong>📧 Email:</strong> ademir@derbent.tech</p>" + "<p><strong>📱 Phone:</strong> +90-462-751-1005</p>"
					+ "<p><strong>🎯 Responsibilities:</strong> Requirements analysis, system design, documentation</p>"
					+ "<div style='background: #fff3e0; padding: 10px; margin-top: 10px; border-radius: 4px;'>"
					+ "<strong>💼 Skills:</strong> Business Analysis, UML, Requirements Engineering</div></div>"
					+ "<div style='background: #fff; border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>"
					+ "<div style='display: flex; align-items: center; margin-bottom: 15px;'>"
					+ "<div style='width: 60px; height: 60px; background: #7b1fa2; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-size: 24px; font-weight: bold; margin-right: 15px;'>BO</div>"
					+ "<div><h4 style='margin: 0; color: #7b1fa2;'>Burak Özkan</h4><p style='margin: 5px 0; color: #666;'>Senior Developer</p></div></div>"
					+ "<p><strong>📧 Email:</strong> bozkan@derbent.tech</p>" + "<p><strong>📱 Phone:</strong> +90-462-751-1004</p>"
					+ "<p><strong>🎯 Responsibilities:</strong> Backend development, API design, database optimization</p>"
					+ "<div style='background: #f3e5f5; padding: 10px; margin-top: 10px; border-radius: 4px;'>"
					+ "<strong>💼 Skills:</strong> PostgreSQL, REST APIs, Performance Optimization</div></div></div></div>");
			pageEntityService.save(teamDirectory);
			// Page 3: Resource Library (Knowledge base)
			final CPageEntity resourceLibrary =
					new CPageEntity("Resource Library", "Resource Library", "resource-library-" + project.getId(), project);
			resourceLibrary.setDescription("Centralized knowledge base with project documents, templates, best practices, and learning resources");
			resourceLibrary.setTitle("Project.Resources.Library");
			resourceLibrary.setMenuOrder("5.3");
			resourceLibrary.setIcon("vaadin:archive");
			resourceLibrary.setContent("<div style='max-width: 1200px; padding: 20px;'>"
					+ "<h2 style='color: #d32f2f; border-bottom: 2px solid #d32f2f; padding-bottom: 10px;'>📚 Resource Library</h2>"
					+ "<p style='margin-bottom: 30px;'>Access comprehensive project documentation, templates, and knowledge resources in one central location.</p>"
					+ "<div style='display: grid; grid-template-columns: repeat(auto-fit, minmax(350px, 1fr)); gap: 25px;'>"
					+ "<div style='background: #fff; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1);'>"
					+ "<div style='background: #1976d2; color: white; padding: 15px;'><h3 style='margin: 0; display: flex; align-items: center;'><span style='margin-right: 10px;'>📋</span>Project Documentation</h3></div>"
					+ "<div style='padding: 20px;'><ul style='margin: 0; padding-left: 20px;'>"
					+ "<li style='margin: 8px 0;'>📄 Project Charter & Scope Statement</li>"
					+ "<li style='margin: 8px 0;'>📊 Business Requirements Document (BRD)</li>"
					+ "<li style='margin: 8px 0;'>🏗️ System Architecture Documentation</li>"
					+ "<li style='margin: 8px 0;'>⚠️ Risk Assessment & Mitigation Plans</li>"
					+ "<li style='margin: 8px 0;'>📈 Project Status Reports</li></ul>"
					+ "<a href='#' style='display: inline-block; margin-top: 15px; padding: 8px 16px; background: #1976d2; color: white; text-decoration: none; border-radius: 4px;'>Browse Documents →</a></div></div>"
					+ "<div style='background: #fff; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1);'>"
					+ "<div style='background: #388e3c; color: white; padding: 15px;'><h3 style='margin: 0; display: flex; align-items: center;'><span style='margin-right: 10px;'>📝</span>Templates & Forms</h3></div>"
					+ "<div style='padding: 20px;'><ul style='margin: 0; padding-left: 20px;'>"
					+ "<li style='margin: 8px 0;'>📝 Meeting Minutes Template</li>"
					+ "<li style='margin: 8px 0;'>📊 Weekly Status Report Template</li>" + "<li style='margin: 8px 0;'>🔄 Change Request Form</li>"
					+ "<li style='margin: 8px 0;'>🧪 Test Case Documentation Template</li>"
					+ "<li style='margin: 8px 0;'>📋 Project Evaluation Form</li></ul>"
					+ "<a href='#' style='display: inline-block; margin-top: 15px; padding: 8px 16px; background: #388e3c; color: white; text-decoration: none; border-radius: 4px;'>Download Templates →</a></div></div>"
					+ "<div style='background: #fff; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1);'>"
					+ "<div style='background: #f57c00; color: white; padding: 15px;'><h3 style='margin: 0; display: flex; align-items: center;'><span style='margin-right: 10px;'>🔗</span>External Resources</h3></div>"
					+ "<div style='padding: 20px;'><ul style='margin: 0; padding-left: 20px;'>"
					+ "<li style='margin: 8px 0;'>🌐 Spring Boot Documentation</li>" + "<li style='margin: 8px 0;'>🎨 Vaadin Design System</li>"
					+ "<li style='margin: 8px 0;'>🗄️ PostgreSQL Best Practices</li>" + "<li style='margin: 8px 0;'>🧪 JUnit Testing Guidelines</li>"
					+ "<li style='margin: 8px 0;'>📚 Maven Configuration Guide</li></ul>"
					+ "<a href='#' style='display: inline-block; margin-top: 15px; padding: 8px 16px; background: #f57c00; color: white; text-decoration: none; border-radius: 4px;'>Access Links →</a></div></div>"
					+ "<div style='background: #fff; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1);'>"
					+ "<div style='background: #7b1fa2; color: white; padding: 15px;'><h3 style='margin: 0; display: flex; align-items: center;'><span style='margin-right: 10px;'>🏆</span>Best Practices</h3></div>"
					+ "<div style='padding: 20px;'><ul style='margin: 0; padding-left: 20px;'>"
					+ "<li style='margin: 8px 0;'>💻 Derbent Coding Standards</li>" + "<li style='margin: 8px 0;'>🔍 Code Review Guidelines</li>"
					+ "<li style='margin: 8px 0;'>🚀 Deployment Procedures</li>" + "<li style='margin: 8px 0;'>📊 Performance Optimization Tips</li>"
					+ "<li style='margin: 8px 0;'>🔒 Security Best Practices</li></ul>"
					+ "<a href='#' style='display: inline-block; margin-top: 15px; padding: 8px 16px; background: #7b1fa2; color: white; text-decoration: none; border-radius: 4px;'>View Guidelines →</a></div></div></div></div>");
			pageEntityService.save(resourceLibrary);
			// Page 4: Project Roadmap (Strategic planning)
			final CPageEntity projectRoadmap = new CPageEntity("Project Roadmap", "Project Roadmap", "project-roadmap-" + project.getId(), project);
			projectRoadmap.setDescription("Strategic project roadmap with timeline, dependencies, and future planning");
			projectRoadmap.setTitle("Project.Roadmap");
			projectRoadmap.setMenuOrder("5.4");
			projectRoadmap.setIcon("vaadin:calendar-clock");
			projectRoadmap.setContent("<div style='max-width: 1200px; padding: 20px;'>"
					+ "<h2 style='color: #00796b; border-bottom: 2px solid #00796b; padding-bottom: 10px;'>🗺️ Project Roadmap</h2>"
					+ "<p style='margin-bottom: 30px;'>Comprehensive timeline showing project phases, milestones, and strategic objectives.</p>"
					+ "<div style='background: #f5f5f5; padding: 20px; border-radius: 8px; margin-bottom: 25px;'>"
					+ "<h3 style='color: #00796b; margin-top: 0;'>📅 Current Quarter Highlights</h3>"
					+ "<div style='display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 15px;'>"
					+ "<div style='background: #e0f2f1; padding: 15px; border-radius: 6px; border-left: 4px solid #4caf50;'>"
					+ "<h4 style='margin: 0 0 10px 0; color: #00695c;'>Q1 2024 - Foundation</h4>"
					+ "<p style='margin: 0; font-size: 14px;'>✅ Requirements gathering<br>✅ Team formation<br>🔄 Architecture design</p></div>"
					+ "<div style='background: #e3f2fd; padding: 15px; border-radius: 6px; border-left: 4px solid #2196f3;'>"
					+ "<h4 style='margin: 0 0 10px 0; color: #0277bd;'>Q2 2024 - Development</h4>"
					+ "<p style='margin: 0; font-size: 14px;'>⏳ Core development<br>⏳ API implementation<br>⏳ Database optimization</p></div>"
					+ "<div style='background: #fff3e0; padding: 15px; border-radius: 6px; border-left: 4px solid #ff9800;'>"
					+ "<h4 style='margin: 0 0 10px 0; color: #f57c00;'>Q3 2024 - Testing</h4>"
					+ "<p style='margin: 0; font-size: 14px;'>⏳ Quality assurance<br>⏳ User testing<br>⏳ Performance tuning</p></div>"
					+ "<div style='background: #f3e5f5; padding: 15px; border-radius: 6px; border-left: 4px solid #9c27b0;'>"
					+ "<h4 style='margin: 0 0 10px 0; color: #7b1fa2;'>Q4 2024 - Launch</h4>"
					+ "<p style='margin: 0; font-size: 14px;'>⏳ Production deployment<br>⏳ User training<br>⏳ Go-live support</p></div></div></div>"
					+ "<div style='background: #fff; border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px; margin-bottom: 25px;'>"
					+ "<h3 style='color: #00796b; margin-top: 0;'>🎯 Strategic Objectives</h3>"
					+ "<table style='width: 100%; border-collapse: collapse;'>"
					+ "<tr style='background: #f5f5f5;'><th style='padding: 12px; text-align: left; border: 1px solid #ddd;'>Objective</th><th style='padding: 12px; text-align: left; border: 1px solid #ddd;'>Target Date</th><th style='padding: 12px; text-align: left; border: 1px solid #ddd;'>Owner</th><th style='padding: 12px; text-align: left; border: 1px solid #ddd;'>Status</th></tr>"
					+ "<tr><td style='padding: 10px; border: 1px solid #ddd;'>🚀 Launch MVP Version</td><td style='padding: 10px; border: 1px solid #ddd;'>June 2024</td><td style='padding: 10px; border: 1px solid #ddd;'>Project Team</td><td style='padding: 10px; border: 1px solid #ddd;'><span style='background: #e8f5e8; color: #2e7d32; padding: 4px 8px; border-radius: 12px; font-size: 12px;'>On Track</span></td></tr>"
					+ "<tr><td style='padding: 10px; border: 1px solid #ddd;'>📈 Achieve 95% Uptime</td><td style='padding: 10px; border: 1px solid #ddd;'>September 2024</td><td style='padding: 10px; border: 1px solid #ddd;'>Operations</td><td style='padding: 10px; border: 1px solid #ddd;'><span style='background: #fff3e0; color: #f57c00; padding: 4px 8px; border-radius: 12px; font-size: 12px;'>Planning</span></td></tr>"
					+ "<tr><td style='padding: 10px; border: 1px solid #ddd;'>👥 Train 100+ Users</td><td style='padding: 10px; border: 1px solid #ddd;'>October 2024</td><td style='padding: 10px; border: 1px solid #ddd;'>Training Team</td><td style='padding: 10px; border: 1px solid #ddd;'><span style='background: #e3f2fd; color: #1976d2; padding: 4px 8px; border-radius: 12px; font-size: 12px;'>Scheduled</span></td></tr>"
					+ "</table></div>" + "<div style='background: #e8f5e8; padding: 20px; border-radius: 8px; border-left: 4px solid #4caf50;'>"
					+ "<h3 style='color: #2e7d32; margin-top: 0;'>📊 Success Metrics</h3>"
					+ "<div style='display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px;'>"
					+ "<div style='text-align: center;'><div style='font-size: 28px; font-weight: bold; color: #2e7d32;'>65%</div><div style='font-size: 14px; color: #555;'>Overall Progress</div></div>"
					+ "<div style='text-align: center;'><div style='font-size: 28px; font-weight: bold; color: #2e7d32;'>12</div><div style='font-size: 14px; color: #555;'>Team Members</div></div>"
					+ "<div style='text-align: center;'><div style='font-size: 28px; font-weight: bold; color: #2e7d32;'>8</div><div style='font-size: 14px; color: #555;'>Milestones Completed</div></div>"
					+ "<div style='text-align: center;'><div style='font-size: 28px; font-weight: bold; color: #2e7d32;'>4</div><div style='font-size: 14px; color: #555;'>Months Remaining</div></div></div></div></div>");
			pageEntityService.save(projectRoadmap);
			// Page 5: Quality Standards (Process documentation)
			final CPageEntity qualityStandards =
					new CPageEntity("Quality Standards", "Quality Standards", "quality-standards-" + project.getId(), project);
			qualityStandards.setDescription("Comprehensive quality standards, testing procedures, and compliance guidelines");
			qualityStandards.setTitle("Project.Quality.Standards");
			qualityStandards.setMenuOrder("5.5");
			qualityStandards.setIcon("vaadin:clipboard-check");
			qualityStandards.setContent("<div style='max-width: 1200px; padding: 20px;'>"
					+ "<h2 style='color: #c62828; border-bottom: 2px solid #c62828; padding-bottom: 10px;'>🏆 Quality Standards</h2>"
					+ "<p style='margin-bottom: 30px;'>Comprehensive quality assurance framework ensuring excellence in all deliverables.</p>"
					+ "<div style='background: #ffebee; padding: 20px; border-radius: 8px; margin-bottom: 25px; border-left: 4px solid #e53935;'>"
					+ "<h3 style='color: #c62828; margin-top: 0;'>🎯 Quality Objectives</h3>" + "<ul style='margin: 10px 0; padding-left: 20px;'>"
					+ "<li style='margin: 8px 0;'>✅ Maintain 99.5% system reliability</li>"
					+ "<li style='margin: 8px 0;'>✅ Achieve zero critical security vulnerabilities</li>"
					+ "<li style='margin: 8px 0;'>✅ Ensure 95% code coverage in unit tests</li>"
					+ "<li style='margin: 8px 0;'>✅ Maintain sub-2 second response times</li>"
					+ "<li style='margin: 8px 0;'>✅ Follow accessibility standards (WCAG 2.1)</li></ul></div>"
					+ "<div style='display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; margin-bottom: 25px;'>"
					+ "<div style='background: #fff; border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px;'>"
					+ "<h4 style='color: #1976d2; margin-top: 0; display: flex; align-items: center;'><span style='margin-right: 8px;'>🧪</span>Testing Standards</h4>"
					+ "<ul style='margin: 0; padding-left: 20px; font-size: 14px;'>"
					+ "<li style='margin: 6px 0;'>Unit Tests: 95% coverage minimum</li>"
					+ "<li style='margin: 6px 0;'>Integration Tests: All critical paths</li>"
					+ "<li style='margin: 6px 0;'>E2E Tests: Core user journeys</li>"
					+ "<li style='margin: 6px 0;'>Performance Tests: Load & stress</li>"
					+ "<li style='margin: 6px 0;'>Security Tests: Vulnerability scans</li></ul></div>"
					+ "<div style='background: #fff; border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px;'>"
					+ "<h4 style='color: #388e3c; margin-top: 0; display: flex; align-items: center;'><span style='margin-right: 8px;'>📋</span>Code Standards</h4>"
					+ "<ul style='margin: 0; padding-left: 20px; font-size: 14px;'>"
					+ "<li style='margin: 6px 0;'>Follow Derbent coding conventions</li>"
					+ "<li style='margin: 6px 0;'>Mandatory code reviews (2+ reviewers)</li>"
					+ "<li style='margin: 6px 0;'>SonarQube quality gates</li>" + "<li style='margin: 6px 0;'>Zero critical/high issues</li>"
					+ "<li style='margin: 6px 0;'>Complete JavaDoc documentation</li></ul></div>"
					+ "<div style='background: #fff; border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px;'>"
					+ "<h4 style='color: #f57c00; margin-top: 0; display: flex; align-items: center;'><span style='margin-right: 8px;'>🔒</span>Security Standards</h4>"
					+ "<ul style='margin: 0; padding-left: 20px; font-size: 14px;'>" + "<li style='margin: 6px 0;'>OWASP Top 10 compliance</li>"
					+ "<li style='margin: 6px 0;'>Regular dependency updates</li>" + "<li style='margin: 6px 0;'>Secure coding practices</li>"
					+ "<li style='margin: 6px 0;'>Authentication & authorization</li>"
					+ "<li style='margin: 6px 0;'>Data encryption at rest/transit</li></ul></div>"
					+ "<div style='background: #fff; border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px;'>"
					+ "<h4 style='color: #7b1fa2; margin-top: 0; display: flex; align-items: center;'><span style='margin-right: 8px;'>📊</span>Performance Standards</h4>"
					+ "<ul style='margin: 0; padding-left: 20px; font-size: 14px;'>" + "<li style='margin: 6px 0;'>Page load: < 2 seconds</li>"
					+ "<li style='margin: 6px 0;'>API response: < 500ms</li>" + "<li style='margin: 6px 0;'>Database queries: Optimized</li>"
					+ "<li style='margin: 6px 0;'>Memory usage: Monitored</li>"
					+ "<li style='margin: 6px 0;'>Lighthouse score: 90+</li></ul></div></div>"
					+ "<div style='background: #e8f5e8; padding: 20px; border-radius: 8px; border-left: 4px solid #4caf50;'>"
					+ "<h3 style='color: #2e7d32; margin-top: 0;'>📈 Quality Metrics Dashboard</h3>"
					+ "<div style='display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 15px; text-align: center;'>"
					+ "<div><div style='font-size: 24px; font-weight: bold; color: #2e7d32;'>97.2%</div><div style='font-size: 12px; color: #555;'>Code Coverage</div></div>"
					+ "<div><div style='font-size: 24px; font-weight: bold; color: #2e7d32;'>0</div><div style='font-size: 12px; color: #555;'>Critical Issues</div></div>"
					+ "<div><div style='font-size: 24px; font-weight: bold; color: #2e7d32;'>1.8s</div><div style='font-size: 12px; color: #555;'>Avg Response</div></div>"
					+ "<div><div style='font-size: 24px; font-weight: bold; color: #2e7d32;'>99.8%</div><div style='font-size: 12px; color: #555;'>Uptime</div></div>"
					+ "<div><div style='font-size: 24px; font-weight: bold; color: #2e7d32;'>A+</div><div style='font-size: 12px; color: #555;'>Security Grade</div></div></div></div></div>");
			pageEntityService.save(qualityStandards);
			// Page 6: Communication Hub (Stakeholder communication)
			final CPageEntity communicationHub =
					new CPageEntity("Communication Hub", "Communication Hub", "communication-hub-" + project.getId(), project);
			communicationHub.setDescription("Central communication center for stakeholder updates, announcements, and project news");
			communicationHub.setTitle("Project.Communication.Hub");
			communicationHub.setMenuOrder("5.6");
			communicationHub.setIcon("vaadin:building");
			communicationHub.setContent("<div style='max-width: 1200px; padding: 20px;'>"
					+ "<h2 style='color: #455a64; border-bottom: 2px solid #455a64; padding-bottom: 10px;'>📢 Communication Hub</h2>"
					+ "<p style='margin-bottom: 30px;'>Stay informed with the latest project updates, announcements, and stakeholder communications.</p>"
					+ "<div style='display: grid; grid-template-columns: 2fr 1fr; gap: 25px; margin-bottom: 25px;'>"
					+ "<div style='background: #fff; border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px;'>"
					+ "<h3 style='color: #455a64; margin-top: 0; margin-bottom: 20px;'>📰 Latest Updates</h3>"
					+ "<div style='border-bottom: 1px solid #eee; padding-bottom: 15px; margin-bottom: 15px;'>"
					+ "<div style='display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;'>"
					+ "<h4 style='margin: 0; color: #1976d2;'>Sprint 3 Successfully Completed</h4>"
					+ "<span style='background: #e3f2fd; color: #1976d2; padding: 4px 8px; border-radius: 12px; font-size: 12px;'>Today</span></div>"
					+ "<p style='margin: 8px 0; font-size: 14px; color: #666;'>The development team has successfully completed all Sprint 3 objectives ahead of schedule. Key deliverables include API optimization and new dashboard features.</p></div>"
					+ "<div style='border-bottom: 1px solid #eee; padding-bottom: 15px; margin-bottom: 15px;'>"
					+ "<div style='display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;'>"
					+ "<h4 style='margin: 0; color: #388e3c;'>Security Audit Passed</h4>"
					+ "<span style='background: #e8f5e8; color: #388e3c; padding: 4px 8px; border-radius: 12px; font-size: 12px;'>2 days ago</span></div>"
					+ "<p style='margin: 8px 0; font-size: 14px; color: #666;'>External security audit completed with excellent results. Zero critical vulnerabilities found, system meets all security standards.</p></div>"
					+ "<div style='border-bottom: 1px solid #eee; padding-bottom: 15px; margin-bottom: 15px;'>"
					+ "<div style='display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;'>"
					+ "<h4 style='margin: 0; color: #f57c00;'>User Training Schedule Released</h4>"
					+ "<span style='background: #fff3e0; color: #f57c00; padding: 4px 8px; border-radius: 12px; font-size: 12px;'>1 week ago</span></div>"
					+ "<p style='margin: 8px 0; font-size: 14px; color: #666;'>Training schedule for end users has been finalized. Sessions will begin next month with both online and in-person options available.</p></div></div>"
					+ "<div style='background: #fff; border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px;'>"
					+ "<h3 style='color: #455a64; margin-top: 0; margin-bottom: 20px;'>📅 Upcoming Events</h3>"
					+ "<div style='margin-bottom: 15px; padding: 12px; background: #f5f5f5; border-radius: 6px; border-left: 4px solid #2196f3;'>"
					+ "<div style='font-weight: bold; color: #1976d2; margin-bottom: 4px;'>Sprint Review</div>"
					+ "<div style='font-size: 14px; color: #666;'>March 15, 2024<br>2:00 PM - Conference Room A</div></div>"
					+ "<div style='margin-bottom: 15px; padding: 12px; background: #f5f5f5; border-radius: 6px; border-left: 4px solid #4caf50;'>"
					+ "<div style='font-weight: bold; color: #388e3c; margin-bottom: 4px;'>Stakeholder Demo</div>"
					+ "<div style='font-size: 14px; color: #666;'>March 20, 2024<br>10:00 AM - Main Auditorium</div></div>"
					+ "<div style='margin-bottom: 15px; padding: 12px; background: #f5f5f5; border-radius: 6px; border-left: 4px solid #ff9800;'>"
					+ "<div style='font-weight: bold; color: #f57c00; margin-bottom: 4px;'>Go-Live Planning</div>"
					+ "<div style='font-size: 14px; color: #666;'>March 25, 2024<br>9:00 AM - Virtual Meeting</div></div></div></div>"
					+ "<div style='background: #f5f5f5; padding: 20px; border-radius: 8px; margin-bottom: 25px;'>"
					+ "<h3 style='color: #455a64; margin-top: 0;'>📞 Key Contacts</h3>"
					+ "<div style='display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 15px;'>"
					+ "<div style='background: #fff; padding: 15px; border-radius: 6px; border: 1px solid #e0e0e0;'>"
					+ "<h4 style='margin: 0 0 10px 0; color: #1976d2;'>Project Office</h4>"
					+ "<p style='margin: 0; font-size: 14px;'>📧 project@derbent.tech<br>📱 +90-462-751-1000<br>⏰ Mon-Fri, 9:00-17:00</p></div>"
					+ "<div style='background: #fff; padding: 15px; border-radius: 6px; border: 1px solid #e0e0e0;'>"
					+ "<h4 style='margin: 0 0 10px 0; color: #388e3c;'>Technical Support</h4>"
					+ "<p style='margin: 0; font-size: 14px;'>📧 support@derbent.tech<br>📱 +90-462-751-1100<br>⏰ 24/7 Emergency</p></div>"
					+ "<div style='background: #fff; padding: 15px; border-radius: 6px; border: 1px solid #e0e0e0;'>"
					+ "<h4 style='margin: 0 0 10px 0; color: #f57c00;'>Training Team</h4>"
					+ "<p style='margin: 0; font-size: 14px;'>📧 training@derbent.tech<br>📱 +90-462-751-1200<br>⏰ Mon-Fri, 8:00-18:00</p></div></div></div></div>");
			pageEntityService.save(communicationHub);
			LOGGER.info("Successfully created {} comprehensive sample page entities for project: {}", 6, project.getName());
		} catch (final Exception e) {
			LOGGER.error("Error creating sample page entities for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize page entities for project: " + project.getName(), e);
		}
	}

	/** Initializes sample projects with different scopes and characteristics. */
	private void initializeProjects() {
		try {
			// Create projects with comprehensive descriptions
			createProjectWithDescription("Digital Transformation Initiative",
					"A comprehensive digital transformation project aimed at modernizing business processes and improving operational efficiency across all departments.");
			createProjectWithDescription("Product Development Phase 2",
					"Second phase of new product development focusing on advanced features, user experience improvements, and market expansion strategies.");
			createProjectWithDescription("Infrastructure Modernization",
					"Complete infrastructure overhaul including server upgrades, network optimization, security enhancements, and cloud migration initiatives.");
			createProjectWithDescription("Customer Experience Enhancement",
					"Strategic initiative to improve customer journey, implement feedback systems, and enhance service quality across all touchpoints.");
		} catch (final Exception e) {
			LOGGER.error("Error creating sample projects", e);
			throw new RuntimeException("Failed to initialize projects", e);
		}
	}

	/** Initializes sample risks with different severity levels and mitigation strategies. */
	private void initializeRisks() {
		try {
			createHighPriorityTechnicalRisk();
			createMediumPriorityBudgetRisk();
			createLowPriorityResourceRisk();
			createCriticalSecurityRisk();
			createLowPriorityScheduleRisk();
			LOGGER.info("Successfully created 5 sample risks");
		} catch (final Exception e) {
			LOGGER.error("Error creating sample risks", e);
			throw new RuntimeException("Failed to initialize risks", e);
		}
	}

	/** Initializes comprehensive user data with different roles and companies. */
	private void initializeUsers() {
		LOGGER.info("initializeUsers called - creating 5+ sample users with different roles");
		try {
			createAdminUser();
			createProjectManagerUser();
			createTeamMemberUsers();
		} catch (final Exception e) {
			LOGGER.error("Error creating sample users", e);
			throw new RuntimeException("Failed to initialize users", e);
		}
	}

	/** Initializes user types for role-based access control. */
	private void initializeUserTypes() {
		try {
			// Get all projects
			final String[] projectNames = {
					"Digital Transformation Initiative", "Product Development Phase 2", "Infrastructure Modernization",
					"Customer Experience Enhancement"
			};
			// Define user types to create for each project
			final String[][] userTypes = {
					{
							"Employee"
					}, {
							"Manager"
					}, {
							"Executive"
					}, {
							"Contractor"
					}
			};
			// Create user types for each project
			for (final String projectName : projectNames) {
				final CProject project = projectService.findByName(projectName).orElseThrow();
				for (final String[] typeData : userTypes) {
					userTypeService.createEntity(typeData[0], project);
				}
			}
			LOGGER.info("Successfully created user types for all projects");
		} catch (final Exception e) {
			LOGGER.error("Error creating user types", e);
			throw new RuntimeException("Failed to initialize user types", e);
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
			if (Files.exists(filePath)) {
				// LOGGER.debug("Loading profile picture from file path: {}", filePath);
				return Files.readAllBytes(filePath);
			}
			// Fallback: Load from classpath resources
			final var resource = getClass().getClassLoader().getResourceAsStream("profile-pictures/" + filename);
			if (resource != null) {
				// LOGGER.debug("Loading profile picture from classpath: {}", filename);
				return resource.readAllBytes();
			}
			LOGGER.warn("Profile picture file not found: {}", filename);
			return null;
		} catch (final Exception e) {
			LOGGER.error("Error loading profile picture: {}", filename, e);
			return null;
		}
	}

	public void loadSampleData() {
		LOGGER.info("loadSampleData called - initializing sample data for core entities");
		try {
			// Initialize data in proper dependency order
			initializeCompanies();
			initializeProjects();
			initializeMeetingStatuses(); // Initialize status entities first
			initializeDecisionStatuses();
			initializeActivityStatuses();
			initializeOrderStatuses();
			initializeCommentPriorities(); // Initialize comment priorities
			initializeUserTypes(); // Now can use projects
			initializeMeetingTypes();
			initializeDecisionTypes();
			initializeOrderTypes();
			initializeUsers();
			initializeRisks();
			final List<CProject> projects = projectService.list(Pageable.unpaged()).getContent();
			projects.forEach(project -> {
				try {
					initializeActivityTypes(project);
					initializeActivities(project);
					initializeMeetings(project);
					initializeScreenWithFields(project);
					createSampleCurrencies(project);
					initializePageEntities(project);
					initializeGridEntity(project);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			createSampleDecisions();
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
}
