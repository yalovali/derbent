package tech.derbent.activities.service;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.domains.CProjectItemService;
import tech.derbent.api.interfaces.IKanbanService;
import tech.derbent.api.services.IEntityOfProjectRepository;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CActivityService extends CProjectItemService<CActivity> implements IKanbanService<CActivity, CActivityStatus> {

	public CActivityService(final IActivityRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Helper method to create a placeholder CActivityStatus for activities without a status.
	 * @param project
	 * @return a CActivityStatus instance representing "No Status" */
	private CActivityStatus createNoStatusInstance(final CProject project) {
		final CActivityStatus noStatus = new CActivityStatus("No Status", project);
		noStatus.setDescription("Activities without an assigned status");
		return noStatus;
	}

	/** Helper method to create a placeholder CActivityType for activities without a type.
	 * @return a CActivityType instance representing "No Type" */
	private CActivityType createNoTypeInstance(final CProject project) {
		final CActivityType noType = new CActivityType("No Type", project);
		noType.setDescription("Activities without an assigned type");
		return noType;
	}

	@Transactional (readOnly = true)
	public Map<CActivityStatus, List<CActivity>> getActivitiesGroupedByStatus(final CProject project) {
		// Get all activities for the project with type and status loaded
		final List<CActivity> activities = ((IEntityOfProjectRepository<CActivity>) repository).listByProject(project);
		// Group by activity status, handling null statuses
		return activities.stream().collect(Collectors
				.groupingBy(activity -> activity.getStatus() != null ? activity.getStatus() : createNoStatusInstance(project), Collectors.toList()));
	}

	@Transactional (readOnly = true)
	public Map<CActivityType, List<CActivity>> getActivitiesGroupedByType(final CProject project) {
		LOGGER.debug("Getting activities grouped by type for project: {}", project.getName());
		// Get all activities for the project with type and status loaded
		final List<CActivity> activities = ((IEntityOfProjectRepository<CActivity>) repository).listByProject(project);
		// Group by activity type, handling null types
		return activities.stream().collect(Collectors.groupingBy(
				activity -> activity.getActivityType() != null ? activity.getActivityType() : createNoTypeInstance(project), Collectors.toList()));
	}

	// CKanbanService implementation methods
	@Override
	public Map<CActivityStatus, List<CActivity>> getEntitiesGroupedByStatus(final Long projectId) {
		// For now, returning empty as per the original minimal implementation
		// This would need proper implementation based on project requirements
		return tech.derbent.api.utils.CKanbanUtils.getEmptyGroupedStatus(this.getClass());
	}

	@Override
	public List<CActivityStatus> getAllStatuses() {
		// This would need to be implemented by calling the status service
		// For minimal changes, using the utility method
		return tech.derbent.api.utils.CKanbanUtils.getEmptyStatusList(this.getClass());
	}

	@Override
	protected Class<CActivity> getEntityClass() { return CActivity.class; }

	@Override
	public CActivity updateEntityStatus(final CActivity entity, final CActivityStatus newStatus) {
		tech.derbent.api.utils.CKanbanUtils.updateEntityStatusSimple(entity, newStatus, CActivity::setStatus);
		return save(entity);
	}
}
