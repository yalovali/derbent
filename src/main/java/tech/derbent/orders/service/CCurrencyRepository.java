package tech.derbent.orders.service;

import tech.derbent.abstracts.services.CAbstractNamedRepository;
import tech.derbent.orders.domain.CCurrency;

/**
 * CCurrencyRepository - Repository interface for CCurrency entities. Layer: Service (MVC)
 * 
 * Provides data access operations for currencies, extending the standard CAbstractNamedRepository to inherit common
 * CRUD and query operations.
 */
public interface CCurrencyRepository extends CAbstractNamedRepository<CCurrency> {
    // Inherits standard operations from CAbstractNamedRepository
    // Additional custom query methods can be added here if needed
}