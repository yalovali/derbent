package tech.derbent.risks.service;

import java.time.Clock;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.vaadin.flow.router.Menu;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.risks.domain.CRisk;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (order = 0, icon = "vaadin:clipboard-check", title = "Settings.Risks")
@PermitAll // When security is enabled, allow all authenticated users
public class CRiskService extends CEntityOfProjectService<CRisk> {

	CRiskService(final CRiskRepository repository, final Clock clock) {
		super(repository, clock);
	}
	// Now using the inherited createEntity(String name) method from
	// CEntityOfProjectService. The original createEntity method is replaced by the
	// parent class implementation which includes createEntityForProject method.

	@Override
	protected CRisk createNewEntityInstance() {
		return new CRisk();
	}
}
