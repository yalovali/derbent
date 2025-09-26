package tech.derbent.decisions.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.services.CEntityOfProjectRepository;
import tech.derbent.decisions.domain.CDecisionType;
import tech.derbent.projects.domain.CProject;

/** CDecisionTypeRepository - Repository interface for CDecisionType entities. Layer: Data Access (MVC) Provides data access methods for project-aware
 * decision type entities with eager loading support. */
public interface CDecisionTypeRepository extends CEntityOfProjectRepository<CDecisionType> {

	@Override
	@Query ("SELECT dt FROM CDecisionType dt " + "LEFT JOIN FETCH dt.project " + "WHERE dt.id = :id")
	Optional<CDecisionType> findById(@Param ("id") Long id);
	@Query ("SELECT dt FROM CDecisionType dt WHERE dt.project = :project AND dt.isActive = false")
	List<CDecisionType> findByProjectAndIsActiveFalse(@Param ("project") CProject project);
	@Query ("SELECT dt FROM CDecisionType dt WHERE dt.project = :project AND dt.isActive = true")
	List<CDecisionType> findByProjectAndIsActiveTrue(@Param ("project") CProject project);
	@Query ("SELECT dt FROM CDecisionType dt WHERE dt.project = :project AND dt.requiresApproval = true")
	List<CDecisionType> findByProjectAndRequiresApprovalTrue(@Param ("project") CProject project);
	@Query ("SELECT dt FROM CDecisionType dt WHERE dt.project = :project")
	List<CDecisionType> findByProjectOrderBySortOrderAsc(@Param ("project") CProject project);
}
