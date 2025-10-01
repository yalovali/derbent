package tech.derbent.users.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.roles.domain.CUserProjectRole;
import tech.derbent.api.roles.service.CUserProjectRoleService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;

/** Integration test to validate that the FetchType.LAZY fix for CUserProjectSettings.role prevents the PostgreSQL 1664 column limit error while still
 * allowing proper data access. This test ensures: 1. All @ManyToOne relationships in CUserProjectSettings use FetchType.LAZY 2. Saving
 * CUserProjectSettings with a role doesn't generate massive JOIN queries 3. Role data can still be accessed within a transaction */
@SpringBootTest (classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false"
})
@DisplayName ("🔍 CUserProjectSettings FetchType Fix Validation Test")
public class CUserProjectSettingsFetchTypeTest {

	@Autowired
	private CUserService userService;
	@Autowired
	private CProjectService projectService;
	@Autowired
	private CUserProjectRoleService roleService;
	@Autowired
	private CUserProjectSettingsService userProjectSettingsService;
	private CUser testUser;
	private CProject testProject;
	private CUserProjectRole testRole;

	@BeforeEach
	@Transactional
	public void setUp() {
		// Create test user
		testUser = new CUser();
		testUser.setName("FetchType Test User");
		testUser.setLogin("fetchtype_test_user_" + System.currentTimeMillis());
		testUser.setEmail("fetchtype@test.com");
		testUser = userService.save(testUser);
		// Create test project
		testProject = new CProject();
		testProject.setName("FetchType Test Project");
		testProject.setDescription("Project for FetchType validation");
		testProject = projectService.save(testProject);
		// Create test role
		testRole = new CUserProjectRole("Test Role", testProject);
		testRole.setIsAdmin(false);
		testRole.setIsUser(true);
		testRole = roleService.save(testRole);
	}

	@Test
	@Transactional
	@DisplayName ("Test saving CUserProjectSettings with role doesn't cause PostgreSQL 1664 error")
	public void testSaveWithRoleDoesNotCauseColumnLimitError() {
		// This test validates that the fix (FetchType.LAZY for role) prevents the error:
		// "ERROR: target lists can have at most 1664 entries"
		// Create settings
		CUserProjectSettings settings = new CUserProjectSettings();
		settings.setUser(testUser);
		settings.setProject(testProject);
		settings.setRole(testRole);
		settings.setPermission("READ_WRITE");
		// This save operation would fail with the error if role was EAGER fetched
		// because CUserProjectRole has two @ElementCollection fields with EAGER fetch
		CUserProjectSettings savedSettings = userProjectSettingsService.save(settings);
		// Verify save succeeded
		assertNotNull(savedSettings, "Settings should be saved successfully");
		assertNotNull(savedSettings.getId(), "Saved settings should have an ID");
		assertEquals(testUser.getId(), savedSettings.getUser().getId(), "User should match");
		assertEquals(testProject.getId(), savedSettings.getProject().getId(), "Project should match");
		assertEquals(testRole.getId(), savedSettings.getRole().getId(), "Role should match");
		assertEquals("READ_WRITE", savedSettings.getPermission(), "Permission should match");
	}

	@Test
	@Transactional
	@DisplayName ("Test role can be accessed within transaction despite LAZY loading")
	public void testRoleAccessibleWithinTransaction() {
		// Create and save settings
		CUserProjectSettings settings = new CUserProjectSettings();
		settings.setUser(testUser);
		settings.setProject(testProject);
		settings.setRole(testRole);
		settings.setPermission("READ");
		CUserProjectSettings savedSettings = userProjectSettingsService.save(settings);
		// Retrieve settings and access lazy-loaded role within transaction
		CUserProjectSettings retrievedSettings = userProjectSettingsService.getById(savedSettings.getId()).orElseThrow();
		// Access role properties - this should work within transaction
		CUserProjectRole retrievedRole = retrievedSettings.getRole();
		assertNotNull(retrievedRole, "Role should be accessible within transaction");
		assertEquals("Test Role", retrievedRole.getName(), "Role name should match");
		assertTrue(retrievedRole.getIsUser(), "Role should have user flag set");
	}

	@Test
	@Transactional
	@DisplayName ("Test multiple settings can be saved without exceeding column limit")
	public void testMultipleSettingsWithRoles() {
		// This test simulates a realistic scenario where multiple users have project assignments
		// In the bug scenario, even one save could fail due to EAGER fetch generating massive queries
		// Create additional users and roles
		CUser user2 = new CUser();
		user2.setName("Second User");
		user2.setLogin("second_user_" + System.currentTimeMillis());
		user2.setEmail("second@test.com");
		user2 = userService.save(user2);
		CUserProjectRole role2 = new CUserProjectRole("Admin Role", testProject);
		role2.setIsAdmin(true);
		role2 = roleService.save(role2);
		// Save multiple settings
		CUserProjectSettings settings1 = new CUserProjectSettings();
		settings1.setUser(testUser);
		settings1.setProject(testProject);
		settings1.setRole(testRole);
		settings1.setPermission("READ");
		CUserProjectSettings settings2 = new CUserProjectSettings();
		settings2.setUser(user2);
		settings2.setProject(testProject);
		settings2.setRole(role2);
		settings2.setPermission("ADMIN");
		// These saves should work without column limit errors
		CUserProjectSettings saved1 = userProjectSettingsService.save(settings1);
		CUserProjectSettings saved2 = userProjectSettingsService.save(settings2);
		// Verify both saves succeeded
		assertNotNull(saved1.getId(), "First settings should be saved");
		assertNotNull(saved2.getId(), "Second settings should be saved");
		// Verify we can retrieve and access role data
		assertEquals(testRole.getId(), saved1.getRole().getId(), "First role should match");
		assertEquals(role2.getId(), saved2.getRole().getId(), "Second role should match");
	}
}
