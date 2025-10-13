package tech.derbent.orders.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.services.CEntityNamedService;
import tech.derbent.orders.domain.COrderApproval;
import tech.derbent.session.service.ISessionService;
import tech.derbent.users.domain.CUser;

/** COrderApprovalService - Service layer for COrderApproval entity. Layer: Service (MVC) Handles business logic for order approval operations
 * including creation, validation, and management of order approval entities. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class COrderApprovalService extends CEntityNamedService<COrderApproval> {

	private final CApprovalStatusService approvalStatusService;

	COrderApprovalService(final IOrderApprovalRepository repository, final Clock clock, final ISessionService sessionService,
			final CApprovalStatusService approvalStatusService) {
		super(repository, clock, sessionService);
		this.approvalStatusService = approvalStatusService;
	}

	@Override
	public String checkDeleteAllowed(final COrderApproval entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	protected Class<COrderApproval> getEntityClass() { return COrderApproval.class; }

	@Override
	public void initializeNewEntity(final COrderApproval entity) {
		super.initializeNewEntity(entity);
		// Get current user from session
		final CUser currentUser = sessionService.getActiveUser()
				.orElseThrow(() -> new CInitializationException("No active user in session - cannot initialize order approval"));
		entity.setApprover(currentUser);
		if (entity.getApprovalLevel() == null) {
			entity.setApprovalLevel(1);
		}
	}
}
