package tech.derbent.app.meetings.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.AssociationOverride;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IHasIcon;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.domain.IHasAttachments;
import tech.derbent.app.gannt.ganntitem.service.IGanntEntityItem;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.base.users.domain.CUser;

/** CMeeting - Domain entity representing meetings. Layer: Domain (MVC) Inherits from CEntityOfProject to provide project association. */
@Entity
@Table (name = "cmeeting") // table name for the entity as the default is the class name
// in lowercase
@AttributeOverride (name = "id", column = @Column (name = "meeting_id"))
@AssociationOverride (name = "status", joinColumns = @JoinColumn (name = "meeting_status_id"))
public class CMeeting extends CProjectItem<CMeeting>
		implements IHasStatusAndWorkflow<CMeeting>, IGanntEntityItem, ISprintableItem, IHasIcon, IHasAttachments {

	public static final String DEFAULT_COLOR = "#DAA520"; // X11 Goldenrod - calendar events (darker)
	public static final String DEFAULT_ICON = "vaadin:calendar";
	public static final String ENTITY_TITLE_PLURAL = "Meetings";
	public static final String ENTITY_TITLE_SINGULAR = "Meeting";
	public static final String VIEW_NAME = "Meetings View";
	@Column (name = "agenda", nullable = true, length = 4000)
	@Size (max = 4000)
	@AMetaData (
			displayName = "Agenda", required = false, readOnly = false, defaultValue = "", description = "Meeting agenda and topics to be discussed",
			hidden = false, maxLength = 4000
	)
	private String agenda;
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "meeting_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Meeting documents and files", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@ManyToMany (fetch = FetchType.EAGER)
	@JoinTable (name = "cmeeting_attendees", joinColumns = @JoinColumn (name = "meeting_id"), inverseJoinColumns = @JoinColumn (name = "user_id"))
	@AMetaData (
			displayName = "Attendees", required = false, readOnly = false, description = "Users who actually attended the meeting", hidden = false,
			dataProviderBean = "CUserService"
	)
	private Set<CUser> attendees = new HashSet<>();
	@Column (name = "end_date", nullable = true)
	@AMetaData (displayName = "End Time", required = false, readOnly = false, description = "End date and time of the meeting", hidden = false)
	private LocalDate endDate;
	@Column (name = "endTime", nullable = true)
	@AMetaData (displayName = "End Time", required = false, readOnly = false, description = "Start date and time of the meeting", hidden = false)
	private LocalTime endTime;
	// Type Management - concrete implementation of parent's typeEntity
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Meeting Type", required = false, readOnly = false, description = "Type category of the meeting", hidden = false,
			dataProviderBean = "CMeetingTypeService"
	)
	private CMeetingType entityType;
	@Column (name = "linked_element", nullable = true, length = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@Size (max = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@AMetaData (
			displayName = "Linked Element", required = false, readOnly = false, defaultValue = "",
			description = "Reference to external documents, systems, or elements", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION
	)
	private String linkedElement;
	@Column (name = "location", nullable = true, length = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@Size (max = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@AMetaData (
			displayName = "Location", required = false, readOnly = false, defaultValue = "",
			description = "Physical or virtual location of the meeting", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION
	)
	private String location;
	@Column (name = "minutes", nullable = true, length = 4000)
	@Size (max = 4000)
	@AMetaData (
			displayName = "Meeting Minutes", required = false, readOnly = false, defaultValue = "",
			description = "Notes and minutes from the meeting", hidden = false, maxLength = 4000
	)
	private String minutes;
	@ManyToMany (fetch = FetchType.EAGER)
	@JoinTable (name = "cmeeting_participants", joinColumns = @JoinColumn (name = "meeting_id"), inverseJoinColumns = @JoinColumn (name = "user_id"))
	@AMetaData (
			displayName = "Participants", required = false, readOnly = false, description = "Users invited to participate in the meeting",
			hidden = false, dataProviderBean = "CUserService"
	)
	private Set<CUser> participants = new HashSet<>();
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "related_activity_id", nullable = true)
	@AMetaData (
			displayName = "Related Activity", required = false, readOnly = false, description = "Project activity related to this meeting",
			hidden = false, dataProviderBean = "CActivityService"
	)
	private CActivity relatedActivity;
	// Sprint Item relationship - REQUIRED: every meeting must have a sprint item for progress tracking
	@OneToOne (fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn (name = "sprintitem_id", nullable = false)
	@jakarta.validation.constraints.NotNull (message = "Sprint item is required for progress tracking")
	@AMetaData (displayName = "Sprint Item", required = true, readOnly = true, description = "Progress tracking for this meeting", hidden = true)
	private CSprintItem sprintItem;
	@Column (name = "sprint_order", nullable = true)
	@jakarta.validation.constraints.Min (value = 1, message = "Sprint order must be positive")
	@AMetaData (
			displayName = "Sprint Order", required = false, readOnly = false,
			description = "Display order within sprint and backlog views (assigned automatically)", hidden = true
	)
	private Integer sprintOrder;
	@Column (nullable = true)
	@AMetaData (
			displayName = "Start Date", required = false, readOnly = false, description = "Planned or actual start date of the activity",
			hidden = false
	)
	private LocalDate startDate;
	@Column (name = "startTime", nullable = true)
	@AMetaData (displayName = "Start Time", required = false, readOnly = false, description = "Start date and time of the meeting", hidden = false)
	private LocalTime startTime;
	@Column (nullable = true)
	@AMetaData (
			displayName = "Story Points", required = false, readOnly = false, defaultValue = "0",
			description = "Estimated effort or complexity in story points", hidden = false
	)
	private Long storyPoint;

	/** Default constructor for JPA. */
	public CMeeting() {
		super();
		attendees = new HashSet<>();
		participants = new HashSet<>();
		// Ensure sprint item is always created for composition pattern
		if (sprintItem == null) {
			sprintItem = tech.derbent.app.sprints.service.CSprintItemService.createDefaultSprintItem();
		}
		// Set back-reference so sprintItem can access parent for display
		if (sprintItem != null) {
			sprintItem.setParentItem(this);
		}
	}

	public CMeeting(final String name, final CProject project) {
		super(CMeeting.class, name, project);
		// Ensure sprint item is always created for composition pattern
		if (sprintItem == null) {
			sprintItem = tech.derbent.app.sprints.service.CSprintItemService.createDefaultSprintItem();
		}
		// Set back-reference so sprintItem can access parent for display
		if (sprintItem != null) {
			sprintItem.setParentItem(this);
		}
	}

	/** Constructor to create a meeting with name, project and meeting type.
	 * @param name        the name of the meeting
	 * @param project     the project associated with the meeting
	 * @param meetingType the type of the meeting */
	public CMeeting(final String name, final CProject project, final CMeetingType meetingType) {
		super(CMeeting.class, name, project);
		entityType = meetingType;
		// Ensure sprint item is always created for composition pattern
		if (sprintItem == null) {
			sprintItem = tech.derbent.app.sprints.service.CSprintItemService.createDefaultSprintItem();
		}
		// Set back-reference so sprintItem can access parent for display
		if (sprintItem != null) {
			sprintItem.setParentItem(this);
		}
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

	@jakarta.persistence.PostLoad
	protected void ensureSprintItemParent() {
		if (sprintItem != null) {
			sprintItem.setParentItem(this);
		}
	}

	public String getAgenda() { return agenda; }

	// IHasAttachments interface methods
	@Override
	public Set<CAttachment> getAttachments() {
		if (attachments == null) {
			attachments = new HashSet<>();
		}
		return attachments;
	}

	public Set<CUser> getAttendees() { return attendees == null ? new HashSet<>() : new HashSet<>(attendees); }

	@Override
	public String getColor() { return DEFAULT_COLOR; }

	@Override
	public LocalDate getEndDate() { return endDate; }

	public LocalTime getEndTime() { return endTime; }

	@Override
	@SuppressWarnings ({})
	public CTypeEntity<?> getEntityType() { return entityType; }

	@Override
	public String getIconString() { return DEFAULT_ICON; }

	public String getLinkedElement() { return linkedElement; }

	public String getLocation() { return location; }

	public CMeetingType getMeetingType() { return entityType; }

	public String getMinutes() { return minutes; }

	public Set<CUser> getParticipants() { return participants == null ? new HashSet<>() : new HashSet<>(participants); }

	@Override
	public Integer getProgressPercentage() {
		Check.notNull(sprintItem, "Sprint item must not be null");
		return sprintItem.getProgressPercentage();
	}

	public CActivity getRelatedActivity() { return relatedActivity; }

	@Override
	public CSprintItem getSprintItem() { return sprintItem; }

	@Override
	public Integer getSprintOrder() { return sprintOrder; }

	@Override
	public LocalDate getStartDate() {
		Check.notNull(sprintItem, "Sprint item must not be null");
		return sprintItem.getStartDate();
	}

	public LocalTime getStartTime() { return startTime; }

	@Override
	public Long getStoryPoint() {
		Check.notNull(sprintItem, "Sprint item must not be null");
		return sprintItem.getStoryPoint();
	}

	@Override
	public CWorkflowEntity getWorkflow() {
		Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	/** Check if a user is an attendee of this meeting.
	 * @param user the user to check
	 * @return true if the user is an attendee, false otherwise */
	public boolean isAttendee(final CUser user) {
		return user != null && attendees.contains(user);
	}

	/** Check if a user is a participant in this meeting.
	 * @param user the user to check
	 * @return true if the user is a participant, false otherwise */
	public boolean isParticipant(final CUser user) {
		return user != null && participants.contains(user);
	}

	/** Checks if this entity matches the given search value in the specified fields. This implementation extends CProjectItem to also search in
	 * meeting-specific entity fields.
	 * @param searchValue the value to search for (case-insensitive)
	 * @param fieldNames  the list of field names to search in. If null or empty, searches only in "name" field. Supported field names: all parent
	 *                    fields plus "entityType", "relatedActivity",
	 * @return true if the entity matches the search criteria in any of the specified fields */
	@Override
	public boolean matchesFilter(final String searchValue, final java.util.Collection<String> fieldNames) {
		if (searchValue == null || searchValue.isBlank()) {
			return true; // No filter means match all
		}
		if (super.matchesFilter(searchValue, fieldNames)) {
			return true;
		}
		final String lowerSearchValue = searchValue.toLowerCase().trim();
		// Check entity fields
		if (fieldNames.remove("entityType") && getEntityType() != null && getEntityType().matchesFilter(lowerSearchValue, Arrays.asList("name"))) {
			return true;
		}
		if (fieldNames.remove("relatedActivity") && getRelatedActivity() != null
				&& getRelatedActivity().matchesFilter(lowerSearchValue, Arrays.asList("name"))) {
			return true;
		}
		if (fieldNames.remove("assignedTo") && getAssignedTo() != null && getAssignedTo().matchesFilter(lowerSearchValue, Arrays.asList("name"))) {
			return true;
		}
		return false;
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

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	public void setAttendees(final Set<CUser> attendees) { this.attendees = attendees != null ? attendees : new HashSet<>(); }

	@Override
	public void setColor(String color) { /*****/
	}

	public void setEndDate(final LocalDate endDate) { this.endDate = endDate; }

	public void setEndTime(final LocalTime endTime) { this.endTime = endTime; }

	public void setEntityType(final CMeetingType entityType) { this.entityType = entityType; }

	@Override
	public void setEntityType(final CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CMeetingType.class, "Type entity must be an instance of CMeetingType");
		Check.notNull(getProject(), "Project must be set before assigning meeting type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning meeting type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning meeting type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()), "Type entity company id "
				+ typeEntity.getCompany().getId() + " does not match meeting project company id " + getProject().getCompany().getId());
		entityType = (CMeetingType) typeEntity;
		updateLastModified();
	}

	public void setLinkedElement(final String linkedElement) { this.linkedElement = linkedElement; }

	public void setLocation(final String location) { this.location = location; }

	public void setMinutes(final String minutes) { this.minutes = minutes; }

	public void setParticipants(final Set<CUser> participants) { this.participants = participants != null ? participants : new HashSet<>(); }

	public void setRelatedActivity(final CActivity relatedActivity) { this.relatedActivity = relatedActivity; }

	@Override
	public void setSprintItem(CSprintItem sprintItem) { this.sprintItem = sprintItem; }

	@Override
	public void setSprintOrder(final Integer sprintOrder) { this.sprintOrder = sprintOrder; }

	public void setStartDate(final LocalDate startDate) {
		Check.notNull(sprintItem, "Sprint item must not be null");
		this.startDate = startDate;
		sprintItem.setStartDate(startDate);
	}

	public void setStartTime(final LocalTime startTime) { this.startTime = startTime; }

	@Override
	public void setStoryPoint(final Long storyPoint) {
		Check.notNull(sprintItem, "Sprint item must not be null");
		this.storyPoint = storyPoint; // Keep for backward compatibility
		sprintItem.setStoryPoint(storyPoint);
	}
}
