package tech.derbent.decisions.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.decisions.domain.CDecisionType;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.ISessionService;

/** CDecisionTypeService - Service class for CDecisionType entities. Layer: Service (MVC) Provides business logic operations for project-aware
 * decision type management including validation, creation, and status management. */
@Service
@PreAuthorize ("isAuthenticated()")
public class CDecisionTypeService extends CEntityOfProjectService<CDecisionType> {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CDecisionTypeService.class);

	public CDecisionTypeService(final IDecisionTypeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Finds all active decision types for a project.
	 * @param project the project
	 * @return list of active decision types for the project */
	@Transactional (readOnly = true)
	public List<CDecisionType> findAllActiveByProject(final CProject project) {
		Optional.ofNullable(project).orElse(null);
		return ((IDecisionTypeRepository) repository).findByProjectAndIsActiveTrue(project);
	}

	@Override
	protected Class<CDecisionType> getEntityClass() { return CDecisionType.class; }

	/** Checks dependencies before allowing decision type deletion.
	 * @param decisionType the decision type entity to check
	 * @return null if type can be deleted, error message otherwise */
	@Override
	public String checkDeleteAllowed(final CDecisionType decisionType) {
		// Call super class first to check common dependencies
		final String superCheck = super.checkDeleteAllowed(decisionType);
		if (superCheck != null) {
			return superCheck;
		}
		// No specific dependencies to check yet - stub for future implementation
		return null;
	}

	/** Initializes a new decision type with default values based on current session and available data.
	 * @param entity the newly created decision type to initialize
	 * @throws IllegalStateException if required fields cannot be initialized */
	@Override
	public void initializeNewEntity(final CDecisionType entity) {
		super.initializeNewEntity(entity);
		try {
			Optional<CProject> activeProject = sessionService.getActiveProject();
			if (activeProject.isPresent()) {
				long typeCount = ((IDecisionTypeRepository) repository).countByProject(activeProject.get());
				String autoName = String.format("DecisionType%02d", typeCount + 1);
				entity.setName(autoName);
			}
			LOGGER.debug("Initialized new cdecisiontype");
		} catch (final Exception e) {
			LOGGER.error("Error initializing new cdecisiontype", e);
			throw new IllegalStateException("Failed to initialize cdecisiontype: " + e.getMessage(), e);
		}
	}
}
