package tech.derbent.orders.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.orders.domain.COrderType;
import tech.derbent.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class COrderTypeService extends CEntityOfProjectService<COrderType> {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(COrderTypeService.class);

	COrderTypeService(final IOrderTypeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected Class<COrderType> getEntityClass() { return COrderType.class; }

	@Override
	public String checkDependencies(final COrderType orderType) {
		final String superCheck = super.checkDependencies(orderType);
		if (superCheck != null) {
			return superCheck;
		}
		return null;
	}

	@Override
	public void initializeNewEntity(final COrderType entity) {
		super.initializeNewEntity(entity);
		tech.derbent.api.utils.Check.notNull(entity, "Order type cannot be null");
		tech.derbent.api.utils.Check.notNull(sessionService, "Session service is required for order type initialization");
		try {
			java.util.Optional<tech.derbent.projects.domain.CProject> activeProject = sessionService.getActiveProject();
			tech.derbent.api.utils.Check.isTrue(activeProject.isPresent(),
					"No active project in session - project context is required to create order types");
			tech.derbent.projects.domain.CProject currentProject = activeProject.get();
			entity.setProject(currentProject);
			java.util.Optional<tech.derbent.users.domain.CUser> currentUser = sessionService.getActiveUser();
			if (currentUser.isPresent()) {
				entity.setCreatedBy(currentUser.get());
			}
			long typeCount = ((IOrderTypeRepository) repository).countByProject(currentProject);
			String autoName = String.format("OrderType%02d", typeCount + 1);
			entity.setName(autoName);
			entity.setDescription("");
			entity.setColor(tech.derbent.orders.domain.COrderType.DEFAULT_COLOR);
			entity.setSortOrder(100);
			entity.setAttributeNonDeletable(false);
			LOGGER.debug("Initialized new order type with auto-generated name: {}", autoName);
		} catch (final Exception e) {
			LOGGER.error("Error initializing new order type", e);
			throw new IllegalStateException("Failed to initialize order type: " + e.getMessage(), e);
		}
	}
}
