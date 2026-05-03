package tech.derbent.plm.agile.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.agile.domain.CFeatureType;

/**
 * Type service for feature hierarchy nodes.
 *
 * <p>Features usually stay parent-capable, but validation still blocks impossible leaf-plus-children
 * combinations when teams customize the generic level model.</p>
 */
@Profile({"derbent", "bab", "default"})
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
			LOGGER.error("Error checking dependencies for feature type: {} reason={}", entity.getName(), e.getMessage());
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

	@Override
	protected void validateEntity(final CFeatureType entity) {
		super.validateEntity(entity);
		Check.notNull(entity.getLevel(), "Hierarchy level is required");
		if (entity.getLevel() < -1) {
			throw new CValidationException("Hierarchy level cannot be less than -1");
		}
		if (entity.getLevel() == -1 && entity.getCanHaveChildren()) {
			throw new CValidationException("Leaf feature types cannot allow children");
		}
	}
}
