package tech.derbent.plm.agile.service;

import java.time.Clock;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.agile.domain.CAgileType;

@Profile("derbent")
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CAgileTypeService extends CTypeEntityService<CAgileType> implements IEntityRegistrable, IEntityWithView {

	private final IAgileTypeRepository repositoryTyped;

	public CAgileTypeService(final IAgileTypeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
		this.repositoryTyped = repository;
	}

	@Override
	public Class<CAgileType> getEntityClass() { return CAgileType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CAgileTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceAgileType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		final CCompany activeCompany = sessionService.getActiveCompany().orElseThrow(() -> new IllegalStateException("No active company in session"));
		final long typeCount = repositoryTyped.countByCompany(activeCompany);
		final String autoName = String.format("AgileType %02d", typeCount + 1);
		((CEntityNamed<?>) entity).setName(autoName);
	}

	@Override
	protected void validateEntity(final CAgileType entity) {
		super.validateEntity(entity);
		
		// Unique Name Check - USE STATIC HELPER
		validateUniqueNameInCompany(repositoryTyped, entity, entity.getName(), entity.getCompany());
	}
}
