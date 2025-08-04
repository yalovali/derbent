package tech.derbent.meetings.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.interfaces.CKanbanService;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.meetings.domain.CMeetingType;
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
	 * Simplified lazy field initialization for CMeeting entity. With eager loading of
	 * small entities (meetingType, status, responsible), this mainly handles complex
	 * collections like participants and attendees.
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
			// Initialize remaining lazy collections (participants, attendees are still
			// lazy)
			initializeLazyRelationship(entity.getParticipants());
			initializeLazyRelationship(entity.getAttendees());
			initializeLazyRelationship(entity.getRelatedActivity());
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

	/**
	 * @deprecated Use entity setters directly instead of this auxiliary method.
	 * This method is temporary for compatibility and will be removed.
	 */
	@Deprecated
	@Transactional
	public CMeeting setParticipants(final CMeeting meeting, final Set<CUser> participants) {
		if (meeting == null) return null;
		if (participants != null && !participants.isEmpty()) {
			meeting.getParticipants().clear();
			for (final CUser participant : participants) {
				if (participant != null) meeting.addParticipant(participant);
			}
		}
		return save(meeting);
	}

	/**
	 * @deprecated Use entity setters directly instead of this auxiliary method.
	 * This method is temporary for compatibility and will be removed.
	 */
	@Deprecated
	@Transactional
	public CMeeting setAttendees(final CMeeting meeting, final Set<CUser> attendees) {
		if (meeting == null) return null;
		if (attendees != null && !attendees.isEmpty()) {
			meeting.getAttendees().clear();
			for (final CUser attendee : attendees) {
				if (attendee != null) meeting.addAttendee(attendee);
			}
		}
		return save(meeting);
	}

	/**
	 * @deprecated Use entity setters directly instead of this auxiliary method.
	 * This method is temporary for compatibility and will be removed.
	 */
	@Deprecated
	@Transactional
	public CMeeting setMeetingDetails(final CMeeting meeting,
		final CMeetingType meetingType, final LocalDateTime meetingDate,
		final LocalDateTime endDate, final String location) {
		if (meeting == null) return null;
		if (meetingType != null) meeting.setMeetingType(meetingType);
		if (meetingDate != null) meeting.setMeetingDate(meetingDate);
		if (endDate != null) meeting.setEndDate(endDate);
		if (location != null && !location.isEmpty()) meeting.setLocation(location);
		return save(meeting);
	}

	/**
	 * @deprecated Use entity setters directly instead of this auxiliary method.
	 * This method is temporary for compatibility and will be removed.
	 */
	@Deprecated
	@Transactional
	public CMeeting setMeetingContent(final CMeeting meeting, final String agenda,
		final CActivity relatedActivity, final CUser responsible) {
		if (meeting == null) return null;
		if (agenda != null && !agenda.isEmpty()) meeting.setAgenda(agenda);
		if (relatedActivity != null) meeting.setRelatedActivity(relatedActivity);
		if (responsible != null) meeting.setResponsible(responsible);
		return save(meeting);
	}

	/**
	 * @deprecated Use entity setters directly instead of this auxiliary method.
	 * This method is temporary for compatibility and will be removed.
	 */
	@Deprecated
	@Transactional
	public CMeeting setMeetingStatus(final CMeeting meeting, final CMeetingStatus status,
		final String minutes, final String linkedElement) {
		if (meeting == null) return null;
		if (status != null) meeting.setStatus(status);
		if (minutes != null && !minutes.isEmpty()) meeting.setMinutes(minutes);
		if (linkedElement != null && !linkedElement.isEmpty()) meeting.setLinkedElement(linkedElement);
		return save(meeting);
	}
}