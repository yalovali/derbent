package tech.derbent.decisions.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import tech.derbent.decisions.domain.CDecisionStatus;
import tech.derbent.projects.domain.CProject;

/**
 * Test class for CDecisionStatusService lazy loading functionality. Tests the enhanced
 * lazy loading fixes for decision status entities.
 */
class CDecisionStatusServiceLazyLoadingTest {

	@Mock
	private CDecisionStatusRepository decisionStatusRepository;

	@Mock
	private Clock clock;

	private CDecisionStatusService decisionStatusService;

	private CProject project;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		decisionStatusService =
			new CDecisionStatusService(decisionStatusRepository, clock);
		project = new CProject("Test Project");
	}

	@Test
	void testGetWithNullIdReturnsEmpty() {
		// When
		final Optional<CDecisionStatus> result = decisionStatusService.getById(null);
		// Then
		assertTrue(result.isEmpty());
	}

	@Test
	void testInitializeLazyFieldsHandlesNullEntity() {
		// When & Then - Should not throw any exception
		assertDoesNotThrow(() -> {
			// The method is inherited from the base class and uses generic type Just
			// verify the service handles null entity gracefully
			decisionStatusService.getById(null);
		});
	}

	@Test
	void testOverriddenGetMethodUsesEagerLoading() {
		// Given
		final Long statusId = 1L;
		final CDecisionStatus decisionStatus =
			new CDecisionStatus("Test Status", project);
		when(decisionStatusRepository.findByIdWithEagerLoading(statusId))
			.thenReturn(Optional.of(decisionStatus));
		// When & Then - Should not throw LazyInitializationException
		assertDoesNotThrow(() -> {
			final Optional<CDecisionStatus> result =
				decisionStatusService.getById(statusId);
			assertTrue(result.isPresent());
			assertNotNull(result.get().getName());
		});
	}
}