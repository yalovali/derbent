package tech.derbent.activities.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import tech.derbent.abstracts.domains.CTestBase;
import tech.derbent.activities.domain.CActivityType;

/**
 * Tests for CActivityTypeService lazy loading functionality. Verifies that the service properly loads related entities
 * to prevent LazyInitializationException.
 */
public class CActivityTypeServiceLazyLoadingTest extends CTestBase {

    @Override
    protected void setupForTest() {
        // TODO Auto-generated method stub
    }

    @Test
    void testGetHandlesNullId() {
        // When
        final Optional<CActivityType> result = activityTypeService.get(null);
        // Then
        assertFalse(result.isPresent());
        verify(activityTypeRepository, never()).findByIdWithRelationships(any());
    }

    @Test
    void testGetUsesEagerLoadingQuery() {
        // Given
        final Long activityTypeId = 1L;
        project.setName("Test Project");
        final CActivityType activityType = new CActivityType("Test Activity Type", project);
        // Note: ID is typically set by JPA, so we'll mock the return directly
        when(activityTypeRepository.findByIdWithRelationships(activityTypeId)).thenReturn(Optional.of(activityType));
        // When
        final Optional<CActivityType> result = activityTypeService.get(activityTypeId);
        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Activity Type", result.get().getName());
        assertEquals("Test Project", result.get().getProject().getName());
        // Verify that the eager loading method was called
        verify(activityTypeRepository).findByIdWithRelationships(activityTypeId);
        verify(activityTypeRepository, never()).findById(activityTypeId);
    }

    @Test
    void testGetWithNonExistentId() {
        // Given
        final Long nonExistentId = 999L;
        when(activityTypeRepository.findByIdWithRelationships(nonExistentId)).thenReturn(Optional.empty());
        // When
        final Optional<CActivityType> result = activityTypeService.get(nonExistentId);
        // Then
        assertFalse(result.isPresent());
        verify(activityTypeRepository).findByIdWithRelationships(nonExistentId);
    }
}