package tech.derbent.plm.activities.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.Nonnull;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.activities.domain.CActivityPriority;
import tech.derbent.plm.sprints.domain.CSprintItem;

import tech.derbent.api.validation.ValidationMessages;

@Service
@PreAuthorize ("isAuthenticated()")
public class CActivityService extends CProjectItemService<CActivity> implements IEntityRegistrable, IEntityWithView {

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
	protected void validateEntity(final CActivity entity) {
		super.validateEntity(entity);
		
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		
		// 2. Length Checks
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
		
		// 3. Unique Checks
		// Name must be unique within project
		final Optional<CActivity> existingName = ((IActivityRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
		
		// 4. Numeric Checks
		validateNumericField(entity.getActualCost(), "Actual Cost", new BigDecimal("999999.99"));
		validateNumericField(entity.getEstimatedCost(), "Estimated Cost", new BigDecimal("999999.99"));
		validateNumericField(entity.getActualHours(), "Actual Hours", new BigDecimal("9999.99"));
		validateNumericField(entity.getEstimatedHours(), "Estimated Hours", new BigDecimal("9999.99"));
		validateNumericField(entity.getHourlyRate(), "Hourly Rate", new BigDecimal("9999.99"));
		validateNumericField(entity.getRemainingHours(), "Remaining Hours", new BigDecimal("9999.99"));
		
		if (entity.getProgressPercentage() != null) {
			if (entity.getProgressPercentage() < 0 || entity.getProgressPercentage() > 100) {
				throw new IllegalArgumentException(ValidationMessages.formatRange(ValidationMessages.VALUE_RANGE, 0, 100).replace("Value", "Progress percentage"));
			}
		}
	}
	
	private void validateNumericField(BigDecimal value, String fieldName, BigDecimal max) {
		if (value != null) {
			if (value.compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalArgumentException(fieldName + " must be positive");
			}
			if (value.compareTo(max) > 0) {
				throw new IllegalArgumentException(fieldName + " cannot exceed " + max);
			}
		}
	}

	@Override
	@Transactional
	public void delete(final CActivity activity) {
		Check.notNull(activity, "Activity cannot be null");
		Check.notNull(activity.getId(), "Activity ID cannot be null");
		// The OneToOne relationship with cascade = CascadeType.ALL and orphanRemoval =
		// true
		// will automatically delete the sprint item when the activity is deleted.
		// No need to manually detach and save, which would violate @NotNull constraint.
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

	@Override
	public Class<CActivity> getEntityClass() { return CActivity.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CActivityInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceActivity.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@SuppressWarnings ("null")
	@Override
	public void initializeNewEntity(final CActivity entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new activity entity");
		// Get current project from session
		@Nonnull
		final CProject<?> currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize activity"));
		// Initialize workflow-based status and type
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, entityTypeService, projectItemStatusService);
		// Initialize activity-specific fields with sensible defaults
		final List<CActivityPriority> priorities = activityPriorityService.listByCompany(currentProject.getCompany());
		Check.notEmpty(priorities,
				"No activity priorities available in company " + currentProject.getCompany().getName() + " - cannot initialize new activity");
		entity.setPriority(priorities.get(0));
		LOGGER.debug("Assigned default priority: {}", priorities.get(0).getName());
		// Budget tracking defaults (business event fields stay in CActivity)
		entity.setActualCost(BigDecimal.ZERO);
		entity.setEstimatedCost(BigDecimal.ZERO);
		entity.setHourlyRate(BigDecimal.ZERO);
		entity.setActualHours(BigDecimal.ZERO);
		// Create sprint item for progress tracking (composition pattern)
		// Progress fields (storyPoint, dates, responsible, progress%) live in
		// CSprintItem
		// CRITICAL: This is the ONLY place where setSprintItem() should be called
		// Sprint items are created ONCE during entity initialization and NEVER replaced
		final CSprintItem sprintItem = new CSprintItem();
		sprintItem.setSprint(null); // null = backlog
		sprintItem.setProgressPercentage(0);
		sprintItem.setStartDate(LocalDate.now(clock));
		sprintItem.setDueDate(LocalDate.now(clock).plusDays(7));
		sprintItem.setCompletionDate(null); // Not completed yet
		sprintItem.setStoryPoint(0L);
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
	public List<CActivity> listForProjectBacklog(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IActivityRepository) repository).listForProjectBacklog(project);
	}
}
