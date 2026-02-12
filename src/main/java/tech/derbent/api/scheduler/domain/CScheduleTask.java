package tech.derbent.api.scheduler.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;

/** CScheduleTask - Scheduled task entity with cron-like scheduling.
 * <p>
 * Enterprise scheduler for periodic tasks including:
 * <ul>
 * <li>Email processing (action="PROCESS_EMAIL_QUEUE")</li>
 * <li>Data cleanup tasks</li>
 * <li>Report generation</li>
 * <li>Custom automated workflows</li>
 * </ul>
 * <p>
 * Uses Spring cron expression syntax for scheduling. Example: "0 *&#47;5 * * * *" = every 5 minutes */
@Entity
@Table (name = "cschedule_task")
@AttributeOverride(name = "id", column = @Column(name = "schedule_task_id"))
public class CScheduleTask extends CEntityOfCompany<CScheduleTask> implements Serializable {

	public static final String ACTION_PROCESS_EMAIL_QUEUE = "PROCESS_EMAIL_QUEUE";
	public static final String DEFAULT_COLOR = "#FF9800";
	public static final String DEFAULT_ICON = "vaadin:clock";
	public static final String ENTITY_TITLE_PLURAL = "Schedule Tasks";
	public static final String ENTITY_TITLE_SINGULAR = "Schedule Task";
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Schedule Tasks";
	@Column (name = "action", nullable = false, length = 100)
	@NotBlank (message = "Action is required")
	@Size (max = 100)
	@AMetaData (displayName = "Action", required = true, description = "Action to execute (e.g., 'PROCESS_EMAIL_QUEUE')", maxLength = 100)
	private String action;
	@Column (name = "action_parameters", length = 2000)
	@Size (max = 2000)
	@AMetaData (displayName = "Action Parameters", required = false, description = "Optional JSON parameters for action", maxLength = 2000)
	private String actionParameters;
	@Column (name = "cron_expression", nullable = false, length = 100)
	@NotBlank (message = "Cron expression is required")
	@Size (max = 100)
	@AMetaData (
			displayName = "Cron Expression", required = true, description = "Spring cron expression (e.g., '0 */5 * * * *' = every 5 minutes)",
			maxLength = 100
	)
	private String cronExpression;
	@Column (name = "enabled", nullable = false)
	@AMetaData (displayName = "Enabled", required = true)
	private Boolean enabled = true;
	@Column (name = "execution_count", nullable = false)
	@AMetaData (displayName = "Executions", readOnly = true)
	private Integer executionCount = 0;
	@Column (name = "failure_count", nullable = false)
	@AMetaData (displayName = "Failures", readOnly = true)
	private Integer failureCount = 0;
	@Column (name = "last_error", length = 2000)
	@Size (max = 2000)
	@AMetaData (displayName = "Last Error", readOnly = true, maxLength = 2000)
	private String lastError;
	@Column (name = "last_run")
	@AMetaData (displayName = "Last Run", readOnly = true)
	private LocalDateTime lastRun;
	@Column (name = "next_run")
	@AMetaData (displayName = "Next Run", readOnly = true)
	private LocalDateTime nextRun;
	@Column (name = "success_count", nullable = false)
	@AMetaData (displayName = "Successes", readOnly = true)
	private Integer successCount = 0;

	protected CScheduleTask() {
		super();
	}

	public CScheduleTask(final String name, final String cronExpression, final String action, final CCompany company) {
		super(CScheduleTask.class, name, company);
		this.cronExpression = cronExpression;
		this.action = action;
		initializeDefaults();
	}

	public String getAction() { return action; }

	public String getActionParameters() { return actionParameters; }

	public String getCronExpression() { return cronExpression; }

	public Boolean getEnabled() { return enabled; }

	public Integer getExecutionCount() { return executionCount; }

	public Integer getFailureCount() { return failureCount; }

	public String getLastError() { return lastError; }

	public LocalDateTime getLastRun() { return lastRun; }

	public LocalDateTime getNextRun() { return nextRun; }

	public Integer getSuccessCount() { return successCount; }

	private final void initializeDefaults() {
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public final boolean isDueForExecution() {
		if (!Boolean.TRUE.equals(enabled)) {
			return false;
		}
		if (nextRun == null) {
			return true;
		}
		return LocalDateTime.now().isAfter(nextRun) || LocalDateTime.now().isEqual(nextRun);
	}

	public final void recordExecution(final boolean success) {
		executionCount = (executionCount != null ? executionCount : 0) + 1;
		if (success) {
			successCount = (successCount != null ? successCount : 0) + 1;
			lastError = null;
		} else {
			failureCount = (failureCount != null ? failureCount : 0) + 1;
		}
		lastRun = LocalDateTime.now();
	}

	public void setAction(final String action) { this.action = action; }

	public void setActionParameters(final String actionParameters) { this.actionParameters = actionParameters; }

	public void setCronExpression(final String cronExpression) { this.cronExpression = cronExpression; }

	public void setEnabled(final Boolean enabled) { this.enabled = enabled; }

	public void setExecutionCount(final Integer executionCount) { this.executionCount = executionCount; }

	public void setFailureCount(final Integer failureCount) { this.failureCount = failureCount; }

	public void setLastError(final String lastError) { this.lastError = lastError; }

	public void setLastRun(final LocalDateTime lastRun) { this.lastRun = lastRun; }

	public void setNextRun(final LocalDateTime nextRun) { this.nextRun = nextRun; }

	public void setSuccessCount(final Integer successCount) { this.successCount = successCount; }
}
