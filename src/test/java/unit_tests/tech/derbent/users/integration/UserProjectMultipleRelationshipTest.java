package unit_tests.tech.derbent.users.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Integration test to verify that the user-project relationship supports:
 * 1. One user can have multiple projects
 * 2. One project can have multiple users
 * 3. Adding new relationships doesn't replace existing ones
 */
class UserProjectMultipleRelationshipTest extends CTestBase {

    private CUser user1;
    private CUser user2;
    private CProject project1;
    private CProject project2;

    @Override
    @BeforeEach
    protected void setupForTest() {
        // Create test users
        user1 = new CUser("Alice");
        user2 = new CUser("Bob");

        // Create test projects
        project1 = new CProject();
        project1.setName("Project Alpha");
        
        project2 = new CProject();
        project2.setName("Project Beta");
    }

    @Test
    void testOneUserMultipleProjects() {
        // Test that one user can have multiple projects
        
        // Create first user-project relationship
        CUserProjectSettings settings1 = new CUserProjectSettings();
        settings1.setUser(user1);
        settings1.setProject(project1);
        settings1.setRole("DEVELOPER");
        settings1.setPermission("READ,WRITE");

        // Initialize user with first project
        List<CUserProjectSettings> userProjects = new ArrayList<>();
        userProjects.add(settings1);
        user1.setProjectSettings(userProjects);

        // Verify initial state
        assertNotNull(user1.getProjectSettings(), "User should have project settings");
        assertEquals(1, user1.getProjectSettings().size(), "User should have one project initially");

        // Create second user-project relationship
        CUserProjectSettings settings2 = new CUserProjectSettings();
        settings2.setUser(user1);
        settings2.setProject(project2);
        settings2.setRole("TEAM_LEAD");
        settings2.setPermission("READ,WRITE,DELETE");

        // Simulate the fixed collection update logic from CUsersView
        List<CUserProjectSettings> currentSettings = user1.getProjectSettings();
        List<CUserProjectSettings> newSettings = new ArrayList<>(currentSettings);
        newSettings.add(settings2);

        // Apply the fix: update existing collection instead of replacing
        currentSettings.clear();
        currentSettings.addAll(newSettings);

        // Verify that user now has multiple projects
        assertEquals(2, user1.getProjectSettings().size(), "User should have two projects");
        
        // Verify both projects are present
        boolean hasProjectAlpha = user1.getProjectSettings().stream()
            .anyMatch(s -> "Project Alpha".equals(s.getProject().getName()));
        boolean hasProjectBeta = user1.getProjectSettings().stream()
            .anyMatch(s -> "Project Beta".equals(s.getProject().getName()));
            
        assertTrue(hasProjectAlpha, "User should be assigned to Project Alpha");
        assertTrue(hasProjectBeta, "User should be assigned to Project Beta");

        // Verify roles are correctly assigned
        boolean hasDeveloperRole = user1.getProjectSettings().stream()
            .anyMatch(s -> "DEVELOPER".equals(s.getRole()));
        boolean hasTeamLeadRole = user1.getProjectSettings().stream()
            .anyMatch(s -> "TEAM_LEAD".equals(s.getRole()));
            
        assertTrue(hasDeveloperRole, "User should have DEVELOPER role");
        assertTrue(hasTeamLeadRole, "User should have TEAM_LEAD role");
    }

    @Test
    void testOneProjectMultipleUsers() {
        // Test that one project can have multiple users
        
        // Create project settings for first user
        CUserProjectSettings settings1 = new CUserProjectSettings();
        settings1.setUser(user1);
        settings1.setProject(project1);
        settings1.setRole("DEVELOPER");
        settings1.setPermission("READ,WRITE");

        // Create project settings for second user
        CUserProjectSettings settings2 = new CUserProjectSettings();
        settings2.setUser(user2);
        settings2.setProject(project1);
        settings2.setRole("QA_ENGINEER");
        settings2.setPermission("READ,WRITE");

        // Set up both users with the same project
        user1.setProjectSettings(List.of(settings1));
        user2.setProjectSettings(List.of(settings2));

        // Verify both users are assigned to the same project
        assertNotNull(user1.getProjectSettings(), "User1 should have project settings");
        assertNotNull(user2.getProjectSettings(), "User2 should have project settings");
        
        assertEquals(1, user1.getProjectSettings().size(), "User1 should have one project");
        assertEquals(1, user2.getProjectSettings().size(), "User2 should have one project");

        // Verify both users are assigned to the same project
        String user1ProjectName = user1.getProjectSettings().get(0).getProject().getName();
        String user2ProjectName = user2.getProjectSettings().get(0).getProject().getName();
        
        assertEquals("Project Alpha", user1ProjectName, "User1 should be assigned to Project Alpha");
        assertEquals("Project Alpha", user2ProjectName, "User2 should be assigned to Project Alpha");

        // Verify different roles
        String user1Role = user1.getProjectSettings().get(0).getRole();
        String user2Role = user2.getProjectSettings().get(0).getRole();
        
        assertEquals("DEVELOPER", user1Role, "User1 should have DEVELOPER role");
        assertEquals("QA_ENGINEER", user2Role, "User2 should have QA_ENGINEER role");
    }

    @Test
    void testBidirectionalRelationshipConsistency() {
        // Test that bidirectional relationships remain consistent
        
        CUserProjectSettings settings = new CUserProjectSettings();
        settings.setUser(user1);
        settings.setProject(project1);
        settings.setRole("ARCHITECT");
        settings.setPermission("READ,WRITE,DELETE,ADMIN");

        // Set up the relationship
        user1.setProjectSettings(List.of(settings));

        // Verify the relationship is correctly established
        assertNotNull(user1.getProjectSettings(), "User should have project settings");
        assertEquals(1, user1.getProjectSettings().size(), "User should have one project");
        
        CUserProjectSettings retrievedSettings = user1.getProjectSettings().get(0);
        assertEquals(user1, retrievedSettings.getUser(), "Settings should reference the correct user");
        assertEquals(project1, retrievedSettings.getProject(), "Settings should reference the correct project");
        assertEquals("ARCHITECT", retrievedSettings.getRole(), "Settings should have correct role");
        assertEquals("READ,WRITE,DELETE,ADMIN", retrievedSettings.getPermission(), "Settings should have correct permissions");
    }
}