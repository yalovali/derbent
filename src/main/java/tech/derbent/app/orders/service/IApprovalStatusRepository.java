package tech.derbent.app.orders.service;

import tech.derbent.api.services.IEntityOfProjectRepository;
import tech.derbent.app.orders.domain.CApprovalStatus;

/** CApprovalStatusRepository - Repository interface for CApprovalStatus entities. Layer: Service (MVC) Provides data access operations for approval
 * statuses, extending the standard CAbstractNamedRepository to inherit common CRUD and query operations. */
public interface IApprovalStatusRepository extends IEntityOfProjectRepository<CApprovalStatus> {
}
