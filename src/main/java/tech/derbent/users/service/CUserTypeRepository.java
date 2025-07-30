package tech.derbent.users.service;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.users.domain.CUserType;

/**
 * CUserTypeRepository - Repository interface for CUserType entity. Layer: Service (MVC) Provides data access
 * operations for project-aware user types with eager loading support.
 */
public interface CUserTypeRepository extends CEntityOfProjectRepository<CUserType> {
    
    /**
     * Finds a user type by ID with eagerly loaded relationships.
     * @param id the user type ID
     * @return Optional containing the user type with loaded relationships
     */
    @Query("SELECT ut FROM CUserType ut " +
           "LEFT JOIN FETCH ut.project " +
           "LEFT JOIN FETCH ut.assignedTo " +
           "LEFT JOIN FETCH ut.createdBy " +
           "WHERE ut.id = :id")
    Optional<CUserType> findByIdWithRelationships(@Param("id") Long id);
}