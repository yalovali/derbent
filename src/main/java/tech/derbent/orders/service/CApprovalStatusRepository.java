package tech.derbent.orders.service;

import tech.derbent.api.services.CEntityOfProjectRepository;
import tech.derbent.orders.domain.CApprovalStatus;

/** CApprovalStatusRepository - Repository interface for CApprovalStatus entities. Layer: Service (MVC) Provides data access operations for approval
 * statuses, extending the standard CAbstractNamedRepository to inherit common CRUD and query operations. */
public interface CApprovalStatusRepository extends CEntityOfProjectRepository<CApprovalStatus> {
}
