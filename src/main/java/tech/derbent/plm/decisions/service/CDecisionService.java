package tech.derbent.plm.decisions.service;

import java.math.BigDecimal;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.decisions.domain.CDecision;

/** CDecisionService - Service class for CDecision entities. Layer: Service (MVC) Provides business logic operations for decision management including
 * validation, creation, approval workflow management, and project-based queries. */
@Service
@PreAuthorize ("isAuthenticated()")
public class CDecisionService extends CEntityOfProjectService<CDecision>
		implements IEntityRegistrable, IEntityWithView, IHasStatusAndWorkflowService<CDecision> {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CDecisionService.class);
	private final CProjectItemStatusService statusService;
	private final CDecisionTypeService typeService;

	public CDecisionService(final IDecisionRepository repository, final Clock clock, final ISessionService sessionService,
			final CDecisionTypeService decisionTypeService, final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService);
		typeService = decisionTypeService;
		this.statusService = statusService;
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
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(), typeService,
				statusService);
	}

	@Override
	protected void validateEntity(final CDecision entity) {
		super.validateEntity(entity);
		
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Decision type is required");
		
		// 2. Unique Name Check - USE STATIC HELPER
		validateUniqueNameInProject((IDecisionRepository) repository, entity, entity.getName().trim(), entity.getProject());
		
		// 3. Numeric Check - USE STATIC HELPER
		validateNumericField(entity.getEstimatedCost(), "Estimated cost", new BigDecimal("9999999999.99"));
	}
	
	/** Service-level method to copy CDecision-specific fields using getters/setters.
	 * This method implements the service-based copy pattern for Decision entities.
	 * 
	 * @param source  the source decision to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy */
	@Override
	public void copyEntityFieldsTo(final CDecision source, final tech.derbent.api.entity.domain.CEntityDB<?> target,
			final tech.derbent.api.interfaces.CCloneOptions options) {
		// Call parent to copy project item fields
		super.copyEntityFieldsTo(source, target, options);
		
		// Only copy if target is a Decision
		if (!(target instanceof CDecision)) {
			return;
		}
		final CDecision targetDecision = (CDecision) target;
		
		// Copy basic decision fields using getters/setters
		CEntityDB.copyField(source::getEstimatedCost, targetDecision::setEstimatedCost);
		CEntityDB.copyField(source::getEntityType, targetDecision::setEntityType);
		
		// Conditional: copy dates if not resetting
		if (!options.isResetDates()) {
			CEntityDB.copyField(source::getImplementationDate, targetDecision::setImplementationDate);
			CEntityDB.copyField(source::getReviewDate, targetDecision::setReviewDate);
		}
		
		LOGGER.debug("Successfully copied decision '{}' with options: {}", source.getName(), options);
	}
}
