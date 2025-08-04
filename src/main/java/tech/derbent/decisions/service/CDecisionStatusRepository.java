package tech.derbent.decisions.service;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.decisions.domain.CDecisionStatus;
import tech.derbent.projects.domain.CProject;

/**
 * CDecisionStatusRepository - Repository interface for CDecisionStatus entities. Layer:
 * Data Access (MVC) Provides data access methods for decision status entities. Since
 * CDecisionStatus extends CStatus which extends CTypeEntity which extends
 * CEntityOfProject, this repository must extend CEntityOfProjectRepository to provide
 * project-aware operations.
 */
public interface CDecisionStatusRepository
	extends CEntityOfProjectRepository<CDecisionStatus> {

	@Query (
		"SELECT ds FROM CDecisionStatus ds WHERE ds.project = :project ORDER BY ds.sortOrder ASC"
	)
	List<CDecisionStatus>
		findAllByProjectOrderBySortOrderAsc(@Param ("project") CProject project);
	@Query (
		"SELECT ds FROM CDecisionStatus ds WHERE ds.project = :project AND ds.isFinal = false"
	)
	List<CDecisionStatus>
		findByProjectAndIsFinalFalse(@Param ("project") CProject project);
	@Query (
		"SELECT ds FROM CDecisionStatus ds WHERE ds.project = :project AND ds.isFinal = false ORDER BY ds.sortOrder ASC"
	)
	List<CDecisionStatus> findByProjectAndIsFinalFalseOrderBySortOrderAsc(
		@Param ("project") CProject project);
	@Query (
		"SELECT ds FROM CDecisionStatus ds WHERE ds.project = :project AND ds.isFinal = true"
	)
	List<CDecisionStatus>
		findByProjectAndIsFinalTrue(@Param ("project") CProject project);
	@Query (
		"SELECT ds FROM CDecisionStatus ds WHERE ds.project = :project AND ds.requiresApproval = true"
	)
	List<CDecisionStatus>
		findByProjectAndRequiresApprovalTrue(@Param ("project") CProject project);
}