package tech.derbent.plm.sprints.service;

import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.domain.CSprintItem;

/** CSprintService - Service class for managing sprints. Provides business logic for sprint operations. */
@Profile("derbent")
@Service
@PreAuthorize ("isAuthenticated()")
public class CSprintService extends CProjectItemService<CSprint> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSprintService.class);

	/** Data provider callback: Calculates the total number of items in a sprint. Called automatically by @PostLoad after entity is loaded from
	 * database.
	 * @param sprint the sprint entity to calculate item count for
	 * @return total number of sprint items */
	public static Integer getCalculatedValueOfItemCount(final CSprint sprint) {
		Check.notNull(sprint, "Sprint cannot be null in getCalculatedValueOfItemCount");
		return sprint.getCalculatedValueOfItemCount(); // Delegates to entity method
	}

	public static Long getCalculatedValueOfTotalStoryPoints(final CSprint sprint) {
		Check.notNull(sprint, "Sprint cannot be null in getCalculatedValueOfTotalStoryPoints");
		return sprint.getCalculatedValueOfTotalStoryPoints(); // Delegates to entity method
	}

	private final CSprintItemService sprintItemService;
	private final CSprintTypeService typeService;

	public CSprintService(final ISprintRepository repository, final Clock clock, final ISessionService sessionService,
			final CSprintTypeService sprintTypeService, final CProjectItemStatusService statusService, final CSprintItemService sprintItemService) {
		super(repository, clock, sessionService, statusService);
		typeService = sprintTypeService;
		this.sprintItemService = sprintItemService;
	}

	public void addSprintItemToSprint(final CSprint sprint, final ISprintableItem item) {
		LOGGER.debug("Adding item {} to sprint {}", item, sprint);
		Check.notNull(sprint, "Sprint cannot be null");
		Check.notNull(item, "Item cannot be null");
		// For unsaved sprints, fall back to in-memory wiring; persistence happens when the sprint is saved.
		if (sprint.getId() == null) {
			sprint.addItem(item);
			return;
		}
		// One sprintable item can be in at most one sprint (sprintItem is a single FK).
		final CSprintItem existing = item.getSprintItem();
		Check.notNull(existing, "Sprint item must exist for sprintable item");
		// Update existing sprint item to point to new sprint
		existing.setSprint(sprint);
		existing.setItemOrder(nextOrderForSprint(sprint));
		sprintItemService.save(existing);
	}

	@Override
	public String checkDeleteAllowed(final CSprint sprint) {
		return super.checkDeleteAllowed(sprint);
	}

	/**
	 * Copy CSprint-specific fields from source to target entity.
	 * Uses direct setter/getter calls for clarity.
	 * 
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy
	 */
	@Override
	public void copyEntityFieldsTo(final CSprint source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);

		if (!(target instanceof CSprint targetSprint)) {
			return;
		}

		// Copy basic fields using direct setter/getter
		targetSprint.setColor(source.getColor());
		targetSprint.setDefinitionOfDone(source.getDefinitionOfDone());
		targetSprint.setRetrospectiveNotes(source.getRetrospectiveNotes());
		targetSprint.setSprintGoal(source.getSprintGoal());
		targetSprint.setVelocity(source.getVelocity());

		// Conditional: dates
		if (!options.isResetDates()) {
			targetSprint.setStartDate(source.getStartDate());
			targetSprint.setEndDate(source.getEndDate());
		}

		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}

	/** Deletes a sprint and moves all its items back to the backlog.
	 * <p>
	 * <strong>BACKLOG SEMANTICS:</strong> Sprint items are NOT deleted when a sprint is deleted. Instead, their sprint reference is set to NULL,
	 * which moves them to the backlog.
	 * </p>
	 * <p>
	 * <strong>CRITICAL PATTERN:</strong> This method demonstrates the correct pattern for moving items to backlog:
	 * </p>
	 *
	 * <pre>
	 * sprintItem.setSprint(null); // Move to backlog
	 * sprintItemService.save(sprintItem); // Persist the change
	 * </pre>
	 * <p>
	 * Sprint items are owned by Activity/Meeting with CASCADE.ALL orphanRemoval=true, so deleting them would cause the parent entities to be deleted
	 * as well.
	 * </p>
	 * @param sprint The sprint to delete (must not be null and must be persisted)
	 * @throws IllegalArgumentException if sprint is null or not persisted */
	@Override
	@Transactional
	public void delete(final CSprint sprint) {
		LOGGER.debug("Deleting sprint {}", sprint);
		Check.notNull(sprint, "Sprint cannot be null");
		Check.notNull(sprint.getId(), "Sprint ID cannot be null");
		// Move sprint items to backlog by setting sprint to null
		// CRITICAL: Sprint items are NOT deleted - they are owned by Activity/Meeting
		final List<CSprintItem> sprintItems = sprintItemService.findByMasterIdWithItems(sprint.getId());
		for (final CSprintItem sprintItem : sprintItems) {
			try {
				// CORRECT PATTERN: Set sprint to NULL to move to backlog (do NOT delete)
				sprintItem.setSprint(null);
				sprintItemService.save(sprintItem);
				LOGGER.debug("Moved sprint item {} to backlog (sprint reference set to null)", sprintItem.getId());
			} catch (final Exception e) {
				LOGGER.error("Failed to move sprint item {} to backlog while deleting sprint {}: {}", sprintItem.getId(), sprint.getId(),
						e.getMessage(), e);
				throw e;
			}
		}
		// Now delete the sprint itself (items are safely in backlog)
		super.delete(sprint);
		LOGGER.info("Successfully deleted sprint {} and moved {} items to backlog", sprint.getId(), sprintItems.size());
	}

	@Override
	@Transactional
	public void delete(final Long id) {
		LOGGER.debug("Deleting sprint by ID {}", id);
		Check.notNull(id, "Sprint ID cannot be null");
		final CSprint sprint = getById(id).orElseThrow(() -> new EntityNotFoundException("Sprint not found with id: " + id));
		delete(sprint);
	}

	@Override
	public Class<CSprint> getEntityClass() { return CSprint.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CSprintInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceSprint.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(), typeService,
				statusService);
	}

	private int nextOrderForSprint(final CSprint sprint) {
		if (sprint == null || sprint.getId() == null) {
			return 1;
		}
		return sprintItemService.findByMasterId(sprint.getId()).size() + 1;
	}

	@Override
	protected void validateEntity(final CSprint entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Sprint type is required");
		Check.notNull(entity.getEndDate(), "End Date is required");
		
		// 2. Length Checks - Use validateStringLength helper
		validateStringLength(entity.getColor(), "Color", 7);
		validateStringLength(entity.getDefinitionOfDone(), "Definition of Done", 2000);
		validateStringLength(entity.getDescription(), "Description", 2000);
		validateStringLength(entity.getRetrospectiveNotes(), "Retrospective Notes", 4000);
		validateStringLength(entity.getSprintGoal(), "Sprint Goal", 500);
		
		// 3. Unique Checks
		validateUniqueNameInProject((ISprintRepository) repository, entity, entity.getName(), entity.getProject());
		// 4. Date Logic
		if (entity.getStartDate() != null && entity.getEndDate() != null && entity.getEndDate().isBefore(entity.getStartDate())) {
			throw new IllegalArgumentException("End date cannot be before start date");
		}
	}
}
