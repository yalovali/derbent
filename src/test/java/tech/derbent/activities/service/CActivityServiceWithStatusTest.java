package tech.derbent.activities.service;

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

import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.domain.CActivityType;

/**
 * Test class for CActivityService with new CActivityStatus relationship.
 * Tests lazy loading functionality for both CActivityType and CActivityStatus.
 */
class CActivityServiceWithStatusTest {

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
    void testGetWithActivityTypeAndStatusUsesEagerLoading() {
        // Given
        final Long activityId = 1L;
        final CActivity activity = createTestActivity();
        when(repository.findByIdWithActivityTypeAndStatus(activityId))
                .thenReturn(Optional.of(activity));

        // When
        final Optional<CActivity> result = activityService.getWithActivityTypeAndStatus(activityId);

        // Then
        assertTrue(result.isPresent());
        assertNotNull(result.get().getActivityType());
        assertNotNull(result.get().getActivityStatus());
    }

    @Test
    void testGetOverriddenMethodUsesEagerLoading() {
        // Given
        final Long activityId = 1L;
        final CActivity activity = createTestActivity();
        when(repository.findByIdWithActivityTypeAndStatus(activityId))
                .thenReturn(Optional.of(activity));

        // When
        final Optional<CActivity> result = activityService.get(activityId);

        // Then
        assertTrue(result.isPresent());
        assertNotNull(result.get().getActivityType());
        assertNotNull(result.get().getActivityStatus());
    }

    @Test
    void testInitializeLazyFieldsWithBothRelationships() {
        // Given
        final CActivity activity = createTestActivity();

        // When/Then - should not throw exception
        assertDoesNotThrow(() -> activityService.initializeLazyFields(activity));
    }

    @Test
    void testInitializeLazyFieldsWithNullActivityType() {
        // Given
        final CActivity activity = new CActivity();
        activity.setName("Test Activity");
        final CActivityStatus status = new CActivityStatus("TODO");
        activity.setActivityStatus(status);
        // activityType is null

        // When/Then - should not throw exception
        assertDoesNotThrow(() -> activityService.initializeLazyFields(activity));
    }

    @Test
    void testInitializeLazyFieldsWithNullActivityStatus() {
        // Given
        final CActivity activity = new CActivity();
        activity.setName("Test Activity");
        final CActivityType type = new CActivityType("Development");
        activity.setActivityType(type);
        // activityStatus is null

        // When/Then - should not throw exception
        assertDoesNotThrow(() -> activityService.initializeLazyFields(activity));
    }

    @Test
    void testInitializeLazyFieldsWithNullBothRelationships() {
        // Given
        final CActivity activity = new CActivity();
        activity.setName("Test Activity");
        // Both activityType and activityStatus are null

        // When/Then - should not throw exception
        assertDoesNotThrow(() -> activityService.initializeLazyFields(activity));
    }

    @Test
    void testInitializeLazyFieldsWithNullEntity() {
        // When/Then - should not throw exception
        assertDoesNotThrow(() -> activityService.initializeLazyFields(null));
    }

    /**
     * Helper method to create a test activity with both type and status relationships.
     */
    private CActivity createTestActivity() {
        final CActivity activity = new CActivity();
        activity.setName("Test Activity");
        
        final CActivityType activityType = new CActivityType("Development");
        activity.setActivityType(activityType);
        
        final CActivityStatus activityStatus = new CActivityStatus("IN_PROGRESS");
        activity.setActivityStatus(activityStatus);
        
        return activity;
    }
}