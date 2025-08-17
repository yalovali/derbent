package tech.derbent.decisions.service;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CAbstractNamedRepository;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.decisions.domain.CDecisionApproval;
import tech.derbent.users.domain.CUser;

/**
 * CDecisionApprovalRepository - Repository interface for CDecisionApproval entities. Layer: Data Access (MVC) Provides
 * data access methods for decision approval entities.
 */
public interface CDecisionApprovalRepository extends CAbstractNamedRepository<CDecisionApproval> {

    /**
     * Counts pending approvals for a specific decision.
     * 
     * @param decision
     *            the decision
     * @return count of pending approvals
     */
    long countByDecisionAndIsApprovedIsNull(CDecision decision);

    /**
     * Counts approved approvals for a specific decision.
     * 
     * @param decision
     *            the decision
     * @return count of approved approvals
     */
    long countByDecisionAndIsApprovedTrue(CDecision decision);

    /**
     * Finds all approvals assigned to a specific user.
     * 
     * @param approver
     *            the approver user
     * @return list of approvals assigned to the user
     */
    List<CDecisionApproval> findByApprover(CUser approver);

    /**
     * Finds pending approvals for a specific user.
     * 
     * @param approver
     *            the approver user
     * @return list of pending approvals for the user
     */
    List<CDecisionApproval> findByApproverAndIsApprovedIsNull(CUser approver);

    /**
     * Finds all approvals for a specific decision.
     * 
     * @param decision
     *            the decision
     * @return list of approvals for the decision
     */
    List<CDecisionApproval> findByDecision(CDecision decision);

    /**
     * Finds required approvals for a specific decision.
     * 
     * @param decision
     *            the decision
     * @return list of required approvals for the decision
     */
    List<CDecisionApproval> findByDecisionAndIsRequiredTrue(CDecision decision);

    /**
     * Finds pending required approvals for a specific decision.
     * 
     * @param decision
     *            the decision
     * @return list of pending required approvals for the decision
     */
    List<CDecisionApproval> findByDecisionAndIsRequiredTrueAndIsApprovedIsNull(CDecision decision);

    /**
     * Finds approvals for a decision with eagerly loaded approver to prevent LazyInitializationException.
     * 
     * @param decision
     *            the decision
     * @return list of approvals with loaded approver
     */
    @Query("SELECT a FROM CDecisionApproval a LEFT JOIN FETCH a.approver WHERE a.decision = :decision")
    List<CDecisionApproval> findByDecisionWithApprover(@Param("decision") CDecision decision);

    /**
     * Finds overdue approvals for a specific user.
     * 
     * @param approver
     *            the approver user
     * @return list of overdue approvals for the user
     */
    @Query("SELECT a FROM CDecisionApproval a WHERE a.approver = :approver AND a.dueDate < CURRENT_TIMESTAMP AND a.isApproved IS NULL")
    List<CDecisionApproval> findOverdueApprovalsByApprover(@Param("approver") CUser approver);
}