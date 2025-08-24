package tech.derbent.orders.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.orders.domain.COrderType;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class COrderTypeService extends CEntityOfProjectService<COrderType> {
	COrderTypeService(final COrderTypeRepository repository, final Clock clock) {
		super(repository, clock);
	}

	@Override
	protected Class<COrderType> getEntityClass() { return COrderType.class; }
}
