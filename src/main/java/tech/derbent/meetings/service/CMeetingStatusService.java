package tech.derbent.meetings.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.ISessionService;

/** CMeetingStatusService - Service class for managing CMeetingStatus entities. Layer: Service (MVC) Provides business logic for meeting status
 * management including CRUD operations, validation, and workflow management. Since CMeetingStatus extends CStatus which extends CTypeEntity which
 * extends CEntityOfProject, this service must extend CEntityOfProjectService to enforce project-based queries. */
@Service
@Transactional
public class CMeetingStatusService extends CEntityOfProjectService<CMeetingStatus> {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingStatusService.class);

	@Autowired
	public CMeetingStatusService(final IMeetingStatusRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Create default meeting statuses if they don't exist. This method should be called during application startup. */
	public void createDefaultStatusesIfNotExist() {
		// TODO implement default statuses creation logic
	}

	/** Find all active (non-final) statuses for a specific project.
	 * @param project the project to find statuses for
	 * @return List of active statuses for the project */
	@Transactional (readOnly = true)
	public List<CMeetingStatus> findAllActiveStatusesByProject(final CProject project) {
		Optional.ofNullable(project).orElse(null);
		return listByProject(project).stream().filter(status -> !status.getFinalStatus()).toList();
	}

	/** Find the default status for new meetings.
	 * @return Optional containing the default status if found */
	@Transactional (readOnly = true)
	public Optional<CMeetingStatus> findDefaultStatus(final CProject project) {
		Optional.ofNullable(project).orElse(null);
		final Optional<CMeetingStatus> status = ((CMeetingStatusService) repository).findDefaultStatus(project);
		return status;
	}

	@Override
	protected Class<CMeetingStatus> getEntityClass() { return CMeetingStatus.class; }

	/** Checks dependencies before allowing meeting status deletion. Prevents deletion if the status is being used by any meetings.
	 * @param meetingStatus the meeting status entity to check
	 * @return null if status can be deleted, error message otherwise */
	@Override
	public String checkDependencies(final CMeetingStatus meetingStatus) {
		// Call super class first to check common dependencies
		final String superCheck = super.checkDependencies(meetingStatus);
		if (superCheck != null) {
			return superCheck;
		}
		// No specific dependencies to check yet - stub for future implementation
		return null;
	}

	/** Initializes a new meeting status with default values based on current session and available data. Sets: - Project from current session - User
	 * for creation tracking - Auto-generated name - Default color - Default sort order - Not marked as non-deletable
	 * @param entity the newly created meeting status to initialize
	 * @throws IllegalStateException if required fields cannot be initialized */
	@Override
	public void initializeNewEntity(final CMeetingStatus entity) {
		super.initializeNewEntity(entity);
		try {
			Optional<CProject> activeProject = sessionService.getActiveProject();
			if (activeProject.isPresent()) {
				long statusCount = ((IMeetingStatusRepository) repository).countByProject(activeProject.get());
				String autoName = String.format("MeetingStatus%02d", statusCount + 1);
				entity.setName(autoName);
			}
			entity.setFinalStatus(false);
			LOGGER.debug("Initialized new meeting status");
		} catch (final Exception e) {
			LOGGER.error("Error initializing new meeting status", e);
			throw new IllegalStateException("Failed to initialize meeting status: " + e.getMessage(), e);
		}
	}
}
