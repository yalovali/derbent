package tech.derbent.app.risks.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.app.activities.service.CProjectItemStatusService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.risks.domain.CRisk;
import tech.derbent.app.risks.domain.ERiskSeverity;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (order = 0, icon = "vaadin:clipboard-check", title = "Settings.Risks")
@PermitAll // When security is enabled, allow all authenticated users
public class CRiskService extends CProjectItemService<CRisk> implements IEntityRegistrable {
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
	public void initializeNewEntity(final CRisk entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new risk entity");
		final CProject currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize risk"));
		// Initialize workflow-based status and type
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, riskTypeService, projectItemStatusService);
		// Initialize risk-specific fields with sensible defaults
		entity.setRiskSeverity(ERiskSeverity.LOW); // Default: low severity
		LOGGER.debug("Risk initialization complete with default severity: LOW");
	}
}
