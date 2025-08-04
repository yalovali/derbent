package tech.derbent.activities.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.domains.CTestBase;
import tech.derbent.activities.domain.CActivityType;

@SpringBootTest
@Transactional
@Rollback
public class CActivityTypeServiceIntegrationTest extends CTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(CActivityTypeServiceIntegrationTest.class);

    @Override
    protected void setupForTest() {
        // TODO Auto-generated method stub
    }

    @AfterEach
    void tearDown() {

        if ((project != null) && (project.getId() != null)) {

            try {
                projectService.delete(project);
            } catch (final Exception e) {
                LOGGER.warn("Cleanup failed in tearDown: {}", e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Should save and retrieve activity type with all fields populated")
    void testActivityTypeWithAllFieldsPopulated() {
        CActivityType activityType = new CActivityType("Complete Test Type", project);
        activityType.setDescription("Complete description");
        activityType = activityTypeService.save(activityType);
        assertNotNull(activityType.getId());
        final var retrieved = activityTypeService.getById(activityType.getId());
        assertTrue(retrieved.isPresent());
        final CActivityType result = retrieved.get();
        assertEquals("Complete Test Type", result.getName());
        assertEquals("Complete description", result.getDescription());
        assertEquals(project.getId(), result.getProject().getId());
        activityTypeService.delete(result);
    }

    @Test
    @DisplayName("Should delete activity type and verify it no longer exists")
    void testDeleteActivityType() {
        CActivityType activityType = new CActivityType("Delete Test Type", project);
        activityType = activityTypeService.save(activityType);
        final Long activityTypeId = activityType.getId();
        activityTypeService.delete(activityType);
        final var retrieved = activityTypeService.getById(activityTypeId);
        assertFalse(retrieved.isPresent());
    }

    @Test
    @DisplayName("Should verify lazy loading with real database")
    void testLazyLoadingWithRealDatabase() {
        CActivityType activityType = new CActivityType("Integration Test Type", project);
        activityType.setDescription("Test type");
        activityType = activityTypeService.save(activityType);
        assertNotNull(activityType.getId());
        final Long activityTypeId = activityType.getId();
        final var retrievedActivityType = activityTypeService.getById(activityTypeId);
        assertTrue(retrievedActivityType.isPresent());
        final CActivityType result = retrievedActivityType.get();
        assertEquals("Integration Test Type", result.getName());
        assertEquals("Test type", result.getDescription());
        assertNotNull(result.getProject());
        assertEquals("Test project for integration tests", result.getProject().getDescription());
        assertEquals("Test Project", result.getProject().getName());
        assertEquals(project.getId(), result.getProject().getId());
        activityTypeService.delete(result);
    }

    @Test
    @DisplayName("Should create and retrieve activity types via service")
    void testServiceCanCreateAndRetrieveActivityTypes() {
        CActivityType activityType = new CActivityType("Service Test Type", project);
        activityType = activityTypeService.save(activityType);
        assertNotNull(activityType.getId());
        final var retrieved = activityTypeService.getById(activityType.getId());
        assertTrue(retrieved.isPresent());
        final CActivityType result = retrieved.get();
        assertEquals("Service Test Type", result.getName());
        assertEquals("Test Project", result.getProject().getName());
        assertEquals(project.getId(), result.getProject().getId());
        activityTypeService.delete(result);
    }
}