package tech.derbent.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.activities.service.CActivityTypeService;
import tech.derbent.comments.service.CCommentService;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.decisions.service.CDecisionTypeService;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.meetings.service.CMeetingTypeService;
import tech.derbent.orders.service.COrderTypeService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.risks.domain.ERiskSeverity;
import tech.derbent.risks.service.CRiskService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserRole;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;

/**
 * CSampleDataInitializer - Enhanced sample data initializer following coding guidelines.
 * Layer: Configuration (MVC) This component runs after the application starts and creates
 * comprehensive sample data for core entities if the database is empty. Follows the
 * coding guidelines: - Class name starts with "C" as per coding standards - Creates at
 * least 4 sample items per core entity type - Uses proper service layer for entity
 * creation - Implements proper error handling and logging - Uses standard test123
 * password for all users Core entities initialized: - CCompany (4 companies with full
 * details) - CUser (5+ users with different roles and companies) - CProject (4 projects
 * with different scopes) - CActivity (4+ comprehensive activities) - Supporting type
 * entities as available
 */
@Component
public class CSampleDataInitializer implements ApplicationRunner {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CSampleDataInitializer.class);

	// Standard password for all users as per coding guidelines
	private static final String STANDARD_PASSWORD = "test123";

	// Sample user profile pictures (base64 encoded SVG icons)
	@SuppressWarnings ("unused")
	private static final String PROFILE_PICTURE_USER =
		"data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjQiIGhlaWdodD0iMjQiIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTEyIDJDMTMuMSAyIDE0IDIuOSAxNCA0QzE0IDUuMSAxMy4xIDYgMTIgNkMxMC45IDYgMTAgNS4xIDEwIDRDMTAgMi45IDEwLjkgMiAxMiAyWk0yMSA5VjIySDNWOUwxMiA2TDIxIDlaIiBmaWxsPSIjMzMzIi8+Cjwvc3ZnPgo=";

	// Service dependencies - injected via constructor
	private final CProjectService projectService;

	private final CUserService userService;

	private final CActivityService activityService;

	private final CUserTypeService userTypeService;

	private final CActivityTypeService activityTypeService;

	private final CMeetingTypeService meetingTypeService;

	private final CDecisionTypeService decisionTypeService;

	private final COrderTypeService orderTypeService;

	private final CCompanyService companyService;

	private final CCommentService commentService;

	private final CMeetingService meetingService;
	
	private final CRiskService riskService;

	public CSampleDataInitializer(final CProjectService projectService,
		final CUserService userService, final CActivityService activityService,
		final CUserTypeService userTypeService,
		final CActivityTypeService activityTypeService,
		final CMeetingTypeService meetingTypeService,
		final CDecisionTypeService decisionTypeService,
		final COrderTypeService orderTypeService,
		final CCompanyService companyService, final CCommentService commentService,
		final CMeetingService meetingService, final CRiskService riskService) {
		LOGGER
			.info("CSampleDataInitializer constructor called with service dependencies");
		this.projectService = projectService;
		this.userService = userService;
		this.activityService = activityService;
		this.userTypeService = userTypeService;
		this.activityTypeService = activityTypeService;
		this.meetingTypeService = meetingTypeService;
		this.decisionTypeService = decisionTypeService;
		this.orderTypeService = orderTypeService;
		this.companyService = companyService;
		this.commentService = commentService;
		this.meetingService = meetingService;
		this.riskService = riskService;
	}

	/**
	 * Creates system administrator user.
	 */
	private void createAdminUser() {
		LOGGER.info("createAdminUser called - creating system administrator");
		final CUser admin = userService.createLoginUser("admin", STANDARD_PASSWORD,
			"Ahmet", "admin@of.gov.tr", "ADMIN,USER");

		// Set user profile using auxiliary method
		final byte[] profilePictureBytes = "profile-picture".getBytes();
		userService.setUserProfile(admin, "Yılmaz", "+90-462-751-1001", profilePictureBytes);

		// Set user role using auxiliary method
		userService.setUserRole(admin, CUserRole.ADMIN, "ADMIN,USER");

		// Set company association using auxiliary method
		final CCompany company = findCompanyByName("Of Teknoloji Çözümleri");
		userService.setCompanyAssociation(admin, company);

		LOGGER.info("Administrator user created successfully");
	}

	/**
	 * Creates backend development activity.
	 */
	private void createBackendDevActivity() {
		LOGGER.info(
			"createBackendDevActivity called - creating backend API development activity");
		final CProject project = findProjectByName("Digital Transformation Initiative");

		if (project == null) {
			LOGGER.warn(
				"Project 'Digital Transformation Initiative' not found, skipping backend activity");
			return;
		}

		// Create the activity using new auxiliary methods
		final CActivity backendDev = new CActivity("Backend API Development", project);

		// Set activity type and description using auxiliary method
		activityService.setActivityType(backendDev, null,
			"Develop REST API endpoints for user management and authentication");

		// Set assigned users using auxiliary method
		final CUser manager = findUserByLogin("mkaradeniz");
		final CUser admin = findUserByLogin("admin");
		activityService.setAssignedUsers(backendDev, manager, admin);

		// Set time tracking using auxiliary method
		activityService.setTimeTracking(backendDev,
			new BigDecimal("40.00"), new BigDecimal("35.50"), new BigDecimal("4.50"));

		// Set date information using auxiliary method
		activityService.setDateInfo(backendDev,
			LocalDate.now().minusDays(10), LocalDate.now().plusDays(5), null);

		// Create comments
		commentService.createComment("Initial backend API development started",
			backendDev, admin);
		commentService.createComment(
			"API endpoints for user registration and login implemented", backendDev,
			admin);
		LOGGER.info("Backend development activity created successfully");
	}

	/**
	 * Creates consulting company.
	 */
	private void createConsultingCompany() {
		LOGGER.info("createConsultingCompany called - creating Of Stratejik Danışmanlık");
		final CCompany consulting = new CCompany("Of Stratejik Danışmanlık",
			"Yönetim danışmanlığı ve stratejik planlama hizmetleri");
		consulting.setAddress("Merkez Mahallesi, Gülbahar Sokağı No:7, Of/Trabzon");
		consulting.setPhone("+90-462-751-0303");
		consulting.setEmail("merhaba@ofdanismanlik.com.tr");
		consulting.setWebsite("https://www.ofdanismanlik.com.tr");
		consulting.setTaxNumber("TR-456789123");
		consulting.setEnabled(true);
		companyService.save(consulting);
		LOGGER.info("Consulting company created successfully");
	}

	/**
	 * Creates healthcare company.
	 */
	private void createHealthcareCompany() {
		LOGGER.info("createHealthcareCompany called - creating Of Sağlık Teknolojileri");
		final CCompany healthcare = new CCompany("Of Sağlık Teknolojileri",
			"İleri tıp teknolojisi ve sağlık çözümleri");
		healthcare.setAddress("Yeni Mahalle, Sağlık Sokağı No:21, Of/Trabzon");
		healthcare.setPhone("+90-462-751-0404");
		healthcare.setEmail("iletisim@ofsaglik.com.tr");
		healthcare.setWebsite("https://www.ofsaglik.com.tr");
		healthcare.setTaxNumber("TR-789123456");
		healthcare.setEnabled(true);
		companyService.save(healthcare);
		LOGGER.info("Healthcare company created successfully");
	}

	/**
	 * Creates manufacturing company.
	 */
	private void createManufacturingCompany() {
		LOGGER.info(
			"createManufacturingCompany called - creating Of Endüstri Dinamikleri");
		final CCompany manufacturing = new CCompany("Of Endüstri Dinamikleri",
			"Hassas mühendislik bileşenlerinde lider üretici");
		manufacturing.setAddress("Sanayi Mahallesi, İstiklal Caddesi No:42, Of/Trabzon");
		manufacturing.setPhone("+90-462-751-0202");
		manufacturing.setEmail("bilgi@ofendüstri.com.tr");
		manufacturing.setWebsite("https://www.ofendüstri.com.tr");
		manufacturing.setTaxNumber("TR-987654321");
		manufacturing.setEnabled(true);
		companyService.save(manufacturing);
		LOGGER.info("Manufacturing company created successfully");
	}

	/**
	 * Creates project manager user.
	 */
	private void createProjectManagerUser() {
		LOGGER.info("createProjectManagerUser called - creating project manager");
		final CUser manager = userService.createLoginUser("mkaradeniz", STANDARD_PASSWORD,
			"Mehmet Emin", "mehmet.karadeniz@ofteknoloji.com.tr", "MANAGER,USER");

		// Set user profile using auxiliary method
		final byte[] profilePictureBytes = "profile-picture".getBytes();
		userService.setUserProfile(manager, "Karadeniz", "+90-462-751-1002", profilePictureBytes);

		// Set user role using auxiliary method
		userService.setUserRole(manager, CUserRole.PROJECT_MANAGER, "MANAGER,USER");

		// Set company association using auxiliary method
		final CCompany company = findCompanyByName("Of Teknoloji Çözümleri");
		userService.setCompanyAssociation(manager, company);

		LOGGER.info("Project manager user created successfully");
	}

	/**
	 * Creates sample project meeting using auxiliary service methods.
	 * Demonstrates the use of auxiliary meeting service methods.
	 */
	private void createSampleProjectMeeting() {
		LOGGER.info("createSampleProjectMeeting called - creating sample project meeting");
		final CProject project = findProjectByName("Digital Transformation Initiative");

		if (project == null) {
			LOGGER.warn("Project 'Digital Transformation Initiative' not found, skipping meeting creation");
			return;
		}

		// Create the meeting using new auxiliary methods
		final CMeeting meeting = new CMeeting("Weekly Project Status Meeting", project);

		// Set meeting details using auxiliary method
		meetingService.setMeetingDetails(meeting, null,
			LocalDateTime.now().plusDays(1).withHour(14).withMinute(0),
			LocalDateTime.now().plusDays(1).withHour(15).withMinute(0),
			"Conference Room A");

		// Set meeting content using auxiliary method
		final CUser responsible = findUserByLogin("mkaradeniz");
		meetingService.setMeetingContent(meeting,
			"Weekly status update on project progress, blockers discussion, and next steps planning",
			null, responsible);

		// Set participants using auxiliary method
		final Set<CUser> participants = new HashSet<>();
		participants.add(findUserByLogin("admin"));
		participants.add(findUserByLogin("mkaradeniz"));
		participants.add(findUserByLogin("bozkan"));
		participants.add(findUserByLogin("msahin"));
		meetingService.setParticipants(meeting, participants);

		// Set meeting status using auxiliary method
		meetingService.setMeetingStatus(meeting, null,
			"Meeting agenda prepared, participants notified",
			"Project management system");

		LOGGER.info("Sample project meeting created successfully using auxiliary methods");
	}

	/**
	 * Creates system architecture design activity.
	 */
	private void createSystemArchitectureActivity() {
		LOGGER.info(
			"createSystemArchitectureActivity called - creating system architecture design activity");
		final CProject project = findProjectByName("Infrastructure Modernization");

		if (project == null) {
			LOGGER.warn(
				"Project 'Infrastructure Modernization' not found, skipping architecture activity");
			return;
		}

		// Create the activity using new auxiliary methods
		final CActivity archDesign = new CActivity("System Architecture Design", project);

		// Set activity type and description using auxiliary method
		activityService.setActivityType(archDesign, null,
			"Design scalable system architecture for infrastructure modernization");

		// Set assigned users using auxiliary method
		final CUser teamMember2 = findUserByLogin("bozkan");
		final CUser admin = findUserByLogin("admin");
		activityService.setAssignedUsers(archDesign, teamMember2, admin);

		// Set time tracking using auxiliary method
		activityService.setTimeTracking(archDesign,
			new BigDecimal("60.00"), new BigDecimal("45.00"), new BigDecimal("15.00"));

		// Set date information using auxiliary method
		activityService.setDateInfo(archDesign,
			LocalDate.now().minusDays(15), LocalDate.now().plusDays(10), null);

		// Create comments
		commentService.createComment("Initial system architecture design phase started",
			archDesign, admin);
		commentService.createComment(
			"Completed high-level architecture diagrams and component definitions",
			archDesign, admin);
		commentService.createComment(
			"Reviewed architecture with team and incorporated feedback", archDesign,
			teamMember2);
		LOGGER.info("System architecture activity created successfully");
	}

	/**
	 * Creates team member Alice Davis.
	 */
	private void createTeamMemberAlice() {
		LOGGER.info("createTeamMemberAlice called - creating Ayşe Demir from Günebakan village");
		final CUser analyst = userService.createLoginUser("ademir", STANDARD_PASSWORD,
			"Ayşe", "ayse.demir@ofsaglik.com.tr", "USER");

		// Set user profile using auxiliary method
		final byte[] profilePictureBytes = "profile-picture".getBytes();
		userService.setUserProfile(analyst, "Demir", "+90-462-751-1005", profilePictureBytes);

		// Set user role using auxiliary method
		userService.setUserRole(analyst, CUserRole.TEAM_MEMBER, "USER");

		// Set company association using auxiliary method
		final CCompany company = findCompanyByName("Of Sağlık Teknolojileri");
		userService.setCompanyAssociation(analyst, company);

		LOGGER.info("Team member Ayşe Demir created successfully");
	}

	/**
	 * Creates team member Bob Wilson.
	 */
	private void createTeamMemberBob() {
		LOGGER.info("createTeamMemberBob called - creating Burak Özkan from Çamburnu village");
		final CUser developer = userService.createLoginUser("bozkan", STANDARD_PASSWORD,
			"Burak", "burak.ozkan@ofdanismanlik.com.tr", "USER");

		// Set user profile using auxiliary method
		final byte[] profilePictureBytes = "profile-picture".getBytes();
		userService.setUserProfile(developer, "Özkan", "+90-462-751-1004", profilePictureBytes);

		// Set user role using auxiliary method
		userService.setUserRole(developer, CUserRole.TEAM_MEMBER, "USER");

		// Set company association using auxiliary method
		final CCompany company = findCompanyByName("Of Stratejik Danışmanlık");
		userService.setCompanyAssociation(developer, company);

		LOGGER.info("Team member Burak Özkan created successfully");
	}

	/**
	 * Creates team member Mary Johnson.
	 */
	private void createTeamMemberMary() {
		LOGGER.info("createTeamMemberMary called - creating Merve Şahin from Ballıköy village");
		final CUser teamMember = userService.createLoginUser("msahin",
			STANDARD_PASSWORD, "Merve", "merve.sahin@ofendüstri.com.tr", "USER");

		// Set user profile using auxiliary method
		final byte[] profilePictureBytes = "profile-picture".getBytes();
		userService.setUserProfile(teamMember, "Şahin", "+90-462-751-1003", profilePictureBytes);

		// Set user role using auxiliary method
		userService.setUserRole(teamMember, CUserRole.TEAM_MEMBER, "USER");

		// Set company association using auxiliary method
		final CCompany company = findCompanyByName("Of Endüstri Dinamikleri");
		userService.setCompanyAssociation(teamMember, company);

		LOGGER.info("Team member Merve Şahin created successfully");
	}

	/**
	 * Creates team member users across different companies.
	 */
	private void createTeamMemberUsers() {
		LOGGER.info("createTeamMemberUsers called - creating team member users");
		createTeamMemberMary();
		createTeamMemberBob();
		createTeamMemberAlice();
		LOGGER.info("Team member users created successfully");
	}

	/**
	 * Creates technology startup company.
	 */
	private void createTechCompany() {
		LOGGER.info("createTechCompany called - creating Of Teknoloji Çözümleri");
		final CCompany techStartup = new CCompany("Of Teknoloji Çözümleri",
			"Dijital dönüşüm için yenilikçi teknoloji çözümleri");
		techStartup.setAddress("Cumhuriyet Mahallesi, Atatürk Caddesi No:15, Of/Trabzon");
		techStartup.setPhone("+90-462-751-0101");
		techStartup.setEmail("iletisim@ofteknoloji.com.tr");
		techStartup.setWebsite("https://www.ofteknoloji.com.tr");
		techStartup.setTaxNumber("TR-123456789");
		techStartup.setEnabled(true);
		companyService.save(techStartup);
		LOGGER.info("Technology company created successfully");
	}

	/**
	 * Creates technical documentation activity.
	 */
	private void createTechnicalDocumentationActivity() {
		LOGGER.info(
			"createTechnicalDocumentationActivity called - creating technical documentation activity");
		final CProject project = findProjectByName("Customer Experience Enhancement");

		if (project == null) {
			LOGGER.warn(
				"Project 'Customer Experience Enhancement' not found, skipping documentation activity");
			return;
		}

		// Create the activity using new auxiliary methods
		final CActivity techDoc = new CActivity("Technical Documentation Update", project);

		// Set activity type and description using auxiliary method
		activityService.setActivityType(techDoc, null,
			"Update and enhance technical documentation for customer experience features");

		// Set assigned users using auxiliary method
		final CUser analyst = findUserByLogin("ademir");
		final CUser manager = findUserByLogin("mkaradeniz");
		activityService.setAssignedUsers(techDoc, analyst, manager);

		// Set time tracking using auxiliary method (completed activity)
		activityService.setTimeTracking(techDoc,
			new BigDecimal("16.00"), new BigDecimal("16.00"), new BigDecimal("0.00"));

		// Set date information using auxiliary method (completed activity)
		activityService.setDateInfo(techDoc,
			LocalDate.now().minusDays(5), LocalDate.now().minusDays(1), LocalDate.now().minusDays(1));

		// Create comments
		commentService.createComment(
			"Initial technical documentation review and updates started", techDoc,
			manager);
		commentService.createComment(
			"Completed updates to user guides and API documentation", techDoc, analyst);
		commentService.createComment(
			"Reviewed documentation with team and incorporated feedback", techDoc,
			analyst);
		LOGGER.info("Technical documentation activity created successfully");
	}

	/**
	 * Creates UI testing activity.
	 */
	private void createUITestingActivity() {
		LOGGER.info(
			"createUITestingActivity called - creating user interface testing activity");
		final CProject project = findProjectByName("Product Development Phase 2");

		if (project == null) {
			LOGGER.warn(
				"Project 'Product Development Phase 2' not found, skipping UI testing activity");
			return;
		}

		// Create the activity using new auxiliary methods
		final CActivity uiTesting = new CActivity("User Interface Testing", project);

		// Set activity type and description using auxiliary method
		activityService.setActivityType(uiTesting, null,
			"Comprehensive testing of user interface components and workflows");

		// Set assigned users using auxiliary method
		final CUser teamMember1 = findUserByLogin("msahin");
		final CUser manager = findUserByLogin("mkaradeniz");
		activityService.setAssignedUsers(uiTesting, teamMember1, manager);

		// Set time tracking using auxiliary method
		activityService.setTimeTracking(uiTesting,
			new BigDecimal("24.00"), new BigDecimal("20.00"), new BigDecimal("4.00"));

		// Set date information using auxiliary method
		activityService.setDateInfo(uiTesting,
			LocalDate.now().minusDays(7), LocalDate.now().plusDays(3), null);

		LOGGER.info("UI testing activity created successfully");
	}

	/**
	 * Helper method to find company by name.
	 * @param name the company name to search for
	 * @return the CCompany entity or null if not found
	 */
	private CCompany findCompanyByName(final String name) {
		LOGGER.info("findCompanyByName called with name: {}", name);

		try {
			return companyService.findByName(name).orElse(null);
		} catch (final Exception e) {
			LOGGER.warn("Could not find company with name: {}", name);
			return null;
		}
	}

	/**
	 * Helper method to find project by name.
	 * @param name the project name to search for
	 * @return the CProject entity or null if not found
	 */
	private CProject findProjectByName(final String name) {
		LOGGER.info("findProjectByName called with name: {}", name);

		try {
			return projectService.findByName(name).orElse(null);
		} catch (final Exception e) {
			LOGGER.warn("Could not find project with name: {}", name);
			return null;
		}
	}

	/**
	 * Helper method to find user by login.
	 * @param login the user login to search for
	 * @return the CUser entity or null if not found
	 */
	private CUser findUserByLogin(final String login) {
		// LOGGER.info("findUserByLogin called with login: {}", login);

		try {
			return userService.findByLogin(login);
		} catch (final Exception e) {
			LOGGER.warn("Could not find user with login: {}", login);
			return null;
		}
	}

	/**
	 * Initializes comprehensive activity data with available fields populated.
	 */
	private void initializeActivities() {
		LOGGER.info(
			"initializeActivities called - creating comprehensive activity samples");

		try {
			createBackendDevActivity();
			createUITestingActivity();
			createSystemArchitectureActivity();
			createTechnicalDocumentationActivity();
			// LOGGER.info("Successfully created 4 comprehensive activity samples");
		} catch (final Exception e) {
			LOGGER.error("Error creating sample activities", e);
			throw new RuntimeException("Failed to initialize activities", e);
		}
	}

	/**
	 * Initializes activity types for categorizing different kinds of work.
	 * Creates types for all projects to ensure project-specific categorization.
	 */
	private void initializeActivityTypes() {
		LOGGER.info(
			"initializeActivityTypes called - creating activity type classifications for all projects");

		try {
			// Get all projects
			final String[] projectNames = {
				"Digital Transformation Initiative",
				"Product Development Phase 2", 
				"Infrastructure Modernization",
				"Customer Experience Enhancement"
			};

			// Define activity types to create for each project
			final String[][] activityTypes = {
				{"Development", "Software development and coding tasks"},
				{"Testing", "Quality assurance and testing activities"},
				{"Design", "UI/UX design and system architecture"},
				{"Documentation", "Technical writing and documentation"},
				{"Research", "Research and analysis activities"}
			};

			// Create activity types for each project
			for (final String projectName : projectNames) {
				final CProject project = findProjectByName(projectName);
				if (project == null) {
					LOGGER.warn("Project '{}' not found, skipping activity type creation", projectName);
					continue;
				}

				LOGGER.info("Creating activity types for project: {}", projectName);
				for (final String[] typeData : activityTypes) {
					activityTypeService.createEntity(typeData[0], typeData[1], project);
				}
			}
			
			LOGGER.info("Successfully created activity types for all projects");
		} catch (final Exception e) {
			LOGGER.error("Error creating activity types", e);
			throw new RuntimeException("Failed to initialize activity types", e);
		}
	}

	/**
	 * Initializes sample companies with full details.
	 */
	private void initializeCompanies() {
		LOGGER.info("initializeCompanies called - creating 4 sample companies");

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

	/**
	 * Initializes sample meetings with participants and content.
	 */
	private void initializeMeetings() {
		LOGGER.info("initializeMeetings called - creating sample meetings");

		try {
			createSampleProjectMeeting();
			createSampleStandupMeeting();
			createSamplePlanningMeeting();
			createSampleReviewMeeting();
			createSampleRetrospectiveMeeting();
			LOGGER.info("Successfully created 5 sample meetings");
		} catch (final Exception e) {
			LOGGER.error("Error creating sample meetings", e);
			throw new RuntimeException("Failed to initialize meetings", e);
		}
	}

	/**
	 * Initializes sample risks with different severity levels and mitigation strategies.
	 */
	private void initializeRisks() {
		LOGGER.info("initializeRisks called - creating sample risks");

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

	/**
	 * Initializes sample projects with different scopes and characteristics.
	 */
	private void initializeProjects() {
		LOGGER.info("initializeProjects called - creating 4 sample projects");

		try {
			projectService.createEntity("Digital Transformation Initiative");
			projectService.createEntity("Product Development Phase 2");
			projectService.createEntity("Infrastructure Modernization");
			projectService.createEntity("Customer Experience Enhancement");
			// LOGGER.info("Successfully created 4 sample projects");
		} catch (final Exception e) {
			LOGGER.error("Error creating sample projects", e);
			throw new RuntimeException("Failed to initialize projects", e);
		}
	}

	/**
	 * Initializes comprehensive user data with different roles and companies.
	 */
	private void initializeUsers() {
		LOGGER.info(
			"initializeUsers called - creating 5+ sample users with different roles");

		try {
			createAdminUser();
			createProjectManagerUser();
			createTeamMemberUsers();
		} catch (final Exception e) {
			LOGGER.error("Error creating sample users", e);
			throw new RuntimeException("Failed to initialize users", e);
		}
	}

	/**
	 * Initializes user types for role-based access control.
	 */
	private void initializeUserTypes() {
		LOGGER.info("initializeUserTypes called - creating user type classifications for all projects");

		try {
			// Get all projects
			final String[] projectNames = {
				"Digital Transformation Initiative",
				"Product Development Phase 2", 
				"Infrastructure Modernization",
				"Customer Experience Enhancement"
			};

			// Define user types to create for each project
			final String[][] userTypes = {
				{"Employee", "Regular employee"},
				{"Manager", "Team manager or lead"},
				{"Executive", "Executive or senior management"},
				{"Contractor", "External contractor or consultant"}
			};

			// Create user types for each project
			for (final String projectName : projectNames) {
				final CProject project = findProjectByName(projectName);
				if (project == null) {
					LOGGER.warn("Project '{}' not found, skipping user type creation", projectName);
					continue;
				}

				LOGGER.info("Creating user types for project: {}", projectName);
				for (final String[] typeData : userTypes) {
					userTypeService.createEntity(typeData[0], typeData[1], project);
				}
			}
			
			LOGGER.info("Successfully created user types for all projects");
		} catch (final Exception e) {
			LOGGER.error("Error creating user types", e);
			throw new RuntimeException("Failed to initialize user types", e);
		}
	}

	/**
	 * Initializes meeting types for categorizing different kinds of meetings.
	 * Creates types for all projects to ensure project-specific categorization.
	 */
	private void initializeMeetingTypes() {
		LOGGER.info("initializeMeetingTypes called - creating meeting type classifications for all projects");

		try {
			// Get all projects
			final String[] projectNames = {
				"Digital Transformation Initiative",
				"Product Development Phase 2", 
				"Infrastructure Modernization",
				"Customer Experience Enhancement"
			};

			// Define meeting types to create for each project
			final String[][] meetingTypes = {
				{"Standup", "Daily standup meeting"},
				{"Planning", "Sprint or project planning meeting"},
				{"Review", "Review and feedback meeting"},
				{"Retrospective", "Team retrospective meeting"},
				{"One-on-One", "Individual meeting with team member"}
			};

			// Create meeting types for each project
			for (final String projectName : projectNames) {
				final CProject project = findProjectByName(projectName);
				if (project == null) {
					LOGGER.warn("Project '{}' not found, skipping meeting type creation", projectName);
					continue;
				}

				LOGGER.info("Creating meeting types for project: {}", projectName);
				for (final String[] typeData : meetingTypes) {
					meetingTypeService.createEntity(typeData[0], typeData[1], project);
				}
			}
			
			LOGGER.info("Successfully created meeting types for all projects");
		} catch (final Exception e) {
			LOGGER.error("Error creating meeting types", e);
			throw new RuntimeException("Failed to initialize meeting types", e);
		}
	}

	/**
	 * Initializes decision types for categorizing different kinds of decisions.
	 * Creates types for all projects to ensure project-specific categorization.
	 */
	private void initializeDecisionTypes() {
		LOGGER.info("initializeDecisionTypes called - creating decision type classifications for all projects");

		try {
			// Get all projects
			final String[] projectNames = {
				"Digital Transformation Initiative",
				"Product Development Phase 2", 
				"Infrastructure Modernization",
				"Customer Experience Enhancement"
			};

			// Define decision types to create for each project
			final String[][] decisionTypes = {
				{"Strategic", "High-level strategic decisions"},
				{"Technical", "Technical architecture decisions"},
				{"Financial", "Budget and cost-related decisions"},
				{"Operational", "Day-to-day operational decisions"},
				{"Resource", "Resource allocation decisions"}
			};

			// Create decision types for each project
			for (final String projectName : projectNames) {
				final CProject project = findProjectByName(projectName);
				if (project == null) {
					LOGGER.warn("Project '{}' not found, skipping decision type creation", projectName);
					continue;
				}

				LOGGER.info("Creating decision types for project: {}", projectName);
				for (final String[] typeData : decisionTypes) {
					decisionTypeService.createDecisionType(typeData[0], typeData[1], true, project);
				}
			}
			
			LOGGER.info("Successfully created decision types for all projects");
		} catch (final Exception e) {
			LOGGER.error("Error creating decision types", e);
			throw new RuntimeException("Failed to initialize decision types", e);
		}
	}

	/**
	 * Initializes order types for categorizing different kinds of orders.
	 * Creates types for all projects to ensure project-specific categorization.
	 */
	private void initializeOrderTypes() {
		LOGGER.info("initializeOrderTypes called - creating order type classifications for all projects");

		try {
			// Get all projects
			final String[] projectNames = {
				"Digital Transformation Initiative",
				"Product Development Phase 2", 
				"Infrastructure Modernization",
				"Customer Experience Enhancement"
			};

			// Define order types to create for each project
			final String[][] orderTypes = {
				{"Service Order", "Service-related orders"},
				{"Purchase Order", "Purchase and procurement orders"},
				{"Maintenance Order", "Maintenance and support orders"},
				{"Change Order", "Change request orders"},
				{"Support Order", "Customer support orders"}
			};

			// Create order types for each project
			for (final String projectName : projectNames) {
				final CProject project = findProjectByName(projectName);
				if (project == null) {
					LOGGER.warn("Project '{}' not found, skipping order type creation", projectName);
					continue;
				}

				LOGGER.info("Creating order types for project: {}", projectName);
				for (final String[] typeData : orderTypes) {
					orderTypeService.createEntity(typeData[0], typeData[1], project);
				}
			}
			
			LOGGER.info("Successfully created order types for all projects");
		} catch (final Exception e) {
			LOGGER.error("Error creating order types", e);
			throw new RuntimeException("Failed to initialize order types", e);
		}
	}

	/**
	 * Checks if the database is empty by counting users.
	 * @return true if database is empty, false otherwise
	 */
	private boolean isDatabaseEmpty() {
		/// LOGGER.info("isDatabaseEmpty called - checking user count");
		final long userCount = userService.count();
		LOGGER.info("User count in database: {}", userCount);
		return userCount == 0;
	}

	public void loadSampleData() {
		LOGGER.info("loadSampleData called - initializing sample data for core entities");

		try {
			// Initialize data in proper dependency order
			initializeCompanies();
			initializeProjects(); // Projects must be created before project-aware entities
			initializeUserTypes(); // Now can use projects
			initializeMeetingTypes();
			initializeDecisionTypes();
			initializeOrderTypes();
			initializeUsers();
			initializeActivityTypes();
			initializeActivities();
			initializeMeetings();
			initializeRisks();
			LOGGER.info("Sample data initialization completed successfully");
		} catch (final Exception e) {
			LOGGER.error("Error loading sample data", e);
			throw new RuntimeException("Failed to load sample data", e);
		}
	}

	@Override
	@Transactional
	public void run(final ApplicationArguments args) throws Exception {
		LOGGER.info("CSampleDataInitializer.run called with ApplicationArguments: {}",
			args);

		try {

			// Check if database already has data - if so, skip initialization
			if (!isDatabaseEmpty()) {
				LOGGER.info(
					"Database already contains data, skipping sample data initialization");
				return;
			}
			loadSampleData(); // Load sample data
		} catch (final Exception e) {
			LOGGER.error("Error during sample data initialization", e);
			throw e;
		}
	}

	// Additional meeting creation methods

	/**
	 * Creates sample standup meeting.
	 */
	private void createSampleStandupMeeting() {
		LOGGER.info("createSampleStandupMeeting called - creating daily standup meeting");
		
		final CProject project = findProjectByName("Digital Transformation Initiative");
		if (project == null) {
			LOGGER.warn("Project not found for standup meeting");
			return;
		}
		
		final CMeeting meeting = new CMeeting("Daily Standup - Sprint 3", project);
		meeting.setDescription("Daily progress sync and impediment discussion");
		meeting.setMeetingDate(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0));
		meeting.setEndDate(LocalDateTime.now().plusDays(1).withHour(9).withMinute(30));
		meeting.setLocation("Conference Room A");
		
		// Add participants
		final Set<CUser> participants = new HashSet<>();
		final CUser manager = findUserByLogin("mkaradeniz");
		final CUser dev1 = findUserByLogin("ademir");
		final CUser dev2 = findUserByLogin("msahin");
		if (manager != null) participants.add(manager);
		if (dev1 != null) participants.add(dev1);
		if (dev2 != null) participants.add(dev2);
		meeting.setParticipants(participants);
		
		meetingService.save(meeting);
		LOGGER.info("Sample standup meeting created successfully");
	}

	/**
	 * Creates sample planning meeting.
	 */
	private void createSamplePlanningMeeting() {
		LOGGER.info("createSamplePlanningMeeting called - creating sprint planning meeting");
		
		final CProject project = findProjectByName("Product Development Phase 2");
		if (project == null) {
			LOGGER.warn("Project not found for planning meeting");
			return;
		}
		
		final CMeeting meeting = new CMeeting("Sprint Planning - Q1 2024", project);
		meeting.setDescription("Planning for next sprint with story estimation and task assignment");
		meeting.setMeetingDate(LocalDateTime.now().plusDays(3).withHour(14).withMinute(0));
		meeting.setEndDate(LocalDateTime.now().plusDays(3).withHour(16).withMinute(0));
		meeting.setLocation("Meeting Room B");
		
		// Add participants
		final Set<CUser> participants = new HashSet<>();
		final CUser manager = findUserByLogin("mkaradeniz");
		final CUser analyst = findUserByLogin("ademir");
		if (manager != null) participants.add(manager);
		if (analyst != null) participants.add(analyst);
		meeting.setParticipants(participants);
		
		meetingService.save(meeting);
		LOGGER.info("Sample planning meeting created successfully");
	}

	/**
	 * Creates sample review meeting.
	 */
	private void createSampleReviewMeeting() {
		LOGGER.info("createSampleReviewMeeting called - creating code review meeting");
		
		final CProject project = findProjectByName("Infrastructure Modernization");
		if (project == null) {
			LOGGER.warn("Project not found for review meeting");
			return;
		}
		
		final CMeeting meeting = new CMeeting("Code Review Session", project);
		meeting.setDescription("Review of architectural changes and code quality improvements");
		meeting.setMeetingDate(LocalDateTime.now().minusDays(2).withHour(10).withMinute(0));
		meeting.setEndDate(LocalDateTime.now().minusDays(2).withHour(11).withMinute(30));
		meeting.setLocation("Virtual - Zoom");
		
		// Add participants
		final Set<CUser> participants = new HashSet<>();
		final CUser manager = findUserByLogin("mkaradeniz");
		final CUser dev = findUserByLogin("msahin");
		if (manager != null) participants.add(manager);
		if (dev != null) participants.add(dev);
		meeting.setParticipants(participants);
		
		meetingService.save(meeting);
		LOGGER.info("Sample review meeting created successfully");
	}

	/**
	 * Creates sample retrospective meeting.
	 */
	private void createSampleRetrospectiveMeeting() {
		LOGGER.info("createSampleRetrospectiveMeeting called - creating sprint retrospective");
		
		final CProject project = findProjectByName("Customer Experience Enhancement");
		if (project == null) {
			LOGGER.warn("Project not found for retrospective meeting");
			return;
		}
		
		final CMeeting meeting = new CMeeting("Sprint Retrospective", project);
		meeting.setDescription("Team reflection on what went well, what could be improved, and action items");
		meeting.setMeetingDate(LocalDateTime.now().minusDays(7).withHour(15).withMinute(0));
		meeting.setEndDate(LocalDateTime.now().minusDays(7).withHour(16).withMinute(0));
		meeting.setLocation("Conference Room C");
		
		// Add participants and attendees
		final Set<CUser> participants = new HashSet<>();
		final Set<CUser> attendees = new HashSet<>();
		final CUser manager = findUserByLogin("mkaradeniz");
		final CUser dev1 = findUserByLogin("ademir");
		final CUser dev2 = findUserByLogin("msahin");
		
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
		LOGGER.info("Sample retrospective meeting created successfully");
	}

	// Risk creation methods

	/**
	 * Creates high priority technical risk.
	 */
	private void createHighPriorityTechnicalRisk() {
		LOGGER.info("createHighPriorityTechnicalRisk called - creating technical risk");
		
		final CProject project = findProjectByName("Digital Transformation Initiative");
		if (project == null) {
			LOGGER.warn("Project not found for technical risk");
			return;
		}
		
		final CRisk risk = new CRisk("Legacy System Integration Challenges", ERiskSeverity.HIGH, project);
		risk.setDescription("Integration with legacy systems may cause compatibility issues and performance bottlenecks");
		
		riskService.save(risk);
		LOGGER.info("High priority technical risk created successfully");
	}

	/**
	 * Creates medium priority budget risk.
	 */
	private void createMediumPriorityBudgetRisk() {
		LOGGER.info("createMediumPriorityBudgetRisk called - creating budget risk");
		
		final CProject project = findProjectByName("Product Development Phase 2");
		if (project == null) {
			LOGGER.warn("Project not found for budget risk");
			return;
		}
		
		final CRisk risk = new CRisk("Budget Overrun Due to Scope Creep", ERiskSeverity.MEDIUM, project);
		risk.setDescription("Uncontrolled feature additions may cause budget to exceed allocated resources");
		
		riskService.save(risk);
		LOGGER.info("Medium priority budget risk created successfully");
	}

	/**
	 * Creates low priority resource risk.
	 */
	private void createLowPriorityResourceRisk() {
		LOGGER.info("createLowPriorityResourceRisk called - creating resource risk");
		
		final CProject project = findProjectByName("Infrastructure Modernization");
		if (project == null) {
			LOGGER.warn("Project not found for resource risk");
			return;
		}
		
		final CRisk risk = new CRisk("Team Member Vacation Scheduling Conflicts", ERiskSeverity.LOW, project);
		risk.setDescription("Overlapping vacation schedules may temporarily reduce team capacity");
		
		riskService.save(risk);
		LOGGER.info("Low priority resource risk created successfully");
	}

	/**
	 * Creates critical security risk.
	 */
	private void createCriticalSecurityRisk() {
		LOGGER.info("createCriticalSecurityRisk called - creating security risk");
		
		final CProject project = findProjectByName("Customer Experience Enhancement");
		if (project == null) {
			LOGGER.warn("Project not found for security risk");
			return;
		}
		
		final CRisk risk = new CRisk("Data Privacy Compliance Gaps", ERiskSeverity.CRITICAL, project);
		risk.setDescription("Current implementation may not fully comply with GDPR and data protection regulations");
		
		riskService.save(risk);
		LOGGER.info("Critical security risk created successfully");
	}

	/**
	 * Creates low priority schedule risk.
	 */
	private void createLowPriorityScheduleRisk() {
		LOGGER.info("createLowPriorityScheduleRisk called - creating schedule risk");
		
		final CProject project = findProjectByName("Digital Transformation Initiative");
		if (project == null) {
			LOGGER.warn("Project not found for schedule risk");
			return;
		}
		
		final CRisk risk = new CRisk("Minor Delays in Third-Party Integrations", ERiskSeverity.LOW, project);
		risk.setDescription("External vendor may experience minor delays in API delivery");
		
		riskService.save(risk);
		LOGGER.info("Low priority schedule risk created successfully");
	}
}