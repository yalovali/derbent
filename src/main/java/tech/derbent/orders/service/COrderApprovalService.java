package tech.derbent.orders.service;

import java.time.Clock;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.services.CEntityNamedService;
import tech.derbent.orders.domain.CApprovalStatus;
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
	protected Class<COrderApproval> getEntityClass() { return COrderApproval.class; }

	@Override
	public String checkDeleteAllowed(final COrderApproval entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public void initializeNewEntity(final COrderApproval entity) {
		super.initializeNewEntity(entity);
		// Get current user from session
		final CUser currentUser = sessionService.getActiveUser()
				.orElseThrow(() -> new CInitializationException("No active user in session - cannot initialize order approval"));
		// Initialize approver with current user
		entity.setApprover(currentUser);
		// Initialize approval level with default value (already set in domain, but ensure it's set)
		if (entity.getApprovalLevel() == null) {
			entity.setApprovalLevel(1);
		}
		// Note: This service is typically not used directly to create approvals
		// Approvals are usually created through COrder.addApproval() or similar methods
		// The order and approvalStatus fields MUST be set before saving
		// We cannot initialize them here without knowing which order this approval belongs to
	}
}
