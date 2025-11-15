package tech.derbent.app.orders.currency.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.app.orders.currency.domain.CCurrency;
import tech.derbent.base.session.service.ISessionService;

/** CCurrencyService - Service layer for CCurrency entity. Layer: Service (MVC) Handles business logic for currency operations including creation,
 * validation, and management of currency entities with currency code and symbol support. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CCurrencyService extends CEntityOfProjectService<CCurrency> implements IEntityRegistrable {

	CCurrencyService(final ICurrencyRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CCurrency entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<CCurrency> getEntityClass() { return CCurrency.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CCurrencyInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceCurrency.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CCurrency entity) {
		super.initializeNewEntity(entity);
		// Additional entity-specific initialization can be added here if needed
	}
}
