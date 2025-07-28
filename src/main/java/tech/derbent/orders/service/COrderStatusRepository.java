package tech.derbent.orders.service;

import tech.derbent.abstracts.services.CAbstractNamedRepository;
import tech.derbent.orders.domain.COrderStatus;

/**
 * COrderStatusRepository - Repository interface for COrderStatus entities.
 * Layer: Service (MVC)
 * 
 * Provides data access operations for order statuses, extending the standard
 * CAbstractNamedRepository to inherit common CRUD and query operations.
 */
public interface COrderStatusRepository extends CAbstractNamedRepository<COrderStatus> {
    // Inherits standard operations from CAbstractNamedRepository
    // Additional custom query methods can be added here if needed
}