package tech.derbent.app.orders.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.app.orders.domain.COrderStatus;
import tech.derbent.api.services.CStatusService;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class COrderStatusService extends CStatusService<COrderStatus> {

	COrderStatusService(final IOrderStatusRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Checks dependencies before allowing order status deletion. Always calls super.checkDeleteAllowed() first to ensure all parent-level checks
	 * (null validation, non-deletable flag) are performed.
	 * @param entity the order status entity to check
	 * @return null if status can be deleted, error message otherwise */
	@Override
	public String checkDeleteAllowed(final COrderStatus entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		return null;
	}

	@Override
	protected Class<COrderStatus> getEntityClass() { return COrderStatus.class; }

	@Override
	public void initializeNewEntity(final COrderStatus entity) {
		super.initializeNewEntity(entity);
		setNameOfEntity(entity, "Order Status");
	}
}
