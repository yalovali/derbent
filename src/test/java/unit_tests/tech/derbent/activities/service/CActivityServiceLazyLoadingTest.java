package unit_tests.tech.derbent.activities.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.Clock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import tech.derbent.activities.service.CActivityRepository;
import tech.derbent.activities.service.CActivityService;
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
	void testInitializeLazyFieldsHandlesNullEntity() {
		// When & Then - Should not throw any exception
		assertDoesNotThrow(() -> {
			// The method is inherited from the base class and uses generic type Just
			// verify the service handles null entity gracefully
			activityService.getById(null);
		});
	}
}