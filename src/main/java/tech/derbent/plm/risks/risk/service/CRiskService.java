package tech.derbent.plm.risks.risk.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.risks.risk.domain.CRisk;
import tech.derbent.plm.risks.risktype.service.CRiskTypeService;

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
		if (entity.getCause() != null && entity.getCause().length() > 1000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Cause cannot exceed %d characters", 1000));
		}
		if (entity.getImpact() != null && entity.getImpact().length() > 1000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Impact cannot exceed %d characters", 1000));
		}
		if (entity.getResult() != null && entity.getResult().length() > 1000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Result cannot exceed %d characters", 1000));
		}
		if (entity.getMitigation() != null && entity.getMitigation().length() > 2000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Mitigation cannot exceed %d characters", 2000));
		}
		if (entity.getPlan() != null && entity.getPlan().length() > 2000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Plan cannot exceed %d characters", 2000));
		}
		if (entity.getResidualRisk() != null && entity.getResidualRisk().length() > 2000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Residual Risk cannot exceed %d characters", 2000));
		}
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
