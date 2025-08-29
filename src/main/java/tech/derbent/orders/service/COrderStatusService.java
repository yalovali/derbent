package tech.derbent.orders.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.orders.domain.COrderStatus;
import tech.derbent.session.service.CSessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class COrderStatusService extends CEntityOfProjectService<COrderStatus> {
	COrderStatusService(final COrderStatusRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected Class<COrderStatus> getEntityClass() { return COrderStatus.class; }
}
