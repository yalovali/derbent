package tech.derbent.decisions.service;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.decisions.domain.CDecisionType;
import tech.derbent.projects.domain.CProject;

/**
 * CDecisionTypeRepository - Repository interface for CDecisionType entities.
 * Layer: Data Access (MVC)
 * 
 * Provides data access methods for project-aware decision type entities.
 */
public interface CDecisionTypeRepository extends CEntityOfProjectRepository<CDecisionType> {

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