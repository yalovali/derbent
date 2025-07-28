package tech.derbent.decisions.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * Unit tests for CDecisionApproval domain entity.
 * Tests approval workflow, status management, and business logic.
 */
class CDecisionApprovalTest {

    @Test
    void testApprovalCreationWithBasicFields() {
        // Given
        final CProject project = new CProject();
        final CDecision decision = new CDecision("Test Decision", project);
        final CUser approver = new CUser("approver", "test123", "John Approver", "approver@example.com");
        
        // When
        final CDecisionApproval approval = new CDecisionApproval(decision, approver);
        
        // Then
        assertNotNull(approval);
        assertEquals(decision, approval.getDecision());
        assertEquals(approver, approval.getApprover());
        assertEquals("Approval by John Approver", approval.getName());
        assertTrue(approval.isPending());
        assertFalse(approval.isApproved());
        assertFalse(approval.isRejected());
    }

    @Test
    void testApprovalProcess() {
        // Given
        final CProject project = new CProject();
        final CDecision decision = new CDecision("Test Decision", project);
        final CUser approver = new CUser("approver", "test123", "John Approver", "approver@example.com");
        final CDecisionApproval approval = new CDecisionApproval(decision, approver);
        final String comments = "This looks good to proceed";
        
        // When
        approval.approve(comments);
        
        // Then
        assertTrue(approval.isApproved());
        assertFalse(approval.isPending());
        assertFalse(approval.isRejected());
        assertEquals(comments, approval.getApprovalComments());
        assertNotNull(approval.getApprovalDate());
        assertEquals("Approved", approval.getApprovalStatusText());
    }

    @Test
    void testRejectionProcess() {
        // Given
        final CProject project = new CProject();
        final CDecision decision = new CDecision("Test Decision", project);
        final CUser approver = new CUser("approver", "test123", "John Approver", "approver@example.com");
        final CDecisionApproval approval = new CDecisionApproval(decision, approver);
        final String comments = "Needs more analysis before approval";
        
        // When
        approval.reject(comments);
        
        // Then
        assertTrue(approval.isRejected());
        assertFalse(approval.isPending());
        assertFalse(approval.isApproved());
        assertEquals(comments, approval.getApprovalComments());
        assertNotNull(approval.getApprovalDate());
        assertEquals("Rejected", approval.getApprovalStatusText());
    }

    @Test
    void testResetToPending() {
        // Given
        final CProject project = new CProject();
        final CDecision decision = new CDecision("Test Decision", project);
        final CUser approver = new CUser("approver", "test123", "John Approver", "approver@example.com");
        final CDecisionApproval approval = new CDecisionApproval(decision, approver);
        
        // When first approving then resetting
        approval.approve("Initial approval");
        approval.resetToPending();
        
        // Then
        assertTrue(approval.isPending());
        assertFalse(approval.isApproved());
        assertFalse(approval.isRejected());
        assertEquals("Pending", approval.getApprovalStatusText());
    }

    @Test
    void testApprovalWithAllFields() {
        // Given
        final CProject project = new CProject();
        final CDecision decision = new CDecision("Test Decision", project);
        final CUser approver = new CUser("approver", "test123", "John Approver", "approver@example.com");
        final LocalDateTime dueDate = LocalDateTime.now().plusDays(7);
        final Integer priority = 1; // Critical
        final boolean isRequired = true;
        
        // When
        final CDecisionApproval approval = new CDecisionApproval(decision, approver, 
                                                               isRequired, priority, dueDate);
        
        // Then
        assertEquals(decision, approval.getDecision());
        assertEquals(approver, approval.getApprover());
        assertEquals(dueDate, approval.getDueDate());
        assertEquals(priority, approval.getApprovalPriority());
        assertTrue(approval.isRequired());
    }

    @Test
    void testOverdueApproval() {
        // Given
        final CProject project = new CProject();
        final CDecision decision = new CDecision("Test Decision", project);
        final CUser approver = new CUser("approver", "test123", "John Approver", "approver@example.com");
        final LocalDateTime pastDueDate = LocalDateTime.now().minusDays(1);
        
        final CDecisionApproval approval = new CDecisionApproval(decision, approver, 
                                                               true, 2, pastDueDate);
        
        // When & Then
        assertTrue(approval.isOverdue());
        assertTrue(approval.isPending());
        
        // When approval is granted
        approval.approve("Late approval");
        
        // Then approval is no longer overdue
        assertFalse(approval.isOverdue());
    }

    @Test
    void testApprovalPriorityDefaults() {
        // Given
        final CProject project = new CProject();
        final CDecision decision = new CDecision("Test Decision", project);
        final CUser approver = new CUser("approver", "test123", "John Approver", "approver@example.com");
        
        // When
        final CDecisionApproval approval = new CDecisionApproval(decision, approver);
        
        // Then
        assertEquals(Integer.valueOf(3), approval.getApprovalPriority()); // Default medium priority
        assertTrue(approval.isRequired()); // Default is required
    }

    @Test
    void testApprovalStatusText() {
        // Given
        final CProject project = new CProject();
        final CDecision decision = new CDecision("Test Decision", project);
        final CUser approver = new CUser("approver", "test123", "John Approver", "approver@example.com");
        final CDecisionApproval approval = new CDecisionApproval(decision, approver);
        
        // When pending
        assertEquals("Pending", approval.getApprovalStatusText());
        
        // When approved
        approval.approve("Good to go");
        assertEquals("Approved", approval.getApprovalStatusText());
        
        // When reset and rejected
        approval.resetToPending();
        approval.reject("Not ready");
        assertEquals("Rejected", approval.getApprovalStatusText());
    }

    @Test
    void testToStringMethod() {
        // Given
        final CProject project = new CProject();
        final CDecision decision = new CDecision("Test Decision", project);
        final CUser approver = new CUser("approver", "test123", "John Approver", "approver@example.com");
        final CDecisionApproval approval = new CDecisionApproval(decision, approver);
        
        // When
        final String result = approval.toString();
        
        // Then
        assertTrue(result.contains("Approval by John Approver"));
        assertTrue(result.contains("Pending"));
    }

    @Test
    void testApproverNameUpdate() {
        // Given
        final CProject project = new CProject();
        final CDecision decision = new CDecision("Test Decision", project);
        final CUser approver1 = new CUser("approver1", "test123", "John Approver", "approver1@example.com");
        final CUser approver2 = new CUser("approver2", "test123", "Jane Manager", "manager@example.com");
        final CDecisionApproval approval = new CDecisionApproval(decision, approver1);
        
        // When
        approval.setApprover(approver2);
        
        // Then
        assertEquals(approver2, approval.getApprover());
        assertEquals("Approval by Jane Manager", approval.getName());
    }
}