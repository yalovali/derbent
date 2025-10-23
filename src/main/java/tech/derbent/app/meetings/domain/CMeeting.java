package tech.derbent.app.meetings.domain;

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
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.domains.CEntityOfProject;
import tech.derbent.api.domains.CProjectItemStatus;
import tech.derbent.api.interfaces.IKanbanEntity;
import tech.derbent.api.interfaces.IKanbanStatus;
import tech.derbent.api.interfaces.IKanbanType;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.users.domain.CUser;

/** CMeeting - Domain entity representing meetings. Layer: Domain (MVC) Inherits from CEntityOfProject to provide project association. */
@Entity
@Table (name = "cmeeting") // table name for the entity as the default is the class name
// in lowercase
@AttributeOverride (name = "id", column = @Column (name = "meeting_id"))
public class CMeeting extends CEntityOfProject<CMeeting> implements IKanbanEntity {

	public static final String DEFAULT_COLOR = "#fd7e14";
	public static final String DEFAULT_ICON = "vaadin:calendar";
	public static final String VIEW_NAME = "Meetings View";
	@Column (name = "agenda", nullable = true, length = 4000)
	@Size (max = 4000)
	@AMetaData (
			displayName = "Agenda", required = false, readOnly = false, defaultValue = "", description = "Meeting agenda and topics to be discussed",
			hidden = false, order = 7, maxLength = 4000
	)
	private String agenda;
	@ManyToMany (fetch = FetchType.LAZY)
	@JoinTable (name = "cmeeting_attendees", joinColumns = @JoinColumn (name = "meeting_id"), inverseJoinColumns = @JoinColumn (name = "user_id"))
	@AMetaData (
			displayName = "Attendees", required = false, readOnly = false, description = "Users who actually attended the meeting", hidden = false,
			order = 12, dataProviderBean = "CUserService"
	)
	private Set<CUser> attendees = new HashSet<>();
	@Column (name = "end_date", nullable = true)
	@AMetaData (
			displayName = "End Time", required = false, readOnly = false, description = "End date and time of the meeting", hidden = false, order = 5
	)
	private LocalDateTime endDate;
	@Column (name = "linked_element", nullable = true, length = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@Size (max = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@AMetaData (
			displayName = "Linked Element", required = false, readOnly = false, defaultValue = "",
			description = "Reference to external documents, systems, or elements", hidden = false, order = 14,
			maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION
	)
	private String linkedElement;
	@Column (name = "location", nullable = true, length = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@Size (max = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@AMetaData (
			displayName = "Location", required = false, readOnly = false, defaultValue = "",
			description = "Physical or virtual location of the meeting", hidden = false, order = 6,
			maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION
	)
	private String location;
	@Column (name = "meeting_date", nullable = true)
	@AMetaData (
			displayName = "Start Time", required = false, readOnly = false, description = "Start date and time of the meeting", hidden = false,
			order = 4
	)
	private LocalDateTime meetingDate;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "cmeetingtype_id", nullable = true)
	@AMetaData (
			displayName = "Meeting Type", required = false, readOnly = false, description = "Type category of the meeting", hidden = false, order = 2,
			dataProviderBean = "CMeetingTypeService"
	)
	private CMeetingType meetingType;
	@Column (name = "minutes", nullable = true, length = 4000)
	@Size (max = 4000)
	@AMetaData (
			displayName = "Meeting Minutes", required = false, readOnly = false, defaultValue = "",
			description = "Notes and minutes from the meeting", hidden = false, order = 11, maxLength = 4000
	)
	private String minutes;
	@ManyToMany (fetch = FetchType.LAZY)
	@JoinTable (name = "cmeeting_participants", joinColumns = @JoinColumn (name = "meeting_id"), inverseJoinColumns = @JoinColumn (name = "user_id"))
	@AMetaData (
			displayName = "Participants", required = false, readOnly = false, description = "Users invited to participate in the meeting",
			hidden = false, order = 13, dataProviderBean = "CUserService"
	)
	private Set<CUser> participants = new HashSet<>();
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "related_activity_id", nullable = true)
	@AMetaData (
			displayName = "Related Activity", required = false, readOnly = false, description = "Project activity related to this meeting",
			hidden = false, order = 8, dataProviderBean = "CActivityService"
	)
	private CActivity relatedActivity;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "responsible_id", nullable = true)
	@AMetaData (
			displayName = "Responsible", required = false, readOnly = false,
			description = "Person responsible for organizing and leading the meeting", hidden = false, order = 10, dataProviderBean = "CUserService"
	)
	private CUser responsible;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "meeting_status_id", nullable = true)
	@AMetaData (
			displayName = "Status", required = false, readOnly = false, description = "Current status of the meeting", hidden = false, order = 9,
			dataProviderBean = "CProjectItemStatusService"
	)
	private CProjectItemStatus status;

	/** Default constructor for JPA. */
	public CMeeting() {
		super();
		attendees = new HashSet<>();
		participants = new HashSet<>();
	}

	public CMeeting(final String name, final CProject project) {
		super(CMeeting.class, name, project);
	}

	/** Constructor to create a meeting with name, project and meeting type.
	 * @param name        the name of the meeting
	 * @param project     the project associated with the meeting
	 * @param meetingType the type of the meeting */
	public CMeeting(final String name, final CProject project, final CMeetingType meetingType) {
		super(CMeeting.class, name, project);
		this.meetingType = meetingType;
	}

	/** Convenience method to add an attendee to the meeting.
	 * @param user the user to add as an attendee */
	public void addAttendee(final CUser user) {
		if (user != null) {
			attendees.add(user);
		}
	}

	/** Convenience method to add a participant to the meeting.
	 * @param user the user to add as a participant */
	public void addParticipant(final CUser user) {
		if (user != null) {
			participants.add(user);
		}
	}

	public String getAgenda() { return agenda; }

	public Set<CUser> getAttendees() { return attendees == null ? new HashSet<>() : new HashSet<>(attendees); }

	public LocalDateTime getEndDate() { return endDate; }

	public String getLinkedElement() { return linkedElement; }

	public String getLocation() { return location; }

	public LocalDateTime getMeetingDate() { return meetingDate; }

	public CMeetingType getMeetingType() { return meetingType; }

	public String getMinutes() { return minutes; }

	public Set<CUser> getParticipants() { return participants == null ? new HashSet<>() : new HashSet<>(participants); }

	public CActivity getRelatedActivity() { return relatedActivity; }

	public CUser getResponsible() { return responsible; }

	@Override
	public CProjectItemStatus getStatus() { return status; }

	@Override
	public IKanbanType getType() { return meetingType; }

	@Override
	public void initializeAllFields() {
		// Initialize lazy-loaded entity relationships
		if (meetingType != null) {
			meetingType.getName(); // Trigger meeting type loading
		}
		if (relatedActivity != null) {
			relatedActivity.getName(); // Trigger related activity loading
		}
		if (responsible != null) {
			responsible.getLogin(); // Trigger responsible user loading
		}
		if (status != null) {
			status.getName(); // Trigger status loading
		}
		// Parent class relationships (from CEntityOfProject)
		if (getProject() != null) {
			getProject().getName(); // Trigger project loading
		}
		if (getAssignedTo() != null) {
			getAssignedTo().getLogin(); // Trigger assigned user loading
		}
		if (getCreatedBy() != null) {
			getCreatedBy().getLogin(); // Trigger creator loading
		}
		// Note: attendees and participants collections will be initialized if accessed
	}

	/** Check if a user is an attendee of this meeting.
	 * @param user the user to check
	 * @return true if the user is an attendee, false otherwise */
	public boolean isAttendee(final CUser user) {
		return (user != null) && attendees.contains(user);
	}

	/** Check if a user is a participant in this meeting.
	 * @param user the user to check
	 * @return true if the user is a participant, false otherwise */
	public boolean isParticipant(final CUser user) {
		return (user != null) && participants.contains(user);
	}

	/** Convenience method to remove an attendee from the meeting.
	 * @param user the user to remove as an attendee */
	public void removeAttendee(final CUser user) {
		if (user != null) {
			attendees.remove(user);
		}
	}

	/** Convenience method to remove a participant from the meeting.
	 * @param user the user to remove as a participant */
	public void removeParticipant(final CUser user) {
		if (user != null) {
			participants.remove(user);
		}
	}

	public void setAgenda(final String agenda) { this.agenda = agenda; }

	public void setAttendees(final Set<CUser> attendees) { this.attendees = attendees != null ? attendees : new HashSet<>(); }

	public void setEndDate(final LocalDateTime endDate) { this.endDate = endDate; }

	public void setLinkedElement(final String linkedElement) { this.linkedElement = linkedElement; }

	public void setLocation(final String location) { this.location = location; }

	public void setMeetingDate(final LocalDateTime meetingDate) { this.meetingDate = meetingDate; }

	public void setMeetingType(final CMeetingType meetingType) { this.meetingType = meetingType; }

	public void setMinutes(final String minutes) { this.minutes = minutes; }

	public void setParticipants(final Set<CUser> participants) { this.participants = participants != null ? participants : new HashSet<>(); }

	public void setRelatedActivity(final CActivity relatedActivity) { this.relatedActivity = relatedActivity; }

	public void setResponsible(final CUser responsible) { this.responsible = responsible; }

	public void setStatus(final CProjectItemStatus status) { this.status = status; }

	// CKanbanEntity implementation methods
	@Override
	public void setStatus(final IKanbanStatus status) {
		if (status instanceof CProjectItemStatus) {
			setStatus((CProjectItemStatus) status);
		}
	}
}
