package tech.derbent.orders.service;

import java.time.Clock;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.api.utils.Check;
import tech.derbent.orders.domain.COrder;
import tech.derbent.session.service.ISessionService;
import tech.derbent.users.domain.CUser;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class COrderService extends CEntityOfProjectService<COrder> {

	COrderService(final IOrderRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final COrder order) {
		return super.checkDeleteAllowed(order);
	}

	public List<COrder> findByRequestor(final CUser requestor) {
		LOGGER.info("findByRequestor called with requestor: {}", requestor != null ? requestor.getName() : "null");
		Check.notNull(requestor, "Requestor cannot be null");
		return ((COrderService) repository).findByRequestor(requestor);
	}

	public List<COrder> findByResponsible(final CUser responsible) {
		LOGGER.info("findByResponsible called with responsible: {}", responsible != null ? responsible.getName() : "null");
		Check.notNull(responsible, "Responsible user cannot be null");
		return ((COrderService) repository).findByResponsible(responsible);
	}

	@Override
	protected Class<COrder> getEntityClass() { return COrder.class; }

	@Override
	public void initializeNewEntity(final COrder entity) {
		super.initializeNewEntity(entity);
		entity.setCurrency();
		entity.setActualCost(0); // Default actual cost
		entity.setEstimatedCost(0); // Default estimated cost
		entity.setOrderDate(now());
		entity.setDueDate(now().plusDays(7)); // Default due date one week from
		entity.setRequestor(getCurrentUser());
		entity.setResponsible(getCurrentUser());
		entity.setStatus(); // Default status
	}
}
