package tech.derbent.app.sprints.service;

import java.time.Clock;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;

/**
 * CSprintService - Service class for managing sprints.
 * Provides business logic for sprint operations.
 */
@Service
@PreAuthorize("isAuthenticated()")
public class CSprintService extends CProjectItemService<CSprint> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSprintService.class);
	private final CSprintTypeService entityTypeService;

	public CSprintService(final ISprintRepository repository, final Clock clock, final ISessionService sessionService,
			final CSprintTypeService sprintTypeService, final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.entityTypeService = sprintTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CSprint sprint) {
		return super.checkDeleteAllowed(sprint);
	}

	@Override
	public Class<CSprint> getEntityClass() {
		return CSprint.class;
	}

	@Override
	public Class<?> getInitializerServiceClass() {
		return CSprintInitializerService.class;
	}

	@Override
	public Class<?> getPageServiceClass() {
		return CPageServiceSprint.class;
	}

	@Override
	public Class<?> getServiceClass() {
		return this.getClass();
	}

	@Override
	public void initializeNewEntity(final CSprint entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new sprint entity");

		// Get current project from session
		final CProject currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize sprint"));

		// Initialize workflow-based status and type
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, (tech.derbent.api.entityOfProject.domain.CTypeEntityService<?>) entityTypeService, projectItemStatusService);

		// Date defaults: start today, end in 2 weeks (standard sprint duration)
		entity.setStartDate(LocalDate.now(clock));
		entity.setEndDate(LocalDate.now(clock).plusWeeks(2));
		entity.setColor(CSprint.DEFAULT_COLOR);

		LOGGER.debug("Sprint initialization complete with default values");
	}
}
