package tech.derbent.app.validation.validationstep.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.app.validation.validationstep.domain.CValidationStep;
import tech.derbent.app.validation.validationstep.view.CComponentListValidationSteps;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
public class CValidationStepService extends CAbstractService<CValidationStep> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CValidationStepService.class);

	CValidationStepService(final IValidationStepRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	public Component createComponentListValidationSteps() {
		try {
			final CComponentListValidationSteps component = new CComponentListValidationSteps(this, sessionService);
			LOGGER.debug("Created validation step component");
			return component;
		} catch (final Exception e) {
			LOGGER.error("Failed to create validation step component.", e);
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading validation step component: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}

	@Override
	public Class<CValidationStep> getEntityClass() { return CValidationStep.class; }
}
