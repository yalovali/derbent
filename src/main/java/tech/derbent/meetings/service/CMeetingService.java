package tech.derbent.meetings.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.meetings.domain.CMeetingType;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

@Service
@PreAuthorize("isAuthenticated()")
public class CMeetingService extends CEntityOfProjectService<CMeeting> {

    private final CMeetingRepository meetingRepository;

    CMeetingService(final CMeetingRepository repository, final Clock clock) {
        super(repository, clock);
        this.meetingRepository = repository;
    }

    /**
     * Finds meetings where the specified user is an attendee.
     * 
     * @param user
     *            the user to search for
     * @return list of meetings where the user is an attendee
     */
    public List<CMeeting> findByAttendee(final CUser user) {

        if ((user == null) || (user.getId() == null)) {
            return List.of();
        }
        return meetingRepository.findByAttendeeId(user.getId());
    }

    /**
     * Finds meetings where the specified user is a participant.
     * 
     * @param user
     *            the user to search for
     * @return list of meetings where the user is a participant
     */
    public List<CMeeting> findByParticipant(final CUser user) {

        if ((user == null) || (user.getId() == null)) {
            return List.of();
        }
        return meetingRepository.findByParticipantId(user.getId());
    }

    /**
     * Finds meetings by project with all relationships loaded to prevent LazyInitializationException. This method
     * provides meeting-specific relationship loading beyond the base implementation.
     * 
     * @param project
     *            the project
     * @return list of meetings with all relationships loaded
     */
    @Transactional(readOnly = true)
    public List<CMeeting> findByProjectWithAllRelationships(final CProject project) {
        LOGGER.info("findByProjectWithAllRelationships called with project: {}",
                project != null ? project.getName() : "null");

        if (project == null) {
            LOGGER.warn("findByProjectWithAllRelationships called with null project");
            return List.of();
        }

        try {
            return meetingRepository.findByProjectWithAllRelationships(project);
        } catch (final Exception e) {
            LOGGER.error("Error finding meetings by project with all relationships '{}': {}", project.getName(),
                    e.getMessage(), e);
            throw new RuntimeException("Failed to find meetings by project with all relationships", e);
        }
    }

    /**
     * Overrides the base get method to eagerly load all relationships to prevent LazyInitializationException when the
     * entity is used in UI contexts.
     * 
     * @param id
     *            the meeting ID
     * @return optional CMeeting with loaded relationships
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CMeeting> get(final Long id) {
        LOGGER.info("get called with id: {}", id);

        if (id == null) {
            return Optional.empty();
        }
        final Optional<CMeeting> entity = meetingRepository.findByIdWithAllRelationships(id);
        // Initialize lazy fields if entity is present (for any other potential lazy
        // relationships)
        entity.ifPresent(this::initializeLazyFields);
        return entity;
    }

    @Override
    protected Class<CMeeting> getEntityClass() {
        return CMeeting.class;
    }

    /**
     * Initializes lazy fields for CMeeting entity to prevent LazyInitializationException. Specifically handles the
     * lazy-loaded relationships including meetingType, participants, attendees, status, responsible, and
     * relatedActivity.
     * 
     * @param entity
     *            the CMeeting entity to initialize
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
            LOGGER.warn("Error initializing lazy fields for CMeeting with ID: {}", entity.getId(), e);
        }
    }
    // Auxiliary methods for sample data initialization and meeting setup

    /**
     * Auxiliary method to set attendees for a meeting.
     * 
     * @param meeting
     *            the meeting to configure
     * @param attendees
     *            set of users who actually attended
     * @return the configured meeting
     */
    @Transactional
    public CMeeting setAttendees(final CMeeting meeting, final Set<CUser> attendees) {
        LOGGER.info("setAttendees called for meeting: {} with {} attendees",
                meeting != null ? meeting.getName() : "null", attendees != null ? attendees.size() : 0);

        if (meeting == null) {
            LOGGER.warn("Meeting is null, cannot set attendees");
            return null;
        }

        if ((attendees != null) && !attendees.isEmpty()) {
            meeting.getAttendees().clear();

            for (final CUser attendee : attendees) {

                if (attendee != null) {
                    meeting.addAttendee(attendee);
                }
            }
        }
        return save(meeting);
    }

    /**
     * Auxiliary method to set meeting agenda and related activity.
     * 
     * @param meeting
     *            the meeting to configure
     * @param agenda
     *            meeting agenda and topics
     * @param relatedActivity
     *            project activity related to this meeting
     * @param responsible
     *            person responsible for the meeting
     * @return the configured meeting
     */
    @Transactional
    public CMeeting setMeetingContent(final CMeeting meeting, final String agenda, final CActivity relatedActivity,
            final CUser responsible) {
        LOGGER.info(
                "setMeetingContent called for meeting: {} with agenda length: {}, related activity: {}, responsible: {}",
                meeting != null ? meeting.getName() : "null", agenda != null ? agenda.length() : 0,
                relatedActivity != null ? relatedActivity.getName() : "null",
                responsible != null ? responsible.getName() : "null");

        if (meeting == null) {
            LOGGER.warn("Meeting is null, cannot set meeting content");
            return null;
        }

        if ((agenda != null) && !agenda.isEmpty()) {
            meeting.setAgenda(agenda);
        }

        if (relatedActivity != null) {
            meeting.setRelatedActivity(relatedActivity);
        }

        if (responsible != null) {
            meeting.setResponsible(responsible);
        }
        return save(meeting);
    }

    /**
     * Auxiliary method to set meeting details like type, dates, and location.
     * 
     * @param meeting
     *            the meeting to configure
     * @param meetingType
     *            the type of meeting
     * @param meetingDate
     *            start date and time
     * @param endDate
     *            end date and time
     * @param location
     *            meeting location (physical or virtual)
     * @return the configured meeting
     */
    @Transactional
    public CMeeting setMeetingDetails(final CMeeting meeting, final CMeetingType meetingType,
            final LocalDateTime meetingDate, final LocalDateTime endDate, final String location) {
        LOGGER.info("setMeetingDetails called for meeting: {} with type: {}, date: {}, location: {}",
                meeting != null ? meeting.getName() : "null", meetingType != null ? meetingType.getName() : "null",
                meetingDate, location);

        if (meeting == null) {
            LOGGER.warn("Meeting is null, cannot set meeting details");
            return null;
        }

        if (meetingType != null) {
            meeting.setMeetingType(meetingType);
        }

        if (meetingDate != null) {
            meeting.setMeetingDate(meetingDate);
        }

        if (endDate != null) {
            meeting.setEndDate(endDate);
        }

        if ((location != null) && !location.isEmpty()) {
            meeting.setLocation(location);
        }
        return save(meeting);
    }

    /**
     * Auxiliary method to set meeting status and additional information.
     * 
     * @param meeting
     *            the meeting to configure
     * @param status
     *            current status of the meeting
     * @param minutes
     *            meeting notes and minutes
     * @param linkedElement
     *            reference to external documents or systems
     * @return the configured meeting
     */
    @Transactional
    public CMeeting setMeetingStatus(final CMeeting meeting, final CMeetingStatus status, final String minutes,
            final String linkedElement) {
        LOGGER.info("setMeetingStatus called for meeting: {} with status: {}, minutes length: {}",
                meeting != null ? meeting.getName() : "null", status != null ? status.getName() : "null",
                minutes != null ? minutes.length() : 0);

        if (meeting == null) {
            LOGGER.warn("Meeting is null, cannot set meeting status");
            return null;
        }

        if (status != null) {
            meeting.setStatus(status);
        }

        if ((minutes != null) && !minutes.isEmpty()) {
            meeting.setMinutes(minutes);
        }

        if ((linkedElement != null) && !linkedElement.isEmpty()) {
            meeting.setLinkedElement(linkedElement);
        }
        return save(meeting);
    }

    /**
     * Auxiliary method to set participants for a meeting. Following coding guidelines to use service layer methods
     * instead of direct field setting.
     * 
     * @param meeting
     *            the meeting to configure
     * @param participants
     *            set of users to add as participants
     * @return the configured meeting
     */
    @Transactional
    public CMeeting setParticipants(final CMeeting meeting, final Set<CUser> participants) {
        LOGGER.info("setParticipants called for meeting: {} with {} participants",
                meeting != null ? meeting.getName() : "null", participants != null ? participants.size() : 0);

        if (meeting == null) {
            LOGGER.warn("Meeting is null, cannot set participants");
            return null;
        }

        if ((participants != null) && !participants.isEmpty()) {
            meeting.getParticipants().clear();

            for (final CUser participant : participants) {

                if (participant != null) {
                    meeting.addParticipant(participant);
                }
            }
        }
        return save(meeting);
    }
}