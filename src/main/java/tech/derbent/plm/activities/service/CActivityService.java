package tech.derbent.plm.activities.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.agileparentrelation.domain.CAgileParentRelation;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.activities.domain.CActivityPriority;
import tech.derbent.plm.links.domain.IHasLinks;
import tech.derbent.plm.sprints.domain.CSprintItem;

@Service
@Profile ("derbent")
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

	/** Service-level method to copy CActivity-specific fields using getters/setters. This method implements the service-based copy pattern for
	 * Activity entities.
	 * @param source  the source activity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy */
	@Override
	public void copyEntityFieldsTo(final CActivity source, final CEntityDB<?> target, final CCloneOptions options) {
		// Call parent to copy project item fields
		super.copyEntityFieldsTo(source, target, options);
		// Only copy if target is an Activity
		if (!(target instanceof final CActivity targetActivity)) {
			return;
		}
		// Copy basic activity fields - direct setter/getter
		targetActivity.setAcceptanceCriteria(source.getAcceptanceCriteria());
		targetActivity.setNotes(source.getNotes());
		targetActivity.setResults(source.getResults());
		// Copy numeric fields - direct setter/getter
		targetActivity.setActualCost(source.getActualCost());
		targetActivity.setActualHours(source.getActualHours());
		targetActivity.setEstimatedCost(source.getEstimatedCost());
		targetActivity.setEstimatedHours(source.getEstimatedHours());
		targetActivity.setHourlyRate(source.getHourlyRate());
		targetActivity.setRemainingHours(source.getRemainingHours());
		// Copy priority and type - direct setter/getter
		targetActivity.setPriority(source.getPriority());
		targetActivity.setEntityType(source.getEntityType());
		// Handle date fields based on options
		if (!options.isResetDates()) {
			targetActivity.setDueDate(source.getDueDate());
			targetActivity.setStartDate(source.getStartDate());
			targetActivity.setCompletionDate(source.getCompletionDate());
		}
		// Copy links using IHasLinks interface method
		IHasLinks.copyLinksTo(source, target, options);
		// Note: Comments, attachments, and status/workflow are copied automatically by base class
		// Note: Sprint item relationship is not cloned - clone starts outside sprint
		// Note: Widget entity is not cloned - will be created separately if needed
		LOGGER.debug("Successfully copied activity '{}' with options: {}", source.getName(), options);
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
		if (entityCasted.getSprintItem() == null) {
			final CSprintItem sprintItem = new CSprintItem(true);
			sprintItem.setParentItem(entityCasted);
			entityCasted.setSprintItem(sprintItem);
		} else {
			entityCasted.getSprintItem().setParentItem(entityCasted);
		}
		if (entityCasted.getAgileParentRelation() == null) {
			entityCasted.setAgileParentRelation(new CAgileParentRelation(entityCasted));
		} else {
			entityCasted.getAgileParentRelation().setOwnerItem(entityCasted);
		}
		// Initialize priority (Context-aware: depends on Company)
		final List<CActivityPriority> priorities = activityPriorityService.listByCompany(currentProject.getCompany());
		Check.notEmpty(priorities,
				"No activity priorities available in company " + currentProject.getCompany().getName() + " - cannot initialize new activity");
		entityCasted.setPriority(priorities.get(0));
	}

	public List<CActivity> getDataProviderValuesOfUser() {
		final CUser currentUser =
				sessionService.getActiveUser().orElseThrow(() -> new CInitializationException("No active user in session - cannot list activities"));
		return ((IActivityRepository) repository).getDataProviderValuesOfUser(currentUser);
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
		// 3. Unique Checks - use base class helper
		validateUniqueNameInProject((IActivityRepository) repository, entity, entity.getName(), entity.getProject());
		// 4. Numeric Checks
		validateNumericField(entity.getActualCost(), "Actual Cost", new BigDecimal("999999.99"));
		validateNumericField(entity.getEstimatedCost(), "Estimated Cost", new BigDecimal("999999.99"));
		validateNumericField(entity.getActualHours(), "Actual Hours", new BigDecimal("9999.99"));
		validateNumericField(entity.getEstimatedHours(), "Estimated Hours", new BigDecimal("9999.99"));
		validateNumericField(entity.getHourlyRate(), "Hourly Rate", new BigDecimal("9999.99"));
		validateNumericField(entity.getRemainingHours(), "Remaining Hours", new BigDecimal("9999.99"));
		final boolean condition =
				entity.getProgressPercentage() != null && (entity.getProgressPercentage() < 0 || entity.getProgressPercentage() > 100);
		if (condition) {
			throw new IllegalArgumentException(
					ValidationMessages.formatRange(ValidationMessages.VALUE_RANGE, 0, 100).replace("Value", "Progress percentage"));
		}
	}
}
