package tech.derbent.app.risks.service;

import java.time.Clock;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.services.CProjectItemService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.service.CProjectItemStatusService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.risks.domain.CRisk;
import tech.derbent.app.risks.domain.CRiskType;
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
		// Get current project from session
		final CProject currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize activity"));
		final List<CRiskType> availableTypes = riskTypeService.listByProject(currentProject);
		Check.notEmpty(availableTypes, "No activity types available in project " + currentProject.getName() + " - cannot initialize new activity");
		entity.setEntityType(availableTypes.get(0));
		entity.setRiskSeverity(ERiskSeverity.LOW);
	}
}
