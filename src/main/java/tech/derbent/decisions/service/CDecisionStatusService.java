package tech.derbent.decisions.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.CStatusService;
import tech.derbent.decisions.domain.CDecisionStatus;
import tech.derbent.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CDecisionStatusService extends CStatusService<CDecisionStatus> {

	public CDecisionStatusService(final IDecisionStatusRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Checks dependencies before allowing decision status deletion. Always calls super.checkDeleteAllowed() first to ensure all parent-level checks
	 * (null validation, non-deletable flag) are performed.
	 * @param entity the decision status entity to check
	 * @return null if status can be deleted, error message otherwise */
	@Override
	public String checkDeleteAllowed(final CDecisionStatus entity) {
		// Call super class first to check common dependencies
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		// No specific dependencies to check yet - stub for future implementation
		return null;
	}

	@Override
	protected Class<CDecisionStatus> getEntityClass() { return CDecisionStatus.class; }

	/** Initializes a new decision status with default values based on current session and available data.
	 * @param entity the newly created decision status to initialize
	 * @throws IllegalStateException if required fields cannot be initialized */
	@Override
	public void initializeNewEntity(final CDecisionStatus entity) {
		super.initializeNewEntity(entity);
		setNameOfEntity(entity, "Decision Status");
	}
}
