package tech.derbent.users.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.derbent.projects.domain.CProject;

/** Unit tests for user-project relationship management in CUser and CProject entities. */
public class CUserProjectRelationshipTest {

	private CUser user;
	private CProject project;
	private CUserProjectSettings settings;

	@BeforeEach
	void setUp() {
		user = new CUser("Test User");
		user.setLogin("testuser");
		user.setEmail("test@example.com");
		project = new CProject("Test Project");
		settings = new CUserProjectSettings();
		settings.setPermission("READ,WRITE");
	}

	@Test
	void testUserProjectSettingsInitialization() {
		// Test that projectSettings is properly initialized
		assertNotNull(user.getProjectSettings(), "User projectSettings should be initialized");
		assertTrue(user.getProjectSettings().isEmpty(), "User projectSettings should be empty initially");
		// Test that userSettings is properly initialized
		assertNotNull(project.getUserSettings(), "Project userSettings should be initialized");
		assertTrue(project.getUserSettings().isEmpty(), "Project userSettings should be empty initially");
	}

	@Test
	void testAddProjectSettingsToUser() {
		// Test adding project settings to user
		user.addProjectSettings(settings);
		assertEquals(1, user.getProjectSettings().size(), "User should have one project setting");
		assertTrue(user.getProjectSettings().contains(settings), "User should contain the added settings");
		assertEquals(user, settings.getUser(), "Settings should reference the user");
	}

	@Test
	void testAddUserSettingsToProject() {
		// Test adding user settings to project
		project.addUserSettings(settings);
		assertEquals(1, project.getUserSettings().size(), "Project should have one user setting");
		assertTrue(project.getUserSettings().contains(settings), "Project should contain the added settings");
		assertEquals(project, settings.getProject(), "Settings should reference the project");
	}

	@Test
	void testBidirectionalRelationship() {
		// Set up the bidirectional relationship
		settings.setUser(user);
		settings.setProject(project);
		user.addProjectSettings(settings);
		project.addUserSettings(settings);
		// Verify both sides of the relationship
		assertEquals(1, user.getProjectSettings().size(), "User should have one project setting");
		assertEquals(1, project.getUserSettings().size(), "Project should have one user setting");
		assertEquals(user, settings.getUser(), "Settings should reference the user");
		assertEquals(project, settings.getProject(), "Settings should reference the project");
	}

	@Test
	void testRemoveProjectSettingsFromUser() {
		// Set up the relationship first
		settings.setUser(user);
		user.addProjectSettings(settings);
		// Remove the settings
		user.removeProjectSettings(settings);
		assertTrue(user.getProjectSettings().isEmpty(), "User projectSettings should be empty after removal");
		assertNull(settings.getUser(), "Settings should not reference the user after removal");
	}

	@Test
	void testRemoveUserSettingsFromProject() {
		// Set up the relationship first
		settings.setProject(project);
		project.addUserSettings(settings);
		// Remove the settings
		project.removeUserSettings(settings);
		assertTrue(project.getUserSettings().isEmpty(), "Project userSettings should be empty after removal");
		assertNull(settings.getProject(), "Settings should not reference the project after removal");
	}

	@Test
	void testStaticHelperMethods() {
		// Test the static helper methods in CUserProjectSettings
		CUserProjectSettings.addUserToProject(project, user, settings);
		// Verify that both sides of the relationship are maintained
		assertEquals(1, user.getProjectSettings().size(), "User should have one project setting");
		assertEquals(1, project.getUserSettings().size(), "Project should have one user setting");
		assertEquals(user, settings.getUser(), "Settings should reference the user");
		assertEquals(project, settings.getProject(), "Settings should reference the project");
	}

	@Test
	void testStaticRemoveMethod() {
		// Set up the relationship first
		settings.setUser(user);
		settings.setProject(project);
		user.addProjectSettings(settings);
		project.addUserSettings(settings);
		// Remove using static method
		CUserProjectSettings.removeUserFromProject(project, user);
		assertTrue(user.getProjectSettings().isEmpty(), "User projectSettings should be empty after removal");
		assertTrue(project.getUserSettings().isEmpty(), "Project userSettings should be empty after removal");
	}

	@Test
	void testNullSafetyInAddMethods() {
		// Test null safety
		user.addProjectSettings(null);
		project.addUserSettings(null);
		assertTrue(user.getProjectSettings().isEmpty(), "Adding null should not affect the collection");
		assertTrue(project.getUserSettings().isEmpty(), "Adding null should not affect the collection");
	}

	@Test
	void testNullSafetyInRemoveMethods() {
		// Test null safety
		user.removeProjectSettings(null);
		project.removeUserSettings(null);
		// Should not throw any exceptions
		assertTrue(user.getProjectSettings().isEmpty(), "Removing null should not affect the collection");
		assertTrue(project.getUserSettings().isEmpty(), "Removing null should not affect the collection");
	}

	@Test
	void testDuplicateAddPrevention() {
		// Add the same settings twice
		user.addProjectSettings(settings);
		user.addProjectSettings(settings);
		assertEquals(1, user.getProjectSettings().size(), "User should have only one instance of the settings");
		project.addUserSettings(settings);
		project.addUserSettings(settings);
		assertEquals(1, project.getUserSettings().size(), "Project should have only one instance of the settings");
	}
}
