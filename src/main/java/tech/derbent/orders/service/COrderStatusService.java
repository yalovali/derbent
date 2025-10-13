package tech.derbent.orders.service;

import java.util.Optional;
import tech.derbent.projects.domain.CProject;
import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CStatusService;
import tech.derbent.orders.domain.COrderStatus;
import tech.derbent.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class COrderStatusService extends CStatusService<COrderStatus> {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(COrderStatusService.class);

	COrderStatusService(final IOrderStatusRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected Class<COrderStatus> getEntityClass() { return COrderStatus.class; }

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
	public void initializeNewEntity(final COrderStatus entity) {
		super.initializeNewEntity(entity);
		try {
			Optional<CProject> activeProject = sessionService.getActiveProject();
			if (activeProject.isPresent()) {
				long statusCount = ((IOrderStatusRepository) repository).countByProject(activeProject.get());
				String autoName = String.format("OrderStatus%02d", statusCount + 1);
				entity.setName(autoName);
			}
			LOGGER.debug("Initialized new corderstatus");
		} catch (final Exception e) {
			LOGGER.error("Error initializing new corderstatus", e);
			throw new IllegalStateException("Failed to initialize corderstatus: " + e.getMessage(), e);
		}
	}
}
