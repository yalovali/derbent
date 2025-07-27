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
	 * Finds meetings by project.
	 */
	public List<CMeeting> findByProject(final CProject project) {
		return ((CMeetingRepository) repository).findByProject(project);
	}

	/**
	 * Overrides the base get method to eagerly load CMeetingType and participants
	 * relationships. This prevents LazyInitializationException when the entity is used in
	 * UI contexts.
	 * @param id the meeting ID
	 * @return optional CMeeting with loaded meetingType and participants
	 */
	@Override
	@Transactional (readOnly = true)
	public Optional<CMeeting> get(final Long id) {
		LOGGER.debug(
			"Getting CMeeting with ID {} (overridden to eagerly load meetingType and participants)",
			id);
		final Optional<CMeeting> entity =
			((CMeetingRepository) repository).findByIdWithMeetingTypeAndParticipants(id);
		// Initialize lazy fields if entity is present (for any other potential lazy
		// relationships)
		entity.ifPresent(this::initializeLazyFields);
		return entity;
	}

	/**
	 * Gets a meeting by ID with eagerly loaded CMeetingType and participants
	 * relationship. This method should be used in UI contexts to prevent
	 * LazyInitializationException.
	 * @param id the meeting ID
	 * @return optional CMeeting with loaded meetingType and participants
	 */
	@Transactional (readOnly = true)
	public Optional<CMeeting> getWithMeetingTypeAndParticipants(final Long id) {
		LOGGER.debug(
			"Getting CMeeting with ID {} and eagerly loading CMeetingType and participants",
			id);
		return ((CMeetingRepository) repository)
			.findByIdWithMeetingTypeAndParticipants(id);
	}

	/**
	 * Initializes lazy fields for CMeeting entity to prevent LazyInitializationException.
	 * Specifically handles the lazy-loaded CMeetingType and participants relationships.
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