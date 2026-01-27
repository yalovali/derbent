package tech.derbent.plm.validation.validationsession.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.validation.validationcase.domain.CValidationCase;
import tech.derbent.plm.validation.validationsession.domain.CValidationCaseResult;
import tech.derbent.plm.validation.validationsession.domain.CValidationResult;
import tech.derbent.plm.validation.validationsession.domain.CValidationSession;
import tech.derbent.plm.validation.validationsession.domain.CValidationStepResult;
import tech.derbent.plm.validation.validationstep.domain.CValidationStep;
import tech.derbent.plm.validation.validationsuite.domain.CValidationSuite;

@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
public class CValidationSessionService extends CEntityOfProjectService<CValidationSession> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CValidationSessionService.class);

	CValidationSessionService(final IValidationSessionRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CValidationSession validationSession) {
		return super.checkDeleteAllowed(validationSession);
	}

	/** Complete a validation session by calculating statistics and setting overall result.
	 * @param validationSession the validation session to complete
	 * @return the completed validation session with calculated statistics */
	public CValidationSession completeValidationSession(final CValidationSession validationSession) {
		Check.notNull(validationSession, "Validation session cannot be null");
		LOGGER.debug("Completing validation session {}", validationSession.getId());
		validationSession.setExecutionEnd(LocalDateTime.now(clock));
		if (validationSession.getExecutionStart() != null && validationSession.getExecutionEnd() != null) {
			final long durationMs = Duration.between(validationSession.getExecutionStart(), validationSession.getExecutionEnd()).toMillis();
			validationSession.setDurationMs(durationMs);
			LOGGER.debug("Validation session duration calculated: {} ms", durationMs);
		}
		int totalCases = 0;
		int passedCases = 0;
		int failedCases = 0;
		int totalSteps = 0;
		int passedSteps = 0;
		int failedSteps = 0;
		for (final CValidationCaseResult caseResult : validationSession.getValidationCaseResults()) {
			totalCases++;
			if (caseResult.getResult() == CValidationResult.PASSED) {
				passedCases++;
			} else if (caseResult.getResult() == CValidationResult.FAILED) {
				failedCases++;
			}
			for (final CValidationStepResult stepResult : caseResult.getValidationStepResults()) {
				totalSteps++;
				if (stepResult.getResult() == CValidationResult.PASSED) {
					passedSteps++;
				} else if (stepResult.getResult() == CValidationResult.FAILED) {
					failedSteps++;
				}
			}
		}
		validationSession.setTotalValidationCases(totalCases);
		validationSession.setPassedValidationCases(passedCases);
		validationSession.setFailedValidationCases(failedCases);
		validationSession.setTotalValidationSteps(totalSteps);
		validationSession.setPassedValidationSteps(passedSteps);
		validationSession.setFailedValidationSteps(failedSteps);
		if (failedCases > 0) {
			validationSession.setResult(passedCases > 0 ? CValidationResult.PARTIAL : CValidationResult.FAILED);
		} else if (passedCases == totalCases && totalCases > 0) {
			validationSession.setResult(CValidationResult.PASSED);
		} else {
			validationSession.setResult(CValidationResult.NOT_EXECUTED);
		}
		LOGGER.debug("Validation session statistics - Total: {}, Passed: {}, Failed: {}, Steps: {}/{}/{}", totalCases, passedCases, failedCases,
				totalSteps, passedSteps, failedSteps);
		LOGGER.debug("Validation session overall result: {}", validationSession.getResult());
		return save(validationSession);
	}

	/** Execute a validation session by initializing validation case results and validation step results for all validation cases in the scenario.
	 * @param validationSession the validation session to execute
	 * @return the validation session with initialized results */
	public CValidationSession executeValidationSession(final CValidationSession validationSession) {
		Check.notNull(validationSession, "Validation session cannot be null");
		final CValidationSuite scenario = validationSession.getValidationSuite();
		Check.notNull(scenario, "Validation session must have a validation suite");
		LOGGER.debug("Executing validation session {} for scenario {}", validationSession.getId(), scenario.getId());
		final Set<CValidationCase> validationCasesSet = scenario.getValidationCases();
		if (validationCasesSet == null || validationCasesSet.isEmpty()) {
			LOGGER.warn("Validation suite {} has no validation cases", scenario.getId());
			return validationSession;
		}
		final List<CValidationCase> validationCases = new java.util.ArrayList<>(validationCasesSet);
		int executionOrder = 1;
		for (final CValidationCase validationCase : validationCases) {
			final CValidationCaseResult caseResult = new CValidationCaseResult(validationSession, validationCase);
			caseResult.setExecutionOrder(executionOrder++);
			caseResult.setResult(CValidationResult.NOT_EXECUTED);
			validationSession.getValidationCaseResults().add(caseResult);
			LOGGER.debug("Initialized validation case result for validation case {} with order {}", validationCase.getId(),
					caseResult.getExecutionOrder());
			final Set<CValidationStep> validationStepsSet = validationCase.getValidationSteps();
			if (validationStepsSet != null && !validationStepsSet.isEmpty()) {
				final List<CValidationStep> validationSteps = new java.util.ArrayList<>(validationStepsSet);
				validationSteps.sort(Comparator.comparing(CValidationStep::getStepOrder));
				for (final CValidationStep validationStep : validationSteps) {
					final CValidationStepResult stepResult = new CValidationStepResult(caseResult, validationStep);
					stepResult.setResult(CValidationResult.NOT_EXECUTED);
					stepResult.setActualResult("");
					caseResult.getValidationStepResults().add(stepResult);
					LOGGER.debug("Initialized validation step result for step {} with order {}", validationStep.getId(),
							validationStep.getStepOrder());
				}
			}
		}
		LOGGER.debug("Validation session execution complete with {} validation case results", validationSession.getValidationCaseResults().size());
		return save(validationSession);
	}

	@Override
	public Class<CValidationSession> getEntityClass() { return CValidationSession.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CValidationSessionInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceValidationSession.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	/** Find validation sessions by scenario.
	 * @param scenario the validation suite
	 * @return list of validation sessions for the scenario */
	public List<CValidationSession> listByScenario(final CValidationSuite scenario) {
		Check.notNull(scenario, "Validation suite cannot be null");
		return ((IValidationSessionRepository) repository).listByScenario(scenario);
	}

	@Override
	protected void validateEntity(final CValidationSession entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getValidationSuite(), "Validation Suite is required");
		if (entity.getExecutionNotes() != null && entity.getExecutionNotes().length() > 5000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Execution Notes cannot exceed %d characters", 5000));
		}
		if (entity.getBuildNumber() != null && entity.getBuildNumber().length() > 100) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Build Number cannot exceed %d characters", 100));
		}
		if (entity.getEnvironment() != null && entity.getEnvironment().length() > 100) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Environment cannot exceed %d characters", 100));
		}
		// 3. Unique Checks
		validateUniqueNameInProject((IValidationSessionRepository) repository, entity, entity.getName(), entity.getProject());
		// 4. Logic Checks
		if (entity.getExecutionStart() != null && entity.getExecutionEnd() != null && entity.getExecutionEnd().isBefore(entity.getExecutionStart())) {
			throw new IllegalArgumentException("Execution End cannot be before Execution Start");
		}
	}
}
