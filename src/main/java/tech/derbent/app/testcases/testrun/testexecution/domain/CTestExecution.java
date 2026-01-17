package tech.derbent.app.testcases.testrun.testexecution.domain;

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
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.testcases.testcase.domain.CTestCase;
import tech.derbent.base.users.domain.CUser;

/** CTestExecution - Entity tracking test case execution and results. */
@Entity (name = "CTestExecutionRecord")
@Table (name = "ctestexecution")
@AttributeOverride (name = "id", column = @Column (name = "testexecution_id"))
public class CTestExecution extends CEntityOfProject<CTestExecution> {

	public static final String DEFAULT_COLOR = "#32CD32"; // LimeGreen - test results
	public static final String DEFAULT_ICON = "vaadin:play-circle";
	public static final String ENTITY_TITLE_PLURAL = "Test Executions";
	public static final String ENTITY_TITLE_SINGULAR = "Test Execution";
	public static final String VIEW_NAME = "Test Execution View";
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "testcase_id", nullable = false)
	@AMetaData (
			displayName = "Test Case", required = true, readOnly = false, description = "Test case being executed", hidden = false,
			dataProviderBean = "CTestCaseService"
	)
	private CTestCase testCase;
	@Enumerated (EnumType.STRING)
	@Column (name = "result", nullable = true, length = 20)
	@AMetaData (displayName = "Result", required = false, readOnly = false, description = "Test execution result", hidden = false)
	private CTestResult result = CTestResult.NOT_EXECUTED;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "executed_by_id", nullable = true)
	@AMetaData (
			displayName = "Executed By", required = false, readOnly = false, description = "User who executed the test", hidden = false,
			dataProviderBean = "CUserService"
	)
	private CUser executedBy;
	@Column (name = "execution_date", nullable = true)
	@AMetaData (displayName = "Execution Date", required = false, readOnly = false, description = "Date and time of test execution", hidden = false)
	private LocalDateTime executionDate;
	@Column (name = "execution_duration_ms", nullable = true)
	@AMetaData (
			displayName = "Duration (ms)", required = false, readOnly = false, description = "Test execution duration in milliseconds", hidden = false
	)
	private Long executionDurationMs;
	@Column (nullable = true, length = 5000)
	@Size (max = 5000)
	@AMetaData (
			displayName = "Notes", required = false, readOnly = false, description = "Execution notes and observations", hidden = false,
			maxLength = 5000
	)
	private String notes;
	@Column (nullable = true, length = 5000)
	@Size (max = 5000)
	@AMetaData (
			displayName = "Actual Results", required = false, readOnly = false, description = "Actual results observed during execution",
			hidden = false, maxLength = 5000
	)
	private String actualResults;
	@Column (nullable = true, length = 5000)
	@Size (max = 5000)
	@AMetaData (
			displayName = "Error Details", required = false, readOnly = false, description = "Error details if test failed", hidden = false,
			maxLength = 5000
	)
	private String errorDetails;
	@Column (name = "build_number", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Build Number", required = false, readOnly = false, description = "Build/version number of software tested", hidden = false,
			maxLength = 100
	)
	private String buildNumber;
	@Column (name = "environment", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Environment", required = false, readOnly = false, description = "Test environment (dev, staging, prod)", hidden = false,
			maxLength = 100
	)
	private String environment;

	/** Default constructor for JPA. */
	public CTestExecution() {
		super(CTestExecution.class, "New Test Execution", null);
		initializeDefaults();
	}

	public CTestExecution(final String name, final CProject project) {
		super(CTestExecution.class, name, project);
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

	public CTestResult getResult() { return result; }

	public CTestCase getTestCase() { return testCase; }

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (result == null) {
			result = CTestResult.NOT_EXECUTED;
		}
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

	public void setResult(final CTestResult result) {
		this.result = result;
		updateLastModified();
	}

	public void setTestCase(final CTestCase testCase) {
		this.testCase = testCase;
		updateLastModified();
	}

	@Override
	public CTestExecution createClone(final CCloneOptions options) throws Exception {
		final CTestExecution clone = super.createClone(options);

		clone.testCase = this.testCase;
		clone.result = this.result;
		clone.notes = this.notes;
		clone.actualResults = this.actualResults;
		clone.errorDetails = this.errorDetails;
		clone.buildNumber = this.buildNumber;
		clone.environment = this.environment;
		clone.executionDurationMs = this.executionDurationMs;

		if (!options.isResetDates()) {
			clone.executionDate = this.executionDate;
		}

		if (!options.isResetAssignments()) {
			clone.executedBy = this.executedBy;
		}

		return clone;
	}
}
