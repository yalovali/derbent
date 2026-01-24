package tech.derbent.plm.validation.validationsession.domain;

import java.time.Duration;
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
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.validation.validationsuite.domain.CValidationSuite;

/** CValidationSession - Entity tracking validation suite execution and results. A validation session executes a validation suite and records results
 * for all validation cases and steps within it. This is also known as Validation Execution. */
@Entity
@Table (name = "cvalidationsession")
@AttributeOverride (name = "id", column = @Column (name = "validationsession_id"))
public class CValidationSession extends CEntityOfProject<CValidationSession> implements IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#32CD32"; // LimeGreen - validation sessions
	public static final String DEFAULT_ICON = "vaadin:play-circle";
	public static final String ENTITY_TITLE_PLURAL = "Validation Sessions";
	public static final String ENTITY_TITLE_SINGULAR = "Validation Session";
	public static final String VIEW_NAME = "Validation Sessions View";
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "validationsession_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Validation session attachments (screenshots, logs, etc.)",
			hidden = false, dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@Column (name = "build_number", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Build Number", required = false, readOnly = false, description = "Build/version number of software tested", hidden = false,
			maxLength = 100
	)
	private String buildNumber;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "validationsession_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments about validation session", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
	@Column (name = "duration_ms", nullable = true)
	@AMetaData (
			displayName = "Duration (ms)", required = false, readOnly = true, description = "Total validation session duration in milliseconds",
			hidden = false
	)
	private Long durationMs;
	@Column (name = "environment", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Environment", required = false, readOnly = false, description = "Validation environment (dev, staging, prod)",
			hidden = false, maxLength = 100
	)
	private String environment;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "executed_by_id", nullable = true)
	@AMetaData (
			displayName = "Executed By", required = false, readOnly = false, description = "User who executed the validation", hidden = false,
			dataProviderBean = "CUserService"
	)
	private CUser executedBy;
	@Column (name = "execution_end", nullable = true)
	@AMetaData (
			displayName = "Execution End", required = false, readOnly = false, description = "Date and time when validation session completed",
			hidden = false
	)
	private LocalDateTime executionEnd;
	@Column (nullable = true, length = 5000)
	@Size (max = 5000)
	@AMetaData (
			displayName = "Execution Notes", required = false, readOnly = false, description = "Notes and observations from validation session",
			hidden = false, maxLength = 5000
	)
	private String executionNotes;
	@Column (name = "execution_start", nullable = true)
	@AMetaData (
			displayName = "Execution Start", required = false, readOnly = false, description = "Date and time when validation session started",
			hidden = false
	)
	private LocalDateTime executionStart;
	@Column (name = "failed_validation_cases", nullable = true)
	@AMetaData (
			displayName = "Failed Validation Cases", required = false, readOnly = true, description = "Number of validation cases that failed",
			hidden = false
	)
	private Integer failedValidationCases = 0;
	@Column (name = "failed_validation_steps", nullable = true)
	@AMetaData (
			displayName = "Failed Validation Steps", required = false, readOnly = true, description = "Number of validation steps that failed",
			hidden = false
	)
	private Integer failedValidationSteps = 0;
	@Column (name = "passed_validation_cases", nullable = true)
	@AMetaData (
			displayName = "Passed Validation Cases", required = false, readOnly = true, description = "Number of validation cases that passed",
			hidden = false
	)
	private Integer passedValidationCases = 0;
	@Column (name = "passed_validation_steps", nullable = true)
	@AMetaData (
			displayName = "Passed Validation Steps", required = false, readOnly = true, description = "Number of validation steps that passed",
			hidden = false
	)
	private Integer passedValidationSteps = 0;
	@Enumerated (EnumType.STRING)
	@Column (name = "result", nullable = true, length = 20)
	@AMetaData (displayName = "Result", required = false, readOnly = false, description = "Overall validation session result", hidden = false)
	private CValidationResult result = CValidationResult.NOT_EXECUTED;
	@Column (name = "total_validation_cases", nullable = true)
	@AMetaData (
			displayName = "Total Validation Cases", required = false, readOnly = true, description = "Total number of validation cases in suite",
			hidden = false
	)
	private Integer totalValidationCases = 0;
	@Column (name = "total_validation_steps", nullable = true)
	@AMetaData (
			displayName = "Total Validation Steps", required = false, readOnly = true, description = "Total number of validation steps executed",
			hidden = false
	)
	private Integer totalValidationSteps = 0;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "validationSession")
	@AMetaData (
			displayName = "Validation Case Results", required = false, readOnly = false, description = "Results for each validation case in scenario",
			hidden = false, dataProviderBean = "CValidationCaseResultService", createComponentMethod = "createComponentListValidationCaseResults"
	)
	private Set<CValidationCaseResult> validationCaseResults = new HashSet<>();
	// Transient field for test execution component (not stored in database)
	@jakarta.persistence.Transient
	@AMetaData (
			displayName = "Validation Execution", required = false, readOnly = false, description = "Validation execution interface", hidden = false,
			dataProviderBean = "CPageServiceValidationSession", createComponentMethod = "createValidationExecutionComponent"
	)
	private transient Object validationExecutionComponent;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "validationsuite_id", nullable = false)
	@AMetaData (
			displayName = "Validation Suite", required = true, readOnly = false, description = "Validation suite being executed in this session",
			hidden = false, dataProviderBean = "CValidationSuiteService"
	)
	private CValidationSuite validationSuite;

	/** Default constructor for JPA. */
	protected CValidationSession() {
		super(CValidationSession.class, "New Validation Session", null);
	}

	public CValidationSession(final String name, final CProject<?> project) {
		super(CValidationSession.class, name, project);
		initializeDefaults();
	}

	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	public String getBuildNumber() { return buildNumber; }

	@Override
	public Set<CComment> getComments() { return comments; }

	public Long getDurationMs() { return durationMs; }

	public String getEnvironment() { return environment; }

	public CUser getExecutedBy() { return executedBy; }

	public LocalDateTime getExecutionEnd() { return executionEnd; }

	public String getExecutionNotes() { return executionNotes; }

	public LocalDateTime getExecutionStart() { return executionStart; }

	public Integer getFailedValidationCases() { return failedValidationCases; }

	public Integer getFailedValidationSteps() { return failedValidationSteps; }

	/** Calculate failure rate percentage.
	 * @return failure rate (0-100) */
	public Double getFailureRate() {
		if (totalValidationSteps == null || totalValidationSteps == 0) {
			return 0.0;
		}
		return failedValidationSteps.doubleValue() / totalValidationSteps.doubleValue() * 100.0;
	}

	public Integer getPassedValidationCases() { return passedValidationCases; }

	public Integer getPassedValidationSteps() { return passedValidationSteps; }

	/** Calculate pass rate percentage.
	 * @return pass rate (0-100) */
	public Double getPassRate() {
		if (totalValidationSteps == null || totalValidationSteps == 0) {
			return 0.0;
		}
		return passedValidationSteps.doubleValue() / totalValidationSteps.doubleValue() * 100.0;
	}

	public CValidationResult getResult() { return result; }

	public Integer getTotalValidationCases() { return totalValidationCases; }

	public Integer getTotalValidationSteps() { return totalValidationSteps; }

	public Set<CValidationCaseResult> getValidationCaseResults() { return validationCaseResults; }

	public CValidationSuite getValidationSuite() { return validationSuite; }

	private final void initializeDefaults() {
		executionNotes = "";
		executionStart = LocalDateTime.now();
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	public void setBuildNumber(final String buildNumber) {
		this.buildNumber = buildNumber;
		updateLastModified();
	}

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setDurationMs(final Long durationMs) {
		this.durationMs = durationMs;
		updateLastModified();
	}

	public void setEnvironment(final String environment) {
		this.environment = environment;
		updateLastModified();
	}

	public void setExecutedBy(final CUser executedBy) {
		this.executedBy = executedBy;
		updateLastModified();
	}

	public void setExecutionEnd(final LocalDateTime executionEnd) {
		this.executionEnd = executionEnd;
		updateLastModified();
		// Auto-calculate duration
		if (executionStart != null && executionEnd != null) {
			durationMs = Duration.between(executionStart, executionEnd).toMillis();
		}
	}

	public void setExecutionNotes(final String executionNotes) {
		this.executionNotes = executionNotes;
		updateLastModified();
	}

	public void setExecutionStart(final LocalDateTime executionStart) {
		this.executionStart = executionStart;
		updateLastModified();
	}

	public void setFailedValidationCases(final Integer failedValidationCases) {
		this.failedValidationCases = failedValidationCases;
		updateLastModified();
	}

	public void setFailedValidationSteps(final Integer failedValidationSteps) {
		this.failedValidationSteps = failedValidationSteps;
		updateLastModified();
	}

	public void setPassedValidationCases(final Integer passedValidationCases) {
		this.passedValidationCases = passedValidationCases;
		updateLastModified();
	}

	public void setPassedValidationSteps(final Integer passedValidationSteps) {
		this.passedValidationSteps = passedValidationSteps;
		updateLastModified();
	}

	public void setResult(final CValidationResult result) {
		this.result = result;
		updateLastModified();
	}

	public void setTotalValidationCases(final Integer totalValidationCases) {
		this.totalValidationCases = totalValidationCases;
		updateLastModified();
	}

	public void setTotalValidationSteps(final Integer totalValidationSteps) {
		this.totalValidationSteps = totalValidationSteps;
		updateLastModified();
	}

	public void setValidationCaseResults(final Set<CValidationCaseResult> validationCaseResults) {
		this.validationCaseResults = validationCaseResults;
		updateLastModified();
	}

	public void setValidationSuite(final CValidationSuite validationSuite) {
		this.validationSuite = validationSuite;
		updateLastModified();
	}
}
