package tech.derbent.activities.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.projects.domain.CProject;

/**
 * CActivityStatusRepository - Repository interface for CActivityStatus entities. Layer:
 * Data Access (MVC) Provides data access operations for activity status management.
 */
@Repository
public interface CActivityStatusRepository
	extends CEntityOfProjectRepository<CActivityStatus> {

	@Query (
		"SELECT s FROM CActivityStatus s WHERE s.finalStatus = true and s.project = :project ORDER BY s.sortOrder ASC"
	)
	List<CActivityStatus> findAllFinalStatuses(@Param ("project") CProject project);
	@Query ("SELECT s FROM CActivityStatus s ORDER BY s.sortOrder ASC, s.name ASC")
	List<CActivityStatus> findAllOrderedBySortOrder();
	@Query (
		"SELECT s FROM CActivityStatus s WHERE LOWER(s.name) IN ('todo', 'new', 'open', 'pending') AND s.project = :project ORDER BY s.sortOrder ASC"
	)
	Optional<CActivityStatus> findDefaultStatus(@Param ("project") CProject project);
}