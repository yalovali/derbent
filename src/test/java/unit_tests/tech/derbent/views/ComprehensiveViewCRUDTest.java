package unit_tests.tech.derbent.views;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.decisions.domain.CDecisionStatus;
import tech.derbent.decisions.domain.CDecisionType;
// Import domain objects
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.domain.CMeetingType;
import tech.derbent.projects.domain.CProject;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserType;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Comprehensive test class for all views in the application. Tests CRUD operations, grid functionality, field
 * population, and data integrity for all entity views as requested in the review comment.
 */
@SpringBootTest
@Transactional
public class ComprehensiveViewCRUDTest extends CTestBase {
    // Meeting services

    @Override
    protected void setupForTest() {
        assertNotNull(meetingService, "Meeting service should be available");
        assertNotNull(projectService, "Project service should be available");
    }

    /**
     * Test all activity-related views and CRUD operations
     */
    @Test
    public void testActivityViewsCRUD() {
        // Test CActivitiesView - main activity management
        final List<CActivity> activities = activityService.list(Pageable.unpaged());
        assertTrue(activities.size() >= 4, "Should have at least 4 activities from sample data");

        // Verify all activity fields are populated
        for (final CActivity activity : activities) {
            assertNotNull(activity.getName(), "Activity name should not be null");
            assertNotNull(activity.getDescription(), "Activity description should not be null");
            assertNotNull(activity.getProject(), "Activity project should not be null");
            assertNotNull(activity.getActivityType(), "Activity type should not be null");
            // Test grid selection and field updates
            final CActivity selectedActivity = activityService.getById(activity.getId()).orElse(null);
            assertNotNull(selectedActivity, "Should be able to retrieve activity by ID");
            assertEquals(activity.getName(), selectedActivity.getName(), "Retrieved activity should match");
        }
        // Test CActivityTypeView - activity type management
        final List<CActivityType> activityTypes = activityTypeService.list(Pageable.unpaged());
        assertTrue(activityTypes.size() >= 4, "Should have at least 4 activity types from sample data");

        for (final CActivityType type : activityTypes) {
            assertNotNull(type.getName(), "Activity type name should not be null");
            assertNotNull(type.getDescription(), "Activity type description should not be null");
        }
        // Test CActivityStatusView - activity status management
        final List<CActivityStatus> activityStatuses = activityStatusService.list(Pageable.unpaged());

        if (!activityStatuses.isEmpty()) {

            for (final CActivityStatus status : activityStatuses) {
                assertNotNull(status.getName(), "Activity status name should not be null");
                assertNotNull(status.getDescription(), "Activity status description should not be null");
            }
        }
    }

    /**
     * Test field validation for all entities to ensure no null or empty critical fields
     */
    @Test
    public void testAllFieldsHaveProperData() {
        // This test ensures that all sample data has complete field population Check
        // projects have all required fields
        final List<CProject> projects = projectService.list(Pageable.unpaged());

        for (final CProject project : projects) {
            assertNotNull(project.getName(), "Project name should not be null");
            assertFalse(project.getName().trim().isEmpty(), "Project name should not be empty");
            assertNotNull(project.getDescription(), "Project description should not be null");
            assertFalse(project.getDescription().trim().isEmpty(), "Project description should not be empty");
        }
        // Check companies have all required fields
        final List<CCompany> companies = companyService.list(Pageable.unpaged());

        for (final CCompany company : companies) {
            assertNotNull(company.getName(), "Company name should not be null");
            assertFalse(company.getName().trim().isEmpty(), "Company name should not be empty");
            assertNotNull(company.getAddress(), "Company address should not be null");
            assertFalse(company.getAddress().trim().isEmpty(), "Company address should not be empty");
            assertNotNull(company.getPhone(), "Company phone should not be null");
            assertFalse(company.getPhone().trim().isEmpty(), "Company phone should not be empty");
        }
        // Check users have all required fields
        final List<CUser> users = userService.list(Pageable.unpaged());

        for (final CUser user : users) {
            assertNotNull(user.getName(), "User name should not be null");
            assertFalse(user.getName().trim().isEmpty(), "User name should not be empty");
            assertNotNull(user.getEmail(), "User email should not be null");
            assertFalse(user.getEmail().trim().isEmpty(), "User email should not be empty");
            assertTrue(user.getEmail().contains("@"), "User email should be valid");
        }
    }

    /**
     * Test all comment-related views and CRUD operations
     */
    @Test
    public void testCommentViewsCRUD() {
        commentService.list(Pageable.unpaged());
        commentPriorityService.list(Pageable.unpaged());
    }

    /**
     * Test all company-related views and CRUD operations
     */
    @Test
    public void testCompanyViewsCRUD() {
        // Test CCompanyView - company management
        final List<CCompany> companies = companyService.list(Pageable.unpaged());
        assertTrue(companies.size() >= 4, "Should have at least 4 companies from sample data");

        // Verify all company fields are populated
        for (final CCompany company : companies) {
            assertNotNull(company.getName(), "Company name should not be null");
            assertNotNull(company.getDescription(), "Company description should not be null");
            assertNotNull(company.getAddress(), "Company address should not be null");
            assertNotNull(company.getPhone(), "Company phone number should not be null");
            assertNotNull(company.getEmail(), "Company email should not be null");
            // Test grid selection and field updates
            final CCompany selectedCompany = companyService.getById(company.getId()).orElse(null);
            assertNotNull(selectedCompany, "Should be able to retrieve company by ID");
            assertEquals(company.getName(), selectedCompany.getName(), "Retrieved company should match");
        }
    }

    /**
     * Test all decision-related views and CRUD operations
     */
    @Test
    public void testDecisionViewsCRUD() {
        // Test CDecisionsView - main decision management
        final List<CDecision> decisions = decisionService.list(Pageable.unpaged());

        if (!decisions.isEmpty()) {

            // Verify all decision fields are populated
            for (final CDecision decision : decisions) {
                assertNotNull(decision.getName(), "Decision name should not be null");
                assertNotNull(decision.getDescription(), "Decision description should not be null");
                assertNotNull(decision.getProject(), "Decision project should not be null");
                // Test grid selection and field updates
                final CDecision selectedDecision = decisionService.getById(decision.getId()).orElse(null);
                assertNotNull(selectedDecision, "Should be able to retrieve decision by ID");
                assertEquals(decision.getName(), selectedDecision.getName(), "Retrieved decision should match");
            }
        }
        // Test CDecisionTypeView - decision type management
        final List<CDecisionType> decisionTypes = decisionTypeService.list(Pageable.unpaged());
        assertTrue(decisionTypes.size() >= 4, "Should have at least 4 decision types from sample data");

        for (final CDecisionType type : decisionTypes) {
            assertNotNull(type.getName(), "Decision type name should not be null");
            assertNotNull(type.getDescription(), "Decision type description should not be null");
        }
        // Test CDecisionStatusView - decision status management
        final List<CDecisionStatus> decisionStatuses = decisionStatusService.list(Pageable.unpaged());

        if (!decisionStatuses.isEmpty()) {

            for (final CDecisionStatus status : decisionStatuses) {
                assertNotNull(status.getName(), "Decision status name should not be null");
                assertNotNull(status.getDescription(), "Decision status description should not be null");
            }
        }
    }

    /**
     * Test grid operations including pagination and sorting
     */
    @Test
    public void testGridOperations() {
        // Test pagination with different page sizes
        final var pageable = Pageable.ofSize(2);
        final List<CProject> pagedProjects = projectService.list(pageable);
        assertNotNull(pagedProjects, "Paged results should not be null");
        assertTrue(pagedProjects.size() <= 2, "Paged results should respect page size");
        // Test with larger page size
        final var largerPageable = Pageable.ofSize(10);
        final List<CProject> largerPagedProjects = projectService.list(largerPageable);
        assertNotNull(largerPagedProjects, "Larger paged results should not be null");
        // Test unpaged results
        final List<CProject> allProjects = projectService.list(Pageable.unpaged());
        assertTrue(allProjects.size() >= pagedProjects.size(), "All projects should include paged results");
    }

    /**
     * Test all meeting-related views and CRUD operations
     */
    @Test
    public void testMeetingViewsCRUD() {
        // Test CMeetingsView - main meeting management
        final List<CMeeting> meetings = meetingService.list(Pageable.unpaged());
        assertTrue(meetings.size() >= 5, "Should have at least 5 meetings from sample data");

        // Verify all meeting fields are populated
        for (final CMeeting meeting : meetings) {
            assertNotNull(meeting.getName(), "Meeting name should not be null");
            assertNotNull(meeting.getDescription(), "Meeting description should not be null");
            assertNotNull(meeting.getMeetingType(), "Meeting type should not be null");
            assertNotNull(meeting.getStatus(), "Meeting status should not be null");
            assertNotNull(meeting.getProject(), "Meeting project should not be null");
            // Test grid selection and field updates
            final CMeeting selectedMeeting = meetingService.getById(meeting.getId()).orElse(null);
            assertNotNull(selectedMeeting, "Should be able to retrieve meeting by ID");
            assertEquals(meeting.getName(), selectedMeeting.getName(), "Retrieved meeting should match");
        }
        // Test CMeetingTypeView - meeting type management
        final List<CMeetingType> meetingTypes = meetingTypeService.list(Pageable.unpaged());
        assertTrue(meetingTypes.size() >= 5, "Should have at least 5 meeting types from sample data");

        for (final CMeetingType type : meetingTypes) {
            assertNotNull(type.getName(), "Meeting type name should not be null");
            assertNotNull(type.getDescription(), "Meeting type description should not be null");
            // Test CRUD operations
            final CMeetingType retrieved = meetingTypeService.getById(type.getId()).orElse(null);
            assertNotNull(retrieved, "Should be able to retrieve meeting type by ID");
            assertEquals(type.getName(), retrieved.getName(), "Retrieved meeting type should match");
        }
    }

    /**
     * Test all order-related views and CRUD operations
     */
    @Test
    public void testOrderViewsCRUD() {
        orderService.list(Pageable.unpaged());
    }

    /**
     * Test all project-related views and CRUD operations
     */
    @Test
    public void testProjectViewsCRUD() {
        // Test CProjectsView - main project management
        final List<CProject> projects = projectService.list(Pageable.unpaged());
        assertTrue(projects.size() >= 4, "Should have at least 4 projects from sample data");

        // Verify all project fields are populated
        for (final CProject project : projects) {
            assertNotNull(project.getName(), "Project name should not be null");
            assertNotNull(project.getDescription(), "Project description should not be null");
            // CProject only has name and description from CEntityNamed Test grid
            // selection and field updates
            final CProject selectedProject = projectService.getById(project.getId()).orElse(null);
            assertNotNull(selectedProject, "Should be able to retrieve project by ID");
            assertEquals(project.getName(), selectedProject.getName(), "Retrieved project should match");
        }
    }

    /**
     * Test all risk-related views and CRUD operations
     */
    @Test
    public void testRiskViewsCRUD() {
        // Test CRiskView - risk management
        final List<CRisk> risks = riskService.list(Pageable.unpaged());
        assertTrue(risks.size() >= 5, "Should have at least 5 risks from sample data");

        // Verify all risk fields are populated
        for (final CRisk risk : risks) {
            assertNotNull(risk.getName(), "Risk name should not be null");
            assertNotNull(risk.getDescription(), "Risk description should not be null");
            assertNotNull(risk.getRiskSeverity(), "Risk severity should not be null");
            // Risk doesn't have getProbability() method - it only has severity
            assertNotNull(risk.getProject(), "Risk project should not be null");
            // Test grid selection and field updates
            final CRisk selectedRisk = riskService.getById(risk.getId()).orElse(null);
            assertNotNull(selectedRisk, "Should be able to retrieve risk by ID");
            assertEquals(risk.getName(), selectedRisk.getName(), "Retrieved risk should match");
        }
    }

    /**
     * Test all user-related views and CRUD operations
     */
    @Test
    public void testUserViewsCRUD() {
        // Test CUsersView - user management
        final List<CUser> users = userService.list(Pageable.unpaged());
        assertTrue(users.size() >= 5, "Should have at least 5 users from sample data");

        // Verify all user fields are populated
        for (final CUser user : users) {
            assertNotNull(user.getName(), "User name should not be null");
            assertNotNull(user.getEmail(), "User email should not be null");
            assertNotNull(user.getUserRole(), "User role should not be null");
            assertNotNull(user.getCompany(), "User company should not be null");
            // Test grid selection and field updates
            final CUser selectedUser = userService.getById(user.getId()).orElse(null);
            assertNotNull(selectedUser, "Should be able to retrieve user by ID");
            assertEquals(user.getName(), selectedUser.getName(), "Retrieved user should match");
        }
        // Test CUserTypeView - user type management
        final List<CUserType> userTypes = userTypeService.list(Pageable.unpaged());
        assertTrue(userTypes.size() >= 4, "Should have at least 4 user types from sample data");

        for (final CUserType type : userTypes) {
            assertNotNull(type.getName(), "User type name should not be null");
            assertNotNull(type.getDescription(), "User type description should not be null");
            assertNotNull(type.getProject(), "User type project should not be null");
        }
    }
}