package tech.derbent.decisions.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.decisions.domain.CDecisionStatus;
import tech.derbent.projects.domain.CProject;

/**
 * CDecisionStatusRepository - Repository interface for CDecisionStatus entities. Layer: Data Access (MVC) Provides data
 * access methods for decision status entities.
 * Since CDecisionStatus extends CStatus which extends CTypeEntity which extends CEntityOfProject,
 * this repository must extend CEntityOfProjectRepository to provide project-aware operations.
 */
public interface CDecisionStatusRepository extends CEntityOfProjectRepository<CDecisionStatus> {

    /**
     * Finds all decision statuses for a specific project ordered by sort order.
     * 
     * @param project the project to filter by
     * @return list of decision statuses sorted by sort order for the specified project
     */
    @Query("SELECT ds FROM CDecisionStatus ds WHERE ds.project = :project ORDER BY ds.sortOrder ASC")
    List<CDecisionStatus> findAllByProjectOrderBySortOrderAsc(@Param("project") CProject project);

    /**
     * Finds non-final decision statuses for a specific project.
     * 
     * @param project the project to filter by
     * @return list of non-final decision statuses for the specified project
     */
    @Query("SELECT ds FROM CDecisionStatus ds WHERE ds.project = :project AND ds.isFinal = false")
    List<CDecisionStatus> findByProjectAndIsFinalFalse(@Param("project") CProject project);

    /**
     * Finds non-final decision statuses for a specific project ordered by sort order.
     * 
     * @param project the project to filter by
     * @return list of non-final decision statuses sorted by sort order for the specified project
     */
    @Query("SELECT ds FROM CDecisionStatus ds WHERE ds.project = :project AND ds.isFinal = false ORDER BY ds.sortOrder ASC")
    List<CDecisionStatus> findByProjectAndIsFinalFalseOrderBySortOrderAsc(@Param("project") CProject project);

    /**
     * Finds final decision statuses for a specific project.
     * 
     * @param project the project to filter by
     * @return list of final decision statuses for the specified project
     */
    @Query("SELECT ds FROM CDecisionStatus ds WHERE ds.project = :project AND ds.isFinal = true")
    List<CDecisionStatus> findByProjectAndIsFinalTrue(@Param("project") CProject project);

    /**
     * Finds decision statuses that require approval for a specific project.
     * 
     * @param project the project to filter by
     * @return list of decision statuses that require approval for the specified project
     */
    @Query("SELECT ds FROM CDecisionStatus ds WHERE ds.project = :project AND ds.requiresApproval = true")
    List<CDecisionStatus> findByProjectAndRequiresApprovalTrue(@Param("project") CProject project);

    /**
     * @deprecated Use findAllByProjectOrderBySortOrderAsc(CProject) instead for project-aware queries.
     * This method violates the project-scoped query requirement.
     */
    @Deprecated
    List<CDecisionStatus> findAllByOrderBySortOrderAsc();

    /**
     * @deprecated Use findByProjectAndIsFinalFalse(CProject) instead for project-aware queries.
     * This method violates the project-scoped query requirement.
     */
    @Deprecated
    List<CDecisionStatus> findByIsFinalFalse();

    /**
     * @deprecated Use findByProjectAndIsFinalFalseOrderBySortOrderAsc(CProject) instead for project-aware queries.
     * This method violates the project-scoped query requirement.
     */
    @Deprecated
    List<CDecisionStatus> findByIsFinalFalseOrderBySortOrderAsc();

    /**
     * @deprecated Use findByProjectAndIsFinalTrue(CProject) instead for project-aware queries.
     * This method violates the project-scoped query requirement.
     */
    @Deprecated
    List<CDecisionStatus> findByIsFinalTrue();

    /**
     * @deprecated Use findByProjectAndRequiresApprovalTrue(CProject) instead for project-aware queries.
     * This method violates the project-scoped query requirement.
     */
    @Deprecated
    List<CDecisionStatus> findByRequiresApprovalTrue();

    /**
     * Finds a decision status by ID with eagerly loaded relationships to prevent LazyInitializationException. Note:
     * CDecisionStatus extends CStatus which doesn't have project relationship like CEntityOfProject, but we add this
     * for consistency and future extensibility.
     * 
     * @param id
     *            the decision status ID
     * @return optional CDecisionStatus with loaded relationships
     */
    @Query("SELECT s FROM CDecisionStatus s WHERE s.id = :id")
    Optional<CDecisionStatus> findByIdWithEagerLoading(@Param("id") Long id);
}