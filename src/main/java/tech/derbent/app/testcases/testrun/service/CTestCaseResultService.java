package tech.derbent.app.testcases.testrun.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.app.testcases.testrun.domain.CTestCaseResult;
import tech.derbent.app.testcases.testrun.view.CComponentListTestCaseResults;
import tech.derbent.base.session.service.ISessionService;

/** CTestCaseResultService - Service for managing test case results within test runs. Handles CRUD operations for individual test case execution
 * results. */
@Service
public class CTestCaseResultService extends CAbstractService<CTestCaseResult> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CTestCaseResultService.class);
	private final ITestCaseResultRepository repository;

	public CTestCaseResultService(final ITestCaseResultRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
		this.repository = repository;
	}

	public Component createComponentListTestCaseResults() {
		try {
			final ISessionService sessionService = tech.derbent.api.config.CSpringContext.getBean(ISessionService.class);
			final CComponentListTestCaseResults component = new CComponentListTestCaseResults(this, sessionService);
			LOGGER.debug("Created test case result component");
			return component;
		} catch (final Exception e) {
			LOGGER.error("Failed to create test case result component.", e);
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading test case result component: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}

	@Override
	public Class<CTestCaseResult> getEntityClass() { return CTestCaseResult.class; }
}
