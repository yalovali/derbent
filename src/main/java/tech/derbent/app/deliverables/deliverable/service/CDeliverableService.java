package tech.derbent.app.deliverables.deliverable.service;

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
import tech.derbent.app.deliverables.deliverable.domain.CDeliverable;
import tech.derbent.app.deliverables.deliverabletype.service.CDeliverableTypeService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:file-o", title = "Settings.Deliverables")
@PermitAll
public class CDeliverableService extends CProjectItemService<CDeliverable> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDeliverableService.class);
	private final CDeliverableTypeService deliverableTypeService;

	CDeliverableService(final IDeliverableRepository repository, final Clock clock, final ISessionService sessionService,
			final CDeliverableTypeService deliverableTypeService, final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.deliverableTypeService = deliverableTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CDeliverable entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<CDeliverable> getEntityClass() { return CDeliverable.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CDeliverableInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceDeliverable.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CDeliverable entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new deliverable entity");
		final CProject currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize deliverable"));
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, deliverableTypeService, projectItemStatusService);
		LOGGER.debug("Deliverable initialization complete");
	}
}
