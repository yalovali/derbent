package tech.derbent.activities.service;

import tech.derbent.abstracts.services.CAbstractNamedRepository;
import tech.derbent.activities.domain.CActivityType;

/**
 * CActivityTypeRepository - Repository interface for CActivityType entity. Layer: Service (MVC) Provides data access
 * operations for activity types.
 */
public interface CActivityTypeRepository extends CAbstractNamedRepository<CActivityType> {
    // Additional custom queries can be added here if needed
}