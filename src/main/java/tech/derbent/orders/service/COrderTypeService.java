package tech.derbent.orders.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CTypeEntityService;
import tech.derbent.orders.domain.COrderType;
import tech.derbent.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class COrderTypeService extends CTypeEntityService<COrderType> {

	COrderTypeService(final IOrderTypeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Checks dependencies before allowing order type deletion. Always calls super.checkDeleteAllowed() first to ensure all parent-level checks (null
	 * validation, non-deletable flag) are performed.
	 * @param entity the order type entity to check
	 * @return null if type can be deleted, error message otherwise */
	@Override
	public String checkDeleteAllowed(final COrderType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		return null;
	}

	@Override
	protected Class<COrderType> getEntityClass() { return COrderType.class; }

	@Override
	public void initializeNewEntity(final COrderType entity) {
		super.initializeNewEntity(entity);
		setNameOfEntity(entity, "Order Type");
	}
}
