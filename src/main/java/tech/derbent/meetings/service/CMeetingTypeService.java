package tech.derbent.meetings.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.meetings.domain.CMeetingType;
import tech.derbent.session.service.ISessionService;

/** CMeetingTypeService - Service layer for CMeetingType entity. Layer: Service (MVC) Handles business logic for project-aware meeting type
 * operations. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CMeetingTypeService extends CEntityOfProjectService<CMeetingType> {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingTypeService.class);

	CMeetingTypeService(final IMeetingTypeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected Class<CMeetingType> getEntityClass() { return CMeetingType.class; }

	/** Checks dependencies before allowing meeting type deletion. Prevents deletion if the type is being used by any meetings.
	 * @param meetingType the meeting type entity to check
	 * @return null if type can be deleted, error message otherwise */
	@Override
	public String checkDependencies(final CMeetingType meetingType) {
		// Call super class first to check common dependencies
		final String superCheck = super.checkDependencies(meetingType);
		if (superCheck != null) {
			return superCheck;
		}
		// No specific dependencies to check yet - stub for future implementation
		return null;
	}

	/** Initializes a new meeting type with default values based on current session and available data. Sets: - Project from current session - User
	 * for creation tracking - Auto-generated name - Default color and icon - Default sort order - Not marked as non-deletable
	 * @param entity the newly created meeting type to initialize
	 * @throws IllegalStateException if required fields cannot be initialized */
	@Override
	public void initializeNewEntity(final CMeetingType entity) {
		super.initializeNewEntity(entity);
		tech.derbent.api.utils.Check.notNull(entity, "Meeting type cannot be null");
		tech.derbent.api.utils.Check.notNull(sessionService, "Session service is required for meeting type initialization");
		try {
			// Get current project from session
			java.util.Optional<tech.derbent.projects.domain.CProject> activeProject = sessionService.getActiveProject();
			tech.derbent.api.utils.Check.isTrue(activeProject.isPresent(),
					"No active project in session - project context is required to create meeting types");
			tech.derbent.projects.domain.CProject currentProject = activeProject.get();
			entity.setProject(currentProject);
			// Get current user from session for createdBy field
			java.util.Optional<tech.derbent.users.domain.CUser> currentUser = sessionService.getActiveUser();
			if (currentUser.isPresent()) {
				entity.setCreatedBy(currentUser.get());
			}
			// Auto-generate name based on count
			long typeCount = ((IMeetingTypeRepository) repository).countByProject(currentProject);
			String autoName = String.format("MeetingType%02d", typeCount + 1);
			entity.setName(autoName);
			entity.setDescription("");
			entity.setColor(tech.derbent.meetings.domain.CMeetingType.DEFAULT_COLOR);
			entity.setSortOrder(100);
			entity.setAttributeNonDeletable(false);
			LOGGER.debug("Initialized new meeting type with auto-generated name: {}", autoName);
		} catch (final Exception e) {
			LOGGER.error("Error initializing new meeting type", e);
			throw new IllegalStateException("Failed to initialize meeting type: " + e.getMessage(), e);
		}
	}
}
