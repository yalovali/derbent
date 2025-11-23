package tech.derbent.app.sprints.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.gannt.ganntitem.service.IGanntEntityItem;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.users.domain.CUser;

// @AssociationOverride (name = "status", joinColumns = @JoinColumn (name = "sprint_status_id"))
@Entity
@Table (name = "csprint")
@AttributeOverride (name = "id", column = @Column (name = "sprint_id"))
public class CSprint extends CProjectItem<CSprint> implements IHasStatusAndWorkflow<CSprint>, IGanntEntityItem {

	public static final String DEFAULT_COLOR = "#28a745";
	public static final String DEFAULT_ICON = "vaadin:calendar-clock";
	public static final String VIEW_NAME = "Sprints View";
	// Sprint Activities - Activities included in this sprint
	@ManyToMany (fetch = FetchType.LAZY)
	@JoinTable (name = "csprint_activities", joinColumns = @JoinColumn (name = "sprint_id"), inverseJoinColumns = @JoinColumn (name = "activity_id"))
	@AMetaData (
			displayName = "Activities", required = false, readOnly = false, description = "Activities included in this sprint", hidden = false,
			order = 30, useDualListSelector = true
	)
	private Set<CActivity> activities = new HashSet<>();
	// Sprint Color for UI display
	@Column (nullable = true, length = 7)
	@Size (max = 7)
	@AMetaData (
			displayName = "Color", required = false, readOnly = false, defaultValue = DEFAULT_COLOR,
			description = "Color code for sprint visualization (hex format)", hidden = false, order = 20, colorField = true
	)
	private String color = DEFAULT_COLOR;
	// Sprint Basic Information
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Description", required = false, readOnly = false, defaultValue = "",
			description = "Detailed description of the sprint goals and objectives", hidden = false, order = 3, maxLength = 2000
	)
	private String description;
	@Column (nullable = true)
	@AMetaData (displayName = "End Date", required = true, readOnly = false, description = "Sprint end date", hidden = false, order = 11)
	private LocalDate endDate;
	// Type Management - using CSprintType
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Sprint Type", required = false, readOnly = false, description = "Type/category of the sprint", hidden = false, order = 2,
			dataProviderBean = "CSprintTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CSprintType entityType;
	// Calculated field for display - stored as transient
	@Transient
	@AMetaData (
			displayName = "Item Count", required = false, readOnly = true, description = "Total number of items in this sprint", hidden = false,
			order = 32
	)
	private Integer itemCount;
	// Sprint Meetings - Meetings included in this sprint
	@ManyToMany (fetch = FetchType.LAZY)
	@JoinTable (name = "csprint_meetings", joinColumns = @JoinColumn (name = "sprint_id"), inverseJoinColumns = @JoinColumn (name = "meeting_id"))
	@AMetaData (
			displayName = "Meetings", required = false, readOnly = false, description = "Meetings included in this sprint", hidden = false,
			order = 31, useDualListSelector = true
	)
	private Set<CMeeting> meetings = new HashSet<>();
	@Column (nullable = true)
	@AMetaData (
			displayName = "Start Date", required = false, readOnly = false, description = "Planned or actual start date of the sprint",
			hidden = false, order = 40
	)
	private LocalDate startDate;

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
		this.setStartDate(startDate);
		this.endDate = endDate;
		initializeDefaults();
	}

	/** Add an activity to this sprint.
	 * @param activity the activity to add */
	public void addActivity(final CActivity activity) {
		if (activity != null) {
			getActivities().add(activity);
			updateLastModified();
		}
	}

	/** Add a project item to this sprint. This method determines the type and adds to the appropriate collection.
	 * @param item the project item to add */
	public void addItem(final CProjectItem<?> item) {
		if (item != null) {
			if (item instanceof CActivity) {
				addActivity((CActivity) item);
			} else if (item instanceof CMeeting) {
				addMeeting((CMeeting) item);
			}
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

	/** Gets the activities in this sprint.
	 * @return set of activities */
	public Set<CActivity> getActivities() { return activities != null ? activities : new HashSet<>(); }

	public String getColor() { return color; }

	@Override
	public String getDescription() { return description; }

	@Override
	public LocalDate getEndDate() { return endDate; }

	/** Gets the sprint type.
	 * @return the sprint type */
	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

	/** Gets the icon for Gantt chart display.
	 * @return the sprint icon identifier */
	@Override
	public String getIcon() { return DEFAULT_ICON; }

	/** Get the total number of items in this sprint. This is a calculated field for UI display.
	 * @return total count of activities and meetings */
	public Integer getItemCount() {
		int count = 0;
		if (activities != null) {
			count += activities.size();
		}
		if (meetings != null) {
			count += meetings.size();
		}
		return count;
	}

	/** Get all sprint items (activities and meetings combined) as a list. This is a convenience method for backward compatibility.
	 * @return combined list of all sprint items */
	public List<CProjectItem<?>> getItems() {
		final List<CProjectItem<?>> allItems = new ArrayList<>();
		if (activities != null) {
			allItems.addAll(activities);
		}
		if (meetings != null) {
			allItems.addAll(meetings);
		}
		return allItems;
	}

	/** Gets the meetings in this sprint.
	 * @return set of meetings */
	public Set<CMeeting> getMeetings() { return meetings != null ? meetings : new HashSet<>(); }

	@Override
	public Integer getProgressPercentage() {
		// Calculate progress based on completed activities
		if (activities == null || activities.isEmpty()) {
			return 0;
		}
		long completedCount = activities.stream().filter(activity -> {
			if (activity.getStatus() != null && activity.getStatus().getFinalStatus()) {
				return true;
			}
			return false;
		}).count();
		return (int) ((completedCount * 100) / activities.size());
	}

	/** Gets the responsible user for Gantt chart display.
	 * @return the assigned user */
	@Override
	public CUser getResponsible() { return getAssignedTo(); }

	@Override
	public LocalDate getStartDate() { return startDate; }

	@Override
	public CWorkflowEntity getWorkflow() { // TODO Auto-generated method stub
		return getEntityType().getWorkflow();
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
		if (getStartDate() == null) {
			setStartDate(LocalDate.now());
		}
		if (endDate == null) {
			endDate = LocalDate.now().plusWeeks(2); // Default to 2-week sprint
		}
	}
	// Getters and Setters

	/** Check if the sprint is active (current date is between start and end dates).
	 * @return true if the sprint is currently active */
	@Transient
	public boolean isActive() {
		if (getStartDate() == null || endDate == null) {
			return false;
		}
		final LocalDate now = LocalDate.now();
		return !now.isBefore(getStartDate()) && !now.isAfter(endDate);
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

	/** Remove an activity from this sprint.
	 * @param activity the activity to remove */
	public void removeActivity(final CActivity activity) {
		if (activity != null) {
			getActivities().remove(activity);
			updateLastModified();
		}
	}

	/** Remove a project item from this sprint. This method determines the type and removes from the appropriate collection.
	 * @param item the project item to remove */
	public void removeItem(final CProjectItem<?> item) {
		if (item != null) {
			if (item instanceof CActivity) {
				removeActivity((CActivity) item);
			} else if (item instanceof CMeeting) {
				removeMeeting((CMeeting) item);
			}
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
		// Note: items collection will be initialized if accessed
	}

	/** Remove a meeting from this sprint.
	 * @param meeting the meeting to remove */
	public void removeMeeting(final CMeeting meeting) {
		if (meeting != null) {
			getMeetings().remove(meeting);
			updateLastModified();
		}
	}

	/** Sets the activities in this sprint.
	 * @param activities the activities to set */
	public void setActivities(final Set<CActivity> activities) {
		this.activities = activities != null ? activities : new HashSet<>();
		updateLastModified();
	}

	public void setColor(final String color) {
		this.color = color;
		updateLastModified();
	}

	@Override
	public void setDescription(final String description) {
		this.description = description;
		updateLastModified();
	}
	// IGanntEntityItem implementation

	public void setEndDate(final LocalDate endDate) {
		this.endDate = endDate;
		updateLastModified();
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		this.entityType = (CSprintType) typeEntity;
		updateLastModified();
	}

	public void setItemCount(final Integer itemCount) { this.itemCount = itemCount; }
	// IHasStatusAndWorkflow implementation

	public void setItems(final List<CProjectItem<?>> items) {
		if (activities == null) {
			activities = new HashSet<>();
		} else {
			activities.clear();
		}
		if (meetings == null) {
			meetings = new HashSet<>();
		} else {
			meetings.clear();
		}
		if (items != null) {
			for (final CProjectItem<?> item : items) {
				if (item instanceof CActivity) {
					activities.add((CActivity) item);
				} else if (item instanceof CMeeting) {
					meetings.add((CMeeting) item);
				}
			}
		}
		updateLastModified();
	}

	/** Sets the meetings in this sprint.
	 * @param meetings the meetings to set */
	public void setMeetings(final Set<CMeeting> meetings) {
		this.meetings = meetings != null ? meetings : new HashSet<>();
		updateLastModified();
	}

	public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
}
