package tech.derbent.config;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.activities.service.CActivityTypeService;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
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

	/**
	 * Constructor with required service dependencies.
	 * @param projectService      the project service
	 * @param userService         the user service
	 * @param activityService     the activity service
	 * @param userTypeService     the user type service
	 * @param activityTypeService the activity type service
	 * @param companyService      the company service
	 */
	public CSampleDataInitializer(final CProjectService projectService,
		final CUserService userService, final CActivityService activityService,
		final CUserTypeService userTypeService,
		final CActivityTypeService activityTypeService,
		final CCompanyService companyService) {
		LOGGER
			.info("CSampleDataInitializer constructor called with service dependencies");
		this.projectService = projectService;
		this.userService = userService;
		this.activityService = activityService;
		this.userTypeService = userTypeService;
		this.activityTypeService = activityTypeService;
		this.companyService = companyService;
	}

	/**
	 * Helper method to add company to a user safely.
	 * @param user        the user to add company to
	 * @param companyName the name of the company to find and assign
	 */
	private void addCompany(final CUser user, final String companyName) {
		LOGGER.info("addCompany called for user: {} with company: {}",
			user != null ? user.getName() : "null", companyName);

		if (user == null) {
			LOGGER.warn("User is null, cannot add company");
			return;
		}

		if ((companyName == null) || companyName.isEmpty()) {
			LOGGER.warn("Company name is null or empty for user: {}", user.getName());
			return;
		}

		try {
			final CCompany company = findCompanyByName(companyName);

			if (company != null) {
				user.setCompany(company);
				LOGGER.info("Company {} added to user {}", companyName, user.getName());
			}
			else {
				LOGGER.warn("Company {} not found for user {}", companyName,
					user.getName());
			}
		} catch (final Exception e) {
			LOGGER.error("Error adding company to user: " + user.getName(), e);
		}
	}

	/**
	 * Creates system administrator user.
	 */
	private void createAdminUser() {
		LOGGER.info("createAdminUser called - creating system administrator");
		final CUser admin = userService.createLoginUser("admin", STANDARD_PASSWORD,
			"Administrator", "admin@system.com", "ADMIN,USER");
		admin.setLastname("System");
		admin.setPhone("+1-555-1001");
		admin.setUserRole(CUserRole.ADMIN);
		addCompany(admin, "TechNova Solutions");
		loadProfilePicture(admin, PROFILE_PICTURE_USER);
		userService.save(admin);
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
		final CActivity backendDev = new CActivity("Backend API Development", project);
		backendDev.setDescription(
			"Develop REST API endpoints for user management and authentication");
		final CUser manager = findUserByLogin("jsmith");

		if (manager != null) {
			backendDev.setAssignedTo(manager);
		}
		final CUser admin = findUserByLogin("admin");

		if (admin != null) {
			backendDev.setCreatedBy(admin);
		}
		backendDev.setEstimatedHours(new BigDecimal("40.00"));
		backendDev.setActualHours(new BigDecimal("35.50"));
		backendDev.setRemainingHours(new BigDecimal("4.50"));
		backendDev.setStartDate(LocalDate.now().minusDays(10));
		backendDev.setDueDate(LocalDate.now().plusDays(5));
		activityService.save(backendDev);
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
		manager.setLastname("Smith");
		manager.setPhone("+1-555-1002");
		manager.setUserRole(CUserRole.PROJECT_MANAGER);
		addCompany(manager, "TechNova Solutions");
		loadProfilePicture(manager, PROFILE_PICTURE_USER);
		userService.save(manager);
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
		final CActivity archDesign = new CActivity("System Architecture Design", project);
		archDesign.setDescription(
			"Design scalable system architecture for infrastructure modernization");
		final CUser teamMember2 = findUserByLogin("bwilson");

		if (teamMember2 != null) {
			archDesign.setAssignedTo(teamMember2);
		}
		final CUser admin = findUserByLogin("admin");

		if (admin != null) {
			archDesign.setCreatedBy(admin);
		}
		archDesign.setEstimatedHours(new BigDecimal("60.00"));
		archDesign.setActualHours(new BigDecimal("45.00"));
		archDesign.setRemainingHours(new BigDecimal("15.00"));
		archDesign.setStartDate(LocalDate.now().minusDays(15));
		archDesign.setDueDate(LocalDate.now().plusDays(10));
		activityService.save(archDesign);
		LOGGER.info("System architecture activity created successfully");
	}

	/**
	 * Creates team member Alice Davis.
	 */
	private void createTeamMemberAlice() {
		LOGGER.info("createTeamMemberAlice called - creating Alice Davis");
		final CUser analyst = userService.createLoginUser("adavis", STANDARD_PASSWORD,
			"Alice", "alice.davis@medtechinnovations.com", "USER");
		analyst.setLastname("Davis");
		analyst.setPhone("+1-555-1005");
		analyst.setUserRole(CUserRole.TEAM_MEMBER);
		addCompany(analyst, "MedTech Innovations");
		loadProfilePicture(analyst, PROFILE_PICTURE_USER);
		userService.save(analyst);
		LOGGER.info("Team member Alice Davis created successfully");
	}

	/**
	 * Creates team member Bob Wilson.
	 */
	private void createTeamMemberBob() {
		LOGGER.info("createTeamMemberBob called - creating Bob Wilson");
		final CUser developer = userService.createLoginUser("bwilson", STANDARD_PASSWORD,
			"Bob", "bob.wilson@strategicadvisors.com", "USER");
		developer.setLastname("Wilson");
		developer.setPhone("+1-555-1004");
		developer.setUserRole(CUserRole.TEAM_MEMBER);
		addCompany(developer, "Strategic Advisors Ltd");
		loadProfilePicture(developer, PROFILE_PICTURE_USER);
		userService.save(developer);
		LOGGER.info("Team member Bob Wilson created successfully");
	}

	/**
	 * Creates team member Mary Johnson.
	 */
	private void createTeamMemberMary() {
		LOGGER.info("createTeamMemberMary called - creating Mary Johnson");
		final CUser teamMember = userService.createLoginUser("mjohnson",
			STANDARD_PASSWORD, "Mary", "mary.johnson@industrialdynamics.com", "USER");
		teamMember.setLastname("Johnson");
		teamMember.setPhone("+1-555-1003");
		teamMember.setUserRole(CUserRole.TEAM_MEMBER);
		addCompany(teamMember, "Industrial Dynamics Corp");
		loadProfilePicture(teamMember, PROFILE_PICTURE_USER);
		userService.save(teamMember);
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
		final CActivity techDoc =
			new CActivity("Technical Documentation Update", project);
		techDoc.setDescription(
			"Update and enhance technical documentation for customer experience features");
		final CUser analyst = findUserByLogin("adavis");

		if (analyst != null) {
			techDoc.setAssignedTo(analyst);
		}
		final CUser manager = findUserByLogin("jsmith");

		if (manager != null) {
			techDoc.setCreatedBy(manager);
		}
		techDoc.setEstimatedHours(new BigDecimal("16.00"));
		techDoc.setActualHours(new BigDecimal("16.00"));
		techDoc.setRemainingHours(new BigDecimal("0.00"));
		techDoc.setStartDate(LocalDate.now().minusDays(5));
		techDoc.setDueDate(LocalDate.now().minusDays(1));
		activityService.save(techDoc);
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
		final CActivity uiTesting = new CActivity("User Interface Testing", project);
		uiTesting.setDescription(
			"Comprehensive testing of user interface components and workflows");
		final CUser teamMember1 = findUserByLogin("mjohnson");

		if (teamMember1 != null) {
			uiTesting.setAssignedTo(teamMember1);
		}
		final CUser manager = findUserByLogin("jsmith");

		if (manager != null) {
			uiTesting.setCreatedBy(manager);
		}
		uiTesting.setEstimatedHours(new BigDecimal("24.00"));
		uiTesting.setActualHours(new BigDecimal("20.00"));
		uiTesting.setRemainingHours(new BigDecimal("4.00"));
		uiTesting.setStartDate(LocalDate.now().minusDays(7));
		uiTesting.setDueDate(LocalDate.now().plusDays(3));
		activityService.save(uiTesting);
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
		LOGGER.info("findUserByLogin called with login: {}", login);

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
			LOGGER.info("Successfully created 4 comprehensive activity samples");
		} catch (final Exception e) {
			LOGGER.error("Error creating sample activities", e);
			throw new RuntimeException("Failed to initialize activities", e);
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
			LOGGER.info("Successfully created 5 activity types");
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
			LOGGER.info("Successfully created 4 sample companies");
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
			LOGGER.info("Successfully created 4 sample projects");
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
			LOGGER.info(
				"Successfully created 5 sample users with different roles and companies");
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
			LOGGER.info("Successfully created 4 user types");
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
		LOGGER.info("isDatabaseEmpty called - checking user count");
		final long userCount = userService.count();
		LOGGER.info("User count in database: {}", userCount);
		return userCount == 0;
	}

	/**
	 * Loads profile picture for a user.
	 * @param user               the user to set profile picture for
	 * @param profilePictureData the base64 encoded profile picture data
	 */
	private void loadProfilePicture(final CUser user, final String profilePictureData) {
		LOGGER.info("loadProfilePicture called for user: {} with picture data length: {}",
			user.getName(), profilePictureData != null ? profilePictureData.length() : 0);

		if ((profilePictureData == null) || profilePictureData.isEmpty()) {
			LOGGER.warn("Profile picture data is null or empty for user: {}",
				user.getName());
			return;
		}

		try {
			// For now, just set a simple byte array to avoid database column size issues
			// In production this would be proper image handling
			final byte[] pictureBytes = "profile-picture".getBytes();
			user.setProfilePictureData(pictureBytes);
			LOGGER.info("Profile picture loaded successfully for user: {}",
				user.getName());
		} catch (final Exception e) {
			LOGGER.error("Error loading profile picture for user: " + user.getName(), e);
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
			LOGGER.info("Database is empty, initializing sample data for core entities");
			// Initialize data in proper dependency order
			initializeCompanies();
			initializeUserTypes();
			initializeUsers();
			initializeProjects();
			initializeActivityTypes();
			initializeActivities();
			LOGGER.info("Sample data initialization completed successfully");
		} catch (final Exception e) {
			LOGGER.error("Error during sample data initialization", e);
			throw e;
		}
	}
}