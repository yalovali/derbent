package tech.derbent.app.orders.approval.service;

import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.app.orders.approval.domain.CApprovalStatus;

/** CApprovalStatusRepository - Repository interface for CApprovalStatus entities. Layer: Service (MVC) Provides data access operations for approval
 * statuses, extending the standard CAbstractNamedRepository to inherit common CRUD and query operations. */
public interface IApprovalStatusRepository extends IEntityOfCompanyRepository<CApprovalStatus> {
}
