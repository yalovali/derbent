package tech.derbent.meetings.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

@Service
@PreAuthorize ("isAuthenticated()")
public class CMeetingService extends CAbstractNamedEntityService<CMeeting> {

	CMeetingService(final CMeetingRepository repository, final Clock clock) {
		super(repository, clock);
	}
	// Now using the inherited createEntity(String name) method from
	// CAbstractNamedEntityService The original createEntity method is replaced by the
	// parent class implementation

	@Override
	protected CMeeting createNewEntityInstance() {
		return new CMeeting();
	}

	/**
	 * Finds meetings where the specified user is a participant.
	 * @param user the user to search for
	 * @return list of meetings where the user is a participant
	 */
	public List<CMeeting> findByParticipant(final CUser user) {

		if ((user == null) || (user.getId() == null)) {
			return List.of();
		}
		return ((CMeetingRepository) repository).findByParticipantId(user.getId());
	}

	/**
	 * Finds meetings where the specified user is an attendee.
	 * @param user the user to search for
	 * @return list of meetings where the user is an attendee
	 */
	public List<CMeeting> findByAttendee(final CUser user) {

		if ((user == null) || (user.getId() == null)) {
			return List.of();
		}
		return ((CMeetingRepository) repository).findByAttendeeId(user.getId());
	}

	/**
	 * Finds meetings by project.
	 * @param project the project to search for
	 * @return list of meetings for the project
	 */
	public List<CMeeting> findByProject(final CProject project) {
		LOGGER.info("findByProject called with project: {}", project);
		if (project == null) {
			return List.of();
		}
		return ((CMeetingRepository) repository).findByProject(project);
	}

	/**
	 * Overrides the base get method to eagerly load all relationships
	 * to prevent LazyInitializationException when the entity is used in UI contexts.
	 * @param id the meeting ID
	 * @return optional CMeeting with loaded relationships
	 */
	@Override
	@Transactional (readOnly = true)
	public Optional<CMeeting> get(final Long id) {
		LOGGER.info("get called with id: {}", id);
		if (id == null) {
			return Optional.empty();
		}
		final Optional<CMeeting> entity =
			((CMeetingRepository) repository).findByIdWithMeetingTypeAndParticipants(id);
		// Initialize lazy fields if entity is present (for any other potential lazy
		// relationships)
		entity.ifPresent(this::initializeLazyFields);
		return entity;
	}

	/**
	 * Gets a meeting by ID with eagerly loaded relationships.
	 * This method should be used in UI contexts to prevent LazyInitializationException.
	 * @param id the meeting ID
	 * @return optional CMeeting with loaded relationships
	 */
	@Transactional (readOnly = true)
	public Optional<CMeeting> getWithMeetingTypeAndParticipants(final Long id) {
		LOGGER.info("getWithMeetingTypeAndParticipants called with id: {}", id);
		if (id == null) {
			return Optional.empty();
		}
		return ((CMeetingRepository) repository)
			.findByIdWithMeetingTypeAndParticipants(id);
	}

	/**
	 * Initializes lazy fields for CMeeting entity to prevent LazyInitializationException.
	 * Specifically handles the lazy-loaded relationships including meetingType, participants,
	 * attendees, status, responsible, and relatedActivity.
	 * @param entity the CMeeting entity to initialize
	 */
	@Override
	protected void initializeLazyFields(final CMeeting entity) {

		if (entity == null) {
			return;
		}
		LOGGER.debug("Initializing lazy fields for CMeeting with ID: {}", entity.getId());

		try {
			// Initialize the entity itself first
			super.initializeLazyFields(entity);
			initializeLazyRelationship(entity.getMeetingType());
			initializeLazyRelationship(entity.getParticipants());
			initializeLazyRelationship(entity.getAttendees());
			initializeLazyRelationship(entity.getStatus());
			initializeLazyRelationship(entity.getResponsible());
			initializeLazyRelationship(entity.getRelatedActivity());
		} catch (final Exception e) {
			LOGGER.warn("Error initializing lazy fields for CMeeting with ID: {}",
				entity.getId(), e);
		}
	}

	/**
	 * Gets paginated meetings by project with all relationships eagerly loaded. This
	 * method should be used for grid display to prevent LazyInitializationException.
	 */
	public Page<CMeeting> listByProject(final CProject project, final Pageable pageable) {
		return ((CMeetingRepository) repository).findByProjectWithRelationships(project,
			pageable);
	}
}