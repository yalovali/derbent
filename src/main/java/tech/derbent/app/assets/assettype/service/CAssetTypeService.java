package tech.derbent.app.assets.assettype.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.assets.asset.service.IAssetRepository;
import tech.derbent.app.assets.assettype.domain.CAssetType;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CAssetTypeService extends CTypeEntityService<CAssetType> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CAssetTypeService.class);
	@Autowired
	private IAssetRepository assetRepository;

	public CAssetTypeService(final IAssetTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final IAssetRepository assetRepository) {
		super(repository, clock, sessionService);
		this.assetRepository = assetRepository;
	}

	@Override
	public String checkDeleteAllowed(final CAssetType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			final long usageCount = assetRepository.countByType(entity);
			if (usageCount > 0) {
				return String.format("Cannot delete. It is being used by %d item%s.", usageCount, usageCount == 1 ? "" : "s");
			}
			return null;
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for asset type: {}", entity.getName(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	@Override
	public Class<CAssetType> getEntityClass() { return CAssetType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CAssetTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceAssetType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CAssetType entity) {
		super.initializeNewEntity(entity);
		CProject activeProject = sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project in session"));
		long typeCount = ((IAssetTypeRepository) repository).countByProject(activeProject);
		String autoName = String.format("AssetType %02d", typeCount + 1);
		entity.setName(autoName);
	}
}
