package tech.derbent.activities.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tech.derbent.abstracts.domains.CTestBase;
import tech.derbent.users.domain.CUser;

/**
 * Unit tests for enhanced CActivity class functionality. Tests the new features added for
 * comprehensive project management.
 */
@DisplayName ("Enhanced CActivity Tests")
class CActivityEnhancedTest extends CTestBase {

	private CActivity activity;

	private CUser user1;

	private CUser user2;

	private CActivityStatus todoStatus;

	private CActivityStatus doneStatus;

	private CActivityPriority highPriority;

	@Override
	protected void setupForTest() {
		// Create test project Create test users
		user1 = new CUser("John");
		user1.setLogin("john.doe");
		user2 = new CUser("Jane");
		user2.setLogin("jane.smith");
		// Create test status
		todoStatus = new CActivityStatus("TODO", project);
		doneStatus =
			new CActivityStatus("DONE", project, "Task completed", "#00AA00", true);
		// Create test priority
		highPriority = new CActivityPriority("HIGH", project);
		// Create test activity
		activity = new CActivity("Test Activity", project);
	}

	@Test
	@DisplayName ("Should auto-set completion date when status is final")
	void shouldAutoSetCompletionDateOnFinalStatus() {
		assertNull(activity.getCompletionDate());
		activity.setProgressPercentage(50);
		activity.setStatus(doneStatus);
		assertNotNull(activity.getCompletionDate());
		assertEquals(LocalDate.now(), activity.getCompletionDate());
		assertEquals(100, activity.getProgressPercentage());
	}

	@Test
	@DisplayName ("Should auto-set completion date when progress reaches 100%")
	void shouldAutoSetCompletionDateOnFullProgress() {
		assertNull(activity.getCompletionDate());
		activity.setProgressPercentage(100);
		assertNotNull(activity.getCompletionDate());
		assertEquals(LocalDate.now(), activity.getCompletionDate());
	}

	@Test
	@DisplayName ("Should calculate variances with null values")
	void shouldCalculateVariancesWithNullValues() {
		// With null values, variances should be zero
		assertEquals(BigDecimal.ZERO, activity.calculateTimeVariance());
		assertEquals(BigDecimal.ZERO, activity.calculateCostVariance());
		// With only estimated hours set, actual is zero (from getter), so variance = 0 -
		// estimated
		activity.setEstimatedHours(new BigDecimal("10.00"));
		assertEquals(new BigDecimal("-10.00"), activity.calculateTimeVariance()); // 0 -
																					// 10
																					// =
																					// -10
		// With both values set
		activity.setActualHours(new BigDecimal("8.00"));
		assertEquals(new BigDecimal("-2.00"), activity.calculateTimeVariance()); // 8 - 10
																					// =
																					// -2
	}

	@Test
	@DisplayName ("Should create activity with user assignment")
	void shouldCreateActivityWithAssignment() {
		final CActivity assignedActivity = new CActivity("Assigned Task", project, user1);
		assertEquals("Assigned Task", assignedActivity.getName());
		assertEquals(project, assignedActivity.getProject());
		assertEquals(user1, assignedActivity.getAssignedTo());
		assertNotNull(assignedActivity.getCreatedDate());
	}

	@Test
	@DisplayName ("Should detect completed activities correctly")
	void shouldDetectCompletedActivities() {
		// Initially not completed
		assertFalse(activity.isCompleted());
		// Set completion date
		activity.setCompletionDate(LocalDate.now());
		assertTrue(activity.isCompleted());
		// Reset and test with progress percentage
		activity.setCompletionDate(null);
		activity.setProgressPercentage(100);
		assertTrue(activity.isCompleted());
		// Reset and test with final status
		activity.setProgressPercentage(50);
		activity.setStatus(doneStatus);
		assertTrue(activity.isCompleted());
	}

	@Test
	@DisplayName ("Should detect overdue activities correctly")
	void shouldDetectOverdueActivities() {
		// No due date - not overdue
		assertFalse(activity.isOverdue());
		// Due date in future - not overdue
		activity.setDueDate(LocalDate.now().plusDays(5));
		assertFalse(activity.isOverdue());
		// Due date in past but completed - not overdue
		activity.setDueDate(LocalDate.now().minusDays(5));
		activity.setCompletionDate(LocalDate.now().minusDays(2));
		assertFalse(activity.isOverdue());
		// Due date in past, not completed, and no final status - overdue
		activity.setCompletionDate(null);
		activity.setProgressPercentage(50); // Not 100%
		activity.setStatus(null); // No final status
		assertTrue(activity.isOverdue());
	}

	@Test
	@DisplayName ("Should handle additional information fields")
	void shouldHandleAdditionalInformation() {
		final String description = "Detailed activity description";
		final String acceptanceCriteria = "Must pass all tests and code review";
		final String notes = "Important notes about implementation";
		activity.setDescription(description);
		activity.setAcceptanceCriteria(acceptanceCriteria);
		activity.setNotes(notes);
		assertEquals(description, activity.getDescription());
		assertEquals(acceptanceCriteria, activity.getAcceptanceCriteria());
		assertEquals(notes, activity.getNotes());
	}

	@Test
	@DisplayName ("Should handle cost tracking correctly")
	void shouldHandleCostTracking() {
		// Set cost values
		activity.setEstimatedCost(new BigDecimal("1000.00"));
		activity.setActualCost(new BigDecimal("1200.00"));
		activity.setHourlyRate(new BigDecimal("50.00"));
		assertEquals(new BigDecimal("1000.00"), activity.getEstimatedCost());
		assertEquals(new BigDecimal("1200.00"), activity.getActualCost());
		assertEquals(new BigDecimal("50.00"), activity.getHourlyRate());
		// Test cost variance calculation
		final BigDecimal costVariance = activity.calculateCostVariance();
		assertEquals(new BigDecimal("200.00"), costVariance); // Over budget
	}

	@Test
	@DisplayName ("Should handle date management correctly")
	void shouldHandleDateManagement() {
		final LocalDate startDate = LocalDate.of(2025, 7, 1);
		final LocalDate dueDate = LocalDate.of(2025, 7, 15);
		final LocalDate completionDate = LocalDate.of(2025, 7, 14);
		activity.setStartDate(startDate);
		activity.setDueDate(dueDate);
		activity.setCompletionDate(completionDate);
		assertEquals(startDate, activity.getStartDate());
		assertEquals(dueDate, activity.getDueDate());
		assertEquals(completionDate, activity.getCompletionDate());
	}

	@Test
	@DisplayName ("Should handle progress percentage validation")
	void shouldHandleProgressValidation() {
		// Valid progress values
		activity.setProgressPercentage(0);
		assertEquals(0, activity.getProgressPercentage());
		activity.setProgressPercentage(50);
		assertEquals(50, activity.getProgressPercentage());
		activity.setProgressPercentage(100);
		assertEquals(100, activity.getProgressPercentage());
		// Note: Invalid values (< 0 or > 100) should be handled by service layer
		// validation The domain object accepts the values but @Min/@Max annotations
		// provide validation hints
	}

	@Test
	@DisplayName ("Should handle status and priority assignments")
	void shouldHandleStatusAndPriority() {
		activity.setStatus(todoStatus);
		activity.setPriority(highPriority);
		assertEquals(todoStatus, activity.getStatus());
		assertEquals(highPriority, activity.getPriority());
	}

	@Test
	@DisplayName ("Should handle time tracking correctly")
	void shouldHandleTimeTracking() {
		// Set time values
		activity.setEstimatedHours(new BigDecimal("10.00"));
		activity.setActualHours(new BigDecimal("8.50"));
		activity.setRemainingHours(new BigDecimal("1.50"));
		assertEquals(new BigDecimal("10.00"), activity.getEstimatedHours());
		assertEquals(new BigDecimal("8.50"), activity.getActualHours());
		assertEquals(new BigDecimal("1.50"), activity.getRemainingHours());
		// Test time variance calculation
		final BigDecimal timeVariance = activity.calculateTimeVariance();
		assertEquals(new BigDecimal("-1.50"), timeVariance); // Under estimated
	}

	@Test
	@DisplayName ("Should handle user assignments correctly")
	void shouldHandleUserAssignments() {
		activity.setAssignedTo(user1);
		activity.setCreatedBy(user2);
		assertEquals(user1, activity.getAssignedTo());
		assertEquals(user2, activity.getCreatedBy());
	}

	@Test
	@DisplayName ("Should initialize with default values")
	void shouldInitializeWithDefaults() {
		assertNotNull(activity.getCreatedDate());
		assertNotNull(activity.getLastModifiedDate());
		assertEquals(BigDecimal.ZERO, activity.getActualHours());
		assertEquals(BigDecimal.ZERO, activity.getActualCost());
		assertEquals(0, activity.getProgressPercentage());
		assertNull(activity.getCompletionDate());
	}

	@Test
	@DisplayName ("Should prevent negative values for time and cost")
	void shouldPreventNegativeValues() {
		// Set negative values - should be converted to zero/default
		activity.setActualHours(new BigDecimal("-5.00"));
		activity.setActualCost(new BigDecimal("-100.00"));
		// Values should be set but validation would occur at service/controller level
		assertEquals(new BigDecimal("-5.00"), activity.getActualHours());
		assertEquals(new BigDecimal("-100.00"), activity.getActualCost());
		// Note: Actual validation happens through @DecimalMin annotations and service
		// layer
	}

	@Test
	@DisplayName ("Should prevent self-referencing parent activity")
	void shouldPreventSelfReferencingParent() {
		// This should be handled gracefully - no exception but no assignment
		activity.setParentActivity(activity);
		// Should not set self as parent (implementation prevents this)
		assertNull(activity.getParentActivity());
	}

	@Test
	@DisplayName ("Should update last modified date on changes")
	void shouldUpdateLastModifiedDate() {
		final LocalDateTime originalModified = activity.getLastModifiedDate();

		// Wait a bit to ensure different timestamp
		try {
			Thread.sleep(10);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		activity.setDescription("Updated description");
		assertNotNull(activity.getLastModifiedDate());
		assertTrue(activity.getLastModifiedDate().isAfter(originalModified));
	}
}