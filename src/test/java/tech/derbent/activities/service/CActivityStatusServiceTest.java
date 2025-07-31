package tech.derbent.activities.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.projects.domain.CProject;

/**
 * Test class for CActivityStatusService functionality. Tests CRUD operations, error
 * handling, and lazy loading concerns.
 */
class CActivityStatusServiceTest {

	@Mock
	private CActivityStatusRepository repository;

	@Mock
	private Clock clock;

	private CActivityStatusService activityStatusService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		activityStatusService = new CActivityStatusService(repository, clock);
	}

	@Test
	void testConstructor() {
		// When/Then - should not throw exception
		assertDoesNotThrow(() -> new CActivityStatusService(repository, clock));
	}

	@Test
	void testCreateEntityWithBlankName() {
		// Given
		final CProject project = new CProject();
		project.setName("Test Project");
		
		// When & Then
		assertThrows(IllegalArgumentException.class, () -> {
			new CActivityStatus("   ", project);
		}, "Should throw IllegalArgumentException for blank name");
	}

	@Test
	void testCreateEntityWithEmptyName() {
		// Given
		final CProject project = new CProject();
		project.setName("Test Project");
		
		// When & Then
		assertThrows(IllegalArgumentException.class, () -> {
			new CActivityStatus("", project);
		}, "Should throw IllegalArgumentException for empty name");
	}

	@Test
	void testCreateEntityWithNullName() {
		// Given
		final CProject project = new CProject();
		project.setName("Test Project");
		
		// When & Then
		assertThrows(IllegalArgumentException.class, () -> {
			new CActivityStatus(null, project);
		}, "Should throw IllegalArgumentException for null name");
	}

	@Test
	void testCreateEntityWithValidName() {
		// Given
		final CProject project = new CProject();
		project.setName("Test Project");
		final String validName = "TODO";
		
		// When
		final CActivityStatus status = new CActivityStatus(validName, project);
		
		// Then
		assertNotNull(status);
		assertEquals(validName, status.getName());
		assertEquals(project, status.getProject());
		assertFalse(status.isFinal());
	}

	@Test
	void testCreateEntityWithNameAndDescription() {
		// Given
		final CProject project = new CProject();
		project.setName("Test Project");
		final String name = "IN_PROGRESS";
		final String description = "Work is in progress";
		final String color = "#0066cc";
		
		// When
		final CActivityStatus status = new CActivityStatus(name, project, description, color, false);
		
		// Then
		assertNotNull(status);
		assertEquals(name, status.getName());
		assertEquals(description, status.getDescription());
		assertEquals(project, status.getProject());
		assertFalse(status.isFinal());
	}

	@Test
	void testGetWithNonExistentId() {
		// Given
		final Long statusId = 999L;
		when(repository.findById(statusId)).thenReturn(Optional.empty());
		// When
		final Optional<CActivityStatus> result = activityStatusService.get(statusId);
		// Then
		assertTrue(result.isEmpty());
	}

	@Test
	void testGetWithNullId() {
		// When
		final Optional<CActivityStatus> result = activityStatusService.get(null);
		// Then
		assertTrue(result.isEmpty());
	}

	@Test
	void testGetWithValidId() {
		// Given
		final Long statusId = 1L;
		final CProject project = new CProject();
		project.setName("Test Project");
		final CActivityStatus expectedStatus = new CActivityStatus("TODO", project);
		when(repository.findById(statusId)).thenReturn(Optional.of(expectedStatus));
		
		// When
		final Optional<CActivityStatus> result = activityStatusService.get(statusId);
		
		// Then
		assertTrue(result.isPresent());
		assertEquals(expectedStatus, result.get());
		verify(repository).findById(statusId);
	}

	@Test
	void testInitializeLazyFieldsWithValidEntity() {
		// TODO FIXME - should throw IllegalArgumentException
	}
}