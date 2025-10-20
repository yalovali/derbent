package tech.derbent.app.activities.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.derbent.api.services.IEntityOfProjectRepository;
import tech.derbent.app.activities.domain.CActivityPriority;
import tech.derbent.app.projects.domain.CProject;

/** CActivityPriorityRepository - Repository interface for CActivityPriority entities. Layer: Data Access (MVC) Provides data access operations for
 * activity priority management. */
@Repository
public interface IActivityPriorityRepository extends IEntityOfProjectRepository<CActivityPriority> {

	@Query ("SELECT p FROM CActivityPriority p WHERE p.isDefault = true and p.project = :project")
	Optional<CActivityPriority> findByIsDefaultTrue(@Param ("project") CProject project);
}
