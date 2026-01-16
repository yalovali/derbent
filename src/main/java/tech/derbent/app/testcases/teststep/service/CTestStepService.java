package tech.derbent.app.testcases.teststep.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
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

	public Component createComponentListTestSteps() {
		try {
			final Div container = new Div();
			container.add(new Span("Test Steps Component - Under Development"));
			LOGGER.debug("Created test step component placeholder");
			return container;
		} catch (final Exception e) {
			LOGGER.error("Failed to create test step component.", e);
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading test step component: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}
}
