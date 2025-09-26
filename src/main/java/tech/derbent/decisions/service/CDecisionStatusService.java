package tech.derbent.decisions.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.api.utils.Check;
import tech.derbent.decisions.domain.CDecisionStatus;
import tech.derbent.session.service.CSessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CDecisionStatusService extends CEntityOfProjectService<CDecisionStatus> {

	public CDecisionStatusService(final CDecisionStatusRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected Class<CDecisionStatus> getEntityClass() { return CDecisionStatus.class; }

	@Transactional
	public CDecisionStatus updateSortOrder(final CDecisionStatus decisionStatus, final Integer sortOrder) {
		Check.notNull(decisionStatus, "Decision status cannot be null");
		decisionStatus.setSortOrder(sortOrder);
		return repository.saveAndFlush(decisionStatus);
	}

	@Transactional
	public CDecisionStatus updateStatusProperties(final CDecisionStatus decisionStatus, final boolean requiresApproval) {
		Check.notNull(decisionStatus, "Decision status cannot be null");
		decisionStatus.setRequiresApproval(requiresApproval);
		return repository.saveAndFlush(decisionStatus);
	}
}
