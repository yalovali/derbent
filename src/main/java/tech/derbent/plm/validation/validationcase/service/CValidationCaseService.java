package tech.derbent.plm.validation.validationcase.service;

import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.validation.validationcase.domain.CValidationCase;
import tech.derbent.plm.validation.validationcase.domain.CValidationPriority;
import tech.derbent.plm.validation.validationcase.domain.CValidationSeverity;
import tech.derbent.plm.validation.validationcase.view.CComponentListValidationCases;
import tech.derbent.plm.validation.validationcasetype.service.CValidationCaseTypeService;
import tech.derbent.plm.validation.validationsuite.domain.CValidationSuite;

@Profile("derbent")
@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
public class CValidationCaseService extends CProjectItemService<CValidationCase> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CValidationCaseService.class);
	private final CValidationCaseTypeService typeService;

	CValidationCaseService(final IValidationCaseRepository repository, final Clock clock, final ISessionService sessionService,
			final CProjectItemStatusService statusService, final CValidationCaseTypeService validationCaseTypeService) {
		super(repository, clock, sessionService, statusService);
		typeService = validationCaseTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CValidationCase validationCase) {
		return super.checkDeleteAllowed(validationCase);
	}

	public Component createComponentListValidationCases() {
		try {
			final ISessionService sessionService1 = CSpringContext.getBean(ISessionService.class);
			final CComponentListValidationCases component = new CComponentListValidationCases(this, sessionService1);
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

	/** Find automated validation cases.
	 * @param project the project
	 * @return list of automated validation cases */
	public List<CValidationCase> findAutomatedTests(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IValidationCaseRepository) repository).findAutomatedTests(project);
	}

	/** Find validation cases by priority.
	 * @param project  the project
	 * @param priority the test priority
	 * @return list of validation cases with the specified priority */
	public List<CValidationCase> findByPriority(final CProject<?> project, final CValidationPriority priority) {
		Check.notNull(project, "Project cannot be null");
		Check.notNull(priority, "Priority cannot be null");
		return ((IValidationCaseRepository) repository).findByPriority(project, priority);
	}

	/** Find validation cases by scenario.
	 * @param scenario the validation suite
	 * @return list of validation cases in the scenario */
	public List<CValidationCase> findByScenario(final CValidationSuite scenario) {
		Check.notNull(scenario, "Validation suite cannot be null");
		return ((IValidationCaseRepository) repository).findByScenario(scenario);
	}

	/** Find validation cases by severity.
	 * @param project  the project
	 * @param severity the test severity
	 * @return list of validation cases with the specified severity */
	public List<CValidationCase> findBySeverity(final CProject<?> project, final CValidationSeverity severity) {
		Check.notNull(project, "Project cannot be null");
		Check.notNull(severity, "Severity cannot be null");
		return ((IValidationCaseRepository) repository).findBySeverity(project, severity);
	}

	@Override
	public Class<CValidationCase> getEntityClass() { return CValidationCase.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CValidationCaseInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceValidationCase.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	/**
	 * Copy CValidationCase-specific fields from source to target entity.
	 * Uses direct setter/getter calls for clarity.
	 * 
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy
	 */
	@Override
	public void copyEntityFieldsTo(final CValidationCase source, final CEntityDB<?> target,
			final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		
		if (!(target instanceof CValidationCase)) {
			return;
		}
		final CValidationCase targetCase = (CValidationCase) target;
		
		// Copy basic fields
		targetCase.setAutomated(source.getAutomated());
		targetCase.setAutomatedTestPath(source.getAutomatedTestPath());
		targetCase.setPreconditions(source.getPreconditions());
		targetCase.setPriority(source.getPriority());
		targetCase.setSeverity(source.getSeverity());
		
		// Copy relations conditionally
		if (options.includesRelations()) {
			targetCase.setValidationSuite(source.getValidationSuite());
			
			// Copy collections
			if (source.getValidationSteps() != null) {
				targetCase.setValidationSteps(new java.util.HashSet<>(source.getValidationSteps()));
			}
		}
		
		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(), typeService,
				statusService);
	}

	@Override
	protected void validateEntity(final CValidationCase entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Validation Case Type is required");
		
		// 2. Length Checks - Use validateStringLength helper
		validateStringLength(entity.getAutomatedTestPath(), "Automated Test Path", 500);
		validateStringLength(entity.getPreconditions(), "Preconditions", 2000);
		
		// 3. Unique Checks
		validateUniqueNameInProject((IValidationCaseRepository) repository, entity, entity.getName(), entity.getProject());
	}
}
