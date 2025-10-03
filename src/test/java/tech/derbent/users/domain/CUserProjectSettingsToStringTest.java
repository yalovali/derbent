package tech.derbent.users.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

/** Integration test to validate that the toString() method of CUserProjectSettings and CUserCompanySetting handles lazy-loaded fields properly and
 * does not throw LazyInitializationException when called outside a transaction. This test ensures: 1. toString() can be safely called outside
 * transactions 2. CSpringAuxillaries.safeGetId() and safeToString() utility methods work correctly 3. The toString() output is meaningful even with
 * unloaded proxies */
@SpringBootTest (classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false"
})
@DisplayName ("🔍 CUserProjectSettings toString() Lazy Loading Test")
public class CUserProjectSettingsToStringTest {

	@Autowired
	private CUserService userService;
	@Autowired
	private CProjectService projectService;
	@Autowired
	private CUserProjectRoleService roleService;
	@Autowired
	private CUserProjectSettingsService userProjectSettingsService;
	private CUserProjectSettings savedSettings;

	@BeforeEach
	@Transactional
	public void setUp() {
		// Create test user
		CUser testUser = new CUser();
		testUser.setName("ToString Test User");
		testUser.setLogin("tostring_test_user_" + System.currentTimeMillis());
		testUser.setEmail("tostring@test.com");
		testUser = userService.save(testUser);
		// Create test project
		CProject testProject = new CProject();
		testProject.setName("ToString Test Project");
		testProject.setDescription("Project for toString validation");
		testProject = projectService.save(testProject);
		// Create test role
		CUserProjectRole testRole = new CUserProjectRole("ToString Test Role", testProject);
		testRole.setIsAdmin(false);
		testRole.setIsUser(true);
		testRole = roleService.save(testRole);
		// Create settings
		CUserProjectSettings settings = new CUserProjectSettings();
		settings.setUser(testUser);
		settings.setProject(testProject);
		settings.setRole(testRole);
		settings.setPermission("READ_WRITE");
		savedSettings = userProjectSettingsService.save(settings);
	}

	@Test
	@DisplayName ("Test toString() does not throw LazyInitializationException outside transaction")
	public void testToStringOutsideTransaction() {
		// Retrieve settings by ID (returns outside of transaction context)
		CUserProjectSettings retrievedSettings = userProjectSettingsService.getById(savedSettings.getId()).orElseThrow();
		// Call toString() outside transaction - should NOT throw LazyInitializationException
		// Before fix: Would throw "org.hibernate.LazyInitializationException: Could not initialize proxy"
		// After fix: Uses CSpringAuxillaries.safeGetId() and safeToString() to safely handle lazy proxies
		String toStringResult = assertDoesNotThrow(() -> retrievedSettings.toString(), "toString() should not throw LazyInitializationException");
		// Verify the result is meaningful
		assertNotNull(toStringResult, "toString() result should not be null");
		assertTrue(toStringResult.contains("UserProjectSettings"), "toString() should contain class name");
		assertTrue(toStringResult.contains("user id="), "toString() should contain user id field");
		assertTrue(toStringResult.contains("project id="), "toString() should contain project id field");
		assertTrue(toStringResult.contains("role="), "toString() should contain role field");
		assertTrue(toStringResult.contains("permission="), "toString() should contain permission field");
	}

	@Test
	@Transactional
	@DisplayName ("Test toString() works correctly within transaction")
	public void testToStringWithinTransaction() {
		// Retrieve settings within transaction
		CUserProjectSettings retrievedSettings = userProjectSettingsService.getById(savedSettings.getId()).orElseThrow();
		// Call toString() within transaction
		String toStringResult = assertDoesNotThrow(() -> retrievedSettings.toString(), "toString() should not throw exception within transaction");
		// Verify the result contains expected information
		assertNotNull(toStringResult, "toString() result should not be null");
		assertTrue(toStringResult.contains("UserProjectSettings"), "toString() should contain class name");
		assertTrue(toStringResult.contains("permission=READ_WRITE"), "toString() should contain permission value");
	}

	@Test
	@DisplayName ("Test toString() with null relationships")
	public void testToStringWithNullRelationships() {
		// Create settings with null relationships
		CUserProjectSettings settings = new CUserProjectSettings();
		settings.setPermission("TEST");
		// Call toString() - should handle nulls gracefully
		String toStringResult = assertDoesNotThrow(() -> settings.toString(), "toString() should handle null relationships");
		assertNotNull(toStringResult, "toString() result should not be null");
		assertTrue(toStringResult.contains("UserProjectSettings"), "toString() should contain class name");
	}

	@Test
	@DisplayName ("Test toString() called during logging doesn't cause exceptions")
	public void testToStringDuringLogging() {
		// Simulate a common use case where toString() might be called during logging
		CUserProjectSettings retrievedSettings = userProjectSettingsService.getById(savedSettings.getId()).orElseThrow();
		// This simulates what happens in logging statements like: LOGGER.debug("Processing: {}", settings);
		assertDoesNotThrow(() -> {
			String message = "Processing settings: " + retrievedSettings.toString();
			assertNotNull(message, "Log message should be created without exception");
		}, "toString() in logging context should not throw exception");
	}
}
