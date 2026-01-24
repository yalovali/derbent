package tech.derbent.plm.risks.risk.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.risks.risk.domain.CRisk;
import tech.derbent.plm.risks.risktype.service.CRiskTypeService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:clipboard-check", title = "Settings.Risks")
@PermitAll // When security is enabled, allow all authenticated users
public class CRiskService extends CProjectItemService<CRisk> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CRiskService.class);
	private final CRiskTypeService riskTypeService;

	CRiskService(final IRiskRepository repository, final Clock clock, final ISessionService sessionService, final CRiskTypeService riskTypeService,
			final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.riskTypeService = riskTypeService;
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
		final CProject<?> currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize risk"));
		((IHasStatusAndWorkflow<?>) entity).initializeDefaults_IHasStatusAndWorkflow(currentProject, riskTypeService, projectItemStatusService);
	}

	@Override
	protected void validateEntity(final CRisk entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Risk type is required");
		Check.notNull(entity.getRiskSeverity(), "Risk severity is required");
		// 2. Length Checks
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new IllegalArgumentException(
					ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
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
		final Optional<CRisk> existingName = ((IRiskRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
		// 4. Numeric Checks
		if (entity.getImpactScore() != null && (entity.getImpactScore() < 1 || entity.getImpactScore() > 10)) {
			throw new IllegalArgumentException("Impact Score must be between 1 and 10");
		}
		if (entity.getProbability() != null && (entity.getProbability() < 1 || entity.getProbability() > 10)) {
			throw new IllegalArgumentException("Probability must be between 1 and 10");
		}
	}
}
