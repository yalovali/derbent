package tech.derbent.activities.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.AMetaData;
import tech.derbent.abstracts.domains.CProjectItem;
import tech.derbent.abstracts.interfaces.CKanbanEntity;
import tech.derbent.abstracts.interfaces.CKanbanStatus;
import tech.derbent.abstracts.interfaces.CKanbanType;
import tech.derbent.activities.view.CActivitiesView;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

@Entity
@Table (name = "cactivity")
@AttributeOverride (name = "id", column = @Column (name = "activity_id"))
public class CActivity extends CProjectItem<CActivity> implements CKanbanEntity {
	private static final Logger LOGGER = LoggerFactory.getLogger(CActivity.class);

	public static String getIconColorCode() {
		return "#007bff"; // Blue color for activity entities
	}

	public static String getIconFilename() { return "vaadin:tasks"; }

	public static Class<?> getViewClass() { return CActivitiesView.class; }

	// Basic Activity Information
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "cactivitytype_id", nullable = true)
	@AMetaData (
			displayName = "Activity Type", required = false, readOnly = false, description = "Type category of the activity", hidden = false,
			order = 2, dataProviderBean = "CActivityTypeService"
	)
	private CActivityType activityType;
	// Time Tracking
	@Column (name = "estimated_hours", nullable = true, precision = 10, scale = 2)
	@DecimalMin (value = "0.0", message = "Estimated hours must be positive")
	@DecimalMax (value = "9999.99", message = "Estimated hours cannot exceed 9999.99")
	@AMetaData (
			displayName = "Estimated Hours", required = false, readOnly = false, defaultValue = "0.00",
			description = "Estimated time in hours to complete this activity", hidden = false, order = 20
	)
	private BigDecimal estimatedHours;
	@Column (name = "actual_hours", nullable = true, precision = 10, scale = 2)
	@DecimalMin (value = "0.0", message = "Actual hours must be positive")
	@DecimalMax (value = "9999.99", message = "Actual hours cannot exceed 9999.99")
	@AMetaData (
			displayName = "Actual Hours", required = false, readOnly = false, defaultValue = "0.00",
			description = "Actual time spent on this activity in hours", hidden = false, order = 21
	)
	private BigDecimal actualHours = BigDecimal.ZERO;
	@Column (name = "remaining_hours", nullable = true, precision = 10, scale = 2)
	@DecimalMin (value = "0.0", message = "Remaining hours must be positive")
	@DecimalMax (value = "9999.99", message = "Remaining hours cannot exceed 9999.99")
	@AMetaData (
			displayName = "Remaining Hours", required = false, readOnly = false, defaultValue = "0.00",
			description = "Estimated remaining time in hours", hidden = false, order = 22
	)
	private BigDecimal remainingHours;
	// Status and Priority Management
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "cactivitystatus_id", nullable = true)
	@AMetaData (
			displayName = "Status", required = false, readOnly = false, description = "Current status of the activity", hidden = false, order = 30,
			dataProviderBean = "CActivityStatusService"
	)
	private CActivityStatus status;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "cactivitypriority_id", nullable = true)
	@AMetaData (
			displayName = "Priority", required = false, readOnly = false, description = "Priority level of the activity", hidden = false, order = 31,
			dataProviderBean = "CActivityPriorityService"
	)
	private CActivityPriority priority;
	@Column (name = "progress_percentage", nullable = true)
	@Min (value = 0, message = "Progress percentage must be between 0 and 100")
	@Max (value = 100, message = "Progress percentage must be between 0 and 100")
	@AMetaData (
			displayName = "Progress %", required = false, readOnly = false, defaultValue = "0", description = "Completion percentage (0-100)",
			hidden = false, order = 32
	)
	private Integer progressPercentage = 0;
	// Date Management
	@Column (name = "start_date", nullable = true)
	@AMetaData (
			displayName = "Start Date", required = false, readOnly = false, description = "Planned or actual start date of the activity",
			hidden = false, order = 40
	)
	private LocalDate startDate;
	@Column (name = "due_date", nullable = true)
	@AMetaData (displayName = "Due Date", required = false, readOnly = false, description = "Expected completion date", hidden = false, order = 41)
	private LocalDate dueDate;
	@Column (name = "completion_date", nullable = true)
	@AMetaData (
			displayName = "Completion Date", required = false, readOnly = true, description = "Actual completion date", hidden = false, order = 42
	)
	private LocalDate completionDate;
	// Budget Management
	@Column (name = "estimated_cost", nullable = true, precision = 12, scale = 2)
	@DecimalMin (value = "0.0", message = "Estimated cost must be positive")
	@DecimalMax (value = "999999.99", message = "Estimated cost cannot exceed 999999.99")
	@AMetaData (
			displayName = "Estimated Cost", required = false, readOnly = false, defaultValue = "0.00",
			description = "Estimated cost to complete this activity", hidden = false, order = 50
	)
	private BigDecimal estimatedCost;
	@Column (name = "actual_cost", nullable = true, precision = 12, scale = 2)
	@DecimalMin (value = "0.0", message = "Actual cost must be positive")
	@DecimalMax (value = "999999.99", message = "Actual cost cannot exceed 999999.99")
	@AMetaData (
			displayName = "Actual Cost", required = false, readOnly = false, defaultValue = "0.00",
			description = "Actual cost spent on this activity", hidden = false, order = 51
	)
	private BigDecimal actualCost = BigDecimal.ZERO;
	@Column (name = "hourly_rate", nullable = true, precision = 10, scale = 2)
	@DecimalMin (value = "0.0", message = "Hourly rate must be positive")
	@DecimalMax (value = "9999.99", message = "Hourly rate cannot exceed 9999.99")
	@AMetaData (
			displayName = "Hourly Rate", required = false, readOnly = false, defaultValue = "0.00", description = "Hourly rate for cost calculation",
			hidden = false, order = 52
	)
	private BigDecimal hourlyRate;
	// Additional Information
	@Column (name = "acceptance_criteria", nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Acceptance Criteria", required = false, readOnly = false, defaultValue = "",
			description = "Criteria that must be met for the activity to be considered complete", hidden = false, order = 70, maxLength = 2000
	)
	private String acceptanceCriteria;
	@Column (name = "notes", nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Notes", required = false, readOnly = false, defaultValue = "", description = "Additional notes and comments",
			hidden = false, order = 71, maxLength = 2000
	)
	private String notes;
	@Column (name = "results", nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Results", required = false, readOnly = false, defaultValue = "", description = "Results and outcomes of the activity",
			hidden = false, order = 72, maxLength = 2000
	)
	private String results;

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
		if ((actualCost == null) || (estimatedCost == null)) {
			LOGGER.debug("calculateCostVariance() - Missing cost data, actual={}, estimated={}", actualCost, estimatedCost);
			return BigDecimal.ZERO;
		}
		return actualCost.subtract(estimatedCost);
	}

	/** Calculate the time variance (actual hours - estimated hours).
	 * @return the time variance, positive if over estimated, negative if under estimated */
	public BigDecimal calculateTimeVariance() {
		if ((actualHours == null) || (estimatedHours == null)) {
			return BigDecimal.ZERO;
		}
		final BigDecimal variance = actualHours.subtract(estimatedHours);
		LOGGER.debug("calculateTimeVariance() - Time variance calculated: {}", variance);
		return variance;
	}

	public String getAcceptanceCriteria() { return acceptanceCriteria; }

	public CActivityType getActivityType() { return activityType; }

	public BigDecimal getActualCost() { return actualCost != null ? actualCost : BigDecimal.ZERO; }
	// Getters and Setters with proper logging and null checking

	public BigDecimal getActualHours() { return actualHours != null ? actualHours : BigDecimal.ZERO; }

	public LocalDate getCompletionDate() { return completionDate; }

	public LocalDate getDueDate() { return dueDate; }

	public BigDecimal getEstimatedCost() { return estimatedCost; }

	public BigDecimal getEstimatedHours() { return estimatedHours; }

	public BigDecimal getHourlyRate() { return hourlyRate; }

	public String getNotes() { return notes; }

	public CActivityPriority getPriority() { return priority; }

	public Integer getProgressPercentage() { return progressPercentage != null ? progressPercentage : 0; }

	public BigDecimal getRemainingHours() { return remainingHours; }

	public String getResults() { return results; }

	public LocalDate getStartDate() { return startDate; }

	@Override
	public CActivityStatus getStatus() { return status; }

	@Override
	public CKanbanType getType() { return activityType; }

	/** Initialize default values for the activity. */
	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (this.actualHours == null) {
			this.actualHours = BigDecimal.ZERO;
		}
		if (this.actualCost == null) {
			this.actualCost = BigDecimal.ZERO;
		}
		if (this.progressPercentage == null) {
			this.progressPercentage = 0;
		}
		if (this.estimatedHours == null) {
			this.estimatedHours = BigDecimal.ZERO;
		}
		if (this.estimatedCost == null) {
			this.estimatedCost = BigDecimal.ZERO;
		}
		if (this.remainingHours == null) {
			this.remainingHours = BigDecimal.ZERO;
		}
		if (this.hourlyRate == null) {
			this.hourlyRate = BigDecimal.ZERO;
		}
		if (this.startDate == null) {
			this.startDate = LocalDate.now();
		}
		if (this.dueDate == null) {
			this.dueDate = LocalDate.now().plusDays(7); // Default to 1 week from today
		}
		if (this.completionDate == null) {
			this.completionDate = null; // No completion date by default
		}
	}

	/** Check if the activity is completed.
	 * @return true if the activity has a completion date or progress is 100% */
	public boolean isCompleted() {
		final boolean hasCompletionDate = completionDate != null;
		final boolean isFullProgress = (progressPercentage != null) && (progressPercentage >= 100);
		final boolean isFinalStatus = (status != null) && status.getFinalStatus();
		final boolean completed = hasCompletionDate || isFullProgress || isFinalStatus;
		LOGGER.debug("isCompleted() - Activity id={} completed={} (completionDate={}, progress={}, finalStatus={})", getId(), completed,
				hasCompletionDate, progressPercentage, isFinalStatus);
		return completed;
	}

	/** Check if the activity is overdue.
	 * @return true if the due date has passed and the activity is not completed */
	public boolean isOverdue() {
		if ((dueDate == null) || isCompleted()) {
			return false;
		}
		final boolean overdue = LocalDate.now().isAfter(dueDate);
		LOGGER.debug("isOverdue() - Activity id={} overdue={} (dueDate={}, today={})", getId(), overdue, dueDate, LocalDate.now());
		return overdue;
	}

	public void setAcceptanceCriteria(final String acceptanceCriteria) {
		this.acceptanceCriteria = acceptanceCriteria;
		updateLastModified();
	}

	public void setActivityType(final CActivityType activityType) {
		this.activityType = activityType;
		updateLastModified();
	}

	public void setActualCost(final BigDecimal actualCost) {
		if ((actualCost != null) && (actualCost.compareTo(BigDecimal.ZERO) < 0)) {
			LOGGER.warn("setActualCost - Attempting to set negative actual cost: {} for activity id={}", actualCost, getId());
		}
		this.actualCost = actualCost != null ? actualCost : BigDecimal.ZERO;
		updateLastModified();
	}

	public void setActualHours(final BigDecimal actualHours) {
		if ((actualHours != null) && (actualHours.compareTo(BigDecimal.ZERO) < 0)) {
			LOGGER.warn("setActualHours - Attempting to set negative actual hours: {} for activity id={}", actualHours, getId());
		}
		this.actualHours = actualHours != null ? actualHours : BigDecimal.ZERO;
		updateLastModified();
	}

	public void setCompletionDate(final LocalDate completionDate) {
		this.completionDate = completionDate;
		if ((completionDate != null) && (progressPercentage != null) && (progressPercentage < 100)) {
			this.progressPercentage = 100;
		}
		updateLastModified();
	}

	public void setDueDate(final LocalDate dueDate) {
		this.dueDate = dueDate;
		updateLastModified();
	}

	public void setEstimatedCost(final BigDecimal estimatedCost) {
		if ((estimatedCost != null) && (estimatedCost.compareTo(BigDecimal.ZERO) < 0)) {
			LOGGER.warn("setEstimatedCost - Attempting to set negative estimated cost: {} for activity id={}", estimatedCost, getId());
		}
		this.estimatedCost = estimatedCost;
		updateLastModified();
	}

	public void setEstimatedHours(final BigDecimal estimatedHours) {
		if ((estimatedHours != null) && (estimatedHours.compareTo(BigDecimal.ZERO) < 0)) {
			LOGGER.warn("setEstimatedHours - Attempting to set negative estimated hours: {} for activity id={}", estimatedHours, getId());
		}
		this.estimatedHours = estimatedHours;
		updateLastModified();
	}

	public void setHourlyRate(final BigDecimal hourlyRate) {
		if ((hourlyRate != null) && (hourlyRate.compareTo(BigDecimal.ZERO) < 0)) {
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
		if ((progressPercentage != null) && ((progressPercentage < 0) || (progressPercentage > 100))) {
			LOGGER.warn("setProgressPercentage - Invalid progress percentage: {} for activity id={}", progressPercentage, getId());
			return;
		}
		this.progressPercentage = progressPercentage != null ? progressPercentage : 0;
		// Auto-set completion date if progress reaches 100%
		if ((progressPercentage != null) && (progressPercentage >= 100) && (completionDate == null)) {
			this.completionDate = LocalDate.now();
		}
		updateLastModified();
	}

	public void setRemainingHours(final BigDecimal remainingHours) {
		if ((remainingHours != null) && (remainingHours.compareTo(BigDecimal.ZERO) < 0)) {
			LOGGER.warn("setRemainingHours - Attempting to set negative remaining hours: {} for activity id={}", remainingHours, getId());
		}
		this.remainingHours = remainingHours;
		updateLastModified();
	}

	public void setResults(final String results) {
		this.results = results;
		updateLastModified();
	}

	public void setStartDate(final LocalDate startDate) {
		this.startDate = startDate;
		updateLastModified();
	}

	public void setStatus(final CActivityStatus status) {
		this.status = status;
		// Auto-set completion date if status is final
		if ((status != null) && status.getFinalStatus() && (completionDate == null)) {
			this.completionDate = LocalDate.now();
			if ((progressPercentage != null) && (progressPercentage < 100)) {
				this.progressPercentage = 100;
			}
		}
		updateLastModified();
	}

	// CKanbanEntity implementation methods
	@Override
	public void setStatus(final CKanbanStatus status) {
		if (status instanceof CActivityStatus) {
			setStatus((CActivityStatus) status);
		}
	}
}
