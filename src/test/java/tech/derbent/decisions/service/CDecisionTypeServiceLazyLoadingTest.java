package tech.derbent.decisions.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import tech.derbent.abstracts.domains.CTestBase;
import tech.derbent.decisions.domain.CDecisionType;
import tech.derbent.projects.domain.CProject;

/**
 * Tests for CDecisionTypeService lazy loading functionality. Verifies that the service
 * properly loads related entities to prevent LazyInitializationException.
 */
public class CDecisionTypeServiceLazyLoadingTest extends CTestBase {

	@Mock
	private CDecisionTypeRepository repository;

	@Mock
	private Clock clock;

	private CDecisionTypeService service;

	private CProject project;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		project = new CProject("Test Project");
		service = new CDecisionTypeService(repository, clock);
	}

	@Test
	void testGetHandlesNullId() {
		// When
		final Optional<CDecisionType> result = service.get(null);
		// Then
		assertFalse(result.isPresent());
		verify(repository, never()).findByIdWithRelationships(any());
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
		when(repository.findByIdWithRelationships(decisionTypeId))
			.thenReturn(Optional.of(decisionType));
		// When
		final Optional<CDecisionType> result = service.get(decisionTypeId);
		// Then
		assertTrue(result.isPresent());
		assertEquals("Test Decision Type", result.get().getName());
		assertEquals("Test Project", result.get().getProject().getName());
		// Verify that the eager loading method was called
		verify(repository).findByIdWithRelationships(decisionTypeId);
		verify(repository, never()).findById(decisionTypeId);
	}

	@Test
	void testGetWithNonExistentId() {
		// Given
		final Long nonExistentId = 999L;
		when(repository.findByIdWithRelationships(nonExistentId))
			.thenReturn(Optional.empty());
		// When
		final Optional<CDecisionType> result = service.get(nonExistentId);
		// Then
		assertFalse(result.isPresent());
		verify(repository).findByIdWithRelationships(nonExistentId);
	}

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
		
	}
}