package unit_tests.tech.derbent.users.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;

/** Test class for CUserProjectSettings relationship management and cascade deletion. */
class CUserProjectSettingsTest {

	@Test
	void testAddUserToProject() {
		// Create entities
		final CProject project = new CProject("Test Project");
		final CUser user = new CUser("Test User");
		user.setProjectSettings(new ArrayList<>());
		final CUserProjectSettings settings = new CUserProjectSettings();
		settings.setRole("Developer");
		settings.setPermission("READ_WRITE");
		// Add user to project
		CUserProjectSettings.addUserToProject(project, user, settings);
		// Verify the bidirectional relationship is properly set
		assertEquals(1, project.getUserSettings().size(), "Project should have one user setting");
		assertEquals(1, user.getProjectSettings().size(), "User should have one project setting");
		final CUserProjectSettings projectSetting = project.getUserSettings().get(0);
		final CUserProjectSettings userSetting = user.getProjectSettings().get(0);
		assertEquals(settings, projectSetting, "Project's user setting should be the same instance");
		assertEquals(settings, userSetting, "User's project setting should be the same instance");
		assertEquals(user, settings.getUser(), "Settings should reference the correct user");
		assertEquals(project, settings.getProject(), "Settings should reference the correct project");
	}

	@Test
	void testRemoveUserFromProject() {
		// Create entities and add relationship
		final CProject project = new CProject("Test Project");
		final CUser user = new CUser("Test User");
		user.setProjectSettings(new ArrayList<>());
		final CUserProjectSettings settings = new CUserProjectSettings();
		CUserProjectSettings.addUserToProject(project, user, settings);
		// Verify relationship exists
		assertEquals(1, project.getUserSettings().size());
		assertEquals(1, user.getProjectSettings().size());
		// Remove user from project
		CUserProjectSettings.removeUserFromProject(project, user);
		// Verify relationship is removed from both sides
		assertEquals(0, project.getUserSettings().size(), "Project should have no user settings after removal");
		assertEquals(0, user.getProjectSettings().size(), "User should have no project settings after removal");
	}

	@Test
	void testMultipleUsersInProject() {
		// Create entities
		final CProject project = new CProject("Test Project");
		final CUser user1 = new CUser("User One");
		final CUser user2 = new CUser("User Two");
		user1.setProjectSettings(new ArrayList<>());
		user2.setProjectSettings(new ArrayList<>());
		final CUserProjectSettings settings1 = new CUserProjectSettings();
		settings1.setRole("Developer");
		final CUserProjectSettings settings2 = new CUserProjectSettings();
		settings2.setRole("Manager");
		// Add both users to project
		CUserProjectSettings.addUserToProject(project, user1, settings1);
		CUserProjectSettings.addUserToProject(project, user2, settings2);
		// Verify both relationships exist
		assertEquals(2, project.getUserSettings().size(), "Project should have two user settings");
		assertEquals(1, user1.getProjectSettings().size(), "User1 should have one project setting");
		assertEquals(1, user2.getProjectSettings().size(), "User2 should have one project setting");
		// Remove one user
		CUserProjectSettings.removeUserFromProject(project, user1);
		// Verify only one relationship remains
		assertEquals(1, project.getUserSettings().size(), "Project should have one user setting after removal");
		assertEquals(0, user1.getProjectSettings().size(), "User1 should have no project settings after removal");
		assertEquals(1, user2.getProjectSettings().size(), "User2 should still have one project setting");
	}

	@Test
	void testToStringMethod() {
		final CProject project = new CProject("Test Project");
		final CUser user = new CUser("Test User");
		user.setLogin("testuser");
		final CUserProjectSettings settings = new CUserProjectSettings();
		settings.setUser(user);
		settings.setProject(project);
		settings.setRole("Developer");
		settings.setPermission("READ_WRITE");
		final String result = settings.toString();
		assertNotNull(result, "toString should not return null");
		assertTrue(result.contains("testuser"), "toString should contain user login");
		assertTrue(result.contains("Test Project"), "toString should contain project name");
		assertTrue(result.contains("Developer"), "toString should contain role");
		assertTrue(result.contains("READ_WRITE"), "toString should contain permission");
	}
}
