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

import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.projects.domain.CProject;

/**
 * Test class for CActivityStatusService lazy loading functionality. Tests the enhanced lazy loading fixes for
 * activity status entities.
 */
class CActivityStatusServiceLazyLoadingTest {

    @Mock
    private CActivityStatusRepository activityStatusRepository;
    
    @Mock
    private Clock clock;
    
    private CActivityStatusService activityStatusService;
    private CProject project;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        activityStatusService = new CActivityStatusService(activityStatusRepository, clock);
        project = new CProject("Test Project");
    }

    @Test
    void testOverriddenGetMethodUsesEagerLoading() {
        // Given
        final Long statusId = 1L;
        final CActivityStatus activityStatus = new CActivityStatus("Test Status", project);
        when(activityStatusRepository.findByIdWithProject(statusId)).thenReturn(Optional.of(activityStatus));
        
        // When & Then - Should not throw LazyInitializationException
        assertDoesNotThrow(() -> {
            final Optional<CActivityStatus> result = activityStatusService.get(statusId);
            
            assertTrue(result.isPresent());
            assertNotNull(result.get().getName());
            // Access project relationship that could cause lazy loading issues
            if (result.get().getProject() != null) {
                result.get().getProject().getName();
            }
        });
    }

    @Test
    void testInitializeLazyFieldsHandlesNullEntity() {
        // When & Then - Should not throw any exception
        assertDoesNotThrow(() -> {
            // The method is inherited from the base class and uses generic type
            // Just verify the service handles null entity gracefully
            activityStatusService.get(null);
        });
    }

    @Test
    void testGetWithNullIdReturnsEmpty() {
        // When
        final Optional<CActivityStatus> result = activityStatusService.get(null);
        
        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testProjectAccessAfterGet() {
        // Given
        final Long statusId = 1L;
        final CActivityStatus activityStatus = new CActivityStatus("Test Status", project);
        when(activityStatusRepository.findByIdWithProject(statusId)).thenReturn(Optional.of(activityStatus));
        
        // When & Then - Should not throw LazyInitializationException when accessing project
        assertDoesNotThrow(() -> {
            final Optional<CActivityStatus> result = activityStatusService.get(statusId);
            
            if (result.isPresent() && result.get().getProject() != null) {
                // This is what the UI might access - should not throw LazyInitializationException
                final String projectName = result.get().getProject().getName();
                assertNotNull(projectName);
            }
        });
    }
}