package tech.derbent.plm.components.componentversion.service;

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
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.plm.components.componentversion.domain.CProjectComponentVersion;
import tech.derbent.plm.components.componentversiontype.service.CProjectComponentVersionTypeService;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:file-o", title = "Settings.ComponentVersions")
@PermitAll
public class CProjectComponentVersionService extends CProjectItemService<CProjectComponentVersion> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectComponentVersionService.class);
	private final CProjectComponentVersionTypeService componentversionTypeService;

	CProjectComponentVersionService(final IProjectComponentVersionRepository repository, final Clock clock, final ISessionService sessionService,
			final CProjectComponentVersionTypeService componentversionTypeService, final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.componentversionTypeService = componentversionTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CProjectComponentVersion entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<CProjectComponentVersion> getEntityClass() { return CProjectComponentVersion.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProjectComponentVersionInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceComponentVersion.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CProjectComponentVersion entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new componentversion entity");
		final CProject<?> currentProject = sessionService.getActiveProject().orElseThrow(() -> new CInitializationException("No active project"));
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, componentversionTypeService, projectItemStatusService);
		LOGGER.debug("ComponentVersion initialization complete");
	}
}
