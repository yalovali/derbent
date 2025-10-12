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
	public String checkDependencies(final CDecisionType decisionType) {
		// Call super class first to check common dependencies
		final String superCheck = super.checkDependencies(decisionType);
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
		tech.derbent.api.utils.Check.notNull(entity, "Decision type cannot be null");
		tech.derbent.api.utils.Check.notNull(sessionService, "Session service is required for decision type initialization");
		try {
			java.util.Optional<tech.derbent.projects.domain.CProject> activeProject = sessionService.getActiveProject();
			tech.derbent.api.utils.Check.isTrue(activeProject.isPresent(),
					"No active project in session - project context is required to create decision types");
			tech.derbent.projects.domain.CProject currentProject = activeProject.get();
			entity.setProject(currentProject);
			java.util.Optional<tech.derbent.users.domain.CUser> currentUser = sessionService.getActiveUser();
			if (currentUser.isPresent()) {
				entity.setCreatedBy(currentUser.get());
			}
			long typeCount = ((IDecisionTypeRepository) repository).countByProject(currentProject);
			String autoName = String.format("DecisionType%02d", typeCount + 1);
			entity.setName(autoName);
			entity.setDescription("");
			entity.setColor(tech.derbent.decisions.domain.CDecisionType.DEFAULT_COLOR);
			entity.setSortOrder(100);
			entity.setAttributeNonDeletable(false);
			entity.setRequiresApproval(false);
			LOGGER.debug("Initialized new decision type with auto-generated name: {}", autoName);
		} catch (final Exception e) {
			LOGGER.error("Error initializing new decision type", e);
			throw new IllegalStateException("Failed to initialize decision type: " + e.getMessage(), e);
		}
	}
}
