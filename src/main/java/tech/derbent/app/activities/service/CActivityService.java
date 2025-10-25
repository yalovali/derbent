package tech.derbent.app.activities.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.services.CProjectItemService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.domain.CActivityPriority;
import tech.derbent.app.activities.domain.CActivityType;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

@Service
@PreAuthorize ("isAuthenticated()")
public class CActivityService extends CProjectItemService<CActivity> {

	private final CActivityPriorityService activityPriorityService;
	private final CActivityTypeService activityTypeService;

	public CActivityService(final IActivityRepository repository, final Clock clock, final ISessionService sessionService,
			final CActivityTypeService activityTypeService, final CProjectItemStatusService projectItemStatusService,
			final CActivityPriorityService activityPriorityService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.activityTypeService = activityTypeService;
		this.activityPriorityService = activityPriorityService;
	}

	@Override
	public String checkDeleteAllowed(final CActivity activity) {
		return super.checkDeleteAllowed(activity);
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
		entity.setEntityType(availableTypes.get(0));
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

	public List<CActivity> listByUser() {
		CUser currentUser =
				sessionService.getActiveUser().orElseThrow(() -> new CInitializationException("No active user in session - cannot list activities"));
		return ((IActivityRepository) repository).listByUser(currentUser);
	}
}
