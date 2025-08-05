package tech.derbent.meetings.service;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import tech.derbent.abstracts.interfaces.CKanbanService;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.users.domain.CUser;

@Service
@PreAuthorize ("isAuthenticated()")
public class CMeetingService extends CEntityOfProjectService<CMeeting>
	implements CKanbanService<CMeeting, CMeetingStatus> {

	CMeetingService(final CMeetingRepository repository, final Clock clock) {
		super(repository, clock);
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
	 * Find meeting by ID with optimized eager loading. Uses repository method with JOIN
	 * FETCH to prevent N+1 queries.
	 * @param id the meeting ID
	 * @return the meeting with eagerly loaded associations, or null if not found
	 */
	public CMeeting findById(final Long id) {

		if (id == null) {
			return null;
		}
		return ((CMeetingRepository) repository).findByIdWithEagerLoading(id)
			.orElse(null);
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

	@Override
	public List<CMeetingStatus> getAllStatuses() {
		// This would need to be implemented by calling the status service For minimal
		// changes, returning empty list for now
		return List.of();
	}

	// CKanbanService implementation methods
	@Override
	public Map<CMeetingStatus, List<CMeeting>>
		getEntitiesGroupedByStatus(final Long projectId) {
		// For minimal changes, returning empty map for now This would need proper
		// implementation by project ID
		return Map.of();
	}

	@Override
	protected Class<CMeeting> getEntityClass() { return CMeeting.class; }

	/**
	 * Optimized lazy field initialization for CMeeting entity. With improved repository
	 * queries using JOIN FETCH, this mainly handles complex collections like participants
	 * and attendees only when needed.
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
			// Initialize only the lazy collections that aren't handled by eager queries
			// Note: meetingType, status, responsible, relatedActivity are now eagerly
			// loaded via JOIN FETCH
			initializeLazyRelationship(entity.getParticipants());
			initializeLazyRelationship(entity.getAttendees());
		} catch (final Exception e) {
			LOGGER.warn("Error initializing lazy fields for CMeeting with ID: {}",
				entity.getId(), e);
		}
	}

	@Override
	public CMeeting updateEntityStatus(final CMeeting entity,
		final CMeetingStatus newStatus) {

		if (entity == null) {
			throw new IllegalArgumentException("Entity cannot be null");
		}

		if (newStatus == null) {
			throw new IllegalArgumentException("New status cannot be null");
		}
		entity.setStatus(newStatus);
		return save(entity);
	}
}