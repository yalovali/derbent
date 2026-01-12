package tech.derbent.app.activities.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.interfaces.IHasIcon;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.utils.Check;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.app.gannt.ganntitem.service.IGanntEntityItem;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.app.sprints.service.CSprintItemService;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.users.domain.CUser;

@Entity
@Table (name = "cactivity")
@AttributeOverride (name = "id", column = @Column (name = "activity_id"))
public class CActivity extends CProjectItem<CActivity> implements IHasStatusAndWorkflow<CActivity>, IGanntEntityItem, ISprintableItem, IHasIcon {

	public static final String DEFAULT_COLOR = "#4966B0"; // OpenWindows Selection Blue - actionable items
	public static final String DEFAULT_ICON = "vaadin:tasks";
	public static final String ENTITY_TITLE_PLURAL = "Activities";
	public static final String ENTITY_TITLE_SINGULAR = "Activity";
	private static final Logger LOGGER = LoggerFactory.getLogger(CActivity.class);
	public static final String VIEW_NAME = "Activities View";
	// Additional Information
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Acceptance Criteria", required = false, readOnly = false, defaultValue = "",
			description = "Criteria that must be met for the activity to be considered complete", hidden = false, maxLength = 2000
	)
	private String acceptanceCriteria;
	// Basic Activity Information
	@Column (nullable = true, precision = 12, scale = 2)
	@DecimalMin (value = "0.0", message = "Actual cost must be positive")
	@DecimalMax (value = "999999.99", message = "Actual cost cannot exceed 999999.99")
	@AMetaData (
			displayName = "Actual Cost", required = false, readOnly = false, defaultValue = "0.00",
			description = "Actual cost spent on this activity", hidden = false
	)
	private BigDecimal actualCost = BigDecimal.ZERO;
	@Column (nullable = true, precision = 10, scale = 2)
	@DecimalMin (value = "0.0", message = "Actual hours must be positive")
	@DecimalMax (value = "9999.99", message = "Actual hours cannot exceed 9999.99")
	@AMetaData (
			displayName = "Actual Hours", required = false, readOnly = false, defaultValue = "0.00",
			description = "Actual time spent on this activity in hours", hidden = false
	)
	private BigDecimal actualHours = BigDecimal.ZERO;
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (mappedBy = "activity", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	private List<CComment> comments = new ArrayList<>();
	@Column (name = "completion_date", nullable = true)
	@AMetaData (displayName = "Completion Date", required = false, readOnly = true, description = "Actual completion date", hidden = false)
	private LocalDate completionDate;
	@AMetaData (
			displayName = "Component Widget", required = false, readOnly = false, description = "Component Widget for item", hidden = false,
			dataProviderBean = "pageservice", dataProviderMethod = "getComponentWidget"
	)
	private final CComponentWidgetEntity<CActivity> componentWidget = null;
	@Column (nullable = true)
	@AMetaData (displayName = "Due Date", required = false, readOnly = false, description = "Expected completion date", hidden = false)
	private LocalDate dueDate;
	// Type Management - concrete implementation of parent's typeEntity
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Activity Type", required = false, readOnly = false, description = "Type category of the activity", hidden = false,
			dataProviderBean = "CActivityTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CActivityType entityType;
	// Budget Management
	@Column (nullable = true, precision = 12, scale = 2)
	@DecimalMin (value = "0.0", message = "Estimated cost must be positive")
	@DecimalMax (value = "999999.99", message = "Estimated cost cannot exceed 999999.99")
	@AMetaData (
			displayName = "Estimated Cost", required = false, readOnly = false, defaultValue = "0.00",
			description = "Estimated cost to complete this activity", hidden = false
	)
	private BigDecimal estimatedCost;
	// Time Tracking
	@Column (nullable = true, precision = 10, scale = 2)
	@DecimalMin (value = "0.0", message = "Estimated hours must be positive")
	@DecimalMax (value = "9999.99", message = "Estimated hours cannot exceed 9999.99")
	@AMetaData (
			displayName = "Estimated Hours", required = false, readOnly = false, defaultValue = "0.00",
			description = "Estimated time in hours to complete this activity", hidden = false
	)
	private BigDecimal estimatedHours;
	@Column (nullable = true, precision = 10, scale = 2)
	@DecimalMin (value = "0.0", message = "Hourly rate must be positive")
	@DecimalMax (value = "9999.99", message = "Hourly rate cannot exceed 9999.99")
	@AMetaData (
			displayName = "Hourly Rate", required = false, readOnly = false, defaultValue = "0.00", description = "Hourly rate for cost calculation",
			hidden = false
	)
	private BigDecimal hourlyRate;
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Notes", required = false, readOnly = false, defaultValue = "", description = "Additional notes and comments",
			hidden = false, maxLength = 2000
	)
	private String notes;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "cactivitypriority_id", nullable = true)
	@AMetaData (
			displayName = "Priority", required = false, readOnly = false, description = "Priority level of the activity", hidden = false,
			dataProviderBean = "CActivityPriorityService", setBackgroundFromColor = true, useIcon = true
	)
	private CActivityPriority priority;
	@Column (nullable = true)
	@Min (value = 0, message = "Progress percentage must be between 0 and 100")
	@Max (value = 100, message = "Progress percentage must be between 0 and 100")
	@AMetaData (
			displayName = "Progress %", required = false, readOnly = false, defaultValue = "0", description = "Completion percentage (0-100)",
			hidden = false
	)
	private Integer progressPercentage = 0;
	@Column (nullable = true, precision = 10, scale = 2)
	@DecimalMin (value = "0.0", message = "Remaining hours must be positive")
	@DecimalMax (value = "9999.99", message = "Remaining hours cannot exceed 9999.99")
	@AMetaData (
			displayName = "Remaining Hours", required = false, readOnly = false, defaultValue = "0.00",
			description = "Estimated remaining time in hours", hidden = false
	)
	private BigDecimal remainingHours;
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Results", required = false, readOnly = false, defaultValue = "", description = "Results and outcomes of the activity",
			hidden = false, maxLength = 2000
	)
	private String results;
	// Sprint Item relationship - REQUIRED: every activity must have a sprint item for progress tracking
	@OneToOne (fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn (name = "sprintitem_id", nullable = false)
	@NotNull (message = "Sprint item is required for progress tracking")
	@AMetaData (displayName = "Sprint Item", required = true, readOnly = true, description = "Progress tracking for this activity", hidden = true)
	private CSprintItem sprintItem;
	@Column (name = "sprint_order", nullable = true)
	@Min (value = 1, message = "Sprint order must be positive")
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
	@Column (nullable = true)
	@AMetaData (
			displayName = "Story Points", required = false, readOnly = false, defaultValue = "0",
			description = "Estimated effort or complexity in story points", hidden = false
	)
	private Long storyPoint;

	/** Default constructor for JPA. */
	public CActivity() {
		super();
		// Initialize with default values for JPA
		initializeDefaults();
	}

	/** Constructor with name and project.
	 * @param name    the name of the activity - must not be null
	 * @param project the project this activity belongs to - must not be null */
	public CActivity(final String name, final CProject project) {
		super(CActivity.class, name, project);
		initializeDefaults();
	}

	/** Constructor with name, project, and assigned user.
	 * @param name       the name of the activity - must not be null
	 * @param project    the project this activity belongs to - must not be null
	 * @param assignedTo the user assigned to this activity - can be null */
	public CActivity(final String name, final CProject project, final CUser assignedTo) {
		super(CActivity.class, name, project);
		initializeDefaults();
		setAssignedTo(assignedTo);
	}

	/** Calculate the cost variance (actual cost - estimated cost).
	 * @return the cost variance, positive if over budget, negative if under budget */
	public BigDecimal calculateCostVariance() {
		if (actualCost == null || estimatedCost == null) {
			LOGGER.debug("calculateCostVariance() - Missing cost data, actual={}, estimated={}", actualCost, estimatedCost);
			return BigDecimal.ZERO;
		}
		return actualCost.subtract(estimatedCost);
	}

	/** Calculate the time variance (actual hours - estimated hours).
	 * @return the time variance, positive if over estimated, negative if under estimated */
	public BigDecimal calculateTimeVariance() {
		if (actualHours == null || estimatedHours == null) {
			return BigDecimal.ZERO;
		}
		final BigDecimal variance = actualHours.subtract(estimatedHours);
		LOGGER.debug("calculateTimeVariance() - Time variance calculated: {}", variance);
		return variance;
	}

	@jakarta.persistence.PostLoad
	protected void ensureSprintItemParent() {
		if (sprintItem != null) {
			sprintItem.setParentItem(this);
		}
	}

	public String getAcceptanceCriteria() { return acceptanceCriteria; }

	public BigDecimal getActualCost() { return actualCost != null ? actualCost : BigDecimal.ZERO; }
	// Getters and Setters with proper logging and null checking

	public BigDecimal getActualHours() { return actualHours != null ? actualHours : BigDecimal.ZERO; }

	@Override
	public String getColor() { return DEFAULT_COLOR; }

	/** Gets the list of comments associated with this activity.
	 * @return list of comments, never null */
	public List<CComment> getComments() { return comments != null ? comments : new ArrayList<>(); }

	public LocalDate getCompletionDate() { return completionDate; }

	public CComponentWidgetEntity<CActivity> getComponentWidget() { return componentWidget; }

	public LocalDate getDueDate() { return dueDate; }

	/** Gets the end date for Gantt chart display (same as due date for activities).
	 * @return the due date */
	@Override
	public LocalDate getEndDate() { return dueDate; }

	/** Gets the activity type.
	 * @return the activity type */
	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

	public BigDecimal getEstimatedCost() { return estimatedCost; }

	public BigDecimal getEstimatedHours() { return estimatedHours; }

	public BigDecimal getHourlyRate() { return hourlyRate; }

	/** Gets the icon for Gantt chart display.
	 * @return the activity icon identifier */
	@Override
	public String getIconString() { return DEFAULT_ICON; }

	public String getNotes() { return notes; }

	public CActivityPriority getPriority() { return priority; }

	@Override
	public Integer getProgressPercentage() {
		Check.notNull(sprintItem, "Sprint item must not be null");
		return sprintItem.getProgressPercentage();
	}

	public BigDecimal getRemainingHours() { return remainingHours; }

	public String getResults() { return results; }

	@Override
	public CSprintItem getSprintItem() { return sprintItem; }

	@Override
	public Integer getSprintOrder() { return sprintOrder; }

	@Override
	public LocalDate getStartDate() {
		Check.notNull(sprintItem, "Sprint item must not be null");
		return sprintItem.getStartDate();
	}

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

	/** Initialize default values for the activity. */
	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (actualHours == null) {
			actualHours = BigDecimal.ZERO;
		}
		if (actualCost == null) {
			actualCost = BigDecimal.ZERO;
		}
		if (progressPercentage == null) {
			progressPercentage = 0;
		}
		if (estimatedHours == null) {
			estimatedHours = BigDecimal.ZERO;
		}
		if (estimatedCost == null) {
			estimatedCost = BigDecimal.ZERO;
		}
		if (remainingHours == null) {
			remainingHours = BigDecimal.ZERO;
		}
		if (hourlyRate == null) {
			hourlyRate = BigDecimal.ZERO;
		}
		if (startDate == null) {
			startDate = LocalDate.now();
		}
		if (dueDate == null) {
			dueDate = LocalDate.now().plusDays(7); // Default to 1 week from today
		}
		if (completionDate == null) {
			completionDate = null; // No completion date by default
		}
		// Ensure sprint item is always created for composition pattern
		if (sprintItem == null) {
			sprintItem = CSprintItemService.createDefaultSprintItem();
		}
		// Set back-reference so sprintItem can access parent for display
		if (sprintItem != null) {
			sprintItem.setParentItem(this);
		}
	}

	/** Check if the activity is completed.
	 * @return true if the activity has a completion date or progress is 100% */
	public boolean isCompleted() {
		final boolean hasCompletionDate = completionDate != null;
		final boolean isFullProgress = progressPercentage != null && progressPercentage >= 100;
		final boolean isFinalStatus = status != null && status.getFinalStatus();
		final boolean completed = hasCompletionDate || isFullProgress || isFinalStatus;
		LOGGER.debug("isCompleted() - Activity id={} completed={} (completionDate={}, progress={}, finalStatus={})", getId(), completed,
				hasCompletionDate, progressPercentage, isFinalStatus);
		return completed;
	}

	/** Check if the activity is overdue.
	 * @return true if the due date has passed and the activity is not completed */
	public boolean isOverdue() {
		if (dueDate == null || isCompleted()) {
			return false;
		}
		final boolean overdue = LocalDate.now().isAfter(dueDate);
		LOGGER.debug("isOverdue() - Activity id={} overdue={} (dueDate={}, today={})", getId(), overdue, dueDate, LocalDate.now());
		return overdue;
	}

	/** Checks if this entity matches the given search value in the specified fields. This implementation extends CProjectItem to also search in
	 * activity-specific entity fields like entityType and priority.
	 * @param searchValue the value to search for (case-insensitive)
	 * @param fieldNames  the list of field names to search in. If null or empty, searches only in "name" field. Supported field names: all parent
	 *                    fields plus "entityType", "priority"
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
		if (fieldNames.remove("priority") && getPriority() != null && getPriority().matchesFilter(lowerSearchValue, Arrays.asList("name"))) {
			return true;
		}
		return false;
	}

	public void setAcceptanceCriteria(final String acceptanceCriteria) {
		this.acceptanceCriteria = acceptanceCriteria;
		updateLastModified();
	}

	public void setActualCost(final BigDecimal actualCost) {
		if (actualCost != null && actualCost.compareTo(BigDecimal.ZERO) < 0) {
			LOGGER.warn("setActualCost - Attempting to set negative actual cost: {} for activity id={}", actualCost, getId());
		}
		this.actualCost = actualCost != null ? actualCost : BigDecimal.ZERO;
		updateLastModified();
	}

	public void setActualHours(final BigDecimal actualHours) {
		if (actualHours != null && actualHours.compareTo(BigDecimal.ZERO) < 0) {
			LOGGER.warn("setActualHours - Attempting to set negative actual hours: {} for activity id={}", actualHours, getId());
		}
		this.actualHours = actualHours != null ? actualHours : BigDecimal.ZERO;
		updateLastModified();
	}

	@Override
	public void setColor(String color) { /*****/
	}

	/** Sets the list of comments for this activity.
	 * @param comments the list of comments */
	public void setComments(final List<CComment> comments) {
		this.comments = comments != null ? comments : new ArrayList<>();
		updateLastModified();
	}

	public void setCompletionDate(final LocalDate completionDate) {
		this.completionDate = completionDate;
		if (completionDate != null && progressPercentage != null && progressPercentage < 100) {
			progressPercentage = 100;
		}
		updateLastModified();
	}

	public void setDueDate(final LocalDate dueDate) {
		this.dueDate = dueDate;
		updateLastModified();
	}

	/** Override to set concrete type entity.
	 * @param typeEntity the type entity to set */
	@Override
	public void setEntityType(final CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CActivityType.class, "Type entity must be an instance of CActivityType");
		Check.notNull(getProject(), "Project must be set before assigning activity type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning activity type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning activity type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()), "Type entity company id "
				+ typeEntity.getCompany().getId() + " does not match activity project company id " + getProject().getCompany().getId());
		entityType = (CActivityType) typeEntity;
		updateLastModified();
	}

	public void setEstimatedCost(final BigDecimal estimatedCost) {
		if (estimatedCost != null && estimatedCost.compareTo(BigDecimal.ZERO) < 0) {
			LOGGER.warn("setEstimatedCost - Attempting to set negative estimated cost: {} for activity id={}", estimatedCost, getId());
		}
		this.estimatedCost = estimatedCost;
		updateLastModified();
	}

	public void setEstimatedHours(final BigDecimal estimatedHours) {
		if (estimatedHours != null && estimatedHours.compareTo(BigDecimal.ZERO) < 0) {
			LOGGER.warn("setEstimatedHours - Attempting to set negative estimated hours: {} for activity id={}", estimatedHours, getId());
		}
		this.estimatedHours = estimatedHours;
		updateLastModified();
	}

	public void setHourlyRate(final BigDecimal hourlyRate) {
		if (hourlyRate != null && hourlyRate.compareTo(BigDecimal.ZERO) < 0) {
			LOGGER.warn("setHourlyRate - Attempting to set negative hourly rate: {} for activity id={}", hourlyRate, getId());
		}
		this.hourlyRate = hourlyRate;
		updateLastModified();
	}

	public void setNotes(final String notes) {
		this.notes = notes;
		updateLastModified();
	}

	public void setPriority(final CActivityPriority priority) {
		this.priority = priority;
		updateLastModified();
	}

	public void setProgressPercentage(final Integer progressPercentage) {
		Check.notNull(sprintItem, "Sprint item must not be null");
		sprintItem.setProgressPercentage(progressPercentage);
		updateLastModified();
	}

	public void setRemainingHours(final BigDecimal remainingHours) {
		if (remainingHours != null && remainingHours.compareTo(BigDecimal.ZERO) < 0) {
			LOGGER.warn("setRemainingHours - Attempting to set negative remaining hours: {} for activity id={}", remainingHours, getId());
		}
		this.remainingHours = remainingHours;
		updateLastModified();
	}

	public void setResults(final String results) {
		this.results = results;
		updateLastModified();
	}

	@Override
	public void setSprintItem(CSprintItem sprintItem) { this.sprintItem = sprintItem; }

	@Override
	public void setSprintOrder(final Integer sprintOrder) { this.sprintOrder = sprintOrder; }

	public void setStartDate(final LocalDate startDate) {
		Check.notNull(sprintItem, "Sprint item must not be null");
		sprintItem.setStartDate(startDate);
		this.startDate = startDate; // Keep for backward compatibility during migration
		updateLastModified();
	}

	@Override
	public void setStatus(final CProjectItemStatus status) {
		super.setStatus(status);
		// Auto-set completion date in sprint item if status is final
		Check.notNull(sprintItem, "Sprint item must not be null");
		if (status != null && status.getFinalStatus() && sprintItem.getCompletionDate() == null) {
			sprintItem.setCompletionDate(LocalDate.now());
			if (sprintItem.getProgressPercentage() < 100) {
				sprintItem.setProgressPercentage(100);
			}
		}
	}

	@Override
	public void setStoryPoint(final Long storyPoint) {
		Check.notNull(sprintItem, "Sprint item must not be null");
		sprintItem.setStoryPoint(storyPoint);
		this.storyPoint = storyPoint; // Keep for backward compatibility during migration
		updateLastModified();
	}
}
