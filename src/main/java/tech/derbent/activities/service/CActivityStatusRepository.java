package tech.derbent.activities.service;

import tech.derbent.abstracts.services.CAbstractRepository;
import tech.derbent.activities.domain.CActivityStatus;

/**
 * CActivityStatusRepository - Repository interface for CActivityStatus entities.
 * Layer: Service (MVC)
 * 
 * Extends CAbstractRepository to provide standard CRUD operations for activity status entities.
 * Additional custom queries can be added here as needed.
 */
public interface CActivityStatusRepository extends CAbstractRepository<CActivityStatus> {
    
    // Additional custom query methods can be added here if needed
    // For example: findByNameIgnoreCase, findByOrderByName, etc.
}