package tech.derbent.app.orders.type.service;

import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.orders.type.domain.COrderType;

/** COrderTypeRepository - Repository interface for COrderType entities. Layer: Service (MVC) Provides data access operations for project-aware order
 * types, extending the standard CEntityOfProjectRepository to inherit common CRUD and query operations. */
public interface IOrderTypeRepository extends IEntityOfProjectRepository<COrderType> {
	// Inherits standard operations from CEntityOfProjectRepository
	// Additional custom query methods can be added here if needed
}
