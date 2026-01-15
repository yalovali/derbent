package tech.derbent.app.testcases.testrun.domain;

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
import tech.derbent.api.entityDB.domain.CEntityDB;
import tech.derbent.app.testcases.testcase.domain.CTestCase;

/** CTestCaseResult - Result of executing a specific test case within a test run. */
@Entity
@Table (name = "ctestcaseresult")
@AttributeOverride (name = "id", column = @Column (name = "testcaseresult_id"))
public class CTestCaseResult extends CEntityDB<CTestCaseResult> {

	public static final String DEFAULT_COLOR = "#90EE90"; // LightGreen - test case results
	public static final String DEFAULT_ICON = "vaadin:check-circle";
	public static final String ENTITY_TITLE_PLURAL = "Test Case Results";
	public static final String ENTITY_TITLE_SINGULAR = "Test Case Result";
	public static final String VIEW_NAME = "Test Case Results View";

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "testrun_id", nullable = false)
	@AMetaData (
			displayName = "Test Run", required = true, readOnly = false,
			description = "Parent test run", hidden = false,
			dataProviderBean = "CTestRunService"
	)
	private CTestRun testRun;

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "testcase_id", nullable = false)
	@AMetaData (
			displayName = "Test Case", required = true, readOnly = false,
			description = "Test case being executed", hidden = false,
			dataProviderBean = "CTestCaseService"
	)
	private CTestCase testCase;

	@Enumerated (EnumType.STRING)
	@Column (name = "result", nullable = true, length = 20)
	@AMetaData (
			displayName = "Result", required = false, readOnly = false,
			description = "Test case result", hidden = false
	)
	private CTestResult result = CTestResult.NOT_EXECUTED;

	@Column (name = "execution_order", nullable = true)
	@AMetaData (
			displayName = "Execution Order", required = false, readOnly = false,
			description = "Order in which test case was executed", hidden = false
	)
	private Integer executionOrder;

	@Column (name = "duration_ms", nullable = true)
	@AMetaData (
			displayName = "Duration (ms)", required = false, readOnly = false,
			description = "Test case execution duration in milliseconds", hidden = false
	)
	private Long durationMs;

	@Column (nullable = true, length = 5000)
	@Size (max = 5000)
	@AMetaData (
			displayName = "Notes", required = false, readOnly = false,
			description = "Execution notes for this test case", hidden = false, maxLength = 5000
	)
	private String notes;

	@Column (nullable = true, length = 5000)
	@Size (max = 5000)
	@AMetaData (
			displayName = "Error Details", required = false, readOnly = false,
			description = "Error details if test case failed", hidden = false, maxLength = 5000
	)
	private String errorDetails;

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "testCaseResult")
	@AMetaData (
			displayName = "Test Step Results", required = false, readOnly = false,
			description = "Results for each step in test case", hidden = false,
			dataProviderBean = "CTestStepResultService", createComponentMethod = "createComponent"
	)
	private Set<CTestStepResult> testStepResults = new HashSet<>();

	/** Default constructor for JPA. */
	public CTestCaseResult() {
		super();
		initializeDefaults();
	}

	public CTestCaseResult(final CTestRun testRun, final CTestCase testCase) {
		super();
		this.testRun = testRun;
		this.testCase = testCase;
		initializeDefaults();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (result == null) {
			result = CTestResult.NOT_EXECUTED;
		}
	}

	public CTestRun getTestRun() { return testRun; }

	public void setTestRun(final CTestRun testRun) {
		this.testRun = testRun;
	}

	public CTestCase getTestCase() { return testCase; }

	public void setTestCase(final CTestCase testCase) {
		this.testCase = testCase;
	}

	public CTestResult getResult() { return result; }

	public void setResult(final CTestResult result) {
		this.result = result;
	}

	public Integer getExecutionOrder() { return executionOrder; }

	public void setExecutionOrder(final Integer executionOrder) {
		this.executionOrder = executionOrder;
	}

	public Long getDurationMs() { return durationMs; }

	public void setDurationMs(final Long durationMs) {
		this.durationMs = durationMs;
	}

	public String getNotes() { return notes; }

	public void setNotes(final String notes) {
		this.notes = notes;
	}

	public String getErrorDetails() { return errorDetails; }

	public void setErrorDetails(final String errorDetails) {
		this.errorDetails = errorDetails;
	}

	public Set<CTestStepResult> getTestStepResults() {
		if (testStepResults == null) {
			testStepResults = new HashSet<>();
		}
		return testStepResults;
	}

	public void setTestStepResults(final Set<CTestStepResult> testStepResults) {
		this.testStepResults = testStepResults;
	}
}
