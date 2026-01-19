package tech.derbent.app.validation.validationsession.domain;

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
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.app.validation.validationcase.domain.CValidationCase;

/** CValidationCaseResult - Result of executing a specific validation case within a validation session. */
@Entity
@Table (name = "cvalidationcaseresult")
@AttributeOverride (name = "id", column = @Column (name = "validationcaseresult_id"))
public class CValidationCaseResult extends CEntityDB<CValidationCaseResult> {

	public static final String DEFAULT_COLOR = "#90EE90"; // LightGreen - validation case results
	public static final String DEFAULT_ICON = "vaadin:check-circle";
	public static final String ENTITY_TITLE_PLURAL = "Validation Case Results";
	public static final String ENTITY_TITLE_SINGULAR = "Validation Case Result";
	public static final String VIEW_NAME = "Validation Case Results View";
	@Column (name = "duration_ms", nullable = true)
	@AMetaData (
			displayName = "Duration (ms)", required = false, readOnly = false, description = "Validation case execution duration in milliseconds",
			hidden = false
	)
	private Long durationMs;
	@Column (nullable = true, length = 5000)
	@Size (max = 5000)
	@AMetaData (
			displayName = "Error Details", required = false, readOnly = false, description = "Error details if validation case failed",
			hidden = false, maxLength = 5000
	)
	private String errorDetails;
	@Column (name = "execution_order", nullable = true)
	@AMetaData (
			displayName = "Execution Order", required = false, readOnly = false, description = "Order in which validation case was executed",
			hidden = false
	)
	private Integer executionOrder;
	@Column (nullable = true, length = 5000)
	@Size (max = 5000)
	@AMetaData (
			displayName = "Notes", required = false, readOnly = false, description = "Execution notes for this validation case", hidden = false,
			maxLength = 5000
	)
	private String notes;
	@Enumerated (EnumType.STRING)
	@Column (name = "result", nullable = true, length = 20)
	@AMetaData (displayName = "Result", required = false, readOnly = false, description = "Validation case result", hidden = false)
	private CValidationResult result = CValidationResult.NOT_EXECUTED;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "validationcase_id", nullable = false)
	@AMetaData (
			displayName = "Validation Case", required = true, readOnly = false, description = "Validation case being executed", hidden = false,
			dataProviderBean = "CValidationCaseService"
	)
	private CValidationCase validationCase;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "validationsession_id", nullable = false)
	@AMetaData (
			displayName = "Validation Session", required = true, readOnly = false, description = "Parent validation session", hidden = false,
			dataProviderBean = "CValidationSessionService"
	)
	private CValidationSession validationSession;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "validationCaseResult")
	@AMetaData (
			displayName = "Validation Step Results", required = false, readOnly = false, description = "Results for each step in validation case",
			hidden = false, dataProviderBean = "CValidationStepResultService", createComponentMethod = "createComponentListValidationStepResults"
	)
	private Set<CValidationStepResult> validationStepResults = new HashSet<>();

	/** Default constructor for JPA. */
	public CValidationCaseResult() {
		super();
		initializeDefaults();
	}

	public CValidationCaseResult(final CValidationSession validationSession, final CValidationCase validationCase) {
		super();
		this.validationSession = validationSession;
		this.validationCase = validationCase;
		initializeDefaults();
	}

	public Long getDurationMs() { return durationMs; }

	public String getErrorDetails() { return errorDetails; }

	public Integer getExecutionOrder() { return executionOrder; }

	public String getNotes() { return notes; }

	public CValidationResult getResult() { return result; }

	public CValidationCase getValidationCase() { return validationCase; }

	public CValidationSession getValidationSession() { return validationSession; }

	public Set<CValidationStepResult> getValidationStepResults() {
		if (validationStepResults == null) {
			validationStepResults = new HashSet<>();
		}
		return validationStepResults;
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (result == null) {
			result = CValidationResult.NOT_EXECUTED;
		}
	}

	public void setDurationMs(final Long durationMs) { this.durationMs = durationMs; }

	public void setErrorDetails(final String errorDetails) { this.errorDetails = errorDetails; }

	public void setExecutionOrder(final Integer executionOrder) { this.executionOrder = executionOrder; }

	public void setNotes(final String notes) { this.notes = notes; }

	public void setResult(final CValidationResult result) { this.result = result; }

	public void setValidationCase(final CValidationCase validationCase) { this.validationCase = validationCase; }

	public void setValidationSession(final CValidationSession validationSession) { this.validationSession = validationSession; }

	public void setValidationStepResults(final Set<CValidationStepResult> validationStepResults) {
		this.validationStepResults = validationStepResults;
	}
}
