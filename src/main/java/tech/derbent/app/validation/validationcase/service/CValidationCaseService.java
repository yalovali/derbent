package tech.derbent.app.validation.validationcase.service;

import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.app.validation.validationcase.domain.CValidationCase;
import tech.derbent.app.validation.validationcase.domain.CValidationPriority;
import tech.derbent.app.validation.validationcase.domain.CValidationSeverity;
import tech.derbent.app.validation.validationcase.view.CComponentListValidationCases;
import tech.derbent.app.validation.validationsuite.domain.CValidationSuite;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize("isAuthenticated()")
@PermitAll
public class CValidationCaseService extends CProjectItemService<CValidationCase> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CValidationCaseService.class);

	CValidationCaseService(final IValidationCaseRepository repository, final Clock clock, final ISessionService sessionService,
			final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
	}

	@Override
	public String checkDeleteAllowed(final CValidationCase validationCase) {
		return super.checkDeleteAllowed(validationCase);
	}

	public Component createComponentListValidationCases() {
		try {
			final ISessionService sessionService = CSpringContext.getBean(ISessionService.class);
			final CComponentListValidationCases component = new CComponentListValidationCases(this, sessionService);
			LOGGER.debug("Created validation case list component");
			return component;
		} catch (final Exception e) {
			LOGGER.error("Failed to create validation case component.", e);
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading validation case component: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}

	@Override
	public Class<CValidationCase> getEntityClass() {
		return CValidationCase.class;
	}

	@Override
	public Class<?> getInitializerServiceClass() {
		return CValidationCaseInitializerService.class;
	}

	@Override
	public Class<?> getPageServiceClass() {
		return CPageServiceValidationCase.class;
	}

	@Override
	public Class<?> getServiceClass() {
		return this.getClass();
	}

	@Override
	public void initializeNewEntity(final CValidationCase entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new validation case entity");
		entity.setPriority(CValidationPriority.MEDIUM);
		entity.setSeverity(CValidationSeverity.NORMAL);
		// EntityType is optional - will be set by user if needed
		// Status will be initialized by parent class from default workflow if entityType is set
		LOGGER.debug("Validation case initialization complete with defaults: priority=MEDIUM, severity=NORMAL");
	}

	/** Find validation cases by scenario.
	 * @param scenario the validation suite
	 * @return list of validation cases in the scenario */
	public List<CValidationCase> findByScenario(final CValidationSuite scenario) {
		Check.notNull(scenario, "Validation suite cannot be null");
		return ((IValidationCaseRepository) repository).findByScenario(scenario);
	}

	/** Find validation cases by priority.
	 * @param project the project
	 * @param priority the test priority
	 * @return list of validation cases with the specified priority */
	public List<CValidationCase> findByPriority(final CProject project, final CValidationPriority priority) {
		Check.notNull(project, "Project cannot be null");
		Check.notNull(priority, "Priority cannot be null");
		return ((IValidationCaseRepository) repository).findByPriority(project, priority);
	}

	/** Find validation cases by severity.
	 * @param project the project
	 * @param severity the test severity
	 * @return list of validation cases with the specified severity */
	public List<CValidationCase> findBySeverity(final CProject project, final CValidationSeverity severity) {
		Check.notNull(project, "Project cannot be null");
		Check.notNull(severity, "Severity cannot be null");
		return ((IValidationCaseRepository) repository).findBySeverity(project, severity);
	}

	/** Find automated validation cases.
	 * @param project the project
	 * @return list of automated validation cases */
	public List<CValidationCase> findAutomatedTests(final CProject project) {
		Check.notNull(project, "Project cannot be null");
		return ((IValidationCaseRepository) repository).findAutomatedTests(project);
	}
}
