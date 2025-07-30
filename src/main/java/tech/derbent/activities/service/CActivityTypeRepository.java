package tech.derbent.activities.service;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.activities.domain.CActivityType;

/**
 * CActivityTypeRepository - Repository interface for CActivityType entity. Layer: Service (MVC) Provides data access
 * operations for project-aware activity types with eager loading support.
 */
public interface CActivityTypeRepository extends CEntityOfProjectRepository<CActivityType> {
    
    /**
     * Finds an activity type by ID with eagerly loaded relationships.
     * @param id the activity type ID
     * @return Optional containing the activity type with loaded relationships
     */
    @Query("SELECT at FROM CActivityType at " +
           "LEFT JOIN FETCH at.project " +
           "LEFT JOIN FETCH at.assignedTo " +
           "LEFT JOIN FETCH at.createdBy " +
           "WHERE at.id = :id")
    Optional<CActivityType> findByIdWithRelationships(@Param("id") Long id);
}