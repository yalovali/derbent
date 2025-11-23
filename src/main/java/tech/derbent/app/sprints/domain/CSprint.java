package tech.derbent.app.sprints.domain;

import java.time.LocalDate;
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
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.gannt.ganntitem.service.IGanntEntityItem;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.users.domain.CUser;

/**
 * CSprint - Domain entity representing a sprint in agile development.
 * A sprint is a time-boxed iteration containing selected activities and meetings.
 * Layer: Domain (MVC)
 * Inherits from CProjectItem to provide project association and workflow support.
 */
@Entity
@Table(name = "csprint")
@AttributeOverride(name = "id", column = @Column(name = "sprint_id"))
public class CSprint extends CProjectItem<CSprint> implements IHasStatusAndWorkflow<CSprint>, IGanntEntityItem {

	public static final String DEFAULT_COLOR = "#28a745";
	public static final String DEFAULT_ICON = "vaadin:calendar-clock";
	public static final String VIEW_NAME = "Sprints View";
	private static final Logger LOGGER = LoggerFactory.getLogger(CSprint.class);

	// Sprint Basic Information
	@Column(nullable = true, length = 2000)
	@Size(max = 2000)
	@AMetaData(
			displayName = "Description", 
			required = false, 
			readOnly = false, 
			defaultValue = "",
			description = "Detailed description of the sprint goals and objectives", 
			hidden = false, 
			order = 3, 
			maxLength = 2000
	)
	private String description;

	// Sprint Timeline
	@Column(nullable = true)
	@AMetaData(
			displayName = "Start Date", 
			required = true, 
			readOnly = false, 
			description = "Sprint start date",
			hidden = false, 
			order = 10
	)
	private LocalDate startDate;

	@Column(nullable = true)
	@AMetaData(
			displayName = "End Date", 
			required = true, 
			readOnly = false, 
			description = "Sprint end date",
			hidden = false, 
			order = 11
	)
	private LocalDate endDate;

	// Sprint Color for UI display
	@Column(nullable = true, length = 7)
	@Size(max = 7)
	@AMetaData(
			displayName = "Color", 
			required = false, 
			readOnly = false, 
			defaultValue = DEFAULT_COLOR,
			description = "Color code for sprint visualization (hex format)", 
			hidden = false, 
			order = 20,
			colorField = true
	)
	private String color = DEFAULT_COLOR;

	// Type Management - using CSprintStatus
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "entitytype_id", nullable = true)
	@AMetaData(
			displayName = "Sprint Type", 
			required = false, 
			readOnly = false, 
			description = "Type/category of the sprint", 
			hidden = false,
			order = 2, 
			dataProviderBean = "CSprintStatusService", 
			setBackgroundFromColor = true, 
			useIcon = true
	)
	private CSprintStatus entityType;

	// Sprint Activities - Many-to-Many relationship
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "csprint_activities", 
			joinColumns = @JoinColumn(name = "sprint_id"), 
			inverseJoinColumns = @JoinColumn(name = "activity_id")
	)
	@AMetaData(
			displayName = "Activities", 
			required = false, 
			readOnly = false, 
			description = "Activities included in this sprint",
			hidden = false, 
			order = 30, 
			dataProviderBean = "CActivityService"
	)
	private Set<CActivity> activities = new HashSet<>();

	// Sprint Meetings - Many-to-Many relationship
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "csprint_meetings", 
			joinColumns = @JoinColumn(name = "sprint_id"), 
			inverseJoinColumns = @JoinColumn(name = "meeting_id")
	)
	@AMetaData(
			displayName = "Meetings", 
			required = false, 
			readOnly = false, 
			description = "Meetings scheduled in this sprint",
			hidden = false, 
			order = 31, 
			dataProviderBean = "CMeetingService"
	)
	private Set<CMeeting> meetings = new HashSet<>();

	// Calculated field for display - stored as transient
	@Transient
	@AMetaData(
			displayName = "Item Count", 
			required = false, 
			readOnly = true, 
			description = "Total number of activities and meetings in this sprint",
			hidden = false, 
			order = 32
	)
	private Integer itemCount;

	/** Default constructor for JPA. */
	public CSprint() {
		super();
		initializeDefaults();
	}

	/** Constructor with name and project.
	 * @param name    the name of the sprint - must not be null
	 * @param project the project this sprint belongs to - must not be null */
	public CSprint(final String name, final CProject project) {
		super(CSprint.class, name, project);
		initializeDefaults();
	}

	/** Constructor with name, project, and dates.
	 * @param name      the name of the sprint - must not be null
	 * @param project   the project this sprint belongs to - must not be null
	 * @param startDate the sprint start date
	 * @param endDate   the sprint end date */
	public CSprint(final String name, final CProject project, final LocalDate startDate, final LocalDate endDate) {
		super(CSprint.class, name, project);
		this.startDate = startDate;
		this.endDate = endDate;
		initializeDefaults();
	}

	/** Initialize default values for the sprint. */
	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (color == null || color.isEmpty()) {
			color = DEFAULT_COLOR;
		}
		if (activities == null) {
			activities = new HashSet<>();
		}
		if (meetings == null) {
			meetings = new HashSet<>();
		}
		if (startDate == null) {
			startDate = LocalDate.now();
		}
		if (endDate == null) {
			endDate = LocalDate.now().plusWeeks(2); // Default to 2-week sprint
		}
	}

	// Getters and Setters

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
		updateLastModified();
	}

	@Override
	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(final LocalDate startDate) {
		this.startDate = startDate;
		updateLastModified();
	}

	@Override
	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(final LocalDate endDate) {
		this.endDate = endDate;
		updateLastModified();
	}

	public String getColor() {
		return color;
	}

	public void setColor(final String color) {
		this.color = color;
		updateLastModified();
	}

	/** Gets the sprint type.
	 * @return the sprint type */
	@Override
	public CTypeEntity<?> getEntityType() {
		return entityType;
	}

	/** Override to set concrete type entity.
	 * @param typeEntity the type entity to set */
	@Override
	public void setEntityType(final CTypeEntity<?> typeEntity) {
		Check.instanceOf(typeEntity, CSprintStatus.class, "Type entity must be an instance of CSprintStatus");
		entityType = (CSprintStatus) typeEntity;
		updateLastModified();
	}

	public Set<CActivity> getActivities() {
		return activities != null ? activities : new HashSet<>();
	}

	public void setActivities(final Set<CActivity> activities) {
		this.activities = activities != null ? activities : new HashSet<>();
		updateLastModified();
	}

	public Set<CMeeting> getMeetings() {
		return meetings != null ? meetings : new HashSet<>();
	}

	public void setMeetings(final Set<CMeeting> meetings) {
		this.meetings = meetings != null ? meetings : new HashSet<>();
		updateLastModified();
	}

	/** Add an activity to this sprint.
	 * @param activity the activity to add */
	public void addActivity(final CActivity activity) {
		if (activity != null) {
			getActivities().add(activity);
			updateLastModified();
		}
	}

	/** Remove an activity from this sprint.
	 * @param activity the activity to remove */
	public void removeActivity(final CActivity activity) {
		if (activity != null) {
			getActivities().remove(activity);
			updateLastModified();
		}
	}

	/** Add a meeting to this sprint.
	 * @param meeting the meeting to add */
	public void addMeeting(final CMeeting meeting) {
		if (meeting != null) {
			getMeetings().add(meeting);
			updateLastModified();
		}
	}

	/** Remove a meeting from this sprint.
	 * @param meeting the meeting to remove */
	public void removeMeeting(final CMeeting meeting) {
		if (meeting != null) {
			getMeetings().remove(meeting);
			updateLastModified();
		}
	}

	/** Get the total number of items (activities + meetings) in this sprint.
	 * This is a calculated field for UI display.
	 * @return total count of activities and meetings */
	public Integer getItemCount() {
		int activityCount = (activities != null) ? activities.size() : 0;
		int meetingCount = (meetings != null) ? meetings.size() : 0;
		return activityCount + meetingCount;
	}

	/** Set the item count (for framework use only - calculated automatically).
	 * @param itemCount the item count */
	public void setItemCount(final Integer itemCount) {
		this.itemCount = itemCount;
	}

	/** Check if the sprint is active (current date is between start and end dates).
	 * @return true if the sprint is currently active */
	@Transient
	public boolean isActive() {
		if (startDate == null || endDate == null) {
			return false;
		}
		final LocalDate now = LocalDate.now();
		return !now.isBefore(startDate) && !now.isAfter(endDate);
	}

	/** Check if the sprint is completed (end date has passed).
	 * @return true if the sprint has ended */
	@Transient
	public boolean isCompleted() {
		if (endDate == null) {
			return false;
		}
		return LocalDate.now().isAfter(endDate);
	}

	// IGanntEntityItem implementation

	/** Gets the icon for Gantt chart display.
	 * @return the sprint icon identifier */
	@Override
	public String getIcon() {
		return DEFAULT_ICON;
	}

	/** Gets the responsible user for Gantt chart display.
	 * @return the assigned user */
	@Override
	public CUser getResponsible() {
		return getAssignedTo();
	}

	@Override
	public Integer getProgressPercentage() {
		// Calculate progress based on completed activities
		if (activities == null || activities.isEmpty()) {
			return 0;
		}
		long completedCount = activities.stream()
				.filter(CActivity::isCompleted)
				.count();
		return (int) ((completedCount * 100) / activities.size());
	}

	// IHasStatusAndWorkflow implementation

	@Override
	public CWorkflowEntity getWorkflow() {
		Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	@Override
	public void initializeAllFields() {
		// Initialize lazy-loaded entity relationships
		if (getEntityType() != null) {
			getEntityType().getName(); // Trigger sprint type loading
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
		// Note: activities and meetings collections will be initialized if accessed
	}
}
