package tech.derbent.api.dashboard.dashboardprojecttype.service;

import java.time.Clock;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.dashboard.dashboardprojecttype.domain.CDashboardProjectType;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;

@Profile ({"derbent", "default", "bab", "test"})
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CDashboardProjectTypeService extends CTypeEntityService<CDashboardProjectType> implements IEntityRegistrable, IEntityWithView {

	CDashboardProjectTypeService(final IDashboardProjectTypeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public Class<CDashboardProjectType> getEntityClass() { return CDashboardProjectType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CDashboardProjectTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return tech.derbent.api.entity.domain.CPageServiceDashboardProjectType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	protected void validateEntity(final CDashboardProjectType entity) {
		super.validateEntity(entity);
		validateUniqueNameInCompany((IDashboardProjectTypeRepository) repository, entity, entity.getName(), entity.getCompany());
	}
}
