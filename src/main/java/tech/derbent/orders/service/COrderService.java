package tech.derbent.orders.service;

import java.time.Clock;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.orders.domain.COrder;
import tech.derbent.users.domain.CUser;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class COrderService extends CEntityOfProjectService<COrder> {
	COrderService(final COrderRepository repository, final Clock clock) {
		super(repository, clock);
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
	public void initializeLazyFields(final COrder entity) {
		Check.notNull(entity, "Entity cannot be null");
		try {
			super.initializeLazyFields(entity); // Handles CEntityOfProject relationships automatically
			initializeLazyRelationship(entity.getOrderType(), "orderType");
			initializeLazyRelationship(entity.getStatus(), "status");
			if ((entity.getApprovals() != null) && !entity.getApprovals().isEmpty()) {
				entity.getApprovals().forEach(approval -> initializeLazyRelationship(approval, "approval"));
			}
		} catch (final Exception e) {
			LOGGER.warn("Error initializing lazy fields for Order with ID: {}", entity.getId(), e);
		}
	}
}
