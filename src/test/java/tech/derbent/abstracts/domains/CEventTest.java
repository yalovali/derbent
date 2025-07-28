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
 * Unit tests for CEvent abstract base class. Tests the event base functionality through a
 * concrete test implementation.
 */
@DisplayName ("CEvent Abstract Base Tests")
class CEventTest {

	// Concrete test implementation of CEvent for testing
	private static class TestEvent extends CEvent {

		public TestEvent() {
			super();
		}

		public TestEvent(final CUser author) {
			super(author);
		}
	}

	private TestEvent event;

	private CProject project;

	private CUser author;

	// Helper method for assertTrue
	private void assertTrue(final boolean condition) {
		org.junit.jupiter.api.Assertions.assertTrue(condition);
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
	@DisplayName ("Should set and get author")
	void testAuthorSetterGetter() {
		// Given
		event = new TestEvent(author);
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
	@DisplayName ("Should create event with project and author")
	void testEventCreation() {
		// When
		event = new TestEvent(author);
		// Then
		assertNotNull(event);
		assertEquals(author, event.getAuthor());
		assertNotNull(event.getEventDate());
		assertEquals("Jane Smith", event.getAuthorName());
	}

	@Test
	@DisplayName ("Should set event date automatically on creation")
	void testEventDateAutoSet() {
		// Given
		final LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);
		// When
		event = new TestEvent(author);
		// Then
		final LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);
		assertNotNull(event.getEventDate());
		assertTrue(event.getEventDate().isAfter(beforeCreation));
		assertTrue(event.getEventDate().isBefore(afterCreation));
	}

	@Test
	@DisplayName ("Should set and get event date")
	void testEventDateSetterGetter() {
		// Given
		event = new TestEvent(author);
		final LocalDateTime customDate = LocalDateTime.of(2024, 1, 15, 10, 30);
		// When
		event.setEventDate(customDate);
		// Then
		assertEquals(customDate, event.getEventDate());
	}

	@Test
	@DisplayName ("Should initialize defaults correctly")
	void testInitializeDefaults() {
		// Given
		event = new TestEvent();
		// When
		event.initializeDefaults();
		// Then
		assertNotNull(event.getEventDate());
	}

	@Test
	@DisplayName ("Should not override existing event date in initializeDefaults")
	void testInitializeDefaultsDoesNotOverrideEventDate() {
		// Given
		event = new TestEvent(author);
		final LocalDateTime originalDate = event.getEventDate();
		// When
		event.initializeDefaults();
		// Then
		assertEquals(originalDate, event.getEventDate());
	}

	@Test
	@DisplayName ("Should handle null author gracefully")
	void testNullAuthor() {
		// Given
		event = new TestEvent();
		event.setAuthor(null);
		// Then
		assertEquals("Unknown Author", event.getAuthorName());
	}
}