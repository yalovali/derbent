package tech.derbent.plm.risks.risk.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.plm.risks.risk.domain.CRisk;
import tech.derbent.plm.risks.risktype.service.CRiskTypeService;

@Profile("derbent")
@Service
@PreAuthorize ("isAuthenticated()")
public class CRiskService extends CProjectItemService<CRisk> implements IEntityRegistrable, IEntityWithView, IHasStatusAndWorkflowService<CRisk> {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CRiskService.class);
	private final CRiskTypeService typeService;

	CRiskService(final IRiskRepository repository, final Clock clock, final ISessionService sessionService, final CRiskTypeService riskTypeService,
			final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, statusService);
		typeService = riskTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CRisk risk) {
		return super.checkDeleteAllowed(risk);
	}

	/**
	 * Copy CRisk-specific fields from source to target entity.
	 * Uses direct setter/getter calls for clarity.
	 * 
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy
	 */
	@Override
	public void copyEntityFieldsTo(final CRisk source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);

		if (!(target instanceof CRisk targetRisk)) {
			return;
		}

		// Copy basic fields using direct setter/getter
		targetRisk.setCause(source.getCause());
		targetRisk.setImpact(source.getImpact());
		targetRisk.setImpactScore(source.getImpactScore());
		targetRisk.setMitigation(source.getMitigation());
		targetRisk.setPlan(source.getPlan());
		targetRisk.setProbability(source.getProbability());
		targetRisk.setResidualRisk(source.getResidualRisk());
		targetRisk.setResult(source.getResult());
		targetRisk.setRiskCriticality(source.getRiskCriticality());
		targetRisk.setRiskLikelihood(source.getRiskLikelihood());
		targetRisk.setRiskResponseStrategy(source.getRiskResponseStrategy());
		targetRisk.setRiskSeverity(source.getRiskSeverity());

		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}

	@Override
	public Class<CRisk> getEntityClass() { return CRisk.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CRiskInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceRisk.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(), typeService,
				statusService);
	}

	@Override
	protected void validateEntity(final CRisk entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Risk type is required");
		Check.notNull(entity.getRiskSeverity(), "Risk severity is required");
		
		// 2. Length Checks - Use validateStringLength helper
		validateStringLength(entity.getCause(), "Cause", 1000);
		validateStringLength(entity.getImpact(), "Impact", 1000);
		validateStringLength(entity.getResult(), "Result", 1000);
		validateStringLength(entity.getMitigation(), "Mitigation", 2000);
		validateStringLength(entity.getPlan(), "Plan", 2000);
		validateStringLength(entity.getResidualRisk(), "Residual Risk", 2000);
		
		// 3. Unique Checks
		validateUniqueNameInProject((IRiskRepository) repository, entity, entity.getName(), entity.getProject());
		// 4. Numeric Checks
		if (entity.getImpactScore() != null && (entity.getImpactScore() < 1 || entity.getImpactScore() > 10)) {
			throw new IllegalArgumentException("Impact Score must be between 1 and 10");
		}
		if (entity.getProbability() != null && (entity.getProbability() < 1 || entity.getProbability() > 10)) {
			throw new IllegalArgumentException("Probability must be between 1 and 10");
		}
	}
}
