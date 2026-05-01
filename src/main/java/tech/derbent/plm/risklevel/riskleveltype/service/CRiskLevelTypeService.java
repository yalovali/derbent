package tech.derbent.plm.risklevel.riskleveltype.service;

import java.time.Clock;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entity.domain.CPageServiceRiskLevelType;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.plm.risklevel.riskleveltype.domain.CRiskLevelType;

@Profile ({"derbent", "default"})
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CRiskLevelTypeService extends CTypeEntityService<CRiskLevelType> implements IEntityRegistrable, IEntityWithView {

	CRiskLevelTypeService(final IRiskLevelTypeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public Class<CRiskLevelType> getEntityClass() { return CRiskLevelType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CRiskLevelTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceRiskLevelType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	protected void validateEntity(final CRiskLevelType entity) {
		super.validateEntity(entity);
		validateUniqueNameInCompany((IRiskLevelTypeRepository) repository, entity, entity.getName(), entity.getCompany());
	}
}
