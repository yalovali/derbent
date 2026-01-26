package tech.derbent.plm.agile.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.entityOfProject.service.IProjectItemRespository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.activities.service.CActivityPriorityService;
import tech.derbent.plm.agile.domain.CFeature;

@Service
@PreAuthorize ("isAuthenticated()")
public class CFeatureService extends CAgileEntityService<CFeature> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CFeatureService.class);
	private final CFeatureTypeService typeService;

	public CFeatureService(final IFeatureRepository repository, final Clock clock, final ISessionService sessionService,
			final CFeatureTypeService featureTypeService, final CProjectItemStatusService statusService,
			final CActivityPriorityService activityPriorityService) {
		super(repository, clock, sessionService, statusService, activityPriorityService);
		this.typeService = featureTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CFeature feature) {
		return super.checkDeleteAllowed(feature);
	}

	@Override
	@Transactional
	public void delete(final CFeature feature) {
		super.delete(feature);
	}

	@Override
	@Transactional
	public void delete(final Long id) {
		super.delete(id);
	}

	@Override
	public Class<CFeature> getEntityClass() { return CFeature.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CFeatureInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceFeature.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	protected IProjectItemRespository<CFeature> getTypedRepository() {
		return (IProjectItemRespository<CFeature>) repository;
	}

	@Override
	protected CTypeEntityService<?> getTypeService() {
		return typeService;
	}

	@Override
	protected Optional<CFeature> findByNameAndProject(final String name, final CProject<?> project) {
		return ((IFeatureRepository) repository).findByNameAndProject(name, project);
	}
}
