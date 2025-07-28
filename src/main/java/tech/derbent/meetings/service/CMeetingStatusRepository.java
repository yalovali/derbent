package tech.derbent.meetings.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tech.derbent.abstracts.services.CAbstractRepository;
import tech.derbent.meetings.domain.CMeetingStatus;

/**
 * CMeetingStatusRepository - Repository interface for CMeetingStatus entities. Layer:
 * Data Access (MVC) Provides data access operations for meeting status management.
 */
@Repository
public interface CMeetingStatusRepository extends CAbstractRepository<CMeetingStatus> {

	/**
	 * Check if a status name already exists (case-insensitive).
	 * @param name the status name to check - must not be null
	 * @return true if the name exists, false otherwise
	 */
	@Query("SELECT COUNT(s) > 0 FROM CMeetingStatus s WHERE LOWER(s.name) = LOWER(:name)")
	boolean existsByNameIgnoreCase(@Param("name") String name);

	/**
	 * Find all non-final status types.
	 * @return List of non-final status types, ordered by sortOrder
	 */
	@Query("SELECT s FROM CMeetingStatus s WHERE s.isFinal = false ORDER BY s.sortOrder ASC")
	List<CMeetingStatus> findAllActiveStatuses();

	/**
	 * Find all final status types (completed/cancelled).
	 * @return List of final status types, ordered by sortOrder
	 */
	@Query("SELECT s FROM CMeetingStatus s WHERE s.isFinal = true ORDER BY s.sortOrder ASC")
	List<CMeetingStatus> findAllFinalStatuses();

	/**
	 * Find all statuses ordered by sort order.
	 * @return List of all statuses in sort order
	 */
	@Query("SELECT s FROM CMeetingStatus s ORDER BY s.sortOrder ASC, s.name ASC")
	List<CMeetingStatus> findAllOrderedBySortOrder();

	/**
	 * Find meeting status by name (case-insensitive).
	 * @param name the status name to search for - must not be null
	 * @return Optional containing the status if found, empty otherwise
	 */
	@Query("SELECT s FROM CMeetingStatus s WHERE LOWER(s.name) = LOWER(:name)")
	Optional<CMeetingStatus> findByNameIgnoreCase(@Param("name") String name);

	/**
	 * Find the default status (typically used for new meetings). This assumes there's a
	 * convention for default status names.
	 * @return Optional containing the default status if found
	 */
	@Query("SELECT s FROM CMeetingStatus s WHERE LOWER(s.name) IN ('planned', 'scheduled', 'new', 'pending') ORDER BY s.sortOrder ASC")
	Optional<CMeetingStatus> findDefaultStatus();
}