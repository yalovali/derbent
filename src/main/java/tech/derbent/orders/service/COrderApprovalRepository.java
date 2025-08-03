package tech.derbent.orders.service;

import tech.derbent.abstracts.services.CAbstractNamedRepository;
import tech.derbent.orders.domain.COrderApproval;

/**
 * COrderApprovalRepository - Repository interface for COrderApproval entities. Layer: Service (MVC)
 * 
 * Provides data access operations for order approvals, extending the standard CAbstractNamedRepository to inherit
 * common CRUD and query operations.
 */
public interface COrderApprovalRepository extends CAbstractNamedRepository<COrderApproval> {
    // Inherits standard operations from CAbstractNamedRepository
    // Additional custom query methods can be added here if needed
}