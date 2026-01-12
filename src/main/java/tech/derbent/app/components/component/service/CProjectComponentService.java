package tech.derbent.app.components.component.service;

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
import tech.derbent.app.components.component.domain.CProjectComponent;
import tech.derbent.app.components.componenttype.service.CProjectComponentTypeService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:file-o", title = "Settings.Components")
@PermitAll
public class CProjectComponentService extends CProjectItemService<CProjectComponent> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectComponentService.class);
	private final CProjectComponentTypeService projectComponentTypeService;

	CProjectComponentService(final IProjectComponentRepository repository, final Clock clock, final ISessionService sessionService,
			final CProjectComponentTypeService projectComponentTypeService, final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.projectComponentTypeService = projectComponentTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CProjectComponent entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<CProjectComponent> getEntityClass() { return CProjectComponent.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProjectComponentInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProjectComponent.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CProjectComponent entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new component entity");
		final CProject currentProject = sessionService.getActiveProject().orElseThrow(() -> new CInitializationException("No active project"));
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, projectComponentTypeService, projectItemStatusService);
		LOGGER.debug("Component initialization complete");
	}
}
