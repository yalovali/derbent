package tech.derbent.orders.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.orders.domain.COrderStatus;
import tech.derbent.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class COrderStatusService extends CEntityOfProjectService<COrderStatus> {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(COrderStatusService.class);

	COrderStatusService(final IOrderStatusRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected Class<COrderStatus> getEntityClass() { return COrderStatus.class; }

	@Override
	public String checkDependencies(final COrderStatus orderStatus) {
		final String superCheck = super.checkDependencies(orderStatus);
		if (superCheck != null) {
			return superCheck;
		}
		return null;
	}

	@Override
	public void initializeNewEntity(final COrderStatus entity) {
		super.initializeNewEntity(entity);
		tech.derbent.api.utils.Check.notNull(entity, "Order status cannot be null");
		tech.derbent.api.utils.Check.notNull(sessionService, "Session service is required for order status initialization");
		try {
			java.util.Optional<tech.derbent.projects.domain.CProject> activeProject = sessionService.getActiveProject();
			tech.derbent.api.utils.Check.isTrue(activeProject.isPresent(),
					"No active project in session - project context is required to create order statuses");
			tech.derbent.projects.domain.CProject currentProject = activeProject.get();
			entity.setProject(currentProject);
			java.util.Optional<tech.derbent.users.domain.CUser> currentUser = sessionService.getActiveUser();
			if (currentUser.isPresent()) {
				entity.setCreatedBy(currentUser.get());
			}
			long statusCount = ((IOrderStatusRepository) repository).countByProject(currentProject);
			String autoName = String.format("OrderStatus%02d", statusCount + 1);
			entity.setName(autoName);
			entity.setDescription("");
			entity.setColor(tech.derbent.orders.domain.COrderStatus.DEFAULT_COLOR);
			entity.setSortOrder(100);
			entity.setAttributeNonDeletable(false);
			LOGGER.debug("Initialized new order status with auto-generated name: {}", autoName);
		} catch (final Exception e) {
			LOGGER.error("Error initializing new order status", e);
			throw new IllegalStateException("Failed to initialize order status: " + e.getMessage(), e);
		}
	}
}
