package tech.derbent.app.testcases.teststep.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.app.testcases.teststep.domain.CTestStep;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize("isAuthenticated()")
@PermitAll
public class CTestStepService extends CAbstractService<CTestStep> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CTestStepService.class);

	CTestStepService(final ITestStepRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public Class<CTestStep> getEntityClass() {
		return CTestStep.class;
	}
}
