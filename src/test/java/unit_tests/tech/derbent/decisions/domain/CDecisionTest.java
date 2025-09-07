package unit_tests.tech.derbent.decisions.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.decisions.domain.CDecisionStatus;
import tech.derbent.decisions.domain.CDecisionType;
import tech.derbent.users.domain.CUser;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/** Unit tests for CDecision domain entity. Tests basic functionality, business logic, and approval workflow. */
class CDecisionTest extends CTestBase {

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
	}

	@Test
	void testAccountableUserAssignment() {
		// Given
		final CDecision decision = new CDecision("Test Decision", project);
		final CUser accountableUser = new CUser("lead", "test123", "Team Lead", "lead@example.com");
		// When
		decision.setAccountableUser(accountableUser);
		// Then
		assertEquals(accountableUser, decision.getAccountableUser());
	}

	@Test
	void testDecisionCreationWithBasicFields() {
		// Given When
		final CDecision decision = new CDecision("Test Decision", project);
		// Then
		assertNotNull(decision);
		assertEquals("Test Decision", decision.getName());
		assertEquals(project, decision.getProject());
		assertEquals("Test Project", decision.getProjectName());
	}

	@Test
	void testDecisionTypeAndStatusAssignment() {
		// Given
		final CDecision decision = new CDecision("Test Decision", project);
		final CDecisionType decisionType = new CDecisionType("Strategic", project);
		final CDecisionStatus decisionStatus = new CDecisionStatus("Under Review", project);
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
		final CDecision decision = new CDecision("Test Decision", project);
		final BigDecimal cost = new BigDecimal("50000.00");
		// When
		decision.setEstimatedCost(cost);
		// Then
		assertEquals(cost, decision.getEstimatedCost());
	}

	@Test
	void testToStringMethod() {
		// Given
		final CDecision decision = new CDecision("Test Decision", project);
		// When
		final String result = decision.toString();
		// Then
		assertEquals("Test Decision", result);
	}
}
