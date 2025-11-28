package tech.derbent.app.assets.asset.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.app.assets.asset.domain.CAsset;
import tech.derbent.app.assets.assettype.service.CAssetTypeService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu ( icon = "vaadin:file-o", title = "Settings.Assets")
@PermitAll
public class CAssetService extends CProjectItemService<CAsset> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CAssetService.class);
	private final CAssetTypeService assetTypeService;

	CAssetService(final IAssetRepository repository, final Clock clock, final ISessionService sessionService,
			final CAssetTypeService assetTypeService, final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.assetTypeService = assetTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CAsset entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<CAsset> getEntityClass() { return CAsset.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CAssetInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceAsset.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CAsset entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new asset entity");
		final CProject currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize asset"));
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, assetTypeService, projectItemStatusService);
		LOGGER.debug("Asset initialization complete");
	}
}
