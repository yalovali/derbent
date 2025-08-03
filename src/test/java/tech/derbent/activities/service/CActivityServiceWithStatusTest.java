package tech.derbent.activities.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import tech.derbent.abstracts.domains.CTestBase;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.domain.CActivityType;

/**
 * Test class for CActivityService with new CActivityStatus relationship. Tests lazy loading functionality for both
 * CActivityType and CActivityStatus.
 */
class CActivityServiceWithStatusTest extends CTestBase {

    /**
     * Helper method to create a test activity with both type and status relationships.
     */
    private CActivity createTestActivity() {
        final CActivity activity = new CActivity("Test Activity", project);
        project.setName("Test Project");
        final CActivityType activityType = new CActivityType("Development", project);
        activity.setActivityType(activityType);
        final CActivityStatus status = new CActivityStatus("IN_PROGRESS", project);
        activity.setStatus(status);
        return activity;
    }

    @Override
    protected void setupForTest() {
        // TODO Auto-generated method stub
    }

    @Test
    void testGetOverriddenMethodUsesEagerLoading() {
        // Given
        final Long activityId = 1L;
        final CActivity activity = createTestActivity();
        when(activityRepository.findByIdWithAllRelationships(activityId)).thenReturn(Optional.of(activity));
        // When
        final Optional<CActivity> result = activityService.getById(activityId);
        // Then
        assertTrue(result.isPresent());
        assertNotNull(result.get().getActivityType());
        assertNotNull(result.get().getStatus());
    }

    @Test
    void testGetWithActivityTypeAndStatusUsesEagerLoading() {
        // Given
        final Long activityId = 1L;
        final CActivity activity = createTestActivity();
        when(activityRepository.findByIdWithActivityTypeAndStatus(activityId)).thenReturn(Optional.of(activity));
        // When
        final Optional<CActivity> result = activityService.getWithActivityTypeAndStatus(activityId);
        // Then
        assertTrue(result.isPresent());
        assertNotNull(result.get().getActivityType());
        assertNotNull(result.get().getStatus());
    }

    @Test
    void testInitializeLazyFieldsWithBothRelationships() {
        // Given
        final CActivity activity = createTestActivity();
        // When/Then - should not throw exception
        assertDoesNotThrow(() -> activityService.initializeLazyFields(activity));
    }

    @Test
    void testInitializeLazyFieldsWithNullActivityStatus() {
        // Given Create test project for the type
        project.setName("Test Project");
        final CActivity activity = new CActivity("Test Activity", project);
        final CActivityType type = new CActivityType("Development", project);
        activity.setActivityType(type);
        // activityStatus is null When/Then - should not throw exception
        assertDoesNotThrow(() -> activityService.initializeLazyFields(activity));
    }

    @Test
    void testInitializeLazyFieldsWithNullActivityType() {
        // Given
        final CActivity activity = new CActivity("Test Activity", project);
        activity.setName("Test Activity");
        final CActivityStatus status = new CActivityStatus("TODO", project);
        activity.setStatus(status);
        // activityType is null When/Then - should not throw exception
        assertDoesNotThrow(() -> activityService.initializeLazyFields(activity));
    }

    @Test
    void testInitializeLazyFieldsWithNullBothRelationships() {
        // Given
        final CActivity activity = new CActivity("Test Activity", project);
        activity.setName("Test Activity");
        // Both activityType and activityStatus are null When/Then - should not throw
        // exception
        assertDoesNotThrow(() -> activityService.initializeLazyFields(activity));
    }

    @Test
    void testInitializeLazyFieldsWithNullEntity() {
        // When/Then - should not throw exception
        assertDoesNotThrow(() -> activityService.initializeLazyFields(null));
    }
}