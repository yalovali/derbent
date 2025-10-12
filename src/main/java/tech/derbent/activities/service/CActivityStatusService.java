package tech.derbent.activities.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.api.utils.Check;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.ISessionService;

/** CActivityStatusService - Service class for managing CActivityStatus entities. Layer: Service (MVC) Provides business logic for activity status
 * management including CRUD operations, validation, and workflow management. */
@Service
@Transactional
public class CActivityStatusService extends CEntityOfProjectService<CActivityStatus> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CActivityStatusService.class);
	@Autowired
	private IActivityRepository activityRepository;

	@Autowired
	public CActivityStatusService(final IActivityStatusRepository repository, final Clock clock, final ISessionService sessionService,
			final IActivityRepository activityRepository) {
		super(repository, clock, sessionService);
		this.activityRepository = activityRepository;
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

	/** Checks dependencies before allowing activity status deletion. Prevents deletion if the status is being used by any activities.
	 * @param activityStatus the activity status entity to check
	 * @return null if status can be deleted, error message otherwise */
	@Override
	public String checkDependencies(final CActivityStatus activityStatus) {
		Check.notNull(activityStatus, "Activity status cannot be null");
		Check.notNull(activityStatus.getId(), "Activity status ID cannot be null");
		try {
			// Check if this status is marked as non-deletable
			if (activityStatus.getAttributeNonDeletable()) {
				return "This activity status is marked as non-deletable and cannot be removed from the system.";
			}
			// Check if any activities are using this status
			final long usageCount = activityRepository.countByActivityStatus(activityStatus);
			if (usageCount > 0) {
				return String.format("Cannot delete activity status. It is being used by %d activit%s.", usageCount, usageCount == 1 ? "y" : "ies");
			}
			return null; // Status can be deleted
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for activity status: {}", activityStatus.getName(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}
}
