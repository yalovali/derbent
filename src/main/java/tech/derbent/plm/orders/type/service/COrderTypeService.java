package tech.derbent.plm.orders.type.service;

import java.time.Clock;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.orders.type.domain.COrderType;

@Profile("derbent")
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class COrderTypeService extends CTypeEntityService<COrderType> implements IEntityRegistrable, IEntityWithView {

	COrderTypeService(final IOrderTypeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Checks dependencies before allowing order type deletion. Always calls super.checkDeleteAllowed() first to ensure all parent-level checks (null
	 * validation, non-deletable flag) are performed.
	 * @param entity the order type entity to check
	 * @return null if type can be deleted, error message otherwise */
	@Override
	public String checkDeleteAllowed(final COrderType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		return null;
	}

	@Override
	public Class<COrderType> getEntityClass() { return COrderType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return COrderTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceOrderType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	@Override
	protected void validateEntity(final COrderType entity) {
		super.validateEntity(entity);
		
		// Unique Name Check - USE STATIC HELPER
		validateUniqueNameInCompany((IOrderTypeRepository) repository, entity, entity.getName(), entity.getCompany());
	}
}
