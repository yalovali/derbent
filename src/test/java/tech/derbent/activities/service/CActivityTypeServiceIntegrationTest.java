package tech.derbent.activities.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.activities.domain.CActivityType;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;

/**
 * Integration test for CActivityTypeService lazy loading functionality.
 * Tests the complete workflow including database interactions.
 */
@SpringBootTest
@Transactional
public class CActivityTypeServiceIntegrationTest {

    @Autowired
    private CActivityTypeService activityTypeService;

    @Autowired
    private CProjectService projectService;

    @Test
    void testLazyLoadingWithRealDatabase() {
        // Given
        CProject project = new CProject();
        project.setName("Integration Test Project");
        project.setDescription("Test project for lazy loading");
        project = projectService.save(project);

        CActivityType activityType = new CActivityType("Integration Test Type", "Test type", project);
        activityType = activityTypeService.save(activityType);

        // Clear the persistence context to ensure we're testing lazy loading
        final Long activityTypeId = activityType.getId();
        assertNotNull(activityTypeId);

        // When - Get the activity type (this should use eager loading)
        var retrievedActivityType = activityTypeService.get(activityTypeId);

        // Then
        assertTrue(retrievedActivityType.isPresent());
        CActivityType result = retrievedActivityType.get();
        
        // These accesses should not cause LazyInitializationException
        assertEquals("Integration Test Type", result.getName());
        assertEquals("Test project for lazy loading", result.getProject().getDescription());
        assertEquals("Integration Test Project", result.getProject().getName());
        
        // Clean up
        activityTypeService.delete(result);
        projectService.delete(project);
    }

    @Test
    void testServiceCanCreateAndRetrieveActivityTypes() {
        // Given
        CProject project = new CProject();
        project.setName("Service Test Project");
        project = projectService.save(project);

        // When
        CActivityType activityType = new CActivityType("Service Test Type", project);
        activityType = activityTypeService.save(activityType);

        // Then
        assertNotNull(activityType.getId());
        var retrieved = activityTypeService.get(activityType.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("Service Test Type", retrieved.get().getName());
        assertEquals("Service Test Project", retrieved.get().getProject().getName());
        
        // Clean up
        activityTypeService.delete(retrieved.get());
        projectService.delete(project);
    }
}