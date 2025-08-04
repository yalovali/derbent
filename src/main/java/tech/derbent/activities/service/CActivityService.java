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

import tech.derbent.abstracts.interfaces.CKanbanService;
import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityPriority;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

@Service
@PreAuthorize ("isAuthenticated()")
public class CActivityService extends CEntityOfProjectService<CActivity>
	implements CKanbanService<CActivity, CActivityStatus> {

	public CActivityService(final CActivityRepository repository, final Clock clock) {
		super(repository, clock);
	}

	/**
	 * Helper method to create a placeholder CActivityStatus for activities without a
	 * status.
	 * @param project
	 * @return a CActivityStatus instance representing "No Status"
	 */
	private CActivityStatus createNoStatusInstance(final CProject project) {
		final CActivityStatus noStatus = new CActivityStatus("No Status", project);
		noStatus.setDescription("Activities without an assigned status");
		return noStatus;
	}

	/**
	 * Helper method to create a placeholder CActivityType for activities without a type.
	 * @return a CActivityType instance representing "No Type"
	 */
	private CActivityType createNoTypeInstance(final CProject project) {
		final CActivityType noType = new CActivityType("No Type", project);
		noType.setDescription("Activities without an assigned type");
		return noType;
	}

	/**
	 * Gets all activities for a project grouped by activity status. Activities without a
	 * status are grouped under a "No Status" key.
	 * @param project the project to get activities for
	 * @return map of activity status to list of activities
	 */
	@Transactional (readOnly = true)
	public Map<CActivityStatus, List<CActivity>>
		getActivitiesGroupedByStatus(final CProject project) {
		// Get all activities for the project with type and status loaded
		final List<CActivity> activities =
			((CEntityOfProjectRepository<CActivity>) repository).findByProject(project);
		// Group by activity status, handling null statuses
		return activities.stream()
			.collect(Collectors.groupingBy(activity -> activity.getStatus() != null
				? activity.getStatus() : createNoStatusInstance(project),
				Collectors.toList()));
	}

	/**
	 * Gets all activities for a project grouped by activity type. Activities without a
	 * type are grouped under a "No Type" key.
	 * @param project the project to get activities for
	 * @return map of activity type to list of activities
	 */
	@Transactional (readOnly = true)
	public Map<CActivityType, List<CActivity>>
		getActivitiesGroupedByType(final CProject project) {
		LOGGER.debug("Getting activities grouped by type for project: {}",
			project.getName());
		// Get all activities for the project with type and status loaded
		final List<CActivity> activities =
			((CEntityOfProjectRepository<CActivity>) repository).findByProject(project);
		// Group by activity type, handling null types
		return activities.stream()
			.collect(Collectors.groupingBy(
				activity -> activity.getActivityType() != null
					? activity.getActivityType() : createNoTypeInstance(project),
				Collectors.toList()));
	}

	@Override
	public List<CActivityStatus> getAllStatuses() {
		// This would need to be implemented by calling the status service For minimal
		// changes, returning empty list for now
		return List.of();
	}

	// CKanbanService implementation methods
	@Override
	public Map<CActivityStatus, List<CActivity>>
		getEntitiesGroupedByStatus(final Long projectId) {
		// Find project by ID For now, we'll use the existing method that takes CProject
		// In a real implementation, you'd want to fetch the project by ID This is a
		// simplification for the minimal change approach
		return Map.of(); // This would need proper implementation
	}

	@Override
	protected Class<CActivity> getEntityClass() { return CActivity.class; }

	/**
	 * Initializes lazy fields for CActivity entity to prevent
	 * LazyInitializationException. Specifically handles the lazy-loaded CActivityType and
	 * CActivityStatus relationships.
	 * @param entity the CActivity entity to initialize
	 */
	@Override
	public void initializeLazyFields(final CActivity entity) {

		if (entity == null) {
			return;
		}
		LOGGER.debug("Initializing lazy fields for CActivity with ID: {} entity: {}",
			entity.getId(), entity.getName());

		try {
			super.initializeLazyFields(entity);
			initializeLazyRelationship(entity.getActivityType());
			initializeLazyRelationship(entity.getAssignedTo());
			initializeLazyRelationship(entity.getCreatedBy());
			initializeLazyRelationship(entity.getStatus());
			initializeLazyRelationship(entity.getPriority());
			initializeLazyRelationship(entity.getParentActivity());
			initializeLazyRelationship(entity.getProject());
		} catch (final Exception e) {
			LOGGER.warn("Error initializing lazy fields for CActivity with ID: {}",
				entity.getId(), e);
		}
	}

	/**
	 * Auxiliary method to set activity type and basic properties for an activity.
	 * Following coding guidelines to use service layer methods instead of direct field
	 * setting.
	 * @param activity     the activity to configure
	 * @param activityType the activity type to set
	 * @param description  the description to set
	 * @return the configured activity
	 */
	@Transactional
	public CActivity setActivityType(final CActivity activity,
		final CActivityType activityType, final String description) {

		if (activity == null) {
			LOGGER.warn("Activity is null, cannot set activity type");
			return null;
		}
		activity.setActivityType(activityType);

		if ((description != null) && !description.isEmpty()) {
			activity.setDescription(description);
		}
		return save(activity);
	}

	/**
	 * Auxiliary method to set additional information for an activity.
	 * @param activity           the activity to configure
	 * @param acceptanceCriteria criteria for completion
	 * @param notes              additional notes
	 * @param parentActivity     parent activity for hierarchical structure
	 * @return the configured activity
	 */
	@Transactional
	public CActivity setAdditionalInfo(final CActivity activity,
		final String acceptanceCriteria, final String notes,
		final CActivity parentActivity) {

		if (activity == null) {
			LOGGER.warn("Activity is null, cannot set additional info");
			return null;
		}

		if ((acceptanceCriteria != null) && !acceptanceCriteria.isEmpty()) {
			activity.setAcceptanceCriteria(acceptanceCriteria);
		}

		if ((notes != null) && !notes.isEmpty()) {
			activity.setNotes(notes);
		}

		if (parentActivity != null) {
			activity.setParentActivity(parentActivity);
		}
		return save(activity);
	}

	/**
	 * Auxiliary method to set assigned users and creator for an activity.
	 * @param activity   the activity to configure
	 * @param assignedTo the user assigned to the activity
	 * @param createdBy  the user who created the activity
	 * @return the configured activity
	 */
	@Transactional
	public CActivity setAssignedUsers(final CActivity activity, final CUser assignedTo,
		final CUser createdBy) {

		if (activity == null) {
			LOGGER.warn("Activity is null, cannot set assigned users");
			return null;
		}

		if (assignedTo != null) {
			activity.setAssignedTo(assignedTo);
		}

		if (createdBy != null) {
			activity.setCreatedBy(createdBy);
		}
		return save(activity);
	}

	/**
	 * Auxiliary method to set budget information for an activity.
	 * @param activity      the activity to configure
	 * @param estimatedCost estimated cost for completion
	 * @param actualCost    actual cost spent
	 * @param hourlyRate    hourly rate for cost calculations
	 * @return the configured activity
	 */
	@Transactional
	public CActivity setBudgetInfo(final CActivity activity,
		final BigDecimal estimatedCost, final BigDecimal actualCost,
		final BigDecimal hourlyRate) {

		if (activity == null) {
			LOGGER.warn("Activity is null, cannot set budget info");
			return null;
		}

		if (estimatedCost != null) {
			activity.setEstimatedCost(estimatedCost);
		}

		if (actualCost != null) {
			activity.setActualCost(actualCost);
		}

		if (hourlyRate != null) {
			activity.setHourlyRate(hourlyRate);
		}
		return save(activity);
	}

	/**
	 * Auxiliary method to set date information for an activity.
	 * @param activity       the activity to configure
	 * @param startDate      planned start date
	 * @param dueDate        expected completion date
	 * @param completionDate actual completion date (optional)
	 * @return the configured activity
	 */
	@Transactional
	public CActivity setDateInfo(final CActivity activity, final LocalDate startDate,
		final LocalDate dueDate, final LocalDate completionDate) {

		if (activity == null) {
			LOGGER.warn("Activity is null, cannot set date info");
			return null;
		}

		if (startDate != null) {
			activity.setStartDate(startDate);
		}

		if (dueDate != null) {
			activity.setDueDate(dueDate);
		}

		if (completionDate != null) {
			activity.setCompletionDate(completionDate);
		}
		return save(activity);
	}

	/**
	 * Auxiliary method to set comprehensive activity information in one call. This
	 * demonstrates how auxiliary methods can be combined for complex setup scenarios.
	 * @param activity       the activity to configure
	 * @param activityType   the activity type
	 * @param description    activity description
	 * @param assignedTo     assigned user
	 * @param estimatedHours estimated time
	 * @param startDate      start date
	 * @param dueDate        due date
	 * @return the fully configured activity
	 */
	@Transactional
	public CActivity setFullActivityInfo(final CActivity activity,
		final CActivityType activityType, final String description,
		final CUser assignedTo, final BigDecimal estimatedHours,
		final LocalDate startDate, final LocalDate dueDate) {

		if (activity == null) {
			LOGGER.warn("Activity is null, cannot set full activity info");
			return null;
		}
		// Use other auxiliary methods for comprehensive setup
		setActivityType(activity, activityType, description);
		setAssignedUsers(activity, assignedTo, null);
		setTimeTracking(activity, estimatedHours, null, null);
		setDateInfo(activity, startDate, dueDate, null);
		return activity;
	}

	/**
	 * Auxiliary method to set status, priority and progress for an activity.
	 * @param activity           the activity to configure
	 * @param status             the activity status
	 * @param priority           the activity priority
	 * @param progressPercentage completion percentage (0-100)
	 * @return the configured activity
	 */
	@Transactional
	public CActivity setStatusAndPriority(final CActivity activity,
		final CActivityStatus status, final CActivityPriority priority,
		final Integer progressPercentage) {

		if (activity == null) {
			LOGGER.warn("Activity is null, cannot set status and priority");
			return null;
		}

		if (status != null) {
			activity.setStatus(status);
		}

		if (priority != null) {
			activity.setPriority(priority);
		}

		if (progressPercentage != null) {
			activity.setProgressPercentage(progressPercentage);
		}
		return save(activity);
	}

	/**
	 * Auxiliary method to set time tracking information for an activity.
	 * @param activity       the activity to configure
	 * @param estimatedHours estimated hours for completion
	 * @param actualHours    actual hours spent
	 * @param remainingHours remaining hours to complete
	 * @return the configured activity
	 */
	@Transactional
	public CActivity setTimeTracking(final CActivity activity,
		final BigDecimal estimatedHours, final BigDecimal actualHours,
		final BigDecimal remainingHours) {

		if (activity == null) {
			LOGGER.warn("Activity is null, cannot set time tracking");
			return null;
		}

		if (estimatedHours != null) {
			activity.setEstimatedHours(estimatedHours);
		}

		if (actualHours != null) {
			activity.setActualHours(actualHours);
		}

		if (remainingHours != null) {
			activity.setRemainingHours(remainingHours);
		}
		return save(activity);
	}

	@Override
	public CActivity updateEntityStatus(final CActivity entity,
		final CActivityStatus newStatus) {

		if (entity == null) {
			throw new IllegalArgumentException("Entity cannot be null");
		}

		if (newStatus == null) {
			throw new IllegalArgumentException("New status cannot be null");
		}
		entity.setStatus(newStatus);
		return save(entity);
	}
}
