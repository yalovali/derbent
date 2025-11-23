package tech.derbent.app.sprints.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.app.sprints.domain.CSprintStatus;
import tech.derbent.base.session.service.ISessionService;

/**
 * CSprintStatusService - Service class for managing sprint status types.
 * Provides business logic for sprint status operations.
 */
@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class CSprintStatusService extends CTypeEntityService<CSprintStatus> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSprintStatusService.class);
	@Autowired
	private ISprintRepository sprintRepository;

	public CSprintStatusService(final ISprintStatusRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CSprintStatus sprintStatus) {
		final String superCheck = super.checkDeleteAllowed(sprintStatus);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			// Check if any sprints are using this status
			final long count = sprintRepository.countByType(sprintStatus);
			if (count > 0) {
				final String message = String.format("Cannot delete. It is being used by %d sprint%s.", 
						count, count == 1 ? "" : "s");
				LOGGER.warn("Delete prevented: {}", message);
				return message;
			}
			return null;
		} catch (final Exception e) {
			LOGGER.error("Error checking sprint status delete dependencies", e);
			return "Error checking dependencies. Please try again.";
		}
	}

	@Override
	public Class<CSprintStatus> getEntityClass() {
		return CSprintStatus.class;
	}

	@Override
	public Class<?> getInitializerServiceClass() {
		return CSprintStatusInitializerService.class;
	}

	@Override
	public Class<?> getPageServiceClass() {
		return CPageServiceSprintStatus.class;
	}

	@Override
	public Class<?> getServiceClass() {
		return this.getClass();
	}

	@Override
	public void initializeNewEntity(final CSprintStatus entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initialized new sprint status entity with defaults");
	}
}
