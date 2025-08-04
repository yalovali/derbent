package tech.derbent.meetings.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.projects.domain.CProject;

/**
 * CMeetingStatusRepository - Repository interface for CMeetingStatus entities. Layer: Data Access (MVC) Provides data
 * access operations for meeting status management.
 * Since CMeetingStatus extends CStatus which extends CTypeEntity which extends CEntityOfProject,
 * this repository must extend CEntityOfProjectRepository to provide project-aware operations.
 */
@Repository
public interface CMeetingStatusRepository extends CEntityOfProjectRepository<CMeetingStatus> {

    /**
     * Check if a status name already exists (case-insensitive).
     * 
     * @param name
     *            the status name to check - must not be null
     * @return true if the name exists, false otherwise
     */
    @Override
    @Query("SELECT COUNT(s) > 0 FROM CMeetingStatus s WHERE LOWER(s.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    /**
     * Find all non-final status types for a specific project.
     * This replaces the non-project-aware findAllActiveStatuses method.
     * 
     * @param project the project to filter by
     * @return List of non-final status types for the project, ordered by sortOrder
     */
    @Query("SELECT s FROM CMeetingStatus s WHERE s.isFinal = false AND s.project = :project ORDER BY s.sortOrder ASC")
    List<CMeetingStatus> findAllActiveStatusesByProject(@Param("project") CProject project);

    /**
     * Find all final status types (completed/cancelled) for a specific project.
     * This replaces the non-project-aware findAllFinalStatuses method.
     * 
     * @param project the project to filter by
     * @return List of final status types for the project, ordered by sortOrder
     */
    @Query("SELECT s FROM CMeetingStatus s WHERE s.isFinal = true AND s.project = :project ORDER BY s.sortOrder ASC")
    List<CMeetingStatus> findAllFinalStatusesByProject(@Param("project") CProject project);

    /**
     * Find all statuses ordered by sort order.
     * @deprecated Use findAllByProject(CProject) instead for project-aware queries.
     * This method violates the project-scoped query requirement.
     * 
     * @return List of all statuses in sort order
     */
    @Deprecated
    @Query("SELECT s FROM CMeetingStatus s ORDER BY s.sortOrder ASC, s.name ASC")
    List<CMeetingStatus> findAllOrderedBySortOrder();

    /**
     * Find meeting status by name and project (case-insensitive).
     * 
     * @param name
     *            the status name to search for - must not be null
     * @param project
     *            the project to search within - may be null for global statuses
     * @return Optional containing the status if found, empty otherwise
     */
    @Query("SELECT s FROM CMeetingStatus s WHERE LOWER(s.name) = LOWER(:name) AND s.project = :project")
    Optional<CMeetingStatus> findByNameAndProject(@Param("name") String name, @Param("project") CProject project);

    /**
     * Find meeting status by name (case-insensitive).
     * 
     * @param name
     *            the status name to search for - must not be null
     * @return Optional containing the status if found, empty otherwise
     */
    @Override
    @Query("SELECT s FROM CMeetingStatus s WHERE LOWER(s.name) = LOWER(:name)")
    Optional<CMeetingStatus> findByNameIgnoreCase(@Param("name") String name);

    /**
     * Find the default status (typically used for new meetings). This assumes there's a convention for default status
     * names.
     * 
     * @return Optional containing the default status if found
     */
    @Query("SELECT s FROM CMeetingStatus s WHERE LOWER(s.name) IN ('planned', 'scheduled', 'new', 'pending') ORDER BY s.sortOrder ASC")
    Optional<CMeetingStatus> findDefaultStatus();
}