package unit_tests.tech.derbent.decisions.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import tech.derbent.decisions.domain.CDecisionType;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Tests for CDecisionTypeService functionality. Verifies that the service properly uses
 * standard repository methods.
 */
public class CDecisionTypeServiceLazyLoadingTest extends CTestBase {

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
	}

	@Test
	void testGetHandlesNullId() {
		// When
		final Optional<CDecisionType> result = decisionTypeService.getById(null);
		// Then
		assertFalse(result.isPresent());
		verify(decisionTypeRepository, never()).findByIdWithRelationships(any());
	}

	@Test
	void testGetUsesEagerLoadingQuery() {
		// Given
		final Long decisionTypeId = 1L;
		final CDecisionType decisionType =
			new CDecisionType("Test Decision Type", project);
		// Set description using the setter method if needed
		decisionType.setDescription("Test Description");
		// Note: ID is typically set by JPA, so we'll mock the return directly
		when(decisionTypeRepository.findByIdWithRelationships(decisionTypeId))
			.thenReturn(Optional.of(decisionType));
		// When
		final Optional<CDecisionType> result =
			decisionTypeService.getById(decisionTypeId);
		// Then
		assertTrue(result.isPresent());
		assertEquals("Test Decision Type", result.get().getName());
		assertEquals("Test Project", result.get().getProject().getName());
		// Verify that the eager loading method was called
		verify(decisionTypeRepository).findByIdWithRelationships(decisionTypeId);
	}

	@Test
	void testGetWithNonExistentId() {
		// Given
		final Long nonExistentId = 999L;
		when(decisionTypeRepository.findByIdWithRelationships(nonExistentId))
			.thenReturn(Optional.empty());
		// When
		final Optional<CDecisionType> result = decisionTypeService.getById(nonExistentId);
		// Then
		assertFalse(result.isPresent());
		verify(decisionTypeRepository).findByIdWithRelationships(nonExistentId);
	}
}