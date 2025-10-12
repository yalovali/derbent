package tech.derbent.orders.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CAbstractNamedEntityService;
import tech.derbent.orders.domain.COrderApproval;
import tech.derbent.session.service.ISessionService;

/** COrderApprovalService - Service layer for COrderApproval entity. Layer: Service (MVC) Handles business logic for order approval operations
 * including creation, validation, and management of order approval entities. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class COrderApprovalService extends CAbstractNamedEntityService<COrderApproval> {

	COrderApprovalService(final IOrderApprovalRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected Class<COrderApproval> getEntityClass() { return COrderApproval.class; }

	@Override
	public String checkDependencies(final COrderApproval entity) {
		final String superCheck = super.checkDependencies(entity);
		if (superCheck != null) {
			return superCheck;
		}
		return null;
	}

	@Override
	public void initializeNewEntity(final COrderApproval entity) {
		super.initializeNewEntity(entity);
		tech.derbent.api.utils.Check.notNull(entity, "Entity cannot be null");
		// Stub for future implementation
	}
}
