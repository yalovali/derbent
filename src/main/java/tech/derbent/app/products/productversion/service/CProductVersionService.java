package tech.derbent.app.products.productversion.service;

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
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.app.products.productversion.domain.CProductVersion;
import tech.derbent.app.products.productversiontype.service.CProductVersionTypeService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:file-o", title = "Settings.ProductVersions")
@PermitAll
public class CProductVersionService extends CProjectItemService<CProductVersion> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProductVersionService.class);
	private final CProductVersionTypeService productversionTypeService;

	CProductVersionService(final IProductVersionRepository repository, final Clock clock, final ISessionService sessionService,
			final CProductVersionTypeService productversionTypeService, final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.productversionTypeService = productversionTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CProductVersion entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<CProductVersion> getEntityClass() { return CProductVersion.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProductVersionInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProductVersion.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CProductVersion entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new productversion entity");
		final CProject currentProject = sessionService.getActiveProject().orElseThrow(() -> new CInitializationException("No active project"));
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, productversionTypeService, projectItemStatusService);
		LOGGER.debug("ProductVersion initialization complete");
	}
}
