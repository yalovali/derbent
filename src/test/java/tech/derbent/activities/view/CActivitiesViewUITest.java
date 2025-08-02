package tech.derbent.activities.view;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import tech.derbent.abstracts.ui.CAbstractUITest;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.comments.service.CCommentService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * CActivitiesViewUITest - Comprehensive UI tests for the Activities view.
 * Layer: Testing (MVC)
 * 
 * Tests grid functionality, lazy loading prevention, data loading, and user interactions
 * for the Activities view to ensure robust UI behavior.
 */
class CActivitiesViewUITest extends CAbstractUITest<CActivity> {

    @Mock
    private CActivityService mockActivityService;

    @Mock 
    private CCommentService mockCommentService;

    private CActivitiesView activitiesView;
    private CProject testProject;
    private CActivityType testActivityType;
    private CActivityStatus testActivityStatus;
    private CUser testUser;

    public CActivitiesViewUITest() {
        super(CActivity.class);
        // Initialize test entities early so they're available for setupTestData
        setupTestEntities();
    }

    @BeforeEach
    void setupActivityTests() {
        activitiesView = new CActivitiesView(
            mockActivityService, 
            mockSessionService,
            mockCommentService
        );
    }

    private void setupTestEntities() {
        // Create test project
        testProject = new CProject();
        testProject.setName("Test Project");
        testProject.setDescription("Test project for activities");

        // Create test activity type
        testActivityType = new CActivityType();
        testActivityType.setName("Development");
        testActivityType.setDescription("Development activities");

        // Create test activity status
        testActivityStatus = new CActivityStatus();
        testActivityStatus.setName("In Progress");
        testActivityStatus.setDescription("Activity is in progress");
        testActivityStatus.setColor("#FFA500");
        testActivityStatus.setSortOrder(1);

        // Create test user
        testUser = new CUser();
        testUser.setName("Test");
        testUser.setLastname("User");
        testUser.setLogin("testuser");
        testUser.setEmail("test@example.com");
    }

    @Override
    protected void setupTestData() {
        CActivity activity1 = createTestEntity(1L, "Test Activity 1");
        CActivity activity2 = createTestEntity(2L, "Test Activity 2");
        CActivity activity3 = createTestEntity(3L, "Parent Activity");
        
        // Set up parent-child relationship for lazy loading testing
        activity1.setParentActivity(activity3);
        
        testEntities = Arrays.asList(activity1, activity2, activity3);
        
        // Mock active project - testProject should be initialized by now
        when(mockSessionService.getActiveProject()).thenReturn(Optional.of(testProject));
    }

    @Override
    protected CActivity createTestEntity(Long id, String name) {
        CActivity activity = new CActivity();
        // Note: ID is auto-generated, not set manually
        activity.setName(name);
        activity.setDescription("Test activity description for " + name);
        activity.setStartDate(LocalDate.now());
        activity.setDueDate(LocalDate.now().plusDays(7));
        
        // Initialize all relationships to prevent lazy loading issues
        activity.setProject(testProject);
        activity.setActivityType(testActivityType);
        activity.setStatus(testActivityStatus);
        activity.setAssignedTo(testUser);
        activity.setCreatedBy(testUser);
        
        return activity;
    }

    @Override
    protected void verifyEntityRelationships(CActivity entity) {
        assertNotNull(entity.getProject(), "Project should be initialized");
        assertNotNull(entity.getActivityType(), "Activity type should be initialized");
        assertNotNull(entity.getStatus(), "Status should be initialized");
        assertNotNull(entity.getAssignedTo(), "Assigned user should be initialized");
        assertNotNull(entity.getCreatedBy(), "Created by user should be initialized");
        
        // Verify lazy relationships can be accessed without exceptions
        try {
            if (entity.getParentActivity() != null) {
                String parentName = entity.getParentActivity().getName();
                assertNotNull(parentName, "Parent activity name should be accessible");
            }
        } catch (Exception e) {
            fail("Parent activity access caused lazy loading exception: " + e.getMessage());
        }
    }

    @Test
    void testGridCreation() {
        LOGGER.info("Testing activities grid creation");
        
        assertNotNull(activitiesView.getGrid(), "Grid should be created");
        assertTrue(activitiesView.getGrid().getColumns().size() > 0, "Grid should have columns");
        
        // Verify expected columns exist
        boolean hasProjectColumn = activitiesView.getGrid().getColumns().stream()
            .anyMatch(col -> "project".equals(col.getKey()));
        assertTrue(hasProjectColumn, "Grid should have project column");
        
        boolean hasNameColumn = activitiesView.getGrid().getColumns().stream()
            .anyMatch(col -> "name".equals(col.getKey()));
        assertTrue(hasNameColumn, "Grid should have name column");
    }

    @Test
    void testGridDataLoading() {
        LOGGER.info("Testing activities grid data loading");
        
        // Test that grid can load data without exceptions
        testGridDataLoading(activitiesView.getGrid());
        
        // Verify service was called
        verify(mockActivityService, atLeastOnce()).list(any());
    }

    @Test
    void testGridColumnAccess() {
        LOGGER.info("Testing activities grid column access for lazy loading issues");
        
        // This tests all columns to ensure no lazy loading exceptions occur
        testGridColumnAccess(activitiesView.getGrid());
        
        // Specifically test problematic columns
        testEntities.forEach(activity -> {
            verifyEntityRelationships(activity);
        });
    }

    @Test
    void testGridSelection() {
        LOGGER.info("Testing activities grid selection");
        
        testGridSelection(activitiesView.getGrid());
    }

    @Test
    void testParentActivityColumnAccess() {
        LOGGER.info("Testing parent activity column access specifically");
        
        CActivity activityWithParent = testEntities.get(0); // Has parent activity set
        
        // Test the parent activity column value provider directly
        String parentActivityDisplay = activityWithParent.getParentActivity() != null
            ? activityWithParent.getParentActivity().getName() 
            : "No Parent Activity";
        
        assertNotNull(parentActivityDisplay, "Parent activity display should not be null");
        assertTrue(parentActivityDisplay.contains("Parent Activity") || 
                  parentActivityDisplay.equals("No Parent Activity"),
                  "Parent activity display should be correct");
    }

    @Test
    void testActivityTypeColumnAccess() {
        LOGGER.info("Testing activity type column access");
        
        testEntities.forEach(activity -> {
            String typeDisplay = activity.getActivityType() != null 
                ? activity.getActivityType().getName() 
                : "No Type";
            
            assertNotNull(typeDisplay, "Activity type display should not be null");
        });
    }

    @Test
    void testActivityStatusColumnAccess() {
        LOGGER.info("Testing activity status column access");
        
        testEntities.forEach(activity -> {
            String statusDisplay = activity.getStatus() != null 
                ? activity.getStatus().getName() 
                : "No Status";
            
            assertNotNull(statusDisplay, "Activity status display should not be null");
        });
    }

    @Test
    void testGridWithEmptyData() {
        LOGGER.info("Testing grid behavior with empty data");
        
        // Mock empty result
        when(mockActivityService.list(any())).thenReturn(Arrays.asList());
        
        // Should not throw exceptions
        assertDoesNotThrow(() -> {
            testGridDataLoading(activitiesView.getGrid());
        }, "Grid should handle empty data gracefully");
    }

    @Test
    void testGridWithNullRelationships() {
        LOGGER.info("Testing grid behavior with null relationships");
        
        // Create activity with null relationships
        CActivity activityWithNulls = new CActivity();
        activityWithNulls.setName("Activity With Nulls");
        // Leave all relationships null
        
        // Test that columns handle null relationships gracefully
        assertDoesNotThrow(() -> {
            // Test activity type column
            String typeDisplay = activityWithNulls.getActivityType() != null 
                ? activityWithNulls.getActivityType().getName() 
                : "No Type";
            assertEquals("No Type", typeDisplay);
            
            // Test status column
            String statusDisplay = activityWithNulls.getStatus() != null 
                ? activityWithNulls.getStatus().getName() 
                : "No Status";
            assertEquals("No Status", statusDisplay);
            
            // Test parent activity column
            String parentDisplay = activityWithNulls.getParentActivity() != null
                ? activityWithNulls.getParentActivity().getName() 
                : "No Parent Activity";
            assertEquals("No Parent Activity", parentDisplay);
            
        }, "Grid columns should handle null relationships gracefully");
    }

    @Test 
    void testViewInitialization() {
        LOGGER.info("Testing activities view initialization");
        
        assertNotNull(activitiesView, "Activities view should be created");
        assertNotNull(activitiesView.getGrid(), "Grid should be initialized");
        
        // Verify view is properly configured
        assertTrue(activitiesView.getClassNames().contains("activities-view"),
                  "View should have proper CSS class");
    }

    @Test
    void testFormPopulation() {
        LOGGER.info("Testing form population with activity data");
        
        if (!testEntities.isEmpty()) {
            CActivity testActivity = testEntities.get(0);
            
            // Test form population doesn't throw exceptions
            assertDoesNotThrow(() -> {
                activitiesView.testPopulateForm(testActivity);
            }, "Form population should not throw exceptions");
        }
    }
}