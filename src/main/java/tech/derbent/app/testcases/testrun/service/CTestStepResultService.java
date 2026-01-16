package tech.derbent.app.testcases.testrun.service;

import java.time.Clock;
import org.springframework.stereotype.Service;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.app.testcases.testrun.domain.CTestStepResult;
import tech.derbent.base.session.service.ISessionService;

/** CTestStepResultService - Service for managing test step execution results.
 * Handles CRUD operations for individual test step results within test case results. */
@Service
public class CTestStepResultService extends CAbstractService<CTestStepResult> {

	private final ITestStepResultRepository repository;

	public CTestStepResultService(final ITestStepResultRepository repository, final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
		this.repository = repository;
	}

	@Override
	public Class<CTestStepResult> getEntityClass() {
		return CTestStepResult.class;
	}
}
