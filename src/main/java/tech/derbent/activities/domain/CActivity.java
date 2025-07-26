package tech.derbent.activities.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * CActivity - Enhanced domain entity representing project activities with comprehensive
 * management features. Layer: Domain (MVC) Supports: - Resource management (user
 * assignments, time tracking) - Task tracking (status, priority, progress) - Project
 * management (milestones, dependencies, deliverables) - Budget planning (cost estimation,
 * actual vs planned tracking) Inspired by Jira and ProjeQtOr functionality for
 * professional project management.
 */
@Entity
@Table (name = "cactivity") // table name for the entity as the default is the class name
							// in lowercase
@AttributeOverride (name = "id", column = @Column (name = "activity_id")) // Override the
																			// default
																			// column name
																			// for the ID
																			// field
public class CActivity extends CEntityOfProject {

	private static final Logger logger = LoggerFactory.getLogger(CActivity.class);

	// Basic Activity Information
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "cactivitytype_id", nullable = true)
	@MetaData (
		displayName = "Activity Type", required = false, readOnly = false,
		description = "Type category of the activity", hidden = false, order = 2,
		dataProviderBean = "CActivityTypeService"
	)
	private CActivityType activityType;

	@Column (name = "description", nullable = true, length = 2000)
	@Size (max = 2000)
	@MetaData (
		displayName = "Description", required = false, readOnly = false,
		defaultValue = "", description = "Detailed description of the activity",
		hidden = false, order = 3, maxLength = 2000
	)
	private String description;

	// Resource Management
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "assigned_to_id", nullable = true)
	@MetaData (
		displayName = "Assigned To", required = false, readOnly = false,
		description = "User assigned to this activity", hidden = false, order = 10,
		dataProviderBean = "CUserService"
	)
	private CUser assignedTo;

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "created_by_id", nullable = true)
	@MetaData (
		displayName = "Created By", required = false, readOnly = true,
		description = "User who created this activity", hidden = false, order = 11,
		dataProviderBean = "CUserService"
	)
	private CUser createdBy;

	// Time Tracking
	@Column (name = "estimated_hours", nullable = true, precision = 10, scale = 2)
	@DecimalMin (value = "0.0", message = "Estimated hours must be positive")
	@DecimalMax (value = "9999.99", message = "Estimated hours cannot exceed 9999.99")
	@MetaData (
		displayName = "Estimated Hours", required = false, readOnly = false,
		defaultValue = "0.00",
		description = "Estimated time in hours to complete this activity", hidden = false,
		order = 20
	)
	private BigDecimal estimatedHours;

	@Column (name = "actual_hours", nullable = true, precision = 10, scale = 2)
	@DecimalMin (value = "0.0", message = "Actual hours must be positive")
	@DecimalMax (value = "9999.99", message = "Actual hours cannot exceed 9999.99")
	@MetaData (
		displayName = "Actual Hours", required = false, readOnly = false,
		defaultValue = "0.00",
		description = "Actual time spent on this activity in hours", hidden = false,
		order = 21
	)
	private BigDecimal actualHours = BigDecimal.ZERO;

	@Column (name = "remaining_hours", nullable = true, precision = 10, scale = 2)
	@DecimalMin (value = "0.0", message = "Remaining hours must be positive")
	@DecimalMax (value = "9999.99", message = "Remaining hours cannot exceed 9999.99")
	@MetaData (
		displayName = "Remaining Hours", required = false, readOnly = false,
		defaultValue = "0.00", description = "Estimated remaining time in hours",
		hidden = false, order = 22
	)
	private BigDecimal remainingHours;

	// Status and Priority Management
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "cactivitystatus_id", nullable = true)
	@MetaData (
		displayName = "Status", required = false, readOnly = false,
		description = "Current status of the activity", hidden = false, order = 30,
		dataProviderBean = "CActivityStatusService"
	)
	private CActivityStatus status;

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "cactivitypriority_id", nullable = true)
	@MetaData (
		displayName = "Priority", required = false, readOnly = false,
		description = "Priority level of the activity", hidden = false, order = 31,
		dataProviderBean = "CActivityPriorityService"
	)
	private CActivityPriority priority;

	@Column (name = "progress_percentage", nullable = true)
	@Min (value = 0, message = "Progress percentage must be between 0 and 100")
	@Max (value = 100, message = "Progress percentage must be between 0 and 100")
	@MetaData (
		displayName = "Progress %", required = false, readOnly = false,
		defaultValue = "0", description = "Completion percentage (0-100)", hidden = false,
		order = 32
	)
	private Integer progressPercentage = 0;

	// Date Management
	@Column (name = "start_date", nullable = true)
	@MetaData (
		displayName = "Start Date", required = false, readOnly = false,
		description = "Planned or actual start date of the activity", hidden = false,
		order = 40
	)
	private LocalDate startDate;

	@Column (name = "due_date", nullable = true)
	@MetaData (
		displayName = "Due Date", required = false, readOnly = false,
		description = "Expected completion date", hidden = false, order = 41
	)
	private LocalDate dueDate;

	@Column (name = "completion_date", nullable = true)
	@MetaData (
		displayName = "Completion Date", required = false, readOnly = true,
		description = "Actual completion date", hidden = false, order = 42
	)
	private LocalDate completionDate;

	// Budget Management
	@Column (name = "estimated_cost", nullable = true, precision = 12, scale = 2)
	@DecimalMin (value = "0.0", message = "Estimated cost must be positive")
	@DecimalMax (value = "999999.99", message = "Estimated cost cannot exceed 999999.99")
	@MetaData (
		displayName = "Estimated Cost", required = false, readOnly = false,
		defaultValue = "0.00", description = "Estimated cost to complete this activity",
		hidden = false, order = 50
	)
	private BigDecimal estimatedCost;

	@Column (name = "actual_cost", nullable = true, precision = 12, scale = 2)
	@DecimalMin (value = "0.0", message = "Actual cost must be positive")
	@DecimalMax (value = "999999.99", message = "Actual cost cannot exceed 999999.99")
	@MetaData (
		displayName = "Actual Cost", required = false, readOnly = false,
		defaultValue = "0.00", description = "Actual cost spent on this activity",
		hidden = false, order = 51
	)
	private BigDecimal actualCost = BigDecimal.ZERO;

	@Column (name = "hourly_rate", nullable = true, precision = 10, scale = 2)
	@DecimalMin (value = "0.0", message = "Hourly rate must be positive")
	@DecimalMax (value = "9999.99", message = "Hourly rate cannot exceed 9999.99")
	@MetaData (
		displayName = "Hourly Rate", required = false, readOnly = false,
		defaultValue = "0.00", description = "Hourly rate for cost calculation",
		hidden = false, order = 52
	)
	private BigDecimal hourlyRate;

	// Hierarchical Structure Support
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "parent_activity_id", nullable = true)
	@MetaData (
		displayName = "Parent Activity", required = false, readOnly = false,
		description = "Parent activity for hierarchical task breakdown", hidden = false,
		order = 60, dataProviderBean = "CActivityService"
	)
	private CActivity parentActivity;

	// Additional Information
	@Column (name = "acceptance_criteria", nullable = true, length = 2000)
	@Size (max = 2000)
	@MetaData (
		displayName = "Acceptance Criteria", required = false, readOnly = false,
		defaultValue = "",
		description = "Criteria that must be met for the activity to be considered complete",
		hidden = false, order = 70, maxLength = 2000
	)
	private String acceptanceCriteria;

	@Column (name = "notes", nullable = true, length = 2000)
	@Size (max = 2000)
	@MetaData (
		displayName = "Notes", required = false, readOnly = false, defaultValue = "",
		description = "Additional notes and comments", hidden = false, order = 71,
		maxLength = 2000
	)
	private String notes;

	// Audit fields
	@Column (name = "created_date", nullable = true)
	@MetaData (
		displayName = "Created Date", required = false, readOnly = true,
		description = "Date and time when the activity was created", hidden = false,
		order = 80
	)
	private LocalDateTime createdDate;

	@Column (name = "last_modified_date", nullable = true)
	@MetaData (
		displayName = "Last Modified", required = false, readOnly = true,
		description = "Date and time when the activity was last modified", hidden = false,
		order = 81
	)
	private LocalDateTime lastModifiedDate;

	/**
	 * Default constructor for JPA.
	 */
	public CActivity() {
		super();
		logger.debug("CActivity() - Creating new activity instance");
		initializeDefaults();
	}

	/**
	 * Constructor with name and project.
	 * @param name    the name of the activity - must not be null
	 * @param project the project this activity belongs to - must not be null
	 */
	public CActivity(final String name, final CProject project) {
		super(name, project);
		logger.debug(
			"CActivity(name={}, project={}) - Creating activity with name and project",
			name, project != null ? project.getName() : "null");

		if (name == null) {
			logger.warn("CActivity constructor - Name parameter is null");
		}

		if (project == null) {
			logger.warn("CActivity constructor - Project parameter is null");
		}
		initializeDefaults();
	}

	/**
	 * Constructor with name, project, and assigned user.
	 * @param name       the name of the activity - must not be null
	 * @param project    the project this activity belongs to - must not be null
	 * @param assignedTo the user assigned to this activity - can be null
	 */
	public CActivity(final String name, final CProject project, final CUser assignedTo) {
		super(name, project);
		logger.debug(
			"CActivity(name={}, project={}, assignedTo={}) - Creating activity with assignment",
			name, project != null ? project.getName() : "null",
			assignedTo != null ? assignedTo.getUsername() : "null");

		if (name == null) {
			logger.warn("CActivity constructor - Name parameter is null");
		}

		if (project == null) {
			logger.warn("CActivity constructor - Project parameter is null");
		}
		this.assignedTo = assignedTo;
		initializeDefaults();
	}

	/**
	 * Calculate the cost variance (actual cost - estimated cost).
	 * @return the cost variance, positive if over budget, negative if under budget
	 */
	public BigDecimal calculateCostVariance() {
		logger.debug(
			"calculateCostVariance() - Calculating cost variance for activity id={}",
			getId());

		if ((actualCost == null) || (estimatedCost == null)) {
			logger.debug(
				"calculateCostVariance() - Missing cost data, actual={}, estimated={}",
				actualCost, estimatedCost);
			return BigDecimal.ZERO;
		}
		return actualCost.subtract(estimatedCost);
	}

	/**
	 * Calculate the time variance (actual hours - estimated hours).
	 * @return the time variance, positive if over estimated, negative if under estimated
	 */
	public BigDecimal calculateTimeVariance() {
		logger.debug(
			"calculateTimeVariance() - Calculating time variance for activity id={}",
			getId());

		if ((actualHours == null) || (estimatedHours == null)) {
			logger.debug(
				"calculateTimeVariance() - Missing time data, actual={}, estimated={}",
				actualHours, estimatedHours);
			return BigDecimal.ZERO;
		}
		final BigDecimal variance = actualHours.subtract(estimatedHours);
		logger.debug("calculateTimeVariance() - Time variance calculated: {}", variance);
		return variance;
	}

	public String getAcceptanceCriteria() { return acceptanceCriteria; }

	public CActivityType getActivityType() { return activityType; }

	public BigDecimal getActualCost() {
		return actualCost != null ? actualCost : BigDecimal.ZERO;
	}
	// Getters and Setters with proper logging and null checking

	public BigDecimal getActualHours() {
		return actualHours != null ? actualHours : BigDecimal.ZERO;
	}

	public CUser getAssignedTo() { return assignedTo; }

	public LocalDate getCompletionDate() { return completionDate; }

	public CUser getCreatedBy() { return createdBy; }

	public LocalDateTime getCreatedDate() { return createdDate; }

	public String getDescription() { return description; }

	public LocalDate getDueDate() { return dueDate; }

	public BigDecimal getEstimatedCost() { return estimatedCost; }

	public BigDecimal getEstimatedHours() { return estimatedHours; }

	public BigDecimal getHourlyRate() { return hourlyRate; }

	public LocalDateTime getLastModifiedDate() { return lastModifiedDate; }

	public String getNotes() { return notes; }

	public CActivity getParentActivity() { return parentActivity; }

	public CActivityPriority getPriority() { return priority; }

	public Integer getProgressPercentage() {
		return progressPercentage != null ? progressPercentage : 0;
	}

	public BigDecimal getRemainingHours() { return remainingHours; }

	public LocalDate getStartDate() { return startDate; }

	public CActivityStatus getStatus() { return status; }

	/**
	 * Initialize default values for the activity.
	 */
	private void initializeDefaults() {
		logger.debug("initializeDefaults() - Setting default values for activity");

		if (this.actualHours == null) {
			this.actualHours = BigDecimal.ZERO;
		}

		if (this.actualCost == null) {
			this.actualCost = BigDecimal.ZERO;
		}

		if (this.progressPercentage == null) {
			this.progressPercentage = 0;
		}

		if (this.createdDate == null) {
			this.createdDate = LocalDateTime.now();
		}
		this.lastModifiedDate = LocalDateTime.now();
	}

	/**
	 * Check if the activity is completed.
	 * @return true if the activity has a completion date or progress is 100%
	 */
	public boolean isCompleted() {
		final boolean hasCompletionDate = completionDate != null;
		final boolean isFullProgress =
			(progressPercentage != null) && (progressPercentage >= 100);
		final boolean isFinalStatus = (status != null) && status.isFinal();
		final boolean completed = hasCompletionDate || isFullProgress || isFinalStatus;
		logger.debug(
			"isCompleted() - Activity id={} completed={} (completionDate={}, progress={}, finalStatus={})",
			getId(), completed, hasCompletionDate, progressPercentage, isFinalStatus);
		return completed;
	}

	/**
	 * Check if the activity is overdue.
	 * @return true if the due date has passed and the activity is not completed
	 */
	public boolean isOverdue() {

		if ((dueDate == null) || isCompleted()) {
			return false;
		}
		final boolean overdue = LocalDate.now().isAfter(dueDate);
		logger.debug("isOverdue() - Activity id={} overdue={} (dueDate={}, today={})",
			getId(), overdue, dueDate, LocalDate.now());
		return overdue;
	}

	public void setAcceptanceCriteria(final String acceptanceCriteria) {
		logger.debug(
			"setAcceptanceCriteria(acceptanceCriteria={}) - Setting acceptance criteria for activity id={}",
			acceptanceCriteria, getId());
		this.acceptanceCriteria = acceptanceCriteria;
		updateLastModified();
	}

	public void setActivityType(final CActivityType activityType) {
		logger.debug(
			"setActivityType(activityType={}) - Setting activity type for activity id={}",
			activityType != null ? activityType.getName() : "null", getId());
		this.activityType = activityType;
		updateLastModified();
	}

	public void setActualCost(final BigDecimal actualCost) {
		logger.debug(
			"setActualCost(actualCost={}) - Setting actual cost for activity id={}",
			actualCost, getId());

		if ((actualCost != null) && (actualCost.compareTo(BigDecimal.ZERO) < 0)) {
			logger.warn(
				"setActualCost - Attempting to set negative actual cost: {} for activity id={}",
				actualCost, getId());
		}
		this.actualCost = actualCost != null ? actualCost : BigDecimal.ZERO;
		updateLastModified();
	}

	public void setActualHours(final BigDecimal actualHours) {
		logger.debug(
			"setActualHours(actualHours={}) - Setting actual hours for activity id={}",
			actualHours, getId());

		if ((actualHours != null) && (actualHours.compareTo(BigDecimal.ZERO) < 0)) {
			logger.warn(
				"setActualHours - Attempting to set negative actual hours: {} for activity id={}",
				actualHours, getId());
		}
		this.actualHours = actualHours != null ? actualHours : BigDecimal.ZERO;
		updateLastModified();
	}

	public void setAssignedTo(final CUser assignedTo) {
		logger.debug(
			"setAssignedTo(assignedTo={}) - Setting assigned user for activity id={}",
			assignedTo != null ? assignedTo.getUsername() : "null", getId());
		this.assignedTo = assignedTo;
		updateLastModified();
	}

	public void setCompletionDate(final LocalDate completionDate) {
		logger.debug(
			"setCompletionDate(completionDate={}) - Setting completion date for activity id={}",
			completionDate, getId());
		this.completionDate = completionDate;

		if ((completionDate != null) && (progressPercentage != null)
			&& (progressPercentage < 100)) {
			logger.debug(
				"setCompletionDate - Auto-setting progress to 100% for completed activity id={}",
				getId());
			this.progressPercentage = 100;
		}
		updateLastModified();
	}

	public void setCreatedBy(final CUser createdBy) {
		logger.debug(
			"setCreatedBy(createdBy={}) - Setting created by user for activity id={}",
			createdBy != null ? createdBy.getUsername() : "null", getId());
		this.createdBy = createdBy;
	}

	public void setCreatedDate(final LocalDateTime createdDate) {
		logger.debug(
			"setCreatedDate(createdDate={}) - Setting created date for activity id={}",
			createdDate, getId());
		this.createdDate = createdDate;
	}

	public void setDescription(final String description) {
		logger.debug(
			"setDescription(description={}) - Setting description for activity id={}",
			description, getId());
		this.description = description;
		updateLastModified();
	}

	public void setDueDate(final LocalDate dueDate) {
		logger.debug("setDueDate(dueDate={}) - Setting due date for activity id={}",
			dueDate, getId());
		this.dueDate = dueDate;
		updateLastModified();
	}

	public void setEstimatedCost(final BigDecimal estimatedCost) {
		logger.debug(
			"setEstimatedCost(estimatedCost={}) - Setting estimated cost for activity id={}",
			estimatedCost, getId());

		if ((estimatedCost != null) && (estimatedCost.compareTo(BigDecimal.ZERO) < 0)) {
			logger.warn(
				"setEstimatedCost - Attempting to set negative estimated cost: {} for activity id={}",
				estimatedCost, getId());
		}
		this.estimatedCost = estimatedCost;
		updateLastModified();
	}

	public void setEstimatedHours(final BigDecimal estimatedHours) {
		logger.debug(
			"setEstimatedHours(estimatedHours={}) - Setting estimated hours for activity id={}",
			estimatedHours, getId());

		if ((estimatedHours != null) && (estimatedHours.compareTo(BigDecimal.ZERO) < 0)) {
			logger.warn(
				"setEstimatedHours - Attempting to set negative estimated hours: {} for activity id={}",
				estimatedHours, getId());
		}
		this.estimatedHours = estimatedHours;
		updateLastModified();
	}

	public void setHourlyRate(final BigDecimal hourlyRate) {
		logger.debug(
			"setHourlyRate(hourlyRate={}) - Setting hourly rate for activity id={}",
			hourlyRate, getId());

		if ((hourlyRate != null) && (hourlyRate.compareTo(BigDecimal.ZERO) < 0)) {
			logger.warn(
				"setHourlyRate - Attempting to set negative hourly rate: {} for activity id={}",
				hourlyRate, getId());
		}
		this.hourlyRate = hourlyRate;
		updateLastModified();
	}

	public void setLastModifiedDate(final LocalDateTime lastModifiedDate) {
		logger.debug(
			"setLastModifiedDate(lastModifiedDate={}) - Setting last modified date for activity id={}",
			lastModifiedDate, getId());
		this.lastModifiedDate = lastModifiedDate;
	}

	public void setNotes(final String notes) {
		logger.debug("setNotes(notes={}) - Setting notes for activity id={}", notes,
			getId());
		this.notes = notes;
		updateLastModified();
	}

	public void setParentActivity(final CActivity parentActivity) {
		logger.debug(
			"setParentActivity(parentActivity={}) - Setting parent activity for activity id={}",
			parentActivity != null ? parentActivity.getName() : "null", getId());

		if ((parentActivity != null) && parentActivity.equals(this)) {
			logger.warn(
				"setParentActivity - Attempting to set self as parent for activity id={}",
				getId());
			return;
		}
		this.parentActivity = parentActivity;
		updateLastModified();
	}

	public void setPriority(final CActivityPriority priority) {
		logger.debug("setPriority(priority={}) - Setting priority for activity id={}",
			priority != null ? priority.getName() : "null", getId());
		this.priority = priority;
		updateLastModified();
	}

	public void setProgressPercentage(final Integer progressPercentage) {
		logger.debug(
			"setProgressPercentage(progressPercentage={}) - Setting progress percentage for activity id={}",
			progressPercentage, getId());

		if ((progressPercentage != null)
			&& ((progressPercentage < 0) || (progressPercentage > 100))) {
			logger.warn(
				"setProgressPercentage - Invalid progress percentage: {} for activity id={}",
				progressPercentage, getId());
			return;
		}
		this.progressPercentage = progressPercentage != null ? progressPercentage : 0;

		// Auto-set completion date if progress reaches 100%
		if ((progressPercentage != null) && (progressPercentage >= 100)
			&& (completionDate == null)) {
			logger.debug(
				"setProgressPercentage - Auto-setting completion date for 100% progress activity id={}",
				getId());
			this.completionDate = LocalDate.now();
		}
		updateLastModified();
	}

	public void setRemainingHours(final BigDecimal remainingHours) {
		logger.debug(
			"setRemainingHours(remainingHours={}) - Setting remaining hours for activity id={}",
			remainingHours, getId());

		if ((remainingHours != null) && (remainingHours.compareTo(BigDecimal.ZERO) < 0)) {
			logger.warn(
				"setRemainingHours - Attempting to set negative remaining hours: {} for activity id={}",
				remainingHours, getId());
		}
		this.remainingHours = remainingHours;
		updateLastModified();
	}

	public void setStartDate(final LocalDate startDate) {
		logger.debug("setStartDate(startDate={}) - Setting start date for activity id={}",
			startDate, getId());
		this.startDate = startDate;
		updateLastModified();
	}

	public void setStatus(final CActivityStatus status) {
		logger.debug("setStatus(status={}) - Setting status for activity id={}",
			status != null ? status.getName() : "null", getId());
		this.status = status;

		// Auto-set completion date if status is final
		if ((status != null) && status.isFinal() && (completionDate == null)) {
			logger.debug(
				"setStatus - Auto-setting completion date for final status activity id={}",
				getId());
			this.completionDate = LocalDate.now();

			if ((progressPercentage != null) && (progressPercentage < 100)) {
				this.progressPercentage = 100;
			}
		}
		updateLastModified();
	}

	/**
	 * Update the last modified date to now.
	 */
	public void updateLastModified() {
		logger.debug(
			"updateLastModified() - Updating last modified date for activity id={}",
			getId());
		this.lastModifiedDate = LocalDateTime.now();
	}
}
