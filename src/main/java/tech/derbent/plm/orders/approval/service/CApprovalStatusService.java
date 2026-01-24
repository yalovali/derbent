package tech.derbent.plm.orders.approval.service;

import java.time.Clock;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfCompany.service.CStatusService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.orders.approval.domain.CApprovalStatus;

/** CApprovalStatusService - Service layer for CApprovalStatus entity. Layer: Service (MVC) Handles business logic for approval status operations
 * including creation, validation, and management of approval status entities. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CApprovalStatusService extends CStatusService<CApprovalStatus> implements IEntityRegistrable, IEntityWithView {

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
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	@Override
	@Transactional
	public CApprovalStatus save(final CApprovalStatus entity) {
		final CApprovalStatus saved = super.save(entity);
		final IApprovalStatusRepository approvalStatusRepository = (IApprovalStatusRepository) repository;
		return approvalStatusRepository.findByIdWithCompany(saved.getId()).orElse(saved);
	}

	@Override
	protected void validateEntity(final CApprovalStatus entity) throws CValidationException {
		super.validateEntity(entity);
		// Unique Name Check
		final Optional<CApprovalStatus> existing =
				((IApprovalStatusRepository) repository).findByNameAndCompany(entity.getName(), entity.getCompany());
		if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
			throw new CValidationException(ValidationMessages.DUPLICATE_NAME_IN_COMPANY);
		}
	}
}
