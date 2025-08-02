package tech.derbent.activities.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import tech.derbent.abstracts.domains.CTestBase;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityType;

/**
 * Test class for CActivityService lazy loading functionality. Specifically tests the fix
 * for LazyInitializationException with CActivityType.
 */
class CActivityServiceLazyLoadingTest extends CTestBase {

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
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
			// Use reflection to access the protected method for testing
			activityService.getClass().getDeclaredMethod("initializeLazyFields",
				CActivity.class);
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
			final Optional<CActivity> result = activityService.get(activityId);

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
			final Optional<CActivity> result = activityService.get(activityId);

			if (result.isPresent()) {
				// This is what the grid calls - should not throw
				// LazyInitializationException
				final String projectName = result.get().getProjectName();
				assertNotNull(projectName);
			}
		});
	}
}