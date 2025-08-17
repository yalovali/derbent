package unit_tests.tech.derbent.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tech.derbent.projects.domain.CProject;

/**
 * CSearchFunctionalityTest - Unit tests for search functionality. Layer: Test
 * 
 * Tests the search interface implementation and basic search behavior.
 */
class CSearchFunctionalityTest {

    private CProject project;

    @BeforeEach
    void setUp() {
        project = new CProject("Test Project");
        project.setDescription("This is a test project for search functionality");
    }

    @Test
    void testProjectImplementsSearchable() {
        // Verify that CProject implements CSearchable
        assertTrue(project instanceof tech.derbent.abstracts.interfaces.CSearchable);
    }

    @Test
    void testSearchByName() {
        // Test exact name match
        assertTrue(project.matches("Test Project"));

        // Test partial name match
        assertTrue(project.matches("Test"));
        assertTrue(project.matches("Project"));

        // Test case insensitive match
        assertTrue(project.matches("test"));
        assertTrue(project.matches("PROJECT"));
        assertTrue(project.matches("tEsT pRoJeCt"));
    }

    @Test
    void testSearchByDescription() {
        // Test exact description match
        assertTrue(project.matches("This is a test project for search functionality"));

        // Test partial description match
        assertTrue(project.matches("test project"));
        assertTrue(project.matches("search functionality"));
        assertTrue(project.matches("functionality"));

        // Test case insensitive match
        assertTrue(project.matches("SEARCH"));
        assertTrue(project.matches("Test Project"));
    }

    @Test
    void testSearchWithNullOrEmpty() {
        // Null search should match all
        assertTrue(project.matches(null));

        // Empty search should match all
        assertTrue(project.matches(""));
        assertTrue(project.matches("   "));
    }

    @Test
    void testSearchNoMatch() {
        // Test strings that should not match
        assertFalse(project.matches("NonExistentText"));
        assertFalse(project.matches("xyz"));
        assertFalse(project.matches("completely different"));
    }

    @Test
    void testSearchWithNullFields() {
        // Create project with null description
        CProject projectWithNullDesc = new CProject("Test Project");
        // Description is null by default

        // Should still match by name
        assertTrue(projectWithNullDesc.matches("Test"));
        assertTrue(projectWithNullDesc.matches("Project"));

        // Should not match description search
        assertFalse(projectWithNullDesc.matches("description"));
    }

    @Test
    void testSearchById() {
        // Set an ID for testing (normally done by JPA)
        java.lang.reflect.Field idField;
        try {
            idField = tech.derbent.abstracts.domains.CEntityDB.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(project, 123L);
        } catch (Exception e) {
            fail("Could not set ID for testing: " + e.getMessage());
        }

        // Should match by ID
        assertTrue(project.matches("123"));
        assertTrue(project.matches("12"));

        // Should not match non-existent ID
        assertFalse(project.matches("456"));
    }
}