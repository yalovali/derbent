package tech.derbent.app.orders.approval.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.domain.CStatusService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.app.orders.approval.domain.CApprovalStatus;
import tech.derbent.base.session.service.ISessionService;

/** CApprovalStatusService - Service layer for CApprovalStatus entity. Layer: Service (MVC) Handles business logic for approval status operations
 * including creation, validation, and management of approval status entities. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CApprovalStatusService extends CStatusService<CApprovalStatus> implements IEntityRegistrable {

	CApprovalStatusService(final IApprovalStatusRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Checks dependencies before allowing approval status deletion. Always calls super.checkDeleteAllowed() first to ensure all parent-level checks
	 * (null validation, non-deletable flag) are performed.
	 * @param entity the approval status entity to check
	 * @return null if status can be deleted, error message otherwise */
	@Override
	public String checkDeleteAllowed(final CApprovalStatus entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		return null;
	}

	@Override
	public Class<CApprovalStatus> getEntityClass() { return CApprovalStatus.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CApprovalStatusInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceApprovalStatus.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CApprovalStatus entity) {
		super.initializeNewEntity(entity);
		setNameOfEntity(entity, "Approval Status");
	}
}
