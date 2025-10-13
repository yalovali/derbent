package tech.derbent.orders.service;

import java.util.Optional;
import tech.derbent.projects.domain.CProject;
import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CStatusService;
import tech.derbent.orders.domain.CApprovalStatus;
import tech.derbent.session.service.ISessionService;

/** CApprovalStatusService - Service layer for CApprovalStatus entity. Layer: Service (MVC) Handles business logic for approval status operations
 * including creation, validation, and management of approval status entities. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CApprovalStatusService extends CStatusService<CApprovalStatus> {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CApprovalStatusService.class);

	CApprovalStatusService(final IApprovalStatusRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected Class<CApprovalStatus> getEntityClass() { return CApprovalStatus.class; }

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
	public void initializeNewEntity(final CApprovalStatus entity) {
		super.initializeNewEntity(entity);
		try {
			Optional<CProject> activeProject = sessionService.getActiveProject();
			if (activeProject.isPresent()) {
				long statusCount = ((IApprovalStatusRepository) repository).countByProject(activeProject.get());
				String autoName = String.format("ApprovalStatus%02d", statusCount + 1);
				entity.setName(autoName);
			}
			LOGGER.debug("Initialized new capprovalstatus");
		} catch (final Exception e) {
			LOGGER.error("Error initializing new capprovalstatus", e);
			throw new IllegalStateException("Failed to initialize capprovalstatus: " + e.getMessage(), e);
		}
	}
}
