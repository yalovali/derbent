package tech.derbent.orders.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.orders.domain.COrderStatus;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class COrderStatusService extends CEntityOfProjectService<COrderStatus> {
	COrderStatusService(final COrderStatusRepository repository, final Clock clock) {
		super(repository, clock);
	}

	@Override
	protected Class<COrderStatus> getEntityClass() { return COrderStatus.class; }
}
