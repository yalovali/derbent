package tech.derbent.app.testcases.testrun.service;

import java.time.Clock;
import org.springframework.stereotype.Service;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.app.testcases.testrun.domain.CTestCaseResult;
import tech.derbent.base.session.service.ISessionService;

/** CTestCaseResultService - Service for managing test case results within test runs.
 * Handles CRUD operations for individual test case execution results. */
@Service
public class CTestCaseResultService extends CAbstractService<CTestCaseResult> {

	private final ITestCaseResultRepository repository;

	public CTestCaseResultService(final ITestCaseResultRepository repository, final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
		this.repository = repository;
	}

	@Override
	public Class<CTestCaseResult> getEntityClass() {
		return CTestCaseResult.class;
	}
}
