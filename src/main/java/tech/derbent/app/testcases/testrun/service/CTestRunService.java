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
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.app.testcases.testcase.domain.CTestCase;
import tech.derbent.app.testcases.testrun.domain.CTestCaseResult;
import tech.derbent.app.testcases.testrun.domain.CTestResult;
import tech.derbent.app.testcases.testrun.domain.CTestRun;
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
		return null;
	}

	@Override
	public Class<?> getPageServiceClass() {
		return null;
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

	/** Execute a test run by initializing test case results for all test cases in the scenario.
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
			final CTestCaseResult result = new CTestCaseResult();
			result.setTestRun(testRun);
			result.setTestCase(testCase);
			result.setExecutionOrder(executionOrder++);
			result.setResult(CTestResult.NOT_EXECUTED);
			testRun.getTestCaseResults().add(result);
			LOGGER.debug("Initialized test case result for test case {} with order {}", testCase.getId(), result.getExecutionOrder());
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
}
