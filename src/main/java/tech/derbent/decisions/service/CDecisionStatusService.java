package tech.derbent.decisions.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.decisions.domain.CDecisionStatus;
import tech.derbent.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CDecisionStatusService extends CEntityOfProjectService<CDecisionStatus> {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CDecisionStatusService.class);

	public CDecisionStatusService(final IDecisionStatusRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected Class<CDecisionStatus> getEntityClass() { return CDecisionStatus.class; }

	/** Checks dependencies before allowing decision status deletion.
	 * @param decisionStatus the decision status entity to check
	 * @return null if status can be deleted, error message otherwise */
	@Override
	public String checkDependencies(final CDecisionStatus decisionStatus) {
		// Call super class first to check common dependencies
		final String superCheck = super.checkDependencies(decisionStatus);
		if (superCheck != null) {
			return superCheck;
		}
		// No specific dependencies to check yet - stub for future implementation
		return null;
	}

	/** Initializes a new decision status with default values based on current session and available data.
	 * @param entity the newly created decision status to initialize
	 * @throws IllegalStateException if required fields cannot be initialized */
	@Override
	public void initializeNewEntity(final CDecisionStatus entity) {
		super.initializeNewEntity(entity);
		tech.derbent.api.utils.Check.notNull(entity, "Decision status cannot be null");
		tech.derbent.api.utils.Check.notNull(sessionService, "Session service is required for decision status initialization");
		try {
			java.util.Optional<tech.derbent.projects.domain.CProject> activeProject = sessionService.getActiveProject();
			tech.derbent.api.utils.Check.isTrue(activeProject.isPresent(),
					"No active project in session - project context is required to create decision statuses");
			tech.derbent.projects.domain.CProject currentProject = activeProject.get();
			entity.setProject(currentProject);
			java.util.Optional<tech.derbent.users.domain.CUser> currentUser = sessionService.getActiveUser();
			if (currentUser.isPresent()) {
				entity.setCreatedBy(currentUser.get());
			}
			long statusCount = ((IDecisionStatusRepository) repository).countByProject(currentProject);
			String autoName = String.format("DecisionStatus%02d", statusCount + 1);
			entity.setName(autoName);
			entity.setDescription("");
			entity.setColor("#4A90E2");
			entity.setSortOrder(100);
			entity.setAttributeNonDeletable(false);
			LOGGER.debug("Initialized new decision status with auto-generated name: {}", autoName);
		} catch (final Exception e) {
			LOGGER.error("Error initializing new decision status", e);
			throw new IllegalStateException("Failed to initialize decision status: " + e.getMessage(), e);
		}
	}
}
