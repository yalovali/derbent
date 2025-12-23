package tech.derbent.app.sprints.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.utils.Check;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;

/** CSprintService - Service class for managing sprints. Provides business logic for sprint operations. */
@Service
@PreAuthorize ("isAuthenticated()")
public class CSprintService extends CProjectItemService<CSprint> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSprintService.class);

	/** Data provider callback: Calculates the total number of items in a sprint. Called automatically by @PostLoad after entity is loaded from
	 * database.
	 * @param sprint the sprint entity to calculate item count for
	 * @return total number of sprint items */
	public static Integer getItemCount(final CSprint sprint) {
		Check.notNull(sprint, "Sprint cannot be null in getItemCount");
		return sprint.getItemCount(); // Delegates to entity method
	}

	public static Long getTotalStoryPoints(final CSprint sprint) {
		Check.notNull(sprint, "Sprint cannot be null in getTotalStoryPoints");
		return sprint.getTotalStoryPoints(); // Delegates to entity method
	}

	private final CSprintTypeService entityTypeService;
	private final CSprintItemService sprintItemService;

	public CSprintService(final ISprintRepository repository, final Clock clock, final ISessionService sessionService,
			final CSprintTypeService sprintTypeService, final CProjectItemStatusService projectItemStatusService,
			final CSprintItemService sprintItemService) {
		super(repository, clock, sessionService, projectItemStatusService);
		entityTypeService = sprintTypeService;
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
		if (existing != null) {
			// If it's already linked, move it to the new sprint instead of creating a duplicate.
			existing.setSprint(sprint);
			existing.setItemOrder(nextOrderForSprint(sprint));
			sprintItemService.save(existing);
			return;
		}
		final CSprintItem sprintItem = sprintItemService.newSprintItem(sprint, item);
		sprintItemService.save(sprintItem);
	}

	@Override
	public String checkDeleteAllowed(final CSprint sprint) {
		return super.checkDeleteAllowed(sprint);
	}

	@Override
	@Transactional
	public void delete(final CSprint sprint) {
		LOGGER.debug("Deleting sprint {}", sprint);
		Check.notNull(sprint, "Sprint cannot be null");
		Check.notNull(sprint.getId(), "Sprint ID cannot be null");
		// Ensure sprint items are detached from their underlying items before the sprint (and its sprint items) are deleted.
		// Otherwise the FK on sprintable items (sprintitem_id) can block deleting CSprintItem rows.
		final List<CSprintItem> sprintItems = sprintItemService.findByMasterIdWithItems(sprint.getId());
		for (final CSprintItem sprintItem : sprintItems) {
			try {
				sprintItemService.delete(sprintItem);
			} catch (final Exception e) {
				LOGGER.error("Failed to delete sprint item {} while deleting sprint {}: {}", sprintItem.getId(), sprint.getId(), e.getMessage(), e);
				throw e;
			}
		}
		super.delete(sprint);
	}

	@Override
	@Transactional
	public void delete(final Long id) {
		LOGGER.debug("Deleting sprint by ID {}", id);
		Check.notNull(id, "Sprint ID cannot be null");
		final CSprint sprint = getById(id).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Sprint not found with id: " + id));
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
	public void initializeNewEntity(final CSprint entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new sprint entity");
		// Get current project from session
		final CProject currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize sprint"));
		// Initialize workflow-based status and type
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, entityTypeService, projectItemStatusService);
		// Date defaults: start today, end in 2 weeks (standard sprint duration)
		entity.setStartDate(LocalDate.now(clock));
		entity.setEndDate(LocalDate.now(clock).plusWeeks(2));
		entity.setColor(CSprint.DEFAULT_COLOR);
		LOGGER.debug("Sprint initialization complete with default values");
	}

	private int nextOrderForSprint(final CSprint sprint) {
		if (sprint == null || sprint.getId() == null) {
			return 1;
		}
		return sprintItemService.findByMasterId(sprint.getId()).size() + 1;
	}
}
