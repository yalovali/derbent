package tech.derbent.app.testcases.testrun.domain;

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
import tech.derbent.app.testcases.testscenario.domain.CTestScenario;
import tech.derbent.base.users.domain.CUser;

/** CTestRun - Entity tracking test scenario execution and results.
 * A test run executes a test scenario and records results for all test cases and steps within it.
 * This is also known as Test Execution. */
@Entity
@Table (name = "ctestrun")
@AttributeOverride (name = "id", column = @Column (name = "testrun_id"))
public class CTestRun extends CEntityOfProject<CTestRun> implements IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#32CD32"; // LimeGreen - test sessions
	public static final String DEFAULT_ICON = "vaadin:play-circle";
	public static final String ENTITY_TITLE_PLURAL = "Test Sessions";
	public static final String ENTITY_TITLE_SINGULAR = "Test Session";
	public static final String VIEW_NAME = "Test Sessions View";

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "testscenario_id", nullable = false)
	@AMetaData (
			displayName = "Test Suite", required = true, readOnly = false,
			description = "Test suite being executed in this session", hidden = false,
			dataProviderBean = "CTestScenarioService"
	)
	private CTestScenario testScenario;

	@Enumerated (EnumType.STRING)
	@Column (name = "result", nullable = true, length = 20)
	@AMetaData (
			displayName = "Result", required = false, readOnly = false,
			description = "Overall test run result", hidden = false
	)
	private CTestResult result = CTestResult.NOT_EXECUTED;

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "executed_by_id", nullable = true)
	@AMetaData (
			displayName = "Executed By", required = false, readOnly = false,
			description = "User who executed the test", hidden = false,
			dataProviderBean = "CUserService"
	)
	private CUser executedBy;

	@Column (name = "execution_start", nullable = true)
	@AMetaData (
			displayName = "Execution Start", required = false, readOnly = false,
			description = "Date and time when test run started", hidden = false
	)
	private LocalDateTime executionStart;

	@Column (name = "execution_end", nullable = true)
	@AMetaData (
			displayName = "Execution End", required = false, readOnly = false,
			description = "Date and time when test run completed", hidden = false
	)
	private LocalDateTime executionEnd;

	@Column (name = "duration_ms", nullable = true)
	@AMetaData (
			displayName = "Duration (ms)", required = false, readOnly = true,
			description = "Total test run duration in milliseconds", hidden = false
	)
	private Long durationMs;

	@Column (name = "total_test_cases", nullable = true)
	@AMetaData (
			displayName = "Total Test Cases", required = false, readOnly = true,
			description = "Total number of test cases in scenario", hidden = false
	)
	private Integer totalTestCases = 0;

	@Column (name = "passed_test_cases", nullable = true)
	@AMetaData (
			displayName = "Passed Test Cases", required = false, readOnly = true,
			description = "Number of test cases that passed", hidden = false
	)
	private Integer passedTestCases = 0;

	@Column (name = "failed_test_cases", nullable = true)
	@AMetaData (
			displayName = "Failed Test Cases", required = false, readOnly = true,
			description = "Number of test cases that failed", hidden = false
	)
	private Integer failedTestCases = 0;

	@Column (name = "total_test_steps", nullable = true)
	@AMetaData (
			displayName = "Total Test Steps", required = false, readOnly = true,
			description = "Total number of test steps executed", hidden = false
	)
	private Integer totalTestSteps = 0;

	@Column (name = "passed_test_steps", nullable = true)
	@AMetaData (
			displayName = "Passed Test Steps", required = false, readOnly = true,
			description = "Number of test steps that passed", hidden = false
	)
	private Integer passedTestSteps = 0;

	@Column (name = "failed_test_steps", nullable = true)
	@AMetaData (
			displayName = "Failed Test Steps", required = false, readOnly = true,
			description = "Number of test steps that failed", hidden = false
	)
	private Integer failedTestSteps = 0;

	@Column (nullable = true, length = 5000)
	@Size (max = 5000)
	@AMetaData (
			displayName = "Execution Notes", required = false, readOnly = false,
			description = "Notes and observations from test run", hidden = false, maxLength = 5000
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
			description = "Test environment (dev, staging, prod)", hidden = false, maxLength = 100
	)
	private String environment;

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "testRun")
	@AMetaData (
			displayName = "Test Case Results", required = false, readOnly = false,
			description = "Results for each test case in scenario", hidden = false,
			dataProviderBean = "CTestCaseResultService", createComponentMethod = "createComponentListTestCaseResults"
	)
	private Set<CTestCaseResult> testCaseResults = new HashSet<>();

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "testrun_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false,
			description = "Test run attachments (screenshots, logs, etc.)", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "testrun_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false,
			description = "Comments about test run", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();

	// Transient field for test execution component (not stored in database)
	@jakarta.persistence.Transient
	@AMetaData (
			displayName = "Test Execution", required = false, readOnly = false,
			description = "Test execution interface", hidden = false,
			dataProviderBean = "CPageServiceTestRun", createComponentMethod = "createTestExecutionComponent"
	)
	private transient Object testExecutionComponent;

	/** Default constructor for JPA. */
	public CTestRun() {
		super(CTestRun.class, "New Test Run", null);
		initializeDefaults();
	}

	public CTestRun(final String name, final CProject project) {
		super(CTestRun.class, name, project);
		initializeDefaults();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (result == null) {
			result = CTestResult.NOT_EXECUTED;
		}
		if (totalTestCases == null) totalTestCases = 0;
		if (passedTestCases == null) passedTestCases = 0;
		if (failedTestCases == null) failedTestCases = 0;
		if (totalTestSteps == null) totalTestSteps = 0;
		if (passedTestSteps == null) passedTestSteps = 0;
		if (failedTestSteps == null) failedTestSteps = 0;
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

	public CTestScenario getTestScenario() { return testScenario; }

	public void setTestScenario(final CTestScenario testScenario) {
		this.testScenario = testScenario;
		updateLastModified();
	}

	public CTestResult getResult() { return result; }

	public void setResult(final CTestResult result) {
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
			durationMs = java.time.Duration.between(executionStart, executionEnd).toMillis();
		}
	}

	public Long getDurationMs() { return durationMs; }

	public void setDurationMs(final Long durationMs) {
		this.durationMs = durationMs;
		updateLastModified();
	}

	public Integer getTotalTestCases() { return totalTestCases; }

	public void setTotalTestCases(final Integer totalTestCases) {
		this.totalTestCases = totalTestCases;
		updateLastModified();
	}

	public Integer getPassedTestCases() { return passedTestCases; }

	public void setPassedTestCases(final Integer passedTestCases) {
		this.passedTestCases = passedTestCases;
		updateLastModified();
	}

	public Integer getFailedTestCases() { return failedTestCases; }

	public void setFailedTestCases(final Integer failedTestCases) {
		this.failedTestCases = failedTestCases;
		updateLastModified();
	}

	public Integer getTotalTestSteps() { return totalTestSteps; }

	public void setTotalTestSteps(final Integer totalTestSteps) {
		this.totalTestSteps = totalTestSteps;
		updateLastModified();
	}

	public Integer getPassedTestSteps() { return passedTestSteps; }

	public void setPassedTestSteps(final Integer passedTestSteps) {
		this.passedTestSteps = passedTestSteps;
		updateLastModified();
	}

	public Integer getFailedTestSteps() { return failedTestSteps; }

	public void setFailedTestSteps(final Integer failedTestSteps) {
		this.failedTestSteps = failedTestSteps;
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

	public Set<CTestCaseResult> getTestCaseResults() {
		if (testCaseResults == null) {
			testCaseResults = new HashSet<>();
		}
		return testCaseResults;
	}

	public void setTestCaseResults(final Set<CTestCaseResult> testCaseResults) {
		this.testCaseResults = testCaseResults;
		updateLastModified();
	}

	/** Calculate pass rate percentage.
	 * @return pass rate (0-100) */
	public Double getPassRate() {
		if (totalTestSteps == null || totalTestSteps == 0) {
			return 0.0;
		}
		return (passedTestSteps.doubleValue() / totalTestSteps.doubleValue()) * 100.0;
	}

	/** Calculate failure rate percentage.
	 * @return failure rate (0-100) */
	public Double getFailureRate() {
		if (totalTestSteps == null || totalTestSteps == 0) {
			return 0.0;
		}
		return (failedTestSteps.doubleValue() / totalTestSteps.doubleValue()) * 100.0;
	}
}
