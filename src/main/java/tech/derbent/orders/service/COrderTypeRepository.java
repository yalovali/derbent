package tech.derbent.orders.service;

import tech.derbent.abstracts.services.CAbstractNamedRepository;
import tech.derbent.orders.domain.COrderType;

/**
 * COrderTypeRepository - Repository interface for COrderType entities.
 * Layer: Service (MVC)
 * 
 * Provides data access operations for order types, extending the standard
 * CAbstractNamedRepository to inherit common CRUD and query operations.
 */
public interface COrderTypeRepository extends CAbstractNamedRepository<COrderType> {
    // Inherits standard operations from CAbstractNamedRepository
    // Additional custom query methods can be added here if needed
}