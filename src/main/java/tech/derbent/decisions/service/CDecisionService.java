package tech.derbent.decisions.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.session.service.ISessionService;

/** CDecisionService - Service class for CDecision entities. Layer: Service (MVC) Provides business logic operations for decision management including
 * validation, creation, approval workflow management, and project-based queries. */
@Service
@PreAuthorize ("isAuthenticated()")
public class CDecisionService extends CEntityOfProjectService<CDecision> {

	public CDecisionService(final IDecisionRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDependencies(final CDecision decision) {
		return super.checkDependencies(decision);
	}

	@Override
	protected Class<CDecision> getEntityClass() { return CDecision.class; }

	@Override
	public void initializeNewEntity(final CDecision entity) {
		super.initializeNewEntity(entity);
		// Additional entity-specific initialization can be added here if needed
	}
}
