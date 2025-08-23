package tech.derbent.risks.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.risks.domain.CRisk;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (order = 0, icon = "vaadin:clipboard-check", title = "Settings.Risks")
@PermitAll // When security is enabled, allow all authenticated users
public class CRiskService extends CEntityOfProjectService<CRisk> {

	CRiskService(final CRiskRepository repository, final Clock clock) {
		super(repository, clock);
	}

	@Override
	protected Class<CRisk> getEntityClass() { return CRisk.class; }

	/** Enhanced initialization of lazy-loaded fields specific to Risk entities. Uses improved null-safe patterns.
	 * @param entity the risk entity to initialize */
	@Override
	public void initializeLazyFields(final CRisk entity) {
		Check.notNull(entity, "Entity cannot be null");
		try {
			super.initializeLazyFields(entity); // Handles CEntityOfProject relationships
												// automatically
			initializeLazyRelationship(entity.getStatus(), "status");
		} catch (final Exception e) {
			LOGGER.warn("Error initializing lazy fields for Risk with ID: {}", entity.getId(), e);
		}
	}
}
