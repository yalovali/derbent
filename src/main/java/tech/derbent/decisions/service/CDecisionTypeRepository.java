package tech.derbent.decisions.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.decisions.domain.CDecisionType;
import tech.derbent.projects.domain.CProject;

/**
 * CDecisionTypeRepository - Repository interface for CDecisionType entities.
 * Layer: Data Access (MVC)
 * 
 * Provides data access methods for project-aware decision type entities with eager loading support.
 */
public interface CDecisionTypeRepository extends CEntityOfProjectRepository<CDecisionType> {

    /**
     * Finds a decision type by ID with eagerly loaded relationships.
     * @param id the decision type ID
     * @return Optional containing the decision type with loaded relationships
     */
    @Query("SELECT dt FROM CDecisionType dt " +
           "LEFT JOIN FETCH dt.project " +
           "LEFT JOIN FETCH dt.assignedTo " +
           "LEFT JOIN FETCH dt.createdBy " +
           "WHERE dt.id = :id")
    Optional<CDecisionType> findByIdWithRelationships(@Param("id") Long id);

    /**
     * Finds all active decision types for a project.
     * @param project the project
     * @return list of active decision types for the project
     */
    @Query("SELECT dt FROM CDecisionType dt WHERE dt.project = :project AND dt.isActive = true ORDER BY dt.sortOrder ASC")
    List<CDecisionType> findByProjectAndIsActiveTrue(@Param("project") CProject project);

    /**
     * Finds all inactive decision types for a project.
     * @param project the project
     * @return list of inactive decision types for the project
     */
    @Query("SELECT dt FROM CDecisionType dt WHERE dt.project = :project AND dt.isActive = false ORDER BY dt.sortOrder ASC")
    List<CDecisionType> findByProjectAndIsActiveFalse(@Param("project") CProject project);

    /**
     * Finds decision types that require approval for a project.
     * @param project the project
     * @return list of decision types that require approval for the project
     */
    @Query("SELECT dt FROM CDecisionType dt WHERE dt.project = :project AND dt.requiresApproval = true ORDER BY dt.sortOrder ASC")
    List<CDecisionType> findByProjectAndRequiresApprovalTrue(@Param("project") CProject project);

    /**
     * Finds all decision types for a project ordered by sort order.
     * @param project the project
     * @return list of decision types for the project sorted by sort order
     */
    @Query("SELECT dt FROM CDecisionType dt WHERE dt.project = :project ORDER BY dt.sortOrder ASC")
    List<CDecisionType> findByProjectOrderBySortOrderAsc(@Param("project") CProject project);
}