package tech.derbent.activities.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.derbent.activities.domain.CActivityPriority;

import java.util.List;
import java.util.Optional;

/**
 * CActivityPriorityRepository - Repository interface for CActivityPriority entities.
 * Layer: Data Access (MVC)
 * Provides data access operations for activity priority management.
 */
@Repository
public interface CActivityPriorityRepository extends JpaRepository<CActivityPriority, Long> {

    /**
     * Find activity priority by name (case-insensitive).
     * @param name the priority name to search for - must not be null
     * @return Optional containing the priority if found, empty otherwise
     */
    @Query("SELECT p FROM CActivityPriority p WHERE LOWER(p.name) = LOWER(:name)")
    Optional<CActivityPriority> findByNameIgnoreCase(@Param("name") String name);

    /**
     * Find all priorities ordered by priority level (1=highest, 5=lowest).
     * @return List of priorities in priority order
     */
    @Query("SELECT p FROM CActivityPriority p ORDER BY p.priorityLevel ASC, p.name ASC")
    List<CActivityPriority> findAllOrderedByPriorityLevel();

    /**
     * Find all high priority levels (level 1 or 2).
     * @return List of high priority types
     */
    @Query("SELECT p FROM CActivityPriority p WHERE p.priorityLevel <= 2 ORDER BY p.priorityLevel ASC")
    List<CActivityPriority> findAllHighPriorities();

    /**
     * Find all low priority levels (level 4 or 5).
     * @return List of low priority types
     */
    @Query("SELECT p FROM CActivityPriority p WHERE p.priorityLevel >= 4 ORDER BY p.priorityLevel ASC")
    List<CActivityPriority> findAllLowPriorities();

    /**
     * Find the default priority.
     * @return Optional containing the default priority if found
     */
    @Query("SELECT p FROM CActivityPriority p WHERE p.isDefault = true")
    Optional<CActivityPriority> findDefaultPriority();

    /**
     * Find priority by priority level.
     * @param priorityLevel the numeric priority level - must not be null
     * @return Optional containing the priority if found, empty otherwise
     */
    @Query("SELECT p FROM CActivityPriority p WHERE p.priorityLevel = :priorityLevel")
    Optional<CActivityPriority> findByPriorityLevel(@Param("priorityLevel") Integer priorityLevel);

    /**
     * Check if a priority name already exists (case-insensitive).
     * @param name the priority name to check - must not be null
     * @return true if the name exists, false otherwise
     */
    @Query("SELECT COUNT(p) > 0 FROM CActivityPriority p WHERE LOWER(p.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    /**
     * Check if a priority level already exists.
     * @param priorityLevel the priority level to check - must not be null  
     * @return true if the level exists, false otherwise
     */
    @Query("SELECT COUNT(p) > 0 FROM CActivityPriority p WHERE p.priorityLevel = :priorityLevel")
    boolean existsByPriorityLevel(@Param("priorityLevel") Integer priorityLevel);
}