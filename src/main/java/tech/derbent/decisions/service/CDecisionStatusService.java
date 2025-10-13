package tech.derbent.decisions.service;

import java.util.Optional;
import tech.derbent.projects.domain.CProject;
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
	public String checkDeleteAllowed(final CDecisionStatus decisionStatus) {
		// Call super class first to check common dependencies
		final String superCheck = super.checkDeleteAllowed(decisionStatus);
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
		try {
			Optional<CProject> activeProject = sessionService.getActiveProject();
			if (activeProject.isPresent()) {
				long statusCount = ((IDecisionStatusRepository) repository).countByProject(activeProject.get());
				String autoName = String.format("DecisionStatus%02d", statusCount + 1);
				entity.setName(autoName);
			}
			LOGGER.debug("Initialized new cdecisionstatus");
		} catch (final Exception e) {
			LOGGER.error("Error initializing new cdecisionstatus", e);
			throw new IllegalStateException("Failed to initialize cdecisionstatus: " + e.getMessage(), e);
		}
	}
}
