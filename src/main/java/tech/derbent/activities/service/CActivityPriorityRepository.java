package tech.derbent.activities.service;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.activities.domain.CActivityPriority;
import tech.derbent.projects.domain.CProject;

/**
 * CActivityPriorityRepository - Repository interface for CActivityPriority entities.
 * Layer: Data Access (MVC) Provides data access operations for activity priority
 * management.
 */
@Repository
public interface CActivityPriorityRepository
	extends CEntityOfProjectRepository<CActivityPriority> {

	@Query (
		"SELECT p FROM CActivityPriority p WHERE p.isDefault = true and p.project = :project"
	)
	Optional<CActivityPriority> findByIsDefaultTrue(@Param ("project") CProject project);
}