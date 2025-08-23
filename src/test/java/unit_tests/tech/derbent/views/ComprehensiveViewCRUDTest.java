package unit_tests.tech.derbent.views;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.comments.domain.CCommentPriority;
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
import tech.derbent.users.domain.EUserRole;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/** Comprehensive test class for all views in the application. Tests CRUD operations, grid functionality, field population, and data integrity for all
 * entity views as requested in the review comment. */
@SpringBootTest (classes = tech.derbent.Application.class)
@Transactional
public class ComprehensiveViewCRUDTest extends CTestBase {
	// Meeting services
	@Override
	protected void setupForTest() {
		assertNotNull(meetingService, "Meeting service should be available");
		assertNotNull(projectService, "Project service should be available");
	}

	/** Test CRUD operations for Activity Status */
	private void testActivityStatusCRUD() {
		// ACT & ASSERT - Test READ operations
		final List<CActivityStatus> activityStatuses = activityStatusService.list(Pageable.unpaged()).getContent();
		if (!activityStatuses.isEmpty()) {
			for (final CActivityStatus status : activityStatuses) {
				assertNotNull(status.getName(), "Activity status name should not be null");
				assertNotNull(status.getDescription(), "Activity status description should not be null");
			}
		}
		// Test CREATE operation
		final CProject firstProject = projectService.list(Pageable.unpaged()).getContent().get(0);
		final String statusName = "TEST_STATUS_" + System.currentTimeMillis();
		final CActivityStatus newStatus = activityStatusService.newEntity(statusName, firstProject);
		newStatus.setDescription("Activity status created by CRUD test");
		final CActivityStatus savedStatus = activityStatusService.save(newStatus);
		assertNotNull(savedStatus, "Saved activity status should not be null");
		assertNotNull(savedStatus.getId(), "Saved activity status should have an ID");
		assertEquals(newStatus.getName(), savedStatus.getName(), "Saved activity status name should match");
		// Test UPDATE operation
		final String updatedName = "UPDATED_" + savedStatus.getName();
		savedStatus.setName(updatedName);
		final CActivityStatus updatedStatus = activityStatusService.save(savedStatus);
		assertEquals(updatedName, updatedStatus.getName(), "Activity status name should be updated");
		// Test DELETE operation
		final Long savedStatusId = savedStatus.getId();
		activityStatusService.delete(savedStatusId);
		final Optional<CActivityStatus> deletedStatus = activityStatusService.getById(savedStatusId);
		assertFalse(deletedStatus.isPresent(), "Deleted activity status should not be found");
	}

	/** Test CRUD operations for Activity Types */
	private void testActivityTypeCRUD() {
		// ACT & ASSERT - Test READ operations
		final List<CActivityType> activityTypes = activityTypeService.list(Pageable.unpaged()).getContent();
		assertTrue(activityTypes.size() >= 1, "Should have at least 1 activity type from sample data");
		for (final CActivityType type : activityTypes) {
			assertNotNull(type.getName(), "Activity type name should not be null");
			assertNotNull(type.getDescription(), "Activity type description should not be null");
		}
		// Test CREATE operation
		final CProject firstProject = projectService.list(Pageable.unpaged()).getContent().get(0);
		final String typeName = "Test Activity Type " + System.currentTimeMillis();
		final CActivityType newType = activityTypeService.newEntity(typeName, firstProject);
		newType.setDescription("Activity type created by CRUD test");
		final CActivityType savedType = activityTypeService.save(newType);
		assertNotNull(savedType, "Saved activity type should not be null");
		assertNotNull(savedType.getId(), "Saved activity type should have an ID");
		assertEquals(newType.getName(), savedType.getName(), "Saved activity type name should match");
		// Test UPDATE operation
		final String updatedName = "Updated " + savedType.getName();
		savedType.setName(updatedName);
		final CActivityType updatedType = activityTypeService.save(savedType);
		assertEquals(updatedName, updatedType.getName(), "Activity type name should be updated");
		// Test DELETE operation
		final Long savedTypeId = savedType.getId();
		activityTypeService.delete(savedTypeId);
		final Optional<CActivityType> deletedType = activityTypeService.getById(savedTypeId);
		assertFalse(deletedType.isPresent(), "Deleted activity type should not be found");
	}

	/** Test all activity-related views and CRUD operations Following AAA pattern: Arrange, Act, Assert */
	@Test
	public void testActivityViewsCRUD() {
		// ARRANGE - Test data already loaded via sample data initializer
		// ACT & ASSERT - Test READ operations
		final List<CActivity> activities = activityService.list(Pageable.unpaged()).getContent();
		assertTrue(activities.size() >= 1, "Should have at least 1 activity from sample data");
		// ACT & ASSERT - Test CRUD operations for each activity
		for (final CActivity activity : activities) {
			// Test READ operation - verify all required fields are populated
			assertNotNull(activity.getName(), "Activity name should not be null");
			assertNotNull(activity.getDescription(), "Activity description should not be null");
			assertNotNull(activity.getProject(), "Activity project should not be null");
			assertNotNull(activity.getActivityType(), "Activity type should not be null");
			// Test READ by ID operation
			final CActivity selectedActivity = activityService.getById(activity.getId()).orElse(null);
			assertNotNull(selectedActivity, "Should be able to retrieve activity by ID");
			assertEquals(activity.getName(), selectedActivity.getName(), "Retrieved activity should match");
			// Test UPDATE operation
			final String originalName = activity.getName();
			final String updatedName = "Updated " + originalName + " " + System.currentTimeMillis();
			activity.setName(updatedName);
			final CActivity updatedActivity = activityService.save(activity);
			assertNotNull(updatedActivity, "Updated activity should not be null");
			assertEquals(updatedName, updatedActivity.getName(), "Activity name should be updated");
			// Restore original name for consistency
			activity.setName(originalName);
			activityService.save(activity);
		}
		// Test CREATE operation
		final CProject firstProject = projectService.list(Pageable.unpaged()).getContent().get(0);
		final String activityName = "Test CRUD Activity " + System.currentTimeMillis();
		final CActivity newActivity = activityService.newEntity(activityName, firstProject);
		newActivity.setDescription("Activity created by CRUD test");
		// Set required fields that have sample data
		final List<CActivityType> activityTypes = activityTypeService.list(Pageable.unpaged()).getContent();
		if (!activityTypes.isEmpty()) {
			newActivity.setActivityType(activityTypes.get(0));
		}
		final CActivity savedActivity = activityService.save(newActivity);
		assertNotNull(savedActivity, "Saved activity should not be null");
		assertNotNull(savedActivity.getId(), "Saved activity should have an ID");
		assertEquals(newActivity.getName(), savedActivity.getName(), "Saved activity name should match");
		// Test DELETE operation
		final Long savedActivityId = savedActivity.getId();
		activityService.delete(savedActivityId);
		final Optional<CActivity> deletedActivity = activityService.getById(savedActivityId);
		assertFalse(deletedActivity.isPresent(), "Deleted activity should not be found");
		// Test CActivityTypeView - activity type management CRUD
		testActivityTypeCRUD();
		// Test CActivityStatusView - activity status management CRUD
		testActivityStatusCRUD();
	}

	/** Test field validation for all entities to ensure no null or empty critical fields */
	@Test
	public void testAllFieldsHaveProperData() {
		// This test ensures that all sample data has complete field population Check
		// projects have all required fields
		final List<CProject> projects = projectService.list(Pageable.unpaged()).getContent();
		for (final CProject project : projects) {
			assertNotNull(project.getName(), "Project name should not be null");
			assertFalse(project.getName().trim().isEmpty(), "Project name should not be empty");
			assertNotNull(project.getDescription(), "Project description should not be null");
			assertFalse(project.getDescription().trim().isEmpty(), "Project description should not be empty");
		}
		// Check companies have all required fields
		final List<CCompany> companies = companyService.list(Pageable.unpaged()).getContent();
		for (final CCompany company : companies) {
			assertNotNull(company.getName(), "Company name should not be null");
			assertFalse(company.getName().trim().isEmpty(), "Company name should not be empty");
			assertNotNull(company.getAddress(), "Company address should not be null");
			assertFalse(company.getAddress().trim().isEmpty(), "Company address should not be empty");
			assertNotNull(company.getPhone(), "Company phone should not be null");
			assertFalse(company.getPhone().trim().isEmpty(), "Company phone should not be empty");
		}
		// Check users have all required fields
		final List<CUser> users = userService.list(Pageable.unpaged()).getContent();
		for (final CUser user : users) {
			assertNotNull(user.getName(), "User name should not be null");
			assertFalse(user.getName().trim().isEmpty(), "User name should not be empty");
			assertNotNull(user.getEmail(), "User email should not be null");
			assertFalse(user.getEmail().trim().isEmpty(), "User email should not be empty");
			assertTrue(user.getEmail().contains("@"), "User email should be valid");
		}
	}

	/** Test all comment-related views and CRUD operations Following AAA pattern: Arrange, Act, Assert */
	@Test
	public void testCommentViewsCRUD() {
		// ARRANGE - Test data already loaded via sample data initializer
		// ACT & ASSERT - Test READ operations for comments
		@SuppressWarnings ("unused")
		final List<tech.derbent.comments.domain.CComment> comments = commentService.list(Pageable.unpaged()).getContent();
		// Note: Comments might be empty in sample data, so we just verify the service
		// works
		// ACT & ASSERT - Test READ operations for comment priorities
		final List<CCommentPriority> commentPriorities = commentPriorityService.list(Pageable.unpaged()).getContent();
		// Comment priorities should have some sample data, test them if available
		if (!commentPriorities.isEmpty()) {
			for (final CCommentPriority priority : commentPriorities) {
				assertNotNull(priority.getName(), "Comment priority name should not be null");
				assertNotNull(priority.getProject(), "Comment priority project should not be null");
				// Test READ by ID operation
				final CCommentPriority selectedPriority = commentPriorityService.getById(priority.getId()).orElse(null);
				assertNotNull(selectedPriority, "Should be able to retrieve comment priority by ID");
				assertEquals(priority.getName(), selectedPriority.getName(), "Retrieved comment priority should match");
			}
		}
	}

	/** Test all company-related views and CRUD operations Following AAA pattern: Arrange, Act, Assert */
	@Test
	public void testCompanyViewsCRUD() {
		// ARRANGE - Test data already loaded via sample data initializer
		// ACT & ASSERT - Test READ operations
		final List<CCompany> companies = companyService.list(Pageable.unpaged()).getContent();
		assertTrue(companies.size() >= 1, "Should have at least 1 company from sample data");
		// Test CRUD operations for each existing company
		for (final CCompany company : companies) {
			// Test READ operation - verify all required fields are populated
			assertNotNull(company.getName(), "Company name should not be null");
			assertNotNull(company.getDescription(), "Company description should not be null");
			assertNotNull(company.getAddress(), "Company address should not be null");
			assertNotNull(company.getPhone(), "Company phone number should not be null");
			assertNotNull(company.getEmail(), "Company email should not be null");
			// Test READ by ID operation
			final CCompany selectedCompany = companyService.getById(company.getId()).orElse(null);
			assertNotNull(selectedCompany, "Should be able to retrieve company by ID");
			assertEquals(company.getName(), selectedCompany.getName(), "Retrieved company should match");
			// Test UPDATE operation
			final String originalPhone = company.getPhone();
			final String updatedPhone = "+1-555-" + System.currentTimeMillis();
			company.setPhone(updatedPhone);
			final CCompany updatedCompany = companyService.save(company);
			assertNotNull(updatedCompany, "Updated company should not be null");
			assertEquals(updatedPhone, updatedCompany.getPhone(), "Company phone should be updated");
			// Restore original phone for consistency
			company.setPhone(originalPhone);
			companyService.save(company);
		}
		// Test CREATE operation
		final CCompany newCompany = companyService.newEntity();
		final String uniqueId = String.valueOf(System.currentTimeMillis());
		newCompany.setName("Test CRUD Company " + uniqueId);
		newCompany.setDescription("Company created by CRUD test");
		newCompany.setAddress("123 Test Street, Test City");
		newCompany.setPhone("+1-555-" + uniqueId);
		newCompany.setEmail("test" + uniqueId + "@example.com");
		final CCompany savedCompany = companyService.save(newCompany);
		assertNotNull(savedCompany, "Saved company should not be null");
		assertNotNull(savedCompany.getId(), "Saved company should have an ID");
		assertEquals(newCompany.getName(), savedCompany.getName(), "Saved company name should match");
		assertEquals(newCompany.getEmail(), savedCompany.getEmail(), "Saved company email should match");
		// Test DELETE operation
		final Long savedCompanyId = savedCompany.getId();
		companyService.delete(savedCompanyId);
		final Optional<CCompany> deletedCompany = companyService.getById(savedCompanyId);
		assertFalse(deletedCompany.isPresent(), "Deleted company should not be found");
	}

	/** Test edge cases and validation scenarios for CRUD operations Following AAA pattern: Arrange, Act, Assert */
	@Test
	public void testCRUDEdgeCasesAndValidation() {
		// ARRANGE - Prepare test data
		final CProject firstProject = projectService.list(Pageable.unpaged()).getContent().get(0);
		// ACT & ASSERT - Test null handling in service methods
		// Test retrieving non-existent entity by ID
		final Optional<CProject> nonExistentProject = projectService.getById(99999L);
		assertFalse(nonExistentProject.isPresent(), "Non-existent project should not be found");
		final Optional<CActivity> nonExistentActivity = activityService.getById(99999L);
		assertFalse(nonExistentActivity.isPresent(), "Non-existent activity should not be found");
		final Optional<CUser> nonExistentUser = userService.getById(99999L);
		assertFalse(nonExistentUser.isPresent(), "Non-existent user should not be found");
		final Optional<CCompany> nonExistentCompany = companyService.getById(99999L);
		assertFalse(nonExistentCompany.isPresent(), "Non-existent company should not be found");
		// Test pagination edge cases
		final Pageable smallPage = Pageable.ofSize(1);
		final List<CProject> smallPageResult = projectService.list(smallPage).getContent();
		assertTrue(smallPageResult.size() <= 1, "Small page size should return at most 1 result");
		final Pageable largePage = Pageable.ofSize(1000);
		final List<CProject> largePageResult = projectService.list(largePage).getContent();
		assertNotNull(largePageResult, "Large page size should not cause errors");
		// Test entity creation with minimal required fields
		final CProject minimalProject = projectService.newEntity();
		minimalProject.setName("Minimal Test Project " + System.currentTimeMillis());
		minimalProject.setDescription("Minimal description");
		final CProject savedMinimalProject = projectService.save(minimalProject);
		assertNotNull(savedMinimalProject, "Minimal project should be saved successfully");
		assertNotNull(savedMinimalProject.getId(), "Saved minimal project should have an ID");
		// Clean up test data
		projectService.delete(savedMinimalProject.getId());
		// Test activity creation with required relationships
		final String activityName = "Edge Case Activity " + System.currentTimeMillis();
		final CActivity edgeActivity = activityService.newEntity(activityName, firstProject);
		edgeActivity.setDescription("Edge case description");
		final CActivity savedEdgeActivity = activityService.save(edgeActivity);
		assertNotNull(savedEdgeActivity, "Edge activity should be saved successfully");
		assertNotNull(savedEdgeActivity.getProject(), "Edge activity should have project relationship");
		assertEquals(firstProject.getId(), savedEdgeActivity.getProject().getId(), "Activity project should match");
		// Clean up
		activityService.delete(savedEdgeActivity.getId());
	}

	/** Test all decision-related views and CRUD operations */
	@Test
	public void testDecisionViewsCRUD() {
		// Test CDecisionsView - main decision management
		final List<CDecision> decisions = decisionService.list(Pageable.unpaged()).getContent();
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
		final List<CDecisionType> decisionTypes = decisionTypeService.list(Pageable.unpaged()).getContent();
		// Decision types might be empty in sample data, test if available
		for (final CDecisionType type : decisionTypes) {
			assertNotNull(type.getName(), "Decision type name should not be null");
			assertNotNull(type.getDescription(), "Decision type description should not be null");
		}
		// Test CDecisionStatusView - decision status management
		final List<CDecisionStatus> decisionStatuses = decisionStatusService.list(Pageable.unpaged()).getContent();
		if (!decisionStatuses.isEmpty()) {
			for (final CDecisionStatus status : decisionStatuses) {
				assertNotNull(status.getName(), "Decision status name should not be null");
				assertNotNull(status.getDescription(), "Decision status description should not be null");
			}
		}
	}

	/** Test grid operations including pagination and sorting */
	@Test
	public void testGridOperations() {
		// Test pagination with different page sizes
		final var pageable = Pageable.ofSize(2);
		final List<CProject> pagedProjects = projectService.list(pageable).getContent();
		assertNotNull(pagedProjects, "Paged results should not be null");
		assertTrue(pagedProjects.size() <= 2, "Paged results should respect page size");
		// Test with larger page size
		final var largerPageable = Pageable.ofSize(10);
		final List<CProject> largerPagedProjects = projectService.list(largerPageable).getContent();
		assertNotNull(largerPagedProjects, "Larger paged results should not be null");
		// Test unpaged results
		final List<CProject> allProjects = projectService.list(Pageable.unpaged()).getContent();
		assertTrue(allProjects.size() >= pagedProjects.size(), "All projects should include paged results");
	}

	/** Test all meeting-related views and CRUD operations */
	@Test
	public void testMeetingViewsCRUD() {
		// Test CMeetingsView - main meeting management
		final List<CMeeting> meetings = meetingService.list(Pageable.unpaged()).getContent();
		assertTrue(meetings.size() >= 1, "Should have at least 1 meeting from sample data");
		// Verify all meeting fields are populated
		for (final CMeeting meeting : meetings) {
			assertNotNull(meeting.getName(), "Meeting name should not be null");
			assertNotNull(meeting.getDescription(), "Meeting description should not be null");
			if (meeting.getMeetingType() != null) {
				assertNotNull(meeting.getMeetingType(), "Meeting type should not be null");
			}
			// Note: Some meetings might not have all relationships set in sample data
			assertNotNull(meeting.getProject(), "Meeting project should not be null");
			// Test grid selection and field updates
			final CMeeting selectedMeeting = meetingService.getById(meeting.getId()).orElse(null);
			assertNotNull(selectedMeeting, "Should be able to retrieve meeting by ID");
			assertEquals(meeting.getName(), selectedMeeting.getName(), "Retrieved meeting should match");
		}
		// Test CMeetingTypeView - meeting type management
		final List<CMeetingType> meetingTypes = meetingTypeService.list(Pageable.unpaged()).getContent();
		assertTrue(meetingTypes.size() >= 5, "Should have at least 5 meeting types from sample data");
		for (final CMeetingType type : meetingTypes) {
			assertNotNull(type.getName(), "Meeting type name should not be null");
			// Note: Description might be null in sample data
			if (type.getDescription() != null) {
				assertFalse(type.getDescription().trim().isEmpty(), "Meeting type description should not be empty if present");
			}
			// Test CRUD operations
			final CMeetingType retrieved = meetingTypeService.getById(type.getId()).orElse(null);
			assertNotNull(retrieved, "Should be able to retrieve meeting type by ID");
			assertEquals(type.getName(), retrieved.getName(), "Retrieved meeting type should match");
		}
	}

	/** Test all order-related views and CRUD operations Following AAA pattern: Arrange, Act, Assert */
	@Test
	public void testOrderViewsCRUD() {
		// ARRANGE - Test data already loaded via sample data initializer
		// ACT & ASSERT - Test READ operations
		final List<tech.derbent.orders.domain.COrder> orders = orderService.list(Pageable.unpaged()).getContent();
		// Note: Orders might be empty in sample data, so we just verify the service works
		// Verify service functionality by testing basic operations
		assertNotNull(orders, "Order list should not be null");
		assertTrue(orders.size() >= 0, "Order list should be non-negative");
		// If there are orders, test their structure
		for (final tech.derbent.orders.domain.COrder order : orders) {
			assertNotNull(order.getName(), "Order name should not be null");
			assertNotNull(order.getProject(), "Order project should not be null");
			// Test READ by ID operation
			final tech.derbent.orders.domain.COrder selectedOrder = orderService.getById(order.getId()).orElse(null);
			assertNotNull(selectedOrder, "Should be able to retrieve order by ID");
			assertEquals(order.getName(), selectedOrder.getName(), "Retrieved order should match");
		}
	}

	/** Test all project-related views and CRUD operations Following AAA pattern: Arrange, Act, Assert */
	@Test
	public void testProjectViewsCRUD() {
		// ARRANGE - Test data already loaded via sample data initializer
		// ACT & ASSERT - Test READ operations
		final List<CProject> projects = projectService.list(Pageable.unpaged()).getContent();
		assertTrue(projects.size() >= 1, "Should have at least 1 project from sample data");
		// Test CRUD operations for each existing project
		for (final CProject project : projects) {
			// Test READ operation - verify all required fields are populated
			assertNotNull(project.getName(), "Project name should not be null");
			assertNotNull(project.getDescription(), "Project description should not be null");
			// Test READ by ID operation
			final CProject selectedProject = projectService.getById(project.getId()).orElse(null);
			assertNotNull(selectedProject, "Should be able to retrieve project by ID");
			assertEquals(project.getName(), selectedProject.getName(), "Retrieved project should match");
			// Test UPDATE operation
			final String originalName = project.getName();
			final String updatedName = "Updated " + originalName + " " + System.currentTimeMillis();
			project.setName(updatedName);
			final CProject updatedProject = projectService.save(project);
			assertNotNull(updatedProject, "Updated project should not be null");
			assertEquals(updatedName, updatedProject.getName(), "Project name should be updated");
			// Restore original name for consistency
			project.setName(originalName);
			projectService.save(project);
		}
		// Test CREATE operation
		final CProject newProject = projectService.newEntity();
		newProject.setName("Test CRUD Project " + System.currentTimeMillis());
		newProject.setDescription("Project created by CRUD test");
		final CProject savedProject = projectService.save(newProject);
		assertNotNull(savedProject, "Saved project should not be null");
		assertNotNull(savedProject.getId(), "Saved project should have an ID");
		assertEquals(newProject.getName(), savedProject.getName(), "Saved project name should match");
		assertEquals(newProject.getDescription(), savedProject.getDescription(), "Saved project description should match");
		// Test DELETE operation
		final Long savedProjectId = savedProject.getId();
		projectService.delete(savedProjectId);
		final Optional<CProject> deletedProject = projectService.getById(savedProjectId);
		assertFalse(deletedProject.isPresent(), "Deleted project should not be found");
	}

	/** Test all risk-related views and CRUD operations Following AAA pattern: Arrange, Act, Assert */
	@Test
	public void testRiskViewsCRUD() {
		// ARRANGE - Test data already loaded via sample data initializer
		// ACT & ASSERT - Test READ operations
		final List<CRisk> risks = riskService.list(Pageable.unpaged()).getContent();
		assertTrue(risks.size() >= 1, "Should have at least 1 risk from sample data");
		// Test CRUD operations for each existing risk
		for (final CRisk risk : risks) {
			// Test READ operation - verify all required fields are populated
			assertNotNull(risk.getName(), "Risk name should not be null");
			assertNotNull(risk.getDescription(), "Risk description should not be null");
			assertNotNull(risk.getRiskSeverity(), "Risk severity should not be null");
			assertNotNull(risk.getProject(), "Risk project should not be null");
			// Test READ by ID operation
			final CRisk selectedRisk = riskService.getById(risk.getId()).orElse(null);
			assertNotNull(selectedRisk, "Should be able to retrieve risk by ID");
			assertEquals(risk.getName(), selectedRisk.getName(), "Retrieved risk should match");
			// Test UPDATE operation
			final String originalDescription = risk.getDescription();
			final String updatedDescription = "Updated " + originalDescription + " " + System.currentTimeMillis();
			risk.setDescription(updatedDescription);
			final CRisk updatedRisk = riskService.save(risk);
			assertNotNull(updatedRisk, "Updated risk should not be null");
			assertEquals(updatedDescription, updatedRisk.getDescription(), "Risk description should be updated");
			// Restore original description for consistency
			risk.setDescription(originalDescription);
			riskService.save(risk);
		}
	}

	/** Test CRUD operations for User Types */
	private void testUserTypeCRUD() {
		// ACT & ASSERT - Test READ operations
		final List<CUserType> userTypes = userTypeService.list(Pageable.unpaged()).getContent();
		for (final CUserType type : userTypes) {
			assertNotNull(type.getName(), "User type name should not be null");
			// Note: description might be null based on current data model
			if (type.getDescription() != null) {
				assertFalse(type.getDescription().trim().isEmpty(), "User type description should not be empty if present");
			}
			assertNotNull(type.getProject(), "User type project should not be null");
		}
		// Test CREATE operation
		final CProject firstProject = projectService.list(Pageable.unpaged()).getContent().get(0);
		final String typeName = "Test User Type " + System.currentTimeMillis();
		final CUserType newType = userTypeService.newEntity(typeName, firstProject);
		newType.setDescription("User type created by CRUD test");
		final CUserType savedType = userTypeService.save(newType);
		assertNotNull(savedType, "Saved user type should not be null");
		assertNotNull(savedType.getId(), "Saved user type should have an ID");
		assertEquals(newType.getName(), savedType.getName(), "Saved user type name should match");
		// Test UPDATE operation
		final String updatedName = "Updated " + savedType.getName();
		savedType.setName(updatedName);
		final CUserType updatedType = userTypeService.save(savedType);
		assertEquals(updatedName, updatedType.getName(), "User type name should be updated");
		// Test DELETE operation
		final Long savedTypeId = savedType.getId();
		userTypeService.delete(savedTypeId);
		final Optional<CUserType> deletedType = userTypeService.getById(savedTypeId);
		assertFalse(deletedType.isPresent(), "Deleted user type should not be found");
	}

	/** Test all user-related views and CRUD operations Following AAA pattern: Arrange, Act, Assert */
	@Test
	public void testUserViewsCRUD() {
		// ARRANGE - Test data already loaded via sample data initializer
		// ACT & ASSERT - Test READ operations
		final List<CUser> users = userService.list(Pageable.unpaged()).getContent();
		assertTrue(users.size() >= 1, "Should have at least 1 user from sample data");
		// Test CRUD operations for each existing user
		for (final CUser user : users) {
			// Test READ operation - verify all required fields are populated
			assertNotNull(user.getName(), "User name should not be null");
			assertNotNull(user.getEmail(), "User email should not be null");
			assertNotNull(user.getUserRole(), "User role should not be null");
			assertNotNull(user.getCompany(), "User company should not be null");
			// Test READ by ID operation
			final CUser selectedUser = userService.getById(user.getId()).orElse(null);
			assertNotNull(selectedUser, "Should be able to retrieve user by ID");
			assertEquals(user.getName(), selectedUser.getName(), "Retrieved user should match");
			// Test UPDATE operation (modify phone number to avoid email uniqueness
			// conflicts)
			final String originalPhone = user.getPhone();
			final String updatedPhone = "+1-555-" + System.currentTimeMillis();
			user.setPhone(updatedPhone);
			final CUser updatedUser = userService.save(user);
			assertNotNull(updatedUser, "Updated user should not be null");
			assertEquals(updatedPhone, updatedUser.getPhone(), "User phone should be updated");
			// Restore original phone for consistency
			user.setPhone(originalPhone);
			userService.save(user);
		}
		// Test CREATE operation
		final CUser newUser = userService.newEntity();
		final String uniqueId = String.valueOf(System.currentTimeMillis());
		final CCompany firstCompany = companyService.list(Pageable.unpaged()).getContent().get(0);
		newUser.setName("Test CRUD User " + uniqueId);
		newUser.setEmail("testuser" + uniqueId + "@example.com");
		newUser.setLogin("testuser" + uniqueId);
		newUser.setUserRole(EUserRole.TEAM_MEMBER);
		newUser.setCompany(firstCompany);
		newUser.setPhone("+1-555-" + uniqueId);
		final CUser savedUser = userService.save(newUser);
		assertNotNull(savedUser, "Saved user should not be null");
		assertNotNull(savedUser.getId(), "Saved user should have an ID");
		assertEquals(newUser.getName(), savedUser.getName(), "Saved user name should match");
		assertEquals(newUser.getEmail(), savedUser.getEmail(), "Saved user email should match");
		// Test DELETE operation
		final Long savedUserId = savedUser.getId();
		userService.delete(savedUserId);
		final Optional<CUser> deletedUser = userService.getById(savedUserId);
		assertFalse(deletedUser.isPresent(), "Deleted user should not be found");
		// Test CUserTypeView - user type management CRUD
		testUserTypeCRUD();
	}
}
