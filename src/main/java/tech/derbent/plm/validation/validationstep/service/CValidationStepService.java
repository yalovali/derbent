package tech.derbent.plm.validation.validationstep.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.plm.validation.validationstep.domain.CValidationStep;
import tech.derbent.plm.validation.validationstep.view.CComponentListValidationSteps;
import tech.derbent.base.session.service.ISessionService;

import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;

@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
public class CValidationStepService extends CAbstractService<CValidationStep> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CValidationStepService.class);

	CValidationStepService(final IValidationStepRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected void validateEntity(final CValidationStep entity) {
		super.validateEntity(entity);
		
		// 1. Required Fields
		Check.notNull(entity.getValidationCase(), "Validation Case is required");
		Check.notNull(entity.getStepOrder(), "Step Order is required");
		
		// 2. Length Checks
		if (entity.getAction() != null && entity.getAction().length() > 2000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Action cannot exceed %d characters", 2000));
		}
		if (entity.getExpectedResult() != null && entity.getExpectedResult().length() > 2000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Expected Result cannot exceed %d characters", 2000));
		}
		if (entity.getNotes() != null && entity.getNotes().length() > 2000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Notes cannot exceed %d characters", 2000));
		}
		if (entity.getTestData() != null && entity.getTestData().length() > 1000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Validation Data cannot exceed %d characters", 1000));
		}
		
		// 3. Numeric Checks
		if (entity.getStepOrder() < 1) {
			throw new IllegalArgumentException("Step Order must be at least 1");
		}
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
