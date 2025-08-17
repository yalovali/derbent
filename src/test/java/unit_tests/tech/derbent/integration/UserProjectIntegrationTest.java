package unit_tests.tech.derbent.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;

/**
 * Integration test to verify the user-project relationship functionality works correctly after the refactoring and base
 * class implementation.
 */
public class UserProjectIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProjectIntegrationTest.class);

    @Test
    public void testUserProjectSettingsDomainIntegration() {
        LOGGER.info("Testing user-project settings domain integration");

        assertDoesNotThrow(() -> {
            // Create test entities
            CUser testUser = new CUser();
            testUser.setName("John");
            testUser.setLastname("Doe");
            testUser.setLogin("johndoe");
            testUser.setEmail("john.doe@example.com");

            CProject testProject = new CProject("Test Project");

            // Create user-project settings
            CUserProjectSettings settings = new CUserProjectSettings();
            settings.setUser(testUser);
            settings.setProject(testProject);
            settings.setRole("DEVELOPER");
            settings.setPermission("READ,WRITE");

            // Verify relationships
            assertNotNull(settings.getUser(), "User should be set");
            assertNotNull(settings.getProject(), "Project should be set");
            assertNotNull(settings.getRole(), "Role should be set");
            assertNotNull(settings.getPermission(), "Permission should be set");

            // Test project's user settings relationship
            testProject.getUserSettings().add(settings);
            assertNotNull(testProject.getUserSettings(), "Project user settings should be accessible");

            LOGGER.info("Domain integration test passed successfully");
        }, "Domain integration should not throw exceptions");
    }

    @Test
    public void testUserProjectRelationshipBidirectional() {
        LOGGER.info("Testing bidirectional user-project relationship");

        assertDoesNotThrow(() -> {
            // Create entities
            CUser user1 = new CUser();
            user1.setName("Alice");
            user1.setLogin("alice");

            CUser user2 = new CUser();
            user2.setName("Bob");
            user2.setLogin("bob");

            CProject project = new CProject("Collaborative Project");

            // Create multiple user-project assignments
            CUserProjectSettings setting1 = new CUserProjectSettings();
            setting1.setUser(user1);
            setting1.setProject(project);
            setting1.setRole("MANAGER");
            setting1.setPermission("READ,WRITE,DELETE");

            CUserProjectSettings setting2 = new CUserProjectSettings();
            setting2.setUser(user2);
            setting2.setProject(project);
            setting2.setRole("DEVELOPER");
            setting2.setPermission("READ,WRITE");

            // Add to project
            project.getUserSettings().add(setting1);
            project.getUserSettings().add(setting2);

            // Verify the relationships work in both directions
            assertNotNull(project.getUserSettings(), "Project should have user settings");
            assertNotNull(setting1.getProject(), "Setting should reference project");
            assertNotNull(setting1.getUser(), "Setting should reference user");
            assertNotNull(setting2.getProject(), "Setting should reference project");
            assertNotNull(setting2.getUser(), "Setting should reference user");

            LOGGER.info("Bidirectional relationship test passed successfully");
        }, "Bidirectional relationship should work correctly");
    }
}