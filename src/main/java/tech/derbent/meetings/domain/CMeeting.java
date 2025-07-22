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
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * CMeeting - Domain entity representing meetings.
 * Layer: Domain (MVC)
 * Inherits from CEntityOfProject to provide project association.
 */
@Entity
@Table(name = "cmeeting") // table name for the entity as the default is the class name in lowercase
@AttributeOverride(name = "id", column = @Column(name = "meeting_id")) // Override the default column name for the ID field
public class CMeeting extends CEntityOfProject {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cmeetingtype_id", nullable = true)
    @MetaData(
        displayName = "Meeting Type", 
        required = false, 
        readOnly = false, 
        description = "Type category of the meeting", 
        hidden = false, 
        order = 2,
        dataProviderBean = "CMeetingTypeService"
    )
    private CMeetingType meetingType;

    @Column(name = "description", nullable = true, length = MAX_LENGTH_DESCRIPTION)
    @Size(max = MAX_LENGTH_DESCRIPTION)
    @MetaData(displayName = "Description", required = false, readOnly = false, defaultValue = "", description = "Description of the meeting", hidden = false, order = 3, maxLength = MAX_LENGTH_DESCRIPTION)
    private String description;

    @Column(name = "meeting_date", nullable = true)
    @MetaData(displayName = "Start Time", required = false, readOnly = false, description = "Start date and time of the meeting", hidden = false, order = 4)
    private LocalDateTime meetingDate;

    @Column(name = "end_date", nullable = true)
    @MetaData(displayName = "End Time", required = false, readOnly = false, description = "End date and time of the meeting", hidden = false, order = 5)
    private LocalDateTime endDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "cmeeting_participants",
        joinColumns = @JoinColumn(name = "meeting_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @MetaData(
        displayName = "Participants", 
        required = false, 
        readOnly = false, 
        description = "Users participating in the meeting", 
        hidden = false, 
        order = 6,
        dataProviderBean = "CUserService"
    )
    private Set<CUser> participants = new HashSet<>();

    public CMeeting() {
        super();
        // Default constructor - project will be set later
    }

    public CMeeting(final String name, final CProject project) {
        super(name, project);
    }

    public CMeeting(final String name, final CProject project, final CMeetingType meetingType) {
        super(name, project);
        this.meetingType = meetingType;
    }

    public CMeetingType getMeetingType() {
        return meetingType;
    }

    public void setMeetingType(final CMeetingType meetingType) {
        this.meetingType = meetingType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public LocalDateTime getMeetingDate() {
        return meetingDate;
    }

    public void setMeetingDate(final LocalDateTime meetingDate) {
        this.meetingDate = meetingDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Set<CUser> getParticipants() {
        return participants;
    }

    public void setParticipants(final Set<CUser> participants) {
        this.participants = participants != null ? participants : new HashSet<>();
    }

    /**
     * Convenience method to add a participant to the meeting.
     * 
     * @param user the user to add as a participant
     */
    public void addParticipant(final CUser user) {
        if (user != null) {
            participants.add(user);
        }
    }

    /**
     * Convenience method to remove a participant from the meeting.
     * 
     * @param user the user to remove as a participant
     */
    public void removeParticipant(final CUser user) {
        if (user != null) {
            participants.remove(user);
        }
    }

    /**
     * Check if a user is a participant in this meeting.
     * 
     * @param user the user to check
     * @return true if the user is a participant, false otherwise
     */
    public boolean isParticipant(final CUser user) {
        return user != null && participants.contains(user);
    }
}