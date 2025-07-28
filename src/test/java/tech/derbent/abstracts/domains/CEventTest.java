package tech.derbent.abstracts.domains;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * Unit tests for CEvent abstract base class.
 * Tests the event base functionality through a concrete test implementation.
 */
@DisplayName("CEvent Abstract Base Tests") 
class CEventTest {

    private TestEvent event;
    private CProject project;
    private CUser author;

    // Concrete test implementation of CEvent for testing
    private static class TestEvent extends CEvent {
        public TestEvent() {
            super();
        }
        
        public TestEvent(CProject project, CUser author) {
            super(project, author);
        }
        
        public TestEvent(String name, CProject project, CUser author) {
            super(name, project, author);
        }
    }

    @BeforeEach
    void setUp() {
        // Create test project
        project = new CProject();
        project.setName("Test Project");
        
        // Create test user (author)
        author = new CUser();
        author.setName("Jane Smith");
        author.setLogin("jane.smith");
    }

    @Test
    @DisplayName("Should create event with project and author")
    void testEventCreation() {
        // When
        event = new TestEvent(project, author);
        
        // Then
        assertNotNull(event);
        assertEquals(project, event.getProject());
        assertEquals(author, event.getAuthor());
        assertNotNull(event.getEventDate());
        assertEquals("Jane Smith", event.getAuthorName());
    }

    @Test
    @DisplayName("Should create event with name, project and author")
    void testEventCreationWithName() {
        // When
        event = new TestEvent("Test Event", project, author);
        
        // Then
        assertNotNull(event);
        assertEquals("Test Event", event.getName());
        assertEquals(project, event.getProject());
        assertEquals(author, event.getAuthor());
        assertNotNull(event.getEventDate());
    }

    @Test
    @DisplayName("Should set event date automatically on creation")
    void testEventDateAutoSet() {
        // Given
        final LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);
        
        // When
        event = new TestEvent(project, author);
        
        // Then
        final LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);
        assertNotNull(event.getEventDate());
        assertTrue(event.getEventDate().isAfter(beforeCreation));
        assertTrue(event.getEventDate().isBefore(afterCreation));
    }

    @Test
    @DisplayName("Should set and get event date")
    void testEventDateSetterGetter() {
        // Given
        event = new TestEvent(project, author);
        final LocalDateTime customDate = LocalDateTime.of(2024, 1, 15, 10, 30);
        
        // When
        event.setEventDate(customDate);
        
        // Then
        assertEquals(customDate, event.getEventDate());
    }

    @Test
    @DisplayName("Should set and get author")
    void testAuthorSetterGetter() {
        // Given
        event = new TestEvent(project, author);
        
        final CUser newAuthor = new CUser();
        newAuthor.setName("John Doe");
        newAuthor.setLogin("john.doe");
        
        // When
        event.setAuthor(newAuthor);
        
        // Then
        assertEquals(newAuthor, event.getAuthor());
        assertEquals("John Doe", event.getAuthorName());
    }

    @Test
    @DisplayName("Should handle null author gracefully")
    void testNullAuthor() {
        // Given
        event = new TestEvent();
        event.setAuthor(null);
        
        // Then
        assertEquals("Unknown Author", event.getAuthorName());
    }

    @Test
    @DisplayName("Should initialize defaults correctly")
    void testInitializeDefaults() {
        // Given
        event = new TestEvent();
        
        // When
        event.initializeDefaults();
        
        // Then
        assertNotNull(event.getEventDate());
    }

    @Test
    @DisplayName("Should not override existing event date in initializeDefaults")
    void testInitializeDefaultsDoesNotOverrideEventDate() {
        // Given
        event = new TestEvent(project, author);
        final LocalDateTime originalDate = event.getEventDate();
        
        // When
        event.initializeDefaults();
        
        // Then
        assertEquals(originalDate, event.getEventDate());
    }

    @Test
    @DisplayName("Should generate meaningful toString")
    void testToString() {
        // Given
        event = new TestEvent("Test Event", project, author);
        
        // When
        final String result = event.toString();
        
        // Then
        assertTrue(result.contains("Test Event"));
        assertTrue(result.contains("eventDate="));
        assertTrue(result.contains("author=Jane Smith"));
    }

    @Test
    @DisplayName("Should inherit from CEntityOfProject correctly")
    void testInheritance() {
        // Given
        event = new TestEvent("Test Event", project, author);
        event.initializeDefaults(); // Initialize audit fields
        
        // Then - Should have inherited methods from CEntityOfProject
        assertEquals("Test Project", event.getProjectName());
        assertNotNull(event.getName());
        assertNotNull(event.getProject());
        
        // Should also have inherited audit fields from CEntityNamed
        assertNotNull(event.getCreatedDate());
        assertNotNull(event.getLastModifiedDate());
    }
    
    // Helper method for assertTrue
    private void assertTrue(boolean condition) {
        org.junit.jupiter.api.Assertions.assertTrue(condition);
    }
}