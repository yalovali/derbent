package tech.derbent.app.orders.service;

import tech.derbent.api.services.IAbstractNamedRepository;
import tech.derbent.app.orders.domain.COrderApproval;

/** COrderApprovalRepository - Repository interface for COrderApproval entities. Layer: Service (MVC) Provides data access operations for order
 * approvals, extending the standard CAbstractNamedRepository to inherit common CRUD and query operations. */
public interface IOrderApprovalRepository extends IAbstractNamedRepository<COrderApproval> {
	// Inherits standard operations from CAbstractNamedRepository
	// Additional custom query methods can be added here if needed
}
