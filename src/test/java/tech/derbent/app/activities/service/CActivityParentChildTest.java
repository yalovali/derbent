package tech.derbent.app.activities.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.domain.CActivityPriority;
import tech.derbent.app.activities.domain.CActivityType;
import tech.derbent.base.session.service.ISessionService;

/** Test class for verifying Activity parent-child relationship functionality.
 * Tests parent assignment, validation, and hierarchy checks. */
@DisplayName("Activity Parent-Child Relationship Tests")
class CActivityParentChildTest {

    @Mock
    private IActivityRepository repository;
    @Mock
    private Clock clock;
    @Mock
    private ISessionService sessionService;
    @Mock
    private CActivityTypeService activityTypeService;
    @Mock
    private CProjectItemStatusService projectItemStatusService;
    @Mock
    private CActivityPriorityService activityPriorityService;
    private CActivityService activityService;
    private CCompany company;
    private CProject project;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        activityService = new CActivityService(repository, clock, sessionService, activityTypeService, projectItemStatusService,
                activityPriorityService);
        // Setup company and project
        company = new CCompany("Test Company");
        company.setId(1L);
        project = new CProject("Test Project", company);
        project.setId(1L);
        // Mock session service
        when(sessionService.getActiveProject()).thenReturn(java.util.Optional.of(project));
        when(sessionService.getActiveCompany()).thenReturn(java.util.Optional.of(company));
    }

    @Test
    @DisplayName("Activity can be assigned a parent")
    void testAssignParent() {
        // Given: Two activities
        final CActivity parent = new CActivity("Parent Activity", project);
        parent.setId(1L);
        final CActivity child = new CActivity("Child Activity", project);
        child.setId(2L);
        // When: Child is assigned parent
        child.setParent(parent);
        // Then: Parent relationship is established
        assertTrue(child.hasParent(), "Child should have parent");
        assertEquals(parent.getId(), child.getParentId(), "Parent ID should match");
        assertEquals("CActivity", child.getParentType(), "Parent type should be CActivity");
    }

    @Test
    @DisplayName("Activity parent can be cleared")
    void testClearParent() {
        // Given: Activity with parent
        final CActivity parent = new CActivity("Parent Activity", project);
        parent.setId(1L);
        final CActivity child = new CActivity("Child Activity", project);
        child.setId(2L);
        child.setParent(parent);
        assertTrue(child.hasParent(), "Child should have parent initially");
        // When: Parent is cleared
        child.clearParent();
        // Then: Parent relationship is removed
        assertFalse(child.hasParent(), "Child should not have parent after clearing");
    }

    @Test
    @DisplayName("Activity cannot be its own parent")
    void testSelfParentPrevention() {
        // Given: An activity
        final CActivity activity = new CActivity("Activity", project);
        activity.setId(1L);
        // When/Then: Attempting to set self as parent throws exception
        assertThrows(IllegalArgumentException.class, () -> activity.setParent(activity), "Should throw exception for self-parent");
    }

    @Test
    @DisplayName("Parent must be persisted (have ID)")
    void testParentMustBePersisted() {
        // Given: A persisted child and unpersisted parent
        final CActivity parent = new CActivity("Parent Activity", project);
        // parent.setId(null); // Not persisted
        final CActivity child = new CActivity("Child Activity", project);
        child.setId(2L);
        // When/Then: Attempting to set unpersisted parent throws exception
        assertThrows(IllegalArgumentException.class, () -> child.setParent(parent), "Should throw exception for unpersisted parent");
    }

    @Test
    @DisplayName("Activity hasParent returns correct value")
    void testHasParent() {
        // Given: Activities with and without parents
        final CActivity parent = new CActivity("Parent Activity", project);
        parent.setId(1L);
        final CActivity withParent = new CActivity("With Parent", project);
        withParent.setId(2L);
        withParent.setParent(parent);
        final CActivity withoutParent = new CActivity("Without Parent", project);
        withoutParent.setId(3L);
        // Then: hasParent returns correct values
        assertTrue(withParent.hasParent(), "Activity with parent should return true");
        assertFalse(withoutParent.hasParent(), "Activity without parent should return false");
    }

    @Test
    @DisplayName("Multi-level hierarchy can be created")
    void testMultiLevelHierarchy() {
        // Given: Activities for 3-level hierarchy
        final CActivity level1 = new CActivity("Level 1", project);
        level1.setId(1L);
        final CActivity level2 = new CActivity("Level 2", project);
        level2.setId(2L);
        final CActivity level3 = new CActivity("Level 3", project);
        level3.setId(3L);
        // When: Creating hierarchy
        level2.setParent(level1);
        level3.setParent(level2);
        // Then: All relationships are correct
        assertFalse(level1.hasParent(), "Top level should have no parent");
        assertTrue(level2.hasParent(), "Level 2 should have parent");
        assertEquals(level1.getId(), level2.getParentId(), "Level 2 parent should be Level 1");
        assertTrue(level3.hasParent(), "Level 3 should have parent");
        assertEquals(level2.getId(), level3.getParentId(), "Level 3 parent should be Level 2");
    }

    @Test
    @DisplayName("Parent can be changed")
    void testChangeParent() {
        // Given: Activity with parent
        final CActivity parent1 = new CActivity("Parent 1", project);
        parent1.setId(1L);
        final CActivity parent2 = new CActivity("Parent 2", project);
        parent2.setId(2L);
        final CActivity child = new CActivity("Child", project);
        child.setId(3L);
        child.setParent(parent1);
        assertEquals(parent1.getId(), child.getParentId(), "Initial parent should be Parent 1");
        // When: Parent is changed
        child.setParent(parent2);
        // Then: New parent is set
        assertEquals(parent2.getId(), child.getParentId(), "Parent should be changed to Parent 2");
    }

    @Test
    @DisplayName("Setting null parent clears parent relationship")
    void testSetNullParent() {
        // Given: Activity with parent
        final CActivity parent = new CActivity("Parent", project);
        parent.setId(1L);
        final CActivity child = new CActivity("Child", project);
        child.setId(2L);
        child.setParent(parent);
        assertTrue(child.hasParent(), "Child should have parent initially");
        // When: Setting null parent
        child.setParent(null);
        // Then: Parent is cleared
        assertFalse(child.hasParent(), "Parent should be cleared when set to null");
    }
}
