package tech.derbent.app.milestones.milestone.service;

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
import tech.derbent.app.milestones.milestone.domain.CMilestone;
import tech.derbent.app.milestones.milestonetype.service.CMilestoneTypeService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:file-o", title = "Settings.Milestones")
@PermitAll
public class CMilestoneService extends CProjectItemService<CMilestone> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CMilestoneService.class);
	private final CMilestoneTypeService milestoneTypeService;

	CMilestoneService(final IMilestoneRepository repository, final Clock clock, final ISessionService sessionService,
			final CMilestoneTypeService milestoneTypeService, final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.milestoneTypeService = milestoneTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CMilestone entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<CMilestone> getEntityClass() { return CMilestone.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CMilestoneInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceMilestone.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CMilestone entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new milestone entity");
		final CProject currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize milestone"));
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, milestoneTypeService, projectItemStatusService);
		LOGGER.debug("Milestone initialization complete");
	}
}
