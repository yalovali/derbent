package tech.derbent.activities.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.projects.domain.CProject;

public interface CActivityRepository extends CEntityOfProjectRepository<CActivity> {

	/**
	 * Finds an activity by ID with eagerly loaded CActivityType to prevent
	 * LazyInitializationException.
	 * @param id the activity ID
	 * @return optional CActivity with loaded activityType
	 */
	@Query ("SELECT a FROM CActivity a LEFT JOIN FETCH a.activityType WHERE a.id = :id")
	Optional<CActivity> findByIdWithActivityType(@Param ("id") Long id);

	/**
	 * Finds an activity by ID with eagerly loaded CActivityType and CActivityStatus to
	 * prevent LazyInitializationException.
	 * @param id the activity ID
	 * @return optional CActivity with loaded activityType and status
	 */
	@Query (
		"SELECT a FROM CActivity a LEFT JOIN FETCH a.activityType LEFT JOIN FETCH a.status WHERE a.id = :id"
	)
	Optional<CActivity> findByIdWithActivityTypeAndStatus(@Param ("id") Long id);

	/**
	 * Finds an activity by ID with eagerly loaded CActivityType, CActivityStatus, and
	 * CProject to prevent LazyInitializationException. This extends the base method
	 * with activity-specific relationships.
	 * @param id the activity ID
	 * @return optional CActivity with loaded activityType, status, and project
	 */
	@Query (
		"SELECT a FROM CActivity a " +
		"LEFT JOIN FETCH a.project " +
		"LEFT JOIN FETCH a.assignedTo " +
		"LEFT JOIN FETCH a.createdBy " +
		"LEFT JOIN FETCH a.activityType " +
		"LEFT JOIN FETCH a.status " +
		"LEFT JOIN FETCH a.parentActivity " +
		"WHERE a.id = :id"
	)
	Optional<CActivity> findByIdWithAllRelationships(@Param ("id") Long id);

	/**
	 * Finds all activities by project with eagerly loaded CActivityType, CActivityStatus,
	 * and CProject to prevent LazyInitializationException. This method returns all
	 * activities without pagination with activity-specific relationships.
	 * @param project the project
	 * @return list of CActivity with loaded activityType, status, and project
	 */
	@Query (
		"SELECT a FROM CActivity a " +
		"LEFT JOIN FETCH a.project " +
		"LEFT JOIN FETCH a.assignedTo " +
		"LEFT JOIN FETCH a.createdBy " +
		"LEFT JOIN FETCH a.activityType " +
		"LEFT JOIN FETCH a.status " +
		"LEFT JOIN FETCH a.parentActivity " +
		"WHERE a.project = :project"
	)
	List<CActivity> findByProjectWithAllRelationships(@Param ("project") CProject project);

	/**
	 * Finds activities by project with eagerly loaded CActivityType, CActivityStatus, and
	 * CProject to prevent LazyInitializationException.
	 * @param project  the project
	 * @param pageable pagination information
	 * @return page of CActivity with loaded activityType, status, and project
	 */
	@Query (
		"SELECT a FROM CActivity a " +
		"LEFT JOIN FETCH a.project " +
		"LEFT JOIN FETCH a.assignedTo " +
		"LEFT JOIN FETCH a.createdBy " +
		"LEFT JOIN FETCH a.activityType " +
		"LEFT JOIN FETCH a.status " +
		"LEFT JOIN FETCH a.parentActivity " +
		"WHERE a.project = :project"
	)
	Page<CActivity> findByProjectWithAllRelationships(@Param ("project") CProject project,
		Pageable pageable);
}
