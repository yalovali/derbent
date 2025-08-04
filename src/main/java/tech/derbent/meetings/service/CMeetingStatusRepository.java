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
 * CMeetingStatusRepository - Repository interface for CMeetingStatus entities. Layer:
 * Data Access (MVC) Provides data access operations for meeting status management. Since
 * CMeetingStatus extends CStatus which extends CTypeEntity which extends
 * CEntityOfProject, this repository must extend CEntityOfProjectRepository to provide
 * project-aware operations.
 */
@Repository
public interface CMeetingStatusRepository
	extends CEntityOfProjectRepository<CMeetingStatus> {

	/**
	 * Find all final status types (completed/cancelled) for a specific project. This
	 * replaces the non-project-aware findAllFinalStatuses method.
	 * @param project the project to filter by
	 * @return List of final status types for the project, ordered by sortOrder
	 */
	@Query (
		"SELECT s FROM CMeetingStatus s WHERE s.finalStatus = true AND s.project = :project ORDER BY s.sortOrder ASC"
	)
	List<CMeetingStatus>
		findAllFinalStatusesByProject(@Param ("project") CProject project);
	/**
	 * Find the default status (typically used for new meetings). This assumes there's a
	 * convention for default status names.
	 * @return Optional containing the default status if found
	 */
	@Query (
		"SELECT s FROM CMeetingStatus s WHERE LOWER(s.name) IN ('planned', 'scheduled', 'new', 'pending') AND s.project = :project ORDER BY s.sortOrder ASC"
	)
	Optional<CMeetingStatus> findDefaultStatus(@Param ("project") CProject project);
}