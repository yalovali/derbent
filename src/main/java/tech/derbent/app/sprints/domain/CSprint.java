package tech.derbent.app.sprints.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IHasColorAndIcon;
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
public class CSprint extends CProjectItem<CSprint> implements IHasStatusAndWorkflow<CSprint>, IGanntEntityItem, IHasColorAndIcon {

	public static final String DEFAULT_COLOR = "#28a745";
	public static final String DEFAULT_ICON = "vaadin:calendar-clock";
	public static final String VIEW_NAME = "Sprints View";
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
	// Sprint Items - Ordered collection of activities and meetings included in this sprint
	// Uses OneToMany pattern with CSprintItem join entity for proper ordering
	// Similar to CDetailLines pattern in CDetailSection
	@OneToMany (mappedBy = "sprint", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	@OrderBy ("itemOrder ASC")
	@AMetaData (
			displayName = "Sprint Items", required = false, readOnly = false,
			description = "Items (activities, meetings, etc.) included in this sprint", hidden = false, order = 30,
			createComponentMethod = "createSpritActivitiesComponent", dataProviderBean = "view"
	)
	private List<CSprintItem> sprintItems = new ArrayList<>();
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
			final CSprintItem sprintItem = new CSprintItem(this, activity, sprintItems.size() + 1);
			sprintItems.add(sprintItem);
			updateLastModified();
		}
	}

	/** Add a project item to this sprint. This method determines the type and adds to the appropriate collection.
	 * @param item the project item to add */
	public void addItem(final CProjectItem<?> item) {
		if (item != null) {
			final CSprintItem sprintItem = new CSprintItem(this, item, sprintItems.size() + 1);
			sprintItems.add(sprintItem);
			updateLastModified();
		}
	}

	/** Add a meeting to this sprint.
	 * @param meeting the meeting to add */
	public void addMeeting(final CMeeting meeting) {
		if (meeting != null) {
			final CSprintItem sprintItem = new CSprintItem(this, meeting, sprintItems.size() + 1);
			sprintItems.add(sprintItem);
			updateLastModified();
		}
	}

	/** Add a sprint item to this sprint.
	 * @param sprintItem the sprint item to add */
	public void addSprintItem(final CSprintItem sprintItem) {
		if (sprintItem != null) {
			sprintItem.setSprint(this);
			if (sprintItem.getItemOrder() == null || sprintItem.getItemOrder() == 0) {
				sprintItem.setItemOrder(sprintItems.size() + 1);
			}
			sprintItems.add(sprintItem);
			updateLastModified();
		}
	}

	/** Gets the activities in this sprint.
	 * @return list of activities */
	public List<CActivity> getActivities() {
		final List<CActivity> activities = new ArrayList<>();
		if (sprintItems != null) {
			for (final CSprintItem sprintItem : sprintItems) {
				if (sprintItem.getItem() instanceof CActivity) {
					activities.add((CActivity) sprintItem.getItem());
				}
			}
		}
		return activities;
	}

	@Override
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
	 * @return total count of sprint items */
	public Integer getItemCount() { return sprintItems != null ? sprintItems.size() : 0; }

	/** Get all sprint items (activities and meetings combined) as a list. This is a convenience method for backward compatibility.
	 * @return combined list of all sprint items */
	public List<CProjectItem<?>> getItems() {
		final List<CProjectItem<?>> allItems = new ArrayList<>();
		if (sprintItems != null) {
			for (final CSprintItem sprintItem : sprintItems) {
				if (sprintItem.getItem() != null) {
					allItems.add(sprintItem.getItem());
				}
			}
		}
		return allItems;
	}

	/** Gets the meetings in this sprint.
	 * @return list of meetings */
	public List<CMeeting> getMeetings() {
		final List<CMeeting> meetings = new ArrayList<>();
		if (sprintItems != null) {
			for (final CSprintItem sprintItem : sprintItems) {
				if (sprintItem.getItem() instanceof CMeeting) {
					meetings.add((CMeeting) sprintItem.getItem());
				}
			}
		}
		return meetings;
	}

	@Override
	public Integer getProgressPercentage() {
		// Calculate progress based on completed activities
		final List<CActivity> activities = getActivities();
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

	/** Gets the sprint items collection.
	 * @return list of sprint items */
	public List<CSprintItem> getSprintItems() { return sprintItems != null ? sprintItems : new ArrayList<>(); }

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
		// Note: sprint items collection will be initialized if accessed
	}

	/** Initialize default values for the sprint. */
	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (color == null || color.isEmpty()) {
			color = DEFAULT_COLOR;
		}
		if (sprintItems == null) {
			sprintItems = new ArrayList<>();
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
		if (activity != null && sprintItems != null) {
			sprintItems.removeIf(item -> item.getItem() != null && item.getItem().equals(activity));
			updateLastModified();
		}
	}

	/** Remove a project item from this sprint. This method determines the type and removes from the appropriate collection.
	 * @param item the project item to remove */
	public void removeItem(final CProjectItem<?> item) {
		if (item != null && sprintItems != null) {
			sprintItems.removeIf(sprintItem -> sprintItem.getItem() != null && sprintItem.getItem().equals(item));
			updateLastModified();
		}
	}

	/** Remove a meeting from this sprint.
	 * @param meeting the meeting to remove */
	public void removeMeeting(final CMeeting meeting) {
		if (meeting != null && sprintItems != null) {
			sprintItems.removeIf(item -> item.getItem() != null && item.getItem().equals(meeting));
			updateLastModified();
		}
	}

	/** Sets the activities in this sprint.
	 * @param activities the activities to set */
	public void setActivities(final List<CActivity> activities) {
		if (sprintItems == null) {
			sprintItems = new ArrayList<>();
		}
		// Remove existing activities
		sprintItems.removeIf(item -> item.getItem() instanceof CActivity);
		// Add new activities
		if (activities != null) {
			int order = 1;
			for (final CActivity activity : activities) {
				final CSprintItem sprintItem = new CSprintItem(this, activity, order++);
				sprintItems.add(sprintItem);
			}
		}
		updateLastModified();
	}

	@Override
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
		if (sprintItems == null) {
			sprintItems = new ArrayList<>();
		} else {
			sprintItems.clear();
		}
		if (items != null) {
			int order = 1;
			for (final CProjectItem<?> item : items) {
				final CSprintItem sprintItem = new CSprintItem(this, item, order++);
				sprintItems.add(sprintItem);
			}
		}
		updateLastModified();
	}

	/** Sets the meetings in this sprint.
	 * @param meetings the meetings to set */
	public void setMeetings(final List<CMeeting> meetings) {
		if (sprintItems == null) {
			sprintItems = new ArrayList<>();
		}
		// Remove existing meetings
		sprintItems.removeIf(item -> item.getItem() instanceof CMeeting);
		// Add new meetings
		if (meetings != null) {
			int order = sprintItems.size() + 1;
			for (final CMeeting meeting : meetings) {
				final CSprintItem sprintItem = new CSprintItem(this, meeting, order++);
				sprintItems.add(sprintItem);
			}
		}
		updateLastModified();
	}

	/** Sets the sprint items collection.
	 * @param sprintItems the sprint items to set */
	public void setSprintItems(final List<CSprintItem> sprintItems) {
		this.sprintItems = sprintItems != null ? sprintItems : new ArrayList<>();
		updateLastModified();
	}

	public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
}
