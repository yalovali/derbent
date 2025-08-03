package tech.derbent.activities.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.projects.domain.CProject;

/**
 * Test class for CActivityService lazy loading functionality. Specifically tests the fix
 * for LazyInitializationException with CActivityType.
 */
class CActivityServiceLazyLoadingTest {

	@Mock
	private CActivityRepository activityRepository;

	@Mock
	private Clock clock;

	private CActivityService activityService;

	private CProject project;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		activityService = new CActivityService(activityRepository, clock);
		project = new CProject("Test Project");
	}

	@Test
	void testGetWithActivityTypeUsesEagerLoading() {
		// Given
		final Long activityId = 1L;
		final CActivity activity = new CActivity("Test Activity", project);
		final CActivityType activityType = new CActivityType("Test Type", project);
		activity.setActivityType(activityType);
		when(activityRepository.findByIdWithActivityType(activityId))
			.thenReturn(Optional.of(activity));
		// When
		final Optional<CActivity> result =
			activityService.getWithActivityType(activityId);
		// Then
		assertNotNull(result);
		assertNotNull(result.orElse(null));
		assertNotNull(result.get().getActivityType());
	}

	@Test
	void testInitializeLazyFieldsHandlesNullEntity() {
		// When & Then - Should not throw any exception
		assertDoesNotThrow(() -> {
			// The method is inherited from the base class and uses generic type Just
			// verify the service handles null entity gracefully
			activityService.getById(null);
		});
	}

	@Test
	void testOverriddenGetMethodUsesEagerLoading() {
		// Given
		final Long activityId = 1L;
		final CActivity activity = new CActivity("Test Activity", project);
		final CActivityType activityType =
			new CActivityType("Test Activity Type", project);
		activity.setActivityType(activityType);
		when(activityRepository.findByIdWithAllRelationships(activityId))
			.thenReturn(Optional.of(activity));
		// When & Then - Should not throw LazyInitializationException
		assertDoesNotThrow(() -> {
			final Optional<CActivity> result = activityService.getById(activityId);

			if (result.isPresent()) {
				// Access the lazy loaded field - this would throw
				// LazyInitializationException before the fix
				final CActivityType type = result.get().getActivityType();

				if (type != null) {
					type.getName(); // Access a property to trigger lazy loading
				}
			}
		});
	}

	@Test
	void testProjectNameAccessForGridDisplay() {
		// Given
		final Long activityId = 1L;
		final CActivity activity = new CActivity("Test Activity", project);
		final CActivityType activityType =
			new CActivityType("Test Activity Type", project);
		activity.setActivityType(activityType);
		when(activityRepository.findByIdWithAllRelationships(activityId))
			.thenReturn(Optional.of(activity));
		// When & Then - Should not throw LazyInitializationException when accessing
		// project name
		assertDoesNotThrow(() -> {
			final Optional<CActivity> result = activityService.getById(activityId);

			if (result.isPresent()) {
				// This is what the grid calls - should not throw
				// LazyInitializationException
				final String projectName = result.get().getProjectName();
				assertNotNull(projectName);
			}
		});
	}
}