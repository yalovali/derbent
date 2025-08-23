package tech.derbent.meetings.service;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.interfaces.CKanbanService;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.users.domain.CUser;

@Service
@PreAuthorize ("isAuthenticated()")
public class CMeetingService extends CEntityOfProjectService<CMeeting> implements CKanbanService<CMeeting, CMeetingStatus> {

	CMeetingService(final CMeetingRepository repository, final Clock clock) {
		super(repository, clock);
	}

	/** Finds meetings where the specified user is an attendee.
	 * @param user the user to search for
	 * @return list of meetings where the user is an attendee */
	public List<CMeeting> findByAttendee(final CUser user) {
		Check.notNull(user, "User must not be null");
		return ((CMeetingRepository) repository).findByAttendeeId(user.getId());
	}

	/** Find meeting by ID with optimized eager loading. Uses repository method with JOIN FETCH to prevent N+1 queries.
	 * @param id the meeting ID
	 * @return the meeting with eagerly loaded associations, or null if not found */
	public CMeeting findById(final Long id) {
		Check.notNull(id, "ID must not be null");
		return ((CMeetingRepository) repository).findByIdWithEagerLoading(id).orElse(null);
	}

	/** Finds meetings where the specified user is a participant.
	 * @param user the user to search for
	 * @return list of meetings where the user is a participant */
	public List<CMeeting> findByParticipant(final CUser user) {
		Check.notNull(user, "User must not be null");
		return ((CMeetingRepository) repository).findByParticipantId(user.getId());
	}

	@Override
	public List<CMeetingStatus> getAllStatuses() {
		// This would need to be implemented by calling the status service For minimal
		// changes, returning empty list for now
		return List.of();
	}

	/** Gets a meeting by ID with all relationships eagerly loaded. This prevents LazyInitializationException when accessing meeting details.
	 * @param id the meeting ID
	 * @return optional meeting with loaded relationships */
	@Override
	@Transactional (readOnly = true)
	public java.util.Optional<CMeeting> getById(final Long id) {
		Check.notNull(id, "ID must not be null");
		final java.util.Optional<CMeeting> entity = ((CMeetingRepository) repository).findByIdWithEagerLoading(id);
		entity.ifPresent(this::initializeLazyFields);
		return entity;
	}

	// CKanbanService implementation methods
	@Override
	public Map<CMeetingStatus, List<CMeeting>> getEntitiesGroupedByStatus(final Long projectId) {
		// For minimal changes, returning empty map for now This would need proper
		// implementation by project ID
		return Map.of();
	}

	@Override
	protected Class<CMeeting> getEntityClass() { return CMeeting.class; }

	/** Optimized lazy field initialization for CMeeting entity. With improved repository queries using JOIN FETCH, this mainly handles complex
	 * collections like participants and attendees only when needed.
	 * @param entity the CMeeting entity to initialize */
	@Override
	public void initializeLazyFields(final CMeeting entity) {
		Check.notNull(entity, "Entity cannot be null");
		try {
			super.initializeLazyFields(entity); // Handles CEntityOfProject relationships automatically
			initializeLazyRelationship(entity.getMeetingType(), "meetingType");
			initializeLazyRelationship(entity.getStatus(), "status");
			initializeLazyRelationship(entity.getResponsible(), "responsible");
			initializeLazyRelationship(entity.getRelatedActivity(), "relatedActivity");
			initializeLazyRelationship(entity.getParticipants(), "participants");
			initializeLazyRelationship(entity.getAttendees(), "attendees");
		} catch (final Exception e) {
			LOGGER.warn("Error initializing lazy fields for CMeeting with ID: {}", entity.getId(), e);
		}
	}

	@Override
	public CMeeting updateEntityStatus(final CMeeting entity, final CMeetingStatus newStatus) {
		Check.notNull(entity, "Entity cannot be null");
		Check.notNull(newStatus, "New status cannot be null");
		entity.setStatus(newStatus);
		return save(entity);
	}
}
