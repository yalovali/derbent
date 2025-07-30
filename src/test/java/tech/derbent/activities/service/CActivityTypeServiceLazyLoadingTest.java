package tech.derbent.activities.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import tech.derbent.activities.domain.CActivityType;
import tech.derbent.projects.domain.CProject;

/**
 * Tests for CActivityTypeService lazy loading functionality.
 * Verifies that the service properly loads related entities to prevent LazyInitializationException.
 */
public class CActivityTypeServiceLazyLoadingTest {

    @Mock
    private CActivityTypeRepository repository;

    @Mock
    private Clock clock;

    private CActivityTypeService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new CActivityTypeService(repository, clock);
    }

    @Test
    void testGetUsesEagerLoadingQuery() {
        // Given
        final Long activityTypeId = 1L;
        final CProject project = new CProject();
        project.setName("Test Project");
        final CActivityType activityType = new CActivityType("Test Activity Type", "Description", project);
        // Note: ID is typically set by JPA, so we'll mock the return directly

        when(repository.findByIdWithRelationships(activityTypeId))
            .thenReturn(Optional.of(activityType));

        // When
        Optional<CActivityType> result = service.get(activityTypeId);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Activity Type", result.get().getName());
        assertEquals("Test Project", result.get().getProject().getName());
        
        // Verify that the eager loading method was called
        verify(repository).findByIdWithRelationships(activityTypeId);
        verify(repository, never()).findById(activityTypeId);
    }

    @Test
    void testGetWithNonExistentId() {
        // Given
        final Long nonExistentId = 999L;
        when(repository.findByIdWithRelationships(nonExistentId))
            .thenReturn(Optional.empty());

        // When
        Optional<CActivityType> result = service.get(nonExistentId);

        // Then
        assertFalse(result.isPresent());
        verify(repository).findByIdWithRelationships(nonExistentId);
    }

    @Test
    void testGetHandlesNullId() {
        // When
        Optional<CActivityType> result = service.get(null);

        // Then
        assertFalse(result.isPresent());
        verify(repository, never()).findByIdWithRelationships(any());
    }

    @Test
    void testCreateNewEntityInstance() {
        // When
        CActivityType entity = service.createNewEntityInstance();

        // Then
        assertNotNull(entity);
        assertTrue(entity instanceof CActivityType);
    }
}