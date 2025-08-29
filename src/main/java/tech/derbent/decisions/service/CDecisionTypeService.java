package tech.derbent.decisions.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.decisions.domain.CDecisionType;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

/** CDecisionTypeService - Service class for CDecisionType entities. Layer: Service (MVC) Provides business logic operations for project-aware
 * decision type management including validation, creation, and status management. */
@Service
@PreAuthorize ("isAuthenticated()")
public class CDecisionTypeService extends CEntityOfProjectService<CDecisionType> {
	public CDecisionTypeService(final CDecisionTypeRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Finds all active decision types for a project.
	 * @param project the project
	 * @return list of active decision types for the project */
	@Transactional (readOnly = true)
	public List<CDecisionType> findAllActiveByProject(final CProject project) {
		Optional.ofNullable(project).orElse(null);
		return ((CDecisionTypeRepository) repository).findByProjectAndIsActiveTrue(project);
	}

	/** Finds decision types that require approval for a project.
	 * @param project the project
	 * @return list of decision types that require approval for the project */
	@Transactional (readOnly = true)
	public List<CDecisionType> findRequiringApprovalByProject(final CProject project) {
		Optional.ofNullable(project).orElse(null);
		return ((CDecisionTypeRepository) repository).findByProjectAndRequiresApprovalTrue(project);
	}

	@Override
	protected Class<CDecisionType> getEntityClass() { return CDecisionType.class; }

	/** Updates the sort order of a decision type.
	 * @param decisionType the decision type to update - must not be null
	 * @param sortOrder    the new sort order
	 * @return the updated decision type */
	@Transactional
	public CDecisionType updateSortOrder(final CDecisionType decisionType, final Integer sortOrder) {
		Check.notNull(decisionType, "Decision type cannot be null");
		decisionType.setSortOrder(sortOrder);
		return repository.saveAndFlush(decisionType);
	}
}
