package tech.derbent.activities.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import tech.derbent.activities.domain.CActivityStatus;

/**
 * Test class for CActivityStatusService functionality.
 * Tests CRUD operations, error handling, and lazy loading concerns.
 */
class CActivityStatusServiceTest {

    @Mock
    private CActivityStatusRepository repository;
    
    @Mock
    private Clock clock;
    
    private CActivityStatusService activityStatusService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        activityStatusService = new CActivityStatusService(repository, clock);
    }

    @Test
    void testConstructor() {
        // When/Then - should not throw exception
        assertDoesNotThrow(() -> new CActivityStatusService(repository, clock));
    }

    @Test
    void testCreateEntityWithName() {
        // Given
        final String name = "TODO";
        final CActivityStatus expectedStatus = new CActivityStatus(name);
        when(repository.saveAndFlush(any(CActivityStatus.class))).thenReturn(expectedStatus);

        // When/Then - should not throw exception
        assertDoesNotThrow(() -> activityStatusService.createEntity(name));
        
        // Verify repository interaction
        verify(repository).saveAndFlush(any(CActivityStatus.class));
    }

    @Test
    void testCreateEntityWithNameAndDescription() {
        // Given
        final String name = "IN_PROGRESS";
        final String description = "Task is currently being worked on";
        final CActivityStatus expectedStatus = new CActivityStatus(name, description);
        when(repository.saveAndFlush(any(CActivityStatus.class))).thenReturn(expectedStatus);

        // When/Then - should not throw exception
        assertDoesNotThrow(() -> activityStatusService.createEntity(name, description));
        
        // Verify repository interaction
        verify(repository).saveAndFlush(any(CActivityStatus.class));
    }

    @Test
    void testCreateEntityWithNullName() {
        // When/Then - should throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, 
                    () -> activityStatusService.createEntity(null));
    }

    @Test
    void testCreateEntityWithEmptyName() {
        // When/Then - should throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, 
                    () -> activityStatusService.createEntity(""));
    }

    @Test
    void testCreateEntityWithBlankName() {
        // When/Then - should throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, 
                    () -> activityStatusService.createEntity("   "));
    }

    @Test
    void testCreateEntityWithFailName() {
        // When/Then - should throw RuntimeException for testing error handling
        assertThrows(RuntimeException.class, 
                    () -> activityStatusService.createEntity("fail"));
    }

    @Test
    void testGetWithValidId() {
        // Given
        final Long statusId = 1L;
        final CActivityStatus status = new CActivityStatus("DONE", "Task is completed");
        when(repository.findById(statusId)).thenReturn(Optional.of(status));

        // When
        final Optional<CActivityStatus> result = activityStatusService.get(statusId);

        // Then
        assertTrue(result.isPresent());
        assertEquals("DONE", result.get().getName());
        assertEquals("Task is completed", result.get().getDescription());
    }

    @Test
    void testGetWithNullId() {
        // When
        final Optional<CActivityStatus> result = activityStatusService.get(null);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetWithNonExistentId() {
        // Given
        final Long statusId = 999L;
        when(repository.findById(statusId)).thenReturn(Optional.empty());

        // When
        final Optional<CActivityStatus> result = activityStatusService.get(statusId);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testInitializeLazyFieldsWithValidEntity() {
        // Given
        final CActivityStatus status = new CActivityStatus("REVIEW", "Task is waiting for review");

        // When/Then - should not throw exception
        assertDoesNotThrow(() -> activityStatusService.initializeLazyFields(status));
    }

    @Test
    void testInitializeLazyFieldsWithNullEntity() {
        // When/Then - should not throw exception
        assertDoesNotThrow(() -> activityStatusService.initializeLazyFields(null));
    }
}