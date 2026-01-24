package tech.derbent.plm.decisions.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.decisions.domain.CDecision;

/** CDecisionService - Service class for CDecision entities. Layer: Service (MVC) Provides business logic operations for decision management including
 * validation, creation, approval workflow management, and project-based queries. */
@Service
@PreAuthorize ("isAuthenticated()")
public class CDecisionService extends CEntityOfProjectService<CDecision> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CDecisionService.class);
	private final CProjectItemStatusService entityStatusService;
	private final CDecisionTypeService entityTypeService;

	public CDecisionService(final IDecisionRepository repository, final Clock clock, final ISessionService sessionService,
			final CDecisionTypeService decisionTypeService, final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService);
		entityTypeService = decisionTypeService;
		entityStatusService = statusService;
	}

	@Override
	public String checkDeleteAllowed(final CDecision decision) {
		return super.checkDeleteAllowed(decision);
	}

	@Override
	public Class<CDecision> getEntityClass() { return CDecision.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CDecisionInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceDecision.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		final CProject<?> currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize decision"));
		((IHasStatusAndWorkflow<?>) entity).initializeDefaults_IHasStatusAndWorkflow(currentProject, entityTypeService, entityStatusService);
	}

	@Override
	protected void validateEntity(final CDecision entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Decision type is required");
		// 2. Length Checks
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new IllegalArgumentException(
					ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
		// 3. Unique Checks
		final Optional<CDecision> existingName = ((IDecisionRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
		// 4. Numeric Checks
		if (entity.getEstimatedCost() != null && entity.getEstimatedCost().compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Estimated cost must be positive");
		}
	}
}
