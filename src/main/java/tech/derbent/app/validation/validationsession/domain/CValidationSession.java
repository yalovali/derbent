package tech.derbent.app.validation.validationsession.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.domain.IHasAttachments;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.app.comments.domain.IHasComments;
import tech.derbent.app.validation.validationsuite.domain.CValidationSuite;
import tech.derbent.base.users.domain.CUser;
import java.time.Duration;


/** CValidationSession - Entity tracking validation suite execution and results.
 * A validation session executes a validation suite and records results for all validation cases and steps within it.
 * This is also known as Validation Execution. */
@Entity
@Table (name = "cvalidationsession")
@AttributeOverride (name = "id", column = @Column (name = "validationsession_id"))
public class CValidationSession extends CEntityOfProject<CValidationSession> implements IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#32CD32"; // LimeGreen - validation sessions
	public static final String DEFAULT_ICON = "vaadin:play-circle";
	public static final String ENTITY_TITLE_PLURAL = "Validation Sessions";
	public static final String ENTITY_TITLE_SINGULAR = "Validation Session";
	public static final String VIEW_NAME = "Validation Sessions View";

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "validationsuite_id", nullable = false)
	@AMetaData (
			displayName = "Validation Suite", required = true, readOnly = false,
			description = "Validation suite being executed in this session", hidden = false,
			dataProviderBean = "CValidationSuiteService"
	)
	private CValidationSuite validationSuite;

	@Enumerated (EnumType.STRING)
	@Column (name = "result", nullable = true, length = 20)
	@AMetaData (
			displayName = "Result", required = false, readOnly = false,
			description = "Overall validation session result", hidden = false
	)
	private CValidationResult result = CValidationResult.NOT_EXECUTED;

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "executed_by_id", nullable = true)
	@AMetaData (
			displayName = "Executed By", required = false, readOnly = false,
			description = "User who executed the validation", hidden = false,
			dataProviderBean = "CUserService"
	)
	private CUser executedBy;

	@Column (name = "execution_start", nullable = true)
	@AMetaData (
			displayName = "Execution Start", required = false, readOnly = false,
			description = "Date and time when validation session started", hidden = false
	)
	private LocalDateTime executionStart;

	@Column (name = "execution_end", nullable = true)
	@AMetaData (
			displayName = "Execution End", required = false, readOnly = false,
			description = "Date and time when validation session completed", hidden = false
	)
	private LocalDateTime executionEnd;

	@Column (name = "duration_ms", nullable = true)
	@AMetaData (
			displayName = "Duration (ms)", required = false, readOnly = true,
			description = "Total validation session duration in milliseconds", hidden = false
	)
	private Long durationMs;

	@Column (name = "total_validation_cases", nullable = true)
	@AMetaData (
			displayName = "Total Validation Cases", required = false, readOnly = true,
			description = "Total number of validation cases in suite", hidden = false
	)
	private Integer totalValidationCases = 0;

	@Column (name = "passed_validation_cases", nullable = true)
	@AMetaData (
			displayName = "Passed Validation Cases", required = false, readOnly = true,
			description = "Number of validation cases that passed", hidden = false
	)
	private Integer passedValidationCases = 0;

	@Column (name = "failed_validation_cases", nullable = true)
	@AMetaData (
			displayName = "Failed Validation Cases", required = false, readOnly = true,
			description = "Number of validation cases that failed", hidden = false
	)
	private Integer failedValidationCases = 0;

	@Column (name = "total_validation_steps", nullable = true)
	@AMetaData (
			displayName = "Total Validation Steps", required = false, readOnly = true,
			description = "Total number of validation steps executed", hidden = false
	)
	private Integer totalValidationSteps = 0;

	@Column (name = "passed_validation_steps", nullable = true)
	@AMetaData (
			displayName = "Passed Validation Steps", required = false, readOnly = true,
			description = "Number of validation steps that passed", hidden = false
	)
	private Integer passedValidationSteps = 0;

	@Column (name = "failed_validation_steps", nullable = true)
	@AMetaData (
			displayName = "Failed Validation Steps", required = false, readOnly = true,
			description = "Number of validation steps that failed", hidden = false
	)
	private Integer failedValidationSteps = 0;

	@Column (nullable = true, length = 5000)
	@Size (max = 5000)
	@AMetaData (
			displayName = "Execution Notes", required = false, readOnly = false,
			description = "Notes and observations from validation session", hidden = false, maxLength = 5000
	)
	private String executionNotes;

	@Column (name = "build_number", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Build Number", required = false, readOnly = false,
			description = "Build/version number of software tested", hidden = false, maxLength = 100
	)
	private String buildNumber;

	@Column (name = "environment", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Environment", required = false, readOnly = false,
			description = "Validation environment (dev, staging, prod)", hidden = false, maxLength = 100
	)
	private String environment;

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "validationSession")
	@AMetaData (
			displayName = "Validation Case Results", required = false, readOnly = false,
			description = "Results for each validation case in scenario", hidden = false,
			dataProviderBean = "CValidationCaseResultService", createComponentMethod = "createComponentListValidationCaseResults"
	)
	private Set<CValidationCaseResult> validationCaseResults = new HashSet<>();

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "validationsession_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false,
			description = "Validation session attachments (screenshots, logs, etc.)", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "validationsession_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false,
			description = "Comments about validation session", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();

	// Transient field for test execution component (not stored in database)
	@jakarta.persistence.Transient
	@AMetaData (
			displayName = "Validation Execution", required = false, readOnly = false,
			description = "Validation execution interface", hidden = false,
			dataProviderBean = "CPageServiceValidationSession", createComponentMethod = "createValidationExecutionComponent"
	)
	private transient Object validationExecutionComponent;

	/** Default constructor for JPA. */
	public CValidationSession() {
		super(CValidationSession.class, "New Validation Session", null);
		initializeDefaults();
	}

	public CValidationSession(final String name, final CProject project) {
		super(CValidationSession.class, name, project);
		initializeDefaults();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (result == null) {
			result = CValidationResult.NOT_EXECUTED;
		}
		if (totalValidationCases == null) totalValidationCases = 0;
		if (passedValidationCases == null) passedValidationCases = 0;
		if (failedValidationCases == null) failedValidationCases = 0;
		if (totalValidationSteps == null) totalValidationSteps = 0;
		if (passedValidationSteps == null) passedValidationSteps = 0;
		if (failedValidationSteps == null) failedValidationSteps = 0;
	}

	@Override
	public Set<CAttachment> getAttachments() {
		if (attachments == null) {
			attachments = new HashSet<>();
		}
		return attachments;
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) {
		this.attachments = attachments;
	}

	@Override
	public Set<CComment> getComments() {
		if (comments == null) {
			comments = new HashSet<>();
		}
		return comments;
	}

	@Override
	public void setComments(final Set<CComment> comments) {
		this.comments = comments;
	}

	public CValidationSuite getValidationSuite() { return validationSuite; }

	public void setValidationSuite(final CValidationSuite validationSuite) {
		this.validationSuite = validationSuite;
		updateLastModified();
	}

	public CValidationResult getResult() { return result; }

	public void setResult(final CValidationResult result) {
		this.result = result;
		updateLastModified();
	}

	public CUser getExecutedBy() { return executedBy; }

	public void setExecutedBy(final CUser executedBy) {
		this.executedBy = executedBy;
		updateLastModified();
	}

	public LocalDateTime getExecutionStart() { return executionStart; }

	public void setExecutionStart(final LocalDateTime executionStart) {
		this.executionStart = executionStart;
		updateLastModified();
	}

	public LocalDateTime getExecutionEnd() { return executionEnd; }

	public void setExecutionEnd(final LocalDateTime executionEnd) {
		this.executionEnd = executionEnd;
		updateLastModified();
		// Auto-calculate duration
		if (executionStart != null && executionEnd != null) {
			durationMs = Duration.between(executionStart, executionEnd).toMillis();
		}
	}

	public Long getDurationMs() { return durationMs; }

	public void setDurationMs(final Long durationMs) {
		this.durationMs = durationMs;
		updateLastModified();
	}

	public Integer getTotalValidationCases() { return totalValidationCases; }

	public void setTotalValidationCases(final Integer totalValidationCases) {
		this.totalValidationCases = totalValidationCases;
		updateLastModified();
	}

	public Integer getPassedValidationCases() { return passedValidationCases; }

	public void setPassedValidationCases(final Integer passedValidationCases) {
		this.passedValidationCases = passedValidationCases;
		updateLastModified();
	}

	public Integer getFailedValidationCases() { return failedValidationCases; }

	public void setFailedValidationCases(final Integer failedValidationCases) {
		this.failedValidationCases = failedValidationCases;
		updateLastModified();
	}

	public Integer getTotalValidationSteps() { return totalValidationSteps; }

	public void setTotalValidationSteps(final Integer totalValidationSteps) {
		this.totalValidationSteps = totalValidationSteps;
		updateLastModified();
	}

	public Integer getPassedValidationSteps() { return passedValidationSteps; }

	public void setPassedValidationSteps(final Integer passedValidationSteps) {
		this.passedValidationSteps = passedValidationSteps;
		updateLastModified();
	}

	public Integer getFailedValidationSteps() { return failedValidationSteps; }

	public void setFailedValidationSteps(final Integer failedValidationSteps) {
		this.failedValidationSteps = failedValidationSteps;
		updateLastModified();
	}

	public String getExecutionNotes() { return executionNotes; }

	public void setExecutionNotes(final String executionNotes) {
		this.executionNotes = executionNotes;
		updateLastModified();
	}

	public String getBuildNumber() { return buildNumber; }

	public void setBuildNumber(final String buildNumber) {
		this.buildNumber = buildNumber;
		updateLastModified();
	}

	public String getEnvironment() { return environment; }

	public void setEnvironment(final String environment) {
		this.environment = environment;
		updateLastModified();
	}

	public Set<CValidationCaseResult> getValidationCaseResults() {
		if (validationCaseResults == null) {
			validationCaseResults = new HashSet<>();
		}
		return validationCaseResults;
	}

	public void setValidationCaseResults(final Set<CValidationCaseResult> validationCaseResults) {
		this.validationCaseResults = validationCaseResults;
		updateLastModified();
	}

	/** Calculate pass rate percentage.
	 * @return pass rate (0-100) */
	public Double getPassRate() {
		if (totalValidationSteps == null || totalValidationSteps == 0) {
			return 0.0;
		}
		return (passedValidationSteps.doubleValue() / totalValidationSteps.doubleValue()) * 100.0;
	}

	/** Calculate failure rate percentage.
	 * @return failure rate (0-100) */
	public Double getFailureRate() {
		if (totalValidationSteps == null || totalValidationSteps == 0) {
			return 0.0;
		}
		return (failedValidationSteps.doubleValue() / totalValidationSteps.doubleValue()) * 100.0;
	}
}
