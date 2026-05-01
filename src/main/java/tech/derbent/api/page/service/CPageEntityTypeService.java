package tech.derbent.api.page.service;

import java.time.Clock;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entity.domain.CPageServicePageEntityType;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.page.domain.CPageEntityType;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;

@Profile ({"derbent", "default"})
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CPageEntityTypeService extends CTypeEntityService<CPageEntityType> implements IEntityRegistrable, IEntityWithView {

	CPageEntityTypeService(final IPageEntityTypeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public Class<CPageEntityType> getEntityClass() { return CPageEntityType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CPageEntityTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServicePageEntityType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	protected void validateEntity(final CPageEntityType entity) {
		super.validateEntity(entity);
		validateUniqueNameInCompany((IPageEntityTypeRepository) repository, entity, entity.getName(), entity.getCompany());
	}
}
