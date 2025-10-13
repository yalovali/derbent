package tech.derbent.risks.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (order = 0, icon = "vaadin:clipboard-check", title = "Settings.Risks")
@PermitAll // When security is enabled, allow all authenticated users
public class CRiskService extends CEntityOfProjectService<CRisk> {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CRiskService.class);

	CRiskService(final IRiskRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected Class<CRisk> getEntityClass() { return CRisk.class; }

	@Override
	public String checkDependencies(final CRisk risk) {
		return super.checkDependencies(risk);
	}

	@Override
	public void initializeNewEntity(final CRisk entity) {
		super.initializeNewEntity(entity);
		// Additional entity-specific initialization can be added here if needed
	}
}
