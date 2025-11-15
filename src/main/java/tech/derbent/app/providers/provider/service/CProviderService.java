package tech.derbent.app.providers.provider.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.app.activities.service.CProjectItemStatusService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.providers.provider.domain.CProvider;
import tech.derbent.app.providers.providertype.service.CProviderTypeService;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (order = 0, icon = "vaadin:file-o", title = "Settings.Providers")
@PermitAll
public class CProviderService extends CProjectItemService<CProvider> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProviderService.class);
	private final CProviderTypeService providerTypeService;

	CProviderService(final IProviderRepository repository, final Clock clock, final ISessionService sessionService,
			final CProviderTypeService providerTypeService,
			final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.providerTypeService = providerTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CProvider entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<CProvider> getEntityClass() { return CProvider.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProviderInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProvider.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CProvider entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new provider entity");
		final CProject currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize provider"));
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, providerTypeService, projectItemStatusService);
		LOGGER.debug("Provider initialization complete");
	}
}
