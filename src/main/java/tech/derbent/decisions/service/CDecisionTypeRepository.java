package tech.derbent.decisions.service;

import java.util.List;

import tech.derbent.abstracts.services.CAbstractNamedRepository;
import tech.derbent.decisions.domain.CDecisionType;

/**
 * CDecisionTypeRepository - Repository interface for CDecisionType entities.
 * Layer: Data Access (MVC)
 * 
 * Provides data access methods for decision type entities.
 */
public interface CDecisionTypeRepository extends CAbstractNamedRepository<CDecisionType> {

    /**
     * Finds all active decision types.
     * @return list of active decision types
     */
    List<CDecisionType> findByIsActiveTrue();

    /**
     * Finds all inactive decision types.
     * @return list of inactive decision types
     */
    List<CDecisionType> findByIsActiveFalse();

    /**
     * Finds decision types that require approval.
     * @return list of decision types that require approval
     */
    List<CDecisionType> findByRequiresApprovalTrue();

    /**
     * Finds all decision types ordered by sort order.
     * @return list of decision types sorted by sort order
     */
    List<CDecisionType> findAllByOrderBySortOrderAsc();

    /**
     * Finds active decision types ordered by sort order.
     * @return list of active decision types sorted by sort order
     */
    List<CDecisionType> findByIsActiveTrueOrderBySortOrderAsc();
}