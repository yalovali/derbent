package tech.derbent.plm.activities.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.activities.domain.CActivityPriority;

@Service
@PreAuthorize ("isAuthenticated()")
public class CActivityService extends CProjectItemService<CActivity> implements IEntityRegistrable, IEntityWithView {

	private final CActivityPriorityService activityPriorityService;
	Logger LOGGER = LoggerFactory.getLogger(CActivityService.class);
	private final CActivityTypeService typeService;

	public CActivityService(final IActivityRepository repository, final Clock clock, final ISessionService sessionService,
			final CActivityTypeService activityTypeService, final CProjectItemStatusService statusService,
			final CActivityPriorityService activityPriorityService) {
		super(repository, clock, sessionService, statusService);
		typeService = activityTypeService;
		this.activityPriorityService = activityPriorityService;
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

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		final CActivity entityCasted = (CActivity) entity;
		final CProject<?> currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize activity"));
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(), typeService,
				statusService);
		// Initialize priority (Context-aware: depends on Company)
		final List<CActivityPriority> priorities = activityPriorityService.listByCompany(currentProject.getCompany());
		Check.notEmpty(priorities,
				"No activity priorities available in company " + currentProject.getCompany().getName() + " - cannot initialize new activity");
		entityCasted.setPriority(priorities.get(0));
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

	@Override
	protected void validateEntity(final CActivity entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
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
		final boolean condition = entity.getProgressPercentage() != null && (entity.getProgressPercentage() < 0 || entity.getProgressPercentage() > 100);
		if (condition) {
			throw new IllegalArgumentException(
					ValidationMessages.formatRange(ValidationMessages.VALUE_RANGE, 0, 100).replace("Value", "Progress percentage"));
		}
	}

	private void validateNumericField(BigDecimal value, String fieldName, BigDecimal max) {
		if (value == null) {
			return;
		}
		if (value.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException(fieldName + " must be positive");
		}
		if (value.compareTo(max) > 0) {
			throw new IllegalArgumentException(fieldName + " cannot exceed " + max);
		}
	}
}
