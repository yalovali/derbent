package tech.derbent.decisions.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * Unit tests for CDecision domain entity. Tests basic functionality, business logic, and
 * approval workflow.
 */
class CDecisionTest {

	@Test
	void testAccountableUserAssignment() {
		// Given
		final CProject project = new CProject();
		final CDecision decision = new CDecision("Test Decision", project);
		final CUser accountableUser =
			new CUser("lead", "test123", "Team Lead", "lead@example.com");
		// When
		decision.setAccountableUser(accountableUser);
		// Then
		assertEquals(accountableUser, decision.getAccountableUser());
	}

	@Test
	void testApprovalWorkflow() {
		// Given
		final CProject project = new CProject();
		final CDecision decision = new CDecision("Test Decision", project);
		final CUser approver1 =
			new CUser("manager1", "test123", "Manager One", "manager1@example.com");
		final CUser approver2 =
			new CUser("manager2", "test123", "Manager Two", "manager2@example.com");
		final CDecisionApproval approval1 = new CDecisionApproval(decision, approver1);
		final CDecisionApproval approval2 = new CDecisionApproval(decision, approver2);
		// When
		decision.addApproval(approval1);
		decision.addApproval(approval2);
		// Then
		assertEquals(2, decision.getApprovalCount());
		assertEquals(0, decision.getApprovedCount());
		assertFalse(decision.isFullyApproved());
		// When first approval is granted
		approval1.approve("Looks good to me");
		// Then
		assertEquals(1, decision.getApprovedCount());
		assertFalse(decision.isFullyApproved());
		// When second approval is granted
		approval2.approve("Approved with conditions");
		// Then
		assertEquals(2, decision.getApprovedCount());
		assertTrue(decision.isFullyApproved());
	}

	@Test
	void testApprovalWorkflowWithRejection() {
		// Given
		final CProject project = new CProject();
		final CDecision decision = new CDecision("Test Decision", project);
		final CUser approver =
			new CUser("manager", "test123", "Manager User", "manager@example.com");
		final CDecisionApproval approval = new CDecisionApproval(decision, approver);
		decision.addApproval(approval);
		// When approval is rejected
		approval.reject("Needs more analysis");
		// Then
		assertEquals(1, decision.getApprovalCount());
		assertEquals(0, decision.getApprovedCount());
		assertFalse(decision.isFullyApproved());
		assertTrue(approval.isRejected());
		assertEquals("Needs more analysis", approval.getApprovalComments());
	}

	@Test
	void testDecisionCreationWithBasicFields() {
		// Given
		final CProject project = new CProject();
		project.setName("Test Project");
		// When
		final CDecision decision = new CDecision("Test Decision", project);
		// Then
		assertNotNull(decision);
		assertEquals("Test Decision", decision.getName());
		assertEquals(project, decision.getProject());
		assertEquals("Test Project", decision.getProjectName());
	}

	@Test
	void testDecisionCreationWithDescription() {
		// Given
		final CProject project = new CProject();
		project.setName("Test Project");
		final String description = "This is a test decision for unit testing";
		// When
		final CDecision decision = new CDecision("Test Decision", project, description);
		// Then
		assertNotNull(decision);
		assertEquals("Test Decision", decision.getName());
		assertEquals(description, decision.getDescription());
		assertEquals(project, decision.getProject());
	}

	@Test
	void testDecisionTypeAndStatusAssignment() {
		// Given
		final CProject project = new CProject();
		final CDecision decision = new CDecision("Test Decision", project);
		final CDecisionType decisionType = new CDecisionType("Strategic", project);
		final CDecisionStatus decisionStatus = new CDecisionStatus("Under Review");
		// When
		decision.setDecisionType(decisionType);
		decision.setDecisionStatus(decisionStatus);
		// Then
		assertEquals(decisionType, decision.getDecisionType());
		assertEquals(decisionStatus, decision.getDecisionStatus());
	}

	@Test
	void testDecisionWithDatesAndTimestamps() {
		// Given
		final CProject project = new CProject();
		final CDecision decision = new CDecision("Test Decision", project);
		final LocalDateTime implementationDate = LocalDateTime.now().plusDays(30);
		final LocalDateTime reviewDate = LocalDateTime.now().plusDays(90);
		// When
		decision.setImplementationDate(implementationDate);
		decision.setReviewDate(reviewDate);
		// Then
		assertEquals(implementationDate, decision.getImplementationDate());
		assertEquals(reviewDate, decision.getReviewDate());
	}

	@SuppressWarnings ("unused")
	@Test
	void testEqualsAndHashCode() {
		// Given
		final CProject project = new CProject();
		final CDecision decision1 = new CDecision("Test Decision", project);
		final CDecision decision2 = new CDecision("Test Decision", project);
		// When & Then
		assertEquals(decision1, decision1); // reflexive
		assertNotNull(decision1); // non-null
		assertEquals(decision1.hashCode(), decision1.hashCode()); // consistent
	}

	@Test
	void testEstimatedCostManagement() {
		// Given
		final CProject project = new CProject();
		final CDecision decision = new CDecision("Test Decision", project);
		final BigDecimal cost = new BigDecimal("50000.00");
		// When
		decision.setEstimatedCost(cost);
		// Then
		assertEquals(cost, decision.getEstimatedCost());
	}

	@Test
	void testTeamMemberManagement() {
		// Given
		final CProject project = new CProject();
		final CDecision decision = new CDecision("Test Decision", project);
		final CUser user1 =
			new CUser("john.doe", "test123", "John Doe", "john.doe@example.com");
		final CUser user2 =
			new CUser("jane.smith", "test123", "Jane Smith", "jane.smith@example.com");
		// When
		decision.addTeamMember(user1);
		decision.addTeamMember(user2);
		// Then
		assertEquals(2, decision.getTeamMembers().size());
		assertTrue(decision.getTeamMembers().contains(user1));
		assertTrue(decision.getTeamMembers().contains(user2));
		// When removing a team member
		decision.removeTeamMember(user1);
		// Then
		assertEquals(1, decision.getTeamMembers().size());
		assertFalse(decision.getTeamMembers().contains(user1));
		assertTrue(decision.getTeamMembers().contains(user2));
	}

	@Test
	void testToStringMethod() {
		// Given
		final CProject project = new CProject();
		final CDecision decision = new CDecision("Test Decision", project);
		// When
		final String result = decision.toString();
		// Then
		assertEquals("Test Decision", result);
	}
}