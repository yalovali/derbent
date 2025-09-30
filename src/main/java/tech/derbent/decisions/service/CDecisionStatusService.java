package tech.derbent.decisions.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.decisions.domain.CDecisionStatus;
import tech.derbent.session.service.CSessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CDecisionStatusService extends CEntityOfProjectService<CDecisionStatus> {

	public CDecisionStatusService(final IDecisionStatusRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected Class<CDecisionStatus> getEntityClass() { return CDecisionStatus.class; }
}
