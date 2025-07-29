package tech.derbent.users.service;

import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.users.domain.CUserType;

/**
 * CUserTypeRepository - Repository interface for CUserType entity. Layer: Service (MVC) Provides data access operations
 * for project-aware user types.
 */
public interface CUserTypeRepository extends CEntityOfProjectRepository<CUserType> {
    // Additional custom queries can be added here if needed
}