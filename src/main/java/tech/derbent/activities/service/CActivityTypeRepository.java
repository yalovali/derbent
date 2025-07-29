package tech.derbent.activities.service;

import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.activities.domain.CActivityType;

/**
 * CActivityTypeRepository - Repository interface for CActivityType entity. Layer: Service (MVC) Provides data access
 * operations for project-aware activity types.
 */
public interface CActivityTypeRepository extends CEntityOfProjectRepository<CActivityType> {
    // Additional custom queries can be added here if needed
}