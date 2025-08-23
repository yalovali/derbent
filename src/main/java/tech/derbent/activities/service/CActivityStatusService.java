package tech.derbent.activities.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.projects.domain.CProject;

/** CActivityStatusService - Service class for managing CActivityStatus entities. Layer: Service (MVC) Provides business logic for activity status
 * management including CRUD operations, validation, and workflow management. */
@Service
@Transactional
public class CActivityStatusService extends CEntityOfProjectService<CActivityStatus> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CActivityStatusService.class);

	@Autowired
	public CActivityStatusService(final CActivityStatusRepository activityStatusRepository, final Clock clock) {
		super(activityStatusRepository, clock);
		Check.notNull(activityStatusRepository, "ActivityStatusRepository cannot be null");
	}

	/** Create default activity statuses if they don't exist. This method should be called during application startup. */
	public void createDefaultStatusesIfNotExist() {
		LOGGER.debug("createDefaultStatusesIfNotExist() - Creating default activity statuses");
	}

	/** Find activity status by ID.
	 * @param id the status ID - must not be null
	 * @return Optional containing the status if found, empty otherwise */
	@Transactional (readOnly = true)
	public Optional<CActivityStatus> findById(final Long id) {
		Check.notNull(id, "ID must not be null");
		final Optional<CActivityStatus> status = repository.findById(id);
		return status;
	}

	/** Find the default status for new activities.
	 * @return Optional containing the default status if found */
	@Transactional (readOnly = true)
	public Optional<CActivityStatus> findDefaultStatus(final CProject project) {
		final Optional<CActivityStatus> status = ((CActivityStatusService) repository).findDefaultStatus(project);
		return status;
	}

	@Override
	protected Class<CActivityStatus> getEntityClass() { return CActivityStatus.class; }
}
