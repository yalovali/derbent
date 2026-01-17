package tech.derbent.app.testcases.testrun.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.app.testcases.testcase.domain.CTestCase;
import tech.derbent.app.testcases.teststep.domain.CTestStep;
import tech.derbent.app.testcases.testrun.domain.CTestCaseResult;
import tech.derbent.app.testcases.testrun.domain.CTestResult;
import tech.derbent.app.testcases.testrun.domain.CTestRun;
import tech.derbent.app.testcases.testrun.domain.CTestStepResult;
import tech.derbent.app.testcases.testscenario.domain.CTestScenario;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize("isAuthenticated()")
@PermitAll
public class CTestRunService extends CEntityOfProjectService<CTestRun> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CTestRunService.class);

	CTestRunService(final ITestRunRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CTestRun testRun) {
		return super.checkDeleteAllowed(testRun);
	}

	@Override
	public Class<CTestRun> getEntityClass() {
		return CTestRun.class;
	}

	@Override
	public Class<?> getInitializerServiceClass() {
		return CTestRunInitializerService.class;
	}

	@Override
	public Class<?> getPageServiceClass() {
		return CPageServiceTestRun.class;
	}

	@Override
	public Class<?> getServiceClass() {
		return this.getClass();
	}

	@Override
	public void initializeNewEntity(final CTestRun entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new test run entity");
		entity.setExecutionStart(LocalDateTime.now(clock));
		LOGGER.debug("Test run initialization complete with current execution start time");
	}

	/** Execute a test run by initializing test case results and test step results for all test cases in the scenario.
	 * @param testRun the test run to execute
	 * @return the test run with initialized results */
	public CTestRun executeTestRun(final CTestRun testRun) {
		Check.notNull(testRun, "Test run cannot be null");
		final CTestScenario scenario = testRun.getTestScenario();
		Check.notNull(scenario, "Test run must have a test scenario");
		LOGGER.debug("Executing test run {} for scenario {}", testRun.getId(), scenario.getId());
		final Set<CTestCase> testCasesSet = scenario.getTestCases();
		if (testCasesSet == null || testCasesSet.isEmpty()) {
			LOGGER.warn("Test scenario {} has no test cases", scenario.getId());
			return testRun;
		}
		final List<CTestCase> testCases = new java.util.ArrayList<>(testCasesSet);
		int executionOrder = 1;
		for (final CTestCase testCase : testCases) {
			final CTestCaseResult caseResult = new CTestCaseResult();
			caseResult.setTestRun(testRun);
			caseResult.setTestCase(testCase);
			caseResult.setExecutionOrder(executionOrder++);
			caseResult.setResult(CTestResult.NOT_EXECUTED);
			testRun.getTestCaseResults().add(caseResult);
			LOGGER.debug("Initialized test case result for test case {} with order {}", testCase.getId(), caseResult.getExecutionOrder());
			final Set<CTestStep> testStepsSet = testCase.getTestSteps();
			if (testStepsSet != null && !testStepsSet.isEmpty()) {
				final List<CTestStep> testSteps = new java.util.ArrayList<>(testStepsSet);
				testSteps.sort(java.util.Comparator.comparing(CTestStep::getStepOrder));
				for (final CTestStep testStep : testSteps) {
					final CTestStepResult stepResult = new CTestStepResult();
					stepResult.setTestCaseResult(caseResult);
					stepResult.setTestStep(testStep);
					stepResult.setResult(CTestResult.NOT_EXECUTED);
					stepResult.setActualResult("");
					caseResult.getTestStepResults().add(stepResult);
					LOGGER.debug("Initialized test step result for step {} with order {}", testStep.getId(), testStep.getStepOrder());
				}
			}
		}
		LOGGER.debug("Test run execution complete with {} test case results", testRun.getTestCaseResults().size());
		return save(testRun);
	}

	/** Find test runs by scenario.
	 * @param scenario the test scenario
	 * @return list of test runs for the scenario */
	public List<CTestRun> listByScenario(final CTestScenario scenario) {
		Check.notNull(scenario, "Test scenario cannot be null");
		return ((ITestRunRepository) repository).listByScenario(scenario);
	}

	/** Complete a test run by calculating statistics and setting overall result.
	 * @param testRun the test run to complete
	 * @return the completed test run with calculated statistics */
	public CTestRun completeTestRun(final CTestRun testRun) {
		Check.notNull(testRun, "Test run cannot be null");
		LOGGER.debug("Completing test run {}", testRun.getId());
		testRun.setExecutionEnd(LocalDateTime.now(clock));
		if (testRun.getExecutionStart() != null && testRun.getExecutionEnd() != null) {
			final long durationMs = java.time.Duration.between(testRun.getExecutionStart(), testRun.getExecutionEnd()).toMillis();
			testRun.setDurationMs(durationMs);
			LOGGER.debug("Test run duration calculated: {} ms", durationMs);
		}
		int totalCases = 0;
		int passedCases = 0;
		int failedCases = 0;
		int totalSteps = 0;
		int passedSteps = 0;
		int failedSteps = 0;
		for (final CTestCaseResult caseResult : testRun.getTestCaseResults()) {
			totalCases++;
			if (caseResult.getResult() == CTestResult.PASSED) {
				passedCases++;
			} else if (caseResult.getResult() == CTestResult.FAILED) {
				failedCases++;
			}
			for (final CTestStepResult stepResult : caseResult.getTestStepResults()) {
				totalSteps++;
				if (stepResult.getResult() == CTestResult.PASSED) {
					passedSteps++;
				} else if (stepResult.getResult() == CTestResult.FAILED) {
					failedSteps++;
				}
			}
		}
		testRun.setTotalTestCases(totalCases);
		testRun.setPassedTestCases(passedCases);
		testRun.setFailedTestCases(failedCases);
		testRun.setTotalTestSteps(totalSteps);
		testRun.setPassedTestSteps(passedSteps);
		testRun.setFailedTestSteps(failedSteps);
		if (failedCases > 0) {
			testRun.setResult(passedCases > 0 ? CTestResult.PARTIAL : CTestResult.FAILED);
		} else if (passedCases == totalCases && totalCases > 0) {
			testRun.setResult(CTestResult.PASSED);
		} else {
			testRun.setResult(CTestResult.NOT_EXECUTED);
		}
		LOGGER.debug("Test run statistics - Total: {}, Passed: {}, Failed: {}, Steps: {}/{}/{}",
				totalCases, passedCases, failedCases, totalSteps, passedSteps, failedSteps);
		LOGGER.debug("Test run overall result: {}", testRun.getResult());
		return save(testRun);
	}
}
