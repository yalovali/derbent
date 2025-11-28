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
import tech.derbent.app.components.component.domain.CComponent;
import tech.derbent.app.components.componenttype.service.CComponentTypeService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu ( icon = "vaadin:file-o", title = "Settings.Components")
@PermitAll
public class CComponentService extends CProjectItemService<CComponent> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentService.class);
	private final CComponentTypeService componentTypeService;

	CComponentService(final IComponentRepository repository, final Clock clock, final ISessionService sessionService,
			final CComponentTypeService componentTypeService, final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.componentTypeService = componentTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CComponent entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<CComponent> getEntityClass() { return CComponent.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CComponentInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceComponent.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CComponent entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new component entity");
		final CProject currentProject = sessionService.getActiveProject().orElseThrow(() -> new CInitializationException("No active project"));
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, componentTypeService, projectItemStatusService);
		LOGGER.debug("Component initialization complete");
	}
}
