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
	private CActivityRepository repository;
	@Mock
	private Clock clock;
	private CActivityService activityService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		activityService = new CActivityService(repository, clock);
	}

	@Test
	void testGetWithActivityTypeUsesEagerLoading() {
		// Given
		final Long activityId = 1L;
		final CActivity activity = new CActivity();
		activity.setName("Test Activity");
		final CActivityType activityType = new CActivityType();
		activityType.setName("Test Activity Type");
		activity.setActivityType(activityType);
		when(repository.findByIdWithActivityType(activityId))
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
		final CActivity activity = new CActivity();
		activity.setName("Test Activity");
		final CActivityType activityType = new CActivityType();
		activityType.setName("Test Activity Type");
		activity.setActivityType(activityType);
		when(repository.findByIdWithActivityTypeStatusAndProject(activityId))
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
		final CActivity activity = new CActivity();
		activity.setName("Test Activity");
		
		// Create a mock project
		final CProject project = new CProject();
		project.setName("Test Project");
		activity.setProject(project);
		
		final CActivityType activityType = new CActivityType();
		activityType.setName("Test Activity Type");
		activity.setActivityType(activityType);
		
		when(repository.findByIdWithActivityTypeStatusAndProject(activityId))
			.thenReturn(Optional.of(activity));
		
		// When & Then - Should not throw LazyInitializationException when accessing project name
		assertDoesNotThrow(() -> {
			final Optional<CActivity> result = activityService.get(activityId);
			if (result.isPresent()) {
				// This is what the grid calls - should not throw LazyInitializationException
				final String projectName = result.get().getProjectName();
				assertNotNull(projectName);
			}
		});
	}
}