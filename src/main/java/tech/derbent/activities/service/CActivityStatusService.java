package tech.derbent.activities.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

/** CActivityStatusService - Service class for managing CActivityStatus entities. Layer: Service (MVC) Provides business logic for activity status
 * management including CRUD operations, validation, and workflow management. */
@Service
@Transactional
public class CActivityStatusService extends CEntityOfProjectService<CActivityStatus> {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CActivityStatusService.class);

	@Autowired
	public CActivityStatusService(final CActivityStatusRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
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
