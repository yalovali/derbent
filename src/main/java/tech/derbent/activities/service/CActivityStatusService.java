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
import tech.derbent.users.domain.CUser;

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

	/** Initializes a new activity status with default values based on current session and available data. Sets: - Project from current session - User
	 * for creation tracking - Auto-generated name - Default color - Default sort order - Not marked as non-deletable
	 * @param activityStatus the newly created activity status to initialize
	 * @throws IllegalStateException if required fields cannot be initialized */
	@Override
	public void initializeNewEntity(final CActivityStatus activityStatus) {
		Check.notNull(activityStatus, "Activity status cannot be null");
		Check.notNull(sessionService, "Session service is required for activity status initialization");
		try {
			// Get current project from session
			Optional<CProject> activeProject = sessionService.getActiveProject();
			Check.isTrue(activeProject.isPresent(), "No active project in session - project context is required to create activity statuses");
			CProject currentProject = activeProject.get();
			activityStatus.setProject(currentProject);
			// Get current user from session for createdBy field
			Optional<CUser> currentUser = sessionService.getActiveUser();
			if (currentUser.isPresent()) {
				activityStatus.setCreatedBy(currentUser.get());
			}
			// Auto-generate name based on count
			long statusCount = ((IActivityStatusRepository) repository).countByProject(currentProject);
			String autoName = String.format("ActivityStatus%02d", statusCount + 1);
			activityStatus.setName(autoName);
			// Set default description
			activityStatus.setDescription("");
			// Set default color
			activityStatus.setColor("#4A90E2");
			// Set default sort order
			activityStatus.setSortOrder(100);
			// Set deletable by default (not system status)
			activityStatus.setAttributeNonDeletable(false);
			LOGGER.debug("Initialized new activity status with auto-generated name: {}", autoName);
		} catch (final Exception e) {
			LOGGER.error("Error initializing new activity status", e);
			throw new IllegalStateException("Failed to initialize activity status: " + e.getMessage(), e);
		}
	}
}
