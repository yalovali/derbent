package tech.derbent.app.sprints.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.utils.Check;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.sprints.domain.CSprintItem;

/**
 * CSprintItemDragDropService - Unified service for managing sprint item drag-drop operations.
 * 
 * <p><strong>CRITICAL PATTERN:</strong> Sprint items are NEVER deleted during drag-drop operations.
 * They are owned by their parent entities (CActivity/CMeeting) with CASCADE.ALL orphanRemoval=true.
 * Deleting sprint items directly causes database constraint violations and data loss.</p>
 * 
 * <p><strong>Backlog Semantics:</strong></p>
 * <ul>
 *   <li>Backlog = sprint item with sprint reference set to NULL</li>
 *   <li>In Sprint = sprint item with sprint reference set to a specific CSprint</li>
 *   <li>Moving to backlog = set sprintItem.sprint to NULL (NOT delete)</li>
 *   <li>Moving to sprint = set sprintItem.sprint to target sprint</li>
 * </ul>
 * 
 * <p><strong>Correct Pattern:</strong></p>
 * <pre>
 * // ✅ CORRECT: Modify sprint reference within existing sprint item
 * item.getSprintItem().setSprint(targetSprint);  // Add to sprint
 * item.getSprintItem().setSprint(null);          // Move to backlog
 * 
 * // ❌ WRONG: These patterns violate the ownership model
 * item.setSprintItem(newSprintItem);             // Creates orphaned sprint item
 * sprintItemService.delete(sprintItem);          // Causes cascade delete of parent
 * </pre>
 * 
 * <p><strong>Lifecycle Rules:</strong></p>
 * <ul>
 *   <li>Sprint items are created ONCE when Activity/Meeting is created</li>
 *   <li>Sprint items are deleted ONLY when parent Activity/Meeting is deleted (CASCADE)</li>
 *   <li>Sprint items are NEVER deleted during drag-drop operations</li>
 *   <li>Sprint items are NEVER replaced - only their sprint reference is modified</li>
 * </ul>
 * 
 * @author Derbent Framework
 * @since 1.0
 */
@Service
public class CSprintItemDragDropService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSprintItemDragDropService.class);

	private final CSprintItemService sprintItemService;

	public CSprintItemDragDropService(final CSprintItemService sprintItemService) {
		this.sprintItemService = sprintItemService;
	}

	/**
	 * Moves a sprint item to the backlog by setting its sprint reference to null.
	 * 
	 * <p><strong>CRITICAL:</strong> This method does NOT delete the sprint item.
	 * Sprint items are owned by Activity/Meeting with CASCADE.ALL, so deleting them
	 * would cause the parent entity to be deleted as well (orphanRemoval=true).</p>
	 * 
	 * @param sprintItem The sprint item to move to backlog (must not be null)
	 * @throws IllegalArgumentException if sprintItem is null
	 */
	@Transactional
	public void moveSprintItemToBacklog(final CSprintItem sprintItem) {
		Check.notNull(sprintItem, "Sprint item cannot be null");
		Check.notNull(sprintItem.getId(), "Sprint item must be persisted (ID cannot be null)");

		final Long sourceSprintId = sprintItem.getSprint() != null ? sprintItem.getSprint().getId() : null;
		
		LOGGER.info("[BacklogMove] Moving sprint item {} from sprint {} to backlog", 
			sprintItem.getId(), sourceSprintId);

		// CRITICAL: Set sprint to NULL (backlog semantics), do NOT delete
		sprintItem.setSprint(null);
		
		// Clear kanban column assignment (backlog items don't have kanban columns)
		sprintItem.setKanbanColumnId(null);
		
		// Save the modified sprint item
		sprintItemService.save(sprintItem);
		
		LOGGER.info("[BacklogMove] Sprint item {} successfully moved to backlog (sprint reference set to null)", 
			sprintItem.getId());
	}

	/**
	 * Moves a sprint item to a target sprint.
	 * 
	 * <p>This method updates the sprint reference and assigns a new item order
	 * at the end of the target sprint's item list.</p>
	 * 
	 * @param sprintItem The sprint item to move (must not be null)
	 * @param targetSprint The target sprint (must not be null and must be persisted)
	 * @throws IllegalArgumentException if parameters are null or invalid
	 */
	@Transactional
	public void moveSprintItemToSprint(final CSprintItem sprintItem, final CSprint targetSprint) {
		Check.notNull(sprintItem, "Sprint item cannot be null");
		Check.notNull(sprintItem.getId(), "Sprint item must be persisted (ID cannot be null)");
		Check.notNull(targetSprint, "Target sprint cannot be null");
		Check.notNull(targetSprint.getId(), "Target sprint must be persisted (ID cannot be null)");

		final Long sourceSprintId = sprintItem.getSprint() != null ? sprintItem.getSprint().getId() : null;
		
		LOGGER.info("[SprintMove] Moving sprint item {} from sprint {} to sprint {}", 
			sprintItem.getId(), sourceSprintId, targetSprint.getId());

		// Set sprint reference to target sprint
		sprintItem.setSprint(targetSprint);
		
		// Assign order at end of target sprint
		final Integer nextOrder = sprintItemService.getNextItemOrder(targetSprint);
		sprintItem.setItemOrder(nextOrder);
		
		// Save the modified sprint item
		sprintItemService.save(sprintItem);
		
		LOGGER.info("[SprintMove] Sprint item {} successfully moved to sprint {} with order {}", 
			sprintItem.getId(), targetSprint.getId(), nextOrder);
	}

	/**
	 * Adds a backlog item (sprintable entity) to a sprint.
	 * 
	 * <p><strong>CRITICAL:</strong> This method uses the existing sprint item owned by
	 * the parent entity (Activity/Meeting). It does NOT create a new sprint item.</p>
	 * 
	 * @param item The sprintable item to add (must not be null and must have a sprint item)
	 * @param targetSprint The target sprint (must not be null and must be persisted)
	 * @throws IllegalArgumentException if parameters are null or invalid
	 */
	@Transactional
	public void addBacklogItemToSprint(final ISprintableItem item, final CSprint targetSprint) {
		Check.notNull(item, "Sprintable item cannot be null");
		Check.notNull(targetSprint, "Target sprint cannot be null");
		Check.notNull(targetSprint.getId(), "Target sprint must be persisted (ID cannot be null)");

		// Get the existing sprint item from the parent entity
		final CSprintItem sprintItem = item.getSprintItem();
		Check.notNull(sprintItem, "Sprint item must exist for sprintable item (should be created with parent)");
		
		LOGGER.info("[BacklogToSprint] Adding item {} (sprint item {}) to sprint {}", 
			item.getId(), sprintItem.getId(), targetSprint.getId());

		// Use the existing sprint item and set its sprint reference
		moveSprintItemToSprint(sprintItem, targetSprint);
	}

	/**
	 * Removes a sprintable item from its current sprint and moves it to backlog.
	 * 
	 * <p>This is a convenience method that wraps moveSprintItemToBacklog with
	 * the sprintable item's existing sprint item.</p>
	 * 
	 * @param item The sprintable item to move to backlog (must not be null)
	 * @throws IllegalArgumentException if item is null or has no sprint item
	 */
	@Transactional
	public void removeItemFromSprint(final ISprintableItem item) {
		Check.notNull(item, "Sprintable item cannot be null");
		
		final CSprintItem sprintItem = item.getSprintItem();
		Check.notNull(sprintItem, "Sprint item must exist for sprintable item");
		
		moveSprintItemToBacklog(sprintItem);
	}

	/**
	 * Saves the underlying project item (Activity or Meeting) using its specific service.
	 * 
	 * <p>This is a utility method for saving parent entities after modifying their
	 * sprint item relationships or other properties.</p>
	 * 
	 * @param item The sprintable item to save (must not be null)
	 * @throws IllegalArgumentException if item is null
	 */
	@Transactional
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void saveProjectItem(final ISprintableItem item) {
		Check.notNull(item, "Sprintable item cannot be null");
		Check.instanceOf(item, CProjectItem.class, "Item must be a CProjectItem");
		
		final CProjectItem projectItem = (CProjectItem) item;
		final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(projectItem.getClass());
		Check.notNull(serviceClass, "Service class not found for entity: " + projectItem.getClass().getName());
		
		final CProjectItemService service = (CProjectItemService) CSpringContext.getBean(serviceClass);
		service.save(projectItem);
		
		LOGGER.debug("[Save] Saved project item {} using service {}", projectItem.getId(), serviceClass.getSimpleName());
	}
}
