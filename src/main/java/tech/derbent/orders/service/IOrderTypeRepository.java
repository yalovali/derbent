package tech.derbent.orders.service;

import tech.derbent.api.services.IEntityOfProjectRepository;
import tech.derbent.orders.domain.COrderType;

/** COrderTypeRepository - Repository interface for COrderType entities. Layer: Service (MVC) Provides data access operations for project-aware order
 * types, extending the standard CEntityOfProjectRepository to inherit common CRUD and query operations. */
public interface IOrderTypeRepository extends IEntityOfProjectRepository<COrderType> {
	// Inherits standard operations from CEntityOfProjectRepository
	// Additional custom query methods can be added here if needed
}
