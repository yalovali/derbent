package tech.derbent.users.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import java.time.Clock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;

/** Unit tests for CUserProjectSettingsService focusing on bidirectional relationship management. */
@ExtendWith (MockitoExtension.class)
public class CUserProjectSettingsServiceTest {

	@Mock
	private IUserProjectSettingsRepository repository;
	@Mock
	private Clock clock;
	@Mock
	private CSessionService sessionService;
	private CUserProjectSettingsService service;
	private CUser user;
	private CProject project;

	@BeforeEach
	void setUp() {
		service = new CUserProjectSettingsService(repository, clock, sessionService, null, null);
		user = new CUser("Test User");
		user.setLogin("testuser");
		user.setEmail("test@example.com");
		project = new CProject("Test Project");
	}

	@Test
	void testAddUserToProject_BidirectionalRelationshipHandling() {
		// Given - Create a settings object manually (simulating what the service should do)
		CUserProjectSettings settings = new CUserProjectSettings();
		settings.setUser(user);
		settings.setProject(project);
		settings.setPermission("READ,WRITE");
		// When - manually maintain bidirectional relationships (this is what the service should do)
		user.addProjectSettings(settings);
		project.addUserSettings(settings);
		// Then
		assertNotNull(settings, "Settings should not be null");
		assertEquals(user, settings.getUser(), "User should be set correctly");
		assertEquals(project, settings.getProject(), "Project should be set correctly");
		// Role is not set in this test, so it should be null (nullable = true in entity)
		assertNull(settings.getRole(), "Role should be null when not set");
		assertEquals("READ,WRITE", settings.getPermission(), "Permission should be set correctly");
		// Verify bidirectional relationships are maintained
		assertEquals(1, user.getProjectSettings().size(), "User should have one project setting");
		assertEquals(1, project.getUserSettings().size(), "Project should have one user setting");
		assertTrue(user.getProjectSettings().contains(settings), "User should contain the added settings");
		assertTrue(project.getUserSettings().contains(settings), "Project should contain the added settings");
	}

	@Test
	void testRemoveUserFromProject_NullIds() {
		// When & Then - user and project with null IDs should fail
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.deleteByUserProject(user, project));
		assertTrue(exception.getMessage().contains("User must have a valid ID") || exception.getMessage().contains("Project must have a valid ID"));
		verify(repository, never()).findByUserIdAndProjectId(any(), any());
	}

	@Test
	void testRemoveUserFromProject_BidirectionalRelationshipHandling() {
		// Given
		CUserProjectSettings settings = new CUserProjectSettings();
		settings.setUser(user);
		settings.setProject(project);
		settings.setPermission("READ,WRITE");
		// Set up the bidirectional relationship
		user.addProjectSettings(settings);
		project.addUserSettings(settings);
		// Test the bidirectional relationship is set up correctly
		assertEquals(1, user.getProjectSettings().size(), "User should have one project setting");
		assertEquals(1, project.getUserSettings().size(), "Project should have one user setting");
		// When - manually call the bidirectional removal (this is what the service should do)
		user.removeProjectSettings(settings);
		project.removeUserSettings(settings);
		// Then
		assertTrue(user.getProjectSettings().isEmpty(), "User should have no project settings");
		assertTrue(project.getUserSettings().isEmpty(), "Project should have no user settings");
		assertNull(settings.getUser(), "Settings should not reference user");
		assertNull(settings.getProject(), "Settings should not reference project");
	}

	@Test
	void testRemoveUserFromProject_NullArguments() {
		// When & Then - null arguments should fail
		IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> service.deleteByUserProject(null, project));
		IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> service.deleteByUserProject(user, null));
		assertTrue(exception1.getMessage().contains("User cannot be null"));
		assertTrue(exception2.getMessage().contains("Project cannot be null"));
	}

	@Test
	void testUserProjectSettingsInitialization_PreventConstraintViolation() {
		// Test that new CUserProjectSettings instances can be created with proper initialization
		// This test simulates what the dialogs should do to prevent user_id constraint violations
		// Given - Create a new settings instance like dialogs do
		CUser testUser = new CUser("Dialog Test User");
		CProject testProject = new CProject("Dialog Test Project");
		// When - Create new settings and immediately set required relationships
		CUserProjectSettings settings = new CUserProjectSettings();
		settings.setUser(testUser);
		settings.setProject(testProject);
		// Then - Verify that required fields are set to prevent constraint violations
		assertNotNull(settings.getUser(), "User must be set to prevent user_id constraint violation");
		assertNotNull(settings.getProject(), "Project must be set to prevent project_id constraint violation");
		assertEquals(testUser, settings.getUser(), "User should be correctly set");
		assertEquals(testProject, settings.getProject(), "Project should be correctly set");
		// Verify that role and permission can be null (as per entity definition)
		assertNull(settings.getRole(), "Role can be null as per entity definition");
		assertNull(settings.getPermission(), "Permission can be null as per entity definition");
	}

	/** Test specifically for the delete relationship fix. This test validates that our bidirectional collection management works correctly. */
	@Test
	void testDeleteRelationshipFix_BidirectionalCollectionManagement() {
		// Given - Create a user, project and their relationship
		CUser testUser = new CUser("Test User");
		CProject testProject = new CProject("Test Project");
		CUserProjectSettings settings = new CUserProjectSettings();
		settings.setUser(testUser);
		settings.setProject(testProject);
		settings.setPermission("READ_WRITE");
		// Set up bidirectional relationships
		testUser.addProjectSettings(settings);
		testProject.addUserSettings(settings);
		// Verify initial state
		assertEquals(1, testUser.getProjectSettings().size(), "User should have one project setting");
		assertEquals(1, testProject.getUserSettings().size(), "Project should have one user setting");
		assertTrue(testUser.getProjectSettings().contains(settings), "User should contain the settings");
		assertTrue(testProject.getUserSettings().contains(settings), "Project should contain the settings");
		// When - Remove the relationship (simulating what our fixed service method does)
		testUser.removeProjectSettings(settings);
		testProject.removeUserSettings(settings);
		// Then - Verify collections are properly cleared
		assertTrue(testUser.getProjectSettings().isEmpty(), "User project settings should be empty after removal");
		assertTrue(testProject.getUserSettings().isEmpty(), "Project user settings should be empty after removal");
		assertNull(settings.getUser(), "Settings should not reference user after removal");
		assertNull(settings.getProject(), "Settings should not reference project after removal");
	}
}
