package tech.derbent.app.testcases.testrun.domain;

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
import tech.derbent.app.testcases.teststep.domain.CTestStep;

/** CTestStepResult - Result of executing a specific test step within a test case.
 * Tracks whether each step passed or failed, with actual results and error details. */
@Entity
@Table (name = "cteststepresult")
@AttributeOverride (name = "id", column = @Column (name = "teststepresult_id"))
public class CTestStepResult extends CEntityDB<CTestStepResult> {

	public static final String DEFAULT_COLOR = "#98FB98"; // PaleGreen - test step results
	public static final String DEFAULT_ICON = "vaadin:dot-circle";
	public static final String ENTITY_TITLE_PLURAL = "Test Step Results";
	public static final String ENTITY_TITLE_SINGULAR = "Test Step Result";
	public static final String VIEW_NAME = "Test Step Results View";

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "testcaseresult_id", nullable = false)
	@AMetaData (
			displayName = "Test Case Result", required = true, readOnly = false,
			description = "Parent test case result", hidden = false,
			dataProviderBean = "CTestCaseResultService"
	)
	private CTestCaseResult testCaseResult;

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "teststep_id", nullable = false)
	@AMetaData (
			displayName = "Test Step", required = true, readOnly = false,
			description = "Test step being executed", hidden = false,
			dataProviderBean = "CTestStepService"
	)
	private CTestStep testStep;

	@Enumerated (EnumType.STRING)
	@Column (name = "result", nullable = true, length = 20)
	@AMetaData (
			displayName = "Result", required = false, readOnly = false,
			description = "Test step result (PASSED/FAILED)", hidden = false
	)
	private CTestResult result = CTestResult.NOT_EXECUTED;

	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Actual Result", required = false, readOnly = false,
			description = "Actual result observed during step execution", hidden = false, maxLength = 2000
	)
	private String actualResult;

	@Column (nullable = true, length = 5000)
	@Size (max = 5000)
	@AMetaData (
			displayName = "Error Details", required = false, readOnly = false,
			description = "Error details if step failed", hidden = false, maxLength = 5000
	)
	private String errorDetails;

	@Column (nullable = true, length = 1000)
	@Size (max = 1000)
	@AMetaData (
			displayName = "Screenshot Path", required = false, readOnly = false,
			description = "Path to screenshot taken during step execution", hidden = false, maxLength = 1000
	)
	private String screenshotPath;

	@Column (name = "duration_ms", nullable = true)
	@AMetaData (
			displayName = "Duration (ms)", required = false, readOnly = false,
			description = "Step execution duration in milliseconds", hidden = false
	)
	private Long durationMs;

	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Notes", required = false, readOnly = false,
			description = "Additional notes about step execution", hidden = false, maxLength = 2000
	)
	private String notes;

	/** Default constructor for JPA. */
	public CTestStepResult() {
		super();
		initializeDefaults();
	}

	public CTestStepResult(final CTestCaseResult testCaseResult, final CTestStep testStep) {
		super();
		this.testCaseResult = testCaseResult;
		this.testStep = testStep;
		initializeDefaults();
	}

	protected void initializeDefaults() {
		super.initializeDefaults();
		if (result == null) {
			result = CTestResult.NOT_EXECUTED;
		}
	}

	public CTestCaseResult getTestCaseResult() { return testCaseResult; }

	public void setTestCaseResult(final CTestCaseResult testCaseResult) {
		this.testCaseResult = testCaseResult;
	}

	public CTestStep getTestStep() { return testStep; }

	public void setTestStep(final CTestStep testStep) {
		this.testStep = testStep;
	}

	public CTestResult getResult() { return result; }

	public void setResult(final CTestResult result) {
		this.result = result;
	}

	public String getActualResult() { return actualResult; }

	public void setActualResult(final String actualResult) {
		this.actualResult = actualResult;
	}

	public String getErrorDetails() { return errorDetails; }

	public void setErrorDetails(final String errorDetails) {
		this.errorDetails = errorDetails;
	}

	public String getScreenshotPath() { return screenshotPath; }

	public void setScreenshotPath(final String screenshotPath) {
		this.screenshotPath = screenshotPath;
	}

	public Long getDurationMs() { return durationMs; }

	public void setDurationMs(final Long durationMs) {
		this.durationMs = durationMs;
	}

	public String getNotes() { return notes; }

	public void setNotes(final String notes) {
		this.notes = notes;
	}

	@Override
	public String toString() {
		if (testStep != null) {
			return String.format("Step %d: %s", testStep.getStepOrder(), result);
		}
		return "Test Step Result: " + result;
	}
}
