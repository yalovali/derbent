package tech.derbent.plm.agile.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.plm.activities.domain.CActivityPriority;
import tech.derbent.plm.activities.service.CActivityPriorityService;
import tech.derbent.plm.agile.domain.CAgileEntity;
import tech.derbent.plm.links.domain.IHasLinks;

@PreAuthorize ("isAuthenticated()")
public abstract class CAgileEntityService<EntityClass extends CAgileEntity<EntityClass, ?>> extends CProjectItemService<EntityClass> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CAgileEntityService.class);
	private final CActivityPriorityService activityPriorityService;

	protected CAgileEntityService(final tech.derbent.api.entityOfProject.service.IProjectItemRespository<EntityClass> repository, final Clock clock,
			final ISessionService sessionService, final CProjectItemStatusService statusService,
			final CActivityPriorityService activityPriorityService) {
		super(repository, clock, sessionService, statusService);
		this.activityPriorityService = activityPriorityService;
	}

	/** Service-level method to copy CAgileEntity-specific fields. Uses direct setter/getter calls for clarity.
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy */
	@Override
	public void copyEntityFieldsTo(final EntityClass source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		if (!(target instanceof CAgileEntity)) {
			return;
		}
		final CAgileEntity<?, ?> targetAgile = (CAgileEntity<?, ?>) target;
		// Copy string fields
		targetAgile.setAcceptanceCriteria(source.getAcceptanceCriteria());
		targetAgile.setNotes(source.getNotes());
		targetAgile.setResults(source.getResults());
		// Copy numeric fields
		targetAgile.setActualCost(source.getActualCost());
		targetAgile.setActualHours(source.getActualHours());
		targetAgile.setEstimatedCost(source.getEstimatedCost());
		targetAgile.setEstimatedHours(source.getEstimatedHours());
		targetAgile.setHourlyRate(source.getHourlyRate());
		targetAgile.setRemainingHours(source.getRemainingHours());
		targetAgile.setProgressPercentage(source.getProgressPercentage());
		targetAgile.setStoryPoint(source.getStoryPoint());
		targetAgile.setSprintOrder(source.getSprintOrder());
		// Copy priority and type
		targetAgile.setPriority(source.getPriority());
		if (target.getClass().equals(source.getClass())) {
			// Only copy type if same class (Epic->Epic, Feature->Feature, etc.)
			targetAgile.setEntityType(source.getEntityType());
		}
		// Handle dates conditionally
		if (!options.isResetDates()) {
			targetAgile.setDueDate(source.getDueDate());
			targetAgile.setStartDate(source.getStartDate());
			targetAgile.setCompletionDate(source.getCompletionDate());
		}
		// Copy links using IHasLinks interface method
		IHasLinks.copyLinksTo(source, target, options);
		// Note: Comments, attachments, and sprint item are handled by base class
		// Note: agileParentRelation is not cloned - clone starts outside hierarchy
		LOGGER.debug("Copied CAgileEntity '{}' with options: {}", source.getName(), options);
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
		// Unique name check - use base class helper
		validateUniqueNameInProject(getTypedRepository(), entity, entity.getName(), entity.getProject());
	}
}
