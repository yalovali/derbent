package tech.derbent.meetings.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tech.derbent.abstracts.services.CAbstractNamedRepository;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.projects.domain.CProject;

/**
 * CMeetingStatusRepository - Repository interface for CMeetingStatus entities. Layer: Data Access (MVC) Provides data
 * access operations for meeting status management.
 */
@Repository
public interface CMeetingStatusRepository extends CAbstractNamedRepository<CMeetingStatus> {

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
     * Find all non-final status types.
     * 
     * @return List of non-final status types, ordered by sortOrder
     */
    @Query("SELECT s FROM CMeetingStatus s WHERE s.isFinal = false ORDER BY s.sortOrder ASC")
    List<CMeetingStatus> findAllActiveStatuses();

    /**
     * Find all meeting statuses for a specific project.
     * 
     * @param project
     *            the project to filter by
     * @return List of meeting statuses for the specified project, ordered by sortOrder
     */
    @Query("SELECT s FROM CMeetingStatus s WHERE s.project = :project ORDER BY s.sortOrder ASC, s.name ASC")
    List<CMeetingStatus> findAllByProject(@Param("project") CProject project);

    /**
     * Find all final status types (completed/cancelled).
     * 
     * @return List of final status types, ordered by sortOrder
     */
    @Query("SELECT s FROM CMeetingStatus s WHERE s.isFinal = true ORDER BY s.sortOrder ASC")
    List<CMeetingStatus> findAllFinalStatuses();

    /**
     * Find all statuses ordered by sort order.
     * 
     * @return List of all statuses in sort order
     */
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