package tech.derbent.app.testcases.testcase.service;

import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.app.testcases.testcase.domain.CTestCase;
import tech.derbent.app.testcases.testcase.domain.CTestPriority;
import tech.derbent.app.testcases.testcase.domain.CTestSeverity;
import tech.derbent.app.testcases.testscenario.domain.CTestScenario;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize("isAuthenticated()")
@PermitAll
public class CTestCaseService extends CProjectItemService<CTestCase> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CTestCaseService.class);

	CTestCaseService(final ITestCaseRepository repository, final Clock clock, final ISessionService sessionService,
			final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
	}

	@Override
	public String checkDeleteAllowed(final CTestCase testCase) {
		return super.checkDeleteAllowed(testCase);
	}

	@Override
	public Class<CTestCase> getEntityClass() {
		return CTestCase.class;
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
	public void initializeNewEntity(final CTestCase entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new test case entity");
		entity.setPriority(CTestPriority.MEDIUM);
		entity.setSeverity(CTestSeverity.NORMAL);
		LOGGER.debug("Test case initialization complete with defaults: priority=MEDIUM, severity=NORMAL");
	}

	/** Find test cases by scenario.
	 * @param scenario the test scenario
	 * @return list of test cases in the scenario */
	public List<CTestCase> findByScenario(final CTestScenario scenario) {
		Check.notNull(scenario, "Test scenario cannot be null");
		return ((ITestCaseRepository) repository).findByScenario(scenario);
	}

	/** Find test cases by priority.
	 * @param project the project
	 * @param priority the test priority
	 * @return list of test cases with the specified priority */
	public List<CTestCase> findByPriority(final CProject project, final CTestPriority priority) {
		Check.notNull(project, "Project cannot be null");
		Check.notNull(priority, "Priority cannot be null");
		return ((ITestCaseRepository) repository).findByPriority(project, priority);
	}

	/** Find test cases by severity.
	 * @param project the project
	 * @param severity the test severity
	 * @return list of test cases with the specified severity */
	public List<CTestCase> findBySeverity(final CProject project, final CTestSeverity severity) {
		Check.notNull(project, "Project cannot be null");
		Check.notNull(severity, "Severity cannot be null");
		return ((ITestCaseRepository) repository).findBySeverity(project, severity);
	}

	/** Find automated test cases.
	 * @param project the project
	 * @return list of automated test cases */
	public List<CTestCase> findAutomatedTests(final CProject project) {
		Check.notNull(project, "Project cannot be null");
		return ((ITestCaseRepository) repository).findAutomatedTests(project);
	}
}
