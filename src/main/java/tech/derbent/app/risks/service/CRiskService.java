package tech.derbent.app.risks.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.domains.IHasStatusAndWorkflow;
import tech.derbent.api.services.CProjectItemService;
import tech.derbent.app.activities.service.CProjectItemStatusService;
import tech.derbent.app.risks.domain.CRisk;
import tech.derbent.app.risks.domain.ERiskSeverity;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (order = 0, icon = "vaadin:clipboard-check", title = "Settings.Risks")
@PermitAll // When security is enabled, allow all authenticated users
public class CRiskService extends CProjectItemService<CRisk> {

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
	protected Class<CRisk> getEntityClass() { return CRisk.class; }

	@Override
	public void initializeNewEntity(final CRisk entity) {
		super.initializeNewEntity(entity);
		IHasStatusAndWorkflow.initializeNewEntity(entity, sessionService.getActiveProject().get(), riskTypeService, projectItemStatusService);
		entity.setRiskSeverity(ERiskSeverity.LOW);
	}
}
