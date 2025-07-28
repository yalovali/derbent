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
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
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
	private static final String PROFILE_PICTURE_USER =
		"data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjQiIGhlaWdodD0iMjQiIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTEyIDJDMTMuMSAyIDE0IDIuOSAxNCA0QzE0IDUuMSAxMy4xIDYgMTIgNkMxMC45IDYgMTAgNS4xIDEwIDRDMTAgMi45IDEwLjkgMiAxMiAyWk0yMSA5VjIySDNWOUwxMiA2TDIxIDlaIiBmaWxsPSIjMzMzIi8+Cjwvc3ZnPgo=";

	// Service dependencies - injected via constructor
	private final CProjectService projectService;

	private final CUserService userService;

	private final CActivityService activityService;

	private final CUserTypeService userTypeService;

	private final CActivityTypeService activityTypeService;

	private final CCompanyService companyService;

	private final CCommentService commentService;

	private final CMeetingService meetingService;

	public CSampleDataInitializer(final CProjectService projectService,
		final CUserService userService, final CActivityService activityService,
		final CUserTypeService userTypeService,
		final CActivityTypeService activityTypeService,
		final CCompanyService companyService, final CCommentService commentService,
		final CMeetingService meetingService) {
		LOGGER
			.info("CSampleDataInitializer constructor called with service dependencies");
		this.projectService = projectService;
		this.userService = userService;
		this.activityService = activityService;
		this.userTypeService = userTypeService;
		this.activityTypeService = activityTypeService;
		this.companyService = companyService;
		this.commentService = commentService;
		this.meetingService = meetingService;
	}

	/**
	 * Creates system administrator user.
	 */
	private void createAdminUser() {
		LOGGER.info("createAdminUser called - creating system administrator");
		final CUser admin = userService.createLoginUser("admin", STANDARD_PASSWORD,
			"Administrator", "admin@system.com", "ADMIN,USER");
		
		// Set user profile using auxiliary method
		final byte[] profilePictureBytes = "profile-picture".getBytes();
		userService.setUserProfile(admin, "System", "+1-555-1001", profilePictureBytes);
		
		// Set user role using auxiliary method
		userService.setUserRole(admin, CUserRole.ADMIN, "ADMIN,USER");
		
		// Set company association using auxiliary method
		final CCompany company = findCompanyByName("TechNova Solutions");
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
		final CUser manager = findUserByLogin("jsmith");
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
		LOGGER.info("createConsultingCompany called - creating Strategic Advisors Ltd");
		final CCompany consulting = new CCompany("Strategic Advisors Ltd",
			"Management consulting and strategic planning services");
		consulting.setAddress("789 Business Center, New York, NY 10001");
		consulting.setPhone("+1-555-0303");
		consulting.setEmail("hello@strategicadvisors.com");
		consulting.setWebsite("https://www.strategicadvisors.com");
		consulting.setTaxNumber("TAX-456789123");
		consulting.setEnabled(true);
		companyService.save(consulting);
		LOGGER.info("Consulting company created successfully");
	}

	/**
	 * Creates healthcare company.
	 */
	private void createHealthcareCompany() {
		LOGGER.info("createHealthcareCompany called - creating MedTech Innovations");
		final CCompany healthcare = new CCompany("MedTech Innovations",
			"Advanced medical technology and healthcare solutions");
		healthcare.setAddress("321 Medical Plaza, Boston, MA 02101");
		healthcare.setPhone("+1-555-0404");
		healthcare.setEmail("contact@medtechinnovations.com");
		healthcare.setWebsite("https://www.medtechinnovations.com");
		healthcare.setTaxNumber("TAX-789123456");
		healthcare.setEnabled(true);
		companyService.save(healthcare);
		LOGGER.info("Healthcare company created successfully");
	}

	/**
	 * Creates manufacturing company.
	 */
	private void createManufacturingCompany() {
		LOGGER.info(
			"createManufacturingCompany called - creating Industrial Dynamics Corp");
		final CCompany manufacturing = new CCompany("Industrial Dynamics Corp",
			"Leading manufacturer of precision engineering components");
		manufacturing.setAddress("456 Manufacturing Blvd, Detroit, MI 48201");
		manufacturing.setPhone("+1-555-0202");
		manufacturing.setEmail("info@industrialdynamics.com");
		manufacturing.setWebsite("https://www.industrialdynamics.com");
		manufacturing.setTaxNumber("TAX-987654321");
		manufacturing.setEnabled(true);
		companyService.save(manufacturing);
		LOGGER.info("Manufacturing company created successfully");
	}

	/**
	 * Creates project manager user.
	 */
	private void createProjectManagerUser() {
		LOGGER.info("createProjectManagerUser called - creating project manager");
		final CUser manager = userService.createLoginUser("jsmith", STANDARD_PASSWORD,
			"John", "john.smith@technova.com", "MANAGER,USER");
		
		// Set user profile using auxiliary method
		final byte[] profilePictureBytes = "profile-picture".getBytes();
		userService.setUserProfile(manager, "Smith", "+1-555-1002", profilePictureBytes);
		
		// Set user role using auxiliary method
		userService.setUserRole(manager, CUserRole.PROJECT_MANAGER, "MANAGER,USER");
		
		// Set company association using auxiliary method
		final CCompany company = findCompanyByName("TechNova Solutions");
		userService.setCompanyAssociation(manager, company);
		
		LOGGER.info("Project manager user created successfully");
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
		final CUser teamMember2 = findUserByLogin("bwilson");
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
		LOGGER.info("createTeamMemberAlice called - creating Alice Davis");
		final CUser analyst = userService.createLoginUser("adavis", STANDARD_PASSWORD,
			"Alice", "alice.davis@medtechinnovations.com", "USER");
		
		// Set user profile using auxiliary method
		final byte[] profilePictureBytes = "profile-picture".getBytes();
		userService.setUserProfile(analyst, "Davis", "+1-555-1005", profilePictureBytes);
		
		// Set user role using auxiliary method
		userService.setUserRole(analyst, CUserRole.TEAM_MEMBER, "USER");
		
		// Set company association using auxiliary method
		final CCompany company = findCompanyByName("MedTech Innovations");
		userService.setCompanyAssociation(analyst, company);
		
		LOGGER.info("Team member Alice Davis created successfully");
	}

	/**
	 * Creates team member Bob Wilson.
	 */
	private void createTeamMemberBob() {
		LOGGER.info("createTeamMemberBob called - creating Bob Wilson");
		final CUser developer = userService.createLoginUser("bwilson", STANDARD_PASSWORD,
			"Bob", "bob.wilson@strategicadvisors.com", "USER");
		
		// Set user profile using auxiliary method
		final byte[] profilePictureBytes = "profile-picture".getBytes();
		userService.setUserProfile(developer, "Wilson", "+1-555-1004", profilePictureBytes);
		
		// Set user role using auxiliary method
		userService.setUserRole(developer, CUserRole.TEAM_MEMBER, "USER");
		
		// Set company association using auxiliary method
		final CCompany company = findCompanyByName("Strategic Advisors Ltd");
		userService.setCompanyAssociation(developer, company);
		
		LOGGER.info("Team member Bob Wilson created successfully");
	}

	/**
	 * Creates team member Mary Johnson.
	 */
	private void createTeamMemberMary() {
		LOGGER.info("createTeamMemberMary called - creating Mary Johnson");
		final CUser teamMember = userService.createLoginUser("mjohnson",
			STANDARD_PASSWORD, "Mary", "mary.johnson@industrialdynamics.com", "USER");
		
		// Set user profile using auxiliary method
		final byte[] profilePictureBytes = "profile-picture".getBytes();
		userService.setUserProfile(teamMember, "Johnson", "+1-555-1003", profilePictureBytes);
		
		// Set user role using auxiliary method
		userService.setUserRole(teamMember, CUserRole.TEAM_MEMBER, "USER");
		
		// Set company association using auxiliary method
		final CCompany company = findCompanyByName("Industrial Dynamics Corp");
		userService.setCompanyAssociation(teamMember, company);
		
		LOGGER.info("Team member Mary Johnson created successfully");
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
		LOGGER.info("createTechCompany called - creating TechNova Solutions");
		final CCompany techStartup = new CCompany("TechNova Solutions",
			"Innovative technology solutions for digital transformation");
		techStartup.setAddress("123 Innovation Drive, Silicon Valley, CA 94025");
		techStartup.setPhone("+1-555-0101");
		techStartup.setEmail("contact@technova.com");
		techStartup.setWebsite("https://www.technova.com");
		techStartup.setTaxNumber("TAX-123456789");
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
		final CUser analyst = findUserByLogin("adavis");
		final CUser manager = findUserByLogin("jsmith");
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
		final CUser teamMember1 = findUserByLogin("mjohnson");
		final CUser manager = findUserByLogin("jsmith");
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
		final CUser responsible = findUserByLogin("jsmith");
		meetingService.setMeetingContent(meeting, 
			"Weekly status update on project progress, blockers discussion, and next steps planning",
			null, responsible);

		// Set participants using auxiliary method
		final Set<CUser> participants = new HashSet<>();
		participants.add(findUserByLogin("admin"));
		participants.add(findUserByLogin("jsmith"));
		participants.add(findUserByLogin("bwilson"));
		participants.add(findUserByLogin("mjohnson"));
		meetingService.setParticipants(meeting, participants);

		// Set meeting status using auxiliary method
		meetingService.setMeetingStatus(meeting, null, 
			"Meeting agenda prepared, participants notified", 
			"Project management system");

		LOGGER.info("Sample project meeting created successfully using auxiliary methods");
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
	 * Initializes sample meetings with participants and content.
	 */
	private void initializeMeetings() {
		LOGGER.info("initializeMeetings called - creating sample meetings");

		try {
			createSampleProjectMeeting();
			LOGGER.info("Successfully created sample meetings");
		} catch (final Exception e) {
			LOGGER.error("Error creating sample meetings", e);
			throw new RuntimeException("Failed to initialize meetings", e);
		}
	}

	/**
	 * Initializes activity types for categorizing different kinds of work.
	 */
	private void initializeActivityTypes() {
		LOGGER.info(
			"initializeActivityTypes called - creating activity type classifications");

		try {
			activityTypeService.createEntity("Development",
				"Software development and coding tasks");
			activityTypeService.createEntity("Testing",
				"Quality assurance and testing activities");
			activityTypeService.createEntity("Design",
				"UI/UX design and system architecture");
			activityTypeService.createEntity("Documentation",
				"Technical writing and documentation");
			activityTypeService.createEntity("Research",
				"Research and analysis activities");
			// LOGGER.info("Successfully created 5 activity types");
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
		LOGGER.info("initializeUserTypes called - creating user type classifications");

		try {
			userTypeService.createEntity("Employee");
			userTypeService.createEntity("Manager");
			userTypeService.createEntity("Executive");
			userTypeService.createEntity("Contractor");
			// LOGGER.info("Successfully created 4 user types");
		} catch (final Exception e) {
			LOGGER.error("Error creating user types", e);
			throw new RuntimeException("Failed to initialize user types", e);
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
			initializeUserTypes();
			initializeUsers();
			initializeProjects();
			initializeActivityTypes();
			initializeActivities();
			initializeMeetings();
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
}