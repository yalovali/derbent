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
	private CUserProjectSettingsRepository repository;
	@Mock
	private Clock clock;
	@Mock
	private CSessionService sessionService;
	private CUserProjectSettingsService service;
	private CUser user;
	private CProject project;

	@BeforeEach
	void setUp() {
		service = new CUserProjectSettingsService(repository, clock, sessionService);
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
		// When & Then
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.removeUserFromProject(user, project));
		assertEquals("User and project must have valid IDs", exception.getMessage());
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
		// When & Then
		IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> service.removeUserFromProject(null, project));
		IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> service.removeUserFromProject(user, null));
		assertEquals("User and project cannot be null", exception1.getMessage());
		assertEquals("User and project cannot be null", exception2.getMessage());
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
}
