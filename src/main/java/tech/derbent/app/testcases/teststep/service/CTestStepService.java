package tech.derbent.app.testcases.teststep.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.Component;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.app.testcases.teststep.domain.CTestStep;
import tech.derbent.app.testcases.teststep.view.CComponentListTestSteps;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize("isAuthenticated()")
@PermitAll
public class CTestStepService extends CAbstractService<CTestStep> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CTestStepService.class);
	private final ISessionService sessionService;

	CTestStepService(final ITestStepRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
		this.sessionService = sessionService;
	}

	@Override
	public Class<CTestStep> getEntityClass() {
		return CTestStep.class;
	}

	public Component createComponentListTestSteps() {
		try {
			final CComponentListTestSteps component = new CComponentListTestSteps(this, sessionService);
			LOGGER.debug("Created test step component");
			return component;
		} catch (final Exception e) {
			LOGGER.error("Failed to create test step component.", e);
			final com.vaadin.flow.component.html.Div errorDiv = new com.vaadin.flow.component.html.Div();
			errorDiv.setText("Error loading test step component: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}
}
