package tech.derbent.orders.service;

import tech.derbent.api.services.IEntityOfProjectRepository;
import tech.derbent.orders.domain.COrderStatus;

/** COrderStatusRepository - Repository interface for COrderStatus entities. Layer: Service (MVC) Provides data access operations for order statuses.
 * Since COrderStatus extends CStatus which extends CTypeEntity which extends CEntityOfProject, this repository must extend CEntityOfProjectRepository
 * to provide project-aware operations. */
public interface IOrderStatusRepository extends IEntityOfProjectRepository<COrderStatus> {
	// Inherits standard operations from CEntityOfProjectRepository
	// Additional custom query methods can be added here if needed
}
