package tech.derbent.plm.orders.currency.service;

import java.time.Clock;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.plm.orders.currency.domain.CCurrency;

/** CCurrencyService - Service layer for CCurrency entity. Layer: Service (MVC) Handles business logic for currency operations including creation,
 * validation, and management of currency entities with currency code and symbol support. */
@Profile("derbent")
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CCurrencyService extends CEntityOfProjectService<CCurrency> implements IEntityRegistrable, IEntityWithView {

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
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	@Override
	protected void validateEntity(final CCurrency entity) {
		super.validateEntity(entity);
		
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		
		// 2. Unique Name Check - USE STATIC HELPER
		validateUniqueNameInProject((ICurrencyRepository) repository, entity, entity.getName(), entity.getProject());
	}
}
