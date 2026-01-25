package tech.derbent.plm.validation.validationsession.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.plm.validation.validationsession.domain.CValidationStepResult;
import tech.derbent.base.session.service.ISessionService;

import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;

/** CValidationStepResultService - Service for managing validation step execution results. Handles CRUD operations for individual validation step results within test
 * case results. */
@Service
public class CValidationStepResultService extends CAbstractService<CValidationStepResult> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CValidationStepResultService.class);

	public CValidationStepResultService(final IValidationStepResultRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected void validateEntity(final CValidationStepResult entity) {
		super.validateEntity(entity);
		
		// 1. Required Fields
		Check.notNull(entity.getValidationCaseResult(), "Validation Case Result is required");
		Check.notNull(entity.getValidationStep(), "Validation Step is required");
		
		// 2. Length Checks
		if (entity.getActualResult() != null && entity.getActualResult().length() > 2000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Actual Result cannot exceed %d characters", 2000));
		}
		if (entity.getNotes() != null && entity.getNotes().length() > 2000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Notes cannot exceed %d characters", 2000));
		}
		if (entity.getErrorDetails() != null && entity.getErrorDetails().length() > 5000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Error Details cannot exceed %d characters", 5000));
		}
		if (entity.getScreenshotPath() != null && entity.getScreenshotPath().length() > 1000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Screenshot Path cannot exceed %d characters", 1000));
		}
	}

	
	public Component createComponentListValidationStepResults() {
		try {
			final Div container = new Div();
			container.add(new Span("Validation Step Results Component - Under Development"));
			LOGGER.debug("Created validation step result component placeholder");
			return container;
		} catch (final Exception e) {
			LOGGER.error("Failed to create validation step result component.", e);
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading validation step result component: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}

	@Override
	public Class<CValidationStepResult> getEntityClass() { return CValidationStepResult.class; }
}
