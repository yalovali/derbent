package tech.derbent.meetings.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CEntityConstants;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.interfaces.CKanbanEntity;
import tech.derbent.abstracts.interfaces.CKanbanStatus;
import tech.derbent.abstracts.interfaces.CKanbanType;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * CMeeting - Domain entity representing meetings. Layer: Domain (MVC) Inherits from CEntityOfProject to provide project
 * association.
 */
@Entity
@Table(name = "cmeeting") // table name for the entity as the default is the class name
// in lowercase
@AttributeOverride(name = "id", column = @Column(name = "meeting_id"))
public class CMeeting extends CEntityOfProject<CMeeting> implements CKanbanEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cmeetingtype_id", nullable = true)
    @MetaData(displayName = "Meeting Type", required = false, readOnly = false, description = "Type category of the meeting", hidden = false, order = 2, dataProviderBean = "CMeetingTypeService")
    private CMeetingType meetingType;

    @Column(name = "description", nullable = true, length = CEntityConstants.MAX_LENGTH_DESCRIPTION)
    @Size(max = CEntityConstants.MAX_LENGTH_DESCRIPTION)
    @MetaData(displayName = "Description", required = false, readOnly = false, defaultValue = "", description = "Description of the meeting", hidden = false, order = 3, maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION)
    private String description;

    @Column(name = "meeting_date", nullable = true)
    @MetaData(displayName = "Start Time", required = false, readOnly = false, description = "Start date and time of the meeting", hidden = false, order = 4)
    private LocalDateTime meetingDate;

    @Column(name = "end_date", nullable = true)
    @MetaData(displayName = "End Time", required = false, readOnly = false, description = "End date and time of the meeting", hidden = false, order = 5)
    private LocalDateTime endDate;

    @Column(name = "location", nullable = true, length = CEntityConstants.MAX_LENGTH_DESCRIPTION)
    @Size(max = CEntityConstants.MAX_LENGTH_DESCRIPTION)
    @MetaData(displayName = "Location", required = false, readOnly = false, defaultValue = "", description = "Physical or virtual location of the meeting", hidden = false, order = 6, maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION)
    private String location;

    @Column(name = "agenda", nullable = true, length = 4000)
    @Size(max = 4000)
    @MetaData(displayName = "Agenda", required = false, readOnly = false, defaultValue = "", description = "Meeting agenda and topics to be discussed", hidden = false, order = 7, maxLength = 4000)
    private String agenda;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "related_activity_id", nullable = true)
    @MetaData(displayName = "Related Activity", required = false, readOnly = false, description = "Project activity related to this meeting", hidden = false, order = 8, dataProviderBean = "CActivityService")
    private CActivity relatedActivity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "meeting_status_id", nullable = true)
    @MetaData(displayName = "Status", required = false, readOnly = false, description = "Current status of the meeting", hidden = false, order = 9, dataProviderBean = "CMeetingStatusService")
    private CMeetingStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "responsible_id", nullable = true)
    @MetaData(displayName = "Responsible", required = false, readOnly = false, description = "Person responsible for organizing and leading the meeting", hidden = false, order = 10, dataProviderBean = "CUserService")
    private CUser responsible;

    @Column(name = "minutes", nullable = true, length = 4000)
    @Size(max = 4000)
    @MetaData(displayName = "Meeting Minutes", required = false, readOnly = false, defaultValue = "", description = "Notes and minutes from the meeting", hidden = false, order = 11, maxLength = 4000)
    private String minutes;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "cmeeting_attendees", joinColumns = @JoinColumn(name = "meeting_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    @MetaData(displayName = "Attendees", required = false, readOnly = false, description = "Users who actually attended the meeting", hidden = false, order = 12, dataProviderBean = "CUserService")
    private Set<CUser> attendees = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "cmeeting_participants", joinColumns = @JoinColumn(name = "meeting_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    @MetaData(displayName = "Participants", required = false, readOnly = false, description = "Users invited to participate in the meeting", hidden = false, order = 13, dataProviderBean = "CUserService")
    private Set<CUser> participants = new HashSet<>();

    @Column(name = "linked_element", nullable = true, length = CEntityConstants.MAX_LENGTH_DESCRIPTION)
    @Size(max = CEntityConstants.MAX_LENGTH_DESCRIPTION)
    @MetaData(displayName = "Linked Element", required = false, readOnly = false, defaultValue = "", description = "Reference to external documents, systems, or elements", hidden = false, order = 14, maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION)
    private String linkedElement;

    /**
     * Default constructor for JPA.
     */
    public CMeeting() {
        super();
        // Initialize collections for JPA
        this.attendees = new HashSet<>();
        this.participants = new HashSet<>();
    }

    public CMeeting(final String name, final CProject project) {
        super(CMeeting.class, name, project);
    }

    /**
     * Constructor to create a meeting with name, project and meeting type.
     * 
     * @param name
     *            the name of the meeting
     * @param project
     *            the project associated with the meeting
     * @param meetingType
     *            the type of the meeting
     */
    public CMeeting(final String name, final CProject project, final CMeetingType meetingType) {
        super(CMeeting.class, name, project);
        this.meetingType = meetingType;
    }

    /**
     * Convenience method to add an attendee to the meeting.
     * 
     * @param user
     *            the user to add as an attendee
     */
    public void addAttendee(final CUser user) {

        if (user != null) {
            attendees.add(user);
        }
    }

    /**
     * Convenience method to add a participant to the meeting.
     * 
     * @param user
     *            the user to add as a participant
     */
    public void addParticipant(final CUser user) {

        if (user != null) {
            participants.add(user);
        }
    }

    public String getAgenda() {
        return agenda;
    }

    public Set<CUser> getAttendees() {
        return attendees;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public String getLinkedElement() {
        return linkedElement;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getMeetingDate() {
        return meetingDate;
    }

    public CMeetingType getMeetingType() {
        return meetingType;
    }

    public String getMinutes() {
        return minutes;
    }

    public Set<CUser> getParticipants() {
        return participants;
    }

    public CActivity getRelatedActivity() {
        return relatedActivity;
    }

    public CUser getResponsible() {
        return responsible;
    }

    public CMeetingStatus getStatus() {
        return status;
    }

    /**
     * Check if a user is an attendee of this meeting.
     * 
     * @param user
     *            the user to check
     * @return true if the user is an attendee, false otherwise
     */
    public boolean isAttendee(final CUser user) {
        return (user != null) && attendees.contains(user);
    }

    /**
     * Check if a user is a participant in this meeting.
     * 
     * @param user
     *            the user to check
     * @return true if the user is a participant, false otherwise
     */
    public boolean isParticipant(final CUser user) {
        return (user != null) && participants.contains(user);
    }

    /**
     * Convenience method to remove an attendee from the meeting.
     * 
     * @param user
     *            the user to remove as an attendee
     */
    public void removeAttendee(final CUser user) {

        if (user != null) {
            attendees.remove(user);
        }
    }

    /**
     * Convenience method to remove a participant from the meeting.
     * 
     * @param user
     *            the user to remove as a participant
     */
    public void removeParticipant(final CUser user) {

        if (user != null) {
            participants.remove(user);
        }
    }

    public void setAgenda(final String agenda) {
        this.agenda = agenda;
    }

    public void setAttendees(final Set<CUser> attendees) {
        this.attendees = attendees != null ? attendees : new HashSet<>();
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    public void setEndDate(final LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public void setLinkedElement(final String linkedElement) {
        this.linkedElement = linkedElement;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public void setMeetingDate(final LocalDateTime meetingDate) {
        this.meetingDate = meetingDate;
    }

    public void setMeetingType(final CMeetingType meetingType) {
        this.meetingType = meetingType;
    }

    public void setMinutes(final String minutes) {
        this.minutes = minutes;
    }

    public void setParticipants(final Set<CUser> participants) {
        this.participants = participants != null ? participants : new HashSet<>();
    }

    public void setRelatedActivity(final CActivity relatedActivity) {
        this.relatedActivity = relatedActivity;
    }

    public void setResponsible(final CUser responsible) {
        this.responsible = responsible;
    }

    public void setStatus(final CMeetingStatus status) {
        this.status = status;
    }

    // CKanbanEntity implementation methods
    @Override
    public void setStatus(final CKanbanStatus status) {
        if (status instanceof CMeetingStatus) {
            setStatus((CMeetingStatus) status);
        }
    }

    @Override
    public CKanbanType getType() {
        return meetingType;
    }

    public static String getIconColorCode() {
        return "#28a745"; // Green color for meeting entities
    }

    public static String getIconFilename() {
        return "vaadin:group";
    }
}