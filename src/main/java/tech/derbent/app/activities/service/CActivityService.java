package tech.derbent.app.activities.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.domains.IHasStatusAndWorkflowService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.services.CProjectItemService;
import tech.derbent.api.services.pageservice.implementations.CPageServiceActivity;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.domain.CActivityPriority;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

@Service
@PreAuthorize ("isAuthenticated()")
public class CActivityService extends CProjectItemService<CActivity> implements IEntityRegistrable {

	private final CActivityPriorityService activityPriorityService;
	private final CActivityTypeService entityTypeService;
	Logger LOGGER = LoggerFactory.getLogger(CActivityService.class);

	public CActivityService(final IActivityRepository repository, final Clock clock, final ISessionService sessionService,
			final CActivityTypeService activityTypeService, final CProjectItemStatusService projectItemStatusService,
			final CActivityPriorityService activityPriorityService) {
		super(repository, clock, sessionService, projectItemStatusService);
		entityTypeService = activityTypeService;
		this.activityPriorityService = activityPriorityService;
	}

	@Override
	public String checkDeleteAllowed(final CActivity activity) {
		return super.checkDeleteAllowed(activity);
	}

	@Override
	public Class<CActivity> getEntityClass() { return CActivity.class; }

	@Override
	public Class<?> getInitializerServiceClass() { // TODO Auto-generated method stub
		return CActivityInitializerService.class;
	}

	@Override
	public Class<?> getPageServiceClass() { // TODO Auto-generated method stub
		return CPageServiceActivity.class;
	}

	@Override
	public Class<?> getServiceClass() { // TODO Auto-generated method stub
		return this.getClass();
	}

	@Override
	public void initializeNewEntity(final CActivity entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new activity entity");
		// Get current project from session
		final CProject currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize activity"));
		// Initialize workflow-based status and type
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, entityTypeService, projectItemStatusService);
		// Initialize activity-specific fields with sensible defaults
		final List<CActivityPriority> priorities = activityPriorityService.listByProject(currentProject);
		Check.notEmpty(priorities, "No activity priorities available in project " + currentProject.getName() + " - cannot initialize new activity");
		entity.setPriority(priorities.get(0));
		LOGGER.debug("Assigned default priority: {}", priorities.get(0).getName());
		// Budget tracking defaults
		entity.setActualCost(BigDecimal.ZERO);
		entity.setEstimatedCost(BigDecimal.ZERO);
		entity.setHourlyRate(BigDecimal.ZERO);
		// Time tracking defaults
		entity.setActualHours(BigDecimal.ZERO);
		entity.setProgressPercentage(0);
		// Date defaults: start today, due in 7 days
		entity.setStartDate(LocalDate.now(clock));
		entity.setDueDate(LocalDate.now(clock).plusDays(7));
		entity.setCompletionDate(null); // Not completed yet
		LOGGER.debug("Activity initialization complete with default values");
	}

	public List<CActivity> listByUser() {
		final CUser currentUser =
				sessionService.getActiveUser().orElseThrow(() -> new CInitializationException("No active user in session - cannot list activities"));
		return ((IActivityRepository) repository).listByUser(currentUser);
	}
}
