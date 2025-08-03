package tech.derbent.activities.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import tech.derbent.abstracts.domains.CTestBase;
import tech.derbent.activities.domain.CActivityStatus;

/**
 * Test class for CActivityStatusService functionality. Tests CRUD operations, error
 * handling, and lazy loading concerns.
 */
class CActivityStatusServiceTest extends CTestBase {

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
	}

	@Test
	void testConstructor() {
		// When/Then - should not throw exception
		assertDoesNotThrow(
			() -> new CActivityStatusService(activityStatusRepository, clock));
	}

	@Test
	void testCreateEntityWithBlankName() {
		// When & Then
		assertThrows(IllegalArgumentException.class, () -> {
			new CActivityStatus("   ", project);
		}, "Should throw IllegalArgumentException for blank name");
	}

	@Test
	void testCreateEntityWithEmptyName() {
		// Given When & Then
		assertThrows(IllegalArgumentException.class, () -> {
			new CActivityStatus("", project);
		}, "Should throw IllegalArgumentException for empty name");
	}

	@Test
	void testCreateEntityWithNameAndDescription() {
		// Given
		final String name = "IN_PROGRESS";
		final String description = "Work is in progress";
		final String color = "#0066cc";
		// When
		final CActivityStatus status =
			new CActivityStatus(name, project, description, color, false);
		// Then
		assertNotNull(status);
		assertEquals(name, status.getName());
		assertEquals(description, status.getDescription());
		assertEquals(project, status.getProject());
		assertFalse(status.isFinal());
	}

	@Test
	void testCreateEntityWithNullName() {
		assertThrows(IllegalArgumentException.class, () -> {
			new CActivityStatus(null, project);
		}, "Should throw IllegalArgumentException for null name");
	}

	@Test
	void testCreateEntityWithValidName() {
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
	void testGetWithNonExistentId() {
		// Given
		final Long statusId = 999L;
		when(activityStatusRepository.findById(statusId)).thenReturn(Optional.empty());
		// When
		final Optional<CActivityStatus> result = activityStatusService.getById(statusId);
		// Then
		assertTrue(result.isEmpty());
	}

	@Test
	void testGetWithNullId() {
		// When
		final Optional<CActivityStatus> result = activityStatusService.getById(null);
		// Then
		assertTrue(result.isEmpty());
	}

	@Test
	void testGetWithValidId() {
		// Given
		final Long statusId = 1L;
		final CActivityStatus expectedStatus = new CActivityStatus("TODO", project);
		when(activityStatusRepository.findById(statusId))
			.thenReturn(Optional.of(expectedStatus));
		// When
		final Optional<CActivityStatus> result = activityStatusService.getById(statusId);
		// Then
		assertTrue(result.isPresent());
		assertEquals(expectedStatus, result.get());
		verify(activityStatusRepository).findById(statusId);
	}

	@Test
	void testInitializeLazyFieldsWithValidEntity() {
		// TODO FIXME - should throw IllegalArgumentException
	}
}