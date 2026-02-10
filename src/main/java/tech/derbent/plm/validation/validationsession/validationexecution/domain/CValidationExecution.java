package tech.derbent.plm.validation.validationsession.validationexecution.domain;

import java.time.LocalDateTime;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.plm.validation.validationcase.domain.CValidationCase;

/** CValidationExecution - Entity tracking validation case execution and results. */
@Entity (name = "CValidationExecutionRecord")
@Table (name = "cvalidationexecution")
@AttributeOverride (name = "id", column = @Column (name = "validationexecution_id"))
public class CValidationExecution extends CEntityOfProject<CValidationExecution> {

	public static final String DEFAULT_COLOR = "#32CD32"; // LimeGreen - validation results
	public static final String DEFAULT_ICON = "vaadin:play-circle";
	public static final String ENTITY_TITLE_PLURAL = "Validation Executions";
	public static final String ENTITY_TITLE_SINGULAR = "Validation Execution";
	public static final String VIEW_NAME = "Validation Execution View";
	@Column (nullable = true, length = 5000)
	@Size (max = 5000)
	@AMetaData (
			displayName = "Actual Results", required = false, readOnly = false, description = "Actual results observed during execution",
			hidden = false, maxLength = 5000
	)
	private String actualResults;
	@Column (name = "build_number", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Build Number", required = false, readOnly = false, description = "Build/version number of software validated",
			hidden = false, maxLength = 100
	)
	private String buildNumber;
	@Column (name = "environment", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Environment", required = false, readOnly = false, description = "Validation environment (dev, staging, prod)",
			hidden = false, maxLength = 100
	)
	private String environment;
	@Column (nullable = true, length = 5000)
	@Size (max = 5000)
	@AMetaData (
			displayName = "Error Details", required = false, readOnly = false, description = "Error details if validation failed", hidden = false,
			maxLength = 5000
	)
	private String errorDetails;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "executed_by_id", nullable = true)
	@AMetaData (
			displayName = "Executed By", required = false, readOnly = false, description = "User who executed the validation", hidden = false,
			dataProviderBean = "CUserService"
	)
	private CUser executedBy;
	@Column (name = "execution_date", nullable = true)
	@AMetaData (
			displayName = "Execution Date", required = false, readOnly = false, description = "Date and time of validation execution", hidden = false
	)
	private LocalDateTime executionDate;
	@Column (name = "execution_duration_ms", nullable = true)
	@AMetaData (
			displayName = "Duration (ms)", required = false, readOnly = false, description = "Validation execution duration in milliseconds",
			hidden = false
	)
	private Long executionDurationMs;
	@Column (nullable = true, length = 5000)
	@Size (max = 5000)
	@AMetaData (
			displayName = "Notes", required = false, readOnly = false, description = "Execution notes and observations", hidden = false,
			maxLength = 5000
	)
	private String notes;
	@Enumerated (EnumType.STRING)
	@Column (name = "result", nullable = true, length = 20)
	@AMetaData (displayName = "Result", required = false, readOnly = false, description = "Validation execution result", hidden = false)
	private CValidationResult result = CValidationResult.NOT_EXECUTED;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "validationcase_id", nullable = false)
	@AMetaData (
			displayName = "Validation Case", required = true, readOnly = false, description = "Validation case being executed", hidden = false,
			dataProviderBean = "CValidationCaseService"
	)
	private CValidationCase validationCase;

	/** Default constructor for JPA. */
	protected CValidationExecution() {}

	public CValidationExecution(final String name, final CProject<?> project) {
		super(CValidationExecution.class, name, project);
		initializeDefaults();
	}

	public String getActualResults() { return actualResults; }

	public String getBuildNumber() { return buildNumber; }

	public String getEnvironment() { return environment; }

	public String getErrorDetails() { return errorDetails; }

	public CUser getExecutedBy() { return executedBy; }

	public LocalDateTime getExecutionDate() { return executionDate; }

	public Long getExecutionDurationMs() { return executionDurationMs; }

	public String getNotes() { return notes; }

	public CValidationResult getResult() { return result; }

	public CValidationCase getValidationCase() { return validationCase; }

	private final void initializeDefaults() {
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public void setActualResults(final String actualResults) {
		this.actualResults = actualResults;
		updateLastModified();
	}

	public void setBuildNumber(final String buildNumber) {
		this.buildNumber = buildNumber;
		updateLastModified();
	}

	public void setEnvironment(final String environment) {
		this.environment = environment;
		updateLastModified();
	}

	public void setErrorDetails(final String errorDetails) {
		this.errorDetails = errorDetails;
		updateLastModified();
	}

	public void setExecutedBy(final CUser executedBy) {
		this.executedBy = executedBy;
		updateLastModified();
	}

	public void setExecutionDate(final LocalDateTime executionDate) {
		this.executionDate = executionDate;
		updateLastModified();
	}

	public void setExecutionDurationMs(final Long executionDurationMs) {
		this.executionDurationMs = executionDurationMs;
		updateLastModified();
	}

	public void setNotes(final String notes) {
		this.notes = notes;
		updateLastModified();
	}

	public void setResult(final CValidationResult result) {
		this.result = result;
		updateLastModified();
	}

	public void setValidationCase(final CValidationCase validationCase) {
		this.validationCase = validationCase;
		updateLastModified();
	}
}
