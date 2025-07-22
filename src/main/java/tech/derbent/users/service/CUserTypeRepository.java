package tech.derbent.users.service;

import tech.derbent.abstracts.services.CAbstractRepository;
import tech.derbent.users.domain.CUserType;

/**
 * CUserTypeRepository - Repository interface for CUserType entity. Layer: Service (MVC) Provides data access operations
 * for user types.
 */
public interface CUserTypeRepository extends CAbstractRepository<CUserType> {
    // Additional custom queries can be added here if needed
}