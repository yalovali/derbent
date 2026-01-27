package tech.derbent.plm.agile.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.plm.activities.domain.CActivityPriority;
import tech.derbent.plm.activities.service.CActivityPriorityService;
import tech.derbent.plm.agile.domain.CAgileEntity;

@PreAuthorize ("isAuthenticated()")
public abstract class CAgileEntityService<EntityClass extends CAgileEntity<EntityClass, ?>> extends CProjectItemService<EntityClass> {

	private final CActivityPriorityService activityPriorityService;

	protected CAgileEntityService(final tech.derbent.api.entityOfProject.service.IProjectItemRespository<EntityClass> repository, final Clock clock,
			final ISessionService sessionService, final CProjectItemStatusService statusService,
			final CActivityPriorityService activityPriorityService) {
		super(repository, clock, sessionService, statusService);
		this.activityPriorityService = activityPriorityService;
	}

	@Override
	@Transactional
	public void delete(final EntityClass entity) {
		Check.notNull(entity, "Entity cannot be null");
		Check.notNull(entity.getId(), "Entity ID cannot be null");
		super.delete(entity);
	}

	@Override
	@Transactional
	public void delete(final Long id) {
		Check.notNull(id, "Entity ID cannot be null");
		final EntityClass entity =
				repository.findById(id).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Entity not found: " + id));
		delete(entity);
	}

	@Override
	public abstract Optional<EntityClass> findByNameAndProject(String name, CProject<?> project);
	protected abstract tech.derbent.api.entityOfProject.service.IProjectItemRespository<EntityClass> getTypedRepository();
	protected abstract tech.derbent.api.entityOfProject.domain.CTypeEntityService<?> getTypeService();

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		@SuppressWarnings ("unchecked")
		final EntityClass entityCasted = (EntityClass) entity;
		final CProject<?> currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize entity"));
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(),
				getTypeService(), statusService);
		final java.util.List<CActivityPriority> priorities = activityPriorityService.listByCompany(currentProject.getCompany());
		Check.notEmpty(priorities,
				"No activity priorities available in company " + currentProject.getCompany().getName() + " - cannot initialize new entity");
		entityCasted.setPriority(priorities.get(0));
	}

	public java.util.List<EntityClass> listByUser() {
		final CUser currentUser =
				sessionService.getActiveUser().orElseThrow(() -> new CInitializationException("No active user in session - cannot list entities"));
		return ((IAgileRepository<EntityClass>) repository).listByUser(currentUser);
	}

	@Override
	protected void validateEntity(final EntityClass entity) {
		super.validateEntity(entity);
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		validateNumericField(entity.getActualCost(), "Actual Cost", new BigDecimal("999999.99"));
		validateNumericField(entity.getEstimatedCost(), "Estimated Cost", new BigDecimal("999999.99"));
		validateNumericField(entity.getActualHours(), "Actual Hours", new BigDecimal("9999.99"));
		validateNumericField(entity.getEstimatedHours(), "Estimated Hours", new BigDecimal("9999.99"));
		validateNumericField(entity.getHourlyRate(), "Hourly Rate", new BigDecimal("9999.99"));
		validateNumericField(entity.getRemainingHours(), "Remaining Hours", new BigDecimal("9999.99"));
		if (entity.getProgressPercentage() < 0 || entity.getProgressPercentage() > 100) {
			throw new IllegalArgumentException(
					ValidationMessages.formatRange(ValidationMessages.VALUE_RANGE, 0, 100).replace("Value", "Progress percentage"));
		}
		final Optional<EntityClass> existingName = findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
	}

	private void validateNumericField(final BigDecimal value, final String fieldName, final BigDecimal max) {
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
