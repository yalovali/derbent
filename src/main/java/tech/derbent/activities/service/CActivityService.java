package tech.derbent.activities.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityPriority;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.interfaces.IKanbanService;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.api.services.IEntityOfProjectRepository;
import tech.derbent.api.utils.Check;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CActivityService extends CEntityOfProjectService<CActivity> implements IKanbanService<CActivity, CActivityStatus> {

	private final CActivityPriorityService activityPriorityService;
	private final CActivityStatusService activityStatusService;
	private final CActivityTypeService activityTypeService;

	public CActivityService(final IActivityRepository repository, final Clock clock, final ISessionService sessionService,
			final CActivityTypeService activityTypeService, final CActivityStatusService activityStatusService,
			final CActivityPriorityService activityPriorityService) {
		super(repository, clock, sessionService);
		this.activityTypeService = activityTypeService;
		this.activityStatusService = activityStatusService;
		this.activityPriorityService = activityPriorityService;
	}

	@Override
	public String checkDeleteAllowed(final CActivity activity) {
		return super.checkDeleteAllowed(activity);
	}

	/** Helper method to create a placeholder CActivityStatus for activities without a status.
	 * @param project
	 * @return a CActivityStatus instance representing "No Status" */
	private CActivityStatus createNoStatusInstance(final CProject project) {
		final CActivityStatus noStatus = new CActivityStatus("No Status", project);
		noStatus.setDescription("Activities without an assigned status");
		return noStatus;
	}

	@Transactional (readOnly = true)
	public Map<CActivityStatus, List<CActivity>> getActivitiesGroupedByStatus(final CProject project) {
		// Get all activities for the project with type and status loaded
		final List<CActivity> activities = ((IEntityOfProjectRepository<CActivity>) repository).listByProject(project);
		// Group by activity status, handling null statuses
		return activities.stream().collect(Collectors
				.groupingBy(activity -> activity.getStatus() != null ? activity.getStatus() : createNoStatusInstance(project), Collectors.toList()));
	}

	@Override
	public List<CActivityStatus> getAllStatuses(Long projectId) {
		// TODO Auto-generated method stub
		return null;
	}

	// CKanbanService implementation methods
	@Override
	public Map<CActivityStatus, List<CActivity>> getEntitiesGroupedByStatus(final Long projectId) {
		// For now, returning empty as per the original minimal implementation
		// This would need proper implementation based on project requirements
		return tech.derbent.api.utils.CKanbanUtils.getEmptyGroupedStatus(this.getClass());
	}

	@Override
	protected Class<CActivity> getEntityClass() { return CActivity.class; }

	@Override
	public void initializeNewEntity(final CActivity entity) {
		super.initializeNewEntity(entity);
		// Get current project from session
		final CProject currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize activity"));
		final List<CActivityType> availableTypes = activityTypeService.listByProject(currentProject);
		Check.notEmpty(availableTypes, "No activity types available in project " + currentProject.getName() + " - cannot initialize new activity");
		entity.setActivityType(availableTypes.get(0));
		final List<CActivityStatus> availableStatuses = activityStatusService.listByProject(currentProject);
		Check.notEmpty(availableStatuses,
				"No activity statuses available in project " + currentProject.getName() + " - cannot initialize new activity with a status");
		entity.setStatus(availableStatuses.get(0));
		final List<CActivityPriority> priorities = activityPriorityService.listByProject(currentProject);
		Check.notEmpty(priorities, "No activity priorities available in project " + currentProject.getName() + " - cannot initialize new activity");
		entity.setPriority(priorities.get(0));
		entity.setActualCost(BigDecimal.ZERO);
		entity.setEstimatedCost(BigDecimal.ZERO);
		entity.setActualHours(BigDecimal.ZERO);
		entity.setStartDate(LocalDate.now(clock));
		entity.setDueDate(LocalDate.now(clock).plusDays(7));
		entity.setCompletionDate(null); // Not completed yet
		entity.setHourlyRate(BigDecimal.ZERO);
		entity.setProgressPercentage(0);
		entity.setStartDate(LocalDate.now(clock));
	}

	@Override
	public CActivity updateEntityStatus(final CActivity entity, final CActivityStatus newStatus) {
		tech.derbent.api.utils.CKanbanUtils.updateEntityStatusSimple(entity, newStatus, CActivity::setStatus);
		return save(entity);
	}
}
