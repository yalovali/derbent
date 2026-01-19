package tech.derbent.plm.validation.validationsession.domain;

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
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.plm.validation.validationstep.domain.CValidationStep;

/** CValidationStepResult - Result of executing a specific validation step within a validation case. Tracks whether each step passed or failed, with
 * actual results and error details. */
@Entity
@Table (name = "cvalidationstepresult")
@AttributeOverride (name = "id", column = @Column (name = "validationstepresult_id"))
public class CValidationStepResult extends CEntityDB<CValidationStepResult> {

	public static final String DEFAULT_COLOR = "#98FB98"; // PaleGreen - validation step results
	public static final String DEFAULT_ICON = "vaadin:dot-circle";
	public static final String ENTITY_TITLE_PLURAL = "Validation Step Results";
	public static final String ENTITY_TITLE_SINGULAR = "Validation Step Result";
	public static final String VIEW_NAME = "Validation Step Results View";
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Actual Result", required = false, readOnly = false, description = "Actual result observed during step execution",
			hidden = false, maxLength = 2000
	)
	private String actualResult;
	@Column (name = "duration_ms", nullable = true)
	@AMetaData (
			displayName = "Duration (ms)", required = false, readOnly = false, description = "Step execution duration in milliseconds", hidden = false
	)
	private Long durationMs;
	@Column (nullable = true, length = 5000)
	@Size (max = 5000)
	@AMetaData (
			displayName = "Error Details", required = false, readOnly = false, description = "Error details if step failed", hidden = false,
			maxLength = 5000
	)
	private String errorDetails;
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Notes", required = false, readOnly = false, description = "Additional notes about step execution", hidden = false,
			maxLength = 2000
	)
	private String notes;
	@Enumerated (EnumType.STRING)
	@Column (name = "result", nullable = true, length = 20)
	@AMetaData (displayName = "Result", required = false, readOnly = false, description = "Validation step result (PASSED/FAILED)", hidden = false)
	private CValidationResult result = CValidationResult.NOT_EXECUTED;
	@Column (nullable = true, length = 1000)
	@Size (max = 1000)
	@AMetaData (
			displayName = "Screenshot Path", required = false, readOnly = false, description = "Path to screenshot taken during step execution",
			hidden = false, maxLength = 1000
	)
	private String screenshotPath;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "validationcaseresult_id", nullable = false)
	@AMetaData (
			displayName = "Validation Case Result", required = true, readOnly = false, description = "Parent validation case result", hidden = false,
			dataProviderBean = "CValidationCaseResultService"
	)
	private CValidationCaseResult validationCaseResult;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "validationstep_id", nullable = false)
	@AMetaData (
			displayName = "Validation Step", required = true, readOnly = false, description = "Validation step being executed", hidden = false,
			dataProviderBean = "CValidationStepService"
	)
	private CValidationStep validationStep;

	/** Default constructor for JPA. */
	public CValidationStepResult() {
		super();
		initializeDefaults();
	}

	public CValidationStepResult(final CValidationCaseResult validationCaseResult, final CValidationStep validationStep) {
		super();
		this.validationCaseResult = validationCaseResult;
		this.validationStep = validationStep;
		initializeDefaults();
	}

	public String getActualResult() { return actualResult; }

	public Long getDurationMs() { return durationMs; }

	public String getErrorDetails() { return errorDetails; }

	public String getNotes() { return notes; }

	public CValidationResult getResult() { return result; }

	public String getScreenshotPath() { return screenshotPath; }

	public CValidationCaseResult getValidationCaseResult() { return validationCaseResult; }

	public CValidationStep getValidationStep() { return validationStep; }

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (result == null) {
			result = CValidationResult.NOT_EXECUTED;
		}
	}

	public void setActualResult(final String actualResult) { this.actualResult = actualResult; }

	public void setDurationMs(final Long durationMs) { this.durationMs = durationMs; }

	public void setErrorDetails(final String errorDetails) { this.errorDetails = errorDetails; }

	public void setNotes(final String notes) { this.notes = notes; }

	public void setResult(final CValidationResult result) { this.result = result; }

	public void setScreenshotPath(final String screenshotPath) { this.screenshotPath = screenshotPath; }

	public void setValidationCaseResult(final CValidationCaseResult validationCaseResult) { this.validationCaseResult = validationCaseResult; }

	public void setValidationStep(final CValidationStep validationStep) { this.validationStep = validationStep; }

	@Override
	public String toString() {
		if (validationStep != null) {
			return String.format("Step %d: %s", validationStep.getStepOrder(), result);
		}
		return "Validation Step Result: " + result;
	}
}
