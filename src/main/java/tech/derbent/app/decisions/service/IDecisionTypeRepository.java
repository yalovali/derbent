package tech.derbent.app.decisions.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.decisions.domain.CDecisionType;
import tech.derbent.app.projects.domain.CProject;

/** CDecisionTypeRepository - Repository interface for CDecisionType entities. Layer: Data Access (MVC) Provides data access methods for project-aware
 * decision type entities with eager loading support. */
public interface IDecisionTypeRepository extends IEntityOfProjectRepository<CDecisionType> {

	@Override
	@Query ("SELECT dt FROM CDecisionType dt " + "LEFT JOIN FETCH dt.project " + "WHERE dt.id = :id")
	Optional<CDecisionType> findById(@Param ("id") Long id);
	@Query ("SELECT dt FROM CDecisionType dt WHERE dt.project = :project AND dt.active = false")
	List<CDecisionType> findByProjectAndActiveFalse(@Param ("project") CProject project);
	@Query ("SELECT dt FROM CDecisionType dt WHERE dt.project = :project AND dt.active = true")
	List<CDecisionType> findByProjectAndActiveTrue(@Param ("project") CProject project);
	@Query ("SELECT dt FROM CDecisionType dt WHERE dt.project = :project AND dt.requiresApproval = true")
	List<CDecisionType> findByProjectAndRequiresApprovalTrue(@Param ("project") CProject project);
	@Query ("SELECT dt FROM CDecisionType dt WHERE dt.project = :project")
	List<CDecisionType> findByProjectOrderBySortOrderAsc(@Param ("project") CProject project);
}
