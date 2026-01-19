package tech.derbent.plm.validation.validationsession.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.plm.validation.validationsession.domain.CValidationCaseResult;
import tech.derbent.plm.validation.validationsession.view.CComponentListValidationCaseResults;
import tech.derbent.base.session.service.ISessionService;

/** CValidationCaseResultService - Service for managing validation case results within validation sessions. Handles CRUD operations for individual
 * validation case execution results. */
@Service
public class CValidationCaseResultService extends CAbstractService<CValidationCaseResult> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CValidationCaseResultService.class);

	public CValidationCaseResultService(final IValidationCaseResultRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	public Component createComponentListValidationCaseResults() {
		try {
			final ISessionService sessionService1 = CSpringContext.getBean(ISessionService.class);
			final CComponentListValidationCaseResults component = new CComponentListValidationCaseResults(this, sessionService1);
			LOGGER.debug("Created validation case result component");
			return component;
		} catch (final Exception e) {
			LOGGER.error("Failed to create validation case result component.", e);
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading validation case result component: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}

	@Override
	public Class<CValidationCaseResult> getEntityClass() { return CValidationCaseResult.class; }
}
