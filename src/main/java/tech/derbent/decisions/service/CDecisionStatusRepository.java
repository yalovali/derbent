package tech.derbent.decisions.service;

import java.util.List;

import tech.derbent.abstracts.services.CAbstractNamedRepository;
import tech.derbent.decisions.domain.CDecisionStatus;

/**
 * CDecisionStatusRepository - Repository interface for CDecisionStatus entities.
 * Layer: Data Access (MVC)
 * 
 * Provides data access methods for decision status entities.
 */
public interface CDecisionStatusRepository extends CAbstractNamedRepository<CDecisionStatus> {

    /**
     * Finds all final decision statuses.
     * @return list of final decision statuses
     */
    List<CDecisionStatus> findByIsFinalTrue();

    /**
     * Finds all non-final decision statuses.
     * @return list of non-final decision statuses
     */
    List<CDecisionStatus> findByIsFinalFalse();

    /**
     * Finds decision statuses that allow editing.
     * @return list of decision statuses that allow editing
     */
    List<CDecisionStatus> findByAllowsEditingTrue();

    /**
     * Finds decision statuses that require approval.
     * @return list of decision statuses that require approval
     */
    List<CDecisionStatus> findByRequiresApprovalTrue();

    /**
     * Finds all decision statuses ordered by sort order.
     * @return list of decision statuses sorted by sort order
     */
    List<CDecisionStatus> findAllByOrderBySortOrderAsc();

    /**
     * Finds non-final decision statuses ordered by sort order.
     * @return list of non-final decision statuses sorted by sort order
     */
    List<CDecisionStatus> findByIsFinalFalseOrderBySortOrderAsc();
}