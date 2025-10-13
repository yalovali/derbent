package tech.derbent.risks.service;

import java.time.Clock;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.risks.domain.CRiskStatus;
import tech.derbent.risks.domain.ERiskSeverity;
import tech.derbent.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (order = 0, icon = "vaadin:clipboard-check", title = "Settings.Risks")
@PermitAll // When security is enabled, allow all authenticated users
public class CRiskService extends CEntityOfProjectService<CRisk> {

	private final CRiskStatusService riskStatusService;

	CRiskService(final IRiskRepository repository, final Clock clock, final ISessionService sessionService,
			final CRiskStatusService riskStatusService) {
		super(repository, clock, sessionService);
		this.riskStatusService = riskStatusService;
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
		// Get current project from session
		final CProject currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize risk"));
		// Initialize risk severity with default value (already set in domain constructor, but ensure it's set)
		if (entity.getRiskSeverity() == null) {
			entity.setRiskSeverity(ERiskSeverity.LOW);
		}
		// Initialize status - get first available risk status for the project (optional field, don't throw if missing)
		final List<CRiskStatus> availableStatuses = riskStatusService.listByProject(currentProject);
		if (!availableStatuses.isEmpty()) {
			entity.setStatus(availableStatuses.get(0));
		}
		// Note: If no status exists, the field will remain null (it's nullable)
	}
}
