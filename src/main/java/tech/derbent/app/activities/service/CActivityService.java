package tech.derbent.app.activities.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.domain.CActivityPriority;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.app.sprints.service.CSprintItemService;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

@Service
@PreAuthorize ("isAuthenticated()")
public class CActivityService extends CProjectItemService<CActivity> implements IEntityRegistrable, IEntityWithView {

	private final CActivityPriorityService activityPriorityService;
	private final CActivityTypeService entityTypeService;
	Logger LOGGER = LoggerFactory.getLogger(CActivityService.class);
	private final CSprintItemService sprintItemService;

	public CActivityService(final IActivityRepository repository, final Clock clock, final ISessionService sessionService,
			final CActivityTypeService activityTypeService, final CProjectItemStatusService projectItemStatusService,
			final CActivityPriorityService activityPriorityService, final CSprintItemService sprintItemService) {
		super(repository, clock, sessionService, projectItemStatusService);
		entityTypeService = activityTypeService;
		this.activityPriorityService = activityPriorityService;
		this.sprintItemService = sprintItemService;
	}

	@Override
	public String checkDeleteAllowed(final CActivity activity) {
		return super.checkDeleteAllowed(activity);
	}

	@Override
	@Transactional
	public void delete(final CActivity activity) {
		Check.notNull(activity, "Activity cannot be null");
		Check.notNull(activity.getId(), "Activity ID cannot be null");
		detachSprintItemIfPresent(activity);
		super.delete(activity);
	}

	@Override
	@Transactional
	public void delete(final Long id) {
		Check.notNull(id, "Activity ID cannot be null");
		final CActivity activity =
				repository.findById(id).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Activity not found: " + id));
		delete(activity);
	}

	private void detachSprintItemIfPresent(final CActivity activity) {
		final CSprintItem sprintItem = activity.getSprintItem();
		if (sprintItem == null || sprintItem.getId() == null) {
			return;
		}
		activity.setSprintItem(null);
		repository.saveAndFlush(activity);
		sprintItemService.delete(sprintItem.getId());
	}

	@Override
	public Class<CActivity> getEntityClass() { return CActivity.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CActivityInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceActivity.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CActivity entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new activity entity");
		// Get current project from session
		final CProject currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize activity"));
		final CUser currentUser = sessionService.getActiveUser()
				.orElseThrow(() -> new CInitializationException("No active user in session - cannot initialize activity"));
		
		// Initialize workflow-based status and type
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, entityTypeService, projectItemStatusService);
		
		// Initialize activity-specific fields with sensible defaults
		final List<CActivityPriority> priorities = activityPriorityService.listByProject(currentProject);
		Check.notEmpty(priorities, "No activity priorities available in project " + currentProject.getName() + " - cannot initialize new activity");
		entity.setPriority(priorities.get(0));
		LOGGER.debug("Assigned default priority: {}", priorities.get(0).getName());
		
		// Budget tracking defaults (business event fields stay in CActivity)
		entity.setActualCost(BigDecimal.ZERO);
		entity.setEstimatedCost(BigDecimal.ZERO);
		entity.setHourlyRate(BigDecimal.ZERO);
		entity.setActualHours(BigDecimal.ZERO);
		
		// Create sprint item for progress tracking (composition pattern)
		// Progress fields (storyPoint, dates, responsible, progress%) live in CSprintItem
		final CSprintItem sprintItem = new CSprintItem();
		sprintItem.setSprint(null); // null = backlog
		sprintItem.setProgressPercentage(0);
		sprintItem.setStartDate(LocalDate.now(clock));
		sprintItem.setDueDate(LocalDate.now(clock).plusDays(7));
		sprintItem.setCompletionDate(null); // Not completed yet
		sprintItem.setStoryPoint(0L);
		sprintItem.setResponsible(currentUser);
		sprintItem.setItemOrder(1); // Default order
		entity.setSprintItem(sprintItem);
		
		LOGGER.debug("Activity initialization complete with sprint item for progress tracking");
	}

	public List<CActivity> listByUser() {
		final CUser currentUser =
				sessionService.getActiveUser().orElseThrow(() -> new CInitializationException("No active user in session - cannot list activities"));
		return ((IActivityRepository) repository).listByUser(currentUser);
	}

	/** Lists activities by project ordered by sprintOrder for sprint-aware components. Items with null sprintOrder will appear last.
	 * @param project the project
	 * @return list of activities ordered by sprintOrder ASC, id DESC */
	public List<CActivity> listForProjectBacklog(final CProject project) {
		Check.notNull(project, "Project cannot be null");
		return ((IActivityRepository) repository).listForProjectBacklog(project);
	}
}
