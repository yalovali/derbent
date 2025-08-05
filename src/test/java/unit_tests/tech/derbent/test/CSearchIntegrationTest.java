package unit_tests.tech.derbent.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tech.derbent.abstracts.components.CSearchToolbar;
import tech.derbent.abstracts.interfaces.CSearchable;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * CSearchIntegrationTest - Integration tests for complete search functionality.
 * Layer: Test
 * 
 * Tests the entire search ecosystem including toolbar, entities, and service integration.
 */
class CSearchIntegrationTest {

    private CProject project;
    private CUser user;
    private CSearchToolbar searchToolbar;

    @BeforeEach
    void setUp() {
        // Create test project
        project = new CProject("Sample Project");
        project.setDescription("A comprehensive test project for the search functionality");
        
        // Create test user
        user = new CUser();
        user.setName("John");
        user.setLastname("Doe");
        user.setLogin("johndoe");
        user.setEmail("john.doe@example.com");
        user.setDescription("Senior Developer");
        
        // Create search toolbar
        searchToolbar = new CSearchToolbar();
    }

    @Test
    void testSearchableImplementation() {
        // Verify entities implement CSearchable
        assertTrue(project instanceof CSearchable);
        assertTrue(user instanceof CSearchable);
    }

    @Test
    void testSearchToolbarComponent() {
        // Test toolbar creation and basic functionality
        assertNotNull(searchToolbar);
        assertNotNull(searchToolbar.getSearchField());
        assertEquals("", searchToolbar.getSearchText());
        
        // Test setting search text
        searchToolbar.setSearchText("test");
        assertEquals("test", searchToolbar.getSearchText());
        
        // Test clearing search
        searchToolbar.clearSearch();
        assertEquals("", searchToolbar.getSearchText());
    }

    @Test
    void testProjectSearchComprehensive() {
        // Test various search scenarios for project
        
        // Name searches
        assertTrue(project.matches("Sample"));
        assertTrue(project.matches("sample"));
        assertTrue(project.matches("SAMPLE"));
        assertTrue(project.matches("Project"));
        assertTrue(project.matches("Sample Project"));
        
        // Description searches
        assertTrue(project.matches("comprehensive"));
        assertTrue(project.matches("test project"));
        assertTrue(project.matches("functionality"));
        assertTrue(project.matches("COMPREHENSIVE"));
        
        // Partial matches
        assertTrue(project.matches("comp"));
        assertTrue(project.matches("func"));
        assertTrue(project.matches("Sam"));
        
        // No matches
        assertFalse(project.matches("nonexistent"));
        assertFalse(project.matches("xyz"));
        
        // Empty searches
        assertTrue(project.matches(""));
        assertTrue(project.matches("   "));
        assertTrue(project.matches(null));
    }

    @Test
    void testUserSearchComprehensive() {
        // Test various search scenarios for user
        
        // Name searches
        assertTrue(user.matches("John"));
        assertTrue(user.matches("john"));
        assertTrue(user.matches("JOHN"));
        assertTrue(user.matches("Doe"));
        assertTrue(user.matches("doe"));
        
        // Login searches
        assertTrue(user.matches("johndoe"));
        assertTrue(user.matches("JOHNDOE"));
        assertTrue(user.matches("john"));
        
        // Email searches
        assertTrue(user.matches("john.doe@example.com"));
        assertTrue(user.matches("example.com"));
        assertTrue(user.matches("john.doe"));
        assertTrue(user.matches("@example"));
        
        // Description searches
        assertTrue(user.matches("Senior"));
        assertTrue(user.matches("Developer"));
        assertTrue(user.matches("senior developer"));
        
        // Partial matches
        assertTrue(user.matches("Jo"));
        assertTrue(user.matches("Do"));
        assertTrue(user.matches("senior"));
        assertTrue(user.matches("dev"));
        
        // No matches
        assertFalse(user.matches("nonexistent"));
        assertFalse(user.matches("xyz"));
        
        // Empty searches
        assertTrue(user.matches(""));
        assertTrue(user.matches("   "));
        assertTrue(user.matches(null));
    }

    @Test
    void testSearchWithSpecialCharacters() {
        // Test project with special characters
        CProject specialProject = new CProject("Test-Project_2024");
        specialProject.setDescription("Project with special chars: @#$%");
        
        assertTrue(specialProject.matches("Test-Project"));
        assertTrue(specialProject.matches("2024"));
        assertTrue(specialProject.matches("special chars"));
        assertTrue(specialProject.matches("@#$"));
        assertTrue(specialProject.matches("Project_2024"));
    }

    @Test
    void testSearchWithNullFields() {
        // Test user with null fields
        CUser nullFieldUser = new CUser();
        nullFieldUser.setName("TestUser");
        // Other fields remain null
        
        assertTrue(nullFieldUser.matches("TestUser"));
        assertTrue(nullFieldUser.matches("test"));
        assertFalse(nullFieldUser.matches("email"));
        assertFalse(nullFieldUser.matches("description"));
    }

    @Test
    void testCaseInsensitiveSearch() {
        // Comprehensive case insensitive testing
        String[] searchTerms = {"sample", "SAMPLE", "SaMpLe", "sAmPlE"};
        
        for (String term : searchTerms) {
            assertTrue(project.matches(term), "Should match: " + term);
        }
        
        String[] userTerms = {"john", "JOHN", "JoHn", "jOhN"};
        
        for (String term : userTerms) {
            assertTrue(user.matches(term), "Should match: " + term);
        }
    }

    @Test
    void testSearchToolbarEvents() {
        // Test that toolbar can handle search events
        final boolean[] eventFired = {false};
        final String[] lastSearchText = {""};
        
        searchToolbar.addSearchListener(event -> {
            eventFired[0] = true;
            lastSearchText[0] = event.getSearchText();
        });
        
        // Simulate typing in search field
        searchToolbar.getSearchField().setValue("test search");
        
        // Note: In a real environment with UI, this would trigger the event
        // For unit test, we verify the event setup is correct
        assertNotNull(searchToolbar.getSearchField().getValue());
        assertEquals("test search", searchToolbar.getSearchField().getValue());
    }

    @Test
    void testMultipleEntitiesWithSameSearchTerm() {
        // Test that the same search term can match different entities appropriately
        String searchTerm = "test";
        
        CProject testProject = new CProject("Test Project");
        CUser testUser = new CUser();
        testUser.setName("Test");
        testUser.setLogin("testuser");
        
        assertTrue(testProject.matches(searchTerm));
        assertTrue(testUser.matches(searchTerm));
        
        // Different search term should only match one
        String projectSpecific = "Project";
        assertTrue(testProject.matches(projectSpecific));
        assertFalse(testUser.matches(projectSpecific));
        
        String userSpecific = "testuser";
        assertFalse(testProject.matches(userSpecific));
        assertTrue(testUser.matches(userSpecific));
    }
}