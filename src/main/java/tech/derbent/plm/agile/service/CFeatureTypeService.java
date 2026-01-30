package tech.derbent.plm.agile.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.agile.domain.CFeatureType;

@Profile("derbent")
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CFeatureTypeService extends CTypeEntityService<CFeatureType> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CFeatureTypeService.class);
	private final IFeatureRepository featureRepository;

	public CFeatureTypeService(final IFeatureTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final IFeatureRepository featureRepository) {
		super(repository, clock, sessionService);
		this.featureRepository = featureRepository;
	}

	@Override
	public String checkDeleteAllowed(final CFeatureType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			final long usageCount = featureRepository.countByType(entity);
			if (usageCount > 0) {
				return String.format("Cannot delete. It is being used by %d feature%s.", usageCount, usageCount == 1 ? "" : "s");
			}
			return null;
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for feature type: {}", entity.getName(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	@Override
	public Class<CFeatureType> getEntityClass() { return CFeatureType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CFeatureTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceFeatureType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }
}
