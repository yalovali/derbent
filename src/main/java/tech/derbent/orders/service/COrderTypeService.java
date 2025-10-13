package tech.derbent.orders.service;

import java.util.Optional;
import tech.derbent.projects.domain.CProject;
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
	public String checkDeleteAllowed(final COrderType orderType) {
		final String superCheck = super.checkDeleteAllowed(orderType);
		if (superCheck != null) {
			return superCheck;
		}
		return null;
	}

	@Override
	public void initializeNewEntity(final COrderType entity) {
		super.initializeNewEntity(entity);
		try {
			Optional<CProject> activeProject = sessionService.getActiveProject();
			if (activeProject.isPresent()) {
				long typeCount = ((IOrderTypeRepository) repository).countByProject(activeProject.get());
				String autoName = String.format("OrderType%02d", typeCount + 1);
				entity.setName(autoName);
			}
			LOGGER.debug("Initialized new cordertype");
		} catch (final Exception e) {
			LOGGER.error("Error initializing new cordertype", e);
			throw new IllegalStateException("Failed to initialize cordertype: " + e.getMessage(), e);
		}
	}
}
