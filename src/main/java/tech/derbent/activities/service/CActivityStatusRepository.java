package tech.derbent.activities.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.derbent.activities.domain.CActivityStatus;

import java.util.List;
import java.util.Optional;

/**
 * CActivityStatusRepository - Repository interface for CActivityStatus entities.
 * Layer: Data Access (MVC)
 * Provides data access operations for activity status management.
 */
@Repository
public interface CActivityStatusRepository extends JpaRepository<CActivityStatus, Long> {

    /**
     * Find activity status by name (case-insensitive).
     * @param name the status name to search for - must not be null
     * @return Optional containing the status if found, empty otherwise
     */
    @Query("SELECT s FROM CActivityStatus s WHERE LOWER(s.name) = LOWER(:name)")
    Optional<CActivityStatus> findByNameIgnoreCase(@Param("name") String name);

    /**
     * Find all final status types (completed/cancelled).
     * @return List of final status types, ordered by sortOrder
     */
    @Query("SELECT s FROM CActivityStatus s WHERE s.isFinal = true ORDER BY s.sortOrder ASC")
    List<CActivityStatus> findAllFinalStatuses();

    /**
     * Find all non-final status types.
     * @return List of non-final status types, ordered by sortOrder
     */
    @Query("SELECT s FROM CActivityStatus s WHERE s.isFinal = false ORDER BY s.sortOrder ASC")
    List<CActivityStatus> findAllActiveStatuses();

    /**
     * Find all statuses ordered by sort order.
     * @return List of all statuses in sort order
     */
    @Query("SELECT s FROM CActivityStatus s ORDER BY s.sortOrder ASC, s.name ASC")
    List<CActivityStatus> findAllOrderedBySortOrder();

    /**
     * Check if a status name already exists (case-insensitive).
     * @param name the status name to check - must not be null
     * @return true if the name exists, false otherwise
     */
    @Query("SELECT COUNT(s) > 0 FROM CActivityStatus s WHERE LOWER(s.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    /**
     * Find the default status (typically used for new activities).
     * This assumes there's a convention for default status names.
     * @return Optional containing the default status if found
     */
    @Query("SELECT s FROM CActivityStatus s WHERE LOWER(s.name) IN ('todo', 'new', 'open', 'pending') ORDER BY s.sortOrder ASC")
    Optional<CActivityStatus> findDefaultStatus();
}